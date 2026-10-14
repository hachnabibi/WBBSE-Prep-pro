package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.firestore.FirestoreTestTemplate
import com.example.data.local.SubjectEntity
import com.example.data.model.AcademicClassProvider
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.util.formatCompactNumber

/**
 * ট্যাব ৫: টেস্ট টেমপ্লেট ম্যানেজমেন্ট
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTestTemplatesScreen(
    viewModel: DeshExamViewModel,
    isLoading: Boolean,
    isBengali: Boolean = true,
    onAction: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val firestoreTestTemplatesList by viewModel.firestoreTestTemplates.collectAsState()
    val allClasses = remember { AcademicClassProvider.ALL_CLASSES }

    var selectedClass by remember {
        mutableStateOf(allClasses.firstOrNull { it.id == "class_10_wbbse" } ?: allClasses.first())
    }

    var dbSubjects by remember { mutableStateOf<List<SubjectEntity>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            dbSubjects = viewModel.repository.dao.getAllSubjectsOnce()
            viewModel.fetchFirestoreTestTemplates()
        } catch (e: Exception) {
            android.util.Log.e("AdminTestTemplatesScreen", "Error loading entities: ${e.message}", e)
        }
    }

    val classSubjects = remember(selectedClass, dbSubjects) {
        val filtered = dbSubjects.filter { it.classId == selectedClass.id }
        if (filtered.isNotEmpty()) filtered else getAdminClassSubjects(selectedClass.id)
    }

    var selectedSubject by remember(selectedClass) {
        mutableStateOf(classSubjects.first())
    }

    var testCmsClassDropdownExpanded by remember { mutableStateOf(false) }
    var testCmsSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var deletingTestTemplate by remember { mutableStateOf<FirestoreTestTemplate?>(null) }

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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
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
                            Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "📑 ফায়ারবেস টেস্ট টেমপ্লেট ব্যবস্থাপনা",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.fetchFirestoreTestTemplates(selectedClass.id, selectedSubject.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = testCmsClassDropdownExpanded,
                            onExpandedChange = { testCmsClassDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedClass.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("শ্রেণি") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = testCmsClassDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = testCmsClassDropdownExpanded,
                                onDismissRequest = { testCmsClassDropdownExpanded = false }
                            ) {
                                allClasses.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text(cls.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedClass = cls
                                            val subs = getAdminClassSubjects(cls.id)
                                            selectedSubject = subs.first()
                                            testCmsClassDropdownExpanded = false
                                            viewModel.fetchFirestoreTestTemplates(cls.id, selectedSubject.id)
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = testCmsSubjectDropdownExpanded,
                            onExpandedChange = { testCmsSubjectDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedSubject.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("বিষয়") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = testCmsSubjectDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = testCmsSubjectDropdownExpanded,
                                onDismissRequest = { testCmsSubjectDropdownExpanded = false }
                            ) {
                                classSubjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = { Text(subject.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedSubject = subject
                                            testCmsSubjectDropdownExpanded = false
                                            viewModel.fetchFirestoreTestTemplates(selectedClass.id, subject.id)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBengali) "মোট ${formatCompactNumber(firestoreTestTemplatesList.size)} টি টেস্ট টেমপ্লেট ফায়ারবেসে সংরক্ষিত আছে।"
                            else "Total ${formatCompactNumber(firestoreTestTemplatesList.size)} test templates available in Firebase.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        if (isLoading && firestoreTestTemplatesList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (firestoreTestTemplatesList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                        Text(
                            text = "কোনো টেস্ট টেমপ্লেট পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        } else {
            items(firestoreTestTemplatesList) { test ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
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
                                text = test.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = test.testType,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                Text(
                                    text = "${test.durationMinutes} মিনিট",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.AdsClick, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(15.dp))
                                Text(
                                    text = "${test.totalMarks} নম্বর",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                                Text(
                                    text = "${test.questions.size} টি প্রশ্ন",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Text(
                            text = "ID: ${test.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { deletingTestTemplate = test },
                            colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🗑️ টেস্ট ডিলিট করুন", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (deletingTestTemplate != null) {
        val test = deletingTestTemplate!!
        AlertDialog(
            onDismissRequest = { deletingTestTemplate = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WrongRed) },
            title = { Text("টেস্ট ডিলিট করতে চান?") },
            text = {
                Text(
                    if (isBengali) "আপনি কি নিশ্চিত যে '${test.title}' টেস্ট টেমপ্লেটটি ফায়ারবেস ক্লাউড থেকে স্থায়ীভাবে মুছে ফেলতে চান?"
                    else "Are you sure you want to delete test '${test.title}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetId = test.id
                        deletingTestTemplate = null
                        viewModel.deleteFirestoreTestTemplate(targetId) { success, msg ->
                            onAction(success, msg)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed)
                ) {
                    Text("🗑️ মুছে ফেলুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTestTemplate = null }) {
                    Text("বাতিল করুন")
                }
            }
        )
    }
}
