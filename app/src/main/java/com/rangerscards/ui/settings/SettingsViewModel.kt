package com.rangerscards.ui.settings

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
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
 * ViewModel to maintain user's settings.
 */
class SettingsViewModel(
    private val apolloClient: ApolloClient,
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

    private fun invalidPasswordToast(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.invalid_password_toast),
            Toast.LENGTH_SHORT,
        ).show()
    }

    private fun invalidEmailToast(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.invalid_email_toast),
            Toast.LENGTH_SHORT,
        ).show()
    }

    fun signIn(mainActivity: MainActivity, email: String, password: String) {
        val context = mainActivity.baseContext
        if (validateEmail(email)) {
            if (validatePassword(password)) {
                mainActivity.signIn(email, password)
            } else {
                invalidPasswordToast(context)
            }
        } else {
            invalidEmailToast(context)
        }
    }

    fun createAccount(mainActivity: MainActivity, email: String, password: String) {
        val context = mainActivity.baseContext
        if (validateEmail(email)) {
            if (validatePassword(password)) {
                mainActivity.createAccount(email, password)
            } else {
                invalidPasswordToast(context)
            }
        } else {
            invalidEmailToast(context)
        }
    }

    fun signOut(mainActivity: MainActivity) {
        mainActivity.signOut()
        _userUiState.update {
            it.copy(currentUser = null, userInfo = null)
        }
    }

    fun deleteUser(mainActivity: MainActivity, email: String, password: String) {
        val context = mainActivity.baseContext
        if (validateEmail(email)) {
            if (validatePassword(password)) {
                val user = userUiState.value.currentUser
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
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.invalid_credentials_toast),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            } else {
                invalidPasswordToast(context)
            }
        } else {
            invalidEmailToast(context)
        }
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun validatePassword(password: String): Boolean {
        return password.length in 6..4096
    }

    private fun normalizeHandle(handle: String): String {
        return handle.replace("[\\.\\$\\[\\]#/]".toRegex(),"_")
            .lowercase(Locale.ENGLISH).trim()
    }

    private suspend fun getCurrentToken(): String? {
        return userUiState.value.currentUser?.getIdToken(false)?.await()?.token
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
        if (handle == userUiState.value.userInfo?.profile?.userProfile?.handle.toString()) return
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
                    val token = getCurrentToken()
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
    }
}

data class UserUIState(
    val currentUser: FirebaseUser? = Firebase.auth.currentUser,
    val userInfo: GetProfileQuery.Data? = null,
)