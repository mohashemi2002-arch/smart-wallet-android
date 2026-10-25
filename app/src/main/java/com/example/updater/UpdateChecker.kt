package com.example.updater

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request

@JsonClass(generateAdapter = true)
data class UpdateMetadata(
    val versionCode: Int,
    val versionName: String,
    val mandatory: Boolean,
    val minSupportedVersionCode: Int,
    val apkUrl: String,
    val sha256: String? = null,
    val releaseNotes: List<String> = emptyList()
)

class UpdateChecker {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(UpdateMetadata::class.java)

    fun getUpdateMetadata(): UpdateMetadata? {
        val updateUrl = BuildConfig.UPDATE_JSON_URL
        if (updateUrl.isNullOrBlank()) {
            Log.d("OTA", "Update URL is not configured")
            return null
        }
        Log.d("OTA", "Checking update from $updateUrl")
        val request = Request.Builder()
            .url(updateUrl)
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: return null
                    val metadata = adapter.fromJson(bodyStr)
                    Log.d("OTA", "Update metadata downloaded successfully. Latest version: ${metadata?.versionCode}")
                    return metadata
                } else if (response.code == 404) {
                    Log.d("OTA", "No remote update manifest found (HTTP 404). App is up to date.")
                } else {
                    Log.w("OTA", "Server returned HTTP ${response.code} when checking update")
                }
            }
        } catch (e: Exception) {
            Log.w("OTA", "Network failure when checking update: ${e.message}")
        }
        return null
    }
}
