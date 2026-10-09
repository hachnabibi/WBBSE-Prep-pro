package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookmarkEntity
import com.example.data.local.RecentlyViewedEntity
import com.example.data.local.SubjectEntity
import com.example.data.model.AcademicClassProvider
import com.example.data.model.AssessmentMode
import com.example.data.model.DailyQuest
import com.example.data.model.StudentProfile
import com.example.data.model.SubjectItem
import com.example.data.model.getTitle
import com.example.data.model.getTextbook
import com.example.data.model.getClassLevelName
import com.example.ui.components.AdBannerCard
import com.example.ui.components.BossBattleCard
import com.example.ui.components.PerformanceSummaryCard
import com.example.ui.components.StreakTrackerCard
import com.example.ui.components.SubjectIconBadge
import com.example.ui.theme.*
import com.example.util.formatCompactNumber
import com.example.ui.viewmodel.AppScreen

@Composable
fun RecentlyViewedCard(item: RecentlyViewedEntity, isBengali: Boolean) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(
                imageVector = if (item.type == "PYQ") Icons.Default.Description else Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HomeScreen(
    profile: StudentProfile,
    subjects: List<SubjectItem>,
    dailyQuests: List<DailyQuest>,
    recentlyViewed: List<RecentlyViewedEntity>,
    bookmarks: List<BookmarkEntity>,
    activeStudentsCount: Int,
    isBengali: Boolean,
    onStartAssessment: (AssessmentMode, String?, String?) -> Unit,
    onSelectSubject: (String) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onSwitchClass: (String, String) -> Unit = { _, _ -> },
    onSwitchStream: (String) -> Unit = {},
    onRepairStreak: () -> Unit = {},
    onOpenSubjectDetail: ((SubjectEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }



    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) subjects
        else {
            val q = searchQuery.trim().lowercase()
            subjects.filter { subject ->
                subject.displayName.lowercase().contains(q) ||
                subject.nameEnglish.lowercase().contains(q) ||
                subject.nameBengali.lowercase().contains(q) ||
                subject.code.id.lowercase().contains(q) ||
                subject.code.name.lowercase().contains(q)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Personalized Student Greeting & Quick Stats Header
        item {
            StudentGreetingHeader(
                profile = profile,
                isBengali = isBengali,
                onOpenProfile = { onNavigate(AppScreen.PROFILE) },
                onOpenWallet = { onNavigate(AppScreen.WALLET) },
                onSwitchClassClick = { onSwitchClass(profile.classId, profile.stream) },
                onOpenBadges = { onNavigate(AppScreen.BADGES) }
            )
        }

        // 1. Real-time Active Students Pulse Ticker
        item {
            ActiveStudentsTicker(count = activeStudentsCount, isBengali = isBengali)
        }

        // 2. Hero Card: Exam Readiness & Countdown
        item {
            HeroExamReadinessCard(
                profile = profile,
                isBengali = isBengali,
                onQuickDaily10 = {
                    onStartAssessment(AssessmentMode.DAILY_10, null, null)
                }
            )
        }

        // 3. Performance Summary with Vico Charts (Weekly Learning Progress, Active Streaks, Total Points & Daily Usage Incentives)
        item {
            PerformanceSummaryCard(
                profile = profile,
                isBengali = isBengali,
                onPracticeNow = {
                    onStartAssessment(AssessmentMode.DAILY_10, null, null)
                },
                onViewFullAnalytics = {
                    onNavigate(AppScreen.PROGRESS_DASHBOARD)
                }
            )
        }

        // 4. Daily Learning Streak Tracker
        item {
            StreakTrackerCard(
                profile = profile,
                isBengali = isBengali,
                onPracticeNow = {
                    onStartAssessment(AssessmentMode.DAILY_10, null, null)
                },
                onRepairStreak = onRepairStreak
            )
        }

        // 4. Recently Viewed section (if any)
        if (recentlyViewed.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBengali) "সম্প্রতি দেখা বিষয়বস্তু" else "Recently Viewed",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(recentlyViewed) { item ->
                            RecentlyViewedCard(item, isBengali)
                        }
                    }
                }
            }
        }

        // 5. ⚡ কুইক প্র্যাকটিস হাব (Tactile 2x2 Practice Grid)
        item {
            val firstActiveSub = subjects.firstOrNull()
            QuickPractice2x2Grid(
                firstActiveSubId = firstActiveSub?.id,
                isBengali = isBengali,
                onStartAssessment = onStartAssessment
            )
        }

        // 6. Active Class & Stream Switcher Banner
        item {
            ActiveClassBanner(
                profile = profile,
                isBengali = isBengali,
                onSwitchClass = onSwitchClass,
                onSwitchStream = onSwitchStream
            )
        }

        // 7. 📚 বিষয়ভিত্তিক সিলেবাস ও অগ্রগতি (Curriculum Hub)
        item {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBengali) "বিষয়ভিত্তিক সিলেবাস ও অগ্রগতি" else "Subject Syllabus & Progress",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isBengali) "বোর্ড পাঠ্যক্রম অনুযায়ী সাজানো" else "Aligned with Board Curriculum",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${subjects.size} টি বিষয়",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Search Bar for Subjects
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subjects_search_input"),
                    placeholder = {
                        Text(
                            text = if (isBengali) "বিষয় খুঁজুন..." else "Search subject...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                if (filteredSubjects.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "'$searchQuery' নামে কোনো বিষয় পাওয়া যায়নি",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedButton(
                                onClick = { searchQuery = "" },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("সার্চ ক্লিয়ার করুন")
                            }
                        }
                    }
                } else {
                    val subjectRowState = rememberLazyListState()
                    if (filteredSubjects.size > 1) {
                        LaunchedEffect(filteredSubjects) {
                            delay(2000) // Initial wait
                            while (true) {
                                val currentVisible = subjectRowState.firstVisibleItemIndex
                                val nextIndex = (currentVisible + 1) % filteredSubjects.size
                                subjectRowState.animateScrollToItem(
                                    index = nextIndex,
                                    scrollOffset = 0
                                )
                                delay(4000) // Pause for 4 seconds on each card
                            }
                        }
                    }

                    LazyRow(
                        state = subjectRowState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_subject_cards_horizontal_row")
                    ) {
                        items(filteredSubjects) { subject ->
                            SubjectProgressCard(
                                subject = subject,
                                isBengali = isBengali,
                                onOpenJourney = {
                                    val subjectEntity = SubjectEntity(
                                        id = subject.id,
                                        code = subject.code.id,
                                        nameBengali = subject.name,
                                        nameEnglish = subject.name,
                                        totalChapters = subject.totalChapters,
                                        completedChapters = subject.completedChapters,
                                        masteryPercentage = subject.masteryPercentage,
                                        colorHex = subject.colorHex
                                    )
                                    if (onOpenSubjectDetail != null) {
                                        onOpenSubjectDetail(subjectEntity)
                                    } else {
                                        onSelectSubject(subject.id)
                                        onNavigate(AppScreen.JOURNEY)
                                    }
                                },
                                modifier = Modifier.width(280.dp)
                            )
                        }
                    }
                }
            }
        }

        // 8. 🗂️ অল-ইন-ওয়ান স্টাডি ও এক্সাম টুলস (Categorized Modern 4-Item Grid)
        item {
            CategorizedToolsHub(
                profile = profile,
                isBengali = isBengali,
                onNavigate = onNavigate
            )
        }

        // Sponsored Educational Banner & AdMob Card
        item {
            AdBannerCard(isBengali = isBengali)
        }

        item {
            com.example.ui.components.AdMobNativeCard(
                isBengali = isBengali,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 9. 🏆 লাইভ রাজ্য মেধা তালিকা • টপার্স স্পটলাইট
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AppScreen.LEADERBOARD) }
                    .testTag("home_leaderboard_spotlight_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ExamGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Leaderboard",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "রাজ্য মেধা তালিকা • টপার্স লিডারবোর্ড",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ExamFlame.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "লাইভ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = ExamFlame,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Text(
                            text = "১ম: অনন্যা (৮৯/৯০ মক • ১৮ বস জয়) • আপনি ${profile.rankState} স্থানে",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Leaderboard",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 10. 🎯 আজকের দৈনিক কোয়েস্ট ও মিশন
        item {
            DailyQuestsCard(
                quests = dailyQuests,
                isBengali = isBengali
            )
        }

        // 11. 🤖 এআই স্টাডি কোচ ডায়াগনস্টিক
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AppScreen.AI_COACH) }
                    .testTag("home_ai_coach_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Coach",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "এআই স্টাডি কোচ ডায়াগনস্টিক",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "দুর্বল ক্ষেত্র বিশ্লেষণ ও ব্যক্তিগত রিভিশন প্ল্যান প্রস্তুত!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 12. ⚠️ অগ্রাধিকার ভিত্তিক দুর্বল ক্ষেত্র ফোকাস
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WrongRed.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_weak_area_focus_card")
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Weak Area",
                                tint = WrongRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "অগ্রাধিকার ভিত্তিক দুর্বল ক্ষেত্র",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = WrongRed
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WrongRed.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "রিভিশন প্রয়োজন",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = WrongRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val weakSub = subjects.firstOrNull()
                    Text(
                        text = if (weakSub != null) "${weakSub.nameBengali} • গুরুত্বপূর্ণ অধ্যায় ও সূত্রাবলীর রিভিশন" else "বিষয়ভিত্তিক রিভিশন ও প্র্যাকটিস",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { onStartAssessment(AssessmentMode.WEAK_AREA, null, null) },
                        colors = ButtonDefaults.buttonColors(containerColor = WrongRed, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "এখনই দুর্বল ক্ষেত্র প্র্যাকটিস করো (+১০০ XP)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentGreetingHeader(
    profile: StudentProfile,
    isBengali: Boolean,
    onOpenProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    onSwitchClassClick: () -> Unit,
    onOpenBadges: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when (currentHour) {
        in 4..11 -> if (isBengali) "শুভ সকাল" else "Good Morning"
        in 12..16 -> if (isBengali) "শুভ দুপুর" else "Good Afternoon"
        in 17..21 -> if (isBengali) "শুভ সন্ধ্যা" else "Good Evening"
        else -> if (isBengali) "শুভ রাত্রি" else "Good Night"
    }

    val currentCls = remember(profile.classId) {
        AcademicClassProvider.ALL_CLASSES.find { it.id == profile.classId }
            ?: AcademicClassProvider.ALL_CLASSES.find { it.targetExam == profile.targetExam }
            ?: AcademicClassProvider.ALL_CLASSES.getOrNull(5)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Welcome greeting on its own full line to avoid truncation
        Text(
            text = "$greeting, ${profile.name.ifBlank { if (isBengali) "শিক্ষার্থী" else "Student" }}! 👋",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenProfile() }
        )

        // Class Selector on the left, Streak & Coins on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.clickable { onSwitchClassClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Class",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${currentCls?.name ?: "১০ম শ্রেণি"} • ${currentCls?.board ?: "WBBSE"} ▼",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Gamification stats (Streak & Coins)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ExamFlame.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ExamFlame.copy(alpha = 0.35f)),
                    modifier = Modifier.clickable { onOpenBadges() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🔥", fontSize = 13.sp)
                        Text(
                            text = "${profile.currentStreak}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = ExamFlame
                        )
                    }
                }

                // Coins
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ExamGold.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { onOpenWallet() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🪙", fontSize = 13.sp)
                        Text(
                            text = formatCompactNumber(profile.coins),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickPractice2x2Grid(
    firstActiveSubId: String?,
    isBengali: Boolean,
    onStartAssessment: (AssessmentMode, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = ExamGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isBengali) "কুইক প্র্যাকটিস হাব" else "Quick Practice Hub",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "দ্রুত প্রস্তুতি",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Row 1: Daily 10 & Mistake Replay
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactPracticeCard(
                title = "ডেইলি ১০",
                subtitle = "১০ প্রশ্ন • ১৫০ XP",
                badge = "জনপ্রিয়",
                icon = Icons.Default.Bolt,
                gradient = listOf(Color(0xFF4F46E5), Color(0xFF3730A3)),
                testTag = "quick_daily_10_btn",
                onClick = { onStartAssessment(AssessmentMode.DAILY_10, null, null) },
                modifier = Modifier.weight(1f)
            )

            CompactPracticeCard(
                title = "ভুল সংশোধন",
                subtitle = "দুর্বল প্রশ্ন রিভিশন",
                badge = "স্মার্ট",
                icon = Icons.Default.Replay,
                gradient = listOf(Color(0xFFDC2626), Color(0xFF991B1B)),
                testTag = "quick_wrong_replay_btn",
                onClick = { onStartAssessment(AssessmentMode.WEAK_AREA, null, null) },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Speed Blitz & Boss Battle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactPracticeCard(
                title = "স্পিড ব্লিৎজ",
                subtitle = "১৫ সে./প্রশ্ন ড্রিল",
                badge = "টাইমার",
                icon = Icons.Default.Timer,
                gradient = listOf(Color(0xFFEA580C), Color(0xFFC2410C)),
                testTag = "quick_speed_blitz_btn",
                onClick = { onStartAssessment(AssessmentMode.SPEED_BLITZ, null, null) },
                modifier = Modifier.weight(1f)
            )

            CompactPracticeCard(
                title = "বস চ্যালেঞ্জ",
                subtitle = "অধ্যায় মাস্টারি ট্রফি",
                badge = "ব্যাটেল",
                icon = Icons.Default.Shield,
                gradient = listOf(Color(0xFFD97706), Color(0xFFB45309)),
                testTag = "quick_boss_raid_btn",
                onClick = {
                    onStartAssessment(
                        AssessmentMode.BOSS_BATTLE,
                        firstActiveSubId?.let { "${it}_ch1" } ?: "phy_ch1",
                        null
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CompactPracticeCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradient))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CategorizedToolsHub(
    profile: StudentProfile,
    isBengali: Boolean,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val currentCls = remember(profile.classId) {
        AcademicClassProvider.ALL_CLASSES.find { it.id == profile.classId }
    }
    val clsLabel = currentCls?.name ?: "পাঠ্যক্রম"

    val tabs = listOf(
        Pair("📚 পড়াশোনা ও নোটস", 0),
        Pair("🎮 পরীক্ষা ও লাইভ", 1),
        Pair("🤖 এআই ও টুলস", 2)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$clsLabel বিশেষ টুলস ও প্রস্তুতি",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "প্রয়োজনীয় রিসোর্স ও লাইভ ফিচারসমূহ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Category Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent,
            indicator = {}
        ) {
            tabs.forEach { (title, idx) ->
                val isSelected = selectedTab == idx
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = idx },
                    label = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        // Tools for the selected category
        when (selectedTab) {
            0 -> {
                // Study Library & Notes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolTileCard(
                            title = "আসল প্রশ্নপত্র PDF",
                            subtitle = if (profile.classId.contains("wbchse")) "WBCHSE পেপার্স" else "WBBSE পেপার্স",
                            icon = Icons.Default.PictureAsPdf,
                            tint = Color(0xFFD97706),
                            testTag = "home_original_pyq_pdf_btn",
                            onClick = { onNavigate(AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolTileCard(
                            title = "সমাধানসহ PDF",
                            subtitle = "২০১৫-২০২৬ সল্যুশন",
                            icon = Icons.Default.FileDownload,
                            tint = Color(0xFF0284C7),
                            testTag = "home_solved_pyq_pdf_btn",
                            onClick = { onNavigate(AppScreen.PYQ_PDF_DOWNLOAD) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolTileCard(
                            title = "পিডিএফ নোটস",
                            subtitle = "অধ্যায়ভিত্তিক মেটেরিয়াল",
                            icon = Icons.Default.Description,
                            tint = Color(0xFFF43F5E),
                            testTag = "home_pdf_notes_btn",
                            onClick = { onNavigate(AppScreen.PDF_NOTES) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolTileCard(
                            title = "স্মার্ট ফ্ল্যাশকার্ড",
                            subtitle = "সূত্র ও রিভিশন ডেক",
                            icon = Icons.Default.Style,
                            tint = Color(0xFF6366F1),
                            testTag = "home_flashcards_btn",
                            onClick = { onNavigate(AppScreen.FLASHCARDS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolTileCard(
                            title = "পাঠ্যপুস্তক PDF",
                            subtitle = "বোর্ড অনুমোদিত বই",
                            icon = Icons.Default.AutoStories,
                            tint = Color(0xFF0D9488),
                            testTag = "home_textbooks_pdf_btn",
                            onClick = { onNavigate(AppScreen.TEXTBOOKS) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolTileCard(
                            title = "অডিও পডকাস্ট",
                            subtitle = "স্পিচ রিভিশন নোটস",
                            icon = Icons.Default.Headphones,
                            tint = Color(0xFF8B5CF6),
                            testTag = "home_podcasts_btn",
                            onClick = { onNavigate(AppScreen.AUDIO_PODCASTS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            1 -> {
                // Exams & Live Competitions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolTileCard(
                            title = "১v১ কুইজ ব্যাটেল",
                            subtitle = "লাইভ লড়াই ও XP",
                            icon = Icons.Default.SportsEsports,
                            tint = Color(0xFFDC2626),
                            testTag = "home_battle_btn",
                            onClick = { onNavigate(AppScreen.MULTIPLAYER_BATTLE) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolTileCard(
                            title = "লাইভ কুইজ",
                            subtitle = "১০০০+ প্রতিযোগী",
                            icon = Icons.Default.Groups,
                            tint = Color(0xFF8B5CF6),
                            testTag = "home_live_quiz_btn",
                            onClick = { onNavigate(AppScreen.LIVE_QUIZ) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolTileCard(
                            title = "লাইভ মক টেস্ট",
                            subtitle = "১৫ মিনিট • রিয়েল এক্সাম",
                            icon = Icons.Default.Timer,
                            tint = Color(0xFFE11D48),
                            testTag = "home_live_mock_test_btn",
                            onClick = { onNavigate(AppScreen.LIVE_MOCK_TEST) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolTileCard(
                            title = "OMR শিট সিমুলেটর",
                            subtitle = "অপটিক্যাল স্ক্যান",
                            icon = Icons.Default.DocumentScanner,
                            tint = Color(0xFF7C3AED),
                            testTag = "home_omr_btn",
                            onClick = { onNavigate(AppScreen.OMR_PRACTICE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolTileCard(
                            title = "PYQ এক্সপ্লোরার",
                            subtitle = "২০১৫-২০২৫ আসল প্রশ্ন",
                            icon = Icons.Default.HistoryEdu,
                            tint = Color(0xFF0D9488),
                            testTag = "home_pyq_btn",
                            onClick = { onNavigate(AppScreen.PYQ_EXPLORER) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolTileCard(
                            title = "স্কুল স্কোয়াড",
                            subtitle = "টিম লিডারবোর্ড",
                            icon = Icons.Default.Groups,
                            tint = Color(0xFF059669),
                            testTag = "home_squads_btn",
                            onClick = { onNavigate(AppScreen.STUDY_SQUADS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            2 -> {
                // Smart AI & Analytics
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolTileCard(
                            title = "স্ন্যাপ ডাউট সলভার",
                            subtitle = "এআই ফটো স্ক্যান",
                            icon = Icons.Default.CameraAlt,
                            tint = Color(0xFF0284C7),
                            testTag = "home_doubt_btn",
                            onClick = { onNavigate(AppScreen.DOUBT_SOLVER) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolTileCard(
                            title = "টপার্স খাতা গাইড",
                            subtitle = "৯০+ স্কোর ব্লুপ্রিন্ট",
                            icon = Icons.Default.MilitaryTech,
                            tint = Color(0xFFD97706),
                            testTag = "home_toppers_btn",
                            onClick = { onNavigate(AppScreen.TOPPERS_GUIDE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolTileCard(
                            title = "স্টাডি প্ল্যানার",
                            subtitle = "রুটিন ও টাস্ক",
                            icon = Icons.AutoMirrored.Filled.EventNote,
                            tint = Color(0xFF059669),
                            testTag = "home_planner_btn",
                            onClick = { onNavigate(AppScreen.STUDY_PLANNER) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolTileCard(
                            title = "প্রোগ্রেস গ্রাফ",
                            subtitle = "স্কোর ট্রেন্ড ড্যাশবোর্ড",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            tint = Color(0xFF7C3AED),
                            testTag = "home_progress_btn",
                            onClick = { onNavigate(AppScreen.PROGRESS_DASHBOARD) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolTileCard(
                            title = "বুকমার্ক হাব",
                            subtitle = "সংরক্ষিত প্রশ্ন ও ফর্মুলা",
                            icon = Icons.Default.Bookmark,
                            tint = Color(0xFFEA580C),
                            testTag = "home_bookmarks_btn",
                            onClick = { onNavigate(AppScreen.BOOKMARK_HUB) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolTileCard(
                            title = "মেরিট সার্টিফিকেট",
                            subtitle = "স্কলার সার্টিফিকেট",
                            icon = Icons.Default.WorkspacePremium,
                            tint = Color(0xFFD97706),
                            testTag = "home_certificate_btn",
                            onClick = { onNavigate(AppScreen.CERTIFICATE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToolTileCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ActiveStudentsTicker(count: Int, isBengali: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)) // Green pulse
            )
            Spacer(modifier = Modifier.width(8.dp))
            val formattedCount = formatCompactNumber(count)
            Text(
                text = if (isBengali) 
                    "বর্তমানে $formattedCount জন শিক্ষার্থী আপনার সাথে পড়াশোনা করছে" 
                else 
                    "Currently $formattedCount students are studying with you",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HeroExamReadinessCard(
    profile: StudentProfile,
    isBengali: Boolean,
    onQuickDaily10: () -> Unit
) {
    val currentCls = remember(profile.classId) {
        AcademicClassProvider.ALL_CLASSES.find { it.id == profile.classId }
    }
    val readinessProgress by animateFloatAsState(
        targetValue = profile.examReadinessPercentage / 100f,
        label = "readiness_anim"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_exam_readiness_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryIndigoDark, PrimaryIndigo, PrimaryIndigoLight)
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Exam date",
                                tint = ExamGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) {
                                    "${currentCls?.nameBengali ?: "মাধ্যমিক"} • বাকি ১৪২ দিন"
                                } else {
                                    "${currentCls?.nameEnglish ?: "Madhyamik"} • 142 Days Left"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExamFlame.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "🔥 ${profile.currentStreak} দিন স্ট্রিক",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Main Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "পরীক্ষা প্রস্তুতি সূচক",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${profile.examReadinessPercentage}% প্রস্তুত",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "প্লাটিনাম মেধা স্তর • রাজ্য র‍্যাঙ্ক #${profile.rankState}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Circular Progress Dial
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(76.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { readinessProgress },
                            strokeWidth = 7.dp,
                            color = ExamGold,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = ExamGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "${profile.examReadinessPercentage}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // CTA Button
                Button(
                    onClick = onQuickDaily10,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExamGold,
                        contentColor = Color(0xFF78350F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_daily_assessment_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "আজকের টেস্ট শুরু করো (+১৫০ XP)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color = Color.White,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.9f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DailyQuestsCard(
    quests: List<DailyQuest>,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_quests_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Quests",
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "আজকের দৈনিক মিশন",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "রিনিউ রাত ১২টায়",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            quests.forEach { quest ->
                val progress = quest.currentCount.toFloat() / quest.targetCount.coerceAtLeast(1)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = quest.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "+${quest.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ExamFlame
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = if (progress >= 1f) CorrectGreen else ExamGold,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${quest.currentCount}/${quest.targetCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectProgressCard(
    subject: SubjectItem,
    isBengali: Boolean,
    onOpenJourney: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completionPercentage = if (subject.totalChapters > 0) {
        ((subject.completedChapters.toFloat() / subject.totalChapters) * 100).toInt()
    } else 0
    val completionFraction = if (subject.totalChapters > 0) {
        (subject.completedChapters.toFloat() / subject.totalChapters).coerceIn(0f, 1f)
    } else 0f

    val subjectColor = Color(subject.colorHex)

    val isDark = MaterialTheme.colorScheme.background.red < 0.45f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.2.dp, subjectColor.copy(alpha = if (isDark) 0.45f else 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenJourney() }
            .testTag("subject_card_${subject.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDark) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                subjectColor.copy(alpha = 0.12f)
                            )
                        )
                    }
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubjectIconBadge(
                    subjectCode = subject.code,
                    colorHex = subject.colorHex
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.getTitle(isBengali),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    val textbookName = subject.getTextbook(isBengali)
                    if (textbookName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = textbookName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = subjectColor
                            ),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val classLevelName = subject.getClassLevelName(isBengali)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        ) {
                            Text(
                                text = classLevelName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }

                        if (subject.stream.isNotBlank() && subject.stream != "common") {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            ) {
                                Text(
                                    text = com.example.util.StreamHelper.getFormattedStreamLabel(subject.stream, isBengali),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Chapter Completion Circular Dial Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { completionFraction },
                        strokeWidth = 4.dp,
                        color = subjectColor,
                        trackColor = subjectColor.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "${completionPercentage}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        ),
                        color = subjectColor
                    )
                }
            }

            // Visual Chapter Completion Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = subjectColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isBengali) 
                                "অধ্যায় অগ্রগতি: ${completionPercentage}%" 
                            else 
                                "Progress: ${completionPercentage}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Text(
                        text = "দক্ষতা: ${subject.masteryPercentage}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { completionFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = subjectColor,
                    trackColor = subjectColor.copy(alpha = 0.15f)
                )
            }
          }
        }
    }
}

@Composable
fun ActiveClassBanner(
    profile: StudentProfile,
    isBengali: Boolean,
    onSwitchClass: (String, String) -> Unit,
    onSwitchStream: (String) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedBoardIdForFilter by remember { mutableStateOf<String?>(null) }
    var selectedClassIdFor1112 by remember { mutableStateOf<String?>(null) }
    var selectedSemesterIdFor1112 by remember { mutableStateOf<String?>(null) }
    val currentClass = AcademicClassProvider.ALL_CLASSES.find { it.id == profile.classId }
        ?: AcademicClassProvider.ALL_CLASSES.find { it.targetExam == profile.targetExam }
        ?: AcademicClassProvider.ALL_CLASSES[5] // Default Class 10

    val isHs = currentClass.classNumber in listOf(11, 12)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_class_banner")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedBoardIdForFilter = null
                            selectedClassIdFor1112 = null
                            selectedSemesterIdFor1112 = null
                            showDialog = true
                        }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Class",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "আপনার বর্তমান শ্রেণি ও বিভাগ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isHs) "${currentClass.name} • ${profile.streamBengali}" else currentClass.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = profile.targetExam,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        selectedBoardIdForFilter = null
                        selectedClassIdFor1112 = null
                        selectedSemesterIdFor1112 = null
                        showDialog = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("switch_class_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "বদলান",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // If Class 11 or 12, display fast Stream Selector Tabs directly on the card!
            if (isHs) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "বিভাগ / স্ট্রিম নির্বাচন করুন (Stream):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val streamChips = listOf(
                            Triple("science", "বিজ্ঞান (Science)", Icons.Default.Science),
                            Triple("arts", "কলা (Arts)", Icons.Default.AutoStories),
                            Triple("commerce", "বাণিজ্য (Commerce)", Icons.Default.AccountBalanceWallet),
                            Triple("all", "সকল বিষয় (All)", Icons.Default.Apps)
                        )
                        items(streamChips.size) { idx ->
                            val (sId, sLabel, sIcon) = streamChips[idx]
                            val isSelected = profile.stream == sId || (profile.stream.contains("_") && profile.stream.split("_")[0] == sId)
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    if (profile.stream.contains("_sem")) {
                                        val sem = profile.stream.split("_")[1]
                                        onSwitchStream(sId + "_" + sem)
                                    } else {
                                        onSwitchStream(sId)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = sIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = sLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = when {
                        selectedBoardIdForFilter == null -> {
                            if (isBengali) "প্রথমে বোর্ড নির্বাচন করুন" else "Select Board First"
                        }
                        selectedClassIdFor1112 == null -> {
                            if (isBengali) "শ্রেণি ও শাখা নির্বাচন করুন" else "Select Class & Stream"
                        }
                        selectedSemesterIdFor1112 == null -> {
                            if (isBengali) "সেমিস্টার নির্বাচন করুন" else "Select Semester"
                        }
                        else -> {
                            if (isBengali) "বিভাগ / স্ট্রিম নির্বাচন করুন" else "Select Stream"
                        }
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when {
                        selectedBoardIdForFilter == null -> {
                            Text(
                                text = if (isBengali)
                                    "অনুগ্রহ করে প্রথমে আপনার শিক্ষা বোর্ড নির্বাচন করুন, তারপর সংশ্লিষ্ট শ্রেণির তালিকা দেখতে পাবেন।"
                                else
                                    "Please select your education board first to see the available classes.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            AcademicClassProvider.ALL_BOARDS.forEach { board ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedBoardIdForFilter = board.id
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val iconVector = when (board.iconName) {
                                            "ChildCare" -> Icons.Default.ChildCare
                                            "MenuBook" -> Icons.Default.MenuBook
                                            "School" -> Icons.Default.School
                                            else -> Icons.Default.MenuBook
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = iconVector,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = if (isBengali) board.fullNameBengali else board.fullNameEnglish,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.8.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                                ) {
                                                    Text(
                                                        text = board.shortName,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }
                                                Text(
                                                    text = if (isBengali) board.classRangeDescription else "Classes " + board.classRangeDescription,
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        selectedClassIdFor1112 == null -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = { selectedBoardIdForFilter = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = if (isBengali) "বোর্ড: $selectedBoardIdForFilter (পরিবর্তন করুন)" else "Board: $selectedBoardIdForFilter (Change)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { selectedBoardIdForFilter = null }
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Text(
                                text = if (isBengali)
                                    "শ্রেণি পরিবর্তন করলে হোম পেজের বিষয় ও সিলেবাস সরাসরি সেই শ্রেণির অনুযায়ী প্রদর্শিত হবে।"
                                else
                                    "Changing your class updates subjects dynamically.",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            val filteredClasses = AcademicClassProvider.ALL_CLASSES.filter { 
                                it.board.equals(selectedBoardIdForFilter, ignoreCase = true) 
                            }
                            filteredClasses.forEach { academicClass ->
                                val isSelected = academicClass.id == profile.classId
                                val isClassHs = academicClass.classNumber in listOf(11, 12)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isClassHs) {
                                                selectedClassIdFor1112 = academicClass.id
                                                selectedSemesterIdFor1112 = null
                                            } else {
                                                onSwitchClass(academicClass.id, "all")
                                                showDialog = false
                                            }
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = academicClass.name,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 12.5.sp
                                                    ),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (isClassHs) "ট্যাপ করে সেমিস্টার ও শাখা বাছুন" else academicClass.targetExam,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (isSelected && !isClassHs) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            if (isClassHs) {
                                                Icon(
                                                    imageVector = Icons.Default.ChevronRight,
                                                    contentDescription = "Select",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        selectedSemesterIdFor1112 == null -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = { selectedClassIdFor1112 = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = if (isBengali) "শ্রেণি: ${if (selectedClassIdFor1112 == "class_11_wbchse") "একাদশ শ্রেণি" else "দ্বাদশ শ্রেণি"}" else "Class: ${if (selectedClassIdFor1112 == "class_11_wbchse") "Class 11" else "Class 12"}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { selectedClassIdFor1112 = null }
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Text(
                                text = if (isBengali) "প্রথমে আপনার সিলেবাসের সেমিস্টার নির্বাচন করুন:" else "Choose semester according to syllabus:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val semesters = if (selectedClassIdFor1112 == "class_11_wbchse") {
                                listOf(
                                    "sem1" to Pair("সেমিস্টার ১ (Semester 1)", "প্রথম সেমিস্টার - MCQ ভিত্তিক পরীক্ষা"),
                                    "sem2" to Pair("সেমিস্টার ২ (Semester 2)", "দ্বিতীয় সেমিস্টার - সংক্ষিপ্ত প্রশ্ন ভিত্তিক পরীক্ষা")
                                )
                            } else {
                                listOf(
                                    "sem3" to Pair("সেমিস্টার ৩ (Semester 3)", "তৃতীয় সেমিস্টার - MCQ ভিত্তিক পরীক্ষা"),
                                    "sem4" to Pair("সেমিস্টার ৪ (Semester 4)", "চতুর্থ সেমিস্টার - সংক্ষিপ্ত প্রশ্ন ভিত্তিক পরীক্ষা")
                                )
                            }

                            semesters.forEach { (semId, semNames) ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedSemesterIdFor1112 = semId
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = semNames.first,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = semNames.second,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Select",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = { selectedSemesterIdFor1112 = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                val semNumStr = selectedSemesterIdFor1112!!.replace("sem", "")
                                val semBengaliNum = when (semNumStr) {
                                    "1" -> "১"
                                    "2" -> "২"
                                    "3" -> "৩"
                                    "4" -> "৪"
                                    else -> semNumStr
                                }
                                Text(
                                    text = if (isBengali) "সেমিস্টার $semBengaliNum (পরিবর্তন করুন)" else "Semester $semNumStr (Change)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { selectedSemesterIdFor1112 = null }
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Text(
                                text = if (isBengali) "আপনার পড়ার বিভাগ / স্ট্রিম সিলেক্ট করুন:" else "Select your academic stream:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val streamsList = listOf(
                                Triple("science", "বিজ্ঞান শাখা (Science)", Icons.Default.Science),
                                Triple("arts", "কলা বিভাগ (Arts)", Icons.Default.AutoStories),
                                Triple("commerce", "বাণিজ্য শাখা (Commerce)", Icons.Default.AccountBalanceWallet)
                            )

                            streamsList.forEach { (subId, subLabel, subIcon) ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedClassIdFor1112?.let { cid ->
                                                onSwitchClass(cid, subId + "_" + selectedSemesterIdFor1112)
                                            }
                                            showDialog = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = subIcon,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (isBengali) subLabel.split(" (")[0] else subLabel,
                                            modifier = Modifier.weight(1f),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Go",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
