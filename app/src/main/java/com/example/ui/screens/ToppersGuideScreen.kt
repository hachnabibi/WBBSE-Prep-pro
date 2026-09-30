package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class TopperTipItem(
    val id: String,
    val title: String,
    val tag: String,
    val summary: String,
    val detailedGuidance: List<String>,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToppersGuideScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedId by remember { mutableStateOf<String?>("tip_1") }

    val guides = remember {
        listOf(
            TopperTipItem(
                id = "tip_1",
                title = "৩ ঘণ্টা ১৫ মিনিটের সেরা সময় বন্টন কৌশল",
                tag = "টাইম ম্যানেজমেন্ট",
                summary = "মাধ্যমিক পরীক্ষার প্রতিটি মিনিট কীভাবে ব্যবহার করবেন যাতে পরীক্ষার ১৫ মিনিট আগেই সব লেখা শেষ হয়।",
                detailedGuidance = listOf(
                    "• প্রথম ১৫ মিনিট (প্রশ্নপত্র পড়ার সময়): কোনো উত্তর লিখবেন না। কেবল প্রশ্ন পড়ে সহজ ও কমন প্রশ্নগুলো টিক দিয়ে রাখুন। বড় প্রশ্নের বিকল্প (Or) নির্বাচন করে ফেলুন।",
                    "• পরবর্তী ৫০ মিনিট (বিভাগ 'ক' ও 'খ'): MCQ ও অতি সংক্ষিপ্ত প্রশ্নগুলো দ্রুত এবং পরিষ্কার হস্তাক্ষরে শেষ করুন।",
                    "• পরবর্তী ১ ঘণ্টা ১৫ মিনিট (বিভাগ 'গ' ও 'ঘ'): সংক্ষিপ্ত ও রচনাধর্মী প্রশ্নের পয়েন্টভিত্তিক উত্তর ও গাণিতিক সমস্যা সমাধান করুন।",
                    "• অন্তিম ২০ মিনিট: কঠিন বা যে প্রশ্নগুলো পরে ভাববেন বলে রেখেছিলেন সেগুলি সম্পূর্ণ করুন।",
                    "• শেষ ১০ মিনিট (আবশ্যক রিভিশন): দাগ নম্বর ঠিক আছে কিনা, খাতার পেজ নম্বর ও লুজ শিট টাই করা হয়েছে কিনা এবং কোনো প্রশ্ন বাদ গেছে কিনা তা মিলিয়ে নিন।"
                ),
                icon = Icons.Default.Timer,
                color = ExamGold
            ),
            TopperTipItem(
                id = "tip_2",
                title = "চিত্র অঙ্কন ও খাতার মার্জিনের গোল্ডেন রুলস",
                tag = "খাতা উপস্থাপনা",
                summary = "জীবনবিজ্ঞান, ভৌতবিজ্ঞান ও ভূগোলে পূর্ণাঙ্গ নম্বর পাওয়ার জন্য ছবির সঠিক বিন্যাস।",
                detailedGuidance = listOf(
                    "• সবসময় স্কেল দিয়ে খাতার উপরে ১ ইঞ্চি ও বামে ১.৫ ইঞ্চি মার্জিন টানবেন।",
                    "• জীবনবিজ্ঞান ও ভূগোলের সমস্ত ছবি শুধুমাত্র শার্প পেন্সিল দিয়ে আঁকবেন (পেন ব্যবহার নিষিদ্ধ)।",
                    "• চিত্রের লেবেলিং সবসময় ছবির ডানদিকে স্কেল দিয়ে সমান্তরাল দাগ টেনে স্পষ্ট হরফে লিখবেন।",
                    "• ছবির নিচে বক্স করে চিত্রের নাম (যেমন: চিত্র-১: প্রতিবর্ত চাপের গঠন) লিখে দেবেন।",
                    "• গুরুত্বপূর্ণ পরিভাষা বা ফর্মুলা নীল কালির নিচে কালো কালির সরু আন্ডারলাইন দিয়ে হাইলাইট করুন।"
                ),
                icon = Icons.Default.Draw,
                color = Color(0xFF10B981)
            ),
            TopperTipItem(
                id = "tip_3",
                title = "ভৌতবিজ্ঞান ও গণিতে ১০০/১০০ পাওয়ার টিপস",
                tag = "বিজ্ঞান ও গণিত",
                summary = "গাণিতিক সমস্যা সমাধানের স্টেপ-বাই-স্টেপ নিয়ম ও একক লেখার গুরুত্ব।",
                detailedGuidance = listOf(
                    "• গাণিতিক প্রশ্নে প্রথমে কী কী প্রদত্ত (Given data) তা একক সহ লিখে নিন।",
                    "• এরপর মূল সূত্রটি পরিষ্কারভাবে লিখুন (যেমন: V = IR বা PV = nRT)।",
                    "• প্রতিটি উত্তরের সাথে অবশ্যই সঠিক একক (SI বা CGS) লিখবেন (একক না লিখলে ১ নম্বর কাটা যায়)।",
                    "• গণিতের খাতার ডানদিকে স্পষ্ট 'Rough Space' রাখুন এবং কাটাকাটি হলে একটামাত্র সরু দাগ দিয়ে কাটবেন।"
                ),
                icon = Icons.Default.Calculate,
                color = Color(0xFF3B82F6)
            ),
            TopperTipItem(
                id = "tip_4",
                title = "ইতিহাস ও ভূগোলের বড় প্রশ্নের পয়েন্ট ফরম্যাট",
                tag = "সমাজবিজ্ঞান",
                summary = "প্যারাগ্রাফের বদলে সুস্পষ্ট সাব-হেডিং ও তথ্যবহুল পয়েন্টের ব্যবহার।",
                detailedGuidance = listOf(
                    "• ৪ বা ৮ নম্বরের প্রশ্নে সবসময় ভূমিকা ও উপসংহার সহ কমপক্ষে ৫-৮টি স্পষ্ট পয়েন্ট তৈরি করবেন।",
                    "• ইতিহাসের ক্ষেত্রে গুরুত্বপূর্ণ সাল, ঐতিহাসিক দলিল, ব্যক্তির নাম ও গ্রন্থ বোল্ড বা আন্ডারলাইন করুন।",
                    "• ভূগোলের যেকোনো আঞ্চলিক আলোচনায় প্রয়োজনীয় রৈখিক মানচিত্র বা ডায়াগ্রাম যুক্ত করলে পূর্ণ নম্বর মেলে।",
                    "• পার্থক্যের প্রশ্নে সবসময় ৩টি কলাম করবেন: 'বিষয়/ভিত্তি', 'প্রথম বিষয়', 'দ্বিতীয় বিষয়'।"
                ),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                color = Color(0xFFEC4899)
            ),
            TopperTipItem(
                id = "tip_5",
                title = "বোর্ড পরীক্ষকদের সাধারণ সতর্কতা ও যেসব ভুল এড়িয়ে চলবেন",
                tag = "সতর্কবার্তা",
                summary = "যেসব ভুলে পরীক্ষার্থীরা সহজে নম্বর হারায় এবং সেগুলি থেকে বাঁচার উপায়।",
                detailedGuidance = listOf(
                    "• দাগ নম্বর ভুল লেখা: প্রশ্নের বিভাগ ও সঠিক দাগ নম্বর না লিখলে পুরো উত্তর বাতিল হতে পারে।",
                    "• অতিরিক্ত কাটাকাটি বা ওভাররাইটিং: ওভাররাইট না করে একটানে কেটে পাশে সুন্দর করে লিখুন।",
                    "• বিকল্প প্রশ্ন না খেয়াল করা: 'যেকোনো ৪টি' বলা থাকলে ৬টি লিখে সময় নষ্ট করবেন না।",
                    "• হাতের লেখা অতি ঘন না করে দুটি শব্দের মাঝে স্পষ্ট ফাঁক রাখুন।"
                ),
                icon = Icons.Default.Warning,
                color = WrongRed
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "টপার্স খাতা ও উত্তর লেখার গাইড",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "বোর্ড এক্সামে ৯০+ স্কোর অর্জনের প্রামাণ্য ব্লুপ্রিন্ট",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("toppers_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Hero Banner
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = DarkBackground,
                    border = BorderStroke(1.5.dp, ExamGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = ExamGold, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "বোর্ড এক্সামে সেরা হওয়ার সিক্রেট",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = ExamGold
                            )
                        }
                        Text(
                            text = "পড়ার পাশাপাশি পরীক্ষার খাতায় উপস্থাপনা ও সময় নিয়ন্ত্রণই ঠিক করে দেয় ৯৫%+ নম্বরের ভাগ্য।",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            items(guides) { tip ->
                val isExpanded = expandedId == tip.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isExpanded) tip.color else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedId = if (isExpanded) null else tip.id }
                        .testTag("topper_tip_${tip.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(tip.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(tip.icon, contentDescription = null, tint = tip.color, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = tip.color.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = tip.tag,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = tip.color,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = tip.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tip.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                tip.detailedGuidance.forEach { point ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = point,
                                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
