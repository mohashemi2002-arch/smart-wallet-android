package com.example.domain.util

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.example.data.database.AppDatabase
import com.example.data.model.BankSmsHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BankSmsHistoryScanner(
    private val context: Context,
    private val database: AppDatabase
) {
    suspend fun scanInboxForBankSms(): List<BankSmsHistoryItem> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<BankSmsHistoryItem>()

        try {
            // 1. Fetch Registered Senders
            val bankAccounts = database.bankAccountDao().getAllBankAccountsSync()
            val smsSenders = database.bankSmsSenderDao().getActiveSenders()

            val activeSendersMap = mutableMapOf<String, String>() // normalized number -> Bank Name
            
            fun normalizeNumber(number: String): String {
                return number.replace("+98", "").replace("098", "").trim()
            }

            bankAccounts.forEach { acc ->
                acc.smsShortcode?.let { shortcode ->
                    if (shortcode.isNotBlank()) {
                        activeSendersMap[normalizeNumber(shortcode)] = acc.bankName
                    }
                }
            }
            smsSenders.forEach { sender ->
                if (sender.senderNumber.isNotBlank()) {
                    activeSendersMap[normalizeNumber(sender.senderNumber)] = sender.bankName
                }
            }

            if (activeSendersMap.isEmpty()) return@withContext emptyList()

            // 2. Fetch existing transactions to detect duplicates
            val allTransactions = database.transactionDao().getAllTransactionsSync()
            val registeredSmsTexts = allTransactions.mapNotNull { it.rawSmsText }.toSet()

            // 3. Targeted SMS Inbox Query
            val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            )

            val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT 500" // limit to last 500 overall to be fast

            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addrIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

                var count = 0
                while (cursor.moveToNext() && count < 100) { // Keep top 100 bank messages
                    val address = cursor.getString(addrIdx) ?: ""
                    val body = cursor.getString(bodyIdx) ?: ""
                    val date = cursor.getLong(dateIdx)
                    val id = cursor.getLong(idIdx)

                    val normalizedAddress = normalizeNumber(address)
                    
                    val matchedBankName = activeSendersMap.entries.firstOrNull { 
                        normalizedAddress.endsWith(it.key) || it.key.endsWith(normalizedAddress) ||
                        (it.key.length > 4 && normalizedAddress.contains(it.key))
                    }?.value

                    if (matchedBankName != null) {
                        // Filter out OTP SMS (رمز پویا / رمز دوم)
                        if (com.example.data.SmsAiParser.isOtpSms(body)) {
                            continue
                        }

                        // Check if already registered by seeing if this raw text is contained in any existing transaction
                        val isAlreadyRegistered = registeredSmsTexts.any { body.contains(it) || it.contains(body) }
                        
                        resultList.add(
                            BankSmsHistoryItem(
                                smsId = id,
                                sender = address,
                                body = body,
                                timestamp = date,
                                bankName = matchedBankName,
                                isRegistered = isAlreadyRegistered
                            )
                        )
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        resultList
    }
}
