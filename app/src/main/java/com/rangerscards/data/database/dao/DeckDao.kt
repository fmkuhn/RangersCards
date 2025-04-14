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

    @Query("DELETE FROM deck WHERE id = :id")
    suspend fun deleteDeckById(id: String)

    @Upsert
    suspend fun upsertAllDecks(decks: List<Deck>)

    @Query("DELETE FROM deck WHERE id NOT IN (:ids) AND uploaded = 1")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("DELETE FROM deck WHERE uploaded = 1")
    suspend fun deleteAllUploadedDecks()

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL " +
            "AND (user_id = :userId OR user_id = '') ORDER BY updated_at DESC"
    )
    fun getAllDecks(userId: String): PagingSource<Int, DeckListItemProjection>

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL " +
            "AND name LIKE :query AND (user_id = :userId OR user_id = '') ORDER BY updated_at DESC"
    )
    fun searchDecks(query: String, userId: String): PagingSource<Int, DeckListItemProjection>

    @Query("Select id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, approach_conflict, approach_reason, approach_exploration, " +
            "approach_connection FROM card WHERE id = :id")
    fun getCard(id: String): Flow<CardListItemProjection?>

    @Query("""SELECT * FROM (
            -- Case 1: Taboo is set – select the override card
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration 
            FROM card WHERE type_id = 'role' AND set_id = :specialty AND (:taboo IS 1 AND taboo_id IS NOT NULL)
            AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When taboo is set but no override exists, fall back to the default card
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card AS c WHERE c.type_id = 'role' AND c.set_id = :specialty AND (:taboo IS 1 AND taboo_id IS NULL)
            AND pack_id IN (:packIds)
            AND NOT EXISTS ( SELECT 1 FROM card c2 WHERE c2.code = c.code AND c2.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: Taboo not set – select only default cards
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration 
            FROM card WHERE type_id = 'role' AND set_id = :specialty AND (:taboo IS 0 AND taboo_id IS NULL)
            AND pack_id IN (:packIds)
        )""")
    fun getRoles(specialty: String, taboo: Boolean, packIds: List<String>): PagingSource<Int, CardListItemProjection>

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

    @Query(
        """SELECT * FROM (
            -- Case 1: Taboo is set – select the override card
            SELECT id, name, text, real_image_src, traits, taboo_id
            FROM card WHERE code = :code AND (:taboo IS 1 AND taboo_id IS NOT NULL)
            UNION ALL
            -- Case 2: When taboo is set but no override exists, fall back to the default card
            SELECT id, name, text, real_image_src, traits, taboo_id
            FROM card AS c WHERE c.code = :code AND (:taboo IS 1 AND taboo_id IS NULL)
              AND NOT EXISTS ( SELECT 1 FROM card c2 WHERE c2.code = c.code AND c2.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: Taboo not set – select only default cards
            SELECT id, name, text, real_image_src, traits, taboo_id
            FROM card WHERE code = :code AND (:taboo IS 0 AND taboo_id IS NULL)
        )"""
    )
    suspend fun getRole(code: String, taboo: Boolean): RoleCardProjection?

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:ids) AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:ids) AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:ids) AND (:tabooId IS NULL) AND taboo_id IS NULL
        ) ORDER BY set_type_id, set_id, set_position""")
    fun getCardsByIds(ids: List<String>, tabooId: String?): Flow<List<CardDeckListItemProjection>>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:ids) AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:ids) AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:ids) AND (:tabooId IS NULL) AND taboo_id IS NULL
        ) ORDER BY set_type_id, set_id, set_position""")
    suspend fun getChangedCardsByIds(ids: List<String>, tabooId: String?): List<CardDeckListItemProjection>


    //Queries for deck pagination without searching
    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id = 'personality' AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id = 'personality' AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            AND pack_id IN (:packIds)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id = 'personality' AND (:tabooId IS NULL) AND taboo_id IS NULL
            AND pack_id IN (:packIds)
        ) ORDER BY aspect_id, set_position""")
    fun getPersonalityCards(tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id = :background AND set_type_id = 'background' AND type_id != 'role'
            AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id = :background AND set_type_id = 'background' AND type_id != 'role'
            AND taboo_id IS NULL AND (:tabooId IS NOT NULL) AND pack_id IN (:packIds)
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id = :background AND set_type_id = 'background' AND type_id != 'role'
            AND (:tabooId IS NULL) AND taboo_id IS NULL AND pack_id IN (:packIds)
        ) ORDER BY aspect_id, set_position""")
    fun getBackgroundCards(background: String, tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id = :specialty AND set_type_id = 'specialty' AND type_id != 'role'
            AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id = :specialty AND set_type_id = 'specialty' AND type_id != 'role'
            AND taboo_id IS NULL AND (:tabooId IS NOT NULL) AND pack_id IN (:packIds)
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id = :specialty AND set_type_id = 'specialty' AND type_id != 'role'
            AND (:tabooId IS NULL) AND taboo_id IS NULL AND pack_id IN (:packIds)
        ) ORDER BY aspect_id, set_position""")
    fun getSpecialtyCards(specialty: String, tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id != :background AND set_id != :specialty AND set_id != 'personality'
            AND type_id != 'role' AND real_traits NOT LIKE '%expert%' AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id != :background AND set_id != :specialty AND set_id != 'personality'
            AND type_id != 'role' AND real_traits NOT LIKE '%expert%' AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId) AND pack_id IN (:packIds)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id != :background AND set_id != :specialty AND set_id != 'personality'
            AND type_id != 'role' AND real_traits NOT LIKE '%expert%' AND (:tabooId IS NULL) AND taboo_id IS NULL
            AND pack_id IN (:packIds)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun getOutsideInterestCards(background: String, specialty: String, tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id == 'reward' AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id == 'reward' AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            AND pack_id IN (:packIds)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id == 'reward' AND (:tabooId IS NULL) AND taboo_id IS NULL
            AND pack_id IN (:packIds)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun getAllRewards(tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:rewards) AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:rewards) AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:rewards) AND (:tabooId IS NULL) AND taboo_id IS NULL
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun getRewards(rewards: List<String>, tabooId: String?): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id == 'malady' AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id == 'malady' AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            AND pack_id IN (:packIds)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE set_id == 'malady' AND (:tabooId IS NULL) AND taboo_id IS NULL
            AND pack_id IN (:packIds)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun getAllMaladies(tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE spoiler = 'false' OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = 'false')) 
            AND type_id != 'role' AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE spoiler = 'false' OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = 'false')) 
            AND type_id != 'role' AND taboo_id IS NULL AND (:tabooId IS NOT NULL) AND pack_id IN (:packIds)
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE spoiler = 'false' OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = 'false')) 
            AND type_id != 'role' AND (:tabooId IS NULL) AND taboo_id IS NULL AND pack_id IN (:packIds)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun getAllCards(tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:ids) AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:ids) AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE code IN (:ids) AND (:tabooId IS NULL) AND taboo_id IS NULL
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun getExtraCards(ids: List<String>, tabooId: String?): PagingSource<Int, CardDeckListItemProjection>


    //Queries for deck pagination with searching
    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, 
            name, type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Branch 1: Taboo is set and an override exists
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id = 'personality' AND pack_id IN (:packIds)
            AND (card_fts MATCH :query) AND (:tabooId IS NOT NULL AND card.taboo_id = :tabooId)
            UNION ALL
            -- Branch 2: Taboo is set but no override exists (fallback to default card)
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id = 'personality' AND pack_id IN (:packIds)
            AND (card_fts MATCH :query) AND (:tabooId IS NOT NULL AND card.taboo_id IS NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Branch 3: No taboo is set – return default cards only
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id = 'personality' AND pack_id IN (:packIds)
            AND (card_fts MATCH :query) AND (:tabooId IS NULL AND card.taboo_id IS NULL)
        ) ORDER BY aspect_id, set_position""")
    fun searchPersonalityCards(query: String, tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, 
            name, type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Branch 1: Taboo is set and an override exists
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id = :background AND set_type_id = 'background' 
            AND type_id != 'role' AND (card_fts MATCH :query) AND (:tabooId IS NOT NULL AND card.taboo_id = :tabooId)
            AND pack_id IN (:packIds)
            UNION ALL
            -- Branch 2: Taboo is set but no override exists (fallback to default card)
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id = :background AND set_type_id = 'background' 
            AND type_id != 'role' AND (card_fts MATCH :query) AND (:tabooId IS NOT NULL AND card.taboo_id IS NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            AND pack_id IN (:packIds)
            UNION ALL
            -- Branch 3: No taboo is set – return default cards only
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id = :background AND set_type_id = 'background' 
            AND type_id != 'role' AND (card_fts MATCH :query) AND (:tabooId IS NULL AND card.taboo_id IS NULL)
            AND pack_id IN (:packIds)
        ) ORDER BY aspect_id, set_position""")
    fun searchBackgroundCards(query: String, background: String, tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, 
            name, type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Branch 1: Taboo is set and an override exists
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id = :specialty AND set_type_id = 'specialty' 
            AND type_id != 'role' AND (card_fts MATCH :query) AND (:tabooId IS NOT NULL AND card.taboo_id = :tabooId)
            AND pack_id IN (:packIds)
            UNION ALL
            -- Branch 2: Taboo is set but no override exists (fallback to default card)
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id = :specialty AND set_type_id = 'specialty' 
            AND type_id != 'role' AND (card_fts MATCH :query) AND (:tabooId IS NOT NULL AND card.taboo_id IS NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            AND pack_id IN (:packIds)
            UNION ALL
            -- Branch 3: No taboo is set – return default cards only
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id = :specialty AND set_type_id = 'specialty' 
            AND type_id != 'role' AND (card_fts MATCH :query) AND (:tabooId IS NULL AND card.taboo_id IS NULL)
            AND pack_id IN (:packIds)
        ) ORDER BY aspect_id, set_position""")
    fun searchSpecialtyCards(query: String, specialty: String, tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, 
            name, type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Branch 1: Taboo is set and an override exists
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id != :background AND set_id != :specialty 
            AND type_id != 'role' AND set_id != 'personality' AND real_traits NOT LIKE '%expert%' AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id = :tabooId) AND pack_id IN (:packIds)
            UNION ALL
            -- Branch 2: Taboo is set but no override exists (fallback to default card)
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id != :background AND set_id != :specialty 
            AND type_id != 'role' AND set_id != 'personality' AND real_traits NOT LIKE '%expert%' AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id IS NULL) AND pack_id IN (:packIds)
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Branch 3: No taboo is set – return default cards only
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id != :background AND set_id != :specialty 
            AND type_id != 'role' AND set_id != 'personality' AND real_traits NOT LIKE '%expert%' AND (card_fts MATCH :query) 
            AND (:tabooId IS NULL AND card.taboo_id IS NULL) AND pack_id IN (:packIds)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun searchOutsideInterestCards(
        query: String,
        background: String,
        specialty: String,
        tabooId: String?,
        packIds: List<String>
    ): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, 
            name, type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Branch 1: Taboo is set and an override exists
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id == 'reward' AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id = :tabooId) AND pack_id IN (:packIds)
            UNION ALL
            -- Branch 2: Taboo is set but no override exists (fallback to default card)
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id == 'reward' AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id IS NULL) AND pack_id IN (:packIds)
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Branch 3: No taboo is set – return default cards only
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id == 'reward' AND (card_fts MATCH :query) 
            AND (:tabooId IS NULL AND card.taboo_id IS NULL) AND pack_id IN (:packIds)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun searchAllRewards(query: String, tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, 
            name, type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Branch 1: Taboo is set and an override exists
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE card.code IN (:rewards) AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id = :tabooId)
            UNION ALL
            -- Branch 2: Taboo is set but no override exists (fallback to default card)
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE card.code IN (:rewards) AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id IS NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Branch 3: No taboo is set – return default cards only
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE card.code IN (:rewards) AND (card_fts MATCH :query) 
            AND (:tabooId IS NULL AND card.taboo_id IS NULL)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun searchRewards(query: String, rewards: List<String>, tabooId: String?): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, 
            name, type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Branch 1: Taboo is set and an override exists
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id == 'malady' AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id = :tabooId) AND pack_id IN (:packIds)
            UNION ALL
            -- Branch 2: Taboo is set but no override exists (fallback to default card)
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id == 'malady' AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id IS NULL) AND pack_id IN (:packIds)
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Branch 3: No taboo is set – return default cards only
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE set_id == 'malady' AND (card_fts MATCH :query) 
            AND (:tabooId IS NULL AND card.taboo_id IS NULL) AND pack_id IN (:packIds)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun searchAllMaladies(query: String, tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, 
            name, type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Branch 1: Taboo is set and an override exists
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE spoiler = 'false' OR (spoiler IS NULL 
            AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = 'false')) AND type_id != 'role' AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id = :tabooId) AND pack_id IN (:packIds)
            UNION ALL
            -- Branch 2: Taboo is set but no override exists (fallback to default card)
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE spoiler = 'false' OR (spoiler IS NULL 
            AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = 'false')) AND type_id != 'role' AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id IS NULL) AND pack_id IN (:packIds)
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Branch 3: No taboo is set – return default cards only
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE spoiler = 'false' OR (spoiler IS NULL 
            AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = 'false')) AND type_id != 'role' AND (card_fts MATCH :query) 
            AND (:tabooId IS NULL AND card.taboo_id IS NULL) AND pack_id IN (:packIds)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun searchAllCards(query: String, tabooId: String?, packIds: List<String>): PagingSource<Int, CardDeckListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, 
            name, type_name, traits, real_traits, level, set_id, set_type_id, deck_limit,
            approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Branch 1: Taboo is set and an override exists
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE card.code IN (:ids) AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id = :tabooId)
            UNION ALL
            -- Branch 2: Taboo is set but no override exists (fallback to default card)
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE card.code IN (:ids) AND (card_fts MATCH :query) 
            AND (:tabooId IS NOT NULL AND card.taboo_id IS NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Branch 3: No taboo is set – return default cards only
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name,
                   card.cost, card.real_image_src, card.name, card.type_name, card.traits, card.real_traits,
                   card.level, card.set_id, card.set_type_id, card.set_position, card.deck_limit, card.approach_connection, 
                   card.approach_reason, card.approach_conflict, card.approach_exploration
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE card.code IN (:ids) AND (card_fts MATCH :query) 
            AND (:tabooId IS NULL AND card.taboo_id IS NULL)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun searchExtraCards(query: String, ids: List<String>, tabooId: String?): PagingSource<Int, CardDeckListItemProjection>
}