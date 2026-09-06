package com.example.domain.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.data.database.AppDatabase
import com.example.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
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
        // Here we could extract other entities similarly
        
        val json = JSONObject().apply {
            val txArray = JSONArray()
            transactions.forEach { t ->
                txArray.put(JSONObject().apply {
                    put("amountToman", t.amountToman)
                    put("type", t.type.name)
                    put("categoryName", t.categoryName)
                    put("merchantName", t.merchantName)
                    put("dateMillis", t.dateMillis)
                    put("cardNumber", t.cardNumber ?: "")
                })
            }
            put("transactions", txArray)
            // Put other arrays
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

            val txArray = jsonObj.optJSONArray("transactions")
            val db = AppDatabase.getDatabase(context)
            
            db.runInTransaction {
                if (txArray != null) {
                    for (i in 0 until txArray.length()) {
                        val obj = txArray.getJSONObject(i)
                        db.transactionDao().insertSync(
                            Transaction(
                                amountToman = obj.getLong("amountToman"),
                                type = com.example.data.model.TransactionType.valueOf(obj.getString("type")),
                                categoryName = obj.getString("categoryName"),
                                merchantName = obj.getString("merchantName"),
                                dateMillis = obj.getLong("dateMillis"),
                                cardNumber = obj.optString("cardNumber").takeIf { it.isNotBlank() }
                            )
                        )
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
            writer.write("تاریخ,نوع,دسته‌بندی,فروشگاه/توضیحات,مبلغ (تومان),شماره کارت\n")
            
            transactions.forEach { t ->
                // Basic CSV escaping
                val date = com.example.ui.components.PersianFormatters.formatDate(t.dateMillis)
                val type = if (t.type == com.example.data.model.TransactionType.EXPENSE) "برداشت" else "واریز"
                val cat = t.categoryName.replace(",", "،")
                val merch = t.merchantName.replace(",", "،")
                val amount = t.amountToman
                val card = t.cardNumber ?: ""
                writer.write("$date,$type,$cat,$merch,$amount,$card\n")
            }
            writer.flush()
        }
    }
}
