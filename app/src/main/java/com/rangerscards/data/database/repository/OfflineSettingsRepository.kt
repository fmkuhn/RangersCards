package com.rangerscards.data.database.repository

import com.rangerscards.data.database.dao.CampaignDao
import com.rangerscards.data.database.dao.DeckDao

class OfflineSettingsRepository(
    private val deckDao: DeckDao,
    private val campaignDao: CampaignDao
) : SettingsRepository {

    override suspend fun deleteAllLocalDecks() = deckDao.deleteAllLocalDecks()

    override suspend fun deleteAllLocalCampaigns() = campaignDao.deleteAllLocalCampaigns()

}