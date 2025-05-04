package com.rangerscards.ui.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.rangerscards.R
import com.rangerscards.ui.cards.CardsViewModel
import com.rangerscards.ui.cards.components.FullCard
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun DeckFullCardWithPagerScreen(
    navigateUp: () -> Unit,
    deckViewModel: DeckViewModel,
    cardsViewModel: CardsViewModel,
    deckCardsViewModel: DeckCardsViewModel,
    cardIndex: Int,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val deck by deckViewModel.originalDeck.collectAsState()
    cardsViewModel.setTabooId(deck?.tabooSetId != null)
    val cardsLazyItems = deckCardsViewModel.searchResults.collectAsLazyPagingItems()
    val pagerState = rememberPagerState(initialPage = cardIndex) { cardsLazyItems.itemCount }
    val values by deckViewModel.updatableValues.collectAsState()
    val slots = deckViewModel.slotsCardsFlow.collectAsState(null)
    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = Modifier.padding(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding()
        ),
        topBar = {
            RangersTopAppBar(
                title = "",
                canNavigateBack = true,
                navigateUp = navigateUp,
                actions = {/*TODO: Implement action buttons*/},
                switch = null
            )
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                ),
        ) { page ->
            val cardCode = cardsLazyItems[page]!!.code
            val fullCard by cardsViewModel.getCardById(cardCode).collectAsState(null)
            val slotInfo = slots.value?.firstOrNull { it.code == cardCode }
            val isInExtraCards = (values?.extraSlots?.get(cardCode) ?: 0) >= 1
            Box(modifier = Modifier.fillMaxSize()) {
                if (fullCard == null) Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = CustomTheme.colors.m
                    )
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                        item {
                            FullCard(
                                tabooId = fullCard!!.tabooId,
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
                                typeId = fullCard!!.typeId,
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
                                subsetSize = fullCard!!.subsetSize,
                                subsetPosition = fullCard!!.subsetPosition,
                                packShortName = fullCard!!.packShortName,
                                sunChallenge = fullCard!!.sunChallenge,
                                mountainChallenge = fullCard!!.mountainChallenge,
                                crestChallenge = fullCard!!.crestChallenge,
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                    // Overlay custom FABs in the bottom-end corner
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .height(IntrinsicSize.Max)
                    ) {
                        IconButton(
                            onClick = {
                                if (isInExtraCards) deckViewModel.removeExtraCard(cardCode)
                                else deckViewModel.addExtraCard(cardCode)
                            },
                            modifier = Modifier.size(62.dp),
                            colors = IconButtonDefaults.iconButtonColors()
                                .copy(containerColor = CustomTheme.colors.d30)
                        ) {
                            Icon(
                                if (isInExtraCards) painterResource(R.drawable.bookmark_32dp_filled)
                                else painterResource(R.drawable.bookmark_32dp),
                                contentDescription = null,
                                tint = CustomTheme.colors.m,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            modifier = Modifier.height(62.dp)
                                .background(CustomTheme.colors.d30, CustomTheme.shapes.circle)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { deckViewModel.removeCard(cardCode, slotInfo?.setId) },
                                colors = IconButtonDefaults.iconButtonColors()
                                    .copy(containerColor = Color.Transparent),
                                modifier = Modifier.size(32.dp),
                                enabled = (values?.slots?.get(cardCode) ?: 0) > 0
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.remove_32dp),
                                    contentDescription = null,
                                    tint = CustomTheme.colors.m,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Surface(
                                modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                                color = CustomTheme.colors.l10,
                                shape = CustomTheme.shapes.small,
                                shadowElevation = 4.dp
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                        .sizeIn(minWidth = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "×${values?.slots?.get(cardCode) ?: 0}",
                                        color = CustomTheme.colors.d10,
                                        fontFamily = Jost,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                    )
                                }
                            }
                            IconButton(
                                onClick = { deckViewModel.addCard(cardCode) },
                                colors = IconButtonDefaults.iconButtonColors()
                                    .copy(containerColor = Color.Transparent),
                                modifier = Modifier.size(32.dp),
                                enabled = (values?.slots?.get(cardCode)
                                    ?: 0) != slotInfo?.deckLimit
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.add_32dp),
                                    contentDescription = null,
                                    tint = CustomTheme.colors.m,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}