package com.example.spendwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_balance")
data class MonthlyBalanceEntity(
    @PrimaryKey val month: String,
    val remainingBalance: Double,
    val resetTimestamp: Long
)

@Entity(tableName = "balance_history")
data class BalanceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val month: String,
    val closingBalance: Double,
    val savedAt: Long
)