with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'r') as f:
    content = f.read()

replacement = """
    suspend fun downloadUpdate(asset: GitHubAsset) = withContext(Dispatchers.IO) {
        _downloadState.value = DownloadState.Downloading(0f, "0", "0")
        try {
            // Use browser_download_url if available, fallback to api url
            val targetUrl = if (asset.browser_download_url.isNotBlank()) asset.browser_download_url else asset.url
            val builder = Request.Builder().url(targetUrl)
            
            // Only add auth headers if using the API URL, because browser_download_url (S3) will reject the token
            if (targetUrl == asset.url) {
                if (token.isNotBlank()) builder.header("Authorization", "Bearer $token")
                builder.header("Accept", "application/octet-stream")
                builder.header("X-GitHub-Api-Version", "2022-11-28")
            }
            
            val request = builder.build()

            client.newCall(request).execute().use { response ->
"""

import re
content = re.sub(r'    suspend fun downloadUpdate.*?client\.newCall\(request\)\.execute\(\)\.use \{ response ->', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'w') as f:
    f.write(content)
