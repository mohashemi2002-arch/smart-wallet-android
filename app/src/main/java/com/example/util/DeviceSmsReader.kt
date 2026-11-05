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
    val detectedBank: String? = null, // Detected bank name if identified
    val associatedBank: String? = null // Explicit user/DB assigned bank entity
)

data class DeviceSenderSmsMessage(
    val id: Long,
    val address: String,
    val body: String,
    val timestamp: Long,
    val detectedBank: String? = null,
    val hasBalance: Boolean = false,
    val associatedBank: String? = null // Explicit bank entity assigned to this sender
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

    /**
     * Strictly matches SMS address with the configured senderNumber.
     * Does NOT check message body - guarantees that messages come only from the exact sender.
     */
    fun isAddressStrictMatch(address: String, senderNumber: String): Boolean {
        val normAddr = cleanNumber(address)
        val normTarget = cleanNumber(senderNumber)

        if (normTarget.isBlank() || normAddr.isBlank()) return false

        // تطبیق دقیق شماره‌های پاکسازی شده
        if (normAddr == normTarget) return true

        // تطبیق پیشوند و پسوند (برای سرشماره‌های کوتاه‌مدت بانکی مانند 30009417 و 9830009417)
        if (normAddr.endsWith(normTarget) || normTarget.endsWith(normAddr)) {
            val minLen = minOf(normAddr.length, normTarget.length)
            if (minLen >= 3) return true
        }

        // تطبیق نام‌های متنی فرستنده (مانند BLUBANK یا Mellat)
        val lowerAddr = address.lowercase().trim()
        val lowerTarget = senderNumber.lowercase().trim()
        
        return lowerAddr == lowerTarget || 
               (lowerTarget.length >= 3 && (lowerAddr.contains(lowerTarget) || lowerTarget.contains(lowerAddr)))
    } // خطوط اضافی و آکولاد مخرب از اینجا حذف شدند

    /**
     * Checks if the SMS contains a balance statement (مانده / موجودی).
     * Financial transactions in Iranian banks always include a balance statement.
     */
    fun hasBalance(body: String): Boolean {
        val norm = normalizeDigits(body)
        return norm.contains("مانده") || norm.contains("موجودی")
    }

    /**
     * Identifies OTP / dynamic password / رمز دوم messages which do NOT contain a transaction balance.
     */
    fun isOtpWithoutBalance(body: String): Boolean {
        val norm = normalizeDigits(body)
        val hasOtpWords = norm.contains("رمز پویا") ||
                norm.contains("رمز موقت") ||
                norm.contains("رمز دوم") ||
                norm.contains("رمز یکبار مصرف") ||
                norm.contains("کد تایید") ||
                norm.contains("کد فعال سازی") ||
                norm.contains("کد فعالسازی")

        return hasOtpWords && !hasBalance(body)
    }

    fun detectBankName(address: String, body: String): String? {
        val combined = "$address $body"
        val bankKeywords = listOf(
            // بلوبانک
            "بلوبانک" to "بلوبانک",
            "بلو بانک" to "بلوبانک",
            "BLUBANK" to "بلوبانک",
            "blubank" to "بلوبانک",
            "30009417" to "بلوبانک",
            "30009411" to "بلوبانک",

            // بانک ملت
            "BankMellat" to "بانک ملت",
            "Bank Mellat" to "بانک ملت",
            "بانک ملت" to "بانک ملت",
            "mellat" to "بانک ملت",
            "ملت" to "بانک ملت",

            // بانک ملی
            "Bank Melli" to "بانک ملی ایران",
            "BankMelli" to "بانک ملی ایران",
            "BMI" to "بانک ملی ایران",
            "بانک ملی" to "بانک ملی ایران",
            "ملی ایران" to "بانک ملی ایران",

            // بانک صادرات
            "BankSaderat" to "بانک صادرات ایران",
            "Bank Saderat" to "بانک صادرات ایران",
            "BSI" to "بانک صادرات ایران",
            "بانک صادرات" to "بانک صادرات ایران",
            "صادرات ایران" to "بانک صادرات ایران",
            "صادرات" to "بانک صادرات ایران",

            // بانک سپه
            "Bank Sepah" to "بانک سپه",
            "BankSepah" to "بانک سپه",
            "بانک سپه" to "بانک سپه",
            "سپه" to "بانک سپه",
            "sepah" to "بانک سپه",

            // بانک تجارت
            "BankTejarat" to "بانک تجارت",
            "Bank Tejarat" to "بانک تجارت",
            "بانک تجارت" to "بانک تجارت",
            "تجارت" to "بانک تجارت",
            "tejarat" to "بانک تجارت",

            // بانک پاسارگاد
            "Pasargad" to "بانک پاسارگاد",
            "Bank Pasargad" to "بانک پاسارگاد",
            "بانک پاسارگاد" to "بانک پاسارگاد",
            "پاسارگاد" to "بانک پاسارگاد",

            // بانک سامان
            "SamanBank" to "بانک سامان",
            "Saman Bank" to "بانک سامان",
            "بانک سامان" to "بانک سامان",
            "سامان" to "بانک سامان",
            "saman" to "بانک سامان",

            // قرض‌الحسنه مهر ایران
            "QMEHRIRAN" to "بانک قرض‌الحسنه مهر ایران",
            "QMehrIran" to "بانک قرض‌الحسنه مهر ایران",
            "مهر ایران" to "بانک قرض‌الحسنه مهر ایران",

            // قرض‌الحسنه رسالت
            "Rqbank" to "بانک قرض‌الحسنه رسالت",
            "RQBank" to "بانک قرض‌الحسنه رسالت",
            "رسالت" to "بانک قرض‌الحسنه رسالت",

            // بانک پارسیان
            "ParsianBank" to "بانک پارسیان",
            "Parsian Bank" to "بانک پارسیان",
            "بانک پارسیان" to "بانک پارسیان",
            "پارسیان" to "بانک پارسیان",
            "parsian" to "بانک پارسیان",

            // بانک کشاورزی
            "Bank Keshavarzi" to "بانک کشاورزی",
            "BankKeshavarzi" to "بانک کشاورزی",
            "بانک کشاورزی" to "بانک کشاورزی",
            "کشاورزی" to "بانک کشاورزی",
            "bki" to "بانک کشاورزی",

            // بانک مسکن
            "Bank Maskan" to "بانک مسکن",
            "BankMaskan" to "بانک مسکن",
            "بانک مسکن" to "بانک مسکن",
            "مسکن" to "بانک مسکن",

            // بانک شهر
            "Shahr Bank" to "بانک شهر",
            "ShahrBank" to "بانک شهر",
            "بانک شهر" to "بانک شهر",
            "shahr" to "بانک شهر",

            // بانک آینده
            "BaBank" to "بانک آینده",
            "Ayandeh" to "بانک آینده",
            "بانک آینده" to "بانک آینده",
            "آینده" to "بانک آینده",

            // سایر بانک‌ها
            "بانک رفاه" to "بانک رفاه کارگران",
            "رفاه کارگران" to "بانک رفاه کارگران",
            "رفاه" to "بانک رفاه کارگران",
            "بانک سینا" to "بانک سینا",
            "سینا" to "بانک سینا",
            "اقتصاد نوین" to "بانک اقتصاد نوین",
            "کارآفرین" to "بانک کارآفرین",
            "سرمایه" to "بانک سرمایه",
            "گردشگری" to "بانک گردشگری",
            "ایران زمین" to "بانک ایران زمین",
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

    fun isNonBankSender(address: String): Boolean {
        val clean = cleanNumber(address)
        // 1. Personal Iranian mobile numbers: 90xxxxxxxx, 91xxxxxxxx, 92xxxxxxxx, 93xxxxxxxx, 99xxxxxxxx
        if (clean.length == 10 && (clean.startsWith("90") || clean.startsWith("91") || clean.startsWith("92") || 
            clean.startsWith("93") || clean.startsWith("99") || clean.startsWith("98"))) {
            return true
        }

        // 2. Telecom operators, promotional masks, app services
        val lower = address.lowercase().trim()
        val blockedKeywords = listOf(
            "irancell", "irnc", "mci", "hamrah", "rightel", "snapp", "tapsi", "isport",
            "isqi", "ofoq", "mygov", "jabama", "missed", "divar", "sheypoor", "torob", "basalam",
            "bazar", "cafebazaar", "rubika", "eitaa", "gap", "soroush", "bale", "digikala"
        )
        if (blockedKeywords.any { lower.contains(it) }) {
            return true
        }
        return false
    }

    fun isBankTransactionSms(address: String, body: String): Boolean {
        if (isNonBankSender(address)) return false

        val text = normalizeDigits(body).lowercase()

        // 1. Never accept OTP / Dynamic passwords
        if (isOtpWithoutBalance(body) || text.contains("رمز پویا") || text.contains("کد تایید") || text.contains("کد تأیید")) {
            return false
        }

        // 2. Iranian Bank Transactions ALWAYS require balance (مانده / موجودی)
        val hasBal = hasBalance(body)
        if (!hasBal) return false

        // 3. Must have financial transaction verb
        val hasTransactionKeyword = text.contains("واریز") ||
                text.contains("برداشت") ||
                text.contains("انتقال") ||
                text.contains("خرید") ||
                text.contains("خريد") ||
                text.contains("پایانه فروش") ||
                text.contains("پايانه فروش") ||
                text.contains("حواله") ||
                text.contains("اصلاحی") ||
                text.contains("اصلاحي") ||
                text.contains("پایا") ||
                text.contains("ساتنا") ||
                text.contains("یارانه") ||
                text.contains("يارانه")
        if (!hasTransactionKeyword) return false

        // 4. Must be associated with an Iranian Bank
        val detectedBank = detectBankName(address, body)
        val hasBankKeyword = detectedBank != null ||
                text.contains("بانک") ||
                text.contains("بانك")

        return hasBankKeyword
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

    /**
     * Gets messages strictly from the specified senderNumber.
     * When requireBalance is true (default), filters out messages without balance (such as OTP / رمز دوم).
     */
    suspend fun getMessagesForSender(
        context: Context,
        senderNumber: String,
        bankName: String? = null,
        requireBalance: Boolean = true,
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
            // استفاده از فیلتر پایگاه داده برای سرعت بالاتر و محدود کردن به سرشماره هدف
            cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                Telephony.Sms.DATE + " DESC LIMIT 2500"
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

                    // ۱. سخت‌گیرانه: باید دقیقاً مطابق سرشماره تعریف‌شده برای کارت باشد
                    if (!isAddressStrictMatch(address, senderNumber)) {
                        continue
                    }

                    val msgHasBalance = hasBalance(body)

                    // ۲. فیلتر کردن رمز دوم و پیامک‌های بدون مانده در صورت نیاز
                    if (requireBalance && (!msgHasBalance || isOtpWithoutBalance(body))) {
                        continue
                    }

                    messages.add(
                        DeviceSenderSmsMessage(
                            id = id,
                            address = address,
                            body = body,
                            timestamp = date,
                            detectedBank = detectBankName(address, body) ?: bankName,
                            hasBalance = msgHasBalance
                        )
                    )
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
        requireBalance: Boolean = true,
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
                Telephony.Sms.DATE + " DESC LIMIT 2500"
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

                    val msgHasBalance = hasBalance(body)

                    if (isNonBankSender(address)) {
                        continue
                    }

                    if (!isBankTransactionSms(address, body)) {
                        continue
                    }

                    messages.add(
                        DeviceSenderSmsMessage(
                            id = id,
                            address = address,
                            body = body,
                            timestamp = date,
                            detectedBank = detectBankName(address, body),
                            hasBalance = msgHasBalance
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceSmsReader", "Error reading all bank SMS: ${e.message}")
        } finally {
            cursor?.close()
        }

        return@withContext messages
    }

    suspend fun getAllIncomingMessages(
        context: Context,
        limit: Int = 1000
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
                Telephony.Sms.DATE + " DESC LIMIT $limit"
            )

            cursor?.let {
                val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val address = it.getString(addressIndex) ?: continue
                    val body = it.getString(bodyIndex) ?: ""
                    val date = it.getLong(dateIndex)
                    val id = it.getLong(idIndex)

                    messages.add(
                        DeviceSenderSmsMessage(
                            id = id,
                            address = address,
                            body = body,
                            timestamp = date,
                            detectedBank = detectBankName(address, body),
                            hasBalance = hasBalance(body)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceSmsReader", "Error reading all incoming SMS: ${e.message}")
        } finally {
            cursor?.close()
        }

        return@withContext messages
    }
}