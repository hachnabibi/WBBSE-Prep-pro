package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Interstitial Ad Dialog:
 * Shown at natural transition points (e.g. Mock test submission, completing flashcard decks,
 * or leaving battle arena). Students can skip after 3 seconds, keeping the experience
 * completely respectful and compliant with AdMob policies.
 */
@Composable
fun InterstitialAdDialog(
    isBengali: Boolean,
    onDismiss: () -> Unit
) {
    var timerSeconds by remember { mutableIntStateOf(3) }
    val canSkip = timerSeconds <= 0

    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            delay(1000L)
            timerSeconds--
        }
    }

    val adCampaigns = remember {
        listOf(
            Triple(
                "পশ্চিমবঙ্গ মেধা ও স্কলারশিপ পোর্টাল ২০২৬",
                "SVMCM ও ঐক্যশ্রী স্কলারশিপের আবেদনের সমস্ত তথ্য ও গাইড এক ক্লিকে পান।",
                "https://wbmdfc.org"
            ),
            Triple(
                "WBJEE & উচ্চ মাধ্যমিক কম্বো প্র্যাকটিস টেস্ট",
                "বিজ্ঞান ও কলা বিভাগের ছাত্রছাত্রীদের জন্য সেরা মক টেস্ট এবং অনলাইন সমাধান।",
                "https://wbjeeb.nic.in"
            ),
            Triple(
                "মাধ্যমিক বিষয়ভিত্তিক ফর্মুলা ও চিট শিট",
                "ভৌতবিজ্ঞান, গণিত ও ভূগোলের সম্পূর্ণ ফর্মুলা বুকলেট বিনামূল্যে ডাউনলোড করুন।",
                "https://wbbse.wb.gov.in"
            )
        )
    }
    val selectedCampaign = remember { adCampaigns.random() }

    Dialog(
        onDismissRequest = {
            if (canSkip) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = canSkip,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .testTag("interstitial_ad_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header: Ad badge & Skip button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ExamGold)
                            )
                            Text(
                                text = "বিজ্ঞাপন / Sponsored",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    // Skip button / countdown
                    Button(
                        onClick = {
                            if (canSkip) onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canSkip) Color(0xFF334155) else Color(0xFF1E293B).copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("interstitial_skip_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (canSkip) {
                                Text(
                                    text = "বাদ দিন ✕",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "$timerSeconds সেকেণ্ডে বাদ দেওয়া যাবে",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }

                // Middle: Visual Presentation & Content
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(PrimaryIndigo, Color(0xFF06B6D4))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Text(
                            text = selectedCampaign.first,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = selectedCampaign.second,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = CorrectGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "যাচাইকৃত পার্টনার",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                }

                                Text(
                                    text = "Google AdMob",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                // Bottom Call To Action
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            // User clicked Learn More
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF06B6D4)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("interstitial_cta_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "বিস্তারিত জানুন (Learn More)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (canSkip) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "অ্যাপে ফিরে যান",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
