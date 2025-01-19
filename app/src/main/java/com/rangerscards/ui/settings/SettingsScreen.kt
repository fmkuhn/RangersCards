package com.rangerscards.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rangerscards.MainActivity
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun SettingsScreen(
    mainActivity: MainActivity,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val user by settingsViewModel.userUiState.collectAsState()
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l10)
            .fillMaxSize()
    ) {
        AccountCard(
            mainActivity = mainActivity,
            isDarkTheme = isDarkTheme,
            settingsViewModel = settingsViewModel,
            user = user
        )
        CardsCard(
            isDarkTheme = isDarkTheme,
            settingsViewModel = settingsViewModel
        )
        SettingsCard(
            isDarkTheme = isDarkTheme,
            settingsViewModel = settingsViewModel
        )
    }
}