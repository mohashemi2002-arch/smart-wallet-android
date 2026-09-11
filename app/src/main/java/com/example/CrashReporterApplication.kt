package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log

class CrashReporterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("CrashReporter", "Uncaught exception", exception)
            val prefs = getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("last_crash", Log.getStackTraceString(exception)).commit()
            
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}
