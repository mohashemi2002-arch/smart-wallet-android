package com.example.updater

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import android.widget.Toast

class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                }
                
                if (confirmationIntent != null) {
                    confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        context.startActivity(confirmationIntent)
                    } catch (e: Exception) {
                        Log.e("InstallResultReceiver", "Error starting confirmation intent", e)
                    }
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(context, "بروزرسانی با موفقیت انجام شد", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.e("InstallResultReceiver", "Install failed: $status, $message")
                Toast.makeText(context, "خطا در نصب برنامه: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
