package com.example.spendwise.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spendwise.data.entity.HiddenCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenCategoryDao {
    @Query("SELECT name FROM hidden_categories")
    fun getAllHidden(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: HiddenCategoryEntity)

    @Query("DELETE FROM hidden_categories WHERE name = :name")
    suspend fun delete(name: String)
}
