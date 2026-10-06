package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

/**
 * Visual Learning Roadmap Component for Each Subject:
 * - Shows overall subject progress and Madhyamik 2026 exam readiness
 * - Displays dynamically recommended practice sessions (Next Topic, Weak Area, Boss Battle, Full Mock, Speed Drill)
 * - Displays sequential connected chapter milestones: Completed, In-Progress, and Upcoming Topics
 */
@Composable
fun SubjectRoadmapComponent(
    subject: SubjectItem,
    chapters: List<ChapterItem>,
    topics: List<TopicItem>,
    recommendedPractices: List<RecommendedPracticeSession>,
    isBengali: Boolean,
    onOpenTopicStudy: (String) -> Unit,
    onStartAssessment: (AssessmentMode, String?, String?) -> Unit,
    modifier: Modifier = Modifier,
    questions: List<com.example.data.local.QuestionEntity> = emptyList(),
    viewModel: com.example.ui.viewmodel.DeshExamViewModel? = null
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, COMPLETED, IN_PROGRESS, UPCOMING
    val expandedChapters = remember { mutableStateMapOf<String, Boolean>() }

    // Initialize all chapters as expanded by default if not set
    LaunchedEffect(chapters) {
        chapters.forEach { chapter ->
            if (!expandedChapters.containsKey(chapter.id)) {
                expandedChapters[chapter.id] = true
            }
        }
    }

    val completedChaptersCount = chapters.count { it.bossCompleted || it.completedTopics >= it.totalTopics }
    val totalTopicsCount = topics.size
    val completedTopicsCount = topics.count { it.masteryPercentage >= 80 }
    val inProgressTopicsCount = topics.count { it.isUnlocked && it.masteryPercentage < 80 }
    val upcomingTopicsCount = topics.count { !it.isUnlocked }

    val filteredChapters = when (selectedFilter) {
        "COMPLETED" -> chapters.filter { it.bossCompleted || it.completedTopics >= it.totalTopics }
        "IN_PROGRESS" -> chapters.filter { (it.completedTopics > 0 || it.bossUnlocked) && !(it.bossCompleted || it.completedTopics >= it.totalTopics) }
        "UPCOMING" -> chapters.filter { it.completedTopics == 0 && !it.bossUnlocked && !it.bossCompleted }
        else -> chapters
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_roadmap_component"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Subject Header & Exam Readiness Tracker
        SubjectRoadmapHeroCard(
            subject = subject,
            totalChapters = chapters.size,
            completedChapters = completedChaptersCount,
            totalTopics = totalTopicsCount,
            completedTopics = completedTopicsCount,
            inProgressTopics = inProgressTopicsCount,
            upcomingTopics = upcomingTopicsCount,
            isBengali = isBengali
        )

        // 2. Recommended Practice Sessions to Stay on Track for Madhyamik
        if (recommendedPractices.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Recommendations",
                            tint = ExamFlame,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "প্রস্তাবিত প্র্যাকটিস সেশন",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ExamFlame.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isBengali) {
                                if (subject.id.startsWith("c11")) "একাদশ শ্রেণির বার্ষিক পরীক্ষা"
                                else if (subject.id.startsWith("c12")) "উচ্চমাধ্যমিক ২০২৬ ট্র্যাকার"
                                else "মাধ্যমিক ২০২৬ ট্র্যাকার"
                            } else {
                                if (subject.id.startsWith("c11")) "Class 11 Annual Exam"
                                else if (subject.id.startsWith("c12")) "HS 2026 Target"
                                else "Madhyamik 2026 Target"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ExamFlame,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recommendedPractices) { session ->
                        RecommendedPracticeCard(
                            session = session,
                            isBengali = isBengali,
                            onStart = {
                                onStartAssessment(
                                    session.mode,
                                    session.targetId,
                                    session.title
                                )
                            }
                        )
                    }
                }
            }
        }

        // 3. Roadmap Milestone Status Filter Tabs
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "অধ্যায় ও টপিক মাইলস্টোন",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "ALL" to ("সব (${chapters.size})"),
                    "COMPLETED" to ("সম্পন্ন ($completedChaptersCount)"),
                    "IN_PROGRESS" to ("চলমান (${chapters.size - completedChaptersCount - (chapters.count { it.completedTopics == 0 && !it.bossUnlocked })})"),
                    "UPCOMING" to ("আসন্ন")
                )

                items(filters) { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(subject.colorHex),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }

        // 4. Sequential Connected Chapter Roadmap Nodes
        if (filteredChapters.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "এই ফিল্টারে কোনো অধ্যায় পাওয়া যায়নি।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                filteredChapters.forEachIndexed { index, chapter ->
                    val isLast = index == filteredChapters.size - 1
                    val chapterTopics = topics.filter { it.chapterId == chapter.id }
                    val isExpanded = expandedChapters[chapter.id] ?: true

                    ChapterRoadmapCard(
                        chapter = chapter,
                        topics = chapterTopics,
                        subjectColorHex = subject.colorHex,
                        isBengali = isBengali,
                        isExpanded = isExpanded,
                        isLastInList = isLast,
                        onToggleExpand = {
                            expandedChapters[chapter.id] = !isExpanded
                        },
                        onOpenStudy = onOpenTopicStudy,
                        onStartTopicPractice = { topicId, title ->
                            onStartAssessment(AssessmentMode.PRACTICE, topicId, title)
                        },
                        onStartBossBattle = {
                            onStartAssessment(
                                AssessmentMode.BOSS_BATTLE,
                                chapter.id,
                                "🔥 অধ্যায় বস ব্যাটল: ${chapter.getTitle(true)}"
                            )
                        },
                        questions = questions,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectRoadmapHeroCard(
    subject: SubjectItem,
    totalChapters: Int,
    completedChapters: Int,
    totalTopics: Int,
    completedTopics: Int,
    inProgressTopics: Int,
    upcomingTopics: Int,
    isBengali: Boolean
) {
    val progressFraction by animateFloatAsState(
        targetValue = if (totalChapters > 0) completedChapters.toFloat() / totalChapters else 0f,
        label = "subject_progress_anim"
    )

    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_roadmap_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(subject.colorHex),
                            Color(subject.colorHex).copy(alpha = 0.85f),
                            PrimaryIndigoDark
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Top row with subject title and badge
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = com.example.ui.components.SubjectSvgIconProvider.getIconForSubjectCode(subject.code),
                                contentDescription = subject.name,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = subject.getTitle(isBengali),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                            Text(
                                text = if (isBengali) {
                                    if (subject.id.startsWith("c11")) "WBCHSE একাদশ শ্রেণি পাঠ্যক্রম রোডম্যাপ"
                                    else if (subject.id.startsWith("c12")) "WBCHSE দ্বাদশ শ্রেণি পাঠ্যক্রম রোডম্যাপ"
                                    else "WBBSE দশম শ্রেণি পাঠ্যক্রম রোডম্যাপ"
                                } else {
                                    if (subject.id.startsWith("c11")) "WBCHSE Class 11 Syllabus Roadmap"
                                    else if (subject.id.startsWith("c12")) "WBCHSE Class 12 Syllabus Roadmap"
                                    else "WBBSE Class 10 Syllabus Roadmap"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${subject.masteryPercentage}% মাস্টারি",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Progress Bar with Label
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সিলেবাস অগ্রগতি: $completedChapters / $totalChapters অধ্যায় সম্পূর্ণ",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ExamGold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = ExamGold,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }

                // 3 Stat Pills Row: Completed vs In-Progress vs Upcoming Topics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoadmapStatPill(
                        count = completedTopics,
                        label = "সম্পন্ন টপিক",
                        icon = Icons.Default.CheckCircle,
                        accentColor = CorrectGreen,
                        modifier = Modifier.weight(1f)
                    )

                    RoadmapStatPill(
                        count = inProgressTopics,
                        label = "চলমান টপিক",
                        icon = Icons.Default.PlayCircle,
                        accentColor = ExamGold,
                        modifier = Modifier.weight(1f)
                    )

                    RoadmapStatPill(
                        count = upcomingTopics,
                        label = "আসন্ন টপিক",
                        icon = Icons.Default.Lock,
                        accentColor = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun RoadmapStatPill(
    count: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.25f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecommendedPracticeCard(
    session: RecommendedPracticeSession,
    isBengali: Boolean,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.45f
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
        modifier = modifier
            .width(280.dp)
            .border(
                width = if (session.isPriority) 1.5.dp else 1.dp,
                color = if (session.isPriority) ExamFlame else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("recommended_practice_card_${session.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tag & Priority Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(session.tagColorHex).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isBengali) session.tagBengali else session.tagEnglish,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = Color(session.tagColorHex),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Duration",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${session.durationMinutes} মি",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+${session.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ExamFlame
                    )
                }
            }

            Text(
                text = session.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = session.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                onClick = onStart,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (session.isPriority) PrimaryIndigo else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (session.isPriority) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("start_rec_session_btn_${session.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "অনুশীলন শুরু করুন",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun ChapterRoadmapCard(
    chapter: ChapterItem,
    topics: List<TopicItem>,
    subjectColorHex: Long,
    isBengali: Boolean,
    isExpanded: Boolean,
    isLastInList: Boolean,
    onToggleExpand: () -> Unit,
    onOpenStudy: (String) -> Unit,
    onStartTopicPractice: (String, String) -> Unit,
    onStartBossBattle: () -> Unit,
    modifier: Modifier = Modifier,
    questions: List<com.example.data.local.QuestionEntity> = emptyList(),
    viewModel: com.example.ui.viewmodel.DeshExamViewModel? = null
) {
    val isCompleted = chapter.bossCompleted || (chapter.completedTopics >= chapter.totalTopics && chapter.totalTopics > 0)
    val isInProgress = !isCompleted && (chapter.completedTopics > 0 || chapter.bossUnlocked)
    val isLocked = !isCompleted && !isInProgress

    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val statusContainerColor = if (isDark) Color(0xFF1E293B) else Color.White

    val statusBorderColor = when {
        isCompleted -> Color(0xFF10B981)
        isInProgress -> Color(subjectColorHex)
        else -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    }

    var showConceptDialog by remember { mutableStateOf(false) }
    var conceptTitle by remember { mutableStateOf("") }
    var conceptSummaryText by remember { mutableStateOf("") }

    var showSolutionDialog by remember { mutableStateOf(false) }
    var solutionTitle by remember { mutableStateOf("") }
    var solutionText by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = statusContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isInProgress) 1.5.dp else 1.dp,
                color = statusBorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .animateContentSize()
            .testTag("chapter_roadmap_card_${chapter.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chapter Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chapter Number Circle / Milestone Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> CorrectGreen
                                isInProgress -> Color(subjectColorHex)
                                else -> if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed Chapter",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Chapter",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${chapter.chapterNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isCompleted -> CorrectGreen.copy(alpha = 0.2f)
                                isInProgress -> Color(subjectColorHex).copy(alpha = 0.2f)
                                else -> if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
                            }
                        ) {
                            Text(
                                text = when {
                                    isCompleted -> "✅ সম্পূর্ণ অধ্যায়"
                                    isInProgress -> "⚡ চলমান মাইলস্টোন"
                                    else -> "🔒 আসন্ন অধ্যায়"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = when {
                                    isCompleted -> if (isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)
                                    isInProgress -> if (isDark) Color(0xFF93C5FD) else Color(subjectColorHex)
                                    else -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Topics Progress count
                        Text(
                            text = "${chapter.completedTopics}/${chapter.totalTopics} টপিক সম্পন্ন",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "অধ্যায় ${chapter.chapterNumber.toBengaliDigits()}: ${chapter.getTitle(true)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = chapter.getDescription(isBengali),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                        maxLines = if (isExpanded) 3 else 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Chapter Stats Badges Bar: topics, questions, quizzes, mock tests
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val chapterTopicsCount = topics.size
                        val chapterQuestionsCount = topics.sumOf { topic ->
                            questions.count { it.topicId == topic.id }.let {
                                if (it > 0) it else if (topic.questionCount > 0) topic.questionCount else 15
                            }
                        }
                        // Topics badge
                        ChapterStatPill(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            label = if (isBengali) "${chapterTopicsCount.toBengaliDigits()}টি টপিক" else "$chapterTopicsCount Topics",
                            color = Color(subjectColorHex),
                            isDark = isDark
                        )
                        // Questions badge
                        ChapterStatPill(
                            icon = Icons.Default.Quiz,
                            label = if (isBengali) "${chapterQuestionsCount.toBengaliDigits()}টি প্রশ্ন" else "$chapterQuestionsCount Qs",
                            color = Color(subjectColorHex),
                            isDark = isDark
                        )
                        // Quizzes badge
                        ChapterStatPill(
                            icon = Icons.Default.Assignment,
                            label = if (isBengali) "${chapterTopicsCount.toBengaliDigits()}টি কুইজ" else "$chapterTopicsCount Quizzes",
                            color = Color(subjectColorHex),
                            isDark = isDark
                        )
                        // Mock test badge
                        ChapterStatPill(
                            icon = Icons.Default.EmojiEvents,
                            label = if (isBengali) "১টি মক টেস্ট" else "1 Mock Test",
                            color = Color(subjectColorHex),
                            isDark = isDark
                        )
                    }
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            }

            // Chapter Progress Indicator
            Spacer(modifier = Modifier.height(10.dp))
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
                    color = if (isCompleted) CorrectGreen else Color(subjectColorHex),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Text(
                    text = "${chapter.masteryPercentage}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Expandable List of Topics & Boss Section
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 0.8.dp
                    )

                    Text(
                        text = "টপিক রোডম্যাপ ও অনুশীলন ট্রেইল:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Topic Nodes
                    topics.forEachIndexed { topicIndex, topic ->
                        val isTopicMastered = topic.masteryPercentage >= 80
                        val isNextTarget = topic.isUnlocked && !isTopicMastered &&
                                topics.take(topicIndex).all { it.masteryPercentage >= 80 }

                        val calculatedQCount = questions.count { it.topicId == topic.id }.let {
                            if (it > 0) it else if (topic.questionCount > 0) topic.questionCount else 15
                        }

                        val topicNumStr = if (isBengali) {
                            "${chapter.chapterNumber.toBengaliDigits()}.${(topicIndex + 1).toBengaliDigits()}"
                        } else {
                            "${chapter.chapterNumber}.${topicIndex + 1}"
                        }

                        RoadmapTopicItemRow(
                            topic = topic,
                            isNextTarget = isNextTarget,
                            isBengali = isBengali,
                            questionCount = calculatedQCount,
                            topicNumber = topicNumStr,
                            index = topicIndex,
                            onConceptClick = {
                                conceptTitle = topic.getTitle(isBengali)
                                conceptSummaryText = (topic.contentData ?: "").ifBlank { "টপিকের মূল ধারণা ও প্রয়োজনীয় কনসেপ্ট নোটস।" }
                                showConceptDialog = true
                            },
                            onSolutionClick = {
                                solutionTitle = topic.getTitle(isBengali)
                                val sol = topic.solutionData
                                solutionText = if (!sol.isNullOrBlank() && !sol.contains("খুব শীঘ্রই")) {
                                    sol
                                } else {
                                    "## ${topic.getTitle(isBengali)} - সমাধান ও প্রশ্নোত্তর\n\n" +
                                    "এই টপিকের সকল গুরুত্বপূর্ণ ও বিগত বছরের প্রশ্নের ব্যাখ্যা ও মডেল সমাধান নিচে দেওয়া হলো:\n\n" +
                                    (topic.contentData ?: topic.summary)
                                }
                                showSolutionDialog = true
                            },
                            onVideoClick = {
                                try {
                                    val lesson = VideoLessonProvider.getVideoForTopic(
                                        topicId = topic.id,
                                        topicTitle = topic.getTitle(isBengali),
                                        subjectId = topic.subjectId,
                                        topicVideoUrl = topic.videoUrl,
                                        topicPlaylistJson = topic.playlistJson
                                    )
                                    viewModel?.openVideoLesson(lesson, topic)
                                } catch (_: Exception) {
                                    val fallback = VideoLessonProvider.getVideoForTopic(topic.id)
                                    viewModel?.openVideoLesson(fallback, topic)
                                }
                            },
                            onStartPractice = {
                                onStartTopicPractice(
                                    topic.id,
                                    "${topic.getTitle(true)} - অনুশীলন"
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (showConceptDialog) {
        ConceptDetailDialog(
            title = conceptTitle,
            conceptSummary = conceptSummaryText,
            onDismiss = { showConceptDialog = false }
        )
    }

    if (showSolutionDialog) {
        ConceptDetailDialog(
            title = solutionTitle,
            subtitle = "টপিক সমাধান ও প্রশ্নোত্তর নোটস",
            conceptSummary = solutionText,
            onDismiss = { showSolutionDialog = false }
        )
    }
}

@Composable
fun RoadmapTopicItemRow(
    topic: TopicItem,
    isNextTarget: Boolean,
    isBengali: Boolean,
    questionCount: Int,
    topicNumber: String,
    index: Int,
    onConceptClick: () -> Unit,
    onSolutionClick: () -> Unit,
    onVideoClick: () -> Unit,
    onStartPractice: () -> Unit
) {
    val isMastered = topic.masteryPercentage >= 80
    val isUnlocked = topic.isUnlocked
    val isLocked = !isUnlocked
    val isDark = MaterialTheme.colorScheme.background.red < 0.45f

    val progress = (topic.masteryPercentage.coerceIn(0, 100)) / 100f

    // Subtle distinct linear gradients for cards based on index
    val cardGradientsLight = listOf(
        Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))), // Subtle Indigo
        Brush.linearGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))), // Subtle Emerald
        Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))), // Subtle Amber
        Brush.linearGradient(listOf(Color(0xFFECFEFF), Color(0xFFCFFAFE))), // Subtle Cyan
        Brush.linearGradient(listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE))), // Subtle Violet
        Brush.linearGradient(listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3))), // Subtle Pink
        Brush.linearGradient(listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6)))  // Subtle Rose
    )
    
    val cardGradientsDark = listOf(
        Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))), // Dark Indigo
        Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF0F172A))), // Dark Emerald
        Brush.linearGradient(listOf(Color(0xFF78350F), Color(0xFF0F172A))), // Dark Amber
        Brush.linearGradient(listOf(Color(0xFF164E63), Color(0xFF0F172A))), // Dark Cyan
        Brush.linearGradient(listOf(Color(0xFF4C1D95), Color(0xFF0F172A))), // Dark Violet
        Brush.linearGradient(listOf(Color(0xFF831843), Color(0xFF0F172A))), // Dark Pink
        Brush.linearGradient(listOf(Color(0xFF881337), Color(0xFF0F172A)))  // Dark Rose
    )

    val cardGradient = if (isDark) {
        cardGradientsDark[index % cardGradientsDark.size]
    } else {
        cardGradientsLight[index % cardGradientsLight.size]
    }

    val lockedGradient = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)))
    }

    val cardBorderColor = when {
        isLocked -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
        isMastered -> if (isDark) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFFA7F3D0)
        isNextTarget -> if (isDark) Color(0xFF818CF8) else PrimaryIndigo
        else -> if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
    }

    val titleColor = when {
        !isUnlocked -> {
            if (isDark) Color(0xFF64748B)
            else Color(0xFF64748B)
        }

        isMastered -> {
            if (isDark) Color(0xFFA7F3D0)
            else Color(0xFF065F46)
        }

        isNextTarget -> {
            if (isDark) Color(0xFFF5F3FF)
            else Color(0xFF1E1B4B)
        }

        else -> {
            if (isDark) Color.White
            else Color(0xFF0F172A)
        }
    }

    val secondaryColor = if (isDark) {
        Color(0xFF94A3B8)
    } else {
        Color(0xFF64748B)
    }

    // Beautiful linear gradients for the number circle backgrounds
    val numberGradients = listOf(
        Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))), // Indigo
        Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))), // Emerald
        Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))), // Amber
        Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0284C7))), // Cyan
        Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))), // Violet
        Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFD946EF))), // Pink-Purple
        Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFE11D48)))  // Rose-Red
    )
    val numberGradient = numberGradients[index % numberGradients.size]

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = if (isNextTarget) 1.5.dp else 1.dp,
            color = cardBorderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("roadmap_topic_row_${topic.id}")
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isLocked) lockedGradient else cardGradient)
                .padding(14.dp)
        ) {

            // =========================================================
            // TOP SECTION
            // =========================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Topic Number / Status with gradients!
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isLocked -> {
                                    if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                }
                                else -> Color.Transparent // to let the gradient show
                            }
                        )
                        .then(
                            if (!isLocked) {
                                Modifier.background(numberGradient)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    when {
                        isMastered -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Mastered",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        isLocked -> {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        else -> {
                            Text(
                                text = topicNumber,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Topic Information
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = topic.getTitle(isBengali),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isNextTarget) {
                                    FontWeight.Black
                                } else {
                                    FontWeight.Bold
                                }
                            ),
                            color = titleColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (isNextTarget) {

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryIndigo
                            ) {
                                Text(
                                    text = "পরবর্তী",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(
                                        horizontal = 6.dp,
                                        vertical = 3.dp
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(13.dp)
                        )

                        Text(
                            text = "${questionCount.toBengaliDigits()}টি প্রশ্ন",
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryColor
                        )

                        Text(
                            text = "•",
                            color = secondaryColor
                        )

                        Text(
                            text = when {
                                isMastered -> "মাস্টারড"
                                !isUnlocked -> "লকড"
                                else -> "চলমান"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = when {
                                isMastered -> CorrectGreen
                                !isUnlocked -> secondaryColor
                                else -> PrimaryIndigo
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // =====================================================
                // CIRCULAR PROGRESS
                // =====================================================

                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {

                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 5.dp,
                        color = when {
                            isMastered -> CorrectGreen
                            isNextTarget -> PrimaryIndigo
                            isUnlocked -> PrimaryIndigo
                            else -> Color(0xFF94A3B8)
                        },
                        trackColor = if (isDark) {
                            Color(0xFF334155)
                        } else {
                            Color(0xFFE2E8F0)
                        }
                    )

                    Text(
                        text = "${topic.masteryPercentage}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        ),
                        color = titleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================
            // ACTION BUTTONS (4 chip buttons instead of readout/practice)
            // =========================================================

            if (isUnlocked) {

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
                        modifier = Modifier.clickable { onConceptClick() }
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
                        modifier = Modifier.clickable { onSolutionClick() }
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
                        modifier = Modifier.clickable { onVideoClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Video Lesson",
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
                            .clickable { onStartPractice() }
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

            } else {

                // Locked state
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = if (isDark) {
                        Color.White.copy(alpha = 0.04f)
                    } else {
                        Color(0xFFF1F5F9)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Text(
                            text = "এই টপিকটি এখনও লক করা",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = secondaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoadmapBossCitadelWaypoint(
    chapter: ChapterItem,
    isBengali: Boolean,
    onStartBoss: () -> Unit
) {
    val isCompleted = chapter.bossCompleted
    val isUnlocked = chapter.bossUnlocked

    val isDark = MaterialTheme.colorScheme.background.red < 0.45f
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = when {
            isCompleted -> if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFF0FDF4)
            isUnlocked -> if (isDark) Color(0xFF78350F).copy(alpha = 0.2f) else Color(0xFFFFFBEB)
            else -> if (isDark) Color.White.copy(alpha = 0.03f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isCompleted -> Color(0xFF10B981).copy(alpha = if (isDark) 0.4f else 0.3f)
                isUnlocked -> ExamGold.copy(alpha = if (isDark) 0.4f else 0.6f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.2f else 0.5f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("roadmap_boss_waypoint_${chapter.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> CorrectGreen
                            isUnlocked -> ExamGold
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.EmojiEvents else if (isUnlocked) Icons.Default.Shield else Icons.Default.Lock,
                    contentDescription = "Boss Citadel",
                    tint = if (isCompleted) Color.White else if (isUnlocked) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "অধ্যায় সমাপনী পরীক্ষা: ${chapter.title}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isCompleted) Color(0xFF064E3B) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when {
                        isCompleted -> "🏆 সমাপনী পরীক্ষা সম্পন্ন! অধ্যায় মাস্টারড।"
                        isUnlocked -> "⚔️ সমাপনী পরীক্ষা প্রস্তুত! সম্পন্ন করুন।"
                        else -> "🔒 সমাপনী পরীক্ষা লক করা। ৭০% টপিক মাস্টরিতে আনলক হবে।"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCompleted) Color(0xFF064E3B).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isUnlocked) {
                Button(
                    onClick = onStartBoss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) CorrectGreen else ExamGold,
                        contentColor = if (isCompleted) Color.White else Color(0xFF78350F)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("roadmap_boss_btn_${chapter.id}")
                ) {
                    Text(
                        text = if (isCompleted) ("রি-ম্যাচ") else ("যুদ্ধ শুরু"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun ChapterStatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isDark) color.copy(alpha = 0.15f) else color.copy(alpha = 0.08f),
        border = BorderStroke(
            width = 0.75.dp,
            color = if (isDark) color.copy(alpha = 0.4f) else color.copy(alpha = 0.25f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) color.copy(alpha = 0.9f) else color,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isDark) Color.White.copy(alpha = 0.9f) else color
            )
        }
    }
}
