package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AudioPodcast
import com.example.ui.theme.ExamGold
import com.example.util.AudioPodcastManager

/**
 * Modern Mini-Player Component for Audio Podcasts.
 * Features fixed sleek 62.dp height, progress indicator bar, play/pause with buffering state,
 * forward/rewind 10s, and tap-to-expand modern Full Player bottom sheet.
 */
@Composable
fun PodcastMiniPlayer(
    podcast: AudioPodcast?,
    isPlaying: Boolean,
    progress: Float,
    currentPositionMs: Long,
    durationMs: Long,
    playbackSpeed: Float = 1.0f,
    isBengali: Boolean = true,
    hasPreviousTrack: Boolean = false,
    hasNextTrack: Boolean = false,
    onPlayPause: () -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    onPreviousTrack: (() -> Unit)? = null,
    onNextTrack: (() -> Unit)? = null,
    onSeek: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFullPlayerSheet by remember { mutableStateOf(false) }
    val isVisible = podcast != null

    // Infinite rotation animation for artwork vinyl
    val infiniteTransition = rememberInfiniteTransition(label = "mini_artwork_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Animated visibility: Hide mini player when full player sheet is expanded
    AnimatedVisibility(
        visible = isVisible && !showFullPlayerSheet,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (podcast != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("podcast_mini_player_root")
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = ExamGold.copy(alpha = 0.35f)
                        ),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF13102C),
                    border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFF59E0B), ExamGold, Color(0xFFFFD54F))
                                        )
                                    )
                            )
                        }

                        // Main Content Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Track Icon with animated status
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                Color(0xFF4F46E5),
                                                Color(0xFF312E81)
                                            )
                                        )
                                    )
                                    .rotate(if (isPlaying) rotation else 0f)
                                    .clickable { showFullPlayerSheet = true }
                                    .testTag("mini_player_artwork"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPlaying) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Playing",
                                        tint = ExamGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = "Podcast",
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            // Middle Info Column (Title & Subject/Time)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 10.dp, end = 6.dp)
                                    .clickable { showFullPlayerSheet = true }
                            ) {
                                Text(
                                    text = cleanPodcastTitle(podcast.title),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                val cleanSub = cleanPodcastSubject(podcast.subject)
                                val bCls = when (podcast.className.trim()) {
                                    "Class 5", "class_5" -> "৫ম শ্রেণি"
                                    "Class 6", "class_6" -> "৬ষ্ঠ শ্রেণি"
                                    "Class 7", "class_7" -> "৭ম শ্রেণি"
                                    "Class 8", "class_8" -> "৮ম শ্রেণি"
                                    "Class 9", "class_9" -> "নবম শ্রেণি"
                                    "Class 10", "class_10" -> "১০ম শ্রেণি"
                                    "Class 11", "class_11" -> "একাদশ শ্রেণি"
                                    "Class 12", "class_12" -> "দ্বাদশ শ্রেণি"
                                    "ALL" -> "সকল শ্রেণি"
                                    else -> podcast.className
                                }

                                val timeStr = if (durationMs > 0) {
                                    val t = "${formatDuration(currentPositionMs)} / ${formatDuration(durationMs)}"
                                    if (isBengali) toBengaliDigits(t) else t
                                } else {
                                    ""
                                }

                                val subText = if (timeStr.isNotBlank()) {
                                    "$cleanSub • ${if (isBengali) bCls else podcast.className} • $timeStr"
                                } else {
                                    "$cleanSub • ${if (isBengali) bCls else podcast.className}"
                                }

                                Text(
                                    text = subText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = Color.White.copy(alpha = 0.72f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Controls Group
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                // Rewind 10s
                                IconButton(
                                    onClick = onRewind10,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("mini_player_rewind_10")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Replay10,
                                        contentDescription = "Rewind 10s",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // Play / Pause Primary Button
                                Surface(
                                    onClick = onPlayPause,
                                    shape = CircleShape,
                                    color = ExamGold,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("mini_player_play_pause")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (AudioPodcastManager.isBuffering) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = Color(0xFF13112E),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = if (isPlaying) "Pause" else "Play",
                                                tint = Color(0xFF13112E),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }

                                // Forward 10s
                                IconButton(
                                    onClick = onForward10,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("mini_player_forward_10")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forward10,
                                        contentDescription = "Forward 10s",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // Close / Stop Button
                                IconButton(
                                    onClick = onClose,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("mini_player_close")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Player",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Detailed Playback Bottom Sheet
    if (showFullPlayerSheet && podcast != null) {
        PodcastFullPlayerSheet(
            podcast = podcast,
            isPlaying = isPlaying,
            progress = progress,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            playbackSpeed = playbackSpeed,
            isBengali = isBengali,
            hasPreviousTrack = hasPreviousTrack,
            hasNextTrack = hasNextTrack,
            onPlayPause = onPlayPause,
            onRewind10 = onRewind10,
            onForward10 = onForward10,
            onPreviousTrack = onPreviousTrack,
            onNextTrack = onNextTrack,
            onSeek = onSeek,
            onSpeedChange = onSpeedChange,
            onDismiss = { showFullPlayerSheet = false }
        )
    }
}

/**
 * Modern Full Audio Player Modal Bottom Sheet.
 * Displays rotating vinyl disc artwork, pure Bengali metadata, precision custom scrubber,
 * smooth controls, and playback speed options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastFullPlayerSheet(
    podcast: AudioPodcast,
    isPlaying: Boolean,
    progress: Float,
    currentPositionMs: Long,
    durationMs: Long,
    playbackSpeed: Float,
    isBengali: Boolean,
    hasPreviousTrack: Boolean,
    hasNextTrack: Boolean,
    onPlayPause: () -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    onPreviousTrack: (() -> Unit)?,
    onNextTrack: (() -> Unit)?,
    onSeek: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_spin"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF100E26),
        contentColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.35f))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 16.dp,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        modifier = Modifier.testTag("podcast_full_player_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar: Collapse button, Title, Speed control in corner, Close button
            var showSpeedMenu by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("full_player_collapse_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Collapse Player",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBengali) "এখন চলছে" else "NOW PLAYING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = ExamGold
                    )
                    Text(
                        text = if (isBengali) "স্মার্ট অডিও লেকচার" else "Smart Audio Lecture",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                // Top-Right Corner Controls: Speed Control Button & Close Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Playback Speed Button in Corner (Default Player style)
                    Box {
                        Surface(
                            onClick = { showSpeedMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            color = if (playbackSpeed != 1.0f) ExamGold.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.12f),
                            border = BorderStroke(
                                1.dp,
                                if (playbackSpeed != 1.0f) ExamGold else Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("full_player_speed_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "গতি নিয়ন্ত্রণ",
                                    tint = if (playbackSpeed != 1.0f) ExamGold else Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                val speedDisplay = when (playbackSpeed) {
                                    0.75f -> "০.৭৫x"
                                    1.0f -> "১.০x"
                                    1.25f -> "১.২৫x"
                                    1.5f -> "১.৫x"
                                    2.0f -> "২.০x"
                                    else -> "${playbackSpeed}x"
                                }
                                Text(
                                    text = if (isBengali) speedDisplay else "${playbackSpeed}x",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (playbackSpeed != 1.0f) ExamGold else Color.White
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        ) {
                            val speeds = listOf(
                                0.75f to if (isBengali) "০.৭৫x (ধীর গতি)" else "0.75x (Slower)",
                                1.0f to if (isBengali) "১.০x (স্বাভাবিক)" else "1.0x (Normal)",
                                1.25f to if (isBengali) "১.২৫x (দ্রুত)" else "1.25x (Faster)",
                                1.5f to if (isBengali) "১.৫x (দেড় গুণ)" else "1.5x (1.5x)",
                                2.0f to if (isBengali) "২.০x (দ্বিগুণ গতি)" else "2.0x (Double)"
                            )
                            speeds.forEach { (sp, label) ->
                                val isSelected = kotlin.math.abs(sp - playbackSpeed) < 0.05f
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) ExamGold else Color.White,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = ExamGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSpeedChange(sp)
                                        showSpeedMenu = false
                                    },
                                    modifier = Modifier.background(
                                        if (isSelected) ExamGold.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rotating Vinyl Disc Artwork
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .shadow(16.dp, CircleShape, spotColor = ExamGold.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF312E81),
                                Color(0xFF4338CA),
                                Color(0xFF1E1B4B),
                                Color(0xFF312E81)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                    .rotate(if (isPlaying) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                // Subtle concentric vinyl groove
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                )

                // Vinyl Center Label
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F0E24))
                        .border(2.dp, ExamGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.Headphones,
                        contentDescription = null,
                        tint = ExamGold,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Track Title (Clean Bengali)
            Text(
                text = cleanPodcastTitle(podcast.title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    lineHeight = 23.sp
                ),
                color = Color.White,
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subject & Class Badges Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cleanSub = cleanPodcastSubject(podcast.subject)
                val bCls = when (podcast.className.trim()) {
                    "Class 5", "class_5" -> "৫ম শ্রেণি"
                    "Class 6", "class_6" -> "৬ষ্ঠ শ্রেণি"
                    "Class 7", "class_7" -> "৭ম শ্রেণি"
                    "Class 8", "class_8" -> "৮ম শ্রেণি"
                    "Class 9", "class_9" -> "নবম শ্রেণি"
                    "Class 10", "class_10" -> "১০ম শ্রেণি"
                    "Class 11", "class_11" -> "একাদশ শ্রেণি"
                    "Class 12", "class_12" -> "দ্বাদশ শ্রেণি"
                    "ALL" -> "সকল শ্রেণি"
                    else -> podcast.className
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF4F46E5).copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = cleanSub,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFC7D2FE),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = if (isBengali) bCls else podcast.className,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }

                if (podcast.narrator.isNotBlank()) {
                    Text(
                        text = "বক্তা: ${podcast.narrator}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sleek Custom Audio Scrubber (Pristine Circular Gold Thumb with White Border)
            CustomAudioScrubber(
                progress = progress,
                onSeek = onSeek,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("full_player_custom_scrubber")
            )

            // Time Indicators Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentStr = formatDuration(currentPositionMs)
                val totalStr = formatDuration(durationMs)
                Text(
                    text = if (isBengali) toBengaliDigits(currentStr) else currentStr,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.75f)
                )
                Text(
                    text = if (isBengali) toBengaliDigits(totalStr) else totalStr,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ExamGold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Playback Control Buttons Row (Standard Audio Player 5-Button Layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Track
                IconButton(
                    onClick = { onPreviousTrack?.invoke() },
                    enabled = hasPreviousTrack && onPreviousTrack != null,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = if (hasPreviousTrack) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Rewind 10s
                IconButton(
                    onClick = onRewind10,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Main Play/Pause Button (Centered, Prominent Gold Circle)
                Surface(
                    onClick = onPlayPause,
                    shape = CircleShape,
                    color = ExamGold,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(62.dp)
                        .testTag("full_player_play_pause_btn")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (AudioPodcastManager.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Color(0xFF13112E),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color(0xFF13112E),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                // Forward 10s
                IconButton(
                    onClick = onForward10,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10s",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Next Track
                IconButton(
                    onClick = { onNextTrack?.invoke() },
                    enabled = hasNextTrack && onNextTrack != null,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = if (hasNextTrack) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Audio Lecture Hint Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = ExamGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "স্মার্ট অডিও রিভিশন • চোখ বন্ধ করে মুখস্থ করুন" else "Smart Audio Revision • Listen and revise anytime",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
            }
        }
    }
}

/**
 * Custom Audio Scrubber Component.
 * Replaces default Material 3 Slider with a pristine circular gold thumb with white border,
 * smooth track filling, real-time dragging, and tap-to-seek support.
 */
@Composable
fun CustomAudioScrubber(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    val currentProgress = (if (isDragging) dragProgress else progress).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(newProgress)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek(dragProgress)
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth
        val thumbDiameter = 16.dp
        val trackHeight = 4.5.dp

        // Inactive background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        )

        // Active progress track
        Box(
            modifier = Modifier
                .fillMaxWidth(currentProgress)
                .height(trackHeight)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFF59E0B), ExamGold, Color(0xFFFFD54F))
                    )
                )
        )

        // Thumb indicator (Pristine circular gold thumb with crisp white border and shadow)
        val usableWidth = (totalWidth - thumbDiameter).coerceAtLeast(0.dp)
        val thumbOffset = usableWidth * currentProgress
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbDiameter)
                .shadow(4.dp, CircleShape)
                .background(ExamGold, CircleShape)
                .border(2.5.dp, Color.White, CircleShape)
        )
    }
}

