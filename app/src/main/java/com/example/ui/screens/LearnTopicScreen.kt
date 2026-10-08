package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.components.VideoLessonPlayerDialog
import com.example.ui.components.VideoPlayer
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ExamCyan
import com.example.ui.theme.ExamGold
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel
import kotlinx.coroutines.launch

@Composable
fun LearnTopicHeroBanner(
    topic: TopicItem,
    totalQuestions: Int,
    totalSets: Int,
    isBengali: Boolean,
    onViewContent: () -> Unit,
    onViewSolution: () -> Unit,
    onViewVideo: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.45f
    val mastery = topic.masteryPercentage.coerceIn(0, 100)

    var showTopicConceptDialog by remember { mutableStateOf(false) }
    var topicConceptTitle by remember { mutableStateOf("") }
    var topicConceptSummaryText by remember { mutableStateOf("") }

    if (showTopicConceptDialog) {
        com.example.ui.components.ConceptDetailDialog(
            title = topicConceptTitle,
            conceptSummary = topic.getSummary(isBengali),
            keyPointsList = topic.keyPoints,
            onDismiss = { showTopicConceptDialog = false }
        )
    }
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E1B2E),
                            Color(0xFF312E81),
                            Color(0xFF0F766E)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Circular Star Icon + Title & Topic Subtitle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star",
                                    tint = ExamGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "টপিক প্রস্তুতি ও অগ্রগতি",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = topic.getTitle(isBengali),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1
                            )
                        }
                    }

                    // Right side circular percentage badge (Gold border, transparent background, white text)
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, ExamGold),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$mastery%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // Topic Metadata Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    com.example.ui.components.StatMiniBadge(
                        icon = Icons.AutoMirrored.Filled.Help,
                        label = "${totalQuestions}টি প্রশ্ন"
                    )
                    com.example.ui.components.StatMiniBadge(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        label = "${totalSets} সেট প্র্যাকটিস"
                    )
                    com.example.ui.components.StatMiniBadge(
                        icon = Icons.Default.PlayCircle,
                        label = "ভিডিও ক্লাস"
                    )
                }

                // Quick Action Gradient Chip Buttons: Concept, Content, Solution, Video, & Podcast Audio
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Concept Button
                    com.example.ui.components.GradientChipButton(
                        icon = Icons.Default.Lightbulb,
                        label = if (isBengali) "কনসেপ্ট" else "Concept",
                        gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                        onClick = {
                            topicConceptTitle = topic.getTitle(isBengali)
                            topicConceptSummaryText = (topic.contentData ?: "").ifBlank { "📌 **মূল বিষয়বস্তু ও সারসংক্ষেপ:**\n\n${topic.getTitle(isBengali)} টপিকের গুরুত্বপূর্ণ বিষয়সমূহ, মূল তথ্য ও পরীক্ষার জন্য দরকারি টিপস নিয়ে সাজানো হয়েছে এই কনসেপ্ট ফাইল।" }
                            showTopicConceptDialog = true
                        }
                    )

                    // 2. Content Button
                    com.example.ui.components.GradientChipButton(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        label = if (isBengali) "কন্টেন্ট" else "Content",
                        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                        onClick = onViewContent
                    )

                    // 3. Solution Button
                    com.example.ui.components.GradientChipButton(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        label = if (isBengali) "সমাধান" else "Solution",
                        gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                        onClick = onViewSolution
                    )

                    // 4. Video Class Button
                    com.example.ui.components.GradientChipButton(
                        icon = Icons.Default.PlayCircle,
                        label = if (isBengali) "ভিডিও ক্লাস" else "Video Class",
                        gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                        onClick = onViewVideo
                    )

                    // 5. Podcast / Audio Button
                    com.example.ui.components.GradientChipButton(
                        icon = Icons.Default.Headphones,
                        label = if (isBengali) "পডকাস্ট অডিও" else "Podcast Audio",
                        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)),
                        onClick = onViewContent
                    )
                }
            }
        }
    }
}

