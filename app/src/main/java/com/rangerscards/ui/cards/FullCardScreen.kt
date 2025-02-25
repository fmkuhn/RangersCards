package com.rangerscards.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.rangerscards.ui.cards.components.FullCard
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun FullCardScreen(
    cardsViewModel: CardsViewModel,
    cardIndex: Int,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val cardsLazyItems = cardsViewModel.searchResults.collectAsLazyPagingItems()
    val pagerState = rememberPagerState(initialPage = cardIndex) { cardsLazyItems.itemCount }
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize(),
    ) { page ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding()
                )
        ) {
            val fullCard by cardsViewModel.getCardById(cardsLazyItems[page]!!.id).collectAsState(null)
            if (fullCard == null) Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp).fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = CustomTheme.colors.m)
            } else FullCard(
                aspectId = fullCard!!.aspectId,
                aspectShortName = fullCard!!.aspectShortName,
                cost = fullCard!!.cost,
                imageSrc = fullCard!!.imageSrc,
                realImageSrc = fullCard!!.realImageSrc,
                name = fullCard!!.name,
                presence = fullCard!!.presence,
                approachConflict = fullCard!!.approachConflict,
                approachReason = fullCard!!.approachReason,
                approachExploration = fullCard!!.approachExploration,
                approachConnection = fullCard!!.approachConnection,
                typeName = fullCard!!.typeName,
                traits = fullCard!!.traits,
                equip = fullCard!!.equip,
                harm = fullCard!!.harm,
                progress = fullCard!!.progress,
                tokenPlurals = fullCard!!.tokenPlurals,
                tokenCount = fullCard!!.tokenCount,
                text = fullCard!!.text,
                flavor = fullCard!!.flavor,
                level = fullCard!!.level,
                setName = fullCard!!.setName,
                setSize = fullCard!!.setSize,
                setPosition = fullCard!!.setPosition,
                isDarkTheme = isDarkTheme
            )
        }
    }
}