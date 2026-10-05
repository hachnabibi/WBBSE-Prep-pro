package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TopicItem
import com.example.data.model.VideoLessonProvider
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VideoHubScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val lazyListState = rememberLazyListState()

    // Select the first subject if none is currently selected
    LaunchedEffect(subjects) {
        if (selectedSubjectId.isBlank() && subjects.isNotEmpty()) {
            viewModel.selectSubject(subjects.first().id)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Top Header with Premium Gradient & Sidebar Hamburger Menu
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(PrimaryIndigo, PrimaryIndigoLight)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Hamburger Menu Icon to open sidebar navigation drawer (aligned with home)
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Sidebar Navigation",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        Column {
                            Text(
                                text = if (isBengali) "স্মার্ট ভিডিও লাইব্রেরি" else "Smart Video Library",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = if (isBengali) "আপনার সিলেবাসের সকল ভিডিও ক্লাস এক জায়গায়" else "All syllabus video classes in one place",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // 2. Sticky Subject Filter Tabs Row (Sticks to top when header scrolls out of view!)
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background, // Match background for clean sticky overlay
                    tonalElevation = 3.dp
                ) {
                    if (subjects.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            subjects.forEach { subject ->
                                val isSelected = subject.id == selectedSubjectId
                                val titleText = if (isBengali) subject.nameBengali else subject.nameEnglish

                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectSubject(subject.id) },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = titleText,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                            // Ultra-sleek transparent badge with a thin border
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.Transparent, CircleShape)
                                                    .border(
                                                        width = 0.75.dp,
                                                        color = if (isSelected) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                                        shape = CircleShape
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isBengali) "ভিডিও" else "Video",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Video Lessons Grid/List List
            if (topics.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OndemandVideo,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = if (isBengali) "এই বিষয়ের অধীনে কোনো ভিডিও ক্লাস পাওয়া যায়নি" else "No video classes found for this subject",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                items(topics) { topic ->
                    // Dynamically obtain the registered or fallback video lesson for this topic
                    val lesson = VideoLessonProvider.getVideoForTopic(
                        topicId = topic.id,
                        topicTitle = topic.title,
                        subjectId = topic.subjectId,
                        topicVideoUrl = topic.videoUrl,
                        topicPlaylistJson = topic.playlistJson
                    )

                    val titleText = if (isBengali) topic.titleBengali else topic.titleEnglish
                    val summaryText = if (isBengali) topic.summaryBengali else topic.summaryEnglish

                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("video_hub_card_${topic.id}")
                                .clickable {
                                    viewModel.openVideoLessonForEntity(lesson, topic)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Side: Visual Thumbnail with Play Icon & Gradient Overlay
                                Box(
                                    modifier = Modifier
                                        .size(width = 110.dp, height = 75.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Decorative elements to make it look like a video container
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    
                                    // YouTube-style compact duration badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = formatDurationText(lesson, isBengali),
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Right Side: Video Info & Actions
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = titleText,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = summaryText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isBengali) "ভিডিও ক্লাস • সম্পূর্ণ প্লেলিস্ট" else "Video Class • Playlist",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun convertDigits(input: String, toBengali: Boolean): String {
    val englishDigits = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val bengaliDigits = listOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    var result = input
    if (toBengali) {
        englishDigits.forEachIndexed { idx, char ->
            result = result.replace(char, bengaliDigits[idx])
        }
    } else {
        bengaliDigits.forEachIndexed { idx, char ->
            result = result.replace(char, englishDigits[idx])
        }
    }
    return result
}

private fun sumPlaylistDurations(playlist: List<com.example.data.model.PlaylistItem>?): String? {
    if (playlist.isNullOrEmpty()) return null
    var totalSeconds = 0
    for (item in playlist) {
        val d = item.duration ?: continue
        val norm = convertDigits(d, false)
        val parts = norm.split(":")
        if (parts.size == 2) {
            val m = parts[0].toIntOrNull() ?: 0
            val s = parts[1].toIntOrNull() ?: 0
            totalSeconds += (m * 60) + s
        } else if (parts.size == 1) {
            val m = parts[0].toIntOrNull() ?: 0
            totalSeconds += m * 60
        }
    }
    if (totalSeconds == 0) return null
    val totalMins = totalSeconds / 60
    val totalSecs = totalSeconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", totalMins, totalSecs)
}

private fun formatDurationText(lesson: com.example.data.model.VideoLesson, isBengali: Boolean): String {
    var rawDuration = lesson.duration ?: ""
    
    // If raw duration is a playlist/long text, try to sum up the playlist items or get the first item's duration
    if (rawDuration.contains("প্লেলিস্ট") || rawDuration.contains("playlist") || rawDuration.contains("সম্পূর্ণ")) {
        val summed = sumPlaylistDurations(lesson.playlist)
        if (!summed.isNullOrBlank()) {
            rawDuration = summed
        } else {
            val firstItemDuration = lesson.playlist?.firstOrNull()?.duration
            if (!firstItemDuration.isNullOrBlank()) {
                rawDuration = firstItemDuration
            } else {
                rawDuration = "25:00"
            }
        }
    }
    
    if (rawDuration.isBlank()) return "10:00"
    
    // Strip out any units (like "মিনিট", "min", "sec", etc.) and keep only colons and digits
    val cleanDuration = rawDuration.replace("মিনিট", "")
        .replace("সেকেন্ড", "")
        .replace("ঘণ্টা", "")
        .replace("min", "")
        .replace("sec", "")
        .replace("hr", "")
        .trim()
    
    // If it is a raw number (no colons), treat as minutes and convert to MM:SS format
    if (!cleanDuration.contains(":")) {
        val num = convertDigits(cleanDuration, false).toIntOrNull()
        if (num != null) {
            return convertDigits(String.format(java.util.Locale.US, "%02d:00", num), toBengali = isBengali)
        }
    }

    // Parse and normalize colon format (HH:MM:SS or MM:SS)
    val parts = cleanDuration.split(":")
    if (parts.isNotEmpty() && parts.all { convertDigits(it, false).toIntOrNull() != null }) {
        val englishParts = parts.map { convertDigits(it, false).toInt() }
        val formattedEnglish = when (englishParts.size) {
            3 -> String.format(java.util.Locale.US, "%d:%02d:%02d", englishParts[0], englishParts[1], englishParts[2])
            2 -> String.format(java.util.Locale.US, "%d:%02d", englishParts[0], englishParts[1])
            1 -> String.format(java.util.Locale.US, "0:%02d", englishParts[0])
            else -> cleanDuration
        }
        return convertDigits(formattedEnglish, toBengali = isBengali)
    }
    
    return convertDigits(cleanDuration, toBengali = isBengali)
}
