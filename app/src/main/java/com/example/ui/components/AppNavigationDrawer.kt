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

@Composable
fun AppNavigationDrawerSheet(
    currentScreen: AppScreen,
    profile: StudentProfile,
    isBengali: Boolean,
    onNavigate: (AppScreen) -> Unit,
    onCloseDrawer: () -> Unit,
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
            modifier = Modifier
                .fillMaxSize()
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

                            val boardAndClass = when {
                                !profile.targetExam.isBlank() -> {
                                    val cleanExam = profile.targetExam.replace("WBBSE", "পশ্চিমবঙ্গ পর্ষদ").replace("CBSE", "সিবিএসই")
                                    "$cleanExam • ${profile.classLevel}"
                                }
                                else -> if (isBengali) "পশ্চিমবঙ্গ পর্ষদ • দশম শ্রেণি" else "WBBSE • Class 10"
                            }

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

                        // Top-right Action Buttons: Admin chip (if isAdmin), Edit Profile icon, Close icon
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAdmin) {
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = ExamGold,
                                    modifier = Modifier.clickable {
                                        onNavigate(AppScreen.ADMIN_PANEL)
                                        onCloseDrawer()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = "Admin",
                                            tint = Color(0xFF261400),
                                            modifier = Modifier.size(13.dp)
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

                            IconButton(
                                onClick = {
                                    onNavigate(AppScreen.EDIT_PROFILE)
                                    onCloseDrawer()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

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
                    }

                    // Badges / Action Chips (Bottom Row): side-by-side pill-shaped chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Coins Chip (Light cream/gold background with coin icon, count text, and chevron right arrow >)
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            onClick = { /* Action */ }
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "🪙", fontSize = 13.sp)
                                    Text(
                                        text = "${profile.coins} ${if (isBengali) "কয়েন" else "Coins"}",
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
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // XP Chip (Matching translucent/soft-colored pill with bolt/thunder icon, XP text, and chevron right arrow >)
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            onClick = { /* Action */ }
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "⚡", fontSize = 13.sp)
                                    Text(
                                        text = "${profile.currentXp} XP",
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
                                    modifier = Modifier.size(16.dp)
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
                label = "লার্নিং রোডম্যাপ",
                isSelected = currentScreen == AppScreen.JOURNEY,
                onClick = { onNavigate(AppScreen.JOURNEY); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.Quiz,
                label = "মক টেস্ট হাব",
                isSelected = currentScreen == AppScreen.MOCK,
                onClick = { onNavigate(AppScreen.MOCK); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.Leaderboard,
                label = "মেধা তালিকা",
                isSelected = currentScreen == AppScreen.LEADERBOARD,
                onClick = { onNavigate(AppScreen.LEADERBOARD); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.Psychology,
                label = "এআই স্টাডি কোচ",
                isSelected = currentScreen == AppScreen.AI_COACH,
                onClick = { onNavigate(AppScreen.AI_COACH); onCloseDrawer() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section 2: Special Exam Tools
            DrawerSectionHeader(title = "বিশেষ পরীক্ষার টুলস")

            DrawerMenuItem(
                icon = Icons.Default.Style,
                label = "স্মার্ট ফ্ল্যাশকার্ড",
                isSelected = currentScreen == AppScreen.FLASHCARDS,
                onClick = { onNavigate(AppScreen.FLASHCARDS); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.HistoryEdu,
                label = "বিগত ১০ বছরের প্রশ্নপত্র",
                isSelected = currentScreen == AppScreen.PYQ_EXPLORER,
                onClick = { onNavigate(AppScreen.PYQ_EXPLORER); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.DocumentScanner,
                label = "OMR শিট সিমুলেটর",
                isSelected = currentScreen == AppScreen.OMR_PRACTICE,
                onClick = { onNavigate(AppScreen.OMR_PRACTICE); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.MilitaryTech,
                label = "টপার্স খাতা গাইড",
                isSelected = currentScreen == AppScreen.TOPPERS_GUIDE,
                onClick = { onNavigate(AppScreen.TOPPERS_GUIDE); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.CameraAlt,
                label = "স্ন্যাপ ডাউট সলভার",
                isSelected = currentScreen == AppScreen.DOUBT_SOLVER,
                onClick = { onNavigate(AppScreen.DOUBT_SOLVER); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.SportsEsports,
                label = "১v১ কুইজ ব্যাটেল",
                isSelected = currentScreen == AppScreen.MULTIPLAYER_BATTLE,
                onClick = { onNavigate(AppScreen.MULTIPLAYER_BATTLE); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.WorkspacePremium,
                label = "মেরিট সার্টিফিকেট",
                isSelected = currentScreen == AppScreen.CERTIFICATE,
                onClick = { onNavigate(AppScreen.CERTIFICATE); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.Headphones,
                label = "অডিও পডকাস্ট",
                isSelected = currentScreen == AppScreen.AUDIO_PODCASTS,
                onClick = { onNavigate(AppScreen.AUDIO_PODCASTS); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.AutoMirrored.Filled.EventNote,
                label = "স্টাডি প্ল্যানার",
                isSelected = currentScreen == AppScreen.STUDY_PLANNER,
                onClick = { onNavigate(AppScreen.STUDY_PLANNER); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.Bookmark,
                label = "বুকমার্ক হাব",
                isSelected = currentScreen == AppScreen.BOOKMARK_HUB,
                onClick = { onNavigate(AppScreen.BOOKMARK_HUB); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.PictureAsPdf,
                label = "পিডিএফ নোটস",
                isSelected = currentScreen == AppScreen.PDF_NOTES,
                onClick = { onNavigate(AppScreen.PDF_NOTES); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.FileDownload,
                label = "সমাধানসহ প্রশ্নপত্র PDF",
                isSelected = currentScreen == AppScreen.PYQ_PDF_DOWNLOAD,
                onClick = { onNavigate(AppScreen.PYQ_PDF_DOWNLOAD); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.PictureAsPdf,
                label = "আসল প্রশ্নপত্র PDF",
                isSelected = currentScreen == AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD,
                onClick = { onNavigate(AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.AutoStories,
                label = "পাঠ্যপুস্তক PDF",
                isSelected = currentScreen == AppScreen.TEXTBOOKS,
                onClick = { onNavigate(AppScreen.TEXTBOOKS); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.Groups,
                label = "লাইভ কুইজ",
                isSelected = currentScreen == AppScreen.LIVE_QUIZ,
                onClick = { onNavigate(AppScreen.LIVE_QUIZ); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.Timer,
                label = "লাইভ মক টেস্ট",
                isSelected = currentScreen == AppScreen.LIVE_MOCK_TEST,
                onClick = { onNavigate(AppScreen.LIVE_MOCK_TEST); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.Default.GroupWork,
                label = "স্কুল স্কোয়াড",
                isSelected = currentScreen == AppScreen.STUDY_SQUADS,
                onClick = { onNavigate(AppScreen.STUDY_SQUADS); onCloseDrawer() }
            )

            DrawerMenuItem(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "প্রোগ্রেস গ্রাফ",
                isSelected = currentScreen == AppScreen.PROGRESS_DASHBOARD,
                onClick = { onNavigate(AppScreen.PROGRESS_DASHBOARD); onCloseDrawer() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section 3: Profile & Settings
            DrawerSectionHeader(title = "অ্যাকাউন্ট ও সেটিংস")

            DrawerMenuItem(
                icon = Icons.Default.Person,
                label = "আমার প্রোফাইল",
                isSelected = currentScreen == AppScreen.PROFILE,
                onClick = { onNavigate(AppScreen.PROFILE); onCloseDrawer() }
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
