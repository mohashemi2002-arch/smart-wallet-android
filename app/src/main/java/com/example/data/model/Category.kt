package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val budgetLimitToman: Long = 0,
    val colorHex: String = "#00897B",
    val iconName: String = "category"
)
