package com.example.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `loans` (
                    `id` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `totalAmount` INTEGER NOT NULL,
                    `installmentAmount` INTEGER NOT NULL,
                    `installmentCount` INTEGER NOT NULL,
                    `paidCount` INTEGER NOT NULL,
                    `startDateTimestamp` INTEGER NOT NULL,
                    `isSettled` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
            """)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `installments` (
                    `id` TEXT NOT NULL,
                    `loanId` TEXT NOT NULL,
                    `installmentNumber` INTEGER NOT NULL,
                    `amount` INTEGER NOT NULL,
                    `dueDateTimestamp` INTEGER NOT NULL,
                    `isPaid` INTEGER NOT NULL,
                    `paidDateTimestamp` INTEGER,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`loanId`) REFERENCES `loans`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_installments_loanId` ON `installments` (`loanId`)")
            
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `cheques` (
                    `id` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `amount` INTEGER NOT NULL,
                    `dueDateTimestamp` INTEGER NOT NULL,
                    `sayadNumber` TEXT NOT NULL,
                    `bankName` TEXT NOT NULL,
                    `partyName` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
            """)
            
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `asset_items` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `quantity` REAL NOT NULL,
                    `purchaseUnitPrice` INTEGER NOT NULL,
                    `currentUnitPrice` INTEGER NOT NULL,
                    `type` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
            """)
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Safely create market_rates cache table and add transaction notes without dropping user data
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `market_rates` (
                    `symbol` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `priceToman` INTEGER NOT NULL,
                    `changePercent` TEXT NOT NULL,
                    `isPositive` INTEGER NOT NULL,
                    `lastUpdatedTimestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`symbol`)
                )
            """)
            try {
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `notes` TEXT")
            } catch (_: Exception) {
                // Column already exists
            }
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Create parsed_sms_records
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `parsed_sms_records` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `smsId` INTEGER,
                    `senderAddress` TEXT NOT NULL,
                    `bankName` TEXT NOT NULL,
                    `rawSmsText` TEXT NOT NULL,
                    `amountToman` INTEGER NOT NULL,
                    `type` TEXT NOT NULL,
                    `cardNumber` TEXT,
                    `balanceToman` INTEGER,
                    `dateMillis` INTEGER NOT NULL,
                    `transactionTimeText` TEXT,
                    `categoryName` TEXT NOT NULL,
                    `merchantName` TEXT NOT NULL,
                    `confidence` REAL NOT NULL,
                    `status` TEXT NOT NULL,
                    `linkedTransactionId` INTEGER,
                    `notes` TEXT,
                    `createdAt` INTEGER NOT NULL
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_parsed_sms_records_rawSmsText` ON `parsed_sms_records` (`rawSmsText`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_parsed_sms_records_status` ON `parsed_sms_records` (`status`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_parsed_sms_records_dateMillis` ON `parsed_sms_records` (`dateMillis`)")

            // 2. Create budgets
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `budgets` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `categoryId` INTEGER,
                    `categoryName` TEXT NOT NULL,
                    `amountLimitToman` INTEGER NOT NULL,
                    `period` TEXT NOT NULL,
                    `jalaliYear` INTEGER NOT NULL,
                    `jalaliMonth` INTEGER NOT NULL,
                    `warningThresholdPercent` INTEGER NOT NULL,
                    `criticalThresholdPercent` INTEGER NOT NULL,
                    `startDateMillis` INTEGER NOT NULL,
                    `endDateMillis` INTEGER NOT NULL,
                    `isActive` INTEGER NOT NULL,
                    `notes` TEXT,
                    `createdAt` INTEGER NOT NULL
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_categoryName` ON `budgets` (`categoryName`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_jalaliYear_jalaliMonth` ON `budgets` (`jalaliYear`, `jalaliMonth`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_isActive` ON `budgets` (`isActive`)")

            // 3. Transactions columns and indices
            try { db.execSQL("ALTER TABLE `transactions` ADD COLUMN `bankName` TEXT") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE `transactions` ADD COLUMN `balanceAfterToman` INTEGER") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE `transactions` ADD COLUMN `senderNumber` TEXT") } catch (_: Exception) {}
            try { db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_dateMillis` ON `transactions` (`dateMillis`)") } catch (_: Exception) {}
            try { db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_status` ON `transactions` (`status`)") } catch (_: Exception) {}
            try { db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryName` ON `transactions` (`categoryName`)") } catch (_: Exception) {}
            try { db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_isParsedFromSms` ON `transactions` (`isParsedFromSms`)") } catch (_: Exception) {}

            // 4. Asset columns
            try { db.execSQL("ALTER TABLE `assets` ADD COLUMN `purchaseUnitPriceToman` INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE `assets` ADD COLUMN `notes` TEXT") } catch (_: Exception) {}

            // 5. AssetTransaction columns
            try { db.execSQL("ALTER TABLE `asset_transactions` ADD COLUMN `notes` TEXT") } catch (_: Exception) {}
        }
    }
}
