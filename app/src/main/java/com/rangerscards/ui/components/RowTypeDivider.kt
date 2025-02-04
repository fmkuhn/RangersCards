package com.rangerscards.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun RowTypeDivider(
    text: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CustomTheme.colors.l10
    ) {
        Text(
            text = text,
            color = CustomTheme.colors.d10,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}