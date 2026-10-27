package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GoogleSheetsSyncResult(
    val success: Boolean,
    val message: String,
    val syncedCardsCount: Int = 0,
    val syncedTransactionsCount: Int = 0
)

data class GoogleSheetsRestoreResult(
    val success: Boolean,
    val message: String,
    val restoredCardsCount: Int = 0,
    val restoredTransactionsCount: Int = 0,
    val username: String? = null,
    val passwordHash: String? = null,
    val pinHash: String? = null
)

class GoogleSheetsSyncManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context)
) {

    companion object {
        private const val TAG = "GoogleSheetsSync"
        const val WEB_APP_URL = "https://script.google.com/macros/s/AKfycbyKw6WzW2Ia-ckpCHwOxBn4tDJCWE8dedh8SMTQ9v9h0IWzny7N86e2fZxXjWXut7tM/exec"

        private val client: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .build()
        }

        fun normalizePhone(phone: String): String {
            val clean = phone.replace("+", "").replace(" ", "").trim()
            val mapped = clean.map { ch ->
                when (ch) {
                    in '۰'..'۹' -> '0' + (ch - '۰')
                    in '٠'..'٩' -> '0' + (ch - '٠')
                    else -> ch
                }
            }.joinToString("")
            return when {
                mapped.startsWith("0") -> mapped
                mapped.startsWith("98") -> "0" + mapped.substring(2)
                else -> "0$mapped"
            }
        }
    }

    /**
     * Send POST request to WEB_APP_URL with action: "sync"
     * Content-Type must be 'text/plain;charset=utf-8' as required by Google Apps Script
     */
    suspend fun syncData(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        username: String? = null,
        passwordHash: String? = null,
        pinHash: String? = null
    ): Result<GoogleSheetsSyncResult> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPhone = normalizePhone(phoneNumber)
            val authPrefs = context.getSharedPreferences("smart_wallet_auth_prefs", Context.MODE_PRIVATE)
            val finalUsername = (username?.trim()?.takeIf { it.isNotEmpty() }
                ?: authPrefs.getString("username", "") ?: "").trim()
            val finalPasswordHash = (passwordHash?.takeIf { it.isNotEmpty() }
                ?: authPrefs.getString("password_hash", "") ?: "")
            val finalPinHash = (pinHash?.takeIf { it.isNotEmpty() }
                ?: authPrefs.getString("pin_hash", "") ?: "")

            val accounts = database.bankAccountDao().getAllBankAccountsList()
            val transactions = database.transactionDao().getAllTransactionsSync()

            val rootJson = JSONObject().apply {
                put("action", "sync")
                put("phoneNumber", cleanPhone)
                put("firstName", firstName.trim())
                put("lastName", lastName.trim())
                put("username", finalUsername)
                put("passwordHash", finalPasswordHash)
                put("pinHash", finalPinHash)

                put("user", JSONObject().apply {
                    put("phoneNumber", cleanPhone)
                    put("firstName", firstName.trim())
                    put("lastName", lastName.trim())
                    put("username", finalUsername)
                    put("passwordHash", finalPasswordHash)
                    put("pinHash", finalPinHash)
                })

                val cardsArray = JSONArray()
                accounts.forEach { acc ->
                    cardsArray.put(JSONObject().apply {
                        put("id", acc.id)
                        put("bankName", acc.bankName)
                        put("accountName", acc.accountName)
                        put("cardNumber", acc.cardNumber)
                        put("accountNumber", acc.accountNumber ?: "")
                        put("balanceToman", acc.balanceToman)
                        put("cardColorHex", acc.cardColorHex)
                        put("smsShortcode", acc.smsShortcode ?: "")
                        put("accountType", acc.accountType)
                        put("readSmsEnabled", acc.readSmsEnabled)
                        put("initialBalanceEnabled", acc.initialBalanceEnabled)
                    })
                }
                put("cards", cardsArray)

                val txArray = JSONArray()
                transactions.forEach { tx ->
                    txArray.put(JSONObject().apply {
                        put("id", tx.id)
                        put("amountToman", tx.amountToman)
                        put("type", tx.type.name)
                        put("categoryName", tx.categoryName)
                        put("merchantName", tx.merchantName)
                        put("cardNumber", tx.cardNumber ?: "")
                        put("dateMillis", tx.dateMillis)
                        put("rawSmsText", tx.rawSmsText ?: "")
                        put("isParsedFromSms", tx.isParsedFromSms)
                        put("status", tx.status.name)
                        put("notes", tx.notes ?: "")
                        put("bankName", tx.bankName ?: "")
                        put("balanceAfterToman", tx.balanceAfterToman ?: -1L)
                        put("senderNumber", tx.senderNumber ?: "")
                    })
                }
                put("transactions", txArray)
            }

            val mediaType = "text/plain; charset=utf-8".toMediaTypeOrNull()
            val body = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(WEB_APP_URL)
                .post(body)
                .header("Content-Type", "text/plain;charset=utf-8")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            Log.d(TAG, "Google Sheets Sync Response code: ${response.code}, body: $responseBody")

            if (!response.isSuccessful && response.code != 302) {
                throw IllegalStateException("خطا در ارتباط با سرور فضای ابری: کد ${response.code}")
            }

            GoogleSheetsSyncResult(
                success = true,
                message = "همگام‌سازی با فضای ابری با موفقیت انجام شد.",
                syncedCardsCount = accounts.size,
                syncedTransactionsCount = transactions.size
            )
        }
    }

    /**
     * Send POST request to WEB_APP_URL with action: "restore"
     * Re-populates local Room DB with returned cards and transactions
     */
    suspend fun restoreData(
        firstName: String,
        lastName: String,
        phoneNumber: String
    ): Result<GoogleSheetsRestoreResult> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPhone = normalizePhone(phoneNumber)

            val rootJson = JSONObject().apply {
                put("action", "restore")
                put("phoneNumber", cleanPhone)
                put("firstName", firstName.trim())
                put("lastName", lastName.trim())
            }

            val mediaType = "text/plain; charset=utf-8".toMediaTypeOrNull()
            val body = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(WEB_APP_URL)
                .post(body)
                .header("Content-Type", "text/plain;charset=utf-8")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            Log.d(TAG, "Google Sheets Restore Response code: ${response.code}, body: $responseBody")

            if (!response.isSuccessful && response.code != 302) {
                throw IllegalStateException("خطا در ارتباط با فضای ابری: کد ${response.code}")
            }

            if (responseBody.isBlank()) {
                throw IllegalStateException("پاسخ دریافتی از سرور خالی است")
            }

            val json = JSONObject(responseBody)
            val userObj = json.optJSONObject("user")
            val restoredUsername = userObj?.optString("username")?.takeIf { it.isNotBlank() }
                ?: json.optString("username").takeIf { it.isNotBlank() }
            val restoredPasswordHash = userObj?.optString("passwordHash")?.takeIf { it.isNotBlank() }
                ?: json.optString("passwordHash").takeIf { it.isNotBlank() }
            val restoredPinHash = userObj?.optString("pinHash")?.takeIf { it.isNotBlank() }
                ?: json.optString("pinHash").takeIf { it.isNotBlank() }

            val cardsArray = json.optJSONArray("cards") ?: JSONArray()
            val txArray = json.optJSONArray("transactions") ?: JSONArray()

            var restoredCards = 0
            var restoredTxs = 0

            // 1. Restore Bank Accounts
            val existingCards = database.bankAccountDao().getAllBankAccountsList()
            for (i in 0 until cardsArray.length()) {
                val obj = cardsArray.getJSONObject(i)
                val bankName = obj.optString("bankName", "بانک")
                val accountName = obj.optString("accountName", "حساب اصلی")
                val cardNumber = obj.optString("cardNumber", "")
                val accountNumber = if (obj.has("accountNumber")) obj.optString("accountNumber").takeIf { it.isNotBlank() } else null
                val balanceToman = obj.optLong("balanceToman", 0L)
                val cardColorHex = obj.optString("cardColorHex", "#1E88E5")
                val smsShortcode = if (obj.has("smsShortcode")) obj.optString("smsShortcode").takeIf { it.isNotBlank() } else null
                val accountType = obj.optString("accountType", "BANK_CARD")
                val readSmsEnabled = obj.optBoolean("readSmsEnabled", true)
                val initialBalanceEnabled = obj.optBoolean("initialBalanceEnabled", true)

                val matched = existingCards.firstOrNull {
                    (it.cardNumber == cardNumber && cardNumber.isNotBlank()) ||
                    (!accountNumber.isNullOrBlank() && it.accountNumber == accountNumber)
                }

                if (matched == null) {
                    database.bankAccountDao().insert(
                        BankAccount(
                            bankName = bankName,
                            accountName = accountName,
                            cardNumber = cardNumber,
                            accountNumber = accountNumber,
                            balanceToman = balanceToman,
                            cardColorHex = cardColorHex,
                            smsShortcode = smsShortcode,
                            accountType = accountType,
                            readSmsEnabled = readSmsEnabled,
                            initialBalanceEnabled = initialBalanceEnabled
                        )
                    )
                    restoredCards++
                } else {
                    // Update existing account balance with latest cloud balance
                    database.bankAccountDao().update(
                        matched.copy(
                            bankName = bankName,
                            accountName = accountName,
                            accountNumber = accountNumber ?: matched.accountNumber,
                            balanceToman = balanceToman
                        )
                    )
                    restoredCards++
                }
            }

            // 2. Restore Transactions
            val existingTxs = database.transactionDao().getAllTransactionsSync()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                val amountToman = obj.optLong("amountToman", 0L)
                val typeStr = obj.optString("type", "EXPENSE")
                val type = try { TransactionType.valueOf(typeStr) } catch (e: Exception) { TransactionType.EXPENSE }
                val categoryName = obj.optString("categoryName", "متفرقه")
                val merchantName = obj.optString("merchantName", "تراکنش")
                val cardNumber = if (obj.has("cardNumber")) obj.optString("cardNumber").takeIf { it.isNotBlank() } else null
                val dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
                val rawSmsText = if (obj.has("rawSmsText")) obj.optString("rawSmsText").takeIf { it.isNotBlank() } else null
                val isParsedFromSms = obj.optBoolean("isParsedFromSms", false)
                val statusStr = obj.optString("status", "CONFIRMED")
                val status = try { TransactionStatus.valueOf(statusStr) } catch (e: Exception) { TransactionStatus.CONFIRMED }
                val notes = if (obj.has("notes")) obj.optString("notes").takeIf { it.isNotBlank() } else null
                val bankName = if (obj.has("bankName")) obj.optString("bankName").takeIf { it.isNotBlank() } else null
                val balanceAfter = if (obj.has("balanceAfterToman")) obj.optLong("balanceAfterToman") else null

                // Avoid duplicate transactions (same dateMillis and amount)
                val duplicate = existingTxs.any {
                    it.dateMillis == dateMillis && it.amountToman == amountToman && it.type == type
                }

                if (!duplicate) {
                    database.transactionDao().insert(
                        Transaction(
                            amountToman = amountToman,
                            type = type,
                            categoryName = categoryName,
                            merchantName = merchantName,
                            cardNumber = cardNumber,
                            dateMillis = dateMillis,
                            rawSmsText = rawSmsText,
                            isParsedFromSms = isParsedFromSms,
                            status = status,
                            notes = notes,
                            bankName = bankName,
                            balanceAfterToman = balanceAfter
                        )
                    )
                    restoredTxs++
                }
            }

            GoogleSheetsRestoreResult(
                success = true,
                message = "اطلاعات با موفقیت از فضای ابری بازیابی شد!\nتعداد $restoredCards کارت و $restoredTxs تراکنش بازگردانی شدند.",
                restoredCardsCount = restoredCards,
                restoredTransactionsCount = restoredTxs,
                username = restoredUsername,
                passwordHash = restoredPasswordHash,
                pinHash = restoredPinHash
            )
        }
    }
}
