package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExamGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import java.util.Locale

private fun formatCoinCount(coins: Int): String {
    return when {
        coins >= 1_000_000 -> String.format(Locale.US, "%.1fM", coins / 1_000_000.0).replace(".0M", "M")
        coins >= 1_000 -> String.format(Locale.US, "%.1fK", coins / 1_000.0).replace(".0K", "K")
        else -> coins.toString()
    }
}

@Composable
fun TopHeaderBar(
    title: String? = null,
    isBengali: Boolean,
    isDarkMode: Boolean = true,
    coinCount: Int = 0,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit = {},
    onOpenWallet: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("top_header_bar")
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                val isCompact = maxWidth < 380.dp

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Side: Back/Menu Button + Title/Brand & Tagline
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showBackButton) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("header_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            IconButton(
                                onClick = onOpenDrawer,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("header_menu_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Menu",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // App Brand or Page Title + Tagline
                        Column {
                            val annotatedTitle = if (title == null || title.equals("DeshExam", ignoreCase = true)) {
                                buildAnnotatedString {
                                    val deshColor = if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669) // Emerald Green
                                    val examColor = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF4338CA) // Bright Cyan / Indigo
                                    
                                    append("Desh")
                                    addStyle(SpanStyle(color = deshColor, fontWeight = FontWeight.Black), 0, 4)
                                    append("Exam")
                                    addStyle(SpanStyle(color = examColor, fontWeight = FontWeight.Black), 4, 8)
                                }
                            } else {
                                buildAnnotatedString {
                                    append(title)
                                    addStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black), 0, title.length)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = annotatedTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = if (isCompact) 17.sp else 19.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Tagline under app name
                            Text(
                                text = if (isBengali) "স্মার্ট এক্সাম প্রিপারেশন" else "Smart Exam Prep",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = if (isCompact) 9.sp else 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.3.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 0.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right Side: Utility Icons & Coin Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Premium Pill-Badge Coin Balance with Solid Yellow Background and Contrast Dark Font
                        Surface(
                            shape = CircleShape,
                            color = ExamGold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onOpenWallet() }
                                .testTag("wallet_header_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GoldCoinIcon()
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${formatCoinCount(coinCount)} ${"কয়েন"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = Color(0xFF261400) // High contrast dark text on yellow background
                                )
                            }
                        }

                        // Language toggle
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleLanguage() }
                                .testTag("language_toggle_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "বাং",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Vertical Stacked Control for Theme Toggle & Settings with Divider
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Top: Theme Toggle
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        .clickable { onToggleTheme() }
                                        .padding(horizontal = 7.dp, vertical = 4.dp)
                                        .testTag("theme_toggle_header_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Night Mode",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isDarkMode) ExamGold else Color(0xFF4338CA)
                                    )
                                }

                                // Middle Divider
                                HorizontalDivider(
                                    modifier = Modifier.width(20.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                )

                                // Bottom: Settings Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                                        .clickable { onOpenSettings() }
                                        .padding(horizontal = 7.dp, vertical = 4.dp)
                                        .testTag("settings_header_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun GoldCoinIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(15.dp)
            .background(Color(0xFFF59E0B), CircleShape) // Bright Gold Base
            .border(1.dp, Color(0xFFD97706), CircleShape), // Darker Amber Outer Rim
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "★",
            color = Color.White, // Shiny white star inside the coin!
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = (-1).dp)
        )
    }
}
