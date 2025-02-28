package com.rangerscards.ui.deck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rangerscards.R
import com.rangerscards.data.objects.CardTextParser
import com.rangerscards.data.objects.DeckMetaMaps
import com.rangerscards.ui.deck.components.FullDeckRoleItem
import com.rangerscards.ui.deck.components.FullDeckStatsItem
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun DeckScreen(
    navController: NavHostController,
    deckViewModel: DeckViewModel,
    deckId: String,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val deck by deckViewModel.getDeck(deckId).collectAsState(null)
    Scaffold(
        containerColor = CustomTheme.colors.l30,
        modifier = Modifier.padding(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding()
        ),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = CustomTheme.colors.l30,
                shadowElevation = 4.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.arrow_back_32dp),
                            contentDescription = null,
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        val background = DeckMetaMaps.background[
                            deck?.meta?.jsonObject?.get("background")?.jsonPrimitive?.content
                        ]
                        val specialty = DeckMetaMaps.specialty[
                            deck?.meta?.jsonObject?.get("specialty")?.jsonPrimitive?.content
                        ]
                        Text(
                            text = buildAnnotatedString {
                                if (background != null)
                                    append(stringResource(background) + " - ")
                                if (specialty != null)
                                    append(stringResource(specialty))
                            },
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = deck?.name ?: "",
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    //TODO:Implement side menu button
                    //IconButton() { }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 8.dp,
                start = 8.dp,
                end = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (deck == null) {
                item {
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
                }
            }
            else {
                item(key = "description/${deckId}") {
                    val role by deckViewModel.getRole(
                        deck!!.meta.jsonObject["role"]?.jsonPrimitive?.content.toString()
                    ).collectAsState(null)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FullDeckRoleItem(
                            imageSrc = role?.realImageSrc,
                            name = role?.name.toString(),
                            text = CardTextParser.parseCustomText(role?.text.toString(), null),
                            campaignName = deck?.campaignName
                        )
                        val stats = listOfNotNull(deck?.awa, deck?.spi, deck?.fit, deck?.foc)
                        FullDeckStatsItem(stats, isDarkTheme)
                    }
                }
            }
        }
    }
}