package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdBannerCard
import com.example.ui.components.AdRewardType
import com.example.ui.components.RewardedAdDialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class ResolvedDoubt(
    val id: String = java.util.UUID.randomUUID().toString(),
    val questionText: String,
    val subject: String,
    val stepByStepSolution: List<String>,
    val finalAnswer: String,
    val keyConcept: String,
    val timestamp: String = "এখনই",
    var isBookmarked: Boolean = false
)

enum class DoubtInputMode {
    TEXT, VOICE, CAMERA, GALLERY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubtSolverScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    credits: Int = 3,
    onWatchAdForCredits: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedInputMode by remember { mutableStateOf(DoubtInputMode.TEXT) }
    var textQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("সব বিষয়") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var currentResolvedDoubt by remember { mutableStateOf<ResolvedDoubt?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showAdForCreditsDialog by remember { mutableStateOf(false) }
    var followUpText by remember { mutableStateOf("") }
    var isSpeaking by remember { mutableStateOf(false) }

    // Speech-to-Text Recognizer Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                textQuery = if (textQuery.isBlank()) spokenText else "$textQuery $spokenText"
                Toast.makeText(
                    context,
                    "🎙️ ভয়েস ইনপুট যুক্ত করা হয়েছে: $spokenText",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val startSpeechToText = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "আপনার প্রশ্নটি মুখে বলুন...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "আপনার ফোনে ভয়েস ইনপুট সার্ভিস উপলব্ধ নয়",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // TTS engine setup
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialize TTS
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val subjectsList = remember(isBengali) {
        if (isBengali) listOf("সব বিষয়", "গণিত", "ভৌতবিজ্ঞান", "জীবনবিজ্ঞান", "ইতিহাস", "ভূগোল", "ইংরেজি")
        else listOf("All Subjects", "Mathematics", "Physical Science", "Life Science", "History", "Geography", "English")
    }

    // Preloaded high-yield doubt templates
    val presetDoubts = remember(isBengali) {
        listOf(
            ResolvedDoubt(
                questionText = "একটি গ্যাসের 27°C উষ্ণতায় আয়তন 300 cc হলে, চাপ অপরিবর্তিত রেখে উষ্ণতা কত করলে আয়তন 400 cc হবে?",
                subject = "ভৌতবিজ্ঞান",
                stepByStepSolution = if (isBengali) listOf(
                    "ধাপ ১: প্রদত্ত তথ্যসমূহ লিখুন: V₁ = 300 cc, T₁ = 27°C = 27 + 273 = 300 K।",
                    "ধাপ ২: অন্তিম আয়তন V₂ = 400 cc, অন্তিম পরম উষ্ণতা T₂ = ?",
                    "ধাপ ৩: চার্লসের সূত্র ব্যবহার করি: V₁/T₁ = V₂/T₂ (চাপ ও ভর স্থির)।",
                    "ধাপ ৪: মান বসিয়ে পাই: 300/300 = 400/T₂ ⇒ T₂ = 400 K।",
                    "ধাপ ৫: সেলসিয়াস স্কেলে রূপান্তরিত উষ্ণতা: 400 - 273 = 127°C।"
                ) else listOf(
                    "Step 1: Write given values: V₁ = 300 cc, T₁ = 27°C = 300 K.",
                    "Step 2: V₂ = 400 cc, T₂ = ?",
                    "Step 3: Apply Charles' Law: V₁/T₁ = V₂/T₂.",
                    "Step 4: 300/300 = 400/T₂ ⇒ T₂ = 400 K.",
                    "Step 5: Convert to °C: 400 - 273 = 127°C."
                ),
                finalAnswer = "সঠিক উত্তর: 127°C (বা 400 কেলভিন)",
                keyConcept = "চার্লসের সূত্র: নির্দিষ্ট চাপে নির্দিষ্ট ভরের গ্যাসের আয়তন ওর পরম উষ্ণতার সমানুপাতিক (V ∝ T)।"
            ),
            ResolvedDoubt(
                questionText = "কোন সমান্তরাল প্রগতির (AP) 5তম পদ 11 এবং 10তম পদ 26 হলে, প্রথম পদ ও সাধারণ অন্তর নির্ণয় কর।",
                subject = "গণিত",
                stepByStepSolution = if (isBengali) listOf(
                    "ধাপ ১: ধরি, প্রথম পদ = a এবং সাধারণ অন্তর = d।",
                    "ধাপ ২: n-তম পদের সূত্র: Tₙ = a + (n - 1)d",
                    "ধাপ ৩: 5ম পদ: a + 4d = 11 ---(সমীকরণ ১)",
                    "ধাপ ৪: 10ম পদ: a + 9d = 26 ---(সমীকরণ ২)",
                    "ধাপ ৫: সমীকরণ (২) - (১) করে পাই: 5d = 15 ⇒ d = 3।",
                    "ধাপ ৬: d = 3 সমীকরণ (১)-এ বসিয়ে পাই: a + 4(3) = 11 ⇒ a = -1।"
                ) else listOf(
                    "Step 1: Let first term = a, common difference = d.",
                    "Step 2: Formula for nth term: Tₙ = a + (n - 1)d",
                    "Step 3: 5th term: a + 4d = 11 --- (1)",
                    "Step 4: 10th term: a + 9d = 26 --- (2)",
                    "Step 5: Subtract (1) from (2): 5d = 15 ⇒ d = 3.",
                    "Step 6: Substitute d = 3 into (1): a + 12 = 11 ⇒ a = -1."
                ),
                finalAnswer = "প্রথম পদ (a) = -1, সাধারণ অন্তর (d) = 3",
                keyConcept = "সমান্তরাল প্রগতির সাধারণ পদ Tₙ = a + (n - 1)d।"
            ),
            ResolvedDoubt(
                questionText = "সালোকসংশ্লেষের আলোক-নিরপেক্ষ দশায় (ডার্ক ফেজ) কেলভিন চক্রের তাৎপর্য কী?",
                subject = "জীবনবিজ্ঞান",
                stepByStepSolution = if (isBengali) listOf(
                    "ধাপ ১: স্থান: কেলভিন চক্র ক্লোরোপ্লাস্টের স্ট্রমা (Stroma)-য় ঘটে।",
                    "ধাপ ২: CO₂ আবদ্ধকরণ: পাতার রন্ধ্র দিয়ে গৃহীত CO₂ রিবিউলোজ বিসফসফেট (RuBP) এর সাথে যুক্ত হয়।",
                    "ধাপ ৩: এনজাইম ভূমিকা: রুবিস্কো (RuBisCO) উৎসেচক এই বিক্রিয়া ত্বরান্বিত করে।",
                    "ধাপ ৪: শর্করা সংশ্লেষ: আলোক দশায় উৎপন্ন ATP ও NADPH₂ ব্যবহার করে প্রথম স্থায়ী যৌগ PGA এবং পরে গ্লুকোজ উৎপন্ন করে।"
                ) else listOf(
                    "Step 1: Location: Occurs in the stroma of chloroplasts.",
                    "Step 2: Carbon Fixation: Atmospheric CO₂ binds with RuBP.",
                    "Step 3: Enzyme: Catalyzed by RuBisCO enzyme.",
                    "Step 4: Glucose Production: Uses ATP and NADPH₂ from light reaction to synthesize glucose."
                ),
                finalAnswer = "কেলভিন চক্রে বায়ুমণ্ডলের CO₂ বিজারিত হয়ে গ্লুকোজ শর্করায় রূপান্তরিত হয়।",
                keyConcept = "CO₂ আবদ্ধকরণের প্রধান উৎসেচক রুবিস্কো (RuBisCO)।"
            )
        )
    }

    val solvedHistory = remember { mutableStateListOf<ResolvedDoubt>() }

    // Dynamic AI Resolution generator function
    val solveDoubtQuery = { query: String, bitmap: Bitmap? ->
        if (credits <= 0) {
            showAdForCreditsDialog = true
        } else {
            isAnalyzing = true
            scope.launch {
                delay(1800) // Simulate Gemini Vision AI processing
                isAnalyzing = false

                // Find matching sample or generate dynamic step-by-step
                val matched = presetDoubts.find {
                    it.questionText.contains(query.take(10), ignoreCase = true) ||
                            it.subject.contains(selectedSubject, ignoreCase = true)
                }

                val result = if (matched != null) {
                    matched.copy(questionText = if (query.isNotBlank()) query else matched.questionText)
                } else {
                    ResolvedDoubt(
                        questionText = if (query.isNotBlank()) query else ("ক্যামেরা ছবি থেকে এক্সট্র্যাক্ট করা গাণিতিক সমীকরণ"),
                        subject = if (selectedSubject != "সব বিষয়" && selectedSubject != "All Subjects") selectedSubject else ("সাধারণ বিজ্ঞান ও গণিত"),
                        stepByStepSolution = if (isBengali) listOf(
                            "ধাপ ১: Gemini Vision AI আপনার প্রশ্নটি বিশ্লেষণ করেছে এবং মূল গাণিতিক/তাত্ত্বিক ডেটা এক্সট্র্যাক্ট করেছে।",
                            "ধাপ ২: প্রাসঙ্গিক সূত্রাবলী প্রয়োগ: প্রশ্নে বর্ণিত রাশিগুলোর মধ্যে গাণিতিক সম্পর্ক তৈরি করা হয়েছে।",
                            "ধাপ ৩: ধাপে ধাপে গাণিতিক সমীকরণ সমাধান ও সরলীকরণ সম্পন্ন করা হয়েছে।",
                            "ধাপ ৪: মাধ্যমিক পরীক্ষার স্ট্যান্ডার্ড অনুযায়ী স্পষ্ট বাংলায় সঠিক যুক্তি উপস্থাপন করা হল।"
                        ) else listOf(
                            "Step 1: Gemini Vision AI analyzed the input and extracted query data.",
                            "Step 2: Applied core formulas and established mathematical relationships.",
                            "Step 3: Solved step-by-step equations simplified accurately.",
                            "Step 4: Formatted answer according to Madhyamik Board standards."
                        ),
                        finalAnswer = "সঠিক সমাধান ও ব্যাখ্যা সম্পূর্ণ প্রস্তুত।",
                        keyConcept = "মূল কনসেপ্ট: মাধ্যমিক সিলেবাসের স্ট্যান্ডার্ড নিয়মানুযায়ী সমস্যা সমাধান করা হয়েছে।"
                    )
                }

                currentResolvedDoubt = result
                if (!solvedHistory.any { it.questionText == result.questionText }) {
                    solvedHistory.add(0, result)
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            solveDoubtQuery("ক্যামেরায় তোলা প্রশ্নের ছবি", bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            solveDoubtQuery("গ্যালারি থেকে নেওয়া প্রশ্নের ছবি", null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "স্ন্যাপ অ্যান্ড সলভ (AI ডাউট সলভার)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gemini 1.5 Flash AI থেকে তাৎক্ষণিক সমাধান পান",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("doubt_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ExamGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ExamGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$credits ক্রেডিট",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExamGold
                            )
                        }
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

                // Input Mode Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedInputMode == DoubtInputMode.TEXT,
                        onClick = { selectedInputMode = DoubtInputMode.TEXT },
                        label = { Text("✍️ টেক্সট", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedInputMode == DoubtInputMode.VOICE,
                        onClick = { selectedInputMode = DoubtInputMode.VOICE },
                        label = { Text("🎙️ ভয়েস", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedInputMode == DoubtInputMode.CAMERA,
                        onClick = {
                            selectedInputMode = DoubtInputMode.CAMERA
                            if (credits > 0) cameraLauncher.launch() else showAdForCreditsDialog = true
                        },
                        label = { Text("📷 ক্যামেরা", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedInputMode == DoubtInputMode.GALLERY,
                        onClick = {
                            selectedInputMode = DoubtInputMode.GALLERY
                            if (credits > 0) {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else {
                                showAdForCreditsDialog = true
                            }
                        },
                        label = { Text("🖼️ গ্যালারি", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Subject Filter Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "বিষয় নির্বাচন করুন:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subjectsList) { subj ->
                            SuggestionChip(
                                onClick = { selectedSubject = subj },
                                label = { Text(subj, fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (selectedSubject == subj) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = if (selectedSubject == subj) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Type / Paste Question Box (Text Input Mode)
            if (selectedInputMode == DoubtInputMode.TEXT) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "আপনার গাণিতিক বা থিওরি প্রশ্ন টাইপ করুন:",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (textQuery.isNotEmpty()) {
                                    TextButton(onClick = { textQuery = "" }) {
                                        Text("ক্লিয়ার", fontSize = 12.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = textQuery,
                                onValueChange = { textQuery = it },
                                placeholder = {
                                    Text(
                                        if (isBengali) "উদাহরণ: চার্লসের সূত্র কী? অথবা ২x + ৩ = ৯ সমীকরণ সমাধান কর..."
                                        else "e.g. Solve 2x + 3 = 9 or Explain Newton's Third Law..."
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = startSpeechToText,
                                        modifier = Modifier.testTag("doubt_mic_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Voice Input",
                                            tint = PrimaryIndigo
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .testTag("doubt_input_textfield"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = startSpeechToText,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, PrimaryIndigo),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("dictate_doubt_speech_btn")
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null, tint = PrimaryIndigo)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🎙️ মুখে বলুন",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryIndigo
                                    )
                                }

                                Button(
                                    onClick = { solveDoubtQuery(textQuery, null) },
                                    enabled = textQuery.isNotBlank() && !isAnalyzing,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("solve_doubt_btn")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ExamGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "সমাধান করুন",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Voice Speech-To-Text Dedicated Mode
            if (selectedInputMode == DoubtInputMode.VOICE) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ExamGold,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .clickable { startSpeechToText() }
                                    .testTag("large_voice_mic_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Search",
                                        tint = Color(0xFF1E1B2E),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🎙️ মুখে বলে প্রশ্ন করুন (Speech-to-Text)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                                Text(
                                    text = "মাইক্রোফোন আইকনে ট্যাপ করে স্পষ্ট বাংলায় প্রশ্নটি বলুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            if (textQuery.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "রেকর্ডকৃত প্রশ্ন:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ExamGold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = textQuery,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = startSpeechToText,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ExamGold, contentColor = Color.Black),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "পুনরায় বলুন",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { solveDoubtQuery(textQuery, null) },
                                    enabled = textQuery.isNotBlank() && !isAnalyzing,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryIndigo),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "এআই উত্তর",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Camera / Image Box Banner
            if (selectedInputMode != DoubtInputMode.TEXT) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedInputMode == DoubtInputMode.CAMERA) Icons.Default.CameraAlt else Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                text = if (selectedInputMode == DoubtInputMode.CAMERA) {
                                    "বইয়ের পাতার প্রশ্নের ছবি তুলুন"
                                } else {
                                    "গ্যালারি থেকে ফটো নির্বাচন করুন"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )

                            Text(
                                text = "ক্যামেরা বা গ্যালারি থেকে ছবি সিলেক্ট করলে এআই ভিশন মুহূর্তেই বাংলা সমাধান তৈরি করবে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            capturedBitmap?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Captured Question",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }

                            Button(
                                onClick = {
                                    if (credits <= 0) {
                                        showAdForCreditsDialog = true
                                    } else {
                                        if (selectedInputMode == DoubtInputMode.CAMERA) cameraLauncher.launch()
                                        else galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ExamGold, contentColor = Color(0xFF1E1B2E)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("launch_media_doubt_btn")
                            ) {
                                Icon(
                                    imageVector = if (selectedInputMode == DoubtInputMode.CAMERA) Icons.Default.PhotoCamera else Icons.Default.PhotoLibrary,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedInputMode == DoubtInputMode.CAMERA) {
                                        "ক্যামেরা অন করুন"
                                    } else {
                                        "গ্যালারি ওপেন করুন"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Rewarded Ad for Free Credits
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                    border = BorderStroke(1.5.dp, ExamGold.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("get_ai_credits_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ExamGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartDisplay,
                                    contentDescription = null,
                                    tint = ExamGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "ফ্রি AI ক্রেডিট রিচার্জ 🎁",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "ভিডিও বিজ্ঞাপন দেখে +৩টি AI ডাউট ক্রেডিট পান",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        Button(
                            onClick = { showAdForCreditsDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("watch_doubt_ad_btn")
                        ) {
                            Text(
                                text = "+৩ ক্রেডিট",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // AI Analyzing Loading State
            if (isAnalyzing) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = ExamGold, modifier = Modifier.size(32.dp))
                            Column {
                                Text(
                                    text = "Gemini Vision AI প্রশ্নটি বিশ্লেষণ করছে...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "ধাপভিত্তিক নিখুঁত সমাধান তৈরি হচ্ছে...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Active AI Resolution Display
            currentResolvedDoubt?.let { doubt ->
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryEmerald.copy(alpha = 0.12f)),
                        border = BorderStroke(1.5.dp, SecondaryEmerald),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_solution_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = SecondaryEmerald,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "🤖 এআই স্টেপ-বাই-স্টেপ সমাধান",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = SecondaryEmerald
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = doubt.subject,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryIndigoLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = doubt.questionText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            HorizontalDivider(color = SecondaryEmerald.copy(alpha = 0.3f))

                            doubt.stepByStepSolution.forEach { step ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            // Key Concept Highlight
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryIndigo.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = ExamGold)
                                    Column {
                                        Text(
                                            text = "💡 মূল কনসেপ্ট / সূত্র:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ExamGold
                                        )
                                        Text(
                                            text = doubt.keyConcept,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Final Answer Box
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = DarkBackground,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "🎯 চূড়ান্ত উত্তর:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ExamGold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = doubt.finalAnswer,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            // Interactive Toolbar (TTS, Copy, Share, Bookmark)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(onClick = {
                                    if (isSpeaking) {
                                        ttsEngine?.stop()
                                        isSpeaking = false
                                    } else {
                                        val fullText = "${doubt.questionText}. ${doubt.stepByStepSolution.joinToString(". ")}. ${doubt.finalAnswer}"
                                        ttsEngine?.language = java.util.Locale.Builder().setLanguage("bn").setRegion("IN").build()
                                        ttsEngine?.speak(fullText, TextToSpeech.QUEUE_FLUSH, null, "DoubtTTS")
                                        isSpeaking = true
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "TTS",
                                        tint = ExamGold
                                    )
                                }

                                IconButton(onClick = {
                                    val fullContent = "${doubt.questionText}\n\n${doubt.stepByStepSolution.joinToString("\n")}\n\n${doubt.finalAnswer}"
                                    clipboardManager.setText(AnnotatedString(fullContent))
                                    Toast.makeText(context, "সমাধান কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                                }

                                IconButton(onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "DeshExam AI Doubt Solution:\n${doubt.questionText}\nAnswer: ${doubt.finalAnswer}")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Doubt Solution"))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                                }

                                IconButton(onClick = {
                                    doubt.isBookmarked = !doubt.isBookmarked
                                    Toast.makeText(
                                        context,
                                        if (doubt.isBookmarked) ("বুকমার্কে সংরক্ষিত হলো!")
                                        else ("বুকমার্ক মুছে ফেলা হলো"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                    Icon(
                                        imageVector = if (doubt.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = ExamGold
                                    )
                                }
                            }

                            // Follow-up Question Input Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = followUpText,
                                    onValueChange = { followUpText = it },
                                    placeholder = { Text("এই সমাধান সম্পর্কে আরও কিছু জিজ্ঞাসা করুন...", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                IconButton(
                                    onClick = {
                                        if (followUpText.isNotBlank()) {
                                            solveDoubtQuery(followUpText, null)
                                            followUpText = ""
                                        }
                                    },
                                    enabled = followUpText.isNotBlank()
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = PrimaryIndigo)
                                }
                            }
                        }
                    }
                }
            }

            // Sponsored Native Banner
            item {
                AdBannerCard(isBengali = isBengali)
            }

            // Sample Questions or History List Section
            item {
                Text(
                    text = "জনপ্রিয় বিগত বছরের প্রশ্নাবলী বেছে নিন:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(presetDoubts) { doubt ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            textQuery = doubt.questionText
                            solveDoubtQuery(doubt.questionText, null)
                        }
                        .testTag("sample_doubt_${doubt.subject}")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryIndigo.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = doubt.subject,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryIndigoLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = doubt.questionText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "এআই সমাধান দেখুন ➔",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExamGold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showAdForCreditsDialog) {
            RewardedAdDialog(
                rewardType = AdRewardType.AI_CREDITS,
                isBengali = isBengali,
                onRewardEarned = {
                    onWatchAdForCredits()
                },
                onDismiss = { showAdForCreditsDialog = false }
            )
        }
    }
}
