package com.example.data

import com.example.data.sync.GoogleSheetsSyncManager
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

        val rootJson = JSONObject().apply {
            put("action", "sync")
            put("phoneNumber", cleanPhone)
            put("firstName", "محمد")
            put("lastName", "حسینی")
            put("username", "mohammad")
            put("passwordHash", "hashedPass123")
            put("pinHash", "hashedPin456")
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
        assertEquals("hashedPass123", rootJson.getString("passwordHash"))
        assertEquals("hashedPin456", rootJson.getString("pinHash"))
        val user = rootJson.getJSONObject("user")
        assertEquals("09123456789", user.getString("phoneNumber"))
        assertEquals("محمد", user.getString("firstName"))
        assertEquals("حسینی", user.getString("lastName"))
        assertEquals("mohammad", user.getString("username"))
        assertEquals("hashedPass123", user.getString("passwordHash"))
        assertEquals("hashedPin456", user.getString("pinHash"))

        val cards = rootJson.getJSONArray("cards")
        assertEquals(1, cards.length())
        assertEquals("80001", cards.getJSONObject(0).getString("accountNumber"))

        val txs = rootJson.getJSONArray("transactions")
        assertEquals(1, txs.length())
        assertEquals(65797724L, txs.getJSONObject(0).getLong("balanceAfterToman"))
    }

    @Test
    fun testRestoreJsonRequestStructure() {
        val rootJson = JSONObject().apply {
            put("action", "restore")
            put("phoneNumber", "09123456789")
            put("firstName", "علی")
            put("lastName", "رضایی")
        }

        assertEquals("restore", rootJson.getString("action"))
        assertEquals("09123456789", rootJson.getString("phoneNumber"))
        assertEquals("علی", rootJson.getString("firstName"))
        assertEquals("رضایی", rootJson.getString("lastName"))
    }
}
