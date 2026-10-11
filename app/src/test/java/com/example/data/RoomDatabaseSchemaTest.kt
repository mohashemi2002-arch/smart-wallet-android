package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomDatabaseSchemaTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testParsedSmsSchemaAndDao() = runBlocking {
        val sms = ParsedSmsEntity(
            senderAddress = "989000",
            bankName = "بانک ملی",
            rawSmsText = "بانك ملي ايران\nحواله پل:1,000,000-\nحساب:02003\nمانده:64,000\n0615-22:34",
            amountToman = 1000000L,
            type = TransactionType.EXPENSE,
            cardNumber = "02003",
            balanceToman = 64000L,
            dateMillis = System.currentTimeMillis(),
            transactionTimeText = "0615-22:34",
            categoryName = "انتقال وجه",
            merchantName = "حواله پل",
            status = SmsProcessingStatus.PENDING
        )

        val id = db.parsedSmsDao().insert(sms)
        assertTrue(id > 0)

        val retrieved = db.parsedSmsDao().getParsedSmsById(id)
        assertNotNull(retrieved)
        assertEquals("بانک ملی", retrieved?.bankName)
        assertEquals(1000000L, retrieved?.amountToman)
        assertEquals(64000L, retrieved?.balanceToman)
        assertEquals(SmsProcessingStatus.PENDING, retrieved?.status)

        // Test status update and linking with transaction
        db.parsedSmsDao().updateStatus(id, SmsProcessingStatus.CONFIRMED, 42L)
        val updated = db.parsedSmsDao().getParsedSmsById(id)
        assertEquals(SmsProcessingStatus.CONFIRMED, updated?.status)
        assertEquals(42L, updated?.linkedTransactionId)

        // Test raw text query
        val found = db.parsedSmsDao().findByRawText(sms.rawSmsText)
        assertNotNull(found)
        assertEquals(id, found?.id)
    }

    @Test
    fun testBudgetSchemaAndDao() = runBlocking {
        val budget = BudgetEntity(
            categoryName = "خوراکی و سوپرمارکت",
            amountLimitToman = 12000000L,
            period = BudgetPeriod.MONTHLY,
            jalaliYear = 1404,
            jalaliMonth = 6,
            warningThresholdPercent = 80,
            criticalThresholdPercent = 100,
            isActive = true
        )

        val id = db.budgetDao().insert(budget)
        assertTrue(id > 0)

        val activeList = db.budgetDao().getActiveBudgets().first()
        assertEquals(1, activeList.size)
        assertEquals("خوراکی و سوپرمارکت", activeList[0].categoryName)
        assertEquals(12000000L, activeList[0].amountLimitToman)

        val queried = db.budgetDao().getBudgetByCategoryNameSync("خوراکی و سوپرمارکت")
        assertNotNull(queried)
        assertEquals(80, queried?.warningThresholdPercent)

        // Update
        db.budgetDao().update(queried!!.copy(amountLimitToman = 15000000L))
        val updated = db.budgetDao().getBudgetById(id)
        assertEquals(15000000L, updated?.amountLimitToman)
    }

    @Test
    fun testAssetAndAssetTransactionSchema() = runBlocking {
        val asset = Asset(
            type = AssetType.GOLD_18K,
            name = "طلای ۱۸ عیار",
            totalQuantity = 10.5,
            currentUnitPriceToman = 4500000L,
            purchaseUnitPriceToman = 4000000L,
            unit = "گرم",
            notes = "خرید آب‌شده"
        )

        val assetId = db.assetDao().insert(asset)
        assertTrue(assetId > 0)

        val retrievedAsset = db.assetDao().getAssetById(assetId)
        assertNotNull(retrievedAsset)
        assertEquals(10.5, retrievedAsset!!.totalQuantity, 0.001)
        assertEquals(4500000L, retrievedAsset.currentUnitPriceToman)
        assertEquals(4000000L, retrievedAsset.purchaseUnitPriceToman)

        // Verify computed PnL
        assertEquals((10.5 * 4500000L).toLong(), retrievedAsset.totalValueToman)
        assertEquals((10.5 * 4000000L).toLong(), retrievedAsset.totalInvestedToman)
        val expectedPnl = retrievedAsset.totalValueToman - retrievedAsset.totalInvestedToman
        assertEquals(expectedPnl, retrievedAsset.unrealizedPnlToman)
        assertTrue(retrievedAsset.unrealizedPnlPercent > 12.0)

        // Insert asset transaction
        val tx = AssetTransactionEntity(
            assetId = assetId,
            type = AssetTransactionType.BUY,
            quantity = 5.0,
            unitPriceToman = 4000000L,
            dateTimestamp = System.currentTimeMillis(),
            notes = "پارت اول خرید"
        )
        val txId = db.assetTransactionDao().insertTransaction(tx)
        assertTrue(txId > 0)

        val txList = db.assetTransactionDao().getTransactionsForAsset(assetId).first()
        assertEquals(1, txList.size)
        assertEquals(5.0, txList[0].quantity, 0.001)
    }

    @Test
    fun testTransactionWithSmsMetadata() = runBlocking {
        val tx = Transaction(
            amountToman = 334840L,
            type = TransactionType.EXPENSE,
            categoryName = "خرید",
            merchantName = "پایانه فروش",
            cardNumber = "80001",
            dateMillis = System.currentTimeMillis(),
            rawSmsText = "بانك صادرات ايران\n خريد: 334,840-\n حساب:80001\n مانده:408,515,640\n 0620 - 12:28",
            isParsedFromSms = true,
            status = TransactionStatus.CONFIRMED,
            bankName = "بانک صادرات ایران",
            balanceAfterToman = 40851564L,
            senderNumber = "+9820008511"
        )

        val id = db.transactionDao().insert(tx)
        assertTrue(id > 0)

        val retrieved = db.transactionDao().getTransactionById(id)
        assertNotNull(retrieved)
        assertEquals("بانک صادرات ایران", retrieved?.bankName)
        assertEquals(40851564L, retrieved?.balanceAfterToman)
        assertEquals("+9820008511", retrieved?.senderNumber)
        assertTrue(retrieved?.isParsedFromSms == true)

        val smsList = db.transactionDao().getTransactionsParsedFromSms().first()
        assertEquals(1, smsList.size)
        assertEquals(id, smsList[0].id)
    }
}
