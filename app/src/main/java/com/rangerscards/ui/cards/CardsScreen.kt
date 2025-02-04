package com.rangerscards.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rangerscards.MainActivity
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

    val user by settingsViewModel.userUiState.collectAsState()
    val userCardsSetting = user.cardsSettings
    LazyColumn(
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
        item {
            RowTypeDivider("Test type")
        }
        item {
            CardListItem(
                aspectId = "AWA",
                aspectShortName = "AWA",
                cost = 2,
                imageSrc = null,
                name = "Scuttler Tunnel",
                typeName = null,
                traits = "Being / Companion / Mammal",
                level = 2,
                isDarkTheme = isDarkTheme
            )
        }
        item {
            CardListItem(
                aspectId = "FOC",
                aspectShortName = "FOC",
                cost = null,
                imageSrc = null,
                name = "Scuttler Tunnel",
                typeName = null,
                traits = "Being / Companion / Mammal / and moooooore / maybe some",
                level = 2,
                isDarkTheme = isDarkTheme
            )
        }
        item {
            CardListItem(
                aspectId = "FIT",
                aspectShortName = "FIT",
                cost = 1,
                imageSrc = null,
                name = "Scuttler Tunnel",
                typeName = null,
                traits = "Being / Companion / Mammal",
                level = 1,
                isDarkTheme = isDarkTheme
            )
        }
        item {
            CardListItem(
                aspectId = "SPI",
                aspectShortName = "SPI",
                cost = 3,
                imageSrc = null,
                name = "Scuttler Tunnel",
                typeName = null,
                traits = "Being / Companion / Mammal",
                level = 3,
                isDarkTheme = isDarkTheme
            )
        }
    }
}