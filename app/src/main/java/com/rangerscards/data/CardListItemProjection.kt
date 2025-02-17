package com.rangerscards.data

import androidx.room.ColumnInfo

data class CardListItemProjection(
    val id: String,
    val name: String?,
    val traits: String?,
    @ColumnInfo(name = "set_name")
    val setName: String?,
    val level: Int?,
    @ColumnInfo(name = "type_name")
    val typeName: String?,
    val cost: Int?,
    @ColumnInfo(name = "aspect_id")
    val aspectId: String?,
    @ColumnInfo(name = "aspect_short_name")
    val aspectShortName: String?,
    @ColumnInfo(name = "real_image_src")
    val realImageSrc: String?,
)
