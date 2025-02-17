package com.rangerscards.data.database

import androidx.room.ColumnInfo

data class FullCardProjection(
    @ColumnInfo(name = "aspect_id")
    val aspectId: String?,
    @ColumnInfo(name = "aspect_short_name")
    val aspectShortName: String?,
    val cost: Int?,
    @ColumnInfo(name = "image_src")
    val imageSrc: String?,
    @ColumnInfo(name = "real_image_src")
    val realImageSrc: String?,
    val name: String,
    val presence: Int?,
    @ColumnInfo(name = "approach_conflict")
    val approachConflict: Int?,
    @ColumnInfo(name = "approach_reason")
    val approachReason: Int?,
    @ColumnInfo(name = "approach_exploration")
    val approachExploration: Int?,
    @ColumnInfo(name = "approach_connection")
    val approachConnection: Int?,
    @ColumnInfo(name = "type_name")
    val typeName: String?,
    val traits: String?,
    val equip: Int?,
    val harm: Int?,
    val progress: Int?,
    @ColumnInfo(name = "token_plurals")
    val tokenPlurals: String?,
    @ColumnInfo(name = "token_count")
    val tokenCount: Int?,
    val text: String?,
    val flavor: String?,
    val level: Int?,
    @ColumnInfo(name = "set_name")
    val setName: String,
    @ColumnInfo(name = "set_size")
    val setSize: Int,
    @ColumnInfo(name = "set_position")
    val setPosition: Int,
    @ColumnInfo(name = "sun_challenge")
    val sunChallenge: String?,
    @ColumnInfo(name = "mountain_challenge")
    val mountainChallenge: String?,
    @ColumnInfo(name = "crest_challenge")
    val crestChallenge: String?,
)
