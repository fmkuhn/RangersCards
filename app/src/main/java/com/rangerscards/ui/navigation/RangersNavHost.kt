package com.rangerscards.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rangerscards.MainActivity
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.settings.SettingsScreen
import com.rangerscards.ui.settings.SettingsViewModel

@Composable
fun RangersNavHost(
    mainActivity: MainActivity,
    isDarkTheme: Boolean,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    var titleId by rememberSaveable { mutableIntStateOf(TopLevelRoutes.Settings.label) }
    var actions: @Composable (RowScope.() -> Unit)? by rememberSaveable { mutableStateOf(null) }
    var switch: @Composable (RowScope.() -> Unit)? by rememberSaveable { mutableStateOf(null) }
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
                SettingsScreen(
                    mainActivity = mainActivity,
                    isDarkTheme = isDarkTheme,
                    settingsViewModel = settingsViewModel,
                    contentPadding = innerPadding
                )
                titleId = TopLevelRoutes.Settings.label
                actions = null
                switch = null
            }
            composable(TopLevelRoutes.Cards.route) {
                SettingsScreen(
                    mainActivity = mainActivity,
                    isDarkTheme = isDarkTheme,
                    settingsViewModel = settingsViewModel,
                    contentPadding = innerPadding
                )
                titleId = TopLevelRoutes.Cards.label
                actions = {/*TODO: Implement action buttons*/}
                switch = {/*TODO: Implement Switch button*/}
            }
            composable(TopLevelRoutes.Decks.route) {
                SettingsScreen(
                    mainActivity = mainActivity,
                    isDarkTheme = isDarkTheme,
                    settingsViewModel = settingsViewModel,
                    contentPadding = innerPadding
                )
                titleId = TopLevelRoutes.Decks.label
                actions = {/*TODO: Implement action buttons*/}
                switch = null
            }
            composable(TopLevelRoutes.Campaigns.route) {
                SettingsScreen(
                    mainActivity = mainActivity,
                    isDarkTheme = isDarkTheme,
                    settingsViewModel = settingsViewModel,
                    contentPadding = innerPadding
                )
                titleId = TopLevelRoutes.Campaigns.label
                actions = {/*TODO: Implement action buttons*/}
                switch = null
            }
        }
    }
}