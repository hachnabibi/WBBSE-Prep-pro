package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.platform.LocalContext
import com.example.util.AdMobManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class AdRewardType {
    DOUBLE_XP,
    AI_CREDITS,
    STREAK_REPAIR,
    FIFTY_FIFTY,
    PDF_UNLOCK,
    EXTRA_TIME,
    BATTLE_REVIVE,
    OFFLINE_PODCAST
}

sealed interface AdLoadState {
    object Loaded : AdLoadState
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun RewardedAdDialog(
    rewardType: AdRewardType,
    isBengali: Boolean,
    onRewardEarned: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showInAppDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val activity = context.findActivity()
        if (activity != null && AdMobManager.hasRewardedAd()) {
            val shown = AdMobManager.showRewardedAd(
                activity = activity,
                onRewardEarned = { _, _ -> onRewardEarned() },
                onAdDismissed = { onDismiss() }
            )
            if (!shown) {
                showInAppDialog = true
            }
        } else {
            AdMobManager.loadRewardedAd(context)
            showInAppDialog = true
        }
    }

    if (!showInAppDialog) {
        return
    }

    var timeLeft by remember { mutableIntStateOf(5) }
    var isRewardReady by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isVideoPlaying by remember { mutableStateOf(true) }
    var showConfetti by remember { mutableStateOf(false) }
    var showSkipWarningDialog by remember { mutableStateOf(false) }

    // Automatic video countdown & reward triggering
    LaunchedEffect(isVideoPlaying) {
        if (isVideoPlaying && !isRewardReady) {
            while (timeLeft > 0) {
                delay(1000L)
                if (isVideoPlaying) {
                    timeLeft--
                }
            }
            if (timeLeft <= 0) {
                isRewardReady = true
                showConfetti = true
            }
        }
    }

    val adDataList = remember {
        listOf(
            Triple(
                "পশ্চিমবঙ্গ স্বামী বিবেকানন্দ মেরিট-কাম-মিনস স্কলারশিপ (SVMCM)",
                "যোগ্য মাধ্যমিক ও উচ্চমাধ্যমিক শিক্ষার্থীদের জন্য বার্ষিক ₹১২,০০০ থেকে ₹৬০,০০০ পর্যন্ত সরকারি অনুদান!",
                Color(0xFF0284C7)
            ),
            Triple(
                "WBJEE, NEET & Madhyamik স্পেশাল ক্র্যাশ কোর্স ২০২৬",
                "শীর্ষ র‍্যাঙ্কারদের তৈরি অধ্যায়ভিত্তিক ভিডিও লেকচার, মক টেস্ট ও লাইভ ডাউট ক্লিয়ারিং!",
                Color(0xFF7C3AED)
            ),
            Triple(
                "পশ্চিমবঙ্গ স্টুডেন্ট ক্রেডিট কার্ড প্রকল্প",
                "মাত্র ৪% সরল সুদে সর্বোচ্চ ১০ লক্ষ টাকা পর্যন্ত উচ্চশিক্ষা লোন সুবিধা。",
                Color(0xFF059669)
            )
        )
    }
    val selectedAd = remember { adDataList.random() }

    val rewardTitle = when (rewardType) {
        AdRewardType.DOUBLE_XP -> "২X বোনাস XP & কয়েন"
        AdRewardType.AI_CREDITS -> "৩টি ফ্রি AI ডাউট কয়েন"
        AdRewardType.STREAK_REPAIR -> "স্টাডি স্ট্রাইক পুনরুদ্ধার"
        AdRewardType.FIFTY_FIFTY -> "৫০:৫০ লাইফলাইন আনলক"
        AdRewardType.PDF_UNLOCK -> "স্পেশাল সাজেশন PDF আনলক"
        AdRewardType.EXTRA_TIME -> "+৬০ সেকেন্ড অতিরিক্ত সময় আনলক"
        AdRewardType.BATTLE_REVIVE -> "ব্যাটল রি-ম্যাচ এনার্জি রিফিল"
        AdRewardType.OFFLINE_PODCAST -> "অফলাইন অডিও পডকাস্ট আনলক"
    }

