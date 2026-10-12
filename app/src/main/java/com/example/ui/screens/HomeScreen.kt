package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

// ইংরেজি সংখ্যাকে বাংলা সংখ্যায় রূপান্তরের হেল্পার ফাংশন
private fun String.toBengaliDigits(): String {
    val eng = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val ben = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    var result = this
    for (i in eng.indices) {
        result = result.replace(eng[i], ben[i])
    }
    return result
}

private fun Int.toBengali(isBengali: Boolean): String =
    if (isBengali) this.toString().toBengaliDigits() else this.toString()

@Composable
fun HomeScreen(
    profile: StudentProfile,
    subjects: List<SubjectItem>,
    dailyQuests: List<DailyQuest>,
    recentlyViewed: List<RecentlyViewedEntity>,
    bookmarks: List<BookmarkEntity>,
    activeStudentsCount: Int,
    isBengali: Boolean,
    isDark: Boolean = isSystemInDarkTheme(),
    onStartAssessment: (AssessmentMode, String?, String?) -> Unit,
    onSelectSubject: (String) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onSwitchClass: (String, String) -> Unit = { _, _ -> },
    onSwitchStream: (String) -> Unit = {},
    onRepairStreak: () -> Unit = {},
    onOpenSubjectDetail: ((SubjectEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val screenBg = if (isDark) Color(0xFF101524) else Color(0xFFF5F8FC)
    var showClassDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ১. হেডার: প্রোফাইল ছবি, শুভেচ্ছা, শ্রেণি এবং ডানদিকে স্ট্রিক ও XP
        item {
            StudentGreetingHeader(
                profile = profile,
                isBengali = isBengali,
                isDark = isDark,
                onOpenProfile = { onNavigate(AppScreen.PROFILE) },
                onOpenWallet = { onNavigate(AppScreen.WALLET) },
                onSwitchClassClick = { showClassDialog = true },
                onOpenBadges = { onNavigate(AppScreen.BADGES) }
            )
        }

        // ২. দৈনিক লক্ষ্য (Daily Goal Card)
        item {
            DailyGoalCard(
                completedMinutes = 12,
                targetMinutes = 20,
                isBengali = isBengali,
                isDark = isDark,
                onClick = { onNavigate(AppScreen.PROGRESS_DASHBOARD) }
            )
        }

        // ৩. পড়া চালিয়ে যাও (Continue Learning)
        item {
            val recentItem = recentlyViewed.firstOrNull()
            ContinueLearningSection(
                title = recentItem?.title ?: if (isBengali) "গণিত: জ্যামিতি পার্ট ২" else "Math: Geometry Part 2",
                isBengali = isBengali,
                isDark = isDark,
                onStartClick = {
                    val firstSubId = subjects.firstOrNull()?.id
                    if (firstSubId != null) {
                        onSelectSubject(firstSubId)
                    }
                    onNavigate(AppScreen.JOURNEY)
                }
            )
        }

        // ৪. বিষয়সমূহ (২x২ গ্রিড ও অটো স্ক্রোল)
        item {
            Subjects2x2Section(
                subjects = subjects,
                isBengali = isBengali,
                isDark = isDark,
                onSeeAllClick = { onNavigate(AppScreen.ALL_SUBJECTS) },
                onSubjectClick = { subject ->
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

        // ৫. দৈনিক কুইজ ব্যানার (Daily Quiz Card)
        item {
            DailyQuizBanner(
                isBengali = isBengali,
                isDark = isDark,
                onPlayQuiz = {
                    onStartAssessment(AssessmentMode.DAILY_10, null, null)
                }
            )
        }

        // ৬. সেরাদের তালিকা (Leaderboard Card)
        item {
            LeaderboardPreviewCard(
                rank = if (profile.rankState > 0) profile.rankState else 4,
                isBengali = isBengali,
                isDark = isDark,
                onClick = { onNavigate(AppScreen.LEADERBOARD) }
            )
        }
    }

    if (showClassDialog) {
        ClassSwitchDialog(
            profile = profile,
            isBengali = isBengali,
            onDismiss = { showClassDialog = false },
            onSwitchClass = { classId, stream ->
                onSwitchClass(classId, stream)
                showClassDialog = false
            }
        )
    }
}

// ============================================================================
// ১. হেডার সেকশন
// ============================================================================
@Composable
fun StudentGreetingHeader(
    profile: StudentProfile,
    isBengali: Boolean,
    isDark: Boolean = isSystemInDarkTheme(),
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

    val nameTrimmed = profile.name.trim()
    val nameParts = nameTrimmed.split("\\s+".toRegex())
    val lastNameOrUsername = when {
        nameParts.size > 1 -> nameParts.last()
        nameTrimmed.isNotBlank() -> nameTrimmed
        else -> if (isBengali) "অনিক" else "Anik"
    }

    val displayName = lastNameOrUsername
    val classText = currentCls?.name ?: if (isBengali) "৮ম" else "8th"

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // বামপাশে গোল প্রোফাইল অ্যাভাটার
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF93C5FD), Color(0xFF3B82F6))
                    )
                )
                .border(2.dp, if (isDark) Color(0xFF334155) else Color.White, CircleShape)
                .clickable { onOpenProfile() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // মাঝখানে শুভেচ্ছা ও শ্রেণি
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$greeting, $displayName!",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                ),
                color = if (isDark) Color.White else Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onOpenProfile() }
            )
            Text(
                text = if (isBengali) "শ্রেণি: ${classText.replace("শ্রেণি", "").trim()}" else "Class: $classText",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp
                ),
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                modifier = Modifier.clickable { onSwitchClassClick() }
            )
        }

        // ডানপাশে ওপরে-নিচে স্ট্রিক এবং কয়েন/XP
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.clickable { onOpenBadges() }
            ) {
                Text(text = "🔥", fontSize = 15.sp)
                val streakVal = if (profile.currentStreak > 0) profile.currentStreak else 5
                Text(
                    text = if (isBengali) "${streakVal.toBengali(true)} দিন" else "$streakVal Days",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.clickable { onOpenWallet() }
            ) {
                Text(text = "🪙", fontSize = 15.sp)
                val xpVal = if (profile.coins > 0) profile.coins else 150
                Text(
                    text = "${xpVal.toBengali(isBengali)} XP",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
            }
        }
    }
}

