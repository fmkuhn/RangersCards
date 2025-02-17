package com.rangerscards.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun FriendListItem(
    handle: String,
    isToAdd: Boolean,
    onClick: () -> Unit,
    onClick2: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = handle,
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Normal,
            fontSize = 20.sp,
            lineHeight = 22.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Surface(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            shape = CustomTheme.shapes.circle,
            color = CustomTheme.colors.l20,
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(if (isToAdd) R.drawable.add_32dp else R.drawable.close_32dp),
                    contentDescription = null,
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        if (onClick2 != null) {
            Surface(
                onClick = onClick2,
                modifier = Modifier.size(40.dp),
                shape = CustomTheme.shapes.circle,
                color = CustomTheme.colors.l20,
                shadowElevation = 4.dp
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        painterResource(R.drawable.close_32dp),
                        contentDescription = null,
                        tint = CustomTheme.colors.m,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}