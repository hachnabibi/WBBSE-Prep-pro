package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
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

private fun getSubjectGradient(subjectIdOrCode: String?, isDark: Boolean): Brush {
    val key = (subjectIdOrCode ?: "").lowercase()
    val colors = when {
        key.contains("math") -> if (isDark) listOf(Color(0xFF2C223B), Color(0xFF3B2A56)) else listOf(Color(0xFFFFECD2), Color(0xFFFCB69F))
        key.contains("phy") -> if (isDark) listOf(Color(0xFF231942), Color(0xFF3F2B96)) else listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
        key.contains("life") || key.contains("bio") -> if (isDark) listOf(Color(0xFF133B31), Color(0xFF1F5E4B)) else listOf(Color(0xFF84FAB0), Color(0xFF8FD3F4))
        key.contains("hist") -> if (isDark) listOf(Color(0xFF4A2E11), Color(0xFF6B3A2A)) else listOf(Color(0xFFFEE140), Color(0xFFFA709A))
        key.contains("geo") -> if (isDark) listOf(Color(0xFF0F3854), Color(0xFF1A5276)) else listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
        key.contains("ben") -> if (isDark) listOf(Color(0xFF4A1525), Color(0xFF6B2D3C)) else listOf(Color(0xFFFF9a9e), Color(0xFFfecfef))
        key.contains("eng") -> if (isDark) listOf(Color(0xFF1A365D), Color(0xFF2B4C7E)) else listOf(Color(0xFFa1c4fd), Color(0xFFc2e9fb))
        key.contains("env") -> if (isDark) listOf(Color(0xFF1B3B2B), Color(0xFF2D6A4F)) else listOf(Color(0xFFd4fc79), Color(0xFF96e6a1))
        else -> if (isDark) listOf(Color(0xFF1E2538), Color(0xFF2A354D)) else listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
    }
    return Brush.linearGradient(colors)
}

@Composable
private fun SubjectProgressCard(
    subject: SubjectItem,
    isBengali: Boolean,
    onOpenJourney: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val gradientBrush = getSubjectGradient(subject.id, isDark)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenJourney() }
            .testTag("subject_card_${subject.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = CircleShape,
                        color = textColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBengali) "অগ্রগতি" else "Progress",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = subTextColor
                        )
                        Text(
                            text = "${subject.masteryPercentage}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = textColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = { subject.masteryPercentage.toFloat() / 100f },
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                        trackColor = textColor.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}

