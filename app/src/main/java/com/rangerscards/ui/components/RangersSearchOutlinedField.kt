package com.rangerscards.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun RangersSearchOutlinedField(
    query: String,
    @StringRes placeholder: Int,
    onQueryChanged: (String) -> Unit,
    onClearClicked: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(8.dp).sizeIn(maxHeight = 52.dp),
        shape = CustomTheme.shapes.circle,
        border = BorderStroke(1.dp, CustomTheme.colors.l10),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painterResource(R.drawable.search_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(24.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    textStyle = TextStyle(
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Search
                    ),
                    cursorBrush = SolidColor(CustomTheme.colors.m),
                    modifier = Modifier.fillMaxWidth()
                )
                if (query.isEmpty()) Text(
                    text = stringResource(placeholder),
                    color = CustomTheme.colors.d10,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                )
            }
            AnimatedVisibility(query.isNotEmpty()) {
                IconButton(onClick = onClearClicked, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painterResource(R.drawable.close_32dp),
                        contentDescription = "Clear Search",
                        tint = CustomTheme.colors.m,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}