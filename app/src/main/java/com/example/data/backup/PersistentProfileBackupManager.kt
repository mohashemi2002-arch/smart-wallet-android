package com.example.data.backup

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.model.BankAccount
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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

    private fun writeBackupJson(fileName: String, jsonContent: String): File {
        var primaryFile: File? = null

        // 1. Android 10+ (API 29+) MediaStore API for Shared Public Documents (Survives Uninstall)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val contentResolver = context.contentResolver
                val projection = arrayOf(MediaStore.MediaColumns._ID)
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(fileName)

                // Delete older version if already present in MediaStore
                contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        val deleteUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                        contentResolver.delete(deleteUri, null, null)
                    }
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/$BACKUP_DIR_NAME")
                }

                val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(jsonContent.toByteArray(Charsets.UTF_8))
                        os.flush()
                    }
                    Log.d(TAG, "Successfully wrote backup to MediaStore Documents URI: $uri")
                }
            } catch (e: Exception) {
                Log.w(TAG, "MediaStore write failed (will use direct files): ${e.message}")
            }
        }

        // 2. Direct Public Documents Directory
        try {
            val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val dir = File(docs, BACKUP_DIR_NAME)
            if (!dir.exists()) dir.mkdirs()
            if (dir.exists() && dir.canWrite()) {
                val f = File(dir, fileName)
                f.writeText(jsonContent, Charsets.UTF_8)
                primaryFile = f
                Log.d(TAG, "Wrote backup to public Documents: ${f.absolutePath}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Public Documents direct file write skipped: ${e.message}")
        }

        // 3. Direct Public Downloads Directory
        try {
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val dir = File(downloads, BACKUP_DIR_NAME)
            if (!dir.exists()) dir.mkdirs()
            if (dir.exists() && dir.canWrite()) {
                val f = File(dir, fileName)
                f.writeText(jsonContent, Charsets.UTF_8)
                if (primaryFile == null) primaryFile = f
                Log.d(TAG, "Wrote backup to public Downloads: ${f.absolutePath}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Public Downloads direct file write skipped: ${e.message}")
        }

        // 4. External App Storage Directory (Survives internal cache clears)
        try {
            val extFiles = context.getExternalFilesDir(null)
            if (extFiles != null) {
                val dir = File(extFiles, BACKUP_DIR_NAME)
                if (!dir.exists()) dir.mkdirs()
                val f = File(dir, fileName)
                f.writeText(jsonContent, Charsets.UTF_8)
                if (primaryFile == null) primaryFile = f
                Log.d(TAG, "Wrote backup to ExternalFilesDir: ${f.absolutePath}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "ExternalFilesDir write skipped: ${e.message}")
        }

        // 5. Internal Storage Directory (Always guaranteed to succeed)
        val internalDir = File(context.filesDir, BACKUP_DIR_NAME)
        if (!internalDir.exists()) internalDir.mkdirs()
        val internalFile = File(internalDir, fileName)
        internalFile.writeText(jsonContent, Charsets.UTF_8)
        Log.d(TAG, "Wrote backup to internal files: ${internalFile.absolutePath}")

        return primaryFile ?: internalFile
    }

    private fun readBackupJsonString(phone: String): String? {
        val cleanPhone = normalizePhone(phone)
        val fileName = "smartwallet_backup_${cleanPhone}.json"

        // 1. Check direct file candidates
        val candidateDirs = listOfNotNull(
            File(context.filesDir, BACKUP_DIR_NAME),
            context.getExternalFilesDir(null)?.let { File(it, BACKUP_DIR_NAME) },
            try { File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), BACKUP_DIR_NAME) } catch (e: Exception) { null },
            try { File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), BACKUP_DIR_NAME) } catch (e: Exception) { null }
        )

        for (dir in candidateDirs) {
            try {
                val f = File(dir, fileName)
                if (f.exists() && f.length() > 0) {
                    val content = f.readText(Charsets.UTF_8)
                    if (content.isNotBlank()) return content
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not read file in ${dir.absolutePath}: ${e.message}")
            }
        }

        // 2. On Android 10+, query MediaStore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val contentResolver = context.contentResolver
                val projection = arrayOf(MediaStore.MediaColumns._ID)
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(fileName)

                contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                        contentResolver.openInputStream(uri)?.use { stream ->
                            val text = stream.bufferedReader(Charsets.UTF_8).readText()
                            if (text.isNotBlank()) return text
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "MediaStore read failed: ${e.message}")
            }
        }

        return null
    }

    suspend fun checkBackupExists(phone: String): Boolean = withContext(Dispatchers.IO) {
        val content = readBackupJsonString(phone)
        !content.isNullOrBlank()
    }

    suspend fun createLocalBackup(
        firstName: String,
        lastName: String,
        phoneNumber: String
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPhone = normalizePhone(phoneNumber)
            val fileName = "smartwallet_backup_${cleanPhone}.json"

            val accounts = database.bankAccountDao().getAllBankAccountsList()
            val transactions = database.transactionDao().getAllTransactionsSync()

            val root = JSONObject()
            root.put("version", 2)
            root.put("backupTimestamp", System.currentTimeMillis())

            val profileObj = JSONObject().apply {
                put("firstName", firstName.trim())
                put("lastName", lastName.trim())
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

            // Multi-strategy write (MediaStore + Documents + Downloads + External + Internal)
            writeBackupJson(fileName, root.toString(2))
        }
    }

    suspend fun restoreLocalBackup(
        phoneNumber: String
    ): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPhone = normalizePhone(phoneNumber)
            val jsonString = readBackupJsonString(cleanPhone)
                ?: throw IllegalStateException("فایل پشتیبان برای شماره $cleanPhone در حافظه دستگاه یافت نشد")

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
                        id = 0L,
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
