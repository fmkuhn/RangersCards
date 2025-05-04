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
    tabooId: String?,
    aspectId: String?,
    aspectShortName: String?,
    cost: Int?,
    imageSrc: String?,
    realImageSrc: String?,
    presence: Int?,
    approachConflict: Int?,
    approachReason: Int?,
    approachExploration: Int?,
    approachConnection: Int?,
    typeId: String?,
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
    subsetSize: Int?,
    subsetPosition: Int?,
    packShortName: String?,
    sunChallenge: String?,
    mountainChallenge: String?,
    crestChallenge: String?,
    isDarkTheme: Boolean,
    name: String,
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
                realImageSrc,
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
                typeId,
                typeName,
                traits,
                equip,
                harm,
                progress,
                tokenPlurals,
                tokenCount,
                text,
                flavor,
                sunChallenge,
                mountainChallenge,
                crestChallenge,
                imageSrc,
                isDarkTheme
            )
            //Set info
            FullCardSetInfo(
                tabooId,
                aspectId,
                aspectShortName,
                level,
                setName,
                subsetSize ?: setSize,
                subsetPosition ?: setPosition,
                packShortName,
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
                tabooId = "",
                aspectId = "AWA",
                aspectShortName = "AWA",
                cost = 2,
                imageSrc = null,
                realImageSrc = null,
                presence = 1,
                approachConflict = 1,
                approachReason = 1,
                approachExploration = 1,
                approachConnection = 1,
                typeName = null,
                typeId = null,
                traits = "Being / Companion / Mammal",
                equip = 2,
                harm = 1,
                progress = 1,
                tokenPlurals = "Запись,Записи,Записей",
                tokenCount = 0,
                text = "Some text\nAnd [[new]] g line",
                flavor = "Some flavor",
                level = 2,
                setName = "Reward",
                setSize = 31,
                setPosition = 2,
                subsetPosition = null,
                subsetSize = null,
                packShortName = null,
                isDarkTheme = isSystemInDarkTheme(),
                name = "Scuttler g Tunnel\nnew g line an some more",
                sunChallenge = null,
                mountainChallenge = null,
                crestChallenge = null,
            )
        }
    }
}
