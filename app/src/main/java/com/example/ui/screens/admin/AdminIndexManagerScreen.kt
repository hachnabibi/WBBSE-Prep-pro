package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ট্যাব ৮: ফায়ারস্টোর ইনডেক্স ম্যানেজার (Firestore Index Manager)
 */
@Composable
fun AdminIndexManagerScreen(
    isBengali: Boolean = true,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    val indexItems = remember {
        listOf(
            FirestoreIndexItem(
                id = 1,
                collectionId = "test_templates",
                title = "১. মক টেস্ট ও অধ্যায় মূল্যায়ন ইনডেক্স",
                description = "বিষয়, অধ্যায় ও টপিকের হাইরার্কিক্যাল মক টেস্ট ও চ্যাপ্টার অ্যাসেসমেন্ট দ্রুত ফেচ করার জন্য।",
                fields = listOf(
                    "classId" to "Ascending",
                    "subjectId" to "Ascending",
                    "chapterId" to "Ascending",
                    "published" to "Ascending"
                )
            ),
            FirestoreIndexItem(
                id = 2,
                collectionId = "test_templates",
                title = "২. কাস্টম মক টেস্ট ও টাইপ ইনডেক্স",
                description = "মক টেস্ট হাবে বিভিন্ন ফিল্টারের টেস্ট টাইপ স্পিডআপ করার জন্য।",
                fields = listOf(
                    "classId" to "Ascending",
                    "testType" to "Ascending",
                    "published" to "Ascending"
                )
            ),
            FirestoreIndexItem(
                id = 3,
                collectionId = "questions",
                title = "৩. অধ্যায়ভিত্তিক প্রশ্ন ব্যাংক ইনডেক্স",
                description = "নির্দিষ্ট ক্লাস, সাবজেক্ট ও চ্যাপ্টারের প্রশ্নমালার স্পিড ফিল্টারিং।",
                fields = listOf(
                    "classId" to "Ascending",
                    "subjectId" to "Ascending",
                    "chapterId" to "Ascending"
                )
            ),
            FirestoreIndexItem(
                id = 4,
                collectionId = "questions",
                title = "৪. বিগত বছরের প্রশ্নপত্র",
                description = "সাল অনুযায়ী (PYQ 2020-2025) দ্রুত ফিল্টার ও সর্টিং করার জন্য।",
                fields = listOf(
                    "classId" to "Ascending",
                    "subjectId" to "Ascending",
                    "pyqYear" to "Descending"
                )
            ),
            FirestoreIndexItem(
                id = 5,
                collectionId = "test_attempts",
                title = "৫. শিক্ষার্থীর পরীক্ষার ইতিহাস ও স্কোর ইনডেক্স",
                description = "শিক্ষার্থীর পরীক্ষার রেজাল্ট ও এনালাইসিস দ্রুত দেখানোর জন্য।",
                fields = listOf(
                    "userId" to "Ascending",
                    "submittedAt" to "Descending"
                )
            ),
            FirestoreIndexItem(
                id = 6,
                collectionId = "leaderboard",
                title = "৬. রিয়েল-টাইম র্যাঙ্কিং ও লিডারবোর্ড ইনডেক্স",
                description = "ক্লাসভিত্তিক স্কোর ও XP পয়েন্টের ওপর লিডারবোর্ড র্যাঙ্ক সাজাতে।",
                fields = listOf(
                    "classId" to "Ascending",
                    "totalXp" to "Descending"
                )
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "⚡ ফায়ারস্টোর ১-ক্লিক ইনডেক্স ম্যানেজার" else "⚡ Firestore 1-Click Index Manager",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (isBengali)
                            "নিচের প্রতিটি ইনডেক্সে ক্লিক করলে ফায়ারবেস কনসোলে সরাসরি ১-ক্লিকে ইনডেক্স পেজ তৈরি হয়ে যাবে। পছন্দসই বাটন চাপুন!"
                        else
                            "Clicking any index below will directly open Firebase Console to configure composite indexes in 1 click.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(indexItems) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Collection ID: ${item.collectionId}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            item.fields.forEach { (fieldName, direction) ->
                                Text(
                                    text = "• $fieldName  ➔  $direction",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            uriHandler.openUri("https://console.firebase.google.com/u/0/project/_/firestore/indexes")
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "🔥 1-Click Create Index in Firebase" else "🔥 1-Click Create Index in Firebase",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private data class FirestoreIndexItem(
    val id: Int,
    val collectionId: String,
    val title: String,
    val description: String,
    val fields: List<Pair<String, String>>
)
