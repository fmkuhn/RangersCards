package com.rangerscards.ui.campaigns

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.data.objects.ChallengeDeck
import com.rangerscards.ui.campaigns.components.ChallengeCard
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

@Composable
fun CampaignChallengeDeckScreen(
    campaignViewModel: CampaignViewModel,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val challengeDeckSize by campaignViewModel.currentChallengeDeck!!.size.collectAsState()
    val challengeDeckScoutPosition by campaignViewModel.currentChallengeDeck!!.scoutPosition.collectAsState()
    val revealedCardIds = remember { mutableStateListOf<Int>() }
    val coroutineScope = rememberCoroutineScope()
    val isScoutAvailable = remember { derivedStateOf { (challengeDeckScoutPosition < challengeDeckSize) &&
            ((revealedCardIds.isNotEmpty() && challengeDeckScoutPosition != 0) ||
                revealedCardIds.isEmpty())
    } }
    Column(
        modifier = Modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item("scoutedCardsList") {
                val showRow by remember { derivedStateOf { revealedCardIds.size > 1 } }
                AnimatedVisibility(showRow) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(revealedCardIds.dropLast(1), { id -> id }) { id ->
                            ChallengeCard(id, isDarkTheme, true)
                        }
                    }
                }
            }
            item("revealedCard") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (revealedCardIds.isEmpty()) Column(
                        modifier = Modifier.size(220.dp, 288.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.draw_challenge_card_placeholder),
                            color = CustomTheme.colors.d15,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            lineHeight = 22.sp
                        )
                    } else ChallengeCard(revealedCardIds.last(), isDarkTheme)
                }
            }
            item("returnScoutedCardsButton") {
                AnimatedVisibility(challengeDeckScoutPosition > 0) {
                    SquareButton(
                        stringId = R.string.return_scouted_cards_button,
                        leadingIcon = R.drawable.close_32dp,
                        iconColor = CustomTheme.colors.warn,
                        textColor = CustomTheme.colors.l30,
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            containerColor = CustomTheme.colors.d30
                        ),
                        onClick = { campaignViewModel.discardScoutedCards()
                                  revealedCardIds.clear() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item("drawCardButton") {
                SquareButton(
                    stringId = R.string.draw_challenge_card_button,
                    leadingIcon = R.drawable.card,
                    iconColor = CustomTheme.colors.l15,
                    textColor = CustomTheme.colors.l30,
                    buttonColor = ButtonDefaults.buttonColors().copy(
                        containerColor = CustomTheme.colors.d10,
                        disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.25f)
                    ),
                    onClick = {
                        coroutineScope.launch {
                            if (revealedCardIds.isNotEmpty()) {
                                if (challengeDeckScoutPosition == 0) {
                                    val card = ChallengeDeck.challengeDeck[revealedCardIds.first()]
                                    if (card!!.reshuffle) campaignViewModel.reshuffleChallengeDeck()
                                } else campaignViewModel.discardScoutedCards()
                                revealedCardIds.clear()
                            } else {
                                val returnedValue = campaignViewModel.drawChallengeCard()
                                if (returnedValue != null) {
                                    revealedCardIds.add(returnedValue)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isEnabled = challengeDeckSize > 0
                )
            }
            item("scoutCardButton") {
                SquareButton(
                    stringId = R.string.scout_challenge_card_button,
                    leadingIcon = R.drawable.scout_32dp,
                    iconColor = CustomTheme.colors.l15,
                    textColor = CustomTheme.colors.l30,
                    buttonColor = ButtonDefaults.buttonColors().copy(
                        containerColor = CustomTheme.colors.d10,
                        disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.25f)
                    ),
                    onClick = { val scoutedCardId = campaignViewModel.scoutChallengeCard()
                              if (scoutedCardId != null) revealedCardIds.add(scoutedCardId) },
                    modifier = Modifier.fillMaxWidth(),
                    isEnabled = isScoutAvailable.value
                )
            }
        }
    }
}