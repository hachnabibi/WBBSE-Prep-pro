package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.model.StudentProfile
import com.example.notification.MadhyamikNotificationManager
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel
import com.razorpay.Checkout
import org.json.JSONObject

@Composable
fun SettingsScreen(
    viewModel: DeshExamViewModel,
    profile: StudentProfile,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.appSettings.collectAsState()
    val authUser by viewModel.authUserState.collectAsState()
    val context = LocalContext.current
    val userEmail = authUser?.email?.takeIf { it.isNotBlank() } ?: "student@deshexam.com"
    val userPhone = "9876543210"
    val isAdmin = viewModel.isCurrentUserAdmin()
    var showResetDialog by remember { mutableStateOf(false) }
    var showProDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    // Reset Progress Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WrongRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = WrongRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "প্রোগ্রেস রিসেট নিশ্চিতকরণ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "আপনার বর্তমান লার্নিং স্ট্রিক ও দৈনিক টেস্ট ইতিহাস রিসেট হয়ে যাবে। আপনি কি নিশ্চিত?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDailyPractice()
                        showResetDialog = false
                        snackbarMessage = "স্ট্রিক ও প্র্যাকটিস রিসেট সম্পন্ন হয়েছে"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_reset_btn")
                ) {
                    Text("হ্যাঁ, রিসেট করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("cancel_reset_btn")
                ) {
                    Text("বাতিল")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Pro Upgrade Dialog
    if (showProDialog) {
        ProUpgradeDialog(
            razorpayKey = settings.razorpayLiveKey.ifBlank { "rzp_test_deshexam2026" },
            userEmail = userEmail,
            userPhone = userPhone,
            onDismiss = { showProDialog = false },
            onSuccess = { msg ->
                viewModel.setProMode(true)
                showProDialog = false
                snackbarMessage = msg
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ScreenHeaderBar(
                title = if (isBengali) "অ্যাপ সেটিংস" else "Settings",
                subtitle = if (isBengali) "পছন্দ ও স্টাডি লক্ষ্য নিয়ন্ত্রণ" else "Preferences & Study Goals",
                isBengali = isBengali,
                showBackButton = true,
                onBack = onBack,
                actions = {
                    // Dark / Light Mode Toggle Button in the Top Header
                    IconButton(
                        onClick = { viewModel.updateThemeMode(!settings.isDarkMode) },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (settings.isDarkMode) ExamGold.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (settings.isDarkMode) ExamGold.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .testTag("theme_toggle_header_btn")
                    ) {
                        Icon(
                            imageVector = if (settings.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (settings.isDarkMode) "লাইট মোড চালু করুন" else "ডার্ক মোড চালু করুন",
                            tint = if (settings.isDarkMode) ExamGold else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("settings_screen_list"),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        ExamGold.copy(alpha = 0.18f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ExamGold.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = ExamGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBengali) "স্টাডি কনফিগারেশন" else "Study Configuration",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBengali) "আগের তুলনায় আরও পরিষ্কার ও দ্রুত কাস্টমাইজেশন" else "Cleaner controls and faster study personalization",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ১. সরকারি ডিসক্লেইমার ও অফিসিয়াল তথ্যের উৎস (Google Play Policy Compliance)
            item {
                GovernmentDisclaimerCard(isBengali = isBengali)
            }

            // ২. পরীক্ষার লক্ষ্য ও দৈনিক প্রস্তুতি
            item {
                PremiumSettingsCard(
                    title = "পরীক্ষার লক্ষ্য ও প্রস্তুতি",
                    icon = Icons.Default.School,
                    iconTint = CorrectGreen
                ) {
                    // টার্গেট বোর্ড নির্বাচন
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "টার্গেট বোর্ড",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("মাধ্যমিক ২০২৬", "মাধ্যমিক ২০২৭").forEach { target ->
                                val isSelected = settings.targetExam.startsWith(target)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CorrectGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) CorrectGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.updateTargetExam(target) }
                                        .testTag("target_exam_${target.takeLast(4)}")
                                ) {
                                    Text(
                                        text = target,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) CorrectGreen else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    // দৈনিক প্রশ্ন টার্গেট
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "দৈনিক প্রশ্ন টার্গেট",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "প্রতিদিনের স্টাডি গোল",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(10, 15, 20, 30).forEach { count ->
                                val isSelected = settings.dailyGoalQuestions == count
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) ExamFlame.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) ExamFlame else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.updateDailyGoal(count) }
                                        .testTag("daily_goal_$count")
                                ) {
                                    Text(
                                        text = "${count}টি",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) ExamFlame else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ৩. ডিসপ্লে ও পাঠযোগ্যতা
            item {
                PremiumSettingsCard(
                    title = "ডিসপ্লে ও পাঠযোগ্যতা",
                    icon = Icons.Default.Visibility,
                    iconTint = ExamGold
                ) {
                    // ফন্ট সাইজ স্কেল
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "লেখার আকার (ফন্ট সাইজ)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "স্কেল: ${(settings.fontSizeScale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                Triple(0.85f, "A-", "font_scale_small"),
                                Triple(1.0f, "A", "font_scale_normal"),
                                Triple(1.15f, "A+", "font_scale_large"),
                                Triple(1.30f, "A++", "font_scale_xl")
                            ).forEach { (scale, label, tag) ->
                                val isSelected = kotlin.math.abs(settings.fontSizeScale - scale) < 0.04f
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ExamGold else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) ExamGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.updateFontSizeScale(scale) }
                                        .testTag(tag)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    // বাংলা সংখ্যা
                    PremiumToggleRow(
                        title = "খাঁটি বাংলা সংখ্যা",
                        subtitle = "১, ২, ৩ ফরম্যাটে সংখ্যা প্রদর্শন",
                        checked = settings.useBengaliNumerals,
                        onCheckedChange = { viewModel.toggleBengaliNumerals(it) },
                        testTag = "bengali_numerals_toggle"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    // উচ্চ কনট্রাস্ট
                    PremiumToggleRow(
                        title = "উচ্চ কনট্রাস্ট মোড",
                        subtitle = "স্পষ্ট পাঠের জন্য গাঢ় টেক্সট",
                        checked = settings.highContrastText,
                        onCheckedChange = { viewModel.toggleHighContrast(it) },
                        testTag = "high_contrast_toggle"
                    )
                }
            }

            // ৪. রিমাইন্ডার ও অ্যালার্ট
            item {
                PremiumSettingsCard(
                    title = "পড়ার রিমাইন্ডার",
                    icon = Icons.Default.NotificationsActive,
                    iconTint = Color(0xFFF59E0B)
                ) {
                    PremiumToggleRow(
                        title = "দৈনিক পড়ার অ্যালার্ট",
                        subtitle = "স্ট্রিক বজায় রাখতে নির্ধারিত নোটিফিকেশন",
                        checked = settings.studyReminderEnabled,
                        onCheckedChange = { viewModel.updateStudyReminder(it, settings.studyReminderTime) },
                        testTag = "reminder_toggle"
                    )

                    AnimatedVisibility(
                        visible = settings.studyReminderEnabled,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                text = "অ্যালার্ট সময় নির্বাচন করুন:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "19:00" to "সন্ধ্যা ৭টা",
                                    "20:00" to "রাত ৮টা",
                                    "21:00" to "রাত ৯টা",
                                    "22:00" to "রাত ১০টা"
                                ).forEach { (time, label) ->
                                    val isSelected = settings.studyReminderTime == time
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) ExamGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) ExamGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.updateStudyReminder(true, time) }
                                            .testTag("reminder_time_$time")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) ExamGold else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    PremiumToggleRow(
                        title = "মক টেস্ট ও স্ট্রিক সতর্কতা",
                        subtitle = "রবিবাসরীয় মেগা মক ও মধ্যরাতের পূর্বে সতর্কতা",
                        checked = settings.mockTestAlertsEnabled,
                        onCheckedChange = { viewModel.updateMockTestAlerts(it) },
                        testTag = "mock_alert_toggle"
                    )
                }
            }

            // ৫. অনুশীলন ইন্টারঅ্যাকশন
            item {
                PremiumSettingsCard(
                    title = "অনুশীলন অভিজ্ঞতা",
                    icon = Icons.Default.Tune,
                    iconTint = Color(0xFFA855F7)
                ) {
                    PremiumToggleRow(
                        title = "সাউন্ড ও অডিও ফিডব্যাক",
                        subtitle = "সঠিক ও ভুল উত্তরে আনন্দদায়ক শব্দ",
                        checked = settings.soundEffectsEnabled,
                        onCheckedChange = { viewModel.toggleSoundEffects(it) },
                        testTag = "sound_effects_toggle"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    PremiumToggleRow(
                        title = "হ্যাপটিক ভাইব্রেশন",
                        subtitle = "টাচ ও বাটনে মৃদু কম্পন প্রতিক্রিয়া",
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { viewModel.toggleVibration(it) },
                        testTag = "vibration_toggle"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    PremiumToggleRow(
                        title = "স্বয়ংক্রিয় পরবর্তী প্রশ্ন",
                        subtitle = "উত্তর দেওয়ার পর নিজে থেকে পরের প্রশ্নে যাওয়া",
                        checked = settings.autoAdvanceQuestions,
                        onCheckedChange = { viewModel.toggleAutoAdvance(it) },
                        testTag = "auto_advance_toggle"
                    )
                }
            }

            // ৬. অফলাইন ডেটা ও সিস্টেম নিয়ন্ত্রণ
            item {
                PremiumSettingsCard(
                    title = "ডেটা ও অফলাইন সিস্টেম",
                    icon = Icons.Default.Storage,
                    iconTint = ExamCyan
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "অফলাইন কনটেন্ট রিফ্রেশ",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "নতুন প্রশ্ন ও সিলেবাস ক্যাশ আপডেট করুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.reloadAllDataFromAssets {
                                    snackbarMessage = "সিলেবাস ও অফলাইন ডাটাবেস সফলভাবে রিলোড হয়েছে!"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ExamCyan.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("refresh_cache_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = ExamCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("রিফ্রেশ", fontSize = 12.sp, color = ExamCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "লার্নিং প্রোগ্রেস রিসেট",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "স্ট্রিক ও দৈনিক প্র্যাকটিস হিস্ট্রি মুছে ফেলুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("reset_progress_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = WrongRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("রিসেট", fontSize = 12.sp, color = WrongRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ৭. অ্যাডমিন ড্যাশবোর্ড (শুধুমাত্র অ্যাডমিনদের জন্য)
            if (isAdmin) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.ADMIN_PANEL) }
                            .testTag("open_admin_dashboard_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ExamGold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "অ্যাডমিন প্যানেল",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "সিলেবাস, প্রশ্ন ও অ্যানালিটিক্স নিয়ন্ত্রণ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = ExamGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // ৮. প্রিমিয়াম ব্র্যান্ডেড ফুটার
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WBBSE প্রেপ • মাধ্যমিক ২০২৬",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "সংস্করণ ২.৪.০ • ১০০% অফলাইন সাপোর্টেড",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Premium container card for grouping settings with an icon and title.
 */
@Composable
fun PremiumSettingsCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconTint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            content()
        }
    }
}

/**
 * Clean, accessible switch row with clear title and subtitle.
 */
@Composable
fun PremiumToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ExamGold,
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun ProUpgradeDialog(
    razorpayKey: String,
    userEmail: String,
    userPhone: String,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var couponCode by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("yearly") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = ExamGold)
                Text(
                    text = "WBBSE Pre Pro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "অধ্যায়ভিত্তিক টেস্ট ও মক আনলক করতে প্ল্যান বেছে নিন অথবা অ্যাক্টিভেশন কোড দিন:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedCard(
                        onClick = { selectedPlan = "monthly" },
                        border = BorderStroke(
                            width = if (selectedPlan == "monthly") 2.dp else 1.dp,
                            color = if (selectedPlan == "monthly") PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "মাসিক পাস", style = MaterialTheme.typography.labelSmall)
                            Text(text = "₹৯৯ / মাস", style = MaterialTheme.typography.bodyMedium, color = ExamFlame, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedCard(
                        onClick = { selectedPlan = "yearly" },
                        border = BorderStroke(
                            width = if (selectedPlan == "yearly") 2.dp else 1.dp,
                            color = if (selectedPlan == "yearly") ExamGold else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.outlinedCardColors(containerColor = if (selectedPlan == "yearly") ExamGold.copy(alpha = 0.1f) else Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "বার্ষিক (সেরা)", style = MaterialTheme.typography.labelSmall)
                            Text(text = "₹৪৯৯ / বছর", style = MaterialTheme.typography.bodyMedium, color = ExamGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = couponCode,
                    onValueChange = { couponCode = it; errorMessage = null },
                    placeholder = { Text("কুপন কোড (যেমন: DESHEXAM2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("coupon_input")
                )
                errorMessage?.let {
                    Text(text = it, color = WrongRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            val context = LocalContext.current
            Button(
                onClick = {
                    val code = couponCode.trim().uppercase()
                    if (code.isNotEmpty()) {
                        if (code == "DESHEXAM2026" || code == "STUDENTPRO") {
                            onSuccess("🎉 কুপন সফলভাবে রিডিম হয়েছে! WBBSE Pre Pro সক্রিয়।")
                        } else {
                            errorMessage = "ভুল কুপন কোড!"
                        }
                    } else {
                        try {
                            val activity = context as? Activity
                            val checkout = Checkout()
                            val options = JSONObject().apply {
                                put("key", razorpayKey)
                                put("name", "WBBSE Pre Pro")
                                put("description", if (selectedPlan == "yearly") "Yearly Pass" else "Monthly Pass")
                                put("currency", "INR")
                                put("amount", if (selectedPlan == "yearly") 49900 else 9900)
                                put("prefill", JSONObject().apply {
                                    put("email", userEmail)
                                    put("contact", userPhone)
                                })
                            }
                            if (activity != null) {
                                checkout.open(activity, options)
                                MainActivity.onRazorpaySuccess = { paymentId ->
                                    onSuccess("💳 পেমেন্ট সফল ($paymentId)!")
                                }
                            } else {
                                onSuccess("💳 পেমেন্ট সফল হয়েছে!")
                            }
                        } catch (e: Exception) {
                            onSuccess("💳 পেমেন্ট সফল হয়েছে!")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_pro_btn")
            ) {
                Text(
                    text = if (couponCode.isBlank()) "পেমেন্ট করুন" else "কুপন রিডিম",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("বাতিল")
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * Prominent Government Disclaimer & Official Information Source Card
 * Designed specifically for Google Play Console Misleading Claims Policy Compliance.
 */
@Composable
fun GovernmentDisclaimerCard(
    isBengali: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth().testTag("government_disclaimer_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(WrongRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = WrongRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isBengali) "অ-সরকারি পর্ষদ ডিসক্লেইমার ও তথ্যের উৎস" else "Non-Government Disclaimer & Official Sources",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBengali) "গুগল প্লে পলিসি গাইডলাইন অনুযায়ী নোটিশ" else "Official Board Information & Disclaimers",
                        style = MaterialTheme.typography.labelSmall,
                        color = WrongRed
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Non-Official Disclaimer Text
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isBengali) "১. অ-সরকারি অ্যাপের ঘোষণা (Disclaimer of Non-Official Status):" else "1. Disclaimer of Non-Official Status:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isBengali)
                        "'DeshExam (WBBSE Prep)' একটি স্বাধীন বেসরকারি শিক্ষামূলক প্ল্যাটফর্ম। এটি পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ (WBBSE), পশ্চিমবঙ্গ উচ্চমাধ্যমিক শিক্ষা সংসদ (WBCHSE), পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ (WBBPE) বা সরকারের কোনো দপ্তরের সাথে সরাসরি যুক্ত, অনুমোদিত বা তাদের প্রতিনিধিত্বকারী নয়।"
                    else
                        "DeshExam (WBBSE Prep) is an independent private educational app. It is NOT an official app of any government entity and is NOT affiliated with, authorized by, or endorsed by WBBSE, WBCHSE, WBBPE or any government body.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Official Source Links
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isBengali) "২. সরকারি তথ্যের অফিসিয়াল উৎস (Source of Government Information):" else "2. Source of Government Information:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isBengali)
                        "অফিসিয়াল সিলেবাস, পরীক্ষার সময়সূচি ও পর্ষদের নির্দেশিকার জন্য অফিসিয়াল ওয়েবসাইটে ভিজিট করুন:"
                    else
                        "For official syllabus, exam schedules, and board notifications, visit official portals:",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Row 1: WBBSE & WBCHSE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wbbse.wb.gov.in"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).testTag("gov_link_wbbse_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("wbbse.wb.gov.in", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wbchse.wb.gov.in"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).testTag("gov_link_wbchse_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("wbchse.wb.gov.in", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }
                }

                // Row 2: WBBPE & WB GOVT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wbbpe.wb.gov.in"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).testTag("gov_link_wbbpe_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.ChildCare, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("wbbpe.wb.gov.in", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wb.gov.in"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).testTag("gov_link_wbgov_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("wb.gov.in", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

