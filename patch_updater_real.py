with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'r') as f:
    content = f.read()

replacement = """
    suspend fun checkForUpdates(isManualCheck: Boolean = false): GitHubRelease? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) return@withContext null
        
        val latestRelease = updateService.getLatestRelease() ?: return@withContext null
        
        val currentVersion = BuildConfig.VERSION_NAME
        val latestTag = latestRelease.tag_name.removePrefix("v").removePrefix("V")
        
        if (latestTag != currentVersion && latestTag.isNotEmpty()) {
            return@withContext latestRelease
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
