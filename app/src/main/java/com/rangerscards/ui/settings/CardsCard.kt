package com.rangerscards.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rangerscards.R
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.SettingsBaseCard
import com.rangerscards.ui.settings.components.SettingsClickableSurface
import com.rangerscards.ui.settings.components.SettingsRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme
import java.util.Locale

@Composable
fun CardsCard(
    isDarkTheme: Boolean,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var openLanguagePickerDialog by remember { mutableStateOf(false) }
    var openLanguageConfirmationDialog by remember { mutableStateOf(false) }
    val selectedLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
    if (openLanguagePickerDialog) {
        Dialog(
            onDismissRequest = { openLanguagePickerDialog = false },
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
                    text = Locale.forLanguageTag("en").displayLanguage,
                    onClick = { openLanguagePickerDialog = false
                        if (selectedLocale.language != "en")
                            settingsViewModel.updateLocale("en") },
                    isSelected = selectedLocale.language == "en"
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = CustomTheme.colors.l10
                )
                SettingsRadioButtonRow(
                    text = Locale.forLanguageTag("ru").displayLanguage,
                    onClick = { openLanguagePickerDialog = false
                        if (selectedLocale.language != "ru")
                            settingsViewModel.updateLocale("ru") },
                    isSelected = selectedLocale.language == "ru"
                )
            }
        }
    }
    SettingsBaseCard(
        isDarkTheme = isDarkTheme,
        labelIdRes = R.string.cards_title,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.background(
                if (isDarkTheme) CustomTheme.colors.l15 else CustomTheme.colors.l20,
                CustomTheme.shapes.large
            ),
        ) {
            SettingsClickableSurface(
                leadingIcon = R.drawable.language_32dp,
                trailingIcon = R.drawable.edit_32dp,
                headerId = R.string.language_header,
                text = selectedLocale.displayLanguage,
                { openLanguagePickerDialog = true }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = CustomTheme.colors.l10
            )
            SettingsClickableSurface(
                leadingIcon = R.drawable.cards_32dp,
                trailingIcon = R.drawable.edit_32dp,
                headerId = R.string.collection_header,
                //TODO:Add number of expansions after implementing collection
                text = pluralStringResource(id = R.plurals.expansions_amount, count = 0, 0),
                { /*TODO:Implement collection*/ }
            )
        }
        SquareButton(
            stringId = R.string.update_cards_button,
            leadingIcon = R.drawable.reshuffle,
            onClick = { /*TODO: Implement cards update*/ }
        )
        SquareButton(
            stringId = R.string.rules_button,
            leadingIcon = R.drawable.book_32dp,
            onClick = { /*TODO: Implement cards update*/ }
        )
    }
}