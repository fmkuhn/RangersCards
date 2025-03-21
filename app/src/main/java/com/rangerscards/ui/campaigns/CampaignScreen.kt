package com.rangerscards.ui.campaigns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.R
import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.ui.campaigns.components.CampaignCurrentPositionCard
import com.rangerscards.ui.campaigns.components.CampaignTitleRow
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.SettingsBaseCard
import com.rangerscards.ui.settings.components.SettingsInputField
import com.rangerscards.ui.theme.CustomTheme
import kotlinx.coroutines.launch

@Composable
fun CampaignScreen(
    campaignViewModel: CampaignViewModel,
    campaign: Campaign?,
    user: FirebaseUser?,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val isSubscriptionStarted by campaignViewModel.isSubscriptionStarted.collectAsState()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    var campaignNameEditing by rememberSaveable { mutableStateOf("") }
    val coroutine = rememberCoroutineScope()
    LaunchedEffect(campaign) {
        if (user != null && !isSubscriptionStarted && campaign?.uploaded == true)
            campaignViewModel.startSubscription(campaign.id)
    }
    Column(
        modifier = Modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
        if (showLoadingDialog) Dialog(
            onDismissRequest = { showLoadingDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            SettingsBaseCard(
                isDarkTheme = isDarkTheme,
                labelIdRes = R.string.saving_deck_changes_header
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), color = CustomTheme.colors.m)
                }
            }
        }
        if (showNameDialog) Dialog(
            onDismissRequest = { showNameDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            SettingsBaseCard(
                isDarkTheme = isDarkTheme,
                labelIdRes = R.string.deck_creation_name_label
            ) {
                if (campaignNameEditing.isEmpty()) campaignNameEditing = campaign?.name ?: ""
                SettingsInputField(
                    leadingIcon = R.drawable.badge_32dp,
                    placeholder = null,
                    textValue = campaignNameEditing,
                    onValueChange = { campaignNameEditing = it },
                    KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SquareButton(
                        stringId = R.string.cancel_button,
                        leadingIcon = R.drawable.close_32dp,
                        onClick = { showNameDialog = false
                            campaignNameEditing = ""
                        },
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            CustomTheme.colors.d30,
                            disabledContainerColor = CustomTheme.colors.m
                        ),
                        iconColor = CustomTheme.colors.warn,
                        textColor = CustomTheme.colors.l30,
                        modifier = Modifier.weight(0.5f),
                    )
                    SquareButton(
                        stringId = R.string.done_button,
                        leadingIcon = R.drawable.done_32dp,
                        onClick = {
                            if (campaignNameEditing != campaign!!.name) coroutine.launch {
                                showNameDialog = false
                                showLoadingDialog = true
                                campaignViewModel.updateCampaignName(
                                    campaign.id,
                                    campaignNameEditing,
                                    campaign.uploaded,
                                    user
                                )
                            }.invokeOnCompletion {
                                campaignNameEditing = ""
                                showLoadingDialog = false
                            } else {
                                campaignNameEditing = ""
                                showNameDialog = false
                            }
                        },
                        buttonColor = ButtonDefaults.buttonColors().copy(
                            CustomTheme.colors.d10,
                            disabledContainerColor = CustomTheme.colors.m
                        ),
                        iconColor = CustomTheme.colors.l15,
                        textColor = CustomTheme.colors.l30,
                        modifier = Modifier.weight(0.5f),
                    )
                }
            }
        }
        if (campaign == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = CustomTheme.colors.m
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(
                    start = 8.dp,
                    end = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    CampaignTitleRow(campaign.name) { showNameDialog = true }
                }
                item {
                    CampaignCurrentPositionCard(
                        campaign.currentLocation,
                        campaign.currentPathTerrain
                    ) { /*TODO:Implement recorded journey*/ }
                }
            }
        }
    }
}