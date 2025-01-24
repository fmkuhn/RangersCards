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
import com.rangerscards.GetProfileQuery
import com.rangerscards.GetUserInfoByHandleQuery
import com.rangerscards.MainActivity
import com.rangerscards.R
import com.rangerscards.UpdateHandleMutation
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
    private val userPreferencesRepository: UserPreferencesRepository
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

    suspend fun getUserInfo(id: String) {
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