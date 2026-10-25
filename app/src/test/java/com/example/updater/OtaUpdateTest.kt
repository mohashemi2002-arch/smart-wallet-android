package com.example.updater

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OtaUpdateTest {

    private fun parseVersionCodeFromTag(tagName: String): Int {
        val clean = tagName.trim().removePrefix("v").removePrefix("V")
        val parts = clean.split(".")
        return parts.lastOrNull()?.toIntOrNull() ?: clean.toIntOrNull() ?: 0
    }

    @Test
    fun testTagVersionParsing() {
        assertEquals(15, parseVersionCodeFromTag("v1.0.15"))
        assertEquals(24, parseVersionCodeFromTag("1.0.24"))
        assertEquals(99, parseVersionCodeFromTag("v99"))
        assertEquals(1, parseVersionCodeFromTag("v1.0.1"))
    }

    @Test
    fun testGitHubReleaseJsonParsingForApkAsset() {
        val jsonString = """
        {
          "tag_name": "v1.0.18",
          "name": "نسخه جدید پولکی v1.0.18",
          "body": "بهبود سیستم پردازش پیامک",
          "assets": [
            {
              "name": "source.zip",
              "size": 1024,
              "browser_download_url": "https://github.com/mohashemi2002-arch/smart-wallet-android/archive/refs/tags/v1.0.18.zip"
            },
            {
              "name": "BankManager-v1.0.18.apk",
              "size": 25480000,
              "browser_download_url": "https://github.com/mohashemi2002-arch/smart-wallet-android/releases/download/v1.0.18/BankManager-v1.0.18.apk"
            }
          ]
        }
        """.trimIndent()

        val json = JSONObject(jsonString)
        val tagName = json.getString("tag_name")
        assertEquals("v1.0.18", tagName)

        val assets = json.getJSONArray("assets")
        var apkUrl: String? = null
        var apkName: String? = null

        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            if (name.endsWith(".apk", ignoreCase = true)) {
                apkUrl = asset.getString("browser_download_url")
                apkName = name
                break
            }
        }

        assertNotNull(apkUrl)
        assertEquals("BankManager-v1.0.18.apk", apkName)
        assertTrue(apkUrl!!.endsWith(".apk"))
    }
}
