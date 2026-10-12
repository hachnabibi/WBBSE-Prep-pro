package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SubjectEntity
import com.example.data.model.SubjectItem
import com.example.ui.components.ScreenHeaderBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSubjectsScreen(
    subjects: List<SubjectItem>,
    isBengali: Boolean,
    onBack: () -> Unit,
    onOpenSubjectDetail: (SubjectEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }

    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) {
            subjects
        } else {
            val q = searchQuery.trim().lowercase()
            subjects.filter {
                it.displayName.lowercase().contains(q) ||
                        it.nameEnglish.lowercase().contains(q) ||
                        it.nameBengali.lowercase().contains(q) ||
                        it.code.id.lowercase().contains(q) ||
                        it.code.name.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeaderBar(
            title = if (isBengali) "সকল বিষয় ও পাঠ্যক্রম" else "All Subjects & Syllabus",
            subtitle = if (isBengali) "আপনার ক্লাসের সমস্ত নির্ধারিত বিষয়" else "All prescribed subjects for your class",
            isBengali = isBengali,
            showBackButton = true,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("all_subjects_search_input"),
                placeholder = {
                    Text(
                        text = if (isBengali) "বিষয় খুঁজুন..." else "Search subject...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (filteredSubjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBengali) "কোনো বিষয় পাওয়া যায়নি" else "No subjects found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("all_subjects_grid"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredSubjects) { subject ->
                        SubjectProgressCard(
                            subject = subject,
                            isBengali = isBengali,
                            onOpenJourney = {
                                val subjectEntity = SubjectEntity(
                                    id = subject.id,
                                    code = subject.code.id,
                                    nameBengali = subject.nameBengali,
                                    nameEnglish = subject.nameEnglish,
                                    totalChapters = subject.totalChapters,
                                    completedChapters = subject.completedChapters,
                                    masteryPercentage = subject.masteryPercentage,
                                    colorHex = subject.colorHex
                                )
                                onOpenSubjectDetail(subjectEntity)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
