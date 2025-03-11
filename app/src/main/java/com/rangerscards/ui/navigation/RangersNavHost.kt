package com.rangerscards.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.rangerscards.ui.cards.components.RangersSpoilerSwitch
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.deck.DeckFullCardScreen
import com.rangerscards.ui.deck.DeckScreen
import com.rangerscards.ui.deck.DeckViewModel
import com.rangerscards.ui.decks.DeckCreationScreen
import com.rangerscards.ui.decks.DecksScreen
import com.rangerscards.ui.decks.DecksViewModel
import com.rangerscards.ui.settings.SettingsAboutScreen
import com.rangerscards.ui.settings.SettingsFriendsScreen
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
    val showBars = currentRoute?.let { route ->
        // Hide the topBar and bottomBar when in the full-screen flow.
        !route.startsWith("deck/")
    } ?: true
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf(context.getString(BottomNavScreen.Settings.label)) }
    var actions: @Composable (RowScope.() -> Unit)? by remember { mutableStateOf(null) }
    var switch: @Composable (RowScope.() -> Unit)? = null
    val isCardsLoading by settingsViewModel.isCardsLoading.collectAsState()
    Scaffold(
        topBar = {
            AnimatedVisibility(showBars) {
                RangersTopAppBar(
                    title = title,
                    canNavigateBack = bottomNavItems.none { it.startDestination == currentRoute },
                    navigateUp = { navController.navigateUp() },
                    actions = actions,
                    switch = switch
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(showBars) {
                RangersNavigationBar(navController, bottomNavItems, currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Decks.route,
            enterTransition = {
                if (initialState.destination.parent == targetState.destination.parent) {
                    fadeIn(
                        animationSpec = tween(300, easing = LinearEasing)
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Up
                    )
                } else {
                    EnterTransition.None
                }
            },
            exitTransition = {
                if (initialState.destination.parent == targetState.destination.parent) {
                    fadeOut(
                        animationSpec = tween(400, easing = LinearEasing)
                    ) + slideOutOfContainer(
                        animationSpec = tween(400, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.Down
                    )
                } else {
                    ExitTransition.None
                }
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
                            navigateToFriends = {
                                navController.navigate(
                                    "${BottomNavScreen.Settings.route}/friends"
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
                composable(BottomNavScreen.Settings.route + "/friends") {
                    SettingsFriendsScreen(
                        settingsViewModel = settingsViewModel,
                        contentPadding = innerPadding
                    )
                    title = stringResource(R.string.your_friends)
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
                        ?: error("cardIndexArgument cannot be null")
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
                    exitTransition = { ExitTransition.None }) { backStackEntry ->
                    val decksViewModel: DecksViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = backStackEntry
                    )
                    if (!isCardsLoading) {
                        DecksScreen(
                            navigateToDeck = { deckId ->
                                navController.navigate(
                                    "deck/$deckId"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            decksViewModel = decksViewModel,
                            settingsViewModel = settingsViewModel,
                            contentPadding = innerPadding
                        )
                    } else {
                        CardsDownloadingCircularProgressIndicator()
                    }
                    title = stringResource(BottomNavScreen.Decks.label)
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    "${BottomNavScreen.Decks.route}/creation"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                            modifier = Modifier.size(32.dp),
                            enabled = !isCardsLoading
                        ) {
                            Icon(
                                painterResource(id = R.drawable.add_32dp),
                                contentDescription = null,
                                tint = CustomTheme.colors.m,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    switch = null
                }
                composable(BottomNavScreen.Decks.route + "/creation") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(BottomNavScreen.Decks.startDestination)
                    }
                    val decksViewModel: DecksViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val user by settingsViewModel.userUiState.collectAsState()
                    DeckCreationScreen(
                        onCancel = {
                            navController.navigateUp()
                        },
                        onCreate = { deckId ->
                            navController.navigate(
                                "deck/$deckId"
                            ) {
                                popUpTo(BottomNavScreen.Decks.startDestination) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        decksViewModel = decksViewModel,
                        user = user,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                    title = stringResource(R.string.new_deck)
                    actions = null
                    switch = null
                }
            }
            val deckIdArgument = "deckId"
            navigation(
                startDestination = "deck/{$deckIdArgument}",
                route = "deck",
            ) {
                composable(
                    route = "deck/{$deckIdArgument}",
                    enterTransition = {
                        if (!initialState.destination.route.orEmpty().startsWith("deck/")) {
                            fadeIn(
                                animationSpec = tween(300, easing = LinearEasing)
                            ) + slideIntoContainer(
                                animationSpec = tween(300, easing = EaseIn),
                                towards = AnimatedContentTransitionScope.SlideDirection.Up
                            )
                        } else {
                            EnterTransition.None
                        }
                    },
                    exitTransition = {
                        if (!targetState.destination.route.orEmpty().startsWith("deck/")) {
                            fadeOut(
                                animationSpec = tween(400, easing = LinearEasing)
                            ) + slideOutOfContainer(
                                animationSpec = tween(400, easing = EaseOut),
                                towards = AnimatedContentTransitionScope.SlideDirection.Down
                            )
                        } else {
                            ExitTransition.None
                        }
                    },
                    arguments = listOf(navArgument(deckIdArgument) { type = NavType.StringType })
                ) { backStackEntry ->
                    val deckViewModel: DeckViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = backStackEntry
                    )
                    val deckId = backStackEntry.arguments?.getString(deckIdArgument)
                        ?: error("deckIdArgument cannot be null")
                    val user by settingsViewModel.userUiState.collectAsState()
                    DeckScreen(
                        navController = navController,
                        deckViewModel = deckViewModel,
                        deckId = deckId,
                        user = user.currentUser,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                    title = ""
                    actions = null
                    switch = null
                }
                val cardIdArgument = "cardId"
                composable(
                    route = "deck/card/{$cardIdArgument}",
                    arguments = listOf(navArgument(cardIdArgument) { type = NavType.StringType })
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("deck/{$deckIdArgument}")
                    }
                    val deckViewModel: DeckViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val cardsViewModel: CardsViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = backStackEntry
                    )
                    val cardId = backStackEntry.arguments?.getString(cardIdArgument)
                        ?: error("cardIdArgument cannot be null")
                    val isEditing by deckViewModel.isEditing.collectAsState()
                    DeckFullCardScreen(
                        navigateUp = { navController.navigateUp() },
                        deckViewModel = deckViewModel,
                        cardsViewModel = cardsViewModel,
                        cardId = cardId,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding,
                        isEditing = isEditing
                    )
                    title = ""
                    actions = null
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
                            navigateToFriends = {
                                navController.navigate(
                                    "${BottomNavScreen.Settings.route}/friends"
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
                    actions = {/*TODO: Implement action buttons*/}
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