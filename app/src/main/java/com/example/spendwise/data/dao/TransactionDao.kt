package com.example.spendwise.data.dao
import androidx.room.*
import com.example.spendwise.data.entity.HiddenCategoryEntity
import com.example.spendwise.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsByUser(userId: String): Flow<List<TransactionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)
    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId")
    suspend fun getTransactionCount(userId: String): Int
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)
    @Query("SELECT * FROM transactions WHERE userId = :userId AND synced = 0")
    suspend fun getUnsyncedTransactions(userId: String): List<TransactionEntity>
}


