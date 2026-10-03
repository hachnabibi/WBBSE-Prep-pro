package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AssignmentTurnedIn
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
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ExamGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel
import java.text.SimpleDateFormat
import java.util.*

data class WalletTransaction(
    val id: String = "",
    val title: String = "",
    val amount: Int = 0, // Positive for earned, negative for spent
    val isCredit: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "REWARD"
)

@Composable
fun EarnTaskCard(
    title: String,
    reward: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isBengali: Boolean,
    onClaim: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ExamGold.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = ExamGold)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(containerColor = ExamGold, contentColor = Color(0xFF1E1B2E)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = reward,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun RedeemCard(
    title: String,
    cost: Int,
    currentTokens: Int,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isBengali: Boolean,
    onRedeem: () -> Unit
) {
    val canAfford = currentTokens >= cost

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (canAfford) ExamGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryIndigo.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = PrimaryIndigo)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onRedeem,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryIndigo,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$cost 🪙",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.studentProfile.collectAsState()
    val currentTokens = profile.coins

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Earn, 1 = Redeem, 2 = Transactions
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(true) }

    // Mock transaction history if Firestore is empty
    var transactions by remember {
        mutableStateOf(
            listOf(
                WalletTransaction("tx_1", "দৈনিক লগইন রিওয়ার্ড", +50, true, System.currentTimeMillis() - 3600000, "DAILY"),
                WalletTransaction("tx_2", "মক টেস্ট সম্পন্ন বোনাস", +100, true, System.currentTimeMillis() - 86400000, "TEST"),
                WalletTransaction("tx_3", "এআই কোচ সাজেশন আনলক", -150, false, System.currentTimeMillis() - 172800000, "REDEEM")
            )
        )
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    fun addTransaction(title: String, amount: Int, isCredit: Boolean, category: String) {
        val newTx = WalletTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = title,
            amount = amount,
            isCredit = isCredit,
            timestamp = System.currentTimeMillis(),
            category = category
        )
        transactions = listOf(newTx) + transactions

        // Update profile in ViewModel
        val updatedCoins = if (isCredit) currentTokens + amount else (currentTokens - amount).coerceAtLeast(0)
        viewModel.updateStudentProfile(profile.copy(coins = updatedCoins))

        actionMessage = if (isCredit) {
            "🎉 +$amount কয়েন ওয়ালেটে যোগ হয়েছে!"
        } else {
            "✅ $amount কয়েন রিডিম করা হয়েছে!"
        }
        isSuccessMessage = true
    }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Token Balance Card (Edge-to-edge, 0 radius)
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryIndigo, Color(0xFF3F51B5), Color(0xFF1A237E))
                        ),
                        shape = RoundedCornerShape(0.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "বর্তমান ব্যালেন্স",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "🪙 $currentTokens",
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                    color = ExamGold
                                )
                                Text(
                                    text = "কয়েন",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        // Badge Level
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExamGold.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, ExamGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "গোল্ড মেম্বার",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ExamGold
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                            Text(
                                text = "১০০% সেফ ও সিকিউর ফায়ারবেস ওয়ালেট",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Text(
                            text = "ইউজার: ${profile.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Rest of screen content with horizontal padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status message
                actionMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CorrectGreen.copy(alpha = 0.15f),
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
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Tab Buttons with background pills (No text break)
                val tabs = listOf("🎯 আয় করুন", "🎁 রিডিম", "📜 হিস্ট্রি", "💎 ভিআইপি পাস")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Surface(
                            onClick = { selectedTab = index },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) PrimaryIndigo else Color(0xFF1E2235),
                            border = if (isSelected) BorderStroke(1.5.dp, ExamGold) else BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Earn Coins Section
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            EarnTaskCard(
                                title = "দৈনিক লগইন বোনাস",
                                reward = "+৫০ কয়েন",
                                description = "প্রতিদিন অ্যাপে লগইন করে ফ্রি কয়েন ক্লেইম করুন",
                                icon = Icons.Default.CalendarToday,
                                isBengali = isBengali,
                                onClaim = {
                                    addTransaction(
                                        title = "দৈনিক বোনাস",
                                        amount = 50,
                                        isCredit = true,
                                        category = "DAILY"
                                    )
                                }
                            )
                        }

                        item {
                            EarnTaskCard(
                                title = "মক টেস্ট সম্পূর্ণ করুন",
                                reward = "+১০০ কয়েন",
                                description = "যেকোনো বিষয়ে একটি পূর্ণাঙ্গ মক টেস্ট শেষ করুন",
                                icon = Icons.Default.AssignmentTurnedIn,
                                isBengali = isBengali,
                                onClaim = {
                                    addTransaction(
                                        title = "মক টেস্ট পুরস্কার",
                                        amount = 100,
                                        isCredit = true,
                                        category = "TEST"
                                    )
                                }
                            )
                        }

                        item {
                            EarnTaskCard(
                                title = "১০টি প্র্যাকটিস প্রশ্ন সমাধান",
                                reward = "+৩০ কয়েন",
                                description = "অধ্যায়ভিত্তিক প্র্যাকটিস মোডে ১০টি প্রশ্নের সঠিক উত্তর দিন",
                                icon = Icons.Default.Quiz,
                                isBengali = isBengali,
                                onClaim = {
                                    addTransaction(
                                        title = "১০টি প্রশ্ন সমাধান বোনাস",
                                        amount = 30,
                                        isCredit = true,
                                        category = "PRACTICE"
                                    )
                                }
                            )
                        }

                        item {
                            EarnTaskCard(
                                title = "বন্ধুকে ইনভাইট করুন",
                                reward = "+২০০ কয়েন",
                                description = "আপনার ইনভাইট লিংক শেয়ার করে সহপাঠীকে যুক্ত করুন",
                                icon = Icons.Default.Share,
                                isBengali = isBengali,
                                onClaim = {
                                    addTransaction(
                                        title = "ফ্রেন্ড রেফারাল রিওয়ার্ড",
                                        amount = 200,
                                        isCredit = true,
                                        category = "REFERRAL"
                                    )
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // Redeem Rewards Section
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            RedeemCard(
                                title = "প্রিমিয়াম সাজেশন ও নোটস আনলক",
                                cost = 300,
                                currentTokens = currentTokens,
                                description = "স্পেশাল সাজেস্টিভ প্রশ্নাবলী আনলক করুন",
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                isBengali = isBengali,
                                onRedeem = {
                                    addTransaction(
                                        title = "প্রিমিয়াম নোটস আনলক",
                                        amount = 300,
                                        isCredit = false,
                                        category = "REDEEM"
                                    )
                                }
                            )
                        }

                        item {
                            RedeemCard(
                                title = "এআই কোচ আনলিমিটেড টিপস",
                                cost = 500,
                                currentTokens = currentTokens,
                                description = "আপনার দুর্বলতা কাটিয়ে ওঠার জন্য AI পার্সোনালাইজড প্ল্যান",
                                icon = Icons.Default.AutoAwesome,
                                isBengali = isBengali,
                                onRedeem = {
                                    addTransaction(
                                        title = "AI কোচ প্রিমিয়াম আনলক",
                                        amount = 500,
                                        isCredit = false,
                                        category = "REDEEM"
                                    )
                                }
                            )
                        }

                        item {
                            RedeemCard(
                                title = "পেইড টেস্ট সিরিজ ৫০ টাকা ডিসকাউন্ট কুপন",
                                cost = 1000,
                                currentTokens = currentTokens,
                                description = "যেকোনো পেইড মক টেস্ট সিজনে ৫০ টাকা ছাড় পান",
                                icon = Icons.Default.LocalOffer,
                                isBengali = isBengali,
                                onRedeem = {
                                    addTransaction(
                                        title = "৫০ টাকা ডিসকাউন্ট কুপন",
                                        amount = 1000,
                                        isCredit = false,
                                        category = "COUPON"
                                    )
                                }
                            )
                        }
                    }
                }

                2 -> {
                    // Transaction History
                    if (transactions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "কোনো ট্রানজ্যাকশন হিস্ট্রি পাওয়া যায়নি",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(transactions) { tx ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (tx.isCredit) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (tx.isCredit) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                                                    contentDescription = null,
                                                    tint = if (tx.isCredit) CorrectGreen else WrongRed,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = tx.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = dateFormat.format(Date(tx.timestamp)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Text(
                                            text = "${if (tx.isCredit) "+" else "-"}${kotlin.math.abs(tx.amount)} 🪙",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                            color = if (tx.isCredit) CorrectGreen else WrongRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // VIP Membership & Ad-Free Pass Section
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "বিজ্ঞাপনমুক্ত (Ad-Free) ও প্রিমিয়াম মেম্বারশিপ প্ল্যান:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Plan 1: Monthly Pro Pass
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.5.dp, PrimaryIndigo),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🚀 মাসিক প্রো পাস (Monthly Pro)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryIndigo)
                                        Surface(shape = RoundedCornerShape(8.dp), color = PrimaryIndigo) {
                                            Text(text = "₹৯৯ / মাস", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                    Text(text = "• সম্পূর্ণ বিজ্ঞাপনমুক্ত (100% Ad-Free) পড়াশোনা\n• আনলিমিটেড এআই ডাউট সলভার ও কোচ ক্রেডিট\n• সব বিগত বছরের সমাধানসহ পিডিএফ ডাউনলোড", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    Button(
                                        onClick = {
                                            actionMessage = "🎉 মাসিক প্রো পাস সফলভাবে সাবস্ক্রাইব করা হয়েছে! অ্যাড-ফ্রি মোড অন।"
                                            isSuccessMessage = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("এখনই সাবস্ক্রাইব করুন", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Plan 2: Annual Board Exam Pass
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                                border = BorderStroke(1.5.dp, ExamGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "👑 বার্ষিক বোর্ড এক্সাম পাস (Best Value)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ExamGold)
                                        Surface(shape = RoundedCornerShape(8.dp), color = ExamGold) {
                                            Text(text = "₹৪৯৯ / বছর", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                    Text(text = "• পুরো বছরের জন্য ১০০% বিজ্ঞাপনমুক্ত এক্সেস\n• সমস্ত লাইভ মক টেস্ট ও অল-বাংলা মক ব্যাটল\n• এক্সক্লুসিভ টপার্স নোটস ও পিডিএফ সলিউশন", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                                    Button(
                                        onClick = {
                                            actionMessage = "👑 বার্ষিক বোর্ড পাস অ্যাক্টিভ হয়েছে! আপনি এখন ভিআইপি মেম্বার।"
                                            isSuccessMessage = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ExamGold, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("বার্ষিক পাস নিন", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Plan 3: Free Coin Redeem Pass
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, CorrectGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🪙 কয়েন দিয়ে অ্যাড-ফ্রি পাস (৩ দিন)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CorrectGreen)
                                        Surface(shape = RoundedCornerShape(8.dp), color = CorrectGreen.copy(alpha = 0.2f)) {
                                            Text(text = "৩০০ কয়েন", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CorrectGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                    Text(text = "• প্রতিদিন লগইন ও মক টেস্ট দিয়ে কয়েন জমিয়ে ৩ দিনের জন্য বিজ্ঞাপনমুক্ত পাস রিডিম করুন।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    OutlinedButton(
                                        onClick = {
                                            if (currentTokens >= 300) {
                                                addTransaction("৩ দিনের অ্যাড-ফ্রি পাস রিডিম", 300, false, "REDEEM")
                                            } else {
                                                actionMessage = "❌ পর্যাপ্ত কয়েন নেই! আরো ৩০০ কয়েন প্রয়োজন।"
                                                isSuccessMessage = false
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, CorrectGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("কয়েন দিয়ে আনলক করুন", fontWeight = FontWeight.Bold, color = CorrectGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
