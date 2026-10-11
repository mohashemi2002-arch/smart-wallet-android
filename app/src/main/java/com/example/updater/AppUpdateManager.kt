package com.example.updater

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float, val downloadedMB: String, val totalMB: String) : DownloadState()
    data class ReadyToInstall(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class AppUpdateManager(private val context: Context) {
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val updateChecker = UpdateChecker()
    private val client = OkHttpClient()

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun checkForUpdates(isManualCheck: Boolean = false): UpdateMetadata? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            android.util.Log.d("OTA", "Network unavailable, skipping update check")
            return@withContext null
        }
        
        val metadata = updateChecker.getUpdateMetadata() ?: return@withContext null
        
        val currentVersionCode = BuildConfig.VERSION_CODE
        
        // Remote versionCode > installed versionCode -> Update available
        if (metadata.versionCode > currentVersionCode) {
            android.util.Log.d("OTA", "OTA: update available")
            return@withContext metadata
        }
        android.util.Log.d("OTA", "OTA: no update available")
        return@withContext null
    }

    suspend fun downloadUpdate(metadata: UpdateMetadata) = withContext(Dispatchers.IO) {
        _downloadState.value = DownloadState.Downloading(0f, "0", "0")
        android.util.Log.d("OTA", "OTA: downloading APK from ${metadata.apkUrl}")
        try {
            val request = Request.Builder().url(metadata.apkUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _downloadState.value = DownloadState.Error("خطا در دانلود (${response.code})")
                    return@use
                }
                
                val body = response.body ?: throw IllegalStateException("محتوای فایل خالی است")
                
                val updateDir = File(context.cacheDir, "updates")
                if (!updateDir.exists()) updateDir.mkdirs()
                val outputFile = File(updateDir, "update_release.apk")
                if (outputFile.exists()) outputFile.delete()
                
                val totalBytes = body.contentLength().takeIf { it > 0 } ?: 0L
                var downloadedBytes = 0L
                val buffer = ByteArray(8 * 1024)
                
                body.byteStream().use { input ->
                    FileOutputStream(outputFile).use { output ->
                        var bytesRead = input.read(buffer)
                        while (bytesRead != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            
                            val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes.toFloat() else 0f
                            val dlMb = String.format(Locale.US, "%.1f", downloadedBytes / (1024f * 1024f))
                            val totMb = String.format(Locale.US, "%.1f", totalBytes / (1024f * 1024f))
                            
                            _downloadState.value = DownloadState.Downloading(progress, dlMb, totMb)
                            
                            bytesRead = input.read(buffer)
                        }
                    }
                }
                
                android.util.Log.d("OTA", "OTA: download completed")
                
                // SHA-256 verification
                if (!metadata.sha256.isNullOrBlank()) {
                    android.util.Log.d("OTA", "OTA: verifying SHA-256")
                    val calculatedSha = calculateSHA256(outputFile)
                    if (!calculatedSha.equals(metadata.sha256, ignoreCase = true)) {
                        android.util.Log.e("OTA", "OTA: verification failed. Expected ${metadata.sha256}, got $calculatedSha")
                        outputFile.delete()
                        _downloadState.value = DownloadState.Error("خطا در تایید فایل (SHA-256 نامعتبر)")
                        return@use
                    }
                    android.util.Log.d("OTA", "OTA: verification successful")
                }
                
                _downloadState.value = DownloadState.ReadyToInstall(outputFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("OTA", "OTA: APK download failure", e)
            _downloadState.value = DownloadState.Error("خطا در دریافت: ${e.localizedMessage}")
        }
    }

    private fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read = input.read(buffer)
            while (read > 0) {
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun installUpdate(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
        }
        
        try {
            val packageInstaller = context.packageManager.packageInstaller
            val params = android.content.pm.PackageInstaller.SessionParams(android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                params.setRequireUserAction(android.content.pm.PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            }
            
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)
            
            val out = session.openWrite("app_update", 0, -1)
            java.io.FileInputStream(file).use { input ->
                input.copyTo(out)
            }
            session.fsync(out)
            out.close()
            
            val intent = Intent(context, InstallResultReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                3439,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
            )
            
            session.commit(pendingIntent.intentSender)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to ACTION_VIEW
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
