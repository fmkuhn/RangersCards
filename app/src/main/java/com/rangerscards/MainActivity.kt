package com.rangerscards

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.rangerscards.ui.AppViewModelProvider
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.RangersCardsTheme

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: SettingsViewModel
    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var isDataLoaded = false
        splashScreen.setKeepOnScreenCondition { !isDataLoaded }
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        appUpdateManager = AppUpdateManagerFactory.create(this)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                val priority = appUpdateInfo.updatePriority()
                val stalenessDays = appUpdateInfo.clientVersionStalenessDays() ?: -1
                val isImmediateUpdateNeeded = when {
                    priority >= 4 -> true
                    priority == 3 && stalenessDays >= 7 -> true
                    priority <= 2 && stalenessDays >= 30 -> true
                    else -> false
                }
                if (isImmediateUpdateNeeded) appUpdateManager.startUpdateFlow(
                    appUpdateInfo,
                    this,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
        setContent {
            viewModel = viewModel(factory = AppViewModelProvider.Factory)
            // Collecting user's theme from shared preferences via viewmodel - false = light, true = dark
            val currentTheme = when(viewModel.themeState.collectAsState().value) {
                0 -> false
                1 -> true
                2 -> isSystemInDarkTheme()
                else -> null
            }
            if (currentTheme != null) {
                isDataLoaded = true
                RangersCardsTheme(currentTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = CustomTheme.colors.l30
                    ) {
                        RangersApp(this, currentTheme, viewModel)
                    }
                }
            }
        }
    }

    // Checks that the update is not stalled during 'onResume()'.
    override fun onResume() {
        super.onResume()

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlow(
                        appUpdateInfo,
                        this,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
    }

    fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    viewModel.setUser(auth.currentUser)
                } else {
                    // If sign in fails, display a message to the user.
                    authenticationFailedToast()
                    viewModel.setUser(null)
                }
            }
    }

    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    viewModel.setUser(auth.currentUser)
                } else {
                    // If sign in fails, display a message to the user.
                    authenticationFailedToast()
                    viewModel.setUser(null)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun authenticationFailedToast() {
        Toast.makeText(
            baseContext,
            getString(R.string.authentication_failed_toast),
            Toast.LENGTH_SHORT,
        ).show()
    }
}