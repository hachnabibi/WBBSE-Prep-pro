package com.example.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentProfile
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
    isAdmin: Boolean = false
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
                    .background(PrimaryIndigo)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "👨‍🎓",
                                    fontSize = 26.sp
                                )
                            }
                        }

                        IconButton(onClick = onCloseDrawer) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Column {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "${profile.school} • ${profile.classLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ExamGold
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "🪙", fontSize = 11.sp)
                                Text(
                                    text = "${profile.coins} ${"কয়েন"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF261400)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "⚡ ${profile.currentXp} XP",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
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
