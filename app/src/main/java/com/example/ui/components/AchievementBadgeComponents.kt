package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AssessmentMode
import com.example.data.model.BadgeCategory
import com.example.data.model.BadgeItem
import com.example.data.model.BadgeTier
import com.example.ui.theme.*

/**
 * Returns an appropriate ImageVector for a given badge icon name
 */
fun getBadgeVector(iconName: String): ImageVector {
    return when (iconName) {
        "CheckCircle" -> Icons.Default.CheckCircle
        "WbSunny" -> Icons.Default.WbSunny
        "Bedtime" -> Icons.Default.Bedtime
        "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
        "Bolt" -> Icons.Default.Bolt
        "Shield" -> Icons.Default.Shield
        "WorkspacePremium" -> Icons.Default.WorkspacePremium
        "MilitaryTech" -> Icons.Default.MilitaryTech
        "AutoAwesome" -> Icons.Default.AutoAwesome
        "EmojiEvents" -> Icons.Default.EmojiEvents
        "Star" -> Icons.Default.Star
        else -> Icons.Default.EmojiEvents
    }
}

/**
 * Get tier-specific visual properties
 */
fun getTierColor(tier: BadgeTier): Color {
    return when (tier) {
        BadgeTier.DIAMOND -> Color(0xFF00E5FF)
        BadgeTier.GOLD -> ExamGold
        BadgeTier.SILVER -> Color(0xFF94A3B8)
        BadgeTier.BRONZE -> Color(0xFFCD7F32)
    }
}

fun getTierBackgroundGradient(tier: BadgeTier, isUnlocked: Boolean): Brush {
    if (!isUnlocked) {
        return Brush.linearGradient(
            listOf(
                Color(0xFF334155).copy(alpha = 0.25f),
                Color(0xFF1E293B).copy(alpha = 0.15f)
            )
        )
    }
    return when (tier) {
        BadgeTier.DIAMOND -> Brush.linearGradient(
            listOf(
                Color(0xFFE0F7FA),
                Color(0xFF80DEEA),
                Color(0xFF26C6DA)
            )
        )
        BadgeTier.GOLD -> Brush.linearGradient(
            listOf(
                Color(0xFFFFFBEB),
                Color(0xFFFDE68A),
                ExamGold
            )
        )
        BadgeTier.SILVER -> Brush.linearGradient(
            listOf(
                Color(0xFFF8FAFC),
                Color(0xFFE2E8F0),
                Color(0xFFCBD5E1)
            )
        )
        BadgeTier.BRONZE -> Brush.linearGradient(
            listOf(
                Color(0xFFFFF7ED),
                Color(0xFFFFEDD5),
                Color(0xFFFDBA74)
            )
        )
    }
}

fun getTierName(tier: BadgeTier, isBengali: Boolean): String {
    return when (tier) {
        BadgeTier.DIAMOND -> "ডায়মন্ড"
        BadgeTier.GOLD -> "গোল্ড"
        BadgeTier.SILVER -> "সিলভার"
        BadgeTier.BRONZE -> "ব্রোঞ্জ"
    }
}

fun getCategoryName(category: BadgeCategory, isBengali: Boolean): String {
    return when (category) {
        BadgeCategory.ALL -> "সকল"
        BadgeCategory.ACCURACY -> "নির্ভুলতা"
        BadgeCategory.CONSISTENCY -> "ধারাবাহিকতা"
        BadgeCategory.TIME -> "সময় সাধনা"
        BadgeCategory.MASTERY -> "অধ্যায় মাস্টারি"
        BadgeCategory.EXAM -> "পরীক্ষা চ্যাম্পিয়ন"
    }
}

/**
 * Top Unlocked Badges Row for Profile Header
 */
@Composable
fun FeaturedBadgesRow(
    badges: List<BadgeItem>,
    isBengali: Boolean,
    onBadgeClick: (BadgeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val earnedBadges = badges.filter { it.isUnlocked }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = ExamGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "শীর্ষ অর্জিত মেডেলসমূহ",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "${earnedBadges.size} / ${badges.size}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = ExamGold
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(earnedBadges.take(6)) { badge ->
                FeaturedBadgeMiniCard(
                    badge = badge,
                    isBengali = isBengali,
                    onClick = { onBadgeClick(badge) }
                )
            }
        }
    }
}

@Composable
fun FeaturedBadgeMiniCard(
    badge: BadgeItem,
    isBengali: Boolean,
    onClick: () -> Unit
) {
    val tierColor = getTierColor(badge.tier)
    val icon = getBadgeVector(badge.iconName)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("mini_badge_${badge.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(getTierBackgroundGradient(badge.tier, true)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = badge.name,
                    tint = tierColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = if (isBengali) badge.name.split("(").first().trim() else badge.name,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "+${badge.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                    color = ExamGold
                )
            }
        }
    }
}

/**
 * Rich Card for the Full Badges List / Trophy Cabinet
 */
