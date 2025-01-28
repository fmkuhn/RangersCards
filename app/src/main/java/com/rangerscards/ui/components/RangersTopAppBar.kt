package com.rangerscards.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme

/**
 * App bar to display title and conditionally display the back navigation.
 */
@Composable
fun RangersTopAppBar(
    @StringRes titleId: Int,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit)?,
    switch: @Composable (RowScope.() -> Unit)?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = CustomTheme.colors.l30,
        shadowElevation = 4.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = if (!canNavigateBack && switch == null) 16.dp else 8.dp,
                vertical = 8.dp)
                .fillMaxWidth()
        ) {
            if (canNavigateBack) IconButton(
                onClick = { navigateUp() },
                colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent)
            ) {
                Icon(
                    painterResource(id = R.drawable.arrow_back_32dp),
                    contentDescription = null,
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(24.dp)
                )
            } else if (switch != null) switch()
            Text(
                text = stringResource(id = titleId),
                color = CustomTheme.colors.d30,
                style = CustomTheme.typography.headline,
                modifier = Modifier.weight(1f)
            )
            if (actions != null) actions()
        }
    }
}