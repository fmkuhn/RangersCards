package com.rangerscards.ui.cards.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FullCardContent(
    aspectId: String?,
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
    sunChallenge: String?,
    mountainChallenge: String?,
    crestChallenge: String?,
    imageSrc: String?,
    isDarkTheme: Boolean
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        FullCardAdditionalContent(
            aspectId,
            traits,
            typeId,
            typeName,
            equip,
            harm,
            progress,
            tokenPlurals,
            tokenCount,
            isDarkTheme
        )
        FullCardTextContent(aspectId, text, flavor, sunChallenge, mountainChallenge, crestChallenge, isDarkTheme)
        FullCardImageContainer(imageSrc)
    }
}