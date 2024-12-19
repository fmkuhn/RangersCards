package com.rangerscards.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rangerscards.GetProfileQuery
import com.rangerscards.MainActivity
import com.rangerscards.R
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.settings.components.SettingsButton
import com.rangerscards.ui.settings.components.SettingsCard
import com.rangerscards.ui.settings.components.SettingsClickableSurface
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.RangersCardsTheme

@Composable
fun SettingsScreen(
    mainActivity: MainActivity,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    //TODO: bring the view to a final design
    val user by settingsViewModel.currentUser.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var data: GetProfileQuery.Data? by remember { mutableStateOf(null) }
    LaunchedEffect(user) {
        data = if (user != null) settingsViewModel.getUserInfo(user!!.uid)
        else null
    }
    Column(modifier = modifier
        .background(CustomTheme.colors.l10)
        .fillMaxSize()
    ) {
        if (user == null) {
            Text(text = "Please log in by email and password")
            TextField(
                value = email,
                onValueChange = { email = it },
                singleLine = true,
                label = { Text(text = "Email") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(),
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                label = { Text(text = "Password") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(),
            )
            Button(
                onClick = { settingsViewModel.signIn(mainActivity, email, password) }
            ) {
                Text(text = "Log in")
            }
        } else {
            //TODO: fetch user data from GraphQL and display it
            if (data == null) {
                SettingsCard(
                    isDarkTheme = isDarkTheme,
                    labelIdRes = R.string.account_title
                ) {
                    Column(
                        modifier = Modifier.background(
                            if (isDarkTheme) CustomTheme.colors.l15 else CustomTheme.colors.l20,
                            CustomTheme.shapes.large
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                Modifier.size(32.dp),
                                color = CustomTheme.colors.m
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = CustomTheme.colors.l10
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                Modifier.size(32.dp),
                                color = CustomTheme.colors.m
                            )
                        }
                    }
                    SettingsButton(
                        R.string.exit_account_button,
                        Icons.AutoMirrored.Filled.ExitToApp,
                        { settingsViewModel.signOut(mainActivity) }
                    )
                }
            } else {
                SettingsCard(
                    isDarkTheme = isDarkTheme,
                    labelIdRes = R.string.account_title
                ) {
                    Column(
                        modifier = Modifier.background(
                            if (isDarkTheme) CustomTheme.colors.l15 else CustomTheme.colors.l20,
                            CustomTheme.shapes.large
                        ),
                    ) {
                        SettingsClickableSurface(
                            leadingIcon = Icons.Filled.AccountCircle,
                            trailingIcon = Icons.Filled.Edit,
                            headerId = R.string.account_name_header,
                            text = data?.profile?.userProfile?.handle.toString(),
                            {}
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = CustomTheme.colors.l10
                        )
                        val friendsCount = data?.profile?.userProfile?.friends?.size ?: 0
                        SettingsClickableSurface(
                            leadingIcon = Icons.Filled.Person,
                            trailingIcon = Icons.Filled.Add,
                            headerId = R.string.friends_amount_header,
                            text = pluralStringResource(id = R.plurals.friends_amount, count = friendsCount, friendsCount),
                            {}
                        )
                    }
                    SettingsButton(
                        R.string.exit_account_button,
                        Icons.AutoMirrored.Filled.ExitToApp,
                        { settingsViewModel.signOut(mainActivity) }
                    )
                }
            }
        }
    }
}