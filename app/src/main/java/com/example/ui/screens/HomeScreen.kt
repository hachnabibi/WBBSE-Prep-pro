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
    hasWeakTopics: Boolean = false,
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
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
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

        // ২. পড়া চালিয়ে যাও (Continue Learning)
        item {
            val recentItem = recentlyViewed.firstOrNull()
            ContinueLearningSection(
                title = recentItem?.title ?: if (isBengali) "আজকের পড়া শুরু করুন" else "Start today's study",
                actionLabel = when (recentItem?.type?.uppercase()) {
                    "PYQ" -> if (isBengali) "PYQ খুলুন" else "Open PYQ"
                    "TEXTBOOK" -> if (isBengali) "বই খুলুন" else "Open book"
                    else -> if (isBengali) "শুরু করুন" else "Start"
                },
                isBengali = isBengali,
                isDark = isDark,
                onStartClick = {
                    when (recentItem?.type?.uppercase()) {
                        "PYQ" -> onNavigate(AppScreen.PYQ_EXPLORER)
                        "TEXTBOOK" -> onNavigate(AppScreen.TEXTBOOKS)
                        else -> onNavigate(AppScreen.JOURNEY)
                    }
                }
            )
        }

        if (hasWeakTopics) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E2538) else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(AppScreen.WEAKNESS_INSIGHT) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (isBengali) "দুর্বল টপিক রিভিশন" else "Review weak topics",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // ৪. দৈনিক লক্ষ্য (Daily Goal Card)
        item {
            DailyGoalCard(
                completedCount = dailyQuests.sumOf { quest ->
                    quest.currentCount.coerceIn(0, quest.targetCount.coerceAtLeast(0))
                },
                targetCount = dailyQuests.sumOf { it.targetCount.coerceAtLeast(0) },
                isBengali = isBengali,
                isDark = isDark,
                onClick = { onNavigate(AppScreen.PROGRESS_DASHBOARD) }
            )
        }

        if (bookmarks.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E2538) else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(AppScreen.BOOKMARK_HUB) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (isBengali) "সংরক্ষিত বিষয়" else "Saved items",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("${bookmarks.size}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ৪. বিষয়সমূহ
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

        // ৬. মক টেস্ট ও প্র্যাকটিস সেট সেকশন
        item {
            MockTestsHomeSection(
                subjects = subjects,
                isBengali = isBengali,
                isDark = isDark,
                onStartAssessment = onStartAssessment,
                onSelectSubject = onSelectSubject,
                onNavigate = onNavigate
            )
        }

        // ৭. সেরাদের তালিকা (Leaderboard Card)
        item {
            LeaderboardPreviewCard(
                rank = profile.rankState.takeIf { it > 0 },
                activeStudentsCount = activeStudentsCount,
                isBengali = isBengali,
                isDark = isDark,
                onClick = { onNavigate(AppScreen.LEADERBOARD) }
            )
        }

        // ১৪. অ-সরকারি পর্ষদ ডিসক্লেইমার ও তথ্যের উৎস (Google Play Policy Compliance)
        item {
            GovernmentDisclaimerCard(isBengali = isBengali)
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

    val currentCls = remember(profile.classId, profile.targetExam) {
        AcademicClassProvider.ALL_CLASSES.find { it.id == profile.classId }
            ?: AcademicClassProvider.ALL_CLASSES.find { it.targetExam == profile.targetExam }
    }

    val nameTrimmed = profile.name.trim()
    val nameParts = nameTrimmed.split("\\s+".toRegex())
    val lastNameOrUsername = when {
        nameParts.size > 1 -> nameParts.last()
        nameTrimmed.isNotBlank() -> nameTrimmed
        else -> if (isBengali) "শিক্ষার্থী" else "Student"
    }

    val displayName = lastNameOrUsername
    val classText = currentCls?.name ?: if (isBengali) "শ্রেণি নির্বাচন করুন" else "Select class"

    val streakVal = profile.currentStreak.coerceAtLeast(0)
    val coinVal = profile.coins.coerceAtLeast(0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF60A5FA),
                            Color(0xFF3B82F6),
                            Color(0xFF2563EB)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable { onOpenProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "$greeting, $displayName!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onOpenProfile() }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.clickable { onSwitchClassClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isBengali) "শ্রেণি: ${classText.replace("শ্রেণি", "").trim()}" else "Class: $classText",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.5.sp
                            ),
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier.clickable { onOpenBadges() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🔥", fontSize = 12.sp)
                            Text(
                                text = if (isBengali) "${streakVal.toBengali(true)} দিন" else "$streakVal Days",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color(0xFFFDE68A).copy(alpha = 0.22f),
                        modifier = Modifier.clickable { onOpenWallet() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🪙", fontSize = 12.sp)
                            Text(
                                text = "${coinVal.toBengali(isBengali)} ${if (isBengali) "কয়েন" else "coins"}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// ২. দৈনিক লক্ষ্য কার্ড (Daily Goal Card)
// ============================================================================
@Composable
fun DailyGoalCard(
    completedCount: Int,
    targetCount: Int,
    isBengali: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    if (targetCount <= 0) return

    val progress by animateFloatAsState(
        targetValue = (completedCount.toFloat() / targetCount).coerceIn(0f, 1f),
        label = "daily_goal_progress"
    )

    val cardBg = if (isDark) Color(0xFF1E2538) else Color(0xFFDCEBFA)
    val ringColor = if (isDark) Color(0xFF8B5CF6) else Color(0xFF2563EB)
    val trackColor = if (isDark) Color(0xFF323B54) else Color.White.copy(alpha = 0.85f)
    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("hero_exam_readiness_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (isDark) {
                            listOf(Color(0xFF312E81), Color(0xFF1E293B))
                        } else {
                            listOf(Color(0xFFE0F2FE), Color(0xFFD9E8FF))
                        }
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
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
                            text = "${completedCount.toBengali(isBengali)}/${targetCount.toBengali(isBengali)}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            ),
                            color = titleColor
                        )
                        Text(
                            text = if (isBengali) "কাজ" else "Tasks",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = subColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = if (isBengali) "দৈনিক লক্ষ্য" else "Daily Goal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = titleColor
                    )
                    Text(
                        text = if (isBengali) "${completedCount.toBengali(true)}টি কাজ সম্পন্ন" else "$completedCount tasks completed",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = subColor
                    )
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            text = if (isBengali) "আজকের অগ্রগতি" else "Today’s progress",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isDark) Color.White else Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
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
    actionLabel: String,
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
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = if (isDark) {
                                listOf(Color(0xFF1F2937), Color(0xFF111827))
                            } else {
                                listOf(Color(0xFFF8FBFF), Color(0xFFE8F1FF))
                            }
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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


                    }

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
                                text = actionLabel,
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
    val cardsData = subjects.take(4).mapIndexed { index, sub ->
        val progress = if (sub.totalChapters > 0) {
            (sub.completedChapters.toFloat() / sub.totalChapters).coerceIn(0f, 1f)
        } else {
            0f
        }

        when (index) {
            0 -> SubjectCardVisual(
                id = sub.id,
                title = sub.getTitle(isBengali),
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
                id = sub.id,
                title = sub.getTitle(isBengali),
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
                id = sub.id,
                title = sub.getTitle(isBengali),
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
                id = sub.id,
                title = sub.getTitle(isBengali),
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

        if (cardsData.isEmpty()) {
            Text(
                text = if (isBengali) "বিষয় লোড হচ্ছে…" else "Subjects are loading…",
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
        } else {
            cardsData.chunked(2).forEach { rowCards ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowCards.forEach { visual ->
                        SubjectGridTile(
                            visual = visual,
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { visual.originSubject?.let(onSubjectClick) }
                        )
                    }
                    if (rowCards.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
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

@Composable
private fun AutoScrollSubjectCard(
    subject: SubjectItem,
    isBengali: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1E2538) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 1.dp),
        modifier = Modifier
            .width(160.dp)
            .height(95.dp)
            .clickable { onClick() }
            .testTag("auto_scroll_subject_${subject.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
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
                        fontSize = 14.sp
                    ),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBengali) "অধ্যায়" else "Chapters",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = subColor
                    )
                    Text(
                        text = "${subject.completedChapters.toBengali(isBengali)}/${subject.totalChapters.toBengali(isBengali)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = textColor
                    )
                }
                val progress = if (subject.totalChapters > 0) {
                    (subject.completedChapters.toFloat() / subject.totalChapters).coerceIn(0f, 1f)
                } else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.4f),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                )
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
    val containerColor = if (isDark) visual.bgDark else visual.bgLight
    val titleColor = if (isDark) Color.White else visual.textColorLight
    val barColor = if (isDark) visual.barColorDark else visual.barColorLight
    val trackColor = if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.55f)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 1.dp),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("subject_card_${visual.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        fontSize = 15.5.sp
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
                    modifier = Modifier.size(26.dp)
                )
            }

            LinearProgressIndicator(
                progress = { visual.progress },
                color = barColor,
                trackColor = trackColor,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
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
    rank: Int?,
    activeStudentsCount: Int,
    isBengali: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1E2538) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val supportingColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("home_leaderboard_spotlight_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isBengali) "সেরাদের তালিকা" else "Leaderboard",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = textColor
                )
                Text(
                    text = rank?.let {
                        if (isBengali) "তোমার অবস্থান ${it.toBengali(true)}" else "Your rank: #$it"
                    } ?: if (isBengali) "কুইজে অংশ নিয়ে নিজের অবস্থান তৈরি করো" else "Take a quiz to earn your rank",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (rank != null) textColor else supportingColor
                )
                if (activeStudentsCount > 0) {
                    Text(
                        text = if (isBengali) "${activeStudentsCount.toBengali(true)} জন শিক্ষার্থী" else "$activeStudentsCount students",
                        style = MaterialTheme.typography.labelSmall,
                        color = supportingColor
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = if (isBengali) "লিডারবোর্ড খুলুন" else "Open leaderboard",
                tint = supportingColor,
                modifier = Modifier.size(18.dp)
            )
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

@Composable
fun MockTestsHomeSection(
    subjects: List<SubjectItem>,
    isBengali: Boolean,
    isDark: Boolean,
    onStartAssessment: (AssessmentMode, String?, String?) -> Unit,
    onSelectSubject: (String) -> Unit,
    onNavigate: (AppScreen) -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = if (isBengali) "🎯 মক টেস্ট ও প্র্যাকটিস সেট" else "Mock Tests & Practice Sets",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }
            TextButton(onClick = { onNavigate(AppScreen.ALL_SUBJECTS) }) {
                Text(
                    text = if (isBengali) "সব দেখুন" else "See All",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(subjects) { subject ->
                val progress = subject.masteryPercentage.coerceIn(0, 100) / 100f
                val completedSets = (subject.completedChapters).coerceAtMost(subject.totalChapters)
                
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .width(220.dp)
                        .clickable {
                            onSelectSubject(subject.id)
                            onNavigate(AppScreen.JOURNEY)
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${subject.totalChapters} সেট",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Progress Bar & Stats
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isBengali) "সম্পন্ন: $completedSets/${subject.totalChapters}" else "Done: $completedSets/${subject.totalChapters}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                                Text(
                                    text = "${subject.masteryPercentage}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF10B981)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF10B981),
                                trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                            )
                        }

                        // Start Quiz Button
                        Button(
                            onClick = {
                                onStartAssessment(AssessmentMode.MOCK_TEST, subject.id, null)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0D9488),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBengali) "কুইজ শুরু করুন" else "Start Quiz",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}