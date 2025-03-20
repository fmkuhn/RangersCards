package com.rangerscards.data.database.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.data.database.campaign.CampaignListItemProjection
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.dao.CampaignDao
import com.rangerscards.data.database.dao.DeckDao
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.DeckListItemProjection
import kotlinx.coroutines.flow.Flow

class OfflineCampaignsRepository(
    private val campaignDao: CampaignDao,
    private val deckDao: DeckDao
) : CampaignsRepository {
    override suspend fun deleteAllUploadedCampaigns() = campaignDao.deleteAllUploadedCampaigns()

    override suspend fun syncCampaigns(networkCampaigns: List<Campaign>) =
        campaignDao.syncCampaigns(networkCampaigns)

    override fun getAllCampaigns(): Flow<PagingData<CampaignListItemProjection>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            pagingSourceFactory = { campaignDao.getAllCampaigns() }
        ).flow
    }

    override fun searchCampaigns(query: String): Flow<PagingData<CampaignListItemProjection>> {
        val newQuery = query
            .lowercase()
            .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
            .split("[^\\p{Alpha}]+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(separator = " ", transform = { "%$it%" })
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            pagingSourceFactory = { campaignDao.searchCampaigns(newQuery) }
        ).flow
    }

    override fun getRolesImages(ids: List<String>): Flow<List<String>> = campaignDao.getRolesImages(ids)

    override suspend fun insertCampaign(campaign: Campaign) = campaignDao.insertCampaign(campaign)

    override suspend fun insertDecks(decks: List<Deck>) = deckDao.upsertAllDecks(decks)
}