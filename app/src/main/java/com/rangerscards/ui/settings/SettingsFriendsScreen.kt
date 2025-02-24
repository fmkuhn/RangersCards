package com.rangerscards.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.components.RowTypeDivider
import com.rangerscards.ui.settings.components.FriendListItem
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(FlowPreview::class)
@Composable
fun SettingsFriendsScreen(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val searchQuery by settingsViewModel.searchQuery.collectAsState()
    val user by settingsViewModel.userUiState.collectAsState()
    val searchResults by settingsViewModel.searchResults.collectAsState()
    LaunchedEffect(searchQuery) {
        snapshotFlow { searchQuery }
            .debounce(400)
            .distinctUntilChanged()
            .collectLatest { query ->
                if (query.trim().length >= 2) settingsViewModel.getUsersByHandle(query.trim())
                else if (query.trim().isEmpty()) settingsViewModel.getUsersByHandle("")
            }
    }
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l20)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
        RangersSearchOutlinedField(
            query = searchQuery,
            placeholder = R.string.search_friends,
            onQueryChanged = settingsViewModel::onSearchQueryChanged,
            onClearClicked = settingsViewModel::clearSearchQuery
        )
        LazyColumn(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val userProfile = user.userInfo?.profile?.userProfile
            if (userProfile?.friends?.isNotEmpty() == true) {
                itemsIndexed(
                    items = userProfile.friends,
                    key = { _, it -> it.user?.id!! }
                ) { index, friend ->
                    if (index == 0) RowTypeDivider(text = stringResource(R.string.friends_amount_header))
                    FriendListItem(
                        handle = friend.user?.userInfo?.handle ?: "",
                        isToAdd = false,
                        onClick = {
                            settingsViewModel.rejectFriendRequest(friend.user?.id.toString())
                        }
                    )
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
            if (userProfile?.sent_requests?.isNotEmpty() == true) {
                itemsIndexed(
                    items = userProfile.sent_requests,
                    key = { _, it -> it.user?.id!! }
                ) { index, friend ->
                    if (index == 0) RowTypeDivider(text = stringResource(R.string.sent_requests))
                    FriendListItem(
                        handle = friend.user?.userInfo?.handle ?: "",
                        isToAdd = false,
                        onClick = {
                            settingsViewModel.rejectFriendRequest(friend.user?.id.toString())
                        }
                    )
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
            if (userProfile?.received_requests?.isNotEmpty() == true) {
                itemsIndexed(
                    items = userProfile.received_requests,
                    key = { _, it -> it.user?.id!! }
                ) { index, friend ->
                    if (index == 0) RowTypeDivider(text = stringResource(R.string.received_requests))
                    FriendListItem(
                        handle = friend.user?.userInfo?.handle ?: "",
                        isToAdd = true,
                        onClick = {
                            settingsViewModel.acceptFriendRequest(friend.user?.id.toString())
                        }
                    ) {
                        settingsViewModel.rejectFriendRequest(friend.user?.id.toString())
                    }
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
            val filteredSearchResults = searchResults.filter { item ->
                userProfile?.friends?.none { it.user?.id == item.id } == true &&
                        userProfile.sent_requests.none { it.user?.id == item.id } &&
                        userProfile.received_requests.none { it.user?.id == item.id } &&
                        item.id != userProfile.id
            }
            if (filteredSearchResults.isNotEmpty()) {
                itemsIndexed(
                    items = filteredSearchResults,
                    key = { _, it -> it.id }
                ) { index, result ->
                    if (index == 0) RowTypeDivider(text = stringResource(R.string.search_results))
                    FriendListItem(
                        handle = result.userInfo.handle ?: "",
                        isToAdd = true,
                        onClick = {
                            settingsViewModel.sendFriendRequest(result.id)
                        }
                    )
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            } else if (searchQuery.trim().isNotEmpty() && searchQuery.trim().length >= 2) {
                item {
                    RowTypeDivider(text = stringResource(R.string.search_results))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.no_matching_results, searchQuery),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            letterSpacing = 0.2.sp,
                        )
                    }
                }
            }
        }
    }
}