package com.example.updater

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.ui.components.PersianFormatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

data class OtaReleaseInfo(
    val tagName: String,
    val releaseName: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val apkSize: Long,
    val remoteVersionCode: Int
)

sealed class OtaDownloadState {
    object Idle : OtaDownloadState()
    data class Downloading(val progress: Float, val currentMb: String, val totalMb: String) : OtaDownloadState()
    data class Success(val file: File) : OtaDownloadState()
    data class Error(val message: String) : OtaDownloadState()
}

class OtaUpdateManager(private val context: Context) {

    companion object {
        private const val TAG = "OtaUpdateManager"
        const val GITHUB_OWNER = "mohashemi2002-arch"
        const val GITHUB_REPO = "smart-wallet-android"
        private const val GITHUB_RELEASES_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

        private val client: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }

    private val _downloadState = MutableStateFlow<OtaDownloadState>(OtaDownloadState.Idle)
    val downloadState: StateFlow<OtaDownloadState> = _downloadState.asStateFlow()

    private fun isNetworkConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun parseVersionCodeFromTag(tagName: String): Int {
        // Examples: "v1.0.15", "1.0.15", "v15", "15"
        val clean = tagName.trim().removePrefix("v").removePrefix("V")
        val parts = clean.split(".")
        return parts.lastOrNull()?.toIntOrNull() ?: clean.toIntOrNull() ?: 0
    }

    suspend fun checkForOtaUpdate(): OtaReleaseInfo? = withContext(Dispatchers.IO) {
        if (!isNetworkConnected()) {
            Log.d(TAG, "No internet connection, skipping OTA check")
            return@withContext null
        }

        try {
            val request = Request.Builder()
                .url(GITHUB_RELEASES_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "SmartWallet-Android-OTA")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "GitHub API returned HTTP ${response.code}")
                return@withContext null
            }

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return@withContext null

            val json = JSONObject(bodyString)
            val tagName = json.optString("tag_name", "")
            val releaseName = json.optString("name", "نسخه جدید پولکی")
            val releaseNotes = json.optString("body", "بهبود عملکرد و ارتقای امکانات برنامه")

            val remoteVersionCode = parseVersionCodeFromTag(tagName)
            val currentVersionCode = BuildConfig.VERSION_CODE

            Log.d(TAG, "Current versionCode: $currentVersionCode, Remote tag: $tagName (parsed: $remoteVersionCode)")

            // Check if remote version is strictly newer
            val isNewer = if (remoteVersionCode > 0 && currentVersionCode > 0) {
                remoteVersionCode > currentVersionCode
            } else {
                // Fallback to name/string comparison if versionCode is 1
                val cleanTag = tagName.removePrefix("v").removePrefix("V")
                val cleanCurrent = BuildConfig.VERSION_NAME.removePrefix("v").removePrefix("V")
                cleanTag != cleanCurrent
            }

            if (!isNewer) {
                Log.d(TAG, "App is already up to date")
                return@withContext null
            }

