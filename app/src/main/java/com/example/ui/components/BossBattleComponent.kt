package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Gamified Boss Battle Challenge Card for displaying upcoming/unlocked chapter bosses,
 * complete with Boss HP, Player HP, phase progression, rewards, and attack triggers.
 */
@Composable
fun BossBattleCard(
    bossName: String,
    chapterTitle: String,
    bossMaxHp: Int = 1000,
    bossCurrentHp: Int = 1000,
    playerMaxHp: Int = 100,
    playerCurrentHp: Int = 100,
    questionsCount: Int = 8,
    questionsCompleted: Int = 0,
    isBengali: Boolean = true,
    isDefeated: Boolean = false,
    onStartBattle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showVictoryConfetti by remember { mutableStateOf(false) }

    // Pulse animation for the Boss Rage Aura
    val infiniteTransition = rememberInfiniteTransition(label = "boss_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boss_scale"
    )

    Box(modifier = modifier) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("boss_battle_card")
        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDefeated) {
                        Brush.verticalGradient(
                            listOf(Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF0F172A))
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color(0xFF450A0A), Color(0xFF7F1D1D), Color(0xFF0F172A))
                        )
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = if (isDefeated) CorrectGreen.copy(alpha = 0.6f) else ExamFlame.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Top Badges: Gamified Challenge Header + Difficulty Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDefeated) CorrectGreen.copy(alpha = 0.25f) else ExamFlame.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDefeated) CorrectGreen.copy(alpha = 0.5f) else ExamFlame.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDefeated) Icons.Default.EmojiEvents else Icons.Default.Whatshot,
                                contentDescription = "Challenge Mode",
                                tint = if (isDefeated) ExamGold else ExamFlame,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isDefeated) {
                                    "🏆 সমাপনী পরীক্ষা সম্পন্ন • মাস্টারি অর্জিত"
                                } else {
                                    "📚 অধ্যায় সমাপনী পরীক্ষা"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDefeated) ExamGold else Color(0xFFFFEDD5)
                            )
                        }
                    }

                    // Level/Tier Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "টায়ার: লেজেন্ডারি",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            color = ExamGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // 2. Boss Character & Chapter Identification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animated Boss Avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(if (!isDefeated) pulseScale else 1.0f)
                            .clip(CircleShape)
                            .background(
                                if (isDefeated) {
                                    Brush.radialGradient(listOf(ExamGold.copy(alpha = 0.4f), Color(0xFF065F46)))
                                } else {
                                    Brush.radialGradient(listOf(ExamFlame.copy(alpha = 0.4f), Color(0xFF991B1B)))
                                }
                            )
                            .border(
                                width = 2.dp,
                                color = if (isDefeated) ExamGold else ExamRose,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDefeated) Icons.Default.CheckCircle else Icons.Default.Dangerous,
                            contentDescription = "Boss Emblem",
                            tint = if (isDefeated) ExamGold else Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bossName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = chapterTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 3. Health Bars: Dual Health Management (Boss HP + Player HP)
                BossHealthBar(
                    label = "মূল্যায়ন প্রোগ্রেস",
                    currentHp = bossCurrentHp,
                    maxHp = bossMaxHp,
                    barColor = if (isDefeated) CorrectGreen else ExamRose,
                    icon = Icons.Default.Assessment,
                    iconColor = ExamFlame,
                    modifier = Modifier.testTag("boss_health_bar")
                )

                PlayerHealthBar(
                    label = "আপনার স্থায়িত্ব (Player HP)",
                    currentHp = playerCurrentHp,
                    maxHp = playerMaxHp,
                    barColor = CorrectGreen,
                    icon = Icons.Default.Favorite,
                    iconColor = CorrectGreen,
                    modifier = Modifier.testTag("player_health_bar")
                )

                // 4. Progress Indicators: Stage / Phase Progress & Surviving Steps
                BattleProgressIndicator(
                    questionsCompleted = questionsCompleted,
                    totalQuestions = questionsCount,
                    isBengali = isBengali,
                    modifier = Modifier.testTag("boss_progress_indicator")
                )

                // 5. Rewards / Loot Dropped Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.35f),
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
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Reward XP",
                                tint = ExamGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+৩৫০ XP • বস বিজয়ী ব্যাজ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = ExamGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "শিল্ড প্রটেকশন",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = ExamGold
                            )
                        }
                    }
                }

                // 6. Action Button: Start or Replay Boss Battle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isDefeated) {
                        OutlinedButton(
                            onClick = { showVictoryConfetti = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExamGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ExamGold),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("celebrate_boss_card_btn")
                        ) {
                            Text(
                                text = "🎉 জয় উৎসব",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Button(
                        onClick = onStartBattle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDefeated) ExamGold else ExamFlame,
                            contentColor = if (isDefeated) Color(0xFF78350F) else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(if (isDefeated) 1.2f else 1f)
                            .height(48.dp)
                            .testTag("attack_boss_btn")
                    ) {
                        Icon(
                            imageVector = if (isDefeated) Icons.Default.Replay else Icons.Default.SportsKabaddi,
                            contentDescription = "Attack Boss",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDefeated) {
                                "পুনরায় পরীক্ষা দিন"
                            } else {
                                "সমাপনী পরীক্ষা শুরু করুন! (৩ মিনিট)"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

        // Confetti effect when student defeats or celebrates boss battle
        ConfettiAnimationOverlay(
            visible = showVictoryConfetti,
            onAnimationEnd = { showVictoryConfetti = false }
        )
    }
}

/**
 * Live In-Battle HUD Component rendered at the top of AssessmentScreen when session.mode == BOSS_BATTLE.
 * Provides real-time animated health bars, combo multipliers, and attack feedback.
 */
@Composable
fun BossBattleLiveHUD(
    bossName: String,
    bossCurrentHp: Int,
    bossMaxHp: Int,
    playerCurrentHp: Int,
    playerMaxHp: Int,
    currentQuestion: Int,
    totalQuestions: Int,
    comboCount: Int = 0,
    lastDamageDealt: Int = 0,
    isBengali: Boolean = true,
    modifier: Modifier = Modifier
) {
    val bossHpFraction = (bossCurrentHp.toFloat() / bossMaxHp).coerceIn(0f, 1f)
    val animatedBossHp by animateFloatAsState(
        targetValue = bossHpFraction,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "live_boss_hp"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E1B2E),
        border = androidx.compose.foundation.BorderStroke(1.dp, ExamFlame.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("boss_battle_live_hud")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Boss Name + Phase & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ExamRose)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bossName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }

                // Combo Badge
                if (comboCount > 1) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ExamFlame.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExamFlame)
                    ) {
                        Text(
                            text = "🔥 ${comboCount}x COMBO!",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = ExamGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "পর্যায় ২/৩",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Boss Health Bar with Live animated value
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "বস এইচপি",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ExamRose
                    )
                    Text(
                        text = "$bossCurrentHp / $bossMaxHp HP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF334155))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedBossHp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ExamRose, ExamFlame)
                                )
                            )
                    )
                }
            }

            // Player Health Bar + Stage Progress Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Player HP
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Player Health",
                        tint = CorrectGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$playerCurrentHp/$playerMaxHp HP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = CorrectGreen
                    )
                }

                // Questions Survived Progress Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..totalQuestions) {
                        val isDone = i < currentQuestion
                        val isCurrent = i == currentQuestion
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isDone -> CorrectGreen
                                        isCurrent -> ExamFlame
                                        else -> Color(0xFF475569)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual Boss Health Bar with HP numeric badge and animated progress.
 */
