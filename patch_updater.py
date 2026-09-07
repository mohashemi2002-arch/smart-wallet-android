with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'r') as f:
    content = f.read()

mock_code = """
    suspend fun checkForUpdates(isManualCheck: Boolean = false): GitHubRelease? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) return@withContext null
        
        // --- Mock Update System for Testing ---
        val currentVersion = BuildConfig.VERSION_NAME
        val mockTag = "9.9.9"
        
        if (currentVersion != mockTag) {
            return@withContext GitHubRelease(
                tag_name = "v$mockTag",
                name = "آپدیت تستی (نسخه نمایشی)",
                body = "این یک پیغام تستی برای بررسی سلامت رابط کاربری آپدیتر است.\\nبرای تست، یک فایل موقت دانلود می‌شود.",
                assets = listOf(
                    GitHubAsset(
                        id = 101,
                        name = "update_test.apk",
                        size = 10485760, // 10 MB
                        url = "https://proof.ovh.net/files/10Mb.dat" // Safe public test file
                    )
                )
            )
        }
        // --------------------------------------

        val latestRelease = updateService.getLatestRelease() ?: return@withContext null
        
        val latestTag = latestRelease.tag_name.removePrefix("v").removePrefix("V")
        
        if (latestTag != currentVersion && latestTag.isNotEmpty()) {
            return@withContext latestRelease
        }
        return@withContext null
    }
"""

import re
# Replace the checkForUpdates function
content = re.sub(
    r'suspend fun checkForUpdates.*?return@withContext null\n    }',
    mock_code.strip(),
    content,
    flags=re.DOTALL
)

# Also fix the token header to avoid empty bearer errors
content = content.replace('.header("Authorization", "Bearer $token")', 'if (token.isNotBlank()) header("Authorization", "Bearer $token") else this')

with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'w') as f:
    f.write(content)
