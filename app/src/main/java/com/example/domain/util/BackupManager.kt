package com.example.domain.util

import android.content.Context
import android.net.Uri
import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12

    private fun generateKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    suspend fun exportEncryptedBackup(context: Context, uri: Uri, password: String) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val transactions = db.transactionDao().getAllTransactionsSync()
        val categories = db.categoryDao().getAllCategoriesSync()
        val budgets = db.budgetDao().getAllBudgetsSync()
        val assets = db.assetDao().getAllAssetsSync()
        val parsedSms = db.parsedSmsDao().getAllParsedSmsSync()
        
        val json = JSONObject().apply {
            // 1. Transactions
            val txArray = JSONArray()
            transactions.forEach { t ->
                txArray.put(JSONObject().apply {
                    put("amountToman", t.amountToman)
                    put("type", t.type.name)
                    put("categoryName", t.categoryName)
                    put("merchantName", t.merchantName)
                    put("dateMillis", t.dateMillis)
                    put("cardNumber", t.cardNumber ?: "")
                    put("rawSmsText", t.rawSmsText ?: "")
                    put("isParsedFromSms", t.isParsedFromSms)
                    put("status", t.status.name)
                    put("notes", t.notes ?: "")
                    put("bankName", t.bankName ?: "")
                    put("balanceAfterToman", t.balanceAfterToman ?: 0L)
                    put("senderNumber", t.senderNumber ?: "")
                })
            }
            put("transactions", txArray)

            // 2. Categories
            val catArray = JSONArray()
            categories.forEach { c ->
                catArray.put(JSONObject().apply {
                    put("name", c.name)
                    put("budgetLimitToman", c.budgetLimitToman)
                    put("colorHex", c.colorHex)
                    put("iconName", c.iconName)
                })
            }
            put("categories", catArray)

            // 3. Budgets
            val budgetArray = JSONArray()
            budgets.forEach { b ->
                budgetArray.put(JSONObject().apply {
                    put("categoryName", b.categoryName)
                    put("amountLimitToman", b.amountLimitToman)
                    put("period", b.period.name)
                    put("jalaliYear", b.jalaliYear)
                    put("jalaliMonth", b.jalaliMonth)
                    put("warningThresholdPercent", b.warningThresholdPercent)
                    put("criticalThresholdPercent", b.criticalThresholdPercent)
                    put("isActive", b.isActive)
                    put("notes", b.notes ?: "")
                })
            }
            put("budgets", budgetArray)

            // 4. Assets
            val assetArray = JSONArray()
            assets.forEach { a ->
                assetArray.put(JSONObject().apply {
                    put("type", a.type.name)
                    put("name", a.name)
                    put("totalQuantity", a.totalQuantity)
                    put("currentUnitPriceToman", a.currentUnitPriceToman)
                    put("purchaseUnitPriceToman", a.purchaseUnitPriceToman)
                    put("unit", a.unit)
                    put("notes", a.notes ?: "")
                })
            }
            put("assets", assetArray)

            // 5. Parsed SMS Records
            val smsArray = JSONArray()
            parsedSms.forEach { s ->
                smsArray.put(JSONObject().apply {
                    put("senderAddress", s.senderAddress)
                    put("bankName", s.bankName)
                    put("rawSmsText", s.rawSmsText)
                    put("amountToman", s.amountToman)
                    put("type", s.type.name)
                    put("cardNumber", s.cardNumber ?: "")
                    put("balanceToman", s.balanceToman ?: 0L)
                    put("dateMillis", s.dateMillis)
                    put("categoryName", s.categoryName)
                    put("merchantName", s.merchantName)
                    put("status", s.status.name)
                })
            }
            put("parsedSms", smsArray)
        }

        val plainText = json.toString().toByteArray(Charsets.UTF_8)

        val salt = ByteArray(SALT_LENGTH)
        val iv = ByteArray(IV_LENGTH)
        val random = SecureRandom()
        random.nextBytes(salt)
        random.nextBytes(iv)

        val key = generateKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val cipherText = cipher.doFinal(plainText)

        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(salt)
            out.write(iv)
            out.write(cipherText)
        }
    }

    suspend fun restoreEncryptedBackup(context: Context, uri: Uri, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext false
            if (bytes.size < SALT_LENGTH + IV_LENGTH) return@withContext false

            val salt = bytes.copyOfRange(0, SALT_LENGTH)
            val iv = bytes.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
            val cipherText = bytes.copyOfRange(SALT_LENGTH + IV_LENGTH, bytes.size)

            val key = generateKey(password, salt)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            val plainText = cipher.doFinal(cipherText)

            val jsonStr = String(plainText, Charsets.UTF_8)
            val jsonObj = JSONObject(jsonStr)

            val db = AppDatabase.getDatabase(context)
            
            db.runInTransaction {
                // Restore Categories
                val catArray = jsonObj.optJSONArray("categories")
                if (catArray != null) {
                    val existingCats = db.categoryDao().getAllCategoriesSync().associateBy { it.name }
                    for (i in 0 until catArray.length()) {
                        val obj = catArray.getJSONObject(i)
                        val name = obj.getString("name")
                        if (!existingCats.containsKey(name)) {
                            db.categoryDao().insertSync(
                                Category(
                                    name = name,
                                    budgetLimitToman = obj.optLong("budgetLimitToman", 0L),
                                    colorHex = obj.optString("colorHex", "#00897B"),
                                    iconName = obj.optString("iconName", "category")
                                )
                            )
                        }
                    }
                }

                // Restore Transactions
                val txArray = jsonObj.optJSONArray("transactions")
                if (txArray != null) {
                    for (i in 0 until txArray.length()) {
                        val obj = txArray.getJSONObject(i)
                        db.transactionDao().insertSync(
                            Transaction(
                                amountToman = obj.getLong("amountToman"),
                                type = TransactionType.valueOf(obj.getString("type")),
                                categoryName = obj.getString("categoryName"),
                                merchantName = obj.getString("merchantName"),
                                dateMillis = obj.getLong("dateMillis"),
                                cardNumber = obj.optString("cardNumber").takeIf { it.isNotBlank() },
                                rawSmsText = obj.optString("rawSmsText").takeIf { it.isNotBlank() },
                                isParsedFromSms = obj.optBoolean("isParsedFromSms", false),
                                status = if (obj.has("status")) TransactionStatus.valueOf(obj.getString("status")) else TransactionStatus.CONFIRMED,
                                notes = obj.optString("notes").takeIf { it.isNotBlank() },
                                bankName = obj.optString("bankName").takeIf { it.isNotBlank() },
                                balanceAfterToman = if (obj.has("balanceAfterToman") && obj.getLong("balanceAfterToman") != 0L) obj.getLong("balanceAfterToman") else null,
                                senderNumber = obj.optString("senderNumber").takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }

                // Restore Budgets
                val budgetArray = jsonObj.optJSONArray("budgets")
                if (budgetArray != null) {
                    for (i in 0 until budgetArray.length()) {
                        val obj = budgetArray.getJSONObject(i)
                        val periodStr = obj.optString("period", "MONTHLY")
                        val period = try { BudgetPeriod.valueOf(periodStr) } catch (_: Exception) { BudgetPeriod.MONTHLY }
                        val b = BudgetEntity(
                            categoryName = obj.getString("categoryName"),
                            amountLimitToman = obj.getLong("amountLimitToman"),
                            period = period,
                            jalaliYear = obj.optInt("jalaliYear", 1404),
                            jalaliMonth = obj.optInt("jalaliMonth", 0),
                            warningThresholdPercent = obj.optInt("warningThresholdPercent", 80),
                            criticalThresholdPercent = obj.optInt("criticalThresholdPercent", 100),
                            isActive = obj.optBoolean("isActive", true),
                            notes = obj.optString("notes").takeIf { it.isNotBlank() }
                        )
                        // Run blocking or insert
                        runCatching {
                            kotlinx.coroutines.runBlocking { db.budgetDao().insert(b) }
                        }
                    }
                }

                // Restore Assets
                val assetArray = jsonObj.optJSONArray("assets")
                if (assetArray != null) {
                    val existingAssets = runCatching {
                        kotlinx.coroutines.runBlocking { db.assetDao().getAllAssetsSync() }
                    }.getOrDefault(emptyList()).associateBy { it.name }

                    for (i in 0 until assetArray.length()) {
                        val obj = assetArray.getJSONObject(i)
                        val name = obj.getString("name")
                        if (!existingAssets.containsKey(name)) {
                            val typeStr = obj.optString("type", "CUSTOM")
                            val type = try { AssetType.valueOf(typeStr) } catch (_: Exception) { AssetType.CUSTOM }
                            val a = Asset(
                                type = type,
                                name = name,
                                totalQuantity = obj.optDouble("totalQuantity", 0.0),
                                currentUnitPriceToman = obj.optLong("currentUnitPriceToman", 0L),
                                purchaseUnitPriceToman = obj.optLong("purchaseUnitPriceToman", 0L),
                                unit = obj.optString("unit", ""),
                                notes = obj.optString("notes").takeIf { it.isNotBlank() }
                            )
                            runCatching {
                                kotlinx.coroutines.runBlocking { db.assetDao().insert(a) }
                            }
                        }
                    }
                }

                // Restore Parsed SMS Records
                val smsArray = jsonObj.optJSONArray("parsedSms")
                if (smsArray != null) {
                    for (i in 0 until smsArray.length()) {
                        val obj = smsArray.getJSONObject(i)
                        val statusStr = obj.optString("status", "PENDING")
                        val status = try { SmsProcessingStatus.valueOf(statusStr) } catch (_: Exception) { SmsProcessingStatus.PENDING }
                        val s = ParsedSmsEntity(
                            senderAddress = obj.getString("senderAddress"),
                            bankName = obj.getString("bankName"),
                            rawSmsText = obj.getString("rawSmsText"),
                            amountToman = obj.getLong("amountToman"),
                            type = TransactionType.valueOf(obj.getString("type")),
                            cardNumber = obj.optString("cardNumber").takeIf { it.isNotBlank() },
                            balanceToman = if (obj.has("balanceToman") && obj.getLong("balanceToman") != 0L) obj.getLong("balanceToman") else null,
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                            categoryName = obj.optString("categoryName", "متفرقه"),
                            merchantName = obj.optString("merchantName", ""),
                            status = status
                        )
                        runCatching {
                            kotlinx.coroutines.runBlocking { db.parsedSmsDao().insert(s) }
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportCsv(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val transactions = db.transactionDao().getAllTransactionsSync()

        context.contentResolver.openOutputStream(uri)?.use { out ->
            // Write UTF-8 BOM
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            
            val writer = out.writer(Charsets.UTF_8)
            writer.write("تاریخ,نوع,دسته‌بندی,فروشگاه/توضیحات,مبلغ (تومان),شماره کارت,بانک,مانده بعد از تراکنش\n")
            
            transactions.forEach { t ->
                val date = com.example.ui.components.PersianFormatters.formatDate(t.dateMillis)
                val type = if (t.type == TransactionType.EXPENSE) "برداشت" else "واریز"
                val cat = t.categoryName.replace(",", "،")
                val merch = t.merchantName.replace(",", "،")
                val amount = t.amountToman
                val card = t.cardNumber ?: ""
                val bank = t.bankName ?: ""
                val balance = t.balanceAfterToman?.toString() ?: ""
                writer.write("$date,$type,$cat,$merch,$amount,$card,$bank,$balance\n")
            }
            writer.flush()
        }
    }
}
