package com.example.spendwise.viewmodel

import java.util.UUID
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val date: String,
    val notes: String = "",
    val synced: Boolean = false
)

data class UserProfile(
    val name: String,
    val phone: String,
    val email: String,
    val work: String
)

enum class TransactionType {
    INCOME, EXPENSE
}

data class MonthlySummary(
    val month: String,
    val income: Double,
    val expenses: Double
)