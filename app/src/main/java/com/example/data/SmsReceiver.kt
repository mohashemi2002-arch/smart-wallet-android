package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val body = sms.messageBody ?: continue
                val sender = sms.originatingAddress ?: ""

                // Check if message looks like an Iranian bank SMS
                if (isBankSms(body, sender)) {
                    Log.d("SmsReceiver", "Bank SMS detected from $sender: $body")
                    val repository = FinanceRepository.getInstance(context)
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.processIncomingSms(body, sender)
                    }
                }
            }
        }
    }

    private fun isBankSms(body: String, sender: String): Boolean {
        val knownSenderNumbers = listOf(
            "2000400", "30009411", "20008511", "989000", "200082", "300025", "200070"
        )
        val cleanSender = sender.replace("+98", "0").replace(" ", "")
        if (knownSenderNumbers.any { cleanSender.contains(it) || sender.contains(it) }) {
            return true
        }

        val bankKeywords = listOf(
            "برداشت", "واریز", "موجودیت", "کارت", "حساب", "بانک",
            "Melli", "Mellat", "Saman", "Pasargad", "Blue", "Sepah"
        )
        return bankKeywords.any { body.contains(it, ignoreCase = true) }
    }
}
