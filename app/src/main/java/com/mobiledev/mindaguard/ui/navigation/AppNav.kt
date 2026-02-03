package com.mobiledev.mindaguard.ui.navigation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobiledev.mindaguard.ui.MainPageScreen
import com.mobiledev.mindaguard.ui.components.PillBottomBar
import com.mobiledev.mindaguard.ui.components.PillTab
import com.mobiledev.mindaguard.ui.menu.MenuActionCallbacks
import com.mobiledev.mindaguard.ui.screens.AlertScreen
import com.mobiledev.mindaguard.ui.screens.EmergencyScreen
import com.mobiledev.mindaguard.ui.screens.LoginScreen
import com.mobiledev.mindaguard.ui.screens.MapScreen
import com.mobiledev.mindaguard.ui.screens.MenuScreen
import com.mobiledev.mindaguard.ui.screens.RegisterScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
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

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Show bottom bar only for the main "tab" destinations
    // (Map is intended to be a full-screen experience)
    val showBottomBar = currentRoute == Screen.Home.route ||
        currentRoute == Screen.Menu.route

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(Screen.Register.route)
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Home.route) {
                    MainPageScreen(
                        onAlertClick = { navController.navigate(Screen.Alerts.route) },
                        onEmergencyClick = { navController.navigate(Screen.Emergency.route) }
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
                    val context = LocalContext.current

                    MenuScreen(
                        actions = MenuActionCallbacks(
                            onNotificationsClick = {
                                // Open system notification settings for this app
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }

                                // Fallback to app details if notifications screen isn't available
                                val safeIntent = if (intent.resolveActivity(context.packageManager) != null) {
                                    intent
                                } else {
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )
                                }

                                context.startActivity(safeIntent)
                            },
                            onAppInfoClick = {
                                // Open app info (App details) screen
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                                context.startActivity(intent)
                            }
                        )
                    )
                }

                composable(Screen.Alerts.route) {
                    AlertScreen()
                }

                composable(Screen.Emergency.route) {
                    EmergencyScreen()
                }
            }

            if (showBottomBar) {
                PillBottomBar(
                    modifier = Modifier
                        .padding(bottom = 15.dp)
                        .align(Alignment.BottomCenter),
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