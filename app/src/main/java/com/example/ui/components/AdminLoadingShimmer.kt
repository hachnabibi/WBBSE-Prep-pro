package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExamGold

/**
 * Custom Shimmer Effect Modifier for Compose.
 * Creates an animated linear gradient highlight sweep across components.
 */
fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(8.dp),
    shimmerColors: List<Color>? = null
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val isDark = MaterialTheme.colorScheme.surface.let { color ->
        val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
        luminance < 0.5
    }

    val defaultColors = if (isDark) {
        listOf(
            Color(0xFF2A273C).copy(alpha = 0.6f),
            Color(0xFF3F3B56).copy(alpha = 0.9f),
            Color(0xFF2A273C).copy(alpha = 0.6f)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0).copy(alpha = 0.7f),
            Color(0xFFF8FAFC),
            Color(0xFFE2E8F0).copy(alpha = 0.7f)
        )
    }

    val brush = Brush.linearGradient(
        colors = shimmerColors ?: defaultColors,
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim, translateAnim)
    )

    this
        .clip(shape)
        .background(brush)
}

/**
 * Basic Shimmer Box Component
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    width: Dp? = null,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    val mod = if (width != null) {
        modifier.size(width = width, height = height)
    } else {
        modifier
            .fillMaxWidth()
            .height(height)
    }
    Box(modifier = mod.shimmerEffect(shape = shape))
}

/**
 * Animated Linear Gradient Progress Bar for Firestore Fetching
 */
@Composable
fun AdminLinearGradientProgress(
    modifier: Modifier = Modifier,
    isBengali: Boolean = false,
    statusText: String? = null
) {
    val transition = rememberInfiniteTransition(label = "linear_progress_transition")
    val offsetFraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress_sweep"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (statusText != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = ExamGold
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ExamGold
                        )
                    )
                }
                Text(
                    text = "লাইভ ক্লাউড",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            val progressBrush = Brush.horizontalGradient(
                colors = listOf(
                    ExamGold.copy(alpha = 0.2f),
                    ExamGold,
                    MaterialTheme.colorScheme.primary,
                    ExamGold.copy(alpha = 0.2f)
                ),
                startX = offsetFraction * 800f,
                endX = (offsetFraction * 800f) + 600f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(progressBrush)
            )
        }
    }
}

/**
 * Cloud Loading Card with rotating icon & pulsating indicator
 */
@Composable
fun AdminCloudLoadingCard(
    isBengali: Boolean,
    message: String = "ফায়ারবেস ক্লাউড থেকে ডেটা আনা হচ্ছে...",
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = ExamGold.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = ExamGold,
                        strokeWidth = 3.dp
                    )
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = ExamGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ক্লাউড সিঙ্ক চালু আছে",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Shimmering Question Item Skeleton for the Question CMS Tab
 */
@Composable
fun QuestionItemShimmerSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: ID Badge & Difficulty Chip Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(width = 110.dp, height = 22.dp, shape = RoundedCornerShape(6.dp))
                ShimmerBox(width = 65.dp, height = 22.dp, shape = RoundedCornerShape(6.dp))
            }

            // Row 2: Subject & Class Badges Skeleton
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(width = 85.dp, height = 20.dp, shape = RoundedCornerShape(6.dp))
                ShimmerBox(width = 75.dp, height = 20.dp, shape = RoundedCornerShape(6.dp))
                ShimmerBox(width = 70.dp, height = 20.dp, shape = RoundedCornerShape(6.dp))
            }

            // Question Text Skeleton (2 lines)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerBox(height = 16.dp, shape = RoundedCornerShape(4.dp))
                ShimmerBox(width = 240.dp, height = 16.dp, shape = RoundedCornerShape(4.dp))
            }

            // 4 Option Skeletons
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) {
                    ShimmerBox(height = 34.dp, shape = RoundedCornerShape(8.dp))
                }
            }

            // Explanation Skeleton
            ShimmerBox(width = 200.dp, height = 14.dp, shape = RoundedCornerShape(4.dp))

            // Action Buttons Skeleton (2 equal width buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShimmerBox(modifier = Modifier.weight(1f), height = 44.dp, shape = RoundedCornerShape(10.dp))
                ShimmerBox(modifier = Modifier.weight(1f), height = 44.dp, shape = RoundedCornerShape(10.dp))
            }
        }
    }
}

/**
 * Question List Shimmer Skeleton (Shows multiple question card placeholders)
 */
@Composable
fun QuestionListShimmerSkeleton(
    count: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(count) {
            QuestionItemShimmerSkeleton()
        }
    }
}

/**
 * Shimmering Test Template Item Skeleton for the Test CMS Tab
 */
@Composable
fun TestTemplateItemShimmerSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title & Test Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(width = 180.dp, height = 22.dp, shape = RoundedCornerShape(6.dp))
                ShimmerBox(width = 80.dp, height = 22.dp, shape = RoundedCornerShape(6.dp))
            }

            // Metadata Chips (Duration, Marks, Questions)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(width = 70.dp, height = 18.dp, shape = RoundedCornerShape(4.dp))
                ShimmerBox(width = 70.dp, height = 18.dp, shape = RoundedCornerShape(4.dp))
                ShimmerBox(width = 70.dp, height = 18.dp, shape = RoundedCornerShape(4.dp))
            }

            // Test ID
            ShimmerBox(width = 120.dp, height = 14.dp, shape = RoundedCornerShape(4.dp))

            // Delete Action Button
            ShimmerBox(height = 42.dp, shape = RoundedCornerShape(10.dp))
        }
    }
}

/**
 * Test Template List Shimmer Skeleton (Shows multiple test template placeholders)
 */
@Composable
fun TestTemplateListShimmerSkeleton(
    count: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(count) {
            TestTemplateItemShimmerSkeleton()
        }
    }
}
