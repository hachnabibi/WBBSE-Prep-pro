package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SplashBg = Color(0xFF080D1D)
private val SplashCard = Color(0xFF131C31)
private val PrimaryStart = Color(0xFF4935D5)
private val PrimaryEnd = Color(0xFF6366F1)
private val TextPrimary = Color(0xFFF8FAFC)
private val TextSecondary = Color(0xFF94A3B8)
private val AccentGold = Color(0xFFFFD700)

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onTimeout: (() -> Unit)? = null
) {
    if (onTimeout != null) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            onTimeout()
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "splash_transition")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated Logo Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Outer pulsing glow ring
                Box(
                    modifier = Modifier
                        .size(130.dp * pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(PrimaryEnd.copy(alpha = ringAlpha * 0.6f), Color.Transparent)
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = PrimaryStart.copy(alpha = ringAlpha),
                            shape = CircleShape
                        )
                )

                // Main Logo Icon
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PrimaryStart, PrimaryEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Desh Exam Logo",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title
            Text(
                text = "দেশ এক্সাম",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SplashCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryEnd.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "WBBSE, WBCHSE & WBBPE প্রস্তুতি",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = AccentGold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Animated Loading Status Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = PrimaryEnd,
                    strokeWidth = 3.dp
                )

                Text(
                    text = "ক্রেডেনশিয়াল যাচাই করা হচ্ছে...",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
