package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChapterEntity
import com.example.data.local.QuestionEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TopicEntity
import com.example.ui.components.HeroBanner
import com.example.data.model.*
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.theme.*

private fun Int.toBengaliDigits(): String {
    val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return this.toString().map { if (it in '0'..'9') bengaliDigits[it - '0'] else it }.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subject: SubjectEntity,
    chapters: List<ChapterEntity>,
    topics: List<TopicEntity>,
    questions: List<QuestionEntity>,
    isBengali: Boolean,
    onBack: () -> Unit,
    onOpenChapterDetail: (ChapterEntity) -> Unit,
    onStartAssessment: (AssessmentMode, String, String) -> Unit,
    onLearnTopic: (String, String) -> Unit,
    getTestScore: ((String) -> Int)? = null,
    modifier: Modifier = Modifier,
    viewModel: com.example.ui.viewmodel.DeshExamViewModel? = null
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Chapters, 1: Tests, 2: PYQs, 3: Key Notes
    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val subjectColor = Color(subject.colorHex)

    val firestoreTestTemplates by (viewModel?.firestoreTestTemplates?.collectAsState() ?: remember { mutableStateOf(emptyList()) })

    LaunchedEffect(subject.id) {
        viewModel?.fetchFirestoreTestTemplates(subject.classId, subject.id)
    }

    val attachedFirestoreTests = remember(firestoreTestTemplates, subject) {
        firestoreTestTemplates.filter { test ->
            val sId = subject.id.lowercase().trim()
            val sCode = subject.code.lowercase().trim()
            val testSubId = (test.subjectId ?: "").lowercase().trim()
            val testId = test.id.lowercase().trim()

            val matchesDirectSubject = testSubId.isNotBlank() && (testSubId == sId || testSubId == sCode || testSubId.contains(sId) || sId.contains(testSubId) || (sCode.length > 2 && testSubId.contains(sCode)))
            val matchesTestId = (sId.length > 2 && testId.contains(sId)) || (sCode.length > 2 && testId.contains(sCode))
            val matchesQuestions = test.questions.any { q ->
                val qSub = q.subjectId.lowercase().trim()
                qSub.isNotBlank() && (qSub == sId || qSub == sCode || qSub.contains(sId) || sId.contains(qSub) || (sCode.length > 2 && qSub.contains(sCode)))
            }

            matchesDirectSubject || matchesTestId || matchesQuestions
        }
    }

    val subjectCodeEnum = remember(subject.code) {
        SubjectCode.values().find { it.id == subject.code } ?: SubjectCode.PHYSICAL_SCIENCE
    }

    val subjectTests = remember(subjectCodeEnum, subject, isBengali) {
        AcademicTestSeriesProvider.getTestsForClassAndSubject(subject.classId, subjectCodeEnum)
    }

    val subjectPyqs = remember(questions, subject) {
        questions.filter { it.subjectId == subject.id && it.pyqYear.isNotBlank() }
    }

    val textbookSubtitle = remember(subject, isBengali) {
        val tb = subject.textbookName
        if (!tb.isNullOrBlank()) {
            if (isBengali) "পাঠ্যবই: $tb" else "Textbook: $tb"
        } else {
            val code = subject.code.uppercase()
            when {
                code.contains("BEN") || code.contains("BN") -> if (isBengali) "পাঠ্যবই: পাতাবাহার / সাহিত্য সঞ্চয়ন" else "Textbook: Patabahar"
                code.contains("ENG") || code.contains("EN") -> if (isBengali) "পাঠ্যবই: বাটারফ্লাই (Butterfly)" else "Textbook: Butterfly"
                code.contains("MATH") || code.contains("MAT") -> if (isBengali) "পাঠ্যবই: আমার গণিত" else "Textbook: Amar Gonit"
                code.contains("PHY") -> if (isBengali) "পাঠ্যবই: পরিবেশ ও বিজ্ঞান (ভৌত বিজ্ঞান)" else "Textbook: Physical Science"
                code.contains("LIFE") || code.contains("BIO") -> if (isBengali) "পাঠ্যবই: পরিবেশ ও বিজ্ঞান (জীবন বিজ্ঞান)" else "Textbook: Life Science"
                code.contains("HIST") -> if (isBengali) "পাঠ্যবই: ইতিহাস ও পরিবেশ" else "Textbook: History & Environment"
                code.contains("GEO") -> if (isBengali) "পাঠ্যবই: পরিবেশ ও ভূগোল" else "Textbook: Geography & Environment"
                else -> if (isBengali) "পাঠ্যবই: নির্ধারিত পাঠ্যবই" else "Textbook: Prescribed Textbook"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeaderBar(
            title = subject.name,
            subtitle = textbookSubtitle,
            isBengali = isBengali,
            showBackButton = true,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Header Banner - Edge-to-Edge with 0dp Corner Radius using HeroBanner component
            item {
                HeroBanner(
                    title = "প্রস্তুতি ও সার্বিক অগ্রগতি",
                    subtitle = "${subject.completedChapters}/${subject.totalChapters}টি অধ্যায় সমাপ্ত",
                    masteryPercentage = subject.masteryPercentage,
                    primaryColor = subjectColor,
                    bottomContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniBadge(
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                label = "${subject.totalChapters} অধ্যায়"
                            )
                            StatMiniBadge(
                                icon = Icons.AutoMirrored.Filled.Assignment,
                                label = "${subjectTests.size} টেস্ট সিরিজ"
                            )
                            StatMiniBadge(
                                icon = Icons.Default.HistoryEdu,
                                label = "PYQs ১০ বছর"
                            )
                        }
                    }
                )
            }

            // 2. Navigation Tab Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        "📖 অধ্যায়সমূহ (${chapters.size})",
                        "📝 ইউনিট ও মক টেস্ট (${subjectTests.size})",
                        "📜 বিগত বছরের প্রশ্ন (${subjectPyqs.size})",
                        "📄 সংক্ষেপ নোটস ও সুত্র"
                    )

                    items(tabs.size) { index ->
                        val isSelected = selectedTab == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            label = {
                                Text(
                                    text = tabs[index],
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) subjectColor else MaterialTheme.colorScheme.outlineVariant
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = subjectColor,
                                selectedLabelColor = Color.White,
                                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // 3. Tab Content
            when (selectedTab) {
                0 -> {
                    // CHAPTERS LIST
                    item {
                        Text(
                            text = "অধ্যায়ভিত্তিক রোডম্যাপ ও টপিকসমূহ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    if (chapters.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "এই বিষয়ের অধ্যায় শীঘ্রই যুক্ত হচ্ছে।",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else {
                        items(chapters) { chapter ->
                            val chTopics = topics.filter { it.chapterId == chapter.id }
                            ChapterDetailCard(
                                chapter = chapter,
                                topicCount = chTopics.size,
                                subjectColor = subjectColor,
                                isBengali = isBengali,
                                isDark = isDark,
                                onOpenChapterDetails = {
                                    onOpenChapterDetail(chapter)
                                },
                                onStartChapterPractice = {
                                    onStartAssessment(
                                        AssessmentMode.PRACTICE,
                                        chapter.id,
                                        chapter.title
                                    )
                                },
                                onStartBossBattle = {
                                    onStartAssessment(
                                        AssessmentMode.BOSS_BATTLE,
                                        chapter.id,
                                        "🔥 অধ্যায় বস: ${chapter.title}"
                                    )
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // SUBJECT TESTS
                    if (attachedFirestoreTests.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🔥 অ্যাডমিন প্রস্তুতকৃত অনলাইন মক টেস্ট (${attachedFirestoreTests.size}টি)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "বিষয়ভিত্তিক অনলাইন মক টেস্ট ও মূল্যায়ন দিয়ে সরাসরি নিজের প্রস্তুতি যাচাই করো।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(attachedFirestoreTests) { test ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, Color(0xFF4F46E5).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF4F46E5),
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = test.testType.ifBlank { "ONLINE MOCK" },
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                                border = BorderStroke(0.5.dp, Color(0xFF0284C7))
                                            ) {
                                                Text(
                                                    text = "${test.durationMinutes} মি. • ${test.totalMarks} মার্কস",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF38BDF8),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = test.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        if (test.description.isNotBlank()) {
                                            Text(
                                                text = test.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Button(
                                        onClick = {
                                            onStartAssessment(
                                                AssessmentMode.MOCK_TEST,
                                                test.id,
                                                test.title
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4F46E5),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("টেস্ট দাও", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    item {
                        Text(
                            text = "${subject.name} - স্পেশাল টেস্ট সিরিজ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    itemsIndexed(subjectTests) { testIdx, test ->
                        val isMock = test.type == TestType.MOCK_TEST
                        val prevTest = if (testIdx > 0) subjectTests[testIdx - 1] else null
                        val prevScore = if (prevTest != null && getTestScore != null) getTestScore(prevTest.id) else 100
                        val isUnlocked = testIdx == 0 || prevScore >= 60
                        val myScore = if (getTestScore != null) getTestScore(test.id) else -1
                        val isPassed = myScore >= 60

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    !isUnlocked -> if (isDark) Color(0xFF131B2A) else Color(0xFFF1F5F9)
                                    isPassed -> if (isDark) Color(0xFF0F2D1F) else Color(0xFFECFDF5)
                                    isMock -> if (isDark) Color(0xFF2C2210) else Color(0xFFFFFBEB)
                                    else -> if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                width = if (isMock || isPassed) 1.5.dp else 1.dp,
                                color = when {
                                    !isUnlocked -> if (isDark) Color(0xFF243247) else Color(0xFFCBD5E1)
                                    isPassed -> CorrectGreen
                                    isMock -> ExamGold
                                    else -> if (isDark) Color(0xFF334155) else subjectColor.copy(alpha = 0.3f)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = if (!isUnlocked) {
                                                            listOf(Color.Gray, Color.DarkGray)
                                                        } else if (isPassed) {
                                                            listOf(Color(0xFF059669), Color(0xFF10B981))
                                                        } else if (isMock) {
                                                            listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                                        } else {
                                                            listOf(subjectColor, subjectColor.copy(alpha = 0.8f))
                                                        }
                                                    )
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = test.badgeLabel,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }

                                        if (!isUnlocked) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "🔒 পূর্ববর্তী টেস্টে ৬০% প্রয়োজন",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = if (isDark) ExamGold else Color(0xFFD97706))
                                            )
                                        } else if (myScore >= 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "✓ স্কোর: $myScore%",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (isPassed) CorrectGreen else ExamGold)
                                            )
                                        }
                                    }

                                    Text(
                                        text = test.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )

                                    Text(
                                        text = test.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = if (isPassed) {
                                                        listOf(Color(0xFF059669), Color(0xFF10B981))
                                                    } else if (isMock) {
                                                        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                                    } else {
                                                        listOf(subjectColor, subjectColor.copy(alpha = 0.8f))
                                                    }
                                                )
                                            )
                                            .clickable {
                                                onStartAssessment(
                                                    if (isMock) AssessmentMode.MOCK_TEST else AssessmentMode.BOSS_BATTLE,
                                                    subject.id,
                                                    test.title
                                                )
                                            }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (myScore >= 0) ("পুনরায়") else ("শুরু করো"),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "লকড",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // PYQ BOARD QUESTIONS
                    item {
                        Text(
                            text = if (isBengali) {
                                if (subject.classId.contains("wbchse")) "বিগত বছরের উচ্চমাধ্যমিক বোর্ড প্রশ্নাবলী"
                                else "বিগত বছরের বোর্ড প্রশ্নাবলী"
                            } else "Previous Year Board Exam Questions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    if (subjectPyqs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HistoryEdu,
                                        contentDescription = "PYQ",
                                        modifier = Modifier.size(40.dp),
                                        tint = subjectColor
                                    )
                                    Text(
                                        text = "এই বিষয়ের সকল বোর্ডের PYQs এক্সপ্লোরার সেকশনে উপলব্ধ।",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(subjectColor, subjectColor.copy(alpha = 0.8f))
                                                )
                                            )
                                            .clickable {
                                                onStartAssessment(
                                                    AssessmentMode.MOCK_TEST,
                                                    subject.id,
                                                    if (subject.classId.contains("wbchse")) "HS PYQ Board Paper"
                                                    else "Board PYQ Paper"
                                                )
                                            }
                                            .padding(horizontal = 18.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "বোর্ড PYQ টেস্ট শুরু করো",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(subjectPyqs) { pyq ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                                    )
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (subject.classId.contains("wbchse")) "WBCHSE ${pyq.pyqYear}" else "WBBSE ${pyq.pyqYear}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = "১ নম্বর",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = pyq.question,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (pyq.explanation.isNotBlank()) {
                                        Text(
                                            text = "💡 সমাধান: ${pyq.correctAnswerText}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = if (isDark) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // QUICK REVISION NOTES & FORMULAS
                    item {
                        Text(
                            text = "গুরুত্বপূর্ণ সূত্র, সংজ্ঞা ও কুইক রিভিশন কার্ড",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                            ),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else subjectColor.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "Tips",
                                        tint = if (isDark) ExamGold else subjectColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "পরীক্ষায় সর্বাধিক আসা বিষয়সমূহ",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDark) ExamGold else subjectColor
                                    )
                                }
                                Text(
                                    text = "• গাণিতিক সূত্রাবলী ও গাণিতিক উদাহরণসমূহ নিখুঁতভাবে অভ্যাস করো।\n• চিত্রসহ প্রশ্নের ক্ষেত্রে পরিষ্কার চিহ্নিত চিত্র আঁকার অভ্যাস জরুরি।\n• সংজ্ঞা ও একক লেখার সময় সঠিক SI একক উল্লেখ করা আবশ্যক।",
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterDetailCard(
    chapter: ChapterEntity,
    topicCount: Int,
    subjectColor: Color,
    isBengali: Boolean,
    isDark: Boolean,
    onOpenChapterDetails: () -> Unit,
    onStartChapterPractice: () -> Unit,
    onStartBossBattle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onOpenChapterDetails() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(subjectColor, subjectColor.copy(alpha = 0.75f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBengali) chapter.chapterNumber.toBengaliDigits() else "${chapter.chapterNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (topicCount > 0) {
                                "${if (isBengali) topicCount.toBengaliDigits() else topicCount}টি টপিক অন্তর্ভুক্ত"
                            } else {
                                chapter.description
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onOpenChapterDetails) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Chapter",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { chapter.masteryPercentage / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (chapter.masteryPercentage >= 80) CorrectGreen else subjectColor,
                    trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                )
                Text(
                    text = "${chapter.masteryPercentage}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onStartChapterPractice,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, subjectColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = subjectColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "অনুশীলন",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = subjectColor
                    )
                }

                Button(
                    onClick = onStartBossBattle,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExamGold,
                        contentColor = Color(0xFF78350F)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "বস চ্যালেঞ্জ",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun StatMiniBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}
