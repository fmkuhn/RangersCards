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

    @Query("SELECT * FROM card WHERE spoiler = :spoiler")
    fun getAllCards(spoiler: Boolean): Flow<List<Card>>
}