package com.rangerscards.data.database.repository

interface SettingsRepository {

    suspend fun deleteAllLocalDecks()

    suspend fun deleteAllLocalCampaigns()

}