@Composable
fun LearnTopicScreen(
    topic: TopicItem,
    isBengali: Boolean,
    onBack: () -> Unit,
    onStartPractice: (AssessmentMode, String?, String?, Int) -> Unit,
    viewModel: DeshExamViewModel? = null,
    getQuestionCount: (suspend (String) -> Int)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showVideoDialog by remember { mutableStateOf(false) }
    val videoLesson = remember(topic, isBengali) {
        VideoLessonProvider.getVideoForTopic(
            topicId = topic.id,
            topicTitle = topic.getTitle(isBengali),
            subjectId = topic.subjectId,
            topicVideoUrl = topic.videoUrl,
            topicPlaylistJson = topic.playlistJson
        )
    }

    val testScoresVersion by (viewModel?.testScoresVersion?.collectAsState() ?: remember { mutableIntStateOf(0) })
    val videoProgressMap by (viewModel?.videoProgressMap?.collectAsState() ?: remember { mutableStateOf(emptyMap()) })
    val currentSavedProgress = videoProgressMap[videoLesson.safeId] ?: videoProgressMap[topic.id]

    var totalQuestions by remember(topic.id) { 
        mutableStateOf(if (topic.questionCount > 0) topic.questionCount else 50) 
    }

    LaunchedEffect(topic.id) {
        if (getQuestionCount != null) {
            val count = getQuestionCount(topic.id)
            if (count > 0) {
                totalQuestions = count
            }
        }
    }

    val chunkSize = 15
    val baseSets = ((totalQuestions + chunkSize - 1) / chunkSize).coerceIn(2, 10)
    val extraSets = remember(topic.id, testScoresVersion) {
        viewModel?.getTopicExtraSets(topic.id) ?: 0
    }
    val totalSets = baseSets + extraSets

    val finalContent = remember(topic, isBengali) {
        topic.contentData?.takeIf { it.isNotBlank() } ?: buildString {
            append("# ${if (isBengali) topic.titleBengali else topic.titleEnglish}\n\n")
            val sumText = if (isBengali) topic.summaryBengali else topic.summary
            if (sumText.isNotBlank()) {
                append("## সারসংক্ষেপ (Summary)\n$sumText\n\n")
            }
            if (topic.keyPoints.isNotEmpty()) {
                append("## মূল বিষয় ও পয়েন্টসমূহ (Key Points)\n")
                topic.keyPoints.forEach { point ->
                    append("* $point\n")
                }
            }
        }
    }

    val finalSolution = remember(topic, isBengali) {
        if (!topic.solutionData.isNullOrBlank() && 
            !topic.solutionData.contains("এর প্রশ্নোত্তরগুলো এখানে থাকবে") &&
            topic.solutionData.trim() != "## সমাধান" &&
            !topic.solutionData.contains("খুব শীঘ্রই যুক্ত করা হবে")) {
            topic.solutionData
        } else {
            null
        }
    }

    // Check unlocks & scores
    val setScores = remember(topic.id, totalSets, testScoresVersion) {
        (1..totalSets).associateWith { setIdx ->
            viewModel?.getTopicSetScore(topic.id, setIdx) ?: -1
        }
    }

    val setUnlocks = remember(topic.id, totalSets, testScoresVersion, setScores) {
        (1..totalSets).associateWith { setIdx ->
            if (setIdx == 1) true
            else {
                val prevScore = setScores[setIdx - 1] ?: -1
                prevScore >= 60
            }
        }
    }

    val mockScore = remember(topic.id, testScoresVersion) {
        viewModel?.getTopicMockScore(topic.id) ?: -1
    }
    val isMockUnlocked = remember(topic.id, testScoresVersion, setScores) {
        (setScores[1] ?: -1) >= 60 || (viewModel?.isTopicMockUnlocked(topic.id) ?: false)
    }

    val speedScore = remember(topic.id, testScoresVersion) {
        viewModel?.getTopicSpeedScore(topic.id) ?: -1
    }
    val isSpeedUnlocked = remember(topic.id, testScoresVersion, mockScore, isMockUnlocked) {
        mockScore >= 60 || (viewModel?.isTopicSpeedUnlocked(topic.id) ?: false)
    }

    val allSetsCompleted = remember(setScores, totalSets) {
        (1..totalSets).all { (setScores[it] ?: -1) >= 60 }
    }

    // Automatic 100% dynamic set generation: when the last set is passed with >= 60%, automatically materialize the next set!
    val lastSetPassed = (setScores[totalSets] ?: -1) >= 60
    LaunchedEffect(lastSetPassed, totalSets) {
        if (lastSetPassed && totalSets > 0 && viewModel != null) {
            viewModel.generateNewDynamicSet(topic.id)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ScreenHeaderBar(
                title = topic.getTitle(isBengali),
                subtitle = "টপিক স্টাডি ও কুইজ টেস্ট সিরিজ ($totalQuestions টি প্রশ্ন • $totalSets টি সেট)",
                isBengali = isBengali,
                showBackButton = true,
                onBack = onBack
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Edge-to-Edge Hero Banner (0dp corner radius)
            LearnTopicHeroBanner(
                topic = topic,
                totalQuestions = totalQuestions,
                totalSets = totalSets,
                isBengali = isBengali,
                onViewContent = {
                    viewModel?.openContentSolution(
                        title = if (isBengali) topic.titleBengali else topic.titleEnglish,
                        content = finalContent,
                        solution = finalSolution,
                        initialTab = 0,
                        topicId = topic.id
                    )
                },
                onViewSolution = {
                    viewModel?.openContentSolution(
                        title = if (isBengali) topic.titleBengali else topic.titleEnglish,
                        content = finalContent,
                        solution = finalSolution,
                        initialTab = 1,
                        topicId = topic.id
                    )
                },
                onViewVideo = {
                    showVideoDialog = true
                }
            )

            // Content Container with horizontal padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🎬 Video Lecture Action Card (Opens full player & playlist dialog)
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
                    ),
                    border = BorderStroke(
                        1.2.dp,
                        if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF60A5FA).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (viewModel != null) {
                                viewModel.openVideoLesson(videoLesson, topic)
                            } else {
                                showVideoDialog = true
                            }
                        }
                        .testTag("topic_video_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Video Play Icon Bubble
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFFEF4444).copy(alpha = 0.25f) else Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "ভিডিও ক্লাস",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Video Title & Meta
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "ভিডিও লেকচার",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (currentSavedProgress != null && currentSavedProgress.lastPositionMs > 3000L) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ExamGold.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(end = 6.dp)
                                    ) {
                                        Text(
                                            text = "চলমান",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = ExamGold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = videoLesson.safeTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!videoLesson.safeInstructorName.isNullOrBlank()) {
                                Text(
                                    text = "👨‍🏫 ${videoLesson.safeInstructorName}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }
                        }

                        // Watch Button
                        Button(
                            onClick = {
                                if (viewModel != null) {
                                    viewModel.openVideoLesson(videoLesson, topic)
                                } else {
                                    showVideoDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF312E81) else Color(0xFF4F46E5),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = ExamGold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentSavedProgress != null && currentSavedProgress.lastPositionMs > 3000L) "চালিয়ে যান" else "দেখুন",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

            // DYNAMIC AUTOMATIC TEST SETS SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "⚡ এই টপিকের কুইজ টেস্ট সিরিজ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "পূর্ববর্তী সেটে ৬০% পেলে পরবর্তী সেট আনলক হবে",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "মোট $totalSets টি সেট",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Generate Automatic Set Cards with Progressive Locking
            for (setIdx in 1..totalSets) {
                val isUnlocked = setUnlocks[setIdx] == true
                val score = setScores[setIdx] ?: -1
                val isAttempted = score >= 0
                val isPassed = score >= 60
                val isDynamicExtra = setIdx > baseSets

                val startQ = (setIdx - 1) * chunkSize + 1
                val endQ = (setIdx * chunkSize).coerceAtMost(totalQuestions.coerceAtLeast(setIdx * chunkSize))
                val setQCount = if (isDynamicExtra) 15 else (endQ - startQ + 1).coerceIn(10, 15)

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
                        .clickable {
                            if (isUnlocked) {
                                onStartPractice(
                                    AssessmentMode.PRACTICE,
                                    topic.id,
                                    "${topic.getTitle(true)} - কুইজ সেট $setIdx",
                                    setIdx
                                )
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = if (isBengali) 
                                            "🔒 সেট $setIdx আনলক করতে পূর্ববর্তী সেট ${setIdx - 1}-এ অন্তত ৬০% নম্বর অর্জন করতে হবে!" 
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
                                        text = "সেট $setIdx",
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
                                text = "$setQCount প্রশ্ন • ১৫ মি. • ৫০ XP",
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
                                            text = "সেট ${setIdx - 1}-এ ৬০% পেলে আনলক হবে",
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
                                            text = "✓ $score% স্কোর অর্জিত (উত্তীর্ণ)",
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
                                            text = "$score% স্কোর • পুনরায় চেষ্টা করুন (৬০% চাই)",
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
                                        onStartPractice(
                                            AssessmentMode.PRACTICE,
                                            topic.id,
                                            "${topic.getTitle(true)} - কুইজ সেট $setIdx",
                                            setIdx
                                        )
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

            // Test: Topic Full Mock Test (30 Questions)
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        !isMockUnlocked -> if (isDark) Color(0xFF131B2A) else Color(0xFFF1F5F9)
                        mockScore >= 60 -> if (isDark) Color(0xFF0F2D1F) else Color(0xFFECFDF5)
                        else -> if (isDark) Color(0xFF0F2A28) else Color(0xFFF0FDFA)
                    }
                ),
                border = BorderStroke(
                    1.5.dp, 
                    if (!isMockUnlocked) Color.Gray.copy(alpha = 0.4f) else if (isDark) Color(0xFF0D9488) else Color(0xFF99F6E4)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isMockUnlocked) {
                            onStartPractice(
                                AssessmentMode.MOCK_TEST,
                                topic.id,
                                "${topic.getTitle(true)} - ৩০ নম্বরের মক টেস্ট",
                                1
                            )
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (isBengali) 
                                        "🔒 মক টেস্ট আনলক করতে অন্তত সেট ১-এ ৬০% নম্বর পেতে হবে!" 
                                    else 
                                        "🔒 Score 60%+ in Set 1 to unlock Topic Full Mock Test!"
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
                            Icon(
                                imageVector = if (isMockUnlocked) Icons.AutoMirrored.Filled.Assignment else Icons.Default.Lock,
                                contentDescription = "Mock Test",
                                tint = if (isMockUnlocked) Color(0xFF0D9488) else Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "টপিক পূর্ণাঙ্গ মক টেস্ট",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isMockUnlocked) (if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F766E)) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "৩০ প্রশ্ন • ৩০ মি. • ১০০ XP",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!isMockUnlocked) {
                            Text(
                                text = "🔒 কুইজ সেট ১-এ ৬০% পেলে আনলক হবে",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD97706))
                            )
                        } else if (mockScore >= 0) {
                            Text(
                                text = "✓ অর্জিত স্কোর: $mockScore%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CorrectGreen)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    if (isMockUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF0D9488), Color(0xFF0F766E))
                                    )
                                )
                                .clickable {
                                    onStartPractice(
                                        AssessmentMode.MOCK_TEST,
                                        topic.id,
                                        "${topic.getTitle(true)} - ৩০ নম্বরের মক টেস্ট",
                                        1
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (mockScore >= 0) ("পুনরায় মক") else ("মক টেস্ট শুরু"),
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

            // Test: Rapid Speed Challenge
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        !isSpeedUnlocked -> if (isDark) Color(0xFF131B2A) else Color(0xFFF1F5F9)
                        speedScore >= 60 -> if (isDark) Color(0xFF0F2D1F) else Color(0xFFECFDF5)
                        else -> if (isDark) Color(0xFF2C2210) else Color(0xFFFFFBEB)
                    }
                ),
                border = BorderStroke(1.5.dp, if (!isSpeedUnlocked) Color.Gray.copy(alpha = 0.4f) else ExamGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isSpeedUnlocked) {
                            onStartPractice(
                                AssessmentMode.SPEED_BLITZ,
                                topic.id,
                                "${topic.getTitle(true)} - স্পিড টেস্ট",
                                1
                            )
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (isBengali) 
                                        "🔒 স্পিড টেস্ট আনলক করতে মক টেস্ট অথবা সেট ১-এ ৬০% নম্বর পেতে হবে!" 
                                    else 
                                        "🔒 Score 60%+ in Mock Test or Set 1 to unlock Speed Blitz!"
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
                            Icon(
                                imageVector = if (isSpeedUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                contentDescription = "Challenge",
                                tint = if (isSpeedUnlocked) ExamGold else Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "টপিক র‍্যাপিড স্পিড টেস্ট",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSpeedUnlocked) (if (isDark) ExamGold else Color(0xFF92400E)) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "১৫ প্রশ্ন • স্পিড চ্যালেঞ্জ • ১৫০ XP",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (isSpeedUnlocked) (if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!isSpeedUnlocked) {
                            Text(
                                text = "🔒 মক টেস্টে ৬০% পেলে আনলক হবে",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD97706))
                            )
                        } else if (speedScore >= 0) {
                            Text(
                                text = "✓ অর্জিত স্কোর: $speedScore%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CorrectGreen)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (isSpeedUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                )
                                .clickable {
                                    onStartPractice(
                                        AssessmentMode.SPEED_BLITZ,
                                        topic.id,
                                        "${topic.getTitle(true)} - স্পিড টেস্ট",
                                        1
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (speedScore >= 0) ("পুনরায় স্পিড") else ("স্পিড চ্যালেঞ্জ"),
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

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showVideoDialog) {
        VideoLessonPlayerDialog(
            lesson = videoLesson,
            onDismiss = { showVideoDialog = false },
            onStartQuiz = {
                onStartPractice(
                    AssessmentMode.PRACTICE,
                    topic.id,
                    "${topic.getTitle(true)} - ভিডিও কুইজ টেস্ট",
                    1
                )
            },
            viewModel = viewModel
        )
    }
}
}

