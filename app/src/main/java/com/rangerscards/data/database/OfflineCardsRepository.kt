package com.rangerscards.data.database

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rangerscards.data.Porter
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class OfflineCardsRepository(private val cardDao: CardDao) : CardsRepository {

    override suspend fun updateAllCards(cards: List<Card>) = cardDao.updateAll(cards)

    override suspend fun insertAllCards(cards: List<Card>) = cardDao.insertAll(cards)

    override suspend fun isExists(): Boolean = cardDao.isExists()

    override fun getAllCards(spoiler: Boolean): Flow<PagingData<CardListItemProjection>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { cardDao.getAllCards(spoiler) }
        ).flow
    }

    override fun searchCards(
        searchQuery: String,
        includeEnglish: Boolean,
        spoiler: Boolean,
        language: String
    ): Flow<PagingData<CardListItemProjection>> {
        // Build the FTS query string
        val ftsQuery = if (language == "ru") {
            val stemedString = searchQuery
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alpha}]+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(separator = " ", transform = { "${Porter.stem(it)}*" })
            createQueryString(stemedString, includeEnglish, language)
        } else {
            val stemedString = searchQuery
                .lowercase(Locale.forLanguageTag(language))
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alpha}]+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(separator = " ", transform = { "$it*" })
            createQueryString(stemedString, includeEnglish, language)
        }

        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { cardDao.searchCards(ftsQuery, spoiler) }
        ).flow
    }

    private fun createQueryString(searchQuery: String, includeEnglish: Boolean, language: String): String {
        return if (!includeEnglish || language == "en") "composite:($searchQuery)"
        else "real_composite:($searchQuery)"
    }

    override fun getCardById(cardId: String): Flow<FullCardProjection> = cardDao.getCardById(cardId)
}