// ============================================================================
// ২. দৈনিক লক্ষ্য কার্ড (Daily Goal Card)
// ============================================================================
@Composable
fun DailyGoalCard(
    completedMinutes: Int,
    targetMinutes: Int,
    isBengali: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = (completedMinutes.toFloat() / targetMinutes.coerceAtLeast(1)).coerceIn(0f, 1f),
        label = "daily_goal_progress"
    )

    val cardBg = if (isDark) Color(0xFF1E2538) else Color(0xFFDCEBFA)
    val ringColor = if (isDark) Color(0xFF6C5CE7) else Color(0xFF5A9BD5)
    val trackColor = if (isDark) Color(0xFF323B54) else Color.White.copy(alpha = 0.75f)
    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("hero_exam_readiness_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // বামদিকে গোল প্রোগ্রেস বার
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(74.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    strokeWidth = 7.dp,
                    color = ringColor,
                    trackColor = trackColor,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${completedMinutes.toBengali(isBengali)}/${targetMinutes.toBengali(isBengali)}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        ),
                        color = titleColor
                    )
                    Text(
                        text = if (isBengali) "মিনিট" else "Min",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = subColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // ডানদিকে টেক্সট
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isBengali) "দৈনিক লক্ষ্য" else "Daily Goal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    color = titleColor
                )
                Text(
                    text = if (isBengali) "${completedMinutes.toBengali(true)} মিনিট সম্পন্ন" else "$completedMinutes mins completed",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = subColor
                )
            }
        }
    }
}

