package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AraxAmber
import com.example.ui.theme.AraxAmberDark
import com.example.ui.theme.AraxBlack
import com.example.ui.theme.AraxTextSecondary

@Composable
fun AraxSplashOverlay(
    isVisible: Boolean,
    progress: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AraxBlack)
                .testTag("splash_screen_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // Subtle ambient radial glow behind the logo
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .scale(pulseScale)
                    .alpha(glowAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AraxAmberDark.copy(alpha = 0.3f),
                                AraxAmberDark.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // Arax Brand Logo
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .testTag("splash_logo_container"),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_arax_logo),
                        contentDescription = stringResource(id = R.string.app_name),
                        modifier = Modifier
                            .width(220.dp)
                            .height(110.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Brand Title in Cinematic Typography
                Text(
                    text = "ARAX",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 8.sp,
                    color = AraxAmber,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "AI THEATER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 4.sp,
                    color = AraxTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Sleek amber loading progress indicator
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(3.dp)
                ) {
                    if (progress in 1..99) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = AraxAmber,
                            trackColor = Color(0xFF1E1E24)
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = AraxAmber,
                            trackColor = Color(0xFF1E1E24)
                        )
                    }
                }
            }
        }
    }
}
