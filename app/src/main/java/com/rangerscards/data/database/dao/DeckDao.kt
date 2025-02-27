package com.rangerscards.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.DeckListItemProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDeck(deck: Deck)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: Deck)

    @Upsert
    suspend fun upsertAllDecks(decks: List<Deck>)

    @Query("DELETE FROM deck WHERE id NOT IN (:ids) AND uploaded = 1")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("DELETE FROM deck WHERE uploaded = 1")
    suspend fun deleteAllUploadedDecks()

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL " +
            "ORDER BY updated_at DESC"
    )
    fun getAllDecks(): PagingSource<Int, DeckListItemProjection>

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL " +
            "AND name LIKE :query ORDER BY updated_at DESC"
    )
    fun searchDecks(query: String): PagingSource<Int, DeckListItemProjection>

    @Query("Select id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level FROM card WHERE id = :id")
    fun getCard(id: String): Flow<CardListItemProjection>

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level FROM card WHERE type_id = 'role' AND set_id = :specialty"
    )
    fun getRoles(specialty: String): PagingSource<Int, CardListItemProjection>

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