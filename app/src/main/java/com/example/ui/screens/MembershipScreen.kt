package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.DeshExamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default Yearly (1)

    val isDark = isSystemInDarkTheme()
    val cyanGlow = Color(0xFF06B6D4)
    val purpleGlow = Color(0xFF8B5CF6)
    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = {
            // Sticky Bottom CTA Bar pinned at the bottom
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            actionMessage = if (selectedTabIndex == 1) {
                                "👑 বার্ষিক প্রো পাস মাত্র ₹৪৯৯-এ সফলভাবে অ্যাক্টিভ হয়েছে!"
                            } else {
                                "🚀 মাসিক প্রো পাস মাত্র ₹৯৯-এ সফলভাবে অ্যাক্টিভ হয়েছে!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(
                                brush = Brush.horizontalGradient(listOf(cyanGlow, purpleGlow)),
                                shape = RoundedCornerShape(50.dp)
                            )
                    ) {
                        Text(
                            text = if (selectedTabIndex == 1) "বার্ষিক প্রো নিন মাত্র ₹৪৯৯" else "মাসিক প্রো নিন মাত্র ₹৯৯",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }

                    Text(
                        text = if (selectedTabIndex == 1) {
                            "বাতিল না করা পর্যন্ত প্রতি বছর ₹৪৯৯ স্বয়ংক্রিয়ভাবে রিনিউ হবে। ইউপিআই, কার্ড এবং নেটব্যাংকিং গ্রহণ করা হয়।"
                        } else {
                            "বাতিল না করা পর্যন্ত প্রতি মাসে ₹৯৯ স্বয়ংক্রিয়ভাবে রিনিউ হবে। ইউপিআই, কার্ড এবং নেটব্যাংকিং গ্রহণ করা হয়।"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Bar: Close & Restore
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50.dp))
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    TextButton(
                        onClick = { actionMessage = "🔄 সাবস্ক্রিপশন সফলভাবে রিস্টোর করা হয়েছে!" },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50.dp))
                            .height(40.dp)
                    ) {
                        Text("রিস্টোর", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Status message
                actionMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CorrectGreen.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, CorrectGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CorrectGreen)
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { actionMessage = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Title & Subtitle
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "প্রো ভার্সনে আপডেট করুন",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "আপনার পড়াশোনাকে করুন আরও স্মার্ট ও সফল",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Segmented Toggle (Monthly vs Yearly)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Monthly Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (selectedTabIndex == 0) {
                                    Brush.horizontalGradient(listOf(PrimaryIndigo, Color(0xFF3B82F6)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable { selectedTabIndex = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "মাসিক পাস",
                            color = if (selectedTabIndex == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Yearly Tab
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (selectedTabIndex == 1) {
                                    Brush.horizontalGradient(listOf(cyanGlow, purpleGlow))
                                } else {
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable { selectedTabIndex = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বার্ষিক পাস",
                                color = if (selectedTabIndex == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selectedTabIndex == 1) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "৫৮% সাশ্রয়",
                                    color = if (selectedTabIndex == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Plan Cards with Glowing Gradient Borders & Comprehensive Bengali Features
                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ProPlanTransition",
                    modifier = Modifier.fillMaxWidth()
                ) { targetTab ->
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = if (targetTab == 1) listOf(cyanGlow, purpleGlow) else listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (targetTab == 1) {
                                        Text(
                                            text = "₹৪৯৯ / বছর",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 26.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "₹২,৯৮৮/yr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                            Text(
                                                text = "কার্যকরী মূল্য মাত্র ₹৪১.৫০/মাস",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = cyanGlow
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "₹৯৯ / মাস",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 26.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "₹২৪৯/mo",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                            Text(
                                                text = "৬০% ছাড়",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = CorrectGreen
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = if (targetTab == 1) Brush.horizontalGradient(listOf(cyanGlow, purpleGlow)) else Brush.horizontalGradient(listOf(CorrectGreen, CorrectGreen)),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (targetTab == 1) "৮৩% সাশ্রয় (BEST VALUE)" else "৬০% ছাড় (SPECIAL)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ProFeatureRow("সম্পূর্ণ বিজ্ঞাপনমুক্ত অভিজ্ঞতা (100% Ad-Free)")
                                ProFeatureRow("আনলিমিটেড এআই ডাউট সলভার ও স্ন্যাপ সলভার")
                                ProFeatureRow("সমস্ত লাইভ মক টেস্ট ও অল-বাংলা মক ব্যাটল")
                                ProFeatureRow("এক্সক্লুসিভ টপার্স নোটস ও পিডিএফ সলিউশন ডাউনলোড")
                                ProFeatureRow("অধ্যায়ভিত্তিক স্মার্ট ফ্ল্যাশকার্ড ও অডিও পডকাস্ট এক্সেস")
                                ProFeatureRow("হাই-স্পিড ডাউনলোড ও অফলাইন স্টাডি মোড")
                                ProFeatureRow("ফুল-লেংথ বোর্ড এক্সাম পেপার্স ও সাজেশন")
                                ProFeatureRow("২৪/৭ প্রায়োরিটি ডেডিকেটেড সাপোর্ট")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProFeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = Color(0xFF06B6D4).copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(13.dp))
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
