package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdBannerCard
import com.example.ui.components.AdRewardType
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
    val audioUrl: String = ""
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
                            audioUrl = obj.optString("audioUrl", "")
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
            AudioPodcast("মহাবিদ্রোহ ১৮৫৭: কারণ ও প্রভাব (ইতিহাস)", "ইতিহাস (History)", "১২ মিনিট", "প্রробнее এ. কে. সেন", "Class 10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
            AudioPodcast("চলতড়িৎ ও ওহমের সূত্রের গাণিতিক শর্টকাট", "ভৌতবিজ্ঞান (Physical Science)", "১৫ মিনিট", "ড. সমীর রায়", "Class 10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"),
            AudioPodcast("জীবের জনন ও বংশগতিবিদ্যা (Mendel's Laws)", "जीवनবিজ্ঞান (Life Science)", "১০ মিনিট", "ডা. সুপর্ণা দাস", "Class 10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"),
            AudioPodcast("ভারতের নদনদী ও জলবায়ু রিভিশন", "ভূগোল (Geography)", "১৪ মিনিট", "অধ্যাপক এস. বোস", "Class 10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"),
            AudioPodcast("কোনি উপন্যাস: ক্ষিতীশ সিংহ ও কোনির লড়াই", "বাংলা (Bengali)", "১৮ মিনিট", "শান্তনু চ্যাটার্জী", "Class 10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"),
            AudioPodcast("স্থিরতড়িৎ ও গাউসের সূত্র আলোচনা (HS 2026)", "পদার্থবিদ্যা (Physics)", "১৬ মিনিট", "ড. পার্থ মুখার্জী", "Class 12", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"),
            AudioPodcast("জৈব রসায়ন: অ্যালকোহল ও ইথার রূপান্তর", "রসায়নবিদ্যা (Chemistry)", "১৪ মিনিট", "প্রফেসর কল্যাণ সাহা", "Class 12", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3"),
            AudioPodcast("বংশগতির আণবিক ভিত্তি ও DNA প্রতিলিপিকরণ", "জীববিজ্ঞান (Biology)", "১৩ মিনিট", "ডা. সুপর্ণা দাস", "Class 12", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"),
            AudioPodcast("উচ্চতর গণিত: কলনবিদ্যার গুরুত্বপূর্ণ সূত্রাবলী", "গণিত (Mathematics)", "১৫ মিনিট", "রমেন ঘোষ", "Class 12", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3"),
            AudioPodcast("নিউটনের গতিসূত্র ও ঘর্ষণ বলের ধারণা", "পদার্থবিদ্যা (Physics)", "১২ মিনিট", "ড. পার্থ মুখার্জী", "Class 11", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"),
            AudioPodcast("পর্যায় সারণি ও রাসায়নিক বন্ধন সারসংক্ষেপ", "রসায়নবিদ্যা (Chemistry)", "১১ মিনিট", "প্রফেসর কল্যাণ সাহা", "Class 11", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3"),
            AudioPodcast("বল ও গতি: সমত্বরণ ও গতির সমীকরণ", "ভৌতবিজ্ঞান (Physical Science)", "১০ মিনিট", "ড. সমীর রায়", "Class 9", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3"),
            AudioPodcast("কোষ ও কলা: গঠন ও কার্যকারিতা", "জীবনবিজ্ঞান (Life Science)", "৯ মিনিট", "ডা. সুপর্ণা দাস", "Class 9", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3"),
            AudioPodcast("অণুজীবের জগৎ ও মানবদেহে প্রভাব", "বিজ্ঞান (Science)", "৮ মিনিট", "অনিন্দিতা ঘোষ", "Class 8", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3"),
            AudioPodcast("তাপ ও আলোর প্রকৃতি সহজ ভাষায়", "বিজ্ঞান (Science)", "৭ মিনিট", "অনিন্দিতা ঘোষ", "Class 7", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3"),
            AudioPodcast("আমাদের পরিবেশের জীববৈচিত্র্য", "বিজ্ঞান (Science)", "৬ মিনিট", "অনিন্দিতা ঘোষ", "Class 6", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3"),
            AudioPodcast("আমাদের পরিবেশ: জল ও মাটির গুরুত্ব", "পরিবেশ (Environment)", "৫ মিনিট", "অনিন্দিতা ঘোষ", "Class 5", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
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
    classId: String = "class_10_wbbse",
    unlockedPodcasts: Set<String> = emptySet(),
    onUnlockPodcast: (title: String) -> Unit = {},
    viewModel: com.example.ui.viewmodel.DeshExamViewModel? = null,
    modifier: Modifier = Modifier
) {
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

    LaunchedEffect(initialClassFilter, viewModel) {
        viewModel?.fetchCustomPodcasts(initialClassFilter)
    }

    val mappedCustomPodcasts = remember(customPodcastsList) {
        customPodcastsList.map { p ->
            AudioPodcast(
                title = p.title,
                subject = p.subject,
                duration = p.duration.ifBlank { "৫ মিনিট" },
                narrator = p.narrator.ifBlank { "সহায়ক শিক্ষক" },
                className = p.className,
                audioUrl = p.audioUrl
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
                    subject = if (isBengali) "${displayName} (প্রশ্ন ব্যাখ্যা)" else "${displayName} (Question Podcast)",
                    duration = "৩ মিনিট",
                    narrator = if (isBengali) "সহায়ক শিক্ষক" else "Assistant Teacher",
                    className = initialClassFilter,
                    audioUrl = q.podcastUrl
                )
            }
            questionPodcasts = mapped
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val combinedPodcasts = remember(podcasts, questionPodcasts, mappedCustomPodcasts) {
        podcasts + questionPodcasts + mappedCustomPodcasts
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
            AudioPodcastManager.onPlayPauseAction?.invoke()
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
            // Unified Custom Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(32.dp).testTag("header_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "অডিও পডকাস্ট",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "স্মার্ট রিভিশন নোটস ও লেকচার",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
            // Hero Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF3730A3))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Headphones, contentDescription = null, tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "স্মার্ট অডিও লার্নিং",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "চোখ বন্ধ করে শুনে মুখস্থ করুন কঠিন উত্তর ও সাল-তারিখ।",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

            // Sponsored Learning Banner
            item {
                AdBannerCard(isBengali = isBengali)
            }

            // Subject Filter Chips Row
            item {
                androidx.compose.foundation.lazy.LazyRow(
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
                    "$classInBengali শ্রেণির উপলব্ধ পডকাস্ট লেকচার (${filteredPodcasts.size}টি):"
                } else {
                    "Available $initialClassFilter Podcasts (${filteredPodcasts.size}):"
                }
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(filteredPodcasts) { podcast ->
                    val isSelected = currentlyPlaying == podcast
                    val isDownloaded = localUnlockedPodcasts.contains(podcast.title)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (isSelected) ExamGold else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (currentlyPlaying == podcast) {
                                    togglePlayPauseInService()
                                } else {
                                    playPodcastInService(podcast)
                                }
                            }
                            .testTag("podcast_item_${podcast.subject}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ExamGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = ExamGold
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = podcast.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${podcast.subject} • ${podcast.duration} • ${podcast.narrator}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Offline download action button (Rewarded Ad)
                            IconButton(
                                onClick = {
                                    if (isDownloaded) {
                                        Toast.makeText(context, "ইতিমধ্যেই অফলাইনে সেভ করা আছে", Toast.LENGTH_SHORT).show()
                                    } else {
                                        podcastToUnlock = podcast
                                    }
                                },
                                modifier = Modifier.testTag("offline_podcast_${podcast.subject}")
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.Download,
                                    contentDescription = "Offline Download",
                                    tint = if (isDownloaded) CorrectGreen else ExamGold
                                )
                            }
                        }
                    }
                }
            }

            currentlyPlaying?.let { playing ->
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF1E293B))
                                )
                            )
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playing.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${playing.subject} • ${playing.narrator}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.75f),
                                        maxLines = 1
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { togglePlayPauseInService() },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                        contentDescription = "Play/Pause",
                                        tint = ExamGold,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { AudioPodcastManager.onStopAction?.invoke() },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Stop",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                progress = { AudioPodcastManager.currentProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ExamGold,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
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
                        "🎉 '${pod.title}' অফলাইনে শোনার জন্য আনলক হয়েছে! (+50 XP, +20 কয়েন)",
                        Toast.LENGTH_LONG
                    ).show()
                    podcastToUnlock = null
                },
                onDismiss = { podcastToUnlock = null }
            )
        }
    }
