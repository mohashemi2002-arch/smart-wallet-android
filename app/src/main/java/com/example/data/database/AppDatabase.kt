package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        Transaction::class,
        Category::class,
        Asset::class,
        AssetTransactionEntity::class,
        LoanEntity::class,
        InstallmentEntity::class,
        ChequeEntity::class,
        BankAccount::class,
        BankSmsSender::class,
        MarketRateEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun assetDao(): AssetDao
    abstract fun assetTransactionDao(): AssetTransactionDao
    abstract fun loanDao(): LoanDao
    abstract fun chequeDao(): ChequeDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun bankSmsSenderDao(): BankSmsSenderDao
    abstract fun marketRateDao(): MarketRateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart wallet_ai_database"
                )
                .addMigrations(AppDatabaseMigrations.MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
