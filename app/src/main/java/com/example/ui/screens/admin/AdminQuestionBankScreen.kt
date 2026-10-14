package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.firestore.FirestoreQuestion
import com.example.data.local.ChapterEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TopicEntity
import com.example.data.model.AcademicClassProvider
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ExamGold
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.util.formatCompactNumber

/**
 * ট্যাব ২: প্রশ্ন ব্যাংক ম্যানেজমেন্ট (CMS)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestionBankScreen(
    viewModel: DeshExamViewModel,
    isLoading: Boolean,
    isBengali: Boolean = true,
    onAction: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val firestoreQuestionsList by viewModel.firestoreQuestions.collectAsState()
    val allClasses = remember { AcademicClassProvider.ALL_CLASSES }

    var selectedClass by remember {
        mutableStateOf(allClasses.firstOrNull { it.id == "class_10_wbbse" } ?: allClasses.first())
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
            android.util.Log.e("AdminQuestionBankScreen", "Error loading DB entities: ${e.message}", e)
        }
    }

    val getAdminClassSubjectsHelper: (String) -> List<SubjectEntity> = { cid ->
        val filtered = dbSubjects.filter { it.classId == cid }
        if (filtered.isNotEmpty()) filtered else getAdminClassSubjects(cid)
    }

    val classSubjects = remember(selectedClass, dbSubjects) {
        getAdminClassSubjectsHelper(selectedClass.id)
    }

    val allSubjectOption = remember(selectedClass.id) {
        SubjectEntity(
            id = "ALL",
            code = "ALL",
            name = "সকল বিষয়",
            totalChapters = 0,
            colorHex = 0xFFEAB308,
            classId = selectedClass.id
        )
    }

    val subjectDropdownList = remember(classSubjects) {
        listOf(allSubjectOption) + classSubjects
    }

    var selectedSubject by remember(selectedClass) {
        mutableStateOf(allSubjectOption)
    }

    var cmsClassDropdownExpanded by remember { mutableStateOf(false) }
    var cmsSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var cmsQuestionSearchQuery by remember { mutableStateOf("") }

    var editingQuestion by remember { mutableStateOf<FirestoreQuestion?>(null) }
    var deletingQuestion by remember { mutableStateOf<FirestoreQuestion?>(null) }

    val allChapters by viewModel.chapters.collectAsState()
    val allTopics by viewModel.topics.collectAsState()
    val effectiveChapters = if (dbChapters.isNotEmpty()) dbChapters else allChapters
    val effectiveTopics = if (dbTopics.isNotEmpty()) dbTopics else allTopics

    LaunchedEffect(selectedClass.id, selectedSubject.id) {
        val filterSubject = if (selectedSubject.id == "ALL") null else selectedSubject.id
        viewModel.fetchFirestoreQuestions(selectedClass.id, filterSubject)
    }

    val filteredQuestions = remember(firestoreQuestionsList, cmsQuestionSearchQuery) {
        if (cmsQuestionSearchQuery.isBlank()) {
            firestoreQuestionsList
        } else {
            val qLower = cmsQuestionSearchQuery.lowercase()
            firestoreQuestionsList.filter { q ->
                q.question.lowercase().contains(qLower) ||
                        q.id.lowercase().contains(qLower) ||
                        q.subjectId.lowercase().contains(qLower) ||
                        q.tags.any { it.lowercase().contains(qLower) }
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
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = ExamGold)
                            Text(
                                text = "📋 ফায়ারবেস প্রশ্নমালা ব্যবস্থাপনা",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(
                            onClick = {
                                val filterSub = if (selectedSubject.id == "ALL") null else selectedSubject.id
                                viewModel.fetchFirestoreQuestions(selectedClass.id, filterSub)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ExamGold)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = cmsClassDropdownExpanded,
                            onExpandedChange = { cmsClassDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedClass.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("শ্রেণি") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cmsClassDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = cmsClassDropdownExpanded,
                                onDismissRequest = { cmsClassDropdownExpanded = false }
                            ) {
                                allClasses.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text(cls.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedClass = cls
                                            val subs = getAdminClassSubjects(cls.id)
                                            selectedSubject = subs.firstOrNull() ?: allSubjectOption
                                            cmsClassDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = cmsSubjectDropdownExpanded,
                            onExpandedChange = { cmsSubjectDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedSubject.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("বিষয়") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cmsSubjectDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = cmsSubjectDropdownExpanded,
                                onDismissRequest = { cmsSubjectDropdownExpanded = false }
                            ) {
                                subjectDropdownList.forEach { subject ->
                                    DropdownMenuItem(
                                        text = { Text(subject.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedSubject = subject
                                            cmsSubjectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = cmsQuestionSearchQuery,
                        onValueChange = { cmsQuestionSearchQuery = it },
                        label = { Text("🔍 প্রশ্ন অথবা আইডি দিয়ে খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ExamGold) },
                        trailingIcon = {
                            if (cmsQuestionSearchQuery.isNotBlank()) {
                                IconButton(onClick = { cmsQuestionSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ExamGold.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBengali) "মোট ${formatCompactNumber(firestoreQuestionsList.size)} টি প্রশ্ন ক্লাউডে সংরক্ষিত আছে (ফিল্টার করা: ${formatCompactNumber(filteredQuestions.size)} টি)। যেকোনো প্রশ্ন সরাসরি সম্পাদনা বা মুছে ফেলতে পারেন।"
                            else "Total ${formatCompactNumber(firestoreQuestionsList.size)} questions in Firebase (${formatCompactNumber(filteredQuestions.size)} matching filter). Edit or delete directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        if (isLoading && firestoreQuestionsList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ExamGold)
                }
            }
        } else if (filteredQuestions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(48.dp), tint = ExamGold)
                        Text(
                            text = "এই ফিল্টারে ফায়ারবেস ক্লাউডে কোনো প্রশ্ন পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "নিচের বাটনে চাপ দিলে সমস্ত বিষয়ের প্রশ্ন ক্লাউডে আপলোড হয়ে যাবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = {
                                viewModel.syncAllDataWithFirebase { success, msg ->
                                    onAction(success, msg)
                                    val filterSub = if (selectedSubject.id == "ALL") null else selectedSubject.id
                                    viewModel.fetchFirestoreQuestions(selectedClass.id, filterSub)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF1E1B2E))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🚀 ফায়ারবেসে প্রশ্নমালা সিঙ্ক করুন",
                                color = Color(0xFF1E1B2E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredQuestions) { question ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
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
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ExamGold.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "ID: ${question.id}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = ExamGold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            val diff = question.difficulty.uppercase()
                            val (diffBg, diffColor, diffText) = when (diff) {
                                "EASY" -> Triple(CorrectGreen.copy(alpha = 0.18f), CorrectGreen, "সহজ")
                                "HARD" -> Triple(WrongRed.copy(alpha = 0.18f), WrongRed, "কঠিন")
                                else -> Triple(ExamGold.copy(alpha = 0.18f), ExamGold, "মাঝারি")
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = diffBg
                            ) {
                                Text(
                                    text = "⚡ $diffText",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = diffColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val subName = when (question.subjectId) {
                                "bengali", "sub_bengali" -> "বাংলা"
                                "english", "sub_english" -> "ইংরেজি"
                                "mathematics", "sub_math" -> "গণিত"
                                "physical_science", "sub_psc" -> "ভৌত বিজ্ঞান"
                                "life_science", "sub_lsc" -> "জীবন বিজ্ঞান"
                                "history", "sub_hist" -> "ইতিহাস"
                                "geography", "sub_geog" -> "ভূগোল"
                                else -> question.subjectId.replace("subject_", "").replace("sub_", "").replace("_", " ")
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "📚 $subName",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            val clsName = when (question.classId) {
                                "class_10_wbbse" -> "১০ম শ্রেণি"
                                "class_9_wbbse" -> "৯ম শ্রেণি"
                                "class_8_wbbse" -> "৮ম শ্রেণি"
                                "class_7_wbbse" -> "৭ম শ্রেণি"
                                "class_6_wbbse" -> "৬ষ্ঠ শ্রেণি"
                                "class_5_wbbse" -> "৫ম শ্রেণি"
                                "wb_primary_tet" -> "প্রাইমারি টেট"
                                "wb_upper_primary_tet" -> "আপার টেট"
                                "wb_slst_pt" -> "এসএলএসটি"
                                else -> question.classId.replace("class_", "Class ").replace("_wbbse", "")
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "🎓 $clsName",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            val pyqTag = question.tags.firstOrNull { it.startsWith("PYQ_") || it.contains("202") || it.contains("201") }
                            if (pyqTag != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF6750A4).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF6750A4).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "🏛️ ${pyqTag.replace("PYQ_", "PYQ ")}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = Color(0xFFD0BCFF),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            question.options.forEachIndexed { idx, opt ->
                                val isCorrect = idx == question.correctOptionIndex
                                val bengaliOptLetters = listOf("ক", "খ", "গ", "ঘ")
                                val optPrefix = if (isBengali) bengaliOptLetters.getOrElse(idx) { ('A' + idx).toString() } else ('A' + idx).toString()
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCorrect) CorrectGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = if (isCorrect) BorderStroke(1.dp, CorrectGreen) else null,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isCorrect) CorrectGreen else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "$optPrefix: $opt",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        if (question.explanation.isNotBlank()) {
                            Text(
                                text = "💡 ${question.explanation}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { editingQuestion = question },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "✍️ সম্পাদনা",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = { deletingQuestion = question },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WrongRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🗑️ মুছে ফেলুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Question Dialog
    if (editingQuestion != null) {
        val q = editingQuestion!!
        var editQText by remember(q) { mutableStateOf(q.question) }
        var editOpt0 by remember(q) { mutableStateOf(q.options.getOrNull(0) ?: "") }
        var editOpt1 by remember(q) { mutableStateOf(q.options.getOrNull(1) ?: "") }
        var editOpt2 by remember(q) { mutableStateOf(q.options.getOrNull(2) ?: "") }
        var editOpt3 by remember(q) { mutableStateOf(q.options.getOrNull(3) ?: "") }
        var editCorrectIdx by remember(q) { mutableIntStateOf(q.correctOptionIndex) }
        var editDifficulty by remember(q) { mutableStateOf(if (q.difficulty.isNotBlank()) q.difficulty.uppercase() else "MEDIUM") }

        val existingPyq = q.tags.firstOrNull { it.startsWith("PYQ_") }?.removePrefix("PYQ_")
            ?: q.tags.firstOrNull { it.matches(Regex("\\d{4}")) } ?: ""
        var editPyqYear by remember(q) { mutableStateOf(existingPyq) }
        var editMarks by remember(q) { mutableIntStateOf(if (q.marks > 0) q.marks else 1) }
        var editExplanation by remember(q) { mutableStateOf(q.explanation) }

        var editClassId by remember(q) { mutableStateOf(q.classId.ifBlank { "class_10_wbbse" }) }
        var editSubjectId by remember(q) { mutableStateOf(q.subjectId) }
        var editChapterId by remember(q) { mutableStateOf(q.chapterId) }
        var editTopicId by remember(q) { mutableStateOf(q.topicId) }

        var editClassDropdownExpanded by remember { mutableStateOf(false) }
        var editSubjectDropdownExpanded by remember { mutableStateOf(false) }
        var editChapterDropdownExpanded by remember { mutableStateOf(false) }
        var editTopicDropdownExpanded by remember { mutableStateOf(false) }

        val editSubjects = remember(editClassId, dbSubjects) { getAdminClassSubjectsHelper(editClassId) }
        val editFilteredChapters = remember(editSubjectId, effectiveChapters) {
            effectiveChapters.filter { it.subjectId == editSubjectId }
        }
        val editFilteredTopics = remember(editChapterId, effectiveTopics) {
            effectiveTopics.filter { it.chapterId == editChapterId }
        }

        Dialog(
            onDismissRequest = { editingQuestion = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f)
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ExamGold.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "✍️",
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "প্রশ্ন সম্পাদনা ও আপডেট",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "ID: ${q.id} • MCQ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExamGold
                                    )
                                }
                            }
                            IconButton(onClick = { editingQuestion = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "শ্রেণি, বিষয়, অধ্যায় ও টপিক (এক সারিতে নির্বাচন করুন):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ExamGold)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    val currClass = allClasses.firstOrNull { it.id == editClassId }
                                    val clsLabel = currClass?.name ?: editClassId
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable { editClassDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Class, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "🎓 $clsLabel",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = editClassDropdownExpanded,
                                        onDismissRequest = { editClassDropdownExpanded = false }
                                    ) {
                                        allClasses.forEach { cls ->
                                            DropdownMenuItem(
                                                text = { Text(cls.name, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    editClassId = cls.id
                                                    val subs = getAdminClassSubjects(cls.id)
                                                    editSubjectId = subs.firstOrNull()?.id ?: ""
                                                    val chs = allChapters.filter { it.subjectId == editSubjectId }
                                                    editChapterId = chs.firstOrNull()?.id ?: ""
                                                    val tps = allTopics.filter { it.chapterId == editChapterId }
                                                    editTopicId = tps.firstOrNull()?.id ?: ""
                                                    editClassDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Box {
                                    val currSub = editSubjects.firstOrNull { it.id == editSubjectId }
                                    val subLabel = currSub?.name ?: editSubjectId.replace("subject_", "").replace("sub_", "").replace("_", " ")
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable { editSubjectDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "📚 $subLabel",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = editSubjectDropdownExpanded,
                                        onDismissRequest = { editSubjectDropdownExpanded = false }
                                    ) {
                                        editSubjects.forEach { sub ->
                                            DropdownMenuItem(
                                                text = { Text(sub.name, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    editSubjectId = sub.id
                                                    val chs = allChapters.filter { it.subjectId == sub.id }
                                                    editChapterId = chs.firstOrNull()?.id ?: ""
                                                    val tps = allTopics.filter { it.chapterId == editChapterId }
                                                    editTopicId = tps.firstOrNull()?.id ?: ""
                                                    editSubjectDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Box {
                                    val currCh = editFilteredChapters.firstOrNull { it.id == editChapterId }
                                    val chLabel = currCh?.title ?: if (editChapterId.isNotBlank()) editChapterId else ("অধ্যায় নেই")
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.clickable { editChapterDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "📖 $chLabel",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = editChapterDropdownExpanded,
                                        onDismissRequest = { editChapterDropdownExpanded = false }
                                    ) {
                                        if (editFilteredChapters.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("কোনো অধ্যায় নেই") },
                                                onClick = { editChapterDropdownExpanded = false }
                                            )
                                        } else {
                                            editFilteredChapters.forEach { ch ->
                                                DropdownMenuItem(
                                                    text = { Text(ch.title) },
                                                    onClick = {
                                                        editChapterId = ch.id
                                                        val tps = allTopics.filter { it.chapterId == ch.id }
                                                        editTopicId = tps.firstOrNull()?.id ?: ""
                                                        editChapterDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Box {
                                    val currTp = editFilteredTopics.firstOrNull { it.id == editTopicId }
                                    val tpLabel = currTp?.title ?: if (editTopicId.isNotBlank()) editTopicId else ("টপিক নেই")
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.clickable { editTopicDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Topic, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "🏷️ $tpLabel",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = editTopicDropdownExpanded,
                                        onDismissRequest = { editTopicDropdownExpanded = false }
                                    ) {
                                        if (editFilteredTopics.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("কোনো টপিক নেই") },
                                                onClick = { editTopicDropdownExpanded = false }
                                            )
                                        } else {
                                            editFilteredTopics.forEach { tp ->
                                                DropdownMenuItem(
                                                    text = { Text(tp.title) },
                                                    onClick = {
                                                        editTopicId = tp.id
                                                        editTopicDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editQText,
                            onValueChange = { editQText = it },
                            label = { Text("প্রশ্নের মূল বিবরণ:") },
                            placeholder = { Text("এখানে প্রশ্নটি লিখুন...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "৪টি অপশন ও সঠিক উত্তর নির্বাচন:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CorrectGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "সঠিক উত্তরে ক্লিক করুন ✓",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CorrectGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            val optLabels = listOf("ক (A)", "খ (B)", "গ (C)", "ঘ (D)")
                            val optValues = listOf(editOpt0, editOpt1, editOpt2, editOpt3)
                            val setOptValues = listOf<(String) -> Unit>(
                                { editOpt0 = it },
                                { editOpt1 = it },
                                { editOpt2 = it },
                                { editOpt3 = it }
                            )

                            optLabels.forEachIndexed { idx, label ->
                                val isCorrect = editCorrectIdx == idx
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isCorrect) CorrectGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        if (isCorrect) 1.5.dp else 1.dp,
                                        if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clickable { editCorrectIdx = idx }
                                                .padding(2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                RadioButton(
                                                    selected = isCorrect,
                                                    onClick = { editCorrectIdx = idx },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = Color.White,
                                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        OutlinedTextField(
                                            value = optValues[idx],
                                            onValueChange = setOptValues[idx],
                                            placeholder = { Text("অপশন $label এর বিবরণ") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = if (isCorrect) CorrectGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = editPyqYear,
                                onValueChange = { editPyqYear = it },
                                label = { Text("বিগত সাল / PYQ (ঐচ্ছিক)", maxLines = 1) },
                                placeholder = { Text("2025") },
                                leadingIcon = { Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp)) },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = editMarks.toString(),
                                onValueChange = { editMarks = it.toIntOrNull() ?: 1 },
                                label = { Text("নম্বর (Marks)", maxLines = 1) },
                                leadingIcon = { Icon(Icons.Default.Score, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp)) },
                                singleLine = true,
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "কঠিনতার মাত্রা (Difficulty Level):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    "EASY" to ("সহজ"),
                                    "MEDIUM" to ("মাঝারি"),
                                    "HARD" to ("কঠিন")
                                ).forEach { (diffKey, diffLabel) ->
                                    val isSelected = editDifficulty.equals(diffKey, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { editDifficulty = diffKey },
                                        label = { Text(diffLabel, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = when(diffKey) {
                                                "EASY" -> CorrectGreen
                                                "HARD" -> WrongRed
                                                else -> ExamGold
                                            },
                                            selectedLabelColor = if (diffKey == "MEDIUM") Color(0xFF1E1B2E) else Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editExplanation,
                            onValueChange = { editExplanation = it },
                            label = { Text("প্রশ্নের ব্যাখ্যা ও সমাধান (ঐচ্ছিক)") },
                            placeholder = { Text("সঠিক উত্তরের কারণ বা সূত্র লিখুন...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { editingQuestion = null },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Text("বাতিল করুন", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val opts = listOf(editOpt0, editOpt1, editOpt2, editOpt3).filter { it.isNotBlank() }
                                    val safeOpts = if (opts.size >= 2) opts else listOf(editOpt0, editOpt1, editOpt2, editOpt3)
                                    val safeCorrectIdx = editCorrectIdx.coerceIn(0, (safeOpts.size - 1).coerceAtLeast(0))

                                    val updatedTags = mutableListOf<String>()
                                    if (editPyqYear.isNotBlank()) {
                                        updatedTags.add("PYQ_${editPyqYear.trim()}")
                                    }
                                    q.tags.filterNot { it.startsWith("PYQ_") }.forEach { updatedTags.add(it) }

                                    val updatedQ = q.copy(
                                        classId = editClassId,
                                        subjectId = editSubjectId,
                                        chapterId = editChapterId,
                                        topicId = editTopicId,
                                        question = editQText,
                                        options = safeOpts,
                                        correctOptionIndex = safeCorrectIdx,
                                        correctAnswerText = safeOpts.getOrNull(safeCorrectIdx) ?: "",
                                        difficulty = editDifficulty,
                                        marks = editMarks,
                                        tags = updatedTags,
                                        explanation = editExplanation
                                    )
                                    viewModel.updateFirestoreQuestion(updatedQ) { success, msg ->
                                        onAction(success, msg)
                                        editingQuestion = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("💾 সংরক্ষণ করুন", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Question Confirmation Dialog
    if (deletingQuestion != null) {
        val q = deletingQuestion!!
        AlertDialog(
            onDismissRequest = { deletingQuestion = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WrongRed) },
            title = { Text("প্রশ্ন মুছে ফেলতে চান?") },
            text = {
                Text(
                    if (isBengali) "আপনি কি নিশ্চিত যে '${q.question}' প্রশ্নটি ফায়ারবেস ক্লাউড থেকে স্থায়ীভাবে মুছে ফেলতে চান?"
                    else "Are you sure you want to permanently delete question ID: ${q.id}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetId = q.id
                        deletingQuestion = null
                        viewModel.deleteFirestoreQuestion(targetId) { success, msg ->
                            onAction(success, msg)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed)
                ) {
                    Text("🗑️ মুছে ফেলুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingQuestion = null }) {
                    Text("বাতিল করুন")
                }
            }
        )
    }
}
