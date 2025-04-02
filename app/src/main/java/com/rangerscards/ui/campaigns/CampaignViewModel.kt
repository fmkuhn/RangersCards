package com.rangerscards.ui.campaigns

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.AddCampaignEventMutation
import com.rangerscards.AddCampaignRemovedMutation
import com.rangerscards.AddFriendToCampaignMutation
import com.rangerscards.CampaignSubscription
import com.rangerscards.CampaignTravelMutation
import com.rangerscards.CampaignUndoTravelMutation
import com.rangerscards.CreateCampaignMutation
import com.rangerscards.DeleteCampaignMutation
import com.rangerscards.ExtendCampaignMutation
import com.rangerscards.GetCampaignQuery
import com.rangerscards.GetDeckQuery
import com.rangerscards.LeaveCampaignMutation
import com.rangerscards.RemoveDeckCampaignMutation
import com.rangerscards.RemoveFriendFromCampaignMutation
import com.rangerscards.SetCampaignCalendarMutation
import com.rangerscards.SetCampaignDayMutation
import com.rangerscards.SetCampaignTitleMutation
import com.rangerscards.SetDeckCampaignMutation
import com.rangerscards.UpdateCampaignEventsMutation
import com.rangerscards.UpdateCampaignRemovedMutation
import com.rangerscards.UpdateCampaignRewardsMutation
import com.rangerscards.UpdateUploadedMutation
import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.deck.RoleCardProjection
import com.rangerscards.data.database.repository.CampaignRepository
import com.rangerscards.data.database.repository.DeckRepository
import com.rangerscards.data.objects.CampaignMaps
import com.rangerscards.data.objects.Weather
import com.rangerscards.ui.decks.getCurrentDateTime
import com.rangerscards.ui.decks.toDeck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class CampaignState(
    val id: String,
    val uploaded: Boolean,
    val userId: String,
    val name: String,
    val currentDay: Int,
    val extendedCalendar: Boolean,
    val cycleId: String,
    val currentLocation: String,
    val currentPathTerrain: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val missions: List<CampaignMission>,
    val events: List<CampaignEvent>,
    val rewards: List<String>,
    val removed: List<CampaignRemoved>,
    val history: List<CampaignHistory>,
    val calendar: Map<Int, List<String>>,
    val decks: List<CampaignDeck>,
    val access: Map<String, String>
)

data class CampaignMission(
    val day: Int,
    val name: String,
    val checks: List<Boolean>,
    val completed: Boolean,
)

data class CampaignEvent(
    val name: String,
    val crossedOut: Boolean,
)

data class CampaignRemoved(
    val name: String,
    val setId: String,
)

data class CampaignHistory(
    val day: Int,
    val camped: Boolean,
    val location: String,
    val pathTerrain: String,
)

data class CampaignTravelDay(
    val day: Int,
    val startingLocation: String?,
    val travel: List<CampaignHistory>
)

data class CampaignDeck(
    val id: String,
    val name: String,
    val role: String,
    val meta: JsonElement,
    val userId: String,
    val userName: String,
)

data class DayInfo(
    val guides: List<String>,
    @DrawableRes val moonIconId: Int
)

