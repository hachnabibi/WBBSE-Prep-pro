package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

data class WbbseSubjectPerformance(
    val id: String,
    val name: String = "",
    val nameBengali: String = "",
    val nameEnglish: String = "",
    val masteryPercentage: Int, // 0 to 100
    val quizScorePercentage: Int,
    val mockTestScorePercentage: Int,
    val totalQuizzesTaken: Int,
    val totalMockTestsTaken: Int,
    val accentColor: Color
) {
    val displayNameBengali: String get() = nameBengali.ifBlank { name.ifBlank { nameEnglish } }
    val displayNameEnglish: String get() = nameEnglish.ifBlank { name.ifBlank { nameBengali } }
}

@Composable
fun SubjectMasteryVicoChart(
    isBengali: Boolean,
    modifier: Modifier = Modifier,
    subjectPerformances: List<WbbseSubjectPerformance> = defaultWbbseSubjects()
) {
    var selectedViewMode by remember { mutableStateOf(0) } // 0 = Mastery Bar Chart, 1 = Score Breakdown Trend
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(selectedViewMode, subjectPerformances) {
        modelProducer.runTransaction {
            if (selectedViewMode == 0) {
                // Column chart of subject mastery %
                columnSeries {
                    series(subjectPerformances.map { it.masteryPercentage.toFloat() })
                }
            } else {
                // Dual line series: Quiz score % vs Mock test score %
                lineSeries {
                    series(subjectPerformances.map { it.quizScorePercentage.toFloat() })
                    series(subjectPerformances.map { it.mockTestScorePercentage.toFloat() })
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_mastery_vico_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryIndigo.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Mastery Analytics",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isBengali) "বিষয়ভিত্তিক দক্ষতা চার্ট" else "Subject Mastery Chart",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBengali) "মক টেস্ট ও কুইজের সামগ্রিক ফলাফল বিশ্লেষণ" else "Performance analysis from mock tests & quizzes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryIndigo.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(14.dp)
                        )
                        val avgMastery = if (subjectPerformances.isNotEmpty()) {
                            subjectPerformances.map { it.masteryPercentage }.average().toInt()
                        } else 0
                        Text(
                            text = "$avgMastery% Avg",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigo
                            )
                        )
                    }
                }
            }

            // Mode Selector Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isBarSelected = selectedViewMode == 0
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isBarSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isBarSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedViewMode = 0 }
                        .testTag("vico_mode_bars")
                ) {
                    Text(
                        text = "📊 বিষয়ভিত্তিক দক্ষতা (Bar)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isBarSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isBarSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                val isLineSelected = selectedViewMode == 1
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLineSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isLineSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedViewMode = 1 }
                        .testTag("vico_mode_lines")
                ) {
                    Text(
                        text = "📈 কুইজ বনাম মক টেস্ট (Line)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isLineSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isLineSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Vico Chart Render Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            ) {
                val chart = rememberCartesianChart(
                    if (selectedViewMode == 0) {
                        rememberColumnCartesianLayer()
                    } else {
                        rememberLineCartesianLayer()
                    },
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis()
                )

                CartesianChartHost(
                    chart = chart,
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Legend / Chart Indicators
            if (selectedViewMode == 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo)
                        )
                        Text(
                            text = "কুইজ পারফরম্যান্স (%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(SecondaryEmerald)
                        )
                        Text(
                            text = "মক টেস্ট স্কোর (%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Interactive Subject Chips with Mastery Badges
            Text(
                text = "বিষয়ভিত্তিক বিস্তারিত পরিসংখ্যান (ট্যাপ করুন):",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subjectPerformances.forEach { subject ->
                    val isExpanded = selectedSubjectId == subject.id
                    val statusText = when {
                        subject.masteryPercentage >= 85 -> "চমৎকার দক্ষতা"
                        subject.masteryPercentage >= 70 -> "ভালো অগ্রগতি"
                        else -> "পুনরাবৃত্তি প্রয়োজন"
                    }
                    val statusColor = when {
                        subject.masteryPercentage >= 85 -> CorrectGreen
                        subject.masteryPercentage >= 70 -> ExamGold
                        else -> MaterialTheme.colorScheme.error
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isExpanded) subject.accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isExpanded) subject.accentColor else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSubjectId = if (isExpanded) null else subject.id
                            }
                            .testTag("subject_mastery_item_${subject.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(subject.accentColor)
                                    )
                                    Text(
                                        text = if (isBengali) subject.displayNameBengali else subject.displayNameEnglish,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Text(
                                        text = "${subject.masteryPercentage}%",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = subject.accentColor
                                        )
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "কুইজ নির্ভুলতা",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${subject.quizScorePercentage}% (${subject.totalQuizzesTaken} টি সম্পন্ন)",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "মক টেস্ট গড়",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${subject.mockTestScorePercentage}% (${subject.totalMockTestsTaken} টি মক টেস্ট)",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    // Custom Linear Progress Bar for Master level
                                    LinearProgressIndicator(
                                        progress = { subject.masteryPercentage / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = subject.accentColor,
                                        trackColor = subject.accentColor.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun defaultWbbseSubjects(): List<WbbseSubjectPerformance> {
    return listOf(
        WbbseSubjectPerformance(
            id = "math",
            nameBengali = "গণিত",
            nameEnglish = "Mathematics",
            masteryPercentage = 82,
            quizScorePercentage = 85,
            mockTestScorePercentage = 78,
            totalQuizzesTaken = 24,
            totalMockTestsTaken = 5,
            accentColor = Color(0xFFF59E0B)
        ),
        WbbseSubjectPerformance(
            id = "pscience",
            nameBengali = "ভৌতবিজ্ঞান",
            nameEnglish = "Physical Science",
            masteryPercentage = 88,
            quizScorePercentage = 90,
            mockTestScorePercentage = 86,
            totalQuizzesTaken = 30,
            totalMockTestsTaken = 6,
            accentColor = Color(0xFF3B82F6)
        ),
        WbbseSubjectPerformance(
            id = "lscience",
            nameBengali = "জীবনবিজ্ঞান",
            nameEnglish = "Life Science",
            masteryPercentage = 94,
            quizScorePercentage = 96,
            mockTestScorePercentage = 92,
            totalQuizzesTaken = 35,
            totalMockTestsTaken = 7,
            accentColor = Color(0xFF10B981)
        ),
        WbbseSubjectPerformance(
            id = "history",
            nameBengali = "ইতিহাস",
            nameEnglish = "History",
            masteryPercentage = 75,
            quizScorePercentage = 78,
            mockTestScorePercentage = 72,
            totalQuizzesTaken = 18,
            totalMockTestsTaken = 4,
            accentColor = Color(0xFFEC4899)
        ),
        WbbseSubjectPerformance(
            id = "geography",
            nameBengali = "ভূগোল",
            nameEnglish = "Geography",
            masteryPercentage = 89,
            quizScorePercentage = 91,
            mockTestScorePercentage = 87,
            totalQuizzesTaken = 22,
            totalMockTestsTaken = 5,
            accentColor = Color(0xFF06B6D4)
        ),
        WbbseSubjectPerformance(
            id = "bengali",
            nameBengali = "বাংলা",
            nameEnglish = "Bengali",
            masteryPercentage = 91,
            quizScorePercentage = 93,
            mockTestScorePercentage = 89,
            totalQuizzesTaken = 28,
            totalMockTestsTaken = 6,
            accentColor = Color(0xFF8B5CF6)
        ),
        WbbseSubjectPerformance(
            id = "english",
            nameBengali = "ইংরেজি",
            nameEnglish = "English",
            masteryPercentage = 80,
            quizScorePercentage = 82,
            mockTestScorePercentage = 78,
            totalQuizzesTaken = 20,
            totalMockTestsTaken = 4,
            accentColor = Color(0xFF14B8A6)
        )
    )
}
