package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BadgeCategory
import com.example.data.model.BadgeItem
import com.example.data.model.BadgeTier
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    var selectedBadgeForDetail by remember { mutableStateOf<BadgeItem?>(null) }
    
    // Filters
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, EARNED, LOCKED
    var selectedCategoryFilter by remember { mutableStateOf(BadgeCategory.ALL) }

    val unlockedCount = remember(badges) { badges.count { it.isUnlocked } }
    val totalXpFromBadges = remember(badges) { badges.filter { it.isUnlocked }.sumOf { it.xpReward } }

    val filteredBadges = remember(badges, statusFilter, selectedCategoryFilter) {
        badges.filter { badge ->
            val matchesStatus = when (statusFilter) {
                "EARNED" -> badge.isUnlocked
                "LOCKED" -> !badge.isUnlocked
                else -> true
            }
            val matchesCategory = selectedCategoryFilter == BadgeCategory.ALL || badge.category == selectedCategoryFilter
            matchesStatus && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBengali) "মেডেল ও কৃতি অর্জন" else "Badges & Achievements",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            // Header Item 1: Summary Card (Full width span)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = ExamGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = if (isBengali) "আপনার মেডেল গ্যালারি" else "Your Trophy Cabinet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isBengali) {
                                "সর্বমোট ${badges.size} টির মধ্যে $unlockedCount টি মেডেল আনলক হয়েছে | +$totalXpFromBadges XP অর্জিত"
                            } else {
                                "Unlocked $unlockedCount of ${badges.size} badges | +$totalXpFromBadges XP Earned"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        val progressFraction = if (badges.isNotEmpty()) unlockedCount.toFloat() / badges.size else 0f
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            // Header Item 2: Filter Section (Full width span)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Status Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusChips = listOf(
                            "ALL" to (if (isBengali) "সব" else "All"),
                            "EARNED" to (if (isBengali) "অর্জিত" else "Earned"),
                            "LOCKED" to (if (isBengali) "লকড" else "Locked")
                        )

                        statusChips.forEach { (status, label) ->
                            val isSelected = statusFilter == status
                            FilterChip(
                                selected = isSelected,
                                onClick = { statusFilter = status },
                                label = { Text(text = label, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Category Scrollable Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categoryFilters = listOf(
                            BadgeCategory.ALL to (if (isBengali) "সকল" else "All"),
                            BadgeCategory.ACCURACY to (if (isBengali) "শতভাগ মেধা" else "Accuracy"),
                            BadgeCategory.CONSISTENCY to (if (isBengali) "ধারাবাহিকতা" else "Consistency"),
                            BadgeCategory.TIME to (if (isBengali) "সময় সাধনা" else "Time"),
                            BadgeCategory.MASTERY to (if (isBengali) "মাস্টারি" else "Mastery"),
                            BadgeCategory.EXAM to (if (isBengali) "বোর্ড মডেল" else "Exam")
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categoryFilters) { (cat, label) ->
                                val isSelected = selectedCategoryFilter == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategoryFilter = cat },
                                    label = { Text(text = label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Space
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Grid Items
            if (filteredBadges.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Empty",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBengali) "কোনো কৃতিত্ব অর্জন মেলেনি" else "No achievements found",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredBadges, key = { it.id }) { badge ->
                    EducationalBadgeGridCard(
                        badge = badge,
                        isBengali = isBengali,
                        onClick = { selectedBadgeForDetail = badge }
                    )
                }
            }
        }
    }

    // Detail Dialog
    selectedBadgeForDetail?.let { badge ->
        EducationalBadgeDetailDialog(
            badge = badge,
            isBengali = isBengali,
            onDismiss = { selectedBadgeForDetail = null }
        )
    }
}

@Composable
fun EducationalBadgeGridCard(
    badge: BadgeItem,
    isBengali: Boolean,
    onClick: () -> Unit
) {
    val tierColor = when (badge.tier) {
        BadgeTier.BRONZE -> Color(0xFFCD7F32)
        BadgeTier.SILVER -> Color(0xFFC0C0C0)
        BadgeTier.GOLD -> ExamGold
        BadgeTier.DIAMOND -> Color(0xFF38BDF8)
    }

    val progressFraction = if (badge.maxProgress > 0) badge.currentProgress.toFloat() / badge.maxProgress else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            }
        ),
        border = BorderStroke(
            width = if (badge.isUnlocked) 1.5.dp else 1.dp,
            color = if (badge.isUnlocked) tierColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Badge Container with Circular Progress Ring
            Box(
                modifier = Modifier.size(76.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Circle Progress Ring
                Canvas(modifier = Modifier.size(72.dp)) {
                    // Track circle
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.15f),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    // Active progress arc
                    drawArc(
                        color = tierColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progressFraction.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }

                // Inner Badge Circle with Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (badge.isUnlocked) {
                                Brush.radialGradient(listOf(tierColor.copy(alpha = 0.25f), Color.Transparent))
                            } else {
                                Brush.radialGradient(listOf(Color.Gray.copy(alpha = 0.1f), Color.Transparent))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (badge.iconName) {
                        "CheckCircle" -> Icons.Default.CheckCircle
                        "WbSunny" -> Icons.Default.WbSunny
                        "Bedtime" -> Icons.Default.Bedtime
                        "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
                        "Bolt" -> Icons.Default.Bolt
                        "Shield" -> Icons.Default.Shield
                        "WorkspacePremium" -> Icons.Default.WorkspacePremium
                        "AutoAwesome" -> Icons.Default.AutoAwesome
                        "EmojiEvents" -> Icons.Default.EmojiEvents
                        "AutoStories" -> Icons.Default.AutoStories
                        "Whatshot" -> Icons.Default.Whatshot
                        else -> Icons.Default.MilitaryTech
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (badge.isUnlocked) tierColor else Color.Gray,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Unlock Indicator Corner Badge
                if (badge.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Unlocked",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = if (isBengali) badge.nameBengali else badge.nameEnglish,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Progress text: e.g. "৭/৭" or "৫/১৫"
            Text(
                text = "${badge.currentProgress}/${badge.maxProgress}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = if (badge.isUnlocked) tierColor else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EducationalBadgeDetailDialog(
    badge: BadgeItem,
    isBengali: Boolean,
    onDismiss: () -> Unit
) {
    val tierColor = when (badge.tier) {
        BadgeTier.BRONZE -> Color(0xFFCD7F32)
        BadgeTier.SILVER -> Color(0xFFC0C0C0)
        BadgeTier.GOLD -> ExamGold
        BadgeTier.DIAMOND -> Color(0xFF38BDF8)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Outer Glow Badge Container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(tierColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(tierColor.copy(alpha = 0.3f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (badge.iconName) {
                            "CheckCircle" -> Icons.Default.CheckCircle
                            "WbSunny" -> Icons.Default.WbSunny
                            "Bedtime" -> Icons.Default.Bedtime
                            "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
                            "Bolt" -> Icons.Default.Bolt
                            "Shield" -> Icons.Default.Shield
                            "WorkspacePremium" -> Icons.Default.WorkspacePremium
                            "AutoAwesome" -> Icons.Default.AutoAwesome
                            "EmojiEvents" -> Icons.Default.EmojiEvents
                            "AutoStories" -> Icons.Default.AutoStories
                            "Whatshot" -> Icons.Default.Whatshot
                            else -> Icons.Default.MilitaryTech
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (badge.isUnlocked) tierColor else Color.Gray,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = if (isBengali) badge.nameBengali else badge.nameEnglish,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Subtitle (Tier & Status)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = CircleShape,
                    color = tierColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, tierColor)
                ) {
                    Text(
                        text = if (isBengali) {
                            "${badge.tier.name} স্তরের মেডেল | ${if (badge.isUnlocked) "অর্জিত 🏆" else "লকড 🔒"}"
                        } else {
                            "${badge.tier.name} Badge | ${if (badge.isUnlocked) "Earned 🏆" else "Locked 🔒"}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = tierColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = if (isBengali) badge.descriptionBengali else badge.descriptionEnglish,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Requirement Info
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBengali) "অর্জনের লক্ষ্য:" else "Requirement:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBengali) badge.milestoneRequirementBengali else badge.milestoneRequirementEnglish,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // XP Reward Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "XP",
                        tint = ExamGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isBengali) "+${badge.xpReward} XP বোনাস পুরস্কার" else "+${badge.xpReward} XP Bonus",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ExamGold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (isBengali) "ঠিক আছে" else "Close")
                }
            }
        }
    }
}