// ============================================================================
// ৩. পড়া চালিয়ে যাও (Continue Learning Section)
// ============================================================================
@Composable
fun ContinueLearningSection(
    title: String,
    isBengali: Boolean,
    isDark: Boolean,
    onStartClick: () -> Unit
) {
    val sectionTitleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val cardBg = if (isDark) Color(0xFF1E2538) else Color.White
    val btnBg = if (isDark) Color(0xFF6C47FF) else Color(0xFF4F86C6)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (isBengali) "পড়া চালিয়ে যাও" else "Continue Learning",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            ),
            color = sectionTitleColor
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // বামদিক: বিষয়ের নাম ও প্লে আইকন
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = sectionTitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF2D364F) else Color(0xFFF1F5F9))
                            .clickable { onStartClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = if (isDark) Color.White else Color(0xFF334155),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // ডানদিক: জ্যামিতিক ইলাস্ট্রেশন ও "শুরু করো" বাটন
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GeometryShapesGraphic()

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = btnBg,
                        modifier = Modifier.clickable { onStartClick() }
                    ) {
                        Text(
                            text = if (isBengali) "শুরু করো" else "Start",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// জ্যামিতিক আকারের ছোট গ্রাফিক (ঘনক, ত্রিভুজ ও শঙ্কু)
@Composable
private fun GeometryShapesGraphic() {
    Canvas(modifier = Modifier.size(width = 68.dp, height = 38.dp)) {
        // নীল ঘনক (Cube)
        drawRoundRect(
            color = Color(0xFF60A5FA),
            topLeft = androidx.compose.ui.geometry.Offset(6f, 12f),
            size = androidx.compose.ui.geometry.Size(34f, 34f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        // লাল পিরামিড (Triangle)
        val trianglePath = Path().apply {
            moveTo(size.width * 0.52f, 4f)
            lineTo(size.width * 0.28f, size.height)
            lineTo(size.width * 0.76f, size.height)
            close()
        }
        drawPath(trianglePath, color = Color(0xFFF87171))

        // সবুজ শঙ্কু (Cone)
        val conePath = Path().apply {
            moveTo(size.width * 0.80f, 8f)
            lineTo(size.width * 0.62f, size.height)
            lineTo(size.width * 0.98f, size.height)
            close()
        }
        drawPath(conePath, color = Color(0xFF34D399))
    }
}

// ============================================================================
// ৪. বিষয়সমূহ (২x২ গ্রিড সেকশন)
// ============================================================================
private data class SubjectCardVisual(
    val id: String,
    val title: String,
    val progress: Float,
    val bgLight: Color,
    val bgDark: Color,
    val barColorLight: Color,
    val barColorDark: Color,
    val textColorLight: Color,
    val icon: ImageVector,
    val iconTint: Color,
    val originSubject: SubjectItem? = null
)

@Composable
fun Subjects2x2Section(
    subjects: List<SubjectItem>,
    isBengali: Boolean,
    isDark: Boolean,
    onSeeAllClick: () -> Unit,
    onSubjectClick: (SubjectItem) -> Unit
) {
    val sectionTitleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val linkColor = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB)

    // ছবির ৪টি প্রধান কার্ডের কালার থিম (গণিত, বিজ্ঞান, বাংলা, ইংরেজি)
    val defaultTitles = if (isBengali) {
        listOf("গণিত", "বিজ্ঞান", "বাংলা", "ইংরেজি")
    } else {
        listOf("Math", "Science", "Bengali", "English")
    }

    val cardsData = (0..3).map { index ->
        val sub = subjects.getOrNull(index)
        val progress = if (sub != null && sub.totalChapters > 0) {
            (sub.completedChapters.toFloat() / sub.totalChapters).coerceIn(0.15f, 1f)
        } else {
            when (index) {
                0 -> 0.58f
                1 -> 0.45f
                2 -> 0.35f
                else -> 0.52f
            }
        }

        when (index) {
            0 -> SubjectCardVisual(
                id = sub?.id ?: "math",
                title = sub?.getTitle(isBengali) ?: defaultTitles[0],
                progress = progress,
                bgLight = Color(0xFFFAD2C8),       // হালকা পিচ/কমলা
                bgDark = Color(0xFF2B224C),        // ডার্ক পার্পল
                barColorLight = Color(0xFF4A90E2),
                barColorDark = Color(0xFF10B981),
                textColorLight = Color(0xFF1E293B),
                icon = Icons.Default.Architecture,
                iconTint = Color(0xFFD97706),
                originSubject = sub
            )
            1 -> SubjectCardVisual(
                id = sub?.id ?: "science",
                title = sub?.getTitle(isBengali) ?: defaultTitles[1],
                progress = progress,
                bgLight = Color(0xFFC7E3FA),       // হালকা আকাশী নীল
                bgDark = Color(0xFF1E2538),        // ডার্ক স্লেট
                barColorLight = Color(0xFF4A80C6),
                barColorDark = Color(0xFF10B981),
                textColorLight = Color(0xFF1E293B),
                icon = Icons.Default.Science,
                iconTint = Color(0xFF3B82F6),
                originSubject = sub
            )
            2 -> SubjectCardVisual(
                id = sub?.id ?: "bengali",
                title = sub?.getTitle(isBengali) ?: defaultTitles[2],
                progress = progress,
                bgLight = Color(0xFFFDE492),       // হালকা হলুদ
                bgDark = Color(0xFF1E2538),        // ডার্ক স্লেট
                barColorLight = Color(0xFF10B981),
                barColorDark = Color(0xFF10B981),
                textColorLight = Color(0xFF1E293B),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconTint = Color(0xFF92400E),
                originSubject = sub
            )
            else -> SubjectCardVisual(
                id = sub?.id ?: "english",
                title = sub?.getTitle(isBengali) ?: defaultTitles[3],
                progress = progress,
                bgLight = Color(0xFF12D17E),       // উজ্জ্বল সবুজ
                bgDark = Color(0xFF1E2538),        // ডার্ক স্লেট
                barColorLight = Color(0xFFA3E635),
                barColorDark = Color(0xFF84CC16),
                textColorLight = Color.White,
                icon = Icons.Default.ChatBubble,
                iconTint = if (isDark) Color(0xFFFACC15) else Color.White,
                originSubject = sub
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header with title and See All link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBengali) "বিষয়সমূহ" else "Subjects",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                ),
                color = sectionTitleColor
            )

            TextButton(
                onClick = onSeeAllClick,
                modifier = Modifier.testTag("see_all_subjects_link")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBengali) "সব বিষয় দেখুন" else "See All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = linkColor
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = linkColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // প্রথম সারি (গণিত ও বিজ্ঞান)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SubjectGridTile(
                visual = cardsData[0],
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = { cardsData[0].originSubject?.let(onSubjectClick) }
            )
            SubjectGridTile(
                visual = cardsData[1],
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = { cardsData[1].originSubject?.let(onSubjectClick) }
            )
        }

        // দ্বিতীয় সারি (বাংলা ও ইংরেজি)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SubjectGridTile(
                visual = cardsData[2],
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = { cardsData[2].originSubject?.let(onSubjectClick) }
            )
            SubjectGridTile(
                visual = cardsData[3],
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = { cardsData[3].originSubject?.let(onSubjectClick) }
            )
        }

        // Auto-scrolling horizontal row showing all subjects
        if (subjects.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isBengali) "সকল বিষয় তালিকা (অটো স্ক্রোল)" else "All Subjects (Auto Scroll)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
            AutoScrollingSubjectsRow(
                subjects = subjects,
                isBengali = isBengali,
                isDark = isDark,
                onSubjectClick = onSubjectClick
            )
        }
    }
}

