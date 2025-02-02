package com.rangerscards.ui.cards

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rangerscards.MainActivity
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.settings.SettingsViewModel

@Composable
fun CardsScreen(
    mainActivity: MainActivity,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    cardsViewModel: CardsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {

    val user by settingsViewModel.userUiState.collectAsState()
    val userCardsSetting = user.cardsSettings
//    val user by settingsViewModel.userUiState.collectAsState()
//    LaunchedEffect(Unit) {
//        settingsViewModel.downloadCardsIfDatabaseNotExists()
//    }
//    LazyColumn(
//        modifier = modifier
//            .background(CustomTheme.colors.l10)
//            .fillMaxSize()
//            .padding(
//                top = contentPadding.calculateTopPadding(),
//                bottom = contentPadding.calculateBottomPadding()
//            ),
//    ) {
//        item {
//            AccountCard(
//                mainActivity = mainActivity,
//                isDarkTheme = isDarkTheme,
//                settingsViewModel = settingsViewModel,
//                user = user
//            )
//        }
//        item {
//            CardsCard(
//                isDarkTheme = isDarkTheme,
//                settingsViewModel = settingsViewModel
//            )
//        }
//        item {
//            SettingsCard(
//                isDarkTheme = isDarkTheme,
//                settingsViewModel = settingsViewModel,
//                language = user.language
//            )
//        }
//        item {
//            SocialsCard(
//                isDarkTheme = isDarkTheme,
//                settingsViewModel = settingsViewModel,
//                language = user.language
//            )
//        }
//        item {
//            SupportCard(
//                isDarkTheme = isDarkTheme,
//                settingsViewModel = settingsViewModel
//            )
//        }
//    }
}