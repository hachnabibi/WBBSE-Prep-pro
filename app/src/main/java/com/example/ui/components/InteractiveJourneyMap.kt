package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import kotlin.math.sin

/**
 * Interactive Journey Map Component:
 * Displays student learning progress as a winding adventure path with unlockable nodes,
 * star ratings, milestone waypoints, interactive node dialogs, and chapter boss citadel terminus.
 */
@Composable
fun InteractiveJourneyMap(
    chapter: ChapterItem,
    topics: List<TopicItem>,
    isBengali: Boolean,
    onOpenStudy: (String) -> Unit,
    onStartPractice: (String, String) -> Unit,
    onStartBossBattle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedNodeForModal by remember { mutableStateOf<TopicItem?>(null) }
    var lockedNodeNotice by remember { mutableStateOf<String?>(null) }
    var showConfettiCelebration by remember { mutableStateOf(chapter.bossCompleted) }

    // Calculate total stars collected across all topics (max 3 per topic)
    val totalStarsPossible = topics.size * 3
    val totalStarsEarned = topics.sumOf { topic ->
        when {
            topic.masteryPercentage >= 80 -> 3
            topic.masteryPercentage >= 50 -> 2
            topic.masteryPercentage > 0 -> 1
            else -> 0
        }
    }
    val unlockedCount = topics.count { it.isUnlocked }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("interactive_journey_map"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // 1. Adventure Path Exploration HUD
        AdventurePathHud(
            chapter = chapter,
            starsEarned = totalStarsEarned,
            totalStars = totalStarsPossible,
            unlockedNodes = unlockedCount,
            totalNodes = topics.size,
            isBengali = isBengali
        )

        // 2. Main Winding Adventure Path Trail
        val isDark = MaterialTheme.colorScheme.background.red < 0.3f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.surface
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(vertical = 20.dp, horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Departure / Expedition Start Flag
                ExpeditionStartBanner(isBengali = isBengali)

                // Render each unlockable topic node in alternating S-curve adventure positions
                topics.forEachIndexed { index, topic ->
                    val isNextActiveNode = topic.isUnlocked && topic.masteryPercentage < 80 &&
                            topics.take(index).all { it.masteryPercentage >= 80 }
                    val horizontalBias = when (index % 3) {
                        0 -> 0.25f // Left bias
                        1 -> 0.75f // Right bias
                        else -> 0.50f // Center bias
                    }

                    AdventureNodeRow(
                        topic = topic,
                        index = index + 1,
                        horizontalBias = horizontalBias,
                        isNextActive = isNextActiveNode,
                        isBengali = isBengali,
                        onClick = {
                            if (topic.isUnlocked) {
                                selectedNodeForModal = topic
                            } else {
                                lockedNodeNotice = if (isBengali) {
                                    "🔒 নোড ${topic.orderIndex} লক করা আছে! আগের নোডটি অন্তত ৬০% নম্বরে সম্পূর্ণ করুন।"
                                } else {
                                    "🔒 Node ${topic.orderIndex} is locked! Complete preceding node to unlock."
                                }
                            }
                        }
                    )

                    // Connecting Trail Section towards next node or Boss Gate
                    if (index < topics.size - 1) {
                        val nextTopic = topics[index + 1]
                        val isPathActive = topic.isUnlocked && nextTopic.isUnlocked
                        ConnectingTrailSegment(
                            fromBias = horizontalBias,
                            toBias = when ((index + 1) % 3) {
                                0 -> 0.25f
                                1 -> 0.75f
                                else -> 0.50f
                            },
                            isActive = isPathActive
                        )
                    } else {
                        // Connector to Boss Citadel
                        ConnectingTrailSegment(
                            fromBias = horizontalBias,
                            toBias = 0.5f,
                            isActive = chapter.bossUnlocked
                        )
                    }
                }

                // Terminus: Monumental Chapter Boss Battle Citadel Gate
                BossCitadelGateNode(
                    chapter = chapter,
                    isBengali = isBengali,
                    onLaunchBoss = onStartBossBattle,
                    onCelebrateVictory = { showConfettiCelebration = true }
                )
            }
        }
    }

    // Modal Details Sheet when an unlocked node is tapped
    selectedNodeForModal?.let { topic ->
            NodeExpeditionDialog(
                topic = topic,
                isBengali = isBengali,
                onDismiss = { selectedNodeForModal = null },
                onStartStudy = {
                    selectedNodeForModal = null
                    onOpenStudy(topic.id)
                },
                onStartQuiz = {
                    selectedNodeForModal = null
                    onStartPractice(
                        topic.id,
                        "${topic.title} - অনুশীলন"
                    )
                },
                onCelebrateModule = {
                    showConfettiCelebration = true
                }
            )
        }

        // Locked Notice Snackbar / Banner
        lockedNodeNotice?.let { notice ->
            AlertDialog(
                onDismissRequest = { lockedNodeNotice = null },
                confirmButton = {
                    TextButton(onClick = { lockedNodeNotice = null }) {
                        Text(text = "ঠিক আছে")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = TertiaryAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "লক করা স্তর",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                text = {
                    Text(text = notice, style = MaterialTheme.typography.bodyMedium)
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Confetti celebration when a module or chapter boss battle is conquered
        ConfettiAnimationOverlay(
            visible = showConfettiCelebration,
            onAnimationEnd = { showConfettiCelebration = false }
        )
    }
}

/**
 * Adventure Path HUD: Shows stars gathered, path completion bar, and waypoint statistics.
 */
@Composable
private fun AdventurePathHud(
    chapter: ChapterItem,
    starsEarned: Int,
    totalStars: Int,
    unlockedNodes: Int,
    totalNodes: Int,
    isBengali: Boolean
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Explore",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "অভিযাত্রা রোডম্যাপ",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "অধ্যায় ${chapter.chapterNumber.toBengaliDigits()}: ${chapter.getTitle(true)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Stars Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ExamGold.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExamGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Stars",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$starsEarned / $totalStars",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        )
                    }
                }
            }

            // Path Progress Bar
            val pathProgress = (unlockedNodes.toFloat() / totalNodes.coerceAtLeast(1)).coerceIn(0f, 1f)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "অগ্রগতি: $unlockedNodes/$totalNodes নোড উন্মুক্ত",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(pathProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ExamGold
                    )
                }

                LinearProgressIndicator(
                    progress = { pathProgress },
                    color = ExamGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

/**
 * Visual Starting Gate Banner for the adventure path.
 */
@Composable
private fun ExpeditionStartBanner(isBengali: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = "Start",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "যাত্রাপথ শুরু • বেসিক ধারণা",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/**
 * Renders a single node placed along the adventure curve with star ratings,
 * active glowing pulse beacon, and title pill.
 */
@Composable
private fun AdventureNodeRow(
    topic: TopicItem,
    index: Int,
    horizontalBias: Float,
    isNextActive: Boolean,
    isBengali: Boolean,
    onClick: () -> Unit
) {
    val isMastered = topic.masteryPercentage >= 80
    val isUnlocked = topic.isUnlocked
    val isDark = isSystemInDarkTheme()

    // Pulsing animation for the active next mission node
    val infiniteTransition = rememberInfiniteTransition(label = "node_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = when {
            horizontalBias < 0.35f -> Alignment.CenterStart
            horizontalBias > 0.65f -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(160.dp)
                .clickable(onClick = onClick)
                .testTag("adventure_node_${topic.id}")
        ) {
            // "Current Quest" Beacon Tooltip
            if (isNextActive) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ExamFlame,
                    shadowElevation = 4.dp,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 পরবর্তী মিশন",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // Star Rating Header (3 Stars)
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                val stars = when {
                    topic.masteryPercentage >= 80 -> 3
                    topic.masteryPercentage >= 50 -> 2
                    topic.masteryPercentage > 0 -> 1
                    else -> 0
                }
                for (s in 1..3) {
                    Icon(
                        imageVector = if (s <= stars) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star",
                        tint = if (s <= stars) ExamGold else Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Main Circular Adventure Node Disc
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .scale(if (isNextActive) pulseScale else 1f)
                    .shadow(
                        elevation = if (isUnlocked) 6.dp else 1.dp,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(
                        when {
                            isMastered -> Brush.radialGradient(
                                listOf(SecondaryEmeraldLight, SecondaryEmerald)
                            )
                            isNextActive -> Brush.radialGradient(
                                listOf(PrimaryIndigoLight, PrimaryIndigo)
                            )
                            isUnlocked -> Brush.radialGradient(
                                listOf(PrimaryContainerLight, Color(0xFFC7D2FE))
                            )
                            else -> Brush.radialGradient(
                                listOf(Color(0xFF64748B), Color(0xFF475569))
                            )
                        }
                    )
                    .border(
                        width = 3.dp,
                        color = when {
                            isMastered -> ExamGold
                            isNextActive -> ExamFlame
                            isUnlocked -> PrimaryIndigo
                            else -> Color(0xFF94A3B8).copy(alpha = 0.4f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !isUnlocked -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Node",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    isMastered -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mastered Node",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$index",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                ),
                                color = if (isNextActive) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${topic.questionCount} Qs",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isNextActive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Topic Title Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isUnlocked) {
                    if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.2f else 0.6f)
                },
                shadowElevation = if (isUnlocked && !isDark) 2.dp else 0.dp,
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isUnlocked) {
                        if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                    } else Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = topic.getTitle(isBengali),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    if (isUnlocked && topic.masteryPercentage > 0) {
                        Text(
                            text = "${topic.masteryPercentage.toBengaliDigits()}% নির্ভুল",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isMastered) CorrectGreen else ExamGold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Connecting Trail Segment drawn on canvas between consecutive nodes.
 */
@Composable
private fun ConnectingTrailSegment(
    fromBias: Float,
    toBias: Float,
    isActive: Boolean
) {
    val activeColor = PrimaryIndigo
    val inactiveColor = Color(0xFF94A3B8).copy(alpha = 0.4f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
    ) {
        val startX = size.width * fromBias
        val endX = size.width * toBias
        val startY = 0f
        val endY = size.height

        val path = Path().apply {
            moveTo(startX, startY)
            cubicTo(
                startX, startY + (endY - startY) * 0.5f,
                endX, startY + (endY - startY) * 0.5f,
                endX, endY
            )
        }

        drawPath(
            path = path,
            color = if (isActive) activeColor else inactiveColor,
            style = Stroke(
                width = if (isActive) 5f else 3.5f,
                cap = StrokeCap.Round,
                pathEffect = if (!isActive) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
            )
        )
    }
}

/**
 * Grand Boss Citadel Gate at the end of the adventure path.
 */
@Composable
private fun BossCitadelGateNode(
    chapter: ChapterItem,
    isBengali: Boolean,
    onLaunchBoss: () -> Unit,
    onCelebrateVictory: () -> Unit = {}
) {
    val isUnlocked = chapter.bossUnlocked
    val isDefeated = chapter.bossCompleted
    val isDark = isSystemInDarkTheme()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("boss_citadel_gate")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    when {
                        isDefeated -> if (isDark) {
                            Brush.verticalGradient(listOf(Color(0xFF064E3B).copy(alpha = 0.6f), Color(0xFF0F172A)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF0F172A)))
                        }
                        isUnlocked -> if (isDark) {
                            Brush.verticalGradient(listOf(Color(0xFF450A0A).copy(alpha = 0.6f), Color(0xFF0F172A)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFF450A0A), Color(0xFF7F1D1D), Color(0xFF0F172A)))
                        }
                        else -> if (isDark) {
                            Brush.verticalGradient(listOf(Color(0xFF1E293B).copy(alpha = 0.4f), Color(0xFF0F172A)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155)))
                        }
                    }
                )
                .border(
                    width = 2.dp,
                    color = when {
                        isDefeated -> CorrectGreen.copy(alpha = 0.6f)
                        isUnlocked -> ExamFlame.copy(alpha = 0.6f)
                        else -> Color(0xFF64748B).copy(alpha = 0.4f)
                    },
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Castle Icon Badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(
                            width = 2.dp,
                            color = if (isDefeated) ExamGold else if (isUnlocked) ExamFlame else Color.Gray,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDefeated) Icons.Default.EmojiEvents else if (isUnlocked) Icons.Default.SportsKabaddi else Icons.Default.Lock,
                        contentDescription = "Boss Citadel",
                        tint = if (isDefeated) ExamGold else if (isUnlocked) Color.White else Color.LightGray,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = when {
                        isDefeated -> "🏆 অধ্যায় সমাপনী পরীক্ষা সফলভাবে সম্পন্ন!"
                        isUnlocked -> "🔥 অধ্যায় সমাপনী পরীক্ষা উন্মুক্ত!"
                        else -> "🔒 অধ্যায় সমাপনী পরীক্ষা (লক করা)"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = when {
                        isDefeated -> "আপনি এই অধ্যায়ের সমস্ত টপিক ও মূল্যায়ন সফলভাবে সম্পন্ন করেছেন।"
                        isUnlocked -> "সমাপনী পরীক্ষা: ৮টি প্রশ্ন • সময় ৩ মিনিট (+৩৫০ XP)"
                        else -> "সমস্ত নোড ৬০%+ সমাপ্ত হলে সমাপনী পরীক্ষা আনলক হবে।"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                if (isUnlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.95f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isDefeated) {
                            OutlinedButton(
                                onClick = onCelebrateVictory,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExamGold),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ExamGold),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("celebrate_citadel_boss_btn")
                            ) {
                                Text(
                                    text = "🎉 জয় উৎসব!",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Button(
                            onClick = onLaunchBoss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDefeated) ExamGold else ExamFlame,
                                contentColor = if (isDefeated) Color(0xFF78350F) else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(if (isDefeated) 1f else 2f)
                                .height(44.dp)
                                .testTag("launch_citadel_boss_btn")
                        ) {
                            Icon(
                                imageVector = if (isDefeated) Icons.Default.Replay else Icons.Default.SportsKabaddi,
                                contentDescription = "Fight",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDefeated) {
                                    "আবার খেলুন"
                                } else {
                                    "চ্যালেঞ্জ করো!"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Node Expedition Dialog: Pops up when a student taps an unlockable node on the adventure path.
 * Offers study notes preview, syllabus coverage, and quick quiz trigger.
 */
@Composable
fun NodeExpeditionDialog(
    topic: TopicItem,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onStartStudy: () -> Unit,
    onStartQuiz: () -> Unit,
    onCelebrateModule: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("node_expedition_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Level and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryIndigo.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "নোড স্তর ${topic.orderIndex}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryIndigo,
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

                // Title
                Text(
                    text = topic.getTitle(isBengali),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Summary
                Text(
                    text = topic.getSummary(isBengali),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                // Key Points / Objectives Chips
                if (topic.keyPoints.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "প্রধান শিখন উদ্দেশ্যসমূহ:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        topic.keyPoints.take(2).forEach { point ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("• ", style = MaterialTheme.typography.bodySmall, color = PrimaryIndigo)
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Stats: Questions & Current Mastery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${topic.questionCount}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryIndigo
                        )
                        Text(
                            text = "প্রশ্ন ভাণ্ডার",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${topic.masteryPercentage}%",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (topic.masteryPercentage >= 80) CorrectGreen else ExamFlame
                        )
                        Text(
                            text = "বর্তমান মাস্টারি",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Module Mastered Celebration Banner with Confetti Trigger
                if (topic.masteryPercentage >= 80) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExamGold.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExamGold.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎉", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "মডিউল সম্পূর্ণ ও মাস্টার্ড!",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "৩টি স্টার অর্জিত হয়েছে",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            TextButton(
                                onClick = onCelebrateModule,
                                modifier = Modifier.testTag("celebrate_module_btn")
                            ) {
                                Text(
                                    text = "কনফেটি দেখুন",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = ExamGold
                                )
                            }
                        }
                    }
                }

                // Action Buttons: Study vs Practice Quiz
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onStartStudy,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("dialog_study_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Read Notes",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "নোটস পড়ুন",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = onStartQuiz,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("dialog_practice_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Practice Quiz",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "কুইজ খেলুন",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
