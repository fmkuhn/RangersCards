package com.rangerscards.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rangerscards.MainActivity
import com.rangerscards.data.Card
import com.rangerscards.ui.components.CardListItem
import com.rangerscards.ui.components.RowTypeDivider
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun CardsScreen(
    mainActivity: MainActivity,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    cardsViewModel: CardsViewModel,
    settingsViewModel: SettingsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {

    //val user by settingsViewModel.userUiState.collectAsState()
    //val userCardsSetting = user.cardsSettings
    val cardsList by cardsViewModel.getAllCards(false).collectAsState(emptyList())
    var groupedCardsList: Map<String, List<Card>> by remember {
        mutableStateOf(cardsList.groupBy { it.setName.toString() })
    }
    LaunchedEffect(cardsList) {
        groupedCardsList = cardsList.groupBy { it.setName.toString() }
    }
    if (groupedCardsList.isEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.background(CustomTheme.colors.l30)
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding()
                ),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = CustomTheme.colors.m)
        }
    }
    else {
        LazyColumn(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding()
                ),
        ) {
            groupedCardsList.forEach { groupKey ->
                item(key = groupKey.key) {
                    RowTypeDivider(groupKey.key)
                }
                items(items = groupKey.value, key = { it.id }) { item ->
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
        }
    }
}