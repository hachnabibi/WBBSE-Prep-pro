package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChapterEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TopicEntity
import com.example.data.model.AcademicClass
import com.example.data.model.AcademicClassProvider
import com.example.data.model.EducationBoard
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.util.formatCompactNumber

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

    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        try {
            dbSubjects = viewModel.repository.dao.getAllSubjectsOnce()
            dbChapters = viewModel.repository.dao.getAllChaptersOnce()
            dbTopics = viewModel.repository.dao.getAllTopicsOnce()
        } catch (e: Exception) {
            android.util.Log.e("AdminCurriculumCRUDTab", "Error loading DB entities: ${e.message}", e)
        }
    }

    // Selectors State
    var boardValue by remember { mutableStateOf("WBBSE") }
    var classIdValue by remember { mutableStateOf("class_10_wbbse") }
    var streamValue by remember { mutableStateOf("all") } // "all", "science", "arts", "commerce", "common"
    var subjectIdValue by remember { mutableStateOf("") }
    var selectedChapter by remember { mutableStateOf<ChapterEntity?>(null) }

    // Dropdowns visibility
    var boardExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }
    var streamExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }

    // Search query for chapters/topics
    var chapterSearchQuery by remember { mutableStateOf("") }
    var topicSearchQuery by remember { mutableStateOf("") }

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

    val boards = if (liveBoards.isNotEmpty()) liveBoards else AcademicClassProvider.ALL_BOARDS
    val classes = if (liveClasses.isNotEmpty()) liveClasses else AcademicClassProvider.ALL_CLASSES
    
    val filteredClasses = remember(boardValue, classes) {
        classes.filter { it.board.equals(boardValue, ignoreCase = true) }
    }

    val availableSubjects = remember(classIdValue, dbSubjects, streamValue) {
        val base = dbSubjects.filter { 
            it.classId == classIdValue && (streamValue == "all" || it.stream.equals(streamValue, ignoreCase = true))
        }
        if (base.isNotEmpty()) base else {
            val allDefaults = listOf(
                SubjectEntity("${classIdValue}_bengali", "bengali", "বাংলা", "Bengali", 10, 0, 0, 0xFFE11D48L, classIdValue, "common"),
                SubjectEntity("${classIdValue}_english", "english", "ইংরেজি", "English", 10, 0, 0, 0xFF2563EBL, classIdValue, "common"),
                SubjectEntity("${classIdValue}_mathematics", "mathematics", "গণিত", "Mathematics", 10, 0, 0, 0xFF059669L, classIdValue, "common"),
                SubjectEntity("${classIdValue}_physical_science", "physical_science", "ভৌতবিজ্ঞান", "Physical Science", 10, 0, 0, 0xFF8B5CF6L, classIdValue, "science"),
                SubjectEntity("${classIdValue}_life_science", "life_science", "জীবনবিজ্ঞান", "Life Science", 10, 0, 0, 0xFFF59E0BL, classIdValue, "science"),
                SubjectEntity("${classIdValue}_history", "history", "ইতিহাস", "History", 10, 0, 0, 0xFFEF4444L, classIdValue, "arts"),
                SubjectEntity("${classIdValue}_geography", "geography", "ভূগোল", "Geography", 10, 0, 0, 0xFF10B981L, classIdValue, "arts")
            )
            allDefaults.filter { streamValue == "all" || it.stream.equals(streamValue, ignoreCase = true) }
        }
    }

    // Auto select first subject when class or stream changes
    LaunchedEffect(classIdValue, streamValue, availableSubjects) {
        if (subjectIdValue.isBlank() || availableSubjects.none { it.id == subjectIdValue }) {
            subjectIdValue = availableSubjects.firstOrNull()?.id ?: ""
        }
    }

    val chaptersForSubject = remember(subjectIdValue, dbChapters, refreshTrigger) {
        dbChapters.filter { it.subjectId == subjectIdValue }
    }

    val filteredChapters = remember(chaptersForSubject, chapterSearchQuery) {
        if (chapterSearchQuery.isBlank()) chaptersForSubject
        else chaptersForSubject.filter { it.title.contains(chapterSearchQuery, ignoreCase = true) || it.id.contains(chapterSearchQuery, ignoreCase = true) }
    }

    // Auto select first chapter of selected subject
    LaunchedEffect(chaptersForSubject) {
        if (selectedChapter == null || chaptersForSubject.none { it.id == selectedChapter?.id }) {
            selectedChapter = chaptersForSubject.firstOrNull()
        }
    }

    val topicsForChapter = remember(selectedChapter, dbTopics, refreshTrigger) {
        val chId = selectedChapter?.id ?: ""
        if (chId.isNotBlank()) dbTopics.filter { it.chapterId == chId } else emptyList()
    }

    val filteredTopics = remember(topicsForChapter, topicSearchQuery) {
        if (topicSearchQuery.isBlank()) topicsForChapter
        else topicsForChapter.filter { it.title.contains(topicSearchQuery, ignoreCase = true) || it.id.contains(topicSearchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ==========================================
        // 1. PREMIUM FILTER & CONTROL CARD
        // ==========================================
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (isBengali) "সিলেবাস ফিল্টার ও কন্ট্রোল" else "Syllabus Filter & Controls",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Stat Chip for currently loaded items
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryIndigo.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "${formatCompactNumber(chaptersForSubject.size)} অধ্যায় • ${formatCompactNumber(topicsForChapter.size)} টপিক",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Dropdowns Grid (Board, Class, Subject)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Board Selector
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = boardValue,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("বোর্ড", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(15.dp), tint = PrimaryIndigo)
                            },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { boardExpanded = true },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                        )
                        DropdownMenu(expanded = boardExpanded, onDismissRequest = { boardExpanded = false }) {
                            boards.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text(b.shortName, fontSize = 12.0.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(15.dp), tint = PrimaryIndigo) },
                                    onClick = {
                                        boardValue = b.id
                                        boardExpanded = false
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
                            label = { Text("শ্রেণি", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(15.dp), tint = CorrectGreen)
                            },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { classExpanded = true },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CorrectGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                        )
                        DropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                            filteredClasses.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls.name, fontSize = 12.0.sp) },
                                    leadingIcon = { Icon(Icons.Default.School, null, modifier = Modifier.size(15.dp), tint = CorrectGreen) },
                                    onClick = {
                                        classIdValue = cls.id
                                        classExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Sub-class / Stream Selector
                    Box(modifier = Modifier.weight(1f)) {
                        val streamsList = listOf(
                            "all" to "সকল বিভাগ",
                            "science" to "বিজ্ঞান শাখা",
                            "arts" to "কলা বিভাগ",
                            "commerce" to "বাণিজ্য শাখা",
                            "common" to "সাধারণ বিষয়"
                        )
                        val currentStreamLabel = streamsList.find { it.first == streamValue }?.second ?: "সকল বিভাগ"
                        OutlinedTextField(
                            value = currentStreamLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("শাখা", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(15.dp), tint = ExamGold)
                            },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { streamExpanded = true },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ExamGold,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                        )
                        DropdownMenu(expanded = streamExpanded, onDismissRequest = { streamExpanded = false }) {
                            streamsList.forEach { (id, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 12.0.sp) },
                                    leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(15.dp), tint = ExamGold) },
                                    onClick = {
                                        streamValue = id
                                        streamExpanded = false
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
                    val subColor = Color(currentSubObj?.colorHex ?: 0xFF6366F1L)

                    OutlinedTextField(
                        value = subLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBengali) "বিষয়" else "Subject", fontSize = 11.sp) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(subColor)
                            )
                        },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { subjectExpanded = true },
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryIndigo,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    )
                    DropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        availableSubjects.forEach { sub ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(sub.colorHex))
                                        )
                                        Text("${sub.name} (${sub.code})", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                },
                                onClick = {
                                    subjectIdValue = sub.id
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Action Buttons Row (Batch Upload & Cloud Backup)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Batch Upload Button
                    OutlinedButton(
                        onClick = {
                            batchImportText = ""
                            showBatchUploadDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = PrimaryIndigo.copy(alpha = 0.05f),
                            contentColor = PrimaryIndigo
                        ),
                        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.35f)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "ব্যাচ আপলোড" else "Batch Upload",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            maxLines = 1
                        )
                    }

                    // Cloud Backup Button
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
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExamGold,
                            contentColor = Color(0xFF1E1B2E)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.5.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        if (isPushingToCloud) {
                            CircularProgressIndicator(modifier = Modifier.size(15.dp), color = Color(0xFF1E1B2E), strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "পুশ হচ্ছে..." else "Pushing...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1E1B2E))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "ফায়ারবেসে ব্যাকআপ" else "Backup to Cloud (Push)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. CHAPTERS & TOPICS DUAL MANAGEMENT PANELS
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ------------------------------------------
            // LEFT COLUMN: CHAPTERS PANEL
            // ------------------------------------------
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Bar
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
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isBengali) "অধ্যায় (${chaptersForSubject.size})" else "Chapters (${chaptersForSubject.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                editingChapter = null
                                formChNum = (chaptersForSubject.size + 1).toString()
                                formChId = "${subjectIdValue}_ch${formChNum}"
                                formChTitle = ""
                                formChDesc = ""
                                formChTotalTopics = "5"
                                formChWeightage = "15"
                                formChContentData = ""
                                formChSolutionData = ""
                                showChapterDialog = true
                            },
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = PrimaryIndigo.copy(alpha = 0.12f),
                                contentColor = PrimaryIndigo
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Chapter", modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Chapter List
                    if (chaptersForSubject.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = if (isBengali) "কোনো অধ্যায় নেই" else "No Chapters",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredChapters, key = { it.id }) { ch ->
                                val isSelected = selectedChapter?.id == ch.id
                                val animatedBorderColor by animateColorAsState(
                                    if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                    label = "borderColor"
                                )
                                val animatedBgColor by animateColorAsState(
                                    if (isSelected) PrimaryIndigo.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                                    label = "bgColor"
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = animatedBgColor,
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, animatedBorderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedChapter = ch }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Chapter Number Circular Badge
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${ch.chapterNumber}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Text(
                                                    text = ch.title,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Edit & Delete Action Buttons
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
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
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Edit",
                                                        tint = PrimaryIndigo,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { deletingChapter = ch },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete",
                                                        tint = WrongRed,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Subtle ID & Topic count chip
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = ch.id,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isSelected) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = PrimaryIndigo.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "✓ সক্রিয়",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryIndigo,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
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

            // ------------------------------------------
            // RIGHT COLUMN: TOPICS PANEL
            // ------------------------------------------
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Bar
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
                                Icons.Default.Label,
                                contentDescription = null,
                                tint = SecondaryEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isBengali) "টপিক (${topicsForChapter.size})" else "Topics (${topicsForChapter.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

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
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = SecondaryEmerald.copy(alpha = 0.12f),
                                contentColor = SecondaryEmerald
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Topic", modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Topic List
                    if (selectedChapter == null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.TouchApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = if (isBengali) "বামপাশ থেকে অধ্যায় বেছে নিন" else "Select a Chapter First",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (topicsForChapter.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.NoteAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = if (isBengali) "কোনো টপিক নেই" else "No Topics Found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredTopics, key = { it.id }) { tp ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Topic Order Badge
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(SecondaryEmerald.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${tp.orderIndex}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SecondaryEmerald
                                                    )
                                                }

                                                Text(
                                                    text = tp.title,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Edit & Delete Action Buttons
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
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
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Edit",
                                                        tint = SecondaryEmerald,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { deletingTopic = tp },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete",
                                                        tint = WrongRed,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = tp.id,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 3. DIALOGS & MODALS
    // ==========================================

    // 1. ADD / EDIT CHAPTER DIALOG
    if (showChapterDialog) {
        val isEdit = editingChapter != null
        AlertDialog(
            onDismissRequest = { showChapterDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (isEdit) Icons.Default.Edit else Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = PrimaryIndigo
                    )
                    Text(
                        text = if (isEdit) "অধ্যায় এডিট করুন" else "নতুন অধ্যায় তৈরি করুন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = formChId,
                        onValueChange = { if (!isEdit) formChId = it.lowercase().replace(" ", "_") },
                        readOnly = isEdit,
                        label = { Text("অধ্যায় আইডি") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = formChNum,
                        onValueChange = {
                            formChNum = it
                            if (!isEdit) {
                                formChId = "${subjectIdValue}_ch${it.trim()}"
                            }
                        },
                        readOnly = isEdit,
                        label = { Text("অধ্যায় নম্বর") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = formChTitle,
                        onValueChange = { formChTitle = it },
                        label = { Text("অধ্যায়ের শিরোনাম") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = formChDesc,
                        onValueChange = { formChDesc = it },
                        label = { Text("অধ্যায়ের বিবরণ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = formChTotalTopics,
                        onValueChange = { formChTotalTopics = it },
                        label = { Text("মোট টপিক সংখ্যা") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = formChContentData,
                        onValueChange = { formChContentData = it },
                        label = { Text("অধ্যায়ের নোট ও পড়াশোনা") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = formChSolutionData,
                        onValueChange = { formChSolutionData = it },
                        label = { Text("অধ্যায়ের সমাধান ও প্রশ্নোত্তর") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
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
                        viewModel.addFirestoreChapter(ch, preserveWeightage = isEdit) { success, msg ->
                            onShowMessage(msg, success)
                            if (success) {
                                showChapterDialog = false
                                refreshTrigger++
                            }
                        }
                    },
                    enabled = formChId.isNotBlank() && formChTitle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isEdit) "আপডেট করুন" else "তৈরি করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChapterDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // 2. ADD / EDIT TOPIC DIALOG
    if (showTopicDialog) {
        val isEdit = editingTopic != null
        AlertDialog(
            onDismissRequest = { showTopicDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (isEdit) Icons.Default.Edit else Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = SecondaryEmerald
                    )
                    Text(
                        text = if (isEdit) "টপিক এডিট করুন" else "নতুন টপিক তৈরি করুন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = formTpId,
                        onValueChange = { if (!isEdit) formTpId = it.lowercase().replace(" ", "_") },
                        readOnly = isEdit,
                        label = { Text("টপিক আইডি") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
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
                        label = { Text("টপিক ক্রমিক নম্বর") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = formTpTitle,
                        onValueChange = { formTpTitle = it },
                        label = { Text("টপিকের নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = formTpSummary,
                        onValueChange = { formTpSummary = it },
                        label = { Text("সংক্ষিপ্ত সারসংক্ষেপ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = formTpKeyPoints,
                        onValueChange = { formTpKeyPoints = it },
                        label = { Text("গুরুত্বপূর্ণ পয়েন্টসমূহ ('|' দিয়ে আলাদা করুন)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = formTpFormulae,
                        onValueChange = { formTpFormulae = it },
                        label = { Text("সূত্র বা সমাধানসমূহ ('|' দিয়ে আলাদা করুন)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = formTpContentData,
                        onValueChange = { formTpContentData = it },
                        label = { Text("টপিক নোট / বিস্তারিত তথ্য") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = formTpVideoUrl,
                        onValueChange = { formTpVideoUrl = it },
                        label = { Text("ভিডিও টিউটোরিয়াল লিংক") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
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
                        viewModel.addFirestoreTopic(tp, preserveQuestionCount = isEdit) { success, msg ->
                            onShowMessage(msg, success)
                            if (success) {
                                showTopicDialog = false
                                refreshTrigger++
                            }
                        }
                    },
                    enabled = formTpId.isNotBlank() && formTpTitle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isEdit) "আপডেট করুন" else "তৈরি করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopicDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // 3. DELETE CHAPTER CONFIRMATION
    if (deletingChapter != null) {
        AlertDialog(
            onDismissRequest = { deletingChapter = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WrongRed) },
            title = { Text("অধ্যায় ডিলিট করুন?", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিতভাবে '${deletingChapter!!.title}' অধ্যায়টি ডিলিট করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFirestoreChapter(deletingChapter!!.id) { success, msg ->
                            onShowMessage(msg, success)
                            deletingChapter = null
                            refreshTrigger++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingChapter = null }) { Text("বাতিল") }
            }
        )
    }

    // 4. DELETE TOPIC CONFIRMATION
    if (deletingTopic != null) {
        AlertDialog(
            onDismissRequest = { deletingTopic = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WrongRed) },
            title = { Text("টপিক ডিলিট করুন?", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিতভাবে '${deletingTopic!!.title}' টপিকটি ডিলিট করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFirestoreTopic(deletingTopic!!.id) { success, msg ->
                            onShowMessage(msg, success)
                            deletingTopic = null
                            refreshTrigger++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTopic = null }) { Text("বাতিল") }
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
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PrimaryIndigo)
                    Text("ব্যাচ আপলোড (CSV / JSON)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "বিষয়: ${availableSubjects.find { it.id == subjectIdValue }?.name ?: subjectIdValue}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = PrimaryIndigo
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { batchImportText = sampleCurriculumJson },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("নমুনা JSON", fontSize = 11.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { batchImportText = sampleCurriculumCsv },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("নমুনা CSV", fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    OutlinedTextField(
                        value = batchImportText,
                        onValueChange = { batchImportText = it },
                        placeholder = { Text("এখানে JSON অ্যারে বা CSV/Pipe লাইন পেস্ট করুন...", fontSize = 11.5.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(10.dp)
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
                                    "✓ শনাক্ত হয়েছে: $parsedChCount টি অধ্যায় ও $parsedTpCount টি টপিক"
                                else
                                    " কোনো অধ্যায়/টপিক পাওয়া যায়নি।",
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
                    enabled = !isUploading && batchImportText.isNotBlank() && subjectIdValue.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(15.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("আপলোড হচ্ছে...", fontSize = 12.sp)
                    } else {
                        Text("আপলোড করুন", fontSize = 12.sp)
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
