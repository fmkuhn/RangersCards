package com.rangerscards.ui.campaigns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.R
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.SettingsBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

@Composable
fun EndTheDayDialog(
    campaignViewModel: CampaignViewModel,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    user: FirebaseUser?
) {
    val campaign by campaignViewModel.campaign.collectAsState()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = CustomTheme.shapes.large,
            color = CustomTheme.colors.l30,
            border = BorderStroke(1.dp, if (isDarkTheme) Color.Transparent else CustomTheme.colors.d15),
            shadowElevation = 4.dp
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.background(
                            if (isDarkTheme) CustomTheme.colors.l15 else CustomTheme.colors.d15,
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        ).fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.end_the_day),
                        color = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                        style = CustomTheme.typography.headline,
                    )
                }
                Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ending_day_number, campaign!!.currentDay),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                    )
                    Text(
                        text = stringResource(R.string.ending_day_warning),
                        color = CustomTheme.colors.d20,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                    )
                }
                SquareButton(
                    stringId = R.string.end_the_day,
                    leadingIcon = R.drawable.camp_32dp,
                    onClick = { coroutine.launch {
                        showLoadingDialog = true
                        campaignViewModel.setCampaignDay(user)
                    }.invokeOnCompletion {
                        showLoadingDialog = false
                        onBack.invoke()
                    } },
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
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
}