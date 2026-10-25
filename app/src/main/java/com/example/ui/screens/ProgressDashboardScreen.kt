package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectItem
import com.example.ui.components.SubjectMasteryVicoChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel

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
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Test history is not persisted by the current data layer; never show sample scores.
    val testHistory = emptyList<TestScoreData>()

    val liveSubjects by viewModel.subjects.collectAsState()
    val liveChapters by viewModel.chapters.collectAsState()

    var expandedSubjectId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isBengali) "অগ্রগতি ড্যাশবোর্ড" else "Progress Dashboard", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryIndigo,
                                        Color(0xFF2563EB),
                                        Color(0xFF0EA5E9)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                val totalCompleted = liveSubjects.sumOf { it.completedChapters }
                                val totalChaptersCount = liveSubjects.sumOf { it.totalChapters }
                                val overallProgressPercent = if (totalChaptersCount > 0) (totalCompleted * 100) / totalChaptersCount else 0

                                Text(
                                    text = if (isBengali) "দারুণ অগ্রগতি! $overallProgressPercent% সিলেবাস সম্পন্ন" else "Great Progress! $overallProgressPercent% Syllabus Completed",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = if (isBengali)
                                        "আপনার মোট $totalChaptersCount টি অধ্যায়ের মধ্যে $totalCompleted টি অধ্যায় সম্পূর্ণ প্রস্তুতি হয়েছে।"
                                    else
                                        "Out of $totalChaptersCount chapters, you have completed $totalCompleted successfully.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // CORE REQUIREMENT: INTERACTIVE SUBJECT PROGRESS CARDS SECTION
            item {
                Text(
                    text = if (isBengali) "📚 বিষয়ভিত্তিক সিলেবাস অগ্রগতি:" else "📚 Subject Syllabus Progress:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (liveSubjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBengali) "কোনো বিষয় খুঁজে পাওয়া যায়নি।" else "No subjects found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(liveSubjects, key = { it.id }) { subject ->
                    val isExpanded = expandedSubjectId == subject.id
                    val progressFloat = if (subject.totalChapters > 0) subject.completedChapters.toFloat() / subject.totalChapters.toFloat() else 0f
                    val displayProgressPercent = (progressFloat * 100).toInt()
                    val themeColor = Color(subject.colorHex)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            width = if (isExpanded) 2.dp else 1.dp,
                            color = if (isExpanded) themeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedSubjectId = if (isExpanded) null else subject.id
                                if (!isExpanded) {
                                    viewModel.selectSubject(subject.id)
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Subject Primary Details Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Circular badge with Color
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(themeColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = subject.nameBengali.take(1),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = themeColor
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = if (isBengali) subject.nameBengali else subject.nameEnglish,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (subject.textbookName.isNotBlank()) {
                                            Text(
                                                text = subject.textbookName,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = themeColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (isBengali) "$displayProgressPercent% সম্পূর্ণ" else "$displayProgressPercent% Done",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = themeColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Custom horizontal syllabus progress bar
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBengali) 
                                            "${subject.completedChapters}/${subject.totalChapters} অধ্যায় সম্পন্ন" 
                                        else 
                                            "${subject.completedChapters}/${subject.totalChapters} Chapters Completed",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (isBengali) "মাস্টারি: ${subject.masteryPercentage}%" else "Mastery: ${subject.masteryPercentage}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (subject.masteryPercentage >= 70) CorrectGreen else Color(0xFFD97706)
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { progressFloat },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = themeColor,
                                    trackColor = themeColor.copy(alpha = 0.15f)
                                )
                            }

                            // Expanded Chapter list
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isBengali) "📖 অধ্যায়ের তালিকা (ক্লিক করে সম্পন্ন চিহ্নিত করুন):" else "📖 Chapters List (Click to mark as done):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = themeColor,
                                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                                    )

                                    if (liveChapters.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = themeColor)
                                        }
                                    } else {
                                        liveChapters.forEach { chapter ->
                                            val isChapterDone = chapter.bossCompleted || chapter.masteryPercentage >= 100
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(
                                                    0.5.dp,
                                                    if (isChapterDone) CorrectGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.toggleChapterCompletion(
                                                            chapterId = chapter.id,
                                                            currentStatus = isChapterDone,
                                                            subjectId = subject.id
                                                        )
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = if (isChapterDone) CorrectGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                                            modifier = Modifier.size(26.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text(
                                                                    text = "${chapter.chapterNumber}",
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 11.sp,
                                                                    color = if (isChapterDone) CorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                        Column {
                                                            Text(
                                                                text = if (isBengali) chapter.titleBengali else chapter.titleEnglish,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = if (isChapterDone) CorrectGreen else MaterialTheme.colorScheme.onSurface,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (chapter.descriptionBengali.isNotBlank()) {
                                                                Text(
                                                                    text = if (isBengali) chapter.descriptionBengali else chapter.descriptionEnglish,
                                                                    fontSize = 10.sp,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Checkbox(
                                                        checked = isChapterDone,
                                                        onCheckedChange = {
                                                            viewModel.toggleChapterCompletion(
                                                                chapterId = chapter.id,
                                                                currentStatus = isChapterDone,
                                                                subjectId = subject.id
                                                            )
                                                        },
                                                        colors = CheckboxDefaults.colors(
                                                            checkedColor = CorrectGreen,
                                                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        ),
                                                        modifier = Modifier.size(24.dp)
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
            }

            // Calendar Heatmap View for Academic Year Study Consistency
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
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
                                    text = if (isBengali) "বার্ষিক স্টাডি হিটম্যাপ" else "Annual Study Heatmap",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryIndigo.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isBengali) "২০২৬ সেশন" else "2026 Session",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryIndigoLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isBengali) "প্রতিদিনের প্র্যাকটিস ও পড়ালেখার ধারাবাহিকতা" else "Daily practice and consistency map",
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
                                    text = if (isBengali) "কম" else "Less",
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
                                    text = if (isBengali) "বেশি" else "More",
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
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBengali) "বিষয়ভিত্তিক স্কোর গ্রাফ" else "Subject Score Trends",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LegendItem(color = PrimaryIndigoLight, label = if (isBengali) "গণিত" else "Math")
                                LegendItem(color = SecondaryEmerald, label = if (isBengali) "ভৌত" else "Phy")
                                LegendItem(color = ExamGold, label = if (isBengali) "জীবন" else "Life")
                            }
                        }

                        // Custom canvas Line Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            val scoresMath = testHistory.map { it.mathScore.toFloat() }
                            val scoresPhysical = testHistory.map { it.physicalScienceScore.toFloat() }
                            val scoresLife = testHistory.map { it.lifeScienceScore.toFloat() }

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val stepX = width / (scoresMath.size - 1).coerceAtLeast(1)

                                // Draw grid lines
                                for (i in 0..4) {
                                    val y = height * (i / 4f)
                                    drawLine(
                                        color = Color.Gray.copy(alpha = 0.15f),
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
                                        drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x, y))
                                    }
                                    drawPath(
                                        path = path,
                                        color = color,
                                        style = Stroke(width = 2.5.dp.toPx())
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
                    text = if (isBengali) "📊 বিস্তারিত টেস্ট হিস্ট্রি ও মার্কস:" else "📊 Detailed Test History:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(testHistory) { test ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
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
                                text = if (isBengali) 
                                    "তারিখ: ${test.date} • গণিত: ${test.mathScore}%, ভৌতবিজ্ঞান: ${test.physicalScienceScore}%"
                                else
                                    "Date: ${test.date} • Math: ${test.mathScore}%, Physical Sci: ${test.physicalScienceScore}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SecondaryEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isBengali) "গড় ${test.averagePercentage}%" else "Avg ${test.averagePercentage}%",
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
