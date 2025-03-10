package com.rangerscards.ui.deck

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.R
import com.rangerscards.SaveDeckMutation
import com.rangerscards.data.database.card.CardDeckListItemProjection
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.RoleCardProjection
import com.rangerscards.data.database.repository.DeckRepository
import com.rangerscards.ui.decks.getCurrentDateTime
import com.rangerscards.ui.decks.toDeck
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class FullDeckState(
    val id: String,
    val uploaded: Boolean,
    val userId: String,
    val userHandle: String?,
    val version: Int,
    val name: String,
    val description: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val roleId: String,
    val background: String,
    val specialty: String,
    val problems: List<String>?,
    val campaignId: Int?,
    val campaignName: String?,
    val campaignRewards: List<String>?,
    val previousId: String?,
    val previousSlots: Map<String, Int>?,
    val previousSideSlots: Map<String, Int>?,
    val nextId: String?,
    val addedCards: Map<String, Int> = persistentMapOf(),
    val removedCards: Map<String, Int> = persistentMapOf(),
    val addedCollectionCards: Map<String, Int> = persistentMapOf(),
    val returnedCollectionCards: Map<String, Int> = persistentMapOf(),
)

data class OftenUpdatableDeckValues(
    val slots: PersistentMap<String, Int>,
    val sideSlots: PersistentMap<String, Int>,
    val extraSlots: PersistentMap<String, Int>,
    val awa: Int,
    val spi: Int,
    val fit: Int,
    val foc: Int,
)

