package com.rangerscards.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.rangerscards.data.database.card.Card
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.card.FullCardProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<Card>)

    @Upsert
    suspend fun upsertAll(cards: List<Card>)

    @Query("SELECT EXISTS(SELECT * FROM card)")
    suspend fun isExists(): Boolean

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: Taboo is set – select the override card
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
                   type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration,
                   set_type_id, set_id, set_position
            FROM card WHERE (spoiler = :spoiler OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = :spoiler)))
              AND (:taboo IS 1 AND taboo_id IS NOT NULL) AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When taboo is set but no override exists, fall back to the default card
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
                   type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration,
                   set_type_id, set_id, set_position
            FROM card AS c WHERE (spoiler = :spoiler OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = :spoiler)))
              AND (:taboo IS 1 AND taboo_id IS NULL) AND pack_id IN (:packIds)
              AND NOT EXISTS ( SELECT 1 FROM card c2 WHERE c2.code = c.code AND c2.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: Taboo not set – select only default cards
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
                   type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration,
                   set_type_id, set_id, set_position
            FROM card WHERE (spoiler = :spoiler OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = :spoiler)))
              AND (:taboo IS 0 AND taboo_id IS NULL) AND pack_id IN (:packIds)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun getAllCards(spoiler: Boolean, taboo: Boolean, packIds: List<String>): PagingSource<Int, CardListItemProjection>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: Taboo is set – return the taboo-specific card
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost,
                   card.real_image_src, card.name, card.type_name, card.traits, card.level,
                   card.approach_connection, card.approach_reason, card.approach_conflict, card.approach_exploration,
                   card.set_type_id, card.set_id, card.set_position
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE (card.spoiler = :spoiler 
                OR (card.spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = :spoiler)))
                AND (card_fts MATCH :query) AND card.pack_id IN (:packIds) AND (:taboo IS 1 AND card.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 2: When taboo is set but no override exists, use the default card
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost,
                   card.real_image_src, card.name, card.type_name, card.traits, card.level,
                   card.approach_connection, card.approach_reason, card.approach_conflict, card.approach_exploration,
                   card.set_type_id, card.set_id, card.set_position
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE (card.spoiler = :spoiler 
                OR (card.spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = :spoiler)))
                AND (card_fts MATCH :query) AND card.pack_id IN (:packIds) AND (:taboo IS 1 AND card.taboo_id IS NULL)
                AND NOT EXISTS (SELECT 1 FROM card c2 WHERE c2.code = card.code AND c2.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: Taboo not set – use default cards only
            SELECT card.id, card.code, card.taboo_id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost,
                   card.real_image_src, card.name, card.type_name, card.traits, card.level,
                   card.approach_connection, card.approach_reason, card.approach_conflict, card.approach_exploration,
                   card.set_type_id, card.set_id, card.set_position
            FROM card JOIN card_fts ON (card.id = card_fts.id) WHERE (card.spoiler = :spoiler 
            OR (card.spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = :spoiler)))
            AND (card_fts MATCH :query) AND card.pack_id IN (:packIds) AND (:taboo IS 0 AND card.taboo_id IS NULL)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun searchCards(query: String, spoiler: Boolean, taboo: Boolean, packIds: List<String>): PagingSource<Int, CardListItemProjection>

    @Query("""SELECT * FROM (
            -- Case 1: Taboo is set – choose the taboo-specific card
            SELECT taboo_id, aspect_id, aspect_short_name, cost, image_src, real_image_src, name,
                presence, approach_conflict, approach_reason, approach_exploration, approach_connection,
                type_name, traits, equip, harm, progress, token_plurals, token_count, text, flavor, level,
                set_name, set_size, set_position, pack_short_name, subset_name, subset_position, subset_size,
                sun_challenge, mountain_challenge, crest_challenge
            FROM card WHERE code = :cardCode AND (:taboo IS 1 AND taboo_id IS NOT NULL)
            UNION ALL
            -- Case 2: When taboo is set but no override exists, return the default card
            SELECT taboo_id, aspect_id, aspect_short_name, cost, image_src, real_image_src, name,
                presence, approach_conflict, approach_reason, approach_exploration, approach_connection,
                type_name, traits, equip, harm, progress, token_plurals, token_count, text, flavor, level,
                set_name, set_size, set_position, pack_short_name, subset_name, subset_position, subset_size,
                sun_challenge, mountain_challenge, crest_challenge
            FROM card AS c WHERE code = :cardCode AND (:taboo IS 1 AND taboo_id IS NULL)
              AND NOT EXISTS ( SELECT 1 FROM card c2 WHERE c2.code = c.code AND c2.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: Taboo not set – return default card
            SELECT taboo_id, aspect_id, aspect_short_name, cost, image_src, real_image_src, name,
                presence, approach_conflict, approach_reason, approach_exploration, approach_connection,
                type_name, traits, equip, harm, progress, token_plurals, token_count, text, flavor, level,
                set_name, set_size, set_position, pack_short_name, subset_name, subset_position, subset_size,
                sun_challenge, mountain_challenge, crest_challenge
            FROM card WHERE code = :cardCode AND (:taboo IS 0 AND taboo_id IS NULL)
        )""")
    fun getCardById(cardCode: String, taboo: Boolean): Flow<FullCardProjection?>
}