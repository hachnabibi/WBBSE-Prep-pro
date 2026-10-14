package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SubjectEntity
import com.example.data.model.AcademicClassProvider
import com.example.data.model.AssessmentMode
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ExamGold
import com.example.ui.viewmodel.DeshExamViewModel

/**
 * ট্যাব ৩: কাস্টম মক টেস্ট ক্রিয়েটর
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCustomTestScreen(
    viewModel: DeshExamViewModel,
    isLoading: Boolean,
    isBengali: Boolean = true,
    onAction: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allClasses = remember { AcademicClassProvider.ALL_CLASSES }
    var selectedClass by remember {
        mutableStateOf(allClasses.firstOrNull { it.id == "class_10_wbbse" } ?: allClasses.first())
    }

    var dbSubjects by remember { mutableStateOf<List<SubjectEntity>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            dbSubjects = viewModel.repository.dao.getAllSubjectsOnce()
        } catch (e: Exception) {
            android.util.Log.e("AdminCustomTestScreen", "Error loading subjects: ${e.message}", e)
        }
    }

    val classSubjects = remember(selectedClass, dbSubjects) {
        val filtered = dbSubjects.filter { it.classId == selectedClass.id }
        if (filtered.isNotEmpty()) filtered else getAdminClassSubjects(selectedClass.id)
    }

    var selectedSubject by remember(selectedClass) {
        mutableStateOf(classSubjects.first())
    }

    var customClassDropdownExpanded by remember { mutableStateOf(false) }
    var customSubjectDropdownExpanded by remember { mutableStateOf(false) }

    var customTestTitle by remember { mutableStateOf("") }
    var customTestId by remember { mutableStateOf("custom_test_${System.currentTimeMillis() % 10000}") }
    var customTestTimeMins by remember { mutableStateOf("25") }
    var selectedAssessmentMode by remember { mutableStateOf(AssessmentMode.MOCK_TEST) }

    var testQuestionText by remember { mutableStateOf("") }
    var testOpt1 by remember { mutableStateOf("") }
    var testOpt2 by remember { mutableStateOf("") }
    var testOpt3 by remember { mutableStateOf("") }
    var testOpt4 by remember { mutableStateOf("") }
    var testCorrectIndex by remember { mutableIntStateOf(0) }
    var testExplanation by remember { mutableStateOf("") }
    var testDifficulty by remember { mutableStateOf("MEDIUM") }

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
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "📝 নিজস্ব টেস্ট তৈরি করে তাতে প্রশ্ন যোগ করুন",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "প্রথমে টেস্টের নাম ও আইডি দিন, তারপর একের পর এক প্রশ্ন যোগ করুন। ছাত্রছাত্রীরা মক টেস্ট হাব থেকে এই টেস্ট দিতে পারবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "১. টেস্টের সাধারণ বিবরণ:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = customTestTitle,
                        onValueChange = { customTestTitle = it },
                        label = { Text("টেস্টের নাম বা শিরোনাম") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = customTestId,
                            onValueChange = { customTestId = it },
                            label = { Text("টেস্টের ইউনিক কোড") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customTestTimeMins,
                            onValueChange = { customTestTimeMins = it },
                            label = { Text("সময় (মিনিট)") },
                            modifier = Modifier.width(110.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = customClassDropdownExpanded,
                    onExpandedChange = { customClassDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedClass.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("শ্রেণি") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customClassDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = customClassDropdownExpanded,
                        onDismissRequest = { customClassDropdownExpanded = false }
                    ) {
                        allClasses.forEach { cls ->
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
                                    customClassDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = customSubjectDropdownExpanded,
                    onExpandedChange = { customSubjectDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSubject.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("বিষয়") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customSubjectDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = customSubjectDropdownExpanded,
                        onDismissRequest = { customSubjectDropdownExpanded = false }
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
                                    customSubjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "২. টেস্টের জন্য প্রশ্ন যোগ করুন:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                OutlinedTextField(
                    value = testQuestionText,
                    onValueChange = { testQuestionText = it },
                    label = { Text("প্রশ্নের বিবরণ") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = testOpt1,
                    onValueChange = { testOpt1 = it },
                    label = { Text("অপশন ক") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = testOpt2,
                    onValueChange = { testOpt2 = it },
                    label = { Text("অপশন খ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = testOpt3,
                    onValueChange = { testOpt3 = it },
                    label = { Text("অপশন গ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = testOpt4,
                    onValueChange = { testOpt4 = it },
                    label = { Text("অপশন ঘ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "সঠিক উত্তর নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val optionTags = if (isBengali) listOf("ক", "খ", "গ", "ঘ") else listOf("A", "B", "C", "D")
                    optionTags.forEachIndexed { idx, letter ->
                        val isSelected = testCorrectIndex == idx
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CorrectGreen else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { testCorrectIndex = idx }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = letter,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "টেস্টের ধরন বা ক্যাটাগরি:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                val modes = listOf(
                    AssessmentMode.MOCK_TEST to ("মডেল টেস্ট"),
                    AssessmentMode.PRACTICE to ("অধ্যায় পরীক্ষা"),
                    AssessmentMode.BOSS_BATTLE to ("ইউনিট টেস্ট"),
                    AssessmentMode.SPEED_BLITZ to ("সময়বদ্ধ পরীক্ষা"),
                    AssessmentMode.DAILY_10 to ("দৈনিক অনুশীলন"),
                    AssessmentMode.WEAK_AREA to ("সংশোধনী টেস্ট"),
                    AssessmentMode.FRIEND_CHALLENGE to ("চ্যালেঞ্জ কুইজ"),
                    AssessmentMode.CHAPTER_PYQ to ("বিগত বছরের প্রশ্ন"),
                    AssessmentMode.MAP_DIAGRAM_QUIZ to ("চিত্রভিত্তিক টেস্ট"),
                    AssessmentMode.CHAPTER_MARATHON_QUIZ to ("ম্যারাথন কুইজ")
                )
                modes.chunked(3).forEach { rowModes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowModes.forEach { (mode, label) ->
                            val isSelected = selectedAssessmentMode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) ExamGold else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedAssessmentMode = mode }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color(0xFF1E1B2E) else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        repeat(3 - rowModes.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = testExplanation,
                onValueChange = { testExplanation = it },
                label = { Text("ব্যাখ্যা ও সমাধান (ঐচ্ছিক)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val opts = listOf(testOpt1, testOpt2, testOpt3, testOpt4).filter { it.isNotBlank() }
                    val correctAns = opts.getOrNull(testCorrectIndex) ?: testOpt1
                    viewModel.createCustomTestAndAddQuestion(
                        testTitle = customTestTitle,
                        testId = customTestId,
                        classId = selectedClass.id,
                        subjectId = selectedSubject.id,
                        timeMinutes = customTestTimeMins.toIntOrNull() ?: 20,
                        assessmentMode = selectedAssessmentMode.name,
                        questionText = testQuestionText,
                        optionsList = opts,
                        correctIndex = testCorrectIndex,
                        correctAnswerText = correctAns,
                        explanationText = testExplanation.ifBlank { "সঠিক উত্তর।" },
                        difficulty = testDifficulty
                    ) { success, msg ->
                        onAction(success, msg)
                        if (success) {
                            testQuestionText = ""
                            testOpt1 = ""
                            testOpt2 = ""
                            testOpt3 = ""
                            testOpt4 = ""
                            testExplanation = ""
                        }
                    }
                },
                enabled = !isLoading && customTestTitle.isNotBlank() && testQuestionText.isNotBlank() && testOpt1.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("যোগ হচ্ছে...", color = Color.White)
                } else {
                    Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "➕ এই টেস্টে প্রশ্ন যোগ করুন",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
