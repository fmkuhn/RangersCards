package com.rangerscards.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.json.JsonElement

@Entity(tableName = "Deck")
data class Deck(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val uploaded: Boolean,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "user_handle")
    val userHandle: String?,
    val slots: JsonElement,
    @ColumnInfo(name = "side_slots")
    val sideSlots: JsonElement,
    @ColumnInfo(name = "extra_slots")
    val extraSlots: JsonElement,
    val version: Int,
    val name: String,
    val description: String?,
    val awa: Int,
    val spi: Int,
    val fit: Int,
    val foc: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,
    val meta: JsonElement,
    @ColumnInfo(name = "campaign_id")
    val campaignId: Int?,
    @ColumnInfo(name = "campaign_name")
    val campaignName: String?,
    @ColumnInfo(name = "campaign_rewards")
    val campaignRewards: JsonElement?,
    @ColumnInfo(name = "previous_id")
    val previousId: Int?,
    @ColumnInfo(name = "previous_slots")
    val previousSlots: JsonElement?,
    @ColumnInfo(name = "previous_side_slots")
    val previousSideSlots: JsonElement?,
    @ColumnInfo(name = "next_id")
    val nextId: Int?,
)
