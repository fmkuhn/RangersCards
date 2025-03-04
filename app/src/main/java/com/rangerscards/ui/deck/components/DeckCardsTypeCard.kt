package com.rangerscards.ui.deck.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun DeckCardsTypeCard(
    showIcon: Boolean = true,
    label: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit),
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = CustomTheme.shapes.large,
        color = CustomTheme.colors.l30,
        border = BorderStroke(1.dp, CustomTheme.colors.d15),
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(
                        CustomTheme.colors.d15,
                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .fillMaxWidth()
                    .clickable { onClick?.invoke() },
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = label,
                        color = CustomTheme.colors.l30,
                        style = CustomTheme.typography.headline,
                        modifier = Modifier.padding(horizontal = 28.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    if (showIcon) Icon(
                        painterResource(id = R.drawable.edit_32dp),
                        contentDescription = null,
                        tint = CustomTheme.colors.l30,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column {
                content()
            }
        }
    }
}