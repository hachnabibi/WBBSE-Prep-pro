package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConceptDetailDialog(
    title: String,
    subtitle: String = "অধ্যায় কনসেপ্ট নোটস",
    conceptSummary: String,
    keyPointsList: List<String> = emptyList(),
    keyPointsRaw: String? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val amberGold = Color(0xFFFBBF24)

    val parsedKeyPoints = androidx.compose.runtime.remember(keyPointsList, keyPointsRaw) {
        if (keyPointsList.isNotEmpty()) {
            keyPointsList
        } else if (!keyPointsRaw.isNullOrBlank()) {
            keyPointsRaw.split("|", "\n", ";").map { it.trim() }.filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF0F172A) else Color.White,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .testTag("concept_detail_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFD97706), Color(0xFFB45309), Color(0xFF78350F))
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "Concept",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "💡 মূল কনসেপ্ট ও সারসংক্ষেপ",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_concept_dialog_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // 2. Scrollable Body Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Executive Summary Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFEF3C7)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "📌 সারসংক্ষেপ ও মূল ভাবনা",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E)
                                )
                            }
                            Text(
                                text = conceptSummary.ifBlank { "এই টপিকের মূল ধারণার বিস্তারিত কনসেপ্ট ফাইল শীঘ্রই যুক্ত করা হচ্ছে। গুরুত্বপূর্ণ পয়েন্টগুলো মুখস্থ রাখুন।" },
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E293B)
                            )
                        }
                    }

                    // Mind Map & Key Points Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "🧠 কুইক কনসেপ্ট ম্যাপ",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            val highlights = if (parsedKeyPoints.isNotEmpty()) parsedKeyPoints else listOf(
                                "১. মূল চরিত্র ও প্রসঙ্গের সঠিক উপলব্ধি এবং রচনার পেছনের মূল বার্তা।",
                                "২. পরীক্ষায় সরাসরি আসা সম্ভাব্য প্রশ্নের গুরুত্বপূর্ণ তথ্য ও সংজ্ঞাবলি।",
                                "৩. সহজ ভাষায় মনে রাখার বিশেষ নোটস ও ধারণার সারসংক্ষেপ।",
                                "৪. বিগত বছরের প্রশ্নপদ্ধতি (PYQ) অনুসরণে তৈরি সংক্ষিপ্ত পয়েন্ট।"
                            )

                            highlights.forEachIndexed { index, item ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isDark) Color(0xFF334155) else Color.White,
                                    border = BorderStroke(0.5.dp, Color(0xFF818CF8).copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📌 ${index + 1}. ",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF4F46E5)
                                        )
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Bottom Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD97706),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dismiss_concept_dialog_btn")
                    ) {
                        Text(
                            text = "বুঝেছি, ধন্যবাদ! 👍",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
