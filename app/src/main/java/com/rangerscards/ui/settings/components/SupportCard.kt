package com.rangerscards.ui.settings.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.settings.email
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

const val boostyText = "Boosty"
const val boostyLink = "https://boosty.to/rangerscards"

@Composable
fun SupportCard(
    isDarkTheme: Boolean,
    navigateToAbout: () -> Unit,
    navigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val supportText = stringResource(R.string.support_text, boostyText)
    val annotatedText = buildAnnotatedString {
        val startIndex = supportText.indexOf(boostyText)
        append(supportText.substring(0, startIndex))
        pushStringAnnotation(tag = "URL", annotation = boostyLink)
        withStyle(
            style = SpanStyle(
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(boostyText)
        }
        pop()
        append(supportText.substring(startIndex + boostyText.length))
    }
    SettingsBaseCard(
        isDarkTheme = isDarkTheme,
        labelIdRes = R.string.support_title,
        modifier = modifier
    ) {
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
        BasicText(
            text = annotatedText,
            // Capture the layout result.
            onTextLayout = { layoutResult ->
                textLayoutResult = layoutResult
            },
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures { tapOffset: Offset ->
                    // When a tap occurs, use the layout result to get the character offset.
                    textLayoutResult?.let { layoutResult ->
                        // Convert tap coordinates into an offset within the text.
                        val tappedOffset = layoutResult.getOffsetForPosition(tapOffset)
                        // Check if that offset is within our annotation range.
                        annotatedText.getStringAnnotations(
                            tag = "URL",
                            start = tappedOffset,
                            end = tappedOffset
                        ).firstOrNull()?.let { annotation ->
                            // When the email is tapped, open the email client.
                            settingsViewModel.openLink(annotation.item, context)
                        }
                    }
                }
            }.padding(horizontal = 8.dp),
            style = TextStyle(
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.2.sp,
            ),
        )
        SquareButton(
            stringId = R.string.about_button,
            leadingIcon = R.drawable.info_32dp,
            onClick = navigateToAbout
        )
        //TODO:Implement data export and import for backup purposes
//        SquareButton(
//            stringId = R.string.backup_data_button,
//            leadingIcon = R.drawable.book_32dp,
//            onClick = { /*TODO: Implement backup*/ }
//        )
        //TODO:Implement data export for diagnostics
        SquareButton(
            stringId = R.string.diagnostics_button,
            leadingIcon = R.drawable.build_32dp,
            onClick = navigateToDiagnostics
        )
        SquareButton(
            stringId = R.string.contact_us_button,
            leadingIcon = R.drawable.mail_32dp,
            buttonColor = ButtonDefaults.buttonColors().copy(CustomTheme.colors.gold),
            iconColor = if (isDarkTheme) CustomTheme.colors.l20 else CustomTheme.colors.d20,
            textColor = if (isDarkTheme) CustomTheme.colors.l30 else CustomTheme.colors.d30,
            onClick = { settingsViewModel.openEmail(email, context) }
        )
    }
}