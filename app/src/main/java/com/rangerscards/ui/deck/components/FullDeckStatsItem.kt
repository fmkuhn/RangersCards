package com.rangerscards.ui.deck.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun FullDeckStatsItem(
    stats: List<Int>,
    isDarkTheme: Boolean,
    isEditing: Boolean,
    isUpgrade: Boolean,
    onStatChange: (Int, Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().sizeIn(maxHeight = 64.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (stats.isNotEmpty()) stats.forEachIndexed { index, stat ->
            val color = when (index) {
                0 -> CustomTheme.colors.green
                1 -> CustomTheme.colors.orange
                2 -> CustomTheme.colors.red
                else -> CustomTheme.colors.blue
            }
            if (isEditing && !isUpgrade) Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painterResource(R.drawable.arrow_drop_up_32dp),
                    contentDescription = null,
                    tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.size(28.dp)
                        .background(color = color, CustomTheme.shapes.small)
                        .clickable { if (stat in 1..2) onStatChange(index, stat + 1) }
                )
                Icon(
                    painterResource(R.drawable.arrow_drop_down_32dp),
                    contentDescription = null,
                    tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.size(28.dp)
                        .background(color = color, CustomTheme.shapes.small)
                        .clickable { if (stat in 2..3) onStatChange(index, stat - 1) }
                )
            }
            Surface(
                modifier = Modifier
                    .fillMaxHeight().aspectRatio(1f),
                color = color,
                shape = CustomTheme.shapes.medium
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painterResource(when(index) {
                            0 -> R.drawable.awa_chakra
                            1 -> R.drawable.spi_chakra
                            2 -> R.drawable.fit_chakra
                            else -> R.drawable.foc_chakra
                        }),
                        contentDescription = null,
                        tint = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        modifier = Modifier.size(52.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = stat.toString(),
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            lineHeight = 28.sp,
                            modifier = Modifier.sizeIn(maxHeight = 28.dp)
                        )
                        Text(
                            text = stringResource(when(index) {
                                0 -> R.string.awa_styled_card_text
                                1 -> R.string.spi_styled_card_text
                                2 -> R.string.fit_styled_card_text
                                else -> R.string.foc_styled_card_text
                            }),
                            color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.sizeIn(maxHeight = 20.dp)
                        )
                    }
                }
            }
        }
    }
}