/**
 * Backward compatibility alias for any existing callers of PodcastFullPlayerDialog.
 */
@Composable
fun PodcastFullPlayerDialog(
    podcast: AudioPodcast,
    isPlaying: Boolean,
    progress: Float,
    currentPositionMs: Long,
    durationMs: Long,
    playbackSpeed: Float,
    isBengali: Boolean,
    hasPreviousTrack: Boolean,
    hasNextTrack: Boolean,
    onPlayPause: () -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    onPreviousTrack: (() -> Unit)?,
    onNextTrack: (() -> Unit)?,
    onSeek: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    PodcastFullPlayerSheet(
        podcast = podcast,
        isPlaying = isPlaying,
        progress = progress,
        currentPositionMs = currentPositionMs,
        durationMs = durationMs,
        playbackSpeed = playbackSpeed,
        isBengali = isBengali,
        hasPreviousTrack = hasPreviousTrack,
        hasNextTrack = hasNextTrack,
        onPlayPause = onPlayPause,
        onRewind10 = onRewind10,
        onForward10 = onForward10,
        onPreviousTrack = onPreviousTrack,
        onNextTrack = onNextTrack,
        onSeek = onSeek,
        onSpeedChange = onSpeedChange,
        onDismiss = onDismiss
    )
}

/**
 * Cleans titles of repetitive subject names or English parentheses.
 */
