package com.rangerscards.ui.decks

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rangerscards.ui.settings.SettingsViewModel

@Composable
fun DecksScreen(
    isDarkTheme: Boolean,
    navigateToDeck: (Int) -> Unit,
    decksViewModel: DecksViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val user by settingsViewModel.userUiState.collectAsState()
    LaunchedEffect(Unit) {
        decksViewModel.getAllNetworkDecks(user.currentUser)
    }
}