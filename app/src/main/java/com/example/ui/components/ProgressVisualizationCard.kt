package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class PerformancePoint(
    val sessionLabel: String,
    val score: Int, // 0 to 100
    val dateLabel: String
)

data class SubjectMasteryStat(
    val subjectName: String,
    val subjectNameEnglish: String,
    val masteryPercentage: Int,
    val improvementDelta: Int, // e.g. +12
    val color: Color
)

/**
 * Progress Visualization Component for the Profile Screen:
 * Inspired by D3/Recharts data visualization paradigms, built natively in Jetpack Compose:
 * - Smooth cubic bezier performance trend curve with gradient area fill
 * - Dynamic data point inspection on tap with tooltip
 * - Subject mastery multi-bar chart comparing all 7 WBBSE subjects
 * - Tabbed switcher between Performance Trend and Subject Mastery
 * - Exam Readiness Gauge
 */
@Composable
fun ProgressVisualizationCard(
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedChartTab by remember { mutableStateOf(0) } // 0: Over Time, 1: Subject Mastery
    var selectedPointIndex by remember { mutableStateOf<Int?>(5) } // default latest point

    // Sample historic performance points over time (Test 1 to 6)
    val trendPoints = remember {
        listOf(
            PerformancePoint("Test 1", 54, "১ম সপ্তাহ"),
            PerformancePoint("Test 2", 62, "২য় সপ্তাহ"),
            PerformancePoint("Test 3", 68, "৩য় সপ্তাহ"),
            PerformancePoint("Test 4", 75, "৪র্থ সপ্তাহ"),
            PerformancePoint("Test 5", 82, "৫ম সপ্তাহ"),
            PerformancePoint("Test 6", 89, "বর্তমান")
        )
    }

    // 7 Core WBBSE Subjects mastery statistics
    val subjectStats = remember {
        listOf(
            SubjectMasteryStat("ভৌতবিজ্ঞান", "Physical Science", 85, 14, Color(0xFF3B82F6)),
            SubjectMasteryStat("জীবনবিজ্ঞান", "Life Science", 92, 18, Color(0xFF10B981)),
            SubjectMasteryStat("গণিত", "Mathematics", 78, 12, Color(0xFFF59E0B)),
            SubjectMasteryStat("ইতিহাস", "History", 74, 9, Color(0xFFEC4899)),
            SubjectMasteryStat("ভূগোল", "Geography", 88, 15, Color(0xFF06B6D4)),
            SubjectMasteryStat("বাংলা", "Bengali", 90, 8, Color(0xFF8B5CF6)),
            SubjectMasteryStat("ইংরেজি", "English", 80, 11, Color(0xFF14B8A6))
        )
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("progress_visualization_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row with Title & Readiness Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "অগ্রগতি ও পারফরম্যান্স অ্যানালিটিক্স",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "বিষয়ভিত্তিক দক্ষতা ও সময়ের সাথে অগ্রগতি",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Growth badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CorrectGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Trending Up",
                            tint = CorrectGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+35% Growth",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CorrectGreen
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            // Tab Selector (Trend Line vs Subject Breakdown)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tab1Selected = selectedChartTab == 0
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (tab1Selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (tab1Selected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedChartTab = 0 }
                        .testTag("tab_trend_chart")
                ) {
                    Text(
                        text = "📈 স্কোর ট্র্যাকার",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (tab1Selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (tab1Selected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                val tab2Selected = selectedChartTab == 1
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (tab2Selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (tab2Selected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedChartTab = 1 }
                        .testTag("tab_subject_mastery")
                ) {
                    Text(
                        text = "📊 বিষয়ভিত্তিক দক্ষতা",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (tab2Selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (tab2Selected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            if (selectedChartTab == 0) {
                // Tab 1: Recharts/D3-Style Canvas Performance Trend Curve
                PerformanceTrendAreaChart(
                    points = trendPoints,
                    selectedPointIndex = selectedPointIndex,
                    onSelectPoint = { selectedPointIndex = it },
                    isBengali = isBengali
                )
            } else {
                // Tab 2: Vico Chart Subject Mastery Visual Component
                SubjectMasteryVicoChart(
                    isBengali = isBengali
                )
            }
        }
    }
}

/**
 * Performance Trend Area Chart:
 * Cubic bezier curve line chart with gradient fill, grid guidelines, data point nodes,
 * and interactive tap inspection.
 */
@Composable
private fun PerformanceTrendAreaChart(
    points: List<PerformancePoint>,
    selectedPointIndex: Int?,
    onSelectPoint: (Int) -> Unit,
    isBengali: Boolean
) {
    val currentSelected = selectedPointIndex?.let { points.getOrNull(it) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Selected Point Tooltip Banner
        currentSelected?.let { pt ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PrimaryIndigo.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${pt.sessionLabel} (${pt.dateLabel})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${pt.score}% নির্ভুলতা",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = PrimaryIndigo
                        )
                    )
                }
            }
        }

        // Main Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val pointSpacing = size.width / (points.size - 1).coerceAtLeast(1)
                            val closestIndex = (offset.x / pointSpacing).toInt().coerceIn(0, points.size - 1)
                            onSelectPoint(closestIndex)
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val minScore = 40f
                val maxScore = 100f
                val pointSpacing = width / (points.size - 1).coerceAtLeast(1)

                // 1. Draw horizontal benchmark grid lines (60% and 80%)
                listOf(60f, 80f, 100f).forEach { scoreVal ->
                    val y = height - ((scoreVal - minScore) / (maxScore - minScore)) * height
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                // 2. Build Cubic Bezier Path
                val path = Path()
                val fillPath = Path()
                val pointOffsets = mutableListOf<Offset>()

                points.forEachIndexed { i, pt ->
                    val x = i * pointSpacing
                    val y = height - ((pt.score - minScore) / (maxScore - minScore)) * height
                    pointOffsets.add(Offset(x, y))

                    if (i == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        val prev = pointOffsets[i - 1]
                        val cx1 = prev.x + (x - prev.x) / 2
                        val cy1 = prev.y
                        val cx2 = prev.x + (x - prev.x) / 2
                        val cy2 = y
                        path.cubicTo(cx1, cy1, cx2, cy2, x, y)
                        fillPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                    }
                }

                fillPath.lineTo(pointOffsets.last().x, height)
                fillPath.close()

                // Draw Gradient Fill under line (Area Chart effect)
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            PrimaryIndigo.copy(alpha = 0.35f),
                            PrimaryIndigo.copy(alpha = 0.02f)
                        )
                    )
                )

                // Draw Main Line
                drawPath(
                    path = path,
                    color = PrimaryIndigo,
                    style = Stroke(
                        width = 3.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw Point Dots
                pointOffsets.forEachIndexed { i, offset ->
                    val isSelected = selectedPointIndex == i
                    // Outer glow/ring for selected point
                    if (isSelected) {
                        drawCircle(
                            color = PrimaryIndigo.copy(alpha = 0.25f),
                            radius = 10.dp.toPx(),
                            center = offset
                        )
                    }
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = if (isSelected) ExamGold else PrimaryIndigo,
                        radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                        center = offset
                    )
                }
            }
        }

        // X-Axis Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEachIndexed { i, pt ->
                Text(
                    text = pt.sessionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (selectedPointIndex == i) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (selectedPointIndex == i) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Subject Mastery Bar Breakdown:
 * Compares student mastery score across all 7 WBBSE subjects with horizontal animated progress bars,
 * delta improvement chips, and subject color signatures.
 */
@Composable
private fun SubjectMasteryBarBreakdown(
    stats: List<SubjectMasteryStat>,
    isBengali: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        stats.forEach { stat ->
            val animatedProgress by animateFloatAsState(
                targetValue = stat.masteryPercentage / 100f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                label = "mastery_progress_${stat.subjectNameEnglish}"
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(stat.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) stat.subjectName else stat.subjectNameEnglish,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stat.masteryPercentage}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "+${stat.improvementDelta}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = CorrectGreen
                            )
                        )
                    }
                }

                // Horizontal Custom Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(stat.color.copy(alpha = 0.8f), stat.color)
                                )
                            )
                    )
                }
            }
        }
    }
}
