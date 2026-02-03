package com.mobiledev.mindaguard.ui.menu

/**
 * Holds all callbacks for the menu page actions.
 * You can pass a custom instance from AppNav or ViewModel.
 */
data class MenuActionCallbacks(
    val onUserProfileClick: () -> Unit = {},
    val onNotificationsClick: () -> Unit = {},
    val onInformationClick: () -> Unit = {},
    val onAppInfoClick: () -> Unit = {},
    val onLogoutClick: () -> Unit = {}
)