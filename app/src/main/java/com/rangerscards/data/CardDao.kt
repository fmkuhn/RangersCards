package com.rangerscards.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(cards: List<Card>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<Card>)

    @Query("SELECT EXISTS(SELECT * FROM card)")
    suspend fun isExists(): Boolean

    @Query("SELECT * FROM card WHERE spoiler = :spoiler " +
            "OR (spoiler IS NULL AND NOT EXISTS (" +
            "SELECT 1 FROM card WHERE spoiler = :spoiler)) " +
            "ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position"
    )
    fun getAllCards(spoiler: Boolean): Flow<List<Card>>
}