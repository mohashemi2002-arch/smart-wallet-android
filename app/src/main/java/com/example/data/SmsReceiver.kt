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
                        showNotification(context)
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "bank_sms_channel"
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Bank SMS Notifications",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for newly received bank SMS waiting for approval"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: android.app.PendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("تراکنش بانکی جدید")
            .setContentText("یک تراکنش جدید دریافت شد. برای تایید کلیک کنید.")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
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
