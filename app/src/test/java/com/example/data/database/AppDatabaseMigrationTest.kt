package com.example.data.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppDatabaseMigrationTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testMigration1To2CreatesExpectedTables() {
        val dbName = "test_migration_1_2.db"
        context.deleteDatabase(dbName)

        // 1. Create v1 database structure
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amountToman` INTEGER NOT NULL, `type` TEXT NOT NULL, `categoryName` TEXT NOT NULL, `merchantName` TEXT NOT NULL, `cardNumber` TEXT, `dateMillis` INTEGER NOT NULL, `rawSmsText` TEXT, `isParsedFromSms` INTEGER NOT NULL, `status` TEXT NOT NULL)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, `color` TEXT NOT NULL, `type` TEXT NOT NULL)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        val v1Db = helper.writableDatabase
        v1Db.execSQL("INSERT INTO `transactions` (`amountToman`, `type`, `categoryName`, `merchantName`, `dateMillis`, `isParsedFromSms`, `status`) VALUES (50000, 'EXPENSE', 'خوراک', 'سوپرمارکت', 1700000000, 0, 'CONFIRMED')")
        v1Db.close()

        // 2. Run Migration 1 -> 2
        val migratedDb = helper.writableDatabase
        AppDatabaseMigrations.MIGRATION_1_2.migrate(migratedDb)

        // 3. Verify user data in transactions was preserved
        val cursor = migratedDb.query("SELECT amountToman, categoryName FROM transactions")
        assertTrue(cursor.moveToFirst())
        assertEquals(50000L, cursor.getLong(0))
        assertEquals("خوراک", cursor.getString(1))
        cursor.close()

        // 4. Verify new tables were successfully created
        val checkLoans = migratedDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='loans'")
        assertTrue("Loans table must exist after migration 1->2", checkLoans.moveToFirst())
        checkLoans.close()

        val checkInstallments = migratedDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='installments'")
        assertTrue("Installments table must exist after migration 1->2", checkInstallments.moveToFirst())
        checkInstallments.close()

        val checkCheques = migratedDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='cheques'")
        assertTrue("Cheques table must exist after migration 1->2", checkCheques.moveToFirst())
        checkCheques.close()

        migratedDb.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun testMigration7To8AddsMarketRatesAndNotes() {
        val dbName = "test_migration_7_8.db"
        context.deleteDatabase(dbName)

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(7) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amountToman` INTEGER NOT NULL, `type` TEXT NOT NULL, `categoryName` TEXT NOT NULL, `merchantName` TEXT NOT NULL, `cardNumber` TEXT, `dateMillis` INTEGER NOT NULL, `rawSmsText` TEXT, `isParsedFromSms` INTEGER NOT NULL, `status` TEXT NOT NULL)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        val v7Db = helper.writableDatabase
        v7Db.execSQL("INSERT INTO `transactions` (`amountToman`, `type`, `categoryName`, `merchantName`, `dateMillis`, `isParsedFromSms`, `status`) VALUES (120000, 'INCOME', 'حقوق', 'شرکت', 1710000000, 0, 'CONFIRMED')")

        // Run Migration 7 -> 8
        AppDatabaseMigrations.MIGRATION_7_8.migrate(v7Db)

        // Check user data preserved
        val cursor = v7Db.query("SELECT amountToman, categoryName FROM transactions")
        assertTrue(cursor.moveToFirst())
        assertEquals(120000L, cursor.getLong(0))
        cursor.close()

        // Check market_rates table created
        val checkRates = v7Db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='market_rates'")
        assertTrue(checkRates.moveToFirst())
        checkRates.close()

        v7Db.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun testMigration8To9AddsBudgetsAndParsedSmsRecords() {
        val dbName = "test_migration_8_9.db"
        context.deleteDatabase(dbName)

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(8) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amountToman` INTEGER NOT NULL, `type` TEXT NOT NULL, `categoryName` TEXT NOT NULL, `merchantName` TEXT NOT NULL, `cardNumber` TEXT, `dateMillis` INTEGER NOT NULL, `rawSmsText` TEXT, `isParsedFromSms` INTEGER NOT NULL, `status` TEXT NOT NULL, `notes` TEXT)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `assets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `name` TEXT NOT NULL, `totalQuantity` REAL NOT NULL, `currentUnitPriceToman` INTEGER NOT NULL, `unit` TEXT NOT NULL)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `asset_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `assetId` INTEGER NOT NULL, `type` TEXT NOT NULL, `quantity` REAL NOT NULL, `unitPriceToman` INTEGER NOT NULL, `dateTimestamp` INTEGER NOT NULL)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        val v8Db = helper.writableDatabase

        // Run Migration 8 -> 9
        AppDatabaseMigrations.MIGRATION_8_9.migrate(v8Db)

        // Verify budgets and parsed_sms_records exist
        val checkBudgets = v8Db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='budgets'")
        assertTrue(checkBudgets.moveToFirst())
        checkBudgets.close()

        val checkParsedSms = v8Db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='parsed_sms_records'")
        assertTrue(checkParsedSms.moveToFirst())
        checkParsedSms.close()

        v8Db.close()
        context.deleteDatabase(dbName)
    }
}
