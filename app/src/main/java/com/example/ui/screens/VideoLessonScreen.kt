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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import com.example.ui.components.ScreenHeaderBar
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
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(1240) }

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

    val videoProgressMap by (viewModel?.videoProgressMap?.collectAsState() ?: remember { mutableStateOf(emptyMap()) })
    val currentSavedProgress = videoProgressMap[currentVideoId] ?: videoProgressMap[lesson.safeTopicId]
    val initialPositionMs = currentSavedProgress?.lastPositionMs ?: 0L

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val topicTitle = remember(topic, isBengali, lesson) {
        if (topic != null) {
            if (isBengali) topic.titleBengali else topic.titleEnglish
        } else {
            lesson.safeTitle
        }
    }

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
                ScreenHeaderBar(
                    title = if (isBengali) "ভিডিও ক্লাস ও লেকচার" else "Video Lecture",
                    subtitle = topicTitle,
                    isBengali = isBengali,
                    showBackButton = true,
                    onBack = onBack,
                    containerColor = if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface,
                    contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    actions = {
                        IconButton(
                            onClick = {
                                isBookmarked = !isBookmarked
                                val msg = if (isBookmarked) {
                                    if (isBengali) "ভিডিওটি সেভ করা হয়েছে" else "Saved to bookmarks"
                                } else {
                                    if (isBengali) "বুকমার্ক থেকে সরানো হয়েছে" else "Removed from bookmarks"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) ExamGold else (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
                            )
                        }

                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, currentVideoTitle)
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "📚 DeshExam এ ভিডিও ক্লাস দেখুন:\n\n🎥 $currentVideoTitle\n👨‍🏫 শিক্ষক: $currentInstructor\n\nঅ্যাপটি ডাউনলোড করুন!"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share",
                                tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
                            playerHeight = 230.dp,
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
                            onVideoSourceChanged = { newSrc -> currentVideoSource = newSrc },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 2. Video Title, Hashtags & Metadata Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF1E293B) else Color.White)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hashtags Row
                        Text(
                            text = "#মাধ্যমিক_বাংলা #জ্ঞানচক্ষু #আশাপূর্ণা_দেবী",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF4F46E5)
                        )

                        // Main Video Title
                        Text(
                            text = currentVideoTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.5.sp,
                                lineHeight = 23.sp
                            ),
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )

                        // Metrics Metadata Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "২.৮K শিক্ষার্থী দেখেছে • ⭐ ৪.৯ রেটিং • অধ্যায় ১",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                            Text(
                                text = "...আরও দেখুন",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF4F46E5),
                                modifier = Modifier.clickable {
                                    selectedTab = VideoTab.SUMMARY
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Channel / Instructor Card with "মক টেস্ট দিন" Action Button
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "গুরু",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = currentInstructor.ifBlank { "বাংলা সাহিত্য ক্লাসরুম" },
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            ),
                                            color = if (isDark) Color.White else Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "দশম শ্রেণী • WBBSE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val targetId = topic?.id ?: lesson.safeTopicId
                                        val title = topicTitle
                                        onStartQuiz?.invoke(AssessmentMode.PRACTICE, targetId, title)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEDE9FE),
                                        contentColor = Color(0xFF4F46E5)
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "মক টেস্ট দিন 🎯",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Scrollable Action Pills Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Like Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isLiked) Color(0xFFEEF2FF) else (if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
                                border = BorderStroke(1.dp, if (isLiked) Color(0xFF4F46E5) else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable {
                                    isLiked = !isLiked
                                    if (isLiked) likeCount++ else likeCount--
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                        contentDescription = "Like",
                                        tint = if (isLiked) Color(0xFF4F46E5) else Color(0xFF475569),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "${(likeCount / 1000.0)}K লাইক",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isLiked) Color(0xFF4F46E5) else Color(0xFF475569)
                                    )
                                }
                            }

                            // PDF Notes Button Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable {
                                    selectedTab = VideoTab.SUMMARY
                                    Toast.makeText(context, "📄 নোটস ট্যাবে বিস্তারিত দেখুন", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "PDF Notes",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "PDF নোটস ডাউনলোড",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = Color(0xFF475569)
                                    )
                                }
                            }

                            // AI Doubt Solver Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable {
                                    onOpenDoubtSolver?.invoke() ?: run {
                                        Toast.makeText(context, "✨ AI ডাউট সলভার প্রস্তুত হচ্ছে...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Solver",
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "AI ডাউট সলভার",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = Color(0xFF4F46E5)
                                    )
                                }
                            }

                            // Save Bookmark Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isBookmarked) Color(0xFFFEF3C7) else (if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
                                border = BorderStroke(1.dp, if (isBookmarked) ExamGold else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable {
                                    isBookmarked = !isBookmarked
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Save",
                                        tint = if (isBookmarked) Color(0xFFD97706) else Color(0xFF475569),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (isBookmarked) "সেভড" else "সেভ করুন",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isBookmarked) Color(0xFFD97706) else Color(0xFF475569)
                                    )
                                }
                            }

                            // Offline Download Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable {
                                    Toast.makeText(context, "⬇️ অফলাইন ডাউনলোড সুবিধা শীঘ্রই আসছে", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = "Offline",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "অফলাইন...",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Tab Navigation Bar (Matching Screenshot)
                item {
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                        contentColor = Color(0xFF4F46E5),
                        divider = { HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)) }
                    ) {
                        Tab(
                            selected = selectedTab == VideoTab.PLAYLIST,
                            onClick = { selectedTab = VideoTab.PLAYLIST },
                            text = {
                                Text(
                                    text = if (isBengali) "প্লেলিস্ট (${playlistItems.size.coerceAtLeast(1)}টি পর্ব)" else "Playlist (${playlistItems.size})",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (selectedTab == VideoTab.PLAYLIST) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == VideoTab.PLAYLIST) Color(0xFF4F46E5) else Color(0xFF64748B)
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == VideoTab.SUMMARY,
                            onClick = { selectedTab = VideoTab.SUMMARY },
                            text = {
                                Text(
                                    text = if (isBengali) "স্মার্ট নোটস" else "Smart Notes",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (selectedTab == VideoTab.SUMMARY) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == VideoTab.SUMMARY) Color(0xFF4F46E5) else Color(0xFF64748B)
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == VideoTab.QUIZ,
                            onClick = { selectedTab = VideoTab.QUIZ },
                            text = {
                                Text(
                                    text = if (isBengali) "অধ্যায়ভিত্তিক MCQ" else "Topic MCQ",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (selectedTab == VideoTab.QUIZ) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == VideoTab.QUIZ) Color(0xFF4F46E5) else Color(0xFF64748B)
                                )
                            }
                        )
                    }
                }

                // 4. Tab Content (Matching Screenshot)
                when (selectedTab) {
                    VideoTab.PLAYLIST -> {
                        if (playlistItems.isNotEmpty()) {
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
                            item {
                                PlaylistItemCard(
                                    item = PlaylistItem(
                                        id = lesson.safeId,
                                        partNumber = 1,
                                        title = lesson.safeTitle,
                                        titleBengali = lesson.safeTitle,
                                        duration = lesson.safeDuration,
                                        youtubeVideoId = lesson.getEffectiveVideoSource(),
                                        instructorName = lesson.safeInstructorName,
                                        description = "মূল গল্প ও তপনের চরিত্র বিশ্লেষণ"
                                    ),
                                    index = 0,
                                    isActive = true,
                                    isDark = isDark,
                                    isBengali = isBengali,
                                    progressPercentage = 0,
                                    onClick = {}
                                )
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
                                                text = if (isBengali) "স্মার্ট নোটস ও সারসংক্ষেপ" else "Smart Notes & Summary",
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
                                                    text = if (isBengali) "মূল পয়েন্ট ও পরীক্ষার টিপস" else "Key Exam Points",
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
                                                        color = Color(0xFF4F46E5).copy(alpha = 0.2f),
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "${idx + 1}",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                ),
                                                                color = Color(0xFF4F46E5)
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
                                                    text = if (isBengali) "অধ্যায়ভিত্তিক MCQ কুইজ" else "Topic MCQ Quiz",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                                )
                                                Text(
                                                    text = if (isBengali) "ভিডিও দেখার পর প্রশ্ন প্র্যাকটিস করে নিজেকে পরখ করুন" else "Practice MCQ questions from this topic",
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
    val videoId = VideoLesson.extractYoutubeId(item.getEffectiveVideoSource())
    val thumbnailUrl = videoId.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF)
            } else {
                if (isDark) Color(0xFF1E293B) else Color.White
            }
        ),
        border = BorderStroke(
            if (isActive) 1.5.dp else 1.dp,
            if (isActive) Color(0xFF4F46E5) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Side: YouTube Thumbnail with Play Icon & Compact Duration Badge
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 70.dp)
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
                if (!thumbnailUrl.isBlank()) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = "Video Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                }

                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                    ) {
                        Text(
                            text = "চলছে...",
                            color = Color(0xFF10B981),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Icon(
                    imageVector = if (isActive) Icons.Default.GraphicEq else Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = if (isActive) Color(0xFF34D399) else Color.White,
                    modifier = Modifier.size(32.dp)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = item.safeDuration,
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Middle Column: Title & Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.safeTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    ),
                    color = if (isActive) {
                        if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
                    } else {
                        if (isDark) Color.White else Color(0xFF0F172A)
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (item.safeDescription.isNotBlank()) item.safeDescription else "বোর্ড স্ট্যান্ডার্ড • পার্ট ${item.partNumber}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Action Icon (Download or Checkmark)
            IconButton(
                onClick = { onClick() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.FileDownload else Icons.Default.CheckCircle,
                    contentDescription = "Action",
                    tint = if (isActive) Color(0xFF4F46E5) else Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
