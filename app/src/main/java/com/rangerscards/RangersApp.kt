package com.rangerscards

import androidx.compose.runtime.Composable
import com.rangerscards.ui.navigation.RangersNavHost
import com.rangerscards.ui.settings.SettingsViewModel

/**
 * Top level composable that represents screens for the application.
 */
@Composable
fun RangersApp(mainActivity: MainActivity, isDarkTheme: Boolean, settingsViewModel: SettingsViewModel) {
    RangersNavHost(
        mainActivity = mainActivity,
        isDarkTheme = isDarkTheme,
        settingsViewModel = settingsViewModel
    )
}