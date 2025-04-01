package com.rangerscards.data.database.repository

import androidx.paging.PagingData
import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.deck.DeckListItemProjection
import com.rangerscards.data.database.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

interface CampaignRepository {

    suspend fun updateCampaign(campaign: Campaign)

    suspend fun insertCampaign(campaign: Campaign)

    fun getCampaignFlowById(id: String): Flow<Campaign>

    suspend fun getCampaignById(id: String): Campaign

    fun getRole(id: String): Flow<RoleCardProjection>

    fun getAllDecks(userId: String, uploaded: Boolean): Flow<PagingData<DeckListItemProjection>>

    fun searchDecks(query: String, userId: String, uploaded: Boolean): Flow<PagingData<DeckListItemProjection>>

    suspend fun deleteCampaign(id: String)

    fun getRewards(): Flow<List<CardListItemProjection>>
}