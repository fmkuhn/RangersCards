package com.rangerscards.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rangerscards.R
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun SettingsCard(
    isDarkTheme: Boolean,
    settingsViewModel: SettingsViewModel,
    language: String,
    modifier: Modifier = Modifier
) {
    var openThemeDialog by rememberSaveable { mutableStateOf(false) }
    val currentThemeText = when (isDarkTheme) {
        false -> stringResource(id = R.string.light_theme)
        else -> stringResource(id = R.string.dark_theme)
    }
    val systemThemeText = when (isSystemInDarkTheme()) {
        false -> stringResource(id = R.string.light_theme)
        else -> stringResource(id = R.string.dark_theme)
    }
    val themeInt = settingsViewModel.themeState.collectAsState().value
    val englishResults by settingsViewModel.isIncludeEnglishSearchResultsState.collectAsState()
    if (openThemeDialog) {
        Dialog(
            onDismissRequest = { openThemeDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            SettingsBaseCard(
                isDarkTheme = isDarkTheme,
                labelIdRes = R.string.theme_header
            ) {
                SettingsRadioButtonRow(
                    text = stringResource(id = R.string.system_theme, systemThemeText),
                    onClick = { openThemeDialog = false
                        if (themeInt != 2) settingsViewModel.selectTheme(2) },
                    isSelected = themeInt == 2,
                    isSingleValue = true
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = CustomTheme.colors.l10
                )
                SettingsRadioButtonRow(
                    text = stringResource(id = R.string.light_theme),
                    onClick = { openThemeDialog = false
                        if (themeInt != 0) settingsViewModel.selectTheme(0) },
                    isSelected = themeInt == 0,
                    isSingleValue = true
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = CustomTheme.colors.l10
                )
                SettingsRadioButtonRow(
                    text = stringResource(id = R.string.dark_theme),
                    onClick = { openThemeDialog = false
                        if (themeInt != 1) settingsViewModel.selectTheme(1) },
                    isSelected = themeInt == 1,
                    isSingleValue = true
                )
            }
        }
    }
    SettingsBaseCard(
        isDarkTheme = isDarkTheme,
        labelIdRes = R.string.settings_title,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.background(
                if (isDarkTheme) CustomTheme.colors.l15 else CustomTheme.colors.l20,
                CustomTheme.shapes.large
            ),
        ) {
            SettingsClickableSurface(
                leadingIcon = R.drawable.theme_32dp,
                trailingIcon = R.drawable.edit_32dp,
                headerId = R.string.theme_header,
                text = currentThemeText,
                { openThemeDialog = true }
            )
        }
        if (language != "en") SettingsRadioButtonRow(
            text = stringResource(id = R.string.english_search_results_radio_button),
            onClick = { settingsViewModel.setEnglishSearchResultsSetting(!englishResults) },
            leadingIcon = R.drawable.search_32dp,
            isSelected = englishResults
        )
    }
}