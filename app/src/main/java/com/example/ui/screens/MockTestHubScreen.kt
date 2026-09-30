package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssessmentMode
import com.example.ui.viewmodel.AppScreen
import com.example.data.model.AcademicTestItem
import com.example.data.model.AcademicTestSeriesProvider
import com.example.data.model.TestType
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.components.SubjectSvgIconProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel

@Composable
fun MockTestHubScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onStartMockTest: (AssessmentMode, String?, String?) -> Unit,
    onOpenSubjectDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val firestoreTestTemplates by viewModel.firestoreTestTemplates.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchFirestoreTestTemplates()
    }

    val subjectMockScores = studentProfile.subjectMockScores
    val passingThresholds = listOf(40, 45, 50, 55, 60, 65, 70)
    var expandedSubjectId by remember { mutableStateOf<String?>(null) }
    var testFilterType by remember { mutableStateOf<TestType?>(null) } // null = All, UNIT_TEST, MOCK_TEST
    var selectedSubjectCodeFilter by remember { mutableStateOf<com.example.data.model.SubjectCode?>(null) }

    val studentClassId = studentProfile.classId
    val filteredFirestoreTemplates = remember(firestoreTestTemplates, studentClassId) {
        val cid = if (studentClassId.isNotBlank()) studentClassId else "class_10_wbbse"
        firestoreTestTemplates.filter { it.classId.lowercase().trim() == cid.lowercase().trim() }
    }

    val currentClass = remember(studentProfile.classId) {
        com.example.data.model.AcademicClassProvider.ALL_CLASSES.find { it.id == studentProfile.classId }
    }
    val className = currentClass?.name ?: ("দশম শ্রেণি")

    val classTests = remember(studentProfile.classId, subjects, isBengali) {
        AcademicTestSeriesProvider.getTestsForClass(studentProfile.classId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("mock_test_hub_screen")
    ) {
        ScreenHeaderBar(
            title = "মক টেস্ট হাব",
            subtitle = "$className - অধ্যায় ও বিষয়ভিত্তিক পূর্ণাঙ্গ টেস্ট",
            isBengali = isBengali,
            showBackButton = true,
            onBack = { viewModel.goBack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Live Mock Test Banner (Connecting to the new Live Mock Test Hall)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE11D48).copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.LIVE_MOCK_TEST) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE11D48).copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "লাইভ মক টেস্ট এরিনা",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "লাইভ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "আসল পরীক্ষার টাইমার ও অল-বেঙ্গল লাইভ মেরিট লিস্ট",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Title Header
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text(
                    text = "বিষয়ভিত্তিক প্রস্তুতি",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "আপনার সিলেবাসের বিষয়সমূহ থেকে পড়াশোনা শুরু করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // List of subject cards
        items(subjects) { sub ->
            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            val cardBg = if (isDark) {
                Brush.linearGradient(
                    listOf(
                        Color(sub.colorHex).copy(alpha = 0.22f),
                        Color(sub.colorHex).copy(alpha = 0.10f)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color(sub.colorHex).copy(alpha = 0.08f),
                        Color(sub.colorHex).copy(alpha = 0.16f)
                    )
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onOpenSubjectDetail?.invoke(sub.id)
                    }
                    .testTag("subject_card_${sub.id}")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBg)
                        .padding(16.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(sub.colorHex).copy(alpha = if (isDark) 0.35f else 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = SubjectSvgIconProvider.getIconForSubjectCode(sub.code),
                                    contentDescription = sub.nameBengali,
                                    tint = if (isDark) Color.White else Color(sub.colorHex),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBengali) sub.nameBengali else sub.nameEnglish,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                if (!sub.textbookName.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = sub.textbookName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open",
                            tint = Color(sub.colorHex).copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
}
