package com.rangerscards.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

/**
 * Top level routes for an app.
 */
enum class TopLevelRoutes(val route: String, @DrawableRes val icon: Int, @StringRes val label: Int) {
    Cards("Cards", R.drawable.card, R.string.cards_nav_bar_button),
    Decks("Decks", R.drawable.cards_32dp, R.string.decks_nav_bar_button),
    Campaigns("Campaigns", R.drawable.guide, R.string.campaigns_nav_bar_button),
    Settings("Settings", R.drawable.settings_32dp, R.string.settings_nav_bar_button)
}

/**
 * App bottom bar to switch primary destinations in an app.
 */
@Composable
fun RangersNavigationBar(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar(
        containerColor = CustomTheme.colors.l30,
        modifier = Modifier.sizeIn(maxHeight = 70.dp)
    ) {
        TopLevelRoutes.entries.forEach { topLevelRoute ->
            NavigationBarItem(
                selected = currentDestination?.route == topLevelRoute.route,
                onClick = { navController.navigate(topLevelRoute.route) {
                    navBackStackEntry?.destination?.route?.let {
                        popUpTo(it) {
                            saveState = true
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                } },
                icon = { Icon(
                    painterResource(id = topLevelRoute.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                ) },
                label = { Text(
                    text = stringResource(id = topLevelRoute.label),
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.5.sp
                ) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors().copy(
                    CustomTheme.colors.l30,
                    CustomTheme.colors.d30,
                    CustomTheme.colors.d30,
                    CustomTheme.colors.d30,
                    CustomTheme.colors.d30,
                )
            )
        }
    }
}
