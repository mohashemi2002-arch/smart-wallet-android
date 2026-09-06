package com.example.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

data class SmsSenderItem(
    val address: String,
    val count: Int,
    val latestSnippet: String,
    val latestTimestamp: Long
)

object SmsInboxReader {
    fun getUniqueSenders(context: Context): List<SmsSenderItem> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

        val sendersMap = mutableMapOf<String, SmsSenderItem>()
        val uri: Uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            Telephony.Sms.DATE + " DESC LIMIT 1000"
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                val address = it.getString(addressIndex) ?: continue
                val body = it.getString(bodyIndex) ?: ""
                val date = it.getLong(dateIndex)
                
                if (sendersMap.containsKey(address)) {
                    val existing = sendersMap[address]!!
                    sendersMap[address] = existing.copy(count = existing.count + 1)
                } else {
                    sendersMap[address] = SmsSenderItem(
                        address = address,
                        count = 1,
                        latestSnippet = body.take(50) + if(body.length > 50) "..." else "",
                        latestTimestamp = date
                    )
                }
            }
        }
        return sendersMap.values.sortedByDescending { it.latestTimestamp }.toList()
    }
}
