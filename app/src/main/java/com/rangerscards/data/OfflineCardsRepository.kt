package com.rangerscards.data

import kotlinx.coroutines.flow.Flow

class OfflineCardsRepository(private val cardDao: CardDao) : CardsRepository {

    override suspend fun updateAllCards(cards: List<Card>) = cardDao.updateAll(cards)

    override suspend fun insertAllCards(cards: List<Card>) = cardDao.insertAll(cards)

    override suspend fun isExists(): Boolean = cardDao.isExists()

    override fun getAllCardsStream(spoiler: Boolean): Flow<List<Card>> =
        cardDao.getAllCards(spoiler)
}