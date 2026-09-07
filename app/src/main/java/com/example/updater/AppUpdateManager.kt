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

    private val owner = BuildConfig.GITHUB_OWNER
    private val repo = BuildConfig.GITHUB_REPO
    private val token = BuildConfig.GITHUB_TOKEN

    private val updateService = GitHubUpdateService(owner, repo, token)
    private val client = OkHttpClient()

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    suspend fun checkForUpdates(isManualCheck: Boolean = false): GitHubRelease? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) return@withContext null
        
        // --- Mock Update System for Testing ---
        val currentVersion = BuildConfig.VERSION_NAME
        val mockTag = "9.9.9"
        
        if (currentVersion != mockTag) {
            val asset = GitHubAsset(id = 101, name = "update_test.apk", size = 10485760L, url = "https://proof.ovh.net/files/10Mb.dat")
            return@withContext GitHubRelease(tag_name = "v9.9.9", name = "آپدیت تستی (نسخه نمایشی)", body = "این یک پیغام تستی است.", assets = listOf(asset))
        }
        return@withContext null
    }

    suspend fun downloadUpdate(asset: GitHubAsset) = withContext(Dispatchers.IO) {
        _downloadState.value = DownloadState.Downloading(0f, "0", "0")
        try {
            val builder = Request.Builder().url(asset.url)
            if (token.isNotBlank()) builder.header("Authorization", "Bearer $token")
            builder.header("Accept", "application/octet-stream")
            builder.header("X-GitHub-Api-Version", "2022-11-28")
            
            val request = builder.build()

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
                
                val totalBytes = body.contentLength().takeIf { it > 0 } ?: asset.size
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
                
                _downloadState.value = DownloadState.ReadyToInstall(outputFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _downloadState.value = DownloadState.Error("خطا در دریافت: ${e.localizedMessage}")
        }
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
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
