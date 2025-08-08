package com.example.dailyexpense.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val notes: String? = null,
    val imageUri: String? = null,
    val timestamp: Long =  System.currentTimeMillis()
)
