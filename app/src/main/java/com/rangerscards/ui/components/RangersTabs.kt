package com.rangerscards.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.ui.theme.RangersCardsTheme

@Composable
fun RangersTabs(
    tabs: List<Int>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = CustomTheme.colors.l20,
        contentColor = CustomTheme.colors.d30,
        // Custom indicator
        indicator = { tabPositions ->
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = CustomTheme.colors.d30,
                shape = AbsoluteRoundedCornerShape(topLeft = 50.dp, topRight = 50.dp),
                width = tabPositions[selectedTabIndex].contentWidth
            )
        },
        divider = {
            HorizontalDivider(color = CustomTheme.colors.l10)
        }
    ) {
        tabs.forEachIndexed { index, titleResId ->
            CustomTab(
                titleResId = titleResId,
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
fun CustomTab(
    @StringRes titleResId: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .selectable(selected) { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .sizeIn(minHeight = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(titleResId),
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@Preview(showSystemUi = true)
fun RangersTabsPreview() {
    RangersCardsTheme {
        var id by remember { mutableIntStateOf(0) }
        Column(
            modifier = Modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize()
        ) {
            RangersTabs(
                listOf(
                    R.string.custom_deck_tab,
                    R.string.starter_deck_tab,

                ),
                id
            ) { id = it}
        }
    }
}
