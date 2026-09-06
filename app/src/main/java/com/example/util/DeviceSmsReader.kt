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
    val totalCount: Int        // How many messages from this sender
)

object DeviceSmsReader {
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
                    
                    if (sendersMap.containsKey(address)) {
                        val existing = sendersMap[address]!!
                        sendersMap[address] = existing.copy(totalCount = existing.totalCount + 1)
                    } else {
                        sendersMap[address] = SmsSenderPreview(
                            address = address,
                            lastMessage = body.take(70) + if(body.length > 70) "..." else "",
                            timestamp = date,
                            totalCount = 1
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
