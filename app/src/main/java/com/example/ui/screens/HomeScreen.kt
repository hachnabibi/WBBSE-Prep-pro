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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.StreakTrackerCard
import com.example.ui.components.SubjectIconBadge
import com.example.ui.theme.*
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
    onSwitchClass: (String) -> Unit = {},
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
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 0. Real-time Active Students Ticker
        item {
            ActiveStudentsTicker(count = activeStudentsCount, isBengali = isBengali)
        }

        // Newly added Recently Viewed section
        if (recentlyViewed.isNotEmpty()) {
            item {
                Text(
                    text = "সম্প্রতি দেখা",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(recentlyViewed) { item ->
                        RecentlyViewedCard(item, isBengali)
                    }
                }
            }
        }



        // 1. Hero Card: Exam Readiness & Countdown
        item {
            HeroExamReadinessCard(
                profile = profile,
                isBengali = isBengali,
                onQuickDaily10 = {
                    onStartAssessment(AssessmentMode.DAILY_10, null, null)
                }
            )
        }

        // 2. Daily Learning Streak Tracker
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

        // Sponsored Educational Banner
        item {
            AdBannerCard(isBengali = isBengali)
        }

        // Horizontal Subject Cards Slider (Placed above Quick Practice Hub)
        item {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "বিষয়ভিত্তিক দ্রুত অগ্রগতি",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyRow(
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

        // 3. Quick Assessment Modes Vertical List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "কুইক প্র্যাকটিস হাব",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                QuickActionCard(
                    title = "ডেইলি ১০",
                    subtitle = "১০ প্রশ্ন • ১৫০ XP",
                    icon = Icons.Default.Bolt,
                    containerColor = PrimaryIndigo,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "quick_daily_10_btn",
                    onClick = { onStartAssessment(AssessmentMode.DAILY_10, null, null) }
                )

                QuickActionCard(
                    title = "ভুল সংশোধন",
                    subtitle = "দুর্বল প্রশ্নসমূহ",
                    icon = Icons.Default.Replay,
                    containerColor = WrongRed,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "quick_wrong_replay_btn",
                    onClick = { onStartAssessment(AssessmentMode.WEAK_AREA, null, null) }
                )

                QuickActionCard(
                    title = "স্পিড ব্লিৎজ",
                    subtitle = "১৫ সেকেন্ড/প্রশ্ন",
                    icon = Icons.Default.Timer,
                    containerColor = ExamFlame,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "quick_speed_blitz_btn",
                    onClick = { onStartAssessment(AssessmentMode.SPEED_BLITZ, null, null) }
                )

                val firstActiveSub = subjects.firstOrNull()
                QuickActionCard(
                    title = "বস চ্যালেঞ্জ",
                    subtitle = "অধ্যায় মাস্টারি",
                    icon = Icons.Default.Shield,
                    containerColor = ExamGold,
                    contentColor = Color(0xFF78350F),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "quick_boss_raid_btn",
                    onClick = { onStartAssessment(AssessmentMode.BOSS_BATTLE, firstActiveSub?.id?.let { "${it}_ch1" } ?: "phy_ch1", null) }
                )
            }
        }

        // Official Google AdMob In-Feed Test Ad
        item {
            com.example.ui.components.AdMobNativeCard(
                isBengali = isBengali,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Class-specific Special Exam Tools Vertical List Section
        item {
            val currentCls = remember(profile.classId) {
                AcademicClassProvider.ALL_CLASSES.find { it.id == profile.classId }
            }
            val clsLabel = currentCls?.name ?: ("পাঠ্যক্রম")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "$clsLabel বিশেষ পরীক্ষার টুলস ও প্রস্তুতি",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                QuickActionCard(
                    title = "স্মার্ট ফ্ল্যাশকার্ড",
                    subtitle = "সূত্র ও রিভিশন হাব",
                    icon = Icons.Default.Style,
                    containerColor = PrimaryIndigo,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_flashcards_btn",
                    onClick = { onNavigate(AppScreen.FLASHCARDS) }
                )

                QuickActionCard(
                    title = "PYQ এক্সপ্লোরার",
                    subtitle = "২০১৫-২০২৫ আসল প্রশ্ন",
                    icon = Icons.Default.HistoryEdu,
                    containerColor = Color(0xFF0D9488),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_pyq_btn",
                    onClick = { onNavigate(AppScreen.PYQ_EXPLORER) }
                )

                QuickActionCard(
                    title = "OMR শিট সিমুলেটর",
                    subtitle = "অপটিক্যাল স্ক্যান প্র্যাকটিস",
                    icon = Icons.Default.DocumentScanner,
                    containerColor = Color(0xFF7C3AED),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_omr_btn",
                    onClick = { onNavigate(AppScreen.OMR_PRACTICE) }
                )

                QuickActionCard(
                    title = "টপার্স খাতা গাইড",
                    subtitle = "৯০+ স্কোর ব্লুপ্রিন্ট",
                    icon = Icons.Default.MilitaryTech,
                    containerColor = ExamGold,
                    contentColor = Color(0xFF78350F),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_toppers_btn",
                    onClick = { onNavigate(AppScreen.TOPPERS_GUIDE) }
                )

                QuickActionCard(
                    title = "স্ন্যাপ ডাউট সলভার",
                    subtitle = "এআই ছবি স্ক্যান সলিউশন",
                    icon = Icons.Default.CameraAlt,
                    containerColor = Color(0xFF0284C7),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_doubt_btn",
                    onClick = { onNavigate(AppScreen.DOUBT_SOLVER) }
                )

                QuickActionCard(
                    title = "১v১ কুইজ ব্যাটেল",
                    subtitle = "লাইভ লড়াই ও এক্সপি",
                    icon = Icons.Default.SportsEsports,
                    containerColor = Color(0xFFDC2626),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_battle_btn",
                    onClick = { onNavigate(AppScreen.MULTIPLAYER_BATTLE) }
                )

                QuickActionCard(
                    title = "মেরিট সার্টিফিকেট",
                    subtitle = "স্কলার সার্টিফিকেট",
                    icon = Icons.Default.WorkspacePremium,
                    containerColor = Color(0xFFD97706),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_certificate_btn",
                    onClick = { onNavigate(AppScreen.CERTIFICATE) }
                )

                QuickActionCard(
                    title = "অডিও পডকাস্ট",
                    subtitle = "স্পিচ রিভিশন নোটস",
                    icon = Icons.Default.Headphones,
                    containerColor = Color(0xFF6366F1),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_podcasts_btn",
                    onClick = { onNavigate(AppScreen.AUDIO_PODCASTS) }
                )

                QuickActionCard(
                    title = "স্টাডি প্ল্যানার",
                    subtitle = "রুটিন ও টাস্ক",
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    containerColor = Color(0xFF059669),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_planner_btn",
                    onClick = { onNavigate(AppScreen.STUDY_PLANNER) }
                )

                QuickActionCard(
                    title = "বুকমার্ক হাব",
                    subtitle = "সংরক্ষিত ফর্মুলা",
                    icon = Icons.Default.Bookmark,
                    containerColor = Color(0xFFD97706),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_bookmarks_btn",
                    onClick = { onNavigate(AppScreen.BOOKMARK_HUB) }
                )

                QuickActionCard(
                    title = "পিডিএফ নোটস",
                    subtitle = "অধ্যায়ভিত্তিক মেটেরিয়াল",
                    icon = Icons.Default.PictureAsPdf,
                    containerColor = Color(0xFFF43F5E),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_pdf_notes_btn",
                    onClick = { onNavigate(AppScreen.PDF_NOTES) }
                )

                QuickActionCard(
                    title = "সমাধানসহ প্রশ্নপত্র PDF",
                    subtitle = "২০১৫ – ২০২৬ জেনারেটেড পেপার",
                    icon = Icons.Default.FileDownload,
                    containerColor = Color(0xFF0284C7),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_solved_pyq_pdf_btn",
                    onClick = { onNavigate(AppScreen.PYQ_PDF_DOWNLOAD) }
                )

                QuickActionCard(
                    title = "আসল প্রশ্নপত্র PDF",
                    subtitle = if (isBengali) {
                        if (profile.classId.contains("wbchse")) "WBCHSE অফিশিয়াল প্রশ্নপত্র"
                        else "WBBSE অফিশিয়াল প্রশ্নপত্র"
                    } else "Official Board Papers",
                    icon = Icons.Default.PictureAsPdf,
                    containerColor = Color(0xFFD97706),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_original_pyq_pdf_btn",
                    onClick = { onNavigate(AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD) }
                )

                QuickActionCard(
                    title = "পাঠ্যপুস্তক PDF",
                    subtitle = "WBBSE ও WBCHSE বোর্ড বই",
                    icon = Icons.Default.AutoStories,
                    containerColor = Color(0xFF0D9488),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_textbooks_pdf_btn",
                    onClick = { onNavigate(AppScreen.TEXTBOOKS) }
                )

                QuickActionCard(
                    title = "লাইভ কুইজ",
                    subtitle = "১০০০+ প্রতিযোগী",
                    icon = Icons.Default.Groups,
                    containerColor = Color(0xFF8B5CF6),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_live_quiz_btn",
                    onClick = { onNavigate(AppScreen.LIVE_QUIZ) }
                )

                QuickActionCard(
                    title = "লাইভ মক টেস্ট",
                    subtitle = "১৫ মিনিট • রিয়েল এক্সাম",
                    icon = Icons.Default.Timer,
                    containerColor = Color(0xFFE11D48),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_live_mock_test_btn",
                    onClick = { onNavigate(AppScreen.LIVE_MOCK_TEST) }
                )

                QuickActionCard(
                    title = "স্কুল স্কোয়াড",
                    subtitle = "লিডারবোর্ড নেটওয়ার্ক",
                    icon = Icons.Default.Groups,
                    containerColor = Color(0xFF0D9488),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_squads_btn",
                    onClick = { onNavigate(AppScreen.STUDY_SQUADS) }
                )

                QuickActionCard(
                    title = "প্রোগ্রেস গ্রাফ",
                    subtitle = "স্কোর ট্রেন্ড ড্যাশবোর্ড",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    containerColor = Color(0xFF7C3AED),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_progress_btn",
                    onClick = { onNavigate(AppScreen.PROGRESS_DASHBOARD) }
                )
            }
        }

        // Leaderboard Spotlight Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
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
                            text = "১ম: অনন্যা (৮৯/৯০ মক • ১৮ বস জয়) • আপনি ৩য় স্থানে",
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

        // 3. Daily Quests Section
        item {
            DailyQuestsCard(
                quests = dailyQuests,
                isBengali = isBengali
            )
        }

        // Gamified Boss Battle Challenge
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚔️ গ্যামিফায়েড বস চ্যালেঞ্জ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = { onNavigate(AppScreen.BATTLE) },
                        modifier = Modifier.testTag("view_all_boss_battles_btn")
                    ) {
                        Text(
                            text = "অ্যারেনা দেখুন",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                val firstActiveSub = subjects.firstOrNull()
                val bossSubName = firstActiveSub?.nameBengali ?: if (isBengali) "অধ্যায়" else "Chapter"
                val bossTargetChapterId = firstActiveSub?.id?.let { "${it}_ch1" } ?: "ch1"

                BossBattleCard(
                    bossName = "$bossSubName দানব বস (Tier 3)",
                    chapterTitle = "$bossSubName • প্রাথমিক মূল্যায়ন ও রিভিশন",
                    bossMaxHp = 1000,
                    bossCurrentHp = 1000,
                    playerMaxHp = 100,
                    playerCurrentHp = 100,
                    questionsCount = 8,
                    questionsCompleted = 0,
                    isBengali = isBengali,
                    isDefeated = false,
                    onStartBattle = {
                        onStartAssessment(
                            AssessmentMode.BOSS_BATTLE,
                            bossTargetChapterId,
                            "🔥 অধ্যায় বস ব্যাটল: $bossSubName"
                        )
                    }
                )
            }
        }

        // 4. Subjects & Journey Progression
        // Search Bar for Subjects
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subjects_search_input"),
                placeholder = {
                    Text(
                        text = "বিষয় খুঁজুন (যেমন: গণিত, Physics)...",
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
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }


        if (filteredSubjects.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "'$searchQuery' নামে কোনো বিষয় পাওয়া যায়নি",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "অন্য শ্রেণি নির্বাচন করুন অথবা ফিল্টার ক্লিয়ার করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = { searchQuery = "" },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("সার্চ ক্লিয়ার করুন")
                        }
                    }
                }
            }
        } else {
            // Filtered Subject cards list (Vertical list)
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
                    }
                )
            }
        }

        // AI Coach Callout (Weak area diagnostic card)
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

        // High-Priority Weak Area Focus Card (Weak Card)
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
            Text(
                text = if (isBengali) 
                    "বর্তমানে $count জন শিক্ষার্থী আপনার সাথে পড়াশোনা করছে" 
                else 
                    "Currently $count students are studying with you",
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

    val isDark = isSystemInDarkTheme()

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
    onSwitchClass: (String) -> Unit,
    onSwitchStream: (String) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
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
                        .clickable { showDialog = true }
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
                            text = "${currentClass.board} • ${currentClass.targetExam}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showDialog = true },
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
                            val isSelected = profile.stream == sId
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSwitchStream(sId) },
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
                    text = "শ্রেণি ও শাখা নির্বাচন করুন",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isBengali)
                            "শ্রেণি পরিবর্তন করলে হোম পেজের বিষয় ও সিলেবাস সরাসরি সেই শ্রেণির অনুযায়ী প্রদর্শিত হবে। একাদশ/দ্বাদশে বিজ্ঞান ও কলা আলাদা করা হয়েছে।"
                        else
                            "Changing your class dynamically updates subjects and curriculum. Class 11/12 includes Science, Arts, and Commerce streams.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AcademicClassProvider.ALL_CLASSES.forEach { academicClass ->
                        val isSelected = academicClass.id == profile.classId
                        val isClassHs = academicClass.classNumber in listOf(11, 12)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isClassHs) {
                                        onSwitchClass(academicClass.id)
                                        showDialog = false
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = academicClass.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isClassHs) "${academicClass.board} • সাব-ক্লাস / শাখা বেছে নিন:" else "${academicClass.board} • ${academicClass.targetExam}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected && !isClassHs) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (isClassHs) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val subClasses = listOf(
                                            Triple("science", "বিজ্ঞান", Icons.Default.Science),
                                            Triple("arts", "কলা", Icons.Default.AutoStories),
                                            Triple("commerce", "বাণিজ্য", Icons.Default.AccountBalanceWallet)
                                        )
                                        subClasses.forEach { (subId, subLabel, subIcon) ->
                                            val isSubSelected = isSelected && profile.stream == subId
                                            AssistChip(
                                                onClick = {
                                                    onSwitchClass(academicClass.id)
                                                    onSwitchStream(subId)
                                                    showDialog = false
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = subIcon,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = subLabel,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = if (isSubSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                    labelColor = if (isSubSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                    leadingIconContentColor = if (isSubSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                                ),
                                                border = AssistChipDefaults.assistChipBorder(
                                                    enabled = true,
                                                    borderColor = if (isSubSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
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
