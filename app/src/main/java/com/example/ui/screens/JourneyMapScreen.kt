package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.BossBattleCard
import com.example.ui.components.InteractiveJourneyMap
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.components.SubjectRoadmapComponent
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel

enum class JourneyViewMode {
    ROADMAP,
    ADVENTURE,
    LIST
}

@Composable
fun JourneyMapScreen(
    viewModel: DeshExamViewModel,
    activeSubjectStudents: Map<String, Int>,
    isBengali: Boolean,
    onOpenTopicStudy: (String) -> Unit,
    onStartAssessment: (AssessmentMode, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()
    val chaptersFlow = viewModel.repository.getChapters(selectedSubjectId)
    val chapters by chaptersFlow.collectAsState(initial = emptyList())
    val selectedChapterId by viewModel.selectedChapterId.collectAsState()
    val topicsFlow = viewModel.repository.getTopics(selectedChapterId)
    val topics by topicsFlow.collectAsState(initial = emptyList())
    val subjectTopicsFlow = viewModel.repository.getTopicsForSubject(selectedSubjectId)
    val subjectTopics by subjectTopicsFlow.collectAsState(initial = emptyList())

    val currentSubject = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()
    val currentChapter = chapters.find { it.id == selectedChapterId } ?: chapters.firstOrNull()
    var viewMode by remember { mutableStateOf(JourneyViewMode.LIST) }

    val recommendedPractices = remember(selectedSubjectId, chapters, subjectTopics) {
        viewModel.repository.getRecommendedPracticesForSubject(selectedSubjectId, chapters, subjectTopics)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("journey_map_screen")
    ) {
        ScreenHeaderBar(
            title = "লার্নিং রোডম্যাপ",
            subtitle = if (isBengali) "${currentSubject?.getTitle(isBengali) ?: ""} অধ্যায় ও বিষয়সমূহ" else "${currentSubject?.getTitle(isBengali) ?: ""} Chapters & Topics",
            isBengali = isBengali,
            showBackButton = true,
            onBack = { viewModel.goBack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(top = 0.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val activeCount = activeSubjectStudents[selectedSubjectId] ?: 0

            // 0. Journey Hero Banner (Edge-to-Edge with 0dp Corner Radius)
            item {
                JourneyHeroBanner(
                    currentSubject = currentSubject,
                    chaptersCount = chapters.size,
                    activeCount = activeCount,
                    isBengali = isBengali,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Active Count Chip
            if (activeCount > 0) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$activeCount জন শিক্ষার্থী এখন এই বিষয়টি পড়ছে",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF064E3B)
                                )
                            }
                        }
                    }
                }
            }

            // 1. Subject Selector
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subjects) { subject ->
                            val isSelected = subject.id == selectedSubjectId
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectSubject(subject.id)
                                },
                                label = {
                                    Text(
                                        text = subject.getTitle(isBengali),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = com.example.ui.components.SubjectSvgIconProvider.getIconForSubjectCode(subject.code),
                                        contentDescription = subject.getTitle(isBengali),
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) Color.White else Color(subject.colorHex)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(subject.colorHex),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("subject_chip_${subject.id}")
                            )
                        }
                    }
                }
            }

        // 2. Chapter Selector Tabs (Visible when in Chapter Adventure or List mode)
        if (viewMode != JourneyViewMode.ROADMAP && chapters.isNotEmpty()) {
            item {
                ScrollableTabRow(
                    selectedTabIndex = chapters.indexOfFirst { it.id == selectedChapterId }.coerceAtLeast(0),
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    divider = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    chapters.forEach { chapter ->
                        val isSelected = chapter.id == selectedChapterId
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.selectChapter(chapter.id) },
                            text = {
                                Text(
                                    text = "অধ্যায় ${chapter.chapterNumber.toBengaliDigits()}: ${chapter.getTitle(true)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.testTag("chapter_tab_${chapter.id}")
                        )
                    }
                }
            }
        }

        // View Mode Switcher Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (viewMode) {
                        JourneyViewMode.ROADMAP -> "রোডম্যাপ"
                        JourneyViewMode.ADVENTURE -> "পাথ"
                        JourneyViewMode.LIST -> "তালিকা"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.testTag("journey_view_mode_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val roadmapSelected = viewMode == JourneyViewMode.ROADMAP
                        val adventureSelected = viewMode == JourneyViewMode.ADVENTURE
                        val listSelected = viewMode == JourneyViewMode.LIST

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (roadmapSelected) PrimaryIndigo else Color.Transparent,
                            modifier = Modifier.clickable { viewMode = JourneyViewMode.ROADMAP }
                        ) {
                            Text(
                                text = "🎯 রোডম্যাপ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (roadmapSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (roadmapSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                maxLines = 1
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (adventureSelected) PrimaryIndigo else Color.Transparent,
                            modifier = Modifier.clickable { viewMode = JourneyViewMode.ADVENTURE }
                        ) {
                            Text(
                                text = "🏔️ পাথ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (adventureSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (adventureSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                maxLines = 1
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (listSelected) PrimaryIndigo else Color.Transparent,
                            modifier = Modifier.clickable { viewMode = JourneyViewMode.LIST }
                        ) {
                            Text(
                                text = "📋 তালিকা",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (listSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (listSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 3. Main Content
        when (viewMode) {
            JourneyViewMode.ROADMAP -> {
                // Full Visual Learning Roadmap for Selected Subject
                item {
                    currentSubject?.let { subj ->
                        SubjectRoadmapComponent(
                            subject = subj,
                            chapters = chapters,
                            topics = subjectTopics,
                            recommendedPractices = recommendedPractices,
                            isBengali = isBengali,
                            onOpenTopicStudy = onOpenTopicStudy,
                            onStartAssessment = onStartAssessment
                        )
                    }
                }
            }

            JourneyViewMode.ADVENTURE -> {
                // Chapter Header Card
                item {
                    currentChapter?.let { ch ->
                        ChapterBannerCard(
                            chapter = ch,
                            isBengali = isBengali,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                if (currentChapter != null) {
                    // Interactive Adventure Path with Unlockable Nodes
                    item {
                        InteractiveJourneyMap(
                            chapter = currentChapter,
                            topics = topics,
                            isBengali = isBengali,
                            onOpenStudy = onOpenTopicStudy,
                            onStartPractice = { topicId, title ->
                                onStartAssessment(AssessmentMode.PRACTICE, topicId, title)
                            },
                            onStartBossBattle = {
                                onStartAssessment(
                                    AssessmentMode.BOSS_BATTLE,
                                    currentChapter.id,
                                    "🔥 অধ্যায় বস ব্যাটল: ${currentChapter.getTitle(true)}"
                                )
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            JourneyViewMode.LIST -> {
                if (chapters.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBengali) "কোনো অধ্যায় খুঁজে পাওয়া যায়নি।" else "No chapters found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    chapters.forEach { chapter ->
                        val chapterTopics = subjectTopics.filter { it.chapterId == chapter.id }
                        item {
                            val isExpanded = remember { mutableStateOf(true) }
                            com.example.ui.components.ChapterRoadmapCard(
                                chapter = chapter,
                                topics = chapterTopics,
                                subjectColorHex = currentSubject?.colorHex ?: 0xFF4F46E5,
                                isBengali = isBengali,
                                isExpanded = isExpanded.value,
                                isLastInList = chapter == chapters.last(),
                                onToggleExpand = { isExpanded.value = !isExpanded.value },
                                onOpenStudy = onOpenTopicStudy,
                                onStartTopicPractice = { topicId, title ->
                                    onStartAssessment(AssessmentMode.PRACTICE, topicId, title)
                                },
                                onStartBossBattle = { },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
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
fun ChapterBannerCard(
    chapter: ChapterItem,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEEF2FF)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) Color(0xFF334155) else Color(0xFFC7D2FE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("chapter_banner_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryIndigo
                ) {
                    Text(
                        text = "অধ্যায় ${chapter.chapterNumber.toBengaliDigits()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${chapter.completedTopics}/${chapter.totalTopics} টপিক সম্পন্ন",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF4338CA)
                )
            }

            Text(
                text = chapter.getTitle(isBengali),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) Color.White else Color(0xFF1E1B4B)
            )

            Text(
                text = chapter.getDescription(isBengali),
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF3730A3)
            )

            LinearProgressIndicator(
                progress = { chapter.masteryPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (chapter.bossCompleted) CorrectGreen else PrimaryIndigo,
                trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE0E7FF)
            )
        }
    }
}

@Composable
fun TopicNodeCard(
    topic: TopicItem,
    isBengali: Boolean,
    onOpenStudy: () -> Unit,
    onStartPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMastered = topic.masteryPercentage >= 80
    val isLocked = !topic.isUnlocked
    val isDark = MaterialTheme.colorScheme.background.red < 0.3f

    val cardBg = when {
        isLocked -> if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFF1F5F9)
        isMastered -> if (isDark) Color(0xFF022C22) else Color(0xFFECFDF5)
        else -> if (isDark) Color(0xFF1E293B) else Color.White
    }

    val cardBorder = when {
        isLocked -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
        isMastered -> if (isDark) Color(0xFF059669) else Color(0xFFA7F3D0)
        else -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    }

    val titleColor = when {
        isLocked -> if (isDark) Color(0xFF64748B) else Color(0xFF64748B)
        isMastered -> if (isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)
        else -> if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    }

    val subtitleColor = when {
        isLocked -> if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
        isMastered -> if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
        else -> if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked || isDark) 0.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("topic_node_${topic.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Milestone Node Icon Circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isLocked -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                            isMastered -> if (isDark) Color(0xFF065F46) else Color(0xFFD1FAE5)
                            else -> if (isDark) Color(0xFF312E81) else Color(0xFFEEF2FF)
                        }
                    )
                    .border(
                        width = 1.5.dp,
                        color = when {
                            isLocked -> if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                            isMastered -> CorrectGreen
                            else -> PrimaryIndigo
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                } else if (isMastered) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mastered",
                        tint = if (isDark) Color(0xFFA7F3D0) else CorrectGreen,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isDark) Color(0xFF818CF8) else PrimaryIndigo,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.getTitle(isBengali),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${topic.questionCount.toBengaliDigits()}টি প্রশ্ন • মাস্টারি ${topic.masteryPercentage.toBengaliDigits()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor
                )

                if (!isLocked) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onOpenStudy,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isDark) Color(0xFF818CF8) else PrimaryIndigo.copy(alpha = 0.6f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isDark) Color(0xFF818CF8) else PrimaryIndigo
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("study_topic_btn_${topic.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = "Learn",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "পড়ুন",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            )
                        }

                        Button(
                            onClick = onStartPractice,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryIndigo,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("practice_topic_btn_${topic.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Practice",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "অনুশীলন",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BossBattleCitadelCard(
    chapter: ChapterItem,
    isBengali: Boolean,
    onStartBossBattle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked = chapter.bossUnlocked
    val isCompleted = chapter.bossCompleted
    val isDark = isSystemInDarkTheme()

    if (isUnlocked || isCompleted) {
        BossBattleCard(
            bossName = "অধ্যায় বস: ${chapter.getTitle(true)}",
            chapterTitle = "অধ্যায় মাস্টারি দুর্গ",
            bossMaxHp = 1000,
            bossCurrentHp = if (isCompleted) 0 else 1000,
            playerMaxHp = 100,
            playerCurrentHp = 100,
            questionsCount = 8,
            questionsCompleted = if (isCompleted) 8 else 0,
            isBengali = isBengali,
            isDefeated = isCompleted,
            onStartBattle = onStartBossBattle,
            modifier = modifier
        )
    } else {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = modifier
                .fillMaxWidth()
                .testTag("boss_battle_citadel_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDark) {
                            Brush.linearGradient(listOf(Color(0xFF1E293B).copy(alpha = 0.5f), Color(0xFF0F172A)))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF334155)))
                        }
                    )
                    .padding(18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Boss Citadel Locked",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Text(
                        text = "🔒 অধ্যায় বস দুর্গ (লক করা)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "বস আনলক করতে এই অধ্যায়ের সমস্ত টপিক ৭০% মাস্টারিতে পৌঁছান।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun JourneyHeroBanner(
    currentSubject: SubjectItem?,
    chaptersCount: Int,
    activeCount: Int,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val mastery = currentSubject?.masteryPercentage ?: 0
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E1B2E), // Deep Indigo / Dark theme rich background
                            Color(0xFF312E81), // PrimaryIndigo dark variant
                            Color(0xFF0F766E)  // Teal / SecondaryEmerald accent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Badge / Chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBengali) "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ ও সংসদ অনুমোদিত" else "WB Board Syllabus Roadmap",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }

                    // Title
                    Text(
                        text = if (isBengali) "অধ্যায়ভিত্তিক লার্নিং রোডম্যাপ" else "Chapter-wise Learning Roadmap",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    )

                    // Subtitle / Subject Name
                    val subName = currentSubject?.getTitle(isBengali) ?: (if (isBengali) "সকল বিষয়" else "All Subjects")
                    Text(
                        text = if (isBengali) "বর্তমান বিষয়: $subName" else "Current Subject: $subName",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Feature Metadata Pill Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeroFeaturePill(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            text = if (isBengali) "$chaptersCount টি অধ্যায়" else "$chaptersCount Chapters"
                        )
                        if (activeCount > 0) {
                            HeroFeaturePill(
                                icon = Icons.Default.Groups,
                                text = if (isBengali) "$activeCount জন পড়ছেন" else "$activeCount Active"
                            )
                        }
                        HeroFeaturePill(
                            icon = Icons.Default.CheckCircle,
                            text = if (isBengali) "স্মার্ট প্রোগ্রেস" else "Smart Progress"
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right Side Circle Progress Style
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(2.dp, Color(0xFFFBBF24).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { (mastery.toFloat() / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.size(64.dp),
                        color = Color(0xFFFBBF24),
                        trackColor = Color.White.copy(alpha = 0.2f),
                        strokeWidth = 6.dp,
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$mastery%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = if (isBengali) "মাস্টারি" else "Mastery",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFFFBBF24)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroFeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White
            )
        }
    }
}
