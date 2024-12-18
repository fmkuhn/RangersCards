package com.rangerscards.ui.settings.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.ui.theme.RangersCardsTheme

@Composable
fun SettingsCard(
    @StringRes labelIdRes: Int,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = CustomTheme.shapes.large,
        colors = CardDefaults.cardColors().copy(containerColor = CustomTheme.colors.l30),
        border = BorderStroke(1.dp, CustomTheme.colors.d15),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors().copy(containerColor = CustomTheme.colors.d15),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Text(
                    text = stringResource(id = labelIdRes),
                    color = CustomTheme.colors.l30,
                    style = CustomTheme.typography.headline,
                )
            }
        }
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsButton(
    @StringRes stringId: Int,
    leadingIcon: Any,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isContactUsButton: Boolean = false,
) {
    lateinit var buttonColor: ButtonColors
    val iconColor: Color
    val textColor: Color
    if (isContactUsButton) {
        buttonColor = ButtonDefaults.buttonColors().copy(CustomTheme.colors.gold)
        iconColor = CustomTheme.colors.d20
        textColor = CustomTheme.colors.d30
    } else {
        buttonColor = ButtonDefaults.buttonColors().copy(CustomTheme.colors.d10)
        iconColor = CustomTheme.colors.l20
        textColor = CustomTheme.colors.l30
    }
    Button(
        onClick = { onClick() },
        modifier = modifier,
        shape = CustomTheme.shapes.small,
        colors = buttonColor,
        contentPadding = PaddingValues(8.dp)
    ) {
        when(leadingIcon) {
            is ImageVector -> Icon(
                leadingIcon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            else -> Icon(
                painterResource(id = leadingIcon as Int),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = stringId),
            modifier = Modifier.fillMaxWidth(),
            color = textColor,
            fontFamily = Jost,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        )
    }
}

@Composable
fun SettingsClickableCard(
    leadingIcon: Any,
    trailingIcon: Any,
    @StringRes headerId: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Card(
        modifier = modifier,
        onClick = { onClick() },
        shape = CustomTheme.shapes.large,
        colors = CardDefaults.cardColors().copy(containerColor = CustomTheme.colors.l20),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when(leadingIcon) {
                is ImageVector -> Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(32.dp)
                )
                else -> Icon(
                    painterResource(id = leadingIcon as Int),
                    contentDescription = null,
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = stringResource(id = headerId),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                )
                Text(
                    text = text,
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                )
            }
            when(trailingIcon) {
                is ImageVector -> Icon(
                    trailingIcon,
                    contentDescription = null,
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(32.dp)
                )
                else -> Icon(
                    painterResource(id = trailingIcon as Int),
                    contentDescription = null,
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    RangersCardsTheme {
        Column(
            modifier = Modifier.background(CustomTheme.colors.l10)
                .fillMaxSize()
        ) {
            SettingsCard(labelIdRes = R.string.account_title) {
                Column(
                    modifier = Modifier.background(
                        CustomTheme.colors.l20,
                        CustomTheme.shapes.large
                    ),
                ) {
                    SettingsClickableCard(
                        leadingIcon = Icons.Filled.AccountCircle,
                        trailingIcon = Icons.Filled.Edit,
                        headerId = R.string.account_name_header,
                        text = "Evgeny727",
                        {}
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = CustomTheme.colors.l10
                    )
                    SettingsClickableCard(
                        leadingIcon = Icons.Filled.Person,
                        trailingIcon = Icons.Filled.Add,
                        headerId = R.string.friends_amount_header,
                        text = "3 friends",
                        {}
                    )
                }
                SettingsButton(
                    R.string.exit_account_button,
                    Icons.Filled.Settings,
                    {}
                )
                SettingsButton(
                    R.string.contact_us_button,
                    Icons.Filled.Email,
                    {},
                    isContactUsButton = true
                )
            }
        }
    }
}