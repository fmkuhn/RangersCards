package com.rangerscards.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.rangerscards.data.database.card.Card
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.card.FullCardProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(cards: List<Card>)

    @Upsert
    suspend fun upsertAll(cards: List<Card>)

    @Query("SELECT EXISTS(SELECT * FROM card)")
    suspend fun isExists(): Boolean

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, approach_connection, approach_reason, approach_conflict, " +
            "approach_exploration FROM card WHERE spoiler = :spoiler OR (spoiler IS NULL " +
            "AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = :spoiler)) " +
            "ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position"
    )
    fun getAllCards(spoiler: Boolean): PagingSource<Int, CardListItemProjection>

    @Query("SELECT card.id, card.set_name, card.aspect_id, card.aspect_short_name, card.cost, " +
            "card.real_image_src, card.name, card.type_name, card.traits, card.level, " +
            "card.approach_connection, card.approach_reason, card.approach_conflict, " +
            "card.approach_exploration FROM card JOIN card_fts ON (card.id = card_fts.id) " +
            "WHERE (spoiler = :spoiler OR (spoiler IS NULL AND NOT EXISTS " +
            "(SELECT 1 FROM card WHERE spoiler = :spoiler))) " +
            "AND (card_fts MATCH :query) " +
            "ORDER BY (card.set_type_id IS NULL), card.set_type_id, card.set_id, card.set_position"
    )
    fun searchCards(query: String, spoiler: Boolean): PagingSource<Int, CardListItemProjection>

    @Query("SELECT aspect_id, aspect_short_name, cost, image_src, real_image_src, name, presence, " +
            "approach_conflict, approach_reason, approach_exploration, approach_connection, " +
            "type_name, traits, equip, harm, progress, token_plurals, token_count, text, flavor, " +
            "level, set_name, set_size, set_position, sun_challenge, mountain_challenge, crest_challenge " +
            "FROM card WHERE id = :cardId"
    )
    fun getCardById(cardId: String): Flow<FullCardProjection>
}