package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Campaign
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
import com.example.ui.theme.ExamGold
import com.example.ui.theme.PrimaryIndigo

@Composable
fun AdBannerCard(
    isBengali: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, Color(0xFF67E8F9).copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("inline_ad_banner_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                        .size(38.dp)
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
                        contentDescription = "Ad",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ExamGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "বিজ্ঞাপন",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ExamGold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "পশ্চিমবঙ্গ স্কলারশিপ ও স্টুডেন্ট ক্রেডিট",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "সকল শ্রেণীর জন্য আবেদন চলছে • বিস্তারিত দেখুন",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open",
                tint = Color(0xFF67E8F9),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
