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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.R
import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.ui.campaigns.components.CampaignCurrentPositionCard
import com.rangerscards.ui.campaigns.components.CampaignSettingsSection
import com.rangerscards.ui.campaigns.components.CampaignTitleRow
import com.rangerscards.ui.campaigns.components.TimeLineLazyRow
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.decks.components.DeckListItem
import com.rangerscards.ui.navigation.BottomNavScreen
import com.rangerscards.ui.settings.components.SettingsBaseCard
import com.rangerscards.ui.settings.components.SettingsInputField
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

@Composable
fun CampaignScreen(
    campaignViewModel: CampaignViewModel,
    campaign: Campaign?,
    user: FirebaseUser?,
    isDarkTheme: Boolean,
    navController: NavHostController,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val isSubscriptionStarted by campaignViewModel.isSubscriptionStarted.collectAsState()
    val campaignState by campaignViewModel.campaign.collectAsState()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    var campaignNameEditing by rememberSaveable { mutableStateOf("") }
    val coroutine = rememberCoroutineScope()
    LaunchedEffect(campaign) {
        if (user != null && !isSubscriptionStarted && campaign?.uploaded == true)
            campaignViewModel.startSubscription(campaign.id)
        if (campaign != null) campaignViewModel.parseCampaign(campaign)
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
                if (campaignNameEditing.isEmpty()) campaignNameEditing = campaignState?.name ?: ""
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
                            if (campaignNameEditing != campaignState!!.name) coroutine.launch {
                                showNameDialog = false
                                showLoadingDialog = true
                                campaignViewModel.updateCampaignName(
                                    campaignState!!.id,
                                    campaignNameEditing,
                                    campaignState!!.uploaded,
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
        if (campaignState == null) {
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    CampaignTitleRow(campaignState!!.name) { showNameDialog = true }
                }
                item {
                    TimeLineLazyRow(
                        campaignViewModel.groupDaysByWeather(),
                        campaignState!!.currentDay
                    ) { navController.navigate(
                        "${BottomNavScreen.Campaigns.route}/campaign/dayInfo/$it"
                    ) {
                        launchSingleTop = true
                    } }
                }
                if (campaignState!!.currentDay == 30 && !campaignState!!.extendedCalendar) item {
                    SquareButton(
                        stringId = R.string.extend_campaign_button,
                        leadingIcon = R.drawable.add_32dp,
                        onClick = { coroutine.launch { campaignViewModel.extendCampaign(user) } },
                        modifier = Modifier.padding(8.dp)
                    )
                }
                item {
                    CampaignCurrentPositionCard(
                        campaignState!!.currentLocation,
                        campaignState!!.currentPathTerrain
                    ) { navController.navigate(
                        "${BottomNavScreen.Campaigns.route}/campaign/journey"
                    ) {
                        launchSingleTop = true
                    } }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        key("travelButton") {
                            SquareButton(
                                stringId = R.string.travel_button,
                                leadingIcon = R.drawable.travel_32dp,
                                iconColor = CustomTheme.colors.m,
                                textColor = CustomTheme.colors.d30,
                                buttonColor = ButtonDefaults.buttonColors().copy(
                                    containerColor = CustomTheme.colors.l20
                                ),
                                onClick = { navController.navigate(
                                    "${BottomNavScreen.Campaigns.route}/campaign/travel"
                                ) {
                                    launchSingleTop = true
                                } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if ((campaignState!!.currentDay != 30 || campaignState!!.extendedCalendar)
                            && campaignState!!.currentDay != 60) key("endDayButton") {
                            SquareButton(
                                stringId = R.string.end_the_day,
                                leadingIcon = R.drawable.camp_32dp,
                                iconColor = CustomTheme.colors.d20,
                                textColor = CustomTheme.colors.d30,
                                buttonColor = ButtonDefaults.buttonColors().copy(
                                    containerColor = CustomTheme.colors.l10
                                ),
                                onClick = { navController.navigate(
                                    "${BottomNavScreen.Campaigns.route}/campaign/endDay"
                                ) {
                                    launchSingleTop = true
                                } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                item {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = stringResource(R.string.rangers_section_header),
                                color = CustomTheme.colors.d10,
                                fontFamily = Jost,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                lineHeight = 20.sp,
                            )
                        }
                        if (campaignState!!.decks.isNotEmpty()) campaignState!!.decks.forEach { deck ->
                            val role = campaignViewModel.getRole(deck.role).collectAsState(null).value
                            if (role != null) DeckListItem(
                                meta = deck.meta,
                                imageSrc = role.realImageSrc!!,
                                name = deck.name,
                                role = role.name!!,
                                onClick = { if (!campaignState!!.uploaded || user?.uid == deck.userId)
                                    navController.navigate(
                                        "deck/${deck.id}"
                                    ) {
                                        launchSingleTop = true
                                    }
                                },
                                isCampaign = false,
                                userName = if (deck.userName == "null") "" else deck.userName,
                                onRemoveDeck = if (!campaignState!!.uploaded || user?.uid == deck.userId) {
                                    { coroutine.launch { showLoadingDialog = true
                                        campaignViewModel.removeDeckCampaign(deck.id, user)
                                    }.invokeOnCompletion { showLoadingDialog = false } }
                                } else null
                            )
                        }
                        SquareButton(
                            stringId = R.string.add_ranger_button,
                            leadingIcon = R.drawable.add_32dp,
                            iconColor = CustomTheme.colors.m,
                            textColor = CustomTheme.colors.d30,
                            buttonColor = ButtonDefaults.buttonColors().copy(
                                containerColor = CustomTheme.colors.l20
                            ),
                            onClick = { navController.navigate(
                                "${BottomNavScreen.Campaigns.route}/campaign/addRanger"
                            ) {
                                launchSingleTop = true
                            } },
                        )
                    }
                }
                item {
                    val isOwner = campaignState!!.userId == user?.uid || campaignState!!.userId.isEmpty()
                    CampaignSettingsSection(
                        onAddOrRemovePlayers = {},
                        onUploadCampaign = if (!campaignState!!.uploaded) {
                            {}
                        } else null,
                        onDeleteOrLeaveCampaign = if (isOwner) { { coroutine.launch {
                            showLoadingDialog = true
                            campaignViewModel.deleteCampaign(user)
                        }.invokeOnCompletion {
                            showLoadingDialog = false
                            navController.navigateUp()
                        } } } else { { coroutine.launch { showLoadingDialog = true
                            campaignViewModel.leaveCampaign(user)
                        }.invokeOnCompletion { showLoadingDialog = false
                            navController.navigateUp()
                        } } },
                        isOwner = isOwner
                    )
                }
            }
        }
    }
}