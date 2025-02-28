package com.rangerscards.data.database.repository

import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

interface DeckRepository {

    fun getDeck(id: String): Flow<Deck>

    fun getRole(id: String): Flow<RoleCardProjection>
}