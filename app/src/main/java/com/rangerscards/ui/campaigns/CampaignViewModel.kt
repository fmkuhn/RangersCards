package com.rangerscards.ui.campaigns

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.CampaignSubscription
import com.rangerscards.SetCampaignTitleMutation
import com.rangerscards.data.database.repository.CampaignRepository
import com.rangerscards.ui.deck.toDeck
import com.rangerscards.ui.decks.toDeck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CampaignViewModel(
    private val apolloClient: ApolloClient,
    private val campaignRepository: CampaignRepository
) : ViewModel() {

    fun getCampaignById(id: String) = campaignRepository.getCampaignFlowById(id)

    var isSubscriptionStarted = MutableStateFlow(false)
        private set

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

    suspend fun updateCampaignName(
        campaignId: String,
        newName: String,
        uploaded: Boolean,
        user: FirebaseUser?
    ) {
        if (uploaded) {
            val token = user!!.getIdToken(true).await().token
            val newDeck = apolloClient.mutation(
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