package com.rangerscards.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class OfflineCardsRepository(private val cardDao: CardDao) : CardsRepository {

    override suspend fun updateAllCards(cards: List<Card>) = cardDao.updateAll(cards)

    override suspend fun insertAllCards(cards: List<Card>) = cardDao.insertAll(cards)

    override suspend fun isExists(): Boolean = cardDao.isExists()

    override fun getAllCards(spoiler: Boolean): Flow<PagingData<Card>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 30
            ),
            pagingSourceFactory = { cardDao.getAllCards(spoiler) }
        ).flow
    }

    override fun searchCards(
        searchQuery: String,
        includeEnglish: Boolean,
        spoiler: Boolean,
        language: String
    ): Flow<PagingData<Card>> {
        // Build the FTS query string
        val ftsQuery = if (language == "ru") {
            val stemedString = searchQuery
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alpha}]+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(separator = " OR ", transform = Porter::stem)
            createQueryString(stemedString, includeEnglish)
        } else {
            createQueryString(searchQuery, includeEnglish)
        }

        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 30
            ),
            pagingSourceFactory = { cardDao.searchCards(ftsQuery, spoiler) }
        ).flow
    }

    private fun createQueryString(searchQuery: String, includeEnglish: Boolean): String {
        return if (includeEnglish) "name:$searchQuery* OR real_name:$searchQuery* OR " +
                "traits:$searchQuery* OR real_traits:$searchQuery* OR " +
                "text:$searchQuery* OR real_text:$searchQuery* OR " +
                "sun_challenge:$searchQuery* OR mountain_challenge:$searchQuery* OR " +
                "crest_challenge:$searchQuery*"
        else "name:$searchQuery* OR traits:$searchQuery* OR " +
                "text:$searchQuery* OR sun_challenge:$searchQuery* OR " +
                "mountain_challenge:$searchQuery* OR crest_challenge:$searchQuery*"
    }
}