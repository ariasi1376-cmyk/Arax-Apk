package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AraxAmber
import com.example.ui.theme.AraxBlack

@Composable
fun AraxSplashOverlay(
    isVisible: Boolean,
    progress: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AraxBlack)
                .testTag("splash_screen_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // Official Arax PWA Logo - pure original proportions, colors, and shape
                Image(
                    painter = painterResource(id = R.drawable.arax_pwa),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier
                        .size(200.dp)
                        .testTag("splash_logo_container")
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Sleek loading progress indicator
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(3.dp)
                ) {
                    if (progress in 1..99) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = AraxAmber,
                            trackColor = Color(0xFF1A1A1A)
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = AraxAmber,
                            trackColor = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
        }
    }
}
