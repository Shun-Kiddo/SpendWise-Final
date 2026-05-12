package com.example.spendwise.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spendwise.data.entity.BalanceHistoryEntity
import com.example.spendwise.data.entity.MonthlyBalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBalanceDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun saveCurrentBalance(entity: MonthlyBalanceEntity)

    @Insert
    suspend fun insertHistory(entity: BalanceHistoryEntity)

    @Query("SELECT * FROM monthly_balance WHERE month = :month LIMIT 1")
    fun getCurrentBalance(month: String): Flow<MonthlyBalanceEntity?>

    @Query("SELECT * FROM balance_history ORDER BY savedAt DESC")
    fun getHistory(): Flow<List<BalanceHistoryEntity>>
}