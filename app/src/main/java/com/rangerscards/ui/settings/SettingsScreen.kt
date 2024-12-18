package com.rangerscards.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apollographql.apollo.api.Optional
import com.rangerscards.GetProfileByHandleQuery
import com.rangerscards.GetProfileQuery
import com.rangerscards.MainActivity
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.theme.RangersCardsTheme
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    mainActivity: MainActivity,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    //TODO: bring the view to a final design
    val user by settingsViewModel.currentUser.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    if (user == null) {
        Column(modifier = modifier) {
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
        }
    } else {
        val data = settingsViewModel.getUserInfo(user!!.uid)
        //TODO: fetch user data from GraphQL and display it
        Column(modifier = modifier) {
            Text(
                text = data?.profile?.userProfile?.handle ?: "Without handle"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user!!.email.toString()
            )
            Button(
                onClick = { settingsViewModel.signOut(mainActivity) }
            ) {
                Text(text = "Log out")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    RangersCardsTheme {
        SettingsScreen(MainActivity())
    }
}