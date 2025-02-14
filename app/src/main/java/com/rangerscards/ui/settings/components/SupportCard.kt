package com.rangerscards.ui.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun SupportCard(
    isDarkTheme: Boolean,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    SettingsBaseCard(
        isDarkTheme = isDarkTheme,
        labelIdRes = R.string.support_title,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.support_text, "Boosty"),
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.2.sp,
            modifier = modifier.padding(horizontal = 8.dp)
        )
        SquareButton(
            stringId = R.string.about_button,
            leadingIcon = R.drawable.info_32dp,
            onClick = { /*TODO: Implement about page*/ }
        )
        SquareButton(
            stringId = R.string.backup_data_button,
            leadingIcon = R.drawable.book_32dp,
            onClick = { /*TODO: Implement backup*/ }
        )
        SquareButton(
            stringId = R.string.diagnostics_button,
            leadingIcon = R.drawable.build_32dp,
            onClick = { /*TODO: Implement diagnostic*/ }
        )
        SquareButton(
            stringId = R.string.contact_us_button,
            leadingIcon = R.drawable.mail_32dp,
            buttonColor = ButtonDefaults.buttonColors().copy(CustomTheme.colors.gold),
            iconColor = if (isDarkTheme) CustomTheme.colors.l20 else CustomTheme.colors.d20,
            textColor = if (isDarkTheme) CustomTheme.colors.l30 else CustomTheme.colors.d30,
            onClick = { /*TODO: Implement contact us button onClick*/ }
        )
    }
}