@Composable
private fun AutoScrollingSubjectsRow(
    subjects: List<SubjectItem>,
    isBengali: Boolean,
    isDark: Boolean,
    onSubjectClick: (SubjectItem) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3500L)
            val totalItems = subjects.size
            if (totalItems > 1) {
                val nextIndex = (listState.firstVisibleItemIndex + 1) % totalItems
                try {
                    listState.animateScrollToItem(nextIndex)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(subjects) { subject ->
            AutoScrollSubjectCard(
                subject = subject,
                isBengali = isBengali,
                isDark = isDark,
                onClick = { onSubjectClick(subject) }
            )
        }
    }
}

private fun getSubjectGradient(subjectIdOrCode: String?, isDark: Boolean): Brush {
    val key = (subjectIdOrCode ?: "").lowercase()
    val colors = when {
        key.contains("math") -> if (isDark) listOf(Color(0xFF2C223B), Color(0xFF3B2A56)) else listOf(Color(0xFFFFECD2), Color(0xFFFCB69F))
        key.contains("phy") -> if (isDark) listOf(Color(0xFF231942), Color(0xFF3F2B96)) else listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
        key.contains("life") || key.contains("bio") -> if (isDark) listOf(Color(0xFF133B31), Color(0xFF1F5E4B)) else listOf(Color(0xFF84FAB0), Color(0xFF8FD3F4))
        key.contains("hist") -> if (isDark) listOf(Color(0xFF4A2E11), Color(0xFF6B3A2A)) else listOf(Color(0xFFFEE140), Color(0xFFFA709A))
        key.contains("geo") -> if (isDark) listOf(Color(0xFF0F3854), Color(0xFF1A5276)) else listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
        key.contains("ben") -> if (isDark) listOf(Color(0xFF4A1525), Color(0xFF6B2D3C)) else listOf(Color(0xFFFF9a9e), Color(0xFFfecfef))
        key.contains("eng") -> if (isDark) listOf(Color(0xFF1A365D), Color(0xFF2B4C7E)) else listOf(Color(0xFFa1c4fd), Color(0xFFc2e9fb))
        key.contains("env") -> if (isDark) listOf(Color(0xFF1B3B2B), Color(0xFF2D6A4F)) else listOf(Color(0xFFd4fc79), Color(0xFF96e6a1))
        else -> if (isDark) listOf(Color(0xFF1E2538), Color(0xFF2A354D)) else listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
    }
    return Brush.linearGradient(colors)
}

