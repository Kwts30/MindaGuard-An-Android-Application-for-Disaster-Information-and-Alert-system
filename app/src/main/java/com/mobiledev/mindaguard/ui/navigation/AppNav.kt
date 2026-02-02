package com.mobiledev.mindaguard.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobiledev.mindaguard.ui.MainPageScreen
import com.mobiledev.mindaguard.ui.components.PillBottomBar
import com.mobiledev.mindaguard.ui.components.PillTab
import com.mobiledev.mindaguard.ui.screens.AlertScreen
import com.mobiledev.mindaguard.ui.screens.EmergencyScreen
import com.mobiledev.mindaguard.ui.screens.MapScreen
import com.mobiledev.mindaguard.ui.screens.MenuScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Map : Screen("map")
    object Menu : Screen("menu")
    object Alerts : Screen("alerts")
    object Emergency : Screen("emergency")
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    val tabs = listOf(
        PillTab(Screen.Home.route, "Home", Icons.Outlined.Home),
        PillTab(Screen.Map.route, "Map", Icons.Outlined.Place),
        PillTab(Screen.Menu.route, "Menu", Icons.Outlined.Menu)
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)              // <- satisfies the lint check
                .background(MaterialTheme.colorScheme.background)
        ) {

            /* ---------------- NAV CONTENT ---------------- */

            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {

                composable(Screen.Home.route) {
                    MainPageScreen(
                        onAlertClick = {
                            navController.navigate(Screen.Alerts.route)
                        },
                        onEmergencyClick = {
                            navController.navigate(Screen.Emergency.route)
                        }
                    )
                }

                composable(Screen.Map.route) {
                    MapScreen(
                        onBackClick = {
                            navController.popBackStack(
                                route = Screen.Home.route,
                                inclusive = false
                            )
                        }
                    )
                }

                composable(Screen.Menu.route) {
                    MenuScreen()
                }

                composable(Screen.Alerts.route) {
                    AlertScreen()
                }

                composable(Screen.Emergency.route) {
                    EmergencyScreen()
                }
            }

            /* ---------------- FLOATING PILL BOTTOM BAR ---------------- */

            val backStack by navController.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route

            if (
                currentRoute == Screen.Home.route ||
                currentRoute == Screen.Menu.route
            ) {
                PillBottomBar(
                    modifier = Modifier.padding(bottom = 15.dp).align(Alignment.BottomCenter),
                    currentRoute = currentRoute,
                    tabs = tabs,
                    onSelectTab = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                        }
                    }
                )
            }
        }
    }
}