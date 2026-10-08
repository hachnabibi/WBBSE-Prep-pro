package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiCoachInsight
import com.example.data.model.AssessmentMode
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.theme.*

@Composable
fun AiCoachScreen(
    insight: AiCoachInsight?,
    isBengali: Boolean,
    onStartTargetedPractice: (AssessmentMode, String?, String?) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val aiData = insight ?: AiCoachInsight()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_coach_screen")
    ) {
        ScreenHeaderBar(
            title = "এআই স্টাডি কোচ",
            subtitle = "ব্যক্তিগতকৃত দুর্বলতা বিশ্লেষণ ও নির্দেশনা",
            isBengali = isBengali,
            showBackButton = true,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // 1. AI Coach Persona Banner
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4338CA))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Coach",
                                    tint = ExamGold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "দেশএক্সাম এআই মেন্টর",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                                Text(
                                    text = "ব্যক্তিগত পারফরম্যান্স ডায়াগনস্টিক",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (isBengali) aiData.diagnosisBengali else aiData.diagnosisEnglish,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = Color.White,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Urgent Weak Area Focus Card
        item {
            val isDark = MaterialTheme.colorScheme.background.red < 0.45f
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) WrongRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, WrongRed.copy(alpha = if (isDark) 0.4f else 0.25f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = "Priority",
                                tint = WrongRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "অগ্রাধিকার ভিত্তিক দুর্বল ক্ষেত্র",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = WrongRed
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WrongRed.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "রিভিশন প্রয়োজন",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = WrongRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = aiData.topWeakTopic,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (isBengali) aiData.recommendedActionBengali else aiData.recommendedActionEnglish,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            onStartTargetedPractice(
                                AssessmentMode.WEAK_AREA,
                                null,
                                "লক্ষ্যভিত্তিক রিভিশন প্র্যাকটিস"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("ai_targeted_practice_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Practice",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "এখনই দুর্বল ক্ষেত্র প্র্যাকটিস করো (+১০০ XP)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // 3. Recommended 3-Step Daily Study Recipe
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "আজকের ৩-ধাপের সুপার রিভিশন ফর্মুলা",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    StudyStepItem(
                        number = "১",
                        title = "কনসেপ্ট রিক্যাপ (১৫ মিনিট)",
                        description = "বয়েল ও চালর্সের সূত্রের গাণিতিক রূপ ও লেখচিত্র রিভাইজ করুন।",
                        icon = Icons.AutoMirrored.Filled.MenuBook
                    )

                    StudyStepItem(
                        number = "২",
                        title = "ডেইলি ১০ স্পিড টেস্ট (১০ মিনিট)",
                        description = "বিভিন্ন বিষয়ের মিশ্রিত ১০টি প্রশ্নে সময় ব্যবস্থাপনা প্র্যাকটিস করুন।",
                        icon = Icons.Default.Bolt
                    )

                    StudyStepItem(
                        number = "৩",
                        title = "অধ্যায় বস ব্যাটল (৫ মিনিট)",
                        description = "টপিকের বসকে হারিয়ে চ্যাপ্টার মাস্টারি স্টার সংগ্রহ করুন।",
                        icon = Icons.Default.Shield
                    )
                }
            }
        }
    }
}
}

@Composable
fun StudyStepItem(
    number: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(PrimaryIndigo.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PrimaryIndigo
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
