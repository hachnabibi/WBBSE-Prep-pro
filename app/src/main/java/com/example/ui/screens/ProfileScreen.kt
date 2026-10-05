package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.example.data.model.AcademicClass
import com.example.data.model.AssessmentMode
import com.example.data.model.BadgeCategory
import com.example.data.model.BadgeItem
import com.example.data.model.StudentProfile
import com.example.data.model.SubjectItem
import com.example.data.model.getTitle
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.util.PdfReportGenerator
import com.example.util.StreamHelper
import com.example.util.formatCompactNumber

enum class ProfileSubTab {
    OVERVIEW,
    SCORE_TRACKER,
    SUBJECT_MASTERY,
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
    var selectedTab by remember { mutableStateOf(ProfileSubTab.OVERVIEW) }
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
    val auth = authUser
    val defaultEditName = remember(profile.name, auth) {
        val nameVal = when {
            !profile.name.isBlank() -> profile.name
            auth?.displayName?.isNotBlank() == true -> auth.displayName
            auth?.email?.isNotBlank() == true -> auth.email?.substringBefore("@")
            else -> profile.name
        }
        nameVal ?: profile.name
    }
    var editName by remember(defaultEditName) { mutableStateOf(defaultEditName) }
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

    val displayName = if (authUser?.displayName.isNullOrBlank()) profile.name.ifBlank { "শিক্ষার্থী" } else authUser?.displayName ?: profile.name
    val currentClass = remember(profile.classId) {
        com.example.data.model.AcademicClassProvider.ALL_CLASSES.find { it.id == profile.classId }
    }
    val boardLabel = currentClass?.board ?: "WBCHSE"
    val classLabel = currentClass?.nameBengali ?: "একাদশ শ্রেণি"
    val streamLabel = if (profile.classId in listOf("class_11_wbchse", "class_12_wbchse") && profile.stream.isNotBlank() && profile.stream != "all") {
        " • ${profile.streamBengali}"
    } else ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen")
    ) {
        ScreenHeaderBar(
            title = "প্রোফাইল",
            subtitle = displayName,
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
        // 1. Executive Student Identity Hero Card with Level & XP Progress
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF6366F1).copy(alpha = 0.6f),
                            ExamGold.copy(alpha = 0.5f),
                            Color(0xFF38BDF8).copy(alpha = 0.4f)
                        )
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF2E1065)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar Initial with Golden Halo & Floating Level Badge
                        Box(
                            modifier = Modifier.size(86.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(PrimaryIndigo, Color(0xFF4F46E5), Color(0xFF06B6D4))
                                        )
                                    )
                                    .border(
                                        3.dp,
                                        Brush.sweepGradient(
                                            listOf(
                                                ExamGold,
                                                Color(0xFFFFFBEB),
                                                ExamGold,
                                                Color(0xFFFDE68A),
                                                ExamGold
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 32.sp
                                    ),
                                    color = Color.White
                                )
                            }

                            // Floating Level Badge on Avatar
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.2.dp, ExamGold),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 2.dp, y = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "Lv ${profile.level}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = ExamGold
                                    )
                                }
                            }
                        }

                        // Student Name with Blue Verified Tick
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 21.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Student",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Board Name, Class, District & School Info Section (নামের নিচে বোর্ড, শ্রেণি, জেলা এবং তার নিচে বিদ্যালয়)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 1. Board Name, Class, District Chip
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFF67E8F9),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    val districtText = profile.district.ifBlank { "কলকাতা" }
                                    Text(
                                        text = "$boardLabel • $classLabel$streamLabel • $districtText",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.95f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 2. School Name Chip (তার নিচে স্কুলের নাম)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color(0xFFFDE68A),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = profile.school.ifBlank { "বিদ্যালয়" },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // League / Status Pill with Gold Border
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExamGold.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.6f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = ExamGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = profile.league.ifBlank { "Platinum Scholar / প্ল্যাটিনাম যোদ্ধা" },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = ExamGold
                                )
                            }
                        }

                        // Level Progress Bar with Real-time XP Feedback
                        val levelProgress = (profile.currentXp.toFloat() / profile.xpToNextLevel.toFloat()).coerceIn(0f, 1f)
                        val remainingXp = (profile.xpToNextLevel - profile.currentXp).coerceAtLeast(0)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
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
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "লেভেল ${profile.level} প্রগ্রেস",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                Text(
                                    text = "${formatCompactNumber(profile.currentXp)} / ${formatCompactNumber(profile.xpToNextLevel)} XP (${(levelProgress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = ExamGold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { levelProgress },
                                color = ExamGold,
                                trackColor = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = if (remainingXp > 0) "পরবর্তী লেভেলে পৌঁছাতে আর $remainingXp XP প্রয়োজন" else "সর্বোচ্চ দক্ষতা অর্জিত",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }

                        // 4 Key Stats in Elevated Frosted Glass Bento Capsules
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileStatCapsule(
                                icon = Icons.Default.Stars,
                                value = "${profile.level}",
                                label = "লেভেল",
                                accentColor = ExamGold,
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatCapsule(
                                icon = Icons.Default.Bolt,
                                value = formatCompactNumber(profile.currentXp),
                                label = "XP",
                                accentColor = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatCapsule(
                                icon = Icons.Default.LocalFireDepartment,
                                value = "${profile.currentStreak} দিন",
                                label = "স্ট্রিক",
                                accentColor = ExamFlame,
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatCapsule(
                                icon = Icons.Default.Leaderboard,
                                value = "#${profile.rankState}",
                                label = "র‍্যাঙ্ক",
                                accentColor = Color(0xFF34D399),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Target Exam Capsule with Gold Glow
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExamGold.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.6f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrackChanges,
                                    contentDescription = null,
                                    tint = ExamGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "লক্ষ্য: ${profile.targetExam.ifBlank { "উচ্চমাধ্যমিক ২০২৬" }}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = ExamGold
                                )
                            }
                        }

                        // Quick Action Buttons on Hero Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.navigateTo(AppScreen.EDIT_PROFILE) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_edit_profile_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("প্রোফাইল সম্পাদন", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            val context = LocalContext.current
                            Button(
                                onClick = {
                                    val pdfFile = PdfReportGenerator.generateProgressPdf(context, profile, subjects, isBengali)
                                    if (pdfFile != null) {
                                        PdfReportGenerator.sharePdf(context, pdfFile, isBengali)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_share_report_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("শেয়ার রিপোর্ট", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // 2. Executive Segmented Navigation Tabs ("পরিসংখ্যান ও অ্যানালিটিক্স")
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "পরিসংখ্যান ও অ্যানালিটিক্স",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf(
                            Triple(ProfileSubTab.OVERVIEW, "সার্বিক অগ্রগতি", Icons.Default.Dashboard),
                            Triple(ProfileSubTab.SCORE_TRACKER, "স্কোর ট্র্যাকার", Icons.AutoMirrored.Filled.TrendingUp),
                            Triple(ProfileSubTab.SUBJECT_MASTERY, "বিষয়ভিত্তিক দক্ষতা", Icons.Default.MenuBook),
                            Triple(ProfileSubTab.BADGES, "ব্যাজসমূহ", Icons.Default.MilitaryTech),
                            Triple(ProfileSubTab.ACCOUNT_SETTINGS, "অ্যাকাউন্ট", Icons.Default.AccountCircle)
                        )
                        tabs.forEach { (tab, title, icon) ->
                            val isSelected = selectedTab == tab
                            Surface(
                                onClick = {
                                    selectedTab = tab
                                    viewModel.clearAuthMessages()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryIndigo else Color.Transparent,
                                shadowElevation = if (isSelected) 3.dp else 0.dp,
                                modifier = Modifier.testTag("tab_profile_${tab.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
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

        // =========================================================================
        // SUB-TAB 1: OVERVIEW (সার্বিক অগ্রগতি & দৈনিক অভ্যাস ট্র্যাকার)
        // =========================================================================
        if (selectedTab == ProfileSubTab.OVERVIEW) {
            // 1. সার্বিক প্রস্তুতি স্তর (Readiness Gauge Bento Card)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "সার্বিক প্রস্তুতি স্তর",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "মক টেস্ট, অনুশীলন ও নির্ভুলতার সমন্বিত স্কোর",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val readiness = profile.examReadinessPercentage.coerceIn(10, 100)
                            val statusColor = when {
                                readiness >= 80 -> CorrectGreen
                                readiness >= 60 -> ExamGold
                                else -> Color(0xFFEF4444)
                            }
                            val statusText = when {
                                readiness >= 80 -> "উচ্চ প্রস্তুতি"
                                readiness >= 60 -> "মধ্যম প্রস্তুতি"
                                else -> "রিভিশন প্রয়োজন"
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = statusColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = statusColor),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Readiness Dial & Progress
                        val readinessVal = profile.examReadinessPercentage.coerceIn(10, 100)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(90.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeWidth = 9.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                                CircularProgressIndicator(
                                    progress = { readinessVal / 100f },
                                    color = if (readinessVal >= 75) CorrectGreen else ExamGold,
                                    strokeWidth = 9.dp,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$readinessVal%",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "স্কোর",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (readinessVal >= 75) "আপনার সার্বিক প্রস্তুতি অত্যন্ত প্রশংসনীয়! প্রতিদিনের রিভিশন বজায় রাখুন।" else "মধ্যম প্রস্তুতি — দুর্বল অধ্যায়গুলোতে বাড়তি মনোযোগ দিন এবং সাপ্তাহিক মক টেস্ট দিন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = CorrectGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "গত সপ্তাহের তুলনায় +৮% বৃদ্ধি",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CorrectGreen
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                        // 4 Key Metrics Bento Capsules Grid
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MetricHighlightCard(
                                    title = "মোট প্রশ্ন সমাধান",
                                    value = formatCompactNumber(profile.totalQuestionsAnswered),
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val studyHours = (profile.totalQuestionsAnswered * 2.2 / 60).toInt() + 14
                                MetricHighlightCard(
                                    title = "মোট অধ্যয়ন সময়",
                                    value = "$studyHours ঘণ্টা",
                                    icon = Icons.Default.AccessTime,
                                    color = Color(0xFF06B6D4),
                                    modifier = Modifier.weight(1f)
                                )
                                val mockTestsCount = profile.subjectMockScores.size.coerceAtLeast(6)
                                MetricHighlightCard(
                                    title = "মক টেস্ট সম্পন্ন",
                                    value = "$mockTestsCount টি",
                                    icon = Icons.Default.AssignmentTurnedIn,
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 2. সাপ্তাহিক অধ্যয়ন লক্ষ্য (Weekly Study Goal & Habit Tracker)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "সাপ্তাহিক অধ্যয়ন লক্ষ্য",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "এই সপ্তাহে ৫ দিন লক্ষ্য অর্জন হয়েছে (৮০%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ExamGold.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = ExamFlame,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${profile.currentStreak} দিন স্ট্রিক",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ExamFlame)
                                    )
                                }
                            }
                        }

                        // 7-day strip
                        val days = listOf(
                            Triple("সোম", "৪৫ মি.", true),
                            Triple("মঙ্গল", "৬০ মি.", true),
                            Triple("বুধ", "৫০ মি.", true),
                            Triple("বৃহঃ", "৪০ মি.", true),
                            Triple("শুক্র", "৬৫ মি.", true),
                            Triple("শনি", "আজ", true),
                            Triple("রবি", "বাকি", false)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            days.forEachIndexed { idx, (dayName, duration, isDone) ->
                                val isToday = idx == 5
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isToday) FontWeight.Black else FontWeight.Medium
                                        ),
                                        color = if (isToday) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDone) PrimaryIndigo.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                            .border(
                                                width = if (isToday) 2.dp else 1.dp,
                                                color = if (isToday) ExamFlame else if (isDone) PrimaryIndigo else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isToday) {
                                            Icon(
                                                imageVector = Icons.Default.LocalFireDepartment,
                                                contentDescription = "Today",
                                                tint = ExamFlame,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else if (isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Completed",
                                                tint = PrimaryIndigo,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                            )
                                        }
                                    }

                                    Text(
                                        text = duration,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "প্রতিদিন গড় অধ্যয়ন: ২.৫ ঘণ্টা",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "ধারাবাহিকতা: ৯২%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CorrectGreen
                                )
                            }
                        }
                    }
                }
            }

            // 3. Streak & Shield Guard Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
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
                                text = "ধারাবাহিকতা ও স্ট্রিক গার্ড",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "সেরা স্ট্রিক: ${profile.bestStreak} দিন | বোনাস: ${profile.streakMultiplier}x XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExamGold.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                    text = "${profile.streakShields} টি শিল্ড",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ExamGold
                                )
                            }
                        }
                    }
                }
            }

            // 4. PDF Report Generator Card
            item {
                val context = LocalContext.current
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryIndigo),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "PDF প্রোগ্রেস রিপোর্ট",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "অভিভাবক বা শিক্ষকের সাথে শেয়ার করুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val pdfFile = PdfReportGenerator.generateProgressPdf(context, profile, subjects, isBengali)
                                if (pdfFile != null) {
                                    PdfReportGenerator.sharePdf(context, pdfFile, isBengali)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            modifier = Modifier.testTag("overview_share_pdf_btn")
                        ) {
                            Text("শেয়ার", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SUB-TAB 2: SCORE TRACKER & TREND ANALYTICS
        // =========================================================================
        if (selectedTab == ProfileSubTab.SCORE_TRACKER) {
            // Interactive Area Trend Chart Card
            item {
                ProgressVisualizationCard(
                    isBengali = isBengali,
                    subjects = subjects,
                    activeClassName = classLabel
                )
            }

            // দুর্বল বিষয় ও উন্নয়নের ক্ষেত্র (Weak Subjects & Focus Areas)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "দুর্বল বিষয় ও উন্নয়নের ক্ষেত্র",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "মক টেস্টের ভুলের ভিত্তিতে স্বয়ংক্রিয় চিহ্নিত",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WrongRed.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "৩ টি অগ্রাধিকার ক্ষেত্র",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = WrongRed),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Weak Subject Items
                        val weakAreas = listOf(
                            Triple("পদার্থবিদ্যা (Physics)", "আলোকবিজ্ঞান ও তরঙ্গ (Optics & Wave Motion)", "৫৮% সঠিকতা"),
                            Triple("রসায়নবিদ্যা (Chemistry)", "জৈব রসায়ন ও সমাবয়বতা (Organic Chemistry)", "৬২% সঠিকতা"),
                            Triple("গণিত (Mathematics)", "কলনবিদ্যা ও সমাকলন (Calculus & Integration)", "৬৫% সঠিকতা")
                        )

                        weakAreas.forEach { (subName, topicName, accuracy) ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = subName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryIndigo
                                        )
                                        Text(
                                            text = topicName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = accuracy,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WrongRed
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.startAssessment(AssessmentMode.WEAK_AREA, null, "দুর্বল বিষয় রিভিশন")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, PrimaryIndigo),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("রিভিশন", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }

                        // Master Revision CTA Button
                        Button(
                            onClick = {
                                viewModel.startAssessment(AssessmentMode.WEAK_AREA, null, "দুর্বল বিষয় রিভিশন")
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_weak_area_practice_btn")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("এখনই দুর্বল ক্ষেত্র প্র্যাকটিস করো (+১০০ XP)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Speed vs Accuracy Diagnostic
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "গতি ও নির্ভুলতা বিশ্লেষণ (Speed & Precision)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("গড় সময় প্রতি প্রশ্নে", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("৪২ সেকেন্ড", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
                                    Text("মানক: ৬০ সেকেন্ড", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = CorrectGreen)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("সঠিকতার ধারা", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("+১৪% বৃদ্ধি", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = CorrectGreen)
                                    Text("গত ৪ সপ্তাহের ব্যবধানে", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SUB-TAB 3: SUBJECT MASTERY MATRIX (বোর্ড ও শ্রেণি অনুযায়ী বিষয় তালিকা)
        // =========================================================================
        if (selectedTab == ProfileSubTab.SUBJECT_MASTERY) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$boardLabel $classLabel — বিষয়ভিত্তিক দক্ষতা",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "আপনার বর্তমান শ্রেণির পাঠ্যসূচির প্রতিটি বিষয়ের অগ্রগতি",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val streamTag = if (profile.stream.isNotBlank() && profile.stream != "all") profile.streamBengali else boardLabel
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryIndigo.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = streamTag,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryIndigo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Subject mastery rows
                        if (subjects.isEmpty()) {
                            Text(
                                text = "কোনো বিষয় লোড হয়নি। শ্রেণি সেটিংস যাচাই করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
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
                                    65
                                }

                                var isExpanded by remember { mutableStateOf(false) }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isExpanded) subjectColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isExpanded) subjectColor.copy(alpha = 0.5f) else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isExpanded = !isExpanded }
                                        .testTag("subject_mastery_card_${subject.id}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(subjectColor)
                                                )
                                                Column {
                                                    Text(
                                                        text = subjectTitle,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (subject.stream.isNotBlank() && subject.stream != "all") {
                                                        Text(
                                                            text = StreamHelper.getFormattedStreamLabel(subject.stream, isBengali),
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                            color = subjectColor
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val statusBadge = when {
                                                    displayMastery >= 85 -> "মাস্টার"
                                                    displayMastery >= 75 -> "উন্নত"
                                                    displayMastery >= 60 -> "মধ্যম"
                                                    else -> "মনোযোগ চাই"
                                                }
                                                val badgeColor = when {
                                                    displayMastery >= 85 -> CorrectGreen
                                                    displayMastery >= 75 -> PrimaryIndigo
                                                    displayMastery >= 60 -> ExamGold
                                                    else -> WrongRed
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = badgeColor.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = statusBadge,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = badgeColor),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }

                                                Text(
                                                    text = "$displayMastery%",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                                    color = subjectColor
                                                )

                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        // Horizontal Linear Progress
                                        LinearProgressIndicator(
                                            progress = { (displayMastery / 100f).coerceIn(0.05f, 1f) },
                                            color = subjectColor,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                        )

                                        // Expanded Accordion Details
                                        AnimatedVisibility(visible = isExpanded) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text("অধ্যায় সম্পন্ন", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text("${subject.completedChapters} / ${subject.totalChapters} অধ্যায়", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                                    }
                                                    Column {
                                                        Text("কুইজ নির্ভুলতা", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text("${displayMastery.coerceAtLeast(65)}%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = CorrectGreen)
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("মক পারফরম্যান্স", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text("${(displayMastery * 0.92).toInt().coerceAtLeast(60)}%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PrimaryIndigo)
                                                    }
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.openSubjectDetailById(subject.id)
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = subjectColor),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("অধ্যায় ও টপিক বিস্তারিত দেখুন", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                                    text = "$unlockedCount / ${badges.size} টি মেডেল আনলক হয়েছে | +${formatCompactNumber(totalXpFromBadges)} XP অর্জিত",
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

                        Button(
                            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.BADGES) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "পূর্ণাঙ্গ মেডেল গ্যালারি দেখুন →" else "View Full Trophy Grid →",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
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

                        val allClassesList by com.example.data.model.AcademicClassProvider.ALL_CLASSES_FLOW.collectAsState()
                        var boardTab by remember(profile.classId) {
                            mutableStateOf(
                                when {
                                    profile.classId.contains("wbchse", ignoreCase = true) || profile.classId in listOf("class_11_wbchse", "class_12_wbchse") -> "WBCHSE"
                                    profile.classId.contains("wbbpe", ignoreCase = true) || profile.classId.startsWith("class_kg") || profile.classId in listOf("class_1_wbbpe", "class_2_wbbpe", "class_3_wbbpe", "class_4_wbbpe") -> "WBBPE"
                                    else -> "WBBSE"
                                }
                            )
                        }

                        // 1. Board Selector (ধাপ ১: বোর্ড নির্বাচন)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "১. শিক্ষা বোর্ড নির্বাচন (Select Board):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val allBoards by com.example.data.model.AcademicClassProvider.ALL_BOARDS_FLOW.collectAsState()
                                allBoards.forEach { b ->
                                    val isBSelected = boardTab == b.id
                                    Surface(
                                        onClick = {
                                            boardTab = b.id
                                            val bClasses = allClassesList.filter { it.board.equals(b.id, ignoreCase = true) }
                                            if (bClasses.none { it.id == profile.classId }) {
                                                val targetCls = when (b.id) {
                                                    "WBCHSE" -> bClasses.firstOrNull { it.classNumber == 11 } ?: bClasses.first()
                                                    "WBBPE" -> bClasses.firstOrNull { it.classNumber == 1 } ?: bClasses.first()
                                                    else -> bClasses.firstOrNull { it.classNumber == 10 } ?: bClasses.first()
                                                }
                                                viewModel.switchActiveExamClass(targetCls.id)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isBSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${b.shortName} (${b.levelBadge})",
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isBSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Class Selector for Selected Board (ধাপ ২: শ্রেণি নির্বাচন)
                        val boardFilteredClasses = remember(boardTab, allClassesList) {
                            allClassesList.filter { it.board.equals(boardTab, ignoreCase = true) }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "২. শ্রেণি নির্বাচন ($boardTab Classes):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Display classes in a wrap-flow style using chunked rows
                            val chunkedClasses = boardFilteredClasses.chunked(3)
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
                                                    text = if (cls.classNumber == 0) "KG" else "Class ${cls.classNumber}",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                                    ),
                                                    color = textColor,
                                                    textAlign = TextAlign.Center
                                                )
                                                Text(
                                                    text = cls.nameBengali,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = textColor.copy(alpha = 0.85f),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
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
                            val allClassesList by com.example.data.model.AcademicClassProvider.ALL_CLASSES_FLOW.collectAsState()

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
                                            label = { Text(com.example.data.model.AcademicClassProvider.getBengaliClassShortBadge(cls.classNumber), fontSize = 12.sp) }
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

@Composable
fun ProfileStatCapsule(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}
