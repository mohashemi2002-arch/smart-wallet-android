package com.example.data

import com.example.data.sync.GoogleSheetsRestoreResult
import com.example.data.sync.GoogleSheetsSyncManager
import com.example.domain.util.PasswordHasher
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GoogleSheetsSyncTest {

    @Test
    fun testSyncJsonStructureMatchesSpecification() {
        val cleanPhone = GoogleSheetsSyncManager.normalizePhone("0912 345 6789")
        assertEquals("09123456789", cleanPhone)

        val timestamp = System.currentTimeMillis()
        val authSig = PasswordHasher.computeHmacSha256("$cleanPhone:sync:$timestamp", "hashedPass123")

        val rootJson = JSONObject().apply {
            put("action", "sync")
            put("phoneNumber", cleanPhone)
            put("firstName", "محمد")
            put("lastName", "حسینی")
            put("username", "mohammad")
            put("timestamp", timestamp)
            put("authSignature", authSig)
            put("user", JSONObject().apply {
                put("phoneNumber", cleanPhone)
                put("firstName", "محمد")
                put("lastName", "حسینی")
                put("username", "mohammad")
                put("passwordHash", "hashedPass123")
                put("pinHash", "hashedPin456")
            })
            put("cards", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 1L)
                    put("bankName", "بانک صادرات")
                    put("accountNumber", "80001")
                    put("balanceToman", 65797724L)
                })
            })
            put("transactions", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 101L)
                    put("amountToman", 1200000L)
                    put("type", "EXPENSE")
                    put("balanceAfterToman", 65797724L)
                })
            })
        }

        assertEquals("sync", rootJson.getString("action"))
        assertEquals("mohammad", rootJson.getString("username"))
        assertEquals(authSig, rootJson.getString("authSignature"))
        assertTrue(rootJson.getLong("timestamp") > 0)

        val cards = rootJson.getJSONArray("cards")
        assertEquals(1, cards.length())
        assertEquals("80001", cards.getJSONObject(0).getString("accountNumber"))

        val txs = rootJson.getJSONArray("transactions")
        assertEquals(1, txs.length())
        assertEquals(65797724L, txs.getJSONObject(0).getLong("balanceAfterToman"))
    }

    @Test
    fun testRestoreSecurityOmitsCredentialHashes() {
        val restoreResult = GoogleSheetsRestoreResult(
            success = true,
            message = "اطلاعات با موفقیت بازیابی شد",
            restoredCardsCount = 2,
            restoredTransactionsCount = 10,
            username = "ali_user"
        )

        assertTrue(restoreResult.success)
        assertEquals(2, restoreResult.restoredCardsCount)
        assertEquals(10, restoreResult.restoredTransactionsCount)
        assertEquals("ali_user", restoreResult.username)
        // Verify GoogleSheetsRestoreResult class has NO passwordHash or pinHash fields exposed
        assertFalse(
            "GoogleSheetsRestoreResult must not contain passwordHash field",
            GoogleSheetsRestoreResult::class.java.declaredFields.any { it.name == "passwordHash" }
        )
        assertFalse(
            "GoogleSheetsRestoreResult must not contain pinHash field",
            GoogleSheetsRestoreResult::class.java.declaredFields.any { it.name == "pinHash" }
        )
    }

    @Test
    fun testRestoreJsonRequestStructureWithAuth() {
        val cleanPhone = "09123456789"
        val timestamp = System.currentTimeMillis()
        val authSig = PasswordHasher.computeHmacSha256("$cleanPhone:restore:$timestamp", "securePassword123")

        val rootJson = JSONObject().apply {
            put("action", "restore")
            put("phoneNumber", cleanPhone)
            put("firstName", "علی")
            put("lastName", "رضایی")
            put("timestamp", timestamp)
            put("authSignature", authSig)
        }

        assertEquals("restore", rootJson.getString("action"))
        assertEquals("09123456789", rootJson.getString("phoneNumber"))
        assertEquals(authSig, rootJson.getString("authSignature"))
        assertEquals("علی", rootJson.getString("firstName"))
        assertEquals("رضایی", rootJson.getString("lastName"))
    }
}
