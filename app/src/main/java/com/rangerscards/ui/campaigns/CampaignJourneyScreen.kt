package com.rangerscards.ui.campaigns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.data.objects.CampaignMaps
import com.rangerscards.data.objects.Path
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun CampaignJourneyScreen(
    campaignViewModel: CampaignViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val campaign by campaignViewModel.campaign.collectAsState()
    val travelHistory by remember(campaign?.history) {
        mutableStateOf(campaignViewModel.buildTravelHistory(campaign?.history ?: emptyList()))
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(travelHistory, CampaignTravelDay::day) { travelDay ->
                // Header for the travel day row
                Column(modifier = Modifier.fillMaxWidth()) {
                    val weatherId = campaignViewModel.getWeatherResId(travelDay.day)
                    Text(
                        text = stringResource(R.string.campaigns_current_day, travelDay.day) +
                                " - ${stringResource(weatherId)}",
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(color = CustomTheme.colors.l10)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Render travel day details
                    TravelDayRow(
                        travelDay = travelDay,
                        currentDay = campaign?.currentDay ?: 1,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TravelDayRow(
    travelDay: CampaignTravelDay,
    currentDay: Int,
) {
    // Resolve starting location from locale
    val start = travelDay.startingLocation
    // Determine final location from the travel list (or fallback to startingLocation)
    val lastEntry = travelDay.travel.lastOrNull()
    val finalLocation = lastEntry?.location ?: travelDay.startingLocation
    // Whether the last travel entry was camped
    val camped = lastEntry?.camped ?: false
    val locationsMap = CampaignMaps.getMapLocations(false)
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Starting location icon
        if (start != null) Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .sizeIn(maxWidth = 110.dp)
                .align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val location = locationsMap[start]!!
            Icon(
                painterResource(location.iconResId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(location.nameResId),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        // Render each travel entry
        travelDay.travel.forEach { entry ->
            Icon(
                painterResource(R.drawable.arrow_forward_32dp),
                contentDescription = "Arrow",
                tint = CustomTheme.colors.m,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
            val path = Path.fromValue(entry.pathTerrain)
            if (path != null) {
                Icon(
                    painter = painterResource(id = path.iconResId),
                    contentDescription = path.name,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            // If not camped, show an arrow and location icon.
            if (!entry.camped) {
                Icon(
                    painterResource(R.drawable.arrow_forward_32dp),
                    contentDescription = "Arrow",
                    tint = CustomTheme.colors.m,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
                val current = locationsMap[entry.location]
                if (current != null) {
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .sizeIn(maxWidth = 110.dp)
                            .align(Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painterResource(current.iconResId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(current.nameResId),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        // At the end, if a final location exists and this is a past day, show a summary icon.
        if (finalLocation != null && travelDay.day < currentDay) {
            Icon(
                painterResource(R.drawable.arrow_forward_32dp),
                contentDescription = "Arrow",
                tint = CustomTheme.colors.m,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
            val finalLocationName = stringResource(locationsMap[finalLocation]!!.nameResId)
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .sizeIn(maxWidth = 110.dp)
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painterResource(if (camped) R.drawable.camping_32dp else R.drawable.camp_32dp),
                    contentDescription = null,
                    tint = CustomTheme.colors.m,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = stringResource(if (camped) R.string.en_route_to else R.string.stayed_at, finalLocationName),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}