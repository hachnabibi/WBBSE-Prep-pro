package com.example.ui.screens

import android.app.Activity
import android.speech.tts.TextToSpeech
import com.example.util.AdMobManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdBannerCard
import com.example.ui.components.InterstitialAdDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel
import java.util.Locale

data class FlashcardItem(
    val id: String,
    val subject: String,
    val subjectBengali: String,
    val chapter: String,
    val frontTitle: String,
    val frontContent: String,
    val backTitle: String,
    val backContent: String,
    val formulaOrKeyPoint: String,
    val examImportance: String, // ⭐️⭐️⭐️
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.studentProfile.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var selectedSubject by remember { mutableStateOf("ALL") }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var masteryScore by remember { mutableIntStateOf(0) }
    var cardsReviewed by remember { mutableIntStateOf(0) }
    var showInterstitialAd by remember { mutableStateOf(false) }

    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Bengali or default locale
                tts?.language = Locale.forLanguageTag("bn-IN")
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val sampleCards = remember {
        listOf(
            FlashcardItem(
                id = "fc_phy_1",
                subject = "PHYSICAL_SCIENCE",
                subjectBengali = "ভৌতবিজ্ঞান",
                chapter = "গ্যাসের আচরণ",
                frontTitle = "চার্লসের সূত্র (Charles's Law)",
                frontContent = "স্থির চাপে নির্দিষ্ট ভরের গ্যাসের ক্ষেত্রে আয়তন ও পরম তাপমাত্রার সম্পর্কটি কী?",
                backTitle = "সূত্রের বিবৃতি ও গাণিতিক রূপ",
                backContent = "স্থির চাপে নির্দিষ্ট ভরের কোনো গ্যাসের আয়তন তার পরম তাপমাত্রার সাথে সমানুপাতিক।",
                formulaOrKeyPoint = "V ∝ T (যখন P ও m স্থির) ⇒ V₁/T₁ = V₂/T₂",
                examImportance = "★★★ মাধ্যমিক ২০২১, ২০২৩",
                color = Color(0xFF3B82F6)
            ),
            FlashcardItem(
                id = "fc_phy_2",
                subject = "PHYSICAL_SCIENCE",
                subjectBengali = "ভৌতবিজ্ঞান",
                chapter = "চলতড়িৎ",
                frontTitle = "ওহমের সূত্র (Ohm's Law)",
                frontContent = "উষ্ণতা ও অন্যান্য ভৌত অবস্থা অপরিবর্তিত থাকলে পরিবাহীর প্রবাহমাত্রা ও বিভবপ্রভেদের সম্পর্ক কী?",
                backTitle = "সূত্রের বিবৃতি ও গাণিতিক রূপ",
                backContent = "কোনো পরিবাহীর মধ্য দিয়ে প্রবাহিত তড়িৎপ্রবাহমাত্রা পরিবাহীর দুই প্রান্তের বিভবপ্রভেদের সমানুপাতিক।",
                formulaOrKeyPoint = "V = I × R (বিভবপ্রভেদ = প্রবাহমাত্রা × রোধ)",
                examImportance = "★★★ মাধ্যমিক ২০২০, ২০২৪",
                color = Color(0xFF3B82F6)
            ),
            FlashcardItem(
                id = "fc_math_1",
                subject = "MATHEMATICS",
                subjectBengali = "গণিত",
                chapter = "দ্বিঘাত সমীকরণ",
                frontTitle = "শ্রীধর আচার্যের সূত্র (Quadratic Formula)",
                frontContent = "ax² + bx + c = 0 সমীকরণের বীজ নির্ণয়ের সাধারণ সূত্রটি কী?",
                backTitle = "বীজদ্বয়ের সূত্র ও নিরূপক (Discriminant)",
                backContent = "বীজদ্বয় = (-b ± √(b² - 4ac)) / (2a)\nনিরূপক D = b² - 4ac",
                formulaOrKeyPoint = "D > 0 হলে বাস্তব ও অসমান, D = 0 হলে বাস্তব ও সমান, D < 0 হলে কাল্পনিক",
                examImportance = "★★★ মাধ্যমিক ২০১৮, ২০২২, ২০২৪",
                color = Color(0xFFF59E0B)
            ),
            FlashcardItem(
                id = "fc_math_2",
                subject = "MATHEMATICS",
                subjectBengali = "গণিত",
                chapter = "লম্ব বৃত্তাকার চোঙ",
                frontTitle = "লম্ব বৃত্তাকার চোঙের সমগ্রতলের ক্ষেত্রফল",
                frontContent = "ভূমির ব্যাসার্ধ r ও উচ্চতা h হলে চোঙের বক্রতল ও সমগ্রতলের ক্ষেত্রফলের সূত্র কী?",
                backTitle = "ক্ষেত্রফল ও আয়তন সূত্রাবলী",
                backContent = "বক্রতলের ক্ষেত্রফল = 2πrh\nসমগ্রতলের ক্ষেত্রফল = 2πr(r + h) বর্গ একক\nআয়তন = πr²h ঘন একক",
                formulaOrKeyPoint = "সমগ্রতল = 2πr(h + r) বর্গ একক",
                examImportance = "★★☆ মাধ্যমিক ২০১৯, ২০২৩",
                color = Color(0xFFF59E0B)
            ),
            FlashcardItem(
                id = "fc_life_1",
                subject = "LIFE_SCIENCE",
                subjectBengali = "জীবনবিজ্ঞান",
                chapter = "জীবজগতে নিয়ন্ত্রণ ও সমন্বয়",
                frontTitle = "উদ্ভিদের বৃদ্ধি সহায়ক হরমোন: অক্সিন (Auxin)",
                frontContent = "অক্সিন হরমোনের প্রধান কাজ ও রাসায়নিক নাম কী?",
                backTitle = "কাজ ও রাসায়নিক নাম",
                backContent = "রাসায়নিক নাম: ইন্ডোল অ্যাসিটিক অ্যাসিড (IAA)\nকাজ: অগ্রস্থ প্রকটতা বজায় রাখা, কোশ বিভাজন ঘটানো এবং ট্রপিক চলন নিয়ন্ত্রণ।",
                formulaOrKeyPoint = "ইন্ডোল অ্যাসিটিক অ্যাসিড (IAA) • আলোকবৃত্তি চলনে প্রধান ভূমিকা",
                examImportance = "★★★ মাধ্যমিক ২০১৬, ২০২০, ২০২২",
                color = Color(0xFF10B981)
            ),
            FlashcardItem(
                id = "fc_history_1",
                subject = "HISTORY",
                subjectBengali = "ইতিহাস",
                chapter = "সংস্কার: বৈশিষ্ট্য ও পর্যালোচনা",
                frontTitle = "সতীদাহ প্রথা রদ ও বিধবাবিবাহ আইন",
                frontContent = "সতীদাহ প্রথা কবে কোন আইনের দ্বারা রদ হয় এবং বিধবাবিবাহ আইন কবে পাস হয়?",
                backTitle = "গুরুত্বপূর্ণ সাল ও আইন",
                backContent = "• ১৮২৯ খ্রিস্টাব্দে সপ্তদশ রেগুলেশন (Reg XVII) দ্বারা লর্ড উইলিয়াম বেন্টিঙ্ক সতীদাহ প্রথা রদ করেন (নেতৃত্বে রাজা রামমোহন রায়)।\n• ১৮৫৬ খ্রিস্টাব্দে লর্ড ক্যানিং-এর আমলে বিধবাবিবাহ আইন পাস হয় (নেতৃত্বে ঈশ্বরচন্দ্র বিদ্যাসাগর)।",
                formulaOrKeyPoint = "১৮২৯: সতীদাহ রদ • ১৮৫৬: বিধবাবিবাহ আইন",
                examImportance = "★★★ মাধ্যমিক ২০১৭, ২০১৯, ২০২৪",
                color = Color(0xFFEC4899)
            ),
            FlashcardItem(
                id = "fc_geo_1",
                subject = "GEOGRAPHY",
                subjectBengali = "ভূগোল",
                chapter = "বহির্জাত প্রক্রিয়া ও ভূমিরূপ",
                frontTitle = "নদীর ক্ষয়কাজের ফলে গঠিত ভূমিরূপ: 'I' ও 'V' আকৃতির উপত্যকা",
                frontContent = "গিরিখাত ও ক্যানিয়নের মূল পার্থক্য কী?",
                backTitle = "গিরিখাত বনাম ক্যানিয়ন",
                backContent = "• গিরিখাত: আর্দ্র পার্বত্য অঞ্চলে নদীর নিম্নক্ষয়ের ফলে গঠিত গভীর সংকীর্ণ 'V' আকৃতির উপত্যকা (যেমন- ইয়াংসিকিয়াং গিরিখাত)।\n• ক্যানিয়ন: শুষ্ক মরু অঞ্চলে নদীর প্রবল নিম্নক্ষয়ের ফলে গঠিত 'I' আকৃতির উপত্যকা (যেমন- কলোরাডো নদীর গ্র্যান্ড ক্যানিয়ন)।",
                formulaOrKeyPoint = "আর্দ্র জলবায়ু = গিরিখাত (V-আকৃতি) • শুষ্ক জলবায়ু = ক্যানিয়ন (I-আকৃতি)",
                examImportance = "★★★ মাধ্যমিক ২০১৮, ২০২১, ২০২৪",
                color = Color(0xFF06B6D4)
            ),
            // Class 12 & 11 Cards
            FlashcardItem(
                id = "fc_hs_phy_1",
                subject = "PHYSICS",
                subjectBengali = "পদার্থবিদ্যা",
                chapter = "স্থিরতড়িৎ (Electrostatics)",
                frontTitle = "কুলম্বের সূত্র ও গাউসের উপপাদ্য (Coulomb's & Gauss's Law)",
                frontContent = "দুটি বিন্দু আধানের মধ্যকার বল এবং বদ্ধ তলের মোট তড়িৎ ফ্লাক্সের সূত্র কী?",
                backTitle = "সূত্র ও গাণিতিক রূপ",
                backContent = "• কুলম্বের সূত্র: F = (1 / 4πε₀) · (q₁q₂ / r²)\n• গাউসের সূত্র: ∮ E·dA = q_enclosed / ε₀",
                formulaOrKeyPoint = "F = k·(q₁q₂/r²) • Φ = q / ε₀",
                examImportance = "★★★ উচ্চমাধ্যমিক ২০২৪, ২০২৬",
                color = Color(0xFF8B5CF6)
            ),
            FlashcardItem(
                id = "fc_hs_chem_1",
                subject = "CHEMISTRY",
                subjectBengali = "রসায়নবিদ্যা",
                chapter = "রাসায়নিক গতিবিদ্যা ও দ্রবণ",
                frontTitle = "রাউল্টের সূত্র (Raoult's Law)",
                frontContent = "অনুদ্বায়ী দ্রাব দ্রবণের আপেক্ষিক বাষ্পচাপের অবনমন সংক্রান্ত সূত্রটি কী?",
                backTitle = "রাউল্টের সূত্র বিবৃতি ও রূপ",
                backContent = "কোনো অনুদ্বায়ী দ্রাব দ্রবণের আপেক্ষিক বাষ্পচাপের অবনমন দ্রবীভূত দ্রাবের মোল ভগ্নাংশের সমান।",
                formulaOrKeyPoint = "(P₀ - P) / P₀ = x₂ (দ্রাবের মোল ভগ্নাংশ)",
                examImportance = "★★★ উচ্চমাধ্যমিক ২০২২, ২০২৫",
                color = Color(0xFF059669)
            ),
            FlashcardItem(
                id = "fc_hs_math_1",
                subject = "MATHEMATICS",
                subjectBengali = "গণিত",
                chapter = "কলনবিদ্যা (Calculus)",
                frontTitle = "অন্তরকলন ও সমাকলন মৌলিক সূত্রাবলী",
                frontContent = "d/dx(xⁿ), d/dx(sin x), ∫ xⁿ dx এর মান কী?",
                backTitle = "মৌলিক অন্তরকলন ও সমাকলন রূপ",
                backContent = "• d/dx(xⁿ) = n·xⁿ⁻¹\n• d/dx(sin x) = cos x, d/dx(eˣ) = eˣ\n• ∫ xⁿ dx = (xⁿ⁺¹)/(n + 1) + c (n ≠ -1)",
                formulaOrKeyPoint = "∫ 1/x dx = ln|x| + c • d/dx(uv) = u(dv/dx) + v(du/dx)",
                examImportance = "★★★ উচ্চমাধ্যমিক ২০২৩, ২০২৬",
                color = Color(0xFFD97706)
            ),
            // Middle School Cards (Class 5 - 8)
            FlashcardItem(
                id = "fc_jr_sci_1",
                subject = "SCIENCE",
                subjectBengali = "বিজ্ঞান",
                chapter = "বল ও গতি",
                frontTitle = "নিউটনের প্রথম ও দ্বিতীয় গতিসূত্র",
                frontContent = "জাড্য ধর্ম এবং বল ও ভরবেগের সম্পর্ক কী?",
                backTitle = "গতিসূত্র ও বলের পরিমাপ",
                backContent = "• ১ম সূত্র: বাইরে থেকে বল প্রযুক্ত না হলে স্থির বস্তু চিরকাল স্থির ও গতিশীল বস্তু চিরকাল সমগতিতে সরলরেখায় চলবে (জাড্য ধর্ম)।\n• ২য় সূত্র: প্রযুক্ত বল = ভর × ত্বরণ (F = ma)",
                formulaOrKeyPoint = "F = m × a (বল = ভর × ত্বরণ)",
                examImportance = "★★★ শ্রেণি ৭, ৮, ৯ বার্ষিক পরীক্ষা",
                color = Color(0xFF2563EB)
            ),
            FlashcardItem(
                id = "fc_jr_env_1",
                subject = "ENVIRONMENT",
                subjectBengali = "পরিবেশ",
                chapter = "আমাদের পরিবেশ ও জলচক্র",
                frontTitle = "সালোকসংশ্লেষ ও জলচক্র (Photosynthesis)",
                frontContent = "গাছ কীভাবে সূর্যালোক ও জলের সাহায্যে খাদ্য তৈরি করে?",
                backTitle = "সালোকসংশ্লেষ সমীকরণ",
                backContent = "কার্বন ডাই-অক্সাইড + জল + সূর্যালোক (ক্লোরোফিল) → গ্লুকোজ + অক্সিজেন + জল।",
                formulaOrKeyPoint = "6CO₂ + 12H₂O → C₆H₁₂O₆ + 6O₂ + 6H₂O",
                examImportance = "★★★ শ্রেণি ৫ ও ৬ পরিবেশ ও বিজ্ঞান",
                color = Color(0xFF16A34A)
            )
        )
    }

    val classSubjectCodes = remember(subjects) { subjects.map { it.code.name.uppercase() }.toSet() }
    val filteredCards = remember(selectedSubject, sampleCards, classSubjectCodes) {
        val classBase = sampleCards.filter { card ->
            if (classSubjectCodes.isEmpty()) true
            else classSubjectCodes.contains(card.subject) ||
                 (card.subject == "SCIENCE" && (classSubjectCodes.contains("SCIENCE") || classSubjectCodes.contains("ENVIRONMENT"))) ||
                 (card.subject == "ENVIRONMENT" && (classSubjectCodes.contains("ENVIRONMENT") || classSubjectCodes.contains("SCIENCE"))) ||
                 (card.subject == "PHYSICAL_SCIENCE" && (classSubjectCodes.contains("PHYSICAL_SCIENCE") || classSubjectCodes.contains("SCIENCE"))) ||
                 (card.subject == "PHYSICS" && classSubjectCodes.contains("PHYSICS")) ||
                 (card.subject == "CHEMISTRY" && classSubjectCodes.contains("CHEMISTRY")) ||
                 (card.subject == "MATHEMATICS" && classSubjectCodes.contains("MATHEMATICS")) ||
                 (card.subject == "LIFE_SCIENCE" && (classSubjectCodes.contains("LIFE_SCIENCE") || classSubjectCodes.contains("BIOLOGY"))) ||
                 (card.subject == "BIOLOGY" && classSubjectCodes.contains("BIOLOGY")) ||
                 (card.subject == "HISTORY" && classSubjectCodes.contains("HISTORY")) ||
                 (card.subject == "GEOGRAPHY" && classSubjectCodes.contains("GEOGRAPHY"))
        }
        if (selectedSubject == "ALL") {
            if (classBase.isNotEmpty()) classBase else sampleCards
        } else {
            sampleCards.filter { it.subject == selectedSubject }
        }
    }

    val activeCard = filteredCards.getOrNull(currentIndex.coerceIn(0, (filteredCards.size - 1).coerceAtLeast(0)))

    // 3D rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "card_flip"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "স্মার্ট ফ্ল্যাশকার্ড ও ফর্মুলা হাব",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "কার্ডে ট্যাপ করে উল্টান • স্পেসড রিভিশন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("flashcard_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExamGold.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$masteryScore XP",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = ExamGold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Subject Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = mutableListOf("ALL" to ("সকল বিষয় (${profile.classLevel})"))
                subjects.forEach { subj ->
                    filters.add(subj.code.name to (subj.name))
                }
                items(filters) { (key, label) ->
                    val isSelected = selectedSubject == key
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedSubject = key
                            currentIndex = 0
                            isFlipped = false
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_$key")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inline Sponsored Study Banner
            AdBannerCard(isBengali = isBengali)

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Header
            if (filteredCards.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কার্ড ${currentIndex + 1} / ${filteredCards.size}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "রিভিউ করা হয়েছে: $cardsReviewed টি",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryEmerald
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / filteredCards.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = ExamGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Flippable Flashcard
            if (activeCard != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clickable { isFlipped = !isFlipped }
                        .testTag("active_flashcard_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // FRONT OF CARD
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(2.dp, activeCard.color.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = activeCard.color.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${activeCard.subjectBengali} • ${activeCard.chapter}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = activeCard.color,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            tts?.speak(
                                                "${activeCard.frontTitle}. ${activeCard.frontContent}",
                                                TextToSpeech.QUEUE_FLUSH,
                                                null,
                                                null
                                            )
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", tint = ExamGold)
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = activeCard.frontTitle,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = activeCard.frontContent,
                                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = activeCard.examImportance,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ExamGold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "উত্তর দেখতে ট্যাপ করুন",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // BACK OF CARD (Flipped)
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(2.dp, SecondaryEmerald.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SecondaryEmerald.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "ব্যাখ্যা ও গাণিতিক রূপ",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = SecondaryEmerald,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            tts?.speak(
                                                "${activeCard.backContent}. ${activeCard.formulaOrKeyPoint}",
                                                TextToSpeech.QUEUE_FLUSH,
                                                null,
                                                null
                                            )
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", tint = ExamGold)
                                    }
                                }

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = activeCard.backTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = activeCard.backContent,
                                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = DarkCard,
                                        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "📌 মনে রাখার সহজ টিপস / সূত্র:",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = ExamGold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = activeCard.formulaOrKeyPoint,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "প্রশ্ন দেখতে পুনরায় ট্যাপ করুন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "এই বিষয়ের জন্য কোনো ফ্ল্যাশকার্ড পাওয়া যায়নি।",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confidence Rating Buttons (Spaced Repetition Feedback)
            Text(
                text = "আপনার এই বিষয়টি কতটা মনে আছে?",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Again (Hard)
                OutlinedButton(
                    onClick = {
                        isFlipped = false
                        cardsReviewed++
                        if (cardsReviewed % 5 == 0) {
                            showInterstitialAd = true
                        }
                        if (currentIndex < filteredCards.size - 1) currentIndex++ else currentIndex = 0
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WrongRed),
                    modifier = Modifier.weight(1f).testTag("flashcard_again_btn")
                ) {
                    Text("ভুলে গেছি\n(Again)", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                }

                // Good
                Button(
                    onClick = {
                        isFlipped = false
                        masteryScore += 10
                        cardsReviewed++
                        if (cardsReviewed % 5 == 0) {
                            showInterstitialAd = true
                        }
                        if (currentIndex < filteredCards.size - 1) currentIndex++ else currentIndex = 0
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.weight(1f).testTag("flashcard_good_btn")
                ) {
                    Text("মনে আছে\n(Good)", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                }

                // Easy
                Button(
                    onClick = {
                        isFlipped = false
                        masteryScore += 25
                        cardsReviewed++
                        if (cardsReviewed % 5 == 0) {
                            showInterstitialAd = true
                        }
                        if (currentIndex < filteredCards.size - 1) currentIndex++ else currentIndex = 0
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                    modifier = Modifier.weight(1f).testTag("flashcard_easy_btn")
                ) {
                    Text("খুব সহজ\n(Easy)", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showInterstitialAd) {
        val activity = context as? Activity
        var showInAppFallback by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (activity != null && AdMobManager.hasInterstitialAd()) {
                val shown = AdMobManager.showInterstitialAd(activity) {
                    showInterstitialAd = false
                }
                if (!shown) {
                    showInAppFallback = true
                }
            } else {
                showInAppFallback = true
            }
        }

        if (showInAppFallback) {
            InterstitialAdDialog(
                isBengali = isBengali,
                onDismiss = { showInterstitialAd = false }
            )
        }
    }
}
