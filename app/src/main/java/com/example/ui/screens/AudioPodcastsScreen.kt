package com.example.ui.screens

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AudioPodcast(
    val title: String,
    val subject: String,
    val duration: String,
    val narrator: String,
    val className: String = "Class 10"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPodcastsScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    classId: String = "class_10_wbbse",
    unlockedPodcasts: Set<String> = emptySet(),
    onUnlockPodcast: (title: String) -> Unit = {},
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
    var selectedClassFilter by remember(initialClassFilter) { mutableStateOf(initialClassFilter) }

    val podcasts = remember {
        listOf(
            // Class 10
            AudioPodcast("মহাবিদ্রোহ ১৮৫৭: কারণ ও প্রভাব (ইতিহাস)", "ইতিহাস (History)", "১২ মিনিট", "প্রফেসর এ. কে. সেন", "Class 10"),
            AudioPodcast("চলতড়িৎ ও ওহমের সূত্রের গাণিতিক শর্টকাট", "ভৌতবিজ্ঞান (Physical Science)", "১৫ মিনিট", "ড. সমীর রায়", "Class 10"),
            AudioPodcast("জীবের জনন ও বংশগতিবিদ্যা (Mendel's Laws)", "জীবনবিজ্ঞান (Life Science)", "১০ মিনিট", "ডা. সুপর্ণা দাস", "Class 10"),
            AudioPodcast("ভারতের নদনদী ও জলবায়ু রিভিশন", "ভূগোল (Geography)", "১৪ মিনিট", "অধ্যাপক এস. বোস", "Class 10"),
            AudioPodcast("কোনি উপন্যাস: ক্ষিতীশ সিংহ ও কোনির লড়াই", "বাংলা (Bengali)", "১৮ মিনিট", "শান্তনু চ্যাটার্জী", "Class 10"),
            // Class 12
            AudioPodcast("স্থিরতড়িৎ ও গাউসের সূত্র আলোচনা (HS 2026)", "পদার্থবিদ্যা (Physics)", "১৬ মিনিট", "ড. পার্থ মুখার্জী", "Class 12"),
            AudioPodcast("জৈব রসায়ন: অ্যালকোহল ও ইথার রূপান্তর", "রসায়নবিদ্যা (Chemistry)", "১৪ মিনিট", "প্রফেসর কল্যাণ সাহা", "Class 12"),
            AudioPodcast("বংশগতির আণবিক ভিত্তি ও DNA প্রতিলিপিকরণ", "জীববিজ্ঞান (Biology)", "১৩ মিনিট", "ডা. সুপর্ণা দাস", "Class 12"),
            AudioPodcast("উচ্চতর গণিত: কলনবিদ্যার গুরুত্বপূর্ণ সূত্রাবলী", "গণিত (Mathematics)", "১৫ মিনিট", "রমেন ঘোষ", "Class 12"),
            // Class 11
            AudioPodcast("নিউটনের গতিসূত্র ও ঘর্ষণ বলের ধারণা", "পদার্থবিদ্যা (Physics)", "১২ মিনিট", "ড. পার্থ মুখার্জী", "Class 11"),
            AudioPodcast("পর্যায় সারণি ও রাসায়নিক বন্ধন সারসংক্ষেপ", "রসায়নবিদ্যা (Chemistry)", "১১ মিনিট", "প্রফেসর কল্যাণ সাহা", "Class 11"),
            // Class 9
            AudioPodcast("বল ও গতি: সমত্বরণ ও গতির সমীকরণ", "ভৌতবিজ্ঞান (Physical Science)", "১০ মিনিট", "ড. সমীর রায়", "Class 9"),
            AudioPodcast("কোষ ও কলা: গঠন ও কার্যকারিতা", "জীবনবিজ্ঞান (Life Science)", "৯ মিনিট", "ডা. সুপর্ণা দাস", "Class 9"),
            // Class 8
            AudioPodcast("অণুজীবের জগৎ ও মানবদেহে প্রভাব", "বিজ্ঞান (Science)", "৮ মিনিট", "অনিন্দিতা ঘোষ", "Class 8"),
            // Class 7
            AudioPodcast("তাপ ও আলোর প্রকৃতি সহজ ভাষায়", "বিজ্ঞান (Science)", "৭ মিনিট", "অনিন্দিতা ঘোষ", "Class 7"),
            // Class 6
            AudioPodcast("আমাদের পরিবেশের জীববৈচিত্র্য", "বিজ্ঞান (Science)", "৬ মিনিট", "অনিন্দিতা ঘোষ", "Class 6"),
            // Class 5
            AudioPodcast("আমাদের পরিবেশ: জল ও মাটির গুরুত্ব", "পরিবেশ (Environment)", "৫ মিনিট", "অনিন্দিতা ঘোষ", "Class 5")
        )
    }

    val classOptions = listOf("ALL", "Class 12", "Class 11", "Class 10", "Class 9", "Class 8", "Class 7", "Class 6", "Class 5")

    val filteredPodcasts = podcasts.filter {
        selectedClassFilter == "ALL" || it.className == selectedClassFilter
    }

    val context = LocalContext.current
    var currentlyPlaying by remember { mutableStateOf<AudioPodcast?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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

            // Sponsored Learning Banner
            AdBannerCard(isBengali = isBengali)

            // Class Filter Chips Row
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(classOptions) { cls ->
                    FilterChip(
                        selected = selectedClassFilter == cls,
                        onClick = { selectedClassFilter = cls },
                        label = { Text(if (cls == "ALL") "সকল শ্রেণি" else cls) }
                    )
                }
            }

            Text(
                text = "উপলব্ধ পডকাস্ট লেকচার (${filteredPodcasts.size}টি):",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
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
                                    isPlaying = !isPlaying
                                } else {
                                    currentlyPlaying = podcast
                                    isPlaying = true
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
                                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF1E293B))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = playing.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White, maxLines = 1)
                                    Text(text = playing.narrator, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                                }
                                IconButton(onClick = { isPlaying = !isPlaying }) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                progress = { 0.45f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
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
}
