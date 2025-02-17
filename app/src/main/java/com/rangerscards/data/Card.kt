package com.rangerscards.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Card")
data class Card (
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String?,
    @ColumnInfo(name = "real_name")
    val realName: String?,
    @ColumnInfo(name = "real_traits")
    val realTraits: String?,
    val traits: String?,
    val equip: Int?,
    val presence: Int?,
    @ColumnInfo(name = "token_id")
    val tokenId: String?,
    @ColumnInfo(name = "token_name")
    val tokenName: String?,
    @ColumnInfo(name = "token_plurals")
    val tokenPlurals: String?,
    @ColumnInfo(name = "token_count")
    val tokenCount: Int?,
    val harm: Int?,
    @ColumnInfo(name = "approach_conflict")
    val approachConflict: Int?,
    @ColumnInfo(name = "approach_reason")
    val approachReason: Int?,
    @ColumnInfo(name = "approach_exploration")
    val approachExploration: Int?,
    @ColumnInfo(name = "approach_connection")
    val approachConnection: Int?,
    val text: String?,
    @ColumnInfo(name = "real_text")
    val realText: String?,
    @ColumnInfo(name = "set_id")
    val setId: String?,
    @ColumnInfo(name = "set_name")
    val setName: String?,
    @ColumnInfo(name = "set_type_id")
    val setTypeId: String?,
    @ColumnInfo(name = "set_size")
    val setSize: Int?,
    @ColumnInfo(name = "set_type_name")
    val setTypeName: String?,
    @ColumnInfo(name = "set_position")
    val setPosition: Int?,
    val quantity: Int?,
    val level: Int?,
    val flavor: String?,
    @ColumnInfo(name = "real_flavor")
    val realFlavor: String?,
    @ColumnInfo(name = "type_id")
    val typeId: String?,
    @ColumnInfo(name = "type_name")
    val typeName: String?,
    val cost: Int?,
    @ColumnInfo(name = "aspect_id")
    val aspectId: String?,
    @ColumnInfo(name = "aspect_name")
    val aspectName: String?,
    @ColumnInfo(name = "aspect_short_name")
    val aspectShortName: String?,
    val progress: Int?,
    @ColumnInfo(name = "image_src")
    val imageSrc: String?,
    @ColumnInfo(name = "real_image_src")
    val realImageSrc: String?,
    val position: Int?,
    @ColumnInfo(name = "deck_limit")
    val deckLimit: Int?,
    val spoiler: Boolean?,
    @ColumnInfo(name = "sun_challenge")
    val sunChallenge: String?,
    @ColumnInfo(name = "mountain_challenge")
    val mountainChallenge: String?,
    @ColumnInfo(name = "crest_challenge")
    val crestChallenge: String?,
    val composite: String?,
    @ColumnInfo(name = "real_composite")
    val realComposite: String?
)