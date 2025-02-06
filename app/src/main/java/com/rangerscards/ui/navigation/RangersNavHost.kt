package com.rangerscards.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rangerscards.MainActivity
import com.rangerscards.R
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.cards.CardsScreen
import com.rangerscards.ui.cards.CardsViewModel
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.settings.SettingsScreen
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun RangersNavHost(
    mainActivity: MainActivity,
    isDarkTheme: Boolean,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    var titleId by rememberSaveable { mutableIntStateOf(TopLevelRoutes.Settings.label) }
    var actions: @Composable (RowScope.() -> Unit)? by remember { mutableStateOf(null) }
    var switch: @Composable (RowScope.() -> Unit)? by remember { mutableStateOf(null) }
    val isCardsLoading by settingsViewModel.isCardsLoading.collectAsState()
    Scaffold(
        topBar = {
            RangersTopAppBar(
                titleId = titleId,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                actions = actions,
                switch = switch
            )
        },
        bottomBar = {
            RangersNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelRoutes.Settings.route
        ) {
            composable(TopLevelRoutes.Settings.route) {
                if (!isCardsLoading) {
                    SettingsScreen(
                        mainActivity = mainActivity,
                        isDarkTheme = isDarkTheme,
                        settingsViewModel = settingsViewModel,
                        contentPadding = innerPadding
                    )
                } else {
                    CardsDownloadingCircularProgressIndicator()
                }
                titleId = TopLevelRoutes.Settings.label
                actions = null
                switch = null
            }
            composable(TopLevelRoutes.Cards.route) { backStackEntry ->
                if (!isCardsLoading) {
                    val cardsViewModel: CardsViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = backStackEntry
                    )
                    CardsScreen(
                        isDarkTheme = isDarkTheme,
                        cardsViewModel = cardsViewModel,
                        contentPadding = innerPadding
                    )
                } else {
                    CardsDownloadingCircularProgressIndicator()
                }
                titleId = TopLevelRoutes.Cards.label
                actions = {/*TODO: Implement action buttons*/}
                switch = {/*TODO: Implement Switch button*/}
            }
            composable(TopLevelRoutes.Decks.route) {
                if (!isCardsLoading) {
                    SettingsScreen(
                        mainActivity = mainActivity,
                        isDarkTheme = isDarkTheme,
                        settingsViewModel = settingsViewModel,
                        contentPadding = innerPadding
                    )
                } else {
                    CardsDownloadingCircularProgressIndicator()
                }
                titleId = TopLevelRoutes.Decks.label
                actions = {/*TODO: Implement action buttons*/}
                switch = null
            }
            composable(TopLevelRoutes.Campaigns.route) {
                if (!isCardsLoading) {
                    SettingsScreen(
                        mainActivity = mainActivity,
                        isDarkTheme = isDarkTheme,
                        settingsViewModel = settingsViewModel,
                        contentPadding = innerPadding
                    )
                } else {
                    CardsDownloadingCircularProgressIndicator()
                }
                titleId = TopLevelRoutes.Campaigns.label
                actions = {/*TODO: Implement action buttons*/}
                switch = null
            }
        }
    }
}

@Composable
fun CardsDownloadingCircularProgressIndicator() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CustomTheme.colors.l30
    ) {
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
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = CustomTheme.colors.m)
        }
    }
}