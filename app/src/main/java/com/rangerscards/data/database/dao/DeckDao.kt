package com.rangerscards.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.rangerscards.data.database.card.CardDeckListItemProjection
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.DeckListItemProjection
import com.rangerscards.data.database.deck.RoleCardProjection
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

    @Query("SELECT * FROM deck WHERE id = :id")
    suspend fun getDeckById(id: String): Deck

    @Query("Select id, name, text, real_image_src FROM card WHERE id = :id")
    suspend fun getRole(id: String): RoleCardProjection

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE id IN (:ids) ORDER BY set_type_id, set_id, set_position")
    fun getCardsByIds(ids: List<String>): Flow<List<CardDeckListItemProjection>>

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE id IN (:ids) ORDER BY set_type_id, set_id, set_position")
    suspend fun getChangedCardsByIds(ids: List<String>): List<CardDeckListItemProjection>


    //Queries for deck pagination without searching
    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE set_id = 'personality' ORDER BY aspect_id, set_position")
    fun getPersonalityCards(): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE set_id = :background AND set_type_id = 'background' AND type_id != 'role' " +
            "ORDER BY aspect_id, set_position")
    fun getBackgroundCards(background: String): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE set_id = :specialty AND set_type_id = 'specialty' AND type_id != 'role' " +
            "ORDER BY aspect_id, set_position")
    fun getSpecialtyCards(specialty: String): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE set_id != :background AND set_id != :specialty AND set_id != 'personality' " +
            "AND type_id != 'role' AND real_traits NOT LIKE '%expert%' " +
            "ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun getOutsideInterestCards(background: String, specialty: String): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE set_id == 'reward' ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun getAllRewards(): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE id IN (:rewards) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun getRewards(rewards: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE set_id == 'malady' ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun getAllMaladies(): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE spoiler = 'false' OR (spoiler IS NULL AND NOT EXISTS (" +
            "SELECT 1 FROM card WHERE spoiler = 'false')) AND type_id != 'role'" +
            "ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position"
    )
    fun getAllCards(): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, set_id, set_type_id, deck_limit FROM card " +
            "WHERE id IN (:ids) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun getExtraCards(ids: List<String>): PagingSource<Int, CardDeckListItemProjection>


    //Queries for deck pagination with searching
    @Query("SELECT card.id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost, " +
            "card.real_image_src, card.name, card.type_name, card.traits, card.level, card.set_id, " +
            "card.set_type_id, card.deck_limit FROM card JOIN card_fts ON (card.id = card_fts.id) " +
            "WHERE set_id = 'personality' AND (card_fts MATCH :query) ORDER BY aspect_id, set_position")
    fun searchPersonalityCards(query: String): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT card.id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost, " +
            "card.real_image_src, card.name, card.type_name, card.traits, card.level, card.set_id, " +
            "card.set_type_id, card.deck_limit FROM card JOIN card_fts ON (card.id = card_fts.id)" +
            "WHERE set_id = :background AND set_type_id = 'background' AND type_id != 'role' " +
            "AND (card_fts MATCH :query) ORDER BY aspect_id, set_position")
    fun searchBackgroundCards(query: String, background: String): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT card.id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost, " +
            "card.real_image_src, card.name, card.type_name, card.traits, card.level, card.set_id, " +
            "card.set_type_id, card.deck_limit FROM card JOIN card_fts ON (card.id = card_fts.id)" +
            "WHERE set_id = :specialty AND set_type_id = 'specialty' AND type_id != 'role' " +
            "AND (card_fts MATCH :query) ORDER BY aspect_id, set_position")
    fun searchSpecialtyCards(query: String, specialty: String): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT card.id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost, " +
            "card.real_image_src, card.name, card.type_name, card.traits, card.level, card.set_id, " +
            "card.set_type_id, card.deck_limit FROM card JOIN card_fts ON (card.id = card_fts.id)" +
            "WHERE set_id != :background AND set_id != :specialty AND type_id != 'role' AND " +
            "set_id != 'personality' AND real_traits NOT LIKE '%expert%' AND (card_fts MATCH :query) " +
            "ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun searchOutsideInterestCards(query: String, background: String, specialty: String): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT card.id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost, " +
            "card.real_image_src, card.name, card.type_name, card.traits, card.level, card.set_id, " +
            "card.set_type_id, card.deck_limit FROM card JOIN card_fts ON (card.id = card_fts.id)" +
            "WHERE set_id == 'reward' AND (card_fts MATCH :query) " +
            "ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun searchAllRewards(query: String): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT card.id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost, " +
            "card.real_image_src, card.name, card.type_name, card.traits, card.level, card.set_id, " +
            "card.set_type_id, card.deck_limit FROM card JOIN card_fts ON (card.id = card_fts.id)" +
            "WHERE card.id IN (:rewards) AND (card_fts MATCH :query) " +
            "ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun searchRewards(query: String, rewards: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT card.id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost, " +
            "card.real_image_src, card.name, card.type_name, card.traits, card.level, card.set_id, " +
            "card.set_type_id, card.deck_limit FROM card JOIN card_fts ON (card.id = card_fts.id)" +
            "WHERE set_id == 'malady' AND (card_fts MATCH :query) " +
            "ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun searchAllMaladies(query: String): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT card.id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost, " +
            "card.real_image_src, card.name, card.type_name, card.traits, card.level, card.set_id, " +
            "card.set_type_id, card.deck_limit FROM card JOIN card_fts ON (card.id = card_fts.id)" +
            "WHERE spoiler = 'false' OR (spoiler IS NULL AND NOT EXISTS (" +
            "SELECT 1 FROM card WHERE spoiler = 'false')) AND type_id != 'role' AND (card_fts MATCH :query)" +
            "ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position"
    )
    fun searchAllCards(query: String): PagingSource<Int, CardDeckListItemProjection>

    @Query("SELECT card.id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost, " +
            "card.real_image_src, card.name, card.type_name, card.traits, card.level, card.set_id, " +
            "card.set_type_id, card.deck_limit FROM card JOIN card_fts ON (card.id = card_fts.id)" +
            "WHERE card.id IN (:ids) AND (card_fts MATCH :query) " +
            "ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun searchExtraCards(query: String, ids: List<String>): PagingSource<Int, CardDeckListItemProjection>
}