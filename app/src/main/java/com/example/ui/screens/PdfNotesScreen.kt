package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdBannerCard
import com.example.ui.components.AdMobNativeCard
import com.example.ui.components.AdRewardType
import com.example.ui.components.RewardedAdDialog
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ExamGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryEmerald
import com.example.util.AdMobManager

data class PdfNote(
    val id: String,
    val title: String,
    val subject: String,
    val size: String,
    val date: String,
    val className: String = "Class 10"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfNotesScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    classId: String = "class_10_wbbse",
    unlockedNotes: Set<String> = setOf("1"),
    onUnlockNote: (noteId: String) -> Unit = {},
    onNavigateToPyqPdf: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
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
    val localUnlockedNotes = remember(unlockedNotes) { mutableStateListOf<String>().apply { addAll(unlockedNotes) } }
    var pendingDownloadNote by remember { mutableStateOf<PdfNote?>(null) }
    var showRewardedAdDialog by remember { mutableStateOf(false) }

    val notes = remember {
        listOf(
            // Class 10
            PdfNote("1", "Physical Science - Environment & Gases", "Phy Sci", "2.4 MB", "20 Sep 2026", "Class 10"),
            PdfNote("2", "Life Science - Genetics & Heredity", "Life Sci", "1.8 MB", "18 Sep 2026", "Class 10"),
            PdfNote("3", "History - Indian Independence Movement", "History", "3.2 MB", "15 Sep 2026", "Class 10"),
            PdfNote("4", "Geography - Map Pointing & Rivers", "Geography", "5.1 MB", "10 Sep 2026", "Class 10"),
            PdfNote("5", "Mathematics - Quadratic Equations & Trigonometry", "Math", "1.5 MB", "05 Sep 2026", "Class 10"),
            PdfNote("6", "Bengali - Koni & Sahitya Sanchayan", "Bengali", "2.1 MB", "01 Sep 2026", "Class 10"),
            PdfNote("7", "English - Grammar, Writing & Texts", "English", "2.0 MB", "28 Aug 2026", "Class 10"),
            // Class 12
            PdfNote("12_1", "Physics - Electrostatics & Optics (HS 2026)", "Physics", "3.4 MB", "22 Sep 2026", "Class 12"),
            PdfNote("12_2", "Chemistry - Organic Reactions & Solutions", "Chemistry", "2.9 MB", "21 Sep 2026", "Class 12"),
            PdfNote("12_3", "Biological Sciences - Genetics & Biotech", "Biology", "2.5 MB", "19 Sep 2026", "Class 12"),
            PdfNote("12_4", "Mathematics - Calculus (Limit, Continuity & Integration)", "Math", "3.1 MB", "18 Sep 2026", "Class 12"),
            PdfNote("12_5", "Accountancy - Partnership & Company Accounts", "Accountancy", "2.7 MB", "16 Sep 2026", "Class 12"),
            PdfNote("12_6", "Political Science - International Relations", "Pol Sci", "2.2 MB", "14 Sep 2026", "Class 12"),
            // Class 11
            PdfNote("11_1", "Physics - Kinematics & Laws of Motion", "Physics", "3.0 MB", "20 Sep 2026", "Class 11"),
            PdfNote("11_2", "Chemistry - Atomic Structure & Periodic Table", "Chemistry", "2.6 MB", "19 Sep 2026", "Class 11"),
            PdfNote("11_3", "Mathematics - Sets, Relations & Trigonometry", "Math", "2.8 MB", "17 Sep 2026", "Class 11"),
            // Class 9
            PdfNote("9_1", "Physical Science - Force, Motion & Energy", "Phy Sci", "2.2 MB", "15 Sep 2026", "Class 9"),
            PdfNote("9_2", "Life Science - Cells & Tissues", "Life Sci", "1.9 MB", "14 Sep 2026", "Class 9"),
            PdfNote("9_3", "Mathematics - Polynomials & Coordinate Geometry", "Math", "2.0 MB", "12 Sep 2026", "Class 9"),
            // Class 8
            PdfNote("8_1", "Science - Force & Pressure, Microorganisms", "Science", "1.8 MB", "10 Sep 2026", "Class 8"),
            PdfNote("8_2", "Mathematics - Rational Numbers & Algebraic Equations", "Math", "1.7 MB", "08 Sep 2026", "Class 8"),
            // Class 7
            PdfNote("7_1", "Science - Heat, Light & Chemical Reactions", "Science", "1.6 MB", "05 Sep 2026", "Class 7"),
            PdfNote("7_2", "Mathematics - Ratio & Fraction Operations", "Math", "1.5 MB", "04 Sep 2026", "Class 7"),
            // Class 6
            PdfNote("6_1", "Science - Living World & Materials", "Science", "1.4 MB", "02 Sep 2026", "Class 6"),
            PdfNote("6_2", "Mathematics - Number System & Geometry", "Math", "1.4 MB", "01 Sep 2026", "Class 6"),
            // Class 5
            PdfNote("5_1", "Our Environment - Nature, Water & Soil", "Environment", "1.2 MB", "30 Aug 2026", "Class 5"),
            PdfNote("5_2", "Amar Gonit - Unitary Method & Basic Arithmetic", "Math", "1.3 MB", "28 Aug 2026", "Class 5")
        )
    }

    val classOptions = listOf("ALL", "Class 12", "Class 11", "Class 10", "Class 9", "Class 8", "Class 7", "Class 6", "Class 5")

    val filteredNotes = notes.filter { note ->
        val matchesClass = selectedClassFilter == "ALL" || note.className == selectedClassFilter
        val matchesSearch = note.title.contains(searchQuery, ignoreCase = true) || 
                            note.subject.contains(searchQuery, ignoreCase = true) || 
                            note.className.contains(searchQuery, ignoreCase = true)
        matchesClass && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "পিডিএফ স্টাডি ও সাজেশন নোটস",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // AdMob Native Ad Card
            item {
                AdMobNativeCard(isBengali = isBengali)
            }

            // Featured PYQ PDF Download Entry Card
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryIndigo.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(1.2.dp, PrimaryIndigo.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigateToPyqPdf() }
                        .testTag("pyq_pdf_download_banner_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryIndigo,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "NEW",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                                    )
                                }
                                Text(
                                    text = "বিগত বছরের প্রশ্নপত্র PDF",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "২০১৫ – ২০২৬ সমস্ত বিষয়ের আসল প্রশ্নপত্র ও উত্তর ডাউনলোড করুন",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("নোটস বা বিষয় খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Class Filter Chips
            item {
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
            }

            // Section Header
            item {
                Text(
                    text = "টপার্স হ্যান্ডরিটেন নোটস (${filteredNotes.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // PDF Notes List
            items(filteredNotes, key = { it.id }) { note ->
                val isUnlocked = localUnlockedNotes.contains(note.id)
                PdfNoteItem(
                    note = note,
                    isUnlocked = isUnlocked,
                    isBengali = isBengali,
                    onDownloadClick = {
                        if (isUnlocked) {
                            Toast.makeText(
                                context,
                                "ডাউনলোড শুরু হয়েছে: ${note.title}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            pendingDownloadNote = note
                            val activity = context as? Activity
                            if (activity != null && AdMobManager.hasRewardedInterstitialAd()) {
                                val shown = AdMobManager.showRewardedInterstitialAd(
                                    activity = activity,
                                    onRewardEarned = { _, _ ->
                                        localUnlockedNotes.add(note.id)
                                        onUnlockNote(note.id)
                                        Toast.makeText(
                                            context,
                                            "পিডিএফ আনলক হয়েছে এবং ডাউনলোড সম্পন্ন! 🎉 (+৫০ XP, +২০ কয়েন)",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        pendingDownloadNote = null
                                    },
                                    onAdDismissed = {
                                        pendingDownloadNote = null
                                    }
                                )
                                if (!shown) {
                                    showRewardedAdDialog = true
                                }
                            } else {
                                showRewardedAdDialog = true
                            }
                        }
                    }
                )
            }
        }
    }

    if (showRewardedAdDialog && pendingDownloadNote != null) {
        RewardedAdDialog(
            rewardType = AdRewardType.PDF_UNLOCK,
            isBengali = isBengali,
            onRewardEarned = {
                pendingDownloadNote?.let { note ->
                    localUnlockedNotes.add(note.id)
                    onUnlockNote(note.id)
                    Toast.makeText(
                        context,
                        "পিডিএফ আনলক হয়েছে এবং ডাউনলোড সম্পন্ন! 🎉 (+50 XP, +20 কয়েন)",
                        Toast.LENGTH_LONG
                    ).show()
                }
                showRewardedAdDialog = false
                pendingDownloadNote = null
            },
            onDismiss = {
                showRewardedAdDialog = false
                pendingDownloadNote = null
            }
        )
    }
}

@Composable
fun PdfNoteItem(
    note: PdfNote,
    isUnlocked: Boolean,
    isBengali: Boolean,
    onDownloadClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isUnlocked) BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.4f)) else BorderStroke(1.dp, ExamGold.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onDownloadClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) CorrectGreen.copy(alpha = 0.12f) else ExamGold.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = if (isUnlocked) CorrectGreen else ExamGold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${note.subject} • ${note.size} • ${note.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isUnlocked) {
                    Text(
                        text = "🔒 আনলক করতে ভিডিও স্পনসর দেখুন",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ExamGold
                    )
                }
            }

            IconButton(
                onClick = onDownloadClick,
                modifier = Modifier.background(
                    if (isUnlocked) CorrectGreen.copy(alpha = 0.15f) else ExamGold.copy(alpha = 0.15f),
                    CircleShape
                )
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.Download else Icons.Default.Lock,
                    contentDescription = "Download",
                    tint = if (isUnlocked) CorrectGreen else ExamGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
