package com.rangerscards.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.GetMyDecksQuery
import com.rangerscards.data.TimestampNormilizer
import com.rangerscards.data.database.CardListItemProjection
import com.rangerscards.data.database.Deck
import com.rangerscards.data.database.DeckListItemProjection
import com.rangerscards.data.database.DecksRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DecksViewModel(
    private val apolloClient: ApolloClient,
    private val decksRepository: DecksRepository
) : ViewModel() {

    // Holds the current search term entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun getAllNetworkDecks(user: FirebaseUser?) {
        viewModelScope.launch {
            if (user != null) {
                val token = user.getIdToken(true).await().token
                val response = apolloClient.query(GetMyDecksQuery(user.uid))
                    .addHttpHeader("Authorization", "Bearer $token")
                    .fetchPolicy(FetchPolicy.NetworkOnly).execute()
                if (response.data != null) {
                    if (response.data?.decks?.isEmpty() == true) decksRepository.syncDecks(emptyList())
                    else decksRepository.syncDecks(response.data!!.decks.toDecks(true))
                }
            }
            else decksRepository.deleteAllUploadedDecks()
        }
    }

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<DeckListItemProjection>> =
        _searchQuery.flatMapLatest { query ->
            // When the search query or include flag changes, perform a new search.
            if (query.trim().isEmpty()) {
                decksRepository.getAllDecks().catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            } else {
                decksRepository.searchDecks(query.trim()).catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            }
        }.cachedIn(viewModelScope)

    fun getCard(id: String): Flow<CardListItemProjection> = decksRepository.getCard(id)

    /**
     * Called when the user enters a new search term.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.update {
            newQuery
        }
    }

    fun clearSearchQuery() {
        _searchQuery.update { "" }
    }
}

/**
 * Extension function to convert [com.rangerscards.fragment.Deck] to [Deck]
 */
fun com.rangerscards.fragment.Deck.toDeck(uploaded: Boolean): Deck {
    return Deck(
        id = this.id,
        uploaded = uploaded,
        userId = this.user_id,
        userHandle = this.user.userInfo.handle,
        slots = this.slots,
        sideSlots = this.side_slots,
        extraSlots = this.extra_slots,
        version = this.version,
        name = this.name,
        description = this.description,
        awa = this.awa,
        spi = this.spi,
        fit = this.fit,
        foc = this.foc,
        createdAt = TimestampNormilizer.fixFraction(this.created_at),
        updatedAt = TimestampNormilizer.fixFraction(this.updated_at),
        meta = this.meta,
        campaignId = this.campaign?.id,
        campaignName = this.campaign?.name,
        campaignRewards = this.campaign?.rewards,
        previousId = this.previous_deck?.id,
        previousSlots = this.previous_deck?.slots,
        previousSideSlots = this.previous_deck?.side_slots,
        nextId = this.next_deck?.id,
    )
}

/**
 * Extension function to convert list of [GetMyDecksQuery.Deck] to list of [Deck]
 */
fun List<GetMyDecksQuery.Deck>.toDecks(uploaded: Boolean): List<Deck> {
    return this.map {
        it.deck.toDeck(uploaded)
    }
}