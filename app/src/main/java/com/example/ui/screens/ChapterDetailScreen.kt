package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.ExamGold

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
    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val subjectColor = Color(subjectColorLong)
    var activeVideoLesson by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<VideoLesson?>(null) }
    var showConceptDialog by remember { mutableStateOf(false) }
    var conceptTitle by remember { mutableStateOf("") }
    var conceptSummaryText by remember { mutableStateOf("") }

    var showSolutionDialog by remember { mutableStateOf(false) }
    var solutionTitle by remember { mutableStateOf("") }
    var solutionText by remember { mutableStateOf("") }

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
    val testScoresVersion by (viewModel?.testScoresVersion?.collectAsState() ?: remember { mutableStateOf(0) })

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeaderBar(
            title = if (isBengali) chapter.titleBengali else chapter.titleEnglish,
            subtitle = "অধ্যায় ${chapter.chapterNumber} - টপিক ও প্রশ্নোত্তর",
            isBengali = isBengali,
            showBackButton = true,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Chapter Hero Banner using HeroBanner component
            item {
                HeroBanner(
                    title = "অধ্যায় প্রস্তুতি ও অগ্রগতি",
                    subtitle = if (isBengali) chapter.titleBengali else chapter.titleEnglish,
                    masteryPercentage = chapter.masteryPercentage,
                    primaryColor = subjectColor,
                    bottomContent = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                com.example.ui.components.StatMiniBadge(
                                    icon = Icons.AutoMirrored.Filled.MenuBook,
                                    label = "${topics.size}টি টপিক"
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
                            }
                        }
                    }
                )
            }

            // 2. CHAPTER CONTENT: TOPICS OR TESTS
            if (topics.isNotEmpty()) {
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
                                    Text(
                                        text = if (isBengali) topic.titleBengali else topic.titleEnglish,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
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
                // NO TOPICS EXIST -> SHOW AVAILABLE TESTS FOR CHAPTER
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⚡ এই অধ্যায়ের উপলব্ধ টেস্টসমূহ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "অধ্যায়ভিত্তিক জ্ঞান পরীক্ষা করতে নিচের টেস্টগুলো শুরু করো।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Test Item 1: Practice Quiz
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else subjectColor.copy(alpha = 0.3f)),
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
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "অধ্যায়ভিত্তিক প্র্যাকটিস কুইজ",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "১০টি গুরুত্বপূর্ণ বোর্ড স্ট্যান্ডার্ড প্রশ্ন। কোনো সময়ের চাপ নেই।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                        )
                                    )
                                    .clickable {
                                        onStartAssessment(
                                            AssessmentMode.PRACTICE,
                                            chapter.id,
                                            "${chapter.titleBengali} - অনুশীলন টেস্ট"
                                        )
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "প্র্যাকটিস শুরু করো",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }


            }

            // ATTACHED CHAPTER TESTS (From Admin Dashboard / Firestore)
            if (attachedChapterTests.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔥 অধ্যায়ভিত্তিক অনলাইন টেস্টসমূহ (${attachedChapterTests.size}টি)",
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
