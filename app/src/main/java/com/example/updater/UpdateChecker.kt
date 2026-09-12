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
        Log.d("OTA", "checking update from ${BuildConfig.UPDATE_JSON_URL}")
        val request = Request.Builder()
            .url(BuildConfig.UPDATE_JSON_URL)
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: return null
                    val metadata = adapter.fromJson(bodyStr)
                    Log.d("OTA", "update metadata downloaded successfully. latest version: ${metadata?.versionCode}")
                    return metadata
                } else {
                    Log.e("OTA", "HTTP error ${response.code} when checking update")
                }
            }
        } catch (e: Exception) {
            Log.e("OTA", "Network failure when checking update", e)
            e.printStackTrace()
        }
        return null
    }
}
