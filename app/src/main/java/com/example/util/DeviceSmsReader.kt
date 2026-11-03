package com.example.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SmsSenderPreview(
    val address: String,       // e.g. "9830009417", "300069", "+982000..."
    val lastMessage: String,   // Preview snippet of the message
    val timestamp: Long,       // Message date
    val totalCount: Int,       // How many messages from this sender
    val detectedBank: String? = null // Detected bank name if identified
)

object DeviceSmsReader {

    fun detectBankName(address: String, body: String): String? {
        val combined = "$address $body"
        val bankKeywords = listOf(
            "بلوبانک" to "بلوبانک",
            "بلو بانک" to "بلوبانک",
            "BLUBANK" to "بلوبانک",
            "بانک ملت" to "بانک ملت",
            "ملت" to "بانک ملت",
            "بانک ملی" to "بانک ملی ایران",
            "بانک صادرات" to "بانک صادرات ایران",
            "صادرات" to "بانک صادرات ایران",
            "بانک تجارت" to "بانک تجارت",
            "تجارت" to "بانک تجارت",
            "بانک سپه" to "بانک سپه",
            "سپه" to "بانک سپه",
            "بانک پاسارگاد" to "بانک پاسارگاد",
            "پاسارگاد" to "بانک پاسارگاد",
            "بانک سامان" to "بانک سامان",
            "سامان" to "بانک سامان",
            "بانک پارسیان" to "بانک پارسیان",
            "پارسیان" to "بانک پارسیان",
            "بانک کشاورزی" to "بانک کشاورزی",
            "کشاورزی" to "بانک کشاورزی",
            "بانک رفاه" to "بانک رفاه کارگران",
            "رفاه" to "بانک رفاه کارگران",
            "بانک مسکن" to "بانک مسکن",
            "مسکن" to "بانک مسکن",
            "بانک شهر" to "بانک شهر",
            "بانک آینده" to "بانک آینده",
            "بانک سینا" to "بانک سینا",
            "اقتصاد نوین" to "بانک اقتصاد نوین",
            "کارآفرین" to "بانک کارآفرین",
            "سرمایه" to "بانک سرمایه",
            "گردشگری" to "بانک گردشگری",
            "ایران زمین" to "بانک ایران زمین",
            "رسالت" to "بانک قرض‌الحسنه رسالت",
            "مهر ایران" to "بانک قرض‌الحسنه مهر ایران",
            "پست بانک" to "پست بانک ایران",
            "بانک دی" to "بانک دی",
            "خاورمیانه" to "بانک خاورمیانه",
            "توسعه تعاون" to "بانک توسعه تعاون"
        )
        for ((kw, bank) in bankKeywords) {
            if (combined.contains(kw, ignoreCase = true)) {
                return bank
            }
        }
        return null
    }

    suspend fun getUniqueSenders(context: Context): List<SmsSenderPreview> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }

        val sendersMap = mutableMapOf<String, SmsSenderPreview>()
        val uri: Uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                Telephony.Sms.DATE + " DESC LIMIT 2000" // Limit to recent messages for performance
            )

            cursor?.let {
                val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val address = it.getString(addressIndex) ?: continue
                    val body = it.getString(bodyIndex) ?: ""
                    val date = it.getLong(dateIndex)
                    val detectedBank = detectBankName(address, body)
                    
                    if (sendersMap.containsKey(address)) {
                        val existing = sendersMap[address]!!
                        sendersMap[address] = existing.copy(
                            totalCount = existing.totalCount + 1,
                            detectedBank = existing.detectedBank ?: detectedBank
                        )
                    } else {
                        sendersMap[address] = SmsSenderPreview(
                            address = address,
                            lastMessage = body.take(90) + if (body.length > 90) "..." else "",
                            timestamp = date,
                            totalCount = 1,
                            detectedBank = detectedBank
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("DeviceSmsReader", "SecurityException reading SMS: ${e.message}")
        } catch (e: Exception) {
            Log.e("DeviceSmsReader", "Error reading SMS: ${e.message}")
        } finally {
            cursor?.close()
        }

        return@withContext sendersMap.values.sortedByDescending { it.timestamp }.toList()
    }
}
