package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChapterEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TopicEntity
import com.example.data.model.AcademicClass
import com.example.data.model.EducationBoard
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ExamGold
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCurriculumCRUDTab(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onShowMessage: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val liveBoards by viewModel.allBoards.collectAsState()
    val liveClasses by viewModel.allClasses.collectAsState()

    var dbSubjects by remember { mutableStateOf<List<SubjectEntity>>(emptyList()) }
    var dbChapters by remember { mutableStateOf<List<ChapterEntity>>(emptyList()) }
    var dbTopics by remember { mutableStateOf<List<TopicEntity>>(emptyList()) }

    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        try {
            dbSubjects = viewModel.repository.dao.getAllSubjectsOnce()
            dbChapters = viewModel.repository.dao.getAllChaptersOnce()
            dbTopics = viewModel.repository.dao.getAllTopicsOnce()
        } catch (e: Exception) {
            android.util.Log.e("AdminCurriculumCRUDTab", "Error loading DB entities: ${e.message}", e)
        }
    }

    // 1. Selectors State
    var boardValue by remember { mutableStateOf("WBBSE") }
    var classIdValue by remember { mutableStateOf("class_10_wbbse") }
    var subjectIdValue by remember { mutableStateOf("") }
    var selectedChapter by remember { mutableStateOf<ChapterEntity?>(null) }

    // Dropdowns visibility
    var boardExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }

    // Dialogs States
    var showChapterDialog by remember { mutableStateOf(false) }
    var showTopicDialog by remember { mutableStateOf(false) }
    var editingChapter by remember { mutableStateOf<ChapterEntity?>(null) }
    var editingTopic by remember { mutableStateOf<TopicEntity?>(null) }
    var deletingChapter by remember { mutableStateOf<ChapterEntity?>(null) }
    var deletingTopic by remember { mutableStateOf<TopicEntity?>(null) }
    var showBatchUploadDialog by remember { mutableStateOf(false) }
    var batchImportText by remember { mutableStateOf("") }

    val sampleCurriculumJson = """[
  {
    "chapterNumber": 1,
    "title": "বল ও গতি",
    "description": "গতির সমীকরণ ও বলের ধারণা",
    "topics": [
      {
        "orderIndex": 1,
        "title": "গতির প্রথম সমীকরণ",
        "summary": "v = u + at সমীকরণ",
        "keyPoints": ["v = অন্তিম বেগ", "u = প্রাথমিক বেগ"],
        "formulae": ["v = u + at"],
        "contentData": "গতির প্রথম সমীকরণটি হলো..."
      }
    ]
  }
]"""

    val sampleCurriculumCsv = """chapterNumber | title | description | orderIndex | topicTitle | summary | keyPoints | formulae | contentData | videoUrl
1 | বল ও গতি | গতির সমীকরণ ও বলের ধারণা | 1 | গতির প্রথম সমীকরণ | v = u + at সমীকরণ | v = অন্তিম বেগ # u = প্রাথমিক বেগ | v = u + at | গতির প্রথম সমীকরণটি হলো... | """

    // Form fields - Chapter
    var formChId by remember { mutableStateOf("") }
    var formChNum by remember { mutableStateOf("1") }
    var formChTitle by remember { mutableStateOf("") }
    var formChDesc by remember { mutableStateOf("") }
    var formChTotalTopics by remember { mutableStateOf("5") }
    var formChWeightage by remember { mutableStateOf("15") }
    var formChContentData by remember { mutableStateOf("") }
    var formChSolutionData by remember { mutableStateOf("") }

    // Form fields - Topic
    var formTpId by remember { mutableStateOf("") }
    var formTpOrder by remember { mutableStateOf("1") }
    var formTpTitle by remember { mutableStateOf("") }
    var formTpSummary by remember { mutableStateOf("") }
    var formTpKeyPoints by remember { mutableStateOf("") }
    var formTpFormulae by remember { mutableStateOf("") }
    var formTpContentData by remember { mutableStateOf("") }
    var formTpVideoUrl by remember { mutableStateOf("") }
    var formTpPlaylistJson by remember { mutableStateOf("") }

    val boards = if (liveBoards.isNotEmpty()) liveBoards else com.example.data.model.AcademicClassProvider.ALL_BOARDS
    val classes = if (liveClasses.isNotEmpty()) liveClasses else com.example.data.model.AcademicClassProvider.ALL_CLASSES
    
    val filteredClasses = remember(boardValue, classes) {
        classes.filter { it.board.equals(boardValue, ignoreCase = true) }
    }

    val availableSubjects = remember(classIdValue, dbSubjects) {
        val base = dbSubjects.filter { it.classId == classIdValue }
        if (base.isNotEmpty()) base else {
            // fallback
            listOf(
                SubjectEntity("${classIdValue}_bengali", "bengali", "বাংলা (Bengali)", "Bengali", 10, 0, 0, 0xFFE11D48L, classIdValue, "common"),
                SubjectEntity("${classIdValue}_english", "english", "ইংরেজি (English)", "English", 10, 0, 0, 0xFF2563EBL, classIdValue, "common"),
                SubjectEntity("${classIdValue}_mathematics", "mathematics", "গণিত (Mathematics)", "Mathematics", 10, 0, 0, 0xFF059669L, classIdValue, "common")
            )
        }
    }

    // Auto select first subject when class changes
    LaunchedEffect(classIdValue, availableSubjects) {
        if (subjectIdValue.isBlank() || availableSubjects.none { it.id == subjectIdValue }) {
            subjectIdValue = availableSubjects.firstOrNull()?.id ?: ""
        }
    }

    val chaptersForSubject = remember(subjectIdValue, dbChapters, refreshTrigger) {
        dbChapters.filter { it.subjectId == subjectIdValue || it.subjectId == subjectIdValue.substringAfterLast("_", "") }
    }

    // Auto select first chapter of selected subject
    LaunchedEffect(chaptersForSubject) {
        selectedChapter = chaptersForSubject.firstOrNull()
    }

    val topicsForChapter = remember(selectedChapter, dbTopics, refreshTrigger) {
        val chId = selectedChapter?.id ?: ""
        if (chId.isNotBlank()) dbTopics.filter { it.chapterId == chId } else emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. SELECTION BAR (BOARD, CLASS, SUBJECT) ---
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isBengali) "🎯 সিলেক্টর বার (সিলেবাস ফিল্টার করুন)" else "🎯 Selection Bar (Filter Syllabus)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Board Selector
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = boardValue,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBengali) "বোর্ড (Board)" else "Board") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth().clickable { boardExpanded = true },
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                        )
                        DropdownMenu(expanded = boardExpanded, onDismissRequest = { boardExpanded = false }) {
                            boards.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text("${b.shortName} - ${b.fullNameBengali}", fontSize = 13.sp) },
                                    onClick = {
                                        boardValue = b.id
                                        boardExpanded = false
                                        // Reset class and subject
                                        val firstCls = classes.firstOrNull { it.board.equals(b.id, ignoreCase = true) }
                                        classIdValue = firstCls?.id ?: ""
                                    }
                                )
                            }
                        }
                    }

                    // Class Selector
                    Box(modifier = Modifier.weight(1f)) {
                        val currentClassObj = classes.find { it.id == classIdValue }
                        val classLabel = currentClassObj?.name ?: classIdValue
                        OutlinedTextField(
                            value = classLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBengali) "ক্লাস (Class)" else "Class") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth().clickable { classExpanded = true }
                        )
                        DropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                            filteredClasses.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls.name, fontSize = 13.sp) },
                                    onClick = {
                                        classIdValue = cls.id
                                        classExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Subject Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    val currentSubObj = availableSubjects.find { it.id == subjectIdValue }
                    val subLabel = currentSubObj?.name ?: subjectIdValue
                    OutlinedTextField(
                        value = subLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBengali) "বিষয় (Subject)" else "Subject") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth().clickable { subjectExpanded = true },
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                    DropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                        availableSubjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text("${sub.name} (${sub.code})", fontSize = 13.sp) },
                                onClick = {
                                    subjectIdValue = sub.id
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Batch Upload Button for Selected Subject
                OutlinedButton(
                    onClick = {
                        batchImportText = ""
                        showBatchUploadDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "📥 অধ্যায় ও টপিক ব্যাচ আপলোড (CSV / JSON)" else "📥 Batch Upload Chapters & Topics (CSV/JSON)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Cloud Backup & Push Button
                var isPushingToCloud by remember { mutableStateOf(false) }

                Button(
                    onClick = {
                        isPushingToCloud = true
                        viewModel.pushLocalDatabaseToFirebase { success, msg ->
                            isPushingToCloud = false
                            onShowMessage(msg, success)
                            if (success) refreshTrigger++
                        }
                    },
                    enabled = !isPushingToCloud,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExamGold,
                        contentColor = Color(0xFF1E1B2E)
                    )
                ) {
                    if (isPushingToCloud) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF1E1B2E))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "ফায়ারবেসে পুশ হচ্ছে..." else "Pushing to Firebase...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E1B2E)
                        )
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1E1B2E))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "☁️ সম্পূর্ণ লোকাল ডেটা ফায়ারবেসে ব্যাকআপ রাখুন (Push)" else "☁️ Backup Local Data to Firebase (Push)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E1B2E)
                        )
                    }
                }
            }
        }

        // --- 2. DOUBLE COLUMN OR STACKED ACTION PANELS ---
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // LEFT COLUMN: CHAPTERS
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBengali) "📖 অধ্যায় সমূহ (${chaptersForSubject.size})" else "📖 Chapters (${chaptersForSubject.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    
                    IconButton(
                        onClick = {
                            editingChapter = null
                            formChId = ""
                            formChNum = (chaptersForSubject.size + 1).toString()
                            formChTitle = ""
                            formChDesc = ""
                            formChTotalTopics = "5"
                            formChWeightage = "15"
                            formChContentData = ""
                            formChSolutionData = ""
                            showChapterDialog = true
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    }
                }

                if (chaptersForSubject.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isBengali) "কোন অধ্যায় নেই" else "No Chapters", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentPadding = PaddingValues(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(chaptersForSubject, key = { it.id }) { ch ->
                            val isSelected = selectedChapter?.id == ch.id
                            val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent
                            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent
                            
                            Card(
                                onClick = { selectedChapter = ch },
                                colors = CardDefaults.cardColors(containerColor = bgColor),
                                border = BorderStroke(1.dp, borderColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${ch.chapterNumber}. ${ch.title}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "ID: ${ch.id}",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingChapter = ch
                                                formChId = ch.id
                                                formChNum = ch.chapterNumber.toString()
                                                formChTitle = ch.title
                                                formChDesc = ch.description
                                                formChTotalTopics = ch.totalTopics.toString()
                                                formChWeightage = "15"
                                                formChContentData = ch.contentData ?: ""
                                                formChSolutionData = ch.solutionData ?: ""
                                                showChapterDialog = true
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        }

                                        IconButton(
                                            onClick = { deletingChapter = ch },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, null, tint = WrongRed, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // RIGHT COLUMN: TOPICS
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBengali) "🏷️ টপিক সমূহ (${topicsForChapter.size})" else "🏷️ Topics (${topicsForChapter.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    IconButton(
                        onClick = {
                            editingTopic = null
                            val chId = selectedChapter?.id ?: ""
                            formTpId = ""
                            formTpOrder = (topicsForChapter.size + 1).toString()
                            formTpTitle = ""
                            formTpSummary = ""
                            formTpKeyPoints = ""
                            formTpFormulae = ""
                            formTpContentData = ""
                            formTpVideoUrl = ""
                            formTpPlaylistJson = ""
                            if (chId.isBlank()) {
                                onShowMessage("প্রথমে একটি অধ্যায় সিলেক্ট করুন", false)
                            } else {
                                showTopicDialog = true
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    }
                }

                if (selectedChapter == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isBengali) "কোন অধ্যায় সিলেক্ট করা নেই" else "Select a Chapter First", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else if (topicsForChapter.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isBengali) "কোন টপিক নেই" else "No Topics", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentPadding = PaddingValues(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(topicsForChapter, key = { it.id }) { tp ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${tp.orderIndex}. ${tp.title}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "ID: ${tp.id}",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingTopic = tp
                                                formTpId = tp.id
                                                formTpOrder = tp.orderIndex.toString()
                                                formTpTitle = tp.title
                                                formTpSummary = tp.summary
                                                formTpKeyPoints = tp.keyPointsJson.replace("|", " | ")
                                                formTpFormulae = tp.solutionData ?: ""
                                                formTpContentData = tp.contentData ?: ""
                                                formTpVideoUrl = tp.videoUrl ?: ""
                                                formTpPlaylistJson = tp.playlistJson ?: ""
                                                showTopicDialog = true
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                        }

                                        IconButton(
                                            onClick = { deletingTopic = tp },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, null, tint = WrongRed, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- 3. DIALOGS ---

    // 1. ADD / EDIT CHAPTER DIALOG
    if (showChapterDialog) {
        val isEdit = editingChapter != null
        AlertDialog(
            onDismissRequest = { showChapterDialog = false },
            title = { Text(if (isEdit) "✏️ অধ্যায় এডিট করুন" else "➕ নতুন অধ্যায় তৈরি করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formChId,
                        onValueChange = { if (!isEdit) formChId = it.lowercase().replace(" ", "_") },
                        readOnly = isEdit,
                        label = { Text("Chapter ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formChNum,
                        onValueChange = {
                            formChNum = it
                            if (!isEdit) {
                                val subObj = dbSubjects.find { s -> s.id == subjectIdValue }
                                val subCode = subObj?.code ?: "sub"
                                formChId = "${subCode}_ch${it.trim()}"
                            }
                        },
                        readOnly = isEdit,
                        label = { Text("Chapter Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formChTitle,
                        onValueChange = { formChTitle = it },
                        label = { Text("Chapter Title (Bengali)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formChDesc,
                        onValueChange = { formChDesc = it },
                        label = { Text("Chapter Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formChTotalTopics,
                        onValueChange = { formChTotalTopics = it },
                        label = { Text("Total Topics") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formChContentData,
                        onValueChange = { formChContentData = it },
                        label = { Text("Chapter Content/Notes (HTML or Plain Text)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = formChSolutionData,
                        onValueChange = { formChSolutionData = it },
                        label = { Text("Chapter Solutions (Reference solutions)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = formChNum.trim().toIntOrNull() ?: 1
                        val ch = com.example.data.firestore.FirestoreChapter(
                            id = formChId.trim(),
                            classId = classIdValue.trim(),
                            subjectId = subjectIdValue.trim(),
                            chapterNumber = num,
                            title = formChTitle.trim(),
                            description = formChDesc.trim(),
                            totalTopics = formChTotalTopics.trim().toIntOrNull() ?: 5,
                            weightageMarks = formChWeightage.trim().toIntOrNull() ?: 15,
                            orderIndex = num,
                            contentData = formChContentData.trim().ifBlank { null },
                            solutionData = formChSolutionData.trim().ifBlank { null }
                        )
                        viewModel.addFirestoreChapter(ch) { success, msg ->
                            onShowMessage(msg, success)
                            if (success) {
                                showChapterDialog = false
                                refreshTrigger++
                            }
                        }
                    },
                    enabled = formChId.isNotBlank() && formChTitle.isNotBlank()
                ) {
                    Text("💾 " + (if (isEdit) "আপডেট" else "তৈরি করুন"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showChapterDialog = false }) { Text("বাতিল করুন") }
            }
        )
    }

    // 2. ADD / EDIT TOPIC DIALOG
    if (showTopicDialog) {
        val isEdit = editingTopic != null
        AlertDialog(
            onDismissRequest = { showTopicDialog = false },
            title = { Text(if (isEdit) "✏️ টপিক এডিট করুন" else "➕ নতুন টপিক তৈরি করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formTpId,
                        onValueChange = { if (!isEdit) formTpId = it.lowercase().replace(" ", "_") },
                        readOnly = isEdit,
                        label = { Text("Topic ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formTpOrder,
                        onValueChange = {
                            formTpOrder = it
                            if (!isEdit) {
                                val chId = selectedChapter?.id ?: "ch"
                                formTpId = "${chId}_t${it.trim()}"
                            }
                        },
                        readOnly = isEdit,
                        label = { Text("Topic Order Index") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formTpTitle,
                        onValueChange = { formTpTitle = it },
                        label = { Text("Topic Title (Bengali)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formTpSummary,
                        onValueChange = { formTpSummary = it },
                        label = { Text("Summary") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formTpKeyPoints,
                        onValueChange = { formTpKeyPoints = it },
                        label = { Text("Key Points (Separate with '|')") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formTpFormulae,
                        onValueChange = { formTpFormulae = it },
                        label = { Text("Formulae / Solutions (Separate with '|')") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formTpContentData,
                        onValueChange = { formTpContentData = it },
                        label = { Text("Topic Content/Detailed Study Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                    OutlinedTextField(
                        value = formTpVideoUrl,
                        onValueChange = { formTpVideoUrl = it },
                        label = { Text("Video Tutorial URL (e.g., YouTube)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formTpPlaylistJson,
                        onValueChange = { formTpPlaylistJson = it },
                        label = { Text("Playlist JSON or Resources") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ord = formTpOrder.trim().toIntOrNull() ?: 1
                        val kps = formTpKeyPoints.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        val fms = formTpFormulae.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        val tp = com.example.data.firestore.FirestoreTopic(
                            id = formTpId.trim(),
                            classId = classIdValue.trim(),
                            subjectId = subjectIdValue.trim(),
                            chapterId = (selectedChapter?.id ?: "").trim(),
                            orderIndex = ord,
                            title = formTpTitle.trim(),
                            summary = formTpSummary.trim(),
                            keyPoints = kps,
                            formulae = fms,
                            questionCount = 0,
                            contentData = formTpContentData.trim().ifBlank { null },
                            solutionData = fms.joinToString("|").ifBlank { null },
                            videoUrl = formTpVideoUrl.trim().ifBlank { null },
                            playlistJson = formTpPlaylistJson.trim().ifBlank { null }
                        )
                        viewModel.addFirestoreTopic(tp) { success, msg ->
                            onShowMessage(msg, success)
                            if (success) {
                                showTopicDialog = false
                                refreshTrigger++
                            }
                        }
                    },
                    enabled = formTpId.isNotBlank() && formTpTitle.isNotBlank()
                ) {
                    Text("💾 " + (if (isEdit) "আপডেট" else "তৈরি করুন"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopicDialog = false }) { Text("বাতিল করুন") }
            }
        )
    }

    // 3. DELETE CHAPTER CONFIRMATION
    if (deletingChapter != null) {
        AlertDialog(
            onDismissRequest = { deletingChapter = null },
            title = { Text("⚠️ অধ্যায় ডিলিট করুন?", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিতভাবে '${deletingChapter!!.title}' অধ্যায়টি এবং এর সাথে সম্পর্কিত সমস্ত তথ্য ফায়ারবেস ও লোকাল স্টোরেজ থেকে ডিলিট করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFirestoreChapter(deletingChapter!!.id) { success, msg ->
                            onShowMessage(msg, success)
                            deletingChapter = null
                            refreshTrigger++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed)
                ) {
                    Text("❌ ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingChapter = null }) { Text("বাতিল করুন") }
            }
        )
    }

    // 4. DELETE TOPIC CONFIRMATION
    if (deletingTopic != null) {
        AlertDialog(
            onDismissRequest = { deletingTopic = null },
            title = { Text("⚠️ টপিক ডিলিট করুন?", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিতভাবে '${deletingTopic!!.title}' টপিকটি ফায়ারবেস ও লোকাল স্টোরেজ থেকে ডিলিট করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFirestoreTopic(deletingTopic!!.id) { success, msg ->
                            onShowMessage(msg, success)
                            deletingTopic = null
                            refreshTrigger++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed)
                ) {
                    Text("❌ ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTopic = null }) { Text("বাতিল করুন") }
            }
        )
    }

    // 5. BATCH UPLOAD DIALOG (CSV / JSON)
    if (showBatchUploadDialog) {
        val isJson = batchImportText.trim().startsWith("[") || batchImportText.trim().startsWith("{")
        val (parsedChCount, parsedTpCount) = remember(batchImportText) {
            if (batchImportText.isBlank()) {
                0 to 0
            } else if (isJson) {
                try {
                    val arr = if (batchImportText.trim().startsWith("[")) {
                        org.json.JSONArray(batchImportText.trim())
                    } else {
                        org.json.JSONObject(batchImportText.trim()).optJSONArray("chapters") ?: org.json.JSONArray()
                    }
                    var tpCount = 0
                    for (i in 0 until arr.length()) {
                        val tArr = arr.optJSONObject(i)?.optJSONArray("topics")
                        tpCount += tArr?.length() ?: 0
                    }
                    arr.length() to tpCount
                } catch (_: Exception) { 0 to 0 }
            } else {
                val lines = batchImportText.trim().split("\n").filter { 
                    val s = it.trim()
                    s.isNotBlank() && !s.startsWith("chapterNumber") && (s.contains("|") || s.contains(","))
                }
                val chSet = mutableSetOf<String>()
                for (l in lines) {
                    val c = if (l.contains("|")) l.split("|") else l.split(",")
                    if (c.isNotEmpty()) chSet.add(c[0].trim())
                }
                chSet.size to lines.size
            }
        }
        var isUploading by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isUploading) showBatchUploadDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("📥 ব্যাচ আপলোড (CSV / JSON)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "বিষয়: ${availableSubjects.find { it.id == subjectIdValue }?.name ?: subjectIdValue}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { batchImportText = sampleCurriculumJson },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📄 নমুনা JSON", fontSize = 11.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { batchImportText = sampleCurriculumCsv },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📋 নমুনা CSV", fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    OutlinedTextField(
                        value = batchImportText,
                        onValueChange = { batchImportText = it },
                        placeholder = { Text("এখানে JSON অ্যারে বা CSV/Pipe লাইন পেস্ট করুন...") },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )

                    if (batchImportText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (parsedChCount > 0 || parsedTpCount > 0) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (parsedChCount > 0 || parsedTpCount > 0) CorrectGreen.copy(alpha = 0.4f) else WrongRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (parsedChCount > 0 || parsedTpCount > 0)
                                    "✓ শনাক্ত হয়েছে: $parsedChCount টি অধ্যায় ও $parsedTpCount টি টপিক"
                                else
                                    "⚠️ কোনো সঠিক অধ্যায়/টপিক পাওয়া যায়নি।",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (parsedChCount > 0 || parsedTpCount > 0) CorrectGreen else WrongRed,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isUploading = true
                        viewModel.importBulkCurriculum(
                            rawInput = batchImportText,
                            classId = classIdValue,
                            subjectId = subjectIdValue
                        ) { success, msg, chs, tps ->
                            isUploading = false
                            onShowMessage(msg, success)
                            if (success) {
                                showBatchUploadDialog = false
                                refreshTrigger++
                            }
                        }
                    },
                    enabled = !isUploading && batchImportText.isNotBlank() && subjectIdValue.isNotBlank()
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("আপলোড হচ্ছে...", fontSize = 12.sp)
                    } else {
                        Text("🚀 আপলোড করুন", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBatchUploadDialog = false },
                    enabled = !isUploading
                ) {
                    Text("বাতিল")
                }
            }
        )
    }
}
