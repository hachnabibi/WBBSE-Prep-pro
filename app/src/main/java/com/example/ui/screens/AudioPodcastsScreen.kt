package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdBannerCard
import com.example.ui.components.AdRewardType
import com.example.ui.components.PodcastMiniPlayer
import com.example.ui.components.RewardedAdDialog
import com.example.ui.theme.*
import com.example.util.AudioPodcastManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AudioPodcast(
    val title: String,
    val subject: String,
    val duration: String,
    val narrator: String,
    val className: String = "Class 10",
    val audioUrl: String = "",
    val boardId: String = "WBBSE"
)

fun loadPodcastsFromAssets(context: android.content.Context, selectedClass: String): List<AudioPodcast> {
    val classesToLoad = if (selectedClass == "ALL") {
        listOf("Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12")
    } else {
        listOf(selectedClass)
    }

    val result = mutableListOf<AudioPodcast>()

    for (cls in classesToLoad) {
        val assetPath = when (cls) {
            "Class 5" -> "data/class_5/podcasts.json"
            "Class 6" -> "data/class_6/podcasts.json"
            "Class 7" -> "data/class_7/podcasts.json"
            "Class 8" -> "data/class_8/podcasts.json"
            "Class 9" -> "data/class_9/podcasts.json"
            "Class 10" -> "data/class_10/podcasts.json"
            "Class 11" -> "data/wbchse/class_11/podcasts.json"
            "Class 12" -> "data/wbchse/class_12/podcasts.json"
            else -> null
        }

        val boardForClass = when (cls) {
            "Class 11", "Class 12" -> "WBCHSE"
            "Class 1", "Class 2", "Class 3", "Class 4" -> "WBBPE"
            else -> "WBBSE"
        }

        if (assetPath != null) {
            try {
                val jsonString = context.assets.open(assetPath).bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    result.add(
                        AudioPodcast(
                            title = obj.optString("title", ""),
                            subject = obj.optString("subject", ""),
                            duration = obj.optString("duration", ""),
                            narrator = obj.optString("narrator", ""),
                            className = obj.optString("className", cls),
                            audioUrl = obj.optString("audioUrl", ""),
                            boardId = obj.optString("boardId", boardForClass)
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Graceful fallback to static list if everything is empty
    if (result.isEmpty()) {
        val fallback = listOf(
            AudioPodcast("মহাবিদ্রোহ ১৮৫৭: কারণ ও প্রভাব", "ইতিহাস", "১২ মিনিট", "প্রফেসর এ. কে. সেন", "Class 10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "WBBSE"),
            AudioPodcast("চলতড়িৎ ও ওহমের সূত্রের গাণিতিক শর্টকাট", "ভৌতবিজ্ঞান", "১৫ মিনিট", "ড. সমীর রায়", "Class 10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "WBBSE"),
            AudioPodcast("জীবের জনন ও বংশগতিবিদ্যা", "জীবনবিজ্ঞান", "১০ মিনিট", "ডা. সুপর্ণা দাস", "Class 10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "WBBSE"),
            AudioPodcast("ভারতের নদনদী ও জলবায়ু রিভিশন", "ভূগোল", "১৪ মিনিট", "অধ্যাপক এস. বোস", "Class 10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3", "WBBSE"),
            AudioPodcast("কোনি উপন্যাস: ক্ষিতীশ সিংহ ও কোনির লড়াই", "বাংলা", "১৮ মিনিট", "শান্তনু চ্যাটার্জী", "Class 10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3", "WBBSE"),
            AudioPodcast("স্থিরতড়িৎ ও গাউসের সূত্র আলোচনা", "পদার্থবিদ্যা", "১৬ মিনিট", "ড. পার্থ মুখার্জী", "Class 12", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3", "WBCHSE"),
            AudioPodcast("জৈব রসায়ন: অ্যালকোহল ও ইথার রূপান্তর", "রসায়নবিদ্যা", "১৪ মিনিট", "প্রফেসর কল্যাণ সাহা", "Class 12", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3", "WBCHSE"),
            AudioPodcast("বংশগতির আণবিক ভিত্তি ও ডিএনএ প্রতিলিপিকরণ", "জীববিজ্ঞান", "১৩ মিনিট", "ডা. সুপর্ণা দাস", "Class 12", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3", "WBCHSE"),
            AudioPodcast("উচ্চতর গণিত: কলনবিদ্যার গুরুত্বপূর্ণ সূত্রাবলী", "গণিত", "১৫ মিনিট", "রমেন ঘোষ", "Class 12", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3", "WBCHSE"),
            AudioPodcast("নিউটনের গতিসূত্র ও ঘর্ষণ বলের ধারণা", "পদার্থবিদ্যা", "১২ মিনিট", "ড. পার্থ মুখার্জী", "Class 11", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3", "WBCHSE"),
            AudioPodcast("পর্যায় সারণি ও রাসায়নিক বন্ধন সারসংক্ষেপ", "রসায়নবিদ্যা", "১১ মিনিট", "প্রফেসর কল্যাণ সাহা", "Class 11", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3", "WBCHSE"),
            AudioPodcast("বল ও গতি: সমত্বরণ ও গতির সমীকরণ", "ভৌতবিজ্ঞান", "১০ মিনিট", "ড. সমীর রায়", "Class 9", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3", "WBBSE"),
            AudioPodcast("কোষ ও কলা: গঠন ও কার্যকারিতা", "জীবনবিজ্ঞান", "৯ মিনিট", "ডা. সুপর্ণা দাস", "Class 9", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3", "WBBSE"),
            AudioPodcast("অণুজীবের জগৎ ও মানবদেহে প্রভাব", "বিজ্ঞান", "৮ মিনিট", "অনিন্দিতা ঘোষ", "Class 8", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3", "WBBSE"),
            AudioPodcast("তাপ ও আলোর প্রকৃতি সহজ ভাষায়", "বিজ্ঞান", "৭ মিনিট", "অনিন্দিতা ঘোষ", "Class 7", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3", "WBBSE"),
            AudioPodcast("আমাদের পরিবেশের জীববৈচিত্র্য", "বিজ্ঞান", "৬ মিনিট", "অনিন্দিতা ঘোষ", "Class 6", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3", "WBBSE"),
            AudioPodcast("আমাদের পরিবেশ: জল ও মাটির গুরুত্ব", "পরিবেশ", "৫ মিনিট", "অনিন্দিতা ঘোষ", "Class 5", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "WBBSE")
        )
        return if (selectedClass == "ALL") {
            fallback
        } else {
            fallback.filter { it.className == selectedClass }
        }
    }

    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPodcastsScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    classId: String = "class_10_wbbse",
    unlockedPodcasts: Set<String> = emptySet(),
    onUnlockPodcast: (title: String) -> Unit = {},
    viewModel: com.example.ui.viewmodel.DeshExamViewModel? = null,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val initialClassFilter = remember(classId) {
        when {
            classId.contains("class_5") -> "Class 5"
            classId.contains("class_6") -> "Class 6"
            classId.contains("class_7") -> "Class 7"
            classId.contains("class_8") -> "Class 8"
            classId.contains("class_9") -> "Class 9"
            classId.contains("class_11") -> "Class 11"
            classId.contains("class_12") -> "Class 12"
            else -> "Class 10"
        }
    }

    val initialBoardFilter = remember(classId) {
        when {
            classId.contains("wbchse", ignoreCase = true) -> "WBCHSE"
            classId.contains("wbbpe", ignoreCase = true) -> "WBBPE"
            classId.contains("cbse", ignoreCase = true) -> "CBSE"
            else -> "WBBSE"
        }
    }

    val context = LocalContext.current
    // podcasts loaded only for user's onboarding class
    val podcasts = remember(initialClassFilter) {
        loadPodcastsFromAssets(context, initialClassFilter)
    }

    val customPodcastsList by if (viewModel != null) {
        viewModel.customPodcasts.collectAsState()
    } else {
        remember { mutableStateOf(emptyList<com.example.data.firestore.FirestorePodcast>()) }
    }

    LaunchedEffect(initialClassFilter, initialBoardFilter, viewModel) {
        viewModel?.fetchCustomPodcasts(initialClassFilter, initialBoardFilter)
    }

    val mappedCustomPodcasts = remember(customPodcastsList, initialClassFilter, initialBoardFilter) {
        customPodcastsList
            .filter { p ->
                val matchesClass = p.className.isBlank() ||
                    p.className == "ALL" ||
                    p.className.equals(initialClassFilter, ignoreCase = true) ||
                    p.className.replace(" ", "").equals(initialClassFilter.replace(" ", ""), ignoreCase = true)
                val matchesBoard = p.boardId.isBlank() ||
                    p.boardId == "ALL" ||
                    p.boardId.equals(initialBoardFilter, ignoreCase = true)
                matchesClass && matchesBoard
            }
            .map { p ->
                AudioPodcast(
                    title = p.title,
                    subject = p.subject,
                    duration = p.duration.ifBlank { "৫ মিনিট" },
                    narrator = p.narrator.ifBlank { "সহায়ক শিক্ষক" },
                    className = p.className,
                    audioUrl = p.audioUrl,
                    boardId = p.boardId.ifBlank { if (p.className.contains("11") || p.className.contains("12")) "WBCHSE" else "WBBSE" }
                )
            }
    }

    var questionPodcasts by remember { mutableStateOf<List<AudioPodcast>>(emptyList()) }
    LaunchedEffect(initialClassFilter) {
        try {
            val db = com.example.data.local.DeshExamDatabase.getDatabase(context, this)
            val dao = db.deshExamDao()
            val classIdPattern = when (initialClassFilter) {
                "Class 5" -> "class_5"
                "Class 6" -> "class_6"
                "Class 7" -> "class_7"
                "Class 8" -> "class_8"
                "Class 9" -> "class_9"
                "Class 11" -> "class_11"
                "Class 12" -> "class_12"
                else -> "class_10"
            }
            
            val dbQuestions = dao.getAllQuestions()
            val filteredQs = dbQuestions.filter { q ->
                (q.classId.contains(classIdPattern) || q.classId.isBlank()) && !q.podcastUrl.isNullOrBlank()
            }
            
            val subjects = dao.getAllSubjectsOnce()
            val subjectIdToName = subjects.associate { it.id to it.name }
            
            val mapped = filteredQs.map { q ->
                val subName = subjectIdToName[q.subjectId] ?: q.subjectId
                val displayName = if (isBengali) {
                    when {
                        subName.contains("ইতিহাস") || subName.lowercase().contains("history") -> "ইতিহাস"
                        subName.contains("ভূগোল") || subName.lowercase().contains("geography") -> "ভূগোল"
                        subName.contains("জীবনবিজ্ঞান") || subName.lowercase().contains("life_science") || subName.lowercase().contains("life science") -> "জীবনবিজ্ঞান"
                        subName.contains("ভৌতবিজ্ঞান") || subName.lowercase().contains("physical_science") || subName.lowercase().contains("physical science") -> "ভৌতবিজ্ঞান"
                        subName.contains("গণিত") || subName.lowercase().contains("mathematics") || subName.lowercase().contains("math") -> "গণিত"
                        subName.contains("ইংরেজি") || subName.lowercase().contains("english") -> "ইংরেজি"
                        subName.contains("বাংলা") || subName.lowercase().contains("bengali") -> "বাংলা"
                        else -> subName
                    }
                } else {
                    subName
                }
                
                AudioPodcast(
                    title = if (!q.podcastTitle.isNullOrBlank()) q.podcastTitle else q.question,
                    subject = if (isBengali) "${displayName} - প্রশ্ন ব্যাখ্যা" else "${displayName} Question Podcast",
                    duration = "৩ মিনিট",
                    narrator = if (isBengali) "সহায়ক শিক্ষক" else "Assistant Teacher",
                    className = initialClassFilter,
                    audioUrl = q.podcastUrl,
                    boardId = initialBoardFilter
                )
            }
            questionPodcasts = mapped
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val combinedPodcasts = remember(podcasts, questionPodcasts, mappedCustomPodcasts) {
        (podcasts + questionPodcasts + mappedCustomPodcasts).map { p ->
            val cleanedSub = p.subject.replace(Regex("\\s*\\([A-Za-z\\s&]+\\)\\s*"), "").trim()
            if (cleanedSub != p.subject) p.copy(subject = cleanedSub) else p
        }
    }

    // List of subjects available in user's class podcasts
    val subjectOptions = remember(combinedPodcasts) {
        val list = combinedPodcasts.map { it.subject }.distinct()
        listOf("ALL") + list
    }

    var selectedSubjectFilter by remember(initialClassFilter) { mutableStateOf("ALL") }

    val filteredPodcasts = remember(combinedPodcasts, selectedSubjectFilter) {
        if (selectedSubjectFilter == "ALL") {
            combinedPodcasts
        } else {
            combinedPodcasts.filter { it.subject == selectedSubjectFilter }
        }
    }
    val currentlyPlaying = AudioPodcastManager.currentlyPlaying
    val isPlaying = AudioPodcastManager.isPlaying
    val playPodcastInService = remember {
        { podcast: AudioPodcast ->
            val intent = Intent(context, com.example.notification.AudioPodcastService::class.java).apply {
                action = com.example.notification.AudioPodcastService.ACTION_PLAY
                putExtra(com.example.notification.AudioPodcastService.EXTRA_TITLE, podcast.title)
                putExtra(com.example.notification.AudioPodcastService.EXTRA_SUBJECT, podcast.subject)
                putExtra(com.example.notification.AudioPodcastService.EXTRA_NARRATOR, podcast.narrator)
                putExtra(com.example.notification.AudioPodcastService.EXTRA_DURATION, podcast.duration)
                putExtra(com.example.notification.AudioPodcastService.EXTRA_CLASS, podcast.className)
                putExtra(com.example.notification.AudioPodcastService.EXTRA_URL, podcast.audioUrl)
                putExtra(com.example.notification.AudioPodcastService.EXTRA_BOARD_ID, podcast.boardId)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    val togglePlayPauseInService = remember {
        {
            AudioPodcastManager.togglePlayPause(context)
        }
    }

    val currentTrackIndex = remember(filteredPodcasts, currentlyPlaying) {
        if (currentlyPlaying == null) -1
        else filteredPodcasts.indexOfFirst { it.title == currentlyPlaying.title }
    }
    val hasPreviousTrack = currentTrackIndex > 0
    val hasNextTrack = currentTrackIndex in 0 until (filteredPodcasts.size - 1)

    val onPreviousTrack: () -> Unit = remember(filteredPodcasts, currentTrackIndex) {
        {
            if (currentTrackIndex > 0) {
                playPodcastInService(filteredPodcasts[currentTrackIndex - 1])
            }
        }
    }
    val onNextTrack: () -> Unit = remember(filteredPodcasts, currentTrackIndex) {
        {
            if (currentTrackIndex in 0 until (filteredPodcasts.size - 1)) {
                playPodcastInService(filteredPodcasts[currentTrackIndex + 1])
            }
        }
    }

    val localUnlockedPodcasts = remember(unlockedPodcasts) { mutableStateListOf<String>().apply { addAll(unlockedPodcasts) } }
    var podcastToUnlock by remember { mutableStateOf<AudioPodcast?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Cohesive Compact Podcast Header Bar
            Surface(
                color = Color.Transparent,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF3730A3))
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Menu Icon replacing Back Icon
                        IconButton(
                            onClick = { onOpenDrawer?.invoke() ?: onBack() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("header_menu_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBengali) "অডিও পডকাস্ট" else "Audio Podcasts",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                            )
                            Text(
                                text = if (isBengali) "স্মার্ট রিভিশন নোটস ও লেকচার" else "Smart Audio Learning & Revision",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp
                                ),
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                            )
                        }

                        // Right side: Active playing status pill or class/board badges
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isPlaying) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (isBengali) "বাজছে" else "Playing",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Board Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f))
                            ) {
                                val bBoardHeader = when (initialBoardFilter.trim()) {
                                    "WBBSE" -> "মধ্যশিক্ষা পর্ষদ"
                                    "WBCHSE" -> "উচ্চমাধ্যমিক সংসদ"
                                    "WBBPE" -> "প্রাথমিক শিক্ষা পর্ষদ"
                                    "CBSE" -> "কেন্দ্রীয় পর্ষদ"
                                    "ALL" -> "সকল বোর্ড"
                                    else -> initialBoardFilter
                                }
                                Text(
                                    text = if (isBengali) bBoardHeader else initialBoardFilter,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            val classLabel = when (initialClassFilter) {
                                "Class 5" -> if (isBengali) "৫ম শ্রেণি" else "Class 5"
                                "Class 6" -> if (isBengali) "৬ষ্ঠ শ্রেণি" else "Class 6"
                                "Class 7" -> if (isBengali) "৭ম শ্রেণি" else "Class 7"
                                "Class 8" -> if (isBengali) "৮ম শ্রেণি" else "Class 8"
                                "Class 9" -> if (isBengali) "নবম শ্রেণি" else "Class 9"
                                "Class 10" -> if (isBengali) "১০ম শ্রেণি" else "Class 10"
                                "Class 11" -> if (isBengali) "একাদশ শ্রেণি" else "Class 11"
                                "Class 12" -> if (isBengali) "দ্বাদশ শ্রেণি" else "Class 12"
                                else -> initialClassFilter
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = classLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 0.dp,
                    end = 0.dp,
                    top = 0.dp,
                    bottom = if (currentlyPlaying != null) 80.dp else 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Edge-to-Edge Style Rich Hero Banner (0 Border Radius)
                item {
                    AudioPodcastHeroBanner(
                        isBengali = isBengali,
                        totalPodcastsCount = filteredPodcasts.size,
                        className = initialClassFilter,
                        boardName = initialBoardFilter,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            // Sponsored Learning Banner
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                    AdBannerCard(isBengali = isBengali)
                }
            }

            // Subject Filter Chips Row
            item {
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjectOptions) { sub ->
                        val labelText = if (sub == "ALL") {
                            if (isBengali) "সব বিষয়" else "All Subjects"
                        } else {
                            sub
                        }
                        FilterChip(
                            selected = selectedSubjectFilter == sub,
                            onClick = { selectedSubjectFilter = sub },
                            label = { Text(labelText) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExamGold.copy(alpha = 0.2f),
                                selectedLabelColor = ExamGold
                            )
                        )
                    }
                }
            }

            item {
                val classInBengali = when (initialClassFilter) {
                    "Class 5" -> "৫ম"
                    "Class 6" -> "৬ষ্ঠ"
                    "Class 7" -> "৭ম"
                    "Class 8" -> "৮ম"
                    "Class 9" -> "৯ম"
                    "Class 10" -> "১০ম"
                    "Class 11" -> "১১শ"
                    "Class 12" -> "দ্বাদশ"
                    else -> initialClassFilter
                }
                val headerText = if (isBengali) {
                    "$classInBengali শ্রেণির উপলব্ধ পডকাস্ট লেকচার (${com.example.ui.components.toBengaliDigits(filteredPodcasts.size.toString())}টি):"
                } else {
                    "Available $initialClassFilter Podcasts (${filteredPodcasts.size}):"
                }
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )
            }

            // Compact Podcast Lecture Cards
            items(filteredPodcasts) { podcast ->
                    val isSelected = currentlyPlaying?.title == podcast.title
                    val isDownloaded = localUnlockedPodcasts.contains(podcast.title)

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 0.75.dp,
                            color = if (isSelected) ExamGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .clickable {
                                if (currentlyPlaying?.title == podcast.title) {
                                    togglePlayPauseInService()
                                } else {
                                    playPodcastInService(podcast)
                                }
                            }
                            .testTag("podcast_item_${podcast.subject}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Compact Circular Play/Pause Indicator
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected && isPlaying) ExamGold
                                        else ExamGold.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected && AudioPodcastManager.isBuffering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = if (isPlaying) Color.Black else ExamGold,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isSelected && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isSelected && isPlaying) "Pause" else "Play",
                                        tint = if (isSelected && isPlaying) Color.Black else ExamGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Details Column
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Badges row: Subject pill + Duration pill + Playing pill
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF6366F1).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = com.example.ui.components.cleanPodcastSubject(podcast.subject),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6366F1),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = podcast.duration,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }

                                    if (isSelected && isPlaying) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF10B981))
                                                )
                                                Text(
                                                    text = if (isBengali) "চলছে" else "Playing",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10B981)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Title
                                Text(
                                    text = com.example.ui.components.cleanPodcastTitle(podcast.title),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        lineHeight = 18.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Narrator
                                Text(
                                    text = "🎙 ${podcast.narrator}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Offline download action button (Rewarded Ad)
                            IconButton(
                                onClick = {
                                    if (isDownloaded) {
                                        Toast.makeText(context, if (isBengali) "ইতিমধ্যেই অফলাইনে সেভ করা আছে" else "Already saved offline", Toast.LENGTH_SHORT).show()
                                    } else {
                                        podcastToUnlock = podcast
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("offline_podcast_${podcast.subject}")
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.Download,
                                    contentDescription = "Offline Download",
                                    tint = if (isDownloaded) CorrectGreen else ExamGold,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Persistent Mini-Player Component docked above bottom toolbar
        PodcastMiniPlayer(
            podcast = currentlyPlaying,
            isPlaying = isPlaying,
            progress = AudioPodcastManager.currentProgress,
            currentPositionMs = AudioPodcastManager.currentPositionMs,
            durationMs = AudioPodcastManager.durationMs,
            playbackSpeed = AudioPodcastManager.playbackSpeed,
            isBengali = isBengali,
            hasPreviousTrack = hasPreviousTrack,
            hasNextTrack = hasNextTrack,
            onPlayPause = { togglePlayPauseInService() },
            onRewind10 = { AudioPodcastManager.seekRelative(-10000L) },
            onForward10 = { AudioPodcastManager.seekRelative(10000L) },
            onPreviousTrack = onPreviousTrack,
            onNextTrack = onNextTrack,
            onSeek = { fraction ->
                val targetMs = (fraction * AudioPodcastManager.durationMs).toLong()
                AudioPodcastManager.seekTo(targetMs)
            },
            onSpeedChange = { speed ->
                AudioPodcastManager.setSpeed(speed)
            },
            onClose = {
                AudioPodcastManager.stop()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        )
    }

        podcastToUnlock?.let { pod ->
            RewardedAdDialog(
                rewardType = AdRewardType.OFFLINE_PODCAST,
                isBengali = isBengali,
                onRewardEarned = {
                    localUnlockedPodcasts.add(pod.title)
                    onUnlockPodcast(pod.title)
                    Toast.makeText(
                        context,
                        "🎉 '${pod.title}' অফলাইনে শোনার জন্য আনলক হয়েছে! (+৫০ পয়েন্ট, +২০ কয়েন)",
                        Toast.LENGTH_LONG
                    ).show()
                    podcastToUnlock = null
                },
                onDismiss = { podcastToUnlock = null }
            )
        }
    }

@Composable
fun AudioPodcastHeroBanner(
    isBengali: Boolean,
    totalPodcastsCount: Int,
    className: String,
    boardName: String,
    modifier: Modifier = Modifier
) {
    val bBoard = when (boardName.trim()) {
        "WBBSE" -> if (isBengali) "মধ্যশিক্ষা পর্ষদ" else "WBBSE"
        "WBCHSE" -> if (isBengali) "উচ্চমাধ্যমিক সংসদ" else "WBCHSE"
        "WBBPE" -> if (isBengali) "প্রাথমিক শিক্ষা পর্ষদ" else "WBBPE"
        "CBSE" -> if (isBengali) "কেন্দ্রীয় পর্ষদ" else "CBSE"
        else -> boardName
    }

    val bCls = when (className.trim()) {
        "Class 5" -> if (isBengali) "৫ম শ্রেণি" else "Class 5"
        "Class 6" -> if (isBengali) "৬ষ্ঠ শ্রেণি" else "Class 6"
        "Class 7" -> if (isBengali) "৭ম শ্রেণি" else "Class 7"
        "Class 8" -> if (isBengali) "৮ম শ্রেণি" else "Class 8"
        "Class 9" -> if (isBengali) "নবম শ্রেণি" else "Class 9"
        "Class 10" -> if (isBengali) "১০ম শ্রেণি" else "Class 10"
        "Class 11" -> if (isBengali) "একাদশ শ্রেণি" else "Class 11"
        "Class 12" -> if (isBengali) "দ্বাদশ শ্রেণি" else "Class 12"
        else -> className
    }

    Card(
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_podcast_hero_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E1B4B), // Deep Indigo
                            Color(0xFF312E81), // Indigo 900
                            Color(0xFF3730A3), // Indigo 800
                            Color(0xFF4F46E5)  // Indigo 600
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Tag Row: Official Audio Hub Badge + Equalizer icon & Headphone badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isBengali) "স্মার্ট অডিও লার্নিং হাব" else "Smart Audio Learning Hub",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }

                    // Floating circular audio icon
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Title and Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = if (isBengali) "স্মার্ট অডিও লেকচার ও রিভিশন" else "Smart Audio Lecture Hub",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.5.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = if (isBengali)
                            "চোখ বন্ধ করে শুনে মুখস্থ করুন কঠিন অধ্যায়, সূত্র ও সাল-তারিখ। চলতে ফিরতে দ্রুত রিভিশন!"
                        else
                            "Listen and memorize key topics, formulas & dates effortlessly. Revise anywhere on the go!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        ),
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }

                // Feature Pills Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AudioHeroFeaturePill(
                        icon = Icons.Default.Bolt,
                        label = if (isBengali) "দ্রুত রিভিশন" else "Fast Revision"
                    )
                    AudioHeroFeaturePill(
                        icon = Icons.Default.VolumeUp,
                        label = if (isBengali) "এইচডি সাউন্ড" else "HD Sound"
                    )
                    AudioHeroFeaturePill(
                        icon = Icons.Default.CloudDone,
                        label = if (isBengali) "অফলাইন" else "Offline"
                    )
                    AudioHeroFeaturePill(
                        icon = Icons.Default.Speed,
                        label = if (isBengali) "স্পিড কন্ট্রোল" else "Speed"
                    )
                }

                // Badges & Counter Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isBengali) "${com.example.ui.components.toBengaliDigits(totalPodcastsCount.toString())}টি লেকচার" else "$totalPodcastsCount Lectures",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = bCls,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = bBoard,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioHeroFeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White
            )
        }
    }
}
