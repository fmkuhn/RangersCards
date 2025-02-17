package com.rangerscards.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rangerscards.MainActivity
import com.rangerscards.R
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.cards.CardsScreen
import com.rangerscards.ui.cards.CardsViewModel
import com.rangerscards.ui.cards.FullCardScreen
import com.rangerscards.ui.cards.components.FullCard
import com.rangerscards.ui.cards.components.RangersSpoilerSwitch
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.settings.SettingsAboutScreen
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
    val bottomNavItems = listOf(BottomNavScreen.Cards, BottomNavScreen.Decks,
        BottomNavScreen.Campaigns, BottomNavScreen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf(context.getString(BottomNavScreen.Settings.label)) }
    var actions: @Composable (RowScope.() -> Unit)? by remember { mutableStateOf(null) }
    var switch: @Composable (RowScope.() -> Unit)? = null
    val isCardsLoading by settingsViewModel.isCardsLoading.collectAsState()
    Scaffold(
        topBar = {
            RangersTopAppBar(
                title = title,
                canNavigateBack = bottomNavItems.none { it.startDestination == currentRoute },
                navigateUp = { navController.navigateUp() },
                actions = actions,
                switch = switch
            )
        },
        bottomBar = {
            RangersNavigationBar(navController, bottomNavItems, currentRoute)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Settings.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(300, easing = LinearEasing)
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Up
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(400, easing = LinearEasing)
                ) + slideOutOfContainer(
                    animationSpec = tween(400, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Down
                )
            }
        ) {
            navigation(
                startDestination = BottomNavScreen.Settings.startDestination,
                route = BottomNavScreen.Settings.route
            ) {
                composable(BottomNavScreen.Settings.startDestination,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) {
                    if (!isCardsLoading) {
                        SettingsScreen(
                            mainActivity = mainActivity,
                            isDarkTheme = isDarkTheme,
                            navigateToAbout = {
                                navController.navigate(
                                    "${BottomNavScreen.Settings.route}/about"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            settingsViewModel = settingsViewModel,
                            contentPadding = innerPadding
                        )
                    } else {
                        CardsDownloadingCircularProgressIndicator()
                    }
                    title = stringResource(BottomNavScreen.Settings.label)
                    actions = null
                    switch = null
                }
                composable(BottomNavScreen.Settings.route + "/about") {
                    SettingsAboutScreen(
                        contentPadding = innerPadding
                    )
                    title = stringResource(R.string.about_button)
                    actions = null
                    switch = null
                }
            }
            navigation(
                startDestination = BottomNavScreen.Cards.startDestination,
                route = BottomNavScreen.Cards.route
            ) {
                composable(BottomNavScreen.Cards.startDestination,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) { backStackEntry ->
                    val cardsViewModel: CardsViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = backStackEntry
                    )
                    if (!isCardsLoading) {
                        CardsScreen(
                            isDarkTheme = isDarkTheme,
                            cardsViewModel = cardsViewModel,
                            contentPadding = innerPadding,
                            navigateToCard = { cardIndex ->
                                navController.navigate(
                                    "${BottomNavScreen.Cards.route}/card/$cardIndex"
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    } else {
                        CardsDownloadingCircularProgressIndicator()
                    }
                    title = stringResource(BottomNavScreen.Cards.label)
                    actions = {/*TODO: Implement action buttons*/}
                    switch = {
                        val spoiler by cardsViewModel.spoiler.collectAsState()
                        RangersSpoilerSwitch(spoiler, cardsViewModel::onSpoilerChanged)
                    }
                }
                val cardIndexArgument = "cardIndex"
                composable(
                    route = BottomNavScreen.Cards.route + "/card/{$cardIndexArgument}",
                    arguments = listOf(navArgument(cardIndexArgument) { type = NavType.IntType })
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(BottomNavScreen.Cards.startDestination)
                    }
                    val cardsViewModel: CardsViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val cardIndex = backStackEntry.arguments?.getInt(cardIndexArgument)
                        ?: error("cardIdArgument cannot be null")
                    FullCardScreen(
                        cardsViewModel = cardsViewModel,
                        cardIndex = cardIndex,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                    title = ""
                    actions = {/*TODO: Implement action buttons*/}
                    switch = null
                }
            }
            navigation(
                startDestination = BottomNavScreen.Decks.startDestination,
                route = BottomNavScreen.Decks.route
            ) {
                composable(BottomNavScreen.Decks.startDestination,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) {
                    if (!isCardsLoading) {
                        Column(
                            modifier = Modifier
                                .background(CustomTheme.colors.l30)
                                .fillMaxSize()
                                .padding(top = 60.dp)
                        ) {
                            FullCard(
                                aspectId = "AWA",
                                aspectShortName = "AWA",
                                cost = 2,
                                imageSrc = "/img/card/core_ru/01038.jpg",
                                realImageSrc = "/img/card/core/01038.jpg",
                                presence = 1,
                                approachConflict = 1,
                                approachReason = 1,
                                approachExploration = 1,
                                approachConnection = 1,
                                typeName = null,
                                traits = "Being / Companion / Mammal",
                                equip = 2,
                                harm = 1,
                                progress = 1,
                                tokenPlurals = "Запись,Записи,Записей",
                                tokenCount = 0,
                                text = "FOC + *: Navigate the winding tunnel to move your * to this feature.\n" +
                                        "Response: When you perform a test, if your * is on this feature, add 1* to it to dodge each card in the same area.\n" +
                                        "Clear *: If your * is on this feature, suffer 1 injury.",
                                flavor = "Some flavor",
                                level = 2,
                                setName = "Reward",
                                setSize = 31,
                                setPosition = 2,
                                isDarkTheme = isDarkTheme,
                                name = "Scuttler g Tunnel\nnew g line"
                            )
                        }
                    } else {
                        CardsDownloadingCircularProgressIndicator()
                    }
                    title = stringResource(BottomNavScreen.Decks.label)
                    actions = {/*TODO: Implement action buttons*/}
                    switch = null
                }
            }
            navigation(
                startDestination = BottomNavScreen.Campaigns.startDestination,
                route = BottomNavScreen.Campaigns.route
            ) {
                composable(BottomNavScreen.Campaigns.startDestination,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) {
                    if (!isCardsLoading) {
                        SettingsScreen(
                            mainActivity = mainActivity,
                            isDarkTheme = isDarkTheme,
                            navigateToAbout = {
                                navController.navigate(
                                    "${BottomNavScreen.Settings.route}/about"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            settingsViewModel = settingsViewModel,
                            contentPadding = innerPadding
                        )
                    } else {
                        CardsDownloadingCircularProgressIndicator()
                    }
                    title = stringResource(BottomNavScreen.Campaigns.label)
                    actions = null
                    switch = null
                }
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