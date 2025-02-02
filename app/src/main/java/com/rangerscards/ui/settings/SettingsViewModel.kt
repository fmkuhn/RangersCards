package com.rangerscards.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.apolloStore
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.exception.ApolloNetworkException
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.rangerscards.GetAllCardsQuery
import com.rangerscards.GetCardsUpdatedAtQuery
import com.rangerscards.GetProfileQuery
import com.rangerscards.GetUserInfoByHandleQuery
import com.rangerscards.MainActivity
import com.rangerscards.R
import com.rangerscards.UpdateHandleMutation
import com.rangerscards.data.Card
import com.rangerscards.data.CardsRepository
import com.rangerscards.data.UserAuthRepository
import com.rangerscards.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

/**
 * Data class to hold state for User's settings
 */
data class UserUIState(
    val currentUser: FirebaseUser? = Firebase.auth.currentUser,
    val userInfo: GetProfileQuery.Data? = null,
    val language: String = Locale.getDefault().language.substring(0..1),
)

/**
 * ViewModel to maintain user's settings.
 */
class SettingsViewModel(
    private val apolloClient: ApolloClient,
    private val userAuthRepository: UserAuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val cardsRepository: CardsRepository
) : ViewModel() {

    private val _userUiState = MutableStateFlow(UserUIState())
    var userUiState = _userUiState.asStateFlow()

    // theme state
    val themeState: StateFlow<Int?> =
        userPreferencesRepository.isDarkTheme.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _isCardsLoading = MutableStateFlow(false)
    var isCardsLoading = _isCardsLoading.asStateFlow()

    fun setUser(user: FirebaseUser?) {
        _userUiState.update {
            it.copy(currentUser = user)
        }
    }

    fun signIn(mainActivity: MainActivity, email: String, password: String) {
        userAuthRepository.signIn(mainActivity, email, password)
    }

    fun createAccount(mainActivity: MainActivity, email: String, password: String) {
        userAuthRepository.createAccount(mainActivity, email, password)
    }

    fun signOut(mainActivity: MainActivity) {
        userAuthRepository.signOut(mainActivity)
        _userUiState.update {
            it.copy(currentUser = null, userInfo = null)
        }
    }

    fun deleteUser(mainActivity: MainActivity, email: String, password: String) {
        val context = mainActivity.baseContext
        if (userAuthRepository.validateEmail(email)) {
            if (userAuthRepository.validatePassword(password)) {
                val user = _userUiState.value.currentUser
                user?.reauthenticate(EmailAuthProvider.getCredential(email, password))
                    ?.addOnCompleteListener {
                        if (it.isSuccessful) user.delete().addOnCompleteListener {
                            Log.d("AUTH", "User account deleted.")
                            Toast.makeText(
                                context,
                                context.getString(R.string.account_successfully_deleted_toast),
                                Toast.LENGTH_SHORT,
                            ).show()
                            _userUiState.update { userUiState ->
                                userUiState.copy(currentUser = null, userInfo = null)
                            }
                            apolloClient.apolloStore.clearAll()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.invalid_credentials_toast),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            } else {
                userAuthRepository.invalidPasswordToast(context)
            }
        } else {
            userAuthRepository.invalidEmailToast(context)
        }
    }

    private fun normalizeHandle(handle: String): String {
        return handle.replace("[\\.\\$\\[\\]#/]".toRegex(),"_")
            .lowercase(Locale.ENGLISH).trim()
    }

    private suspend fun getCurrentToken(refresh: Boolean?): String? {
        return userUiState.value.currentUser?.getIdToken(refresh ?: false)?.await()?.token
    }

    fun getUserInfo(id: String) {
        viewModelScope.launch {
            apolloClient.query(GetProfileQuery(id))
                .toFlow()
                .collect {
                    val response = it
                    when {
                        response.errors.orEmpty().isNotEmpty() -> {
                            // GraphQL error
                            Log.d("GraphQL error", response.errors!!.first().message)
                        }
                        response.exception is ApolloNetworkException -> {
                            // Network error
                            Log.d("Network error", "Please check your network connectivity.")
                        }
                        response.data != null -> {
                            // data (never partial)
                            _userUiState.update { uiState ->
                                uiState.copy(userInfo = response.data)
                            }
                        }
                        else -> {
                            // Another fetch error, maybe a cache miss?
                            // Or potentially a non-compliant server returning data: null without an error
                            Log.d("Another fetch error", "Oh no... An error happened.")
                        }
                    }
                }
        }
    }

    suspend fun updateHandle(mainActivity: MainActivity, handle: String) {
        if (handle == (userUiState.value.userInfo?.profile?.userProfile?.handle ?: "")) return
        var isTaken: Boolean
        viewModelScope.launch {
            val context = mainActivity.baseContext
            if (handle.length !in 3..22) {
                Toast.makeText(
                    context,
                    context.getString(R.string.invalid_handle_toast),
                    Toast.LENGTH_SHORT,
                ).show()
            }
            else {
                val result = apolloClient.query(GetUserInfoByHandleQuery(handle))
                    .fetchPolicy(FetchPolicy.NetworkOnly).execute()
                if (result.data?.profile?.isEmpty() == false) {
                    isTaken = true
                    Toast.makeText(
                        context,
                        context.getString(R.string.handle_already_taken_toast),
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    isTaken = false
                }
                if (!isTaken) {
                    val token = getCurrentToken(true)
                    val response = apolloClient.mutation(UpdateHandleMutation(
                        userUiState.value.currentUser!!.uid,
                        handle.trim(),
                        normalizeHandle(handle))
                    ).addHttpHeader("Authorization", "Bearer $token")
                        .execute()
                    when {
                        response.errors.orEmpty().isNotEmpty() -> {
                            // GraphQL error
                            Log.d("GraphQL error", response.errors!!.first().message)
                        }
                        response.exception is ApolloNetworkException -> {
                            // Network error
                            Log.d("Network error", "Please check your network connectivity.")
                        }
                        response.data != null -> {
                            // data (never partial)
                            getUserInfo(userUiState.value.currentUser!!.uid)
                        }
                        else -> {
                            // Another fetch error, maybe a cache miss?
                            // Or potentially a non-compliant server returning data: null without an error
                            Log.d("Another fetch error", "Oh no... An error happened.")
                        }
                    }
                }
            }
        }.join()
    }

    fun selectTheme(theme: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(theme)
        }
    }

    fun updateLocale(locale: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
        _userUiState.update { userUIState ->
            userUIState.copy(language = locale)
        }
        downloadCards()
    }

    private fun downloadCards() {
        _isCardsLoading.update { true }
        viewModelScope.launch {
            val language = _userUiState.value.language
            val response = apolloClient.query(GetAllCardsQuery(language))
                .fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data != null) {
                if (cardsRepository.isExists())
                    cardsRepository.updateAllCards(response.data!!.cards.toCards(language))
                else {
                    cardsRepository.insertAllCards(response.data!!.cards.toCards(language))
                }
                userPreferencesRepository.saveCardsUpdatedTimestamp(
                    response.data!!.all_updated_at[0].updated_at.toString()
                )
            }
        }.invokeOnCompletion {
            _isCardsLoading.update { false }
        }
    }

    fun updateCardsIfNotUpdated() {
        _isCardsLoading.update { true }
        viewModelScope.launch {
           val response = apolloClient.query(GetCardsUpdatedAtQuery(_userUiState.value.language))
               .fetchPolicy(FetchPolicy.NetworkOnly).execute()
           if (response.data != null) {
               userPreferencesRepository.getCarsUpdatedAt().collect {
                   if (userPreferencesRepository.compareTimestamps(
                           it,
                           response.data!!.card_updated_at[0].updated_at.toString()
                       )) {
                       downloadCards()
                   }
                   else {
                       _isCardsLoading.update { false }
                   }
               }
           }
        }
    }

    fun downloadCardsIfDatabaseNotExists() {
        viewModelScope.launch {
            val exists = cardsRepository.isExists()
            if (!exists) {
                downloadCards()
            }
        }
    }

    fun openLink(link: String, context: Context) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(link)
            )
        )
    }
}

