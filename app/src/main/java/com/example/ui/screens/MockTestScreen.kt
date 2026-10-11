package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.QuestionItem
import com.example.ui.components.RealtimeCountdownTimer
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssessmentUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestScreen(
    state: AssessmentUiState,
    isBengali: Boolean,
    onSelectOption: (Int) -> Unit,
    onNavigateToQuestion: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onToggleMarkForReview: (Int?) -> Unit,
    onClearResponse: () -> Unit,
    onSubmitTest: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = state.session ?: return
    val questions = session.questions
    val totalQuestions = questions.size
    val currentIndex = state.currentQuestionIndex.coerceIn(0, (totalQuestions - 1).coerceAtLeast(0))
    val currentQuestion: QuestionItem? = questions.getOrNull(currentIndex)

    var showExitDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }

    val answeredCount = state.selectedAnswersMap.size
    val reviewCount = state.markedForReviewIndices.size
    val unansweredCount = (totalQuestions - answeredCount).coerceAtLeast(0)
    val isCurrentMarkedForReview = state.markedForReviewIndices.contains(currentIndex)
    val currentSelectedOption = state.selectedAnswersMap[currentIndex] ?: -1

    // Intercept hardware back button
    BackHandler(enabled = true) {
        showExitDialog = true
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("mock_test_screen"),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    // Top Action Bar: Exit, Timer, and Submit button aligned evenly on the same horizontal line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Exit Button
                        IconButton(
                            onClick = { showExitDialog = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .testTag("mock_exit_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "পরীক্ষা ত্যাগ করুন",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Center: Countdown Timer Badge
                        RealtimeCountdownTimer(
                            totalSeconds = session.timeLimitSeconds,
                            remainingSeconds = state.timeRemainingSeconds,
                            isBengali = isBengali,
                            isCompact = true,
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("mock_timer_badge")
                        )

                        // Top-Right: Submit Button (same height and alignment as Timer badge)
                        Button(
                            onClick = { showSubmitDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ExamGold,
                                contentColor = Color(0xFF1E1B4B)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("mock_top_submit_btn")
                        ) {
                            Text(
                                text = "জমা দিন",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    // Test Title below the action bar
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Horizontal Question Strip
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                itemsIndexed(questions) { index, _ ->
                                    val isCurrent = index == currentIndex
                                    val isAnswered = state.selectedAnswersMap.containsKey(index)
                                    val isMarked = state.markedForReviewIndices.contains(index)

                                    val badgeBgColor = when {
                                        isCurrent -> PrimaryIndigo
                                        isMarked -> Color(0xFF9333EA) // Purple
                                        isAnswered -> CorrectGreen
                                        else -> MaterialTheme.colorScheme.surface
                                    }

                                    val textColor = when {
                                        isCurrent || isMarked || isAnswered -> Color.White
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(badgeBgColor)
                                            .border(
                                                width = if (isCurrent) 2.dp else 1.dp,
                                                color = if (isCurrent) ExamGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                            .clickable { onNavigateToQuestion(index) }
                                            .testTag("mock_q_pill_$index"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isMarked && !isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                                    fontSize = 12.sp
                                                ),
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Grid Palette Icon
                            IconButton(
                                onClick = { showPaletteSheet = true },
                                modifier = Modifier.size(34.dp).testTag("open_mock_palette_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "প্রশ্ন গ্রিড",
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button (Solid themed background color)
                    Button(
                        onClick = { onPreviousQuestion() },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("mock_prev_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "পূর্ববর্তী",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Next or Final Submit Button
                    if (currentIndex == totalQuestions - 1) {
                        Button(
                            onClick = { showSubmitDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CorrectGreen,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("mock_final_submit_btn")
                        ) {
                            Text(
                                text = "জমা দিন",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = { onNextQuestion() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("mock_next_btn")
                        ) {
                            Text(
                                text = "পরবর্তী",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Question Card (natural height)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mock_question_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 26.sp,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mock_question_text")
                        )
                    }
                }

                // Options Section Header with Save / Bookmark Icon on the right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সঠিক বিকল্পটি নির্বাচন করুন:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clear Selection button (when option is chosen)
                        if (currentSelectedOption != -1) {
                            IconButton(
                                onClick = { onClearResponse() },
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .testTag("mock_clear_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "উত্তর মুছুন",
                                    tint = WrongRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Save / Bookmark (Mark for Review) Icon (Small and clean)
                        IconButton(
                            onClick = { onToggleMarkForReview(currentIndex) },
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrentMarkedForReview) Color(0xFF9333EA).copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                                .testTag("mock_mark_review_btn")
                        ) {
                            Icon(
                                imageVector = if (isCurrentMarkedForReview) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "রিভিউ বা সেভ রাখুন",
                                tint = if (isCurrentMarkedForReview) Color(0xFF9333EA) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Options List (A, B, C, D)
                currentQuestion.options.forEachIndexed { optionIndex, optionText ->
                    val isSelected = currentSelectedOption == optionIndex
                    val optionLabel = when (optionIndex) {
                        0 -> "A"
                        1 -> "B"
                        2 -> "C"
                        3 -> "D"
                        else -> "${(optionIndex + 65).toChar()}"
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                Color(0xFF3730A3)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF818CF8) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectOption(optionIndex) }
                            .testTag("mock_option_$optionIndex")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Option Label Circle
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFF6366F1)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionLabel,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Option Content Text
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            // Radio Check Icon
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF6366F1),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Full 30-Question Grid Palette
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "সম্পূর্ণ প্রশ্ন তালিকা (Question Grid)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Summary Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PaletteSummaryBadge(
                        label = "উত্তর দেওয়া",
                        count = answeredCount,
                        color = CorrectGreen
                    )
                    PaletteSummaryBadge(
                        label = "রিভিউ",
                        count = reviewCount,
                        color = Color(0xFF9333EA)
                    )
                    PaletteSummaryBadge(
                        label = "বাকি",
                        count = unansweredCount,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                HorizontalDivider()

                // Grid View of All Questions
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    itemsIndexed(questions) { index, _ ->
                        val isCurrent = index == currentIndex
                        val isAnswered = state.selectedAnswersMap.containsKey(index)
                        val isMarked = state.markedForReviewIndices.contains(index)

                        val badgeColor = when {
                            isCurrent -> PrimaryIndigo
                            isMarked -> Color(0xFF9333EA)
                            isAnswered -> CorrectGreen
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(badgeColor)
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) ExamGold else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onNavigateToQuestion(index)
                                    showPaletteSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                color = if (isCurrent || isMarked || isAnswered) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Submit Exam Confirmation Dialog
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircleOutline,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "মক টেস্ট জমা দিতে চান?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "আপনার বর্তমান পরীক্ষার স্থিতি:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusRow(
                                label = "মোট প্রশ্ন",
                                value = "$totalQuestions",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusRow(
                                label = "উত্তর দেওয়া হয়েছে",
                                value = "$answeredCount",
                                color = CorrectGreen
                            )
                            StatusRow(
                                label = "রিভিউয়ের জন্য চিহ্নিত",
                                value = "$reviewCount",
                                color = Color(0xFF9333EA)
                            )
                            StatusRow(
                                label = "উত্তর বাকি",
                                value = "$unansweredCount",
                                color = WrongRed
                            )
                        }
                    }

                    if (unansweredCount > 0) {
                        Text(
                            text = if (isBengali)
                                "⚠️ আপনার এখনও $unansweredCount টি প্রশ্নের উত্তর দেওয়া বাকি আছে।"
                            else
                                "⚠️ You still have $unansweredCount unanswered questions.",
                            style = MaterialTheme.typography.labelSmall,
                            color = WrongRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        onSubmitTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_mock_submit_btn")
                ) {
                    Text(
                        text = "চূড়ান্ত জমা দিন",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSubmitDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("cancel_mock_submit_btn")
                ) {
                    Text(
                        text = "পরীক্ষায় ফিরুন",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Exit Assessment Confirmation Dialog
    if (showExitDialog) {
        Dialog(
            onDismissRequest = { showExitDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "warning_exit_mock_animation")
            val iconScale by infiniteTransition.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "icon_scale"
            )
            val ringScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ring_scale"
            )
            val ringAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ring_alpha"
            )
            val iconRotation by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(650, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "icon_rotation"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(top = 36.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isBengali) "মক টেস্ট বাতিল করতে চান?" else "Cancel Mock Test?",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (isBengali)
                                "এখন বেরিয়ে গেলে আপনার দেওয়া উত্তর ও স্কোর সংরক্ষিত হবে না। আপনি কি নিশ্চিত বের হতে চান?"
                            else
                                "If you leave now, your answers and test score will not be saved. Are you sure you want to exit?",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showExitDialog = false },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cancel_exit_mock_btn"),
                                border = BorderStroke(1.5.dp, PrimaryIndigo)
                            ) {
                                Text(
                                    text = if (isBengali) "চালিয়ে যান" else "Continue",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryIndigo
                                )
                            }

                            Button(
                                onClick = {
                                    showExitDialog = false
                                    onExit()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("confirm_exit_mock_btn")
                            ) {
                                Text(
                                    text = if (isBengali) "বের হন" else "Exit",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Animated Warning Icon Badge positioned at the top-center edge of the card
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing radar ripple ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = ringScale
                                scaleY = ringScale
                                alpha = ringAlpha
                            }
                            .clip(CircleShape)
                            .background(WrongRed.copy(alpha = 0.35f))
                    )

                    // Core Circular Badge with border & glowing background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(WrongRed.copy(alpha = 0.15f))
                            .border(
                                3.dp,
                                WrongRed.copy(alpha = 0.45f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Warning",
                            tint = WrongRed,
                            modifier = Modifier
                                .size(40.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                    rotationZ = iconRotation
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteSummaryBadge(
    label: String,
    count: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}
