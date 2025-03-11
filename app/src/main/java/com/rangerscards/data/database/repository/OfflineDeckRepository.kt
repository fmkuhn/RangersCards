package com.rangerscards.data.database.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rangerscards.data.database.card.CardDeckListItemProjection
import com.rangerscards.data.database.dao.DeckDao
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.RoleCardProjection
import com.rangerscards.data.objects.PorterStem
import com.rangerscards.ui.deck.DeckInfo
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class OfflineDeckRepository(private val deckDao: DeckDao) : DeckRepository {

    override suspend fun getDeck(id: String): Deck = deckDao.getDeckById(id)

    override suspend fun getRole(id: String): RoleCardProjection = deckDao.getRole(id)

    override suspend fun updateDeck(deck: Deck) = deckDao.updateDeck(deck)

    override fun getCardsByIds(ids: List<String>): Flow<List<CardDeckListItemProjection>> =
        deckDao.getCardsByIds(ids)

    override suspend fun getChangedCardsByIds(ids: List<String>): List<CardDeckListItemProjection> =
        deckDao.getChangedCardsByIds(ids)

    override fun getAllCards(
        deckInfo: DeckInfo,
        typeIndex: Int,
        showAllSpoilers: Boolean,
    ): Flow<PagingData<CardDeckListItemProjection>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        val pagingSourceFactory = if (!deckInfo.isUpgrade) {
            when(typeIndex) {
                0 -> deckDao.getPersonalityCards()
                1 -> deckDao.getBackgroundCards(deckInfo.background)
                2 -> deckDao.getSpecialtyCards(deckInfo.specialty)
                else -> deckDao.getOutsideInterestCards(deckInfo.background, deckInfo.specialty)
            }
        } else {
            when(typeIndex) {
                0 -> if (showAllSpoilers) deckDao.getAllRewards()
                else deckDao.getRewards(deckInfo.rewards)
                1 -> deckDao.getAllMaladies()
                2 -> deckDao.getAllCards()
                else -> deckDao.getExtraCards(deckInfo.extraSlots)
            }
        }
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { pagingSourceFactory }
        ).flow
    }

    override fun searchCards(
        searchQuery: String,
        deckInfo: DeckInfo,
        includeEnglish: Boolean,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        language: String
    ): Flow<PagingData<CardDeckListItemProjection>> {
        // Build the FTS query string
        val ftsQuery = if (language == "ru") {
            val stemedString = searchQuery
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alpha}]+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(separator = " ", transform = { "${PorterStem.stem(it)}*" })
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
        val pagingSourceFactory = if (!deckInfo.isUpgrade) {
            when(typeIndex) {
                0 -> deckDao.searchPersonalityCards(ftsQuery)
                1 -> deckDao.searchBackgroundCards(ftsQuery, deckInfo.background)
                2 -> deckDao.searchSpecialtyCards(ftsQuery, deckInfo.specialty)
                else -> deckDao.searchOutsideInterestCards(ftsQuery, deckInfo.background, deckInfo.specialty)
            }
        } else {
            when(typeIndex) {
                0 -> if (showAllSpoilers) deckDao.searchAllRewards(ftsQuery)
                else deckDao.searchRewards(ftsQuery, deckInfo.rewards)
                1 -> deckDao.searchAllMaladies(ftsQuery)
                2 -> deckDao.searchAllCards(ftsQuery)
                else -> deckDao.searchExtraCards(ftsQuery, deckInfo.extraSlots)
            }
        }
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { pagingSourceFactory }
        ).flow
    }

    private fun createQueryString(searchQuery: String, includeEnglish: Boolean, language: String): String {
        return if (!includeEnglish || language == "en") "composite:($searchQuery)"
        else "real_composite:($searchQuery)"
    }
}