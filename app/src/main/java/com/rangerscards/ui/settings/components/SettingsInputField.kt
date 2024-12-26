package com.rangerscards.ui.settings.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.ui.theme.RangersCardsTheme

@Composable
fun SettingsInputField(
    leadingIcon: Any,
    @StringRes placeholder: Int?,
    textValue: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    visualTransformation: PasswordVisualTransformation? = null
) {
    TextField(
        value = textValue,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = CustomTheme.shapes.small,
        placeholder = {Text(
            text = if (placeholder != null) stringResource(id = placeholder) else "",
            color = CustomTheme.colors.d15,
            fontFamily = Jost,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        )},
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(
            color = CustomTheme.colors.d30,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        ),
        leadingIcon = { when(leadingIcon) {
            is ImageVector -> Icon(
                leadingIcon,
                contentDescription = null,
                tint = CustomTheme.colors.d20,
                modifier = Modifier.size(24.dp)
            )
            else -> Icon(
                painterResource(id = leadingIcon as Int),
                contentDescription = null,
                tint = CustomTheme.colors.d20,
                modifier = Modifier.size(24.dp)
            ) }
        },
        visualTransformation = visualTransformation ?: VisualTransformation.None,
        singleLine = true,
        colors = TextFieldDefaults.colors().copy(
            focusedContainerColor = CustomTheme.colors.l15,
            unfocusedContainerColor = CustomTheme.colors.l15,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = CustomTheme.colors.d30
        )
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SettingsInputFieldPreview() {
    RangersCardsTheme {
        SettingsInputField(
            Icons.Filled.Email,
            R.string.sign_in_to_account_button,
            "",
            {},
            KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                )
        )
    }
}