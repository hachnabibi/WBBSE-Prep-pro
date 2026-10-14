package com.example.ui.screens.admin

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.AudioPodcastManager
import com.example.notification.AudioPodcastService
import com.example.data.firestore.FirestorePodcast
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ExamGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel

/**
 * ট্যাব ৯: অডিও পডকাস্ট ম্যানেজমেন্ট (Audio Podcast Management)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPodcastScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean = true,
    onAction: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val customPds by viewModel.customPodcasts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCustomPodcasts()
    }

    var pTitle by remember { mutableStateOf("") }
    var pSubject by remember { mutableStateOf("") }
    var pDuration by remember { mutableStateOf("১০ মিনিট") }
    var pNarrator by remember { mutableStateOf("সহায়ক শিক্ষক") }
    var pBoardId by remember { mutableStateOf("WBBSE") }
    var pClassName by remember { mutableStateOf("Class 10") }
    var pAudioUrl by remember { mutableStateOf("") }
    var boardDropdownExp by remember { mutableStateOf(false) }
    var classDropdownExp by remember { mutableStateOf(false) }
    var subjectDropdownExp by remember { mutableStateOf(false) }
    var isSavingPodcast by remember { mutableStateOf(false) }

    val podcastBoardList = listOf(
        "WBBSE",
        "WBCHSE",
        "WBBPE",
        "CBSE",
        "ALL"
    )

    val getBoardDisplayName = { bId: String ->
        when (bId) {
            "ALL" -> "সকল বোর্ড"
            else -> bId
        }
    }

    val podcastClassList = listOf(
        "Class 10",
        "Class 12",
        "Class 11",
        "Class 9",
        "Class 8",
        "Class 7",
        "Class 6",
        "Class 5",
        "ALL"
    )

    val getClassDisplayName = { cls: String ->
        when (cls) {
            "Class 5" -> "পঞ্চম শ্রেণি"
            "Class 6" -> "ষষ্ঠ শ্রেণি"
            "Class 7" -> "সপ্তম শ্রেণি"
            "Class 8" -> "অষ্টম শ্রেণি"
            "Class 9" -> "নবম শ্রেণি"
            "Class 10" -> "দশম শ্রেণি (মাধ্যমিক)"
            "Class 11" -> "একাদশ শ্রেণি (উচ্চমাধ্যমিক)"
            "Class 12" -> "দ্বাদশ শ্রেণি (উচ্চমাধ্যমিক)"
            "ALL" -> "সকল শ্রেণি"
            else -> cls
        }
    }

    val getClassSubjects = { cls: String ->
        when (cls) {
            "Class 11", "Class 12" -> listOf(
                "পদার্থবিদ্যা",
                "রসায়নবিদ্যা",
                "জীববিজ্ঞান",
                "গণিত",
                "বাংলা",
                "ইংরেজি",
                "ইতিহাস",
                "ভূগোল"
            )
            "Class 9", "Class 10" -> listOf(
                "ইতিহাস",
                "ভূগোল",
                "জীবনবিজ্ঞান",
                "ভৌতবিজ্ঞান",
                "গণিত",
                "বাংলা",
                "ইংরেজি"
            )
            "Class 7", "Class 8" -> listOf(
                "বিজ্ঞান",
                "পরিবেশ ও বিজ্ঞান",
                "গণিত",
                "ইতিহাস",
                "ভূগোল",
                "বাংলা",
                "ইংরেজি"
            )
            "Class 5", "Class 6" -> listOf(
                "আমাদের পরিবেশ",
                "বিজ্ঞান",
                "গণিত",
                "বাংলা",
                "ইংরেজি"
            )
            else -> listOf(
                "ইতিহাস",
                "ভূগোল",
                "জীবনবিজ্ঞান",
                "ভৌতবিজ্ঞান",
                "গণিত",
                "বাংলা",
                "ইংরেজি",
                "সাধারণ লেকচার"
            )
        }
    }

    var pSearchQuery by remember { mutableStateOf("") }
    var pFilterBoard by remember { mutableStateOf("ALL") }
    var pFilterClass by remember { mutableStateOf("ALL") }

    var editingPodcast by remember { mutableStateOf<FirestorePodcast?>(null) }
    var deletingPodcast by remember { mutableStateOf<FirestorePodcast?>(null) }

    var pBulkInput by remember { mutableStateOf("") }
    var bulkUploadStatusMessage by remember { mutableStateOf<String?>(null) }
    var bulkUploadSuccess by remember { mutableStateOf(true) }

    val pBulkSample = """শিরোনাম | বিষয় | সময়কাল | বক্তা | শ্রেণি | বোর্ড | অডিও ফাইল লিংক
বঙ্গভঙ্গ আন্দোলন ও স্বদেশী যুগ | ইতিহাস | ১২ মিনিট | ড. সুদীপ বোস | Class 10 | WBBSE | https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3
স্থিরতড়িৎ ও গাউসের সূত্র | পদার্থবিদ্যা | ১৬ মিনিট | ড. পার্থ মুখার্জী | Class 12 | WBCHSE | https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3
উদ্ভিদের সংবেদনশীলতা | জীবনবিজ্ঞান | ১৫ মিনিট | শ্রীমতী সেন | Class 9 | WBBSE | https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"""

    val testPlayAudio: (String, String, String) -> Unit = { url, title, subject ->
        if (url.isBlank()) {
            onAction(false, "⚠️ অডিও লিংক ফাঁকা! কোনো লিংক দিয়ে টেস্ট করুন।")
        } else if (AudioPodcastManager.isPlaying && AudioPodcastManager.currentlyPlaying?.audioUrl == url) {
            AudioPodcastManager.onPlayPauseAction?.invoke()
        } else {
            val intent = Intent(context, AudioPodcastService::class.java).apply {
                action = AudioPodcastService.ACTION_PLAY
                putExtra(AudioPodcastService.EXTRA_TITLE, title.ifBlank { "টেস্ট অডিও" })
                putExtra(AudioPodcastService.EXTRA_SUBJECT, subject.ifBlank { "পডকাস্ট অডিও" })
                putExtra(AudioPodcastService.EXTRA_NARRATOR, "অ্যাডমিন টেস্ট")
                putExtra(AudioPodcastService.EXTRA_DURATION, "১০ মিনিট")
                putExtra(AudioPodcastService.EXTRA_CLASS, "All")
                putExtra(AudioPodcastService.EXTRA_URL, url)
                putExtra(AudioPodcastService.EXTRA_BOARD_ID, "ALL")
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    val filteredCustomPds = remember(customPds, pFilterBoard, pFilterClass, pSearchQuery) {
        customPds.filter { p ->
            val matchesBoard = pFilterBoard == "ALL" || p.boardId.equals(pFilterBoard, ignoreCase = true) || p.boardId.isBlank()
            val matchesClass = pFilterClass == "ALL" || p.className.equals(pFilterClass, ignoreCase = true)
            val query = pSearchQuery.trim().lowercase()
            val matchesSearch = query.isBlank() ||
                    p.title.lowercase().contains(query) ||
                    p.subject.lowercase().contains(query) ||
                    p.narrator.lowercase().contains(query) ||
                    p.boardId.lowercase().contains(query)
            matchesBoard && matchesClass && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "এখানে যুক্ত করা অডিও পডকাস্ট সরাসরি শিক্ষার্থীদের পডকাস্ট স্ক্রিনে প্রদর্শিত হবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 1. New Podcast Creation Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(6.dp).size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "নতুন অডিও পডকাস্ট তৈরি করুন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Firestore-এ সেভ ও লাইভ সিঙ্ক হবে",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CorrectGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CorrectGreen)
                                )
                                Text(
                                    text = "ক্লাউড সিঙ্ক",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CorrectGreen,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // ─── ধাপ ১: শিক্ষা বোর্ড নির্বাচন ───
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                            Text(
                                text = "১. শিক্ষা বোর্ড নির্বাচন *:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(podcastBoardList) { bId ->
                                val isSelected = pBoardId == bId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        pBoardId = bId
                                        if (bId == "WBCHSE" && (pClassName !in listOf("Class 11", "Class 12", "ALL"))) {
                                            pClassName = "Class 12"
                                        } else if (bId == "WBBSE" && (pClassName in listOf("Class 11", "Class 12"))) {
                                            pClassName = "Class 10"
                                        } else if (bId == "WBBPE" && (pClassName !in listOf("Class 5", "ALL"))) {
                                            pClassName = "Class 5"
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (bId == "ALL") "সকল বোর্ড" else bId,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryIndigo.copy(alpha = 0.2f),
                                        selectedLabelColor = PrimaryIndigo
                                    )
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = getBoardDisplayName(pBoardId),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("নির্বাচিত বোর্ড") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boardDropdownExp) },
                                modifier = Modifier.fillMaxWidth().clickable { boardDropdownExp = true },
                                shape = RoundedCornerShape(10.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { boardDropdownExp = true }
                            )
                            DropdownMenu(
                                expanded = boardDropdownExp,
                                onDismissRequest = { boardDropdownExp = false }
                            ) {
                                podcastBoardList.forEach { bId ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = getBoardDisplayName(bId),
                                                fontWeight = if (pBoardId == bId) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            pBoardId = bId
                                            if (bId == "WBCHSE" && (pClassName !in listOf("Class 11", "Class 12", "ALL"))) {
                                                pClassName = "Class 12"
                                            } else if (bId == "WBBSE" && (pClassName in listOf("Class 11", "Class 12"))) {
                                                pClassName = "Class 10"
                                            } else if (bId == "WBBPE" && (pClassName !in listOf("Class 5", "ALL"))) {
                                                pClassName = "Class 5"
                                            }
                                            boardDropdownExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ─── ধাপ ২: শ্রেণি নির্বাচন ───
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                            Text(
                                text = "২. লক্ষ্য শ্রেণি:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(podcastClassList) { cls ->
                                val isSelected = pClassName == cls
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { pClassName = cls },
                                    label = {
                                        Text(
                                            text = when (cls) {
                                                "Class 10" -> "দশম শ্রেণি"
                                                "Class 12" -> "দ্বাদশ শ্রেণি"
                                                "Class 11" -> "একাদশ শ্রেণি"
                                                "Class 9" -> "নবম শ্রেণি"
                                                "Class 8" -> "অষ্টম শ্রেণি"
                                                "Class 7" -> "সপ্তম শ্রেণি"
                                                "Class 6" -> "ষষ্ঠ শ্রেণি"
                                                "Class 5" -> "পঞ্চম শ্রেণি"
                                                "ALL" -> "সকল শ্রেণি"
                                                else -> cls
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryIndigo.copy(alpha = 0.2f),
                                        selectedLabelColor = PrimaryIndigo
                                    )
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = getClassDisplayName(pClassName),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("নির্বাচিত শ্রেণি") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExp) },
                                modifier = Modifier.fillMaxWidth().clickable { classDropdownExp = true },
                                shape = RoundedCornerShape(10.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { classDropdownExp = true }
                            )
                            DropdownMenu(
                                expanded = classDropdownExp,
                                onDismissRequest = { classDropdownExp = false }
                            ) {
                                podcastClassList.forEach { cls ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = getClassDisplayName(cls),
                                                fontWeight = if (pClassName == cls) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            pClassName = cls
                                            classDropdownExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ─── ধাপ ৩: বিষয় নির্বাচন ───
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                            Text(
                                text = "৩. বিষয় নির্বাচন:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = pSubject,
                                onValueChange = { pSubject = it },
                                label = { Text("বিষয় (যেমন: ইতিহাস, ভূগোল, পদার্থবিদ্যা)") },
                                placeholder = { Text("ক্লিক করে বা লিখে বিষয় নির্ধারণ করুন") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { subjectDropdownExp = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Subject")
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = subjectDropdownExp,
                                onDismissRequest = { subjectDropdownExp = false }
                            ) {
                                getClassSubjects(pClassName).forEach { sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub, fontWeight = if (pSubject == sub) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            pSubject = sub
                                            subjectDropdownExp = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = "দ্রুত পছন্দের বিষয় বেছে নিন:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(getClassSubjects(pClassName)) { qs ->
                                val isSubSelected = pSubject == qs
                                FilterChip(
                                    selected = isSubSelected,
                                    onClick = { pSubject = qs },
                                    label = { Text(qs, fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ExamGold.copy(alpha = 0.2f),
                                        selectedLabelColor = ExamGold
                                    )
                                )
                            }
                        }
                    }

                    // ─── ধাপ ৪: পডকাস্টের শিরোনাম ───
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Title, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                            Text(
                                text = "৪. পডকাস্টের শিরোনাম *:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedTextField(
                            value = pTitle,
                            onValueChange = { pTitle = it },
                            label = { Text("শিরোনাম লিখুন *") },
                            placeholder = { Text("যেমন: চলতড়িৎ ও ওহমের সূত্রের গাণিতিক শর্টকাট") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Text(
                            text = "💡 পডকাস্টে আলোচিত অধ্যায় ও মূল বিষয়ের সুস্পষ্ট নাম দিন।",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // ─── ধাপ ৫: সময়কাল ও বক্তা/শিক্ষকের নাম ───
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "৫. সময়কাল:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            OutlinedTextField(
                                value = pDuration,
                                onValueChange = { pDuration = it },
                                label = { Text("যেমন: ১০ মিনিট") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(listOf("৫ মিনিট", "১০ মিনিট", "১৫ মিনিট", "২০ মিনিট", "৩০ মিনিট")) { dur ->
                                    SuggestionChip(
                                        onClick = { pDuration = dur },
                                        label = { Text(dur, fontSize = 10.5.sp) }
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "বক্তা / শিক্ষক:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            OutlinedTextField(
                                value = pNarrator,
                                onValueChange = { pNarrator = it },
                                label = { Text("যেমন: সহায়ক শিক্ষক") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(listOf("সহায়ক শিক্ষক", "বিষয় শিক্ষক", "ড. সমীর রায়", "অধ্যাপক এস. বোস")) { nar ->
                                    SuggestionChip(
                                        onClick = { pNarrator = nar },
                                        label = { Text(nar, fontSize = 10.5.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // ─── ধাপ ৬: অডিও ফাইল লিংক ও ইনস্ট্যান্ট প্রিভিউ টেস্ট ───
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Audiotrack, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                            Text(
                                text = "৬. অডিও ফাইল লিংক *:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedTextField(
                            value = pAudioUrl,
                            onValueChange = { pAudioUrl = it },
                            label = { Text("সরাসরি প্লে-যোগ্য অডিও ফাইলের লিংক দিন *") },
                            placeholder = { Text("https://example.com/audio.mp3") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = PrimaryIndigo) },
                            trailingIcon = {
                                if (pAudioUrl.isNotBlank()) {
                                    IconButton(onClick = { pAudioUrl = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        )

                        Text(
                            text = "💡 MP3, M4A, Firebase Storage বা যেকোনো সরাসরি প্লে-যোগ্য অডিও লিংক দিন।",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        pAudioUrl = clip.trim()
                                        onAction(true, "✓ ক্লিপবোর্ড থেকে অডিও লিংক পেস্ট করা হয়েছে!")
                                    } else {
                                        onAction(false, "⚠️ ক্লিপবোর্ডে কোনো লিংক পাওয়া যায়নি!")
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পেস্ট", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    pAudioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নমুনা URL", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }

                            val isFormPlaying = AudioPodcastManager.isPlaying && AudioPodcastManager.currentlyPlaying?.audioUrl == pAudioUrl.trim()
                            Button(
                                onClick = {
                                    if (pAudioUrl.isBlank()) {
                                        onAction(false, "⚠️ টেস্ট শোনার জন্য প্রথমে একটি অডিও লিংক দিন বা 'নমুনা URL' এ ট্যাপ করুন।")
                                    } else {
                                        testPlayAudio(pAudioUrl.trim(), pTitle.trim(), pSubject.trim())
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFormPlaying) WrongRed else if (pAudioUrl.isNotBlank()) CorrectGreen else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Icon(
                                    if (isFormPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFormPlaying) "পজ" else "টেস্ট শুনুন", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Live Student View Preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "👁️ শিক্ষার্থীদের অ্যাপে লাইভ প্রিভিউ:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (pAudioUrl.isNotBlank()) CorrectGreen.copy(alpha = 0.15f) else ExamGold.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, if (pAudioUrl.isNotBlank()) CorrectGreen.copy(alpha = 0.4f) else ExamGold.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = if (pAudioUrl.isNotBlank()) "লিংক প্রস্তুত ✔️" else "লিংক ফাঁকা ⚠️",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pAudioUrl.isNotBlank()) CorrectGreen else ExamGold,
                                        softWrap = false,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Mock Preview Card
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryIndigo.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Headphones,
                                            contentDescription = null,
                                            tint = PrimaryIndigo,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = PrimaryIndigo.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = pBoardId,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = ExamGold.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = getClassDisplayName(pClassName),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ExamGold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = pSubject.ifBlank { "বিষয়" },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = pTitle.ifBlank { "পডকাস্টের শিরোনাম এখানে দেখাবে..." },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (pTitle.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            maxLines = 1
                                        )

                                        Text(
                                            text = "${pDuration.ifBlank { "১০ মিনিট" }} • ${pNarrator.ifBlank { "সহায়ক শিক্ষক" }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Save Podcast Button
                    Button(
                        onClick = {
                            val finalDuration = pDuration.trim().ifBlank { "১০ মিনিট" }
                            val finalNarrator = pNarrator.trim().ifBlank { "সহায়ক শিক্ষক" }
                            val finalSubject = pSubject.trim().ifBlank { "সাধারণ লেকচার" }
                            isSavingPodcast = true

                            viewModel.addCustomPodcast(
                                title = pTitle.trim(),
                                subject = finalSubject,
                                duration = finalDuration,
                                narrator = finalNarrator,
                                className = pClassName,
                                boardId = pBoardId,
                                audioUrl = pAudioUrl.trim()
                            ) { success, msg ->
                                isSavingPodcast = false
                                onAction(success, msg)
                                if (success) {
                                    pTitle = ""
                                    pSubject = ""
                                    pDuration = "১০ মিনিট"
                                    pNarrator = "সহায়ক শিক্ষক"
                                    pAudioUrl = ""
                                }
                            }
                        },
                        enabled = pTitle.isNotBlank() && pAudioUrl.isNotBlank() && !isSavingPodcast,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSavingPodcast) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ফায়ারবেসে সংরক্ষণ হচ্ছে...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🎙️ ফায়ারবেসে পডকাস্ট সংরক্ষণ করুন", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                        }
                    }

                    if (pTitle.isBlank() || pAudioUrl.isBlank()) {
                        Text(
                            text = "⚠️ সংরক্ষণ করতে শিরোনাম ও অডিও ফাইল লিংক অবশ্যই পূরণ করুন।",
                            style = MaterialTheme.typography.labelSmall,
                            color = ExamGold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // 2. Batch Upload Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(6.dp).size(20.dp)
                            )
                        }
                        Text(
                            text = "📤 ব্যাচ পডকাস্ট আপলোড (CSV / JSON)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Text(
                        text = "নিচে পাইপ (|) বা কমা (,) দিয়ে আলাদা করে একাধিক পডকাস্ট একসাথে আপলোড করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                pBulkInput = pBulkSample
                                bulkUploadStatusMessage = "✓ নমুনা CSV লোড হয়েছে!"
                                bulkUploadSuccess = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📋 নমুনা CSV লোড", fontSize = 11.5.sp, maxLines = 1)
                        }

                        if (pBulkInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    pBulkInput = ""
                                    bulkUploadStatusMessage = null
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear", tint = WrongRed)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = pBulkInput,
                        onValueChange = { pBulkInput = it },
                        placeholder = {
                            Text("শিরোনাম | বিষয় | সময়কাল | বক্তা | শ্রেণি | বোর্ড (যেমন: WBBSE / WBCHSE) | অডিও URL")
                        },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    bulkUploadStatusMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (bulkUploadSuccess) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (bulkUploadSuccess) CorrectGreen.copy(alpha = 0.4f) else WrongRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (bulkUploadSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (bulkUploadSuccess) CorrectGreen else WrongRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (bulkUploadSuccess) CorrectGreen else WrongRed
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.importBulkCustomPodcasts(pBulkInput) { success, msg, _ ->
                                bulkUploadSuccess = success
                                bulkUploadStatusMessage = msg
                                onAction(success, msg)
                                if (success) {
                                    pBulkInput = ""
                                }
                            }
                        },
                        enabled = pBulkInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📤 কাস্টম পডকাস্ট ব্যাচ আপলোড করুন", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }
        }

        // 3. Existing Custom Podcasts List with Search & Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 বর্তমান কাস্টম পডকাস্টসমূহ (${customPds.size} টি)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryIndigo.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "ফিল্টার্ড: ${filteredCustomPds.size} টি",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = pSearchQuery,
                    onValueChange = { pSearchQuery = it },
                    placeholder = { Text("🔎 পডকাস্ট খুঁজুন (শিরোনাম, বিষয় বা বক্তা)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (pSearchQuery.isNotBlank()) {
                            IconButton(onClick = { pSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Search", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "শিক্ষা বোর্ড ফিল্টার:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val boards = listOf("ALL", "WBBSE", "WBCHSE", "WBBPE", "CBSE")
                        items(boards) { b ->
                            val isSelected = pFilterBoard == b
                            FilterChip(
                                selected = isSelected,
                                onClick = { pFilterBoard = b },
                                label = { Text(if (b == "ALL") "সকল বোর্ড" else b, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryIndigo.copy(alpha = 0.2f),
                                    selectedLabelColor = PrimaryIndigo
                                )
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "শ্রেণি ফিল্টার:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val classes = listOf("ALL", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12")
                        items(classes) { cls ->
                            val isSelected = pFilterClass == cls
                            FilterChip(
                                selected = isSelected,
                                onClick = { pFilterClass = cls },
                                label = { Text(getClassDisplayName(cls), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryIndigo.copy(alpha = 0.2f),
                                    selectedLabelColor = PrimaryIndigo
                                )
                            )
                        }
                    }
                }
            }
        }

        if (filteredCustomPds.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = if (customPds.isEmpty()) "কোনো কাস্টম পডকাস্ট পাওয়া যায়নি।\nউপরের ফর্মটি পূরণ করে যুক্ত করুন।"
                                else "ফিল্টারের সাথে মেলে এমন কোনো পডকাস্ট নেই।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredCustomPds) { p ->
                val isThisPlaying = AudioPodcastManager.isPlaying && AudioPodcastManager.currentlyPlaying?.audioUrl == p.audioUrl
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isThisPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isThisPlaying) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    testPlayAudio(p.audioUrl, p.title, p.subject)
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isThisPlaying) WrongRed else PrimaryIndigo.copy(alpha = 0.15f)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isThisPlaying) "Pause" else "Play",
                                    tint = if (isThisPlaying) Color.White else PrimaryIndigo,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PrimaryIndigo.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = p.boardId.ifBlank { "WBBSE" },
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryIndigo,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ExamGold.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = getClassDisplayName(p.className),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExamGold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = p.subject.replace(Regex("\\s*\\([A-Za-z\\s&]+\\)\\s*"), "").trim(),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "• ${p.narrator}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = p.duration,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ExamGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = p.audioUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(p.audioUrl))
                                        onAction(true, "✓ অডিও লিংক কপি করা হয়েছে!")
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy Link",
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isThisPlaying) Icons.Default.VolumeUp else Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = if (isThisPlaying) CorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isThisPlaying) "🔊 প্লে হচ্ছে..." else "আইডি: ${p.id.takeLast(8)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isThisPlaying) CorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isThisPlaying) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { editingPodcast = p },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("সম্পাদনা", fontSize = 11.5.sp)
                                }

                                OutlinedButton(
                                    onClick = { deletingPodcast = p },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WrongRed),
                                    border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WrongRed, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("মুছুন", fontSize = 11.5.sp, color = WrongRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Podcast Dialog
    if (editingPodcast != null) {
        val ep = editingPodcast!!
        var editBoardId by remember(ep) { mutableStateOf(ep.boardId.ifBlank { "WBBSE" }) }
        var editTitle by remember(ep) { mutableStateOf(ep.title) }
        var editSubject by remember(ep) { mutableStateOf(ep.subject) }
        var editDuration by remember(ep) { mutableStateOf(ep.duration) }
        var editNarrator by remember(ep) { mutableStateOf(ep.narrator) }
        var editClassName by remember(ep) { mutableStateOf(ep.className.ifBlank { "Class 10" }) }
        var editAudioUrl by remember(ep) { mutableStateOf(ep.audioUrl) }

        var editBoardDropdownExp by remember { mutableStateOf(false) }
        var editClassDropdownExp by remember { mutableStateOf(false) }
        var editSubjectDropdownExp by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { editingPodcast = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryIndigo)
                            Column {
                                Text(
                                    text = "পডকাস্ট সম্পাদনা করুন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "আইডি: ${ep.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ExamGold
                                )
                            }
                        }

                        IconButton(onClick = { editingPodcast = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider()

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("শিরোনাম *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editSubject,
                        onValueChange = { editSubject = it },
                        label = { Text("বিষয়") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editDuration,
                            onValueChange = { editDuration = it },
                            label = { Text("সময়কাল") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = editNarrator,
                            onValueChange = { editNarrator = it },
                            label = { Text("বক্তা") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = editAudioUrl,
                        onValueChange = { editAudioUrl = it },
                        label = { Text("অডিও URL *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingPodcast = null },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("বাতিল")
                        }

                        Button(
                            onClick = {
                                val updated = ep.copy(
                                    title = editTitle.trim(),
                                    subject = editSubject.trim().ifBlank { "সাধারণ লেকচার" },
                                    duration = editDuration.trim().ifBlank { "১০ মিনিট" },
                                    narrator = editNarrator.trim().ifBlank { "সহায়ক শিক্ষক" },
                                    className = editClassName,
                                    boardId = editBoardId,
                                    audioUrl = editAudioUrl.trim()
                                )
                                viewModel.updateCustomPodcast(updated) { success, msg ->
                                    onAction(success, msg)
                                    if (success) {
                                        editingPodcast = null
                                    }
                                }
                            },
                            enabled = editTitle.isNotBlank() && editAudioUrl.isNotBlank(),
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("আপডেট সংরক্ষণ", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (deletingPodcast != null) {
        val dp = deletingPodcast!!
        Dialog(
            onDismissRequest = { deletingPodcast = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(WrongRed)
                                .border(2.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Warning",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Text(
                            text = "পডকাস্ট মুছে ফেলতে চান?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = dp.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "${dp.subject} • ${dp.className} • ${dp.narrator}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "⚠️ এটি ক্লাউড ফায়ারস্টোর থেকে স্থায়ীভাবে মুছে ফেলা হবে এবং শিক্ষার্থীরা আর এই পডকাস্টটি শুনতে পাবে না।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { deletingPodcast = null },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text("বাতিল")
                            }

                            Button(
                                onClick = {
                                    viewModel.deleteCustomPodcast(dp.id) { success, msg ->
                                        onAction(success, msg)
                                        if (success) {
                                            deletingPodcast = null
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("মুছে ফেলুন", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
