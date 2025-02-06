package com.rangerscards.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.PrimaryKey

@Fts4(tokenizer = FtsOptions.TOKENIZER_UNICODE61, contentEntity = Card::class)
@Entity(tableName = "Card_fts")
data class CardFts(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Int,
    val id: String,
    val name: String?,
    @ColumnInfo(name = "real_name")
    val realName: String?,
    @ColumnInfo(name = "real_traits")
    val realTraits: String?,
    val traits: String?,
    val text: String?,
    @ColumnInfo(name = "real_text")
    val realText: String?,
    val flavor: String?,
    @ColumnInfo(name = "real_flavor")
    val realFlavor: String?,
    @ColumnInfo(name = "sun_challenge")
    val sunChallenge: String?,
    @ColumnInfo(name = "mountain_challenge")
    val mountainChallenge: String?,
    @ColumnInfo(name = "crest_challenge")
    val crestChallenge: String?,
)
