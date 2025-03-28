package com.rangerscards.ui.campaigns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rangerscards.R
import com.rangerscards.data.objects.CampaignMaps
import com.rangerscards.data.objects.DeckMetaMaps
import com.rangerscards.ui.components.DataPicker
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.UserUIState
import com.rangerscards.ui.settings.components.SettingsBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

@Composable
fun CampaignCreationScreen(
    onCancel: () -> Unit,
    onCreate: (String) -> Unit,
    campaignsViewModel: CampaignsViewModel,
    user: UserUIState,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var isCreating by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var isUploading by rememberSaveable { mutableStateOf(false) }
    var cycle by remember { mutableStateOf("") }
    var showDialogPicker by rememberSaveable { mutableStateOf(false) }
    val isLegit by remember {
        derivedStateOf {
            name.isNotEmpty() && cycle.isNotEmpty()
        }
    }
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
        val coroutine = rememberCoroutineScope()
        if (!isCreating) {
            Column(
                modifier = modifier
                    .background(CustomTheme.colors.l30)
                    .fillMaxWidth().padding(horizontal = 8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(text = buildAnnotatedString {
                            append(stringResource(R.string.deck_creation_name_label))
                            withStyle(style = SpanStyle(color = CustomTheme.colors.warn)) {
                                append("*")
                            }
                        })
                    },
                    placeholder = {
                        Text(text = stringResource(R.string.campaign_creation_name_placeholder))
                    },
                    textStyle = TextStyle(
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                    ),
                    singleLine = true,
                    shape = CustomTheme.shapes.small,
                    colors = TextFieldDefaults.colors().copy(
                        focusedIndicatorColor = CustomTheme.colors.m,
                        unfocusedIndicatorColor = CustomTheme.colors.m,
                        unfocusedLabelColor = CustomTheme.colors.d30,
                        focusedLabelColor = CustomTheme.colors.d30,
                        unfocusedPlaceholderColor = CustomTheme.colors.d30,
                        focusedPlaceholderColor = CustomTheme.colors.d30,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                DataPicker(
                    onClick = { showDialogPicker = true },
                    type = R.string.campaign
                ) {
                    Text(
                        text = stringResource(if (cycle.isEmpty())
                            R.string.campaign_placeholder
                        else CampaignMaps.campaignCyclesMap[cycle]!!),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showDialogPicker) Dialog(
                    onDismissRequest = { showDialogPicker = false },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        usePlatformDefaultWidth = false
                    )
                ) {
                    SettingsBaseCard(
                        isDarkTheme = isDarkTheme,
                        labelIdRes = R.string.campaign
                    ) {
                        LazyColumn(modifier = Modifier.sizeIn(maxHeight = 400.dp)) {
                            CampaignMaps.campaignCyclesMap.forEach { (key, value) ->
                                item {
                                    Text(
                                        text = stringResource(value),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                cycle = key
                                                showDialogPicker = false
                                            }
                                            .padding(
                                                horizontal = 16.dp,
                                                vertical = 8.dp
                                            ),
                                        color = CustomTheme.colors.d30,
                                        fontFamily = Jost,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 18.sp,
                                        lineHeight = 22.sp,
                                    )
                                    HorizontalDivider(color = CustomTheme.colors.l10)
                                }
                            }
                        }
                    }
                }
                val context = LocalContext.current.applicationContext
                if (user.currentUser != null && campaignsViewModel.isConnected(context)) Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { isUploading = !isUploading },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.upload_to_rangersdb),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                    RadioButton(
                        selected = isUploading,
                        onClick = { isUploading = !isUploading },
                        colors = RadioButtonDefaults.colors().copy(
                            selectedColor = CustomTheme.colors.m,
                            unselectedColor = CustomTheme.colors.m
                        ),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SquareButton(
                    stringId = R.string.cancel_button,
                    leadingIcon = R.drawable.close_32dp,
                    onClick = onCancel,
                    buttonColor = ButtonDefaults.buttonColors()
                        .copy(CustomTheme.colors.warn),
                    iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.weight(1f)
                )
                SquareButton(
                    stringId = R.string.create_campaign_button,
                    leadingIcon = R.drawable.add_32dp,
                    onClick = {
                        coroutine.launch {
                            isCreating = true
                            campaignsViewModel.createCampaign(
                                name = name,
                                cycleId = cycle,
                                isUploading = isUploading,
                                currentLocation = CampaignMaps.startingLocations[cycle]!!,
                                user = user,
                            )
                        }.invokeOnCompletion {
                            onCreate.invoke(campaignsViewModel.campaignIdToOpen.value)
                        }
                    },
                    buttonColor = ButtonDefaults.buttonColors().copy(
                        containerColor = CustomTheme.colors.d10,
                        disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.25f)
                    ),
                    iconColor = CustomTheme.colors.m,
                    textColor = CustomTheme.colors.l30,
                    modifier = Modifier.weight(1.1f),
                    isEnabled = isLegit
                )
            }
        }
        else Column(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = CustomTheme.colors.m)
        }
    }
}