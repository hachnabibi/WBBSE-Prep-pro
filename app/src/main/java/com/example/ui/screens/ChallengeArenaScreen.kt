package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssessmentMode
import com.example.data.model.FriendChallengeItem
import com.example.data.model.LeaderboardStudent
import com.example.ui.components.BossBattleCard
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel

@Composable
fun ChallengeArenaScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onStartChallenge: (AssessmentMode, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val leaderboard = remember { viewModel.repository.getLeaderboard() }
    val friendChallenges = remember { viewModel.repository.getFriendChallenges() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("challenge_arena_screen")
    ) {
        ScreenHeaderBar(
            title = "চ্যালেঞ্জ অ্যারেনা",
            subtitle = "১v১ লাইভ কুইজ যুদ্ধ ও ফ্রেন্ড চ্যালেঞ্জ",
            isBengali = isBengali,
            showBackButton = true,
            onBack = { viewModel.goBack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // 1. Hero 1v1 Battle Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF4C1D95), Color(0xFF6D28D9), Color(0xFF8B5CF6))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "⚔️ লাইভ ফ্রেন্ড যুদ্ধ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "৫টি প্রশ্ন • স্পিড রাউন্ড",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Text(
                            text = "বন্ধুদের চ্যালেঞ্জ জানিয়ে প্র্যাকটিস করো",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )

                        Text(
                            text = "একই প্রশ্নে একই সাথে উত্তর দাও। যে দ্রুত সঠিক উত্তর দেবে সে অতিরিক্ত স্পিড বোনাস পাবে!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Button(
                            onClick = {
                                viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.MULTIPLAYER_BATTLE)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ExamGold,
                                contentColor = Color(0xFF78350F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_1v1_battle_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsKabaddi,
                                contentDescription = "Battle",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "কাস্টম ১v১ চ্যালেঞ্জ সেটআপ (+১৮০ XP)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // 2. Segmented Tabs: Boss Battles vs 1v1 Battles vs State Leaderboard
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "🔥 বস ব্যাটল",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "⚔️ ফ্রেন্ডস (${friendChallenges.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "🏆 লিডারবোর্ড",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
        }

        // 3. Tab Content
        if (selectedTab == 0) {
            // Boss Battle Gamified Challenges with Health Bars and Progress Indicators
            item {
                Text(
                    text = "অধ্যায় বস দুর্গসমূহ (Chapter Boss Citadels)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                BossBattleCard(
                    bossName = "বায়ুমণ্ডলীয় দানব বস (Tier 3)",
                    chapterTitle = "ভৌত বিজ্ঞান • পরিবেশের জন্য ভাবনা",
                    bossMaxHp = 1000,
                    bossCurrentHp = 1000,
                    playerMaxHp = 100,
                    playerCurrentHp = 100,
                    questionsCount = 8,
                    questionsCompleted = 0,
                    isBengali = isBengali,
                    isDefeated = false,
                    onStartBattle = {
                        onStartChallenge(
                            AssessmentMode.BOSS_BATTLE,
                            "phy_ch1",
                            "🔥 অধ্যায় বস ব্যাটল: পরিবেশের জন্য ভাবনা"
                        )
                    }
                )
            }

            item {
                BossBattleCard(
                    bossName = "অম্ল ও ক্ষারক সর্দার ড্রাগন (Tier 4)",
                    chapterTitle = "ভৌত বিজ্ঞান • রাসায়নিক গণনা ও অম্ল",
                    bossMaxHp = 1200,
                    bossCurrentHp = 1200,
                    playerMaxHp = 100,
                    playerCurrentHp = 100,
                    questionsCount = 10,
                    questionsCompleted = 0,
                    isBengali = isBengali,
                    isDefeated = false,
                    onStartBattle = {
                        onStartChallenge(
                            AssessmentMode.BOSS_BATTLE,
                            "phy_ch2",
                            "🔥 অধ্যায় বস ব্যাটল: রাসায়নিক গণনা"
                        )
                    }
                )
            }

            item {
                BossBattleCard(
                    bossName = "জীবজগতের নিয়ন্ত্রণ অধিপতি",
                    chapterTitle = "জীবন বিজ্ঞান • জীবজগতের নিয়ন্ত্রণ",
                    bossMaxHp = 1000,
                    bossCurrentHp = 0,
                    playerMaxHp = 100,
                    playerCurrentHp = 100,
                    questionsCount = 8,
                    questionsCompleted = 8,
                    isBengali = isBengali,
                    isDefeated = true,
                    onStartBattle = {
                        onStartChallenge(
                            AssessmentMode.BOSS_BATTLE,
                            "bio_ch1",
                            "🔥 অধ্যায় বস ব্যাটল: জীবজগতের নিয়ন্ত্রণ"
                        )
                    }
                )
            }
        } else if (selectedTab == 1) {
            // Friend Challenges List
            items(friendChallenges) { challenge ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = challenge.opponentName.take(1),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryIndigo
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = challenge.opponentName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${challenge.opponentSchool} • ${challenge.subjectName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (challenge.status) {
                                "Won" -> CorrectGreen.copy(alpha = 0.15f)
                                "Lost" -> WrongRed.copy(alpha = 0.15f)
                                else -> ExamGold.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = when (challenge.status) {
                                    "Won" -> "বিজয়ী ${challenge.myScore}-${challenge.opponentScore}"
                                    "Lost" -> "পরাজিত ${challenge.myScore}-${challenge.opponentScore}"
                                    else -> "অপেক্ষমাণ"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = when (challenge.status) {
                                    "Won" -> CorrectGreen
                                    "Lost" -> WrongRed
                                    else -> Color(0xFFB45309)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Leaderboard List
            items(leaderboard) { student ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (student.isCurrentUser) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (student.isCurrentUser) 4.dp else 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank Badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (student.rank) {
                                        1 -> ExamGold
                                        2 -> Color(0xFF94A3B8)
                                        3 -> Color(0xFFB45309)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${student.rank}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (student.rank <= 3) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (student.isCurrentUser) FontWeight.Black else FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = student.school,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${student.xp} XP",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryIndigo
                            )
                            Text(
                                text = "${student.accuracy}% নির্ভুল",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
}
