package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AssessmentMode
import com.example.data.model.BadgeCategory
import com.example.data.model.BadgeItem
import com.example.data.model.StudentProfile
import com.example.data.model.getTitle
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel

enum class ProfileSubTab {
    PROGRESS,
    BADGES,
    ACCOUNT_SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: DeshExamViewModel,
    profile: StudentProfile,
    isBengali: Boolean,
    onToggleLanguage: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ProfileSubTab.PROGRESS) }
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val authUser by viewModel.authUserState.collectAsStateWithLifecycle()
    val isGuest by viewModel.isGuestUser.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val authSuccess by viewModel.authSuccessMessage.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val celebratingBadge by viewModel.celebratingBadge.collectAsStateWithLifecycle()
    val districts by viewModel.districts.collectAsStateWithLifecycle()
    val districtSchoolsMap by viewModel.districtSchoolsMap.collectAsStateWithLifecycle()
    val isLoadingDistricts by viewModel.isLoadingDistricts.collectAsStateWithLifecycle()

    // Selected Badge for Detail Modal
    var selectedBadgeForDetail by remember { mutableStateOf<BadgeItem?>(null) }

    // Edit Profile State
    var isEditingProfile by remember { mutableStateOf(false) }
    var editName by remember(profile.name) { mutableStateOf(profile.name) }
    var editSchool by remember(profile.school) { mutableStateOf(profile.school) }
    var editDistrict by remember(profile.district) { mutableStateOf(profile.district) }
    var editTargetExam by remember(profile.targetExam) { mutableStateOf(profile.targetExam) }

    // Change Password State
    var isChangingPassword by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Badge filter (Status + Category)
    var badgeFilter by remember { mutableStateOf("ALL") }
    var selectedCategoryFilter by remember { mutableStateOf(BadgeCategory.ALL) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen")
    ) {
        ScreenHeaderBar(
            title = "প্রোফাইল",
            subtitle = profile.name.ifBlank { "শিক্ষার্থী" },
            isBengali = isBengali,
            showBackButton = true,
            onBack = { viewModel.goBack() },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "সেটিংস",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // 1. Student Identity Hero Card with Level & XP Progress
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E1B4B),
                                    PrimaryIndigo,
                                    Color(0xFF3B82F6)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar Initial with Crown/Badge
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(3.5.dp, ExamGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val displayName = if (authUser?.displayName.isNullOrBlank()) profile.name else authUser?.displayName ?: profile.name
                            Text(
                                text = displayName.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                color = PrimaryIndigo
                            )
                        }

                        // Student Name
                        Text(
                            text = if (authUser?.displayName.isNullOrBlank()) profile.name else authUser?.displayName ?: profile.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )

                        // School & Target Exam
                        Text(
                            text = "${profile.school} • ${profile.district} • ${profile.targetExam}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.88f),
                            textAlign = TextAlign.Center
                        )

                        // League / Status Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExamGold.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.6f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = ExamGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = profile.league,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ExamGold
                                )
                            }
                        }

                        // Level Progress Bar
                        val levelProgress = (profile.currentXp.toFloat() / profile.xpToNextLevel.toFloat()).coerceIn(0f, 1f)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "লেভেল ${profile.level} প্রগ্রেস",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${profile.currentXp} / ${profile.xpToNextLevel} XP (${(levelProgress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ExamGold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { levelProgress },
                                color = ExamGold,
                                trackColor = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // 4 Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatColumn(
                                value = "${profile.level}",
                                label = "লেভেল",
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatColumn(
                                value = "${profile.currentXp}",
                                label = "XP",
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatColumn(
                                value = "${profile.currentStreak} দিন",
                                label = "স্ট্রিক",
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatColumn(
                                value = "#${profile.rankState}",
                                label = "রাজ্য র‍্যাঙ্ক",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 2. Tab Navigation Selector (Progress, Badges, Account Settings)
        item {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .height(48.dp)
            ) {
                Tab(
                    selected = selectedTab == ProfileSubTab.PROGRESS,
                    onClick = {
                        selectedTab = ProfileSubTab.PROGRESS
                        viewModel.clearAuthMessages()
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "প্রোগ্রেস",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_profile_progress")
                )
                Tab(
                    selected = selectedTab == ProfileSubTab.BADGES,
                    onClick = {
                        selectedTab = ProfileSubTab.BADGES
                        viewModel.clearAuthMessages()
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ব্যাজসমূহ",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_profile_badges")
                )
                Tab(
                    selected = selectedTab == ProfileSubTab.ACCOUNT_SETTINGS,
                    onClick = {
                        selectedTab = ProfileSubTab.ACCOUNT_SETTINGS
                        viewModel.clearAuthMessages()
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "অ্যাকাউন্ট",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_profile_account")
                )
            }
        }

        // Global Alert Messages (Success / Error from Auth or Updates)
        if (!authError.isNullOrBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WrongRed.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = WrongRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = authError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = WrongRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (!authSuccess.isNullOrBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CorrectGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = CorrectGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = authSuccess ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = CorrectGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ==========================================
        // SUB-TAB 1: LEARNING PROGRESS & ANALYTICS
        // ==========================================
        if (selectedTab == ProfileSubTab.PROGRESS) {
            // 4 KPI Cards
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                Button(
                    onClick = {
                        val pdfFile = com.example.util.PdfReportGenerator.generateProgressPdf(context, profile, subjects, isBengali)
                        if (pdfFile != null) {
                            com.example.util.PdfReportGenerator.sharePdf(context, pdfFile, isBengali)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("share_progress_pdf_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF Report",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "শিক্ষক বা অভিভাবকদের জন্য PDF প্রোগ্রেস রিপোর্ট শেয়ার করুন",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricHighlightCard(
                        title = "মোট প্রশ্ন সমাধান",
                        value = "${profile.totalQuestionsAnswered}",
                        icon = Icons.Default.Quiz,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    MetricHighlightCard(
                        title = "সার্বিক নির্ভুলতা",
                        value = "${profile.overallAccuracy}%",
                        icon = Icons.Default.CheckCircle,
                        color = CorrectGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                        MetricHighlightCard(
                        title = "প্রস্তুতি স্কোর",
                        value = "${profile.examReadinessPercentage}%",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = ExamGold,
                        modifier = Modifier.weight(1f)
                    )
                    MetricHighlightCard(
                        title = "মোট অধ্যয়নের দিন",
                        value = "${profile.totalStudyDays} দিন",
                        icon = Icons.Default.CalendarMonth,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interactive Progress Visualization Charts Card
            item {
                ProgressVisualizationCard(isBengali = isBengali)
            }

            // Subject-wise Mastery Matrix
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বিষয়ভিত্তিক দক্ষতা ম্যাট্রিক্স",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryIndigo.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "WBBSE সিলেবাস",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryIndigo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Subject mastery rows
                        subjects.forEach { subject ->
                            val subjectTitle = subject.getTitle(isBengali).ifBlank {
                                subject.name.ifBlank {
                                    if (isBengali) subject.code.subjectNameBengali else subject.code.subjectNameEnglish
                                }
                            }
                            val subjectColor = if (subject.colorHex != 0L) Color(subject.colorHex) else Color(subject.code.colorHex)
                            val displayMastery = if (subject.masteryPercentage > 0) {
                                subject.masteryPercentage
                            } else if (subject.completedChapters > 0 && subject.totalChapters > 0) {
                                ((subject.completedChapters.toFloat() / subject.totalChapters) * 100).toInt()
                            } else {
                                when (subject.code) {
                                    com.example.data.model.SubjectCode.PHYSICAL_SCIENCE -> 75
                                    com.example.data.model.SubjectCode.MATHEMATICS -> 65
                                    com.example.data.model.SubjectCode.LIFE_SCIENCE -> 80
                                    com.example.data.model.SubjectCode.BENGALI -> 80
                                    com.example.data.model.SubjectCode.HISTORY -> 75
                                    com.example.data.model.SubjectCode.GEOGRAPHY -> 70
                                    com.example.data.model.SubjectCode.ENGLISH -> 72
                                    com.example.data.model.SubjectCode.BENGALI_GRAMMAR -> 50
                                    com.example.data.model.SubjectCode.ENGLISH_GRAMMAR -> 45
                                    else -> 60
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = subjectTitle,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${displayMastery}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = subjectColor
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (displayMastery / 100f).coerceIn(0.05f, 1f) },
                                    color = subjectColor,
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

            // Streak & Consistency Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ধারাবাহিকতা ও স্ট্রিক",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "সেরা স্ট্রিক: ${profile.bestStreak} দিন | বোনাস: ${profile.streakMultiplier}x",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExamGold.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = ExamGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${profile.streakShields} Shield",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ExamGold
                                )
                            }
                        }
                    }
                }
            }
            // Featured Achievement Badges Row
            item {
                FeaturedBadgesRow(
                    badges = badges,
                    isBengali = isBengali,
                    onBadgeClick = { selectedBadgeForDetail = it }
                )
            }
        }

        // ==========================================
        // SUB-TAB 2: BADGES & TROPHY CABINET
        // ==========================================
        if (selectedTab == ProfileSubTab.BADGES) {
            // Badges Summary & Filter
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val unlockedCount = badges.count { it.isUnlocked }
                        val totalXpFromBadges = badges.filter { it.isUnlocked }.sumOf { it.xpReward }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "অর্জিত মেডেল ও ট্রফি কেবিনেট",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$unlockedCount / ${badges.size} টি মেডেল আনলক হয়েছে | +$totalXpFromBadges XP অর্জিত",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            CircularProgressIndicator(
                                progress = { if (badges.isNotEmpty()) unlockedCount.toFloat() / badges.size else 0f },
                                color = ExamGold,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 4.5.dp
                            )
                        }

                        // Status Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = badgeFilter == "ALL",
                                onClick = { badgeFilter = "ALL" },
                                label = { Text("সব (${badges.size})") }
                            )
                            FilterChip(
                                selected = badgeFilter == "EARNED",
                                onClick = { badgeFilter = "EARNED" },
                                label = { Text("অর্জিত ($unlockedCount)") }
                            )
                            FilterChip(
                                selected = badgeFilter == "LOCKED",
                                onClick = { badgeFilter = "LOCKED" },
                                label = { Text("লকড (${badges.size - unlockedCount})") }
                            )
                        }

                        // Category Filter Chips
                        Text(
                            text = "ক্যাটেগরি অনুযায়ী ফিল্টার:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                BadgeCategory.ALL to "সকল",
                                BadgeCategory.ACCURACY to "নিখুঁত স্কোর",
                                BadgeCategory.CONSISTENCY to "ধারাবাহিকতা",
                                BadgeCategory.TIME to "সময় সাধনা",
                                BadgeCategory.MASTERY to "মাস্টারি",
                                BadgeCategory.EXAM to "মাধ্যমিক"
                            ).forEach { (cat, label) ->
                                FilterChip(
                                    selected = selectedCategoryFilter == cat,
                                    onClick = { selectedCategoryFilter = cat },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }

            val filteredBadges = badges.filter { badge ->
                val matchesStatus = when (badgeFilter) {
                    "EARNED" -> badge.isUnlocked
                    "LOCKED" -> !badge.isUnlocked
                    else -> true
                }
                val matchesCategory = selectedCategoryFilter == BadgeCategory.ALL || badge.category == selectedCategoryFilter
                matchesStatus && matchesCategory
            }

            if (filteredBadges.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "এই ক্যাটেগরিতে কোনো মেডেল পাওয়া যায়নি",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredBadges, key = { it.id }) { badge ->
                    AchievementBadgeCard(
                        badge = badge,
                        isBengali = isBengali,
                        onClick = { selectedBadgeForDetail = badge }
                    )
                }
            }
        }

        // ==========================================
        // SUB-TAB 3: ACCOUNT SETTINGS & FIREBASE
        // ==========================================
        if (selectedTab == ProfileSubTab.ACCOUNT_SETTINGS) {
            // 0. Class Selector Card (always visible, no edit mode needed)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "আমার শ্রেণি নির্বাচন করুন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "এখান থেকে ক্লাস বেছে নিন — অ্যাপের সব কন্টেন্ট সেই ক্লাস অনুযায়ী ফিল্টার হবে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val allClassesList = remember { com.example.data.model.AcademicClassProvider.ALL_CLASSES }

                        // Display classes in a wrap-flow style using chunked rows
                        val chunkedClasses = allClassesList.chunked(3)
                        chunkedClasses.forEach { rowClasses ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowClasses.forEach { cls ->
                                    val isSelected = profile.classId == cls.id
                                    val bgColor = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                                    Surface(
                                        onClick = {
                                            if (!isSelected) {
                                                viewModel.switchActiveExamClass(cls.id)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = bgColor,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("class_selector_${cls.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(
                                                text = "Class ${cls.classNumber}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                                ),
                                                color = textColor,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = cls.board,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = textColor.copy(alpha = 0.75f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                // Fill remaining space if row has < 3 items
                                repeat(3 - rowClasses.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Stream Selection for Class 11 & 12
                        val isSeniorSec = profile.classId in listOf("class_11_wbchse", "class_12_wbchse")
                        if (isSeniorSec) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = "স্ট্রিম / বিভাগ নির্বাচন করুন (Choose Stream):",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val streamsList = listOf(
                                Triple("science", "বিজ্ঞান (Science)", Icons.Default.Science),
                                Triple("arts", "কলা (Arts)", Icons.Default.AutoStories),
                                Triple("commerce", "বাণিজ্য", Icons.Default.AccountBalanceWallet),
                                Triple("all", "সব (All)", Icons.Default.Apps)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                streamsList.forEach { (streamKey, label, icon) ->
                                    val isStreamSelected = profile.stream == streamKey
                                    val chipBgColor = if (isStreamSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    val chipTextColor = if (isStreamSelected) Color.White else MaterialTheme.colorScheme.onSurface

                                    Surface(
                                        onClick = {
                                            viewModel.switchStream(streamKey)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = chipBgColor,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("stream_selector_$streamKey")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isStreamSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                color = chipTextColor,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Currently selected class info
                        val currentClass = remember(profile.classId) {
                            allClassesList.find { it.id == profile.classId }
                        }
                        if (currentClass != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PrimaryIndigo.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    val activeStreamName = when (profile.stream) {
                                        "science" -> "বিজ্ঞান শাখা"
                                        "arts" -> "কলা বিভাগ"
                                        "commerce" -> "বাণিজ্য শাখা"
                                        else -> "সকল বিষয়"
                                    }
                                    val streamSuffix = if (isSeniorSec) " (${activeStreamName})" else ""
                                    Text(
                                        text = "বর্তমান: ${currentClass.nameBengali}$streamSuffix • ${currentClass.board}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = PrimaryIndigo
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. Firebase Auth Connection Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryIndigo.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ফায়ারবেস ক্লাউড অ্যাকাউন্ট",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (authUser?.email != null) "ইমেইল: ${authUser?.email}" else if (isGuest) "অতিথি ব্যবহারকারী (Guest User)" else "অফলাইন ব্যবহারকারী",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (authUser?.email != null) CorrectGreen.copy(alpha = 0.15f) else ExamGold.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (authUser?.email != null) "Verified" else if (isGuest) "Guest" else "Local",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (authUser?.email != null) CorrectGreen else ExamGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (authUser?.uid != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "UID: ${authUser?.uid?.take(16)}...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.syncAllDataWithFirebase { _, _ -> } },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔄 ফায়ারবেসের সাথে ডেটা সিঙ্ক করুন",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Edit Profile Details (Room + Firebase Sync)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "প্রোফাইল তথ্য সম্পাদনা",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(onClick = { viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.EDIT_PROFILE) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = PrimaryIndigo
                                )
                            }
                        }

                        if (isEditingProfile) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("শিক্ষার্থীর পুরো নাম") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_edit_name")
                            )

                            // District Selector Dropdown
                            var districtExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = districtExpanded,
                                onExpandedChange = { districtExpanded = !districtExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = editDistrict,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("জেলা (District)") },
                                    leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
                                    trailingIcon = {
                                        if (isLoadingDistricts) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded)
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                        .testTag("input_edit_district_dropdown")
                                )
                                ExposedDropdownMenu(
                                    expanded = districtExpanded,
                                    onDismissRequest = { districtExpanded = false }
                                ) {
                                    districts.forEach { dist ->
                                        DropdownMenuItem(
                                            text = { Text(dist) },
                                            onClick = {
                                                editDistrict = dist
                                                districtExpanded = false
                                                val suggestedSchools = districtSchoolsMap[dist] ?: emptyList()
                                                if (suggestedSchools.isNotEmpty()) {
                                                    editSchool = suggestedSchools.first()
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // School Selector Dropdown filtered by District
                            var schoolExpanded by remember { mutableStateOf(false) }
                            val districtSchools = remember(editDistrict) { districtSchoolsMap[editDistrict] ?: emptyList() }
                            ExposedDropdownMenuBox(
                                expanded = schoolExpanded,
                                onExpandedChange = { schoolExpanded = !schoolExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = editSchool,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("বিদ্যালয়ের নাম (School)") },
                                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schoolExpanded) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                        .testTag("input_edit_school_dropdown")
                                )
                                ExposedDropdownMenu(
                                    expanded = schoolExpanded,
                                    onDismissRequest = { schoolExpanded = false }
                                ) {
                                    if (districtSchools.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("কোনো স্কুল পাওয়া যায়নি") },
                                            onClick = { schoolExpanded = false }
                                        )
                                    } else {
                                        districtSchools.forEach { sch ->
                                            DropdownMenuItem(
                                                text = { Text(sch) },
                                                onClick = {
                                                    editSchool = sch
                                                    schoolExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            var targetExamExpanded by remember { mutableStateOf(false) }
                            val allClassesList = remember { com.example.data.model.AcademicClassProvider.ALL_CLASSES }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "শ্রেণি ও টার্গেট পরীক্ষা নির্বাচন করুন:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(allClassesList) { cls ->
                                        val isSelected = editTargetExam == cls.targetExam || editTargetExam.contains("${cls.classNumber}")
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { editTargetExam = cls.targetExam },
                                            label = { Text("${cls.classNumber}ম শ্রেণি", fontSize = 12.sp) }
                                        )
                                    }
                                }

                                ExposedDropdownMenuBox(
                                    expanded = targetExamExpanded,
                                    onExpandedChange = { targetExamExpanded = !targetExamExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = editTargetExam,
                                        onValueChange = { editTargetExam = it },
                                        label = { Text("টার্গেট পরীক্ষা ও শ্রেণি") },
                                        leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExamExpanded) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                                            .fillMaxWidth()
                                            .testTag("input_edit_target_exam")
                                    )
                                    ExposedDropdownMenu(
                                        expanded = targetExamExpanded,
                                        onDismissRequest = { targetExamExpanded = false }
                                    ) {
                                        allClassesList.forEach { cls ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(cls.targetExam, fontWeight = FontWeight.Bold)
                                                        Text(cls.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                },
                                                onClick = {
                                                    editTargetExam = cls.targetExam
                                                    targetExamExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.updateProfileDetails(editName, editSchool, editDistrict, editTargetExam)
                                    isEditingProfile = false
                                },
                                enabled = !isAuthLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_save_profile_details")
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "তথ্য সেভ করুন",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        } else {
                            // Read-only info preview
                            ProfileInfoRow(
                                label = "নাম",
                                value = if (authUser?.displayName.isNullOrBlank()) profile.name else authUser?.displayName ?: profile.name,
                                icon = Icons.Default.Person
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ProfileInfoRow(
                                label = "বিদ্যালয়",
                                value = profile.school,
                                icon = Icons.Default.School
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ProfileInfoRow(
                                label = "টার্গেট পরীক্ষা",
                                value = profile.targetExam,
                                icon = Icons.Default.Flag
                            )
                        }
                    }
                }
            }

            // 3. Security & Password Change
            if (authUser?.email != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LockReset,
                                        contentDescription = null,
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "নিরাপত্তা ও পাসওয়ার্ড",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                TextButton(onClick = { isChangingPassword = !isChangingPassword }) {
                                    Text(
                                        text = if (isChangingPassword) "বাতিল" else "পরিবর্তন",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryIndigo
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isChangingPassword) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = newPassword,
                                        onValueChange = { newPassword = it },
                                        label = { Text("নতুন পাসওয়ার্ড") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                        trailingIcon = {
                                            IconButton(onClick = { showPassword = !showPassword }) {
                                                Icon(
                                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = confirmNewPassword,
                                        onValueChange = { confirmNewPassword = it },
                                        label = { Text("নতুন পাসওয়ার্ড নিশ্চিত করুন") },
                                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            focusManager.clearFocus()
                                            if (newPassword == confirmNewPassword) {
                                                viewModel.updateAccountPassword(newPassword)
                                                newPassword = ""
                                                confirmNewPassword = ""
                                                isChangingPassword = false
                                            }
                                        },
                                        enabled = !isAuthLoading && newPassword.isNotBlank() && newPassword == confirmNewPassword,
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (isAuthLoading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text(
                                                text = "পাসওয়ার্ড আপডেট করুন",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Reset Link Option
                            OutlinedButton(
                                onClick = {
                                    authUser?.email?.let { email ->
                                        viewModel.sendPasswordReset(email)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ইমেইলে রিসেট লিংক পাঠান",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Quick Preferences & Language
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "অ্যাপ পছন্দ ও ইন্টারফেস",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Language Switcher Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ভাষা (Language)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "বর্তমান: বাংলা (Bengali)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutlinedButton(
                                onClick = onToggleLanguage,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("toggle_language_btn")
                            ) {
                                Text(
                                    text = "English এ পরিবর্তন",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                        // Audio feedback toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "শব্দ ও ভাইব্রেশন প্রতিক্রিয়া",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "সঠিক উত্তরের সাউন্ড ইফেক্ট",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = appSettings.soundEffectsEnabled,
                                onCheckedChange = { viewModel.toggleSoundEffects(it) }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                        // Full Control Panel shortcut
                        Button(
                            onClick = onOpenSettings,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_open_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = DarkBackground,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সম্পূর্ণ সেটিংস প্যানেল",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = DarkBackground
                            )
                        }
                    }
                }
            }

            // 5. Sign Out / Switch Account
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WrongRed.copy(alpha = 0.06f)),
                    border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "অ্যাকাউন্ট সাইন আউট",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = WrongRed
                        )
                        Text(
                            text = "লগআউট করলে আপনার ক্লাউড ডাটা সুরক্ষিত থাকবে এবং নতুন একাউন্টে প্রবেশ করতে পারবেন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = { viewModel.signOut() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WrongRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WrongRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_signout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "লগআউট / অ্যাকাউন্ট পরিবর্তন করুন",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }

    // Achievement Badge Detail Dialog
    selectedBadgeForDetail?.let { badge ->
        AchievementBadgeDetailDialog(
            badge = badge,
            isBengali = isBengali,
            onDismiss = { selectedBadgeForDetail = null },
            onStartPractice = { mode ->
                selectedBadgeForDetail = null
                viewModel.startAssessment(mode)
            }
        )
    }

    // Achievement Unlocked Celebration Pop-up
    celebratingBadge?.let { badge ->
        AchievementUnlockedCelebrationDialog(
            badge = badge,
            isBengali = isBengali,
            onDismiss = { viewModel.dismissBadgeCelebration() }
        )
    }
}
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryIndigo,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MetricHighlightCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProfileStatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}
