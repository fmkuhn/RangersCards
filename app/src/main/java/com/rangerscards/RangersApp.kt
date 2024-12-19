package com.rangerscards

import androidx.compose.runtime.Composable
import com.rangerscards.ui.settings.SettingsScreen

/**
 * Top level composable that represents screens for the application.
 */
@Composable
fun RangersApp(mainActivity: MainActivity, isDarkTheme: Boolean) {
    //RangersNavHost(navController = navController)

    //TODO: Move to NavGraph
    SettingsScreen(mainActivity, isDarkTheme)
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