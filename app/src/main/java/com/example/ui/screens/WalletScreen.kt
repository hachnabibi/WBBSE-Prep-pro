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

data class EarnTask(
    val id: String,
    val title: String,
    val description: String,
    val reward: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun EarnTaskCard(
    title: String,
    reward: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isBengali: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isClaimed) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isClaimed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isClaimed) Color.Gray.copy(alpha = 0.15f) else ExamGold.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = if (isClaimed) Color.Gray else ExamGold)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isClaimed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isClaimed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onClaim,
                enabled = !isClaimed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExamGold,
                    contentColor = Color(0xFF1E1B2E),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
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
    val claimedTaskIds by viewModel.claimedCoinTaskIds.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Earn, 1 = Redeem, 2 = Transactions, 3 = VIP
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(true) }

    // Mock transaction history
    var transactions by remember {
        mutableStateOf(
            listOf(
                WalletTransaction("tx_1", if (isBengali) "দৈনিক লগইন রিওয়ার্ড" else "Daily Login Reward", +50, true, System.currentTimeMillis() - 3600000, "DAILY"),
                WalletTransaction("tx_2", if (isBengali) "মক টেস্ট সম্পন্ন বোনাস" else "Mock Test Finished Bonus", +100, true, System.currentTimeMillis() - 86400000, "TEST"),
                WalletTransaction("tx_3", if (isBengali) "এআই কোচ সাজেস্ট আনলক" else "AI Coach Suggestion Unlock", -150, false, System.currentTimeMillis() - 172800000, "REDEEM")
            )
        )
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    fun addTransaction(title: String, amount: Int, isCredit: Boolean, category: String, claimedTaskId: String? = null) {
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
        val updatedClaimedSet = if (claimedTaskId != null) profile.claimedCoinTaskIds + claimedTaskId else profile.claimedCoinTaskIds
        viewModel.updateStudentProfile(profile.copy(coins = updatedCoins, claimedCoinTaskIds = updatedClaimedSet))

        actionMessage = if (isCredit) {
            if (isBengali) "🎉 +$amount কয়েন ওয়ালেটে যোগ হয়েছে!" else "🎉 +$amount Coins added to your wallet!"
        } else {
            if (isBengali) "✅ $amount কয়েন রিডিম করা হয়েছে!" else "✅ $amount Coins redeemed successfully!"
        }
        isSuccessMessage = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBengali) "কয়েন ও ওয়ালেট" else "Coins & Wallet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryIndigo,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Hero Balance Card
            item {
                Card(
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(PrimaryIndigo, Color(0xFF3F51B5), Color(0xFF1A237E))
                            ),
                            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
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
                                    text = if (isBengali) "বর্তমান ব্যালেন্স" else "Current Balance",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "🪙 $currentTokens",
                                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                        color = ExamGold
                                    )
                                    Text(
                                        text = if (isBengali) "কয়েন" else "Coins",
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
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isBengali) "গোল্ড মেম্বার" else "Gold Member",
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = ExamGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isBengali) "১০০% সুরক্ষিত ওয়ালেট ব্যাকএন্ড" else "100% Safe & Secure Wallet Backend",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Text(
                                text = if (isBengali) "ইউজার: ${profile.name}" else "User: ${profile.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // 2. Action Messages and Tab Bar Area
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Action Message Notification Banner
                    actionMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSuccessMessage) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (isSuccessMessage) CorrectGreen else WrongRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSuccessMessage) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isSuccessMessage) CorrectGreen else WrongRed
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { actionMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Tab Buttons with background pills (Horizontal Scroll)
                    val tabs = listOf(
                        if (isBengali) "🎯 কয়েন আর্ন" else "🎯 Earn Coins",
                        if (isBengali) "🎁 রিডিম" else "🎁 Redeem",
                        if (isBengali) "📜 হিস্ট্রি" else "📜 History"
                    )

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
                                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) BorderStroke(1.5.dp, ExamGold) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
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
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Dynamic Tab Content Items
            when (selectedTab) {
                0 -> {
                    // TAB 0: Earn Coins Section (Persistent single claim preventer)
                    val tasks = listOf(
                        EarnTask(
                            id = "task_daily_login",
                            title = if (isBengali) "দৈনিক লগইন বোনাস" else "Daily Login Bonus",
                            description = if (isBengali) "প্রতিদিন অ্যাপে লগইন করে ফ্রি কয়েন ক্লেইম করুন" else "Claim free coins by logging into the app daily",
                            reward = 50,
                            icon = Icons.Default.CalendarToday
                        ),
                        EarnTask(
                            id = "task_mock_test",
                            title = if (isBengali) "মক টেস্ট সম্পূর্ণ করুন" else "Complete a Mock Test",
                            description = if (isBengali) "যেকোনো বিষয়ে একটি পূর্ণাঙ্গ মক টেস্ট শেষ করুন" else "Finish a full-length mock test in any subject",
                            reward = 100,
                            icon = Icons.Default.AssignmentTurnedIn
                        ),
                        EarnTask(
                            id = "task_practice_10",
                            title = if (isBengali) "১০টি প্র্যাকটিস প্রশ্ন সমাধান" else "Solve 10 Practice Questions",
                            description = if (isBengali) "অধ্যায়ভিত্তিক প্র্যাকটিস মোডে ১০টি প্রশ্নের সঠিক উত্তর দিন" else "Answer 10 questions correctly in chapter practice mode",
                            reward = 30,
                            icon = Icons.Default.Quiz
                        ),
                        EarnTask(
                            id = "task_referral",
                            title = if (isBengali) "বন্ধুকে ইনভাইট করুন" else "Invite a Friend",
                            description = if (isBengali) "আপনার ইনভাইট লিংক শেয়ার করে সহপাঠীকে যুক্ত করুন" else "Share your invite link with classmate to join",
                            reward = 200,
                            icon = Icons.Default.Share
                        )
                    )

                    items(tasks, key = { it.id }) { task ->
                        val isClaimed = claimedTaskIds.contains(task.id)
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            EarnTaskCard(
                                title = task.title,
                                reward = if (isClaimed) (if (isBengali) "ক্লেইমড" else "Claimed") else "+${task.reward}",
                                description = task.description,
                                icon = task.icon,
                                isBengali = isBengali,
                                isClaimed = isClaimed,
                                onClaim = {
                                    if (!isClaimed) {
                                        addTransaction(
                                            title = task.title,
                                            amount = task.reward,
                                            isCredit = true,
                                            category = "REWARD",
                                            claimedTaskId = task.id
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // TAB 1: Redeem Rewards Section (Bilingual options)
                    val rewards = listOf(
                        Triple(
                            if (isBengali) "প্রিমিয়াম সাজেশন ও নোটস আনলক" else "Unlock Premium Notes & Suggestions",
                            if (isBengali) "অধ্যায়ের বিশেষ সাজেস্টিভ পিডিএফ নোটাবলী আনলক করুন" else "Unlock premium expert-designed pdf chapter notes",
                            300
                        ),
                        Triple(
                            if (isBengali) "এআই কোচ আনলিমিটেড টিপস" else "AI Coach Unlimited Smart Tips",
                            if (isBengali) "আপনার দুর্বলতা কাটিয়ে ওঠার জন্য AI পার্সোনালাইজড প্ল্যান" else "AI personalized feedback plan to boost your scores",
                            500
                        ),
                        Triple(
                            if (isBengali) "টেস্ট সিরিজ ৫০ টাকা ডিসকাউন্ট কুপন" else "Test Series ₹50 Off Voucher",
                            if (isBengali) "যেকোনো পেইড মক টেস্ট সিজনে ৫০ টাকা ছাড় পান" else "Get ₹50 flat discount voucher on paid test seasons",
                            1000
                        )
                    )

                    items(rewards) { reward ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            RedeemCard(
                                title = reward.first,
                                cost = reward.third,
                                currentTokens = currentTokens,
                                description = reward.second,
                                icon = when (reward.third) {
                                    300 -> Icons.AutoMirrored.Filled.MenuBook
                                    500 -> Icons.Default.AutoAwesome
                                    else -> Icons.Default.LocalOffer
                                },
                                isBengali = isBengali,
                                onRedeem = {
                                    addTransaction(
                                        title = reward.first,
                                        amount = reward.third,
                                        isCredit = false,
                                        category = "REDEEM"
                                    )
                                }
                            )
                        }
                    }
                }

                2 -> {
                    // TAB 2: Transaction History
                    if (transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBengali) "কোনো ট্রানজ্যাকশন হিস্ট্রি পাওয়া যায়নি" else "No transactions found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(transactions) { tx ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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
            }
        }
    }
}
