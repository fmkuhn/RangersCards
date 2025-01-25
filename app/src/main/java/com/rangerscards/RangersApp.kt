package com.rangerscards

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rangerscards.ui.settings.SettingsScreen
import com.rangerscards.ui.settings.SettingsViewModel
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

data class TopLevelRoute(val route: String, @DrawableRes val icon: Int, @StringRes val label: Int)
// Routes
val topLevelRoutes = listOf(
    TopLevelRoute("Cards", R.drawable.card, R.string.cards_nav_bar_button),
    TopLevelRoute("Decks", R.drawable.cards_32dp, R.string.decks_nav_bar_button),
    TopLevelRoute("Campaigns", R.drawable.guide, R.string.campaigns_nav_bar_button),
    TopLevelRoute("Settings", R.drawable.settings_32dp, R.string.settings_nav_bar_button),
)
/**
 * Top level composable that represents screens for the application.
 */
@Composable
fun RangersApp(mainActivity: MainActivity, isDarkTheme: Boolean, settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    //RangersNavHost(navController = navController)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CustomTheme.colors.l30
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                topLevelRoutes.forEach { topLevelRoute ->
                    NavigationBarItem(
                        selected = currentDestination?.route == topLevelRoute.route,
                        onClick = { /*TODO*/ },
                        icon = { Icon(
                            painterResource(id = topLevelRoute.icon),
                            contentDescription = null,
                            tint = CustomTheme.colors.d30,
                            modifier = Modifier.size(24.dp)
                        ) },
                        label = { Text(
                            text = stringResource(id = topLevelRoute.label),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 0.5.sp
                        ) },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemColors(
                            CustomTheme.colors.d30,
                            CustomTheme.colors.d30,
                            Color.Transparent,
                            CustomTheme.colors.d30,
                            CustomTheme.colors.d30,
                            CustomTheme.colors.d30,
                            CustomTheme.colors.d30
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = topLevelRoutes[3].route
        ) {
            composable(topLevelRoutes[3].route) {
                SettingsScreen(
                    mainActivity = mainActivity,
                    isDarkTheme = isDarkTheme,
                    settingsViewModel = settingsViewModel,
                    contentPadding = innerPadding)
            }
        }
    }
}

/**
 * App bar to display title and conditionally display the back navigation.
 */
//TODO: Implement TopAppBar in custom design
//@Composable
//fun RangersTopAppBar(
//    title: String,
//    canNavigateBack: Boolean,
//    modifier: Modifier = Modifier,
//    navigateUp: () -> Unit = {}
//) {
//    CenterAlignedTopAppBar(
//        title = { Text(title) },
//        modifier = modifier,
//        navigationIcon = {
//            if (canNavigateBack) {
//                IconButton(onClick = navigateUp) {
//                    Icon(
//                        imageVector = Icons.Filled.ArrowBack,
//                        contentDescription = stringResource(string.back_button)
//                    )
//                }
//            }
//        }
//    )
//}