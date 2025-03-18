package com.rangerscards.data.database.campaign

import androidx.room.ColumnInfo
import kotlinx.serialization.json.JsonElement

data class CampaignListItemProjection(
    val id: String,
    val name: String,
    val day: Int,
    @ColumnInfo(name = "current_location")
    val currentLocation: String,
    @ColumnInfo(name = "latest_decks")
    val latestDecks: JsonElement,
    val access: JsonElement,
)
