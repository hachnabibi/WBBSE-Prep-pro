package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateScreen(
    profile: StudentProfile,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDownloaded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Modern Certificate Card
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f) // Landscape certificate look
                    .testTag("certificate_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFDF6E3))
                            )
                        )
                        .padding(24.dp)
                ) {
                    // Decorative Borders
                    Box(modifier = Modifier.fillMaxSize().border(2.dp, ExamGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp)))

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = ExamGold, modifier = Modifier.size(24.dp))
                            Text(
                                text = "WBBSE • MADHYAMIK 2026",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
                                color = Color(0xFF78350F)
                            )
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = ExamGold, modifier = Modifier.size(24.dp))
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "কৃতিত্বের সনদ",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = Color(0xFF92400E))
                            )
                            Text(
                                text = "স্বীকৃতিপত্র প্রদান করা হচ্ছে:",
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                color = Color(0xFFB45309)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E1B2E)),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "মাধ্যমিক ২০২৬ মেগা মক টেস্টে অসাধারণ সাফল্যের জন্য।",
                                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                                color = Color(0xFF451A03)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "__________", color = Color(0xFFB45309))
                                Text(text = "পরীক্ষা নিয়ন্ত্রক", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF78350F))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "__________", color = Color(0xFFB45309))
                                Text(text = "অ্যাকাডেমি", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF78350F))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { isDownloaded = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFF1E1B2E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(ExamGold.copy(alpha = 0.9f), ExamGold)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .testTag("download_certificate_btn")
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDownloaded) ("✅ সার্টিফিকেট সেভ করা হয়েছে!") else ("ডাউনলোড ও শেয়ার করুন"),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
