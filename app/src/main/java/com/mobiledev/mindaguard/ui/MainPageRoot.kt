package com.mobiledev.mindaguard.ui

import androidx.compose.runtime.Composable
import com.mobiledev.mindaguard.theme.MindaGuardTheme
import java.time.LocalTime

@Composable
fun MainPageRoot() {
    // Always light theme now
    MindaGuardTheme(darkTheme = false) {
        MainPageScreen(currentTime = LocalTime.now())
    }
}