package com.rangerscards.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rangerscards.R
import com.rangerscards.data.ImageSrc
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.ui.theme.RangersCardsTheme

@Composable
fun CardListItem(
    aspectId: String?,
    aspectShortName: String?,
    cost: Int?,
    imageSrc: String?,
    name: String,
    typeName: String?,
    traits: String?,
    level: Int?,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Top
            ) {
                CardListItemImageContainer(
                    aspectId,
                    aspectShortName,
                    cost,
                    imageSrc,
                    name,
                    isDarkTheme,
                    Modifier.align(Alignment.CenterVertically)
                )
                CardListItemTextContainer(name, typeName, traits, Modifier.weight(1f))
                if (level != null)
                    CardListItemLevelContainer(aspectId, aspectShortName, level, isDarkTheme)
            }
            HorizontalDivider(
                color = CustomTheme.colors.l10
            )
        }
    }
}

@Composable
fun CardListItemImageContainer(
    aspectId: String?,
    aspectShortName: String?,
    cost: Int?,
    imageSrc: String?,
    name: String,
    isDarkTheme: Boolean,
    modifier: Modifier
) {
    Surface(
        modifier = modifier
            .sizeIn(maxHeight = 40.dp)
            .aspectRatio(1f),
        shape = CustomTheme.shapes.small,
        color = when (aspectId) {
            "AWA" -> CustomTheme.colors.green
            "FIT" -> CustomTheme.colors.red
            "FOC" -> CustomTheme.colors.blue
            "SPI" -> CustomTheme.colors.orange
            else -> Color.Transparent
        },
        border = BorderStroke(
            1.dp,
            if (aspectId != null) Color.Transparent else CustomTheme.colors.d10),
    ) {
        if (aspectId != null) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(when(aspectId) {
                        "AWA" -> R.drawable.awa_chakra
                        "FIT" -> R.drawable.fit_chakra
                        "FOC" -> R.drawable.foc_chakra
                        "SPI" -> R.drawable.spi_chakra
                        else -> R.drawable.spi_chakra
                    }),
                    contentDescription = null,
                    tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.size(34.dp)
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (cost != null) Text(
                        text = when(cost) {
                            -2 -> "X"
                            else -> cost.toString()
                        },
                        color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.sizeIn(maxHeight = 18.dp)
                    )
                    Text(
                        text = aspectShortName.toString(),
                        color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.sizeIn(maxHeight = 14.dp)
                    )
                }
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(ImageSrc.imageSrc + imageSrc)
                    .build(),
                placeholder = painterResource(id = R.drawable.per_ranger),
                error = painterResource(id = R.drawable.per_ranger),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.graphicsLayer { translationY = 3F }
            )
        }
    }
}

@Composable
fun CardListItemTextContainer(
    name: String,
    typeName: String?,
    traits: String?,
    weight: Modifier
) {
    Column(
        modifier = weight,
    ) {
        Text(
            text = name,
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = traits ?: typeName ?: "",
            color = CustomTheme.colors.d10,
            fontFamily = Jost,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CardListItemLevelContainer(
    aspectId: String?,
    aspectShortName: String?,
    level: Int?,
    isDarkTheme: Boolean
) {
    Surface(
        shape = CustomTheme.shapes.small,
        color = when (aspectId) {
            "AWA" -> CustomTheme.colors.green
            "FIT" -> CustomTheme.colors.red
            "FOC" -> CustomTheme.colors.blue
            "SPI" -> CustomTheme.colors.orange
            else -> Color.Transparent
        },
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = (level ?: 0).toString() + " " + aspectShortName.toString(),
            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    RangersCardsTheme {
        Column(
            modifier = Modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize()
        ) {
            CardListItem(
                aspectId = "AWA",
                aspectShortName = "AWA",
                cost = 2,
                imageSrc = null,
                name = "Scuttler g Tunnel",
                typeName = null,
                traits = "Being / Companion / Mammal",
                level = 2,
                isDarkTheme = isSystemInDarkTheme(),
                onClick = {}
            )
        }
    }
}