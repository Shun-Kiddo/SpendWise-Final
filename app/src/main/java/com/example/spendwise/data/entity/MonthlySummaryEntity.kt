package com.example.spendwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spendwise.viewmodel.MonthlySummary

@Entity(tableName = "monthly_summaries")
data class MonthlySummaryEntity(
    @PrimaryKey val month: String,
    val income: Double,
    val expenses: Double
)


fun MonthlySummaryEntity.toUi(): MonthlySummary {
    return MonthlySummary(
        month = month,
        income = income,
        expenses = expenses
    )
}