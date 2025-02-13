package com.rangerscards.ui.cards.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.RangersCardsTheme

@Composable
fun FullCard(
    aspectId: String?,
    aspectShortName: String?,
    cost: Int?,
    imageSrc: String?,
    name: String,
    presence: Int?,
    approachConflict: Int?,
    approachReason: Int?,
    approachExploration: Int?,
    approachConnection: Int?,
    typeName: String?,
    traits: String?,
    equip: Int?,
    harm: Int?,
    progress: Int?,
    tokenPlurals: String?,
    tokenCount: Int?,
    text: String?,
    flavor: String?,
    level: Int?,
    setName: String,
    setSize: Int,
    setPosition: Int,
    //TODO:Implement campaign cards
//    sunChallenge: String?,
//    mountainChallenge: String?,
//    crestChallenge: String?,
    isDarkTheme: Boolean,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(CustomTheme.shapes.large),
        shape = CustomTheme.shapes.large,
        color = CustomTheme.colors.l30,
        border = BorderStroke(1.dp, when (aspectId) {
            "AWA" -> CustomTheme.colors.green
            "FIT" -> CustomTheme.colors.red
            "FOC" -> CustomTheme.colors.blue
            "SPI" -> CustomTheme.colors.orange
            else -> CustomTheme.colors.m
        }),
        shadowElevation = 4.dp
    ) {
        Column {
            //Header
            FullCardHeader(
                aspectId,
                aspectShortName,
                cost,
                imageSrc,
                name,
                presence,
                approachConflict,
                approachReason,
                approachExploration,
                approachConnection,
                isDarkTheme,
            )
            //Content block
            FullCardContent(
                aspectId,
                typeName,
                traits,
                equip,
                harm,
                progress,
                tokenPlurals,
                tokenCount,
                text,
                flavor,
                imageSrc,
                isDarkTheme
            )
            //Set info
            FullCardSetInfo(
                aspectId,
                aspectShortName,
                level,
                setName,
                setSize,
                setPosition,
                isDarkTheme
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FullCardScreenPreview() {
    RangersCardsTheme {
        Column(
            modifier = Modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize()
        ) {
            FullCard(
                aspectId = "AWA",
                aspectShortName = "AWA",
                cost = 2,
                imageSrc = null,
                name = "Scuttler g Tunnel\nnew g line an some more",
                presence = 1,
                approachConflict = 1,
                approachReason = 1,
                approachExploration = 1,
                approachConnection = 1,
                typeName = null,
                traits = "Being / Companion / Mammal",
                equip = 2,
                harm = 1,
                progress = 1,
                tokenPlurals = "Запись,Записи,Записей",
                tokenCount = 0,
                text = "Some text\nAnd new g line",
                flavor = "Some flavor",
                level = 2,
                setName = "Reward",
                setSize = 31,
                setPosition = 2,
                isDarkTheme = isSystemInDarkTheme()
            )
        }
    }
}
