package com.rangerscards.data.database.repository

import androidx.paging.PagingData
import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.data.database.campaign.CampaignListItemProjection
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

interface CampaignsRepository {

    suspend fun deleteAllUploadedCampaigns()

    suspend fun syncCampaigns(networkCampaigns: List<Campaign>)

    suspend fun upsertCampaigns(campaigns: List<Campaign>)

    suspend fun getCampaignById(id: String): Campaign

    fun getAllCampaigns(): Flow<PagingData<CampaignListItemProjection>>

    fun searchCampaigns(query: String): Flow<PagingData<CampaignListItemProjection>>

    fun getAllCampaignsForTransfer(cycleId: String): Flow<List<CampaignListItemProjection>>

    fun getRolesImages(ids: List<String>): Flow<List<RoleCardProjection>>

    suspend fun insertCampaign(campaign: Campaign)

    suspend fun insertDecks(decks: List<Deck>)
}