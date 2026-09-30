package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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

data class SchoolSquadMember(
    val rank: Int,
    val studentName: String,
    val schoolName: String,
    val xp: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySquadsScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val squadMembers = remember {
        listOf(
            SchoolSquadMember(1, "হচনা বিবি", "কাকদ্বীপ বহুমুখী উচ্চ বিদ্যালয়", 2450),
            SchoolSquadMember(2, "অরিত্র মুখার্জি", "সাউথ পয়েন্ট হাই স্কুল, কলকাতা", 2320),
            SchoolSquadMember(3, "সায়ন্তন ঘোষ", "হাওড়া জেলা স্কুল", 2150),
            SchoolSquadMember(4, "প্রিয়া মণ্ডল", "বেথুন স্কুল", 1980),
            SchoolSquadMember(5, "শুভদীপ কর্মকার", "বটানিক্যাল গার্ডেন হাই স্কুল", 1850)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "স্কুল স্টাডি স্কোয়াড ও লিডারবোর্ড",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "পশ্চিমবঙ্গের পরীক্ষার্থীদের সাথে র‍্যাংক তুলনা",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("squads_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D9488)),
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
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White)
                    }
                    Column {
                        Text(
                            text = "বোর্ড এক্সাম স্কোয়াড",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "আপনার স্কোয়াড র‍্যাংক: #১ (শীর্ষ ১০% পরীক্ষার্থী)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Text(
                text = "টপ পারফর্মার লিডারবোর্ড:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(squadMembers) { member ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (member.rank == 1) ExamGold else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().testTag("squad_member_${member.rank}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (member.rank == 1) ExamGold else PrimaryIndigo.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${member.rank}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                    color = if (member.rank == 1) Color(0xFF1E1B2E) else PrimaryIndigoLight
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.studentName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = member.schoolName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ExamGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${member.xp} XP",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
