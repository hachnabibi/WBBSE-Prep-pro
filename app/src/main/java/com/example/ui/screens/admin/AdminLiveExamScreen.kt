package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChapterEntity
import com.example.data.local.SubjectEntity
import com.example.data.model.AcademicClassProvider
import com.example.data.model.AssessmentMode
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ExamGold
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.ui.viewmodel.StructuredQuestionInput

/**
 * ট্যাব ৪: লাইভ এক্সাম ও স্ট্রাকচার্ড টেস্ট
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLiveExamScreen(
    viewModel: DeshExamViewModel,
    isLoading: Boolean,
    isBengali: Boolean = true,
    onAction: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
    LaunchedEffect(Unit) {
        try {
            dbSubjects = viewModel.repository.dao.getAllSubjectsOnce()
        } catch (e: Exception) {
            android.util.Log.e("AdminLiveExamScreen", "Error loading subjects: ${e.message}", e)
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
    var selectedStructuredChapter by remember(selectedSubject, allChapters) {
        val chs = allChapters.filter { it.subjectId == selectedSubject.id }
        mutableStateOf(chs.firstOrNull())
    }

    var structuredTestType by remember { mutableStateOf("CHAPTER_ASSESSMENT") }
    var structuredTestTitle by remember { mutableStateOf("") }
    var structuredTestDuration by remember { mutableStateOf("25") }
    var structuredTestMarks by remember { mutableStateOf("20") }

    var structuredBoardDropdownExpanded by remember { mutableStateOf(false) }
    var structuredClassDropdownExpanded by remember { mutableStateOf(false) }
    var structuredSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var structuredChapterDropdownExpanded by remember { mutableStateOf(false) }

    var structuredQuestionText by remember { mutableStateOf("") }
    var structuredOpt1 by remember { mutableStateOf("") }
    var structuredOpt2 by remember { mutableStateOf("") }
    var structuredOpt3 by remember { mutableStateOf("") }
    var structuredOpt4 by remember { mutableStateOf("") }
    var structuredCorrectIndex by remember { mutableIntStateOf(0) }
    var structuredExplanation by remember { mutableStateOf("") }
    var structuredStagedQuestions by remember { mutableStateOf<List<StructuredQuestionInput>>(emptyList()) }

    var lastCreatedTestId by remember { mutableStateOf<String?>(null) }
    var lastCreatedTestTitle by remember { mutableStateOf<String?>(null) }

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
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "নির্দিষ্ট অধ্যায় বা বিষয় নির্বাচন করে অধ্যায় পরীক্ষা, বিষয়ভিত্তিক বিশেষ কুইজ ও সম্পূর্ণ সিলেবাস মক টেস্ট তৈরি করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "টেস্টের ধরন নির্বাচন:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                val structuredTypes = listOf(
                    "CHAPTER_ASSESSMENT" to ("অধ্যায় পরীক্ষা"),
                    "SUBJECT_MASTERY_QUIZ" to ("বিষয়ভিত্তিক বিশেষ কুইজ"),
                    "FULL_SYLLABUS_MOCK" to ("সম্পূর্ণ সিলেবাস মক")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    structuredTypes.forEach { (typeKey, label) ->
                        val isSelected = structuredTestType == typeKey
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { structuredTestType = typeKey }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                // 1. Board Selector
                ExposedDropdownMenuBox(
                    expanded = structuredBoardDropdownExpanded,
                    onExpandedChange = { structuredBoardDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedBoard.shortName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("১. বোর্ড / পর্ষদ নির্বাচন", maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = structuredBoardDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = structuredBoardDropdownExpanded,
                        onDismissRequest = { structuredBoardDropdownExpanded = false }
                    ) {
                        allBoards.forEach { board ->
                            DropdownMenuItem(
                                text = { Text(board.shortName, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedBoard = board
                                    val boardClasses = allClasses.filter { it.board.equals(board.id, ignoreCase = true) || it.board.equals(board.shortName, ignoreCase = true) }.ifEmpty { allClasses }
                                    selectedClass = boardClasses.first()
                                    val subs = getAdminClassSubjects(selectedClass.id)
                                    selectedSubject = subs.first()
                                    val chs = allChapters.filter { it.subjectId == selectedSubject.id }
                                    selectedStructuredChapter = chs.firstOrNull()
                                    structuredBoardDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 2. Class Selector
                ExposedDropdownMenuBox(
                    expanded = structuredClassDropdownExpanded,
                    onExpandedChange = { structuredClassDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedClass.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("২. শ্রেণি") },
                        leadingIcon = { Icon(Icons.Default.Class, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = structuredClassDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = structuredClassDropdownExpanded,
                        onDismissRequest = { structuredClassDropdownExpanded = false }
                    ) {
                        currentBoardClasses.forEach { cls ->
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
                                    val chs = allChapters.filter { it.subjectId == selectedSubject.id }
                                    selectedStructuredChapter = chs.firstOrNull()
                                    structuredClassDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 3. Subject Selector
                ExposedDropdownMenuBox(
                    expanded = structuredSubjectDropdownExpanded,
                    onExpandedChange = { structuredSubjectDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSubject.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("৩. বিষয়") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = structuredSubjectDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = structuredSubjectDropdownExpanded,
                        onDismissRequest = { structuredSubjectDropdownExpanded = false }
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
                                    val chs = allChapters.filter { it.subjectId == subject.id }
                                    selectedStructuredChapter = chs.firstOrNull()
                                    structuredSubjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 4. Chapter Selector
                if (structuredTestType != "FULL_SYLLABUS_MOCK") {
                    ExposedDropdownMenuBox(
                        expanded = structuredChapterDropdownExpanded,
                        onExpandedChange = { structuredChapterDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedStructuredChapter?.title ?: "অধ্যায় নির্বাচন করুন",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("৪. নির্দিষ্ট অধ্যায়") },
                            leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = structuredChapterDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = structuredChapterDropdownExpanded,
                            onDismissRequest = { structuredChapterDropdownExpanded = false }
                        ) {
                            val subChapters = allChapters.filter { it.subjectId == selectedSubject.id }
                            if (subChapters.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("কোনো অধ্যায় পাওয়া যায়নি") },
                                    onClick = { structuredChapterDropdownExpanded = false }
                                )
                            } else {
                                subChapters.forEach { ch ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${ch.chapterNumber}. ${ch.title}",
                                                fontWeight = if (selectedStructuredChapter?.id == ch.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            selectedStructuredChapter = ch
                                            structuredChapterDropdownExpanded = false
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = structuredTestTitle,
                    onValueChange = { structuredTestTitle = it },
                    label = { Text("টেস্টের শিরোনাম") },
                    leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = structuredTestDuration,
                        onValueChange = { structuredTestDuration = it },
                        label = { Text("সময় (মিনিট)") },
                        leadingIcon = { Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = structuredTestMarks,
                        onValueChange = { structuredTestMarks = it },
                        label = { Text("মোট নম্বর") },
                        leadingIcon = { Icon(Icons.Default.Grade, contentDescription = null, tint = ExamGold, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
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
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "৫. প্রশ্নের বিবরণ ও অপশন যোগ করুন:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = structuredQuestionText,
                        onValueChange = { structuredQuestionText = it },
                        label = { Text("প্রশ্নের বিবরণ (Question Text)") },
                        placeholder = { Text("যেমন: সালোকসংশ্লেষ প্রক্রিয়ায় নির্গত উপজাত গ্যাস কোনটি?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = structuredOpt1,
                        onValueChange = { structuredOpt1 = it },
                        label = { Text("অপশন ক") },
                        leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = structuredOpt2,
                        onValueChange = { structuredOpt2 = it },
                        label = { Text("অপশন খ") },
                        leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = structuredOpt3,
                        onValueChange = { structuredOpt3 = it },
                        label = { Text("অপশন গ") },
                        leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = structuredOpt4,
                        onValueChange = { structuredOpt4 = it },
                        label = { Text("অপশন ঘ") },
                        leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "সঠিক উত্তর কোনটি? (নিচের অপশন বেছে নিন):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val optionLabels = listOf("ক (১ম)", "খ (২য়)", "গ (৩য়)", "ঘ (৪র্থ)")
                        optionLabels.forEachIndexed { idx, lbl ->
                            val isSelected = structuredCorrectIndex == idx
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CorrectGreen else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) CorrectGreen else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { structuredCorrectIndex = idx }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        Text(
                                            text = lbl,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = structuredExplanation,
                        onValueChange = { structuredExplanation = it },
                        label = { Text("ব্যাখ্যা ও সমাধান (ঐচ্ছিক)") },
                        placeholder = { Text("প্রশ্নের সঠিক উত্তরের ব্যাখ্যা ও সূত্র লিখুন...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            val opts = listOf(structuredOpt1, structuredOpt2, structuredOpt3, structuredOpt4).filter { it.isNotBlank() }
                            if (structuredQuestionText.isBlank()) {
                                Toast.makeText(context, "অনুগ্রহ করে প্রশ্নের বিবরণ লিখুন!", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            if (opts.size < 2) {
                                Toast.makeText(context, "কমপক্ষে ২টি অপশন লিখুন!", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            val qInput = StructuredQuestionInput(
                                questionText = structuredQuestionText.trim(),
                                options = opts,
                                correctIndex = structuredCorrectIndex.coerceIn(0, opts.size - 1),
                                explanation = structuredExplanation.trim()
                            )
                            structuredStagedQuestions = structuredStagedQuestions + qInput
                            structuredQuestionText = ""
                            structuredOpt1 = ""
                            structuredOpt2 = ""
                            structuredOpt3 = ""
                            structuredOpt4 = ""
                            structuredExplanation = ""
                            structuredCorrectIndex = 0
                            Toast.makeText(context, "✓ প্রশ্ন যোগ হয়েছে! (মোট ${structuredStagedQuestions.size}টি প্রশ্ন প্রস্তুত)", Toast.LENGTH_SHORT).show()
                        },
                        enabled = structuredQuestionText.isNotBlank() && structuredOpt1.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "➕ এই টেস্টে প্রশ্নটি যোগ করুন (${structuredStagedQuestions.size + 1} নং প্রশ্ন)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        if (structuredStagedQuestions.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
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
                            Text(
                                text = "📋 প্রস্তুত করা প্রশ্নসমূহ (${structuredStagedQuestions.size}টি):",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = { structuredStagedQuestions = emptyList() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("সব মুছুন", color = WrongRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        structuredStagedQuestions.forEachIndexed { qIdx, sq ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${qIdx + 1}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sq.questionText, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), maxLines = 2)
                                        val correctOptText = sq.options.getOrNull(sq.correctIndex) ?: ""
                                        Text("সঠিক: $correctOptText", style = MaterialTheme.typography.labelSmall, color = CorrectGreen)
                                    }
                                    IconButton(
                                        onClick = {
                                            structuredStagedQuestions = structuredStagedQuestions.filterIndexed { index, _ -> index != qIdx }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WrongRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            val totalQuestionCount = remember(structuredStagedQuestions, structuredQuestionText) {
                structuredStagedQuestions.size + (if (structuredQuestionText.isNotBlank() && structuredOpt1.isNotBlank()) 1 else 0)
            }

            Button(
                onClick = {
                    val allQList = mutableListOf<StructuredQuestionInput>()
                    allQList.addAll(structuredStagedQuestions)

                    if (structuredQuestionText.isNotBlank() && structuredOpt1.isNotBlank()) {
                        val opts = listOf(structuredOpt1, structuredOpt2, structuredOpt3, structuredOpt4).filter { it.isNotBlank() }
                        allQList.add(
                            StructuredQuestionInput(
                                questionText = structuredQuestionText.trim(),
                                options = opts,
                                correctIndex = structuredCorrectIndex.coerceIn(0, opts.size - 1),
                                explanation = structuredExplanation.trim()
                            )
                        )
                    }

                    if (allQList.isEmpty()) {
                        Toast.makeText(context, "অনুগ্রহ করে কমপক্ষে ১টি প্রশ্ন তৈরি করুন!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.createStructuredAssessment(
                        testType = structuredTestType,
                        classId = selectedClass.id,
                        subjectId = selectedSubject.id,
                        chapterId = selectedStructuredChapter?.id ?: "${selectedSubject.id}_ch1",
                        title = structuredTestTitle.trim(),
                        description = "Structured test linked to ${selectedStructuredChapter?.title ?: selectedSubject.name}",
                        durationMinutes = structuredTestDuration.toIntOrNull() ?: 25,
                        totalMarks = structuredTestMarks.toIntOrNull() ?: allQList.size,
                        questions = allQList
                    ) { success, msg, createdId ->
                        onAction(success, msg)
                        if (success) {
                            lastCreatedTestId = createdId
                            lastCreatedTestTitle = structuredTestTitle.trim()
                            structuredStagedQuestions = emptyList()
                            structuredQuestionText = ""
                            structuredOpt1 = ""
                            structuredOpt2 = ""
                            structuredOpt3 = ""
                            structuredOpt4 = ""
                            structuredExplanation = ""
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isLoading && structuredTestTitle.isNotBlank() && (structuredStagedQuestions.isNotEmpty() || (structuredQuestionText.isNotBlank() && structuredOpt1.isNotBlank())),
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
                    Text(
                        "🚀 মোট ${if (totalQuestionCount > 0) totalQuestionCount else 1}টি প্রশ্ন সহ টেস্ট প্রকাশ করুন",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (lastCreatedTestId != null && lastCreatedTestTitle != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CorrectGreen)
                            Text(
                                text = "টেস্ট তৈরি সম্পন্ন: \"${lastCreatedTestTitle}\"",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "আপনার নতুন টেস্টটি ফায়ারবেস ক্লাউডে সফলভাবে সেভ হয়েছে। এখনই টেস্ট করে দেখতে নিচের বাটনে চাপ দিন:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Button(
                            onClick = {
                                viewModel.startAssessment(
                                    mode = AssessmentMode.MOCK_TEST,
                                    targetId = lastCreatedTestId,
                                    customTitle = lastCreatedTestTitle
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("▶️ প্রকাশিত টেস্টটিতে এখনই অংশ নিন / টেস্ট করুন", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
