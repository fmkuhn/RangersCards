package com.rangerscards.ui.cards

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.repository.CardsRepository
import com.rangerscards.data.database.card.FullCardProjection
import com.rangerscards.data.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Locale

data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

class CardsViewModel(
    private val cardsRepository: CardsRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    // Holds the current search term entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Holds the current state of whether to include English search results.
    private val _includeEnglish: StateFlow<Boolean> =
        userPreferencesRepository.isIncludeEnglishSearchResults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    // Holds the current spoiler state.
    private val _spoiler = MutableStateFlow(false)
    val spoiler: StateFlow<Boolean> = _spoiler.asStateFlow()

    private val _taboo = MutableStateFlow(false)
    private val _packIds = MutableStateFlow(listOf("core"))

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<CardListItemProjection>> =
        combine(_searchQuery, _includeEnglish, _spoiler, _taboo, _packIds) { query, include, spoiler, taboo, packIds ->
            Quintuple(query.trim(), include, spoiler, taboo, packIds)
        }.flatMapLatest { (query, include, spoiler, taboo, packIds) ->
            // When the search query or include flag changes, perform a new search.
            if (query.isEmpty()) {
                cardsRepository.getAllCards(spoiler, taboo, packIds).catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            } else {
                cardsRepository.searchCards(
                    searchQuery = query,
                    includeEnglish = include,
                    spoiler = spoiler,
                    language = Locale.getDefault().language.substring(0..1),
                    taboo = taboo,
                    packIds = packIds
                ).catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            }
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

    /**
     * Called when the user switches spoiler.
     */
    fun onSpoilerChanged() {
        _spoiler.update { !it }
    }

    fun setTabooId(taboo: Boolean?) {
        _taboo.update { taboo ?: false }
    }

    fun setPackIds(packIds: List<String>) {
        _packIds.update { packIds }
    }

    fun getCardById(cardCode: String): Flow<FullCardProjection> =
        cardsRepository.getCardById(cardCode, _taboo.value, _packIds.value)
}