@Composable
fun AchievementBadgeCard(
    badge: BadgeItem,
    isBengali: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tierColor = getTierColor(badge.tier)
    val icon = getBadgeVector(badge.iconName)
    val progressFraction = (badge.currentProgress.toFloat() / badge.maxProgress.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            1.2.dp,
            if (badge.isUnlocked) tierColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (badge.isUnlocked) 2.dp else 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("badge_card_${badge.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tier Emblem & Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(getTierBackgroundGradient(badge.tier, badge.isUnlocked))
                        .border(
                            2.dp,
                            if (badge.isUnlocked) tierColor else Color.Gray.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (badge.isUnlocked) icon else Icons.Default.Lock,
                        contentDescription = badge.name,
                        tint = if (badge.isUnlocked) tierColor else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Tier / Category Chips
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = tierColor.copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, tierColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = getTierName(badge.tier, isBengali),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = tierColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryIndigo.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = getCategoryName(badge.category, isBengali),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 9.sp),
                                color = PrimaryIndigo,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Unlocked status / XP reward badge
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (badge.isUnlocked) CorrectGreen.copy(alpha = 0.15f) else ExamGold.copy(alpha = 0.12f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            if (badge.isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CorrectGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "অর্জিত",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CorrectGreen
                                )
                            } else {
                                Text(
                                    text = "+${badge.xpReward} XP",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ExamGold
                                )
                            }
                        }
                    }

                    if (badge.isUnlocked && badge.unlockedDate != null) {
                        Text(
                            text = badge.unlockedDate,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Milestone progress bar (if locked or multi-step)
            if (!badge.isUnlocked || badge.maxProgress > 1) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "মাইলস্টোন শর্ত: ${badge.milestoneRequirementBengali}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${badge.currentProgress}/${badge.maxProgress}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = if (badge.isUnlocked) CorrectGreen else tierColor
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = if (badge.isUnlocked) CorrectGreen else tierColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

/**
 * Detailed Modal Dialog for an Achievement Badge
 */
@Composable
fun AchievementBadgeDetailDialog(
    badge: BadgeItem,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onStartPractice: (AssessmentMode) -> Unit = {}
) {
    val tierColor = getTierColor(badge.tier)
    val icon = getBadgeVector(badge.iconName)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("badge_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Tier and Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tierColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${getTierName(badge.tier, isBengali)} • ${getCategoryName(badge.category, isBengali)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Giant Animated Badge Icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(getTierBackgroundGradient(badge.tier, badge.isUnlocked))
                        .border(
                            3.5.dp,
                            if (badge.isUnlocked) tierColor else Color.Gray.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (badge.isUnlocked) icon else Icons.Default.Lock,
                        contentDescription = badge.name,
                        tint = if (badge.isUnlocked) tierColor else Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                }

                // Badge Name & Description
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Milestone Requirement Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "মাইলস্টোন শর্ত:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ExamGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "+${badge.xpReward} XP বোনাস",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ExamGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isBengali) badge.milestoneRequirementBengali else badge.milestoneRequirementEnglish.ifBlank { badge.milestoneRequirementBengali },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val progressFraction = (badge.currentProgress.toFloat() / badge.maxProgress.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (badge.isUnlocked) {
                                    "✅ শর্ত পূরণ সম্পন্ন"
                                } else {
                                    "অগ্রগতি: ${badge.currentProgress}/${badge.maxProgress}"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (badge.isUnlocked) CorrectGreen else tierColor
                            )

                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (badge.isUnlocked) CorrectGreen else tierColor
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            color = if (badge.isUnlocked) CorrectGreen else tierColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }

                // Unlock Status / Action Button
                if (badge.isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CorrectGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CorrectGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "এই মেডেলটি ${badge.unlockedDate ?: "ইতিমধ্যে"} আপনার সংগ্রহে যুক্ত হয়েছে!" else "Earned & added to your trophy cabinet!",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = CorrectGreen,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            text = "ঠিক আছে",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            onDismiss()
                            when (badge.id) {
                                "badge_speed_demon" -> onStartPractice(AssessmentMode.SPEED_BLITZ)
                                "badge_boss_slayer" -> onStartPractice(AssessmentMode.BOSS_BATTLE)
                                "badge_madhyamik_ready" -> onStartPractice(AssessmentMode.MOCK_TEST)
                                else -> onStartPractice(AssessmentMode.PRACTICE)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "মেডেল অর্জনে কুইজ শুরু করুন",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Achievement Unlock Celebration Dialog (Award Pop-up)
 */
@Composable
fun AchievementUnlockedCelebrationDialog(
    badge: BadgeItem,
    isBengali: Boolean,
    onDismiss: () -> Unit
) {
    val tierColor = getTierColor(badge.tier)
    val icon = getBadgeVector(badge.iconName)

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("achievement_celebration_dialog")
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background decorative glow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    tierColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExamGold,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "নতুন মেডেল অর্জিত হয়েছে!",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = Color.Black
                            )
                        }
                    }

                    // Pulsing Glowing Badge Emblem
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(getTierBackgroundGradient(badge.tier, true))
                            .border(4.dp, tierColor, CircleShape)
                            .shadow(8.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = badge.name,
                            tint = tierColor,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    // Name & Description
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = badge.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = badge.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    // XP Bonus Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ExamGold.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = ExamGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+${badge.xpReward} XP বোনাস রিওয়ার্ড!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    // Claim Button
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("claim_achievement_button")
                    ) {
                        Text(
                            text = "সংগ্রহ করুন ও চালিয়ে যান",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
