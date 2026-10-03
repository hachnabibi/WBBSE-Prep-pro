package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChapterEntity
import com.example.data.local.TopicEntity
import com.example.data.model.*
import com.example.ui.components.HeroBanner
import com.example.ui.components.ScreenHeaderBar
import com.example.ui.components.VideoLessonPlayerDialog
import com.example.ui.theme.ExamGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    chapter: ChapterEntity,
    topics: List<TopicEntity>,
    subjectColorLong: Long = 0xFF2563EBL,
    isBengali: Boolean,
    onBack: () -> Unit,
    onSelectTopic: (String) -> Unit,
    onStartAssessment: (AssessmentMode, String, String) -> Unit,
    onViewContent: (String, String?, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: com.example.ui.viewmodel.DeshExamViewModel? = null
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val subjectColor = Color(subjectColorLong)
    var activeVideoLesson by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<VideoLesson?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeaderBar(
            title = if (isBengali) chapter.titleBengali else chapter.titleEnglish,
            subtitle = "অধ্যায় ${chapter.chapterNumber} - টপিক ও প্রশ্নোত্তর",
            isBengali = isBengali,
            showBackButton = true,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Chapter Hero Banner using HeroBanner component
            item {
                HeroBanner(
                    title = "অধ্যায় প্রস্তুতি ও অগ্রগতি",
                    subtitle = if (isBengali) chapter.titleBengali else chapter.titleEnglish,
                    masteryPercentage = chapter.masteryPercentage,
                    primaryColor = subjectColor,
                    bottomContent = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onViewContent(
                                        if (isBengali) chapter.titleBengali else chapter.titleEnglish,
                                        chapter.contentData,
                                        chapter.solutionData
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "কন্টেন্ট ও নোটস",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    try {
                                        val firstTopic = topics.firstOrNull()
                                        val topicId = firstTopic?.id ?: "chap_${chapter.id}"
                                        val title = firstTopic?.let { if (isBengali) it.titleBengali else it.titleEnglish } ?: (if (isBengali) chapter.titleBengali else chapter.titleEnglish)
                                        val lesson = VideoLessonProvider.getVideoForTopic(topicId, title, chapter.subjectId, firstTopic?.videoUrl, firstTopic?.playlistJson)
                                        if (viewModel != null) {
                                            viewModel.openVideoLessonForEntity(lesson, firstTopic)
                                        } else {
                                            activeVideoLesson = lesson
                                        }
                                    } catch (_: Exception) {
                                        val fallback = VideoLessonProvider.getVideoForTopic(chapter.id)
                                        if (viewModel != null) {
                                            viewModel.openVideoLessonForEntity(fallback, topics.firstOrNull())
                                        } else {
                                            activeVideoLesson = fallback
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444).copy(alpha = 0.35f),
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ভিডিও ক্লাস",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                )
            }

            // 2. CHAPTER CONTENT: TOPICS OR TESTS
            if (topics.isNotEmpty()) {
                // TOPICS EXIST FOR THIS CHAPTER
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📌 এই অধ্যায়ের টপিকসমূহ (${topics.size.toBengaliDigits()}টি টপিক)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "যেকোনো টপিকের ওপর ক্লিক করে নোটস, প্রশ্ন ও টেস্টসমূহ খুলুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                itemsIndexed(topics) { index, topic ->
                    val keyPointCount = remember(topic.keyPointsJson) {
                        if (topic.keyPointsJson.isBlank()) 0 else topic.keyPointsJson.split("|").filter { it.isNotBlank() }.size
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { onSelectTopic(topic.id) }
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
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(subjectColor, subjectColor.copy(alpha = 0.75f))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${chapter.chapterNumber.toBengaliDigits()}.${(index + 1).toBengaliDigits()}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isBengali) topic.titleBengali else topic.titleEnglish,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Open",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val summary = if (isBengali) topic.summaryBengali else topic.summaryEnglish
                            if (summary.isNotBlank()) {
                                Text(
                                    text = summary,
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = "💡 ${keyPointCount.toBengaliDigits()}টি পয়েন্ট",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quick Video Button
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFEF4444).copy(alpha = if (isDark) 0.25f else 0.12f),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .clickable {
                                                try {
                                                    val title = if (isBengali) topic.titleBengali else topic.titleEnglish
                                                    val lesson = VideoLessonProvider.getVideoForTopic(
                                                        topicId = topic.id,
                                                        topicTitle = title,
                                                        subjectId = chapter.subjectId,
                                                        topicVideoUrl = topic.videoUrl,
                                                        topicPlaylistJson = topic.playlistJson
                                                    )
                                                    if (viewModel != null) {
                                                        viewModel.openVideoLessonForEntity(lesson, topic)
                                                    } else {
                                                        activeVideoLesson = lesson
                                                    }
                                                } catch (_: Exception) {
                                                    val fallback = VideoLessonProvider.getVideoForTopic(topic.id)
                                                    if (viewModel != null) {
                                                        viewModel.openVideoLessonForEntity(fallback, topic)
                                                    } else {
                                                        activeVideoLesson = fallback
                                                    }
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = "ভিডিও ক্লাস",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "ভিডিও",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isDark) Color.White else Color(0xFFB91C1C)
                                            )
                                        }
                                    }

                                    // Study Button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                                )
                                            )
                                            .clickable { onSelectTopic(topic.id) }
                                            .padding(horizontal = 12.dp, vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Study",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "পড়ো ও টেস্ট",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // NO TOPICS EXIST -> SHOW AVAILABLE TESTS FOR CHAPTER
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⚡ এই অধ্যায়ের উপলব্ধ টেস্টসমূহ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "অধ্যায়ভিত্তিক জ্ঞান পরীক্ষা করতে নিচের টেস্টগুলো শুরু করো।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Test Item 1: Practice Quiz
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else subjectColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "অধ্যায়ভিত্তিক প্র্যাকটিস কুইজ",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "১০টি গুরুত্বপূর্ণ বোর্ড স্ট্যান্ডার্ড প্রশ্ন। কোনো সময়ের চাপ নেই।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                        )
                                    )
                                    .clickable {
                                        onStartAssessment(
                                            AssessmentMode.PRACTICE,
                                            chapter.id,
                                            "${chapter.titleBengali} - অনুশীলন টেস্ট"
                                        )
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "প্র্যাকটিস শুরু করো",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }


            }
        }
    }

    val currentLesson = activeVideoLesson
    if (currentLesson != null) {
        VideoLessonPlayerDialog(
            lesson = currentLesson,
            onDismiss = { activeVideoLesson = null },
            onStartQuiz = {
                val topicId = currentLesson.safeTopicId
                activeVideoLesson = null
                onStartAssessment(
                    AssessmentMode.PRACTICE,
                    topicId,
                    "ভিডিও অনুশীলন টেস্ট"
                )
            },
            viewModel = viewModel
        )
    }
}
