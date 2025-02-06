package com.rangerscards.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

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
    fun getAllCards(spoiler: Boolean): PagingSource<Int, Card>

    @Query("SELECT card.* FROM card JOIN card_fts ON (card.id = card_fts.id) " +
            "WHERE (spoiler = :spoiler OR (spoiler IS NULL AND NOT EXISTS " +
            "(SELECT 1 FROM card WHERE spoiler = :spoiler))) " +
            "AND (card_fts MATCH :query) " +
            "ORDER BY (card.set_type_id IS NULL), card.set_type_id, card.set_id, card.set_position"
    )
    fun searchCards(query: String, spoiler: Boolean): PagingSource<Int, Card>
}