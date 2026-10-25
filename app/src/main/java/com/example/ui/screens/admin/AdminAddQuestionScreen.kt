package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChapterEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TopicEntity
import com.example.data.model.AcademicClassProvider
import com.example.data.model.AcademicStreams
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ExamGold
import com.example.ui.viewmodel.DeshExamViewModel

/**
 * ট্যাব ১: নতুন প্রশ্ন সংযোজন
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddQuestionScreen(
    viewModel: DeshExamViewModel,
    isLoading: Boolean,
    isBengali: Boolean = true,
    onAction: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val firestoreQuestionsList by viewModel.firestoreQuestions.collectAsState()

    val allBoards = remember { AcademicClassProvider.ALL_BOARDS }
    var selectedBoard by remember { mutableStateOf(allBoards.find { it.id == "WBBSE" } ?: allBoards.first()) }
    var boardDropdownExpanded by remember { mutableStateOf(false) }

    val allClasses = remember { AcademicClassProvider.ALL_CLASSES }
    val classesForBoard = remember(selectedBoard, allClasses) {
        allClasses.filter { it.board.equals(selectedBoard.id, ignoreCase = true) }
    }
    var selectedClass by remember(selectedBoard) {
        mutableStateOf(classesForBoard.firstOrNull { it.id == "class_10_wbbse" } ?: classesForBoard.firstOrNull() ?: allClasses.first())
    }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    val hasStreams = remember(selectedClass) {
        selectedClass.board.equals("WBCHSE", ignoreCase = true) || selectedClass.classNumber in 11..12
    }
    val allStreams = remember { AcademicStreams.LIST }
    var selectedStream by remember(selectedClass) {
        mutableStateOf(if (hasStreams) AcademicStreams.ALL else null)
    }
    var streamDropdownExpanded by remember { mutableStateOf(false) }

    var dbSubjects by remember { mutableStateOf<List<SubjectEntity>>(emptyList()) }
    var dbChapters by remember { mutableStateOf<List<ChapterEntity>>(emptyList()) }
    var dbTopics by remember { mutableStateOf<List<TopicEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            dbSubjects = viewModel.repository.dao.getAllSubjectsOnce()
            dbChapters = viewModel.repository.dao.getAllChaptersOnce()
            dbTopics = viewModel.repository.dao.getAllTopicsOnce()
        } catch (e: Exception) {
            android.util.Log.e("AdminAddQuestionScreen", "Error loading DB entities: ${e.message}", e)
        }
    }

    val getAdminClassSubjectsHelper: (String) -> List<SubjectEntity> = { cid ->
        val filtered = dbSubjects.filter { it.classId == cid }
        if (filtered.isNotEmpty()) filtered else getAdminClassSubjects(cid)
    }

    val classSubjects = remember(selectedClass, selectedStream, dbSubjects) {
        val baseSubs = getAdminClassSubjectsHelper(selectedClass.id)
        if (selectedStream != null && selectedStream!!.id != "all") {
            baseSubs.filter { it.stream == "common" || it.stream.equals(selectedStream!!.id, ignoreCase = true) }
        } else {
            baseSubs
        }
    }

    var selectedSubject by remember(selectedClass, selectedStream) {
        mutableStateOf(classSubjects.first())
    }

    LaunchedEffect(selectedSubject) {
        viewModel.selectSubject(selectedSubject.id)
    }

    val allChapters by viewModel.chapters.collectAsState()
    val allTopics by viewModel.topics.collectAsState()

    val effectiveChapters = if (dbChapters.isNotEmpty()) dbChapters else allChapters
    val effectiveTopics = if (dbTopics.isNotEmpty()) dbTopics else allTopics

    val filteredChapters = remember(selectedSubject, selectedClass, effectiveChapters, firestoreQuestionsList) {
        val localChs = effectiveChapters.filter { it.subjectId == selectedSubject.id || it.subjectId == selectedSubject.code }
        if (localChs.isNotEmpty()) {
            localChs
        } else {
            val fsChIds = firestoreQuestionsList
                .filter { (it.classId == selectedClass.id || it.classId.isEmpty()) && (it.subjectId == selectedSubject.id || it.subjectId == selectedSubject.code) && it.chapterId.isNotBlank() }
                .map { it.chapterId }
                .distinct()
            if (fsChIds.isNotEmpty()) {
                fsChIds.mapIndexed { idx, chId ->
                    ChapterEntity(
                        id = chId,
                        subjectId = selectedSubject.id,
                        chapterNumber = idx + 1,
                        title = chId.replace("_", " ").replace("-", " ").uppercase(),
                        description = "Firestore Chapter",
                        totalTopics = 1,
                        completedTopics = 0,
                        bossUnlocked = true,
                        bossCompleted = false,
                        masteryPercentage = 0
                    )
                }
            } else {
                // Intelligent fallback chapters so data is ALWAYS available for any class & subject
                listOf(
                    ChapterEntity("${selectedSubject.id}_ch1", selectedSubject.id, 1, "অধ্যায় ১: মৌলিক ধারণা ও পাঠ ১", "Foundational Concepts", 2, 0, true, false, 0),
                    ChapterEntity("${selectedSubject.id}_ch2", selectedSubject.id, 2, "অধ্যায় ২: পরিচ্ছেদ ও নমুনা প্রশ্নমালা", "Core Lesson & Practice", 2, 0, true, false, 0),
                    ChapterEntity("${selectedSubject.id}_ch3", selectedSubject.id, 3, "অধ্যায় ৩: বোর্ড স্পেশাল ও রিভিশন", "Board Special & Revision", 2, 0, true, false, 0)
                )
            }
        }
    }

    var selectedChapter by remember(selectedSubject, selectedClass, filteredChapters) {
        mutableStateOf(filteredChapters.firstOrNull())
    }

    val filteredTopics = remember(selectedChapter, selectedSubject, allTopics, firestoreQuestionsList) {
        val chId = selectedChapter?.id ?: ""
        if (chId.isNotBlank()) {
            val localTps = allTopics.filter { it.chapterId == chId }
            if (localTps.isNotEmpty()) {
                localTps
            } else {
                val fsTpIds = firestoreQuestionsList
                    .filter { it.chapterId == chId && it.topicId.isNotBlank() }
                    .map { it.topicId }
                    .distinct()
                if (fsTpIds.isNotEmpty()) {
                    fsTpIds.mapIndexed { idx, tpId ->
                        TopicEntity(
                            id = tpId,
                            chapterId = chId,
                            subjectId = selectedSubject.id,
                            orderIndex = idx + 1,
                            title = tpId.replace("_", " ").replace("-", " ").uppercase(),
                            summary = "Firestore Topic",
                            keyPointsJson = "[]",
                            isUnlocked = true,
                            masteryPercentage = 0
                        )
                    }
                } else {
                    listOf(
                        TopicEntity("${chId}_top1", chId, selectedSubject.id, 1, "টপিক ১: তত্ত্ব ও মূল আলোচনা", "Core Theory", "[]", true, 0),
                        TopicEntity("${chId}_top2", chId, selectedSubject.id, 2, "টপিক ২: গুরুত্বপূর্ণ বহুনির্বাচনী প্রশ্নাবলি (MCQ)", "Sample MCQs", "[]", true, 0)
                    )
                }
            }
        } else {
            emptyList()
        }
    }

    var selectedTopic by remember(selectedChapter, filteredTopics) {
        mutableStateOf(filteredTopics.firstOrNull())
    }

    var isCustomChapter by remember { mutableStateOf(false) }
    var customChapterInput by remember { mutableStateOf("") }
    var isCustomTopic by remember { mutableStateOf(false) }
    var customTopicInput by remember { mutableStateOf("") }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }
    var topicDropdownExpanded by remember { mutableStateOf(false) }

    var questionText by remember { mutableStateOf("") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var opt3 by remember { mutableStateOf("") }
    var opt4 by remember { mutableStateOf("") }
    var selectedCorrectIndex by remember { mutableIntStateOf(0) }
    var difficulty by remember { mutableStateOf("EASY") }
    var explanationText by remember { mutableStateOf("") }
    var podcastUrlText by remember { mutableStateOf("") }
    var podcastTitleText by remember { mutableStateOf("") }

    var customQuestionId by remember { mutableStateOf("") }
    val defaultGeneratedId = remember(selectedSubject, selectedChapter, selectedTopic) {
        val subCode = selectedSubject.code.ifBlank { selectedSubject.id }.take(6).lowercase().replace(" ", "_")
        val chNum = selectedChapter?.chapterNumber ?: 1
        val tpNum = selectedTopic?.orderIndex ?: 1
        val randomSuffix = (100..999).random()
        "q_${subCode}_c${chNum}t${tpNum}_$randomSuffix"
    }

    var pyqYear by remember { mutableStateOf("") }
    var pyqDropdownExpanded by remember { mutableStateOf(false) }

    val pyqYearOptions = remember {
        listOf(
            "" to ("নিয়মিত প্রশ্ন (সাধারণ প্রশ্ন)"),
            "2026" to ("২০২৬ সালের মডেল প্রশ্ন"),
            "2025" to ("২০২৫ সালের মাধ্যমিক/বোর্ড প্রশ্ন"),
            "2024" to ("২০২৪ সালের মাধ্যমিক/বোর্ড প্রশ্ন"),
            "2023" to ("২০২৩ সালের মাধ্যমিক/বোর্ড প্রশ্ন"),
            "2022" to ("২০২২ সাল"),
            "2021" to ("২০২১ সাল"),
            "2020" to ("২০২০ সাল"),
            "2019" to ("২০১৯ সাল"),
            "2018" to ("২০১৮ সাল"),
            "2017" to ("২০১৭ সাল"),
            "2016" to ("২০১৬ সাল"),
            "2015" to ("২০১৫ সাল")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = CorrectGreen)
                    Text(
                        text = "প্রথমে পর্ষদ/বোর্ড ও শ্রেণি নির্বাচন করুন, তারপর বিষয়, অধ্যায় ও অপশন বেছে নিয়ে প্রশ্ন যোগ করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 1. Board Selector
        item {
            ExposedDropdownMenuBox(
                expanded = boardDropdownExpanded,
                onExpandedChange = { boardDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedBoard.shortName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("১. পর্ষদ / বোর্ড (Education Board)") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ExamGold) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boardDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = boardDropdownExpanded,
                    onDismissRequest = { boardDropdownExpanded = false }
                ) {
                    allBoards.forEach { board ->
                        DropdownMenuItem(
                            text = { Text(board.shortName, fontWeight = FontWeight.Bold) },
                            onClick = {
                                selectedBoard = board
                                val clsForB = allClasses.filter { it.board.equals(board.id, ignoreCase = true) }
                                selectedClass = clsForB.firstOrNull() ?: allClasses.first()
                                val subs = getAdminClassSubjects(selectedClass.id)
                                selectedSubject = subs.first()
                                boardDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 2. Class Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "২. শ্রেণি নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(classesForBoard) { cls ->
                        val isSelected = selectedClass.id == cls.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedClass = cls
                                val subs = getAdminClassSubjects(cls.id)
                                selectedSubject = subs.first()
                            },
                            label = {
                                Text(
                                    text = cls.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExamGold,
                                selectedLabelColor = Color(0xFF1E1B2E)
                            )
                        )
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = classDropdownExpanded,
                    onExpandedChange = { classDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${selectedClass.name} (${selectedClass.board})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("শ্রেণি (Class)") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = ExamGold) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = classDropdownExpanded,
                        onDismissRequest = { classDropdownExpanded = false }
                    ) {
                        classesForBoard.forEach { cls ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(cls.name, fontWeight = FontWeight.Bold)
                                        Text(cls.board, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedClass = cls
                                    val subs = getAdminClassSubjects(cls.id)
                                    selectedSubject = subs.first()
                                    classDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. SubClass / Stream Selector
        if (hasStreams) {
            item {
                ExposedDropdownMenuBox(
                    expanded = streamDropdownExpanded,
                    onExpandedChange = { streamDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedStream?.name ?: "সকল বিভাগ (All Streams)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("৩. বিভাগ / শাখা (Stream / SubClass)") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = ExamGold) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = streamDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = streamDropdownExpanded,
                        onDismissRequest = { streamDropdownExpanded = false }
                    ) {
                        allStreams.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.name, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedStream = st
                                    val subs = getAdminClassSubjects(selectedClass.id)
                                    val filteredSubs = if (st.id != "all") subs.filter { it.stream == "common" || it.stream.equals(st.id, ignoreCase = true) } else subs
                                    selectedSubject = filteredSubs.firstOrNull() ?: subs.first()
                                    streamDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 4. Subject Selector
        item {
            ExposedDropdownMenuBox(
                expanded = subjectDropdownExpanded,
                onExpandedChange = { subjectDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSubject.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("৪. বিষয় নির্বাচন করুন (Subject)") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = ExamGold) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = subjectDropdownExpanded,
                    onDismissRequest = { subjectDropdownExpanded = false }
                ) {
                    classSubjects.forEach { subject ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(subject.name, fontWeight = FontWeight.Bold)
                                    if (!isBengali) {
                                        Text(subject.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            onClick = {
                                selectedSubject = subject
                                subjectDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 5. Chapter Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "৫. অধ্যায় নির্বাচন বা তৈরি করুন (Chapter):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { isCustomChapter = !isCustomChapter },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isCustomChapter) Icons.Default.List else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ExamGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCustomChapter) "তালিকা থেকে বাছুন" else "নতুন লিখুন",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ExamGold
                        )
                    }
                }

                if (isCustomChapter) {
                    OutlinedTextField(
                        value = customChapterInput,
                        onValueChange = { customChapterInput = it },
                        label = { Text("নতুন অধ্যায়ের নাম লিখুন") },
                        placeholder = { Text("যেমন: আলো / রাসায়নিক বিক্রিয়া") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ExamGold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = chapterDropdownExpanded,
                        onExpandedChange = { chapterDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedChapter?.let { "অধ্যায় ${it.chapterNumber}: ${it.title}" } ?: "কোনো অধ্যায় পাওয়া যায়নি",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("অধ্যায় তালিকা") },
                            leadingIcon = { Icon(Icons.Default.Bookmarks, contentDescription = null, tint = ExamGold) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = chapterDropdownExpanded,
                            onDismissRequest = { chapterDropdownExpanded = false }
                        ) {
                            filteredChapters.forEach { chapter ->
                                DropdownMenuItem(
                                    text = { Text("অধ্যায় ${chapter.chapterNumber}: ${chapter.title}") },
                                    onClick = {
                                        selectedChapter = chapter
                                        val tps = allTopics.filter { it.chapterId == chapter.id }
                                        selectedTopic = tps.firstOrNull() ?: filteredTopics.firstOrNull()
                                        chapterDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Topic Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "৬. টপিক নির্বাচন বা তৈরি করুন (Topic):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { isCustomTopic = !isCustomTopic },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isCustomTopic) Icons.Default.List else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ExamGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCustomTopic) "তালিকা থেকে বাছুন" else "নতুন লিখুন",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ExamGold
                        )
                    }
                }

                if (isCustomTopic) {
                    OutlinedTextField(
                        value = customTopicInput,
                        onValueChange = { customTopicInput = it },
                        label = { Text("নতুন টপিকের নাম লিখুন") },
                        placeholder = { Text("যেমন: আলোর প্রতিফলন ও প্রতিসরণ") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ExamGold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = topicDropdownExpanded,
                        onExpandedChange = { topicDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedTopic?.title ?: "কোনো টপিক নেই",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("টপিক তালিকা") },
                            leadingIcon = { Icon(Icons.Default.Topic, contentDescription = null, tint = ExamGold) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = topicDropdownExpanded,
                            onDismissRequest = { topicDropdownExpanded = false }
                        ) {
                            filteredTopics.forEach { topic ->
                                DropdownMenuItem(
                                    text = { Text(topic.title) },
                                    onClick = {
                                        selectedTopic = topic
                                        topicDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. PYQ Year Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "৭. বোর্ড পরীক্ষার সাল নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                ExposedDropdownMenuBox(
                    expanded = pyqDropdownExpanded,
                    onExpandedChange = { pyqDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentLabel = pyqYearOptions.find { it.first == pyqYear }?.second ?: ("নিয়মিত প্রশ্ন")
                    OutlinedTextField(
                        value = currentLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("বোর্ড বর্ষ") },
                        leadingIcon = { Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = ExamGold) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pyqDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = pyqDropdownExpanded,
                        onDismissRequest = { pyqDropdownExpanded = false }
                    ) {
                        pyqYearOptions.forEach { (yearCode, yearLabel) ->
                            DropdownMenuItem(
                                text = { Text(yearLabel) },
                                onClick = {
                                    pyqYear = yearCode
                                    pyqDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(listOf("" to ("নিয়মিত"), "2025" to "২০২৫", "2024" to "২০২৪", "2023" to "২০২৩", "2022" to "২০২২", "2020" to "২০২০", "2019" to "২০১৯")) { (code, chipLabel) ->
                        val isSelected = pyqYear == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { pyqYear = code },
                            label = { Text(chipLabel, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExamGold,
                                selectedLabelColor = Color(0xFF1E1B2E)
                            )
                        )
                    }
                }
            }
        }

        // 8. Custom Question ID
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "৮. প্রশ্নের ইউনিক আইডি:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { customQuestionId = defaultGeneratedId },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp), tint = ExamGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "অটো-আইডি বসান",
                            style = MaterialTheme.typography.labelSmall,
                            color = ExamGold
                        )
                    }
                }
                OutlinedTextField(
                    value = customQuestionId,
                    onValueChange = { customQuestionId = it },
                    placeholder = { Text(defaultGeneratedId, color = Color.Gray) },
                    label = { Text("কাস্টম আইডি (ঐচ্ছিক - ফাঁকা রাখলে স্বয়ংক্রিয় তৈরি হবে)") },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = ExamGold) },
                    trailingIcon = {
                        if (customQuestionId.isNotBlank()) {
                            IconButton(onClick = { customQuestionId = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_question_id"),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = if (isBengali)
                        "💡 আপনি চাইলে নিজের পছন্দমতো আইডি লিখতে পারেন অথবা ঘরটি ফাঁকা রাখলে স্বয়ংক্রিয়ভাবে ক্লিন আইডি বসে যাবে।"
                    else
                        "💡 You can enter your own ID (e.g. q_ben_ch1_01) or leave empty for clean auto-structured ID.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 9. Question Text
        item {
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("৯. প্রশ্নের বিবরণ লিখুন:") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 10. Options Inputs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "১০. ৪টি বিকল্প অপশন লিখুন:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                OutlinedTextField(
                    value = opt1,
                    onValueChange = { opt1 = it },
                    label = { Text("অপশন ক (১ম বিকল্প)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = opt2,
                    onValueChange = { opt2 = it },
                    label = { Text("অপশন খ (২য় বিকল্প)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = opt3,
                    onValueChange = { opt3 = it },
                    label = { Text("অপশন গ (৩য় বিকল্প)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = opt4,
                    onValueChange = { opt4 = it },
                    label = { Text("অপশন ঘ (৪র্থ বিকল্প)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // 11. Correct Option Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "১১. সঠিক উত্তর কোনটি? (সরাসরি ক্লিক করুন):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val bengaliTags = listOf("ক", "খ", "গ", "ঘ")
                    bengaliTags.forEachIndexed { index, tag ->
                        val isSelected = selectedCorrectIndex == index
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CorrectGreen else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.5.dp, if (isSelected) CorrectGreen else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCorrectIndex = index }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "অপশন $tag",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isSelected) ("✓ সঠিক উত্তর") else ("বিকল্প ${index + 1}"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                val optionTexts = listOf(opt1, opt2, opt3, opt4)
                val chosenText = optionTexts.getOrNull(selectedCorrectIndex)?.trim().orEmpty()
                if (chosenText.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CorrectGreen.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "সঠিক উত্তর: $chosenText",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = CorrectGreen,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 12. Difficulty
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "১২. প্রশ্নের কঠিনতার মাত্রা:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "EASY" to ("সহজ"),
                        "MEDIUM" to ("মাঝারি"),
                        "HARD" to ("কঠিন")
                    ).forEach { (diffKey, diffLabel) ->
                        val isSelected = difficulty == diffKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { difficulty = diffKey },
                            label = { Text(diffLabel) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 13. Explanation
        item {
            OutlinedTextField(
                value = explanationText,
                onValueChange = { explanationText = it },
                label = { Text("১৩. প্রশ্নের ব্যাখ্যা বা সমাধান (ঐচ্ছিক)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // 14. Podcast Audio URL
        item {
            OutlinedTextField(
                value = podcastUrlText,
                onValueChange = { podcastUrlText = it },
                label = { Text("১৪. পডকাস্ট অডিও লিংক / URL (ঐচ্ছিক)") },
                placeholder = { Text("যেমন: https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Icon(Icons.Default.Audiotrack, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )
        }

        // 15. Podcast Title
        item {
            OutlinedTextField(
                value = podcastTitleText,
                onValueChange = { podcastTitleText = it },
                label = { Text("১৫. পডকাস্ট শিরোনাম (ঐচ্ছিক)") },
                placeholder = { Text("যেমন: চলতড়িৎ ও ওহমের সূত্রের গাণিতিক শর্টকাট") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )
        }

        // 16. Submit Button
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = {
                    val opts = listOf(opt1, opt2, opt3, opt4).filter { it.isNotBlank() }
                    val resolvedCorrectAnswer = opts.getOrNull(selectedCorrectIndex) ?: opt1
                    val resolvedChapterId = if (isCustomChapter && customChapterInput.isNotBlank()) {
                        "ch_" + customChapterInput.trim().lowercase().replace(Regex("[^a-zA-Z0-9_]"), "_").trim('_').ifBlank { "custom" }
                    } else {
                        selectedChapter?.id ?: "${selectedSubject.id}_ch1"
                    }
                    val resolvedTopicId = if (isCustomTopic && customTopicInput.isNotBlank()) {
                        "top_" + customTopicInput.trim().lowercase().replace(Regex("[^a-zA-Z0-9_]"), "_").trim('_').ifBlank { "custom" }
                    } else {
                        selectedTopic?.id ?: "${resolvedChapterId}_top1"
                    }

                    viewModel.addQuestionToFirestore(
                        classId = selectedClass.id,
                        subjectId = selectedSubject.id,
                        chapterId = resolvedChapterId,
                        topicId = resolvedTopicId,
                        questionText = questionText,
                        optionsList = opts,
                        correctIndex = selectedCorrectIndex,
                        correctAnswerText = resolvedCorrectAnswer,
                        explanationText = explanationText.ifBlank { "সঠিক উত্তর হলো $resolvedCorrectAnswer।" },
                        difficulty = difficulty,
                        marks = 1,
                        pyqYear = pyqYear,
                        customQuestionId = customQuestionId.ifBlank { null },
                        podcastUrl = podcastUrlText.trim(),
                        podcastTitle = podcastTitleText.trim()
                    ) { success, msg ->
                        onAction(success, msg)
                        if (success) {
                            customQuestionId = ""
                            questionText = ""
                            opt1 = ""
                            opt2 = ""
                            opt3 = ""
                            opt4 = ""
                            explanationText = ""
                            podcastUrlText = ""
                            podcastTitleText = ""
                            if (isCustomChapter) customChapterInput = ""
                            if (isCustomTopic) customTopicInput = ""
                        }
                    }
                },
                enabled = !isLoading &&
                    questionText.isNotBlank() &&
                    opt1.isNotBlank() &&
                    opt2.isNotBlank() &&
                    (if (isCustomChapter) customChapterInput.isNotBlank() else selectedChapter != null) &&
                    (if (isCustomTopic) customTopicInput.isNotBlank() else selectedTopic != null),
                colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
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
                    Text(
                        "☁️ ${selectedClass.classNumber}ম শ্রেণির প্রশ্ন ক্লাউডে এন্ট্রি করুন",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
