package com.rangerscards.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun RangersRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconToggleButton(
        checked = selected,
        onCheckedChange = { onClick() },
        modifier = modifier,
        enabled = enabled
    ) {
        val asset = if (selected)
            painterResource(R.drawable.radio_button_checked_32dp)
        else
            painterResource(R.drawable.radio_button_unchecked_32dp)
        Icon(
            painter = asset,
            contentDescription = if (selected) "Selected" else "Not selected",
            tint = CustomTheme.colors.m,
            modifier = Modifier.size(24.dp)
        )
    }
}