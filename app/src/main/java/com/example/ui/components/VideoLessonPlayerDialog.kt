package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PlaylistItem
import com.example.data.model.VideoLesson
import com.example.data.model.VideoLessonProvider
import com.example.ui.theme.ExamCyan
import com.example.ui.theme.ExamGold

private const val TAG = "VideoLessonPlayer"

@Composable
fun VideoLessonPlayerDialog(
    lesson: VideoLesson,
    onDismiss: () -> Unit,
    onStartQuiz: (() -> Unit)? = null,
    onVideoUrlChanged: ((String) -> Unit)? = null,
    viewModel: com.example.ui.viewmodel.DeshExamViewModel? = null
) {
    val context = LocalContext.current
    var currentVideoSource by remember(lesson) { mutableStateOf(lesson.getEffectiveVideoSource()) }
    var currentVideoTitle by remember(lesson) { mutableStateOf(lesson.safeTitle) }
    var showEditUrlDialog by remember { mutableStateOf(false) }
    var inputUrl by remember { mutableStateOf(lesson.getEffectiveVideoSource()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Playlist, 1: Summary & Notes
    var webViewError by remember(currentVideoSource) { mutableStateOf(false) }

    val isMp4 = remember(currentVideoSource) { VideoLesson.isDirectMediaUrl(currentVideoSource) }
    val effectiveYoutubeId = remember(currentVideoSource) { VideoLesson.extractYoutubeId(currentVideoSource) }

    val videoProgressMap by (viewModel?.videoProgressMap?.collectAsState() ?: remember { mutableStateOf(emptyMap()) })
    val savedProgress = videoProgressMap[lesson.safeId] ?: videoProgressMap[lesson.safeTopicId]

    val scrollState = rememberScrollState()

    // Ensure Android System Back Button safely closes only this dialog without quitting the app
    BackHandler {
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // 1. Top Bar with Close, Title & Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ করুন",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentVideoTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isMp4) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFFEF4444).copy(alpha = 0.25f),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = if (isMp4) "🎥 MP4 ভিডিও" else "▶️ YouTube",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isMp4) Color(0xFF34D399) else Color(0xFFF87171),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = "👨‍🏫 ${lesson.safeInstructorName}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Edit Custom Video Link (MP4 or YouTube)
                        IconButton(
                            onClick = { 
                                inputUrl = currentVideoSource
                                showEditUrlDialog = true 
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "ভিডিও লিংক পরিবর্তন",
                                tint = ExamGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Open in External Player / YouTube
                        IconButton(
                            onClick = {
                                openVideoExternally(context, currentVideoSource, isMp4, effectiveYoutubeId)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "বাহিরে খুলুন",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 2. Universal Video Player Frame with Native ExoPlayer & Speed Controls
                VideoPlayer(
                    videoUrl = currentVideoSource,
                    title = currentVideoTitle,
                    instructorName = lesson.safeInstructorName,
                    autoPlay = true,
                    playerHeight = 230.dp,
                    showTopBar = false,
                    showExternalButton = false,
                    showSpeedControls = true,
                    initialPositionMs = savedProgress?.lastPositionMs ?: 0L,
                    isCloudSynced = true,
                    onProgressUpdate = { posMs, durMs, speed, isComplete ->
                        viewModel?.saveVideoPlaybackProgress(
                            videoId = lesson.safeId.ifBlank { lesson.safeTopicId },
                            topicId = lesson.safeTopicId,
                            lessonTitle = currentVideoTitle,
                            videoUrl = currentVideoSource,
                            positionMs = posMs,
                            durationMs = durMs,
                            playbackSpeed = speed,
                            isCompleted = isComplete
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Quick Action Row & Instant Player Launcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B).copy(alpha = 0.95f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                openVideoExternally(context, currentVideoSource, isMp4, effectiveYoutubeId)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isMp4) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isMp4) Icons.Default.FileDownloadDone else Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = if (isMp4) Color(0xFF34D399) else Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isMp4) "🎥 সরাসরি ক্লাউড MP4 ভিডিও চলছে" else "▶️ YouTube প্লেয়ার (ফুলস্ক্রিন মোড)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = if (isMp4) Color(0xFF34D399) else ExamCyan
                            )
                            Text(
                                text = if (isMp4) "হোস্টিং সার্ভার থেকে সরাসরি বাফারিংহীন স্ট্রিমিং" else "সম্পূর্ণ ফুলস্ক্রিন ও এইচডি কোয়ালিটিতে দেখতে এখানে ট্যাপ করুন",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    if (onStartQuiz != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onStartQuiz()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ExamGold,
                                contentColor = Color(0xFF78350F)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "কুইজ টেস্ট",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // 4. Content Tabs: [📺 ভিডিও প্লেলিস্ট (Playlist)] & [📝 নোটস ও সারসংক্ষেপ]
                val playlist = lesson.safePlaylist
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0F172A),
                    contentColor = ExamGold,
                    divider = { HorizontalDivider(color = Color(0xFF334155)) }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (selectedTab == 0) ExamGold else Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ভিডিও প্লেলিস্ট (${playlist.size.coerceAtLeast(1)})",
                                    color = if (selectedTab == 0) ExamGold else Color(0xFF94A3B8),
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (selectedTab == 1) ExamGold else Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "নোটস ও সারাংশ",
                                    color = if (selectedTab == 1) ExamGold else Color(0xFF94A3B8),
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }

                // 5. Tab Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == 0) {
                        // PLAYLIST TAB
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📺 এই টপিকের ভিডিও লেকচার তালিকা:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF1F5F9)
                            )
                        }

                        if (playlist.isNotEmpty()) {
                            playlist.forEach { item ->
                                val itemSource = item.getEffectiveVideoSource()
                                val isCurrent = currentVideoSource == itemSource
                                val itemIsMp4 = item.isDirectMp4()

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isCurrent) Color(0xFF312E81) else Color(0xFF1E293B),
                                    border = BorderStroke(
                                        1.2.dp,
                                        if (isCurrent) ExamGold else Color(0xFF334155)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            currentVideoSource = itemSource
                                            currentVideoTitle = item.safeTitle
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isCurrent) ExamGold else Color.White.copy(alpha = 0.12f),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    if (isCurrent) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Playing",
                                                            tint = Color(0xFF0F172A),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "${item.partNumber}",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = item.safeTitle,
                                                        style = MaterialTheme.typography.titleSmall.copy(
                                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                                            fontSize = 13.sp
                                                        ),
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (isCurrent) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = ExamGold.copy(alpha = 0.25f)
                                                        ) {
                                                            Text(
                                                                text = "▶️ এখন চলছে",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                ),
                                                                color = ExamGold,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (itemIsMp4) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                                                    ) {
                                                        Text(
                                                            text = if (itemIsMp4) "MP4" else "YouTube",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                            color = if (itemIsMp4) Color(0xFF34D399) else Color(0xFFF87171),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = "⏱️ ${item.safeDuration} • 👨‍🏫 ${item.safeInstructorName}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                        color = Color(0xFF94A3B8)
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                currentVideoSource = itemSource
                                                currentVideoTitle = item.safeTitle
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCurrent) Icons.Default.VolumeUp else Icons.Default.PlayCircle,
                                                contentDescription = "Play",
                                                tint = if (isCurrent) ExamGold else Color(0xFFCBD5E1),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Single item fallback
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF312E81),
                                border = BorderStroke(1.2.dp, ExamGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ExamGold,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Playing",
                                                tint = Color(0xFF0F172A),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = lesson.safeTitle,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "▶️ পর্ব ১ • ⏱️ ${lesson.safeDuration}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = ExamGold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // SUMMARY & NOTES TAB
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = ExamCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ভিডিও ক্লাসের মূল সারাংশ",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = lesson.safeSummary,
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        val keyTakeaways = lesson.safeKeyTakeaways
                        if (keyTakeaways.isNotEmpty()) {
                            Text(
                                text = "🎯 গুরুত্বপূর্ণ পয়েন্টস ও পরীক্ষার টিপস:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            keyTakeaways.forEach { takeaway ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "• ",
                                        color = ExamGold,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = takeaway,
                                        color = Color(0xFFCBD5E1),
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Custom Video URL Edit Dialog (Supports MP4, WebM, Hosted Video Links & YouTube)
    if (showEditUrlDialog) {
        AlertDialog(
            onDismissRequest = { showEditUrlDialog = false },
            title = {
                Text(
                    text = "ভিডিও লিংক যোগ বা পরিবর্তন",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "এখানে যেকোনো হোস্টেড MP4 ডিরেক্ট লিংক (যেমন: https://.../video.mp4) অথবা যেকোনো ইউটিউব লিংক দিন:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        placeholder = { Text("https://.../video.mp4 বা YouTube লিংক") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ExamGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = inputUrl.trim()
                        if (trimmed.isNotBlank()) {
                            currentVideoSource = trimmed
                            VideoLessonProvider.setCustomVideoUrl(lesson.safeTopicId, trimmed)
                            onVideoUrlChanged?.invoke(trimmed)
                        }
                        showEditUrlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExamGold, contentColor = Color(0xFF78350F))
                ) {
                    Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUrlDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/**
 * Safely creates and configures WebView for robust video playback without crashing.
 */
@SuppressLint("SetJavaScriptEnabled")
/**
 * Safely opens video in YouTube app or external browser without throwing exceptions.
 */
private fun openVideoExternally(
    context: Context,
    videoSource: String,
    isMp4: Boolean,
    effectiveYoutubeId: String
) {
    try {
        val targetUrl = if (isMp4) {
            videoSource
        } else {
            "https://www.youtube.com/watch?v=$effectiveYoutubeId"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.w(TAG, "Cannot launch external video app: ${e.message}")
    }
}
