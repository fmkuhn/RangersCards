package com.rangerscards.data.database.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.data.database.dao.CampaignDao
import com.rangerscards.data.database.dao.DeckDao
import com.rangerscards.data.database.deck.DeckListItemProjection
import com.rangerscards.data.database.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

class OfflineCampaignRepository(
    private val campaignDao: CampaignDao,
) : CampaignRepository {

    override suspend fun updateCampaign(campaign: Campaign) = campaignDao.updateCampaign(campaign)

    override fun getCampaignFlowById(id: String): Flow<Campaign> = campaignDao.getCampaignFlowById(id)

    override suspend fun getCampaignById(id: String): Campaign = campaignDao.getCampaignById(id)

    override fun getRole(id: String): Flow<RoleCardProjection> = campaignDao.getRole(id)

    override fun getAllDecks(userId: String, uploaded: Boolean): Flow<PagingData<DeckListItemProjection>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { campaignDao.getAllDecks(userId, uploaded) }
        ).flow
    }

    override fun searchDecks(query: String, userId: String, uploaded: Boolean): Flow<PagingData<DeckListItemProjection>> {
        val newQuery = query
            .lowercase()
            .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
            .split("[^\\p{Alpha}]+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(separator = " ", transform = { "%$it%" })
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { campaignDao.searchDecks(newQuery, userId, uploaded) }
        ).flow
    }

    override suspend fun deleteCampaign(id: String) = campaignDao.deleteCampaign(id)
}