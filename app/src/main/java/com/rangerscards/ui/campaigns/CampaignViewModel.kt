package com.rangerscards.ui.campaigns

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.CampaignSubscription
import com.rangerscards.SetCampaignTitleMutation
import com.rangerscards.data.database.campaign.Campaign
import com.rangerscards.data.database.repository.CampaignRepository
import com.rangerscards.data.objects.CampaignMaps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
    val calendar: List<CampaignCalendar>,
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

data class CampaignCalendar(
    val day: Int,
    val guides: List<String>,
)

data class CampaignDeck(
    val id: String,
    val name: String,
    val role: Int,
    val background: String,
    val specialty: String,
    val userId: String,
    val userName: String,
)

class CampaignViewModel(
    private val apolloClient: ApolloClient,
    private val campaignRepository: CampaignRepository
) : ViewModel() {

    fun getCampaignById(id: String) = campaignRepository.getCampaignFlowById(id)

    var isSubscriptionStarted = MutableStateFlow(false)
        private set

    private val _campaign = MutableStateFlow<CampaignState?>(null)
    val campaign: StateFlow<CampaignState?> = _campaign.asStateFlow()

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
            campaignRepository.updateCampaign(campaign.copy(name = newName))
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
        calendar = (CampaignMaps.fixedGuideEntries[this.cycleId]?.map {
            CampaignCalendar(it.key, it.value)
        } ?: emptyList()) + this.calendar.jsonArray.map { element ->
            val value = element.jsonObject
            CampaignCalendar(
                value["day"]!!.jsonPrimitive.content.toInt(),
                value["guides"]!!.jsonArray.map { it.jsonPrimitive.content }
            )
        },
        decks = this.latestDecks.jsonObject.map {
            val value = it.value.jsonArray
            val meta = value[1].jsonObject
            val user = value[2].jsonObject
            CampaignDeck(
                it.key,
                value[0].jsonPrimitive.content,
                meta["role"]!!.jsonPrimitive.content.toInt(),
                meta["background"]!!.jsonPrimitive.content,
                meta["specialty"]!!.jsonPrimitive.content,
                user.keys.first(),
                user.values.first().jsonPrimitive.content
            )
        },
        access = this.access.jsonObject.mapValues { it.value.jsonPrimitive.content }
    )
}