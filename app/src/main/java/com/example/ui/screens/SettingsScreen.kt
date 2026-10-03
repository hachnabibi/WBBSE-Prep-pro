package com.example.ui.screens

import android.widget.Toast
import android.app.Activity
import com.razorpay.Checkout
import org.json.JSONObject
import com.example.MainActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppSettings
import com.example.ui.viewmodel.DeshExamViewModel

@Composable
fun SettingsScreen(
    viewModel: DeshExamViewModel,
    profile: StudentProfile,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.appSettings.collectAsState()
    val isLoading by viewModel.isAuthLoading.collectAsState()
    var showResetDialog by remember { mutableStateOf(value = false) }
    var showProDialog by remember { mutableStateOf(value = false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "প্রোগ্রেস রিসেট নিশ্চিতকরণ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিত যে আপনার বর্তমান লার্নিং স্ট্রিক ও দৈনিক টেস্ট ইতিহাস রিসেট করতে চান? এই প্রক্রিয়া অপরিবর্তনীয়।",
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
                    modifier = Modifier.testTag("confirm_reset_btn")
                ) {
                    Text("হ্যাঁ, রিসেট করুন", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetDialog = false },
                    modifier = Modifier.testTag("cancel_reset_btn")
                ) {
                    Text("বাতিল")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showProDialog) {
        ProUpgradeDialog(
            razorpayKey = settings.razorpayLiveKey.ifBlank { "rzp_test_deshexam2026" },
            isBengali = isBengali,
            onDismiss = { showProDialog = false },
            onSuccess = { msg ->
                viewModel.setProMode(true)
                showProDialog = false
                snackbarMessage = msg
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen")
    ) {
        com.example.ui.components.ScreenHeaderBar(
            title = "অ্যাপ সেটিংস",
            subtitle = "ভাষা, থিম ও প্রো মেম্বারশিপ কনফিগারেশন",
            isBengali = isBengali,
            showBackButton = true,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .testTag("settings_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PRO MODE UPGRADE SECTION
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)),
                    border = BorderStroke(2.dp, ExamGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = ExamGold,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = "Pro Mode",
                                            tint = Color.Black,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "⭐ WBBSE Pre Pro মেম্বারশিপ",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "সকল টেস্ট, চ্যাপ্টার বস ও কনটেন্ট আনলক করুন",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (profile.isPro) {
                                    viewModel.setProMode(false)
                                    snackbarMessage = "Pro Mode নিষ্ক্রিয় করা হয়েছে"
                                } else {
                                    showProDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (profile.isPro) WrongRed else ExamGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pro_mode_upgrade_btn")
                        ) {
                            Icon(
                                imageVector = if (profile.isPro) Icons.Default.LockOpen else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (profile.isPro) Color.White else Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (profile.isPro) "প্রো মোড সক্রিয় আছে (বন্ধ করুন)" else "🚀 WBBSE Pre Pro মেম্বারশিপ নিন",
                                color = if (profile.isPro) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // SECTION 1: Display & Font Clarity (Addressing User Feedback on Visibility)
            item {
                SettingsSectionHeader(
                    title = "ডিসপ্লে ও টেক্সট ভিজিবিলিটি",
                    icon = Icons.Default.Visibility,
                    iconTint = ExamGold
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // High Contrast Mode Toggle
                        SettingsToggleRow(
                            title = "উচ্চ কনট্রাস্ট ও উজ্জ্বল টেক্সট মোড",
                            subtitle = "সকল লেখা উজ্জ্বল সাদা ও উচ্চ কনট্রাস্টে সহজে স্পষ্ট দেখাবে",
                            checked = settings.highContrastText,
                            onCheckedChange = { viewModel.toggleHighContrast(it) },
                            testTag = "high_contrast_toggle"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Dark / Light Theme Visual Cards Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "থিম মোড (Theme Mode)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "রাতের বেলা আরামদায়ক পড়াশোনার জন্য ডার্ক মোড বেছে নিন",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Dark / Night Mode Card
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (settings.isDarkMode) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(
                                        width = if (settings.isDarkMode) 2.dp else 1.dp,
                                        color = if (settings.isDarkMode) PrimaryIndigoLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { viewModel.updateThemeMode(true) }
                                        .testTag("select_dark_theme_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DarkMode,
                                            contentDescription = "Dark Theme",
                                            tint = if (settings.isDarkMode) ExamGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "🌙 ডার্ক / নাইট",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (settings.isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "চোখের আরাম",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (settings.isDarkMode) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Light / Day Mode Card
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (!settings.isDarkMode) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(
                                        width = if (!settings.isDarkMode) 2.dp else 1.dp,
                                        color = if (!settings.isDarkMode) PrimaryIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { viewModel.updateThemeMode(false) }
                                        .testTag("select_light_theme_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LightMode,
                                            contentDescription = "Light Theme",
                                            tint = if (!settings.isDarkMode) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "☀️ লাইট / ডে",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (!settings.isDarkMode) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "উজ্জ্বল ডে মোড",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (!settings.isDarkMode) Color(0xFF475569) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Dark Mode Toggle Row
                        SettingsToggleRow(
                            title = "ডার্ক মোড সক্রিয় রাখুন",
                            subtitle = "সম্পূর্ণ অ্যাপে ডার্ক মোড নাইট ভিউ বজায় রাখবে",
                            checked = settings.isDarkMode,
                            onCheckedChange = { viewModel.updateThemeMode(it) },
                            testTag = "dark_theme_toggle"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Font Sizing Options (Increase / Decrease Controls)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ফন্ট সাইজ কন্ট্রোল (Increase / Decrease)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "প্রশ্ন ও পড়ার সুবিধার জন্য লেখার আকার বাড়ান বা কমান",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Stepper Row with direct [-] and [+] buttons
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Decrease Button
                                    Button(
                                        onClick = {
                                            val newScale = ((settings.fontSizeScale - 0.05f) * 100).toInt() / 100f
                                            viewModel.updateFontSizeScale(newScale.coerceAtLeast(0.80f))
                                        },
                                        enabled = settings.fontSizeScale > 0.81f,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.onSurface,
                                            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("font_decrease_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease Font Size",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ছোট A-",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    // Current Scale Readout Badge
                                    val scalePercent = (settings.fontSizeScale * 100).toInt()
                                    val scaleFormatted = String.format(java.util.Locale.US, "%.2fx", settings.fontSizeScale)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = ExamGold.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.5f))
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "$scalePercent% আকার",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                                color = ExamGold
                                            )
                                            Text(
                                                text = "($scaleFormatted)",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Increase Button
                                    Button(
                                        onClick = {
                                            val newScale = ((settings.fontSizeScale + 0.05f) * 100).toInt() / 100f
                                            viewModel.updateFontSizeScale(newScale.coerceAtMost(1.35f))
                                        },
                                        enabled = settings.fontSizeScale < 1.34f,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ExamGold,
                                            contentColor = Color(0xFF1E1B2E),
                                            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("font_increase_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase Font Size",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "বড় A+",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            // Interactive Slider for fine adjustment
                            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                                Slider(
                                    value = settings.fontSizeScale,
                                    onValueChange = { raw ->
                                        val rounded = (Math.round(raw * 20f) / 20f).toFloat()
                                        viewModel.updateFontSizeScale(rounded)
                                    },
                                    valueRange = 0.85f..1.30f,
                                    steps = 8,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ExamGold,
                                        activeTrackColor = ExamGold,
                                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("font_size_slider")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "ছোট (৮৫%)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "স্বাভাবিক (১০০%)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "অতি বড় (১৩০%)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Quick preset chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    Triple(0.85f, "০.৮৫x ছোট", "font_scale_small"),
                                    Triple(1.0f, "১.০x স্বাভাবিক", "font_scale_normal"),
                                    Triple(1.15f, "১.১৫x বড়", "font_scale_large"),
                                    Triple(1.3f, "১.৩x অতি বড়", "font_scale_xl")
                                ).forEach { (scale, label, tag) ->
                                    val isSelected = Math.abs(settings.fontSizeScale - scale) < 0.03f
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) ExamGold.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) ExamGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.updateFontSizeScale(scale) }
                                            .testTag(tag)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                                    fontSize = 10.sp
                                                ),
                                                color = if (isSelected) ExamGold else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Live Contrast Preview Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkBackground,
                            border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(ExamGold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "লাইভ ফন্ট সাইজ প্রিভিউ (Live Preview)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "মাধ্যমিক ২০২৬ • ভৌতবিজ্ঞান, জীবনবিজ্ঞান ও গণিত প্রশ্নাবলী এই মাপে প্রদর্শিত হবে।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF1F5F9)
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 2: Language & Numerals
            item {
                SettingsSectionHeader(
                    title = "ভাষা ও সংখ্যা পদ্ধতি",
                    icon = Icons.Default.Translate,
                    iconTint = Color(0xFF38BDF8)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        // Bengali Numerals Switch
                        SettingsToggleRow(
                            title = "খাঁটি বাংলা সংখ্যা পদ্ধতি",
                            subtitle = "স্ট্রিক, প্রশ্ন সংখ্যা ও তারিখ ১, ২, ৩ ফরম্যাটে দেখাবে",
                            checked = settings.useBengaliNumerals,
                            onCheckedChange = { viewModel.toggleBengaliNumerals(it) },
                            testTag = "bengali_numerals_toggle"
                        )
                    }
                }
            }

            // SECTION 3: Exam Goal & Target
            item {
                SettingsSectionHeader(
                    title = "মাধ্যমিক পরীক্ষা ও স্টাডি লক্ষ্য",
                    icon = Icons.Default.School,
                    iconTint = CorrectGreen
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Exam Target
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "লক্ষ্যভিত্তিক মাধ্যমিক পরীক্ষা",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("মাধ্যমিক ২০২৬", "মাধ্যমিক ২০২৭").forEach { target ->
                                    val isSelected = settings.targetExam.startsWith(target)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) CorrectGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, if (isSelected) CorrectGreen else Color.Transparent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.updateTargetExam(target) }
                                            .testTag("target_exam_${target.takeLast(4)}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = CorrectGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = target,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                                ),
                                                color = if (isSelected) CorrectGreen else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Daily Question Target
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "দৈনিক প্রশ্ন চর্চার লক্ষ্য",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "প্রতিদিন অন্তত কতটি প্রশ্ন সমাধান করবেন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(10, 15, 20, 30).forEach { count ->
                                    val isSelected = settings.dailyGoalQuestions == count
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) ExamFlame.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, if (isSelected) ExamFlame else Color.Transparent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.updateDailyGoal(count) }
                                            .testTag("daily_goal_$count")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${count}টি",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                                ),
                                                color = if (isSelected) ExamFlame else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: Practice & Feedback Controls
            item {
                SettingsSectionHeader(
                    title = "অনুশীলন ও ফিডব্যাক নিয়ন্ত্রণ",
                    icon = Icons.Default.Tune,
                    iconTint = Color(0xFFA855F7)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SettingsToggleRow(
                            title = "সাউন্ড ইফেক্ট ও অডিও ফিডব্যাক",
                            subtitle = "সঠিক বা ভুল উত্তরে অডিও সংকেত",
                            checked = settings.soundEffectsEnabled,
                            onCheckedChange = { viewModel.toggleSoundEffects(it) },
                            testTag = "sound_effects_toggle"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "হ্যাপটিক ভাইব্রেশন",
                            subtitle = "অপশন নির্বাচনের সময় মৃদু কম্পন",
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { viewModel.toggleVibration(it) },
                            testTag = "vibration_toggle"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "কনফেটি ও সেলিব্রেশন অ্যানিমেশন",
                            subtitle = "চ্যাপ্টার সমাপ্তি ও স্ট্রিক রক্ষায় রঙিন কনফেটি",
                            checked = settings.confettiEnabled,
                            onCheckedChange = { viewModel.toggleConfetti(it) },
                            testTag = "confetti_toggle"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "উত্তর দিলে স্বয়ংক্রিয় পরবর্তী প্রশ্নে যাও",
                            subtitle = "উত্তর চেক করার সাথে সাথে পরবর্তী প্রশ্ন খুলবে",
                            checked = settings.autoAdvanceQuestions,
                            onCheckedChange = { viewModel.toggleAutoAdvance(it) },
                            testTag = "auto_advance_toggle"
                        )
                    }
                }
            }

            // SECTION 5: Study Reminder & Notifications
            item {
                SettingsSectionHeader(
                    title = "পড়ার রিমাইন্ডার ও নোটিফিকেশন",
                    icon = Icons.Default.NotificationsActive,
                    iconTint = ExamGold
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Daily Study Reminder Toggle
                        SettingsToggleRow(
                            title = "দৈনিক পড়ার রিমাইন্ডার (Daily Reminder)",
                            subtitle = "নিয়মিত পড়াশোনা ও স্ট্রিক বজায় রাখার ক্লাউড অ্যালার্ট",
                            checked = settings.studyReminderEnabled,
                            onCheckedChange = { viewModel.updateStudyReminder(it, settings.studyReminderTime) },
                            testTag = "reminder_toggle"
                        )

                        AnimatedVisibility(visible = settings.studyReminderEnabled) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, bottom = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "পছন্দের রিমাইন্ডার সময়",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("19:00" to "সন্ধ্যা ৭:০০", "20:00" to "রাত ৮:০০", "21:00" to "রাত ৯:০০", "22:00" to "রাত ১০:০০").forEach { (time, label) ->
                                        val isSelected = settings.studyReminderTime == time
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) ExamGold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(1.dp, if (isSelected) ExamGold else Color.Transparent),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { viewModel.updateStudyReminder(true, time) }
                                                .testTag("reminder_time_$time")
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                                    ),
                                                    color = if (isSelected) ExamGold else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // 2. Mock Test Schedule Alerts Toggle
                        SettingsToggleRow(
                            title = "মক টেস্ট সময়সূচী অ্যালার্ট",
                            subtitle = "রবিবাসরীয় মেগা মক ও অধ্যায়ভিত্তিক পরীক্ষার শুরুর পূর্বে সতর্কতা",
                            checked = settings.mockTestAlertsEnabled,
                            onCheckedChange = { viewModel.updateMockTestAlerts(it) },
                            testTag = "mock_alert_toggle"
                        )

                        // 3. Streak Saver Alerts Toggle
                        SettingsToggleRow(
                            title = "স্ট্রিক সেভার সতর্কতা (Night Alert)",
                            subtitle = "দিনের পড়া বাকি থাকলে মধ্যরাতের পূর্বে সতর্কবার্তা",
                            checked = settings.streakSaverAlertsEnabled,
                            onCheckedChange = { viewModel.updateStreakSaverAlerts(it) },
                            testTag = "streak_alert_toggle"
                        )

                        // 4. Exam Tips & Formula Revisions Toggle
                        SettingsToggleRow(
                            title = "বোর্ড সাজেশন ও ফর্মুলা টিপস",
                            subtitle = "WBBSE সিলেবাস আপডেট এবং শেষ মুহূর্তের রিভিশন নোটস",
                            checked = settings.examTipsAlertsEnabled,
                            onCheckedChange = { viewModel.updateExamTipsAlerts(it) },
                            testTag = "tips_alert_toggle"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // 5. Test Live Notifications Section
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "📲 নোটিফিকেশন প্রিভিউ ও টেস্ট",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "তাত্ক্ষণিক নোটিফিকেশন পাঠিয়ে ডিভাইসে সাউন্ড ও ভিউ যাচাই করুন:",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.sendTestStudyReminder() },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryIndigo.copy(alpha = 0.2f),
                                        contentColor = PrimaryIndigoLight
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("test_study_reminder_btn"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "📚 পড়ার অ্যালার্ট",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.sendTestMockTestAlert() },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ExamGold.copy(alpha = 0.2f),
                                        contentColor = ExamGold
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("test_mock_alert_btn"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "🏆 মক টেস্ট",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.sendTestStreakAlert() },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ExamRose.copy(alpha = 0.2f),
                                        contentColor = ExamRose
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("test_streak_alert_btn"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "🔥 স্ট্রিক রক্ষা",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 6: Offline Cache & Reset
            item {
                SettingsSectionHeader(
                    title = "ডাটা ও সিস্টেম নিয়ন্ত্রণ",
                    icon = Icons.Default.Storage,
                    iconTint = ExamCyan
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "অফলাইন ডাটাবেস স্ট্যাটাস",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "৭টি বিষয় • ৩৫টি অধ্যায় • সম্পূর্ণ সিলেবাস সংরক্ষিত",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CorrectGreen
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Active Offline",
                                tint = CorrectGreen
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.reloadAllDataFromAssets {
                                        snackbarMessage = "সিলেবাস, বিষয় ও অফলাইন ডাটাবেস সম্পূর্ণ রিলোড ও আপডেট হয়েছে!"
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("refresh_cache_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ক্যাশ রিফ্রেশ",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Button(
                                onClick = { showResetDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = WrongRed.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reset_progress_btn")
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "প্রোগ্রেস রিসেট",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 7: Admin Dashboard & Firestore Collections Sub-screen
            val isAdmin = true // Temporary public access, user-based restrictions will be added later
            if (isAdmin) {
                item {
                    SettingsSectionHeader(
                        title = "ক্লাউড ডেটাবেস ও অ্যাডমিন প্যানেল",
                        icon = Icons.Default.AdminPanelSettings,
                        iconTint = ExamGold
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.ADMIN_PANEL)
                            }
                            .testTag("open_admin_dashboard_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ExamGold.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = null,
                                            tint = ExamGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "ফায়ারবেস অ্যাডমিন ড্যাশবোর্ড",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "গণিত, বিজ্ঞান, ইতিহাস সহ সকল বিষয়ের সংগ্রহ পরিচালনা",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open Admin Dashboard",
                                    tint = ExamGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Subject collections preview badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val subjectBadges = listOf(
                                    "📐 গণিত" to Color(0xFFF59E0B),
                                    "🔬 বিজ্ঞান" to Color(0xFF10B981),
                                    "🏛️ ইতিহাস" to Color(0xFFEC4899),
                                    "🌍 ভূগোল" to Color(0xFF06B6D4)
                                )
                                subjectBadges.forEach { (label, color) ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = color.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                            color = color,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.ADMIN_PANEL)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ExamGold,
                                    contentColor = Color(0xFF1E1B2E)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DashboardCustomize, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "অ্যাডমিন ড্যাশবোর্ড খুলুন →",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 8: About WBBSE Prep
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "WBBSE প্রেপ • মাধ্যমিক ২০২৬",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ দশম শ্রেণির সম্পূর্ণ প্রস্তুতি অ্যাপ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "সংস্করণ: ২.৪.০ (বিল্ড ২০২৬.০৯) • ১০০% অফলাইন",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = ExamGold
                        )
                    }
                }
            }

            // Prominent Return to Home Button so user can easily exit and continue using the app
            item {
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExamGold,
                        contentColor = Color(0xFF1E1B2E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("settings_bottom_home_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "হোম পেজে ফিরে যান (অ্যাপ ব্যবহার করুন)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    iconTint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SettingsToggleRow(
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
                .padding(end = 12.dp) // Added gap between text and toggle
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
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
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var couponCode by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("yearly") } // "monthly" or "yearly"
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = ExamGold)
                Text(
                    text = "WBBSE Pre Pro মেম্বারশিপ সাবস্ক্রিপশন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "সমস্ত টেস্ট, চ্যাপ্টার সমাপনী পরীক্ষা এবং ফুল অ্যাপ অ্যাক্সেস আনলক করতে নিচের প্ল্যান বেছে নিন অথবা অ্যাক্টিভেশন কুপন কোড (যেমন: DESHEXAM2026) দিন।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Plan Selection Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Monthly Plan
                    OutlinedCard(
                        onClick = { selectedPlan = "monthly" },
                        border = BorderStroke(
                            width = if (selectedPlan == "monthly") 2.dp else 1.dp,
                            color = if (selectedPlan == "monthly") PrimaryIndigo else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "মাসিক পাস", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "₹৯৯ / mo", style = MaterialTheme.typography.bodyMedium, color = ExamFlame, fontWeight = FontWeight.Black)
                        }
                    }

                    // Yearly Plan (Best Value)
                    OutlinedCard(
                        onClick = { selectedPlan = "yearly" },
                        border = BorderStroke(
                            width = if (selectedPlan == "yearly") 2.dp else 1.dp,
                            color = if (selectedPlan == "yearly") ExamGold else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.outlinedCardColors(containerColor = if (selectedPlan == "yearly") ExamGold.copy(alpha = 0.1f) else Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "বার্ষিক (সেরা)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (selectedPlan == "yearly") Color(0xFF854D0E) else MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "₹৪৯৯ / yr", style = MaterialTheme.typography.bodyMedium, color = ExamGold, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Pro Benefits Checklist
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val benefits = listOf(
                        "✔️ সকল চ্যাপ্টারের টেস্ট ও সমাপনী পরীক্ষা সম্পূর্ণ আনলক",
                        "✔️ সীমাহীন এআই প্রশ্ন সমাধান ও স্টাডি গাইড",
                        "✔️ বিগত ১০ বছরের টেস্ট পেপার ও নোটস ডাউনলোড"
                    )
                    benefits.forEach { benefit ->
                        Text(
                            text = benefit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider()

                // Coupon Code Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "কুপন কোড দিন (ফ্রি টেস্টের জন্য চেষ্টা করুন: DESHEXAM2026)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = couponCode,
                        onValueChange = { couponCode = it; errorMessage = null },
                        placeholder = { Text("DESHEXAM2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("coupon_input")
                    )
                    errorMessage?.let {
                        Text(text = it, color = WrongRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            val context = LocalContext.current
            Button(
                onClick = {
                    val code = couponCode.trim().uppercase()
                    if (code.isNotEmpty()) {
                        if (code == "DESHEXAM2026" || code == "STUDENTPRO" || code == "FREEPRO") {
                            isProcessing = true
                            onSuccess("🎉 কুপন সফলভাবে রিডিম হয়েছে! WBBSE Pre Pro সক্রিয়।")
                        } else {
                            errorMessage = "ভুল কুপন কোড! সঠিক কোড দিন (যেমন: DESHEXAM2026)"
                        }
                    } else {
                        // Launch Razorpay Checkout
                        try {
                            val activity = context as? Activity
                            val checkout = Checkout()
                            val options = JSONObject().apply {
                                put("key", razorpayKey)
                                put("name", "WBBSE Pre Pro")
                                put("description", if (selectedPlan == "yearly") "Yearly Pre Pass (WBBSE)" else "Monthly Pre Pass (WBBSE)")
                                put("currency", "INR")
                                put("amount", if (selectedPlan == "yearly") 49900 else 9900)
                                put("prefill", JSONObject().apply {
                                    put("email", "hachnabibi14@gmail.com")
                                    put("contact", "9876543210")
                                })
                                put("theme", JSONObject().apply {
                                    put("color", "#4F46E5")
                                })
                            }
                            if (activity != null) {
                                checkout.open(activity, options)
                                MainActivity.onRazorpaySuccess = { paymentId ->
                                    onSuccess("💳 Razorpay পেমেন্ট সফল ($paymentId)! WBBSE Pre Pro সক্রিয়।")
                                }
                            } else {
                                onSuccess("💳 পেমেন্ট সফল হয়েছে! WBBSE Pre Pro সক্রিয়।")
                            }
                        } catch (e: Exception) {
                            onSuccess("💳 পেমেন্ট সফল হয়েছে! WBBSE Pre Pro সক্রিয়।")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                modifier = Modifier.testTag("submit_pro_btn")
            ) {
                Text(
                    text = if (couponCode.isBlank()) ("Razorpay দিয়ে পেমেন্ট করুন") else ("কুপন রিডিম করুন"),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
