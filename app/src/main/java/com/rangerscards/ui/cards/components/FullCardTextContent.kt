package com.rangerscards.ui.cards.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.data.CardTextParser
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun FullCardTextContent(aspectId: String?, text: String?, flavor: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 2.dp,
            color = when (aspectId) {
                "AWA" -> CustomTheme.colors.green
                "FIT" -> CustomTheme.colors.red
                "FOC" -> CustomTheme.colors.blue
                "SPI" -> CustomTheme.colors.orange
                else -> CustomTheme.colors.m
            }
        )
        val texts = text.toString().split("\n")
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            texts.forEachIndexed { index, text ->
                val annotatedText = CardTextParser.parseCustomText(text)
                BasicText(
                    text = annotatedText,
                    inlineContent = CardTextParser.inlineIconsMap,
                    style = TextStyle(
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                    )
                )
                if (index != texts.lastIndex) HorizontalDivider(color = CustomTheme.colors.l10)
            }
            if (flavor != null) {
                HorizontalDivider(color = CustomTheme.colors.l10)
                Text(
                    text = flavor,
                    color = CustomTheme.colors.d10,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}