@Composable
fun BossHealthBar(
    label: String,
    currentHp: Int,
    maxHp: Int,
    barColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val hpFraction = (currentHp.toFloat() / maxHp).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = hpFraction,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "boss_hp"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Text(
                text = "$currentHp / $maxHp HP",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                ),
                color = if (hpFraction < 0.3f) ExamRose else ExamGold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                barColor,
                                ExamFlame
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Visual Player Health Bar with shield and heart indicators.
 */
@Composable
fun PlayerHealthBar(
    label: String,
    currentHp: Int,
    maxHp: Int,
    barColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val hpFraction = (currentHp.toFloat() / maxHp).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = hpFraction,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "player_hp"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE2E8F0)
                )
            }

            Text(
                text = "$currentHp / $maxHp HP",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = CorrectGreen
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(barColor, ExamCyan)
                        )
                    )
            )
        }
    }
}

/**
 * Progress indicator showing completed stages and question milestones against the boss.
 */
@Composable
fun BattleProgressIndicator(
    questionsCompleted: Int,
    totalQuestions: Int,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = (questionsCompleted.toFloat() / totalQuestions).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ব্যাটল অগ্রগতি ($questionsCompleted/$totalQuestions প্রশ্ন)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFCBD5E1)
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = ExamGold
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            color = ExamFlame,
            trackColor = Color.White.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
