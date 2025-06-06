package com.rangerscards.ui.cards

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.ui.cards.components.CardListItem
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.components.RowTypeDivider
import com.rangerscards.ui.settings.UserUIState
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.flow.drop

@Composable
fun CardsScreen(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    cardsViewModel: CardsViewModel,
    userUIState: UserUIState,
    navigateToCard: (Int) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val filterOptions by cardsViewModel.filterOptions.collectAsState()
    val spoiler by cardsViewModel.spoiler.collectAsState()
    val cardsLazyItems = cardsViewModel.searchResults.collectAsLazyPagingItems()
    // Remember a LazyListState to control and observe scroll position.
    val listState = rememberLazyListState()

    val activity = LocalActivity.current

    // Whenever the search query changes, scroll the list back to the top.
    LaunchedEffect(Unit) {
        snapshotFlow { filterOptions.searchQuery to spoiler }
            .drop(1)
            .collect {
                // Scroll to the first item
                listState.animateScrollToItem(0)
            }
    }
    LaunchedEffect(userUIState.userInfo) {
        val settings = userUIState.settings
        cardsViewModel.setTabooId(settings.taboo)
        cardsViewModel.setPackIds(settings.collection)
    }

    BackHandler {
        activity?.finish()
    }

    // Search TextField: user enters the search query.
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l20)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
        RangersSearchOutlinedField(
            query = filterOptions.searchQuery,
            R.string.search_for_card,
            onQueryChanged = cardsViewModel::onSearchQueryChanged,
            onClearClicked = cardsViewModel::clearSearchQuery
        )
        LazyColumn(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize(),
            state = listState
        ) {
            if (cardsLazyItems.itemCount == 0 && cardsLazyItems.loadState.isIdle) item {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Text(
                        text = if (filterOptions.searchQuery.isEmpty())
                            stringResource(R.string.no_matching_cards_filtered)
                            else stringResource(id = R.string.no_matching_cards, filterOptions.searchQuery),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.2.sp,
                    )
                }
            }
            items(
                count = cardsLazyItems.itemCount,
                key = cardsLazyItems.itemKey(CardListItemProjection::id),
                contentType = cardsLazyItems.itemContentType { it }
            ) { index ->
                val item = cardsLazyItems[index] ?: return@items
                val showHeader = if (index == 0) true
                else cardsLazyItems[index - 1]?.setName != item.setName
                if (showHeader) RowTypeDivider(text = item.setName.toString())
                CardListItem(
                    tabooId = item.tabooId,
                    aspectId = item.aspectId,
                    aspectShortName = item.aspectShortName,
                    cost = item.cost,
                    imageSrc = item.realImageSrc,
                    approachConflict = item.approachConflict,
                    approachConnection = item.approachConnection,
                    approachReason = item.approachReason,
                    approachExploration = item.approachExploration,
                    name = item.name.toString(),
                    typeName = item.typeName,
                    traits = item.traits,
                    level = item.level,
                    isDarkTheme = isDarkTheme,
                    onClick = { navigateToCard.invoke(index) }
                )
            }

            // Handle load states: initial load and pagination load errors/loading.
            cardsLazyItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp).fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = CustomTheme.colors.m)
                            }
                        }
                    }

                    loadState.append is LoadState.Loading -> {
                        item {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp).fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = CustomTheme.colors.m)
                            }
                        }
                    }
                }
            }
        }
    }
}