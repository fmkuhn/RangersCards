package com.rangerscards.ui.settings

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
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
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.rangerscards.AcceptFriendRequestMutation
import com.rangerscards.GetAllCardsQuery
import com.rangerscards.GetCardsUpdatedAtQuery
import com.rangerscards.GetProfileQuery
import com.rangerscards.GetUserInfoByHandleQuery
import com.rangerscards.MainActivity
import com.rangerscards.R
import com.rangerscards.RejectFriendRequestMutation
import com.rangerscards.SendFriendRequestMutation
import com.rangerscards.SetAdhereTaboosMutation
import com.rangerscards.SetPackCollectionMutation
import com.rangerscards.UpdateHandleMutation
import com.rangerscards.data.UserAuthRepository
import com.rangerscards.data.UserPreferencesRepository
import com.rangerscards.data.database.card.Card
import com.rangerscards.data.database.repository.CardsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.util.Locale

/**
 * Data class to hold state for User's settings
 */
data class UserUIState(
    val currentUser: FirebaseUser? = Firebase.auth.currentUser,
    val userInfo: GetProfileQuery.Data? = null,
    val language: String = Locale.getDefault().language.substring(0..1),
    val settings: UserSettings = UserSettings()
)

data class UserSettings(
    val taboo: Boolean = false,
    val collection: List<String> = listOf("core")
)

