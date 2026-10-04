package com.example.ui.screens

import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.example.util.AdMobManager
import kotlinx.coroutines.delay
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AdBannerCard
import com.example.ui.components.AdRewardType
import com.example.ui.components.ConfettiAnimationOverlay
import com.example.ui.components.InterstitialAdDialog
import com.example.ui.components.RewardedAdDialog
import com.example.ui.theme.*

private enum class QuestionFilter {
    ALL,
    CORRECT,
    INCORRECT
}

@Composable
fun ResultAnalysisScreen(
    result: AssessmentResult,
    isBengali: Boolean,
    onRetryWeakArea: () -> Unit,
    onReturnHome: () -> Unit,
    onDoubleReward: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPassed = result.accuracyPercentage >= 50
    var selectedFilter by remember { mutableStateOf(QuestionFilter.ALL) }
    var expandedQuestionIndices by remember { mutableStateOf(setOf<Int>()) }
    var isDoubleClaimed by remember { mutableStateOf(false) }
    var showRewardedAd by remember { mutableStateOf(false) }
    var showFallbackInterstitial by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        if (activity != null) {
            var attempts = 0
            while (attempts < 6 && !AdMobManager.hasInterstitialAd()) {
                delay(300L)
                attempts++
            }
            val shown = AdMobManager.showInterstitialAd(activity) {}
            if (!shown) {
                showFallbackInterstitial = true
            }
        }
    }

    // Average time per question calculation
    val avgTimePerQ = if (result.totalQuestions > 0) {
        result.timeSpentSeconds / result.totalQuestions
    } else 0

    val minutes = result.timeSpentSeconds / 60
    val seconds = result.timeSpentSeconds % 60
    val formattedTotalTime = String.format("%02d:%02d", minutes, seconds)

    // Filtered answers
    val filteredAnswerIndices = remember(selectedFilter, result.answers) {
        result.answers.mapIndexedNotNull { index, ans ->
            when (selectedFilter) {
                QuestionFilter.ALL -> index
                QuestionFilter.CORRECT -> if (ans.isCorrect) index else null
                QuestionFilter.INCORRECT -> if (!ans.isCorrect) index else null
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onReturnHome,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("result_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ফিরে যান",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "পারফরম্যান্স ও ফলাফল বিশ্লেষণ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = result.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (result.incorrectCount > 0) {
                            OutlinedButton(
                                onClick = onRetryWeakArea,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = WrongRed
                                ),
                                border = BorderStroke(1.5.dp, WrongRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("retry_mistakes_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Retry Mistakes",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ভুল সংশোধন (${result.incorrectCount})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = onReturnHome,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("result_home_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "হোমে ফিরে যান",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("result_analysis_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hero Score & Performance Summary Card
                item {
                    ScoreSummaryHeroCard(
                        result = result,
                        formattedTotalTime = formattedTotalTime,
                        avgTimePerQ = avgTimePerQ,
                        isBengali = isBengali
                    )
                }

                // 1.1 Rewarded Ad: 2X XP & Bonus Multiplier
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDoubleClaimed) Color(0xFF0F172A) else Color(0xFF1E1B4B)
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isDoubleClaimed) CorrectGreen else ExamGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rewarded_2x_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDoubleClaimed) CorrectGreen.copy(alpha = 0.2f)
                                                else ExamGold.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isDoubleClaimed) Icons.Default.CheckCircle else Icons.Default.Stars,
                                            contentDescription = null,
                                            tint = if (isDoubleClaimed) CorrectGreen else ExamGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = if (isDoubleClaimed)
                                                ("২X বোনাস সক্রিয় হয়েছে! 🎉")
                                            else
                                                ("ভিডিও দেখে স্কোর দ্বিগুণ করুন!"),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (isDoubleClaimed)
                                                ("+${result.xpEarned} অতিরিক্ত XP ও কয়েন যোগ হয়েছে")
                                            else
                                                ("ছোট স্পনসর বিজ্ঞাপন দেখে +${result.xpEarned} XP জিতে নিন"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                }
                            }

                            if (!isDoubleClaimed) {
                                Button(
                                    onClick = { showRewardedAd = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("watch_2x_ad_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "বিজ্ঞাপন দেখুন এবং ২X XP নিন 🎁",
                                        color = Color.Black,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }

                // 1.2 Sponsored Native Banner
                item {
                    AdBannerCard(isBengali = isBengali)
                }

                // 2. Level Up / Badge Achievement Banner
                if (result.levelUp) {
                    item {
                        LevelUpCard(result = result, isBengali = isBengali)
                    }
                }

                // 2.1 Newly Unlocked Badges Banner
                if (result.newlyUnlockedBadges.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = ExamGold.copy(alpha = 0.12f)),
                            border = BorderStroke(1.5.dp, ExamGold.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "নতুন মেডেল অর্জিত হয়েছে! 🎖️",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                result.newlyUnlockedBadges.forEach { badge ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(listOf(Color(0xFFFFFBEB), ExamGold))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EmojiEvents,
                                                    contentDescription = null,
                                                    tint = Color(0xFF92400E),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = badge.name,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = badge.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = ExamGold.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "+${badge.xpReward} XP",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = ExamGold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Category-Wise Performance Analysis Section
                if (result.categoryBreakdowns.isNotEmpty()) {
                    item {
                        SectionTitle(
                            title = "বিষয় ও অধ্যায়ভিত্তিক বিশ্লেষণ",
                            subtitle = "প্রতিটি বিভাগে অর্জিত স্কোর ও দক্ষতার মূল্যায়ন",
                            icon = Icons.Default.Category
                        )
                    }

                    itemsIndexed(result.categoryBreakdowns) { _, cat ->
                        CategoryPerformanceCard(
                            category = cat,
                            isBengali = isBengali
                        )
                    }
                }

                // 4. Difficulty Level Performance Matrix
                if (result.difficultyBreakdowns.isNotEmpty()) {
                    item {
                        DifficultyBreakdownSection(
                            difficulties = result.difficultyBreakdowns,
                            isBengali = isBengali
                        )
                    }
                }

                // 5. Weak Area Recommendations Callout
                if (result.incorrectCount > 0) {
                    item {
                        WeakAreaRecommendationCard(
                            result = result,
                            isBengali = isBengali,
                            onRetryWeakArea = onRetryWeakArea
                        )
                    }
                }

                // 6. Detailed Question-by-Question Solution Section Header & Filter Tabs
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle(
                            title = "প্রশ্ন ও সমাধান বিশ্লেষণ",
                            subtitle = "প্রতিটি প্রশ্নের নির্ভুল সমাধান ও বিস্তারিত ব্যাখ্যা",
                            icon = Icons.Default.Quiz
                        )

                        // Filter Chips Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedFilter == QuestionFilter.ALL,
                                onClick = { selectedFilter = QuestionFilter.ALL },
                                label = {
                                    Text(
                                        text = "সব প্রশ্ন (${result.totalQuestions})",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                modifier = Modifier.testTag("filter_all_questions")
                            )

                            FilterChip(
                                selected = selectedFilter == QuestionFilter.CORRECT,
                                onClick = { selectedFilter = QuestionFilter.CORRECT },
                                label = {
                                    Text(
                                        text = "সঠিক (${result.correctCount})",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = CorrectGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("filter_correct_questions")
                            )

                            FilterChip(
                                selected = selectedFilter == QuestionFilter.INCORRECT,
                                onClick = { selectedFilter = QuestionFilter.INCORRECT },
                                label = {
                                    Text(
                                        text = "ভুল (${result.incorrectCount})",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = WrongRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("filter_incorrect_questions")
                            )
                        }
                    }
                }

                // 7. Question Cards Review List
                if (filteredAnswerIndices.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "এই ফিল্টারে কোনো প্রশ্ন নেই",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(filteredAnswerIndices) { _, originalIndex ->
                        val ans = result.answers.getOrNull(originalIndex)
                        val question = result.questions.getOrNull(originalIndex)
                        val isExpanded = expandedQuestionIndices.contains(originalIndex)

                        if (ans != null) {
                            DetailedQuestionSolutionCard(
                                questionNumber = originalIndex + 1,
                                answer = ans,
                                question = question,
                                isBengali = isBengali,
                                isExpanded = isExpanded,
                                onToggleExpand = {
                                    expandedQuestionIndices = if (isExpanded) {
                                        expandedQuestionIndices - originalIndex
                                    } else {
                                        expandedQuestionIndices + originalIndex
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Confetti celebration overlay for passing
        ConfettiAnimationOverlay(
            visible = isPassed
        )

        if (showRewardedAd) {
            RewardedAdDialog(
                rewardType = AdRewardType.DOUBLE_XP,
                isBengali = isBengali,
                onRewardEarned = {
                    isDoubleClaimed = true
                    onDoubleReward?.invoke()
                },
                onDismiss = { showRewardedAd = false }
            )
        }

        if (showFallbackInterstitial) {
            InterstitialAdDialog(
                isBengali = isBengali,
                onDismiss = { showFallbackInterstitial = false }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Sub-Components
// -------------------------------------------------------------------------------------------------

@Composable
private fun ScoreSummaryHeroCard(
    result: AssessmentResult,
    formattedTotalTime: String,
    avgTimePerQ: Int,
    isBengali: Boolean
) {
    val isPassed = result.accuracyPercentage >= 50
    val gradeText = when {
        result.accuracyPercentage >= 90 -> "🏆 অসামান্য কৃতিত্ব! (গ্রেড A+)"
        result.accuracyPercentage >= 75 -> "🌟 চমৎকার পারফরম্যান্স! (গ্রেড A)"
        result.accuracyPercentage >= 60 -> "👍 ভালো ফলাফল (গ্রেড B)"
        result.accuracyPercentage >= 40 -> "📈 উন্নতির সুযোগ আছে (গ্রেড C)"
        else -> "💪 আরও অনুশীলন প্রয়োজন (গ্রেড D)"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("score_summary_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        if (isPassed) {
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        } else {
                            listOf(Color(0xFF3B1818), Color(0xFF1F1111))
                        }
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Performance Title Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isPassed) ExamGold.copy(alpha = 0.2f) else WrongRed.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (isPassed) ExamGold.copy(alpha = 0.5f) else WrongRed.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = gradeText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isPassed) ExamGold else Color(0xFFFCA5A5),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Big Score Metric
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${result.totalScore} / ${result.maxScore}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "অর্জিত মোট স্কোর (Marks Scored)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                // 4-Grid Key Performance Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricBox(
                        title = "সঠিকতা",
                        value = "${result.accuracyPercentage}%",
                        valueColor = if (isPassed) CorrectGreen else WrongRed,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "সময়",
                        value = formattedTotalTime,
                        subValue = "${avgTimePerQ}s/Q",
                        valueColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "প্রশ্ন বণ্টন",
                        value = "✓${result.correctCount}  ✗${result.incorrectCount}",
                        valueColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "অর্জিত XP",
                        value = "+${result.xpEarned}",
                        valueColor = ExamGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    subValue: String? = null,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = valueColor,
            maxLines = 1
        )
        if (subValue != null) {
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CategoryPerformanceCard(
    category: CategoryPerformance,
    isBengali: Boolean
) {
    val statusColor = when (category.status) {
        PerformanceStatus.STRONG -> CorrectGreen
        PerformanceStatus.MODERATE -> ExamGold
        PerformanceStatus.WEAK -> WrongRed
    }

    val statusBadge = when (category.status) {
        PerformanceStatus.STRONG -> "সবল ক্ষেত্র (Strong)"
        PerformanceStatus.MODERATE -> "সন্তোষজনক (Moderate)"
        PerformanceStatus.WEAK -> "পুনরাবৃত্তি প্রয়োজন (Weak)"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = category.categoryName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = statusBadge,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Accuracy progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { (category.accuracyPercentage / 100f).coerceIn(0f, 1f) },
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBengali)
                            "${category.correctCount}/${category.totalQuestions} প্রশ্ন সঠিক"
                        else
                            "${category.correctCount}/${category.totalQuestions} Correct",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (isBengali)
                            "স্কোর: ${category.scoredMarks}/${category.totalMarks} (${category.accuracyPercentage}%)"
                        else
                            "Score: ${category.scoredMarks}/${category.totalMarks} (${category.accuracyPercentage}%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyBreakdownSection(
    difficulties: List<DifficultyPerformance>,
    isBengali: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "কঠিনতার স্তর অনুযায়ী ফলাফল",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                difficulties.forEach { diff ->
                    val diffLabel = when (diff.difficulty) {
                        Difficulty.EASY -> "সহজ"
                        Difficulty.MEDIUM -> "মাঝারি"
                        Difficulty.HARD -> "কঠিন"
                    }

                    val diffColor = when (diff.difficulty) {
                        Difficulty.EASY -> CorrectGreen
                        Difficulty.MEDIUM -> ExamGold
                        Difficulty.HARD -> WrongRed
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, diffColor.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = diffLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = diffColor
                            )
                            Text(
                                text = "${diff.accuracyPercentage}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${diff.correctCount}/${diff.totalQuestions} Q",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeakAreaRecommendationCard(
    result: AssessmentResult,
    isBengali: Boolean,
    onRetryWeakArea: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WrongRed.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(WrongRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "AI Coach Suggestion",
                    tint = WrongRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI কোচ বিশ্লেষণ ও সুপারিশ",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isBengali)
                        "আপনার ${result.incorrectCount}টি প্রশ্নের উত্তর সঠিক হয়নি। ভুল সংশোধন সেশন করে এখনই ধারণা পরিষ্কার করে নিন।"
                    else
                        "You missed ${result.incorrectCount} questions. Quick revision helps retain concepts 3x faster.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailedQuestionSolutionCard(
    questionNumber: Int,
    answer: UserAnswer,
    question: QuestionItem?,
    isBengali: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val isCorrect = answer.isCorrect
    val statusColor = if (isCorrect) CorrectGreen else WrongRed

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            1.dp,
            if (isCorrect) CorrectGreen.copy(alpha = 0.3f) else WrongRed.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Question # + Correct/Incorrect Indicator + Expand Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = if (isCorrect) "Correct" else "Incorrect",
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "প্রশ্ন $questionNumber",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Time spent badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${answer.timeSpentSeconds}s",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Question text preview
            if (question != null) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Expandable Solution & Options details
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Options List
                    if (question != null) {
                        val options = question.options
                        options.forEachIndexed { optIdx, optText ->
                            val isSelectedByUser = answer.selectedOptionIndex == optIdx
                            val isOptionCorrect = question.correctOptionIndex == optIdx

                            val optBgColor = when {
                                isOptionCorrect -> CorrectGreen.copy(alpha = 0.12f)
                                isSelectedByUser && !isCorrect -> WrongRed.copy(alpha = 0.12f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }

                            val optBorderColor = when {
                                isOptionCorrect -> CorrectGreen
                                isSelectedByUser && !isCorrect -> WrongRed
                                else -> Color.Transparent
                            }

                            val optPrefix = when (optIdx) {
                                0 -> "A"
                                1 -> "B"
                                2 -> "C"
                                3 -> "D"
                                else -> "${optIdx + 1}"
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = optBgColor,
                                border = if (optBorderColor != Color.Transparent) BorderStroke(1.dp, optBorderColor) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$optPrefix.",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isOptionCorrect) CorrectGreen else if (isSelectedByUser) WrongRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = optText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isOptionCorrect) {
                                        Text(
                                            text = "সঠিক উত্তর",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CorrectGreen
                                        )
                                    } else if (isSelectedByUser) {
                                        Text(
                                            text = "আপনার উত্তর",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = WrongRed
                                        )
                                    }
                                }
                            }
                        }

                        // Explanation Callout
                        val explanation = question.explanation
                        if (explanation.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryIndigo.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Explanation",
                                            tint = PrimaryIndigo,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ব্যাখ্যা ও সমাধান:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryIndigo
                                        )
                                    }
                                    Text(
                                        text = explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelUpCard(
    result: AssessmentResult,
    isBengali: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ExamGold.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ExamGold.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Upgrade,
                    contentDescription = "Level Up",
                    tint = ExamGold,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "লেভেল আপ! লেভেল ${result.currentLevel} এ উন্নীত",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "নতুন র্যাঙ্ক ব্যাজ ও প্লাস বোনাস আনলক হয়েছে!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryIndigo,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
