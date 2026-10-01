package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PromoType {
    PYQ_DOWNLOAD,
    LIVE_MOCK_TEST,
    LIVE_QUIZ,
    SCHOLARSHIP
}

@Composable
fun CustomAppPromoCard(
    promoType: PromoType,
    isBengali: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = when (promoType) {
        PromoType.PYQ_DOWNLOAD -> listOf(
            "বিগত বছরের প্রশ্নপত্র",
            "বোর্ড পরীক্ষার সব বিষয়ের প্রশ্নপত্র ডাউনলোড করুন",
            Icons.Default.Download,
            listOf(Color(0xFF4F46E5), Color(0xFF818CF8))
        )
        PromoType.LIVE_MOCK_TEST -> listOf(
            "লাইভ মক টেস্ট দিন",
            "নিজের প্রস্তুতি যাচাই করুন মক টেস্ট দিয়ে",
            Icons.AutoMirrored.Filled.Assignment,
            listOf(Color(0xFF10B981), Color(0xFF34D399))
        )
        PromoType.LIVE_QUIZ -> listOf(
            "লাইভ কুইজ খেলুন",
            "বন্ধুদের সাথে কুইজ খেলে প্রাইজ জিতুন",
            Icons.Default.Quiz,
            listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
        )
        PromoType.SCHOLARSHIP -> listOf(
            "স্কলারশিপ জিতুন",
            "আমাদের লিডারবোর্ডে টপ করে আকর্ষণীয় প্রাইজ জিতুন",
            Icons.Default.EmojiEvents,
            listOf(Color(0xFFE11D48), Color(0xFFFB7185))
        )
    }

    @Suppress("UNCHECKED_CAST")
    val title = result[0] as String
    @Suppress("UNCHECKED_CAST")
    val subtitle = result[1] as String
    @Suppress("UNCHECKED_CAST")
    val icon = result[2] as ImageVector
    @Suppress("UNCHECKED_CAST")
    val colors = result[3] as List<Color>

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors[0].copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(colors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "প্রস্তুতি আরও মজবুত করুন",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors[0],
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                colors[0].copy(alpha = 0.1f), 
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go",
                tint = colors[0],
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
