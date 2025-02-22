package com.rangerscards.data.database


interface DecksRepository {

    suspend fun deleteAllUploadedDecks()

    suspend fun syncDecks(networkDecks: List<Deck>)
}