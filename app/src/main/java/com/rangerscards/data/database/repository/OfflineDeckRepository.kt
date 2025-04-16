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

    override suspend fun getDeck(id: String): Deck? = deckDao.getDeckById(id)

    override suspend fun getRole(code: String, taboo: Boolean): RoleCardProjection? =
        deckDao.getRole(code, taboo)

    override suspend fun updateDeck(deck: Deck) = deckDao.updateDeck(deck)

    override suspend fun insertDeck(deck: Deck) = deckDao.insertDeck(deck)

    override suspend fun upsertDeck(deck: Deck) = deckDao.upsertDeck(deck)

    override suspend fun deleteDeckById(id: String) = deckDao.deleteDeckById(id)

    override suspend fun deleteDecksById(ids: List<String>) = deckDao.deleteDecksById(ids)

    override fun getCardsByIds(ids: List<String>, tabooId: String?): Flow<List<CardDeckListItemProjection>> =
        deckDao.getCardsByIds(ids, tabooId)

    override suspend fun getChangedCardsByIds(ids: List<String>, tabooId: String?): List<CardDeckListItemProjection> =
        deckDao.getChangedCardsByIds(ids, tabooId)

    override fun getAllCards(
        deckInfo: DeckInfo,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        packIds: List<String>
    ): Flow<PagingData<CardDeckListItemProjection>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        val pagingSourceFactory = if (!deckInfo.isUpgrade) {
            when(typeIndex) {
                0 -> deckDao.getPersonalityCards(deckInfo.taboo, packIds)
                1 -> deckDao.getBackgroundCards(deckInfo.background, deckInfo.taboo, packIds)
                2 -> deckDao.getSpecialtyCards(deckInfo.specialty, deckInfo.taboo, packIds)
                else -> deckDao.getOutsideInterestCards(deckInfo.background, deckInfo.specialty, deckInfo.taboo, packIds)
            }
        } else {
            when(typeIndex) {
                0 -> if (showAllSpoilers) deckDao.getAllRewards(deckInfo.taboo, packIds)
                else deckDao.getRewards(deckInfo.rewards, deckInfo.taboo)
                1 -> deckDao.getAllMaladies(deckInfo.taboo, packIds)
                2 -> deckDao.getAllCards(deckInfo.taboo, packIds)
                else -> deckDao.getExtraCards(deckInfo.extraSlots, deckInfo.taboo)
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
        language: String,
        packIds: List<String>
    ): Flow<PagingData<CardDeckListItemProjection>> {
        // Build the FTS query string
        val ftsQuery = if (language == "ru") {
            val stemedString = searchQuery
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alnum}]+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(separator = " ", transform = { "${PorterStem.stem(it)}*" })
            createQueryString(stemedString, includeEnglish, language)
        } else {
            val stemedString = searchQuery
                .lowercase(Locale.forLanguageTag(language))
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alnum}]+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(separator = " ", transform = { "$it*" })
            createQueryString(stemedString, includeEnglish, language)
        }

        // Create a Pager that wraps the PagingSource from the DAO.
        val pagingSourceFactory = if (!deckInfo.isUpgrade) {
            when(typeIndex) {
                0 -> deckDao.searchPersonalityCards(ftsQuery, deckInfo.taboo, packIds)
                1 -> deckDao.searchBackgroundCards(ftsQuery, deckInfo.background, deckInfo.taboo, packIds)
                2 -> deckDao.searchSpecialtyCards(ftsQuery, deckInfo.specialty, deckInfo.taboo, packIds)
                else -> deckDao.searchOutsideInterestCards(ftsQuery, deckInfo.background, deckInfo.specialty, deckInfo.taboo, packIds)
            }
        } else {
            when(typeIndex) {
                0 -> if (showAllSpoilers) deckDao.searchAllRewards(ftsQuery, deckInfo.taboo, packIds)
                else deckDao.searchRewards(ftsQuery, deckInfo.rewards, deckInfo.taboo)
                1 -> deckDao.searchAllMaladies(ftsQuery, deckInfo.taboo, packIds)
                2 -> deckDao.searchAllCards(ftsQuery, deckInfo.taboo, packIds)
                else -> deckDao.searchExtraCards(ftsQuery, deckInfo.extraSlots, deckInfo.taboo)
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