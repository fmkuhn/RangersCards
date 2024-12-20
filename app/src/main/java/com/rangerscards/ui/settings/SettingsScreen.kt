package com.rangerscards.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rangerscards.MainActivity
import com.rangerscards.R
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.settings.components.SettingsButton
import com.rangerscards.ui.settings.components.SettingsCard
import com.rangerscards.ui.settings.components.SettingsClickableSurface
import com.rangerscards.ui.settings.components.SettingsInputField
import com.rangerscards.ui.settings.components.TextWhenNotLoggedIn
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    mainActivity: MainActivity,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    //TODO: bring the view to a final design
    val user by settingsViewModel.userUiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var openAuthDialog by remember { mutableStateOf(false) }
    var openHandleDialog by remember { mutableStateOf(false) }
    var userHandle by remember { mutableStateOf("") }
    LaunchedEffect(user.currentUser) {
        if (user.currentUser != null) settingsViewModel.getUserInfo(user.currentUser!!.uid)
        userHandle = user.userInfo?.profile?.userProfile?.handle ?: ""
    }
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l10)
            .fillMaxSize()
    ) {
        SettingsCard(
            isDarkTheme = isDarkTheme,
            labelIdRes = R.string.account_title
        ) {
            if (user.currentUser == null) {
                if (openAuthDialog) Dialog(
                    onDismissRequest = { openAuthDialog = false },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        usePlatformDefaultWidth = false
                    )
                ) {
                    SettingsCard(
                        isDarkTheme = isDarkTheme,
                        labelIdRes = R.string.sign_in_up_to_app_account_title
                    ) {
                        SettingsInputField(
                            leadingIcon = Icons.Filled.Email,
                            placeholder = R.string.email_placeholder,
                            textValue = email,
                            onValueChange = { email = it },
                            KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            )
                        )
                        SettingsInputField(
                            leadingIcon = Icons.Filled.Lock,
                            placeholder = R.string.password_placeholder,
                            textValue = password,
                            onValueChange = { password = it },
                            KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            visualTransformation = PasswordVisualTransformation()
                        )
                        SettingsButton(
                            R.string.sign_in_to_account_button,
                            Icons.AutoMirrored.Filled.ArrowForward,
                            {
                                settingsViewModel.signIn(mainActivity, email, password)
                                openAuthDialog = false
                                email = ""
                                password = ""
                            }
                        )
                        SettingsButton(
                            R.string.sign_up_to_app_account_button,
                            Icons.Filled.Add,
                            {
                                settingsViewModel.createAccount(mainActivity, email, password)
                                openAuthDialog = false
                                email = ""
                                password = ""
                            }
                        )
                    }
                }
                TextWhenNotLoggedIn()
                SettingsButton(
                    stringId = R.string.sign_in_to_app_account_button,
                    leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { openAuthDialog = true }
                )
            } else {
                if (openAuthDialog) Dialog(
                    onDismissRequest = { openAuthDialog = false },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        usePlatformDefaultWidth = false
                    )
                ) {
                    SettingsCard(
                        isDarkTheme = isDarkTheme,
                        labelIdRes = R.string.sign_out_account_title
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_out_account_text),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            modifier = modifier.padding(horizontal = 4.dp)
                        )
                        Button(
                            onClick = { openAuthDialog = false },
                            modifier = modifier,
                            shape = CustomTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors().copy(CustomTheme.colors.d30),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                //painterResource(id = leadingIcon as Int),
                                contentDescription = null,
                                tint = CustomTheme.colors.warn,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.cancel_button),
                                modifier = Modifier.fillMaxWidth(),
                                color = CustomTheme.colors.l30,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.1.sp
                            )
                        }
                        SettingsButton(
                            R.string.sign_out_account_button,
                            Icons.AutoMirrored.Filled.ExitToApp,
                            {
                                settingsViewModel.signOut(mainActivity)
                                openAuthDialog = false
                            }
                        )
                    }
                }
                else if (openHandleDialog) {
                    val coroutineScope = rememberCoroutineScope()
                    var isLoading by remember { mutableStateOf(false) }
                    Dialog(
                        onDismissRequest = { openHandleDialog = false },
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                            usePlatformDefaultWidth = false
                        )
                    ) {
                        SettingsCard(
                            isDarkTheme = isDarkTheme,
                            labelIdRes = R.string.account_name_header
                        ) {
                            if (!isLoading) {
                                if (userHandle.isEmpty()) userHandle = user.userInfo?.profile?.userProfile?.handle ?: ""
                                SettingsInputField(
                                    leadingIcon = Icons.Filled.AccountCircle,
                                    placeholder = null,
                                    textValue = userHandle,
                                    onValueChange = { userHandle = it },
                                    KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done,
                                    )
                                )
                            }
                            else CircularProgressIndicator(
                                Modifier.size(32.dp).align(Alignment.CenterHorizontally),
                                color = CustomTheme.colors.d20
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { openHandleDialog = false },
                                    modifier = modifier.weight(0.5f),
                                    shape = CustomTheme.shapes.small,
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.buttonColors().copy(
                                        CustomTheme.colors.d30,
                                        disabledContainerColor = CustomTheme.colors.m
                                    ),
                                    contentPadding = PaddingValues(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        //painterResource(id = leadingIcon as Int),
                                        contentDescription = null,
                                        tint = CustomTheme.colors.warn,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(id = R.string.cancel_button),
                                        modifier = Modifier.fillMaxWidth(),
                                        color = CustomTheme.colors.l30,
                                        fontFamily = Jost,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 20.sp,
                                        lineHeight = 20.sp,
                                        letterSpacing = 0.1.sp
                                    )
                                }
                                Button(
                                    onClick = {
                                        isLoading = true
                                        coroutineScope.launch {
                                            settingsViewModel.updateHandle(mainActivity, userHandle)
                                        }.invokeOnCompletion {
                                            isLoading = false
                                            openHandleDialog = false
                                            userHandle = user.userInfo?.profile?.userProfile?.handle.toString()
                                        }
                                    },
                                    modifier = modifier.weight(0.5f),
                                    shape = CustomTheme.shapes.small,
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.buttonColors().copy(
                                        CustomTheme.colors.d10,
                                        disabledContainerColor = CustomTheme.colors.m
                                    ),
                                    contentPadding = PaddingValues(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Done,
                                        //painterResource(id = leadingIcon as Int),
                                        contentDescription = null,
                                        tint = CustomTheme.colors.l15,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(id = R.string.done_button),
                                        modifier = Modifier.fillMaxWidth(),
                                        color = CustomTheme.colors.l30,
                                        fontFamily = Jost,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 20.sp,
                                        lineHeight = 20.sp,
                                        letterSpacing = 0.1.sp
                                    )
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.background(
                        if (isDarkTheme) CustomTheme.colors.l15 else CustomTheme.colors.l20,
                        CustomTheme.shapes.large
                    ),
                ) {
                    if (user.userInfo == null) Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            Modifier.size(32.dp),
                            color = CustomTheme.colors.m
                        )
                    }
                    else SettingsClickableSurface(
                        leadingIcon = Icons.Filled.AccountCircle,
                        trailingIcon = Icons.Filled.Edit,
                        headerId = R.string.account_name_header,
                        text = user.userInfo?.profile?.userProfile?.handle ?: "",
                        { openHandleDialog = true }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = CustomTheme.colors.l10
                    )
                    if (user.userInfo == null) Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            Modifier.size(32.dp),
                            color = CustomTheme.colors.m
                        )
                    }
                    else {
                        val friendsCount = user.userInfo?.profile?.userProfile?.friends?.size ?: 0
                        SettingsClickableSurface(
                            leadingIcon = Icons.Filled.Person,
                            trailingIcon = Icons.Filled.Add,
                            headerId = R.string.friends_amount_header,
                            text = pluralStringResource(
                                id = R.plurals.friends_amount,
                                count = friendsCount,
                                friendsCount
                            ),
                            {/*TODO:Implement friends changing*/ }
                        )
                    }
                }
                SettingsButton(
                    R.string.sign_out_account_button,
                    Icons.AutoMirrored.Filled.ExitToApp,
                    { openAuthDialog = true }
                )
            }
        }
    }
}
//        if (user.currentUser == null) {
//            Text(text = "Please log in by email and password")
//            TextField(
//                value = email,
//                onValueChange = { email = it },
//                singleLine = true,
//                label = { Text(text = "Email") },
//                keyboardOptions = KeyboardOptions.Default.copy(
//                    keyboardType = KeyboardType.Email,
//                    imeAction = ImeAction.Next,
//                ),
//                modifier = Modifier
//                    .padding(bottom = 32.dp)
//                    .fillMaxWidth(),
//            )
//            TextField(
//                value = password,
//                onValueChange = { password = it },
//                singleLine = true,
//                label = { Text(text = "Password") },
//                keyboardOptions = KeyboardOptions.Default.copy(
//                    keyboardType = KeyboardType.Password,
//                    imeAction = ImeAction.Done
//                ),
//                visualTransformation = PasswordVisualTransformation(),
//                modifier = Modifier
//                    .padding(bottom = 32.dp)
//                    .fillMaxWidth(),
//            )
//            Button(
//                onClick = { settingsViewModel.signIn(mainActivity, email, password) }
//            ) {
//                Text(text = "Log in")
//            }
//        }