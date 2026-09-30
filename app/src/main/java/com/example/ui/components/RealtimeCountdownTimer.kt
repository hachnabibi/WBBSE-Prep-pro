package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Real-time Countdown Timer Component for timed practice and mock test sessions.
 * Provides rich visual feedback:
 * - Dynamic circular arc tracking fraction of time remaining
 * - Color gradient shifting (Indigo/Emerald -> Amber Warning -> Crimson Danger)
 * - Pulsing heartbeat urgency animation during the final seconds
 * - Formatted MM:SS display with contextual status badges
 */
@Composable
fun RealtimeCountdownTimer(
    totalSeconds: Int,
    remainingSeconds: Int,
    isBengali: Boolean,
    modifier: Modifier = Modifier,
    isCompact: Boolean = true
) {
    val totalSafe = totalSeconds.coerceAtLeast(1)
    val progress = (remainingSeconds.toFloat() / totalSafe).coerceIn(0f, 1f)

    // Smooth animated arc progression
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
        label = "timer_arc_progress"
    )

    val isDanger = remainingSeconds in 1..20 || (progress <= 0.15f && remainingSeconds > 0)
    val isWarning = !isDanger && (remainingSeconds in 21..60 || progress <= 0.30f)

    // Pulsing heartbeat animation when time is critically low
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isDanger) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Dynamic color transition based on urgency
    val arcColor by animateColorAsState(
        targetValue = when {
            isDanger -> WrongRed
            isWarning -> TertiaryAmber
            else -> PrimaryIndigo
        },
        animationSpec = tween(durationMillis = 300),
        label = "arc_color"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    if (isCompact) {
        val pillBgColor = when {
            isDanger -> WrongRed.copy(alpha = if (isDark) 0.25f else 0.15f)
            isWarning -> TertiaryAmber.copy(alpha = if (isDark) 0.25f else 0.15f)
            else -> if (isDark) Color(0xFF2E2660) else Color(0xFFEDE9FE)
        }
        val pillBorderColor = when {
            isDanger -> WrongRed.copy(alpha = 0.6f)
            isWarning -> TertiaryAmber.copy(alpha = 0.6f)
            else -> PrimaryIndigo.copy(alpha = 0.4f)
        }
        val pillContentColor = when {
            isDanger -> WrongRed
            isWarning -> TertiaryAmber
            else -> if (isDark) Color(0xFFA5B4FC) else PrimaryIndigo
        }

        // Compact Pill matching Question Counter Card on right
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = pillBgColor,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = pillBorderColor
            ),
            modifier = modifier
                .height(34.dp)
                .defaultMinSize(minWidth = 84.dp)
                .scale(if (isDanger) pulseScale.coerceAtMost(1.06f) else 1f)
                .testTag("realtime_countdown_timer")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Mini Circular Arc Indicator
                Box(
                    modifier = Modifier.size(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 2.dp.toPx()
                        // Background track
                        drawCircle(
                            color = pillContentColor.copy(alpha = 0.2f),
                            style = Stroke(width = strokeWidth)
                        )
                        // Progress arc
                        drawArc(
                            color = pillContentColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Icon(
                        imageVector = if (isDanger) Icons.Default.WarningAmber else Icons.Default.Timer,
                        contentDescription = "Timer Icon",
                        tint = pillContentColor,
                        modifier = Modifier.size(9.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = pillContentColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    } else {
        // Expanded Circular Dial (for Mock Test HUD or dedicated Timer Cards)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
                .scale(if (isDanger) pulseScale else 1f)
                .testTag("realtime_countdown_timer_expanded")
        ) {
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 7.dp.toPx()
                    // Track
                    drawCircle(
                        color = arcColor.copy(alpha = 0.15f),
                        style = Stroke(width = strokeWidth)
                    )
                    // Animated Arc with Gradient
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(arcColor.copy(alpha = 0.7f), arcColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isDanger) Icons.Default.HourglassBottom else Icons.Default.HourglassTop,
                        contentDescription = "Hourglass",
                        tint = arcColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isDanger) WrongRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = arcColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = when {
                        remainingSeconds <= 0 -> "সময় শেষ!"
                        isDanger -> "⚡ দ্রুত শেষ করো!"
                        isWarning -> "⚠️ শেষ মিনিট"
                        else -> "সময় বাকি"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = arcColor
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
