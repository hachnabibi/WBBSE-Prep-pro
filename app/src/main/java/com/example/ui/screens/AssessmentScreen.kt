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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.components.AdBannerCard
import com.example.ui.components.AdRewardType
import com.example.ui.components.RealtimeCountdownTimer
import com.example.ui.components.RewardedAdDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssessmentUiState

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
    
    // হ্যাপটিক ফিডব্যাকের জন্য
    val haptic = LocalHapticFeedback.current

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

    // Premium Colors
    val screenBgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBgColorUi = if (isDark) Color(0xFF1E293B) else Color.White
    val borderColorUi = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val navyBlueBadge = Color(0xFF1E3A8A)

    var showExitConfirmation by remember { mutableStateOf(false) }
    var extraTimeSeconds by remember { mutableIntStateOf(0) }
    var isExtraTimeUsed by remember { mutableStateOf(false) }
    var isSkipUsed by remember { mutableStateOf(false) }
    val effectiveRemainingSeconds = (state.timeRemainingSeconds + extraTimeSeconds).coerceAtLeast(0)

    var showRewardedAdForFiftyFifty by remember { mutableStateOf(false) }
    var showRewardedAdForExtraTime by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        showExitConfirmation = true
    }

    val blurEffect by animateDpAsState(
        targetValue = if (state.isAnswerChecked || showExitConfirmation) 12.dp else 0.dp,
        label = "blur_effect"
    )

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
                        "এখন বেরিয়ে গেলে আপনার বর্তমান টেস্টের স্কোর সংরক্ষিত হবে না। আপনি কি নিশ্চিত?"
                    else
                        "If you leave now, your current test progress will be lost. Are you sure?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (isDark) Color.LightGray else Color.DarkGray
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showExitConfirmation = false },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, PrimaryIndigo)
                    ) {
                        Text(
                            text = "চালিয়ে যান",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryIndigo
                        )
                    }
                    Button(
                        onClick = {
                            showExitConfirmation = false
                            onExit()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "বের হন",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            },
            dismissButton = {},
            shape = RoundedCornerShape(20.dp),
            containerColor = cardBgColorUi
        )
    }

    // Rewarded Dialogs (Omitted standard details for brevity, assumed correctly handled in components)
    if (showRewardedAdForFiftyFifty) {
        RewardedAdDialog(rewardType = AdRewardType.FIFTY_FIFTY, isBengali = isBengali, onRewardEarned = { showRewardedAdForFiftyFifty = false; onUseFiftyFifty?.invoke() }, onDismiss = { showRewardedAdForFiftyFifty = false })
    }
    if (showRewardedAdForExtraTime) {
        RewardedAdDialog(rewardType = AdRewardType.EXTRA_TIME, isBengali = isBengali, onRewardEarned = { showRewardedAdForExtraTime = false; extraTimeSeconds += 60; isExtraTimeUsed = true; onUseExtraTime?.invoke() }, onDismiss = { showRewardedAdForExtraTime = false })
    }

    Box(modifier = modifier.fillMaxSize().background(screenBgColor).testTag("assessment_screen")) {
        Scaffold(
            modifier = Modifier.blur(blurEffect),
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = cardBgColorUi,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { showExitConfirmation = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Exit", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            }

                            RealtimeCountdownTimer(
                                totalSeconds = session.timeLimitSeconds + extraTimeSeconds,
                                remainingSeconds = effectiveRemainingSeconds,
                                isBengali = isBengali,
                                isCompact = true,
                                modifier = Modifier.height(36.dp).wrapContentWidth()
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF),
                                border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f)),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxHeight().padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "প্রশ্ন $currentNum/$totalQ",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                        color = if (isDark) Color(0xFFA5B4FC) else PrimaryIndigo
                                    )
                                }
                            }
                        }

                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = PrimaryIndigo, trackColor = borderColorUi
                        )
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = cardBgColorUi,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, borderColorUi),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (!state.isFiftyFiftyUsed) showRewardedAdForFiftyFifty = true 
                            },
                            enabled = !state.isFiftyFiftyUsed && !state.isAnswerChecked,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (state.isFiftyFiftyUsed) borderColorUi else ExamCyan),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (state.isFiftyFiftyUsed) Color.Transparent else ExamCyan.copy(alpha = 0.08f), contentColor = ExamCyan),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("৫০:৫০", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (!isExtraTimeUsed) showRewardedAdForExtraTime = true 
                            },
                            enabled = !isExtraTimeUsed,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isExtraTimeUsed) borderColorUi else ExamGold),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isExtraTimeUsed) Color.Transparent else ExamGold.copy(alpha = 0.08f), contentColor = ExamGold),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.MoreTime, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+৩০ সে.", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (!isSkipUsed) { isSkipUsed = true; onUseSkip?.invoke() }
                            },
                            enabled = !isSkipUsed && !state.isAnswerChecked,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSkipUsed) borderColorUi else ExamFlame),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSkipUsed) Color.Transparent else ExamFlame.copy(alpha = 0.08f), contentColor = ExamFlame),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBengali) "স্কিপ" else "Skip", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                AdBannerCard(isBengali = isBengali, modifier = Modifier.fillMaxWidth())

                // Premium Question Card with Gradient Background
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent), // Transparent to show gradient
                    border = BorderStroke(1.dp, borderColorUi),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = if (isDark) listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                    else listOf(Color.White, Color(0xFFF1F5F9))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        Text(
                            text = currentQ.question,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 30.sp,
                                fontSize = 19.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Options List with Animated Colors & Haptics
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val optionLabels = if (isBengali) listOf("ক", "খ", "গ", "ঘ") else listOf("A", "B", "C", "D")

                    currentQ.options.forEachIndexed { idx, optionText ->
                        val isEliminated = state.eliminatedOptionIndices.contains(idx)
                        val isSelected = state.selectedOptionIndex == idx
                        val isCorrectOption = idx == currentQ.correctOptionIndex

                        // Determine target colors
                        val targetItemBgColor = when {
                            state.isAnswerChecked && isCorrectOption -> CorrectGreen.copy(alpha = if (isDark) 0.25f else 0.1f)
                            state.isAnswerChecked && isSelected && !isCorrectOption -> WrongRed.copy(alpha = if (isDark) 0.25f else 0.1f)
                            isSelected -> if (isDark) Color(0xFF1E2145) else Color(0xFFEEF2FF)
                            isEliminated -> if (isDark) Color(0xFF0F172A).copy(alpha = 0.5f) else Color(0xFFF1F5F9)
                            else -> cardBgColorUi
                        }

                        val targetBorderColor = when {
                            state.isAnswerChecked && isCorrectOption -> CorrectGreen
                            state.isAnswerChecked && isSelected && !isCorrectOption -> WrongRed
                            isSelected -> PrimaryIndigo
                            else -> borderColorUi
                        }

                        // Smoothly animate colors
                        val animatedItemBgColor by animateColorAsState(
                            targetValue = targetItemBgColor,
                            animationSpec = tween(durationMillis = 300),
                            label = "bg_anim_$idx"
                        )
                        val animatedBorderColor by animateColorAsState(
                            targetValue = targetBorderColor,
                            animationSpec = tween(durationMillis = 300),
                            label = "border_anim_$idx"
                        )

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = animatedItemBgColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected && !state.isAnswerChecked) 2.dp else 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (state.isAnswerChecked && (isCorrectOption || isSelected)) 2.dp else if (isSelected) 1.5.dp else 1.dp,
                                    color = animatedBorderColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = !state.isAnswerChecked && !isEliminated) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Haptic on tap
                                    onSelectOption(idx)
                                    onCheckOrSubmit()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 52.dp)
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Navy Blue Badge
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isEliminated) navyBlueBadge.copy(alpha = 0.4f) else navyBlueBadge),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionLabels.getOrElse(idx) { "$idx" },
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                        color = if (isEliminated) Color.White.copy(alpha = 0.5f) else Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Option Text
                                Text(
                                    text = if (isEliminated) "— বাতিলকৃত অপশন —" else optionText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (state.isAnswerChecked && isCorrectOption) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 16.sp,
                                        lineHeight = 22.sp
                                    ),
                                    color = if (isEliminated) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                // Animated Icons on Right
                                AnimatedVisibility(
                                    visible = state.isAnswerChecked && (isCorrectOption || (isSelected && !isCorrectOption)),
                                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    if (isCorrectOption) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Correct", tint = CorrectGreen, modifier = Modifier.size(24.dp).padding(start = 6.dp))
                                    } else if (isSelected && !isCorrectOption) {
                                        Icon(Icons.Rounded.Cancel, contentDescription = "Wrong", tint = WrongRed, modifier = Modifier.size(24.dp).padding(start = 6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (state.isAnswerChecked) {
            val correctOptionText = currentQ.options.getOrNull(currentQ.correctOptionIndex) ?: ""
            QuizResultPopupCard(
                isCorrect = state.isCorrect,
                correctAnswerText = correctOptionText,
                isBengali = isBengali,
                onDismiss = onAdvanceNext
            )
        }
    }
}

@Composable
private fun QuizResultPopupCard(
    isCorrect: Boolean,
    correctAnswerText: String,
    isBengali: Boolean,
    onDismiss: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.4f) }
    val timerProgress = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        // Trigger haptic when result popup appears
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
    }

    LaunchedEffect(Unit) {
        timerProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
        )
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Rounded.Check else Icons.Rounded.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Text(
                        text = if (isCorrect) "চমৎকার!" else "ভুল উত্তর!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                        color = if (isCorrect) Color(0xFF059669) else Color(0xFFDC2626)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isCorrect) "সঠিক উত্তর" else "আবার চেষ্টা করো!",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isCorrect) "তুমি খুব ভালো করেছো!" else "হাল ছেড়ো না, তুমি পারবে!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }

                    if (isCorrect) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                Text("+25 XP", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = Color(0xFFB45309))
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(18.dp))
                                Text(
                                    text = "সঠিক উত্তর: $correctAnswerText",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                    color = Color(0xFFBE123C),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { timerProgress.value },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                            trackColor = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "পরের প্রশ্নে যাচ্ছি... ৫ সেকেন্ড",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCorrect) Color(0xFF10B981) else Color(0xFFE11D48),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "পরের প্রশ্ন →",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        )
                    }
                }
            }
        }
    }
}