class DeckViewModel(
    private val apolloClient: ApolloClient,
    private val deckRepository: DeckRepository,
) : ViewModel() {

    // Holds the original deck loaded from the database.
    private val _originalDeck = MutableStateFlow<FullDeckState?>(null)
    val originalDeck: StateFlow<FullDeckState?> = _originalDeck.asStateFlow()

    // Holds the deck being edited.
    var editableDeck = MutableStateFlow<FullDeckState?>(null)
        private set

    private val _updatableValues = MutableStateFlow<OftenUpdatableDeckValues?>(null)
    val updatableValues: StateFlow<OftenUpdatableDeckValues?> = _updatableValues.asStateFlow()

    // Backup of the often-updating values when editing begins.
    private var backupOftenValues: OftenUpdatableDeckValues? = null

    // Flag to indicate if we're in edit mode.
    var isEditing  = MutableStateFlow(false)
        private set

    // Deck Role
    private val _role = MutableStateFlow<RoleCardProjection?>(null)
    val role: StateFlow<RoleCardProjection?> = _role.asStateFlow()

    // Deck Changed cards
    private val _changedCards = MutableStateFlow<Map<Int, List<CardDeckListItemProjection>>?>(null)
    val changedCards: StateFlow<Map<Int, List<CardDeckListItemProjection>>?> = _changedCards.asStateFlow()

    // Create flows that fetch the corresponding cards from Room.
    @OptIn(ExperimentalCoroutinesApi::class)
    val slotsCardsFlow: Flow<List<CardDeckListItemProjection>> = _updatableValues
        .filterNotNull().map { it.slots.keys.toList() }.distinctUntilChanged().flatMapLatest {
            deckRepository.getCardsByIds(it)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val extraSlotsCardsFlow: Flow<List<CardDeckListItemProjection>> = _updatableValues
        .filterNotNull().map { it.extraSlots.keys.toList() }.distinctUntilChanged().flatMapLatest {
            deckRepository.getCardsByIds(it)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val deckProblemsFlow: Flow<Pair<List<String>, Pair<Int, Int?>>> = slotsCardsFlow
        .flatMapLatest {
            parseDeckForErrors(it)
        }

    fun loadDeck(id: String) {
        viewModelScope.launch {
            val deck = deckRepository.getDeck(id)
            _updatableValues.update {
                OftenUpdatableDeckValues(
                    slots = deck.slots.toPersistentMap(),
                    sideSlots = deck.sideSlots.toPersistentMap(),
                    extraSlots = deck.extraSlots.toPersistentMap(),
                    awa = deck.awa,
                    spi = deck.spi,
                    fit = deck.fit,
                    foc = deck.foc,
                )
            }
            _originalDeck.update { deck.toDeckState() }
            if (deck.previousId != null) {
                val previousDeck = deckRepository.getDeck(deck.previousId)
                computeDeckChanges(
                    slots = deck.slots.toPersistentMap(),
                    sideSlots = deck.sideSlots.toPersistentMap(),
                    previousSlots = previousDeck.slots.toPersistentMap(),
                    previousSideSlots = previousDeck.sideSlots.toPersistentMap(),
                )
                if (_originalDeck.value!!.addedCards.isNotEmpty() ||
                    _originalDeck.value!!.removedCards.isNotEmpty() ||
                    _originalDeck.value!!.addedCollectionCards.isNotEmpty() ||
                    _originalDeck.value!!.returnedCollectionCards.isNotEmpty()) {
                    val added = deckRepository
                        .getChangedCardsByIds(_originalDeck.value!!.addedCards.keys.toList())
                    val removed = deckRepository
                        .getChangedCardsByIds(_originalDeck.value!!.removedCards.keys.toList())
                    val addedCollection = deckRepository
                        .getChangedCardsByIds(_originalDeck.value!!.addedCollectionCards.keys.toList())
                    val returnedCollection = deckRepository
                        .getChangedCardsByIds(_originalDeck.value!!.returnedCollectionCards.keys.toList())
                    _changedCards.update {
                        mapOf(
                            R.string.deck_changes_added to added,
                            R.string.deck_changes_removed to removed,
                            R.string.deck_changes_added_collection to addedCollection,
                            R.string.deck_changes_returned_collection to returnedCollection,
                        )
                    }
                }
            }
        }
    }

    private fun computeDeckChanges(
        slots: PersistentMap<String, Int>,
        sideSlots: PersistentMap<String, Int>,
        previousSlots: PersistentMap<String, Int>,
        previousSideSlots: PersistentMap<String, Int>
    ) {
        // Prepare mutable maps to track changes.
        val addedCards = mutableMapOf<String, Int>()
        val removedCards = mutableMapOf<String, Int>()
        val addedCollectionCards = mutableMapOf<String, Int>()
        val returnedCollectionCards = mutableMapOf<String, Int>()

        // Get the union of all keys from the current and previous decks.
        val allCodes = slots.keys + sideSlots.keys + previousSlots.keys + previousSideSlots.keys

        // Process each card code.
        for (code in allCodes) {
            // Get the current and previous counts, defaulting to 0 if missing.
            val currentSlot = slots[code] ?: 0
            val currentSide = sideSlots[code] ?: 0
            val prevSlot = previousSlots[code] ?: 0
            val prevSide = previousSideSlots[code] ?: 0

            // If there is no change in both the main and side slots, skip.
            if (currentSlot == prevSlot && currentSide == prevSide) {
                continue
            }

            // Check if the overall count remains the same.
            if ((currentSlot + currentSide) == (prevSlot + prevSide)) {
                // Normal swaps: only the distribution between main and side has changed.
                val difference = currentSlot - prevSlot
                if (difference > 0) {
                    addedCards[code] = difference
                } else {
                    removedCards[code] = difference
                }
            } else {
                // Collection swaps: the total number of cards has changed.
                val difference = (currentSlot + currentSide) - (prevSlot + prevSide)
                if (difference > 0) {
                    addedCollectionCards[code] = difference
                } else {
                    returnedCollectionCards[code] = difference
                }
            }
        }
        _originalDeck.update {
            it!!.copy(
                addedCards = addedCards.toMap(),
                removedCards = removedCards.toMap(),
                addedCollectionCards = addedCollectionCards.toMap(),
                returnedCollectionCards = returnedCollectionCards.toMap()
            )
        }
    }

    private fun parseDeckForErrors(
        cards: List<CardDeckListItemProjection>
    ): Flow<Pair<List<String>, Pair<Int, Int?>>> {
        val isUpgrade = originalDeck.value?.previousId != null
        val problems = mutableListOf<String>()
        // Build stats mapping
        val stats = mapOf(
            "AWA" to updatableValues.value!!.awa,
            "FIT" to updatableValues.value!!.fit,
            "FOC" to updatableValues.value!!.foc,
            "SPI" to updatableValues.value!!.spi
        )
        var splashCount = 0
        var splashResId: Int? = 0
        val deckSize = cards.associateWith { updatableValues.value!!.slots[it.id] }
            .entries.sumOf { it.value ?: 0 }
        cards.forEach { card ->
            val cardCount = updatableValues.value!!.slots[card.id] ?: 0
            if (cardCount > 2) {
                if (card.setId != "malady") {
                    problems.add("too_many_duplicates")
                }
            }else if (!isUpgrade && cardCount != 2) {
                problems.add("need_two_cards")
            }
            if (card.aspectId != null && card.level != null) {
                if ((stats[card.aspectId] ?: 0) < card.level) {
                    problems.add("invalid_aspect_levels")
                }
            }
        }
        if (isUpgrade) {
            if (deckSize < 30) {
                problems.add("too_few_cards")
            } else if (deckSize > 30) {
                problems.add("too_many_cards")
            }
        } else {
            // Additional rules for starting decks:
            var backgroundNonExpert = 0
            var backgroundCount = 0
            var specialtyNonExpert = 0
            var specialtyCount = 0
            val personalityCount = mutableMapOf(
                "AWA" to 0,
                "FIT" to 0,
                "FOC" to 0,
                "SPI" to 0
            )
            cards.forEach { card ->
                val cardCount = updatableValues.value!!.slots[card.id] ?: 0
                when(card.setId) {
                    "personality" -> {
                        if (card.aspectId != null) {
                            when (card.aspectId) {
                                "AWA" -> {
                                    personalityCount["AWA"] = personalityCount.getValue("AWA") + 2
                                    if (personalityCount.getValue("AWA") > 2) problems.add("too_many_awa_personality")
                                }
                                "FOC" -> {
                                    personalityCount["FOC"] = personalityCount.getValue("FOC") + 2
                                    if (personalityCount.getValue("FOC") > 2) problems.add("too_many_foc_personality")
                                }
                                "FIT" -> {
                                    personalityCount["FIT"] = personalityCount.getValue("FIT") + 2
                                    if (personalityCount.getValue("FIT") > 2) problems.add("too_many_fit_personality")
                                }
                                "SPI" -> {
                                    personalityCount["SPI"] = personalityCount.getValue("SPI") + 2
                                    if (personalityCount.getValue("SPI") > 2) problems.add("too_many_spi_personality")
                                }
                            }
                        }
                    }
                    else -> {
                        when (card.setTypeId) {
                            "background" -> {
                                if (card.setId == originalDeck.value!!.background) {
                                    backgroundCount += cardCount
                                    if (card.realTraits == null || !card.realTraits.contains("Expert")) {
                                        backgroundNonExpert += cardCount
                                    }
                                    if (backgroundCount > 10) {
                                        if (backgroundCount > 12 || splashCount >= 2) {
                                            problems.add("too_many_background")
                                        } else if (backgroundNonExpert < 2) {
                                            problems.add("invalid_outside_interest")
                                        } else {
                                            splashResId = R.string.background_as_outside_interest
                                            splashCount += cardCount
                                        }
                                    }
                                } else {
                                    if (card.realTraits != null && card.realTraits.contains("Expert")) {
                                        problems.add("invalid_outside_interest")
                                    } else {
                                        splashCount += cardCount
                                        if (splashCount > 2) {
                                            problems.add("too_many_outside_interest")
                                        }
                                    }
                                }
                            }
                            "specialty" -> {
                                if (card.setId == originalDeck.value!!.specialty) {
                                    specialtyCount += cardCount
                                    if (card.realTraits == null || !card.realTraits.contains("Expert")) {
                                        specialtyNonExpert += updatableValues.value!!.slots[card.id] ?: 0
                                    }
                                    if (specialtyCount > 10) {
                                        if (specialtyCount > 12 || splashCount >= 2) {
                                            problems.add("too_many_specialty")
                                        } else if (specialtyNonExpert < 2) {
                                            problems.add("invalid_outside_interest")
                                        } else {
                                            splashResId = R.string.specialty_as_outside_interest
                                            splashCount += cardCount
                                        }
                                    }
                                } else {
                                    if (card.realTraits != null && card.realTraits.contains("Expert")) {
                                        problems.add("invalid_outside_interest")
                                    } else {
                                        splashCount += cardCount
                                        if (splashCount > 2) {
                                            problems.add("too_many_outside_interest")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Validate personality counts: each aspect must equal exactly 2.
            if (personalityCount["AWA"] != 2 ||
                personalityCount["FIT"] != 2 ||
                personalityCount["FOC"] != 2 ||
                personalityCount["SPI"] != 2
            ) {
                problems.add("personality")
            }
            if (specialtyCount < 10) {
                problems.add("specialty")
            }
            if (backgroundCount < 10) {
                problems.add("background")
            }
            if (splashCount < 2) {
                problems.add("outside_interest")
            }
        }
        return flow {
            emit(problems to (if (splashCount == 2) splashCount to splashResId else 0 to null))
        }
    }

    fun enterEditMode() {
        originalDeck.value?.let { original ->
            editableDeck.update { original.copy() }
            backupOftenValues = updatableValues.value
            isEditing.update { true }
        }
    }

    fun addCard(id: String) {
        _updatableValues.update {
            if (originalDeck.value!!.previousId == null) it!!.copy(
                slots = it.slots.put(id, 2)
            ) else if (it!!.sideSlots.contains(id)) {
                if (it.sideSlots[id]!! > 1) it.copy(
                    slots = it.slots.put(id, (it.slots[id] ?: 0) + 1),
                    sideSlots = it.sideSlots.put(id, it.sideSlots[id]!! - 1)
                ) else it.copy(
                    slots = it.slots.put(id, (it.slots[id] ?: 0) + 1),
                    sideSlots = it.sideSlots.remove(id)
                )
            } else it.copy(
                slots = it.slots.put(id, (it.slots[id] ?: 0) + 1)
            )
        }
    }

    fun removeCard(id: String, setId: String?) {
        _updatableValues.update {
            if (originalDeck.value!!.previousId == null) it!!.copy(
                slots = it.slots.remove(id)
            ) else if (it!!.slots[id]!! > 1) {
                if (originalDeck.value!!.campaignRewards?.contains(id) == true ||
                    setId == "malady") it.copy(
                        slots = it.slots.put(id, it.slots[id]!! - 1)
                ) else it.copy(
                        slots = it.slots.put(id, it.slots[id]!! - 1),
                        sideSlots = it.sideSlots.put(id, (it.sideSlots[id] ?: 0) + 1)
                )
            } else {
                if (originalDeck.value!!.campaignRewards?.contains(id) == true ||
                    setId == "malady") it.copy(
                        slots = it.slots.remove(id),
                ) else it.copy(
                    slots = it.slots.remove(id),
                    sideSlots = it.sideSlots.put(id, (it.sideSlots[id] ?: 0) + 1)
                )
            }
        }
    }

    fun addExtraCard(id: String) {
        _updatableValues.update {
            it!!.copy(
                extraSlots = it.extraSlots.put(id, 1)
            )
        }
    }

    fun removeExtraCard(id: String) {
        _updatableValues.update {
            it!!.copy(
                extraSlots = it.extraSlots.remove(id)
            )
        }
    }

    fun checkChanges(): Boolean {
        return (originalDeck.value != editableDeck.value ||
                updatableValues.value != backupOftenValues) &&
                editableDeck.value != null && backupOftenValues != null
    }

    suspend fun saveChanges(user: FirebaseUser?, problems: List<String>?) {
        if (checkChanges()) {
            if (editableDeck.value!!.uploaded) {
                val values = updatableValues.value!!
                val token = user!!.getIdToken(true).await().token
                Log.d("test1", token.toString())
                val newDeck = apolloClient.mutation(SaveDeckMutation(
                    id = editableDeck.value!!.id.toInt(),
                    name = editableDeck.value!!.name,
                    foc = values.foc,
                    fit = values.fit,
                    awa = values.awa,
                    spi = values.spi,
                    meta = buildJsonObject {
                        put("role", editableDeck.value!!.roleId)
                        if (!problems.isNullOrEmpty()) put("problem", buildJsonArray {
                            problems.forEach { add(it) }
                        })
                        put("background", editableDeck.value!!.background)
                        put("specialty", editableDeck.value!!.specialty)
                    },
                    slots = buildJsonObject {
                        values.slots.forEach { (key, value) -> put(key, value) } },
                    sideSlots = buildJsonObject {
                        values.sideSlots.forEach { (key, value) -> put(key, value) } },
                    extraSlots = buildJsonObject {
                        values.extraSlots.forEach { (key, value) -> put(key, value) } },
                )).addHttpHeader("Authorization", "Bearer $token").execute()
                if (newDeck.data != null) {
                    Log.d("test", newDeck.data!!.update_rangers_deck_by_pk!!.deck.name)
                    deckRepository.updateDeck(
                        newDeck.data!!.update_rangers_deck_by_pk!!.deck.toDeck(true)
                    )
                }
            } else {
                deckRepository.updateDeck(editableDeck.value!!.toDeck(
                    updatableValues.value!!, problems
                ))
            }
        }
        backupOftenValues = null
        isEditing.update { false }
        editableDeck.update { null }
    }

    fun discardChanges() {
        editableDeck.update { null }
        _updatableValues.update {
            backupOftenValues
        }
        backupOftenValues = null
        isEditing.update { false }
    }

    fun getRole(id: String) {
        viewModelScope.launch {
            val role = deckRepository.getRole(id)
            _role.update { role }
        }
    }
}

fun JsonElement.toPersistentMap(): PersistentMap<String, Int> {
    return this.jsonObject.mapValues { (_, value) -> value.jsonPrimitive.int }.toPersistentMap()
}

fun Deck.toDeckState(): FullDeckState {
    return FullDeckState(
        id = this.id,
        uploaded = this.uploaded,
        userId = this.userId,
        userHandle = this.userHandle,
        version = this.version,
        name = this.name,
        description = this.description,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        roleId = this.meta.jsonObject["role"]!!.jsonPrimitive.content,
        background = this.meta.jsonObject["background"]!!.jsonPrimitive.content,
        specialty = this.meta.jsonObject["specialty"]!!.jsonPrimitive.content,
        problems = this.meta.jsonObject["problem"]?.jsonArray?.map { it.jsonPrimitive.content },
        campaignId = this.campaignId,
        campaignName = this.campaignName,
        campaignRewards = this.campaignRewards?.jsonArray?.map { it.jsonPrimitive.content },
        previousId = this.previousId,
        previousSlots = this.previousSlots?.toPersistentMap(),
        previousSideSlots = this.previousSideSlots?.toPersistentMap(),
        nextId = this.nextId
    )
}

fun FullDeckState.toDeck(values: OftenUpdatableDeckValues, problems: List<String>?): Deck {
    val deckState = this
    return Deck(
        id = this.id,
        uploaded = this.uploaded,
        userId = this.userId,
        userHandle = this.userHandle,
        slots = buildJsonObject { values.slots.forEach { (key, value) -> put(key, value) } },
        sideSlots = buildJsonObject { values.sideSlots.forEach { (key, value) -> put(key, value) } },
        extraSlots = buildJsonObject { values.extraSlots.forEach { (key, value) -> put(key, value) } },
        version = this.version,
        name = this.name,
        description = this.description,
        awa = values.awa,
        spi = values.spi,
        fit = values.fit,
        foc = values.foc,
        createdAt = this.createdAt,
        updatedAt = getCurrentDateTime(),
        meta = buildJsonObject {
            put("role", deckState.roleId)
            if (!problems.isNullOrEmpty()) put("problem", buildJsonArray {
                problems.forEach { add(it) }
            })
            put("background", deckState.background)
            put("specialty", deckState.specialty)
        },
        campaignId = this.campaignId,
        campaignName = this.campaignName,
        campaignRewards = buildJsonArray { deckState.campaignRewards?.forEach { add(it) } },
        previousId = this.previousId,
        previousSlots = buildJsonObject { deckState.previousSlots?.forEach { (key, value) -> put(key, value) } },
        previousSideSlots = buildJsonObject { deckState.previousSideSlots?.forEach { (key, value) -> put(key, value) } },
        nextId = this.nextId
    )
}