/**
 * Extension function to convert [GetAllCardsQuery.Card] to [Card]
 */
fun GetAllCardsQuery.Card.toCard(locale: String): Card? {
    return this.card.id?.let {
        Card(
            id = it,
            name = this.card.name,
            realName = if (locale == "en") null else this.card.real_name,
            realTraits = this.card.real_traits,
            traits = this.card.traits,
            equip = this.card.equip,
            presence = this.card.presence,
            tokenId = this.card.token_id,
            tokenName = this.card.token_name,
            tokenPlurals = this.card.token_plurals,
            tokenCount = this.card.token_count,
            harm = this.card.harm,
            approachConflict = this.card.approach_conflict,
            approachReason = this.card.approach_reason,
            approachExploration = this.card.approach_exploration,
            approachConnection = this.card.approach_connection,
            text = this.card.text,
            realText = if (locale == "en") null else this.card.real_text,
            setId = this.card.set_id,
            setName = this.card.set_name,
            setTypeId = this.card.set_type_id,
            setSize = this.card.set_size,
            setTypeName = this.card.set_type_name,
            setPosition = this.card.set_position,
            quantity = this.card.quantity,
            level = this.card.level,
            flavor = this.card.flavor,
            realFlavor = if (locale == "en") null else this.card.real_flavor,
            typeId = this.card.type_id,
            typeName = this.card.type_name,
            cost = this.card.cost,
            aspectId = this.card.aspect_id,
            aspectName = this.card.aspect_name,
            aspectShortName = this.card.aspect_short_name,
            progress = this.card.progress,
            imageSrc = this.card.imagesrc ?: this.card.real_imagesrc,
            position = this.card.position,
            deckLimit = this.card.deck_limit,
            spoiler = this.card.spoiler,
            sunChallenge = this.card.sun_challenge,
            mountainChallenge = this.card.mountain_challenge,
            crestChallenge = this.card.crest_challenge,
        )
    }
}

/**
 * Extension function to convert list of [GetAllCardsQuery.Card] to list of [Card]
 */
fun List<GetAllCardsQuery.Card>.toCards(locale: String): List<Card> {
    return this.mapNotNull {
        it.toCard(locale)
    }
}