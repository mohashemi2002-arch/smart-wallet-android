package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
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

data class DeviceSenderSmsMessage(
    val id: Long,
    val address: String,
    val body: String,
    val timestamp: Long,
    val detectedBank: String? = null
)

object DeviceSmsReader {

    fun normalizeDigits(input: String): String {
        return input
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
            .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
    }

    fun cleanNumber(num: String): String {
        val norm = normalizeDigits(num).lowercase().trim()
        return norm
            .replace("+98", "")
            .replace("0098", "")
            .replace("98", "")
            .replace("-", "")
            .replace(" ", "")
            .trimStart('0')
    }

    fun detectBankName(address: String, body: String): String? {
        val combined = "$address $body"
        val bankKeywords = listOf(
            "بلوبانک" to "بلوبانک",
            "بلو بانک" to "بلوبانک",
            "BLUBANK" to "بلوبانک",
            "blubank" to "بلوبانک",
            "بانک ملت" to "بانک ملت",
            "ملت" to "بانک ملت",
            "mellat" to "بانک ملت",
            "بانک ملی" to "بانک ملی ایران",
            "ملی ایران" to "بانک ملی ایران",
            "بانک صادرات" to "بانک صادرات ایران",
            "صادرات ایران" to "بانک صادرات ایران",
            "صادرات" to "بانک صادرات ایران",
            "بانک تجارت" to "بانک تجارت",
            "تجارت" to "بانک تجارت",
            "tejarat" to "بانک تجارت",
            "بانک سپه" to "بانک سپه",
            "سپه" to "بانک سپه",
            "sepah" to "بانک سپه",
            "بانک پاسارگاد" to "بانک پاسارگاد",
            "پاسارگاد" to "بانک پاسارگاد",
            "pasargad" to "بانک پاسارگاد",
            "بانک سامان" to "بانک سامان",
            "سامان" to "بانک سامان",
            "saman" to "بانک سامان",
            "بانک پارسیان" to "بانک پارسیان",
            "پارسیان" to "بانک پارسیان",
            "parsian" to "بانک پارسیان",
            "بانک کشاورزی" to "بانک کشاورزی",
            "کشاورزی" to "بانک کشاورزی",
            "bki" to "بانک کشاورزی",
            "بانک رفاه" to "بانک رفاه کارگران",
            "رفاه کارگران" to "بانک رفاه کارگران",
            "رفاه" to "بانک رفاه کارگران",
            "بانک مسکن" to "بانک مسکن",
            "مسکن" to "بانک مسکن",
            "بانک شهر" to "بانک شهر",
            "shahr" to "بانک شهر",
            "بانک آینده" to "بانک آینده",
            "آینده" to "بانک آینده",
            "ayandeh" to "بانک آینده",
            "بانک سینا" to "بانک سینا",
            "سینا" to "بانک سینا",
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

    fun isBankTransactionSms(address: String, body: String): Boolean {
        // Quick check for transaction keywords
        val text = body.lowercase()
        val hasTransactionKeyword = text.contains("واریز") ||
                text.contains("برداشت") ||
                text.contains("انتقال") ||
                text.contains("خرید") ||
                text.contains("مانده") ||
                text.contains("تومان") ||
                text.contains("ریال")

        val hasBankKeyword = detectBankName(address, body) != null ||
                text.contains("بانک") ||
                text.contains("حساب") ||
                text.contains("کارت")

        // Exclude OTP codes that don't have any financial transaction
        val isPureOtp = (text.contains("رمز موقت") || text.contains("رمز یکبار مصرف") || text.contains("کد تایید")) &&
                !text.contains("واریز") && !text.contains("برداشت") && !text.contains("خرید")

        return (hasTransactionKeyword && hasBankKeyword) && !isPureOtp
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
                Telephony.Sms.DATE + " DESC LIMIT 2000"
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
        } catch (e: Exception) {
            Log.e("DeviceSmsReader", "Error reading SMS: ${e.message}")
        } finally {
            cursor?.close()
        }

        return@withContext sendersMap.values.sortedByDescending { it.timestamp }.toList()
    }

    suspend fun getMessagesForSender(
        context: Context,
        senderNumber: String,
        bankName: String? = null,
        limit: Int = 150
    ): List<DeviceSenderSmsMessage> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }

        val messages = mutableListOf<DeviceSenderSmsMessage>()
        val uri: Uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val targetClean = cleanNumber(senderNumber)
        val targetLower = senderNumber.lowercase().trim()
        val bankNameTrim = bankName?.trim() ?: ""

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                Telephony.Sms.DATE + " DESC LIMIT 2000"
            )

            cursor?.let {
                val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (it.moveToNext() && messages.size < limit) {
                    val address = it.getString(addressIndex) ?: continue
                    val body = it.getString(bodyIndex) ?: ""
                    val date = it.getLong(dateIndex)
                    val id = it.getLong(idIndex)

                    val addrClean = cleanNumber(address)
                    val addrLower = address.lowercase().trim()

                    val isAddressMatch = targetClean.isNotBlank() && (
                        addrClean == targetClean ||
                        addrClean.endsWith(targetClean) ||
                        targetClean.endsWith(addrClean) ||
                        (targetClean.length >= 4 && addrClean.contains(targetClean)) ||
                        (targetLower.length >= 3 && addrLower.contains(targetLower))
                    )

                    val isBankNameMatch = bankNameTrim.isNotBlank() && (
                        addrLower.contains(bankNameTrim.lowercase()) ||
                        body.contains(bankNameTrim, ignoreCase = true)
                    )

                    if (isAddressMatch || isBankNameMatch) {
                        messages.add(
                            DeviceSenderSmsMessage(
                                id = id,
                                address = address,
                                body = body,
                                timestamp = date,
                                detectedBank = detectBankName(address, body) ?: bankName
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceSmsReader", "Error reading messages for sender $senderNumber: ${e.message}")
        } finally {
            cursor?.close()
        }

        return@withContext messages
    }

    suspend fun getAllBankSms(
        context: Context,
        limit: Int = 150
    ): List<DeviceSenderSmsMessage> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }

        val messages = mutableListOf<DeviceSenderSmsMessage>()
        val uri: Uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf(
            Telephony.Sms._ID,
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
                Telephony.Sms.DATE + " DESC LIMIT 2000"
            )

            cursor?.let {
                val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (it.moveToNext() && messages.size < limit) {
                    val address = it.getString(addressIndex) ?: continue
                    val body = it.getString(bodyIndex) ?: ""
                    val date = it.getLong(dateIndex)
                    val id = it.getLong(idIndex)

                    if (isBankTransactionSms(address, body)) {
                        messages.add(
                            DeviceSenderSmsMessage(
                                id = id,
                                address = address,
                                body = body,
                                timestamp = date,
                                detectedBank = detectBankName(address, body)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceSmsReader", "Error reading all bank SMS: ${e.message}")
        } finally {
            cursor?.close()
        }

        return@withContext messages
    }
}
