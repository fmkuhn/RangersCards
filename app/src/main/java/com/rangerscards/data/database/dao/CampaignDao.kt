package com.rangerscards.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.data.database.campaign.CampaignListItemProjection
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.deck.DeckListItemProjection
import com.rangerscards.data.database.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCampaign(campaign: Campaign)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign)

    @Query("DELETE FROM campaign WHERE id = :id")
    suspend fun deleteCampaignById(id: String)

    @Upsert
    suspend fun upsertAllCampaigns(campaigns: List<Campaign>)

    @Query("DELETE FROM campaign WHERE id NOT IN (:ids) AND uploaded = 1")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("DELETE FROM campaign WHERE uploaded = 1")
    suspend fun deleteAllUploadedCampaigns()

    @Query("SELECT id, name, day, current_location, latest_decks, access FROM campaign " +
            "WHERE cycle_id != 'demo' ORDER BY updated_at DESC"
    )
    fun getAllCampaigns(): PagingSource<Int, CampaignListItemProjection>

    @Query("SELECT id, name, day, current_location, latest_decks, access FROM campaign " +
            "WHERE name LIKE :query AND cycle_id != 'demo' ORDER BY updated_at DESC"
    )
    fun searchCampaigns(query: String): PagingSource<Int, CampaignListItemProjection>

    @Query("Select real_image_src FROM card WHERE id IN (:ids)")
    fun getRolesImages(ids: List<String>): Flow<List<String>>

    @Transaction
    suspend fun syncCampaigns(networkData: List<Campaign>) {
        // Insert or update all the network data.
        upsertAllCampaigns(networkData)

        if (networkData.isEmpty()) {
            // If the network data is empty, clear the rows with uploaded = true.
            deleteAllUploadedCampaigns()
        } else {
            // Otherwise, delete any rows not present in the network data.
            val networkIds = networkData.map { it.id }
            deleteNotIn(networkIds)
        }
    }

    @Query("SELECT * FROM campaign WHERE id = :id")
    fun getCampaignFlowById(id: String): Flow<Campaign>

    @Query("SELECT * FROM campaign WHERE id = :id")
    suspend fun getCampaignById(id: String): Campaign

    @Query("Select id, name, text, real_image_src, traits FROM card WHERE id = :id")
    fun getRole(id: String): Flow<RoleCardProjection>

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL AND uploaded = :uploaded " +
            " AND campaign_id IS NULL AND (user_id = :userId OR user_id = '') ORDER BY updated_at DESC"
    )
    fun getAllDecks(userId: String, uploaded: Boolean): PagingSource<Int, DeckListItemProjection>

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL AND uploaded = :uploaded " +
            " AND campaign_id IS NULL AND name LIKE :query AND (user_id = :userId OR user_id = '') ORDER BY updated_at DESC"
    )
    fun searchDecks(query: String, userId: String, uploaded: Boolean): PagingSource<Int, DeckListItemProjection>

    @Query("DELETE FROM campaign WHERE id = :id")
    suspend fun deleteCampaign(id: String)

    @Query("SELECT id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, approach_connection, approach_reason, approach_conflict, " +
            "approach_exploration FROM card WHERE set_id == 'reward' ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position")
    fun getAllRewards(): Flow<List<CardListItemProjection>>
}