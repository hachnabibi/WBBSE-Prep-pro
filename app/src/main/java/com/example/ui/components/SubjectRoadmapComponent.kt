package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    modifier: Modifier = Modifier
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
                        }
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
    val isDark = isSystemInDarkTheme()
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
    modifier: Modifier = Modifier
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

                        RoadmapTopicItemRow(
                            topic = topic,
                            isNextTarget = isNextTarget,
                            isBengali = isBengali,
                            onOpenStudy = { onOpenStudy(topic.id) },
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
}

@Composable
fun RoadmapTopicItemRow(
    topic: TopicItem,
    isNextTarget: Boolean,
    isBengali: Boolean,
    onOpenStudy: () -> Unit,
    onStartPractice: () -> Unit
) {
    val isMastered = topic.masteryPercentage >= 80
    val isUnlocked = topic.isUnlocked
    val isDark = isSystemInDarkTheme()

    val progress = (topic.masteryPercentage.coerceIn(0, 100)) / 100f

    val cardBackground = when {
        isNextTarget -> {
            if (isDark) Color(0xFF1E1B4B)
            else Color(0xFFF5F3FF)
        }

        isMastered -> {
            if (isDark) Color(0xFF052E2B)
            else Color(0xFFF0FDF4)
        }

        isUnlocked -> {
            if (isDark) Color(0xFF172033)
            else Color.White
        }

        else -> {
            if (isDark) Color(0xFF111827)
            else Color(0xFFF8FAFC)
        }
    }

    val cardBorder = when {
        isNextTarget -> {
            if (isDark) Color(0xFF818CF8)
            else PrimaryIndigo
        }

        isMastered -> {
            if (isDark) Color(0xFF059669)
            else Color(0xFFA7F3D0)
        }

        isUnlocked -> {
            if (isDark) Color(0xFF334155)
            else Color(0xFFE2E8F0)
        }

        else -> {
            if (isDark) Color(0xFF1E293B)
            else Color(0xFFE2E8F0)
        }
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

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBackground,
        border = BorderStroke(
            width = if (isNextTarget) 1.5.dp else 1.dp,
            color = cardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("roadmap_topic_row_${topic.id}")
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {

            // =========================================================
            // TOP SECTION
            // =========================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Topic Number / Status
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isMastered -> {
                                    if (isDark) {
                                        Color(0xFF065F46)
                                    } else {
                                        Color(0xFFD1FAE5)
                                    }
                                }

                                isNextTarget -> PrimaryIndigo

                                isUnlocked -> {
                                    if (isDark) {
                                        Color(0xFF312E81)
                                    } else {
                                        Color(0xFFE0E7FF)
                                    }
                                }

                                else -> {
                                    if (isDark) {
                                        Color(0xFF334155)
                                    } else {
                                        Color(0xFFE2E8F0)
                                    }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    when {

                        isMastered -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Mastered",
                                tint = if (isDark) {
                                    Color(0xFFA7F3D0)
                                } else {
                                    CorrectGreen
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        !isUnlocked -> {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = if (isDark) {
                                    Color(0xFF94A3B8)
                                } else {
                                    Color(0xFF64748B)
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        isNextTarget -> {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Next Topic",
                                tint = Color.White,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        else -> {
                            Text(
                                text = "${topic.orderIndex}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = if (isDark) {
                                    Color(0xFFC7D2FE)
                                } else {
                                    PrimaryIndigo
                                }
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
                            text = "${topic.questionCount}টি প্রশ্ন",
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
            // ACTION BUTTONS
            // =========================================================

            if (isUnlocked) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    // Study
                    OutlinedButton(
                        onClick = onOpenStudy,
                        shape = RoundedCornerShape(9.dp),
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 0.dp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "পড়ুন",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Practice
                    Button(
                        onClick = onStartPractice,
                        shape = RoundedCornerShape(9.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryIndigo,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 0.dp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "অনুশীলন",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
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

    val isDark = isSystemInDarkTheme()
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
