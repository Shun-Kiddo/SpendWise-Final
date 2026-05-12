package com.example.spendwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spendwise.viewmodel.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val userId: String,
    val amount: Double,
    val date: String,
    val notes: String,
    val type: TransactionType,
    val synced: Boolean = false
)