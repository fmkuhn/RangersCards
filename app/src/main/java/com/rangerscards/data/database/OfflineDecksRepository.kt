package com.rangerscards.data.database


class OfflineDecksRepository(private val deckDao: DeckDao) : DecksRepository {
    override suspend fun deleteAllUploadedDecks() = deckDao.deleteAllUploadedDecks()

    override suspend fun syncDecks(networkDecks: List<Deck>) = deckDao.syncDecks(networkDecks)
}