package com.rangerscards.ui.deck.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.data.objects.DeckErrorsMap
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun FullDeckProblemsItem(
    problems: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val iconId = "problem"
        BasicText(
            text = buildAnnotatedString {
                appendInlineContent(
                    iconId,
                    "[$iconId]"
                )
                append(" ${
                    stringResource(
                    DeckErrorsMap.deckErrorsMap[problems[0]]!!
                )
                } ")
                if (problems.size > 1) {
                    append(
                        pluralStringResource(
                        R.plurals.more_problems,
                        problems.size - 1,
                        problems.size - 1
                    )
                    )
                }
            },
            inlineContent = mapOf(
                "problem" to InlineTextContent(
                    Placeholder(
                        width = 16.sp,
                        height = 16.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.error_32dp),
                        contentDescription = "Error Icon",
                        tint = CustomTheme.colors.warn
                    )
                },
            ),
            style = TextStyle(
                color = CustomTheme.colors.warn,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 18.sp,
            ),
        )
        HorizontalDivider(color = CustomTheme.colors.l10)
    }
}