package com.rangerscards.ui.settings.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rangerscards.R
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.settings.UserUIState
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CardsCard(
    isDarkTheme: Boolean,
    settingsViewModel: SettingsViewModel,
    userUIState: UserUIState,
    navigateToCollection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var openLanguagePickerDialog by rememberSaveable { mutableStateOf(false) }
    val selectedLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext
    if (showLoadingDialog) Dialog(
        onDismissRequest = { showLoadingDialog = false },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        SettingsBaseCard(
            isDarkTheme = isDarkTheme,
            labelIdRes = R.string.saving_changes_header
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = CustomTheme.colors.m)
            }
        }
    }
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
                labelIdRes = R.string.language_header
            ) {
                Text(
                    text = stringResource(id = R.string.info_text_about_locale_switching),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.2.sp,
                    modifier = modifier.padding(horizontal = 4.dp)
                )
                SettingsRadioButtonRow(
                    text = Locale.forLanguageTag("en").displayLanguage,
                    onClick = { openLanguagePickerDialog = false
                        if (selectedLocale.language != "en")
                            settingsViewModel.updateLocale("en", context) },
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
                            settingsViewModel.updateLocale("ru", context) },
                    isSelected = selectedLocale.language == "ru"
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = CustomTheme.colors.l10
                )
                SettingsRadioButtonRow(
                    text = Locale.forLanguageTag("de").displayLanguage,
                    onClick = { openLanguagePickerDialog = false
                        if (selectedLocale.language != "de")
                            settingsViewModel.updateLocale("de", context) },
                    isSelected = selectedLocale.language == "de"
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = CustomTheme.colors.l10
                )
                SettingsRadioButtonRow(
                    text = Locale.forLanguageTag("fr").displayLanguage,
                    onClick = { openLanguagePickerDialog = false
                        if (selectedLocale.language != "fr")
                            settingsViewModel.updateLocale("fr", context) },
                    isSelected = selectedLocale.language == "fr"
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = CustomTheme.colors.l10
                )
                SettingsRadioButtonRow(
                    text = Locale.forLanguageTag("it").displayLanguage,
                    onClick = { openLanguagePickerDialog = false
                        if (selectedLocale.language != "it")
                            settingsViewModel.updateLocale("it", context) },
                    isSelected = selectedLocale.language == "it"
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
            val amount = userUIState.settings.collection.size
            SettingsClickableSurface(
                leadingIcon = R.drawable.cards_32dp,
                trailingIcon = R.drawable.edit_32dp,
                headerId = R.string.collection_header,
                text = pluralStringResource(id = R.plurals.expansions_amount, count = amount, amount),
                onClick = navigateToCollection
            )
        }
        SquareButton(
            stringId = R.string.update_cards_button,
            leadingIcon = R.drawable.reshuffle,
            onClick = { settingsViewModel.updateCardsIfNotUpdated(context) }
        )
        //TODO:Implement rules
//        SquareButton(
//            stringId = R.string.rules_button,
//            leadingIcon = R.drawable.book_32dp,
//            onClick = {  }
//        )
        SettingsRadioButtonRow(
            text = stringResource(id = R.string.use_taboo),
            onClick = { coroutine.launch { showLoadingDialog = true
                settingsViewModel.setTaboo(context)
            }.invokeOnCompletion { showLoadingDialog = false } },
            leadingIcon = R.drawable.uncommon_wisdom,
            isSelected = userUIState.settings.taboo
        )
    }
}