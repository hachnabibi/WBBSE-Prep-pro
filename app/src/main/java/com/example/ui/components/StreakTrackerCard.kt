package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StreakDay
import com.example.data.model.StreakMilestone
import com.example.data.model.StudentProfile
import com.example.ui.theme.*

fun toBengaliDigits(value: Int): String {
    val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return value.toString().map { char ->
        if (char in '0'..'9') bengaliDigits[char - '0'] else char
    }.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakTrackerCard(
    profile: StudentProfile,
    isBengali: Boolean,
    onPracticeNow: () -> Unit,
    onRepairStreak: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDetailSheet by remember { mutableStateOf(false) }

    // Pulsing flame animation for active streak
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    // Standard 7-day week reflecting the user's current streak
    val weekDays = remember(profile.currentStreak) {
        val daysBengali = listOf("সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি", "রবি")
        val daysEnglish = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val questionsList = listOf(16, 20, 14, 22, 18, 15, 25)
        val minutesList = listOf(25, 30, 20, 35, 28, 25, 40)

        // When streak is 7 or more, all 7 days of the streak cycle are lit with active flames!
        val completedCount = profile.currentStreak.coerceIn(1, 7)
        val todayIndex = (completedCount - 1).coerceIn(0, 6)

        daysBengali.mapIndexed { index, bnName ->
            val isCompleted = index < completedCount
            val isToday = index == todayIndex
            StreakDay(
                dayOfWeekBengali = bnName,
                dayOfWeekEnglish = daysEnglish[index],
                dayNumber = 15 + index,
                isCompleted = isCompleted,
                isToday = isToday,
                questionsAnswered = if (isCompleted) questionsList[index] else 0,
                minutesSpent = if (isCompleted) minutesList[index] else 0
            )
        }
    }

    val nextMilestoneTarget = 10
    val currentStreak = profile.currentStreak
    val progressToMilestone = (currentStreak.toFloat() / nextMilestoneTarget).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDetailSheet = true }
            .testTag("streak_tracker_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Fire Flame + Streak Title & Subtitle + Right Badges (Shield & XP multiplier)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left & Middle: Fire Icon + Clean Title Column
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        ExamFlame.copy(alpha = 0.35f),
                                        ExamFlame.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .border(1.5.dp, ExamFlame.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak Flame",
                            tint = ExamFlame,
                            modifier = Modifier
                                .size(26.dp)
                                .scale(flameScale)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "${toBengaliDigits(profile.currentStreak)} দিনের লার্নিং স্ট্রিক!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "টানা চর্চায় মাধ্যমিক ২০২৬ প্রস্তুতি মজবুত হচ্ছে",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Two neatly stacked badges: Streak Shield + XP Multiplier
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.testTag("streak_shield_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Streak Shield",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "শিল্ড: ২",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ExamGold.copy(alpha = 0.2f),
                        modifier = Modifier.testTag("streak_xp_multiplier_badge")
                    ) {
                        Text(
                            text = "⚡ ১.৫x XP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 7-Day Day-by-Day Strip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekDays.forEach { day ->
                        DayStreakNode(
                            day = day,
                            isBengali = isBengali
                        )
                    }
                }
            }

            // Milestone Target Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val remainingDays = (nextMilestoneTarget - currentStreak).coerceAtLeast(0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "পরবর্তী মাইলস্টোন: ১০ দিন (বাকি ${toBengaliDigits(remainingDays)} দিন)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+৩০০ XP • ফায়ার স্কলার 🏅",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = ExamFlame
                    )
                }

                LinearProgressIndicator(
                    progress = { progressToMilestone },
                    color = ExamFlame,
                    trackColor = ExamFlame.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            // Madhyamik Motivation & Quick Extension Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = ExamGold.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Today Completed",
                        tint = CorrectGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "আজকের স্ট্রিক নিরাপদ! আরও চর্চায় এগিয়ে থাকো",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = onPracticeNow,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.testTag("streak_practice_cta_btn")
                ) {
                    Text(
                        text = "চর্চা করো",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ExamGold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(14.dp),
                        tint = ExamGold
                    )
                }
            }
        }
    }

    // Expanded Detailed Streak Hub Bottom Sheet
    if (showDetailSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.testTag("streak_details_bottom_sheet")
        ) {
            StreakDetailsContent(
                profile = profile,
                weekDays = weekDays,
                isBengali = isBengali,
                onDismiss = { showDetailSheet = false },
                onPracticeNow = {
                    showDetailSheet = false
                    onPracticeNow()
                },
                onRepairStreak = onRepairStreak
            )
        }
    }
}

