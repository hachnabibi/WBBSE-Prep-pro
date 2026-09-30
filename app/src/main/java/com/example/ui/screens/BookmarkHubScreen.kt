package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class BookmarkItem(
    val id: Int,
    val title: String,
    val category: String,
    val contentSnippet: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkHubScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bookmarks by remember {
        mutableStateOf(
            listOf(
                BookmarkItem(1, "বয়েলের সূত্র ও চার্লসের সূত্রের সমন্বয় রূপ: PV = nRT", "ভৌতবিজ্ঞান", "চাপ, আয়তন এবং পরম তাপমাত্রার সমন্বিত সমীকরণ..."),
                BookmarkItem(2, "মহাবিদ্রোহের (1857) প্রধান কেন্দ্রগুলি ও নেতৃত্ব", "ইতিহাস", "দিল্লি: বাহাদুর শাহ জাফর, ঝাঁসি: ঝাঁসির রানি লক্ষ্মীবাই..."),
                BookmarkItem(3, "মাধ্যমিক গণিত: লম্ব বৃত্তাকার চোঙের আয়তন ও বক্রতলের ক্ষেত্রফল সূত্র", "গণিত", "আয়তন = πr²h, বক্রতলের ক্ষেত্রফল = 2πrh"),
                BookmarkItem(4, "কোষ বিভাজন: মায়োসিস মাইটোসিসের পার্থক্য", "জীবনবিজ্ঞান", "মায়োসিস হ্রাজন বিভাজন এবং মাইটোসিস সমবিভাজন...")
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ExamGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF78350F))
                    }
                    Column {
                        Text(
                            text = "সংরক্ষিত ফর্মুলা ও নোটস",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF78350F)
                        )
                        Text(
                            text = "পরীক্ষার আগের রাতে দ্রুত রিভিশন দেওয়ার জন্য সেরা কালেকশন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF78350F).copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Text(
                text = "আপনার বুকমার্ক লিস্ট (${bookmarks.size}টি আইটেম):",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(bookmarks) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().testTag("bookmark_item_${item.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryIndigoLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    bookmarks = bookmarks.filter { it.id != item.id }
                                }) {
                                    Icon(Icons.Default.BookmarkAdded, contentDescription = "Remove", tint = ExamGold)
                                }
                            }
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.contentSnippet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