    // Early Close Warning Dialog
    if (showSkipWarningDialog) {
        Dialog(
            onDismissRequest = { showSkipWarningDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Warning Icon in the CENTER TOP of the card
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ExamGold.copy(alpha = 0.15f))
                                .border(
                                    2.dp,
                                    ExamGold.copy(alpha = 0.45f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Warning",
                                tint = ExamGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "বিজ্ঞাপন বন্ধ করতে চান?",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (isBengali)
                                "এখনই বিজ্ঞাপন বন্ধ করলে আপনি '$rewardTitle' রিওয়ার্ডটি পাবেন না। আপনি কি নিশ্চিত?"
                            else
                                "If you close the video now, you will not receive '$rewardTitle'. Are you sure?",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showSkipWarningDialog = false
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.5.dp, WrongRed.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "হ্যাঁ, কেটে দিন",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = WrongRed
                                )
                            }

                            Button(
                                onClick = { showSkipWarningDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "বিজ্ঞাপন দেখুন (${timeLeft}s)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (isRewardReady) {
                onRewardEarned()
                onDismiss()
            } else {
                showSkipWarningDialog = true
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .testTag("rewarded_ad_fullscreen_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Bar with Sponsored Badge and Close/Audio Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ExamGold)
                            )
                            Text(
                                text = "AdMob টেস্ট বিজ্ঞাপন • রিওয়ার্ড",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sound Toggle
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Audio toggle",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Countdown Timer badge
                        if (!isRewardReady) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, Color(0xFF67E8F9).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF67E8F9),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${timeLeft}s",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF67E8F9)
                                    )
                                }
                            }
                        }

                        // Close Button (Always accessible so user can close anytime)
                        IconButton(
                            onClick = {
                                if (isRewardReady) {
                                    onRewardEarned()
                                    onDismiss()
                                } else {
                                    showSkipWarningDialog = true
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRewardReady) CorrectGreen else Color.White.copy(alpha = 0.2f)
                                )
                                .testTag("ad_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Ad",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Middle: Interactive Animated Video Player Mock
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B)),
                    border = BorderStroke(1.5.dp, PrimaryIndigo.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background gradient & visual branding
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF0F172A),
                                            Color(0xFF1E1B4B),
                                            Color(0xFF090D16)
                                        )
                                    )
                                )
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Video header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isVideoPlaying) CorrectGreen else WrongRed)
                                    )
                                    Text(
                                        text = if (isVideoPlaying) "Auto Playing HD Video" else "Video Paused",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Official AdMob",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Center Video Content Animation
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                // Animated Video Player Screen
                                val infiniteTransition = rememberInfiniteTransition(label = "video_wave")
                                val pulseScale by infiniteTransition.animateFloat(
                                    initialValue = 0.95f,
                                    targetValue = 1.05f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "pulse"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .graphicsLayer {
                                            scaleX = if (isVideoPlaying) pulseScale else 1f
                                            scaleY = if (isVideoPlaying) pulseScale else 1f
                                        }
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(selectedAd.third, Color(0xFF06B6D4))))
                                        .clickable { isVideoPlaying = !isVideoPlaying },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isVideoPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = "Video Status",
                                        tint = Color.White,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }

                                Text(
                                    text = selectedAd.first,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 22.sp,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = selectedAd.second,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        lineHeight = 18.sp,
                                        fontSize = 13.sp
                                    ),
                                    color = Color(0xFFCBD5E1),
                                    textAlign = TextAlign.Center
                                )

                                // CTA Action Button
                                Button(
                                    onClick = { /* Simulated partner link */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF67E8F9)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "বিস্তারিত জানুন (Official Portal) ↗",
                                        color = Color(0xFF0F172A),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            // Bottom Video Progress Bar (Live 5s automatic play tracker)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val progress = ((5 - timeLeft) / 5f).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF67E8F9),
                                    trackColor = Color.White.copy(alpha = 0.2f)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (!isRewardReady)
                                            ("পুরস্কার পেতে ভিডিওটি স্বয়ংক্রিয়ভাবে চলছে...")
                                        else
                                            ("🎉 পুরস্কার আনলক হয়েছে!"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isRewardReady) ExamGold else Color.LightGray
                                    )
                                    Text(
                                        text = "00:0${(5 - timeLeft).coerceIn(0, 5)} / 00:05",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Reward Claim Bar
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isRewardReady) Color(0xFF1E293B) else Color(0xFF0F172A),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (isRewardReady) ExamGold else Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isRewardReady) ExamGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isRewardReady) Icons.Default.CheckCircle else Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = if (isRewardReady) ExamGold else Color.LightGray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isRewardReady)
                                        ("পুরস্কার অর্জিত হয়েছে!")
                                    else
                                        ("আনলক হতে যাওয়া পুরস্কার:"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isRewardReady) ExamGold else Color.Gray
                                )
                                Text(
                                    text = rewardTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isRewardReady) {
                                    onRewardEarned()
                                    onDismiss()
                                } else {
                                    showSkipWarningDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRewardReady) ExamGold else Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("claim_reward_btn")
                        ) {
                            Text(
                                text = if (isRewardReady)
                                    ("সংগ্রহ করুন 🎁")
                                else
                                    ("বন্ধ করুন ✕"),
                                color = if (isRewardReady) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            if (showConfetti) {
                ConfettiAnimationOverlay(
                    visible = showConfetti,
                    onAnimationEnd = { showConfetti = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