val SUPPORTED_LANGUAGES = listOf("en", "ru", "de", "fr", "it")

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

    init {
        // Collect values from the data store
        viewModelScope.launch {
            userPreferencesRepository.isTabooSet.collect { taboo ->
                _userUiState.update {
                    it.copy(settings = it.settings.copy(taboo = taboo))
                }
            }
        }
    }

    init {
        // Collect values from the data store
        viewModelScope.launch {
            userPreferencesRepository.collection.collect { collection ->
                _userUiState.update {
                    it.copy(settings = it.settings.copy(collection = collection))
                }
            }
        }
    }

    private var _cardsUpdatedAt = MutableStateFlow("")

    init {
        viewModelScope.launch {
            userPreferencesRepository.cardsUpdatedAt.collect { updateAt ->
                _cardsUpdatedAt.update { updateAt }
            }
        }
    }

    // theme state
    val themeState: StateFlow<Int?> =
        userPreferencesRepository.isDarkTheme.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val isIncludeEnglishSearchResultsState: StateFlow<Boolean> =
        userPreferencesRepository.isIncludeEnglishSearchResults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private val _isCardsLoading = MutableStateFlow(false)
    var isCardsLoading = _isCardsLoading.asStateFlow()

    // Holds the current search query entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(emptyList<GetUserInfoByHandleQuery.Profile>())
    val searchResults: StateFlow<List<GetUserInfoByHandleQuery.Profile>> = _searchResults.asStateFlow()

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

    fun deleteUser(context: Context, email: String, password: String) {
        if (userAuthRepository.validateEmail(email)) {
            if (userAuthRepository.validatePassword(password)) {
                val user = _userUiState.value.currentUser
                user?.reauthenticate(EmailAuthProvider.getCredential(email, password))
                    ?.addOnCompleteListener {
                        if (it.isSuccessful) user.delete().addOnCompleteListener {
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

    private suspend fun getCurrentToken(context: Context): String? {
        return userUiState.value.currentUser?.getIdToken(isConnected(context))?.await()?.token
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) != null
    }

    fun getUserInfo(context: Context, id: String) {
        viewModelScope.launch {
            var token: String? = ""
            val result = performFirebaseOperationWithRetry {
                token = getCurrentToken(context)
            }
            if (result != null) apolloClient.query(GetProfileQuery(id))
                .addHttpHeader("Authorization", "Bearer $token")
                .toFlow()
                .collect {
                    if (it.data != null) _userUiState.update { uiState ->
                        uiState.copy(userInfo = it.data,
                            settings = UserSettings(
                                taboo = it.data!!.settings?.adhere_taboos ?: false,
                                collection = it.data!!.settings?.pack_collection
                                    ?.jsonArray?.map { element -> element.jsonPrimitive.content }
                                    ?: listOf("core")
                            )
                        )
                    }
                }
        }
    }

    suspend fun updateHandle(context: Context, handle: String) {
        if (handle == (userUiState.value.userInfo?.profile?.userProfile?.handle ?: "")) return
        var isTaken: Boolean
        viewModelScope.launch {
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
                    val token = getCurrentToken(context)
                    val response = apolloClient.mutation(UpdateHandleMutation(
                        userUiState.value.currentUser!!.uid,
                        handle.trim(),
                        normalizeHandle(handle))
                    ).addHttpHeader("Authorization", "Bearer $token")
                        .execute()
                    if (response.data != null) getUserInfo(context, userUiState.value.currentUser!!.uid)
                }
            }
        }.join()
    }

    fun selectTheme(theme: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveTabooPreference(theme)
        }
    }

    suspend fun setTaboo(context: Context) {
        val taboo = !userUiState.value.settings.taboo
        if (userUiState.value.currentUser != null) {
            val token = getCurrentToken(context)
            val response = apolloClient.mutation(SetAdhereTaboosMutation(
                userUiState.value.currentUser!!.uid,
                taboo)
            ).addHttpHeader("Authorization", "Bearer $token").execute()
            if (response.data != null) getUserInfo(context, userUiState.value.currentUser!!.uid)
        } else {
            userPreferencesRepository.saveTabooPreference(taboo)
            _userUiState.update {
                it.copy(
                    settings = it.settings.copy(taboo = taboo)
                )
            }
        }
    }

    fun setCollection(collection: List<String>, context: Context) {
        viewModelScope.launch {
            if (userUiState.value.currentUser != null) {
                val token = getCurrentToken(context)
                val response = apolloClient.mutation(SetPackCollectionMutation(
                    userUiState.value.currentUser!!.uid,
                    buildJsonArray { collection.forEach { add(it) } })
                ).addHttpHeader("Authorization", "Bearer $token")
                    .execute()
                if (response.data != null) getUserInfo(context, userUiState.value.currentUser!!.uid)
                userPreferencesRepository.saveCollectionPreference(collection)
                _userUiState.update {
                    it.copy(
                        settings = it.settings.copy(collection = collection)
                    )
                }
            } else {
                userPreferencesRepository.saveCollectionPreference(collection)
                _userUiState.update {
                    it.copy(
                        settings = it.settings.copy(collection = collection)
                    )
                }
            }
        }
    }

    fun updateLocale(locale: String, context: Context) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
        _userUiState.update { userUIState ->
            userUIState.copy(language = locale)
        }
        downloadCards(context)
    }

    private fun downloadCards(context: Context) {
        if (!isConnected(context)) return
        _isCardsLoading.update { true }
        viewModelScope.launch {
            val language = if (SUPPORTED_LANGUAGES.contains(_userUiState.value.language)) _userUiState.value.language
            else "en"
            val response = apolloClient.query(GetAllCardsQuery(language))
                .fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data != null) {
                if (cardsRepository.isExists()) cardsRepository.updateAllCards(response.data!!.cards.toCards(language))
                else cardsRepository.upsertAllCards(response.data!!.cards.toCards(language))
                val timestamp = response.data!!.all_updated_at.getOrNull(0)?.updated_at.toString()
                userPreferencesRepository.saveCardsUpdatedTimestamp(
                    timestamp
                )
                _cardsUpdatedAt.update { timestamp }
            }
        }.invokeOnCompletion {
            _isCardsLoading.update { false }
        }
    }

    fun updateCardsIfNotUpdated(context: Context) {
        if (!isConnected(context)) return
        _isCardsLoading.update { true }
        viewModelScope.launch {
            val response = apolloClient.query(GetCardsUpdatedAtQuery(_userUiState.value.language))
               .fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data != null) {
               if (userPreferencesRepository.compareTimestamps(
                       _cardsUpdatedAt.value,
                       response.data!!.card_updated_at.getOrNull(0)?.updated_at.toString()
               )) {
                   downloadCards(context)
               } else {
                   _isCardsLoading.update { false }
               }
            }
        }
    }

    fun downloadCardsIfDatabaseNotExists(context: Context) {
        viewModelScope.launch {
            val exists = cardsRepository.isExists()
            if (!exists) {
                downloadCards(context)
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

    fun openEmail(email: String, context: Context) {
        val uri = Uri.parse("mailto:$email")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        context.startActivity(intent)
    }

    fun setEnglishSearchResultsSetting(isInclude: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveIncludeEnglishSearchResults(isInclude)
        }
    }

    /**
     * Called when the user enters a new search term.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.update {
            newQuery
        }
    }

    fun clearSearchQuery() {
        _searchQuery.update { "" }
    }

    fun getUsersByHandle(handle: String) {
        viewModelScope.launch {
            if (handle == "") _searchResults.update {
                emptyList()
            } else {
                val normalizeHandle = normalizeHandle(handle)
                val result = apolloClient.query(GetUserInfoByHandleQuery("%$normalizeHandle%"))
                    .fetchPolicy(FetchPolicy.NetworkOnly).execute()
                if (result.data != null) {
                    _searchResults.update {
                        result.data?.profile ?: emptyList()
                    }
                }
            }
        }
    }

    fun sendFriendRequest(toUserId: String, context: Context) {
        val userId = userUiState.value.currentUser?.uid!!
        viewModelScope.launch {
            val token = getCurrentToken(context)
            apolloClient.mutation(SendFriendRequestMutation(toUserId))
                .addHttpHeader("Authorization", "Bearer $token").execute()
            getUserInfo(context, userId)
        }
    }
    fun acceptFriendRequest(toUserId: String, context: Context) {
        val userId = userUiState.value.currentUser?.uid!!
        viewModelScope.launch {
            val token = getCurrentToken(context)
            apolloClient.mutation(AcceptFriendRequestMutation(toUserId))
                .addHttpHeader("Authorization", "Bearer $token").execute()
            getUserInfo(context, userId)
        }
    }
    fun rejectFriendRequest(toUserId: String, context: Context) {
        val userId = userUiState.value.currentUser?.uid!!
        viewModelScope.launch {
            val token = getCurrentToken(context)
            apolloClient.mutation(RejectFriendRequestMutation(toUserId))
                .addHttpHeader("Authorization", "Bearer $token").execute()
            getUserInfo(context, userId)
        }
    }
}

suspend fun <T> performFirebaseOperationWithRetry(
    maxRetries: Int = 3,
    initialDelay: Long = 1000L,
    factor: Double = 2.0,
    block: suspend () -> T
): T? {
    var currentDelay = initialDelay
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            Log.w("FirebaseOperation", "Attempt ${attempt + 1} failed: ${e.localizedMessage}")
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong()
    }
    return null
}

/**
 * Extension function to convert [GetAllCardsQuery.Card] to [Card]
 */
fun GetAllCardsQuery.Card.toCard(locale: String): Card? {
    return this.card.id?.let {
        Card(
            id = it,
            code = this.card.code.toString(),
            name = this.card.name,
            realName = if (locale == "en") null else this.card.real_name,
            realTraits = this.card.real_traits,
            traits = this.card.traits,
            equip = this.card.equip,
            presence = this.card.presence,
            tabooId = this.card.taboo_id,
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
            realImageSrc = this.card.real_imagesrc,
            position = this.card.position,
            deckLimit = this.card.deck_limit,
            spoiler = this.card.spoiler,
            sunChallenge = this.card.sun_challenge,
            mountainChallenge = this.card.mountain_challenge,
            crestChallenge = this.card.crest_challenge,
            packId = this.card.pack_id,
            packName = this.card.pack_name,
            packShortName = this.card.pack_short_name,
            packPosition = this.card.pack_position,
            subsetId = this.card.subset_id,
            subsetName = this.card.set_name,
            subsetPosition = this.card.subset_position,
            subsetSize = this.card.subset_size,
            composite = listOfNotNull(
                this.card.name, this.card.traits, this.card.text, this.card.flavor, this.card.type_name,
                this.card.sun_challenge, this.card.mountain_challenge, this.card.crest_challenge
            ).joinToString(" "),
            realComposite = if (locale == "en") null else listOfNotNull(
                this.card.name, this.card.real_name, this.card.traits, this.card.real_traits,
                this.card.type_name, this.card.type_id, this.card.text, this.card.real_text,
                this.card.flavor, this.card.real_flavor, this.card.sun_challenge,
                this.card.mountain_challenge, this.card.crest_challenge
            ).joinToString(" "),
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