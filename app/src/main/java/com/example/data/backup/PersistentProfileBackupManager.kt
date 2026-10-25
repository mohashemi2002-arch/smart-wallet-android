package com.example.data.backup

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.model.BankAccount
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class RestoreSummary(
    val restoredAccountsCount: Int,
    val restoredTransactionsCount: Int,
    val backupDateMillis: Long,
    val ownerName: String
)

class PersistentProfileBackupManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context)
) {

    companion object {
        private const val TAG = "PersistentBackup"
        private const val BACKUP_DIR_NAME = "SmartWallet_Backups"

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

    private fun getBackupDirectories(): List<File> {
        val dirs = mutableListOf<File>()

        // 1. Primary: Shared Public Documents Directory (Persists after App Uninstall)
        try {
            val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val smartWalletDocs = File(docs, BACKUP_DIR_NAME)
            if (!smartWalletDocs.exists()) smartWalletDocs.mkdirs()
            if (smartWalletDocs.exists() && smartWalletDocs.canWrite()) {
                dirs.add(smartWalletDocs)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Documents dir not accessible: ${e.message}")
        }

        // 2. Secondary: Public Downloads Directory
        try {
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val smartWalletDownloads = File(downloads, BACKUP_DIR_NAME)
            if (!smartWalletDownloads.exists()) smartWalletDownloads.mkdirs()
            if (smartWalletDownloads.exists() && smartWalletDownloads.canWrite()) {
                dirs.add(smartWalletDownloads)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Downloads dir not accessible: ${e.message}")
        }

        // 3. Fallback: External Files Directory
        try {
            val extFiles = context.getExternalFilesDir(null)
            if (extFiles != null) {
                val appBackupDir = File(extFiles, BACKUP_DIR_NAME)
                if (!appBackupDir.exists()) appBackupDir.mkdirs()
                dirs.add(appBackupDir)
            }
        } catch (e: Exception) {
            Log.w(TAG, "ExtFiles dir not accessible: ${e.message}")
        }

        return dirs
    }

    private fun getBackupFileForPhone(phone: String, forWriting: Boolean = false): File? {
        val cleanPhone = normalizePhone(phone)
        val fileName = "smartwallet_backup_${cleanPhone}.json"

        val dirs = getBackupDirectories()
        if (!forWriting) {
            for (dir in dirs) {
                val f = File(dir, fileName)
                if (f.exists() && f.length() > 0) return f
            }
        }
        return dirs.firstOrNull()?.let { File(it, fileName) }
    }

    suspend fun checkBackupExists(phone: String): Boolean = withContext(Dispatchers.IO) {
        val file = getBackupFileForPhone(phone, forWriting = false)
        file != null && file.exists() && file.length() > 0
    }

    suspend fun createLocalBackup(
        firstName: String,
        lastName: String,
        phoneNumber: String
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPhone = normalizePhone(phoneNumber)
            val targetFile = getBackupFileForPhone(cleanPhone, forWriting = true)
                ?: throw IllegalStateException("حافظه مشترک گوشی در دسترس نیست")

            val accounts = database.bankAccountDao().getAllBankAccountsList()
            val transactions = database.transactionDao().getAllTransactionsSync()
            val categories = database.categoryDao().getAllCategoriesSync()

            val root = JSONObject()
            root.put("version", 2)
            root.put("backupTimestamp", System.currentTimeMillis())

            val profileObj = JSONObject().apply {
                put("firstName", firstName)
                put("lastName", lastName)
                put("phoneNumber", cleanPhone)
            }
            root.put("profile", profileObj)

            // Bank Accounts Array
            val accountsArray = JSONArray()
            accounts.forEach { acc ->
                val accObj = JSONObject().apply {
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
                }
                accountsArray.put(accObj)
            }
            root.put("accounts", accountsArray)

            // Transactions Array
            val txArray = JSONArray()
            transactions.forEach { tx ->
                val txObj = JSONObject().apply {
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
                }
                txArray.put(txObj)
            }
            root.put("transactions", txArray)

            // Write to file
            FileOutputStream(targetFile).use { fos ->
                fos.write(root.toString(2).toByteArray(Charsets.UTF_8))
            }
            Log.d(TAG, "Persistent backup successfully written to: ${targetFile.absolutePath}")
            targetFile
        }
    }

    suspend fun restoreLocalBackup(
        phoneNumber: String
    ): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPhone = normalizePhone(phoneNumber)
            val file = getBackupFileForPhone(cleanPhone, forWriting = false)
                ?: throw IllegalStateException("فایل پشتیبان برای شماره $cleanPhone یافت نشد")

            val jsonString = FileInputStream(file).use { fis ->
                fis.bufferedReader(Charsets.UTF_8).readText()
            }
            val root = JSONObject(jsonString)

            val profileObj = root.optJSONObject("profile")
            val firstName = profileObj?.optString("firstName") ?: ""
            val lastName = profileObj?.optString("lastName") ?: ""
            val backupTime = root.optLong("backupTimestamp", System.currentTimeMillis())

            val accountsArray = root.optJSONArray("accounts") ?: JSONArray()
            val txArray = root.optJSONArray("transactions") ?: JSONArray()

            val accountsToRestore = mutableListOf<BankAccount>()
            for (i in 0 until accountsArray.length()) {
                val obj = accountsArray.getJSONObject(i)
                val accNumber = obj.optString("accountNumber").ifBlank { null }
                accountsToRestore.add(
                    BankAccount(
                        id = 0L, // Auto-generate clean IDs
                        bankName = obj.optString("bankName", "بانک"),
                        accountName = obj.optString("accountName", "حساب"),
                        cardNumber = obj.optString("cardNumber", ""),
                        accountNumber = accNumber,
                        balanceToman = obj.optLong("balanceToman", 0L),
                        cardColorHex = obj.optString("cardColorHex", "#1E88E5"),
                        smsShortcode = obj.optString("smsShortcode").ifBlank { null },
                        accountType = obj.optString("accountType", "BANK_CARD"),
                        readSmsEnabled = obj.optBoolean("readSmsEnabled", true),
                        initialBalanceEnabled = obj.optBoolean("initialBalanceEnabled", true)
                    )
                )
            }

            val txToRestore = mutableListOf<Transaction>()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                val bal = obj.optLong("balanceAfterToman", -1L)
                txToRestore.add(
                    Transaction(
                        id = 0L,
                        amountToman = obj.optLong("amountToman", 0L),
                        type = try { TransactionType.valueOf(obj.optString("type", "EXPENSE")) } catch (e: Exception) { TransactionType.EXPENSE },
                        categoryName = obj.optString("categoryName", "متفرقه"),
                        merchantName = obj.optString("merchantName", ""),
                        cardNumber = obj.optString("cardNumber").ifBlank { null },
                        dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                        rawSmsText = obj.optString("rawSmsText").ifBlank { null },
                        isParsedFromSms = obj.optBoolean("isParsedFromSms", false),
                        status = try { TransactionStatus.valueOf(obj.optString("status", "CONFIRMED")) } catch (e: Exception) { TransactionStatus.CONFIRMED },
                        notes = obj.optString("notes").ifBlank { null },
                        bankName = obj.optString("bankName").ifBlank { null },
                        balanceAfterToman = if (bal >= 0L) bal else null,
                        senderNumber = obj.optString("senderNumber").ifBlank { null }
                    )
                )
            }

            // Insert into Room without duplicates
            val existingAccounts = database.bankAccountDao().getAllBankAccountsList()
            var restoredAccCount = 0
            for (acc in accountsToRestore) {
                val alreadyExists = existingAccounts.any { 
                    it.cardNumber == acc.cardNumber || (acc.accountNumber != null && it.accountNumber == acc.accountNumber)
                }
                if (!alreadyExists) {
                    database.bankAccountDao().insert(acc)
                    restoredAccCount++
                }
            }

            val existingTx = database.transactionDao().getAllTransactionsSync()
            var restoredTxCount = 0
            for (tx in txToRestore) {
                val alreadyExists = existingTx.any {
                    it.dateMillis == tx.dateMillis && it.amountToman == tx.amountToman
                }
                if (!alreadyExists) {
                    database.transactionDao().insert(tx)
                    restoredTxCount++
                }
            }

            RestoreSummary(
                restoredAccountsCount = restoredAccCount,
                restoredTransactionsCount = restoredTxCount,
                backupDateMillis = backupTime,
                ownerName = "$firstName $lastName".trim()
            )
        }
    }
}
