package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SubjectMasteryVicoChart
import com.example.ui.theme.*

data class TestScoreData(
    val testName: String,
    val date: String,
    val mathScore: Int,
    val physicalScienceScore: Int,
    val lifeScienceScore: Int,
    val averagePercentage: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDashboardScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val testHistory = remember {
        listOf(
            TestScoreData("মক টেস্ট ১", "০১ ফেব", 65, 70, 68, 68),
            TestScoreData("মক টেস্ট ২", "১০ ফেব", 72, 75, 78, 75),
            TestScoreData("মক টেস্ট ৩", "২০ ফেব", 80, 82, 85, 82),
            TestScoreData("ফাইনাল মক", "০১ মার্চ", 90, 88, 92, 90)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White)
                        }
                        Column {
                            Text(
                                text = "দারুণ অগ্রগতি! +২২% উন্নতি",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "আপনার শেষ ৪টি মক টেস্টের স্কোরে ধারাবাহিক উন্নতি লক্ষ্য করা গেছে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Calendar Heatmap View for Academic Year Study Consistency
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = PrimaryIndigoLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "বার্ষিক স্টাডি হিটম্যাপ (Study Consistency)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryIndigo.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "২০২৬ সেশন",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryIndigoLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "প্রতিদিনের প্র্যাকটিস ও পড়ালেখার ধারাবাহিকতা (GitHub স্টাইলের কন্ট্রিবিউশন গ্রিড)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Heatmap Grid Simulation (Months / Days)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val months = if (isBengali) {
                                listOf("জানু", "ফেব", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")
                            } else {
                                listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                            }

                            // Month labels row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                months.forEach { month ->
                                    Text(
                                        text = month,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 7 rows representing days of the week, with 26 weeks columns
                            val randomActivityLevels = remember {
                                (0 until 7 * 28).map { (0..4).random() }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (row in 0 until 7) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        for (col in 0 until 28) {
                                            val index = row * 28 + col
                                            val level = randomActivityLevels.getOrElse(index) { 0 }
                                            val cellColor = when (level) {
                                                0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                1 -> SecondaryEmerald.copy(alpha = 0.25f)
                                                2 -> SecondaryEmerald.copy(alpha = 0.50f)
                                                3 -> SecondaryEmerald.copy(alpha = 0.75f)
                                                else -> SecondaryEmerald
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(9.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(cellColor)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Legend
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "কম",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    SecondaryEmerald.copy(alpha = 0.25f),
                                    SecondaryEmerald.copy(alpha = 0.50f),
                                    SecondaryEmerald.copy(alpha = 0.75f),
                                    SecondaryEmerald
                                ).forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                                Text(
                                    text = "বেশি",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Line Chart Visualization Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বিষয়ভিত্তিক স্কোর গ্রাফ (Score Trends)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                LegendItem(color = PrimaryIndigoLight, label = "গণিত")
                                LegendItem(color = SecondaryEmerald, label = "ভৌতবিজ্ঞান")
                                LegendItem(color = ExamGold, label = "জীবনবিজ্ঞান")
                            }
                        }

                        // Custom D3/Recharts style Canvas Line Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            val scoresMath = testHistory.map { it.mathScore.toFloat() }
                            val scoresPhysical = testHistory.map { it.physicalScienceScore.toFloat() }
                            val scoresLife = testHistory.map { it.lifeScienceScore.toFloat() }
                            val labels = testHistory.map { it.testName }

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val stepX = width / (scoresMath.size - 1).coerceAtLeast(1)

                                // Draw grid lines
                                for (i in 0..4) {
                                    val y = height * (i / 4f)
                                    drawLine(
                                        color = Color.Gray.copy(alpha = 0.2f),
                                        start = Offset(0f, y),
                                        end = Offset(width, y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                // Helper function to draw lines
                                fun drawLineGraph(scores: List<Float>, color: Color) {
                                    val path = Path()
                                    scores.forEachIndexed { index, score ->
                                        val x = index * stepX
                                        val y = height - (score / 100f) * height
                                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                        drawCircle(color = color, radius = 5.dp.toPx(), center = Offset(x, y))
                                    }
                                    drawPath(
                                        path = path,
                                        color = color,
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }

                                drawLineGraph(scoresMath, PrimaryIndigoLight)
                                drawLineGraph(scoresPhysical, SecondaryEmerald)
                                drawLineGraph(scoresLife, ExamGold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            testHistory.forEach { test ->
                                Text(
                                    text = test.testName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Vico Subject Mastery Analytics Chart
            item {
                SubjectMasteryVicoChart(isBengali = isBengali)
            }

            // Detailed Test History List
            item {
                Text(
                    text = "বিস্তারিত টেস্ট হিস্ট্রি ও মার্কস:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(testHistory) { test ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = test.testName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "তারিখ: ${test.date} • গণিত: ${test.mathScore}%, ভৌত: ${test.physicalScienceScore}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SecondaryEmerald.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "গড় ${test.averagePercentage}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = SecondaryEmerald,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
