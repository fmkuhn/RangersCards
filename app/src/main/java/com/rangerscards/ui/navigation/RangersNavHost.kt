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
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rangerscards.MainActivity
import com.rangerscards.R
import com.rangerscards.data.objects.CampaignMaps
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.campaigns.AddDeckToCampaignScreen
import com.rangerscards.ui.campaigns.AddPlayersToCampaign
import com.rangerscards.ui.campaigns.CampaignCreationScreen
import com.rangerscards.ui.campaigns.CampaignDecksViewModel
import com.rangerscards.ui.campaigns.CampaignJourneyScreen
import com.rangerscards.ui.campaigns.CampaignScreen
import com.rangerscards.ui.campaigns.CampaignViewModel
import com.rangerscards.ui.campaigns.CampaignsScreen
import com.rangerscards.ui.campaigns.CampaignsViewModel
import com.rangerscards.ui.campaigns.dialogs.AddMissionDialog
import com.rangerscards.ui.campaigns.dialogs.AddRemovedDialog
import com.rangerscards.ui.campaigns.dialogs.CampaignEventDialog
import com.rangerscards.ui.campaigns.dialogs.CampaignMissionDialog
import com.rangerscards.ui.campaigns.dialogs.DayInfoDialog
import com.rangerscards.ui.campaigns.dialogs.EndTheDayDialog
import com.rangerscards.ui.campaigns.dialogs.RecordEventDialog
import com.rangerscards.ui.campaigns.dialogs.TravelDialog
import com.rangerscards.ui.campaigns.dialogs.UndoTravelDialog
import com.rangerscards.ui.cards.CardsScreen
import com.rangerscards.ui.cards.CardsViewModel
import com.rangerscards.ui.cards.FullCardScreen
import com.rangerscards.ui.cards.components.RangersSpoilerSwitch
import com.rangerscards.ui.components.RangersTopAppBar
import com.rangerscards.ui.deck.DeckCardsSearchingListScreen
import com.rangerscards.ui.deck.DeckCardsViewModel
import com.rangerscards.ui.deck.DeckFullCardScreen
import com.rangerscards.ui.deck.DeckFullCardWithPagerScreen
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
    val context = LocalContext.current.applicationContext
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
                        settingsViewModel = settingsViewModel,
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
                }
                composable(route = "deck/cardsList",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("deck/{$deckIdArgument}")
                    }
                    val deckViewModel: DeckViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val deckCardsViewModel: DeckCardsViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = backStackEntry
                    )
                    DeckCardsSearchingListScreen(
                        navigateUp = { navController.navigateUp() },
                        deckViewModel = deckViewModel,
                        deckCardsViewModel = deckCardsViewModel,
                        isDarkTheme = isDarkTheme,
                        navigateToCard = { cardIndex ->
                            navController.navigate(
                                "deck/cardsList/card/$cardIndex"
                            ) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                val cardIndexArgument = "cardIndex"
                composable(
                    route = "deck/cardsList/card/{$cardIndexArgument}",
                    arguments = listOf(navArgument(cardIndexArgument) { type = NavType.IntType })
                ) { backStackEntry ->
                    val parentGraphEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("deck/{$deckIdArgument}")
                    }
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("deck/cardsList")
                    }
                    val deckViewModel: DeckViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentGraphEntry
                    )
                    val deckCardsViewModel: DeckCardsViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val cardsViewModel: CardsViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = backStackEntry
                    )
                    val cardIndex = backStackEntry.arguments?.getInt(cardIndexArgument)
                        ?: error("cardIndexArgument cannot be null")
                    DeckFullCardWithPagerScreen(
                        navigateUp = { navController.navigateUp() },
                        deckViewModel = deckViewModel,
                        cardsViewModel = cardsViewModel,
                        deckCardsViewModel = deckCardsViewModel,
                        cardIndex = cardIndex,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding,
                    )
                }
            }
            navigation(
                startDestination = BottomNavScreen.Campaigns.startDestination,
                route = BottomNavScreen.Campaigns.route
            ) {
                composable(BottomNavScreen.Campaigns.startDestination,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) { backStackEntry ->
                    val campaignsViewModel: CampaignsViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = backStackEntry
                    )
                    if (!isCardsLoading) {
                        CampaignsScreen(
                            navigateToCampaign = { campaignId ->
                                navController.navigate(
                                    "${BottomNavScreen.Campaigns.route}/campaign/$campaignId"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            campaignsViewModel = campaignsViewModel,
                            settingsViewModel = settingsViewModel,
                            isDarkTheme = isDarkTheme,
                            contentPadding = innerPadding
                        )
                    } else {
                        CardsDownloadingCircularProgressIndicator()
                    }
                    title = stringResource(BottomNavScreen.Campaigns.label)
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    "${BottomNavScreen.Campaigns.route}/creation"
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
                composable(BottomNavScreen.Campaigns.route + "/creation") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(BottomNavScreen.Campaigns.startDestination)
                    }
                    val campaignsViewModel: CampaignsViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val user by settingsViewModel.userUiState.collectAsState()
                    CampaignCreationScreen(
                        onCancel = {
                            navController.navigateUp()
                        },
                        onCreate = { campaignId ->
                            navController.navigate(
                                "${BottomNavScreen.Campaigns.route}/campaign/$campaignId"
                            ) {
                                popUpTo(BottomNavScreen.Campaigns.startDestination) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        campaignsViewModel = campaignsViewModel,
                        user = user,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding
                    )
                    title = stringResource(R.string.new_campaign)
                    actions = null
                    switch = null
                }
                val campaignIdArgument = "campaignId"
                composable(
                    route = "${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}",
                    enterTransition = {
                        if (initialState.destination.route == BottomNavScreen.Campaigns.startDestination) {
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
                        if (targetState.destination.route == BottomNavScreen.Campaigns.startDestination) {
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
                    arguments = listOf(navArgument(campaignIdArgument) { type = NavType.StringType }))
                    { backStackEntry ->
                        val campaignViewModel: CampaignViewModel = viewModel(
                            factory = AppViewModelProvider.Factory,
                            viewModelStoreOwner = backStackEntry
                        )
                        val campaignId = backStackEntry.arguments?.getString(campaignIdArgument)
                            ?: error("campaignIdArgument cannot be null")
                        val user by settingsViewModel.userUiState.collectAsState()
                        val campaign = campaignViewModel.getCampaignById(campaignId).collectAsState(null)
                        if (!isCardsLoading) {
                            CampaignScreen(
                                campaignViewModel = campaignViewModel,
                                campaign = campaign.value,
                                user = user.currentUser,
                                isDarkTheme = isDarkTheme,
                                navController = navController,
                                contentPadding = innerPadding
                            )
                        } else {
                            CardsDownloadingCircularProgressIndicator()
                        }
                        title = if (campaign.value != null) stringResource(CampaignMaps.campaignCyclesMap[campaign.value!!.cycleId]!!)
                        else ""
                        actions = {
                            IconButton(
                                onClick = {
                                    navController.navigate(
                                        "${BottomNavScreen.Campaigns.route}/campaign/undo"
                                    ) {
                                        launchSingleTop = true
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                                modifier = Modifier.size(32.dp),
                                enabled = !isCardsLoading
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.undo_32dp),
                                    contentDescription = null,
                                    tint = CustomTheme.colors.m,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            //TODO:Add navigation to campaign guide screen
                        }
                        switch = null
                }
                val dayInfoIdArgument = "dayInfoId"
                dialog("${BottomNavScreen.Campaigns.route}/campaign/dayInfo/{$dayInfoIdArgument}",
                    arguments = listOf(navArgument(dayInfoIdArgument) { type = NavType.IntType }))
                { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val dayInfoId = backStackEntry.arguments?.getInt(dayInfoIdArgument)
                        ?: error("dayInfoId cannot be null")
                    val user by settingsViewModel.userUiState.collectAsState()
                    DayInfoDialog(
                        campaignViewModel = campaignViewModel,
                        dayId = dayInfoId,
                        isDarkTheme = isDarkTheme,
                        onBack = { navController.popBackStack() },
                        user = user.currentUser
                    )
                }
                composable(route = "${BottomNavScreen.Campaigns.route}/campaign/journey") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    CampaignJourneyScreen(
                        campaignViewModel = campaignViewModel,
                        contentPadding = innerPadding
                    )
                    title = stringResource(R.string.journey_title)
                    actions = null
                    switch = null
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/endDay") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val user by settingsViewModel.userUiState.collectAsState()
                    EndTheDayDialog(
                        campaignViewModel = campaignViewModel,
                        isDarkTheme = isDarkTheme,
                        onBack = { navController.popBackStack() },
                        user = user.currentUser
                    )
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/travel") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val user by settingsViewModel.userUiState.collectAsState()
                    TravelDialog(
                        campaignViewModel = campaignViewModel,
                        isDarkTheme = isDarkTheme,
                        onBack = { navController.popBackStack() },
                        user = user.currentUser
                    )
                }
                composable(route = "${BottomNavScreen.Campaigns.route}/campaign/addRanger") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val campaignDecksViewModel: CampaignDecksViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = backStackEntry
                    )
                    val user by settingsViewModel.userUiState.collectAsState()
                    AddDeckToCampaignScreen(
                        navController = navController,
                        campaignViewModel = campaignViewModel,
                        campaignDecksViewModel = campaignDecksViewModel,
                        user = user.currentUser,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding,
                    )
                    title = stringResource(R.string.add_ranger_button)
                    actions = null
                    switch = null
                }
                composable(route = "${BottomNavScreen.Campaigns.route}/campaign/addPlayer") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val user by settingsViewModel.userUiState.collectAsState()
                    AddPlayersToCampaign(
                        navigateBack = { navController.navigateUp() },
                        campaignViewModel = campaignViewModel,
                        userState = user,
                        isDarkTheme = isDarkTheme,
                        contentPadding = innerPadding,
                    )
                    title = stringResource(R.string.your_friends)
                    actions = null
                    switch = null
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/undo") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val user by settingsViewModel.userUiState.collectAsState()
                    UndoTravelDialog(
                        campaignViewModel = campaignViewModel,
                        isDarkTheme = isDarkTheme,
                        onBack = { navController.popBackStack() },
                        user = user.currentUser
                    )
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/removeCard") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val user by settingsViewModel.userUiState.collectAsState()
                    AddRemovedDialog(
                        campaignViewModel = campaignViewModel,
                        isDarkTheme = isDarkTheme,
                        onBack = { navController.popBackStack() },
                        user = user.currentUser
                    )
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/recordEvent") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val user by settingsViewModel.userUiState.collectAsState()
                    RecordEventDialog(
                        campaignViewModel = campaignViewModel,
                        isDarkTheme = isDarkTheme,
                        onBack = { navController.popBackStack() },
                        user = user.currentUser
                    )
                }
                val eventNameArgument = "eventNameArgument"
                dialog("${BottomNavScreen.Campaigns.route}/campaign/event/{$eventNameArgument}",
                    arguments = listOf(navArgument(eventNameArgument) { type = NavType.StringType }))
                { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val eventName = backStackEntry.arguments?.getString(eventNameArgument)
                        ?: error("eventNameArgument cannot be null")
                    val user by settingsViewModel.userUiState.collectAsState()
                    CampaignEventDialog(
                        campaignViewModel = campaignViewModel,
                        eventName = eventName,
                        isDarkTheme = isDarkTheme,
                        onBack = { navController.popBackStack() },
                        user = user.currentUser
                    )
                }
                dialog("${BottomNavScreen.Campaigns.route}/campaign/addMission") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val user by settingsViewModel.userUiState.collectAsState()
                    AddMissionDialog(
                        campaignViewModel = campaignViewModel,
                        isDarkTheme = isDarkTheme,
                        onBack = { navController.popBackStack() },
                        user = user.currentUser
                    )
                }
                val missionNameArgument = "missionNameArgument"
                dialog("${BottomNavScreen.Campaigns.route}/campaign/mission/{$missionNameArgument}",
                    arguments = listOf(navArgument(missionNameArgument) { type = NavType.StringType }))
                { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("${BottomNavScreen.Campaigns.route}/campaign/{$campaignIdArgument}")
                    }
                    val campaignViewModel: CampaignViewModel = viewModel(
                        factory = AppViewModelProvider.Factory,
                        viewModelStoreOwner = parentEntry
                    )
                    val missionName = backStackEntry.arguments?.getString(missionNameArgument)
                        ?: error("missionNameArgument cannot be null")
                    val user by settingsViewModel.userUiState.collectAsState()
                    CampaignMissionDialog(
                        campaignViewModel = campaignViewModel,
                        missionName = missionName,
                        isDarkTheme = isDarkTheme,
                        onBack = { navController.popBackStack() },
                        user = user.currentUser
                    )
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