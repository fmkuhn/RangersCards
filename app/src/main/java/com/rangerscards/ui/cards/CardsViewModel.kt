package com.rangerscards.ui.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rangerscards.data.Card
import com.rangerscards.data.CardsRepository
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

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<Card>> =
        combine(_searchQuery, _includeEnglish) { query, include ->
            query.trim() to include
        }.flatMapLatest { (query, include) ->
            // When the search query or include flag changes, perform a new search.
            if (query.isEmpty()) {
                cardsRepository.getAllCards(false).catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            } else {
                cardsRepository.searchCards(
                    searchQuery = query,
                    includeEnglish = include,
                    spoiler = false,
                    language = Locale.getDefault().language.substring(0..1)
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
            newQuery.trim()
        }
    }
}