package com.rangerscards.ui.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.ui.components.RangersRadioButton
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun SettingsRadioButtonRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    @DrawableRes leadingIcon: Int? = null,
    isSelected: Boolean = false,
) {
    Surface(
        onClick = { onClick() },
        modifier = modifier,
        shape = CustomTheme.shapes.large,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) Icon(
                painterResource(id = leadingIcon),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = text,
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
            RangersRadioButton(
                selected = isSelected,
                onClick = { onClick() },
                modifier = Modifier.size(32.dp)
            )
        }
    }
}