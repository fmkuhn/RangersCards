package com.rangerscards.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDeck(deck: Deck)

    @Upsert
    suspend fun upsertAllDecks(decks: List<Deck>)

    @Query("DELETE FROM deck WHERE id NOT IN (:ids) AND uploaded = 1")
    suspend fun deleteNotIn(ids: List<Int>)

    @Query("DELETE FROM deck WHERE uploaded = 1")
    suspend fun deleteAllUploadedDecks()

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL " +
            "ORDER BY updated_at DESC"
    )
    fun getAllDecks(): PagingSource<Int, DeckListItemProjection>

    @Query("Select id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level FROM card WHERE id = :id")
    fun getCard(id: String): Flow<CardListItemProjection>

    @Transaction
    suspend fun syncDecks(networkData: List<Deck>) {
        // Insert or update all the network data.
        upsertAllDecks(networkData)

        if (networkData.isEmpty()) {
            // If the network data is empty, clear the rows with uploaded = true.
            deleteAllUploadedDecks()
        } else {
            // Otherwise, delete any rows not present in the network data.
            val networkIds = networkData.map { it.id }
            deleteNotIn(networkIds)
        }
    }
}