package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AdRewardType
import com.example.ui.components.RealtimeCountdownTimer
import com.example.ui.components.RewardedAdDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssessmentUiState
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AssessmentScreen(
    state: AssessmentUiState,
    isBengali: Boolean,
    onSelectOption: (Int) -> Unit,
    onCheckOrSubmit: () -> Unit,
    onAdvanceNext: () -> Unit,
    onExit: () -> Unit,
    onNavigateToQuestion: ((Int) -> Unit)? = null,
    onNextMockQuestion: (() -> Unit)? = null,
    onPreviousMockQuestion: (() -> Unit)? = null,
    onToggleMarkForReview: ((Int?) -> Unit)? = null,
    onClearResponse: (() -> Unit)? = null,
    onSubmitMockTest: (() -> Unit)? = null,
    onUseFiftyFifty: (() -> Unit)? = null,
    onUseExtraTime: (() -> Unit)? = null,
    onUseSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val session = state.session ?: return

    if (session.mode == AssessmentMode.MOCK_TEST) {
        MockTestScreen(
            state = state,
            isBengali = isBengali,
            onSelectOption = onSelectOption,
            onNavigateToQuestion = { idx -> onNavigateToQuestion?.invoke(idx) },
            onNextQuestion = { onNextMockQuestion?.invoke() },
            onPreviousQuestion = { onPreviousMockQuestion?.invoke() },
            onToggleMarkForReview = { idx -> onToggleMarkForReview?.invoke(idx) },
            onClearResponse = { onClearResponse?.invoke() },
            onSubmitTest = { onSubmitMockTest?.invoke() ?: onCheckOrSubmit() },
            onExit = onExit,
            modifier = modifier
        )
        return
    }

    val currentQ = session.questions.getOrNull(state.currentQuestionIndex) ?: return
    val totalQ = session.questions.size
    val currentNum = state.currentQuestionIndex + 1
    val progress = currentNum.toFloat() / totalQ.toFloat()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    var showExitConfirmation by remember { mutableStateOf(false) }
    var extraTimeSeconds by remember { mutableIntStateOf(0) }
    var isExtraTimeUsed by remember { mutableStateOf(false) }
    var isSkipUsed by remember { mutableStateOf(false) }
    val effectiveRemainingSeconds = (state.timeRemainingSeconds + extraTimeSeconds).coerceAtLeast(0)

    var showRewardedAdForFiftyFifty by remember { mutableStateOf(false) }
    var showRewardedAdForExtraTime by remember { mutableStateOf(false) }

    // Intercept back button
    BackHandler(enabled = true) {
        showExitConfirmation = true
    }

    // Auto-advance to next question smoothly after celebration animation (2.1 seconds duration)
    LaunchedEffect(state.isAnswerChecked, state.currentQuestionIndex) {
        if (state.isAnswerChecked) {
            delay(2100)
            onAdvanceNext()
        }
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Warning",
                    tint = WrongRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "টেস্ট ছেড়ে বের হতে চান?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = if (isBengali)
                        "এখন বেরিয়ে গেলে আপনার বর্তমান টেস্টের স্কোর সংরক্ষিত হবে না। আপনি কি নিশ্চিত টেস্ট বন্ধ করতে চান?"
                    else
                        "If you leave now, your current test progress will be lost. Are you sure you want to exit?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmation = false
                        onExit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_exit_assessment_btn")
                ) {
                    Text(
                        text = "হ্যাঁ, বের হন",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitConfirmation = false },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("cancel_exit_assessment_btn")
                ) {
                    Text(
                        text = "পরীক্ষা চালিয়ে যান",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showRewardedAdForFiftyFifty) {
        RewardedAdDialog(
            rewardType = AdRewardType.FIFTY_FIFTY,
            isBengali = isBengali,
            onRewardEarned = {
                showRewardedAdForFiftyFifty = false
                onUseFiftyFifty?.invoke()
            },
            onDismiss = { showRewardedAdForFiftyFifty = false }
        )
    }

    if (showRewardedAdForExtraTime) {
        RewardedAdDialog(
            rewardType = AdRewardType.EXTRA_TIME,
            isBengali = isBengali,
            onRewardEarned = {
                showRewardedAdForExtraTime = false
                extraTimeSeconds += 60
                isExtraTimeUsed = true
                onUseExtraTime?.invoke()
            },
            onDismiss = { showRewardedAdForExtraTime = false }
        )
    }

    Box(modifier = modifier.fillMaxSize().testTag("assessment_screen")) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        // Top Header Action Row (Exit, Timer, Question Count)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Close Button
                            IconButton(
                                onClick = { showExitConfirmation = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .testTag("assessment_exit_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "বেরিয়ে যান",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Center: Realtime Countdown Timer (Matching right card shape, height & style)
                            RealtimeCountdownTimer(
                                totalSeconds = session.timeLimitSeconds + extraTimeSeconds,
                                remainingSeconds = effectiveRemainingSeconds,
                                isBengali = isBengali,
                                isCompact = true,
                                modifier = Modifier
                                    .height(34.dp)
                                    .wrapContentWidth()
                                    .testTag("assessment_timer_badge")
                            )

                            // Right: Question Counter Pill (Matching center card shape, height & style)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDark) Color(0xFF2E2660) else Color(0xFFEDE9FE),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = PrimaryIndigo.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "প্রশ্ন $currentNum/$totalQ",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = if (isDark) Color(0xFFA5B4FC) else PrimaryIndigo
                                    )
                                }
                            }
                        }

                        // Test Title Centered
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Full-width Sleek Progress Bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = when (session.mode) {
                                AssessmentMode.BOSS_BATTLE -> ExamFlame
                                AssessmentMode.MOCK_TEST -> ExamCyan
                                else -> PrimaryIndigo
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            },
            bottomBar = {
                // Bottom Lifelines Row for easy one-handed accessibility
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lifeline 1: 50:50 (Cyan/Teal)
                        OutlinedButton(
                            onClick = {
                                if (!state.isFiftyFiftyUsed) {
                                    showRewardedAdForFiftyFifty = true
                                }
                            },
                            enabled = !state.isFiftyFiftyUsed && !state.isAnswerChecked,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (state.isFiftyFiftyUsed) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else ExamCyan
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (state.isFiftyFiftyUsed) Color.Transparent else ExamCyan.copy(alpha = if (isDark) 0.15f else 0.08f),
                                contentColor = ExamCyan
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (state.isFiftyFiftyUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else ExamCyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "৫০:৫০",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                color = if (state.isFiftyFiftyUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else ExamCyan
                            )
                        }

                        // Lifeline 2: +30s Extra Time (Gold/Amber)
                        OutlinedButton(
                            onClick = {
                                if (!isExtraTimeUsed) {
                                    showRewardedAdForExtraTime = true
                                }
                            },
                            enabled = !isExtraTimeUsed,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (isExtraTimeUsed) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else ExamGold
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isExtraTimeUsed) Color.Transparent else ExamGold.copy(alpha = if (isDark) 0.15f else 0.08f),
                                contentColor = ExamGold
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isExtraTimeUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else ExamGold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+৩০ সে.",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                color = if (isExtraTimeUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else ExamGold
                            )
                        }

                        // Lifeline 3: Skip (Orange/Flame)
                        OutlinedButton(
                            onClick = {
                                if (!isSkipUsed) {
                                    isSkipUsed = true
                                    onUseSkip?.invoke()
                                }
                            },
                            enabled = !isSkipUsed && !state.isAnswerChecked,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSkipUsed) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else ExamFlame
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSkipUsed) Color.Transparent else ExamFlame.copy(alpha = if (isDark) 0.15f else 0.08f),
                                contentColor = ExamFlame
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSkipUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else ExamFlame
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "স্কিপ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                color = if (isSkipUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else ExamFlame
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
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
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF131F33) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF283952) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = currentQ.question,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 26.sp,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Options List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val optionLabelsBengali = listOf("ক", "খ", "গ", "ঘ")
                    val optionLabelsEnglish = listOf("A", "B", "C", "D")

                    currentQ.options.forEachIndexed { idx, optionText ->
                        val isEliminated = state.eliminatedOptionIndices.contains(idx)
                        val isSelected = state.selectedOptionIndex == idx
                        val isCorrectOption = idx == currentQ.correctOptionIndex

                        val cardBgColor = when {
                            state.isAnswerChecked && isCorrectOption && state.isCorrect -> CorrectGreen.copy(alpha = if (isDark) 0.25f else 0.15f)
                            state.isAnswerChecked && isSelected && !isCorrectOption -> WrongRed.copy(alpha = if (isDark) 0.25f else 0.15f)
                            isSelected -> if (isDark) Color(0xFF1E2145) else Color(0xFFF5F3FF)
                            isEliminated -> if (isDark) Color(0xFF0F172A).copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            else -> if (isDark) Color(0xFF131F33) else MaterialTheme.colorScheme.surface
                        }

                        val borderLineColor = when {
                            state.isAnswerChecked && isCorrectOption && state.isCorrect -> CorrectGreen
                            state.isAnswerChecked && isSelected && !isCorrectOption -> WrongRed
                            isSelected -> PrimaryIndigo
                            else -> if (isDark) Color(0xFF283952) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (state.isAnswerChecked && ((isCorrectOption && state.isCorrect) || (isSelected && !isCorrectOption))) 2.5.dp else if (isSelected) 2.dp else 1.dp,
                                    color = borderLineColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = !state.isAnswerChecked && !isEliminated) { onSelectOption(idx) }
                                .testTag("option_item_$idx")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 15.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isEliminated -> if (isDark) Color(0xFF1E293B).copy(alpha = 0.3f) else Color(0xFFE2E8F0).copy(alpha = 0.5f)
                                                state.isAnswerChecked && isCorrectOption && state.isCorrect -> CorrectGreen
                                                state.isAnswerChecked && isSelected && !isCorrectOption -> WrongRed
                                                isSelected -> PrimaryIndigo
                                                else -> if (isDark) Color(0xFF23324A) else Color(0xFFEDE9FE)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.isAnswerChecked && isCorrectOption && state.isCorrect) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Correct",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else if (state.isAnswerChecked && isSelected && !isCorrectOption) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Wrong",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = if (isBengali) optionLabelsBengali.getOrElse(idx) { "$idx" } else optionLabelsEnglish.getOrElse(idx) { "$idx" },
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                            color = if (isEliminated) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else if (isSelected) Color.White else if (isDark) Color(0xFFE2E8F0) else PrimaryIndigo
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = if (isEliminated) ("— বাতিলকৃত অপশন (৫০:৫০) —") else optionText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (state.isAnswerChecked && isCorrectOption && state.isCorrect) FontWeight.Bold else if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp
                                    ),
                                    color = if (isEliminated) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                if (state.isAnswerChecked && isCorrectOption && state.isCorrect) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CorrectGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "সঠিক",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CorrectGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else if (state.isAnswerChecked && isSelected && !isCorrectOption) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = WrongRed.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "ভুল",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = WrongRed,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // FULL SCREEN CELEBRATION / FEEDBACK ANIMATION OVERLAY (2+ SECONDS EXPERIENCE)
        if (state.isAnswerChecked) {
            ScreenCelebrationOverlay(
                isCorrect = state.isCorrect,
                isBengali = isBengali
            )
        }
    }
}

