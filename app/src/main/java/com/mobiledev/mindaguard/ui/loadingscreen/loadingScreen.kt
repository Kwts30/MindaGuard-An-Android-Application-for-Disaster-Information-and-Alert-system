package com.mobiledev.mindaguard.ui.loadingscreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobiledev.mindaguard.MainActivity
import com.mobiledev.mindaguard.R
import com.mobiledev.mindaguard.theme.MindaGuardTheme
import kotlinx.coroutines.delay

/**
 * Splash activity. This is the launcher activity in the manifest.
 */
class LoadingScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            MindaGuardTheme {
                MindaGuardSplash(
                    onTimeout = {
                        startActivity(Intent(this@LoadingScreen, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

/**
 * Single composable for splash. There MUST be only one with this name.
 */
@Composable
fun MindaGuardSplash(onTimeout: () -> Unit) {
    val isPreview = LocalInspectionMode.current

    // Draw UI
    MindaGuardSplashContent()

    // Trigger navigation only at runtime, not in Preview
    if (!isPreview) {
        LaunchedEffect(Unit) {
            delay(1500) // 1.5s splash
            onTimeout()
        }
    }
}

@Composable
fun MindaGuardSplashContent(
    logoFraction: Float = 0.5f,
    subtextMaxHeightDp: Int = 80
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.bk),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Top logo
        Image(
            painter = painterResource(id = R.drawable.mmcm_logo),
            contentDescription = stringResource(id = R.string.splash_logo_description),
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopCenter)
                .padding(top = 50.dp),
            contentScale = ContentScale.Fit
        )

        // Center content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_only),
                contentDescription = stringResource(id = R.string.splash_logo_description),
                modifier = Modifier
                    .fillMaxWidth(logoFraction)
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(id = R.drawable.icon_text),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = subtextMaxHeightDp.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    MindaGuardTheme { MindaGuardSplashContent() }
}