            // Find APK asset
            val assets = json.optJSONArray("assets") ?: return@withContext null
            var apkUrl: String? = null
            var apkSize: Long = 0L

            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name", "")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url")
                    apkSize = asset.optLong("size", 0L)
                    break
                }
            }

            if (apkUrl.isNullOrBlank()) {
                Log.w(TAG, "No .apk asset found in GitHub release")
                return@withContext null
            }

            OtaReleaseInfo(
                tagName = tagName,
                releaseName = releaseName,
                releaseNotes = releaseNotes,
                apkDownloadUrl = apkUrl,
                apkSize = apkSize,
                remoteVersionCode = remoteVersionCode
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for OTA update", e)
            null
        }
    }

    suspend fun downloadAndInstall(releaseInfo: OtaReleaseInfo) = withContext(Dispatchers.IO) {
        _downloadState.value = OtaDownloadState.Downloading(0f, "0.0", "0.0")
        Log.d(TAG, "Downloading OTA update from: ${releaseInfo.apkDownloadUrl}")

        try {
            val request = Request.Builder()
                .url(releaseInfo.apkDownloadUrl)
                .header("User-Agent", "SmartWallet-Android-OTA")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                _downloadState.value = OtaDownloadState.Error("خطا در دانلود فایل (کد ${response.code})")
                return@withContext
            }

            val body = response.body ?: throw IllegalStateException("پاسخ دریافتی خالی است")
            val updateDir = File(context.cacheDir, "updates")
            if (!updateDir.exists()) updateDir.mkdirs()

            val outputFile = File(updateDir, "BankManager-latest.apk")
            if (outputFile.exists()) outputFile.delete()

            val totalBytes = if (body.contentLength() > 0) body.contentLength() else releaseInfo.apkSize
            var downloadedBytes = 0L
            val buffer = ByteArray(8 * 1024)

            body.byteStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    var read = input.read(buffer)
                    while (read != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes.toFloat() else 0f
                        val dlMb = String.format(Locale.US, "%.1f", downloadedBytes / (1024f * 1024f))
                        val totMb = String.format(Locale.US, "%.1f", totalBytes / (1024f * 1024f))

                        _downloadState.value = OtaDownloadState.Downloading(progress, dlMb, totMb)
                        read = input.read(buffer)
                    }
                }
            }

            Log.d(TAG, "Download finished successfully: ${outputFile.absolutePath}")
            _downloadState.value = OtaDownloadState.Success(outputFile)

            // Prompt install immediately
            withContext(Dispatchers.Main) {
                installApk(outputFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading OTA update", e)
            _downloadState.value = OtaDownloadState.Error("خطا در دانلود بروزرسانی: ${e.localizedMessage}")
        }
    }

    fun installApk(file: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(permissionIntent)
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching package installer", e)
            _downloadState.value = OtaDownloadState.Error("خطا در باز کردن نصاب اندروید: ${e.localizedMessage}")
        }
    }

    fun resetDownloadState() {
        _downloadState.value = OtaDownloadState.Idle
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtaUpdateDialog(
    releaseInfo: OtaReleaseInfo,
    otaManager: OtaUpdateManager,
    onDismiss: () -> Unit
) {
    val downloadState by otaManager.downloadState.collectAsState()
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BasicAlertDialog(
            onDismissRequest = {
                if (downloadState !is OtaDownloadState.Downloading) {
                    onDismiss()
                }
            },
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF5C82E6).copy(alpha = 0.15f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = Color(0xFF8AB4F8),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Text(
                        text = "نسخه جدید برنامه آماده نصب است",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF283446),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نسخه جدید: ${releaseInfo.tagName}",
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "نسخه فعلی: v${BuildConfig.VERSION_NAME}",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (releaseInfo.releaseNotes.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "توضیحات بروزرسانی:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF8AB4F8)
                            )
                            Text(
                                text = releaseInfo.releaseNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCFD8DC),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    when (val state = downloadState) {
                        is OtaDownloadState.Downloading -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { state.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = Color(0xFF5C82E6),
                                    trackColor = Color(0xFF2C394E)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${(state.progress * 100).toInt()}% دانلود شده",
                                        fontSize = 12.sp,
                                        color = Color(0xFF8AB4F8),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${state.currentMb} / ${state.totalMb} مگابایت",
                                        fontSize = 11.sp,
                                        color = Color(0xFF90A4AE)
                                    )
                                }
                            }
                        }

                        is OtaDownloadState.Success -> {
                            Text(
                                text = "فایل دانلود شد! در حال آماده‌سازی پنجره نصب...",
                                color = Color(0xFF81C784),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { otaManager.installApk(state.file) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("نصب بروزرسانی", fontWeight = FontWeight.Bold)
                            }
                        }

                        is OtaDownloadState.Error -> {
                            Text(
                                text = state.message,
                                color = Color(0xFFE57373),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        otaManager.downloadAndInstall(releaseInfo)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("تلاش مجدد دانلود", fontWeight = FontWeight.Bold)
                            }
                        }

                        is OtaDownloadState.Idle -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("بعداً", color = Color(0xFFB0BEC5))
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            otaManager.downloadAndInstall(releaseInfo)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "دریافت و نصب بروزرسانی",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