@Composable
private fun AutoScrollSubjectCard(
    subject: SubjectItem,
    isBengali: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val gradientBrush = getSubjectGradient(subject.id, isDark)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 1.dp),
        modifier = Modifier
            .width(150.dp)
            .height(86.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("auto_scroll_subject_${subject.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject.getTitle(isBengali),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBengali) "অধ্যায়" else "Chapters",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = subColor
                        )
                        Text(
                            text = "${subject.completedChapters.toBengali(isBengali)}/${subject.totalChapters.toBengali(isBengali)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = textColor
                        )
                    }
                    val progress = if (subject.totalChapters > 0) {
                        (subject.completedChapters.toFloat() / subject.totalChapters).coerceIn(0f, 1f)
                    } else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                        trackColor = textColor.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectGridTile(
    visual: SubjectCardVisual,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradientBrush = getSubjectGradient(visual.id, isDark)
    val titleColor = if (isDark) Color.White else visual.textColorLight
    val barColor = if (isDark) visual.barColorDark else visual.barColorLight
    val trackColor = titleColor.copy(alpha = 0.2f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 1.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("subject_card_${visual.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = visual.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp
                        ),
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = visual.icon,
                        contentDescription = visual.title,
                        tint = visual.iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                LinearProgressIndicator(
                    progress = { visual.progress },
                    color = barColor,
                    trackColor = trackColor,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

// ============================================================================
// ৫. দৈনিক কুইজ ব্যানার (Daily Quiz Banner)
// ============================================================================
@Composable
fun DailyQuizBanner(
    isBengali: Boolean,
    isDark: Boolean,
    onPlayQuiz: () -> Unit
) {
    val gradientColors = if (isDark) {
        listOf(Color(0xFF5B2EFF), Color(0xFF7C3AED))
    } else {
        listOf(Color(0xFFD6ECFA), Color(0xFFE6F3FD))
    }

    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subColor = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155)
    val btnContainer = if (isDark) Color.White else Color(0xFF4F86C6)
    val btnContent = if (isDark) Color(0xFF1E293B) else Color.White

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onPlayQuiz() }
            .testTag("start_daily_assessment_btn")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // বামদিক: শিরোনাম ও সাবটাইটেল
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isBengali) "দৈনিক কুইজ" else "Daily Quiz",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            ),
                            color = titleColor
                        )
                    }

                    Text(
                        text = if (isBengali) "আজকের কুইজ খেলো ও\n৫০ XP জিতো!" else "Play today's quiz &\nwin 50 XP!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = subColor
                    )
                }

                // ডানদিক: বড় স্টার আইকন ও "খেলো" বাটন
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = btnContainer,
                        modifier = Modifier.clickable { onPlayQuiz() }
                    ) {
                        Text(
                            text = if (isBengali) "খেলো" else "Play",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            ),
                            color = btnContent,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// ৬. সেরাদের তালিকা (Leaderboard Card)
// ============================================================================
@Composable
fun LeaderboardPreviewCard(
    rank: Int,
    isBengali: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1E2538) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("home_leaderboard_spotlight_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isBengali) "সেরাদের তালিকা" else "Leaderboard",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = textColor
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // বামপাশে ৩ জন টপারের অ্যাভাটার ও ব্যাজ
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TopperAvatarBadge(
                        avatarSize = 36.dp,
                        bgColor = Color(0xFFCBD5E1),
                        badgeColor = Color(0xFF3B82F6)
                    )
                    TopperAvatarBadge(
                        avatarSize = 44.dp,
                        bgColor = Color(0xFFFDE68A),
                        badgeColor = Color(0xFFF59E0B)
                    )
                    TopperAvatarBadge(
                        avatarSize = 36.dp,
                        bgColor = Color(0xFFFECDD3),
                        badgeColor = Color(0xFFD97706)
                    )
                }

                // ডানপাশে "তুমি ৪র্থ অবস্থানে ↗"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val rankStr = rank.toBengali(isBengali)
                    Text(
                        text = if (isBengali) "তুমি ${rankStr}র্থ অবস্থানে" else "You are #$rankStr",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp
                        ),
                        color = textColor
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopperAvatarBadge(
    avatarSize: androidx.compose.ui.unit.Dp,
    bgColor: Color,
    badgeColor: Color
) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(bgColor)
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF334155),
                modifier = Modifier.size(avatarSize * 0.65f)
            )
        }
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(badgeColor)
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(9.dp)
            )
        }
    }
}

// ============================================================================
// শ্রেণি পরিবর্তনের ডায়ালগ (হেডারের "শ্রেণি: ৮ম"-এ ট্যাপ করলে খুলবে)
// ============================================================================
@Composable
private fun ClassSwitchDialog(
    profile: StudentProfile,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSwitchClass: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBengali) "তোমার শ্রেণি নির্বাচন করো" else "Select Your Class",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AcademicClassProvider.ALL_CLASSES.forEach { academicClass ->
                    val isSelected = academicClass.id == profile.classId
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSwitchClass(academicClass.id, "all") }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = academicClass.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// প্রজেক্টের অন্য কোনো ফাইলে রেফারেন্স থাকলে যেন এরর না আসে তার জন্য রাখা কম্পোজেবল
@Composable
fun RecentlyViewedCard(item: RecentlyViewedEntity, isBengali: Boolean) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
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