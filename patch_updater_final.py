with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'r') as f:
    content = f.read()

# Completely rewrite the checkForUpdates and downloadUpdate methods to be safe
replacement = """
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
"""

import re
content = re.sub(r'    suspend fun checkForUpdates.*?client\.newCall\(request\)\.execute\(\)\.use \{ response ->', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'w') as f:
    f.write(content)
