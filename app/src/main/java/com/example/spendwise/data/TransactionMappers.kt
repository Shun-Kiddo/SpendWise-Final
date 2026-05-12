package com.example.spendwise.data

import com.example.spendwise.data.entity.TransactionEntity
import com.example.spendwise.viewmodel.Transaction
import com.example.spendwise.viewmodel.TransactionType
import com.google.firebase.auth.FirebaseAuth


fun TransactionEntity.toTransaction(): Transaction = Transaction(
    id = id,
    userId = userId,
    title = title,
    amount = amount,
    type = type,
    date = date,
    notes = notes,
    synced = synced
)


fun Transaction.toEntity(): TransactionEntity {

    val finalUid = if (userId.isNotEmpty()) userId
    else FirebaseAuth.getInstance().currentUser?.uid ?: ""

    return TransactionEntity(
        id = id,
        userId = finalUid,
        title = title,
        amount = amount,
        type = type,
        date = date,
        notes = notes,
        synced = synced
    )
}