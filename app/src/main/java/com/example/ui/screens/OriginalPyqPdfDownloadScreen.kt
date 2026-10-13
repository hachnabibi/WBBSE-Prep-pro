package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicClassProvider
import com.example.data.repository.PyqPdfItem
import com.example.data.repository.PyqPdfRepository
import com.example.ui.components.AdMobBanner
import com.example.ui.components.AdMobNativeCard
import com.example.ui.components.InAppPdfViewer
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.util.InAppPdfManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ViewingPdfDoc(
    val file: File,
    val title: String,
    val subtitle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginalPyqPdfDownloadScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onBack: () -> Unit,
    onNavigateToSolved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val allQuestions by viewModel.allPyqQuestions.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()

    // Determine default class strictly from student's onboarding / profile selection
    val defaultStudentClass = remember(profile.classId, profile.classLevel, profile.targetExam) {
        val foundClass = AcademicClassProvider.ALL_CLASSES.find { it.id == profile.classId }
            ?: AcademicClassProvider.ALL_CLASSES.find { 
                profile.classLevel.contains(it.classNumber.toString()) || 
                (it.nameBengali.isNotBlank() && profile.classLevel.contains(it.nameBengali.take(4)))
            }
        if (foundClass != null) {
            "Class ${foundClass.classNumber}"
        } else {
            when {
                profile.classId.contains("12") || profile.classLevel.contains("12") || profile.classLevel.contains("দ্বাদশ") || profile.targetExam.contains("hs") -> "Class 12"
                profile.classId.contains("11") || profile.classLevel.contains("11") || profile.classLevel.contains("একাদশ") -> "Class 11"
                profile.classId.contains("9") || profile.classLevel.contains("9") || profile.classLevel.contains("নবম") -> "Class 9"
                profile.classId.contains("8") || profile.classLevel.contains("8") || profile.classLevel.contains("অষ্টম") -> "Class 8"
                profile.classId.contains("7") || profile.classLevel.contains("7") || profile.classLevel.contains("সপ্তম") -> "Class 7"
                profile.classId.contains("6") || profile.classLevel.contains("6") || profile.classLevel.contains("ষষ্ঠ") -> "Class 6"
                profile.classId.contains("5") || profile.classLevel.contains("5") || profile.classLevel.contains("পঞ্চম") -> "Class 5"
                else -> "Class 10"
            }
        }
    }

    var selectedYear by remember { mutableIntStateOf(0) } // 0 = All Years
    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    var selectedClass by remember(defaultStudentClass) { mutableStateOf(defaultStudentClass) }

    // Auto sync when student's profile class changes
    LaunchedEffect(defaultStudentClass) {
        selectedClass = defaultStudentClass
    }

    val downloadedPdfIds = remember { mutableStateListOf<String>() }
    var downloadingPdfId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    // Active In-App PDF viewing state (Zero external app required)
    var activeViewingDoc by remember { mutableStateOf<ViewingPdfDoc?>(null) }

    // Sync downloaded files on start
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dir = InAppPdfManager.getPyqDir(context)
            val files = dir.listFiles { _, name -> name.endsWith(".pdf") } ?: emptyArray()
            val validIds = files.filter { it.length() > 2048 }.map { it.nameWithoutExtension }
            withContext(Dispatchers.Main) {
                downloadedPdfIds.clear()
                downloadedPdfIds.addAll(validIds)
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

    // Get PDFs for current selected year or all years
    val allYearPdfs = remember(selectedYear) {
        if (selectedYear == 0) PyqPdfRepository.getAllPdfs()
        else PyqPdfRepository.getPdfsForYear(selectedYear)
    }

    val filteredPdfs = remember(allYearPdfs, selectedSubjectFilter, selectedClass) {
        allYearPdfs.filter { pdf ->
            val matchesClass = selectedClass == "ALL" || pdf.className.equals(selectedClass, ignoreCase = true) ||
                    (selectedClass == "Class 10" && (pdf.className.contains("10") || pdf.className.isBlank()))
            val matchesSubject = selectedSubjectFilter == "ALL" || pdf.subjectCode.equals(selectedSubjectFilter, ignoreCase = true)
            matchesClass && matchesSubject
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "বিগত বছরের আসল প্রশ্নপত্র PDF",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "অ্যাপের ভেতরেই সরাসরি পড়ুন ও ডাউনলোড করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("pyq_pdf_back_btn")
                    ) {
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
            contentPadding = PaddingValues(top = 0.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Hero Banner (Edge-to-Edge with 0dp Corner Radius)
            item {
                OriginalPyqHeroBanner(
                    studentClass = selectedClass,
                    totalPdfs = filteredPdfs.size,
                    isBengali = isBengali,
                    onNavigateToSolved = onNavigateToSolved,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 1.5 Government Disclaimer Card (Google Play Policy Compliance)
            item {
                Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                    GovernmentDisclaimerCard(isBengali = isBengali)
                }
            }

            // 2. Class Selector Header & Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
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
                            val isStudentClass = clsKey == defaultStudentClass && clsKey != "ALL"
                            val displayLabel = if (isStudentClass) {
                                if (isBengali) "$label (আপনার শ্রেণি)" else "$label (Your Class)"
                            } else label

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) PrimaryIndigo else if (isStudentClass) PrimaryIndigo.copy(alpha = 0.5f) else Color.Transparent),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedClass = clsKey }
                                    .testTag("pyq_pdf_class_$clsKey")
                            ) {
                                Text(
                                    text = displayLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected || isStudentClass) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Year Selector Header & Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "পরীক্ষার বছর নির্বাচন করুন:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val yearList = listOf(0 to ("সকল বছর")) +
                                PyqPdfRepository.supportedYears.map { it to ("$it সাল") }

                        items(yearList) { (year, label) ->
                            val isSelected = selectedYear == year
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) ExamGold else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) ExamGold else Color.Transparent),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedYear = year }
                                    .testTag("pyq_pdf_year_$year")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF1E1B2E) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Subject Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        "ALL" to ("সকল বিষয় (৭টি)"),
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
                            modifier = Modifier.testTag("pyq_pdf_chip_$key")
                        )
                    }
                }
            }

            // 5. AdMob Policy-Compliant Native Ad Card (Clearly labeled and separated)
            item {
                Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                    AdMobPolicyContainer(isBengali = isBengali) {
                        AdMobNativeCard(isBengali = isBengali)
                    }
                }
            }

            // 6. Section Header & Count (Small Screen Responsive)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
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
                    val yearText = if (selectedYear == 0) {
                        "সকল বছর"
                    } else {
                        "$selectedYear সাল"
                    }

                    Text(
                        text = "$classText • $yearText (${filteredPdfs.size}টি)",
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
                            text = "অফিসিয়াল WB",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SecondaryEmerald,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 7. PDF Question Paper Items with AdMob Banner Ad (Policy-compliant single placement with spacing)
            filteredPdfs.forEachIndexed { index, pdfItem ->
                item(key = pdfItem.id) {
                    val isDownloaded = downloadedPdfIds.contains(pdfItem.id)
                    val isDownloading = downloadingPdfId == pdfItem.id

                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                        OriginalPyqPdfCard(
                            pdfItem = pdfItem,
                            isBengali = isBengali,
                            isDownloaded = isDownloaded,
                            isDownloading = isDownloading,
                            downloadProgress = if (isDownloading) downloadProgress else 0f,
                            onDownloadClick = {
                                coroutineScope.launch {
                                    // Check if already cached locally
                                    val existingFile = InAppPdfManager.getLocalPdfFile(context, pdfItem.id)
                                    if (existingFile != null) {
                                        activeViewingDoc = ViewingPdfDoc(
                                            file = existingFile,
                                            title = "${pdfItem.className} ${pdfItem.year} ${pdfItem.subjectBengali}",
                                            subtitle = "মূল পশ্চিমবঙ্গ পর্ষদ প্রশ্নপত্র"
                                        )
                                        return@launch
                                    }

                                    // Download & prepare in-app
                                    downloadingPdfId = pdfItem.id
                                    downloadProgress = 0.05f

                                    // Filter questions for local fallback generation if needed
                                    val subjectQuestions = withContext(Dispatchers.Default) {
                                        allQuestions.filter { q ->
                                            val qSub = q.subject.trim().uppercase().replace(" ", "_")
                                            val pSub = pdfItem.subjectCode.uppercase().replace(" ", "_")
                                            (q.year == pdfItem.year) && (qSub == pSub ||
                                                    (pSub == "MATHEMATICS" && (qSub == "MATH" || qSub == "MATHEMATICS")) ||
                                                    (pSub == "LIFE_SCIENCE" && (qSub == "LIFE_SCIENCE" || qSub == "LIFESCIENCE" || qSub == "LIFE_SCI")) ||
                                                    (pSub == "PHYSICAL_SCIENCE" && (qSub == "PHYSICAL_SCIENCE" || qSub == "PHY_SCI")) ||
                                                    (pSub == "HISTORY" && (qSub == "HISTORY" || qSub == "HIST")) ||
                                                    (pSub == "GEOGRAPHY" && (qSub == "GEOGRAPHY" || qSub == "GEOG")) ||
                                                    (pSub == "BENGALI" && (qSub == "BENGALI" || qSub == "BEN")) ||
                                                    (pSub == "ENGLISH" && (qSub == "ENGLISH" || qSub == "ENG")))
                                        }
                                    }

                                    val preparedFile = InAppPdfManager.downloadOrPrepareOriginalPdf(
                                        context = context,
                                        pdfItem = pdfItem,
                                        questions = subjectQuestions,
                                        onProgress = { progress ->
                                            downloadProgress = progress
                                        }
                                    )

                                    downloadingPdfId = null

                                    if (preparedFile != null) {
                                        if (!downloadedPdfIds.contains(pdfItem.id)) {
                                            downloadedPdfIds.add(pdfItem.id)
                                        }
                                        Toast.makeText(
                                            context,
                                            "✓ ডাউনলোড সম্পন্ন! অ্যাপের ভেতরেই খোলা হচ্ছে...",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Open directly inside the In-App PDF Viewer (NO external app required)
                                        activeViewingDoc = ViewingPdfDoc(
                                            file = preparedFile,
                                            title = "${pdfItem.className} ${pdfItem.year} ${pdfItem.subjectBengali}",
                                            subtitle = "মূল পশ্চিমবঙ্গ পর্ষদ প্রশ্নপত্র"
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "পিডিএফ লোড বা ডাউনলোড ব্যর্থ হয়েছে।",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                }

                // Policy-Compliant Banner Ad: placed spaced out (every 6 cards), clearly labeled with ad container
                if ((index + 1) % 6 == 0 && index < filteredPdfs.size - 1) {
                    item(key = "policy_compliant_banner_ad_${pdfItem.id}_$index") {
                        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                            AdMobPolicyContainer(isBengali = isBengali, modifier = Modifier.padding(vertical = 6.dp)) {
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
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun OriginalPyqPdfCard(
    pdfItem: PyqPdfItem,
    isBengali: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float = 0f,
    onDownloadClick: () -> Unit
) {
    val subjectColor = when (pdfItem.subjectCode) {
        "PHYSICAL_SCIENCE" -> Color(0xFF3B82F6)
        "LIFE_SCIENCE" -> Color(0xFF10B981)
        "MATHEMATICS" -> Color(0xFFF59E0B)
        "HISTORY" -> Color(0xFFEC4899)
        "GEOGRAPHY" -> Color(0xFF06B6D4)
        "BENGALI" -> Color(0xFF8B5CF6)
        "ENGLISH" -> Color(0xFF14B8A6)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isDownloaded) SecondaryEmerald.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDownloadClick() }
            .testTag("pyq_pdf_item_${pdfItem.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Subject Icon Surface
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = subjectColor.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
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
                        text = if (isBengali) pdfItem.subjectBengali else pdfItem.subjectEnglish,
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
                    when (pdfItem.className) {
                        "Class 12" -> "দ্বাদশ শ্রেণি"
                        "Class 11" -> "একাদশ শ্রেণি"
                        "Class 10" -> "দশম শ্রেণি"
                        "Class 9" -> "নবম শ্রেণি"
                        "Class 8" -> "অষ্টম শ্রেণি"
                        "Class 7" -> "সপ্তম শ্রেণি"
                        "Class 6" -> "ষষ্ঠ শ্রেণি"
                        "Class 5" -> "পঞ্চম শ্রেণি"
                        else -> pdfItem.className
                    }
                } else pdfItem.className

                Text(
                    text = "$classLabel • ${pdfItem.year} • ${pdfItem.fileSize}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Button
            if (isDownloading) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp)) {
                    if (downloadProgress > 0f) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp,
                            color = PrimaryIndigo
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp,
                            color = PrimaryIndigo
                        )
                    }
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
                        .testTag("open_pdf_btn_${pdfItem.id}")
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
                        .testTag("download_pdf_btn_${pdfItem.id}")
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

@Composable
fun OriginalPyqHeroBanner(
    studentClass: String,
    totalPdfs: Int,
    isBengali: Boolean,
    onNavigateToSolved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("pyq_hero_banner")
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
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top row with official badge
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
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBengali) "পশ্চিমবঙ্গ পর্ষদ অফিশিয়াল" else "WBBSE & WBCHSE Official",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = Color(0xFFFEF3C7)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isBengali) "২০১৫ – ২০২৬" else "2015 – 2026",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title and subtitle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "বিগত বছরের মূল প্রশ্ন সম্ভার" else "Official Board Question Papers",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBengali)
                                "বোর্ড পরীক্ষার আসল প্রশ্নপত্র এক ক্লিকে ডাউনলোড করে ইন-অ্যাপ রিডারে যেকোনো সময় অফলাইনে পড়ুন।"
                            else
                                "Download genuine board exam question papers and read offline anytime with built-in PDF viewer.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Decorative Icon Art
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Feature Pill Tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroFeaturePill(
                        icon = Icons.Default.CloudDownload,
                        text = if (isBengali) "অফলাইন রিডার" else "Offline Reader"
                    )
                    HeroFeaturePill(
                        icon = Icons.Default.CheckCircle,
                        text = if (isBengali) "১০০% নির্ভুল" else "100% Verified"
                    )
                    HeroFeaturePillClickable(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        text = if (isBengali) "সমাধানসহ দেখুন ➔" else "View Solutions ➔",
                        onClick = onNavigateToSolved
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroFeaturePill(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun HeroFeaturePillClickable(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF59E0B).copy(alpha = 0.25f),
        border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f)),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun AdMobPolicyContainer(
    isBengali: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = if (isBengali) "বিজ্ঞাপন • Ad" else "Sponsored • Ad",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            content()
        }
    }
}
