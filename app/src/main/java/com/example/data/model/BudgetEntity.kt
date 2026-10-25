package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BudgetPeriod {
    MONTHLY,
    YEARLY,
    WEEKLY,
    CUSTOM
}

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["categoryName"]),
        Index(value = ["jalaliYear", "jalaliMonth"]),
        Index(value = ["isActive"])
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long? = null,
    val categoryName: String,
    val amountLimitToman: Long,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val jalaliYear: Int = 1404,
    val jalaliMonth: Int = 1, // 1..12, or 0 for entire year
    val warningThresholdPercent: Int = 80,
    val criticalThresholdPercent: Int = 100,
    val startDateMillis: Long = 0L,
    val endDateMillis: Long = 0L,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
