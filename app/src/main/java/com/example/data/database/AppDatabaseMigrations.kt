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
}
