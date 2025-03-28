package com.rangerscards.ui.deck.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun BoxScope.DeckRightSideDrawer(
    isOpen: Boolean,
    onClick: () -> Unit,
    deckName: String,
    deckId: String?,
    changeName: () -> Unit,
    toNotes: () -> Unit,
    toCharts: () -> Unit,
    camp:  (() -> Unit)?,
    toPreviousDeck: (() -> Unit)?,
    toNextDeck: (() -> Unit)?,
    cloneDeck: () -> Unit,
    upload: (() -> Unit)?,
    deleteDeck: () -> Unit,
) {
    // Get the current density for converting Dp to pixels.
    val density = LocalDensity.current

    // Determine the target offset in Dp based on the switch state.
    val targetOffsetDp = if (isOpen) 0.dp else 240.dp
    // Convert the Dp value to pixels as an Int.
    val targetOffsetPx = with(density) { targetOffsetDp.toPx().toInt() }

    // When the drawer is open, the offset is 0. When closed, the drawer is moved
    // out by its own width to the right.
    val offsetX by animateIntOffsetAsState(
        targetValue = IntOffset(targetOffsetPx, 0),
        animationSpec = tween(durationMillis = 300),
        label = "Open/Close drawer"
    )

    // Draw the scrim over the main content if the drawer is open.
    if (isOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onClick() }
        )
    }

    // The drawer content anchored to the right side.
    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd) // Anchor to the right edge of the screen.
            .fillMaxHeight()
            .width(240.dp)
            .offset { offsetX } // Animate horizontally.
            .background(CustomTheme.colors.l30)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DrawerSectionHeader(R.string.deck_section_header)
                    DrawerSectionButtonRow(
                        R.drawable.badge_32dp,
                        deckName,
                        changeName,
                        if (deckId != null) stringResource(R.string.deck_section_deck_id, deckId) else null
                    )
                    //TODO:Implement deck notes
//                    HorizontalDivider(color = CustomTheme.colors.l10)
//                    DrawerSectionButtonRow(
//                        R.drawable.edit_32dp,
//                        stringResource(R.string.deck_section_notes),
//                        toNotes,
//                    )
                }
            }
            //TODO:Implement deck charts
//            item {
//                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    DrawerSectionHeader(R.string.tools_section_header)
//                    DrawerSectionButtonRow(
//                        R.drawable.charts_32dp,
//                        stringResource(R.string.tools_section_charts),
//                        toCharts,
//                    )
//                }
//            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DrawerSectionHeader(R.string.campaign_section_header)
                    if (camp != null) DrawerSectionButtonRow(
                        R.drawable.camp_32dp,
                        stringResource(R.string.campaign_section_camp),
                        camp,
                    )
                    if (toPreviousDeck != null) {
                        if (camp != null) HorizontalDivider(color = CustomTheme.colors.l10)
                        DrawerSectionButtonRow(
                            R.drawable.arrow_back_32dp,
                            stringResource(R.string.campaign_section_previous_deck),
                            toPreviousDeck,
                        )
                    }
                    if (toNextDeck != null) {
                        if (camp != null || toPreviousDeck != null)
                            HorizontalDivider(color = CustomTheme.colors.l10)
                        DrawerSectionButtonRow(
                            R.drawable.arrow_forward_32dp,
                            stringResource(R.string.campaign_section_next_deck),
                            toNextDeck,
                        )
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DrawerSectionHeader(R.string.options_section_header)
                    DrawerSectionButtonRow(
                        R.drawable.content_copy_32dp,
                        stringResource(R.string.options_section_clone_deck),
                        cloneDeck,
                    )
                    if (upload != null) {
                        HorizontalDivider(color = CustomTheme.colors.l10)
                        DrawerSectionButtonRow(
                            R.drawable.language_32dp,
                            stringResource(
                                R.string.upload_to_rangersdb
                            ),
                            upload,
                        )
                    }
                    HorizontalDivider(color = CustomTheme.colors.l10)
                    DrawerSectionButtonRow(
                        R.drawable.delete_32dp,
                        stringResource(R.string.options_section_delete_deck),
                        deleteDeck,
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerSectionHeader(@StringRes textResId: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CustomTheme.colors.l20,
        shape = CustomTheme.shapes.circle
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
            Text(
                text = stringResource(textResId),
                color = CustomTheme.colors.d10,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DrawerSectionButtonRow(
    @DrawableRes iconResId: Int,
    mainText: String,
    onClick: () -> Unit,
    additionalText: String? = null
) {

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(id = iconResId),
            contentDescription = null,
            tint = CustomTheme.colors.m,
            modifier = Modifier.size(32.dp)
        )
        Column {
            Text(
                text = mainText,
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 18.sp,
            )
            if (additionalText != null) {
                Text(
                    text = additionalText,
                    color = CustomTheme.colors.d10,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}