class CampaignViewModel(
    private val apolloClient: ApolloClient,
    private val campaignRepository: CampaignRepository,
    private val deckRepository: DeckRepository,
) : ViewModel() {

    fun getCampaignById(id: String) = campaignRepository.getCampaignFlowById(id)

    var isSubscriptionStarted = MutableStateFlow(false)
        private set

    private val _campaign = MutableStateFlow<CampaignState?>(null)
    val campaign: StateFlow<CampaignState?> = _campaign.asStateFlow()

    val uploadedCampaignIdToOpen = MutableStateFlow<String?>(null)

    fun startSubscription(campaignId: String) {
        viewModelScope.launch {
            try {
                // Convert the Apollo subscription call to a Flow
                apolloClient.subscription(CampaignSubscription(campaignId.toInt()))
                    .toFlow()
                    .collect { response ->
                        // Check for errors or handle the data
                        if (response.hasErrors()) {
                            // Handle error case
                            Log.d("SubscriptionErrors", response.errors.toString())
                        } else {
                            val data = response.data
                            if (data != null) {
                                isSubscriptionStarted.update { true }
                                campaignRepository.updateCampaign(data.campaign!!.campaign.toCampaign(true))
                            }
                        }
                    }
            } catch (e: Exception) {
                // Handle cancellation or other exceptions
                // For instance, log the error or inform the user
                isSubscriptionStarted.update { false }
                Log.e("SubscriptionError", e.message.toString())
            }
        }
    }

    fun parseCampaign(campaign: Campaign) {
        _campaign.update {
            campaign.toCampaignState()
        }
    }

    suspend fun updateCampaignName(
        campaignId: String,
        newName: String,
        uploaded: Boolean,
        user: FirebaseUser?
    ) {
        if (uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                SetCampaignTitleMutation(
                name = newName,
                campaignId = campaignId.toInt(),
            )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaign = campaignRepository.getCampaignById(campaignId)
            campaignRepository.updateCampaign(campaign.copy(name = newName,
                updatedAt = getCurrentDateTime()))
        }
    }

    // This function creates an extended weather list when needed.
    // It returns the original list for a normal 30-day calendar, or a list of 60 days weather entries for extended mode.
    private fun getExtendedWeatherList(): List<Weather> {
        val weathers = CampaignMaps.weather[campaign.value!!.cycleId]!!
        return if (campaign.value!!.extendedCalendar) {
            // For extended calendars, create a second block with start and end values increased by 30.
            weathers + weathers.map { original ->
                original.copy(start = original.start + 30, end = original.end + 30)
            }
        } else {
            weathers
        }
    }

    // This function groups days by the corresponding Weather.
    // For extendedCalendar, days 31-60 mirror days 1-30.
    fun groupDaysByWeather(): Map<Weather, Map<Int, DayInfo>> {
        val campaign = campaign.value!!
        val weathers = getExtendedWeatherList()
        val guidesMap = campaign.calendar.toMutableMap()
        val starterGuides = CampaignMaps.fixedGuideEntries[campaign.cycleId]!!
        for ((key, value) in starterGuides) {
            // Check if the key exists in the first map
            if (guidesMap.containsKey(key)) {
                // If yes, merge the lists (concatenate the values)
                // Using the plus operator to concatenate two lists
                guidesMap[key] = value + guidesMap[key]!!
            } else {
                // If the key does not exist, add it to the first map
                guidesMap[key] = value
            }
        }
        val iconsId = CampaignMaps.moonIconsMap
        // Determine the maximum day based on calendar mode
        val maxDay = if (campaign.extendedCalendar) 60 else 30
        val result = mutableMapOf<Weather, MutableList<Int>>()
        val dayInfoMap = mutableMapOf<Int, DayInfo>()
        // Iterate over the days in the defined range.
        for (day in 1..maxDay) {
            val weatherForDay = weathers.firstOrNull { day in it.start..it.end }
            if (weatherForDay != null) {
                result.getOrPut(weatherForDay) { mutableListOf() }.add(day)
            }
            dayInfoMap[day] = DayInfo(
                guidesMap[day] ?: emptyList(),
                if (day > 30) iconsId[day - 30]!! else iconsId[day]!!
            )
        }
        return result.mapValues { (_, days) ->
            days.associateWith { day -> dayInfoMap[day]!! }
        }
    }

    suspend fun setCampaignCalendar(day: Int, guides: List<String>, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val map: MutableMap<Int, List<String>> = campaign.calendar.toMutableMap()
        if (map.containsKey(day)) {
            if (guides.isEmpty()) map.remove(day)
            else map[day] = guides
        } else {
            if (guides.isNotEmpty()) map[day] = guides
        }
        val newCalendar = buildJsonArray { map.forEach { add(buildJsonObject {
            put("day", it.key)
            put("guides", buildJsonArray { it.value.forEach { guide -> add(guide) } })
        }) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                SetCampaignCalendarMutation(
                    campaignId = campaign.id.toInt(),
                    calendar = newCalendar,
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(calendar = newCalendar,
                updatedAt = getCurrentDateTime()))
        }
    }

    /**
     * Computes a list of TravelDay objects based on the campaign history.
     */
    fun buildTravelHistory(history: List<CampaignHistory>): List<CampaignTravelDay> {
        val campaign = campaign.value!!
        // Group entries by day
        val daysMap = history.groupBy { it.day }
        val result = mutableListOf<CampaignTravelDay>()
        // Determine the starting live location using a constant lookup.
        var liveLocation: String? = CampaignMaps.startingLocations[campaign.cycleId]
        // For each day from 1 to campaign.day, build the travel day object.
        for (day in 1..campaign.currentDay) {
            val travel = daysMap[day] ?: emptyList()
            result.add(
                CampaignTravelDay(
                    day = day,
                    startingLocation = liveLocation,
                    travel = travel
                )
            )
            if (travel.isNotEmpty()) {
                // Update liveLocation to the location from the last entry
                liveLocation = travel.lastOrNull()?.location ?: liveLocation
            }
        }
        return result
    }

    fun getWeatherResId(day: Int): Int {
        val campaign = campaign.value!!
        val weatherList = CampaignMaps.weather[campaign.cycleId]!!
        return (weatherList.firstOrNull { day in it.start..it.end }
            ?: weatherList.firstOrNull { day in (it.start + 30)..(it.end + 30) })?.nameResId!!
    }

    suspend fun extendCampaign(user: FirebaseUser?) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                ExtendCampaignMutation(campaignId = campaign.id.toInt())
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(
                extendedCalendar = true,
                updatedAt = getCurrentDateTime()))
        }
    }

    suspend fun setCampaignDay(user: FirebaseUser?) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                SetCampaignDayMutation(
                    campaignId = campaign.id.toInt(),
                    day = campaign.currentDay + 1
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(
                day = campaign.currentDay + 1,
                updatedAt = getCurrentDateTime()))
        }
    }

    suspend fun campaignTravel(
        selectedLocation: String,
        selectedPathTerrain: String,
        isCamping: Boolean,
        user: FirebaseUser?
    ) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                CampaignTravelMutation(
                    campaignId = campaign.id.toInt(),
                    day = campaign.currentDay + if (isCamping) 1 else 0,
                    currentLocation = selectedLocation,
                    currentPathTerrain = selectedPathTerrain,
                    history = buildJsonObject {
                        put("day", campaign.currentDay)
                        put("camped", isCamping)
                        put("location", selectedLocation)
                        put("path_terrain", selectedPathTerrain)
                    }
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val newHistory = campaign.history + CampaignHistory(
                campaign.currentDay,
                isCamping,
                selectedLocation,
                selectedPathTerrain
            )
            val newHistoryJson = buildJsonArray { newHistory.forEach { add(buildJsonObject {
                put("day", it.day)
                put("camped", it.camped)
                put("location", it.location)
                put("path_terrain", it.pathTerrain)
            }) } }
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(
                day = campaign.currentDay + if (isCamping) 1 else 0,
                currentLocation = selectedLocation,
                currentPathTerrain = selectedPathTerrain,
                history = newHistoryJson,
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    fun getRole(id: String): Flow<RoleCardProjection> = campaignRepository.getRole(id)

    suspend fun removeDeckCampaign(deckId: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                RemoveDeckCampaignMutation(
                    deckId = deckId.toInt(),
                    campaignId = campaign.id.toInt(),
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
            val response = apolloClient.query(GetDeckQuery(deckId.toInt()))
                .addHttpHeader("Authorization", "Bearer $token")
                .fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data != null) deckRepository.updateDeck(response.data!!.deck!!.deck.toDeck(true))
        } else {
            val deck = deckRepository.getDeck(deckId)
            deckRepository.updateDeck(deck.copy(
                campaignId = null,
                campaignName = null,
                campaignRewards = null
            ))
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(
                latestDecks = JsonObject(campaignEntry.latestDecks.jsonObject.filterKeys { it != deckId }),
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun addDeckCampaign(deckId: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                SetDeckCampaignMutation(
                    deckId = deckId.toInt(),
                    campaignId = campaign.id.toInt(),
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
            val response = apolloClient.query(GetDeckQuery(deckId.toInt()))
                .addHttpHeader("Authorization", "Bearer $token")
                .fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data != null) deckRepository.updateDeck(response.data!!.deck!!.deck.toDeck(true))
        } else {
            val deck = deckRepository.getDeck(deckId)
            deckRepository.updateDeck(deck.copy(
                campaignId = campaign.id,
                campaignName = campaign.name,
                campaignRewards = buildJsonArray { campaign.rewards.forEach { add(it) } }
            ))
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            val newDeckJson = buildJsonArray {
                add(deck.name)
                add(deck.meta)
                add(buildJsonObject {
                    put(deck.userId, deck.userHandle)
                })
            }
            campaignRepository.updateCampaign(campaignEntry.copy(
                latestDecks = JsonObject(campaignEntry.latestDecks.jsonObject + (deckId to newDeckJson)),
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun addFriendToCampaign(user: FirebaseUser?, friendId: String) {
        val campaign = campaign.value!!
        val token = user!!.getIdToken(true).await().token
        apolloClient.mutation(
            AddFriendToCampaignMutation(
                campaignId = campaign.id.toInt(),
                userId = friendId
            )
        ).addHttpHeader("Authorization", "Bearer $token").execute()
    }

    suspend fun removeFriendFromCampaign(user: FirebaseUser?, friendId: String) {
        val campaign = campaign.value!!
        val token = user!!.getIdToken(true).await().token
        apolloClient.mutation(
            RemoveFriendFromCampaignMutation(
                campaignId = campaign.id.toInt(),
                userId = friendId
            )
        ).addHttpHeader("Authorization", "Bearer $token").execute()
    }

    suspend fun uploadCampaign(user: FirebaseUser?) {
        val campaign = campaign.value!!
        val token = user!!.getIdToken(true).await().token
        val uploadedCampaign = apolloClient.mutation(
            CreateCampaignMutation(
                name = campaign.name,
                cycleId = campaign.cycleId,
                currentLocation = campaign.currentLocation,
            )
        ).addHttpHeader("Authorization", "Bearer $token").execute()
        if (uploadedCampaign.data != null) {
            val newCampaignId = uploadedCampaign.data!!.campaign!!.campaign.id
            apolloClient.mutation(
                UpdateUploadedMutation(
                    campaignId = newCampaignId,
                    currentPathTerrain = Optional.present(campaign.currentPathTerrain),
                    day = campaign.currentDay,
                    extendedCalendar = Optional.present(campaign.extendedCalendar),
                    rewards = buildJsonArray { campaign.rewards.forEach { add(it) } },
                    missions = buildJsonArray { campaign.missions.forEach { add(buildJsonObject {
                        put("day", it.day)
                        put("name", it.name)
                        put("checks", buildJsonArray { it.checks.forEach { check -> add(check) } })
                        put("completed", it.completed)
                    }) } },
                    events = buildJsonArray { campaign.events.forEach { add(buildJsonObject {
                        put("event", it.name)
                        put("crossed_out", it.crossedOut)
                    }) } },
                    removed = buildJsonArray { campaign.removed.forEach { add(buildJsonObject {
                        put("name", it.name)
                        put("set_id", it.setId)
                    }) } },
                    history = buildJsonArray { campaign.history.forEach { add(buildJsonObject {
                        put("day", it.day)
                        put("camped", it.camped)
                        put("location", it.location)
                        put("path_terrain", it.pathTerrain)
                    }) } },
                    calendar = buildJsonArray { campaign.calendar.forEach { add(buildJsonObject {
                        put("day", it.key)
                        put("guides", buildJsonArray { it.value.forEach { guide -> add(guide) } })
                    }) } }
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
            val newCampaign = apolloClient.query(GetCampaignQuery(newCampaignId)).fetchPolicy(FetchPolicy.NetworkOnly)
                .addHttpHeader("Authorization", "Bearer $token").execute()
            if (newCampaign.data != null) {
                val uploadedData = newCampaign.data!!.campaign!!.campaign
                campaignRepository.insertCampaign(uploadedData.toCampaign(true))
                uploadedCampaignIdToOpen.update { uploadedData.id.toString() }
                campaignRepository.deleteCampaign(campaign.id)
            }
        }
    }

    suspend fun deleteCampaign(user: FirebaseUser?) {
        val campaign = campaign.value!!
        val deckIds = campaign.decks.map { it.id }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                DeleteCampaignMutation(
                    campaignId = campaign.id.toInt(),
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
            deckIds.forEach {
                val response = apolloClient.query(GetDeckQuery(it.toInt()))
                    .addHttpHeader("Authorization", "Bearer $token")
                    .fetchPolicy(FetchPolicy.NetworkOnly).execute()
                if (response.data != null) deckRepository.updateDeck(response.data!!.deck!!.deck.toDeck(true))
            }
            campaignRepository.deleteCampaign(campaign.id)
        } else {
            deckIds.forEach {
                val deck = deckRepository.getDeck(it)
                deckRepository.updateDeck(deck.copy(
                    campaignId = null,
                    campaignName = null,
                    campaignRewards = null
                ))
            }
            campaignRepository.deleteCampaign(campaign.id)
        }
    }

    suspend fun leaveCampaign(user: FirebaseUser?) {
        val campaign = campaign.value!!
        val deckIds = campaign.decks.filter { user?.uid == it.userId }.map { it.id }
        val token = user!!.getIdToken(true).await().token
        apolloClient.mutation(
            LeaveCampaignMutation(
                campaignId = campaign.id.toInt(),
                userId = user.uid
            )
        ).addHttpHeader("Authorization", "Bearer $token").execute()
        deckIds.forEach {
            removeDeckCampaign(it, user)
        }
        campaignRepository.deleteCampaign(campaign.id)
    }

    fun checkIfCanUndo(): Boolean {
        val campaign = campaign.value!!
        // Get the last travel record (if any)
        val lastTravel = campaign.history.lastOrNull()
        // Compute whether we can undo an "end day"
        val canUndoEndDay = campaign.currentDay > 1 && (
                lastTravel == null ||
                        (if (lastTravel.camped) lastTravel.day + 1 else lastTravel.day) < campaign.currentDay
                )
        return lastTravel != null || canUndoEndDay
    }

    suspend fun undoTravel(user: FirebaseUser?) {
        val campaign = campaign.value!!
        // Get the last travel record (if any)
        val lastTravel = campaign.history.lastOrNull()
        // Compute whether we can undo an "end day"
        val canUndoEndDay = campaign.currentDay > 1 && (
                lastTravel == null ||
                        (if (lastTravel.camped) lastTravel.day + 1 else lastTravel.day) < campaign.currentDay
                )
        if (canUndoEndDay) {
            if (campaign.uploaded) {
                val token = user!!.getIdToken(true).await().token
                apolloClient.mutation(
                    SetCampaignDayMutation(
                        campaignId = campaign.id.toInt(),
                        day = campaign.currentDay - 1
                    )
                ).addHttpHeader("Authorization", "Bearer $token").execute()
            } else {
                val campaignEntry = campaignRepository.getCampaignById(campaign.id)
                campaignRepository.updateCampaign(campaignEntry.copy(
                    day = campaign.currentDay - 1,
                    updatedAt = getCurrentDateTime()
                ))
            }
        } else if (lastTravel != null) {
            var previousLocation = CampaignMaps.startingLocations[campaign.cycleId]!!
            var previousPathTerrain: String? = null
            if (campaign.history.size >= 2) {
                val penultimateEntry = campaign.history[campaign.history.size - 2]
                if (penultimateEntry.location.isNotEmpty()) {
                    previousLocation = penultimateEntry.location
                }
                previousPathTerrain = penultimateEntry.pathTerrain
            }
            // Remove the last entry from history.
            val newHistory = campaign.history.dropLast(1)
            val newHistoryJson = buildJsonArray { newHistory.forEach { add(buildJsonObject {
                put("day", it.day)
                put("camped", it.camped)
                put("location", it.location)
                put("path_terrain", it.pathTerrain)
            }) } }
            // Adjust previous day depending on whether the last travel had 'camped'
            val previousDay = campaign.currentDay - if (lastTravel.camped) 1 else 0
            if (campaign.uploaded) {
                val token = user!!.getIdToken(true).await().token
                apolloClient.mutation(
                    CampaignUndoTravelMutation(
                        campaignId = campaign.id.toInt(),
                        history = newHistoryJson,
                        previousDay = previousDay,
                        previousLocation = previousLocation,
                        previousPathTerrain = Optional.present(previousPathTerrain)
                    )
                ).addHttpHeader("Authorization", "Bearer $token").execute()
            } else {
                val campaignEntry = campaignRepository.getCampaignById(campaign.id)
                campaignRepository.updateCampaign(campaignEntry.copy(
                    day = previousDay,
                    history = newHistoryJson,
                    currentLocation = previousLocation,
                    currentPathTerrain = previousPathTerrain,
                    updatedAt = getCurrentDateTime()
                ))
            }
        }
    }

    fun getRewardsCards(): Flow<List<CardListItemProjection>> = campaignRepository.getRewards()

    suspend fun addCampaignReward(id: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newList = campaign.rewards + id
        val newJsonRewards = buildJsonArray { newList.forEach { add(it) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                UpdateCampaignRewardsMutation(
                    campaignId = campaign.id.toInt(),
                    rewards = newJsonRewards
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(
                rewards = newJsonRewards,
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun removeCampaignReward(id: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newList = campaign.rewards.filterNot { it == id }
        val newJsonRewards = buildJsonArray { newList.forEach { add(it) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                UpdateCampaignRewardsMutation(
                    campaignId = campaign.id.toInt(),
                    rewards = newJsonRewards
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(
                rewards = newJsonRewards,
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    fun getRemovedSetsInfo(): Map<String, Pair<Int, Int>> {
        val campaign = campaign.value!!
        val maps = CampaignMaps.generalSetsMap + CampaignMaps.getMapLocations(false)
        val removedSets = mutableMapOf<String, Pair<Int, Int>>()
        campaign.removed.forEach { removed ->
            val fromPath = CampaignMaps.Path.fromValue(removed.setId)
            val fromMaps = maps[removed.setId]
            if (fromPath != null) removedSets[removed.setId] = fromPath.iconResId to fromPath.nameResId
            else removedSets[removed.setId] = fromMaps!!.iconResId to fromMaps.nameResId
        }
        return removedSets
    }

    suspend fun updateCampaignRemoved(name: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newList = campaign.removed.filterNot { it.name == name }
        val newJsonRemoved = buildJsonArray { newList.forEach { add(buildJsonObject {
            put("name", it.name)
            put("set_id", it.setId)
        }) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                UpdateCampaignRemovedMutation(
                    campaignId = campaign.id.toInt(),
                    removed = newJsonRemoved
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(
                removed = newJsonRemoved,
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    fun getAllRemovedSets(): Map<String, Pair<Int, Int>> {
        val maps = CampaignMaps.generalSetsMap + CampaignMaps.getMapLocations(false)
        val paths = CampaignMaps.Path.entries
        val sets = mutableMapOf<String, Pair<Int, Int>>()
        maps.forEach { (key, value) ->
            sets[key] = value.iconResId to value.nameResId
        }
        paths.forEach {
            sets[it.value] = it.iconResId to it.nameResId
        }
        return sets
    }

    suspend fun addCampaignRemoved(setId: String, name: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newJsonRemoved = buildJsonObject {
            put("name", name)
            put("set_id", setId)
        }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                AddCampaignRemovedMutation(
                    campaignId = campaign.id.toInt(),
                    removed = newJsonRemoved
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(
                removed = JsonArray(campaignEntry.removed.jsonArray + newJsonRemoved),
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun recordCampaignEvent(name: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newJsonEvent = buildJsonObject {
            put("event", name)
        }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                AddCampaignEventMutation(
                    campaignId = campaign.id.toInt(),
                    event = newJsonEvent
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(
                events = JsonArray(campaignEntry.removed.jsonArray + newJsonEvent),
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun updateCampaignEvent(oldName: String, newName: String, crossedOut: Boolean, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newEventsList = campaign.events.map { if (it.name == oldName) CampaignEvent(newName, crossedOut) else it }
        val newJsonList = buildJsonArray { newEventsList.forEach { add(buildJsonObject {
            put("event", it.name)
            put("crossed_out", it.crossedOut)
        }) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                UpdateCampaignEventsMutation(
                    campaignId = campaign.id.toInt(),
                    events = newJsonList
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignRepository.getCampaignById(campaign.id)
            campaignRepository.updateCampaign(campaignEntry.copy(
                events = newJsonList,
                updatedAt = getCurrentDateTime()
            ))
        }
    }
}

fun Campaign.toCampaignState(): CampaignState {
    return CampaignState(
        id = this.id,
        uploaded = this.uploaded,
        userId = this.userId,
        name = this.name,
        currentDay = this.day,
        extendedCalendar = this.extendedCalendar ?: false,
        cycleId = this.cycleId,
        currentLocation = this.currentLocation,
        currentPathTerrain = this.currentPathTerrain,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        missions = this.missions.jsonArray.map { element ->
            val value = element.jsonObject
            CampaignMission(
                value["day"]!!.jsonPrimitive.content.toInt(),
                value["name"]!!.jsonPrimitive.content,
                value["checks"]?.jsonArray?.map { it.jsonPrimitive.content.toBoolean() }
                    ?: listOf(false, false, false),
                value["completed"]?.jsonPrimitive?.content.toBoolean()
            )
        },
        events = this.events.jsonArray.map { element ->
            val value = element.jsonObject
            CampaignEvent(
                value["event"]!!.jsonPrimitive.content,
                value["crossed_out"]?.jsonPrimitive?.content.toBoolean()
            )
        },
        rewards = this.rewards.jsonArray.map { it.jsonPrimitive.content },
        removed = this.removed.jsonArray.map { element ->
            val value = element.jsonObject
            CampaignRemoved(
                value["name"]!!.jsonPrimitive.content,
                value["set_id"]!!.jsonPrimitive.content
            )
        },
        history = this.history.jsonArray.map { element ->
            val value = element.jsonObject
            CampaignHistory(
                value["day"]!!.jsonPrimitive.content.toInt(),
                value["camped"]!!.jsonPrimitive.content.toBoolean(),
                value["location"]!!.jsonPrimitive.content,
                value["path_terrain"]!!.jsonPrimitive.content
            )
        },
        calendar = this.calendar.jsonArray.associate { element ->
            val value = element.jsonObject
            value["day"]!!.jsonPrimitive.content.toInt() to value["guides"]!!.jsonArray.map { it.jsonPrimitive.content }
        },
        decks = this.latestDecks.jsonObject.map {
            val value = it.value.jsonArray
            val meta = value[1].jsonObject
            val user = value[2].jsonObject
            CampaignDeck(
                it.key,
                value[0].jsonPrimitive.content,
                meta["role"]!!.jsonPrimitive.content,
                meta,
                user.keys.first(),
                user.values.first().jsonPrimitive.content
            )
        },
        access = this.access.jsonObject.mapValues { it.value.jsonPrimitive.content }
    )
}