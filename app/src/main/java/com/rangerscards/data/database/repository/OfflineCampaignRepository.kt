package com.rangerscards.data.database.repository

import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.data.database.dao.CampaignDao
import com.rangerscards.data.database.dao.DeckDao
import kotlinx.coroutines.flow.Flow

class OfflineCampaignRepository(
    private val campaignDao: CampaignDao,
    private val deckDao: DeckDao
) : CampaignRepository {

    override suspend fun updateCampaign(campaign: Campaign) = campaignDao.updateCampaign(campaign)

    override fun getCampaignFlowById(id: String): Flow<Campaign> = campaignDao.getCampaignFlowById(id)

    override suspend fun getCampaignById(id: String): Campaign = campaignDao.getCampaignById(id)
}