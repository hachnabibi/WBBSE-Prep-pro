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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
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
    ModalDrawerSheet(
        modifier = modifier
            .width(310.dp)
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
                                modifier = Modifier.weight(1f)
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
                                                contentDescription = "User Profile Picture",
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
                                    !profile.name.isBlank() -> profile.name
                                    authUserState?.displayName?.isNotBlank() == true -> authUserState.displayName
                                    authUserState?.email?.isNotBlank() == true -> authUserState.email.substringBefore("@")
                                    else -> "Sourav Das"
                                }

                                val boardShort = when {
                                    profile.targetExam.contains("WBBSE", ignoreCase = true) -> "WBBSE"
                                    profile.targetExam.contains("CBSE", ignoreCase = true) -> "CBSE"
                                    profile.targetExam.contains("WBCHSE", ignoreCase = true) -> "WBCHSE"
                                    else -> "WBBSE"
                                }
                                val classShort = when {
                                    profile.classLevel.contains("10") || profile.classLevel.contains("১০") -> "Class 10"
                                    profile.classLevel.contains("9") || profile.classLevel.contains("৯") -> "Class 9"
                                    profile.classLevel.contains("8") || profile.classLevel.contains("৮") -> "Class 8"
                                    profile.classLevel.contains("7") || profile.classLevel.contains("৭") -> "Class 7"
                                    profile.classLevel.contains("6") || profile.classLevel.contains("৬") -> "Class 6"
                                    profile.classLevel.contains("5") || profile.classLevel.contains("৫") -> "Class 5"
                                    else -> profile.classLevel.ifBlank { "Class 10" }
                                }
                                val boardAndClass = "$boardShort • $classShort"

                                val schoolName = if (!profile.school.isBlank()) profile.school else "Kolkata Model High School"

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
                            }

                            // Top-right Action Buttons: Close icon only
                            IconButton(
                                onClick = onCloseDrawer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
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
                                    .height(36.dp),
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
                                    .height(36.dp),
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

                // Section 1: Main App Screens
                DrawerSectionHeader(title = "প্রধান নেভিগেশন")

                DrawerMenuItem(
                    icon = Icons.Default.Home,
                    label = "হোমপেজ",
                    isSelected = currentScreen == AppScreen.HOME,
                    onClick = { onNavigate(AppScreen.HOME); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Map,
                    label = "লার্নিং জার্নি ম্যাপ",
                    isSelected = currentScreen == AppScreen.JOURNEY,
                    onClick = { onNavigate(AppScreen.JOURNEY); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Assessment,
                    label = "মক টেস্ট হাব",
                    isSelected = currentScreen == AppScreen.MOCK,
                    onClick = { onNavigate(AppScreen.MOCK); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "মেডেল ও কৃতি অর্জন",
                    isSelected = currentScreen == AppScreen.BADGES,
                    onClick = { onNavigate(AppScreen.BADGES); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Leaderboard,
                    label = "লীডারবোর্ড র‍্যাঙ্ক",
                    isSelected = currentScreen == AppScreen.LEADERBOARD,
                    onClick = { onNavigate(AppScreen.LEADERBOARD); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.SportsEsports,
                    label = "চ্যালেঞ্জ এরিনা",
                    isSelected = currentScreen == AppScreen.BATTLE,
                    onClick = { onNavigate(AppScreen.BATTLE); onCloseDrawer() }
                )

                // Section 2: Specialty Tools
                DrawerSectionHeader(title = "বিশেষ পরীক্ষার টুলস")

                DrawerMenuItem(
                    icon = Icons.Default.Style,
                    label = "স্মার্ট ফ্ল্যাশকার্ড",
                    isSelected = currentScreen == AppScreen.FLASHCARDS,
                    onClick = { onNavigate(AppScreen.FLASHCARDS); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Search,
                    label = "বিগত বছরের PYQ এক্সপ্লোরার",
                    isSelected = currentScreen == AppScreen.PYQ_EXPLORER,
                    onClick = { onNavigate(AppScreen.PYQ_EXPLORER); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.AssignmentTurnedIn,
                    label = "ডিজিটাল OMR শিট প্র্যাকটিস",
                    isSelected = currentScreen == AppScreen.OMR_PRACTICE,
                    onClick = { onNavigate(AppScreen.OMR_PRACTICE); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.MenuBook,
                    label = "টপার্স স্টাডি গাইড",
                    isSelected = currentScreen == AppScreen.TOPPERS_GUIDE,
                    onClick = { onNavigate(AppScreen.TOPPERS_GUIDE); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Quiz,
                    label = "ডাউট সলভার ফোরাম (Ask AI)",
                    isSelected = currentScreen == AppScreen.DOUBT_SOLVER,
                    onClick = { onNavigate(AppScreen.DOUBT_SOLVER); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.People,
                    label = "মাল্টিপ্লেয়ার ব্যাটল কুইজ",
                    isSelected = currentScreen == AppScreen.MULTIPLAYER_BATTLE,
                    onClick = { onNavigate(AppScreen.MULTIPLAYER_BATTLE); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.CardMembership,
                    label = "প্রস্তুতি সার্টিফিকেট ও মেডেল",
                    isSelected = currentScreen == AppScreen.CERTIFICATE,
                    onClick = { onNavigate(AppScreen.CERTIFICATE); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.PlayCircle,
                    label = "ভিডিও লেকচার ক্লাস",
                    isSelected = currentScreen == AppScreen.VIDEO_LESSON,
                    onClick = { onNavigate(AppScreen.VIDEO_LESSON); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Audiotrack,
                    label = "অডিও পডকাস্ট রিভিশন",
                    isSelected = currentScreen == AppScreen.AUDIO_PODCASTS,
                    onClick = { onNavigate(AppScreen.AUDIO_PODCASTS); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Schedule,
                    label = "পার্সোনালাইজড স্টাডি প্ল্যানার",
                    isSelected = currentScreen == AppScreen.STUDY_PLANNER,
                    onClick = { onNavigate(AppScreen.STUDY_PLANNER); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Bookmarks,
                    label = "বুকমার্কড গুরুত্বপূর্ণ প্রশ্নসমূহ",
                    isSelected = currentScreen == AppScreen.BOOKMARK_HUB,
                    onClick = { onNavigate(AppScreen.BOOKMARK_HUB); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Description,
                    label = "অধ্যায়ভিত্তিক PDF নোটস ও হ্যান্ডআউট",
                    isSelected = currentScreen == AppScreen.PDF_NOTES,
                    onClick = { onNavigate(AppScreen.PDF_NOTES); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Download,
                    label = "বিগত বছরের সলভড PDF ফাইলসমূহ",
                    isSelected = currentScreen == AppScreen.PYQ_PDF_DOWNLOAD,
                    onClick = { onNavigate(AppScreen.PYQ_PDF_DOWNLOAD); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.HistoryEdu,
                    label = "বিগত বছরের অরিজিনাল PDF ডাউনলোড",
                    isSelected = currentScreen == AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD,
                    onClick = { onNavigate(AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.LibraryBooks,
                    label = "অফিসিয়াল সরকারি পাঠ্যবইসমূহ",
                    isSelected = currentScreen == AppScreen.TEXTBOOKS,
                    onClick = { onNavigate(AppScreen.TEXTBOOKS); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.LiveTv,
                    label = "লাইভ কুইজ ইভেন্ট",
                    isSelected = currentScreen == AppScreen.LIVE_QUIZ,
                    onClick = { onNavigate(AppScreen.LIVE_QUIZ); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Alarm,
                    label = "লাইভ মক টেস্ট ও রাজ্য র‍্যাঙ্কিং",
                    isSelected = currentScreen == AppScreen.LIVE_MOCK_TEST,
                    onClick = { onNavigate(AppScreen.LIVE_MOCK_TEST); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Groups,
                    label = "গ্রুপ স্টাডি স্কোয়াডস",
                    isSelected = currentScreen == AppScreen.STUDY_SQUADS,
                    onClick = { onNavigate(AppScreen.STUDY_SQUADS); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Insights,
                    label = "প্রোগ্রেস অ্যানালিটিক্স ও ড্যাশবোর্ড",
                    isSelected = currentScreen == AppScreen.PROGRESS_DASHBOARD,
                    onClick = { onNavigate(AppScreen.PROGRESS_DASHBOARD); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.AutoGraph,
                    label = "দুর্বল অধ্যায় অ্যানালাইজার",
                    isSelected = currentScreen == AppScreen.WEAKNESS_INSIGHT,
                    onClick = { onNavigate(AppScreen.WEAKNESS_INSIGHT); onCloseDrawer() }
                )

                // Section 3: Profile & Settings
                DrawerSectionHeader(title = "অ্যাকাউন্ট ও সেটিংস")

                DrawerMenuItem(
                    icon = Icons.Default.Person,
                    label = "আমার প্রোফাইল",
                    isSelected = currentScreen == AppScreen.PROFILE,
                    onClick = { onNavigate(AppScreen.PROFILE); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.WorkspacePremium,
                    label = "ভিআইপি মেম্বারশিপ",
                    isSelected = currentScreen == AppScreen.MEMBERSHIP,
                    onClick = { onNavigate(AppScreen.MEMBERSHIP); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "ওয়ালেট ও কয়েন",
                    isSelected = currentScreen == AppScreen.WALLET,
                    onClick = { onNavigate(AppScreen.WALLET); onCloseDrawer() }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    label = "অ্যাপ সেটিংস",
                    isSelected = currentScreen == AppScreen.SETTINGS,
                    onClick = { onNavigate(AppScreen.SETTINGS); onCloseDrawer() }
                )

                if (isAdmin) {
                    DrawerMenuItem(
                        icon = Icons.Default.AdminPanelSettings,
                        label = "ফায়ারবেস অ্যাডমিন",
                        isSelected = currentScreen == AppScreen.ADMIN_PANEL,
                        onClick = { onNavigate(AppScreen.ADMIN_PANEL); onCloseDrawer() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Fixed Bottom Bar containing Logout Icon, Edit Profile Chip, and Admin Chip Button
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
                // Logout Icon Button
                IconButton(
                    onClick = {
                        onSignOut()
                        onCloseDrawer()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Profile Chip Button
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .height(36.dp)
                        .clickable {
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
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isBengali) "প্রোফাইল" else "Profile",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Edit Profile Chip Button
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .height(36.dp)
                        .clickable {
                            onNavigate(AppScreen.EDIT_PROFILE)
                            onCloseDrawer()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isBengali) "এডিট" else "Edit",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Admin Chip Button (Only if isAdmin)
                if (isAdmin) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = ExamGold,
                        modifier = Modifier
                            .height(36.dp)
                            .clickable {
                                onNavigate(AppScreen.ADMIN_PANEL)
                                onCloseDrawer()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = Color(0xFF261400),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isBengali) "অ্যাডমিন" else "Admin",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                fontSize = 11.sp,
                                color = Color(0xFF261400)
                            )
                        }
                    }
                }
            }
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
        icon = { Icon(imageVector = icon, contentDescription = label) },
        label = { Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = PrimaryIndigo.copy(alpha = 0.12f),
            selectedIconColor = PrimaryIndigo,
            selectedTextColor = PrimaryIndigo,
            unselectedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}
