package com.mobiledev.mindaguard.ui.navigation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mobiledev.mindaguard.backend.CommunityAlertsViewModel
import com.mobiledev.mindaguard.backend.ProfileUiState
import com.mobiledev.mindaguard.backend.UserProfileViewModel
import com.mobiledev.mindaguard.ui.MainPageScreen
import com.mobiledev.mindaguard.ui.components.PillBottomBar
import com.mobiledev.mindaguard.ui.components.PillTab
import com.mobiledev.mindaguard.ui.menu.MenuActionCallbacks
import com.mobiledev.mindaguard.ui.screens.AdminAlertsScreen
import com.mobiledev.mindaguard.ui.screens.AlertScreen
import com.mobiledev.mindaguard.ui.screens.CommunityReport
import com.mobiledev.mindaguard.ui.screens.CreateAlertScreen
import com.mobiledev.mindaguard.ui.screens.EditProfileScreen
import com.mobiledev.mindaguard.ui.screens.EarthquakeAlarmScreen
import com.mobiledev.mindaguard.ui.screens.EarthquakeAlertScreen
import com.mobiledev.mindaguard.ui.screens.EmergencyScreen
import com.mobiledev.mindaguard.ui.screens.LoginScreen
import com.mobiledev.mindaguard.ui.screens.MapScreen
import com.mobiledev.mindaguard.ui.screens.MenuScreen
import com.mobiledev.mindaguard.ui.screens.MyReportsScreen
import com.mobiledev.mindaguard.ui.screens.ProfileScreen
import com.mobiledev.mindaguard.ui.screens.RegisterScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Map : Screen("map")
    object Menu : Screen("menu")
    object Alerts : Screen("alerts")
    object Emergency : Screen("emergency")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object CreateAlert : Screen("create_alert")
    object MyReports : Screen("my_reports")
    object AdminAlerts : Screen("admin_alerts")
    object EarthquakeAlert : Screen("earthquake_alert")
    object EarthquakeAlarm : Screen("earthquake_alarm")
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val profileViewModel: UserProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val alertsViewModel: CommunityAlertsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    // If user is already signed in, skip straight to Home
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    val tabs = listOf(
        PillTab(Screen.Home.route, "Home", Icons.Outlined.Home),
        PillTab(Screen.Map.route, "Map", Icons.Outlined.Place),
        PillTab(Screen.CreateAlert.route, "Create Alert", Icons.Outlined.AddAlert),
        PillTab(Screen.Menu.route, "Menu", Icons.Outlined.Menu)
    )

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val showBottomBar = currentRoute == Screen.Home.route ||
        currentRoute == Screen.CreateAlert.route ||
        currentRoute == Screen.Menu.route

    // Routes that live on the bottom bar — smooth crossfade only, no slide
    val bottomTabRoutes = setOf(
        Screen.Home.route,
        Screen.CreateAlert.route,
        Screen.Menu.route
    )

    // Material-style decelerate easing for slide transitions
    val decelerateEasing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val slideSpec        = tween<IntOffset>(durationMillis = 340, easing = decelerateEasing)
    val fadeSpec         = tween<Float>(durationMillis = 220, easing = decelerateEasing)
    val crossfadeSpec    = tween<Float>(durationMillis = 220, easing = decelerateEasing)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    val from = initialState.destination.route
                    val to   = targetState.destination.route
                    if (from in bottomTabRoutes && to in bottomTabRoutes) {
                        // Tab ↔ Tab: smooth crossfade, no jarring slide
                        fadeIn(crossfadeSpec)
                    } else {
                        // Sub-screen push: slide in from right with small offset buffer
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = slideSpec,
                            initialOffset = { (it * 0.10f).toInt() }   // only 10% slide, rest is fade
                        ) + fadeIn(fadeSpec)
                    }
                },
                exitTransition = {
                    val from = initialState.destination.route
                    val to   = targetState.destination.route
                    if (from in bottomTabRoutes && to in bottomTabRoutes) {
                        // Tab ↔ Tab: fade out simultaneously with crossfade
                        fadeOut(crossfadeSpec)
                    } else {
                        // Sub-screen push exit: old screen shrinks slightly left + fades
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = slideSpec,
                            targetOffset = { -(it * 0.10f).toInt() }
                        ) + fadeOut(fadeSpec)
                    }
                },
                popEnterTransition = {
                    // Back-navigate: slide in from left
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = slideSpec,
                        initialOffset = { -(it * 0.10f).toInt() }
                    ) + fadeIn(fadeSpec)
                },
                popExitTransition = {
                    // Back-navigate exit: slide out to right
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = slideSpec,
                        targetOffset = { (it * 0.10f).toInt() }
                    ) + fadeOut(fadeSpec)
                }
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            profileViewModel.loadProfile()
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
                            profileViewModel.loadProfile()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Home.route) {
                    val isRefreshing  by alertsViewModel.isRefreshing.collectAsState()
                    val listenerError by alertsViewModel.listenerError.collectAsState()
                    MainPageScreen(
                        alerts           = alertsViewModel.alerts,
                        isRefreshing     = isRefreshing,
                        listenerError    = listenerError,
                        onRefresh        = { alertsViewModel.refresh() },
                        onAlertClick     = { navController.navigate(Screen.Alerts.route) },
                        onEmergencyClick = { navController.navigate(Screen.Emergency.route) },
                        onMapClick       = { navController.navigate(Screen.Map.route) }
                    )
                }

                composable(Screen.Map.route) {
                    MapScreen(
                        onBackClick = { navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }}
                    )
                }

                composable(Screen.Menu.route) {
                    val context = LocalContext.current
                    val profile = (profileUiState as? ProfileUiState.Success)?.profile
                    val displayName = profile?.displayName ?: "User"
                    val photoUrl = profile?.photoUrl.orEmpty()
                    val isAdmin by alertsViewModel.isAdmin.collectAsState()

                    MenuScreen(
                        userName = displayName,
                        photoUrl = photoUrl,
                        isAdmin  = isAdmin,
                        actions = MenuActionCallbacks(
                            onUserProfileClick = {
                                navController.navigate(Screen.Profile.route)
                            },
                            onMyReportsClick = {
                                navController.navigate(Screen.MyReports.route)
                            },
                            onAdminPanelClick = {
                                navController.navigate(Screen.AdminAlerts.route)
                            },
                            onNotificationsClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            },
                            onAppInfoClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                                context.startActivity(intent)
                            },
                            onEarthquakeAlertClick = {
                                navController.navigate(Screen.EarthquakeAlert.route)
                            },
                            onLogoutClick = {
                                alertsViewModel.resetForLogout()
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    )
                }

                composable(Screen.Alerts.route) {
                    val isRefreshing  by alertsViewModel.isRefreshing.collectAsState()
                    val listenerError by alertsViewModel.listenerError.collectAsState()
                    AlertScreen(
                        alerts        = alertsViewModel.alerts,
                        isRefreshing  = isRefreshing,
                        listenerError = listenerError,
                        onRefresh     = { alertsViewModel.refresh() },
                        onBackClick   = { navController.popBackStack() }
                    )
                }

                composable(Screen.Emergency.route) {
                    EmergencyScreen()
                }

                composable(Screen.CreateAlert.route) {
                    val isSubmitting  by alertsViewModel.isSubmitting.collectAsState()
                    val submitError   by alertsViewModel.submitError.collectAsState()
                    val submitSuccess by alertsViewModel.submitSuccess.collectAsState()

                    // Navigate to Alerts feed once Firestore write succeeds
                    LaunchedEffect(submitSuccess) {
                        if (submitSuccess) {
                            alertsViewModel.clearSubmitSuccess()
                            navController.navigate(Screen.Alerts.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                    }

                    CreateAlertScreen(
                        onBackClick  = { navController.popBackStack() },
                        isSubmitting = isSubmitting,
                        submitError  = submitError,
                        onClearError = { alertsViewModel.clearSubmitError() },
                        onSubmit = { report: CommunityReport ->
                            alertsViewModel.addReport(report)
                            // Navigation happens via submitSuccess LaunchedEffect above
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onBackClick = { navController.popBackStack() },
                        onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                        viewModel = profileViewModel
                    )
                }

                composable(Screen.EditProfile.route) {
                    EditProfileScreen(
                        onBackClick = { navController.popBackStack() },
                        profileViewModel = profileViewModel
                    )
                }

                composable(Screen.MyReports.route) {
                    MyReportsScreen(
                        reports = alertsViewModel.myReports,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminAlerts.route) {
                    AdminAlertsScreen(
                        viewModel   = alertsViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.EarthquakeAlert.route) {
                    EarthquakeAlertScreen(
                        onBackClick = { navController.popBackStack() },
                        onSeeDemo   = { navController.navigate(Screen.EarthquakeAlarm.route) }
                    )
                }

                composable(Screen.EarthquakeAlarm.route) {
                    EarthquakeAlarmScreen(
                        eventName  = "TEST EARTHQUAKE",
                        distanceKm = 42.6f,
                        onDismiss  = { navController.popBackStack() }
                    )
                }
            }

            AnimatedVisibility(
                visible = showBottomBar,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 340, easing = decelerateEasing)
                ) + fadeIn(tween(durationMillis = 280, easing = decelerateEasing)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 280, easing = decelerateEasing)
                ) + fadeOut(tween(durationMillis = 220, easing = decelerateEasing))
            ) {
                PillBottomBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
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