@Composable
private fun DayStreakNode(
    day: StreakDay,
    isBengali: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Day name (e.g. সোম / Mon)
        Text(
            text = if (isBengali) day.dayOfWeekBengali else day.dayOfWeekEnglish,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (day.isToday) FontWeight.Black else FontWeight.Medium
            ),
            color = if (day.isToday) ExamFlame else MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Circle node
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        day.isCompleted -> ExamFlame
                        day.isToday -> ExamFlame.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .then(
                    if (day.isToday) {
                        Modifier.border(2.dp, ExamFlame, CircleShape)
                    } else if (day.isCompleted) {
                        Modifier.border(1.dp, ExamGold, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (day.isCompleted) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else if (day.isToday) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Today",
                    tint = ExamFlame,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = if (isBengali) toBengaliDigits(day.dayNumber) else "${day.dayNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        }

        // Questions / Status label
        Text(
            text = when {
                day.isCompleted -> "${toBengaliDigits(day.questionsAnswered)}টি"
                day.isToday -> "আজকে"
                else -> "বাকি"
            },
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = if (day.isCompleted || day.isToday) FontWeight.Bold else FontWeight.Normal
            ),
            color = when {
                day.isCompleted -> CorrectGreen
                day.isToday -> ExamFlame
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }
        )
    }
}

@Composable
private fun StreakDetailsContent(
    profile: StudentProfile,
    weekDays: List<StreakDay>,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onPracticeNow: () -> Unit,
    onRepairStreak: () -> Unit = {}
) {
    var showStreakAdDialog by remember { mutableStateOf(false) }
    val milestones = remember {
        listOf(
            StreakMilestone(dayRequirement = 3, title = "স্পার্ক লার্নার", xpBonus = 100, badgeTitle = "স্পার্ক পদক", isClaimed = true),
            StreakMilestone(dayRequirement = 7, title = "ফ্লেম স্কলার", xpBonus = 250, badgeTitle = "অগ্নি মুকুট", isClaimed = true),
            StreakMilestone(dayRequirement = 14, title = "অগ্নি যোদ্ধা", xpBonus = 500, badgeTitle = "লিজেন্ডারি শিল্ড", isClaimed = false),
            StreakMilestone(dayRequirement = 30, title = "মাধ্যমিক লিজেন্ড", xpBonus = 1000, badgeTitle = "গোল্ডেন স্টার ফ্রেম", isClaimed = false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "দৈনিক লার্নিং স্ট্রিক হাব",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ধারাবাহিক পড়াশোনাই মাধ্যমিকের সফলতার মূল ভিত্তি",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Stats Matrix Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StreakStatCard(
                title = "বর্তমান স্ট্রিক",
                value = "${if (isBengali) toBengaliDigits(profile.currentStreak) else profile.currentStreak} " + "দিন",
                icon = Icons.Default.LocalFireDepartment,
                iconColor = ExamFlame,
                modifier = Modifier.weight(1f)
            )
            StreakStatCard(
                title = "সেরা রেকর্ড",
                value = "${if (isBengali) toBengaliDigits(profile.bestStreak) else profile.bestStreak} " + "দিন",
                icon = Icons.Default.EmojiEvents,
                iconColor = ExamGold,
                modifier = Modifier.weight(1f)
            )
            StreakStatCard(
                title = "মোট স্টাডি দিন",
                value = "${if (isBengali) toBengaliDigits(profile.totalStudyDays) else profile.totalStudyDays} " + "দিন",
                icon = Icons.Default.CalendarMonth,
                iconColor = PrimaryIndigoLight,
                modifier = Modifier.weight(1f)
            )
        }

        // Streak Shield Feature Section
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigoLight.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = PrimaryIndigoLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "স্ট্রিক শিল্ড সুরক্ষা (২টি উপলব্ধ)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "স্কুল পরীক্ষা বা অসুস্থতায় কোনোদিন টেস্ট না দিতে পারলে স্ট্রিক ভাঙবে না।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Streak Recovery / Repair Rewarded Ad Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1B4B),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ExamGold.copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth().testTag("streak_repair_card")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ExamGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = ExamGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "স্ট্রিক মেরামত ও ফ্রিজ 🛡️",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "ভিডিও স্পনসর দেখে মিসড দিনের স্ট্রিক মেরামত করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Button(
                    onClick = { showStreakAdDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("repair_streak_btn")
                ) {
                    Text(
                        text = "রিপেয়ার",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }
            }
        }

        if (showStreakAdDialog) {
            RewardedAdDialog(
                rewardType = AdRewardType.STREAK_REPAIR,
                isBengali = isBengali,
                onRewardEarned = {
                    onRepairStreak()
                    showStreakAdDialog = false
                },
                onDismiss = { showStreakAdDialog = false }
            )
        }

        // Milestone Progression List
        Text(
            text = "স্ট্রিক মাইলস্টোন ও রিওয়ার্ডস",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            milestones.forEach { milestone ->
                MilestoneItemRow(
                    milestone = milestone,
                    currentStreak = profile.currentStreak,
                    isBengali = isBengali
                )
            }
        }

        // 4-Week Mini Consistency Heatmap
        Text(
            text = "সাম্প্রতিক ৪ সপ্তাহের স্টাডি কনসিস্টেন্সি",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        MiniConsistencyHeatmap()

        // CTA Button
        Button(
            onClick = onPracticeNow,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ExamFlame,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("streak_sheet_practice_btn")
        ) {
            Icon(imageVector = Icons.Default.Bolt, contentDescription = "Practice")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "আজকের প্র্যাকটিস চালিয়ে যাও (+১৫০ XP)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun StreakStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MilestoneItemRow(
    milestone: StreakMilestone,
    currentStreak: Int,
    isBengali: Boolean
) {
    val isAchieved = currentStreak >= milestone.dayRequirement

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isAchieved) ExamGold.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = if (isAchieved) androidx.compose.foundation.BorderStroke(1.dp, ExamGold.copy(alpha = 0.5f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isAchieved) ExamGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAchieved) Icons.Default.Check else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isAchieved) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "${milestone.dayRequirement} " + ("দিন: ") +
                                (milestone.title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+${milestone.xpBonus} XP • ${milestone.badgeTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isAchieved) ExamFlame else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isAchieved) CorrectGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = if (isAchieved) {
                        "অর্জিত ✓"
                    } else {
                        "${currentStreak}/${milestone.dayRequirement}"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isAchieved) CorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniConsistencyHeatmap() {
    // 4 rows x 7 columns grid representing 28 days of study consistency
    val activeDays = remember {
        setOf(0, 1, 2, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (row in 0 until 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 7) {
                        val dayIndex = (row * 7) + col
                        val isActive = dayIndex in activeDays
                        val isToday = dayIndex == 26 // 27th day

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isActive -> ExamFlame.copy(alpha = 0.85f)
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    }
                                )
                                .then(
                                    if (isToday) Modifier.border(1.5.dp, ExamGold, RoundedCornerShape(4.dp)) else Modifier
                                )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "৪ সপ্তাহ আগে",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "আজ (৯২% ধারাবাহিকতা)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = CorrectGreen
                )
            }
        }
    }
}
