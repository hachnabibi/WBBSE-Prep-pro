package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AssessmentMode
import com.example.data.model.LeaderboardStudent
import com.example.data.model.SubjectItem
import com.example.data.model.toBengaliDigits
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.util.formatCompactNumber

enum class LeaderboardMainTab {
    SUBJECT_MOCKS,
    OVERALL_MOCK,
    BOSS_BATTLES,
    OVERALL_XP
}

enum class LeaderboardTimeframe {
    WEEKLY,
    MONTHLY,
    ALL_TIME
}

enum class LeaderboardRankFilter {
    ALL,
    TOP_10,
    NEAR_ME
}

data class SubjectLeaderboardItem(
    val student: LeaderboardStudent,
    val subjectRank: Int,
    val subjectScore: Int,
    val subjectPercentile: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: DeshExamViewModel,
    liveLeaderboard: List<LeaderboardStudent>,
    isBengali: Boolean,
    onStartAssessment: (AssessmentMode, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(LeaderboardMainTab.SUBJECT_MOCKS) }
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    var selectedSubjectId by remember { mutableStateOf("phy_sci") }
    var selectedTimeframe by remember { mutableStateOf(LeaderboardTimeframe.ALL_TIME) }
    var selectedDistrict by remember { mutableStateOf<String?>(null) }
    var selectedSchool by remember { mutableStateOf<String?>(null) }
    var inspectedStudent by remember { mutableStateOf<LeaderboardStudent?>(null) }

    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedRankFilter by remember { mutableStateOf(LeaderboardRankFilter.ALL) }
    var showAllSubjectsComparison by remember { mutableStateOf(false) }
    var showShareCertificate by remember { mutableStateOf(false) }

    val rawLeaderboard = if (liveLeaderboard.isNotEmpty()) liveLeaderboard else remember { viewModel.repository.getLeaderboard() }
    val currentSubject = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()

    val districts = com.example.util.WestBengalData.districts

    val schools = remember(rawLeaderboard, selectedDistrict) {
        val listFromDistrict = if (selectedDistrict != null) com.example.util.WestBengalData.getSchoolsForDistrict(selectedDistrict) else emptyList()
        val listFromLeaderboard = if (selectedDistrict != null) {
            rawLeaderboard.filter { it.district == selectedDistrict }.map { it.school }
        } else {
            rawLeaderboard.map { it.school }
        }
        (listFromDistrict + listFromLeaderboard).filter { it.isNotBlank() }.distinct()
    }

    // Subject-specific sorted ranking calculation
    val baseSubjectRankedStudents = remember(selectedSubjectId, selectedDistrict, selectedSchool, rawLeaderboard) {
        var filtered = if (selectedDistrict == null) rawLeaderboard else rawLeaderboard.filter { it.district == selectedDistrict }
        if (selectedSchool != null) {
            filtered = filtered.filter { it.school == selectedSchool }
        }
        val sorted = filtered.sortedByDescending { it.subjectMockScores[selectedSubjectId] ?: 0 }
        sorted.mapIndexed { index, student ->
            val score = student.subjectMockScores[selectedSubjectId] ?: 0
            val percentile = 80.0 + ((score.toDouble() / 90.0) * 19.8)
            SubjectLeaderboardItem(
                student = student,
                subjectRank = index + 1,
                subjectScore = score,
                subjectPercentile = Math.round(percentile * 10.0) / 10.0,
            )
        }
    }

    // Other categories sorting
    val baseCategoryRankedStudents = remember(selectedTab, selectedDistrict, selectedSchool, rawLeaderboard) {
        var filtered = if (selectedDistrict == null) rawLeaderboard else rawLeaderboard.filter { it.district == selectedDistrict }
        if (selectedSchool != null) {
            filtered = filtered.filter { it.school == selectedSchool }
        }
        when (selectedTab) {
            LeaderboardMainTab.OVERALL_MOCK -> filtered.sortedByDescending { it.mockScore }
            LeaderboardMainTab.BOSS_BATTLES -> filtered.sortedByDescending { it.bossVictories }
            LeaderboardMainTab.OVERALL_XP -> filtered.sortedByDescending { it.xp }
            else -> filtered
        }.mapIndexed { index, student -> student.copy(rank = index + 1) }
    }

    // Search and Rank Bracket Filtered Lists
    val subjectRankedStudents = remember(baseSubjectRankedStudents, searchQuery, selectedRankFilter) {
        var list = baseSubjectRankedStudents
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.student.name.lowercase().contains(q) ||
                it.student.school.lowercase().contains(q) ||
                it.student.district.lowercase().contains(q) ||
                it.student.badgeTitle.lowercase().contains(q)
            }
        }
        when (selectedRankFilter) {
            LeaderboardRankFilter.TOP_10 -> list.take(10)
            LeaderboardRankFilter.NEAR_ME -> {
                val userIdx = list.indexOfFirst { it.student.isCurrentUser }
                if (userIdx != -1) {
                    val start = maxOf(0, userIdx - 4)
                    val end = minOf(list.size, userIdx + 5)
                    list.subList(start, end)
                } else list
            }
            LeaderboardRankFilter.ALL -> list
        }
    }

    val categoryRankedStudents = remember(baseCategoryRankedStudents, searchQuery, selectedRankFilter) {
        var list = baseCategoryRankedStudents
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.school.lowercase().contains(q) ||
                it.district.lowercase().contains(q) ||
                it.badgeTitle.lowercase().contains(q)
            }
        }
        when (selectedRankFilter) {
            LeaderboardRankFilter.TOP_10 -> list.take(10)
            LeaderboardRankFilter.NEAR_ME -> {
                val userIdx = list.indexOfFirst { it.isCurrentUser }
                if (userIdx != -1) {
                    val start = maxOf(0, userIdx - 4)
                    val end = minOf(list.size, userIdx + 5)
                    list.subList(start, end)
                } else list
            }
            LeaderboardRankFilter.ALL -> list
        }
    }

    val currentUserSubjectItem = baseSubjectRankedStudents.find { it.student.isCurrentUser }
    val currentUserCategory = baseCategoryRankedStudents.find { it.isCurrentUser } ?: rawLeaderboard.find { it.isCurrentUser }

    // Today's Star Student (Highest Mock Score or XP)
    val starPerformer = remember(rawLeaderboard) {
        rawLeaderboard.maxByOrNull { it.accuracy } ?: rawLeaderboard.firstOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("leaderboard_screen")
    ) {
        ScreenHeaderBar(
            title = "মেধা তালিকা",
            subtitle = "রাজ্য ও জেলাভিত্তিক লাইভ র‍্যাঙ্কিং",
            isBengali = isBengali,
            showBackButton = true,
            onBack = { viewModel.goBack() }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp),
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
                                        Color(0xFF1E1B4B),
                                        Color(0xFF3730A3),
                                        Color(0xFF4F46E5)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = ExamGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "WBBSE মাধ্যমিক মেধা তালিকা ২০২৬",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ExamGold
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Certificate / Share Button
                                    IconButton(
                                        onClick = { showShareCertificate = true },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .testTag("leaderboard_share_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = "Certificate",
                                            tint = ExamGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // 1v1 Battle Action
                                    Button(
                                        onClick = {
                                            onStartAssessment(
                                                AssessmentMode.FRIEND_CHALLENGE,
                                                selectedSubjectId,
                                                "১v১ লাইভ মেধা ব্যাটল"
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
                                            .testTag("leaderboard_1v1_cta_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsKabaddi,
                                            contentDescription = "1v1",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "১v১ চ্যালেঞ্জ",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = when (selectedTab) {
                                    LeaderboardMainTab.SUBJECT_MOCKS -> "বিষয়ভিত্তিক মক টেস্ট মেধা তালিকা"
                                    LeaderboardMainTab.OVERALL_MOCK -> "পূর্ণাঙ্গ গ্র্যান্ড মক টেস্ট টপার্স"
                                    LeaderboardMainTab.BOSS_BATTLES -> "অধ্যায় বস ব্যাটল অপরাজেয় বীর"
                                    LeaderboardMainTab.OVERALL_XP -> "সামগ্রিক মেধা ও XP র‍্যাঙ্ক"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )

                            Text(
                                text = if (selectedTab == LeaderboardMainTab.SUBJECT_MOCKS) {
                                    if (isBengali) "WBBSE মাধ্যমিকের ৭টি প্রধান বিষয়ে আপনার ৯০ নম্বরের মক টেস্ট নম্বরের সাথে সমগ্র রাজ্যের পরীক্ষার্থীদের র‍্যাঙ্ক তুলনা করুন।"
                                    else "Compare your subject 90-mark mock test performance with all West Bengal board students."
                                } else {
                                    if (isBengali) "নিয়মিত মক টেস্ট, অধ্যায় যুদ্ধ ও অর্জিত স্কোরের ভিত্তিতে রাজ্যের শীর্ষ মেধা ক্রম।"
                                    else "Ranked by aggregate board model test scores, boss citadels, and answer accuracy."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }
                    }
                }

                // 2. Today's Star Performer Highlight Banner
                starPerformer?.let { star ->
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            ),
                            border = BorderStroke(1.dp, PrimaryIndigoLight.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { inspectedStudent = star }
                                .testTag("today_star_performer_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(ExamGold.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = ExamGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "আজকের স্টার পারফর্মার",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryIndigo
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Whatshot,
                                                contentDescription = null,
                                                tint = Color(0xFFEA580C),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Text(
                                            text = "${star.name} (${star.school.take(18)})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CorrectGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${star.accuracy}% সঠিকতা",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CorrectGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Main Navigation Tabs (Subject Mocks vs Grand Mock vs Boss vs Overall)
                item {
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .testTag("leaderboard_main_tabrow")
                    ) {
                         Tab(
                            selected = selectedTab == LeaderboardMainTab.SUBJECT_MOCKS,
                            onClick = { selectedTab = LeaderboardMainTab.SUBJECT_MOCKS },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.Subject, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "বিষয়ভিত্তিক",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_subject_mocks")
                        )

                        Tab(
                            selected = selectedTab == LeaderboardMainTab.OVERALL_MOCK,
                            onClick = { selectedTab = LeaderboardMainTab.OVERALL_MOCK },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "গ্র্যান্ড মক",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_grand_mock")
                        )

                        Tab(
                            selected = selectedTab == LeaderboardMainTab.BOSS_BATTLES,
                            onClick = { selectedTab = LeaderboardMainTab.BOSS_BATTLES },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "বস ব্যাটল",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_boss_battles")
                        )

                        Tab(
                            selected = selectedTab == LeaderboardMainTab.OVERALL_XP,
                            onClick = { selectedTab = LeaderboardMainTab.OVERALL_XP },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MilitaryTech, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "সামগ্রিক",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_overall_xp")
                        )
                    }
                }

                // 4. Subject Selector Bar (Displayed when in SUBJECT_MOCKS tab)
                if (selectedTab == LeaderboardMainTab.SUBJECT_MOCKS) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "বিষয় নির্বাচন করুন (WBBSE ৭টি প্রধান বিষয়):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(subjects) { subject ->
                                    val isSelected = subject.id == selectedSubjectId
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedSubjectId = subject.id },
                                        label = {
                                            Text(
                                                text = subject.name,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            )
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(subject.colorHex))
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(subject.colorHex).copy(alpha = 0.2f),
                                            selectedLabelColor = Color(subject.colorHex)
                                        ),
                                        border = if (isSelected) BorderStroke(1.5.dp, Color(subject.colorHex)) else null,
                                        modifier = Modifier.testTag("chip_subject_${subject.id}")
                                    )
                                }
                            }
                        }
                    }

                    // Subject Benchmark & User Standing Insight Card
                    item {
                        currentSubject?.let { subj ->
                            currentUserSubjectItem?.let { me ->
                                val topScore = baseSubjectRankedStudents.firstOrNull()?.subjectScore ?: 90
                                val topperName = baseSubjectRankedStudents.firstOrNull()?.student?.name ?: "অনন্যা মুখোপাধ্যায়"
                                val avgScore = 66
                                val gapToFirst = topScore - me.subjectScore
                                val nextRankStudent = baseSubjectRankedStudents.getOrNull(me.subjectRank - 2)
                                val gapToNextRank = if (nextRankStudent != null) (nextRankStudent.subjectScore - me.subjectScore + 1).coerceAtLeast(1) else 0

                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, Color(subj.colorHex).copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // 1. Single Full-Width Subject Header Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(subj.colorHex).copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Analytics,
                                                    contentDescription = null,
                                                    tint = Color(subj.colorHex),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${subj.name} - মেধা তুলনা",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "আপনার স্থান: ${me.subjectRank.toBengaliDigits()}ম (শীর্ষ ${(100 - me.subjectPercentile).toInt().toBengaliDigits()}%)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(subj.colorHex).copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "#${me.subjectRank}ম স্থান",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(subj.colorHex),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        // 2. Action Buttons Row (Below title: All Subjects Comparison & Take Mock Test)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { showAllSubjectsComparison = true },
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(42.dp)
                                                    .testTag("btn_compare_all_subjects")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CompareArrows,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = PrimaryIndigo
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "সকল বিষয় তুলনা",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo,
                                                    maxLines = 1
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    onStartAssessment(
                                                        AssessmentMode.MOCK_TEST,
                                                        subj.id,
                                                        "${subj.name} পূর্ণাঙ্গ মক টেস্ট"
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(subj.colorHex),
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(42.dp)
                                                    .testTag("btn_take_subject_mock")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "মক টেস্ট দিন",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))

                                        // 3 Benchmark Statistics Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            SubjectBenchmarkPill(
                                                title = "আপনার স্কোর",
                                                value = "${me.subjectScore} / 90",
                                                subtitle = "${me.subjectPercentile}%ile",
                                                color = Color(subj.colorHex),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            SubjectBenchmarkPill(
                                                title = "রাজ্য সেরা স্কোর",
                                                value = "$topScore / 90",
                                                subtitle = topperName.take(10),
                                                color = ExamGold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            SubjectBenchmarkPill(
                                                title = "রাজ্য গড় স্কোর",
                                                value = "$avgScore / 90",
                                                subtitle = "WBBSE Avg",
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        // Milestone & Progress Bar comparison
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (gapToNextRank > 0 && me.subjectRank > 1) {
                                                        "পরবর্তী র‍্যাঙ্কে যেতে আর মাত্র $gapToNextRank নম্বর প্রয়োজন"
                                                    } else {
                                                        "টপ স্কোরের সাথে ব্যবধান"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = if (gapToFirst > 0) {
                                                        "টপার থেকে $gapToFirst কম"
                                                    } else {
                                                        "আপনিই রাজ্যে শীর্ষে!"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (gapToFirst > 0) PrimaryIndigo else CorrectGreen
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { me.subjectScore.toFloat() / 90f },
                                                color = Color(subj.colorHex),
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Search Bar and Rank Bracket Filters
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Search Text Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "শিক্ষার্থীর নাম, রোল বা স্কুলের নাম দিয়ে খুঁজুন...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Search",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("leaderboard_search_input")
                        )

                        // Rank Filter Chips (All, Top 10, Near Me)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LeaderboardRankFilter.entries.forEach { rf ->
                                val isSelected = selectedRankFilter == rf
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedRankFilter = rf },
                                    label = {
                                        Text(
                                            text = when (rf) {
                                                LeaderboardRankFilter.ALL -> "সকল শিক্ষার্থী"
                                                LeaderboardRankFilter.TOP_10 -> "🏆 শীর্ষ ১০"
                                                LeaderboardRankFilter.NEAR_ME -> "🎯 আমার আশেপাশে"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Timeframe row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LeaderboardTimeframe.entries.forEach { tf ->
                                val isSelected = selectedTimeframe == tf
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTimeframe = tf },
                                    label = {
                                        Text(
                                            text = when (tf) {
                                                LeaderboardTimeframe.WEEKLY -> "এই সপ্তাহ"
                                                LeaderboardTimeframe.MONTHLY -> "চলতি মাস"
                                                LeaderboardTimeframe.ALL_TIME -> "সর্বকালীন (২০২৬)"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryIndigo.copy(alpha = 0.85f),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // District Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedDistrict == null,
                                    onClick = { selectedDistrict = null },
                                    label = {
                                        Text(
                                            text = "সমস্ত জেলা (All)",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                            items(districts) { dist ->
                                val isSelected = selectedDistrict == dist
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedDistrict = if (isSelected) null else dist },
                                    label = {
                                        Text(
                                            text = dist,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // 6. Top 3 Podium Rendering (Only when not in Search/Near-Me filter)
                if (searchQuery.isBlank() && selectedRankFilter == LeaderboardRankFilter.ALL) {
                    if (selectedTab == LeaderboardMainTab.SUBJECT_MOCKS) {
                        val top1 = subjectRankedStudents.getOrNull(0)
                        val top2 = subjectRankedStudents.getOrNull(1)
                        val top3 = subjectRankedStudents.getOrNull(2)
                        if (top1 != null) {
                            item {
                                SubjectLeaderboardPodium(
                                    first = top1,
                                    second = top2,
                                    third = top3,
                                    subjectName = if (isBengali) currentSubject?.nameBengali ?: "বিষয়" else currentSubject?.nameEnglish ?: "Subject",
                                    subjectColor = Color(currentSubject?.colorHex ?: 0xFF3B82F6),
                                    isBengali = isBengali,
                                    onStudentClick = { inspectedStudent = it.student },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    } else {
                        val top1 = categoryRankedStudents.getOrNull(0)
                        val top2 = categoryRankedStudents.getOrNull(1)
                        val top3 = categoryRankedStudents.getOrNull(2)
                        if (top1 != null) {
                            item {
                                LeaderboardPodium(
                                    first = top1,
                                    second = top2,
                                    third = top3,
                                    tab = selectedTab,
                                    isBengali = isBengali,
                                    onStudentClick = { inspectedStudent = it },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                // 7. Section Header for Student List
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "অনুসন্ধানের ফলাফল" else "মেধা ক্রম তালিকা",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (selectedTab == LeaderboardMainTab.SUBJECT_MOCKS) {
                                "${subjectRankedStudents.size} জন শিক্ষার্থী"
                            } else {
                                "${categoryRankedStudents.size} জন শিক্ষার্থী"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 8. List items (All students matching the filter)
                if (selectedTab == LeaderboardMainTab.SUBJECT_MOCKS) {
                    val listToShow = if (searchQuery.isBlank() && selectedRankFilter == LeaderboardRankFilter.ALL && subjectRankedStudents.size > 3) {
                        subjectRankedStudents.subList(3, subjectRankedStudents.size)
                    } else {
                        subjectRankedStudents
                    }
                    items(listToShow) { item ->
                        SubjectStudentCard(
                            item = item,
                            currentSubject = currentSubject,
                            isBengali = isBengali,
                            onClick = { inspectedStudent = item.student },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    val listToShow = if (searchQuery.isBlank() && selectedRankFilter == LeaderboardRankFilter.ALL && categoryRankedStudents.size > 3) {
                        categoryRankedStudents.subList(3, categoryRankedStudents.size)
                    } else {
                        categoryRankedStudents
                    }
                    items(listToShow) { student ->
                        CategoryStudentCard(
                            student = student,
                            tab = selectedTab,
                            isBengali = isBengali,
                            onClick = { inspectedStudent = student },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
        }

        // 9. Sticky Floating "Your Standing" Card at the bottom
        if (selectedTab == LeaderboardMainTab.SUBJECT_MOCKS) {
            currentUserSubjectItem?.let { me ->
                StickySubjectUserCard(
                    item = me,
                    subject = currentSubject,
                    isBengali = isBengali,
                    onTakeAction = {
                        onStartAssessment(
                            AssessmentMode.MOCK_TEST,
                            selectedSubjectId,
                            if (isBengali) "${currentSubject?.nameBengali ?: ""} পূর্ণাঙ্গ মক টেস্ট" else "Mock Test"
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        } else {
            currentUserCategory?.let { me ->
                StickyCategoryUserCard(
                    student = me,
                    tab = selectedTab,
                    isBengali = isBengali,
                    onTakeAction = {
                        when (selectedTab) {
                            LeaderboardMainTab.OVERALL_MOCK -> onStartAssessment(
                                AssessmentMode.MOCK_TEST,
                                null,
                                "মাধ্যমিক পূর্ণাঙ্গ গ্র্যান্ড মক টেস্ট"
                            )
                            LeaderboardMainTab.BOSS_BATTLES -> onStartAssessment(
                                AssessmentMode.BOSS_BATTLE,
                                "phy_ch1",
                                "🔥 অধ্যায় বস ব্যাটল: পরিবেশের জন্য ভাবনা"
                            )
                            LeaderboardMainTab.OVERALL_XP -> onStartAssessment(
                                AssessmentMode.DAILY_10,
                                null,
                                "ডেইলি ১০ স্পিড টেস্ট"
                            )
                            else -> {}
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        // 10. Bottom Sheet for Inspected Student Comparison Matrix
        inspectedStudent?.let { student ->
            ModalBottomSheet(
                onDismissRequest = { inspectedStudent = null },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                SubjectStudentComparisonSheet(
                    student = student,
                    subjects = subjects,
                    isBengali = isBengali,
                    onChallenge = {
                        inspectedStudent = null
                        onStartAssessment(
                            AssessmentMode.FRIEND_CHALLENGE,
                            selectedSubjectId,
                            "${student.name}-এর সাথে ১v১ চ্যালেঞ্জ"
                        )
                    },
                    onClose = { inspectedStudent = null }
                )
            }
        }

        // 11. Modal Dialog for All 7-Subjects Comparison
        if (showAllSubjectsComparison) {
            AllSubjectsComparisonDialog(
                subjects = subjects,
                onClose = { showAllSubjectsComparison = false }
            )
        }

        // 12. Modal Dialog for Sharing Rank Certificate
        if (showShareCertificate) {
            currentUserSubjectItem?.let { me ->
                ShareRankCardDialog(
                    item = me,
                    subject = currentSubject,
                    onClose = { showShareCertificate = false },
                    onShare = { shareText ->
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "মেধা সার্টিফিকেট শেয়ার করুন")
                        context.startActivity(shareIntent)
                    }
                )
            }
        }
    }
}
}

@Composable
fun SubjectBenchmarkPill(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SubjectLeaderboardPodium(
    first: SubjectLeaderboardItem,
    second: SubjectLeaderboardItem?,
    third: SubjectLeaderboardItem?,
    subjectName: String,
    subjectColor: Color,
    isBengali: Boolean,
    onStudentClick: (SubjectLeaderboardItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_leaderboard_podium")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$subjectName • শীর্ষ ৩ মেধা পডিয়াম",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                if (second != null) {
                    SubjectPodiumPillar(
                        item = second,
                        place = 2,
                        height = 130.dp,
                        podiumColor = Color(0xFF94A3B8),
                        onClick = { onStudentClick(second) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // 1st Place (Gold - Tallest Center)
                SubjectPodiumPillar(
                    item = first,
                    place = 1,
                    height = 160.dp,
                    podiumColor = ExamGold,
                    onClick = { onStudentClick(first) },
                    modifier = Modifier.weight(1.15f)
                )

                // 3rd Place (Bronze)
                if (third != null) {
                    SubjectPodiumPillar(
                        item = third,
                        place = 3,
                        height = 110.dp,
                        podiumColor = Color(0xFFD97706),
                        onClick = { onStudentClick(third) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SubjectPodiumPillar(
    item: SubjectLeaderboardItem,
    place: Int,
    height: androidx.compose.ui.unit.Dp,
    podiumColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
            .testTag("subject_podium_place_$place")
    ) {
        if (place == 1) {
            Icon(
                imageVector = Icons.Default.MilitaryTech,
                contentDescription = "Gold Crown",
                tint = ExamGold,
                modifier = Modifier.size(24.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(if (place == 1) 56.dp else 48.dp)
                .clip(CircleShape)
                .background(podiumColor.copy(alpha = 0.2f))
                .border(2.dp, podiumColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.student.name.take(1),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = podiumColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.student.name,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            text = item.student.school,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = podiumColor.copy(alpha = 0.18f)
        ) {
            Text(
                text = "${item.subjectScore} / 90",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(podiumColor.copy(alpha = 0.4f), podiumColor.copy(alpha = 0.85f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#$place",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    text = when (place) {
                        1 -> "১ম স্থান"
                        2 -> "২য় স্থান"
                        else -> "৩য় স্থান"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun SubjectStudentCard(
    item: SubjectLeaderboardItem,
    currentSubject: SubjectItem?,
    isBengali: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.student.isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.student.isCurrentUser) 3.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("subject_student_row_${item.subjectRank}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.student.isCurrentUser) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${item.subjectRank}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (item.student.isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.student.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (item.student.isCurrentUser) FontWeight.Black else FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.student.isCurrentUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryIndigo
                        ) {
                            Text(
                                text = "আপনি",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${item.student.school} • ${item.student.district}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ExamCyan.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${item.subjectPercentile}%ile",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFF0E7490),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryIndigo.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "স্ট্রিক: ${item.student.streak} দিন",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Score Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.subjectScore} / 90",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = if (currentSubject != null) Color(currentSubject.colorHex) else PrimaryIndigo
                )
                Text(
                    text = "মক স্কোর",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StickySubjectUserCard(
    item: SubjectLeaderboardItem,
    subject: SubjectItem?,
    isBengali: Boolean,
    onTakeAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .border(1.5.dp, PrimaryIndigoLight, RoundedCornerShape(18.dp))
            .testTag("sticky_subject_user_rank_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PrimaryIndigo),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${item.subjectRank}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBengali) "${subject?.nameBengali ?: "বিষয়"} স্থান: ${item.subjectRank}ম" else "${subject?.nameEnglish ?: "Subject"} Rank: #${item.subjectRank}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "আপনার স্কোর: ${item.subjectScore}/৯০ (${item.subjectPercentile}%ile)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onTakeAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (subject != null) Color(subject.colorHex) else PrimaryIndigo
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("leaderboard_boost_subject_btn")
            ) {
                Text(
                    text = "মক টেস্ট দিন",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun LeaderboardPodium(
    first: LeaderboardStudent,
    second: LeaderboardStudent?,
    third: LeaderboardStudent?,
    tab: LeaderboardMainTab,
    isBengali: Boolean,
    onStudentClick: (LeaderboardStudent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("leaderboard_podium_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "শীর্ষ ৩ মেধা পডিয়াম",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                if (second != null) {
                    CategoryPodiumPillar(
                        student = second,
                        place = 2,
                        height = 130.dp,
                        podiumColor = Color(0xFF94A3B8),
                        tab = tab,
                        isBengali = isBengali,
                        onClick = { onStudentClick(second) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                CategoryPodiumPillar(
                    student = first,
                    place = 1,
                    height = 160.dp,
                    podiumColor = ExamGold,
                    tab = tab,
                    isBengali = isBengali,
                    onClick = { onStudentClick(first) },
                    modifier = Modifier.weight(1.15f)
                )

                if (third != null) {
                    CategoryPodiumPillar(
                        student = third,
                        place = 3,
                        height = 110.dp,
                        podiumColor = Color(0xFFD97706),
                        tab = tab,
                        isBengali = isBengali,
                        onClick = { onStudentClick(third) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CategoryPodiumPillar(
    student: LeaderboardStudent,
    place: Int,
    height: androidx.compose.ui.unit.Dp,
    podiumColor: Color,
    tab: LeaderboardMainTab,
    isBengali: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
            .testTag("podium_place_$place")
    ) {
        if (place == 1) {
            Icon(
                imageVector = Icons.Default.MilitaryTech,
                contentDescription = "Gold Crown",
                tint = ExamGold,
                modifier = Modifier.size(24.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(if (place == 1) 56.dp else 48.dp)
                .clip(CircleShape)
                .background(podiumColor.copy(alpha = 0.2f))
                .border(2.dp, podiumColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = student.name.take(1),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = podiumColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = student.name,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            text = student.school,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = podiumColor.copy(alpha = 0.18f)
        ) {
            Text(
                text = when (tab) {
                    LeaderboardMainTab.OVERALL_MOCK -> "${student.mockScore}/90"
                    LeaderboardMainTab.BOSS_BATTLES -> "⚔️ ${student.bossVictories} জয়"
                    LeaderboardMainTab.OVERALL_XP -> "${formatCompactNumber(student.xp)} XP"
                    else -> "${student.mockScore}/90"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(podiumColor.copy(alpha = 0.4f), podiumColor.copy(alpha = 0.85f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#$place",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    text = when (place) {
                        1 -> "১ম স্থান"
                        2 -> "২য় স্থান"
                        else -> "৩য় স্থান"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun CategoryStudentCard(
    student: LeaderboardStudent,
    tab: LeaderboardMainTab,
    isBengali: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (student.isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (student.isCurrentUser) 3.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("leaderboard_student_row_${student.rank}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (student.isCurrentUser) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${student.rank}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (student.isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (student.isCurrentUser) FontWeight.Black else FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (student.isCurrentUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryIndigo
                        ) {
                            Text(
                                text = "আপনি",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${student.school} • ${student.district}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryIndigoLight
                    ) {
                        Text(
                            text = "মক: ${student.mockScore}/৯০",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ExamFlame
                    ) {
                        Text(
                            text = "⚔️ ${student.bossVictories} বস জয়",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                when (tab) {
                    LeaderboardMainTab.OVERALL_MOCK -> {
                        Text(
                            text = "${student.mockScore} / 90",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = PrimaryIndigo
                        )
                        Text(
                            text = "${student.mockPercentile}%ile",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LeaderboardMainTab.BOSS_BATTLES -> {
                        Text(
                            text = "${student.bossVictories}টি জয়",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = ExamFlame
                        )
                        Text(
                            text = "অধ্যায় বস",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LeaderboardMainTab.OVERALL_XP -> {
                        Text(
                            text = "${formatCompactNumber(student.xp)} XP",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = ExamGold
                        )
                        Text(
                            text = "${student.accuracy}% নির্ভুল",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun StickyCategoryUserCard(
    student: LeaderboardStudent,
    tab: LeaderboardMainTab,
    isBengali: Boolean,
    onTakeAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .border(1.5.dp, PrimaryIndigoLight, RoundedCornerShape(18.dp))
            .testTag("sticky_category_user_rank_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PrimaryIndigo),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${student.rank}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "আপনার অবস্থান: ${student.rank}ম স্থান",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = when (tab) {
                        LeaderboardMainTab.OVERALL_MOCK -> "মক টেস্ট: ${student.mockScore}/৯০ • শুভম পালের চেয়ে ৫ নম্বর পেছনে"
                        LeaderboardMainTab.BOSS_BATTLES -> "বস জয়: ${student.bossVictories}টি • আর ৭টি জয় করলে ১ম স্থান!"
                        LeaderboardMainTab.OVERALL_XP -> "মোট XP: ${formatCompactNumber(student.xp)} • পরবর্তী স্তরে যেতে ৩০০ XP প্রয়োজন"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onTakeAction,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("leaderboard_boost_rank_btn")
            ) {
                Text(
                    text = when (tab) {
                        LeaderboardMainTab.OVERALL_MOCK -> "মক দিন"
                        LeaderboardMainTab.BOSS_BATTLES -> "বস যুদ্ধ"
                        LeaderboardMainTab.OVERALL_XP -> "XP অর্জন"
                        else -> "প্র্যাকটিস"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun SubjectStudentComparisonSheet(
    student: LeaderboardStudent,
    subjects: List<SubjectItem>,
    isBengali: Boolean,
    onChallenge: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .testTag("student_comparison_sheet"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = PrimaryIndigo
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${student.school} • ${student.district}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Text(
            text = "বিষয়ভিত্তিক মক টেস্ট স্কোর তুলনা (৯০ নম্বরের ভিত্তিতে):",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Subject Comparison Table
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                subjects.forEach { subj ->
                    val opponentScore = student.subjectMockScores[subj.id] ?: 75
                    val myScore = when (subj.id) {
                        "phy_sci" -> 84
                        "life_sci" -> 86
                        "math" -> 76
                        "history" -> 82
                        "geography" -> 85
                        "bengali" -> 83
                        "english" -> 80
                        else -> 80
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(subj.colorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subj.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // My Score vs Opponent Score
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryIndigo.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "আপনি: $myScore",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryIndigo,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (opponentScore > myScore) CorrectGreen.copy(alpha = 0.15f) else ExamGold.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$opponentScore / 90",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (opponentScore > myScore) CorrectGreen else ExamGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onChallenge,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("challenge_opponent_btn")
            ) {
                Icon(Icons.Default.SportsKabaddi, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "১v১ লাইভ যুদ্ধ আহ্বান",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun AllSubjectsComparisonDialog(
    subjects: List<SubjectItem>,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("বন্ধ করুন", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "৭টি বিষয়ের পূর্ণাঙ্গ মেধা বিশ্লেষণ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "আপনার মক টেস্ট নম্বর, রাজ্য সেরা (Topper) ও রাজ্য গড়ের (State Avg) তুলনামূলক চার্ট:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(subjects) { subj ->
                    val myScore = when (subj.id) {
                        "phy_sci" -> 84
                        "life_sci" -> 86
                        "math" -> 76
                        "history" -> 82
                        "geography" -> 85
                        "bengali" -> 83
                        "english" -> 80
                        else -> 80
                    }
                    val topScore = when (subj.id) {
                        "math" -> 90
                        "phy_sci" -> 89
                        "life_sci" -> 90
                        "bengali" -> 88
                        else -> 88
                    }
                    val avgScore = 65

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, Color(subj.colorHex).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(subj.colorHex))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = subj.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "আপনি: $myScore",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = Color(subj.colorHex)
                                    )
                                    Text(
                                        text = "• টপার: $topScore",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExamGold
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { myScore.toFloat() / 90f },
                                color = Color(subj.colorHex),
                                trackColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (myScore >= 80) "🌟 শক্তিশালী বিষয় (Strong)" else "📈 আরও অনুশীলনের সুযোগ রয়েছে",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (myScore >= 80) CorrectGreen else Color(0xFFD97706)
                                )
                                Text(
                                    text = "গড়: $avgScore/৯০",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ShareRankCardDialog(
    item: SubjectLeaderboardItem,
    subject: SubjectItem?,
    onClose: () -> Unit,
    onShare: (String) -> Unit
) {
    val shareText = buildString {
        append("🏆 পশ্চিমবঙ্গ মাধ্যমিক ২০২৬ মেধা তালিকায় আমার স্থান!\n")
        append("📌 বিষয়: ${subject?.name ?: "সকল বিষয়"}\n")
        append("🏅 রাজ্য মেধা ক্রম: #${item.subjectRank}ম স্থান\n")
        append("📊 স্কোর: ${item.subjectScore} / ৯০ (শীর্ষ ${(100 - item.subjectPercentile).toInt()}%)\n")
        append("🏫 স্কুল: ${item.student.school}, ${item.student.district}\n\n")
        append("তুমিও পরীক্ষা দিয়ে নিজের মেধা যাচাই করো দেশ এক্সাম অ্যাপে! 📲")
    }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            Button(
                onClick = {
                    onShare(shareText)
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExamGold,
                    contentColor = Color(0xFF78350F)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("শেয়ার করুন", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("বন্ধ করুন")
            }
        },
        title = {
            Text(
                text = "মেধা সার্টিফিকেট ও র‍্যাঙ্ক কার্ড",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                border = BorderStroke(2.dp, ExamGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = ExamGold,
                        modifier = Modifier.size(44.dp)
                    )

                    Text(
                        text = "WEST BENGAL MADHYAMIK 2026",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ExamGold
                    )

                    Text(
                        text = item.student.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )

                    Text(
                        text = "${item.student.school} • ${item.student.district}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(color = ExamGold.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "রাজ্য র‍্যাঙ্ক",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "#${item.subjectRank}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = ExamGold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "স্কোর",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${item.subjectScore}/৯০",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "পার্সেন্টাইল",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${item.subjectPercentile}%",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = ExamCyan
                            )
                        }
                    }
                }
            }
        }
    )
}
