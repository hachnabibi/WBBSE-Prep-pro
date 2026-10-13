package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdBannerCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel

import kotlinx.serialization.Serializable

@Serializable
data class PyqQuestionItem(
    val id: String = "",
    val year: Int = 2026,
    val subject: String = "",
    val group: String = "", // Group A (MCQ), Group B (VSA), Group C (SA), Group D (LA)
    val question: String = "",
    val marks: Int = 1,
    val officialAnswerBengali: String = "",
    val keyTips: String = "",
    val chapterName: String = "",
    val options: List<String>? = null,
    val correctOptionIndex: Int = -1,
    val className: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyqExplorerScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onBack: () -> Unit,
    onStartTest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.studentProfile.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var selectedYear by remember { mutableIntStateOf(2026) }
    var selectedSubject by remember { mutableStateOf("ALL") }
    var expandedQuestionId by remember { mutableStateOf<String?>(null) }
    var selectedOptions by remember { mutableStateOf(mapOf<String, Int>()) }

    val years = remember { listOf(2026, 2025, 2024, 2023, 2022, 2020, 2019, 2018, 2017, 2016, 2015) }

    val pyqQuestions by viewModel.allPyqQuestions.collectAsState()

    LaunchedEffect(pyqQuestions) {
        val initialSelections = pyqQuestions
            .filter { it.correctOptionIndex != -1 }
            .associate { it.id to it.correctOptionIndex }
        selectedOptions = initialSelections
    }

    val filteredQuestions = remember(selectedYear, selectedSubject, pyqQuestions) {
        pyqQuestions.filter { q ->
            if (q.year != selectedYear) return@filter false
            if (selectedSubject == "ALL") return@filter true

            val qSub = q.subject.trim().uppercase().replace(" ", "_")
            val selSub = selectedSubject.trim().uppercase().replace(" ", "_")

            qSub == selSub ||
            (selSub == "MATHEMATICS" && (qSub == "MATH" || qSub == "MATHEMATICS")) ||
            (selSub == "LIFE_SCIENCE" && (qSub == "LIFE_SCIENCE" || qSub == "LIFESCIENCE" || qSub == "LIFE_SCI")) ||
            (selSub == "PHYSICAL_SCIENCE" && (qSub == "PHYSICAL_SCIENCE" || qSub == "PHYSICALSCIENCE" || qSub == "PHY_SCI")) ||
            (selSub == "HISTORY" && (qSub == "HISTORY" || qSub == "HIST")) ||
            (selSub == "GEOGRAPHY" && (qSub == "GEOGRAPHY" || qSub == "GEOG")) ||
            (selSub == "BENGALI" && (qSub == "BENGALI" || qSub == "BEN")) ||
            (selSub == "ENGLISH" && (qSub == "ENGLISH" || qSub == "ENG"))
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Unified Custom Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(32.dp).testTag("header_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = "বিগত ১০ বছরের প্রশ্নপত্র",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "২০১৫ – ২০২৫ আসল প্রশ্ন ও সমাধান",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { onStartTest("mock_pyq_${selectedYear}_all") },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("pyq_exam_btn").height(32.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$selectedYear মক পরীক্ষা",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = Color.White
                        )
                    }
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ad Card added at the top
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    com.example.ui.components.AdMobNativeCard(isBengali = isBengali)
                }

                // Government Disclaimer Card (Google Play Policy Compliance)
                item {
                    GovernmentDisclaimerCard(isBengali = isBengali)
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "পরীক্ষার বছর নির্বাচন করুন:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(years) { year ->
                            val isSelected = selectedYear == year
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ExamGold else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) ExamGold else Color.Transparent),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedYear = year }
                                    .testTag("pyq_year_$year")
                            ) {
                                Text(
                                    text = "$year সাল",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF1E1B2E) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

            item {
                // Subject Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = mutableListOf("ALL" to ("সকল বিষয় (${profile.classLevel})"))
                    subjects.forEach { subj ->
                        filters.add(subj.code.name to (subj.name))
                    }
                    items(filters) { (key, label) ->
                        val isSelected = selectedSubject == key
                        val matchingSubject = subjects.find { it.code.name == key }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                if (key == "ALL") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = "All",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                } else if (matchingSubject != null) {
                                    Icon(
                                        imageVector = com.example.ui.components.SubjectSvgIconProvider.getIconForSubjectCode(matchingSubject.code),
                                        contentDescription = matchingSubject.name,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) Color.White else Color(matchingSubject.colorHex)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryIndigo,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("pyq_filter_$key")
                        )
                    }
                }
            }

            item {
                // Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PrimaryIndigo.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = ExamGold, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$selectedYear সালের পর্ষদ স্ট্যান্ডার্ড প্রশ্নমালা",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "প্রশ্নে ট্যাপ করে অফিশিয়াল মডেল উত্তর ও নম্বর বিভাজন দেখুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                AdBannerCard(isBengali = isBengali)
            }

            if (filteredQuestions.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "$selectedYear সালের এই বিষয়ের প্রশ্নপত্র লোড হচ্ছে...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(filteredQuestions) { q ->
                val isExpanded = expandedQuestionId == q.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pyq_item_${q.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = q.group,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryIndigoLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = q.chapterName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = ExamGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${q.marks} নম্বর",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = ExamGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = q.question,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Display options if they exist
                        if (!q.options.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val selectedOption = selectedOptions[q.id] ?: -1
                            q.options.forEachIndexed { index, option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedOptions = selectedOptions.toMutableMap().apply { put(q.id, index) }
                                        }
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (selectedOption == index),
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = if (selectedOption != -1) {
                                                if (index == q.correctOptionIndex) Color(0xFF10B981) // Green for correct
                                                else if (selectedOption == index) Color(0xFFDC2626) // Red for wrong
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            } else MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selectedOption != -1) {
                                            if (index == q.correctOptionIndex) Color(0xFF10B981)
                                            else if (selectedOption == index) Color(0xFFDC2626)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        } else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Answer Toggler (only this is clickable to expand)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedQuestionId = if (isExpanded) null else q.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isExpanded) "মডেল উত্তর লুকান" else "অফিশিয়াল মডেল উত্তর দেখুন",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isExpanded) ExamGold else PrimaryIndigoLight
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = if (isExpanded) ExamGold else PrimaryIndigoLight
                                )
                            }
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, SecondaryEmerald),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "✅ সঠিক উত্তর:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = SecondaryEmerald
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = q.officialAnswerBengali,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = DarkCard,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "💡 টিপস: ${q.keyTips}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ExamGold,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
}

