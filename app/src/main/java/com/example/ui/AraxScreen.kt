package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.components.AraxSplashOverlay
import com.example.ui.components.AraxWebViewContainer
import com.example.ui.theme.AraxBlack
import kotlinx.coroutines.delay

@Composable
fun AraxScreen(
    modifier: Modifier = Modifier
) {
    var loadingProgress by remember { mutableIntStateOf(0) }
    var isPageFinished by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var isSplashVisible by remember { mutableStateOf(true) }
    var minSplashElapsed by remember { mutableStateOf(false) }

    // Enforce a minimum 1.2-second cinematic splash presence for smooth branding,
    // with a fallback timer to ensure the splash never permanently blocks the UI
    LaunchedEffect(Unit) {
        delay(1200)
        minSplashElapsed = true
        delay(2300)
        isSplashVisible = false
    }

    // Dismiss splash screen smoothly when both minimum duration elapsed and page is ready
    LaunchedEffect(minSplashElapsed, isPageFinished, loadingProgress, hasError) {
        if (hasError) {
            isSplashVisible = false
        } else if (minSplashElapsed && (isPageFinished || loadingProgress >= 95)) {
            // Short delay to let initial DOM paint complete
            delay(200)
            isSplashVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AraxBlack)
            .testTag("arax_main_screen")
    ) {
        // Native full-screen PWA WebView Container
        AraxWebViewContainer(
            onProgressUpdate = { progress ->
                loadingProgress = progress
            },
            onPageFinishedLoading = {
                isPageFinished = true
            },
            onErrorOccurred = {
                hasError = true
            },
            onErrorCleared = {
                hasError = false
            },
            hasError = hasError,
            modifier = Modifier.fillMaxSize()
        )

        // Cinematic Arax Splash Screen Overlay
        AraxSplashOverlay(
            isVisible = isSplashVisible,
            progress = loadingProgress,
            modifier = Modifier.fillMaxSize()
        )
    }
}