/**
 * High-energy gamified celebration / feedback animation overlay with minimum 2-second display
 */
@Composable
private fun ScreenCelebrationOverlay(
    isCorrect: Boolean,
    isBengali: Boolean
) {
    val scaleAnim = remember { Animatable(0.2f) }
    val alphaAnim = remember { Animatable(0f) }
    val shakeAnim = remember { Animatable(0f) }
    val timerProgress = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    LaunchedEffect(Unit) {
        // Entrance bounce
        alphaAnim.animateTo(1f, tween(180))
        scaleAnim.animateTo(
            targetValue = 1.08f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        scaleAnim.animateTo(1.0f, tween(120))

        // Shake if wrong
        if (!isCorrect) {
            repeat(3) {
                shakeAnim.animateTo(14f, tween(45))
                shakeAnim.animateTo(-14f, tween(45))
            }
            shakeAnim.animateTo(0f, tween(40))
        }
    }

    // 2.1 second countdown progress bar filling smoothly
    LaunchedEffect(Unit) {
        timerProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2050, easing = LinearEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (isCorrect) 0.45f else 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        if (isCorrect) {
            // High energy particles with multiple colors and shapes
            ConfettiParticlesCanvas()
        }

        // Floating Status Card with glow and shake
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = if (isCorrect) Color(0xFF064E3B) else Color(0xFF450A0A),
            border = BorderStroke(
                width = 3.dp,
                color = if (isCorrect) CorrectGreen.copy(alpha = 0.9f) else WrongRed.copy(alpha = 0.9f)
            ),
            shadowElevation = 24.dp,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(0.92f)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                    translationX = shakeAnim.value
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pulsing Icon Badge with outer glow ring
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Outer pulsing halo
                    Box(
                        modifier = Modifier
                            .size(86.dp * pulseGlow)
                            .clip(CircleShape)
                            .background(
                                (if (isCorrect) CorrectGreen else WrongRed).copy(alpha = 0.2f)
                            )
                    )

                    // Inner badge
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCorrect) CorrectGreen.copy(alpha = 0.35f) else WrongRed.copy(alpha = 0.35f)
                            )
                            .border(
                                width = 2.dp,
                                color = if (isCorrect) CorrectGreen else WrongRed,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = "Status",
                            tint = if (isCorrect) Color(0xFF34D399) else Color(0xFFF87171),
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                // Title Banner
                Text(
                    text = if (isCorrect) {
                        "🎉 চমৎকার! একদম সঠিক উত্তর!"
                    } else {
                        "❌ ভুল উত্তর!"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Detailed feedback / reward badge
                if (isCorrect) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF047857),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "XP",
                                tint = ExamGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "+২৫ XP যোগ হয়েছে • দারুণ গতি!",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Encouraging message without revealing correct answer
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF7F1D1D).copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "হাল ছেড়ো না • পরবর্তী প্রশ্নে চেষ্টা চালিয়ে যাও!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Bottom 2-Second Countdown Progress Tracker
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "পরবর্তী প্রশ্ন লোড হচ্ছে...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    LinearProgressIndicator(
                        progress = { timerProgress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isCorrect) Color(0xFF34D399) else Color(0xFFF87171),
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

/**
 * Dynamic multi-color confetti particle bursts with physics
 */
@Composable
private fun ConfettiParticlesCanvas() {
    val particles = remember {
        List(55) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.7f,
                radius = Random.nextFloat() * 8f + 4f,
                rotation = Random.nextFloat() * 360f,
                isStar = Random.nextBoolean(),
                color = when (Random.nextInt(6)) {
                    0 -> Color(0xFF10B981) // Emerald
                    1 -> Color(0xFFF59E0B) // Gold
                    2 -> Color(0xFF6366F1) // Indigo
                    3 -> Color(0xFFEC4899) // Pink
                    4 -> Color(0xFF06B6D4) // Cyan
                    else -> Color(0xFFFFFFFF) // White Sparkle
                }
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti_fall")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetY"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val curY = (p.y * size.height + offsetY * 1.5f) % size.height
            drawCircle(
                color = p.color,
                radius = p.radius,
                center = Offset(p.x * size.width, curY)
            )
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val rotation: Float,
    val isStar: Boolean,
    val color: Color
)
