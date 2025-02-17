package com.rangerscards.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rangerscards.MainActivity
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.settings.components.AccountCard
import com.rangerscards.ui.settings.components.CardsCard
import com.rangerscards.ui.settings.components.SettingsCard
import com.rangerscards.ui.settings.components.SocialsCard
import com.rangerscards.ui.settings.components.SupportCard
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun SettingsScreen(
    mainActivity: MainActivity,
    isDarkTheme: Boolean,
    navigateToAbout: () -> Unit,
    navigateToFriends: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    val user by settingsViewModel.userUiState.collectAsState()
    LaunchedEffect(Unit) {
        settingsViewModel.downloadCardsIfDatabaseNotExists()
    }
    LazyColumn(
        modifier = modifier
            .background(CustomTheme.colors.l10)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
        item {
            AccountCard(
                mainActivity = mainActivity,
                isDarkTheme = isDarkTheme,
                settingsViewModel = settingsViewModel,
                user = user,
                navigateToFriends = { navigateToFriends.invoke() }
            )
        }
        item {
            CardsCard(
                isDarkTheme = isDarkTheme,
                settingsViewModel = settingsViewModel
            )
        }
        item {
            SettingsCard(
                isDarkTheme = isDarkTheme,
                settingsViewModel = settingsViewModel,
                language = user.language
            )
        }
        item {
            SocialsCard(
                isDarkTheme = isDarkTheme,
                settingsViewModel = settingsViewModel,
                language = user.language
            )
        }
        item {
            SupportCard(
                isDarkTheme = isDarkTheme,
                settingsViewModel = settingsViewModel,
                navigateToAbout = { navigateToAbout.invoke() }
            )
        }
    }
}