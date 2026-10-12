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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChapterEntity
import com.example.data.local.TopicEntity
import com.example.data.model.*
import com.example.ui.components.HeroBanner
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.components.VideoLessonPlayerDialog
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ExamCyan
import com.example.ui.theme.ExamGold
import com.example.ui.theme.WrongRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    chapter: ChapterEntity,
    topics: List<TopicEntity>,
    subjectColorLong: Long = 0xFF2563EBL,
    isBengali: Boolean,
    onBack: () -> Unit,
    onSelectTopic: (String) -> Unit,
    onStartAssessment: (AssessmentMode, String, String) -> Unit,
    onViewContent: (String, String?, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: com.example.ui.viewmodel.DeshExamViewModel? = null
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.45f
    val subjectColor = Color(subjectColorLong)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var activeVideoLesson by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<VideoLesson?>(null) }
    var showConceptDialog by remember { mutableStateOf(false) }
    var conceptTitle by remember { mutableStateOf("") }
    var conceptSummaryText by remember { mutableStateOf("") }

    var showSolutionDialog by remember { mutableStateOf(false) }
    var solutionTitle by remember { mutableStateOf("") }
    var solutionText by remember { mutableStateOf("") }

    val testScoresVersion by (viewModel?.testScoresVersion?.collectAsState() ?: remember { mutableIntStateOf(0) })
    var selectedTestTab by remember { mutableIntStateOf(0) }
    val fallbackTopicQCount = remember(topics) {
        if (topics.isNotEmpty()) topics.sumOf { it.questionCount } else 0
    }
    var totalChapterQuestions by remember(chapter.id) { 
        mutableIntStateOf(if (fallbackTopicQCount > 0) fallbackTopicQCount else 0) 
    }
    LaunchedEffect(chapter.id) {
        if (viewModel != null) {
            val count = viewModel.getChapterQuestionCount(chapter.id)
            if (count > 0) totalChapterQuestions = count
        }
    }
    val displayedQuestionCount = if (totalChapterQuestions > 0) totalChapterQuestions else fallbackTopicQCount
    val chunkSize = 15
    val safeTotalQ = if (displayedQuestionCount > 0) displayedQuestionCount else 45
    // Each unique slice of 15 questions is prepared in 2 versions:
    // (a) Normal sequential order, and (b) Fully randomized questions & options.
    // So if total unique 15-q chunks = K, then initial ready sets = 2 * K (minimum 2 sets).
    val baseUniqueChunks = ((safeTotalQ + chunkSize - 1) / chunkSize).coerceIn(1, 10)
    val baseSets = (baseUniqueChunks * 2).coerceAtLeast(2)
    val extraSets = remember(chapter.id, testScoresVersion) {
        viewModel?.getTopicExtraSets(chapter.id) ?: 0
    }
    val totalSets = baseSets + extraSets

    val setScores = remember(chapter.id, totalSets, testScoresVersion) {
        (1..totalSets).associateWith { setIdx ->
            viewModel?.getTopicSetScore(chapter.id, setIdx) ?: -1
        }
    }
    val setUnlocks = remember(chapter.id, totalSets, testScoresVersion, setScores) {
        (1..totalSets).associateWith { setIdx ->
            if (setIdx == 1) true
            else {
                val prevScore = setScores[setIdx - 1] ?: -1
                prevScore >= 60
            }
        }
    }

    val mockScores = remember(chapter.id, testScoresVersion) {
        (1..3).associateWith { idx -> viewModel?.getTopicMockScore(chapter.id, idx) ?: -1 }
    }
    val mockUnlocks = remember(chapter.id, testScoresVersion, mockScores) {
        (1..3).associateWith { idx -> if (idx == 1) true else (mockScores[idx - 1] ?: -1) >= 60 }
    }

    val speedScores = remember(chapter.id, testScoresVersion) {
        (1..3).associateWith { idx -> viewModel?.getTopicSpeedScore(chapter.id, idx) ?: -1 }
    }
    val speedUnlocks = remember(chapter.id, testScoresVersion, speedScores) {
        (1..3).associateWith { idx -> if (idx == 1) true else (speedScores[idx - 1] ?: -1) >= 60 }
    }

    val bossScores = remember(chapter.id, testScoresVersion) {
        (1..3).associateWith { idx -> viewModel?.getTopicBossScore(chapter.id, idx) ?: -1 }
    }
    val bossUnlocks = remember(chapter.id, testScoresVersion, bossScores) {
        (1..3).associateWith { idx -> if (idx == 1) true else (bossScores[idx - 1] ?: -1) >= 60 }
    }

    val pyqScores = remember(chapter.id, testScoresVersion) {
        (1..3).associateWith { idx -> viewModel?.getTopicPYQScore(chapter.id, idx) ?: -1 }
    }
    val pyqUnlocks = remember(chapter.id, testScoresVersion, pyqScores) {
        (1..3).associateWith { idx -> if (idx == 1) true else (pyqScores[idx - 1] ?: -1) >= 60 }
    }
    var chapterMainTab by remember(topics.isEmpty()) { mutableIntStateOf(if (topics.isEmpty()) 1 else 0) }

    val lastSetPassed = (setScores[totalSets] ?: -1) >= 60
    LaunchedEffect(lastSetPassed, totalSets) {
        if (lastSetPassed && totalSets > 0 && viewModel != null) {
            viewModel.generateNewDynamicSet(chapter.id)
        }
    }

    if (showConceptDialog) {
        com.example.ui.components.ConceptDetailDialog(
            title = conceptTitle,
            conceptSummary = conceptSummaryText,
            onDismiss = { showConceptDialog = false }
        )
    }

    if (showSolutionDialog) {
        com.example.ui.components.ConceptDetailDialog(
            title = solutionTitle,
            subtitle = "টপিক সমাধান ও প্রশ্নোত্তর নোটস",
            conceptSummary = solutionText,
            onDismiss = { showSolutionDialog = false }
        )
    }

    val firestoreTestTemplates by (viewModel?.firestoreTestTemplates?.collectAsState() ?: remember { mutableStateOf(emptyList()) })

    LaunchedEffect(chapter.id) {
        viewModel?.fetchFirestoreTestTemplates(chapter.classId, chapter.subjectId)
    }

    val attachedChapterTests = remember(firestoreTestTemplates, chapter) {
        firestoreTestTemplates.filter { test ->
            val chId = chapter.id.lowercase().trim()
            val chNumStr = "ch${chapter.chapterNumber}"
            val chTitle = chapter.title.lowercase().trim()
            val testChId = (test.chapterId ?: "").lowercase().trim()
            val testSubId = (test.subjectId ?: "").lowercase().trim()
            val testId = test.id.lowercase().trim()

            val matchesDirectChapter = testChId.isNotBlank() && (testChId == chId || testChId.contains(chId) || chId.contains(testChId))
            val matchesTestId = (chId.length > 2 && testId.contains(chId)) || testId.contains(chNumStr)
            val matchesQuestions = test.questions.any { q ->
                val qCh = q.chapterId.lowercase().trim()
                qCh.isNotBlank() && (qCh == chId || qCh.contains(chId) || chId.contains(qCh))
            }
            val matchesSubjectFallback = (testChId.isBlank() || testChId == "null") && testSubId.isNotBlank() && (testSubId == chapter.subjectId.lowercase().trim() || chapter.subjectId.lowercase().trim().contains(testSubId))

            matchesDirectChapter || matchesTestId || matchesQuestions || matchesSubjectFallback
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ScreenHeaderBar(
                title = if (isBengali) chapter.titleBengali else chapter.titleEnglish,
                subtitle = if (topics.isNotEmpty()) "অধ্যায় ${chapter.chapterNumber} - টপিক ও প্রশ্নোত্তর" else "অধ্যায় ${chapter.chapterNumber} - কুইজ টেস্ট ও প্রস্তুতি (${totalChapterQuestions.toBengaliDigits()}টি প্রশ্ন • ${totalSets.toBengaliDigits()}টি সেট)",
                isBengali = isBengali,
                showBackButton = true,
                onBack = onBack
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Chapter Hero Banner using HeroBanner component
            item {
                HeroBanner(
                    title = "অধ্যায় প্রস্তুতি ও অগ্রগতি",
                    subtitle = if (displayedQuestionCount > 0) {
                        "${if (isBengali) chapter.titleBengali else chapter.titleEnglish} • মোট ${displayedQuestionCount.toBengaliDigits()}টি প্রশ্ন"
                    } else {
                        if (isBengali) chapter.titleBengali else chapter.titleEnglish
                    },
                    masteryPercentage = chapter.masteryPercentage,
                    primaryColor = subjectColor,
                    bottomContent = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.example.ui.components.StatMiniBadge(
                                    icon = Icons.AutoMirrored.Filled.Help,
                                    label = if (displayedQuestionCount > 0) "মোট ${displayedQuestionCount.toBengaliDigits()}টি প্রশ্ন" else "প্রশ্নোত্তর প্রস্তুতি",
                                    containerColor = Color.White.copy(alpha = 0.28f)
                                )
                                com.example.ui.components.StatMiniBadge(
                                    icon = Icons.AutoMirrored.Filled.MenuBook,
                                    label = if (topics.isNotEmpty()) "${topics.size.toBengaliDigits()}টি টপিক" else "${totalSets.toBengaliDigits()}টি টেস্ট সেট"
                                )
                                com.example.ui.components.StatMiniBadge(
                                    icon = Icons.AutoMirrored.Filled.Assignment,
                                    label = "অধ্যায় মূল্যায়ন"
                                )
                                com.example.ui.components.StatMiniBadge(
                                    icon = Icons.Default.Shield,
                                    label = "বস ব্যাটল"
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Concept Button
                                com.example.ui.components.GradientChipButton(
                                    icon = Icons.Default.Lightbulb,
                                    label = "কনসেপ্ট",
                                    gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                                    onClick = {
                                        conceptTitle = if (isBengali) chapter.titleBengali else chapter.titleEnglish
                                        conceptSummaryText = chapter.contentData ?: "অধ্যায়ের বিস্তারিত মূল ধারণা ও পয়েন্ট সম্বলিত নোটস।"
                                        showConceptDialog = true
                                    }
                                )

                                // 2. Content Button
                                com.example.ui.components.GradientChipButton(
                                    icon = Icons.AutoMirrored.Filled.MenuBook,
                                    label = "কন্টেন্ট ও নোটস",
                                    gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                                    onClick = {
                                        onViewContent(
                                            if (isBengali) chapter.titleBengali else chapter.titleEnglish,
                                            chapter.contentData ?: "",
                                            chapter.solutionData ?: ""
                                        )
                                    }
                                )

                                // 3. Solution Button
                                com.example.ui.components.GradientChipButton(
                                    icon = Icons.AutoMirrored.Filled.Assignment,
                                    label = "সমাধান",
                                    gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                                    onClick = {
                                        onViewContent(
                                            "সমাধান - ${if (isBengali) chapter.titleBengali else chapter.titleEnglish}",
                                            (chapter.solutionData ?: "").ifBlank { "অধ্যায়ের বিস্তারিত সমাধান ও উত্তরপত্র শীঘ্রই যুক্ত করা হচ্ছে।" },
                                            chapter.solutionData ?: ""
                                        )
                                    }
                                )

                                // 4. Video Class Button
                                com.example.ui.components.GradientChipButton(
                                    icon = Icons.Default.PlayCircle,
                                    label = "ভিডিও ক্লাস",
                                    gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                                    onClick = {
                                        try {
                                            val firstTopic = topics.firstOrNull()
                                            val topicId = firstTopic?.id ?: "chap_${chapter.id}"
                                            val title = firstTopic?.let { if (isBengali) it.titleBengali else it.titleEnglish } ?: (if (isBengali) chapter.titleBengali else chapter.titleEnglish)
                                            val lesson = VideoLessonProvider.getVideoForTopic(topicId, title, chapter.subjectId, firstTopic?.videoUrl, firstTopic?.playlistJson)
                                            if (viewModel != null) {
                                                viewModel.openVideoLessonForEntity(lesson, firstTopic)
                                            } else {
                                                activeVideoLesson = lesson
                                            }
                                        } catch (_: Exception) {
                                            val fallback = VideoLessonProvider.getVideoForTopic(chapter.id)
                                            if (viewModel != null) {
                                                viewModel.openVideoLessonForEntity(fallback, topics.firstOrNull())
                                            } else {
                                                activeVideoLesson = fallback
                                            }
                                        }
                                    }
                                )

                                // 5. Podcast / Audio Button
                                com.example.ui.components.GradientChipButton(
                                    icon = Icons.Default.Headphones,
                                    label = "পডকাস্ট অডিও",
                                    gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)),
                                    onClick = {
                                        onViewContent(
                                            "🎧 পডকাস্ট অডিও সামারি - ${if (isBengali) chapter.titleBengali else chapter.titleEnglish}",
                                            "🔊 **অধ্যায় পডকাস্ট অডিও নোটস:**\n\nএই অধ্যায়ের গুরুত্বপূর্ণ সূত্র ও সংক্ষেপ আলোচনা অডিও লেকচার হিসেবে প্লে করার অপশন যুক্ত রয়েছে। মনোযোগ দিয়ে শুনুন এবং দ্রুত রিভিশন দিন!",
                                            ""
                                        )
                                    }
                                )

                                // 6. Chapter Tests Button
                                com.example.ui.components.GradientChipButton(
                                    icon = Icons.Default.Quiz,
                                    label = "অধ্যায় টেস্টস",
                                    gradientColors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)),
                                    onClick = {
                                        chapterMainTab = 1
                                    }
                                )
                            }
                        }
                    }
                )
            }

            // 2. MAIN SECTION SELECTOR (TOPICS OR TESTS)
            if (topics.isNotEmpty()) {
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val mainTabs = listOf(
                            "📖 অধ্যায় টপিকসমূহ (${topics.size.toBengaliDigits()}টি)",
                            "🎯 অধ্যায় টেস্ট ও পরীক্ষা"
                        )
                        items(mainTabs.size) { idx ->
                            val isSelected = chapterMainTab == idx
                            FilterChip(
                                selected = isSelected,
                                onClick = { chapterMainTab = idx },
                                label = {
                                    Text(
                                        text = mainTabs[idx],
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
            }

            // 3. CHAPTER CONTENT: TOPICS OR TESTS
            if (chapterMainTab == 0 && topics.isNotEmpty()) {
                // TOPICS EXIST FOR THIS CHAPTER
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📌 এই অধ্যায়ের টপিকসমূহ (${topics.size.toBengaliDigits()}টি টপিক)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "যেকোনো টপিকের ওপর ক্লিক করে নোটস, প্রশ্ন ও টেস্টসমূহ খুলুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                itemsIndexed(topics) { index, topic ->
                    val keyPointCount = remember(topic.keyPointsJson) {
                        if (topic.keyPointsJson.isBlank()) 0 else topic.keyPointsJson.split("|").filter { it.isNotBlank() }.size
                    }

                    val effectiveMastery = remember(topic.id, topic.masteryPercentage, testScoresVersion) {
                        val s1 = viewModel?.getTopicSetScore(topic.id, 1) ?: -1
                        val s2 = viewModel?.getTopicSetScore(topic.id, 2) ?: -1
                        val mock = viewModel?.getTopicMockScore(topic.id) ?: -1
                        val validScores = listOf(s1, s2, mock).filter { it >= 0 }
                        if (validScores.isNotEmpty()) {
                            validScores.average().toInt().coerceIn(0, 100)
                        } else {
                            topic.masteryPercentage.coerceIn(0, 100)
                        }
                    }

                    val qCount = remember(topic.id, topic.questionCount) {
                        if (topic.questionCount > 0) topic.questionCount else 15
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                            .clickable { onSelectTopic(topic.id) }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 1. Top Row: Topic Number & Title (Left) + Circular Progress Indicator (Right)
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
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(subjectColor, subjectColor.copy(alpha = 0.75f))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${chapter.chapterNumber.toBengaliDigits()}.${(index + 1).toBengaliDigits()}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isBengali) topic.titleBengali else topic.titleEnglish,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (topic.author.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "লেখক/কবি: ${topic.author}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Circular Progress Percentage
                                val progressFloat = (effectiveMastery / 100f).coerceIn(0f, 1f)
                                val progressRingColor = when {
                                    effectiveMastery >= 80 -> Color(0xFF10B981) // Emerald Green
                                    effectiveMastery >= 50 -> Color(0xFF3B82F6) // Blue
                                    effectiveMastery > 0 -> Color(0xFFF59E0B)  // Amber
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
                                        progress = { progressFloat },
                                        modifier = Modifier.fillMaxSize(),
                                        color = progressRingColor,
                                        strokeWidth = 3.dp,
                                        strokeCap = StrokeCap.Round
                                    )
                                    Text(
                                        text = "${effectiveMastery.toBengaliDigits()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.5.sp
                                        ),
                                        color = if (effectiveMastery > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 2. Middle: Info Chips (Questions, Estimated Time, XP, Key Points)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Question Count Chip
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = if (isDark) Color(0xFF1E3A5F) else Color(0xFFEFF6FF)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Quiz,
                                            contentDescription = null,
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${qCount.toBengaliDigits()} প্রশ্ন",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                            color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                                        )
                                    }
                                }

                                // Estimated Time Chip
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = if (isDark) Color(0xFF3730A3).copy(alpha = 0.4f) else Color(0xFFF5F3FF)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = Color(0xFF8B5CF6),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "১৫ মি.",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                            color = if (isDark) Color(0xFFC4B5FD) else Color(0xFF6D28D9)
                                        )
                                    }
                                }

                                // XP Reward Chip
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
                                            text = "+১০০ XP",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                        )
                                    }
                                }

                                // Key Points Count Chip
                                if (keyPointCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(5.dp),
                                        color = if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFECFDF5)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${keyPointCount.toBengaliDigits()} পয়েন্ট",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                                color = if (isDark) Color(0xFFA7F3D0) else Color(0xFF047857)
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Bottom Row: 4 Action Chip Buttons (Concept, Solution, Video, Tests)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 💡 Concept Button
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = if (isDark) Color(0xFF451A03).copy(alpha = 0.4f) else Color(0xFFFFFBEB),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable {
                                        conceptTitle = if (isBengali) topic.titleBengali else topic.titleEnglish
                                        val text = topic.contentData ?: topic.summaryBengali
                                        conceptSummaryText = if (text.isNotBlank()) text else "টপিকের মূল ধারণা ও প্রয়োজনীয় কনসেপ্ট নোটস।"
                                        showConceptDialog = true
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = "Concept",
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "কনসেপ্ট",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                        )
                                    }
                                }

                                // 📝 Solution Button
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = if (isDark) Color(0xFF134E4A).copy(alpha = 0.4f) else Color(0xFFF0FDFA),
                                    border = BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable {
                                        solutionTitle = if (isBengali) topic.titleBengali else topic.titleEnglish
                                        val sol = topic.solutionData
                                        solutionText = if (!sol.isNullOrBlank() && !sol.contains("খুব শীঘ্রই")) {
                                            sol
                                        } else {
                                            "## ${if (isBengali) topic.titleBengali else topic.titleEnglish} - সমাধান ও প্রশ্নোত্তর\n\n" +
                                            "এই টপিকের সকল গুরুত্বপূর্ণ ও বিগত বছরের প্রশ্নের আদর্শ ব্যাখ্যা ও মডেল সমাধান নিচে দেওয়া হলো:\n\n" +
                                            (topic.contentData ?: topic.summaryBengali)
                                        }
                                        showSolutionDialog = true
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = "Solution",
                                            tint = Color(0xFF0D9488),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "সমাধান",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                            color = if (isDark) Color(0xFF99F6E4) else Color(0xFF0F766E)
                                        )
                                    }
                                }

                                // ▶ Video Button
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.35f) else Color(0xFFFEF2F2),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable {
                                        try {
                                            val title = if (isBengali) topic.titleBengali else topic.titleEnglish
                                            val lesson = VideoLessonProvider.getVideoForTopic(
                                                topicId = topic.id,
                                                topicTitle = title,
                                                subjectId = chapter.subjectId,
                                                topicVideoUrl = topic.videoUrl,
                                                topicPlaylistJson = topic.playlistJson
                                            )
                                            if (viewModel != null) {
                                                viewModel.openVideoLessonForEntity(lesson, topic)
                                            } else {
                                                activeVideoLesson = lesson
                                            }
                                        } catch (_: Exception) {
                                            val fallback = VideoLessonProvider.getVideoForTopic(topic.id)
                                            if (viewModel != null) {
                                                viewModel.openVideoLessonForEntity(fallback, topic)
                                            } else {
                                                activeVideoLesson = fallback
                                            }
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = "ভিডিও ক্লাস",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "ভিডিও",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                            color = if (isDark) Color(0xFFFECACA) else Color(0xFFB91C1C)
                                        )
                                    }
                                }

                                // 🎯 Tests Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                            )
                                        )
                                        .clickable { onSelectTopic(topic.id) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Quiz,
                                            contentDescription = "Tests",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "টেস্টস",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // CHAPTER TEST HUB ORGANIZED IN TABS (বিভিন্ন টেস্ট টাইপস ট্যাব এর ভেতরে থাকবে)
                item {
                    val tabs = listOf(
                        "⚡ কুইজ টেস্ট (${totalSets.toBengaliDigits()})",
                        "📝 পূর্ণাঙ্গ মক টেস্ট (৩টি)",
                        "⚡ স্পিড ব্লিৎজ (৩টি)",
                        "👹 বস ব্যাটল (৩টি)",
                        "📜 বিগত বোর্ড প্রশ্ন (৩টি)",
                        if (attachedChapterTests.isNotEmpty()) "🔥 অনলাইন টেস্ট (${attachedChapterTests.size.toBengaliDigits()})" else null
                    ).filterNotNull()

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🎯 এই অধ্যায়ের টেস্ট ও কুইজসমূহ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(tabs.size) { tabIdx ->
                                val isSelected = selectedTestTab == tabIdx
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTestTab = tabIdx },
                                    label = {
                                        Text(
                                            text = tabs[tabIdx],
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
                }

                // TAB 0: ⚡ QUIZ TEST SERIES (১৫টি প্রশ্ন, প্রথম টেস্ট আনলক, ৬০% পেলে পরবর্তী আনলক, ২বার করে তৈরি: একবার সাধারণ ক্রম ও একবার প্রশ্ন-অপশন র‍্যান্ডমাইজড)
                if (selectedTestTab == 0) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "⚡ এই অধ্যায়ের কুইজ টেস্ট সিরিজ",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "প্রতি সেটে ১৫টি প্রশ্ন • ৬০% পেলে পরবর্তী সেট আনলক • সেট সমাপ্তে নতুন সেট",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "মোট ${totalSets.toBengaliDigits()}টি সেট",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                // Generate Automatic Set Cards with Progressive Locking (Styled like LearnTopicScreen)
                for (setIdx in 1..totalSets) {
                    val isUnlocked = setUnlocks[setIdx] == true
                    val score = setScores[setIdx] ?: -1
                    val isAttempted = score >= 0
                    val isPassed = score >= 60
                    val isDynamicExtra = setIdx > baseSets

                    val startQ = (setIdx - 1) * chunkSize + 1
                    val endQ = (setIdx * chunkSize).coerceAtMost(safeTotalQ.coerceAtLeast(setIdx * chunkSize))
                    val setQCount = if (isDynamicExtra) 15 else (endQ - startQ + 1).coerceIn(10, 15)

                    item(key = "chap_set_$setIdx") {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    !isUnlocked -> if (isDark) Color(0xFF131B2A) else Color(0xFFF1F5F9)
                                    isPassed -> if (isDark) Color(0xFF0F2D1F) else Color(0xFFECFDF5)
                                    isAttempted -> if (isDark) Color(0xFF2C2210) else Color(0xFFFFFBEB)
                                    else -> if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                width = if (isPassed) 1.5.dp else 1.dp,
                                color = when {
                                    !isUnlocked -> if (isDark) Color(0xFF243247) else Color(0xFFCBD5E1)
                                    isPassed -> CorrectGreen
                                    isAttempted -> ExamGold
                                    else -> if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .clickable {
                                    if (isUnlocked) {
                                        if (viewModel != null) {
                                            viewModel.startAssessment(
                                                AssessmentMode.PRACTICE,
                                                chapter.id,
                                                "${chapter.titleBengali} - কুইজ সেট ${setIdx.toBengaliDigits()}",
                                                setIdx
                                            )
                                        } else {
                                            onStartAssessment(
                                                AssessmentMode.PRACTICE,
                                                chapter.id,
                                                "${chapter.titleBengali} - কুইজ সেট ${setIdx.toBengaliDigits()}"
                                            )
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = if (isBengali)
                                                    "🔒 সেট ${setIdx.toBengaliDigits()} আনলক করতে পূর্ববর্তী সেট ${(setIdx - 1).toBengaliDigits()}-এ অন্তত ৬০% নম্বর অর্জন করতে হবে!"
                                                else
                                                    "🔒 Score 60%+ in Set ${setIdx - 1} to unlock Set $setIdx!"
                                            )
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 92.dp)
                                    .padding(horizontal = 16.dp, vertical = 18.dp),
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
                                                        colors = when {
                                                            !isUnlocked -> listOf(Color(0xFF64748B), Color(0xFF475569))
                                                            isPassed -> listOf(Color(0xFF059669), Color(0xFF10B981))
                                                            isAttempted -> listOf(Color(0xFFD97706), Color(0xFFF59E0B))
                                                            else -> listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                                        }
                                                    )
                                                )
                                                .padding(horizontal = 7.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "সেট ${setIdx.toBengaliDigits()}${if (isDynamicExtra) " (বোনাস)" else ""}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "অনুশীলন কুইজ",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }

                                    Text(
                                        text = "${if (isDynamicExtra) "ডায়নামিক বোনাস" else if (((setIdx - 1) % 2) == 1) "র‍্যান্ডমাইজড প্রশ্ন ও অপশন" else "ক্রমিক অনুশীলন"} • ${setQCount.toBengaliDigits()} প্রশ্ন • ১৫ মি. • ৫০ XP",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Status requirement or result badge
                                    when {
                                        !isUnlocked -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "সেট ${(setIdx - 1).toBengaliDigits()}-এ ৬০% পেলে আনলক হবে",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                                    color = Color(0xFFD97706)
                                                )
                                            }
                                        }
                                        isPassed -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Passed",
                                                    tint = CorrectGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "✓ ${score.toBengaliDigits()}% স্কোর অর্জিত (উত্তীর্ণ)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CorrectGreen)
                                                )
                                            }
                                        }
                                        isAttempted -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Needs improvement",
                                                    tint = ExamGold,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${score.toBengaliDigits()}% স্কোর • পুনরায় চেষ্টা করুন (৬০% চাই)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = if (isDark) ExamGold else Color(0xFFD97706))
                                                )
                                            }
                                        }
                                        else -> {
                                            Text(
                                                text = "🔓 আনলকড • শুরু করার জন্য প্রস্তুত",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF6366F1))
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = if (isPassed) {
                                                        listOf(Color(0xFF059669), Color(0xFF10B981))
                                                    } else {
                                                        listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                                    }
                                                )
                                            )
                                            .clickable {
                                                if (viewModel != null) {
                                                    viewModel.startAssessment(
                                                        AssessmentMode.PRACTICE,
                                                        chapter.id,
                                                        "${chapter.titleBengali} - কুইজ সেট ${setIdx.toBengaliDigits()}",
                                                        setIdx
                                                    )
                                                } else {
                                                    onStartAssessment(
                                                        AssessmentMode.PRACTICE,
                                                        chapter.id,
                                                        "${chapter.titleBengali} - কুইজ সেট ${setIdx.toBengaliDigits()}"
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when {
                                                isPassed -> "পুনরায় পরীক্ষা"
                                                isAttempted -> "আবার চেষ্টা"
                                                else -> "কুইজ শুরু"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isDark) {
                                                    Brush.horizontalGradient(listOf(Color(0xFF292524), Color(0xFF1C1917)))
                                                } else {
                                                    Brush.horizontalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)))
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "🔒 সেট ${setIdx.toBengaliDigits()} আনলক করতে পূর্ববর্তী সেট ${(setIdx - 1).toBengaliDigits()}-এ অন্তত ৬০% নম্বর অর্জন করতে হবে!"
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 9.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "লকড",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: 📝 FULL MOCK TEST SERIES (৩টি টেস্ট: টেস্ট ১ আনলক, ৬০% পেলে পরবর্তী আনলক)
            if (selectedTestTab == 1) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📝 পূর্ণাঙ্গ অধ্যায় মক টেস্ট সিরিজ",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "৩০টি প্রশ্ন • বোর্ড স্ট্যান্ডার্ড পূর্ণাঙ্গ পরীক্ষা • প্রতিটি টেস্টে ৬০% পেলে পরবর্তী আনলক",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0D9488).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "মোট ৩টি টেস্ট",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0D9488),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                for (idx in 1..3) {
                    val isUnlocked = mockUnlocks[idx] == true
                    val score = mockScores[idx] ?: -1
                    val isPassed = score >= 60
                    val isAttempted = score >= 0

                    item(key = "chap_mock_$idx") {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    !isUnlocked -> if (isDark) Color(0xFF131B2A) else Color(0xFFF1F5F9)
                                    isPassed -> if (isDark) Color(0xFF0F2D1F) else Color(0xFFECFDF5)
                                    isAttempted -> if (isDark) Color(0xFF2C2210) else Color(0xFFFFFBEB)
                                    else -> if (isDark) Color(0xFF0F2A28) else Color(0xFFF0FDFA)
                                }
                            ),
                            border = BorderStroke(
                                width = if (isPassed) 1.5.dp else 1.dp,
                                color = when {
                                    !isUnlocked -> if (isDark) Color(0xFF243247) else Color(0xFFCBD5E1)
                                    isPassed -> CorrectGreen
                                    isAttempted -> ExamGold
                                    else -> if (isDark) Color(0xFF0D9488) else Color(0xFF99F6E4)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .clickable {
                                    if (isUnlocked) {
                                        if (viewModel != null) {
                                            viewModel.startAssessment(
                                                AssessmentMode.MOCK_TEST,
                                                chapter.id,
                                                "${chapter.titleBengali} - পূর্ণাঙ্গ মক টেস্ট ${idx.toBengaliDigits()}",
                                                idx
                                            )
                                        } else {
                                            onStartAssessment(
                                                AssessmentMode.MOCK_TEST,
                                                chapter.id,
                                                "${chapter.titleBengali} - পূর্ণাঙ্গ মক টেস্ট ${idx.toBengaliDigits()}"
                                            )
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = if (isBengali)
                                                    "🔒 মক টেস্ট ${idx.toBengaliDigits()} আনলক করতে পূর্ববর্তী মক টেস্ট ${(idx - 1).toBengaliDigits()}-এ অন্তত ৬০% নম্বর পেতে হবে!"
                                                else
                                                    "🔒 Score 60%+ in Mock Test ${idx - 1} to unlock Mock Test $idx!"
                                            )
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 92.dp)
                                    .padding(horizontal = 16.dp, vertical = 18.dp),
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
                                                        colors = if (isUnlocked) {
                                                            listOf(Color(0xFF0D9488), Color(0xFF0F766E))
                                                        } else {
                                                            listOf(Color(0xFF64748B), Color(0xFF475569))
                                                        }
                                                    )
                                                )
                                                .padding(horizontal = 7.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "মক টেস্ট ${idx.toBengaliDigits()}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "পূর্ণাঙ্গ অধ্যায় মক টেস্ট",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isUnlocked) (if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F766E)) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }

                                    Text(
                                        text = "৩০ প্রশ্ন • ৩০ মি. • ১০০ XP • বোর্ড স্ট্যান্ডার্ড",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    when {
                                        !isUnlocked -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "মক টেস্ট ${(idx - 1).toBengaliDigits()}-এ ৬০% পেলে আনলক হবে",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                                    color = Color(0xFFD97706)
                                                )
                                            }
                                        }
                                        isPassed -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Passed",
                                                    tint = CorrectGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "✓ ${score.toBengaliDigits()}% স্কোর অর্জিত (উত্তীর্ণ)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CorrectGreen)
                                                )
                                            }
                                        }
                                        isAttempted -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Needs improvement",
                                                    tint = ExamGold,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${score.toBengaliDigits()}% স্কোর • পুনরায় চেষ্টা করুন (৬০% চাই)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = if (isDark) ExamGold else Color(0xFFD97706))
                                                )
                                            }
                                        }
                                        else -> {
                                            Text(
                                                text = "🔓 আনলকড • শুরু করার জন্য প্রস্তুত",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF0D9488))
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF0D9488), Color(0xFF0F766E))
                                                )
                                            )
                                            .clickable {
                                                if (viewModel != null) {
                                                    viewModel.startAssessment(
                                                        AssessmentMode.MOCK_TEST,
                                                        chapter.id,
                                                        "${chapter.titleBengali} - পূর্ণাঙ্গ মক টেস্ট ${idx.toBengaliDigits()}",
                                                        idx
                                                    )
                                                } else {
                                                    onStartAssessment(
                                                        AssessmentMode.MOCK_TEST,
                                                        chapter.id,
                                                        "${chapter.titleBengali} - পূর্ণাঙ্গ মক টেস্ট ${idx.toBengaliDigits()}"
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when {
                                                isPassed -> "পুনরায় মক"
                                                isAttempted -> "আবার চেষ্টা"
                                                else -> "মক শুরু"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isDark) {
                                                    Brush.horizontalGradient(listOf(Color(0xFF292524), Color(0xFF1C1917)))
                                                } else {
                                                    Brush.horizontalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)))
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = if (isBengali)
                                                            "🔒 মক টেস্ট ${idx.toBengaliDigits()} আনলক করতে পূর্ববর্তী মক টেস্ট ${(idx - 1).toBengaliDigits()}-এ অন্তত ৬০% নম্বর পেতে হবে!"
                                                        else
                                                            "🔒 Score 60%+ in Mock Test ${idx - 1} to unlock Mock Test $idx!"
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 9.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "লকড",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: ⚡ SPEED BLITZ SERIES (৩টি টেস্ট: টেস্ট ১ আনলক, ৬০% পেলে পরবর্তী আনলক)
            if (selectedTestTab == 2) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚡ অধ্যায় র‍্যাপিড স্পিড টেস্ট সিরিজ",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "১৫টি প্রশ্ন • দ্রুত সঠিক উত্তরের প্রতিযোগিতা • প্রতিটি টেস্টে ৬০% পেলে পরবর্তী আনলক",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "মোট ৩টি টেস্ট",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                for (idx in 1..3) {
                    val isUnlocked = speedUnlocks[idx] == true
                    val score = speedScores[idx] ?: -1
                    val isPassed = score >= 60
                    val isAttempted = score >= 0

                    item(key = "chap_speed_$idx") {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    !isUnlocked -> if (isDark) Color(0xFF131B2A) else Color(0xFFF1F5F9)
                                    isPassed -> if (isDark) Color(0xFF0F2D1F) else Color(0xFFECFDF5)
                                    isAttempted -> if (isDark) Color(0xFF2C2210) else Color(0xFFFFFBEB)
                                    else -> if (isDark) Color(0xFF2C2210) else Color(0xFFFFFBEB)
                                }
                            ),
                            border = BorderStroke(
                                width = if (isPassed) 1.5.dp else 1.dp,
                                color = when {
                                    !isUnlocked -> if (isDark) Color(0xFF243247) else Color(0xFFCBD5E1)
                                    isPassed -> CorrectGreen
                                    isAttempted -> ExamGold
                                    else -> ExamGold
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .clickable {
                                    if (isUnlocked) {
                                        if (viewModel != null) {
                                            viewModel.startAssessment(
                                                AssessmentMode.SPEED_BLITZ,
                                                chapter.id,
                                                "${chapter.titleBengali} - স্পিড টেস্ট ${idx.toBengaliDigits()}",
                                                idx
                                            )
                                        } else {
                                            onStartAssessment(
                                                AssessmentMode.SPEED_BLITZ,
                                                chapter.id,
                                                "${chapter.titleBengali} - স্পিড টেস্ট ${idx.toBengaliDigits()}"
                                            )
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = if (isBengali)
                                                    "🔒 স্পিড টেস্ট ${idx.toBengaliDigits()} আনলক করতে পূর্ববর্তী স্পিড টেস্ট ${(idx - 1).toBengaliDigits()}-এ অন্তত ৬০% নম্বর পেতে হবে!"
                                                else
                                                    "🔒 Score 60%+ in Speed Test ${idx - 1} to unlock Speed Test $idx!"
                                            )
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 92.dp)
                                    .padding(horizontal = 16.dp, vertical = 18.dp),
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
                                                        colors = if (isUnlocked) {
                                                            listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                                        } else {
                                                            listOf(Color(0xFF64748B), Color(0xFF475569))
                                                        }
                                                    )
                                                )
                                                .padding(horizontal = 7.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "স্পিড ব্লিৎজ ${idx.toBengaliDigits()}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "অধ্যায় র‍্যাপিড স্পিড টেস্ট",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isUnlocked) (if (isDark) ExamGold else Color(0xFF92400E)) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }

                                    Text(
                                        text = "১৫ প্রশ্ন • স্পিড চ্যালেঞ্জ • ১৫০ XP",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    when {
                                        !isUnlocked -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "স্পিড টেস্ট ${(idx - 1).toBengaliDigits()}-এ ৬০% পেলে আনলক হবে",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                                    color = Color(0xFFD97706)
                                                )
                                            }
                                        }
                                        isPassed -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Passed",
                                                    tint = CorrectGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "✓ ${score.toBengaliDigits()}% স্কোর অর্জিত (উত্তীর্ণ)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CorrectGreen)
                                                )
                                            }
                                        }
                                        isAttempted -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Needs improvement",
                                                    tint = ExamGold,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${score.toBengaliDigits()}% স্কোর • পুনরায় চেষ্টা করুন (৬০% চাই)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = if (isDark) ExamGold else Color(0xFFD97706))
                                                )
                                            }
                                        }
                                        else -> {
                                            Text(
                                                text = "🔓 আনলকড • শুরু করার জন্য প্রস্তুত",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFFF59E0B))
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                                )
                                            )
                                            .clickable {
                                                if (viewModel != null) {
                                                    viewModel.startAssessment(
                                                        AssessmentMode.SPEED_BLITZ,
                                                        chapter.id,
                                                        "${chapter.titleBengali} - স্পিড টেস্ট ${idx.toBengaliDigits()}",
                                                        idx
                                                    )
                                                } else {
                                                    onStartAssessment(
                                                        AssessmentMode.SPEED_BLITZ,
                                                        chapter.id,
                                                        "${chapter.titleBengali} - স্পিড টেস্ট ${idx.toBengaliDigits()}"
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when {
                                                isPassed -> "পুনরায় স্পিড"
                                                isAttempted -> "আবার চেষ্টা"
                                                else -> "স্পিড শুরু"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isDark) {
                                                    Brush.horizontalGradient(listOf(Color(0xFF292524), Color(0xFF1C1917)))
                                                } else {
                                                    Brush.horizontalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)))
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = if (isBengali)
                                                            "🔒 স্পিড টেস্ট ${idx.toBengaliDigits()} আনলক করতে পূর্ববর্তী স্পিড টেস্ট ${(idx - 1).toBengaliDigits()}-এ অন্তত ৬০% নম্বর পেতে হবে!"
                                                        else
                                                            "🔒 Score 60%+ in Speed Test ${idx - 1} to unlock Speed Test $idx!"
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 9.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "লকড",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: 👹 BOSS BATTLE SERIES (৩টি টেস্ট: টেস্ট ১ আনলক, ৬০% পেলে পরবর্তী আনলক)
            if (selectedTestTab == 3) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "👹 অধ্যায় বস ব্যাটল চ্যালেঞ্জ সিরিজ",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "কঠিন প্রশ্ন ও চ্যালেঞ্জিং লড়াই • প্রতিটি টেস্টে ৬০% পেলে পরবর্তী আনলক",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDC2626).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "মোট ৩টি ব্যাটল",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                for (idx in 1..3) {
                    val isUnlocked = bossUnlocks[idx] == true
                    val score = bossScores[idx] ?: -1
                    val isPassed = score >= 60
                    val isAttempted = score >= 0

                    item(key = "chap_boss_$idx") {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    !isUnlocked -> if (isDark) Color(0xFF131B2A) else Color(0xFFF1F5F9)
                                    isPassed -> if (isDark) Color(0xFF0F2D1F) else Color(0xFFECFDF5)
                                    isAttempted -> if (isDark) Color(0xFF2C1A1A) else Color(0xFFFEF2F2)
                                    else -> if (isDark) Color(0xFF2C1A1A) else Color(0xFFFEF2F2)
                                }
                            ),
                            border = BorderStroke(
                                width = if (isPassed) 1.5.dp else 1.dp,
                                color = when {
                                    !isUnlocked -> if (isDark) Color(0xFF243247) else Color(0xFFCBD5E1)
                                    isPassed -> CorrectGreen
                                    isAttempted -> ExamGold
                                    else -> Color(0xFFEF4444).copy(alpha = 0.6f)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .clickable {
                                    if (isUnlocked) {
                                        if (viewModel != null) {
                                            viewModel.startAssessment(
                                                AssessmentMode.BOSS_BATTLE,
                                                chapter.id,
                                                "${chapter.titleBengali} - বস ব্যাটল ${idx.toBengaliDigits()}",
                                                idx
                                            )
                                        } else {
                                            onStartAssessment(
                                                AssessmentMode.BOSS_BATTLE,
                                                chapter.id,
                                                "${chapter.titleBengali} - বস ব্যাটল ${idx.toBengaliDigits()}"
                                            )
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = if (isBengali)
                                                    "🔒 বস ব্যাটল ${idx.toBengaliDigits()} আনলক করতে পূর্ববর্তী বস ব্যাটল ${(idx - 1).toBengaliDigits()}-এ অন্তত ৬০% নম্বর পেতে হবে!"
                                                else
                                                    "🔒 Score 60%+ in Boss Battle ${idx - 1} to unlock Boss Battle $idx!"
                                            )
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 92.dp)
                                    .padding(horizontal = 16.dp, vertical = 18.dp),
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
                                                        colors = if (isUnlocked) {
                                                            listOf(Color(0xFFDC2626), Color(0xFFEF4444))
                                                        } else {
                                                            listOf(Color(0xFF64748B), Color(0xFF475569))
                                                        }
                                                    )
                                                )
                                                .padding(horizontal = 7.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "বস ব্যাটল ${idx.toBengaliDigits()}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "অধ্যায় বস ব্যাটল চ্যালেঞ্জ",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isUnlocked) (if (isDark) Color(0xFFFECACA) else Color(0xFF991B1B)) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }

                                    Text(
                                        text = "কঠিন প্রশ্ন ও চ্যালেঞ্জিং লড়াই • বড় কয়েন ও ২০০ XP পুরস্কার",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    when {
                                        !isUnlocked -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "বস ব্যাটল ${(idx - 1).toBengaliDigits()}-এ ৬০% পেলে আনলক হবে",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                                    color = Color(0xFFD97706)
                                                )
                                            }
                                        }
                                        isPassed -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Passed",
                                                    tint = CorrectGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "✓ ${score.toBengaliDigits()}% স্কোর অর্জিত (বিজয়)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CorrectGreen)
                                                )
                                            }
                                        }
                                        isAttempted -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Needs improvement",
                                                    tint = ExamGold,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${score.toBengaliDigits()}% স্কোর • পুনরায় চেষ্টা করুন (৬০% চাই)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = if (isDark) ExamGold else Color(0xFFD97706))
                                                )
                                            }
                                        }
                                        else -> {
                                            Text(
                                                text = "🔥 অধ্যায়ের সকল কঠিন প্রশ্ন একসঙ্গে জয় করো",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFFDC2626), Color(0xFFEF4444))
                                                )
                                            )
                                            .clickable {
                                                if (viewModel != null) {
                                                    viewModel.startAssessment(
                                                        AssessmentMode.BOSS_BATTLE,
                                                        chapter.id,
                                                        "${chapter.titleBengali} - বস ব্যাটল ${idx.toBengaliDigits()}",
                                                        idx
                                                    )
                                                } else {
                                                    onStartAssessment(
                                                        AssessmentMode.BOSS_BATTLE,
                                                        chapter.id,
                                                        "${chapter.titleBengali} - বস ব্যাটল ${idx.toBengaliDigits()}"
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when {
                                                isPassed -> "পুনরায় ব্যাটল"
                                                isAttempted -> "আবার চেষ্টা"
                                                else -> "বস ব্যাটল"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isDark) {
                                                    Brush.horizontalGradient(listOf(Color(0xFF292524), Color(0xFF1C1917)))
                                                } else {
                                                    Brush.horizontalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)))
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = if (isBengali)
                                                            "🔒 বস ব্যাটল ${idx.toBengaliDigits()} আনলক করতে পূর্ববর্তী বস ব্যাটল ${(idx - 1).toBengaliDigits()}-এ অন্তত ৬০% নম্বর পেতে হবে!"
                                                        else
                                                            "🔒 Score 60%+ in Boss Battle ${idx - 1} to unlock Boss Battle $idx!"
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 9.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "লকড",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 4: 📜 PYQ BOARD QUESTIONS SERIES (৩টি টেস্ট: টেস্ট ১ আনলক, ৬০% পেলে পরবর্তী আনলক)
            if (selectedTestTab == 4) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📜 বিগত বছরের বোর্ড প্রশ্নাবলি (PYQ)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "মাধ্যমিক পরীক্ষার বিগত বছরের আসল বোর্ড প্রশ্ন • প্রতিটি সেটে ৬০% পেলে পরবর্তী আনলক",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "মোট ৩টি সেট",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                for (idx in 1..3) {
                    val isUnlocked = pyqUnlocks[idx] == true
                    val score = pyqScores[idx] ?: -1
                    val isPassed = score >= 60
                    val isAttempted = score >= 0

                    item(key = "chap_pyq_$idx") {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    !isUnlocked -> if (isDark) Color(0xFF131B2A) else Color(0xFFF1F5F9)
                                    isPassed -> if (isDark) Color(0xFF0F2D1F) else Color(0xFFECFDF5)
                                    isAttempted -> if (isDark) Color(0xFF2C2210) else Color(0xFFFFFBEB)
                                    else -> if (isDark) Color(0xFF0F2D1F) else Color(0xFFECFDF5)
                                }
                            ),
                            border = BorderStroke(
                                width = if (isPassed) 1.5.dp else 1.dp,
                                color = when {
                                    !isUnlocked -> if (isDark) Color(0xFF243247) else Color(0xFFCBD5E1)
                                    isPassed -> CorrectGreen
                                    isAttempted -> ExamGold
                                    else -> Color(0xFF10B981).copy(alpha = 0.6f)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .clickable {
                                    if (isUnlocked) {
                                        if (viewModel != null) {
                                            viewModel.startAssessment(
                                                AssessmentMode.CHAPTER_PYQ,
                                                chapter.id,
                                                "${chapter.titleBengali} - বিগত বছরের বোর্ড প্রশ্ন ${idx.toBengaliDigits()}",
                                                idx
                                            )
                                        } else {
                                            onStartAssessment(
                                                AssessmentMode.CHAPTER_PYQ,
                                                chapter.id,
                                                "${chapter.titleBengali} - বিগত বছরের বোর্ড প্রশ্ন ${idx.toBengaliDigits()}"
                                            )
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = if (isBengali)
                                                    "🔒 বিগত বোর্ড প্রশ্ন সেট ${idx.toBengaliDigits()} আনলক করতে পূর্ববর্তী সেট ${(idx - 1).toBengaliDigits()}-এ অন্তত ৬০% নম্বর পেতে হবে!"
                                                else
                                                    "🔒 Score 60%+ in PYQ Set ${idx - 1} to unlock PYQ Set $idx!"
                                            )
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 92.dp)
                                    .padding(horizontal = 16.dp, vertical = 18.dp),
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
                                                        colors = if (isUnlocked) {
                                                            listOf(Color(0xFF059669), Color(0xFF10B981))
                                                        } else {
                                                            listOf(Color(0xFF64748B), Color(0xFF475569))
                                                        }
                                                    )
                                                )
                                                .padding(horizontal = 7.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "PYQ সেট ${idx.toBengaliDigits()}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "বিগত বছরের বোর্ড প্রশ্ন (PYQ)",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isUnlocked) (if (isDark) Color(0xFFA7F3D0) else Color(0xFF047857)) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }

                                    Text(
                                        text = "মাধ্যমিক পরীক্ষার বিগত বছরের আসল বোর্ড প্রশ্ন • ১০০ XP",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    when {
                                        !isUnlocked -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "PYQ সেট ${(idx - 1).toBengaliDigits()}-এ ৬০% পেলে আনলক হবে",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                                    color = Color(0xFFD97706)
                                                )
                                            }
                                        }
                                        isPassed -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Passed",
                                                    tint = CorrectGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "✓ ${score.toBengaliDigits()}% স্কোর অর্জিত (উত্তীর্ণ)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CorrectGreen)
                                                )
                                            }
                                        }
                                        isAttempted -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Needs improvement",
                                                    tint = ExamGold,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${score.toBengaliDigits()}% স্কোর • পুনরায় চেষ্টা করুন (৬০% চাই)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = if (isDark) ExamGold else Color(0xFFD97706))
                                                )
                                            }
                                        }
                                        else -> {
                                            Text(
                                                text = "🎯 বিগত ৫-১০ বছরের মাধ্যমিক প্রশ্ন অনুশীলন",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF059669), Color(0xFF10B981))
                                                )
                                            )
                                            .clickable {
                                                if (viewModel != null) {
                                                    viewModel.startAssessment(
                                                        AssessmentMode.CHAPTER_PYQ,
                                                        chapter.id,
                                                        "${chapter.titleBengali} - বিগত বছরের বোর্ড প্রশ্ন ${idx.toBengaliDigits()}",
                                                        idx
                                                    )
                                                } else {
                                                    onStartAssessment(
                                                        AssessmentMode.CHAPTER_PYQ,
                                                        chapter.id,
                                                        "${chapter.titleBengali} - বিগত বছরের বোর্ড প্রশ্ন ${idx.toBengaliDigits()}"
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when {
                                                isPassed -> "পুনরায় PYQ"
                                                isAttempted -> "আবার চেষ্টা"
                                                else -> "PYQ টেস্ট"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isDark) {
                                                    Brush.horizontalGradient(listOf(Color(0xFF292524), Color(0xFF1C1917)))
                                                } else {
                                                    Brush.horizontalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)))
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = if (isBengali)
                                                            "🔒 বিগত বোর্ড প্রশ্ন সেট ${idx.toBengaliDigits()} আনলক করতে পূর্ববর্তী সেট ${(idx - 1).toBengaliDigits()}-এ অন্তত ৬০% নম্বর পেতে হবে!"
                                                        else
                                                            "🔒 Score 60%+ in PYQ Set ${idx - 1} to unlock PYQ Set $idx!"
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 9.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "লকড",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 5: 🔥 ATTACHED CHAPTER TESTS (From Admin Dashboard / Firestore)
            if (selectedTestTab == 5 && attachedChapterTests.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔥 অধ্যায়ভিত্তিক অনলাইন টেস্টসমূহ (${attachedChapterTests.size.toBengaliDigits()}টি)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "অ্যাডমিন তৈরি অনলাইন মক টেস্ট দিয়ে এই অধ্যায়ের প্রস্তুতি পরখ করো।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                itemsIndexed(attachedChapterTests) { _, test ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, Color(0xFF4F46E5).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
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
                                            text = test.testType.ifBlank { "CHAPTER TEST" },
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
                                            text = "${test.durationMinutes} মি. • ${test.totalMarks} নম্বর",
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
            }
        }
    }

    val currentLesson = activeVideoLesson
    if (currentLesson != null) {
        VideoLessonPlayerDialog(
            lesson = currentLesson,
            onDismiss = { activeVideoLesson = null },
            onStartQuiz = {
                val topicId = currentLesson.safeTopicId
                activeVideoLesson = null
                onStartAssessment(
                    AssessmentMode.PRACTICE,
                    topicId,
                    "ভিডিও অনুশীলন টেস্ট"
                )
            },
            viewModel = viewModel
        )
    }
}
}
