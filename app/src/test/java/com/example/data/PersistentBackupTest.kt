package com.example.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.PersistentProfileBackupManager
import com.example.data.database.AppDatabase
import com.example.data.model.BankAccount
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PersistentBackupTest {

    @Test
    fun testCreateAndRestoreLocalBackup() = runBlocking(Dispatchers.IO) {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)

        // Seed some test data
        db.bankAccountDao().insert(
            BankAccount(
                bankName = "بانک صادرات",
                accountName = "حساب جاری",
                cardNumber = "6037-1234-5678-9012",
                accountNumber = "80001",
                balanceToman = 65000000L
            )
        )
        db.transactionDao().insert(
            Transaction(
                amountToman = 1500000L,
                type = TransactionType.EXPENSE,
                categoryName = "خوراکی",
                merchantName = "فروشگاه",
                cardNumber = "6037-1234-5678-9012",
                dateMillis = System.currentTimeMillis(),
                status = TransactionStatus.CONFIRMED,
                balanceAfterToman = 63500000L
            )
        )

        val backupManager = PersistentProfileBackupManager(app, db)
        val phone = "09121234567"

        // 1. Create Backup
        val backupResult = backupManager.createLocalBackup("علی", "محمدی", phone)
        assertTrue(backupResult.isSuccess)
        val backupFile = backupResult.getOrThrow()
        assertTrue(backupFile.exists())
        assertTrue(backupFile.length() > 0)

        // 2. Verify exists
        val exists = backupManager.checkBackupExists(phone)
        assertTrue(exists)

        // 3. Clear DB to simulate fresh install
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            db.clearAllTables()
        }
        assertEquals(0, db.bankAccountDao().getAllBankAccountsList().size)
        assertEquals(0, db.transactionDao().getAllTransactionsSync().size)

        // 4. Restore
        val restoreResult = backupManager.restoreLocalBackup(phone)
        assertTrue(restoreResult.isSuccess)
        val summary = restoreResult.getOrThrow()
        assertEquals(1, summary.restoredAccountsCount)
        assertEquals(1, summary.restoredTransactionsCount)
        assertEquals("علی محمدی", summary.ownerName)

        // Check restored DB records
        val restoredAccounts = db.bankAccountDao().getAllBankAccountsList()
        assertEquals(1, restoredAccounts.size)
        assertEquals("80001", restoredAccounts[0].accountNumber)
        assertEquals(65000000L, restoredAccounts[0].balanceToman)
    }
}
