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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

/**
 * Top level routes for an app.
 */
sealed class BottomNavScreen(
    val route: String,
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    val startDestination: String
) {
    data object Cards : BottomNavScreen(
        route = "cards",
        icon = R.drawable.card,
        label = R.string.cards_nav_bar_button,
        startDestination = "cards/start"
    )
    data object Decks : BottomNavScreen(
        route = "decks",
        icon = R.drawable.cards_32dp,
        label = R.string.decks_nav_bar_button,
        startDestination = "decks/start"
    )
    data object Campaigns : BottomNavScreen(
        route = "campaigns",
        icon = R.drawable.guide,
        label = R.string.campaigns_nav_bar_button,
        startDestination = "campaigns/start"
    )
    data object Settings : BottomNavScreen(
        route = "settings",
        icon = R.drawable.settings_32dp,
        label = R.string.settings_nav_bar_button,
        startDestination = "settings/start"
    )
}

/**
 * App bottom bar to switch primary destinations in an app.
 */
@Composable
fun RangersNavigationBar(
    navController: NavHostController,
    bottomNavItems: List<BottomNavScreen>,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = CustomTheme.colors.l30,
        modifier = Modifier.sizeIn(maxHeight = 70.dp)
    ) {
        bottomNavItems.forEach { bottomNavItem ->
            val isSelected = currentRoute == bottomNavItem.route ||
                    currentRoute?.startsWith(bottomNavItem.route) == true
            NavigationBarItem(
                selected = isSelected,
                onClick = { if (isSelected) {
                    navController.popBackStack(bottomNavItem.startDestination, false)
                } else {
                    navController.navigate(bottomNavItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                } },
                icon = { Icon(
                    painterResource(id = bottomNavItem.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                ) },
                label = { Text(
                    text = stringResource(id = bottomNavItem.label),
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
                ),
                modifier = Modifier.ignoreHorizontalParentPadding(horizontal = 8.dp)
            )
        }
    }
}

fun Modifier.ignoreHorizontalParentPadding(horizontal: Dp): Modifier =
    this.layout { measurable, constraints ->
        // Convert the Dp into pixels:
        val extraPx = horizontal.roundToPx()
        // Make a new constraint that's wider by 2 * extraPx:
        val newConstraints = constraints.copy(
            minWidth = (constraints.minWidth - 2 * extraPx).coerceAtLeast(0),
            maxWidth = constraints.maxWidth + 2 * extraPx
        )
        // Measure with the expanded width:
        val placeable = measurable.measure(newConstraints)
        // The layout size: we still honor the *original* constraints' height,
        // but we size the width to fill whichever is larger (placeable or minWidth).
        val width  = placeable.width.coerceAtLeast(constraints.minWidth)
        val height = placeable.height

        layout(width, height) {
            // Place the content shifted left by extraPx, so its center stays aligned:
            placeable.place(x = -extraPx, y = 0)
        }
    }
