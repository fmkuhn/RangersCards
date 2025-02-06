package com.rangerscards.ui.cards

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.data.Card
import com.rangerscards.ui.components.CardListItem
import com.rangerscards.ui.components.RowTypeDivider
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun CardsScreen(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    cardsViewModel: CardsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val searchQuery by cardsViewModel.searchQuery.collectAsState()
    val cardsLazyItems = cardsViewModel.searchResults.collectAsLazyPagingItems()
    // Remember a LazyListState to control and observe scroll position.
    val listState = rememberLazyListState()

    // Whenever the search query changes, scroll the list back to the top.
    LaunchedEffect(searchQuery) {
        // Scroll to the first item
        listState.animateScrollToItem(0)
    }

    // Search TextField: user enters the search query.
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l15)
            .fillMaxWidth()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery -> cardsViewModel.onSearchQueryChanged(newQuery) },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        LazyColumn(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize(),
            state = listState
        ) {
            items(
                count = cardsLazyItems.itemCount,
                key = cardsLazyItems.itemKey(Card::id),
                contentType = cardsLazyItems.itemContentType { it }
            ) { index ->
                if (cardsLazyItems.itemCount == 0) {
                    //TODO:Change text
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.cards_updating),
                            color = CustomTheme.colors.d30,
                            style = CustomTheme.typography.headline
                        )
                    }
                } else {
                    val item = cardsLazyItems[index] ?: return@items
                    val showHeader = if (index == 0) true
                    else cardsLazyItems[index - 1]?.setName != item.setName
                    if (showHeader) RowTypeDivider(text = item.setName.toString())
                    CardListItem(
                        aspectId = item.aspectId,
                        aspectShortName = item.aspectShortName,
                        cost = item.cost,
                        imageSrc = item.imageSrc,
                        name = item.name.toString(),
                        typeName = item.typeName,
                        traits = item.traits,
                        level = item.level,
                        isDarkTheme = isDarkTheme,
                        onClick = {/*TODO: Implement navigation to item's page*/}
                    )
                }
            }

            // Handle load states: initial load and pagination load errors/loading.
            cardsLazyItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp).fillMaxWidth(),
                                color = CustomTheme.colors.m)
                        }
                    }

                    loadState.append is LoadState.Loading -> {
                        item {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp).fillMaxWidth(),
                                color = CustomTheme.colors.m)
                        }
                    }
                }
            }
        }
    }
}