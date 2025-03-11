package com.rangerscards.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rangerscards.data.UserPreferencesRepository
import com.rangerscards.data.database.card.CardDeckListItemProjection
import com.rangerscards.data.database.repository.DeckRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Locale

data class DeckInfo(
    val isUpgrade: Boolean,
    val background: String,
    val specialty: String,
    val rewards: List<String>,
    val extraSlots: List<String>
)

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val forth: D,
)

class DeckCardsViewModel(
    private val deckRepository: DeckRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    // Holds the current search term entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _deckInfo= MutableStateFlow<DeckInfo?>(null)

    private val _showAllSpoilers = MutableStateFlow(false)
    val showAllSpoilers = _showAllSpoilers.asStateFlow()

    // Holds the current state of whether to include English search results.
    private val _includeEnglish: StateFlow<Boolean> =
        userPreferencesRepository.isIncludeEnglishSearchResults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    // Holds the current type index.
    private val _typeIndex = MutableStateFlow(0)
    val typeIndex: StateFlow<Int> = _typeIndex.asStateFlow()

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<CardDeckListItemProjection>> =
        combine(_searchQuery, _deckInfo, _typeIndex, _showAllSpoilers) { query, deckInfo, typeIndex, showAllSpoilers ->
            Quadruple(query.trim(), deckInfo, typeIndex, showAllSpoilers)
        }.flatMapLatest { (query, deckInfo, typeIndex, showAllSpoilers) ->
            // When the search query or include flag changes, perform a new search.
            if (deckInfo != null) {
                if (query.isEmpty()) {
                    deckRepository.getAllCards(deckInfo, typeIndex, showAllSpoilers)
                        .catch { throwable ->
                            // Log the error.
                            throwable.printStackTrace()
                            // Return an empty PagingData on error so that the flow continues.
                            emit(PagingData.empty())
                        }
                } else {
                    deckRepository.searchCards(
                        searchQuery = query,
                        deckInfo = deckInfo,
                        includeEnglish = _includeEnglish.value,
                        typeIndex = typeIndex,
                        showAllSpoilers = showAllSpoilers,
                        language = Locale.getDefault().language.substring(0..1)
                    ).catch { throwable ->
                        // Log the error.
                        throwable.printStackTrace()
                        // Return an empty PagingData on error so that the flow continues.
                        emit(PagingData.empty())
                    }
                }
            } else flow { emit(PagingData.empty()) }
        }.cachedIn(viewModelScope)

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

    fun updateDeckInfo(deck: FullDeckState, extraSlots: List<String>) {
        _deckInfo.update {
            DeckInfo(
                isUpgrade = deck.previousId != null,
                background = deck.background,
                specialty = deck.specialty,
                rewards = deck.campaignRewards ?: emptyList(),
                extraSlots = extraSlots
            )
        }
    }

    fun updateShowAllSpoilers(newValue: Boolean) {
        _showAllSpoilers.update { newValue }
    }

    fun onTypeIndexChanged(newIndex: Int) {
        _typeIndex.update {
            newIndex
        }
    }
}