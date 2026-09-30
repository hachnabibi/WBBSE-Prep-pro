package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TextbookItem
import com.example.data.repository.TextbookRepository
import com.example.ui.components.AdMobBanner
import com.example.ui.components.AdMobNativeCard
import com.example.ui.components.InAppPdfViewer
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextbooksScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onNavigateToInternetArchiveSearch: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profile by viewModel.studentProfile.collectAsState()

    // Determine default class from student's onboarding / profile selection
    val defaultStudentClass = remember(profile.classId, profile.classLevel, profile.targetExam) {
        when {
            profile.classId == "class_12_wbchse" || profile.classLevel.contains("12") || profile.classLevel.contains("দ্বাদশ") || profile.targetExam.contains("hs") -> "Class 12"
            profile.classId == "class_11_wbchse" || profile.classLevel.contains("11") || profile.classLevel.contains("একাদশ") -> "Class 11"
            profile.classId == "class_9_wbbse" || profile.classLevel.contains("9") || profile.classLevel.contains("নবম") -> "Class 9"
            profile.classId == "class_8_wbbse" || profile.classLevel.contains("8") || profile.classLevel.contains("অষ্টম") -> "Class 8"
            profile.classId == "class_7_wbbse" || profile.classLevel.contains("7") || profile.classLevel.contains("সপ্তম") -> "Class 7"
            profile.classId == "class_6_wbbse" || profile.classLevel.contains("6") || profile.classLevel.contains("ষষ্ঠ") -> "Class 6"
            profile.classId == "class_5_wbbse" || profile.classLevel.contains("5") || profile.classLevel.contains("পঞ্চম") -> "Class 5"
            else -> "Class 10"
        }
    }

    var selectedClass by remember(defaultStudentClass) { mutableStateOf(defaultStudentClass) }
    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    val downloadedBookIds = remember { mutableStateListOf<String>() }
    var downloadingBookId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var activeViewingDoc by remember { mutableStateOf<ViewingPdfDoc?>(null) }

    // Sync downloaded books on start
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dir = TextbookRepository.getTextbookDir(context)
            val files = dir.listFiles() ?: emptyArray()
            val existingIds = files
                .filter { it.extension.equals("pdf", ignoreCase = true) && it.length() > 1024 }
                .map { it.nameWithoutExtension }
            withContext(Dispatchers.Main) {
                downloadedBookIds.clear()
                downloadedBookIds.addAll(existingIds)
            }
        }
    }

    // Intercept back button if currently viewing a PDF inside the app
    if (activeViewingDoc != null) {
        InAppPdfViewer(
            file = activeViewingDoc!!.file,
            title = activeViewingDoc!!.title,
            subtitle = activeViewingDoc!!.subtitle,
            isBengali = isBengali,
            onBack = { activeViewingDoc = null },
            modifier = modifier
        )
        return
    }

    val filteredBooks = remember(selectedSubjectFilter, selectedClass) {
        TextbookRepository.allTextbooks.filter { book ->
            val matchesClass = selectedClass == "ALL" || book.className.equals(selectedClass, ignoreCase = true) ||
                    (selectedClass == "Class 10" && (book.className.contains("10") || book.className.isBlank()))
            val matchesSubject = selectedSubjectFilter == "ALL" || book.subjectCode.equals(selectedSubjectFilter, ignoreCase = true)
            matchesClass && matchesSubject
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "পাঠ্যপুস্তক PDF",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "WBBSE ও WBCHSE বোর্ড অনুমোদিত ই-বুক",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("textbooks_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        BackHandler {
            onBack()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 0. Search Box for Internet Archive
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { 
                        Text(
                            text = "ইন্টারনেট আর্কাইভে বই খুঁজুন...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                onNavigateToInternetArchiveSearch(searchQuery)
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                )
            }

            // 1. Top AdMob Card
            item {
                AdMobNativeCard(isBengali = isBengali)
            }

            // 2. Class Selector Header & Chips (Pure language per version)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "শ্রেণি নির্বাচন করুন:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val classList = if (isBengali) listOf(
                            "ALL" to "সকল শ্রেণি",
                            "Class 12" to "দ্বাদশ শ্রেণি",
                            "Class 11" to "একাদশ শ্রেণি",
                            "Class 10" to "দশম শ্রেণি",
                            "Class 9" to "নবম শ্রেণি",
                            "Class 8" to "অষ্টম শ্রেণি",
                            "Class 7" to "সপ্তম শ্রেণি",
                            "Class 6" to "ষষ্ঠ শ্রেণি",
                            "Class 5" to "পঞ্চম শ্রেণি"
                        ) else listOf(
                            "ALL" to "All Classes",
                            "Class 12" to "Class 12",
                            "Class 11" to "Class 11",
                            "Class 10" to "Class 10",
                            "Class 9" to "Class 9",
                            "Class 8" to "Class 8",
                            "Class 7" to "Class 7",
                            "Class 6" to "Class 6",
                            "Class 5" to "Class 5"
                        )
                        items(classList) { (clsKey, label) ->
                            val isSelected = selectedClass == clsKey
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) PrimaryIndigo else Color.Transparent),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedClass = clsKey }
                                    .testTag("textbook_class_$clsKey")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Subject Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        "ALL" to ("সকল বিষয়"),
                        "BENGALI" to ("বাংলা"),
                        "ENGLISH" to ("ইংরেজি"),
                        "MATHEMATICS" to ("গণিত"),
                        "PHYSICAL_SCIENCE" to ("ভৌতবিজ্ঞান"),
                        "LIFE_SCIENCE" to ("জীবনবিজ্ঞান"),
                        "HISTORY" to ("ইতিহাস"),
                        "GEOGRAPHY" to ("ভূগোল")
                    )
                    items(filters) { (key, label) ->
                        val isSelected = selectedSubjectFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubjectFilter = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryIndigo,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("textbook_chip_$key")
                        )
                    }
                }
            }

            // 4. Section Header & Notice (Small Screen Responsive)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val classText = if (isBengali) {
                        when (selectedClass) {
                            "ALL" -> "সকল শ্রেণি"
                            "Class 12" -> "দ্বাদশ শ্রেণি"
                            "Class 11" -> "একাদশ শ্রেণি"
                            "Class 10" -> "দশম শ্রেণি"
                            "Class 9" -> "নবম শ্রেণি"
                            "Class 8" -> "অষ্টম শ্রেণি"
                            "Class 7" -> "সপ্তম শ্রেণি"
                            "Class 6" -> "ষষ্ঠ শ্রেণি"
                            "Class 5" -> "পঞ্চম শ্রেণি"
                            else -> selectedClass
                        }
                    } else {
                        when (selectedClass) {
                            "ALL" -> "All Classes"
                            else -> selectedClass
                        }
                    }

                    Text(
                        text = "$classText • পাঠ্যবই তালিকা (${filteredBooks.size}টি)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SecondaryEmerald.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SecondaryEmerald.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "সরকারি WB বোর্ড",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SecondaryEmerald,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 5. Textbook Items with Banner Ad after every 2 cards
            filteredBooks.forEachIndexed { index, book ->
                item(key = book.id) {
                    val isDownloaded = downloadedBookIds.contains(book.id)
                    val isDownloading = downloadingBookId == book.id

                    TextbookCard(
                        book = book,
                        isBengali = isBengali,
                        isDownloaded = isDownloaded,
                        isDownloading = isDownloading,
                        downloadProgress = if (isDownloading) downloadProgress else 0f,
                        onDownloadClick = {
                            coroutineScope.launch {
                                val existingFile = TextbookRepository.getLocalBookFile(context, book.id)
                                if (existingFile != null) {
                                    activeViewingDoc = ViewingPdfDoc(
                                        file = existingFile,
                                        title = if (isBengali) book.titleBengali else book.titleEnglish,
                                        subtitle = "${book.board} সরকারি পাঠ্যপুস্তক ডিজিটাল সংস্করণ"
                                    )
                                    return@launch
                                }

                                downloadingBookId = book.id
                                downloadProgress = 0f
                                val prepared = TextbookRepository.prepareTextbookPdf(context, book) { progress ->
                                    downloadProgress = progress
                                }
                                downloadingBookId = null

                                if (prepared != null) {
                                    if (!downloadedBookIds.contains(book.id)) {
                                        downloadedBookIds.add(book.id)
                                    }
                                    Toast.makeText(
                                        context,
                                        "✓ ${book.titleBengali} ডাউনলোড সম্পন্ন! খোলা হচ্ছে...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    activeViewingDoc = ViewingPdfDoc(
                                        file = prepared,
                                        title = if (isBengali) book.titleBengali else book.titleEnglish,
                                        subtitle = "${book.board} সরকারি পাঠ্যপুস্তক ডিজিটাল সংস্করণ"
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        "পাঠ্যবই লোড করা যায়নি।",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                }

                // Add Banner Ad every 2 cards
                if ((index + 1) % 2 == 0 && index < filteredBooks.size - 1) {
                    item(key = "tb_banner_slot_${book.id}_$index") {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            AdMobBanner(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                isBengali = isBengali
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TextbookCard(
    book: TextbookItem,
    isBengali: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float = 0f,
    onDownloadClick: () -> Unit
) {
    val subjectColor = when (book.subjectCode.uppercase()) {
        "BENGALI" -> Color(0xFF0D9488) // Teal
        "ENGLISH" -> Color(0xFF0284C7) // Sky
        "MATHEMATICS" -> Color(0xFFE11D48) // Rose
        "PHYSICAL_SCIENCE" -> Color(0xFF7C3AED) // Violet
        "LIFE_SCIENCE" -> Color(0xFF16A34A) // Green
        "HISTORY" -> Color(0xFFD97706) // Amber
        "GEOGRAPHY" -> Color(0xFF2563EB) // Blue
        else -> PrimaryIndigo
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isDownloaded) SecondaryEmerald.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDownloadClick() }
            .testTag("textbook_item_${book.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Book Icon Surface
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = subjectColor.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = subjectColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Info Details (Title & Subtitle fit cleanly without line breaks)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isBengali) book.titleBengali else book.titleEnglish,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isDownloaded) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "সেভ করা আছে",
                            tint = SecondaryEmerald,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                val classLabel = if (isBengali) {
                    when (book.className) {
                        "Class 12" -> "দ্বাদশ শ্রেণি"
                        "Class 11" -> "একাদশ শ্রেণি"
                        "Class 10" -> "দশম শ্রেণি"
                        "Class 9" -> "নবম শ্রেণি"
                        "Class 8" -> "অষ্টম শ্রেণি"
                        "Class 7" -> "সপ্তম শ্রেণি"
                        "Class 6" -> "ষষ্ঠ শ্রেণি"
                        "Class 5" -> "পঞ্চম শ্রেণি"
                        else -> book.className
                    }
                } else book.className

                Text(
                    text = "$classLabel • ${book.edition} • ${book.fileSize}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Download/Read Action Button (Small, Slim & Compact)
            if (isDownloading) {
                if (downloadProgress > 0f) {
                    CircularProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 2.5.dp,
                        color = PrimaryIndigo
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 2.5.dp,
                        color = PrimaryIndigo
                    )
                }
            } else if (isDownloaded) {
                FilledTonalButton(
                    onClick = onDownloadClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SecondaryEmerald.copy(alpha = 0.18f),
                        contentColor = SecondaryEmerald
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .heightIn(min = 32.dp)
                        .testTag("open_book_btn_${book.id}")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "পড়ুন",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            } else {
                Button(
                    onClick = onDownloadClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .heightIn(min = 32.dp)
                        .testTag("download_book_btn_${book.id}")
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "ডাউনলোড",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
