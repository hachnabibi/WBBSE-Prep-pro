package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionItem
import com.example.data.model.toBengaliDigits
import com.example.ui.components.AdBannerCard
import com.example.ui.components.AdMobBanner
import com.example.ui.components.AdRewardType
import com.example.ui.components.RewardedAdDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerBattleScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    viewModel: DeshExamViewModel? = null,
    onRewardEarned: (xp: Int, coins: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val studentProfile by (viewModel?.studentProfile?.collectAsState() ?: remember { mutableStateOf(com.example.data.model.StudentProfile()) })
    val subjectsList by (viewModel?.subjects?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val chaptersList by (viewModel?.chapters?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val topicsList by (viewModel?.topics?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val currentClassId = studentProfile.classId.ifBlank { "class_10_wbbse" }

    // Screen Sub-State: 0 = Setup/Configuration, 1 = Searching / Live Battle Arena, 2 = Result
    var battleStage by remember { mutableIntStateOf(0) }

    // Configuration states chosen by the user
    var selectedSubjectId by remember { mutableStateOf<String?>("all") }
    var selectedChapterId by remember { mutableStateOf<String?>("all") }
    var selectedTopicId by remember { mutableStateOf<String?>("all") }
    var selectedQuestionCount by remember { mutableIntStateOf(5) } // 5, 10, 15, 20
    var selectedTimerSeconds by remember { mutableIntStateOf(15) } // 10, 15, 20, 30
    var matchMode by remember { mutableStateOf("online_peer") } // online_peer, create_room
    var generatedRoomCode by remember { mutableStateOf("DESH-${(1000..9999).random()}") }

    // Active Battle states
    var isSearching by remember { mutableStateOf(false) }
    var matchFoundAnim by remember { mutableStateOf(false) }
    var opponentName by remember { mutableStateOf("") }
    var opponentSchool by remember { mutableStateOf("") }
    var opponentDistrict by remember { mutableStateOf("") }

    var battleQuestions by remember { mutableStateOf<List<QuestionItem>>(emptyList()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isTimedOut by remember { mutableStateOf(false) }

    var myScore by remember { mutableIntStateOf(0) }
    var opponentScore by remember { mutableIntStateOf(0) }
    var is2XBonusClaimed by remember { mutableStateOf(false) }
    var bonusXpAwarded by remember { mutableIntStateOf(0) }
    var bonusCoinsAwarded by remember { mutableIntStateOf(0) }
    var showDoubleXpAdDialog by remember { mutableStateOf(false) }

    // Live Question Timer
    var secondsRemaining by remember { mutableIntStateOf(selectedTimerSeconds) }
    var isTimerActive by remember { mutableStateOf(false) }

    val className = remember(currentClassId, isBengali) {
        when {
            currentClassId.contains("class_5") -> "পঞ্চম শ্রেণি"
            currentClassId.contains("class_6") -> "ষষ্ঠ শ্রেণি"
            currentClassId.contains("class_7") -> "সপ্তম শ্রেণি"
            currentClassId.contains("class_8") -> "অষ্টম শ্রেণি"
            currentClassId.contains("class_9") -> "নবম শ্রেণি"
            currentClassId.contains("class_11") -> "একাদশ শ্রেণি"
            currentClassId.contains("class_12") -> "দ্বাদশ শ্রেণি"
            else -> "দশম শ্রেণি (মাধ্যমিক)"
        }
    }

    // Filter chapters for selected subject
    val availableChapters = remember(selectedSubjectId, chaptersList) {
        if (selectedSubjectId == null || selectedSubjectId == "all") emptyList()
        else chaptersList.filter { it.subjectId == selectedSubjectId }
    }

    // Filter topics for selected chapter
    val availableTopics = remember(selectedChapterId, topicsList) {
        if (selectedChapterId == null || selectedChapterId == "all") emptyList()
        else topicsList.filter { it.chapterId == selectedChapterId }
    }

    // Function to advance to next question or complete battle
    fun advanceToNextQuestion() {
        if (currentQuestionIndex < battleQuestions.size - 1) {
            currentQuestionIndex++
            selectedOptionIndex = null
            isTimedOut = false
            secondsRemaining = selectedTimerSeconds
        } else {
            battleStage = 2
            val finalMatchXp = if (myScore > 0) myScore else 10
            val finalMatchCoins = (myScore / 2).coerceAtLeast(10)
            onRewardEarned(finalMatchXp, finalMatchCoins)
        }
    }

    // Function to launch battle
    fun launchChallenge() {
        battleStage = 1
        isSearching = true
        matchFoundAnim = false
        currentQuestionIndex = 0
        selectedOptionIndex = null
        isTimedOut = false
        myScore = 0
        opponentScore = 0
        is2XBonusClaimed = false
        bonusXpAwarded = 0
        bonusCoinsAwarded = 0

        scope.launch {
            val qs = if (viewModel != null) {
                viewModel.getRandomBattleQuestions(
                    subjectId = if (selectedSubjectId == "all") null else selectedSubjectId,
                    chapterId = if (selectedChapterId == "all") null else selectedChapterId,
                    topicId = if (selectedTopicId == "all") null else selectedTopicId,
                    count = selectedQuestionCount
                )
            } else {
                emptyList()
            }

            battleQuestions = if (qs.isNotEmpty()) qs else listOf(
                QuestionItem(
                    id = "b1",
                    topicId = "t1",
                    chapterId = "ch1",
                    subjectId = "s1",
                    type = com.example.data.model.QuestionType.MCQ,
                    difficulty = com.example.data.model.Difficulty.MEDIUM,
                    question = "কোনটি সঠিক বৈজ্ঞানিক একক?",
                    options = listOf("মিটার", "গ্রাম-ভার", "পাউন্ডাল", "ডাইন"),
                    correctOptionIndex = 0,
                    correctAnswerText = "মিটার",
                    explanation = "মিটার হলো দূরত্বের আন্তর্জাতিক এস.আই একক।"
                )
            )

            // Dynamic realistic matching animation
            val searchDelay = if (matchMode == "online_peer") 2000L else 2400L
            delay(searchDelay)

            // Generate class-appropriate opponent details
            val opponents = when {
                currentClassId.contains("class_5") -> listOf(
                    Triple("সৌম্যদীপ রায়", "বাঁকুড়া মডেল প্রাইমারি", "বাঁকুড়া"),
                    Triple("অঙ্কিতা মণ্ডল", "মেদিনীপুর টাউন জুনিয়র স্কুল", "পশ্চিম মেদিনীপুর"),
                    Triple("দেবজ্যোতি সাহা", "হাওড়া আদর্শ বিদ্যালয়", "হাওড়া")
                )
                currentClassId.contains("class_6") || currentClassId.contains("class_7") || currentClassId.contains("class_8") -> listOf(
                    Triple("সায়ন মুখার্জী", "বর্ধমান বিদ্যার্থী ভবন", "পূর্ব বর্ধমান"),
                    Triple("শ্রেয়া সেনগুপ্ত", "কৃষ্ণনগর হাই স্কুল", "নদীয়া"),
                    Triple("তানিশা খাতুন", "মুর্শিদাবাদ জুনিয়র অ্যাকাডেমি", "মুর্শিদাবাদ")
                )
                currentClassId.contains("class_11") || currentClassId.contains("class_12") -> listOf(
                    Triple("রাহুল চক্রবর্তী", "বেথুন কলেজিয়েট", "কলকাতা"),
                    Triple("আকাশ ব্যানার্জি", "যাদবপুর এইচ এস স্কুল", "কলকাতা"),
                    Triple("শুভজিৎ বিশ্বাস", "বারাসাত মহাত্মা গান্ধী স্কুল", "উত্তর ২৪ পরগনা")
                )
                else -> listOf(
                    Triple("রোহিত শর্মা", "হিন্দু স্কুল", "কলকাতা"),
                    Triple("অনন্যা মুখোপাধ্যায়", "যাদবপুর বিদ্যাপীঠ", "কলকাতা"),
                    Triple("অয়ন পাল", "মালদা জিলা স্কুল", "মালদা"),
                    Triple("দেবস্মিতা ঘোষ", "সিলিগুরি বয়েজ হাই স্কুল", "দার্জিলিং")
                )
            }
            val randomOpponent = opponents.random()
            opponentName = if (matchMode == "create_room") "সহপাঠী বন্ধু (Friend)" else randomOpponent.first
            opponentSchool = if (matchMode == "create_room") "রুম কোড দ্বারা যুক্ত" else randomOpponent.second
            opponentDistrict = if (matchMode == "create_room") "পশ্চিমবঙ্গ" else randomOpponent.third

            matchFoundAnim = true
            delay(1200) // Brief "Match Found!" celebration transition

            isSearching = false
            secondsRemaining = selectedTimerSeconds
        }
    }

    // Live Countdown Timer loop — when time runs out, automatically move to next question
    LaunchedEffect(isSearching, currentQuestionIndex, battleStage) {
        if (battleStage == 1 && !isSearching && currentQuestionIndex < battleQuestions.size) {
            secondsRemaining = selectedTimerSeconds
            isTimedOut = false

            while (secondsRemaining > 0 && selectedOptionIndex == null) {
                delay(1000)
                if (selectedOptionIndex != null) break
                secondsRemaining--
            }

            // If time ran out without answer, show time's up feedback and auto-advance
            if (secondsRemaining <= 0 && selectedOptionIndex == null) {
                isTimedOut = true
                delay(1000)
                advanceToNextQuestion()
            }
        }
    }

    // Live Opponent response simulation
    LaunchedEffect(isSearching, currentQuestionIndex, battleStage) {
        if (battleStage == 1 && !isSearching) {
            val opponentAnswerDelay = ((selectedTimerSeconds * 250L)..(selectedTimerSeconds * 650L)).random()
            delay(opponentAnswerDelay)
            if ((0..10).random() >= 3) {
                val speedBonus = if (opponentAnswerDelay < (selectedTimerSeconds * 400L)) 15 else 10
                opponentScore += speedBonus
            }
        }
    }

    val activeQuestion = battleQuestions.getOrNull(currentQuestionIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (battleStage == 0) 
                                ("১ বনাম ১ কুইজ সেটআপ")
                            else 
                                ("লাইভ ১ বনাম ১ লড়াই"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$className • কাস্টম লড়াই",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (battleStage == 1 && isSearching) {
                                isSearching = false
                                battleStage = 0
                            } else if (battleStage > 0) {
                                battleStage = 0
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("battle_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (battleStage == 0) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { launchChallenge() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("start_1v1_custom_challenge_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF9333EA))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "১ বনাম ১ লড়াই শুরু করুন",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (battleStage == 1 && !isSearching) {
                // FIXED BOTTOM BANNER AD (Safe, Play Store compliant, perfectly separated from question & options)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AdMobBanner(
                            isBengali = isBengali,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (battleStage) {
                // ==========================================
                // 0. CUSTOM CHALLENGE CONFIGURATION SCREEN
                // ==========================================
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hero Header Banner
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                                border = BorderStroke(1.5.dp, ExamGold.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(ExamGold.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsEsports,
                                            contentDescription = null,
                                            tint = ExamGold,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "১ বনাম ১ কুইজ লড়াই",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (isBengali) "আপনার পছন্দমতো বিষয়, প্রশ্নসংখ্যা ও সময় নির্বাচন করে সহপাঠীদের সাথে লড়াই করুন।"
                                            else "Choose your preferred subject, questions & timer to battle live with peers.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                                            color = Color.LightGray
                                        )
                                    }
                                }
                            }
                        }

                        // STEP 1: SUBJECT SELECTION
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "১. বিষয় নির্বাচন করুন",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        item {
                                            FilterChip(
                                                selected = selectedSubjectId == "all" || selectedSubjectId == null,
                                                onClick = {
                                                    selectedSubjectId = "all"
                                                    selectedChapterId = "all"
                                                    selectedTopicId = "all"
                                                },
                                                label = {
                                                    Text(
                                                        text = "🎲 সকল বিষয় (মিক্সড)",
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            )
                                        }

                                        items(subjectsList) { sub ->
                                            FilterChip(
                                                selected = selectedSubjectId == sub.id,
                                                onClick = {
                                                    selectedSubjectId = sub.id
                                                    selectedChapterId = "all"
                                                    selectedTopicId = "all"
                                                },
                                                label = {
                                                    Text(
                                                        text = if (isBengali) sub.nameBengali else sub.nameEnglish,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 2: CHAPTER SELECTION (Appears if specific subject chosen)
                        if (selectedSubjectId != null && selectedSubjectId != "all" && availableChapters.isNotEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "২. অধ্যায় নির্বাচন করুন",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            item {
                                                FilterChip(
                                                    selected = selectedChapterId == "all" || selectedChapterId == null,
                                                    onClick = {
                                                        selectedChapterId = "all"
                                                        selectedTopicId = "all"
                                                    },
                                                    label = {
                                                        Text(
                                                            text = "📖 সকল অধ্যায়",
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                )
                                            }

                                            items(availableChapters) { ch ->
                                                FilterChip(
                                                    selected = selectedChapterId == ch.id,
                                                    onClick = {
                                                        selectedChapterId = ch.id
                                                        selectedTopicId = "all"
                                                    },
                                                    label = { Text(ch.title, fontWeight = FontWeight.Bold) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 3: TOPIC SELECTION (Appears if specific chapter chosen)
                        if (selectedChapterId != null && selectedChapterId != "all" && availableTopics.isNotEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = ExamGold, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "৩. টপিক নির্বাচন করুন",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            item {
                                                FilterChip(
                                                    selected = selectedTopicId == "all" || selectedTopicId == null,
                                                    onClick = { selectedTopicId = "all" },
                                                    label = {
                                                        Text(
                                                            text = "🎯 সকল টপিক",
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                )
                                            }

                                            items(availableTopics) { top ->
                                                FilterChip(
                                                    selected = selectedTopicId == top.id,
                                                    onClick = { selectedTopicId = top.id },
                                                    label = { Text(top.title, fontWeight = FontWeight.Bold) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 4: QUESTION COUNT SELECTION
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "৪. মোট প্রশ্ন সংখ্যা নির্বাচন করুন",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(5, 10, 15, 20).forEach { qCount ->
                                            val isSelected = selectedQuestionCount == qCount
                                            val label = "${qCount.toBengaliDigits()}টি প্রশ্ন"
                                            Surface(
                                                onClick = { selectedQuestionCount = qCount },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                                border = BorderStroke(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) ExamGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                            fontSize = 12.sp
                                                        ),
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 5: TIMER PER QUESTION SELECTION
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = ExamGold, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "৫. প্রতি প্রশ্নের সময় নির্ধারণ করুন",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            10 to ("১০ সে ⚡"),
                                            15 to ("১৫ সে ⏱️"),
                                            20 to ("২০ সে ⏳"),
                                            30 to ("৩০ সে 🧠")
                                        ).forEach { (sec, label) ->
                                            val isSelected = selectedTimerSeconds == sec
                                            Surface(
                                                onClick = { selectedTimerSeconds = sec },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                                border = BorderStroke(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) ExamGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                            fontSize = 12.sp
                                                        ),
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 6: MATCHMAKING MODE (Auto online or Friend Room)
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "৬. খেলার মোড নির্বাচন করুন",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val isOnline = matchMode == "online_peer"
                                        Surface(
                                            onClick = { matchMode = "online_peer" },
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isOnline) Color(0xFF0F766E).copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                            border = BorderStroke(
                                                width = if (isOnline) 1.5.dp else 1.dp,
                                                color = if (isOnline) Color(0xFF14B8A6) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            ) {
                                                Text(text = "🌐", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "অনলাইন ম্যাচ",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isOnline) FontWeight.Bold else FontWeight.SemiBold
                                                    ),
                                                    color = if (isOnline) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        val isFriend = matchMode == "create_room"
                                        Surface(
                                            onClick = { matchMode = "create_room" },
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isFriend) Color(0xFF4338CA).copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                            border = BorderStroke(
                                                width = if (isFriend) 1.5.dp else 1.dp,
                                                color = if (isFriend) Color(0xFF818CF8) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            ) {
                                                Text(text = "👥", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "রুম কোড",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isFriend) FontWeight.Bold else FontWeight.SemiBold
                                                    ),
                                                    color = if (isFriend) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    if (matchMode == "create_room") {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF1E1B4B),
                                            border = BorderStroke(1.dp, ExamGold),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = "আপনার রুম কোড: $generatedRoomCode",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = ExamGold
                                                )
                                                Text(
                                                    text = "বন্ধুকে এই কোডটি দিয়ে অ্যাপে জয়েন করতে বলুন।",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.LightGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Summary Info Pill
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBengali) 
                                            "সারাংশ: ${selectedQuestionCount.toBengaliDigits()}টি প্রশ্ন • প্রতি প্রশ্নে ${selectedTimerSeconds.toBengaliDigits()} সেকেন্ড • দ্রুত সঠিক উত্তরে সর্বোচ্চ +১৫ XP" 
                                        else 
                                            "Setup: $selectedQuestionCount Questions • ${selectedTimerSeconds}s/Q • Speed Bonus up to +15 XP",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 1. LIVE BATTLE ARENA & MATCHMAKING SCREEN
                // ==========================================
                1 -> {
                    if (isSearching) {
                        // ENHANCED RADAR & MATCHMAKING SEARCHING ANIMATION
                        val infiniteTransition = rememberInfiniteTransition(label = "radar")
                        val radarPulse by infiniteTransition.animateFloat(
                            initialValue = 0.85f,
                            targetValue = 1.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )
                        val radarAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 0.8f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Tag
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                val subLabel = if (selectedSubjectId == "all" || selectedSubjectId == null) {
                                    "সকল বিষয়"
                                } else {
                                    subjectsList.find { it.id == selectedSubjectId }?.let { if (isBengali) it.nameBengali else it.nameEnglish } ?: ""
                                }
                                Text(
                                    text = "⚔️ $className • $subLabel ($selectedQuestionCount টি প্রশ্ন)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            // Center Radar & Avatar Comparison
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // You (Player)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(PrimaryIndigo, Color(0xFF8B5CF6))
                                                    )
                                                )
                                                .border(3.dp, ExamGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = studentProfile.name.take(1).ifBlank { "তু" },
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "আপনি",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = studentProfile.name.ifBlank { "শিক্ষার্থী" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Center VS Badge with Pulsing Rings
                                    Box(
                                        modifier = Modifier.size(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(74.dp)
                                                .scale(radarPulse)
                                                .clip(CircleShape)
                                                .background(ExamGold.copy(alpha = radarAlpha * 0.35f))
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = if (matchFoundAnim) Color(0xFF10B981) else Color(0xFF1E1B4B),
                                            border = BorderStroke(2.dp, ExamGold),
                                            modifier = Modifier.size(54.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (matchFoundAnim) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "VS",
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.Black,
                                                            color = ExamGold
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Opponent (Scanning / Found)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (matchFoundAnim) Brush.linearGradient(colors = listOf(Color(0xFFEF4444), Color(0xFFF97316)))
                                                    else Brush.linearGradient(colors = listOf(Color(0xFF334155), Color(0xFF475569)))
                                                )
                                                .border(
                                                    3.dp,
                                                    if (matchFoundAnim) Color(0xFF10B981) else ExamGold.copy(alpha = 0.5f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (matchFoundAnim) {
                                                Text(
                                                    text = opponentName.take(1),
                                                    style = MaterialTheme.typography.headlineMedium.copy(
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White
                                                    )
                                                )
                                            } else {
                                                CircularProgressIndicator(
                                                    color = ExamGold,
                                                    strokeWidth = 3.dp,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (matchFoundAnim) opponentName else ("প্রতিপক্ষ খোঁজা হচ্ছে..."),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (matchFoundAnim) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (matchFoundAnim) opponentSchool else ("লাইভ সহপাঠী"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Animated Status Card
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (matchFoundAnim) Color(0xFF064E3B) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.5.dp, if (matchFoundAnim) Color(0xFF10B981) else ExamGold.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (matchFoundAnim) 
                                                ("🎉 প্রতিপক্ষ পাওয়া গেছে! যুদ্ধ শুরু হচ্ছে...") 
                                            else 
                                                ("🔍 পশ্চিমবঙ্গের সহপাঠী খোঁজা হচ্ছে..."),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (matchFoundAnim) Color(0xFF6EE7B7) else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (matchFoundAnim)
                                                ("$opponentName ($opponentDistrict) • প্রস্তুত থাকুন ⚡")
                                            else
                                                ("প্রশ্নপত্র সিঙ্ক ও টাইমার প্রস্তুত হচ্ছে..."),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (matchFoundAnim) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Cancel Button
                            OutlinedButton(
                                onClick = {
                                    isSearching = false
                                    battleStage = 0
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("অনুসন্ধান বাতিল করুন")
                            }
                        }
                    } else if (activeQuestion != null) {
                        // LIVE BATTLE ARENA
                        val optionLetters = listOf("A", "B", "C", "D")
                        val defaultOptionStyles = listOf(
                            Triple(Color(0xFF1E293B), Color(0xFF3B82F6).copy(alpha = 0.45f), Color(0xFF2563EB)), // A: Slate Blue
                            Triple(Color(0xFF0F2E28), Color(0xFF10B981).copy(alpha = 0.45f), Color(0xFF0D9488)), // B: Teal Emerald
                            Triple(Color(0xFF2E1C0A), Color(0xFFF59E0B).copy(alpha = 0.45f), Color(0xFFD97706)), // C: Warm Amber
                            Triple(Color(0xFF291538), Color(0xFFA855F7).copy(alpha = 0.45f), Color(0xFF7C3AED))  // D: Violet Purple
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            // 1. Live Scoreboard Header Card
                            Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "আপনি (You)",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = PrimaryIndigoLight
                                            )
                                            Text(
                                                text = "$myScore XP",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                                color = PrimaryIndigoLight
                                            )
                                        }
                                        Surface(shape = CircleShape, color = ExamGold.copy(alpha = 0.2f)) {
                                            Text(
                                                text = "VS",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                                color = ExamGold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = opponentName,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = WrongRed,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "$opponentScore XP",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                                color = WrongRed
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 2. UNIFIED QUESTION CARD (Directly below Scoreboard Card)
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.2.dp, ExamGold.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Top Countdown Bar inside the Card
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Timer,
                                                        contentDescription = "Timer",
                                                        tint = when {
                                                            secondsRemaining <= 4 -> WrongRed
                                                            secondsRemaining <= 8 -> ExamGold
                                                            else -> CorrectGreen
                                                        },
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = if (isTimedOut) 
                                                            ("⏰ সময় শেষ! পরবর্তী প্রশ্ন আসছে...") 
                                                        else 
                                                            ("টাইমার: $secondsRemaining সেকেন্ড"),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = when {
                                                                isTimedOut || secondsRemaining <= 4 -> WrongRed
                                                                secondsRemaining <= 8 -> ExamGold
                                                                else -> CorrectGreen
                                                            }
                                                        )
                                                    )
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = ExamGold.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "প্রশ্ন ${currentQuestionIndex + 1}/${battleQuestions.size}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = ExamGold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            LinearProgressIndicator(
                                                progress = { (secondsRemaining.toFloat() / selectedTimerSeconds.toFloat()).coerceIn(0f, 1f) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(CircleShape),
                                                color = when {
                                                    secondsRemaining <= 4 -> WrongRed
                                                    secondsRemaining <= 8 -> ExamGold
                                                    else -> CorrectGreen
                                                },
                                                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            )
                                        }

                                        HorizontalDivider(
                                            color = ExamGold.copy(alpha = 0.25f),
                                            thickness = 1.dp
                                        )

                                        // Question Content inside the same card
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                        ) {
                                            Text(
                                                text = activeQuestion.question,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 19.sp,
                                                    lineHeight = 27.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                            Spacer(modifier = Modifier.height(30.dp))

                            // 3. Option Cards placed directly after the Question Card
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                activeQuestion.options.forEachIndexed { optIndex, optionText ->
                                    val isSelected = selectedOptionIndex == optIndex
                                    val isCorrect = optIndex == activeQuestion.correctOptionIndex
                                    val defaultStyle = defaultOptionStyles.getOrElse(optIndex % defaultOptionStyles.size) { defaultOptionStyles[0] }

                                    val containerColor = when {
                                        selectedOptionIndex == null && !isTimedOut -> defaultStyle.first
                                        isSelected && isCorrect -> Color(0xFF10B981)
                                        isSelected && !isCorrect -> Color(0xFFEF4444)
                                        (selectedOptionIndex != null || isTimedOut) && isCorrect -> Color(0xFF10B981).copy(alpha = 0.45f)
                                        else -> defaultStyle.first.copy(alpha = 0.35f)
                                    }

                                    val contentColor = when {
                                        selectedOptionIndex == null && !isTimedOut -> Color.White
                                        isSelected || isCorrect -> Color.White
                                        else -> Color.White.copy(alpha = 0.6f)
                                    }

                                    val borderColor = when {
                                        selectedOptionIndex == null && !isTimedOut -> defaultStyle.second
                                        isSelected && isCorrect -> Color(0xFF10B981)
                                        isSelected && !isCorrect -> Color(0xFFEF4444)
                                        (selectedOptionIndex != null || isTimedOut) && isCorrect -> Color(0xFF10B981)
                                        else -> defaultStyle.second.copy(alpha = 0.25f)
                                    }

                                    val badgeBgColor = when {
                                        selectedOptionIndex == null && !isTimedOut -> defaultStyle.third
                                        isSelected && isCorrect -> Color.White.copy(alpha = 0.25f)
                                        isSelected && !isCorrect -> Color.White.copy(alpha = 0.25f)
                                        (selectedOptionIndex != null || isTimedOut) && isCorrect -> Color(0xFF10B981)
                                        else -> defaultStyle.third.copy(alpha = 0.35f)
                                    }

                                    val badgeTextColor = when {
                                        selectedOptionIndex == null && !isTimedOut -> Color.White
                                        isSelected || isCorrect -> Color.White
                                        else -> Color.White.copy(alpha = 0.6f)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = containerColor,
                                        border = BorderStroke(1.dp, borderColor),
                                        shadowElevation = if (selectedOptionIndex == null && !isTimedOut) 2.dp else 0.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable(enabled = selectedOptionIndex == null && !isTimedOut) {
                                                selectedOptionIndex = optIndex
                                                isTimerActive = false

                                                if (isCorrect) {
                                                    val speedBonus = if (secondsRemaining >= (selectedTimerSeconds / 2)) 15 else 10
                                                    myScore += speedBonus
                                                }

                                                scope.launch {
                                                    delay(1100)
                                                    advanceToNextQuestion()
                                                }
                                            }
                                            .testTag("battle_option_$optIndex")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Option Number / Letter Badge
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(badgeBgColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = optionLetters.getOrElse(optIndex) { "${optIndex + 1}" },
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                                    color = badgeTextColor
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            // Left-aligned Option Text
                                            Text(
                                                text = optionText,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = contentColor,
                                                    lineHeight = 20.sp
                                                ),
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                // ==========================================
                // 2. BATTLE RESULT & REWARDS SCREEN
                // ==========================================
                2 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(2.dp, ExamGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = if (myScore >= opponentScore) Icons.Default.EmojiEvents else Icons.Default.SentimentSatisfied,
                                    contentDescription = null,
                                    tint = ExamGold,
                                    modifier = Modifier.size(64.dp)
                                )

                                Text(
                                    text = if (myScore > opponentScore) 
                                        ("🏆 অভিনন্দন! আপনি বিজয়ী হয়েছেন!") 
                                    else if (myScore == opponentScore) 
                                        ("🤝 সমানে সমানে ড্র ম্যাচ!") 
                                    else 
                                        ("👏 দুর্দান্ত লড়াই!"),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                    color = ExamGold
                                )

                                Text(
                                    text = if (isBengali) 
                                        "আপনার স্কোর: $myScore XP\nপ্রতিপক্ষ ($opponentName, $opponentSchool): $opponentScore XP" 
                                    else 
                                        "Your Score: $myScore XP\nOpponent ($opponentName, $opponentSchool): $opponentScore XP",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // 2X Battle Trophy & XP Boost Card
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (is2XBonusClaimed) Color(0xFF1E293B) else Color(0xFF1E1B4B),
                                    border = BorderStroke(1.dp, if (is2XBonusClaimed) CorrectGreen else ExamGold),
                                    modifier = Modifier.fillMaxWidth().testTag("battle_2x_reward_card")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (is2XBonusClaimed) Icons.Default.CheckCircle else Icons.Default.Stars,
                                                contentDescription = null,
                                                tint = if (is2XBonusClaimed) CorrectGreen else ExamGold,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = if (is2XBonusClaimed)
                                                        ("২X ব্যাটল বোনাস যোগ হয়েছে! 🎉")
                                                    else
                                                        ("২X ব্যাটল এক্সপি ও ট্রফি দ্বিগুণ করুন"),
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = if (is2XBonusClaimed)
                                                        ("+$bonusXpAwarded অতিরিক্ত XP এবং +$bonusCoinsAwarded কয়েন সফলভাবে যোগ হয়েছে!")
                                                    else
                                                        ("একটি ছোট স্পনসর ভিডিও দেখুন (নূন্যতম +৫০ XP)"),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (is2XBonusClaimed) CorrectGreen else Color.LightGray
                                                )
                                            }
                                        }

                                        if (!is2XBonusClaimed) {
                                            Button(
                                                onClick = { showDoubleXpAdDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "২X করুন",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { battleStage = 0 },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("নতুন কনফিগ")
                                    }

                                    Button(
                                        onClick = { launchChallenge() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("রিম্যাচ খেলুন")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDoubleXpAdDialog) {
            RewardedAdDialog(
                rewardType = AdRewardType.DOUBLE_XP,
                isBengali = isBengali,
                onRewardEarned = {
                    val addedXp = if (myScore > 0) myScore else 50
                    val addedCoins = if (myScore > 0) (myScore / 2).coerceAtLeast(50) else 50
                    bonusXpAwarded = addedXp
                    bonusCoinsAwarded = addedCoins
                    myScore += addedXp
                    is2XBonusClaimed = true
                    onRewardEarned(addedXp, addedCoins)
                    showDoubleXpAdDialog = false
                },
                onDismiss = { showDoubleXpAdDialog = false }
            )
        }
    }
}
