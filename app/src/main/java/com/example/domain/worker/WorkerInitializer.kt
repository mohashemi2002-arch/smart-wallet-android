package com.example.domain.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkerInitializer {
    fun enqueueFinancialReminders(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<FinancialReminderWorker>(24, TimeUnit.HOURS)
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "FinancialReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