fun cleanPodcastTitle(title: String): String {
    return title
        .replace(Regex("\\s*\\(ইতিহাস\\)\\s*"), "")
        .replace(Regex("\\s*\\(History\\)\\s*"), "")
        .replace(Regex("\\s*\\(Physical Science\\)\\s*"), "")
        .replace(Regex("\\s*\\(Life Science\\)\\s*"), "")
        .replace(Regex("\\s*\\(Geography\\)\\s*"), "")
        .replace(Regex("\\s*\\(Bengali\\)\\s*"), "")
        .replace(Regex("\\s*\\(Mendel's Laws\\)\\s*"), "")
        .replace(Regex("\\s*\\(Science\\)\\s*"), "")
        .replace(Regex("\\s*\\(Environment\\)\\s*"), "")
        .replace(Regex("\\s*\\(Physics\\)\\s*"), "")
        .replace(Regex("\\s*\\(Chemistry\\)\\s*"), "")
        .replace(Regex("\\s*\\(Biology\\)\\s*"), "")
        .replace(Regex("\\s*\\(Mathematics\\)\\s*"), "")
        .replace(Regex("\\s*\\([A-Za-z\\s&']+\\)\\s*"), "")
        .trim()
}

/**
 * Cleans subjects of parentheses and Hindi characters.
 */
fun cleanPodcastSubject(subject: String): String {
    return subject
        .replace(Regex("\\s*\\([A-Za-z\\s&]+\\)\\s*"), "")
        .replace("जीवनবিজ্ঞান", "জীবনবিজ্ঞান")
        .trim()
}

/** Formats milliseconds into mm:ss string representation */
fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/** Converts ASCII numbers to Bengali script digits */
fun toBengaliDigits(input: String): String {
    val bengaliDigits = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )
    return input.map { bengaliDigits[it] ?: it }.joinToString("")
}
