package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.model.AssessmentMode
import com.example.data.model.PlaylistItem
import com.example.data.model.TopicItem
import com.example.data.model.VideoLesson
import com.example.ui.components.VideoPlayer
import com.example.ui.theme.ExamCyan
import com.example.ui.theme.ExamGold
import com.example.ui.viewmodel.DeshExamViewModel

internal fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

enum class VideoTab {
    PLAYLIST,
    SUMMARY,
    QUIZ,
    DOUBT
}

/**
 * Dedicated Fullscreen Video Lesson Screen (AppScreen.VIDEO_LESSON)
 * Provides professional EdTech video learning experience with:
 * - Rotated Landscape Fullscreen Mode on Fullscreen Click
 * - Auto-play next video in the playlist with toggle switch
 * - Clear "Playing Now" animated indicator in the playlist and player
 * - Universal ExoPlayer / YouTube player with speed controls (0.5x - 2.0x)
 * - Automatic Firebase Cloud & Local sync of video playback progress
 * - Interactive multi-part playlist switcher
 * - Key Concept Notes & Formula summary
 * - Quick Quiz assessment launch
 * - Doubt solver integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLessonScreen(
    lesson: VideoLesson,
    topic: TopicItem? = null,
    isBengali: Boolean = true,
    viewModel: DeshExamViewModel? = null,
    onBack: () -> Unit,
    onStartQuiz: ((mode: AssessmentMode, targetId: String, title: String) -> Unit)? = null,
    onOpenDoubtSolver: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var isFullscreen by remember { mutableStateOf(false) }
    var isAutoPlayNext by remember { mutableStateOf(true) }
    var shouldAutoPlayCurrent by remember { mutableStateOf(false) }

    var currentVideoSource by remember(lesson) {
        mutableStateOf(lesson.getEffectiveVideoSource())
    }
    var currentVideoTitle by remember(lesson) {
        mutableStateOf(lesson.safeTitle)
    }
    var currentInstructor by remember(lesson) {
        mutableStateOf(lesson.safeInstructorName)
    }
    var currentVideoId by remember(lesson) {
        mutableStateOf(lesson.safeId)
    }
    var activePlaylistItemIndex by remember(lesson) {
        mutableIntStateOf(0)
    }

    var selectedTab by remember { mutableStateOf(VideoTab.PLAYLIST) }
    var isBookmarked by remember { mutableStateOf(false) }

    val playlistItems = remember(lesson) { lesson.safePlaylist }

    fun setFullscreenState(enable: Boolean) {
        isFullscreen = enable
        val act = activity ?: return
        try {
            if (enable) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                act.window?.let { window ->
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                    insetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                act.window?.let { window ->
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Reset orientation safely on leave
    DisposableEffect(Unit) {
        onDispose {
            try {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                activity?.window?.let { window ->
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            } catch (_: Exception) {}
        }
    }

    BackHandler {
        if (isFullscreen) {
            setFullscreenState(false)
        } else {
            onBack()
        }
    }

    fun playEpisode(index: Int, autoPlay: Boolean = true) {
        if (index in playlistItems.indices) {
            val item = playlistItems[index]
            activePlaylistItemIndex = index
            currentVideoSource = item.getEffectiveVideoSource()
            currentVideoTitle = item.safeTitle
            currentInstructor = item.safeInstructorName
            currentVideoId = item.safeId.ifBlank { "part_${item.partNumber}_${lesson.safeTopicId}" }
            shouldAutoPlayCurrent = autoPlay
        }
    }

    fun playNextEpisode() {
        if (playlistItems.isNotEmpty() && activePlaylistItemIndex + 1 < playlistItems.size) {
            val nextIndex = activePlaylistItemIndex + 1
            playEpisode(nextIndex, autoPlay = true)
            val nextItem = playlistItems[nextIndex]
            Toast.makeText(
                context,
                if (isBengali) "▶️ পরবর্তী পর্ব চালু হচ্ছে: ${nextItem.safeTitle}" else "▶️ Auto-playing next: ${nextItem.safeTitle}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Progress map from ViewModel
    val videoProgressMap by (viewModel?.videoProgressMap?.collectAsState() ?: remember { mutableStateOf(emptyMap()) })
    val currentSavedProgress = videoProgressMap[currentVideoId] ?: videoProgressMap[lesson.safeTopicId]
    val initialPositionMs = currentSavedProgress?.lastPositionMs ?: 0L
    val savedPercentageInt = currentSavedProgress?.percentage?.toInt() ?: 0

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val topicTitle = remember(topic, isBengali, lesson) {
        if (topic != null) {
            if (isBengali) topic.titleBengali else topic.titleEnglish
        } else {
            lesson.safeTitle
        }
    }

    // If Fullscreen, display edge-to-edge landscape video player
    if (isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            VideoPlayer(
                videoUrl = currentVideoSource,
                title = currentVideoTitle,
                instructorName = currentInstructor,
                autoPlay = true,
                playerHeight = 0.dp,
                isFullscreen = true,
                showTopBar = false,
                showExternalButton = false,
                showSpeedControls = true,
                initialPositionMs = initialPositionMs,
                isCloudSynced = true,
                onProgressUpdate = { posMs, durMs, speed, isComp ->
                    val effectiveTopicId = topic?.id ?: lesson.safeTopicId
                    viewModel?.saveVideoPlaybackProgress(
                        videoId = currentVideoId,
                        topicId = effectiveTopicId,
                        lessonTitle = currentVideoTitle,
                        videoUrl = currentVideoSource,
                        positionMs = posMs,
                        durationMs = durMs,
                        playbackSpeed = speed,
                        isCompleted = isComp
                    )
                    if (isComp && isAutoPlayNext) {
                        playNextEpisode()
                    }
                },
                onFullScreenClick = { setFullscreenState(false) },
                onVideoSourceChanged = { newSrc -> currentVideoSource = newSrc },
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isBengali) "ভিডিও ক্লাস ও লেকচার" else "Video Lecture",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = topicTitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("video_screen_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isBengali) "ফিরে যান" else "Back"
                            )
                        }
                    },
                    actions = {
                        // Bookmark action
                        IconButton(
                            onClick = {
                                isBookmarked = !isBookmarked
                                val msg = if (isBookmarked) {
                                    if (isBengali) "ভিডিওটি বুকমার্কে সংরক্ষিত হয়েছে" else "Saved to bookmarks"
                                } else {
                                    if (isBengali) "বুকমার্ক থেকে সরানো হয়েছে" else "Removed from bookmarks"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) ExamGold else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Share action
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        currentVideoTitle
                                    )
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "📚 দেশ একজাম (Madhyamik Exam App) এ ভিডিও ক্লাস দেখুন:\n\n🎥 $currentVideoTitle\n👨‍🏫 শিক্ষক: $currentInstructor\n\nঅ্যাপটি এখনই ডাউনলোড করুন!"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Video Lesson"))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface,
                        titleContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF8FAFC),
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Universal Video Player Box
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF000000))
                    ) {
                        VideoPlayer(
                            videoUrl = currentVideoSource,
                            title = currentVideoTitle,
                            instructorName = currentInstructor,
                            autoPlay = shouldAutoPlayCurrent,
                            playerHeight = 240.dp,
                            isFullscreen = false,
                            showTopBar = false,
                            showExternalButton = false,
                            showSpeedControls = true,
                            initialPositionMs = initialPositionMs,
                            isCloudSynced = true,
                            onProgressUpdate = { posMs, durMs, speed, isComp ->
                                val effectiveTopicId = topic?.id ?: lesson.safeTopicId
                                viewModel?.saveVideoPlaybackProgress(
                                    videoId = currentVideoId,
                                    topicId = effectiveTopicId,
                                    lessonTitle = currentVideoTitle,
                                    videoUrl = currentVideoSource,
                                    positionMs = posMs,
                                    durationMs = durMs,
                                    playbackSpeed = speed,
                                    isCompleted = isComp
                                )
                                if (isComp && isAutoPlayNext) {
                                    playNextEpisode()
                                }
                            },
                            onFullScreenClick = { setFullscreenState(true) },
                            onVideoSourceChanged = { newSrc ->
                                currentVideoSource = newSrc
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 2. Video Title, Instructor & Sync Meta Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF1E293B) else Color.White)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        // Badges row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (playlistItems.size > 1) {
                                                if (isBengali) "পর্ব ${activePlaylistItemIndex + 1}/${playlistItems.size}" else "Part ${activePlaylistItemIndex + 1}/${playlistItems.size}"
                                            } else {
                                                if (isBengali) "এইচডি ক্লাস" else "HD Class"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = lesson.safeDuration,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF3B82F6),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Cloud Synced status indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Synced",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (savedPercentageInt > 0) {
                                        "$savedPercentageInt% ${if (isBengali) "দেখা হয়েছে" else "Watched"}"
                                    } else {
                                        if (isBengali) "ক্লাউড সিঙ্কড" else "Cloud Synced"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentVideoTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                lineHeight = 23.sp
                            ),
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "👨‍🏫 $currentInstructor",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                            )
                            Text(
                                text = "•",
                                color = if (isDark) Color(0xFF64748B) else Color(0xFFCBD5E1)
                            )
                            Text(
                                text = lesson.safeChannelOrSource,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                            )
                        }

                        // Progress bar if watched
                        if (savedPercentageInt > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isBengali) "ভিডিও দেখার অগ্রগতি" else "Watch Progress",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "$savedPercentageInt%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = ExamGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (savedPercentageInt / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = ExamGold,
                                    trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }

                // 3. Quick Action Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Practice Quiz Button
                        Button(
                            onClick = {
                                val targetId = topic?.id ?: lesson.safeTopicId
                                val title = topicTitle
                                onStartQuiz?.invoke(AssessmentMode.PRACTICE, targetId, title)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("video_start_quiz_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "কুইজ টেস্ট দিন" else "Start Quiz",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Ask Doubt Button
                        OutlinedButton(
                            onClick = {
                                onOpenDoubtSolver?.invoke() ?: run {
                                    Toast.makeText(
                                        context,
                                        if (isBengali) "AI ডাউট সলভার প্রস্তুত হচ্ছে..." else "Opening Doubt Solver...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isDark) ExamCyan else Color(0xFF0284C7)
                            ),
                            border = BorderStroke(1.dp, if (isDark) ExamCyan.copy(alpha = 0.5f) else Color(0xFF0284C7).copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "ডাউট জিজ্ঞেস" else "Ask Doubt",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // 4. Tab Navigation Strip
                item {
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = { HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)) }
                    ) {
                        Tab(
                            selected = selectedTab == VideoTab.PLAYLIST,
                            onClick = { selectedTab = VideoTab.PLAYLIST },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistPlay,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBengali) "প্লেলিস্ট (${playlistItems.size.coerceAtLeast(1)})" else "Playlist",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == VideoTab.PLAYLIST) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == VideoTab.SUMMARY,
                            onClick = { selectedTab = VideoTab.SUMMARY },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Notes,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBengali) "টপিক নোটস" else "Notes",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == VideoTab.SUMMARY) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == VideoTab.QUIZ,
                            onClick = { selectedTab = VideoTab.QUIZ },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentTurnedIn,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBengali) "প্র্যাকটিস কুইজ" else "Practice",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == VideoTab.QUIZ) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                    }
                }

                // 5. Tab Content
                when (selectedTab) {
                    VideoTab.PLAYLIST -> {
                        if (playlistItems.isNotEmpty()) {
                            // Header with Auto-play Toggle
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBengali) "অধ্যায়ের পর্বসমূহ (${playlistItems.size}টি):" else "Episodes (${playlistItems.size}):",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                        )
                                    )

                                    // Auto Play Next Toggle
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isAutoPlayNext) ExamGold.copy(alpha = 0.15f) else (if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isAutoPlayNext) ExamGold else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                        ),
                                        modifier = Modifier.clickable { isAutoPlayNext = !isAutoPlayNext }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isAutoPlayNext) Icons.Default.Autorenew else Icons.Default.PlayDisabled,
                                                contentDescription = null,
                                                tint = if (isAutoPlayNext) ExamGold else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (isBengali) "অটো-প্লে" else "Auto-play",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                ),
                                                color = if (isAutoPlayNext) ExamGold else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                                            )
                                        }
                                    }
                                }
                            }

                            itemsIndexed(playlistItems) { index, pItem ->
                                val isCurrentActive = currentVideoSource == pItem.getEffectiveVideoSource() ||
                                        (index == activePlaylistItemIndex && currentVideoSource == lesson.getEffectiveVideoSource())
                                val pProgress = videoProgressMap[pItem.safeId]

                                PlaylistItemCard(
                                    item = pItem,
                                    index = index,
                                    isActive = isCurrentActive,
                                    isDark = isDark,
                                    isBengali = isBengali,
                                    progressPercentage = pProgress?.percentage?.toInt() ?: 0,
                                    onClick = {
                                        playEpisode(index, autoPlay = true)
                                    }
                                )
                            }
                        } else {
                            // Single video fallback card
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = lesson.safeTitle,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isDark) Color.White else Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "⏱ ${lesson.safeDuration} • 👨‍🏫 ${lesson.safeInstructorName}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    VideoTab.SUMMARY -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Video summary card
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                                    ),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = ExamGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = if (isBengali) "লেকচার সারসংক্ষেপ" else "Lesson Summary",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isDark) Color.White else Color(0xFF0F172A)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = lesson.safeSummary,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                lineHeight = 20.sp
                                            ),
                                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                                        )
                                    }
                                }

                                // Key takeaways
                                val takeaways = lesson.safeKeyTakeaways.ifEmpty {
                                    topic?.keyPoints ?: emptyList()
                                }
                                if (takeaways.isNotEmpty()) {
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                                        ),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = if (isBengali) "মূল শিক্ষণীয় বিষয়সমূহ (Key Takeaways)" else "Key Takeaways",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            takeaways.forEachIndexed { idx, point ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "${idx + 1}",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                ),
                                                                color = Color(0xFF3B82F6)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = point,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 13.sp,
                                                            lineHeight = 19.sp
                                                        ),
                                                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    VideoTab.QUIZ -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Quiz Promo Card
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFECFDF5)
                                    ),
                                    border = BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Psychology,
                                                        contentDescription = null,
                                                        tint = Color(0xFF10B981),
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = if (isBengali) "টপিক কুইজ টেস্ট" else "Topic Quiz Test",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                                )
                                                Text(
                                                    text = if (isBengali) "ভিডিও দেখার পর নিজেকে যাচাই করুন" else "Test what you learned from this video",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                val targetId = topic?.id ?: lesson.safeTopicId
                                                val title = topicTitle
                                                onStartQuiz?.invoke(AssessmentMode.PRACTICE, targetId, title)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF10B981)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isBengali) "কুইজ শুরু করুন (MCQ Test)" else "Start Topic Quiz Now",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    VideoTab.DOUBT -> {}
                }
            }
        }
    }
}

@Composable
private fun PlaylistItemCard(
    item: PlaylistItem,
    index: Int,
    isActive: Boolean,
    isDark: Boolean,
    isBengali: Boolean,
    progressPercentage: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFECFDF5)
            } else {
                if (isDark) Color(0xFF1E293B) else Color.White
            }
        ),
        border = BorderStroke(
            if (isActive) 1.6.dp else 1.dp,
            if (isActive) Color(0xFF10B981) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Part Number Badge / Animated GraphicEq playing status
            Surface(
                shape = CircleShape,
                color = if (isActive) Color(0xFF10B981) else (if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "এখন চলছে",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text = "${item.partNumber}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.safeTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        ),
                        color = if (isActive) {
                            if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                        } else {
                            if (isDark) Color.White else Color(0xFF0F172A)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "⏱ ${item.safeDuration}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    if (progressPercentage > 0) {
                        Text(
                            text = "• ${progressPercentage}% ${if (isBengali) "সম্পন্ন" else "done"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExamGold
                            )
                        )
                    }
                }
            }

            // Right side "Playing Now" status badge
            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    border = BorderStroke(0.8.dp, Color(0xFF10B981))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF34D399) else Color(0xFF047857),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isBengali) "এখন চলছে" else "Playing",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                        )
                    }
                }
            }
        }
    }
}
