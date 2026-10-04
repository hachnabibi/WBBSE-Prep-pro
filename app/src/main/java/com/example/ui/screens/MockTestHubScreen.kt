package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssessmentMode
import com.example.ui.viewmodel.AppScreen
import com.example.data.model.AcademicTestItem
import com.example.data.model.AcademicTestSeriesProvider
import com.example.data.model.TestType
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.components.SubjectSvgIconProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel

private fun Int.toBengaliDigits(): String {
    val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return this.toString().map { if (it in '0'..'9') bengaliDigits[it - '0'] else it }.joinToString("")
}

@Composable
fun MockTestHubScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onStartMockTest: (AssessmentMode, String?, String?) -> Unit,
    onOpenSubjectDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val firestoreTestTemplates by viewModel.firestoreTestTemplates.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchFirestoreTestTemplates()
    }

    val subjectMockScores = studentProfile.subjectMockScores
    val passingThresholds = listOf(40, 45, 50, 55, 60, 65, 70)
    var expandedSubjectId by remember { mutableStateOf<String?>(null) }
    var testFilterType by remember { mutableStateOf<TestType?>(null) } // null = All, UNIT_TEST, MOCK_TEST
    var selectedSubjectCodeFilter by remember { mutableStateOf<com.example.data.model.SubjectCode?>(null) }

    val studentClassId = studentProfile.classId
    val filteredFirestoreTemplates = remember(firestoreTestTemplates, studentClassId) {
        val cid = if (studentClassId.isNotBlank()) studentClassId else "class_10_wbbse"
        firestoreTestTemplates.filter { it.classId.lowercase().trim() == cid.lowercase().trim() }
    }

    val currentClass = remember(studentProfile.classId) {
        com.example.data.model.AcademicClassProvider.ALL_CLASSES.find { it.id == studentProfile.classId }
    }
    val className = currentClass?.name ?: ("দশম শ্রেণি")

    val classTests = remember(studentProfile.classId, subjects, isBengali) {
        AcademicTestSeriesProvider.getTestsForClass(studentProfile.classId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("mock_test_hub_screen")
    ) {
        ScreenHeaderBar(
            title = "মক টেস্ট হাব",
            subtitle = "$className - অধ্যায় ও বিষয়ভিত্তিক পূর্ণাঙ্গ টেস্ট",
            isBengali = isBengali,
            showBackButton = true,
            onBack = { viewModel.goBack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Edge-to-Edge Header Hero Banner (0dp Corner Radius)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF3730A3)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ExamGold.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Quiz,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$className • WBBSE মডেল টেস্ট হাব ২০২৬",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ExamGold
                                    )
                                }
                            }

                            // Quick Start Grand Mock Button
                            Button(
                                onClick = {
                                    onStartMockTest(
                                        AssessmentMode.MOCK_TEST,
                                        null,
                                        "$className - পূর্ণাঙ্গ গ্র্যান্ড মক টেস্ট"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ExamGold,
                                    contentColor = Color(0xFF78350F)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("hero_grand_mock_cta")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "গ্র্যান্ড মক",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "পরীক্ষার আসল প্রস্তুতি ও লাইভ মূল্যায়ন",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                            Text(
                                text = "সময়ভিত্তিক ৯০ নম্বরের পূর্ণাঙ্গ টেস্ট দিন, মেধা যাচাই করুন এবং দুর্বল বিষয়গুলোতে নিজের প্রস্তুতি জোরদার করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }

                        // Stats Highlights Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${subjects.size}টি বিষয়",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Grade,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "৯০ নম্বর",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = ExamCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "আসল টাইমার",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Live Mock Test Banner (Connecting to the Live Mock Test Hall)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE11D48).copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { viewModel.navigateTo(AppScreen.LIVE_MOCK_TEST) }
                        .testTag("live_mock_test_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE11D48).copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "লাইভ মক টেস্ট এরিনা",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "লাইভ",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFEF4444),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "আসল পরীক্ষার টাইমার ও অল-বেঙ্গল লাইভ মেরিট লিস্ট",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 3. Section Title Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "বিষয়ভিত্তিক পূর্ণাঙ্গ প্রস্তুতি",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "আপনার সিলেবাসের প্রতিটি বিষয়ের অধ্যায় ও ৯০ নম্বরের মক টেস্ট শুরু করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. List of subject cards
            items(subjects) { sub ->
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val userScore = subjectMockScores[sub.id]
                val hasScore = userScore != null && userScore > 0
                val chaptersCount = if (sub.totalChapters > 0) sub.totalChapters else 8
                val scorePercent = if (hasScore) ((userScore.toFloat() / 90f) * 100).toInt().coerceIn(0, 100) else 0
                val progressFloat = if (hasScore) (userScore.toFloat() / 90f).coerceIn(0f, 1f) else (sub.completedChapters.toFloat() / chaptersCount.toFloat()).coerceIn(0f, 1f)
                val baseColor = Color(sub.colorHex)

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else baseColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                        .clickable { onOpenSubjectDetail?.invoke(sub.id) }
                        .testTag("subject_card_${sub.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Top Row: Subject Icon + Title & Subtitle + Circular Progress / Score Ring
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(baseColor.copy(alpha = if (isDark) 0.35f else 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = SubjectSvgIconProvider.getIconForSubjectCode(sub.code),
                                        contentDescription = sub.nameBengali,
                                        tint = if (isDark) Color.White else baseColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBengali) sub.nameBengali else sub.nameEnglish,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = sub.textbookName ?: "WBBSE মাধ্যমিক সিলেবাস",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Circular Progress / Score Ring (32.dp)
                            val ringProgress = if (hasScore) (userScore.toFloat() / 90f).coerceIn(0f, 1f) else progressFloat
                            val ringColor = when {
                                hasScore && scorePercent >= 80 -> Color(0xFF10B981) // Emerald Green
                                hasScore && scorePercent >= 50 -> Color(0xFF3B82F6) // Blue
                                hasScore -> Color(0xFFF59E0B)  // Amber
                                progressFloat > 0 -> baseColor
                                else -> if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1) // Slate
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                    strokeWidth = 3.dp,
                                    strokeCap = StrokeCap.Round
                                )
                                CircularProgressIndicator(
                                    progress = { ringProgress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = ringColor,
                                    strokeWidth = 3.dp,
                                    strokeCap = StrokeCap.Round
                                )
                                Text(
                                    text = if (hasScore) "${userScore.toBengaliDigits()}" else "${(progressFloat * 100).toInt().toBengaliDigits()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    ),
                                    color = if (hasScore || progressFloat > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 2. Middle Row: Metadata Chips (Chapters, Marks, Time, Best Score / XP)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Chapters count chip
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = if (isDark) Color(0xFF1E3A5F) else Color(0xFFEFF6FF)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${chaptersCount.toBengaliDigits()}টি অধ্যায়",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                        color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                                    )
                                }
                            }

                            // Marks chip
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = if (isDark) Color(0xFF3730A3).copy(alpha = 0.4f) else Color(0xFFF5F3FF)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Quiz,
                                        contentDescription = null,
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "৯০ মার্কস",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                        color = if (isDark) Color(0xFFC4B5FD) else Color(0xFF6D28D9)
                                    )
                                }
                            }

                            // Exam Time chip
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = if (isDark) Color(0xFF451A03).copy(alpha = 0.4f) else Color(0xFFFFF7ED)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "৩ঘণ্টা ১৫মি.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                        color = if (isDark) Color(0xFFFED7AA) else Color(0xFFC2410C)
                                    )
                                }
                            }

                            // Score or XP chip
                            if (hasScore) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFECFDF5)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "সেরা: ${userScore.toBengaliDigits()}/৯০",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = if (isDark) Color(0xFFA7F3D0) else Color(0xFF047857)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = if (isDark) Color(0xFF451A03).copy(alpha = 0.5f) else Color(0xFFFFFBEB)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "+৫০০ XP",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Bottom Row: 3 Action Chip Buttons (Chapters, Unit Tests, Start Mock)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 📖 Chapters Button
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = if (isDark) Color(0xFF1E3A5F).copy(alpha = 0.4f) else Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { onOpenSubjectDetail?.invoke(sub.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = "Chapters",
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "অধ্যায়সমূহ",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                        color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                                    )
                                }
                            }

                            // 📝 Unit Test Button
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = if (isDark) Color(0xFF134E4A).copy(alpha = 0.4f) else Color(0xFFF0FDFA),
                                border = BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { onOpenSubjectDetail?.invoke(sub.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Quiz,
                                        contentDescription = "Unit Tests",
                                        tint = Color(0xFF0D9488),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "ইউনিট টেস্ট",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                        color = if (isDark) Color(0xFF99F6E4) else Color(0xFF0F766E)
                                    )
                                }
                            }

                            // 🚀 Start Mock Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(baseColor, baseColor.copy(alpha = 0.85f))
                                        )
                                    )
                                    .clickable {
                                        onStartMockTest(
                                            AssessmentMode.MOCK_TEST,
                                            sub.id,
                                            "${if (isBengali) sub.nameBengali else sub.nameEnglish} পূর্ণাঙ্গ মক টেস্ট"
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start Mock",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "মক টেস্ট শুরু",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredFirestoreTemplates.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🔥 ফায়ারবেস ক্লাউড মক টেস্ট ও লাইভ এক্সাম",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ফায়ারবেসে সরাসরি সংরক্ষিত ও নতুন যুক্ত হওয়া পরীক্ষা কুইজসমূহ:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(filteredFirestoreTemplates) { test ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = test.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = test.testType,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = "${test.durationMinutes} মিনিট",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(Icons.Default.Grade, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = "${test.totalMarks} নম্বর",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = "${test.questionCount.coerceAtLeast(test.questions.size)}টি প্রশ্ন",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onStartMockTest(
                                        AssessmentMode.MOCK_TEST,
                                        test.id,
                                        test.title
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("পরীক্ষা শুরু করুন", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
    }
}
}
