package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmrPracticeScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var totalQuestions by remember { mutableIntStateOf(15) }
    // User selected bubbles: map of questionIndex (0..total-1) -> selectedOption (0:A, 1:B, 2:C, 3:D)
    val userAnswers = remember { mutableStateMapOf<Int, Int>() }
    var isSubmitted by remember { mutableStateOf(false) }

    // Mock answer key for automatic optical evaluation
    val answerKey = remember(totalQuestions) {
        // Predefined or deterministic pattern
        List(totalQuestions) { index -> (index * 2 + 1) % 4 }
    }

    val correctCount = remember(isSubmitted, userAnswers) {
        if (!isSubmitted) 0
        else userAnswers.count { (qIndex, selected) -> answerKey.getOrNull(qIndex) == selected }
    }

    val incorrectCount = remember(isSubmitted, userAnswers) {
        if (!isSubmitted) 0
        else userAnswers.count { (qIndex, selected) -> answerKey.getOrNull(qIndex) != null && answerKey[qIndex] != selected }
    }

    val unattemptedCount = totalQuestions - userAnswers.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ডিজিটাল OMR শীট সিমুলেটর",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("omr_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSubmitted) {
                        TextButton(
                            onClick = {
                                userAnswers.clear()
                                isSubmitted = false
                            },
                            modifier = Modifier.testTag("omr_reset_btn")
                        ) {
                            Text("পুনরায় পরীক্ষা")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Question count selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15 to "১৫টি প্রশ্ন (মিনি)", 30 to "৩০টি প্রশ্ন (স্ট্যান্ডার্ড)", 90 to "৯০টি প্রশ্ন (পূর্ণাঙ্গ)").forEach { (count, label) ->
                        val isSelected = totalQuestions == count
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ExamGold else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = !isSubmitted) {
                                    totalQuestions = count
                                    userAnswers.clear()
                                }
                                .testTag("omr_count_$count")
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isBengali) label else "$count Questions",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 10.sp
                                    ),
                                    color = if (isSelected) Color(0xFF1E1B2E) else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // OMR Candidate Header Box
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WBBSE OMR RESPONSE SHEET",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                color = ExamGold
                            )
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text(
                                    text = "SET-A • ২০২৬",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "নির্দেশিকা: উত্তরটি নির্দেশ করতে কালো কালির বৃত্তে ট্যাপ করে সম্পূর্ণ ভরাট করুন।",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Result Score Card (if submitted)
            if (isSubmitted) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SecondaryEmerald.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.5.dp, SecondaryEmerald),
                        modifier = Modifier.fillMaxWidth().testTag("omr_score_summary_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🎉 অপটিক্যাল স্ক্যান মূল্যায়ন সমাপ্ত!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = SecondaryEmerald
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$correctCount", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = CorrectGreen)
                                    Text(text = "সঠিক উত্তর", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$incorrectCount", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = WrongRed)
                                    Text(text = "ভুল উত্তর", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$unattemptedCount", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "অনবোর্ড", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // OMR Bubble Rows (4 options per question: A, B, C, D)
            items(totalQuestions) { index ->
                val qNumber = index + 1
                val selectedOption = userAnswers[index]
                val correctOption = answerKey.getOrNull(index)

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        when {
                            !isSubmitted -> MaterialTheme.colorScheme.surfaceVariant
                            selectedOption == correctOption -> CorrectGreen
                            selectedOption != null -> WrongRed
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("omr_row_$qNumber")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Question Number
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$qNumber",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Options A, B, C, D Bubbles
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            listOf("A" to 0, "B" to 1, "C" to 2, "D" to 3).forEach { (label, optIndex) ->
                                val isSelected = selectedOption == optIndex
                                val isCorrectOption = correctOption == optIndex

                                val bubbleColor = when {
                                    !isSubmitted -> if (isSelected) Color(0xFF1E293B) else Color.Transparent
                                    isSubmitted && isSelected && isCorrectOption -> CorrectGreen
                                    isSubmitted && isSelected && !isCorrectOption -> WrongRed
                                    isSubmitted && !isSelected && isCorrectOption -> ExamGold.copy(alpha = 0.4f)
                                    else -> Color.Transparent
                                }

                                val textColor = when {
                                    !isSubmitted -> if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    isSubmitted && isSelected -> Color.White
                                    isSubmitted && isCorrectOption -> ExamGold
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(bubbleColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.5.dp,
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                            shape = CircleShape
                                        )
                                        .clickable(enabled = !isSubmitted) {
                                            userAnswers[index] = optIndex
                                        }
                                        .testTag("omr_bubble_${qNumber}_$label"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Submit Button
            item {
                if (!isSubmitted) {
                    Button(
                        onClick = { isSubmitted = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ExamGold, contentColor = Color(0xFF1E1B2E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("omr_submit_scan_btn")
                    ) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OMR স্ক্যান ও ফলাফল মূল্যায়ন করুন (${userAnswers.size}/$totalQuestions)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
