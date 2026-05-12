package com.example.spendwise.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spendwise.data.entity.MonthlySummaryEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface MonthlySummaryDao {

    @Query("SELECT * FROM monthly_summaries ORDER BY month DESC")
    fun getAllSummaries(): Flow<List<MonthlySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSummary(summary: MonthlySummaryEntity)

    @Query("DELETE FROM monthly_summaries")
    suspend fun deleteAllSummaries()
}