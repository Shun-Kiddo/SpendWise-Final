package com.example.spendwise.viewmodel

import androidx.room.TypeConverter

class TransactionTypeConverter {

    @TypeConverter
    fun fromType(type: TransactionType): String = type.name

    @TypeConverter
    fun toType(value: String): TransactionType = TransactionType.valueOf(value)
}