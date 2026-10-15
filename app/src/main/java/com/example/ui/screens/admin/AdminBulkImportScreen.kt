package com.example.ui.screens.admin

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ChapterEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TopicEntity
import com.example.data.model.AcademicClassProvider
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel

/**
 * ট্যাব ৬: বাল্ক ইমপোর্ট (প্রশ্ন ও সিলেবাস)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBulkImportScreen(
    viewModel: DeshExamViewModel,
    isLoading: Boolean,
    isBengali: Boolean = true,
    onAction: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var bulkImportSubTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: Curriculum
    var bulkImportText by remember { mutableStateOf("") }
    var bulkCustomTestTitle by remember { mutableStateOf("") }
    var bulkPyqYearInput by remember { mutableStateOf("") }
    var bulkFileStatusMessage by remember { mutableStateOf<String?>(null) }
    var bulkFileStatusSuccess by remember { mutableStateOf(false) }

    var showJsonGuideDialog by remember { mutableStateOf(false) }

    val allBoards = remember { AcademicClassProvider.ALL_BOARDS }
    var selectedBoard by remember { mutableStateOf(allBoards.find { it.id == "WBBSE" } ?: allBoards.first()) }

    val allClasses = remember { AcademicClassProvider.ALL_CLASSES }
    val currentBoardClasses = remember(selectedBoard, allClasses) {
        allClasses.filter { it.board.equals(selectedBoard.id, ignoreCase = true) || it.board.equals(selectedBoard.shortName, ignoreCase = true) }
            .ifEmpty { allClasses }
    }
    var selectedClass by remember(selectedBoard) {
        mutableStateOf(currentBoardClasses.firstOrNull { it.id == "class_10_wbbse" } ?: currentBoardClasses.first())
    }

    var dbSubjects by remember { mutableStateOf<List<SubjectEntity>>(emptyList()) }
    var dbChapters by remember { mutableStateOf<List<ChapterEntity>>(emptyList()) }
    var dbTopics by remember { mutableStateOf<List<TopicEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            dbSubjects = viewModel.repository.dao.getAllSubjectsOnce()
            dbChapters = viewModel.repository.dao.getAllChaptersOnce()
            dbTopics = viewModel.repository.dao.getAllTopicsOnce()
        } catch (e: Exception) {
            android.util.Log.e("AdminBulkImportScreen", "Error loading entities: ${e.message}", e)
        }
    }

    val classSubjects = remember(selectedClass, dbSubjects) {
        val filtered = dbSubjects.filter { it.classId == selectedClass.id }
        if (filtered.isNotEmpty()) filtered else getAdminClassSubjects(selectedClass.id)
    }

    var selectedSubject by remember(selectedClass) {
        mutableStateOf(classSubjects.first())
    }

    val allChapters by viewModel.chapters.collectAsState()
    val allTopics by viewModel.topics.collectAsState()
    val effectiveChapters = if (dbChapters.isNotEmpty()) dbChapters else allChapters
    val effectiveTopics = if (dbTopics.isNotEmpty()) dbTopics else allTopics

    val filteredChapters = remember(selectedSubject, effectiveChapters) {
        effectiveChapters.filter { it.subjectId == selectedSubject.id || it.subjectId == selectedSubject.code }
    }

    var selectedChapter by remember(selectedSubject, filteredChapters) {
        mutableStateOf(filteredChapters.firstOrNull())
    }

    val filteredTopics = remember(selectedChapter, effectiveTopics) {
        val chId = selectedChapter?.id ?: ""
        if (chId.isNotBlank()) effectiveTopics.filter { it.chapterId == chId } else emptyList()
    }

    var selectedTopic by remember(selectedChapter, filteredTopics) {
        mutableStateOf(filteredTopics.firstOrNull())
    }

    var bulkBoardDropdownExpanded by remember { mutableStateOf(false) }
    var bulkClassDropdownExpanded by remember { mutableStateOf(false) }
    var bulkSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var bulkChapterDropdownExpanded by remember { mutableStateOf(false) }
    var bulkTopicDropdownExpanded by remember { mutableStateOf(false) }
    var bulkPyqDropdownExpanded by remember { mutableStateOf(false) }

    val sampleJsonTemplate = """[
  {
    "question": "সালোকসংশ্লেষ প্রক্রিয়ায় নির্গত উপজাত গ্যাসের নাম কী?",
    "options": [
      "অক্সিজেন (O2)",
      "কার্বন ডাই অক্সাইড (CO2)",
      "নাইট্রোজেন (N2)",
      "হাইড্রোজেন (H2)"
    ],
    "correctOptionIndex": 0,
    "explanation": "সালোকসংশ্লেষে জল বিশ্লিষ্ট হয়ে উপজাত পদার্থ হিসেবে অক্সিজেন গ্যাস নির্গত হয়।",
    "difficulty": "EASY",
    "marks": 1,
    "pyqYear": "2024"
  },
  {
    "question": "মানবদেহের দীর্ঘতম স্নায়ু (Nerve) কোনটি?",
    "options": [
      "ভেগাস স্নায়ু",
      "অপটিক স্নায়ু",
      "সায়াটিক স্নায়ু",
      "অডিটরি স্নায়ু"
    ],
    "correctOptionIndex": 2,
    "explanation": "সায়াটিক নার্ভ (Sciatic Nerve) হলো মানবদেহের সবচেয়ে দীর্ঘতম ও মোটা স্নায়ু।",
    "difficulty": "MEDIUM",
    "marks": 1,
    "pyqYear": "2023"
  }
]"""

    val sampleCsvTemplate = """উদ্ভিদের জল সংবহনকারী কলা কোনটি? | জাইলেম | ফ্লোয়েম | ক্যামবিয়াম | কর্টেক্স | 0 | জাইলেম কলার মাধ্যমে জল সংবহন হয়।
কোষের শক্তিঘর বলা হয় কাকে? | গলগি বডি | মাইটোকন্ড্রিয়া | নিউক্লিয়াস | রাইবোজোম | 1 | মাইটোকন্ড্রিয়ায় ATP উৎপন্ন হয়।"""

    val sampleCurriculumJsonTemplate = """[
  {
    "chapterNumber": 1,
    "title": "বল ও গতি",
    "description": "গতির সমীকরণ ও বলের ধারণা",
    "topics": [
      {
        "orderIndex": 1,
        "title": "গতির প্রথম সমীকরণ",
        "summary": "গতির প্রথম সমীকরণটি হলো v = u + at",
        "keyPoints": [
          "v হলো অন্তিম বেগ",
          "u হলো প্রাথমিক বেগ",
          "a হলো ত্বরণ ও t হলো সময়"
        ],
        "formulae": [
          "v = u + at"
        ],
        "contentData": "গতির প্রথম সমীকরণ প্রতিষ্ঠা করার জন্য ধরি একটি কণা u বেগে যাত্রা শুরু করল...",
        "videoUrl": "https://youtu.be/example1"
      }
    ]
  }
]"""

    val sampleCurriculumCsvTemplate = """chapterNumber | title | description | orderIndex | topicTitle | summary | keyPoints | formulae | contentData | videoUrl
1 | বল ও গতি | গতির সমীকরণ ও বলের ধারণা | 1 | গতির প্রথম সমীকরণ | গতির প্রথম সমীকরণটি হলো v = u + at | v হলো অন্তিম বেগ # u হলো প্রাথমিক বেগ # a হলো ত্বরণ ও t হলো সময় | v = u + at | গতির প্রথম সমীকরণ প্রতিষ্ঠা করার জন্য ধরি একটি কণা u বেগে যাত্রা শুরু করল... | https://youtu.be/example1"""

    val jsonFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (content.isNotBlank()) {
                    bulkImportText = content
                    bulkFileStatusMessage = "ফাইল সফলভাবে পড়া হয়েছে! (${(content.length / 1024.0).let { "%.1f KB".format(it) }})"
                    bulkFileStatusSuccess = true
                }
            } catch (e: Exception) {
                bulkFileStatusMessage = "ফাইল লোড করতে সমস্যা: ${e.message}"
                bulkFileStatusSuccess = false
            }
        }
    }

    val downloadQuestionJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(sampleJsonTemplate.toByteArray(Charsets.UTF_8))
                }
                bulkFileStatusMessage = "নমুনা প্রশ্ন JSON সফলভাবে সংরক্ষণ করা হয়েছে!"
                bulkFileStatusSuccess = true
            } catch (e: Exception) {
                bulkFileStatusMessage = "সেভ করতে ত্রুটি: ${e.message}"
                bulkFileStatusSuccess = false
            }
        }
    }

    val downloadSyllabusJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(sampleCurriculumJsonTemplate.toByteArray(Charsets.UTF_8))
                }
                bulkFileStatusMessage = "নমুনা সিলেবাস JSON সফলভাবে সংরক্ষণ করা হয়েছে!"
                bulkFileStatusSuccess = true
            } catch (e: Exception) {
                bulkFileStatusMessage = "সেভ করতে ত্রুটি: ${e.message}"
                bulkFileStatusSuccess = false
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Tab Switcher (0: Questions, 1: Curriculum)
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (bulkImportSubTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                bulkImportSubTab = 0
                                bulkImportText = ""
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (bulkImportSubTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "প্রশ্ন ব্যাংক",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (bulkImportSubTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (bulkImportSubTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                bulkImportSubTab = 1
                                bulkImportText = ""
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (bulkImportSubTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "সিলেবাস",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (bulkImportSubTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (bulkImportSubTab == 0) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "একসাথে একাধিক প্রশ্ন / টেস্ট আপলোড",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "জেসন (JSON) ফাইল সরাসরি আপলোড করুন অথবা JSON / পাইপ লাইন ফরম্যাটে প্রশ্ন পেস্ট করে ডেটাবেজে সংরক্ষণ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // JSON File Upload Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.FileUpload,
                                        contentDescription = "Upload JSON",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "JSON ফাইল সরাসরি আপলোড",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "ডিভাইস থেকে .json বা .txt ফাইল সিলেক্ট করুন",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { jsonFilePickerLauncher.launch("*/*") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ফাইল বাছুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        if (bulkFileStatusMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (bulkFileStatusSuccess) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (bulkFileStatusSuccess) CorrectGreen.copy(alpha = 0.4f) else WrongRed.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = bulkFileStatusMessage ?: "",
                                    color = if (bulkFileStatusSuccess) CorrectGreen else WrongRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Target Dropdowns
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "পর্ষদ, শ্রেণি, বিষয়, অধ্যায় ও টপিক নির্বাচন:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Board
                        ExposedDropdownMenuBox(
                            expanded = bulkBoardDropdownExpanded,
                            onExpandedChange = { bulkBoardDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedBoard.shortName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("১. বোর্ড", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkBoardDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = bulkBoardDropdownExpanded,
                                onDismissRequest = { bulkBoardDropdownExpanded = false }
                            ) {
                                allBoards.forEach { board ->
                                    DropdownMenuItem(
                                        text = { Text(board.shortName, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedBoard = board
                                            val filteredCls = allClasses.filter { it.board.equals(board.id, ignoreCase = true) || it.board.equals(board.shortName, ignoreCase = true) }
                                            if (filteredCls.isNotEmpty()) {
                                                selectedClass = filteredCls.first()
                                                val subs = getAdminClassSubjects(selectedClass.id)
                                                selectedSubject = subs.first()
                                                val chs = allChapters.filter { it.subjectId == selectedSubject.id }
                                                selectedChapter = chs.firstOrNull()
                                                val tps = if (selectedChapter != null) allTopics.filter { it.chapterId == selectedChapter!!.id } else emptyList()
                                                selectedTopic = tps.firstOrNull()
                                            }
                                            bulkBoardDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Class
                        ExposedDropdownMenuBox(
                            expanded = bulkClassDropdownExpanded,
                            onExpandedChange = { bulkClassDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedClass.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("২. শ্রেণি", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Class, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkClassDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = bulkClassDropdownExpanded,
                                onDismissRequest = { bulkClassDropdownExpanded = false }
                            ) {
                                currentBoardClasses.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text(cls.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedClass = cls
                                            val subs = getAdminClassSubjects(cls.id)
                                            selectedSubject = subs.first()
                                            val chs = allChapters.filter { it.subjectId == selectedSubject.id }
                                            selectedChapter = chs.firstOrNull()
                                            val tps = if (selectedChapter != null) allTopics.filter { it.chapterId == selectedChapter!!.id } else emptyList()
                                            selectedTopic = tps.firstOrNull()
                                            bulkClassDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Subject
                        ExposedDropdownMenuBox(
                            expanded = bulkSubjectDropdownExpanded,
                            onExpandedChange = { bulkSubjectDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedSubject.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("৩. বিষয়", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkSubjectDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = bulkSubjectDropdownExpanded,
                                onDismissRequest = { bulkSubjectDropdownExpanded = false }
                            ) {
                                classSubjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = { Text(subject.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedSubject = subject
                                            val chs = allChapters.filter { it.subjectId == subject.id }
                                            selectedChapter = chs.firstOrNull()
                                            val tps = if (selectedChapter != null) allTopics.filter { it.chapterId == selectedChapter!!.id } else emptyList()
                                            selectedTopic = tps.firstOrNull()
                                            bulkSubjectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Chapter
                        ExposedDropdownMenuBox(
                            expanded = bulkChapterDropdownExpanded,
                            onExpandedChange = { bulkChapterDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedChapter?.title ?: "কোনো অধ্যায় নেই",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("৪. অধ্যায়", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkChapterDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = bulkChapterDropdownExpanded,
                                onDismissRequest = { bulkChapterDropdownExpanded = false }
                            ) {
                                filteredChapters.forEach { chapter ->
                                    DropdownMenuItem(
                                        text = { Text(chapter.title) },
                                        onClick = {
                                            selectedChapter = chapter
                                            val tps = allTopics.filter { it.chapterId == chapter.id }
                                            selectedTopic = tps.firstOrNull()
                                            bulkChapterDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Topic
                        ExposedDropdownMenuBox(
                            expanded = bulkTopicDropdownExpanded,
                            onExpandedChange = { bulkTopicDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedTopic?.title ?: "কোনো টপিক নেই",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("৫. টপিক", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Topic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkTopicDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = bulkTopicDropdownExpanded,
                                onDismissRequest = { bulkTopicDropdownExpanded = false }
                            ) {
                                filteredTopics.forEach { topic ->
                                    DropdownMenuItem(
                                        text = { Text(topic.title) },
                                        onClick = {
                                            selectedTopic = topic
                                            bulkTopicDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sample Templates Download & Copy
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("নমুনা ফরম্যাট", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            TextButton(onClick = { showJsonGuideDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("স্ট্রাকচার গাইড", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { downloadQuestionJsonLauncher.launch("desh_exam_sample_questions.json") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("📥 নমুনা JSON ডাউনলোড", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(sampleJsonTemplate))
                                    bulkFileStatusMessage = "✓ নমুনা JSON কোড ক্লিপবোর্ডে কপি করা হয়েছে!"
                                    bulkFileStatusSuccess = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("📋 কপি JSON", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    bulkImportText = sampleJsonTemplate
                                    bulkFileStatusMessage = "✓ নমুনা JSON এডিটরে লোড হয়েছে!"
                                    bulkFileStatusSuccess = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📄 এডিটরে JSON লোড করুন", fontSize = 11.sp, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = {
                                    bulkImportText = sampleCsvTemplate
                                    bulkFileStatusMessage = "✓ নমুনা পাইপ ফরম্যাট এডিটরে লোড হয়েছে!"
                                    bulkFileStatusSuccess = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📋 নমুনা পাইপ ফরম্যাট লোড", fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Input TextField
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "প্রশ্নের ডেটা পেস্ট করুন:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = bulkImportText,
                        onValueChange = { bulkImportText = it },
                        placeholder = {
                            Text("এখানে নমুনা ফরম্যাট অনুযায়ী প্রশ্ন পেস্ট করুন (JSON অ্যারে বা পাইপ-সেপারেটেড)...")
                        },
                        minLines = 8,
                        maxLines = 16,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        viewModel.importBulkQuestions(
                            rawInput = bulkImportText,
                            classId = selectedClass.id,
                            subjectId = selectedSubject.id,
                            chapterId = selectedChapter?.id ?: "",
                            topicId = selectedTopic?.id ?: "",
                            pyqYear = bulkPyqYearInput,
                            customTestTitle = bulkCustomTestTitle
                        ) { success, msg, _ ->
                            onAction(success, msg)
                            if (success) {
                                bulkImportText = ""
                            }
                        }
                    },
                    enabled = !isLoading && bulkImportText.isNotBlank() && selectedChapter != null && selectedTopic != null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("সংরক্ষণ হচ্ছে...", color = Color.White)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🚀 একসাথে সব প্রশ্ন আপলোড করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        } else {
            // SUB-TAB 1: CURRICULUM (Chapters & Topics)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🏫 সিলেবাস ও কারিকুলাম ব্যাচ আপলোড (CSV / JSON)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "নির্দিষ্ট বিষয়ের জন্য এক ক্লিকে একাধিক অধ্যায় ও তাদের টপিক একসাথে JSON বা CSV/Pipe ফরম্যাটে পেস্ট করে ফায়ারবেস ক্লাউড ও লোকাল ডেটাবেজে ব্যাচ ইমপোর্ট করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("টার্গেট শ্রেণি ও বিষয় নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = bulkClassDropdownExpanded,
                                onExpandedChange = { bulkClassDropdownExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedClass.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("১. শ্রেণি", maxLines = 1) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkClassDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = bulkClassDropdownExpanded,
                                    onDismissRequest = { bulkClassDropdownExpanded = false }
                                ) {
                                    allClasses.forEach { cls ->
                                        DropdownMenuItem(
                                            text = { Text(cls.name) },
                                            onClick = {
                                                selectedClass = cls
                                                val subs = getAdminClassSubjects(cls.id)
                                                selectedSubject = subs.first()
                                                bulkClassDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = bulkSubjectDropdownExpanded,
                                onExpandedChange = { bulkSubjectDropdownExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedSubject.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("২. বিষয়", maxLines = 1) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkSubjectDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = bulkSubjectDropdownExpanded,
                                    onDismissRequest = { bulkSubjectDropdownExpanded = false }
                                ) {
                                    classSubjects.forEach { subject ->
                                        DropdownMenuItem(
                                            text = { Text(subject.name) },
                                            onClick = {
                                                selectedSubject = subject
                                                bulkSubjectDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { downloadSyllabusJsonLauncher.launch("desh_exam_sample_syllabus.json") },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সিলেবাস JSON ডাউনলোড", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(sampleCurriculumJsonTemplate))
                            bulkFileStatusMessage = "✓ সিলেবাস JSON কপি করা হয়েছে!"
                            bulkFileStatusSuccess = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কপি JSON", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "কারিকুলাম ডেটা পেস্ট করুন:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = bulkImportText,
                        onValueChange = { bulkImportText = it },
                        placeholder = { Text("এখানে নমুনা ফরম্যাট অনুযায়ী অধ্যায় ও টপিকের JSON অ্যারে অথবা CSV/Pipe লাইন পেস্ট করুন...") },
                        minLines = 8,
                        maxLines = 16,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.importBulkCurriculum(
                            rawInput = bulkImportText,
                            classId = selectedClass.id,
                            subjectId = selectedSubject.id
                        ) { success, msg, _, _ ->
                            onAction(success, msg)
                            if (success) {
                                bulkImportText = ""
                            }
                        }
                    },
                    enabled = !isLoading && bulkImportText.isNotBlank() && selectedSubject.id.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("সংরক্ষণ হচ্ছে...", color = Color.White)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("একসাথে সব অধ্যায় ও টপিক আপলোড করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }

    if (showJsonGuideDialog) {
        Dialog(
            onDismissRequest = { showJsonGuideDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.88f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Text("JSON ফাইল স্ট্রাকচার গাইড", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        IconButton(onClick = { showJsonGuideDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("• question : মূল প্রশ্ন (String - আবশ্যক)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("• options : ৪টি অপশনের লিস্ট (Array of Strings - আবশ্যক)", fontSize = 12.sp)
                        Text("• correctOptionIndex : সঠিক উত্তরের ইনডেক্স (0=১ম, 1=২য়, 2=৩য়, 3=৪র্থ) (Number - আবশ্যক)", fontSize = 12.sp, color = CorrectGreen, fontWeight = FontWeight.SemiBold)
                        Text("• explanation : সঠিক উত্তরের ব্যাখ্যা ও সমাধান (String - ঐচ্ছিক)", fontSize = 12.sp)
                        Text("• difficulty : 'EASY', 'MEDIUM', অথবা 'HARD' (String - ঐচ্ছিক)", fontSize = 12.sp)
                        Text("• marks : প্রশ্নের মান (যেমন: 1, 2) (Number - ঐচ্ছিক)", fontSize = 12.sp)
                        Text("• pyqYear : বিগত বছরের সাল যেমন '2024' (String - ঐচ্ছিক)", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showJsonGuideDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বুঝেছি, বন্ধ করুন")
                    }
                }
            }
        }
    }
}
