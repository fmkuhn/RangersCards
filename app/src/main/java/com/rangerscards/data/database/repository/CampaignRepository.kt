package com.rangerscards.data.database.repository

import com.rangerscards.data.database.campaign.Campaign
import kotlinx.coroutines.flow.Flow

interface CampaignRepository {

    suspend fun updateCampaign(campaign: Campaign)

    fun getCampaignFlowById(id: String): Flow<Campaign>

    suspend fun getCampaignById(id: String): Campaign
}