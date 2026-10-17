package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.data.model.StudentProfile
import com.example.data.auth.AuthUserState
import com.example.ui.theme.ExamGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.AppScreen

private fun formatCount(count: Int): String {
    return if (count >= 1000) {
        val kValue = count / 1000.0
        val rem = count % 1000
        if (rem == 0) {
            "${kValue.toInt()}K"
        } else {
            val formatted = ((kValue * 10).toInt() / 10.0)
            "${formatted}K"
        }
    } else {
        count.toString()
    }
}

private fun drawerLabel(isBengali: Boolean, bengali: String, english: String): String =
    if (isBengali) bengali else english

@Composable
fun AppNavigationDrawerSheet(
    currentScreen: AppScreen,
    profile: StudentProfile,
    isBengali: Boolean,
    onNavigate: (AppScreen) -> Unit,
    onCloseDrawer: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    authUserState: AuthUserState? = null
) {
    var showSignOutConfirmation by remember { mutableStateOf(false) }

    ModalDrawerSheet(
        modifier = modifier
            .widthIn(max = 310.dp)
            .testTag("app_navigation_drawer_sheet"),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrollable Menu List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Section: Profile & App Branding
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E40AF), Color(0xFF0F172A))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Profile & Info Row (Horizontal Layout) with Actions at top-right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        onClickLabel = drawerLabel(isBengali, "প্রোফাইল খুলুন", "Open profile"),
                                        role = Role.Button
                                    ) {
                                        onNavigate(AppScreen.PROFILE)
                                        onCloseDrawer()
                                    }
                            ) {
                                // Circular avatar on the left with a subtle white ring/border & real picture if logged in with Google
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.6f)),
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (!authUserState?.photoUrl.isNullOrBlank()) {
                                            SubcomposeAsyncImage(
                                                model = authUserState?.photoUrl,
                                                contentDescription = drawerLabel(isBengali, "প্রোফাইল ছবি", "Profile picture"),
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Text(
                                                text = "👨‍🎓",
                                                fontSize = 26.sp
                                            )
                                        }
                                    }
                                }

                                val realName = when {
                                    profile.name.isNotBlank() && profile.name != "Sourav Das" -> profile.name
                                    authUserState?.displayName?.isNotBlank() == true -> authUserState.displayName
                                    authUserState?.email?.isNotBlank() == true -> authUserState.email.substringBefore("@")
                                    else -> drawerLabel(isBengali, "শিক্ষার্থী", "Student")
                                }

                                val boardShort = when {
                                    profile.targetExam.contains("WBBSE", ignoreCase = true) -> "WBBSE"
                                    profile.targetExam.contains("CBSE", ignoreCase = true) -> "CBSE"
                                    profile.targetExam.contains("WBCHSE", ignoreCase = true) -> "WBCHSE"
                                    else -> "WBBSE"
                                }
                                val classShort = when {
                                    profile.classLevel.contains("10") || profile.classLevel.contains("১০") -> drawerLabel(isBengali, "দশম শ্রেণি", "Class 10")
                                    profile.classLevel.contains("9") || profile.classLevel.contains("৯") -> drawerLabel(isBengali, "নবম শ্রেণি", "Class 9")
                                    profile.classLevel.contains("8") || profile.classLevel.contains("৮") -> drawerLabel(isBengali, "অষ্টম শ্রেণি", "Class 8")
                                    profile.classLevel.contains("7") || profile.classLevel.contains("৭") -> drawerLabel(isBengali, "সপ্তম শ্রেণি", "Class 7")
                                    profile.classLevel.contains("6") || profile.classLevel.contains("৬") -> drawerLabel(isBengali, "ষষ্ঠ শ্রেণি", "Class 6")
                                    profile.classLevel.contains("5") || profile.classLevel.contains("৫") -> drawerLabel(isBengali, "পঞ্চম শ্রেণি", "Class 5")
                                    else -> profile.classLevel.ifBlank { drawerLabel(isBengali, "দশম শ্রেণি", "Class 10") }
                                }
                                val boardAndClass = "$boardShort • $classShort"

                                val schoolName = profile.school.takeIf {
                                    it.isNotBlank() && it != "Kolkata Model High School"
                                }

                                // Vertically stacked user information
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // User Name in bold white text (font size ~18px)
                                    Text(
                                        text = realName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // Board & Class in semi-transparent light white text (font size ~13px)
                                    Text(
                                        text = boardAndClass,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 13.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // School Name in soft muted white text (font size ~12px)
                                    if (schoolName != null) {
                                        Text(
                                            text = schoolName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 12.sp
                                            ),
                                            color = Color.White.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (isAdmin) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50.dp),
                                            color = ExamGold.copy(alpha = 0.22f),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AdminPanelSettings,
                                                    contentDescription = null,
                                                    tint = ExamGold,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = drawerLabel(isBengali, "অ্যাডমিন এক্সেস", "Admin access"),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = Color.White,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Top-right Action Buttons: Close icon only
                            IconButton(
                                onClick = onCloseDrawer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = drawerLabel(isBengali, "বন্ধ করুন", "Close"),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }



                        // Badges / Action Chips (Bottom Row): Coins & XP counters side-by-side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Coins Chip (Opens Wallet Screen)
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                onClick = {
                                    onNavigate(AppScreen.WALLET)
                                    onCloseDrawer()
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = "🪙", fontSize = 12.sp)
                                        Text(
                                            text = formatCount(profile.coins),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            color = Color(0xFF78350F)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF78350F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // XP Chip (Opens Profile / Progress Screen)
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                onClick = {
                                    onNavigate(AppScreen.PROFILE)
                                    onCloseDrawer()
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = "⚡", fontSize = 12.sp)
                                        Text(
                                            text = "${formatCount(profile.currentXp)} XP",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick access row with a more polished, action-oriented layout.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionChip(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = drawerLabel(isBengali, "ওয়ালেট", "Wallet"),
                        onClick = { onNavigate(AppScreen.WALLET); onCloseDrawer() },
                        container = Color(0xFFFEF3C7),
                        content = Color(0xFF7C2D12)
                    )
                    QuickActionChip(
                        icon = Icons.Default.Person,
                        label = drawerLabel(isBengali, "প্রোফাইল", "Profile"),
                        onClick = { onNavigate(AppScreen.PROFILE); onCloseDrawer() },
                        container = Color(0xFFE0E7FF),
                        content = Color(0xFF1E3A8A)
                    )
                    if (isAdmin) {
                        QuickActionChip(
                            icon = Icons.Default.AdminPanelSettings,
                            label = drawerLabel(isBengali, "অ্যাডমিন", "Admin"),
                            onClick = { onNavigate(AppScreen.ADMIN_PANEL); onCloseDrawer() },
                            container = ExamGold.copy(alpha = 0.9f),
                            content = Color(0xFF261400)
                        )
                    }
                }

                // Section 1: Main App Screens
                DrawerSectionHeader(title = drawerLabel(isBengali, "প্রধান নেভিগেশন", "Main navigation"))

                DrawerMenuItem(
                    icon = Icons.Default.Home,
                    label = drawerLabel(isBengali, "হোমপেজ", "Home"),
                    isSelected = currentScreen == AppScreen.HOME,
                    onClick = { onNavigate(AppScreen.HOME); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Map,
                    label = drawerLabel(isBengali, "লার্নিং জার্নি ম্যাপ", "Learning journey"),
                    isSelected = currentScreen == AppScreen.JOURNEY,
                    onClick = { onNavigate(AppScreen.JOURNEY); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Assessment,
                    label = drawerLabel(isBengali, "মক টেস্ট হাব", "Mock tests"),
                    isSelected = currentScreen == AppScreen.MOCK,
                    onClick = { onNavigate(AppScreen.MOCK); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.EmojiEvents,
                    label = drawerLabel(isBengali, "মেডেল ও কৃতি অর্জন", "Badges and achievements"),
                    isSelected = currentScreen == AppScreen.BADGES,
                    onClick = { onNavigate(AppScreen.BADGES); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Leaderboard,
                    label = drawerLabel(isBengali, "লিডারবোর্ড র‍্যাঙ্ক", "Leaderboard"),
                    isSelected = currentScreen == AppScreen.LEADERBOARD,
                    onClick = { onNavigate(AppScreen.LEADERBOARD); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.SportsEsports,
                    label = drawerLabel(isBengali, "চ্যালেঞ্জ এরিনা", "Challenge arena"),
                    isSelected = currentScreen == AppScreen.BATTLE,
                    onClick = { onNavigate(AppScreen.BATTLE); onCloseDrawer() }
                )

                // Section 2: Specialty Tools
                DrawerSectionHeader(title = drawerLabel(isBengali, "বিশেষ পরীক্ষার টুলস", "Study tools"))

                DrawerMenuItem(
                    icon = Icons.Default.Style,
                    label = drawerLabel(isBengali, "স্মার্ট ফ্ল্যাশকার্ড", "Smart flashcards"),
                    isSelected = currentScreen == AppScreen.FLASHCARDS,
                    onClick = { onNavigate(AppScreen.FLASHCARDS); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Search,
                    label = drawerLabel(isBengali, "বিগত বছরের PYQ এক্সপ্লোরার", "Previous-year questions"),
                    isSelected = currentScreen == AppScreen.PYQ_EXPLORER,
                    onClick = { onNavigate(AppScreen.PYQ_EXPLORER); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.AssignmentTurnedIn,
                    label = drawerLabel(isBengali, "ডিজিটাল OMR শিট প্র্যাকটিস", "OMR practice"),
                    isSelected = currentScreen == AppScreen.OMR_PRACTICE,
                    onClick = { onNavigate(AppScreen.OMR_PRACTICE); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.MenuBook,
                    label = drawerLabel(isBengali, "টপার্স স্টাডি গাইড", "Topper study guide"),
                    isSelected = currentScreen == AppScreen.TOPPERS_GUIDE,
                    onClick = { onNavigate(AppScreen.TOPPERS_GUIDE); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Quiz,
                    label = drawerLabel(isBengali, "ডাউট সলভার ফোরাম (Ask AI)", "Ask AI"),
                    isSelected = currentScreen == AppScreen.DOUBT_SOLVER,
                    onClick = { onNavigate(AppScreen.DOUBT_SOLVER); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.People,
                    label = drawerLabel(isBengali, "মাল্টিপ্লেয়ার ব্যাটল কুইজ", "Multiplayer battle quiz"),
                    isSelected = currentScreen == AppScreen.MULTIPLAYER_BATTLE,
                    onClick = { onNavigate(AppScreen.MULTIPLAYER_BATTLE); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.CardMembership,
                    label = drawerLabel(isBengali, "প্রস্তুতি সার্টিফিকেট ও মেডেল", "Certificates and medals"),
                    isSelected = currentScreen == AppScreen.CERTIFICATE,
                    onClick = { onNavigate(AppScreen.CERTIFICATE); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.PlayCircle,
                    label = drawerLabel(isBengali, "ভিডিও লেকচার ক্লাস", "Video lessons"),
                    isSelected = currentScreen == AppScreen.VIDEO_LESSON,
                    onClick = { onNavigate(AppScreen.VIDEO_LESSON); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Audiotrack,
                    label = drawerLabel(isBengali, "অডিও পডকাস্ট রিভিশন", "Audio podcasts"),
                    isSelected = currentScreen == AppScreen.AUDIO_PODCASTS,
                    onClick = { onNavigate(AppScreen.AUDIO_PODCASTS); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Schedule,
                    label = drawerLabel(isBengali, "পার্সোনালাইজড স্টাডি প্ল্যানার", "Study planner"),
                    isSelected = currentScreen == AppScreen.STUDY_PLANNER,
                    onClick = { onNavigate(AppScreen.STUDY_PLANNER); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Bookmarks,
                    label = drawerLabel(isBengali, "বুকমার্কড গুরুত্বপূর্ণ প্রশ্নসমূহ", "Bookmarked questions"),
                    isSelected = currentScreen == AppScreen.BOOKMARK_HUB,
                    onClick = { onNavigate(AppScreen.BOOKMARK_HUB); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Description,
                    label = drawerLabel(isBengali, "অধ্যায়ভিত্তিক PDF নোটস ও হ্যান্ডআউট", "Chapter PDF notes"),
                    isSelected = currentScreen == AppScreen.PDF_NOTES,
                    onClick = { onNavigate(AppScreen.PDF_NOTES); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Download,
                    label = drawerLabel(isBengali, "বিগত বছরের সমাধান করা PDF", "Solved question PDFs"),
                    isSelected = currentScreen == AppScreen.PYQ_PDF_DOWNLOAD,
                    onClick = { onNavigate(AppScreen.PYQ_PDF_DOWNLOAD); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.HistoryEdu,
                    label = drawerLabel(isBengali, "বিগত বছরের মূল PDF ডাউনলোড", "Original question PDFs"),
                    isSelected = currentScreen == AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD,
                    onClick = { onNavigate(AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.LibraryBooks,
                    label = drawerLabel(isBengali, "অফিসিয়াল সরকারি পাঠ্যবই", "Official textbooks"),
                    isSelected = currentScreen == AppScreen.TEXTBOOKS,
                    onClick = { onNavigate(AppScreen.TEXTBOOKS); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.LiveTv,
                    label = drawerLabel(isBengali, "লাইভ কুইজ ইভেন্ট", "Live quiz"),
                    isSelected = currentScreen == AppScreen.LIVE_QUIZ,
                    onClick = { onNavigate(AppScreen.LIVE_QUIZ); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Alarm,
                    label = drawerLabel(isBengali, "লাইভ মক টেস্ট ও রাজ্য র‍্যাঙ্কিং", "Live mock test"),
                    isSelected = currentScreen == AppScreen.LIVE_MOCK_TEST,
                    onClick = { onNavigate(AppScreen.LIVE_MOCK_TEST); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Groups,
                    label = drawerLabel(isBengali, "গ্রুপ স্টাডি স্কোয়াডস", "Study squads"),
                    isSelected = currentScreen == AppScreen.STUDY_SQUADS,
                    onClick = { onNavigate(AppScreen.STUDY_SQUADS); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Insights,
                    label = drawerLabel(isBengali, "প্রোগ্রেস অ্যানালিটিক্স ও ড্যাশবোর্ড", "Progress dashboard"),
                    isSelected = currentScreen == AppScreen.PROGRESS_DASHBOARD,
                    onClick = { onNavigate(AppScreen.PROGRESS_DASHBOARD); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.AutoGraph,
                    label = drawerLabel(isBengali, "দুর্বল অধ্যায় বিশ্লেষণ", "Weakness insights"),
                    isSelected = currentScreen == AppScreen.WEAKNESS_INSIGHT,
                    onClick = { onNavigate(AppScreen.WEAKNESS_INSIGHT); onCloseDrawer() }
                )

                // Section 3: Profile & Settings
                DrawerSectionHeader(title = drawerLabel(isBengali, "অ্যাকাউন্ট ও সেটিংস", "Account and settings"))

                DrawerMenuItem(
                    icon = Icons.Default.Person,
                    label = drawerLabel(isBengali, "আমার প্রোফাইল", "My profile"),
                    isSelected = currentScreen == AppScreen.PROFILE,
                    onClick = { onNavigate(AppScreen.PROFILE); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.WorkspacePremium,
                    label = drawerLabel(isBengali, "ভিআইপি মেম্বারশিপ", "VIP membership"),
                    isSelected = currentScreen == AppScreen.MEMBERSHIP,
                    onClick = { onNavigate(AppScreen.MEMBERSHIP); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = drawerLabel(isBengali, "ওয়ালেট ও কয়েন", "Wallet and coins"),
                    isSelected = currentScreen == AppScreen.WALLET,
                    onClick = { onNavigate(AppScreen.WALLET); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    label = drawerLabel(isBengali, "অ্যাপ সেটিংস", "App settings"),
                    isSelected = currentScreen == AppScreen.SETTINGS,
                    onClick = { onNavigate(AppScreen.SETTINGS); onCloseDrawer() }
                )

                if (isAdmin) {
                    DrawerMenuItem(
                        icon = Icons.Default.AdminPanelSettings,
                        label = drawerLabel(isBengali, "ফায়ারবেস অ্যাডমিন", "Firebase admin"),
                        isSelected = currentScreen == AppScreen.ADMIN_PANEL,
                        onClick = { onNavigate(AppScreen.ADMIN_PANEL); onCloseDrawer() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Keep profile and sign-out actions here; other destinations
            // already have dedicated entries in the scrollable menu.
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { showSignOutConfirmation = true },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = drawerLabel(isBengali, "লগ আউট", "Sign out"),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable(
                            onClickLabel = drawerLabel(isBengali, "প্রোফাইল খুলুন", "Open profile"),
                            role = Role.Button
                        ) {
                            onNavigate(AppScreen.PROFILE)
                            onCloseDrawer()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = drawerLabel(isBengali, "প্রোফাইল", "Profile"),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = drawerLabel(isBengali, "প্রোফাইল", "Profile"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isAdmin) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = ExamGold,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable(
                                onClickLabel = drawerLabel(isBengali, "অ্যাডমিন প্যানেল খুলুন", "Open admin panel"),
                                role = Role.Button
                            ) {
                                onNavigate(AppScreen.ADMIN_PANEL)
                                onCloseDrawer()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = drawerLabel(isBengali, "অ্যাডমিন প্যানেল", "Admin panel"),
                                tint = Color(0xFF261400),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = drawerLabel(isBengali, "অ্যাডমিন", "Admin"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                fontSize = 11.sp,
                                color = Color(0xFF261400)
                            )
                        }
                    }
                }

            }

            if (showSignOutConfirmation) {
                AlertDialog(
                    onDismissRequest = { showSignOutConfirmation = false },
                    title = {
                        Text(drawerLabel(isBengali, "লগ আউট করবেন?", "Sign out?"))
                    },
                    text = {
                        Text(
                            drawerLabel(
                                isBengali,
                                "আপনি কি নিশ্চিতভাবে অ্যাকাউন্ট থেকে লগ আউট করতে চান?",
                                "Are you sure you want to sign out of your account?"
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSignOutConfirmation = false
                                onSignOut()
                                onCloseDrawer()
                            }
                        ) {
                            Text(drawerLabel(isBengali, "লগ আউট", "Sign out"))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSignOutConfirmation = false }) {
                            Text(drawerLabel(isBengali, "বাতিল", "Cancel"))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RowScope.QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = container,
        tonalElevation = 1.dp,
        modifier = modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .clickable(onClickLabel = label, role = Role.Button, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = content,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = content,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = {
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = PrimaryIndigo.copy(alpha = 0.12f),
            selectedIconColor = PrimaryIndigo,
            selectedTextColor = PrimaryIndigo,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}
