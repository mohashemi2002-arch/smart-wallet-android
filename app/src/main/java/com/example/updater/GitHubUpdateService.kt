package com.example.updater

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request

@JsonClass(generateAdapter = true)
data class GitHubRelease(
    val tag_name: String = "",
    val name: String = "",
    val body: String = "",
    val assets: List<GitHubAsset> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GitHubAsset(
    val id: Long = 0,
    val name: String = "",
    val size: Long = 0,
    val url: String = "",
    val browser_download_url: String = ""
)

class GitHubUpdateService(private val owner: String, private val repo: String, private val token: String) {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(GitHubRelease::class.java)

    fun getLatestRelease(): GitHubRelease? {
        if (owner.isBlank() || repo.isBlank() || token.isBlank()) return null
        
        val request = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/releases/latest")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: return null
                    return adapter.fromJson(bodyStr)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
