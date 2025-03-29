package com.rangerscards.ui.campaigns.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun CampaignDialog(
    header: String,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = CustomTheme.shapes.large,
            color = CustomTheme.colors.l30,
            border = BorderStroke(
                1.dp,
                if (isDarkTheme) Color.Transparent else CustomTheme.colors.d15
            ),
            shadowElevation = 4.dp
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.background(
                        if (isDarkTheme) CustomTheme.colors.l15 else CustomTheme.colors.d15,
                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    ).fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                ) {
                    Text(
                        text = header,
                        color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        style = CustomTheme.typography.headline,
                    )
                }
                content()
            }
        }
    }
}