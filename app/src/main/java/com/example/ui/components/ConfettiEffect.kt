package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ConfettiShape {
    RECTANGLE,
    CIRCLE,
    RIBBON
}

data class ConfettiParticle(
    val initialX: Float, // 0.0 to 1.0 (screen width fraction)
    val initialY: Float, // -0.2 to 0.0 (above screen)
    val speedY: Float,
    val speedX: Float,
    val swayAmplitude: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color,
    val shape: ConfettiShape
)

/**
 * Confetti Animation Overlay:
 * Provides a festive celebration explosion of colorful particles when students
 * achieve milestone events (e.g. completing a module in the Interactive Journey Map
 * or defeating a Chapter Boss Battle).
 */
@Composable
fun ConfettiAnimationOverlay(
    visible: Boolean,
    onAnimationEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val confettiColors = remember {
        listOf(
            ExamGold,
            ExamFlame,
            CorrectGreen,
            PrimaryIndigo,
            Color(0xFFEC4899), // Pink
            Color(0xFF06B6D4), // Cyan
            Color(0xFF8B5CF6), // Purple
            Color(0xFFF97316)  // Orange
        )
    }

    val particles = remember {
        List(70) {
            ConfettiParticle(
                initialX = Random.nextFloat(),
                initialY = -Random.nextFloat() * 0.3f,
                speedY = Random.nextFloat() * 0.7f + 0.5f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                swayAmplitude = Random.nextFloat() * 25f + 15f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                size = Random.nextFloat() * 10f + 8f,
                color = confettiColors[Random.nextInt(confettiColors.size)],
                shape = when (Random.nextInt(3)) {
                    0 -> ConfettiShape.RECTANGLE
                    1 -> ConfettiShape.CIRCLE
                    else -> ConfettiShape.RIBBON
                }
            )
        }
    }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
        )
        onAnimationEnd()
    }

    val progress = animProgress.value
    // Fade out towards the end
    val globalAlpha = ((1f - progress) * 2f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("confetti_animation_overlay")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            particles.forEach { p ->
                val currentY = (p.initialY + p.speedY * progress * 1.3f) * height
                val sway = sin(progress * 10f + p.initialX * 10f) * p.swayAmplitude
                val currentX = (p.initialX + p.speedX * progress) * width + sway
                val currentRotation = progress * p.rotationSpeed
                val flipScale = cos(progress * 15f + p.initialX * 5f)

                if (currentY in -20f..(height + 40f) && currentX in -20f..(width + 20f)) {
                    rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                        val particleColor = p.color.copy(alpha = globalAlpha)
                        when (p.shape) {
                            ConfettiShape.RECTANGLE -> {
                                drawRect(
                                    color = particleColor,
                                    topLeft = Offset(currentX - p.size / 2, currentY - p.size / 4 * flipScale),
                                    size = Size(p.size, (p.size * 0.55f * flipScale).coerceAtLeast(1f))
                                )
                            }
                            ConfettiShape.CIRCLE -> {
                                drawCircle(
                                    color = particleColor,
                                    radius = (p.size * 0.4f).coerceAtLeast(1f),
                                    center = Offset(currentX, currentY)
                                )
                            }
                            ConfettiShape.RIBBON -> {
                                drawRoundRect(
                                    color = particleColor,
                                    topLeft = Offset(currentX - p.size / 2, currentY - p.size * flipScale),
                                    size = Size(p.size * 0.4f, (p.size * 1.4f * flipScale).coerceAtLeast(1f)),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
