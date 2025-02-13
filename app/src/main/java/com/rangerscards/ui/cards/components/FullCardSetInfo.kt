package com.rangerscards.ui.cards.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun FullCardSetInfo(
    aspectId: String?,
    aspectShortName: String?,
    level: Int?,
    name: String,
    size: Int,
    position: Int,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (level != null) Surface(
            color = when (aspectId) {
                "AWA" -> CustomTheme.colors.green
                "FIT" -> CustomTheme.colors.red
                "FOC" -> CustomTheme.colors.blue
                "SPI" -> CustomTheme.colors.orange
                else -> Color.Transparent
            },
            modifier = Modifier
        ) {
            Text(
                text = level.toString() + " " + aspectShortName.toString(),
                color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        Surface(
            color = CustomTheme.colors.l10,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = name + " - " + stringResource(R.string.set_info, position, size),
                color = CustomTheme.colors.d20,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}