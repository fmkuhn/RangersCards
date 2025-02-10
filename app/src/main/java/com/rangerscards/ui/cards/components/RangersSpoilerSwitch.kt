package com.rangerscards.ui.cards.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun RangersSpoilerSwitch(
    spoiler: Boolean,
    onSpoilerChanged: () -> Unit
) {
    // Define dimensions for the switch
    val switchWidth = 75.dp
    val switchHeight = 40.dp
    val indicatorSize = 34.dp
    val padding = 3.dp

    // Get the current density for converting Dp to pixels.
    val density = LocalDensity.current

    // Determine the target offset in Dp based on the switch state.
    val targetOffsetDp = if (spoiler) switchWidth - indicatorSize - padding else padding
    // Convert the Dp value to pixels as an Int.
    val targetOffsetPx = with(density) { targetOffsetDp.toPx().toInt() }

    // Animate the IntOffset for the indicator using animateIntOffsetAsState.
    val animatedOffset by animateIntOffsetAsState(
        targetValue = IntOffset(targetOffsetPx, 0),
        animationSpec = tween(durationMillis = 250),
        label = "offset"
    )

    val animatedColorRangerIcon by animateColorAsState(
        targetValue = if (spoiler) CustomTheme.colors.l10 else CustomTheme.colors.d30,
        label = "Color for ranger icon"
    )
    val animatedColorCrestIcon by animateColorAsState(
        targetValue = if (spoiler) CustomTheme.colors.d30 else CustomTheme.colors.l10,
        label = "Color for crest icon"
    )

    Box(
        modifier = Modifier
            .size(width = switchWidth, height = switchHeight)
            .clip(CustomTheme.shapes.circle)
            .background(CustomTheme.colors.l30)
            .border(1.dp, CustomTheme.colors.m, CustomTheme.shapes.circle)
            .clickable { onSpoilerChanged() },
        contentAlignment = Alignment.CenterStart
    ) {
        // The animated circular indicator that moves behind the active icon.
        Box(
            modifier = Modifier
                .size(indicatorSize)
                .offset{ animatedOffset }
                .clip(CustomTheme.shapes.circle)
                .background(CustomTheme.colors.l10)
        )
        // Place the icons on top of the background. They are always visible.
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(R.drawable.ranger),
                contentDescription = "No spoiler cards",
                tint = animatedColorRangerIcon,
                modifier = Modifier.size(24.dp)
            )
            Icon(
                painter = painterResource(R.drawable.crest),
                contentDescription = "Spoiler cards",
                tint = animatedColorCrestIcon,
                modifier = Modifier.size(24.dp).padding(end = 2.dp)
            )
        }
    }
}