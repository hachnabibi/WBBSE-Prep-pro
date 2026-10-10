package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ChapterEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TopicEntity
import com.example.data.model.AcademicClass
import com.example.data.model.AcademicClassProvider
import com.example.data.model.EducationBoard
import com.example.data.model.AssessmentMode
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.ui.viewmodel.StructuredQuestionInput
import com.example.util.formatCompactNumber
import com.example.data.firestore.FirestorePodcast
import com.example.notification.AudioPodcastService
import com.example.util.AudioPodcastManager

private fun getAdminClassSubjects(classId: String): List<SubjectEntity> {
    return when {
        classId.contains("wbchse") || classId.contains("class_11") || classId.contains("class_12") -> listOf(
            SubjectEntity("${classId}_bengali", "bengali", "বাংলা (Bengali)", "Bengali", 10, 0, 0, 0xFFE11D48L, classId, "common"),
            SubjectEntity("${classId}_english", "english", "ইংরেজি (English)", "English", 10, 0, 0, 0xFF2563EBL, classId, "common"),
            SubjectEntity("${classId}_physics", "physics", "পদার্থবিদ্যা (Physics)", "Physics", 10, 0, 0, 0xFF7C3AEDL, classId, "science"),
            SubjectEntity("${classId}_chemistry", "chemistry", "রসায়ন (Chemistry)", "Chemistry", 10, 0, 0, 0xFF059669L, classId, "science"),
            SubjectEntity("${classId}_mathematics", "mathematics", "গণিত (Mathematics)", "Mathematics", 10, 0, 0, 0xFFD97706L, classId, "science"),
            SubjectEntity("${classId}_biology", "biology", "জীববিজ্ঞান (Biological Science)", "Biology", 10, 0, 0, 0xFF16A34AL, classId, "science"),
            SubjectEntity("${classId}_history", "history", "ইতিহাস (History)", "History", 10, 0, 0, 0xFFD97706L, classId, "arts"),
            SubjectEntity("${classId}_geography", "geography", "ভূগোল (Geography)", "Geography", 10, 0, 0, 0xFF0284C7L, classId, "arts"),
            SubjectEntity("${classId}_pol_science", "political_science", "রাষ্ট্রবিজ্ঞান (Political Science)", "Political Science", 10, 0, 0, 0xFF8B5CF6L, classId, "arts"),
            SubjectEntity("${classId}_accountancy", "accountancy", "হিসাবশাস্ত্র (Accountancy)", "Accountancy", 10, 0, 0, 0xFF0D9488L, classId, "commerce")
        )
        classId.contains("wbbpe") -> listOf(
            SubjectEntity("${classId}_bengali", "bengali", "আমার বই / পাতাবাহার (বাংলা)", "Bengali", 5, 0, 0, 0xFFE11D48L, classId, "common"),
            SubjectEntity("${classId}_english", "english", "Butterfly / English", "English", 5, 0, 0, 0xFF2563EBL, classId, "common"),
            SubjectEntity("${classId}_math", "mathematics", "আমার গণিত (Mathematics)", "Mathematics", 5, 0, 0, 0xFF059669L, classId, "common"),
            SubjectEntity("${classId}_environment", "environment", "আমাদের পরিবেশ (Environment)", "Environment", 5, 0, 0, 0xFF16A34AL, classId, "common")
        )
        else -> listOf(
            SubjectEntity("${classId}_bengali", "bengali", "প্রথম ভাষা বাংলা (Bengali)", "Bengali", 10, 0, 0, 0xFFE11D48L, classId, "common"),
            SubjectEntity("${classId}_english", "english", "দ্বিতীয় ভাষা ইংরেজি (English)", "English", 10, 0, 0, 0xFF2563EBL, classId, "common"),
            SubjectEntity("${classId}_mathematics", "mathematics", "গণিত (Mathematics)", "Mathematics", 10, 0, 0, 0xFF059669L, classId, "common"),
            SubjectEntity("${classId}_physical_science", "physical_science", "ভৌত বিজ্ঞান (Physical Science)", "Physical Science", 10, 0, 0, 0xFF7C3AEDL, classId, "common"),
            SubjectEntity("${classId}_life_science", "life_science", "জীবন বিজ্ঞান (Life Science)", "Life Science", 10, 0, 0, 0xFF16A34AL, classId, "common"),
            SubjectEntity("${classId}_history", "history", "ইতিহাস (History)", "History", 10, 0, 0, 0xFFD97706L, classId, "common"),
            SubjectEntity("${classId}_geography", "geography", "ভূগোল (Geography)", "Geography", 10, 0, 0, 0xFF0284C7L, classId, "common")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUid = viewModel.authRepository.currentUserState?.uid
    val isAdmin = true // Temporary public access, user-based restrictions will be added later

    if (!isAdmin) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Access Denied",
                tint = WrongRed,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "অননুমোদিত প্রবেশাধিকার (Access Denied)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "শুধুমাত্র অনুমোদিত অ্যাডমিন (Admin ID: juVeT3Z6WDcKhgO4a6kaU7kkbm43) এই প্যানেলটি ব্যবহার করতে পারবেন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ফিরে যান")
            }
        }
        return
    }

    val isLoading by viewModel.isAuthLoading.collectAsState()
    val authSuccess by viewModel.authSuccessMessage.collectAsState()
    val authError by viewModel.authErrorMessage.collectAsState()

    val autoSyncStatus by viewModel.autoSyncStatus.collectAsState()
    val backupState by viewModel.cloudBackupState.collectAsState()
    val cloudStats by viewModel.cloudStats.collectAsState()
    val isPulling by viewModel.isPullingFromCloud.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLocalDatabaseStats()
        viewModel.checkCloudDatabaseIntegrity()
    }

    val liveBoards by viewModel.allBoards.collectAsState()
    val liveClasses by viewModel.allClasses.collectAsState()

    val firestoreQuestionsList by viewModel.firestoreQuestions.collectAsState()
    val firestoreTestTemplatesList by viewModel.firestoreTestTemplates.collectAsState()

    var cmsQuestionSearchQuery by remember { mutableStateOf("") }
    var editingQuestion by remember { mutableStateOf<com.example.data.firestore.FirestoreQuestion?>(null) }
    var deletingQuestion by remember { mutableStateOf<com.example.data.firestore.FirestoreQuestion?>(null) }
    var deletingTestTemplate by remember { mutableStateOf<com.example.data.firestore.FirestoreTestTemplate?>(null) }

    var selectedTab by remember { mutableStateOf(0) }

    val authUser by viewModel.authUserState.collectAsState()
    var showEditAdminDialog by remember { mutableStateOf(false) }
    var editAdminNameText by remember { mutableStateOf("") }
    var isSavingAdminProfile by remember { mutableStateOf(false) }

    var customTestTitle by remember { mutableStateOf("গণিত স্পেশাল মক টেস্ট") }
    var customTestId by remember { mutableStateOf("math_test_01") }
    var customTestTimeMins by remember { mutableStateOf("20") }
    var testQuestionText by remember { mutableStateOf("") }
    var testOpt1 by remember { mutableStateOf("") }
    var testOpt2 by remember { mutableStateOf("") }
    var testOpt3 by remember { mutableStateOf("") }
    var testOpt4 by remember { mutableStateOf("") }
    var testCorrectIndex by remember { mutableStateOf(0) }
    var testExplanation by remember { mutableStateOf("") }
    var testDifficulty by remember { mutableStateOf("MEDIUM") }
    var selectedAssessmentMode by remember { mutableStateOf(AssessmentMode.MOCK_TEST) }

    var structuredTestType by remember { mutableStateOf("CHAPTER_ASSESSMENT") }
    var structuredTestTitle by remember { mutableStateOf("অধ্যায় মূল্যায়ন টেস্ট") }
    var structuredTestDuration by remember { mutableStateOf("25") }
    var structuredTestMarks by remember { mutableStateOf("25") }
    var structuredQuestionText by remember { mutableStateOf("") }
    var structuredOpt1 by remember { mutableStateOf("") }
    var structuredOpt2 by remember { mutableStateOf("") }
    var structuredOpt3 by remember { mutableStateOf("") }
    var structuredOpt4 by remember { mutableStateOf("") }
    var structuredCorrectIndex by remember { mutableStateOf(0) }
    var structuredExplanation by remember { mutableStateOf("") }
    var structuredStagedQuestions by remember { mutableStateOf<List<StructuredQuestionInput>>(emptyList()) }
    var lastCreatedTestId by remember { mutableStateOf<String?>(null) }
    var lastCreatedTestTitle by remember { mutableStateOf<String?>(null) }

    var bulkImportText by remember { mutableStateOf("") }
    var bulkCustomTestTitle by remember { mutableStateOf("") }
    var bulkPyqYearInput by remember { mutableStateOf("") }
    var bulkImportType by remember { mutableIntStateOf(0) } // 0 for Questions, 1 for Chapters/Topics

    val sampleCurriculumJsonTemplate = """[
  {
    "chapterNumber": 1,
    "title": "বল ও গতি",
    "description": "গতির সমীকরণ ও বলের ধারণা",
    "topics": [
      {
        "orderIndex": 1,
        "title": "গতির প্রথম সমীকরণ",
        "summary": "গতির প্রথম সমীকরণটি হলো v = u + at",
        "keyPoints": [
          "v হলো অন্তিম বেগ",
          "u হলো প্রাথমিক বেগ",
          "a হলো ত্বরণ ও t হলো সময়"
        ],
        "formulae": [
          "v = u + at"
        ],
        "contentData": "গতির প্রথম সমীকরণ প্রতিষ্ঠা করার জন্য ধরি একটি কণা u বেগে যাত্রা শুরু করল...",
        "videoUrl": "https://youtu.be/example1"
      }
    ]
  }
]"""

    val sampleCurriculumCsvTemplate = """chapterNumber | title | description | orderIndex | topicTitle | summary | keyPoints | formulae | contentData | videoUrl
1 | বল ও গতি | গতির সমীকরণ ও বলের ধারণা | 1 | গতির প্রথম সমীকরণ | গতির প্রথম সমীকরণটি হলো v = u + at | v হলো অন্তিম বেগ # u হলো প্রাথমিক বেগ # a হলো ত্বরণ ও t হলো সময় | v = u + at | গতির প্রথম সমীকরণ প্রতিষ্ঠা করার জন্য ধরি একটি কণা u বেগে যাত্রা শুরু করল... | https://youtu.be/example1"""

    val sampleJsonTemplate = """[
  {
    "question": "সালোকসংশ্লেষ প্রক্রিয়ায় নির্গত উপজাত গ্যাসের নাম কী?",
    "options": [
      "অক্সিজেন (O2)",
      "কার্বন ডাই অক্সাইড (CO2)",
      "নাইট্রোজেন (N2)",
      "হাইড্রোজেন (H2)"
    ],
    "correctOptionIndex": 0,
    "explanation": "সালোকসংশ্লেষে জল বিশ্লিষ্ট হয়ে উপজাত পদার্থ হিসেবে অক্সিজেন গ্যাস নির্গত হয়।",
    "difficulty": "EASY",
    "marks": 1,
    "pyqYear": "2024"
  },
  {
    "question": "মানবদেহের দীর্ঘতম স্নায়ু (Nerve) কোনটি?",
    "options": [
      "ভেগাস স্নায়ু",
      "অপটিক স্নায়ু",
      "সায়াটিক স্নায়ু",
      "অডিটরি স্নায়ু"
    ],
    "correctOptionIndex": 2,
    "explanation": "সায়াটিক নার্ভ (Sciatic Nerve) হলো মানবদেহের সবচেয়ে দীর্ঘতম ও মোটা স্নায়ু।",
    "difficulty": "MEDIUM",
    "marks": 1,
    "pyqYear": "2023"
  },
  {
    "question": "একটি বৃত্তের ব্যাসার্ধ r হলে, বৃত্তের ক্ষেত্রফলের সূত্র কোনটি?",
    "options": [
      "2πr",
      "πr²",
      "2πr²",
      "4/3 πr³"
    ],
    "correctOptionIndex": 1,
    "explanation": "বৃত্তের ক্ষেত্রফল = π × (ব্যাসার্ধ)² = πr² বর্গ একক।",
    "difficulty": "EASY",
    "marks": 1,
    "pyqYear": "2025"
  }
]"""

    val sampleCsvTemplate = """উদ্ভিদের জল সংবহনকারী কলা কোনটি? | জাইলেম | ফ্লোয়েম | ক্যামবিয়াম | কর্টেক্স | 0 | জাইলেম কলার মাধ্যমে জল সংবহন হয়।
কোষের শক্তিঘর বলা হয় কাকে? | গলগি বডি | মাইটোকন্ড্রিয়া | নিউক্লিয়াস | রাইবোজোম | 1 | মাইটোকন্ড্রিয়ায় ATP উৎপন্ন হয়।"""

    // 1. Board Selector
    val allBoards = remember { com.example.data.model.AcademicClassProvider.ALL_BOARDS }
    var selectedBoard by remember { mutableStateOf(allBoards.find { it.id == "WBBSE" } ?: allBoards.first()) }
    var boardDropdownExpanded by remember { mutableStateOf(false) }

    // 2. Class Selector (Filtered by Selected Board)
    val allClasses = remember { com.example.data.model.AcademicClassProvider.ALL_CLASSES }
    val classesForBoard = remember(selectedBoard, allClasses) {
        allClasses.filter { it.board.equals(selectedBoard.id, ignoreCase = true) }
    }
    var selectedClass by remember(selectedBoard) {
        mutableStateOf(classesForBoard.firstOrNull { it.id == "class_10_wbbse" } ?: classesForBoard.firstOrNull() ?: allClasses.first())
    }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    // 3. SubClass / Stream Selector (Condition: Class 11, 12 or WBCHSE)
    val hasStreams = remember(selectedClass) {
        selectedClass.board.equals("WBCHSE", ignoreCase = true) || selectedClass.classNumber in 11..12
    }
    val allStreams = remember { com.example.data.model.AcademicStreams.LIST }
    var selectedStream by remember(selectedClass) {
        mutableStateOf(if (hasStreams) com.example.data.model.AcademicStreams.ALL else null)
    }
    var streamDropdownExpanded by remember { mutableStateOf(false) }

    // Dynamic Room database states
    var dbSubjects by remember { mutableStateOf<List<SubjectEntity>>(emptyList()) }
    var dbChapters by remember { mutableStateOf<List<ChapterEntity>>(emptyList()) }
    var dbTopics by remember { mutableStateOf<List<TopicEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            dbSubjects = viewModel.repository.dao.getAllSubjectsOnce()
            dbChapters = viewModel.repository.dao.getAllChaptersOnce()
            dbTopics = viewModel.repository.dao.getAllTopicsOnce()
        } catch (e: Exception) {
            android.util.Log.e("AdminPanelScreen", "Error loading DB entities: ${e.message}", e)
        }
    }

    // Lookup subjects from Room or fallback
    val getAdminClassSubjectsHelper: (String) -> List<SubjectEntity> = { cid ->
        val filtered = dbSubjects.filter { it.classId == cid }
        if (filtered.isNotEmpty()) filtered else getAdminClassSubjects(cid)
    }

    // 4. Subject Selector: Local / Asset Data -> Firestore Fallback
    val classSubjects = remember(selectedClass, selectedStream, dbSubjects) {
        val baseSubs = getAdminClassSubjectsHelper(selectedClass.id)
        if (selectedStream != null && selectedStream!!.id != "all") {
            baseSubs.filter { it.stream == "common" || it.stream.equals(selectedStream!!.id, ignoreCase = true) }
        } else {
            baseSubs
        }
    }

    var selectedSubject by remember(selectedClass, selectedStream) {
        mutableStateOf(classSubjects.first())
    }

    LaunchedEffect(selectedSubject) {
        viewModel.selectSubject(selectedSubject.id)
    }

    val allChapters by viewModel.chapters.collectAsState()
    val allTopics by viewModel.topics.collectAsState()

    val effectiveChapters = if (dbChapters.isNotEmpty()) dbChapters else allChapters
    val effectiveTopics = if (dbTopics.isNotEmpty()) dbTopics else allTopics

    // 5. Chapter Selector: Local First -> Firestore Fallback
    val filteredChapters = remember(selectedSubject, selectedClass, effectiveChapters, firestoreQuestionsList) {
        val localChs = effectiveChapters.filter { it.subjectId == selectedSubject.id || it.subjectId == selectedSubject.code }
        if (localChs.isNotEmpty()) {
            localChs
        } else {
            // Local data unavailable -> Firestore fallback
            val fsChIds = firestoreQuestionsList
                .filter { (it.classId == selectedClass.id || it.classId.isEmpty()) && (it.subjectId == selectedSubject.id || it.subjectId == selectedSubject.code) && it.chapterId.isNotBlank() }
                .map { it.chapterId }
                .distinct()
            fsChIds.mapIndexed { idx, chId ->
                ChapterEntity(
                    id = chId,
                    subjectId = selectedSubject.id,
                    chapterNumber = idx + 1,
                    title = chId.replace("_", " ").replace("-", " ").uppercase(),
                    description = "Firestore Chapter",
                    totalTopics = 1,
                    completedTopics = 0,
                    bossUnlocked = true,
                    bossCompleted = false,
                    masteryPercentage = 0
                )
            }
        }
    }

    var selectedChapter by remember(selectedSubject, selectedClass, filteredChapters) {
        mutableStateOf(filteredChapters.firstOrNull())
    }

    var selectedStructuredChapter by remember(filteredChapters) {
        mutableStateOf(filteredChapters.firstOrNull())
    }

    // 6. Topic Selector: Local First -> Firestore Fallback
    val filteredTopics = remember(selectedChapter, allTopics, firestoreQuestionsList) {
        val chId = selectedChapter?.id ?: ""
        if (chId.isNotBlank()) {
            val localTps = allTopics.filter { it.chapterId == chId }
            if (localTps.isNotEmpty()) {
                localTps
            } else {
                // Local data unavailable -> Firestore fallback
                val fsTpIds = firestoreQuestionsList
                    .filter { it.chapterId == chId && it.topicId.isNotBlank() }
                    .map { it.topicId }
                    .distinct()
                fsTpIds.mapIndexed { idx, tpId ->
                    TopicEntity(
                        id = tpId,
                        chapterId = chId,
                        subjectId = selectedSubject.id,
                        orderIndex = idx + 1,
                        title = tpId.replace("_", " ").replace("-", " ").uppercase(),
                        summary = "Firestore Topic",
                        keyPointsJson = "",
                        isUnlocked = true,
                        masteryPercentage = 0,
                        questionCount = 10
                    )
                }
            }
        } else {
            emptyList()
        }
    }

    var selectedTopic by remember(selectedChapter, filteredTopics) {
        mutableStateOf(filteredTopics.firstOrNull())
    }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }
    var topicDropdownExpanded by remember { mutableStateOf(false) }
    var pyqDropdownExpanded by remember { mutableStateOf(false) }

    var cmsClassDropdownExpanded by remember { mutableStateOf(false) }
    var cmsSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var testCmsClassDropdownExpanded by remember { mutableStateOf(false) }
    var testCmsSubjectDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showJsonGuideDialog by remember { mutableStateOf(false) }
    var bulkBoardDropdownExpanded by remember { mutableStateOf(false) }
    var bulkClassDropdownExpanded by remember { mutableStateOf(false) }
    var bulkSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var bulkChapterDropdownExpanded by remember { mutableStateOf(false) }
    var bulkTopicDropdownExpanded by remember { mutableStateOf(false) }
    var bulkPyqDropdownExpanded by remember { mutableStateOf(false) }
    var bulkFileStatusMessage by remember { mutableStateOf<String?>(null) }
    var bulkFileStatusSuccess by remember { mutableStateOf(true) }

    val jsonFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                    reader.readText()
                } ?: ""
                if (content.isNotBlank()) {
                    bulkImportText = content
                    bulkFileStatusMessage = "✓ JSON ফাইল সফলভাবে লোড হয়েছে!"
                    bulkFileStatusSuccess = true
                } else {
                    bulkFileStatusMessage = "⚠️ ফাইলটি ফাঁকা!"
                    bulkFileStatusSuccess = false
                }
            } catch (e: Exception) {
                bulkFileStatusMessage = "ফাইল পড়তে সমস্যা হয়েছে: ${e.message}"
                bulkFileStatusSuccess = false
            }
        }
    }

    val downloadQuestionJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(sampleJsonTemplate.toByteArray(Charsets.UTF_8))
                }
                bulkFileStatusMessage = "✓ নমুনা প্রশ্ন JSON ফাইল সফলভাবে ডাউনলোড / সেভ হয়েছে!"
                bulkFileStatusSuccess = true
            } catch (e: Exception) {
                bulkFileStatusMessage = "ফাইল সেভ করতে সমস্যা হয়েছে: ${e.message}"
                bulkFileStatusSuccess = false
            }
        }
    }

    val downloadSyllabusJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(sampleCurriculumJsonTemplate.toByteArray(Charsets.UTF_8))
                }
                bulkFileStatusMessage = "✓ নমুনা সিলেবাস JSON ফাইল সফলভাবে ডাউনলোড / সেভ হয়েছে!"
                bulkFileStatusSuccess = true
            } catch (e: Exception) {
                bulkFileStatusMessage = "ফাইল সেভ করতে সমস্যা হয়েছে: ${e.message}"
                bulkFileStatusSuccess = false
            }
        }
    }
    var customClassDropdownExpanded by remember { mutableStateOf(false) }
    var customSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var structuredBoardDropdownExpanded by remember { mutableStateOf(false) }
    var structuredClassDropdownExpanded by remember { mutableStateOf(false) }
    var structuredSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var structuredChapterDropdownExpanded by remember { mutableStateOf(false) }

    var customQuestionId by remember { mutableStateOf("") }
    var questionText by remember { mutableStateOf("") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var opt3 by remember { mutableStateOf("") }
    var opt4 by remember { mutableStateOf("") }
    var selectedCorrectIndex by remember { mutableIntStateOf(0) }
    var explanationText by remember { mutableStateOf("") }
    var pyqYear by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("EASY") }
    var podcastUrlText by remember { mutableStateOf("") }
    var podcastTitleText by remember { mutableStateOf("") }

    val defaultGeneratedId by remember(selectedSubject, selectedChapter, pyqYear) {
        derivedStateOf {
            val cleanSub = selectedSubject.id.replace("subject_", "").replace("-", "_").take(8)
            val cleanCh = selectedChapter?.id?.replace("chapter_", "")?.replace("-", "_")?.take(8) ?: "ch_0"
            if (pyqYear.isNotBlank()) {
                "q_pyq_${pyqYear}_${cleanSub}_01"
            } else {
                "q_${cleanSub}_${cleanCh}_01"
            }
        }
    }

    var actionMessage by remember { mutableStateOf<String?>(null) }
    var actionSuccess by remember { mutableStateOf(true) }

    val pyqYearOptions = remember(isBengali) {
        listOf(
            "" to ("নিয়মিত প্রশ্ন"),
            "2025" to ("২০২৫ সাল"),
            "2024" to ("২০২৪ সাল"),
            "2023" to ("২০২৩ সাল"),
            "2022" to ("২০২২ সাল"),
            "2021" to ("২০২১ সাল"),
            "2020" to ("২০২০ সাল"),
            "2019" to ("২০১৯ সাল"),
            "2018" to ("২০১৮ সাল"),
            "2017" to ("২০১৭ সাল"),
            "2016" to ("২০১৬ সাল"),
            "2015" to ("২০১৫ সাল")
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                drawerTonalElevation = 4.dp,
                modifier = Modifier.width(320.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Column {
                        Text(
                            text = "🛠️ অ্যাডমিন প্যানেল",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ফায়ারবেস ক্লাউড ম্যানেজার (Firestore CMS)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content Column (Only Menu Items)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 12.dp)
                ) {
                    val drawerItems = listOf(
                        Triple("📊 ক্লাউড ব্যাকআপ ও ড্যাশবোর্ড", "📊 Cloud Backup & Dashboard", Icons.Default.CloudSync),
                        Triple("✍️ নতুন প্রশ্ন সংযোজন", "✍️ Add New Question", Icons.Default.PlaylistAdd),
                        Triple("📋 প্রশ্ন ব্যাংক ম্যানেজমেন্ট", "📋 Question Bank CMS", Icons.Default.MenuBook),
                        Triple("📝 কাস্টম মক টেস্ট ক্রিয়েটর", "📝 Custom Mock Test Creator", Icons.Default.Quiz),
                        Triple("🎯 লাইভ এক্সাম ক্রিয়েটর", "🎯 Live Exam Creator", Icons.Default.Stars),
                        Triple("📑 টেস্ট টেমপ্লেট ম্যানেজমেন্ট", "📑 Test Template CMS", Icons.Default.Assignment),
                        Triple("📦 বাল্ক ইমপোর্ট", "📦 Bulk Import (CSV/JSON)", Icons.Default.FileUpload),
                        Triple("🏫 কারিকুলাম ম্যানেজার", "🏫 Curriculum Manager", Icons.Default.AddBusiness),
                        Triple("📖 অধ্যায় ও টপিক ম্যানেজমেন্ট", "📖 Chapter & Topic Management", Icons.Default.FolderSpecial),
                        Triple("⚡ ফায়ারস্টোর ইনডেক্স ম্যানেজার", "⚡ Firestore Index Manager", Icons.Default.FlashOn),
                        Triple("🎙️ অডিও পডকাস্ট ম্যানেজমেন্ট", "🎙️ Audio Podcast CMS", Icons.Default.Audiotrack)
                    )

                    drawerItems.forEachIndexed { index, (bengaliTitle, englishTitle, icon) ->
                        val isSelected = selectedTab == index
                        NavigationDrawerItem(
                            icon = { Icon(icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                            label = { 
                                Text(
                                    text = if (isBengali) bengaliTitle else englishTitle,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.5.sp
                                ) 
                            },
                            selected = isSelected,
                            onClick = {
                                selectedTab = index
                                scope.launch { drawerState.close() }
                                if (index == 2) viewModel.fetchFirestoreQuestions()
                                if (index == 5) viewModel.fetchFirestoreTestTemplates()
                                if (index == 10) viewModel.fetchCustomPodcasts()
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }

                // Fixed Bottom Area (Always visible at the bottom of the sidebar)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(bottom = 16.dp)
                ) {
                    // Divider separating menus from admin card
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Admin Info & Edit Profile Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                                        .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Admin Avatar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = authUser?.displayName ?: (if (isBengali) "অ্যাডমিন ইউজার" else "Admin User"),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = authUser?.email ?: "admin@wbbseprep.com",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    editAdminNameText = authUser?.displayName ?: ""
                                    showEditAdminDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Admin",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Close Drawer Button
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "ফিরে যান (Exit Admin)" else "Exit Admin Dashboard",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                val (screenTitle, screenSubtitle) = when (selectedTab) {
                    0 -> "ক্লাউড ব্যাকআপ ও ড্যাশবোর্ড" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    1 -> "নতুন প্রশ্ন সংযোজন" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    2 -> "প্রশ্ন ব্যাংক ম্যানেজমেন্ট" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    3 -> "কাস্টম মক টেস্ট ক্রিয়েটর" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    4 -> "লাইভ এক্সাম ক্রিয়েটর" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    5 -> "টেস্ট টেমপ্লেট ব্যবস্থাপনা" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    6 -> "বাল্ক ইমপোর্ট" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    7 -> "কারিকুলাম ম্যানেজার" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    8 -> "অধ্যায় ও টপিক ম্যানেজার" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    9 -> "ফায়ারস্টোর ইনডেক্স ম্যানেজার" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    10 -> "🎙️ পডকাস্ট ব্যবস্থাপনা" to "অডিও লেকচার ও রিভিশন হাব"
                    else -> "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল" to "ম্যানেজমেন্ট মডিউল"
                }

                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = screenTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (selectedTab == 10) 15.sp else 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = screenSubtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp
                                ),
                                color = if (selectedTab == 10) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        if (selectedTab != 0) {
                            IconButton(onClick = { selectedTab = 0 }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "ড্যাশবোর্ডে ফিরুন",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open drawer menu")
                            }
                        }
                    },
                    actions = {
                        if (selectedTab == 10) {
                            IconButton(
                                onClick = {
                                    viewModel.fetchCustomPodcasts()
                                    actionSuccess = true
                                    actionMessage = "🔄 ক্লাউড থেকে পডকাস্ট তালিকা রিফ্রেশ করা হয়েছে।"
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "রিফ্রেশ",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (selectedTab != 0 && selectedTab != 10) {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "মেনু ড্রয়ার",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Exit Admin",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { innerPadding ->
            BackHandler(enabled = selectedTab != 0) {
                selectedTab = 0
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val displayMessage = actionMessage ?: authSuccess ?: authError
                if (displayMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (actionSuccess) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (actionSuccess) CorrectGreen else WrongRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (actionSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (actionSuccess) CorrectGreen else WrongRed
                            )
                            Text(
                                text = displayMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { actionMessage = null; viewModel.clearAuthMessages() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    val localStats = backupState.stats
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Top Header with Quick Refresh
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(PrimaryIndigo.copy(alpha = 0.18f), ExamCyan.copy(alpha = 0.10f))
                                                )
                                            )
                                            .border(1.dp, PrimaryIndigo.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CloudSync,
                                            contentDescription = null,
                                            tint = PrimaryIndigo,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "লোকাল ডেটাবেজ ক্লাউড ব্যাকআপ",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "বোর্ড, ক্লাস, বিষয়, অধ্যায় ও প্রশ্নমালা ক্লাউডে পুশ করুন",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.loadLocalDatabaseStats() },
                                    enabled = !backupState.isBackingUp,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Refresh stats",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                            // Current Local Database Counts
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Storage,
                                                contentDescription = null,
                                                tint = PrimaryIndigo,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "সংরক্ষিত ডেটা পরিসংখ্যান",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        val totalItems = (localStats?.boardCount ?: 0) +
                                                (localStats?.classCount ?: 0) +
                                                (localStats?.subjectCount ?: 0) +
                                                (localStats?.chapterCount ?: 0) +
                                                (localStats?.topicCount ?: 0) +
                                                (localStats?.questionCount ?: 0)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryIndigo.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Analytics,
                                                    contentDescription = null,
                                                    tint = PrimaryIndigo,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = "${formatCompactNumber(totalItems)} টি আইটেম",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryIndigo
                                                )
                                            }
                                        }
                                    }

                                    // Badges Row 1: বোর্ড, ক্লাস, বিষয়
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AdminStatGradientBadge(
                                            count = formatCompactNumber(localStats?.boardCount ?: 0),
                                            label = "বোর্ড",
                                            icon = Icons.Default.AccountBalance,
                                            gradientColors = listOf(Color(0xFF6366F1).copy(alpha = 0.16f), Color(0xFF4338CA).copy(alpha = 0.05f)),
                                            borderColor = Color(0xFF6366F1).copy(alpha = 0.4f),
                                            accentColor = Color(0xFF4F46E5),
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminStatGradientBadge(
                                            count = formatCompactNumber(localStats?.classCount ?: 0),
                                            label = "ক্লাস",
                                            icon = Icons.Default.School,
                                            gradientColors = listOf(Color(0xFF10B981).copy(alpha = 0.16f), Color(0xFF059669).copy(alpha = 0.05f)),
                                            borderColor = Color(0xFF10B981).copy(alpha = 0.4f),
                                            accentColor = Color(0xFF059669),
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminStatGradientBadge(
                                            count = formatCompactNumber(localStats?.subjectCount ?: 0),
                                            label = "বিষয়",
                                            icon = Icons.Default.MenuBook,
                                            gradientColors = listOf(Color(0xFFF59E0B).copy(alpha = 0.16f), Color(0xFFD97706).copy(alpha = 0.05f)),
                                            borderColor = Color(0xFFF59E0B).copy(alpha = 0.4f),
                                            accentColor = Color(0xFFD97706),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Badges Row 2: অধ্যায়, টপিক, প্রশ্ন
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AdminStatGradientBadge(
                                            count = formatCompactNumber(localStats?.chapterCount ?: 0),
                                            label = "অধ্যায়",
                                            icon = Icons.Default.BookmarkBorder,
                                            gradientColors = listOf(Color(0xFF06B6D4).copy(alpha = 0.16f), Color(0xFF0284C7).copy(alpha = 0.05f)),
                                            borderColor = Color(0xFF06B6D4).copy(alpha = 0.4f),
                                            accentColor = Color(0xFF0284C7),
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminStatGradientBadge(
                                            count = formatCompactNumber(localStats?.topicCount ?: 0),
                                            label = "টপিক",
                                            icon = Icons.Default.Layers,
                                            gradientColors = listOf(Color(0xFFA855F7).copy(alpha = 0.16f), Color(0xFF7C3AED).copy(alpha = 0.05f)),
                                            borderColor = Color(0xFFA855F7).copy(alpha = 0.4f),
                                            accentColor = Color(0xFF7C3AED),
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminStatGradientBadge(
                                            count = formatCompactNumber(localStats?.questionCount ?: 0),
                                            label = "প্রশ্ন",
                                            icon = Icons.Default.Quiz,
                                            gradientColors = listOf(Color(0xFFF43F5E).copy(alpha = 0.16f), Color(0xFFE11D48).copy(alpha = 0.05f)),
                                            borderColor = Color(0xFFF43F5E).copy(alpha = 0.4f),
                                            accentColor = Color(0xFFE11D48),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Last backup status (Compact Note)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        val backupTimeText = if (backupState.lastBackupTimestamp > 0) {
                                            val fmt = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                            "সর্বশেষ ব্যাকআপ: ${fmt.format(java.util.Date(backupState.lastBackupTimestamp))}"
                                        } else {
                                            "সর্বশেষ ব্যাকআপ: এই সেশনে ব্যাকআপ নেওয়া হয়নি"
                                        }
                                        Text(
                                            text = backupTimeText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "💡 ফায়ারবেসে পুশ করলে ডেটাবেজের সকল ডেটা ক্লাউডে স্থায়ীভাবে সংরক্ষিত হবে।",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // In-progress indicator
                            if (backupState.isBackingUp) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = backupState.step,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${(backupState.progress * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { backupState.progress },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                }
                            }

                            // Success Message
                            if (backupState.summary != null && !backupState.isBackingUp) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CorrectGreen.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = backupState.summary ?: "",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CorrectGreen
                                        )
                                    }
                                }
                            }

                            // Error Message
                            if (backupState.error != null && !backupState.isBackingUp) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = WrongRed.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Error, contentDescription = null, tint = WrongRed, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = backupState.error ?: "",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WrongRed
                                        )
                                    }
                                }
                            }

                            // Premium Action Button 1: Push Full Database
                            Button(
                                onClick = {
                                    viewModel.pushLocalDatabaseToFirebase { success, msg ->
                                        actionSuccess = success
                                        actionMessage = msg
                                    }
                                },
                                enabled = !isLoading && !backupState.isBackingUp,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                if (backupState.isBackingUp) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "ফায়ারবেসে ব্যাকআপ হচ্ছে... ${(backupState.progress * 100).toInt()}%",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "ফায়ারবেস ক্লাউডে সম্পূর্ণ ডেটা পুশ করুন",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }

                            // Action Button 2: Sync Question Bank
                            OutlinedButton(
                                onClick = {
                                    viewModel.forceSyncQuestionsToFirebase { success, msg ->
                                        actionSuccess = success
                                        actionMessage = msg
                                    }
                                },
                                enabled = !isLoading && !backupState.isBackingUp,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryIndigo),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                Icon(
                                    Icons.Default.Quiz,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "প্রশ্ন ব্যাংক ক্লাউডে সিঙ্ক ও পুশ করুন",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                            }

                            // Quick Action Chip Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = {
                                        viewModel.loadLocalDatabaseStats()
                                        actionSuccess = true
                                        actionMessage = "🔄 লোকাল ডেটাবেজ পরিসংখ্যান সফলভাবে রিফ্রেশ হয়েছে।"
                                    },
                                    label = { Text("পরিসংখ্যান রিফ্রেশ", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryIndigo)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.25f)),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = PrimaryIndigo.copy(alpha = 0.06f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                AssistChip(
                                    onClick = {
                                        actionSuccess = true
                                        actionMessage = "🟢 ক্লাউড ফায়ারস্টোর সংযোগ সক্রিয় ও রেডি রয়েছে!"
                                    },
                                    label = { Text("ক্লাউড সংযোগ স্ট্যাটাস", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    leadingIcon = {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = CorrectGreen)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.25f)),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = CorrectGreen.copy(alpha = 0.06f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                // 2. Cloud Restore / Pull & Health Monitor Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(CorrectGreen.copy(alpha = 0.20f), ExamCyan.copy(alpha = 0.10f))
                                                )
                                            )
                                            .border(1.dp, CorrectGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = CorrectGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "ক্লাউড থেকে রিস্টোর ও সিঙ্ক",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "ক্লাউড ফায়ারস্টোর থেকে সিলেবাস ডিভাইসে নামিয়ে নিন",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.checkCloudDatabaseIntegrity() },
                                    enabled = !cloudStats.isChecking,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        Icons.Default.CloudQueue,
                                        contentDescription = "Check Cloud",
                                        tint = CorrectGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                            // Cloud Stats Summary Surface
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ফায়ারস্টোর ক্লাউড ডেটা",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = CorrectGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (cloudStats.isOnline) "🟢 অনলাইন" else "🔴 অফলাইন",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CorrectGreen,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("বোর্ড: ${formatCompactNumber(cloudStats.boardCount)}", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("ক্লাস: ${formatCompactNumber(cloudStats.classCount)}", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("বিষয়: ${formatCompactNumber(cloudStats.subjectCount)}", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("প্রশ্ন: ${formatCompactNumber(cloudStats.questionCount)}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = CorrectGreen)
                                    }
                                }
                            }

                            // Pull / Restore Button
                            Button(
                                onClick = {
                                    viewModel.pullCloudDatabaseToLocal { success, msg ->
                                        actionSuccess = success
                                        actionMessage = msg
                                    }
                                },
                                enabled = !isPulling,
                                colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                if (isPulling) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "ক্লাউড থেকে ডেটা নামানো হচ্ছে...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "ক্লাউড থেকে লোকাল ডেটাবেজে রিস্টোর করুন",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Audio Podcast CMS Quick Hub Card
                item {
                    val pdsList by viewModel.customPodcasts.collectAsState()
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(PrimaryIndigo.copy(alpha = 0.20f), ExamGold.copy(alpha = 0.15f))
                                                )
                                            )
                                            .border(1.dp, PrimaryIndigo.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Audiotrack,
                                            contentDescription = null,
                                            tint = PrimaryIndigo,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "🎙️ অডিও পডকাস্ট সিএমএস হাব",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "লেকচার অডিও, রিভিশন সামারি ও পডকাস্ট কন্টেন্ট পরিচালনা করুন",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "${pdsList.size} টি পডকাস্ট",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryIndigo,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        selectedTab = 10
                                        viewModel.fetchCustomPodcasts()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(42.dp)
                                ) {
                                    Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("পডকাস্ট সিএমএস খুলুন", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.fetchCustomPodcasts()
                                        actionSuccess = true
                                        actionMessage = "🔄 ক্লাউড থেকে পডকাস্ট তালিকা রিফ্রেশ করা হয়েছে।"
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.4f)),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryIndigo)
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = CorrectGreen)
                            Text(
                                text = "প্রথমে পর্ষদ/বোর্ড ও শ্রেণি নির্বাচন করুন, তারপর বিষয়, অধ্যায় ও অপশন বেছে নিয়ে প্রশ্ন যোগ করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 1. Board Selector
                item {
                    ExposedDropdownMenuBox(
                        expanded = boardDropdownExpanded,
                        onExpandedChange = { boardDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "${selectedBoard.shortName} — ${selectedBoard.classRangeDescription}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("১. পর্ষদ / বোর্ড (Education Board)") },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ExamGold) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boardDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = boardDropdownExpanded,
                            onDismissRequest = { boardDropdownExpanded = false }
                        ) {
                            allBoards.forEach { board ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${board.shortName} (${board.fullNameBengali})", fontWeight = FontWeight.Bold)
                                            Text("📌 শ্রেণি সীমা: ${board.classRangeDescription}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedBoard = board
                                        val clsForB = allClasses.filter { it.board.equals(board.id, ignoreCase = true) }
                                        selectedClass = clsForB.firstOrNull() ?: allClasses.first()
                                        val subs = getAdminClassSubjects(selectedClass.id)
                                        selectedSubject = subs.first()
                                        boardDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Class Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "২. শ্রেণি নির্বাচন করুন:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(classesForBoard) { cls ->
                                val isSelected = selectedClass.id == cls.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedClass = cls
                                        val subs = getAdminClassSubjects(cls.id)
                                        selectedSubject = subs.first()
                                    },
                                    label = {
                                        Text(
                                            text = cls.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ExamGold,
                                        selectedLabelColor = Color(0xFF1E1B2E)
                                    )
                                )
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = classDropdownExpanded,
                            onExpandedChange = { classDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = "${selectedClass.name} (${selectedClass.board})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("শ্রেণি (Class)") },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = ExamGold) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = classDropdownExpanded,
                                onDismissRequest = { classDropdownExpanded = false }
                            ) {
                                classesForBoard.forEach { cls ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(cls.name, fontWeight = FontWeight.Bold)
                                                Text(cls.board, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            selectedClass = cls
                                            val subs = getAdminClassSubjects(cls.id)
                                            selectedSubject = subs.first()
                                            classDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. SubClass / Stream Selector (if applicable)
                if (hasStreams) {
                    item {
                        ExposedDropdownMenuBox(
                            expanded = streamDropdownExpanded,
                            onExpandedChange = { streamDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedStream?.name ?: "সকল বিভাগ (All Streams)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("৩. বিভাগ / শাখা (Stream / SubClass)") },
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = ExamGold) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = streamDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = streamDropdownExpanded,
                                onDismissRequest = { streamDropdownExpanded = false }
                            ) {
                                allStreams.forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedStream = st
                                            val subs = getAdminClassSubjects(selectedClass.id)
                                            val filteredSubs = if (st.id != "all") subs.filter { it.stream == "common" || it.stream.equals(st.id, ignoreCase = true) } else subs
                                            selectedSubject = filteredSubs.firstOrNull() ?: subs.first()
                                            streamDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Subject Selector (Local First -> Firestore Fallback)
                item {
                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedSubject.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("৪. বিষয় নির্বাচন করুন (Subject)") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = ExamGold) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            classSubjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(subject.name, fontWeight = FontWeight.Bold)
                                            if (!isBengali) {
                                                Text(subject.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedSubject = subject
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. Chapter Selector (Local First -> Firestore Fallback)
                item {
                    ExposedDropdownMenuBox(
                        expanded = chapterDropdownExpanded,
                        onExpandedChange = { chapterDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedChapter?.let { "অধ্যায় ${it.chapterNumber}: ${it.title}" } ?: "কোনো অধ্যায় পাওয়া যায়নি (লোকাল/ক্লাউড)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("৫. অধ্যায় নির্বাচন করুন (Chapter)") },
                            leadingIcon = { Icon(Icons.Default.Bookmarks, contentDescription = null, tint = ExamGold) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = chapterDropdownExpanded,
                            onDismissRequest = { chapterDropdownExpanded = false }
                        ) {
                            if (filteredChapters.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("কোনো অধ্যায় পাওয়া যায়নি") },
                                    onClick = { chapterDropdownExpanded = false }
                                )
                            } else {
                                filteredChapters.forEach { chapter ->
                                    DropdownMenuItem(
                                        text = { Text("অধ্যায় ${chapter.chapterNumber}: ${chapter.title}") },
                                        onClick = {
                                            selectedChapter = chapter
                                            val tps = allTopics.filter { it.chapterId == chapter.id }
                                            selectedTopic = tps.firstOrNull()
                                            chapterDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = topicDropdownExpanded,
                        onExpandedChange = { topicDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedTopic?.title ?: "কোনো টপিক নেই",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("৪. টপিক নির্বাচন করুন") },
                            leadingIcon = { Icon(Icons.Default.Topic, contentDescription = null, tint = ExamGold) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = topicDropdownExpanded,
                            onDismissRequest = { topicDropdownExpanded = false }
                        ) {
                            if (filteredTopics.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("কোনো টপিক পাওয়া যায়নি") },
                                    onClick = { topicDropdownExpanded = false }
                                )
                            } else {
                                filteredTopics.forEach { topic ->
                                    DropdownMenuItem(
                                        text = { Text(topic.title) },
                                        onClick = {
                                            selectedTopic = topic
                                            topicDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "৫. বোর্ড পরীক্ষার সাল নির্বাচন করুন:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        ExposedDropdownMenuBox(
                            expanded = pyqDropdownExpanded,
                            onExpandedChange = { pyqDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val currentLabel = pyqYearOptions.find { it.first == pyqYear }?.second ?: ("নিয়মিত প্রশ্ন")
                            OutlinedTextField(
                                value = currentLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("বোর্ড বর্ষ") },
                                leadingIcon = { Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = ExamGold) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pyqDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = pyqDropdownExpanded,
                                onDismissRequest = { pyqDropdownExpanded = false }
                            ) {
                                pyqYearOptions.forEach { (yearCode, yearLabel) ->
                                    DropdownMenuItem(
                                        text = { Text(yearLabel) },
                                        onClick = {
                                            pyqYear = yearCode
                                            pyqDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(listOf("" to ("নিয়মিত"), "2025" to "২০২৫", "2024" to "২০২৪", "2023" to "২০২৩", "2022" to "২০২২", "2020" to "২০২০", "2019" to "২০১৯")) { (code, chipLabel) ->
                                val isSelected = pyqYear == code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { pyqYear = code },
                                    label = { Text(chipLabel, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ExamGold,
                                        selectedLabelColor = Color(0xFF1E1B2E)
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "৬. প্রশ্নের ইউনিক আইডি:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = { customQuestionId = defaultGeneratedId },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp), tint = ExamGold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "অটো-আইডি বসান",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ExamGold
                                )
                            }
                        }
                        OutlinedTextField(
                            value = customQuestionId,
                            onValueChange = { customQuestionId = it },
                            placeholder = { Text(defaultGeneratedId, color = Color.Gray) },
                            label = { Text("কাস্টম আইডি (ঐচ্ছিক - ফাঁকা রাখলে স্বয়ংক্রিয় তৈরি হবে)") },
                            leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = ExamGold) },
                            trailingIcon = {
                                if (customQuestionId.isNotBlank()) {
                                    IconButton(onClick = { customQuestionId = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_question_id"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Text(
                            text = if (isBengali)
                                "💡 আপনি চাইলে নিজের পছন্দমতো আইডি লিখতে পারেন অথবা ঘরটি ফাঁকা রাখলে স্বয়ংক্রিয়ভাবে ক্লিন আইডি বসে যাবে।"
                            else
                                "💡 You can enter your own ID (e.g. q_ben_ch1_01) or leave empty for clean auto-structured ID.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("৭. প্রশ্নের বিবরণ লিখুন:") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "৮. ৪টি বিকল্প অপশন লিখুন:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = opt1,
                            onValueChange = { opt1 = it },
                            label = { Text("অপশন ক (১ম বিকল্প)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = opt2,
                            onValueChange = { opt2 = it },
                            label = { Text("অপশন খ (২য় বিকল্প)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = opt3,
                            onValueChange = { opt3 = it },
                            label = { Text("অপশন গ (৩য় বিকল্প)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = opt4,
                            onValueChange = { opt4 = it },
                            label = { Text("অপশন ঘ (৪র্থ বিকল্প)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "৯. সঠিক উত্তর কোনটি? (সরাসরি ক্লিক করুন):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val optionTags = listOf("A", "B", "C", "D")
                            val bengaliTags = listOf("ক", "খ", "গ", "ঘ")
                            optionTags.forEachIndexed { index, _ ->
                                val isSelected = selectedCorrectIndex == index
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CorrectGreen else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.5.dp, if (isSelected) CorrectGreen else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedCorrectIndex = index }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "অপশন ${bengaliTags[index]}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isSelected) ("✓ সঠিক উত্তর") else ("বিকল্প ${index + 1}"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        val optionTexts = listOf(opt1, opt2, opt3, opt4)
                        val chosenText = optionTexts.getOrNull(selectedCorrectIndex)?.trim().orEmpty()
                        if (chosenText.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CorrectGreen.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "সঠিক উত্তর: $chosenText",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = CorrectGreen,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "১০. প্রশ্নের কঠিনতার মাত্রা:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "EASY" to ("সহজ"),
                                "MEDIUM" to ("মাঝারি"),
                                "HARD" to ("কঠিন")
                            ).forEach { (diffKey, diffLabel) ->
                                val isSelected = difficulty == diffKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { difficulty = diffKey },
                                    label = { Text(diffLabel) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = explanationText,
                        onValueChange = { explanationText = it },
                        label = { Text("১১. প্রশ্নের ব্যাখ্যা বা সমাধান (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = podcastUrlText,
                        onValueChange = { podcastUrlText = it },
                        label = { Text("১২. পডকাস্ট অডিও লিংক / URL (ঐচ্ছিক)") },
                        placeholder = { Text("যেমন: https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.Audiotrack, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }

                item {
                    OutlinedTextField(
                        value = podcastTitleText,
                        onValueChange = { podcastTitleText = it },
                        label = { Text("১৩. পডকাস্ট শিরোনাম (ঐচ্ছিক)") },
                        placeholder = { Text("যেমন: চলতড়িৎ ও ওহমের সূত্রের গাণিতিক শর্টকাট") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val opts = listOf(opt1, opt2, opt3, opt4).filter { it.isNotBlank() }
                            val resolvedCorrectAnswer = opts.getOrNull(selectedCorrectIndex) ?: opt1
                            viewModel.addQuestionToFirestore(
                                classId = selectedClass.id,
                                subjectId = selectedSubject.id,
                                chapterId = selectedChapter?.id ?: "",
                                topicId = selectedTopic?.id ?: "",
                                questionText = questionText,
                                optionsList = opts,
                                correctIndex = selectedCorrectIndex,
                                correctAnswerText = resolvedCorrectAnswer,
                                explanationText = explanationText.ifBlank { "সঠিক উত্তর হলো $resolvedCorrectAnswer।" },
                                difficulty = difficulty,
                                marks = 1,
                                pyqYear = pyqYear,
                                customQuestionId = customQuestionId.ifBlank { null },
                                podcastUrl = podcastUrlText.trim(),
                                podcastTitle = podcastTitleText.trim()
                            ) { success, msg ->
                                actionSuccess = success
                                actionMessage = msg
                                if (success) {
                                    customQuestionId = ""
                                    questionText = ""
                                    opt1 = ""
                                    opt2 = ""
                                    opt3 = ""
                                    opt4 = ""
                                    explanationText = ""
                                    podcastUrlText = ""
                                    podcastTitleText = ""
                                }
                            }
                        },
                        enabled = !isLoading && questionText.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank() && selectedChapter != null && selectedTopic != null,
                        colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("সংরক্ষণ হচ্ছে...", color = Color.White)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "☁️ ${selectedClass.classNumber}ম শ্রেণির প্রশ্ন ক্লাউডে এন্ট্রি করুন",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else if (selectedTab == 2) {
            val allSubjectOption = remember(selectedClass.id) {
                SubjectEntity(
                    id = "ALL",
                    code = "ALL",
                    name = "সকল বিষয়",
                    totalChapters = 0,
                    colorHex = 0xFFEAB308,
                    classId = selectedClass.id
                )
            }

            val subjectDropdownList = remember(classSubjects) {
                listOf(allSubjectOption) + classSubjects
            }

            LaunchedEffect(selectedClass.id, selectedSubject.id) {
                val filterSubject = if (selectedSubject.id == "ALL") null else selectedSubject.id
                viewModel.fetchFirestoreQuestions(selectedClass.id, filterSubject)
            }

            val filteredQuestions = remember(firestoreQuestionsList, cmsQuestionSearchQuery) {
                if (cmsQuestionSearchQuery.isBlank()) {
                    firestoreQuestionsList
                } else {
                    val qLower = cmsQuestionSearchQuery.lowercase()
                    firestoreQuestionsList.filter { q ->
                        q.question.lowercase().contains(qLower) ||
                                q.id.lowercase().contains(qLower) ||
                                q.subjectId.lowercase().contains(qLower) ||
                                q.tags.any { it.lowercase().contains(qLower) }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = ExamGold)
                                    Text(
                                        text = "📋 ফায়ারবেস প্রশ্নমালা ব্যবস্থাপনা",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val filterSub = if (selectedSubject.id == "ALL") null else selectedSubject.id
                                        viewModel.fetchFirestoreQuestions(selectedClass.id, filterSub)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ExamGold)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                ExposedDropdownMenuBox(
                                    expanded = cmsClassDropdownExpanded,
                                    onExpandedChange = { cmsClassDropdownExpanded = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedClass.name,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("শ্রেণি") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cmsClassDropdownExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = cmsClassDropdownExpanded,
                                        onDismissRequest = { cmsClassDropdownExpanded = false }
                                    ) {
                                        allClasses.forEach { cls ->
                                            DropdownMenuItem(
                                                text = { Text(cls.name, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    selectedClass = cls
                                                    val subs = getAdminClassSubjects(cls.id)
                                                    selectedSubject = subs.first()
                                                    cmsClassDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                ExposedDropdownMenuBox(
                                    expanded = cmsSubjectDropdownExpanded,
                                    onExpandedChange = { cmsSubjectDropdownExpanded = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedSubject.name,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("বিষয়") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cmsSubjectDropdownExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = cmsSubjectDropdownExpanded,
                                        onDismissRequest = { cmsSubjectDropdownExpanded = false }
                                    ) {
                                        subjectDropdownList.forEach { subject ->
                                            DropdownMenuItem(
                                                text = { Text(subject.name, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    selectedSubject = subject
                                                    cmsSubjectDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = cmsQuestionSearchQuery,
                                onValueChange = { cmsQuestionSearchQuery = it },
                                label = { Text("🔍 প্রশ্ন অথবা আইডি দিয়ে খুঁজুন...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ExamGold) },
                                trailingIcon = {
                                    if (cmsQuestionSearchQuery.isNotBlank()) {
                                        IconButton(onClick = { cmsQuestionSearchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ExamGold.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isBengali) "মোট ${formatCompactNumber(firestoreQuestionsList.size)} টি প্রশ্ন ক্লাউডে সংরক্ষিত আছে (ফিল্টার করা: ${formatCompactNumber(filteredQuestions.size)} টি)। যেকোনো প্রশ্ন সরাসরি সম্পাদনা বা মুছে ফেলতে পারেন।"
                                    else "Total ${formatCompactNumber(firestoreQuestionsList.size)} questions in Firebase (${formatCompactNumber(filteredQuestions.size)} matching filter). Edit or delete directly.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                if (isLoading && firestoreQuestionsList.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ExamGold)
                        }
                    }
                } else if (filteredQuestions.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(48.dp), tint = ExamGold)
                                Text(
                                    text = "এই ফিল্টারে ফায়ারবেস ক্লাউডে কোনো প্রশ্ন পাওয়া যায়নি",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "নিচের বাটনে চাপ দিলে সমস্ত বিষয়ের প্রশ্ন ক্লাউডে আপলোড হয়ে যাবে।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        viewModel.syncAllDataWithFirebase { success, msg ->
                                            actionSuccess = success
                                            actionMessage = msg
                                            val filterSub = if (selectedSubject.id == "ALL") null else selectedSubject.id
                                            viewModel.fetchFirestoreQuestions(selectedClass.id, filterSub)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF1E1B2E))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "🚀 ফায়ারবেসে প্রশ্নমালা সিঙ্ক করুন",
                                        color = Color(0xFF1E1B2E),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredQuestions) { question ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ExamGold.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, ExamGold.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "ID: ${question.id}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = ExamGold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    val diff = question.difficulty.uppercase()
                                    val (diffBg, diffColor, diffText) = when (diff) {
                                        "EASY" -> Triple(CorrectGreen.copy(alpha = 0.18f), CorrectGreen, "সহজ")
                                        "HARD" -> Triple(WrongRed.copy(alpha = 0.18f), WrongRed, "কঠিন")
                                        else -> Triple(ExamGold.copy(alpha = 0.18f), ExamGold, "মাঝারি")
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = diffBg
                                    ) {
                                        Text(
                                            text = "⚡ $diffText",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                            color = diffColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val subName = when (question.subjectId) {
                                        "bengali", "sub_bengali" -> "বাংলা"
                                        "english", "sub_english" -> "ইংরেজি"
                                        "mathematics", "sub_math" -> "গণিত"
                                        "physical_science", "sub_psc" -> "ভৌত বিজ্ঞান"
                                        "life_science", "sub_lsc" -> "জীবন বিজ্ঞান"
                                        "history", "sub_hist" -> "ইতিহাস"
                                        "geography", "sub_geog" -> "ভূগোল"
                                        else -> question.subjectId.replace("subject_", "").replace("sub_", "").replace("_", " ")
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "📚 $subName",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }

                                    val clsName = when (question.classId) {
                                        "class_10_wbbse" -> "১০ম শ্রেণি"
                                        "class_9_wbbse" -> "৯ম শ্রেণি"
                                        "class_8_wbbse" -> "৮ম শ্রেণি"
                                        "class_7_wbbse" -> "৭ম শ্রেণি"
                                        "class_6_wbbse" -> "৬ষ্ঠ শ্রেণি"
                                        "class_5_wbbse" -> "৫ম শ্রেণি"
                                        "wb_primary_tet" -> "প্রাইমারি টেট"
                                        "wb_upper_primary_tet" -> "আপার টেট"
                                        "wb_slst_pt" -> "এসএলএসটি"
                                        else -> question.classId.replace("class_", "Class ").replace("_wbbse", "")
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "🎓 $clsName",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }

                                    val pyqTag = question.tags.firstOrNull { it.startsWith("PYQ_") || it.contains("202") || it.contains("201") }
                                    if (pyqTag != null) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF6750A4).copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, Color(0xFF6750A4).copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "🏛️ ${pyqTag.replace("PYQ_", "PYQ ")}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                                color = Color(0xFFD0BCFF),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = question.question,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    question.options.forEachIndexed { idx, opt ->
                                        val isCorrect = idx == question.correctOptionIndex
                                        val bengaliOptLetters = listOf("ক", "খ", "গ", "ঘ")
                                        val optPrefix = if (isBengali) bengaliOptLetters.getOrElse(idx) { ('A' + idx).toString() } else ('A' + idx).toString()
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCorrect) CorrectGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            border = if (isCorrect) BorderStroke(1.dp, CorrectGreen) else null,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                    contentDescription = null,
                                                    tint = if (isCorrect) CorrectGreen else Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "$optPrefix: $opt",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }

                                if (question.explanation.isNotBlank()) {
                                    Text(
                                        text = "💡 ${question.explanation}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { editingQuestion = question },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "✍️ সম্পাদনা",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Button(
                                        onClick = { deletingQuestion = question },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = WrongRed,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "🗑️ মুছে ফেলুন",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 3) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "📝 নিজস্ব টেস্ট তৈরি করে তাতে প্রশ্ন যোগ করুন",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "প্রথমে টেস্টের নাম ও আইডি দিন, তারপর একের পর এক প্রশ্ন যোগ করুন। ছাত্রছাত্রীরা মক টেস্ট হাব থেকে এই টেস্ট দিতে পারবে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "১. টেস্টের সাধারণ বিবরণ:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedTextField(
                                value = customTestTitle,
                                onValueChange = { customTestTitle = it },
                                label = { Text("টেস্টের নাম বা শিরোনাম") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = customTestId,
                                    onValueChange = { customTestId = it },
                                    label = { Text("টেস্টের ইউনিক কোড") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = customTestTimeMins,
                                    onValueChange = { customTestTimeMins = it },
                                    label = { Text("সময় (মিনিট)") },
                                    modifier = Modifier.width(110.dp),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = customClassDropdownExpanded,
                            onExpandedChange = { customClassDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedClass.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("শ্রেণি") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customClassDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = customClassDropdownExpanded,
                                onDismissRequest = { customClassDropdownExpanded = false }
                            ) {
                                allClasses.forEach { cls ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(cls.name, fontWeight = FontWeight.Bold)
                                                Text(cls.board, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            selectedClass = cls
                                            val subs = getAdminClassSubjects(cls.id)
                                            selectedSubject = subs.first()
                                            customClassDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = customSubjectDropdownExpanded,
                            onExpandedChange = { customSubjectDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedSubject.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("বিষয়") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customSubjectDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = customSubjectDropdownExpanded,
                                onDismissRequest = { customSubjectDropdownExpanded = false }
                            ) {
                                classSubjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(subject.name, fontWeight = FontWeight.Bold)
                                                if (!isBengali) {
                                                    Text(subject.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedSubject = subject
                                            val chs = allChapters.filter { it.subjectId == subject.id }
                                            selectedChapter = chs.firstOrNull()
                                            val tps = if (selectedChapter != null) allTopics.filter { it.chapterId == selectedChapter!!.id } else emptyList()
                                            selectedTopic = tps.firstOrNull()
                                            customSubjectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "২. টেস্টের জন্য প্রশ্ন যোগ করুন:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = testQuestionText,
                            onValueChange = { testQuestionText = it },
                            label = { Text("প্রশ্নের বিবরণ") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = testOpt1,
                            onValueChange = { testOpt1 = it },
                            label = { Text("অপশন ক") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = testOpt2,
                            onValueChange = { testOpt2 = it },
                            label = { Text("অপশন খ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = testOpt3,
                            onValueChange = { testOpt3 = it },
                            label = { Text("অপশন গ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = testOpt4,
                            onValueChange = { testOpt4 = it },
                            label = { Text("অপশন ঘ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "সঠিক উত্তর নির্বাচন করুন:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val optionTags = if (isBengali) listOf("ক", "খ", "গ", "ঘ") else listOf("A", "B", "C", "D")
                            optionTags.forEachIndexed { idx, letter ->
                                val isSelected = testCorrectIndex == idx
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CorrectGreen else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { testCorrectIndex = idx }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = letter,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "টেস্টের ধরন বা ক্যাটাগরি:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        val modes = listOf(
                            AssessmentMode.MOCK_TEST to ("মডেল টেস্ট"),
                            AssessmentMode.PRACTICE to ("অধ্যায় পরীক্ষা"),
                            AssessmentMode.BOSS_BATTLE to ("ইউনিট টেস্ট"),
                            AssessmentMode.SPEED_BLITZ to ("সময়বদ্ধ পরীক্ষা"),
                            AssessmentMode.DAILY_10 to ("দৈনিক অনুশীলন"),
                            AssessmentMode.WEAK_AREA to ("সংশোধনী টেস্ট"),
                            AssessmentMode.FRIEND_CHALLENGE to ("চ্যালেঞ্জ কুইজ"),
                            AssessmentMode.CHAPTER_PYQ to ("বিগত বছরের প্রশ্ন"),
                            AssessmentMode.MAP_DIAGRAM_QUIZ to ("চিত্রভিত্তিক টেস্ট"),
                            AssessmentMode.CHAPTER_MARATHON_QUIZ to ("ম্যারাথন কুইজ")
                        )
                        modes.chunked(3).forEach { rowModes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowModes.forEach { (mode, label) ->
                                    val isSelected = selectedAssessmentMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) ExamGold else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedAssessmentMode = mode }
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) Color(0xFF1E1B2E) else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                repeat(3 - rowModes.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = testExplanation,
                        onValueChange = { testExplanation = it },
                        label = { Text("ব্যাখ্যা ও সমাধান (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val opts = listOf(testOpt1, testOpt2, testOpt3, testOpt4).filter { it.isNotBlank() }
                            val correctAns = opts.getOrNull(testCorrectIndex) ?: testOpt1
                            viewModel.createCustomTestAndAddQuestion(
                                testTitle = customTestTitle,
                                testId = customTestId,
                                classId = selectedClass.id,
                                subjectId = selectedSubject.id,
                                timeMinutes = customTestTimeMins.toIntOrNull() ?: 20,
                                assessmentMode = selectedAssessmentMode.name,
                                questionText = testQuestionText,
                                optionsList = opts,
                                correctIndex = testCorrectIndex,
                                correctAnswerText = correctAns,
                                explanationText = testExplanation.ifBlank { "সঠিক উত্তর।" },
                                difficulty = testDifficulty
                            ) { success, msg ->
                                actionSuccess = success
                                actionMessage = msg
                                if (success) {
                                    testQuestionText = ""
                                    testOpt1 = ""
                                    testOpt2 = ""
                                    testOpt3 = ""
                                    testOpt4 = ""
                                    testExplanation = ""
                                }
                            }
                        },
                        enabled = !isLoading && customTestTitle.isNotBlank() && testQuestionText.isNotBlank() && testOpt1.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("যোগ হচ্ছে...", color = Color.White)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "➕ এই টেস্টে প্রশ্ন যোগ করুন",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else if (selectedTab == 4) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "নির্দিষ্ট অধ্যায় বা বিষয় নির্বাচন করে অধ্যায় পরীক্ষা, বিষয়ভিত্তিক বিশেষ কুইজ ও সম্পূর্ণ সিলেবাস মক টেস্ট তৈরি করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "টেস্টের ধরন নির্বাচন:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val structuredTypes = listOf(
                            "CHAPTER_ASSESSMENT" to ("অধ্যায় পরীক্ষা"),
                            "SUBJECT_MASTERY_QUIZ" to ("বিষয়ভিত্তিক বিশেষ কুইজ"),
                            "FULL_SYLLABUS_MOCK" to ("সম্পূর্ণ সিলেবাস মক")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            structuredTypes.forEach { (typeKey, label) ->
                                val isSelected = structuredTestType == typeKey
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { structuredTestType = typeKey }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    val currentBoardClasses = remember(selectedBoard, allClasses) {
                        allClasses.filter { it.board.equals(selectedBoard.id, ignoreCase = true) || it.board.equals(selectedBoard.shortName, ignoreCase = true) }
                            .ifEmpty { allClasses }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        // 1. Board Selector
                        ExposedDropdownMenuBox(
                            expanded = structuredBoardDropdownExpanded,
                            onExpandedChange = { structuredBoardDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = "${selectedBoard.shortName} - ${selectedBoard.fullNameBengali}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("১. বোর্ড / পর্ষদ নির্বাচন", maxLines = 1) },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = structuredBoardDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = structuredBoardDropdownExpanded,
                                onDismissRequest = { structuredBoardDropdownExpanded = false }
                            ) {
                                allBoards.forEach { board ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${board.shortName} (${board.fullNameBengali})", fontWeight = FontWeight.Bold)
                                                Text(board.subtitleBengali, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            selectedBoard = board
                                            val boardClasses = allClasses.filter { it.board.equals(board.id, ignoreCase = true) || it.board.equals(board.shortName, ignoreCase = true) }.ifEmpty { allClasses }
                                            selectedClass = boardClasses.first()
                                            val subs = getAdminClassSubjects(selectedClass.id)
                                            selectedSubject = subs.first()
                                            val chs = allChapters.filter { it.subjectId == selectedSubject.id }
                                            selectedStructuredChapter = chs.firstOrNull()
                                            structuredBoardDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Class Selector
                        ExposedDropdownMenuBox(
                            expanded = structuredClassDropdownExpanded,
                            onExpandedChange = { structuredClassDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedClass.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("২. শ্রেণি") },
                                leadingIcon = { Icon(Icons.Default.Class, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = structuredClassDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = structuredClassDropdownExpanded,
                                onDismissRequest = { structuredClassDropdownExpanded = false }
                            ) {
                                currentBoardClasses.forEach { cls ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(cls.name, fontWeight = FontWeight.Bold)
                                                Text(cls.board, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            selectedClass = cls
                                            val subs = getAdminClassSubjects(cls.id)
                                            selectedSubject = subs.first()
                                            val chs = allChapters.filter { it.subjectId == selectedSubject.id }
                                            selectedStructuredChapter = chs.firstOrNull()
                                            structuredClassDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 3. Subject Selector
                        ExposedDropdownMenuBox(
                            expanded = structuredSubjectDropdownExpanded,
                            onExpandedChange = { structuredSubjectDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedSubject.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("৩. বিষয়") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = structuredSubjectDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = structuredSubjectDropdownExpanded,
                                onDismissRequest = { structuredSubjectDropdownExpanded = false }
                            ) {
                                classSubjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(subject.name, fontWeight = FontWeight.Bold)
                                                if (!isBengali) {
                                                    Text(subject.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedSubject = subject
                                            val chs = allChapters.filter { it.subjectId == subject.id }
                                            selectedChapter = chs.firstOrNull()
                                            selectedStructuredChapter = chs.firstOrNull()
                                            val tps = if (selectedChapter != null) allTopics.filter { it.chapterId == selectedChapter!!.id } else emptyList()
                                            selectedTopic = tps.firstOrNull()
                                            structuredSubjectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 4. Chapter Selector (Dropdown)
                        if (structuredTestType != "FULL_SYLLABUS_MOCK") {
                            ExposedDropdownMenuBox(
                                expanded = structuredChapterDropdownExpanded,
                                onExpandedChange = { structuredChapterDropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedStructuredChapter?.title ?: "অধ্যায় নির্বাচন করুন",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("৪. নির্দিষ্ট অধ্যায়") },
                                    leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = structuredChapterDropdownExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = structuredChapterDropdownExpanded,
                                    onDismissRequest = { structuredChapterDropdownExpanded = false }
                                ) {
                                    val subChapters = allChapters.filter { it.subjectId == selectedSubject.id }
                                    if (subChapters.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("কোনো অধ্যায় পাওয়া যায়নি") },
                                            onClick = { structuredChapterDropdownExpanded = false }
                                        )
                                    } else {
                                        subChapters.forEach { ch ->
                                            DropdownMenuItem(
                                                text = { 
                                                    Text(
                                                        "${ch.chapterNumber}. ${ch.title}",
                                                        fontWeight = if (selectedStructuredChapter?.id == ch.id) FontWeight.Bold else FontWeight.Normal
                                                    ) 
                                                },
                                                onClick = {
                                                    selectedStructuredChapter = ch
                                                    selectedChapter = ch
                                                    structuredChapterDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = structuredTestTitle,
                            onValueChange = { structuredTestTitle = it },
                            label = { Text("টেস্টের শিরোনাম") },
                            leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Time and Total Marks in a single Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = structuredTestDuration,
                                onValueChange = { structuredTestDuration = it },
                                label = { Text("সময় (মিনিট)") },
                                leadingIcon = { Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = structuredTestMarks,
                                onValueChange = { structuredTestMarks = it },
                                label = { Text("মোট নম্বর") },
                                leadingIcon = { Icon(Icons.Default.Grade, contentDescription = null, tint = ExamGold, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "৫. প্রশ্নের বিবরণ ও অপশন যোগ করুন:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            OutlinedTextField(
                                value = structuredQuestionText,
                                onValueChange = { structuredQuestionText = it },
                                label = { Text("প্রশ্নের বিবরণ (Question Text)") },
                                placeholder = { Text("যেমন: সালোকসংশ্লেষ প্রক্রিয়ায় নির্গত উপজাত গ্যাস কোনটি?") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 4 MCQ Options
                            OutlinedTextField(
                                value = structuredOpt1,
                                onValueChange = { structuredOpt1 = it },
                                label = { Text("অপশন ক") },
                                leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = structuredOpt2,
                                onValueChange = { structuredOpt2 = it },
                                label = { Text("অপশন খ") },
                                leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = structuredOpt3,
                                onValueChange = { structuredOpt3 = it },
                                label = { Text("অপশন গ") },
                                leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = structuredOpt4,
                                onValueChange = { structuredOpt4 = it },
                                label = { Text("অপশন ঘ") },
                                leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Correct Answer Option Selector
                            Text(
                                text = "সঠিক উত্তর কোনটি? (নিচের অপশন বেছে নিন):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val optionLabels = listOf("ক (১ম)", "খ (২য়)", "গ (৩য়)", "ঘ (৪র্থ)")
                                optionLabels.forEachIndexed { idx, lbl ->
                                    val isSelected = structuredCorrectIndex == idx
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) CorrectGreen else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, if (isSelected) CorrectGreen else MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { structuredCorrectIndex = idx }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                }
                                                Text(
                                                    text = lbl,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 11.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = structuredExplanation,
                                onValueChange = { structuredExplanation = it },
                                label = { Text("ব্যাখ্যা ও সমাধান (ঐচ্ছিক)") },
                                placeholder = { Text("প্রশ্নের সঠিক উত্তরের ব্যাখ্যা ও সূত্র লিখুন...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(10.dp)
                            )
                            // Add Question to Staged List button
                            OutlinedButton(
                                onClick = {
                                    val opts = listOf(structuredOpt1, structuredOpt2, structuredOpt3, structuredOpt4).filter { it.isNotBlank() }
                                    if (structuredQuestionText.isBlank()) {
                                        Toast.makeText(context, "অনুগ্রহ করে প্রশ্নের বিবরণ লিখুন!", Toast.LENGTH_SHORT).show()
                                        return@OutlinedButton
                                    }
                                    if (opts.size < 2) {
                                        Toast.makeText(context, "কমপক্ষে ২টি অপশন লিখুন!", Toast.LENGTH_SHORT).show()
                                        return@OutlinedButton
                                    }
                                    val qInput = StructuredQuestionInput(
                                        questionText = structuredQuestionText.trim(),
                                        options = opts,
                                        correctIndex = structuredCorrectIndex.coerceIn(0, opts.size - 1),
                                        explanation = structuredExplanation.trim()
                                    )
                                    structuredStagedQuestions = structuredStagedQuestions + qInput
                                    structuredQuestionText = ""
                                    structuredOpt1 = ""
                                    structuredOpt2 = ""
                                    structuredOpt3 = ""
                                    structuredOpt4 = ""
                                    structuredExplanation = ""
                                    structuredCorrectIndex = 0
                                    Toast.makeText(context, "✓ প্রশ্ন যোগ হয়েছে! (মোট ${structuredStagedQuestions.size}টি প্রশ্ন প্রস্তুত)", Toast.LENGTH_SHORT).show()
                                },
                                enabled = structuredQuestionText.isNotBlank() && structuredOpt1.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "➕ এই টেস্টে প্রশ্নটি যোগ করুন (${structuredStagedQuestions.size + 1} নং প্রশ্ন)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Staged Questions Preview Card
                if (structuredStagedQuestions.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📋 প্রস্তুত করা প্রশ্নসমূহ (${structuredStagedQuestions.size}টি):",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    TextButton(
                                        onClick = { structuredStagedQuestions = emptyList() },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("সব মুছুন", color = WrongRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                structuredStagedQuestions.forEachIndexed { qIdx, sq ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("${qIdx + 1}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(sq.questionText, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), maxLines = 2)
                                                val correctOptText = sq.options.getOrNull(sq.correctIndex) ?: ""
                                                Text("সঠিক: $correctOptText", style = MaterialTheme.typography.labelSmall, color = CorrectGreen)
                                            }
                                            IconButton(
                                                onClick = {
                                                    structuredStagedQuestions = structuredStagedQuestions.filterIndexed { index, _ -> index != qIdx }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WrongRed, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    val totalQuestionCount = remember(structuredStagedQuestions, structuredQuestionText) {
                        structuredStagedQuestions.size + (if (structuredQuestionText.isNotBlank() && structuredOpt1.isNotBlank()) 1 else 0)
                    }

                    Button(
                        onClick = {
                            val allQList = mutableListOf<StructuredQuestionInput>()
                            allQList.addAll(structuredStagedQuestions)

                            if (structuredQuestionText.isNotBlank() && structuredOpt1.isNotBlank()) {
                                val opts = listOf(structuredOpt1, structuredOpt2, structuredOpt3, structuredOpt4).filter { it.isNotBlank() }
                                allQList.add(
                                    StructuredQuestionInput(
                                        questionText = structuredQuestionText.trim(),
                                        options = opts,
                                        correctIndex = structuredCorrectIndex.coerceIn(0, opts.size - 1),
                                        explanation = structuredExplanation.trim()
                                    )
                                )
                            }

                            if (allQList.isEmpty()) {
                                Toast.makeText(context, "অনুগ্রহ করে কমপক্ষে ১টি প্রশ্ন তৈরি করুন!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.createStructuredAssessment(
                                testType = structuredTestType,
                                classId = selectedClass.id,
                                subjectId = selectedSubject.id,
                                chapterId = selectedStructuredChapter?.id ?: "${selectedSubject.id}_ch1",
                                title = structuredTestTitle.trim(),
                                description = "Structured test linked to ${selectedStructuredChapter?.title ?: selectedSubject.name}",
                                durationMinutes = structuredTestDuration.toIntOrNull() ?: 25,
                                totalMarks = structuredTestMarks.toIntOrNull() ?: allQList.size,
                                questions = allQList
                            ) { success, msg, createdId ->
                                actionSuccess = success
                                actionMessage = msg
                                if (success) {
                                    lastCreatedTestId = createdId
                                    lastCreatedTestTitle = structuredTestTitle.trim()
                                    structuredStagedQuestions = emptyList()
                                    structuredQuestionText = ""
                                    structuredOpt1 = ""
                                    structuredOpt2 = ""
                                    structuredOpt3 = ""
                                    structuredOpt4 = ""
                                    structuredExplanation = ""
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isLoading && structuredTestTitle.isNotBlank() && (structuredStagedQuestions.isNotEmpty() || (structuredQuestionText.isNotBlank() && structuredOpt1.isNotBlank())),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("সংরক্ষণ হচ্ছে...", color = Color.White)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🚀 মোট ${if (totalQuestionCount > 0) totalQuestionCount else 1}টি প্রশ্ন সহ টেস্ট প্রকাশ করুন",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (lastCreatedTestId != null && lastCreatedTestTitle != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CorrectGreen)
                                    Text(
                                        text = "টেস্ট তৈরি সম্পন্ন: \"${lastCreatedTestTitle}\"",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = "আপনার নতুন টেস্টটি ফায়ারবেস ক্লাউডে সফলভাবে সেভ হয়েছে। এখনই টেস্ট করে দেখতে নিচের বাটনে চাপ দিন:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Button(
                                    onClick = {
                                        viewModel.startAssessment(
                                            mode = com.example.data.model.AssessmentMode.MOCK_TEST,
                                            targetId = lastCreatedTestId,
                                            customTitle = lastCreatedTestTitle
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("▶️ প্রকাশিত টেস্টটিতে এখনই অংশ নিন / টেস্ট করুন", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 5) {
            LaunchedEffect(Unit) {
                viewModel.fetchFirestoreTestTemplates()
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = "📑 ফায়ারবেস টেস্ট টেমপ্লেট ব্যবস্থাপনা",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.fetchFirestoreTestTemplates(selectedClass.id, selectedSubject.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                ExposedDropdownMenuBox(
                                    expanded = testCmsClassDropdownExpanded,
                                    onExpandedChange = { testCmsClassDropdownExpanded = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedClass.name,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("শ্রেণি") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = testCmsClassDropdownExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = testCmsClassDropdownExpanded,
                                        onDismissRequest = { testCmsClassDropdownExpanded = false }
                                    ) {
                                        allClasses.forEach { cls ->
                                            DropdownMenuItem(
                                                text = { Text(cls.name, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    selectedClass = cls
                                                    val subs = getAdminClassSubjects(cls.id)
                                                    selectedSubject = subs.first()
                                                    testCmsClassDropdownExpanded = false
                                                    viewModel.fetchFirestoreTestTemplates(cls.id, selectedSubject.id)
                                                }
                                            )
                                        }
                                    }
                                }

                                ExposedDropdownMenuBox(
                                    expanded = testCmsSubjectDropdownExpanded,
                                    onExpandedChange = { testCmsSubjectDropdownExpanded = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedSubject.name,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("বিষয়") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = testCmsSubjectDropdownExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = testCmsSubjectDropdownExpanded,
                                        onDismissRequest = { testCmsSubjectDropdownExpanded = false }
                                    ) {
                                        classSubjects.forEach { subject ->
                                            DropdownMenuItem(
                                                text = { Text(subject.name, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    selectedSubject = subject
                                                    testCmsSubjectDropdownExpanded = false
                                                    viewModel.fetchFirestoreTestTemplates(selectedClass.id, subject.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isBengali) "মোট ${formatCompactNumber(firestoreTestTemplatesList.size)} টি টেস্ট টেমপ্লেট ফায়ারবেসে সংরক্ষিত আছে।"
                                    else "Total ${formatCompactNumber(firestoreTestTemplatesList.size)} test templates available in Firebase.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                if (isLoading && firestoreTestTemplatesList.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (firestoreTestTemplatesList.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                                Text(
                                    text = "কোনো টেস্ট টেমপ্লেট পাওয়া যায়নি",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                } else {
                    items(firestoreTestTemplatesList) { test ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = test.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = test.testType,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                        Text(
                                            text = "${test.durationMinutes} মিনিট",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Icon(Icons.Default.AdsClick, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(15.dp))
                                        Text(
                                            text = "${test.totalMarks} নম্বর",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                                        Text(
                                            text = "${test.questions.size} টি প্রশ্ন",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Text(
                                    text = "ID: ${test.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = { deletingTestTemplate = test },
                                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("🗑️ টেস্ট ডিলিট করুন", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 6) {
            var bulkImportSubTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: Curriculum (Chapters & Topics)

            if (bulkImportSubTab == 0) {
                val currentBoardClasses = remember(selectedBoard, allClasses) {
                    allClasses.filter { it.board.equals(selectedBoard.id, ignoreCase = true) || it.board.equals(selectedBoard.shortName, ignoreCase = true) }
                        .ifEmpty { allClasses }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Non-sticky Tab Switcher at top
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { 
                                            bulkImportSubTab = 0
                                            bulkImportText = ""
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Quiz, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "❓ প্রশ্ন ব্যাংক", 
                                            fontWeight = FontWeight.Bold, 
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { 
                                            bulkImportSubTab = 1
                                            bulkImportText = ""
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.MenuBook, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "📖 সিলেবাস", 
                                            fontWeight = FontWeight.Bold, 
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "📦 একসাথে একাধিক প্রশ্ন / টেস্ট আপলোড",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "জেসন (JSON) ফাইল সরাসরি আপলোড করুন অথবা JSON / পাইপ লাইন ফরম্যাটে প্রশ্ন পেস্ট করে ডেটাবেজে সংরক্ষণ করুন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 1. JSON FILE UPLOAD CARD
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.FileUpload,
                                                    contentDescription = "Upload JSON",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "📁 JSON ফাইল সরাসরি আপলোড",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "ডিভাইস থেকে .json বা .txt ফাইল সিলেক্ট করুন",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                jsonFilePickerLauncher.launch("*/*")
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("ফাইল বাছুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }

                                    if (bulkFileStatusMessage != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (bulkFileStatusSuccess) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, if (bulkFileStatusSuccess) CorrectGreen.copy(alpha = 0.4f) else WrongRed.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = bulkFileStatusMessage ?: "",
                                                color = if (bulkFileStatusSuccess) CorrectGreen else WrongRed,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    if (bulkImportText.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "ডেটা সাইজ: ${(bulkImportText.length / 1024.0).let { "%.1f KB".format(it) }}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                TextButton(
                                                    onClick = { 
                                                        bulkImportText = ""
                                                        bulkFileStatusMessage = null
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = WrongRed, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("ক্লিয়ার", color = WrongRed, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. TARGET HIERARCHY SELECTION (SINGLE FULL-WIDTH ROWS)
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AccountTree,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "টার্গেট পর্ষদ, শ্রেণি, বিষয়, অধ্যায় ও টপিক নির্বাচন:",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // FIELD 1: BOARD (বোর্ড / পর্ষদ)
                                    ExposedDropdownMenuBox(
                                        expanded = bulkBoardDropdownExpanded,
                                        onExpandedChange = { bulkBoardDropdownExpanded = it },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = "${selectedBoard.shortName} - ${selectedBoard.fullNameBengali}",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("১. বোর্ড / পর্ষদ (Board)", fontWeight = FontWeight.SemiBold) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.AccountBalance,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkBoardDropdownExpanded) },
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                            modifier = Modifier
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                                .fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = bulkBoardDropdownExpanded,
                                            onDismissRequest = { bulkBoardDropdownExpanded = false }
                                        ) {
                                            allBoards.forEach { board ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(board.shortName, fontWeight = FontWeight.Bold)
                                                            Text(board.fullNameBengali, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedBoard = board
                                                        val filteredCls = allClasses.filter { it.board.equals(board.id, ignoreCase = true) || it.board.equals(board.shortName, ignoreCase = true) }
                                                        if (filteredCls.isNotEmpty()) {
                                                            selectedClass = filteredCls.first()
                                                            val subs = getAdminClassSubjects(selectedClass.id)
                                                            selectedSubject = subs.first()
                                                            val chs = allChapters.filter { it.subjectId == selectedSubject.id }
                                                            selectedChapter = chs.firstOrNull()
                                                            val tps = if (selectedChapter != null) allTopics.filter { it.chapterId == selectedChapter!!.id } else emptyList()
                                                            selectedTopic = tps.firstOrNull()
                                                        }
                                                        bulkBoardDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // FIELD 2: CLASS (শ্রেণি)
                                    ExposedDropdownMenuBox(
                                        expanded = bulkClassDropdownExpanded,
                                        onExpandedChange = { bulkClassDropdownExpanded = it },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = selectedClass.name,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("২. শ্রেণি (Class)", fontWeight = FontWeight.SemiBold) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Class,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkClassDropdownExpanded) },
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                            modifier = Modifier
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                                .fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = bulkClassDropdownExpanded,
                                            onDismissRequest = { bulkClassDropdownExpanded = false }
                                        ) {
                                            currentBoardClasses.forEach { cls ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(cls.name, fontWeight = FontWeight.Bold)
                                                            Text(cls.board, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedClass = cls
                                                        val subs = getAdminClassSubjects(cls.id)
                                                        selectedSubject = subs.first()
                                                        val chs = allChapters.filter { it.subjectId == selectedSubject.id }
                                                        selectedChapter = chs.firstOrNull()
                                                        val tps = if (selectedChapter != null) allTopics.filter { it.chapterId == selectedChapter!!.id } else emptyList()
                                                        selectedTopic = tps.firstOrNull()
                                                        bulkClassDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // FIELD 3: SUBJECT (বিষয়)
                                    ExposedDropdownMenuBox(
                                        expanded = bulkSubjectDropdownExpanded,
                                        onExpandedChange = { bulkSubjectDropdownExpanded = it },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = selectedSubject.name,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("৩. বিষয় (Subject)", fontWeight = FontWeight.SemiBold) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.MenuBook,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkSubjectDropdownExpanded) },
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                            modifier = Modifier
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                                .fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = bulkSubjectDropdownExpanded,
                                            onDismissRequest = { bulkSubjectDropdownExpanded = false }
                                        ) {
                                            classSubjects.forEach { subject ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(subject.name, fontWeight = FontWeight.Bold)
                                                            if (!isBengali) {
                                                                Text(subject.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedSubject = subject
                                                        val chs = allChapters.filter { it.subjectId == subject.id }
                                                        selectedChapter = chs.firstOrNull()
                                                        val tps = if (selectedChapter != null) allTopics.filter { it.chapterId == selectedChapter!!.id } else emptyList()
                                                        selectedTopic = tps.firstOrNull()
                                                        bulkSubjectDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // FIELD 4: CHAPTER (অধ্যায়)
                                    ExposedDropdownMenuBox(
                                        expanded = bulkChapterDropdownExpanded,
                                        onExpandedChange = { bulkChapterDropdownExpanded = it },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = selectedChapter?.title ?: "কোনো অধ্যায় নেই",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("৪. অধ্যায় (Chapter)", fontWeight = FontWeight.SemiBold) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.LibraryBooks,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkChapterDropdownExpanded) },
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                            modifier = Modifier
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                                .fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = bulkChapterDropdownExpanded,
                                            onDismissRequest = { bulkChapterDropdownExpanded = false }
                                        ) {
                                            if (filteredChapters.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("কোনো অধ্যায় নেই") },
                                                    onClick = { bulkChapterDropdownExpanded = false }
                                                )
                                            } else {
                                                filteredChapters.forEach { chapter ->
                                                    DropdownMenuItem(
                                                        text = { Text(chapter.title) },
                                                        onClick = {
                                                            selectedChapter = chapter
                                                            val tps = allTopics.filter { it.chapterId == chapter.id }
                                                            selectedTopic = tps.firstOrNull()
                                                            bulkChapterDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // FIELD 5: TOPIC (টপিক)
                                    ExposedDropdownMenuBox(
                                        expanded = bulkTopicDropdownExpanded,
                                        onExpandedChange = { bulkTopicDropdownExpanded = it },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = selectedTopic?.title ?: "কোনো টপিক নেই",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("৫. টপিক (Topic)", fontWeight = FontWeight.SemiBold) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Topic,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkTopicDropdownExpanded) },
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                            modifier = Modifier
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                                .fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = bulkTopicDropdownExpanded,
                                            onDismissRequest = { bulkTopicDropdownExpanded = false }
                                        ) {
                                            if (filteredTopics.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("কোনো টপিক নেই") },
                                                    onClick = { bulkTopicDropdownExpanded = false }
                                                )
                                            } else {
                                                filteredTopics.forEach { topic ->
                                                    DropdownMenuItem(
                                                        text = { Text(topic.title) },
                                                        onClick = {
                                                            selectedTopic = topic
                                                            bulkTopicDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                // 3. SAMPLE TEMPLATES & JSON DOWNLOAD CARD
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "নমুনা ফরম্যাট ও উদাহরণ (Template):",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                TextButton(
                                    onClick = { showJsonGuideDialog = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("স্ট্রাকচার গাইড", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Text(
                                text = "💡 টেমপ্লেট কী কাজে লাগে? একসাথে শত শত প্রশ্ন তৈরি ও আপলোড করতে সঠিক নিয়ম (যেমন: প্রশ্ন, অপশন ১, ২, ৩, ৪, সঠিক উত্তর ইত্যাদি) জানার জন্য এই নমুনা ব্যবহার করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Download and copy buttons row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        downloadQuestionJsonLauncher.launch("desh_exam_sample_questions.json")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "📥 নমুনা JSON ডাউনলোড",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(sampleJsonTemplate))
                                        bulkFileStatusMessage = "✓ নমুনা JSON কোড ক্লিপবোর্ডে কপি করা হয়েছে!"
                                        bulkFileStatusSuccess = true
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                    modifier = Modifier.weight(0.9f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "📋 কপি JSON",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        try {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, sampleJsonTemplate)
                                                putExtra(Intent.EXTRA_TITLE, "sample_questions.json")
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "নমুনা JSON শেয়ার / সেভ করুন"))
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            // Fill sample into editor buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { 
                                        bulkImportText = sampleJsonTemplate 
                                        bulkFileStatusMessage = "✓ নমুনা JSON এডিটরে লোড হয়েছে!"
                                        bulkFileStatusSuccess = true
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "📄 এডিটরে JSON লোড করুন",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }

                                OutlinedButton(
                                    onClick = { 
                                        bulkImportText = sampleCsvTemplate 
                                        bulkFileStatusMessage = "✓ নমুনা পাইপ ফরম্যাট এডিটরে লোড হয়েছে!"
                                        bulkFileStatusSuccess = true
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "📋 নমুনা পাইপ ফরম্যাট লোড",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }

                                if (bulkImportText.isNotBlank()) {
                                    IconButton(
                                        onClick = { 
                                            bulkImportText = "" 
                                            bulkFileStatusMessage = null
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = WrongRed)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = bulkCustomTestTitle,
                            onValueChange = { bulkCustomTestTitle = it },
                            label = { Text("টেস্ট শিরোনাম (ঐচ্ছিক)", maxLines = 1) },
                            placeholder = { Text("মডেল টেস্ট ১") },
                            leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // PYQ Year Dropdown selector
                        val pyqYearList = remember {
                            listOf(
                                "সাধারণ (কোনো নির্দিষ্ট সাল নয়)",
                                "2026", "2025", "2024", "2023", "2022", "2021",
                                "2020", "2019", "2018", "2017", "2016", "2015",
                                "2014", "2013", "2012", "2011", "2010"
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = bulkPyqDropdownExpanded,
                            onExpandedChange = { bulkPyqDropdownExpanded = it },
                            modifier = Modifier.weight(0.9f)
                        ) {
                            OutlinedTextField(
                                value = if (bulkPyqYearInput.isBlank()) "কোনো সাল নয়" else bulkPyqYearInput,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("বিগত সাল (PYQ)", maxLines = 1) },
                                leadingIcon = { Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkPyqDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = bulkPyqDropdownExpanded,
                                onDismissRequest = { bulkPyqDropdownExpanded = false }
                            ) {
                                pyqYearList.forEach { yr ->
                                    val isNone = yr.startsWith("সাধারণ")
                                    val isSelected = (isNone && bulkPyqYearInput.isBlank()) || (bulkPyqYearInput == yr)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    if (isNone) Icons.Default.Block else Icons.Default.CalendarToday,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    yr,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        },
                                        onClick = {
                                            bulkPyqYearInput = if (isNone) "" else yr
                                            bulkPyqDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "প্রশ্নের ডেটা পেস্ট করুন:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = bulkImportText,
                            onValueChange = { bulkImportText = it },
                            placeholder = {
                                Text(
                                    if (isBengali)
                                        "এখানে নমুনা ফরম্যাট অনুযায়ী প্রশ্ন পেস্ট করুন (যেমন: প্রশ্ন | অপশন১ | অপশন২ | অপশন৩ | অপশন৪ | সঠিক_সূচক(০-৩) | ব্যাখ্যা)..."
                                    else
                                        "Paste JSON array or Pipe-separated question lines here..."
                                )
                            },
                            minLines = 8,
                            maxLines = 16,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                item {
                    val isJson = bulkImportText.trim().startsWith("[") || bulkImportText.trim().startsWith("{")
                    val lineCount = if (isJson) {
                        try {
                            val arr = if (bulkImportText.trim().startsWith("[")) {
                                org.json.JSONArray(bulkImportText.trim())
                            } else {
                                org.json.JSONObject(bulkImportText.trim()).optJSONArray("questions") ?: org.json.JSONArray()
                            }
                            arr.length()
                        } catch (_: Exception) { 0 }
                    } else {
                        bulkImportText.trim().split("\n").filter { it.split("|").size >= 5 }.size
                    }

                    if (bulkImportText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (lineCount > 0) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (lineCount > 0) CorrectGreen.copy(alpha = 0.4f) else WrongRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (lineCount > 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (lineCount > 0) CorrectGreen else WrongRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (lineCount > 0) {
                                        if (isBengali) "✓ সঠিকভাবে শনাক্ত করা হয়েছে: $lineCount টি প্রশ্ন"
                                        else "✓ Valid format detected: $lineCount questions (${if (isJson) "JSON" else "Pipe-Separated"})"
                                    } else {
                                        if (isBengali) "⚠️ কোনো সঠিক প্রশ্ন পাওয়া যায়নি। নমুনা দেখুন।"
                                        else "⚠️ No valid questions parsed. Check template."
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (lineCount > 0) CorrectGreen else WrongRed
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.importBulkQuestions(
                                rawInput = bulkImportText,
                                classId = selectedClass.id,
                                subjectId = selectedSubject.id,
                                chapterId = selectedChapter?.id ?: "",
                                topicId = selectedTopic?.id ?: "",
                                pyqYear = bulkPyqYearInput,
                                customTestTitle = bulkCustomTestTitle
                            ) { success, msg, _ ->
                                actionSuccess = success
                                actionMessage = msg
                                if (success) {
                                    bulkImportText = ""
                                }
                            }
                        },
                        enabled = !isLoading && bulkImportText.isNotBlank() && selectedChapter != null && selectedTopic != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("সংরক্ষণ হচ্ছে...", color = Color.White)
                        } else {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (bulkImportText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🚀 একসাথে সব প্রশ্ন আপলোড করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Non-sticky Tab Switcher at top of Syllabus tab
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { 
                                            bulkImportSubTab = 0
                                            bulkImportText = ""
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Quiz, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "❓ প্রশ্ন ব্যাংক", 
                                            fontWeight = FontWeight.Bold, 
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { 
                                            bulkImportSubTab = 1
                                            bulkImportText = ""
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.MenuBook, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "📖 সিলেবাস", 
                                            fontWeight = FontWeight.Bold, 
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🏫 সিলেবাস ও কারিকুলাম ব্যাচ আপলোড (CSV / JSON)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "নির্দিষ্ট বিষয়ের জন্য এক ক্লিকে একাধিক অধ্যায় ও তাদের টপিক একসাথে JSON বা CSV/Pipe ফরম্যাটে পেস্ট করে ফায়ারবেস ক্লাউড ও লোকাল ডেটাবেজে ব্যাচ ইমপোর্ট করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Select Target Board, Class, Subject
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "টার্গেট পর্ষদ, শ্রেণি ও বিষয় নির্বাচন করুন:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Class Dropdown
                                    ExposedDropdownMenuBox(
                                        expanded = bulkClassDropdownExpanded,
                                        onExpandedChange = { bulkClassDropdownExpanded = it },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = selectedClass.name,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("১. শ্রেণি", maxLines = 1) },
                                            leadingIcon = { Icon(Icons.Default.Class, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkClassDropdownExpanded) },
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = bulkClassDropdownExpanded,
                                            onDismissRequest = { bulkClassDropdownExpanded = false }
                                        ) {
                                            allClasses.forEach { cls ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(cls.name, fontWeight = FontWeight.Bold)
                                                            Text(cls.board, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedClass = cls
                                                        val subs = getAdminClassSubjects(cls.id)
                                                        selectedSubject = subs.first()
                                                        bulkClassDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Subject Dropdown
                                    ExposedDropdownMenuBox(
                                        expanded = bulkSubjectDropdownExpanded,
                                        onExpandedChange = { bulkSubjectDropdownExpanded = it },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = selectedSubject.name,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("২. বিষয়", maxLines = 1) },
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkSubjectDropdownExpanded) },
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = bulkSubjectDropdownExpanded,
                                            onDismissRequest = { bulkSubjectDropdownExpanded = false }
                                        ) {
                                            classSubjects.forEach { subject ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(subject.name, fontWeight = FontWeight.Bold)
                                                    },
                                                    onClick = {
                                                        selectedSubject = subject
                                                        bulkSubjectDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sample template and JSON download buttons for Syllabus
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DownloadForOffline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "সিলেবাস নমুনা ও JSON ডাউনলোড:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            downloadSyllabusJsonLauncher.launch("desh_exam_sample_syllabus.json")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "📥 সিলেবাস JSON ডাউনলোড",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(sampleCurriculumJsonTemplate))
                                            bulkFileStatusMessage = "✓ সিলেবাস JSON কোড ক্লিপবোর্ডে কপি করা হয়েছে!"
                                            bulkFileStatusSuccess = true
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                        modifier = Modifier.weight(0.9f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "📋 কপি JSON",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            try {
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, sampleCurriculumJsonTemplate)
                                                    putExtra(Intent.EXTRA_TITLE, "sample_syllabus.json")
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "সিলেবাস JSON শেয়ার / সেভ করুন"))
                                            } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { 
                                            bulkImportText = sampleCurriculumJsonTemplate 
                                            bulkFileStatusMessage = "✓ নমুনা সিলেবাস JSON এডিটরে লোড হয়েছে!"
                                            bulkFileStatusSuccess = true
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            "📄 এডিটরে JSON লোড",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { 
                                            bulkImportText = sampleCurriculumCsvTemplate 
                                            bulkFileStatusMessage = "✓ নমুনা CSV এডিটরে লোড হয়েছে!"
                                            bulkFileStatusSuccess = true
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            "📋 নমুনা CSV লোড",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }

                                    if (bulkImportText.isNotBlank()) {
                                        IconButton(
                                            onClick = { 
                                                bulkImportText = "" 
                                                bulkFileStatusMessage = null
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = WrongRed)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Input field
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "কারিকুলাম ডেটা পেস্ট করুন:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedTextField(
                                value = bulkImportText,
                                onValueChange = { bulkImportText = it },
                                placeholder = {
                                    Text(
                                        "এখানে নমুনা ফরম্যাট অনুযায়ী অধ্যায় ও টপিকের JSON অ্যারে অথবা CSV/Pipe লাইন পেস্ট করুন..."
                                    )
                                },
                                minLines = 8,
                                maxLines = 16,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Live Validation Indicator
                    item {
                        val isCurriculumJson = bulkImportText.trim().startsWith("[") || bulkImportText.trim().startsWith("{")
                        val (parsedChCount, parsedTpCount) = remember(bulkImportText) {
                            if (bulkImportText.isBlank()) {
                                0 to 0
                            } else if (isCurriculumJson) {
                                try {
                                    val arr = if (bulkImportText.trim().startsWith("[")) {
                                        org.json.JSONArray(bulkImportText.trim())
                                    } else {
                                        org.json.JSONObject(bulkImportText.trim()).optJSONArray("chapters") ?: org.json.JSONArray()
                                    }
                                    var tpCount = 0
                                    for (i in 0 until arr.length()) {
                                        val tArr = arr.optJSONObject(i)?.optJSONArray("topics")
                                        tpCount += tArr?.length() ?: 0
                                    }
                                    arr.length() to tpCount
                                } catch (_: Exception) {
                                    0 to 0
                                }
                            } else {
                                val lines = bulkImportText.trim().split("\n").filter { 
                                    val s = it.trim()
                                    s.isNotBlank() && !s.startsWith("chapterNumber") && (s.contains("|") || s.contains(","))
                                }
                                val chSet = mutableSetOf<String>()
                                for (l in lines) {
                                    val c = if (l.contains("|")) l.split("|") else l.split(",")
                                    if (c.isNotEmpty()) chSet.add(c[0].trim())
                                }
                                chSet.size to lines.size
                            }
                        }

                        if (bulkImportText.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (parsedChCount > 0 || parsedTpCount > 0) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (parsedChCount > 0 || parsedTpCount > 0) CorrectGreen.copy(alpha = 0.4f) else WrongRed.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (parsedChCount > 0 || parsedTpCount > 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (parsedChCount > 0 || parsedTpCount > 0) CorrectGreen else WrongRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (parsedChCount > 0 || parsedTpCount > 0) {
                                            "✓ শনাক্ত করা হয়েছে: $parsedChCount টি অধ্যায় ও $parsedTpCount টি টপিক (${if (isCurriculumJson) "JSON" else "CSV/Pipe"})"
                                        } else {
                                            "⚠️ কোনো সঠিক অধ্যায় বা টপিক শনাক্ত করা যায়নি। নমুনা দেখুন।"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (parsedChCount > 0 || parsedTpCount > 0) CorrectGreen else WrongRed
                                    )
                                }
                            }
                        }
                    }

                    // Submit Button
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.importBulkCurriculum(
                                    rawInput = bulkImportText,
                                    classId = selectedClass.id,
                                    subjectId = selectedSubject.id
                                ) { success, msg, chs, tps ->
                                    actionSuccess = success
                                    actionMessage = msg
                                    if (success) {
                                        bulkImportText = ""
                                    }
                                }
                            },
                            enabled = !isLoading && bulkImportText.isNotBlank() && selectedSubject.id.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("সংরক্ষণ হচ্ছে...", color = Color.White)
                            } else {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = if (bulkImportText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "🚀 একসাথে সব অধ্যায় ও টপিক আপলোড করুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        } else if (selectedTab == 7) {
            var subSectionTab by remember { mutableStateOf(0) }
            
            ScrollableTabRow(
                selectedTabIndex = subSectionTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                val subTabs = listOf("🏫 পর্ষদ (Board)", "🎓 ক্লাস (Class)", "📘 বিষয় (Subject)", "📖 অধ্যায় (Chapter)", "🏷️ টপিক (Topic)")
                subTabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = subSectionTab == idx,
                        onClick = { subSectionTab = idx },
                        text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (subSectionTab == 0) {
                    item {
                        var boardId by remember { mutableStateOf("") }
                        var shortName by remember { mutableStateOf("") }
                        var fullNameBengali by remember { mutableStateOf("") }
                        var fullNameEnglish by remember { mutableStateOf("") }
                        var subtitleBengali by remember { mutableStateOf("") }
                        var levelBadge by remember { mutableStateOf("") }
                        var classRange by remember { mutableStateOf("") }
                        
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("🏫 ফায়ারবেসে নতুন বোর্ড (Board) যুক্ত করুন", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                
                                OutlinedTextField(value = boardId, onValueChange = { boardId = it.uppercase() }, label = { Text("Board ID (e.g. WBBSE, WBCHSE)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = shortName, onValueChange = { shortName = it }, label = { Text("Short Name (e.g. WBBSE)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = fullNameBengali, onValueChange = { fullNameBengali = it }, label = { Text("পর্ষদের পূর্ণ নাম (বাংলা)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = fullNameEnglish, onValueChange = { fullNameEnglish = it }, label = { Text("Full Name (English)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = subtitleBengali, onValueChange = { subtitleBengali = it }, label = { Text("ছোট বিবরণ / সাবটাইটেল") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = levelBadge, onValueChange = { levelBadge = it }, label = { Text("লেভেল ব্যাজ (e.g. মাধ্যমিক স্তর)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = classRange, onValueChange = { classRange = it }, label = { Text("ক্লাস রেঞ্জ (e.g. ৫ম থেকে ১০ম শ্রেণি)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                
                                Button(
                                    onClick = {
                                        val newBoard = com.example.data.firestore.FirestoreEducationBoard(
                                            id = boardId.trim(),
                                            shortName = shortName.trim(),
                                            fullNameBengali = fullNameBengali.trim(),
                                            fullNameEnglish = fullNameEnglish.trim(),
                                            subtitleBengali = subtitleBengali.trim(),
                                            levelBadge = levelBadge.trim(),
                                            classRangeDescription = classRange.trim(),
                                            orderIndex = 10,
                                            isActive = true
                                        )
                                        viewModel.addFirestoreBoard(newBoard) { success, msg ->
                                            actionSuccess = success
                                            actionMessage = msg
                                            if (success) {
                                                boardId = ""; shortName = ""; fullNameBengali = ""; fullNameEnglish = ""; subtitleBengali = ""; levelBadge = ""; classRange = ""
                                            }
                                        }
                                    },
                                    enabled = boardId.isNotBlank() && shortName.isNotBlank() && fullNameBengali.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("💾 পর্ষদ তৈরি করুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (subSectionTab == 1) {
                    item {
                        var classId by remember { mutableStateOf("") }
                        var classNumInput by remember { mutableStateOf("10") }
                        var nameBengaliValue by remember { mutableStateOf("") }
                        var nameEnglishValue by remember { mutableStateOf("") }
                        var boardValue by remember { mutableStateOf("WBBSE") }
                        var targetExamValue by remember { mutableStateOf("") }
                        var descriptionValue by remember { mutableStateOf("") }
                        
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("🎓 ফায়ারবেসে নতুন ক্লাস (Class) যুক্ত করুন", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                
                                var boardDropdownExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = boardValue,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("শিক্ষা পর্ষদ নির্বাচন করুন (Select Board)") },
                                        trailingIcon = {
                                            IconButton(onClick = { boardDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Board")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { boardDropdownExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = boardDropdownExpanded,
                                        onDismissRequest = { boardDropdownExpanded = false }
                                    ) {
                                        val boardsToShow = if (liveBoards.isNotEmpty()) liveBoards else com.example.data.model.AcademicClassProvider.ALL_BOARDS
                                        boardsToShow.forEach { board ->
                                            DropdownMenuItem(
                                                text = { Text("${board.shortName} - ${board.fullNameBengali}") },
                                                onClick = {
                                                    boardValue = board.id
                                                    boardDropdownExpanded = false
                                                    val num = classNumInput.trim().toIntOrNull() ?: 10
                                                    classId = if (num == 0) "class_kg_${board.id.lowercase()}" else "class_${num}_${board.id.lowercase()}"
                                                    targetExamValue = "${board.id} Class $num"
                                                }
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(value = classId, onValueChange = { classId = it.lowercase().replace(" ", "_") }, label = { Text("Class ID (e.g. class_10_wbbse)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(
                                    value = classNumInput,
                                    onValueChange = {
                                        classNumInput = it
                                        val num = it.trim().toIntOrNull() ?: 10
                                        classId = if (num == 0) "class_kg_${boardValue.lowercase()}" else "class_${num}_${boardValue.lowercase()}"
                                        targetExamValue = "${boardValue} Class $num"
                                    },
                                    label = { Text("শ্রেণি নম্বর (e.g. 5, 10, 11)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(value = nameBengaliValue, onValueChange = { nameBengaliValue = it }, label = { Text("ক্লাসের নাম (বাংলা)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = nameEnglishValue, onValueChange = { nameEnglishValue = it }, label = { Text("Class Name (English)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = targetExamValue, onValueChange = { targetExamValue = it }, label = { Text("পরীক্ষার নাম (e.g. WBBSE Madhyamik 2026)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = descriptionValue, onValueChange = { descriptionValue = it }, label = { Text("ক্লাসের বিবরণ ও টার্গেট") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                
                                Button(
                                    onClick = {
                                        val newCls = com.example.data.firestore.FirestoreClass(
                                            id = classId.trim(),
                                            classNumber = classNumInput.trim().toIntOrNull() ?: 10,
                                            name = nameBengaliValue.trim(),
                                            nameBengaliValue = nameBengaliValue.trim(),
                                            nameEnglishValue = nameEnglishValue.trim(),
                                            board = boardValue.trim(),
                                            targetExam = targetExamValue.trim(),
                                            description = descriptionValue.trim(),
                                            isActive = true,
                                            orderIndex = 10
                                        )
                                        viewModel.addFirestoreClass(newCls) { success, msg ->
                                            actionSuccess = success
                                            actionMessage = msg
                                            if (success) {
                                                classId = ""; nameBengaliValue = ""; nameEnglishValue = ""; targetExamValue = ""; descriptionValue = ""
                                            }
                                        }
                                    },
                                    enabled = classId.isNotBlank() && nameBengaliValue.isNotBlank() && boardValue.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("💾 ক্লাস তৈরি করুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (subSectionTab == 2) {
                    item {
                        var subjectId by remember { mutableStateOf("") }
                        var subjectCode by remember { mutableStateOf("") }
                        var subjectName by remember { mutableStateOf("") }
                        var classIdValue by remember { mutableStateOf("class_10_wbbse") }
                        var textbookNameValue by remember { mutableStateOf("") }
                        var streamValue by remember { mutableStateOf("common") }
                        var colorHexValue by remember { mutableStateOf("0xFF3B82F6") }
                        var totalChaptersInput by remember { mutableStateOf("10") }
                        
                        var editMode by remember { mutableStateOf(false) }
                        var selectedSubjectToEdit by remember { mutableStateOf<com.example.data.local.SubjectEntity?>(null) }
                        
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = if (editMode) "📘 ফায়ারবেসে বিষয় (Subject) এডিট করুন" else "📘 ফায়ারবেসে নতুন বিষয় (Subject) যুক্ত করুন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = !editMode,
                                        onClick = {
                                            editMode = false
                                            selectedSubjectToEdit = null
                                            subjectId = ""
                                            subjectCode = ""
                                            subjectName = ""
                                            textbookNameValue = ""
                                            streamValue = "common"
                                            colorHexValue = "0xFF3B82F6"
                                            totalChaptersInput = "10"
                                        },
                                        label = { Text("➕ নতুন তৈরি করুন") }
                                    )
                                    FilterChip(
                                        selected = editMode,
                                        onClick = {
                                            editMode = true
                                        },
                                        label = { Text("✏️ এডিট করুন") }
                                    )
                                }
                                
                                var classDropdownExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = classIdValue,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("শ্রেণি নির্বাচন করুন (Select Class)") },
                                        trailingIcon = {
                                            IconButton(onClick = { classDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Class")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { classDropdownExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = classDropdownExpanded,
                                        onDismissRequest = { classDropdownExpanded = false }
                                    ) {
                                        val classesToShow = if (liveClasses.isNotEmpty()) liveClasses else com.example.data.model.AcademicClassProvider.ALL_CLASSES
                                        classesToShow.forEach { cls: com.example.data.model.AcademicClass ->
                                            val displayName = cls.name
                                            DropdownMenuItem(
                                                text = { Text("$displayName (${cls.board})") },
                                                onClick = {
                                                    classIdValue = cls.id
                                                    classDropdownExpanded = false
                                                    val code = subjectCode.ifBlank { "subject" }
                                                    subjectId = "${cls.id}_$code"
                                                }
                                            )
                                        }
                                    }
                                }

                                val availableSubsForClass = remember(classIdValue, dbSubjects) {
                                    getAdminClassSubjectsHelper(classIdValue)
                                }

                                if (editMode) {
                                    var editSubjectDropdownExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = selectedSubjectToEdit?.name ?: "এডিটের জন্য বিষয় সিলেক্ট করুন",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("বিষয় নির্বাচন করুন") },
                                            trailingIcon = {
                                                IconButton(onClick = { editSubjectDropdownExpanded = true }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Subject")
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().clickable { editSubjectDropdownExpanded = true }
                                        )
                                        DropdownMenu(
                                            expanded = editSubjectDropdownExpanded,
                                            onDismissRequest = { editSubjectDropdownExpanded = false }
                                        ) {
                                            if (availableSubsForClass.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("কোন বিষয় খুঁজে পাওয়া যায়নি") },
                                                    onClick = { editSubjectDropdownExpanded = false }
                                                )
                                            } else {
                                                availableSubsForClass.forEach { sub ->
                                                    DropdownMenuItem(
                                                        text = { Text("${sub.name} (${sub.code})") },
                                                        onClick = {
                                                            selectedSubjectToEdit = sub
                                                            editSubjectDropdownExpanded = false
                                                            subjectId = sub.id
                                                            subjectCode = sub.code
                                                            subjectName = sub.name
                                                            textbookNameValue = sub.textbookName
                                                            streamValue = sub.stream
                                                            colorHexValue = "0x" + sub.colorHex.toString(16).uppercase()
                                                            totalChaptersInput = sub.totalChapters.toString()
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = subjectId,
                                    onValueChange = { if (!editMode) subjectId = it.lowercase().replace(" ", "_") },
                                    readOnly = editMode,
                                    label = { Text("Subject ID (e.g. class_10_physics)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = subjectCode,
                                    onValueChange = {
                                        if (!editMode) {
                                            subjectCode = it.lowercase().replace(" ", "_")
                                            subjectId = "${classIdValue}_${it.lowercase().replace(" ", "_")}"
                                        }
                                    },
                                    readOnly = editMode,
                                    label = { Text("Subject Code (e.g. physics)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(value = subjectName, onValueChange = { subjectName = it }, label = { Text("বিষয়ের নাম (বাংলা)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = textbookNameValue, onValueChange = { textbookNameValue = it }, label = { Text("পাঠ্যবইয়ের নাম (e.g. গণিত প্রকাশ)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = streamValue, onValueChange = { streamValue = it.lowercase() }, label = { Text("শাখা (e.g. common, science, arts)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = colorHexValue, onValueChange = { colorHexValue = it }, label = { Text("থিম কালার কোড (e.g. 0xFF3B82F6)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = totalChaptersInput, onValueChange = { totalChaptersInput = it }, label = { Text("অধ্যায়ের মোট সংখ্যা") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                
                                Button(
                                    onClick = {
                                        val newSub = com.example.data.firestore.FirestoreSubject(
                                            id = subjectId.trim(),
                                            classId = classIdValue.trim(),
                                            code = subjectCode.trim(),
                                            name = subjectName.trim(),
                                            textbookName = textbookNameValue.trim(),
                                            stream = streamValue.trim(),
                                            totalChapters = totalChaptersInput.trim().toIntOrNull() ?: 10,
                                            colorHex = colorHexValue.trim(),
                                            iconName = "Book",
                                            orderIndex = 10
                                        )
                                        viewModel.addFirestoreSubject(newSub) { success, msg ->
                                            actionSuccess = success
                                            actionMessage = msg
                                            if (success && !editMode) {
                                                subjectId = ""; subjectCode = ""; subjectName = ""; textbookNameValue = ""
                                            }
                                        }
                                    },
                                    enabled = subjectId.isNotBlank() && subjectCode.isNotBlank() && subjectName.isNotBlank() && classIdValue.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = if (editMode) "💾 বিষয় আপডেট করুন" else "💾 বিষয় তৈরি করুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (subSectionTab == 4) {
                    item {
                        var topicId by remember { mutableStateOf("") }
                        var classIdValue by remember { mutableStateOf("class_10_wbbse") }
                        var subjectIdValue by remember { mutableStateOf("") }
                        var chapterIdValue by remember { mutableStateOf("") }
                        var topicOrderInput by remember { mutableStateOf("1") }
                        var topicTitleValue by remember { mutableStateOf("") }
                        var summaryValue by remember { mutableStateOf("") }
                        var keyPointsValue by remember { mutableStateOf("") }
                        var formulaeValue by remember { mutableStateOf("") }
                        
                        var editMode by remember { mutableStateOf(false) }
                        var selectedTopicToEdit by remember { mutableStateOf<com.example.data.local.TopicEntity?>(null) }
                        
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = if (editMode) "🏷️ ফায়ারবেসে টপিক (Topic) এডিট করুন" else "🏷️ ফায়ারবেসে নতুন টপিক (Topic) যুক্ত করুন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = !editMode,
                                        onClick = {
                                            editMode = false
                                            selectedTopicToEdit = null
                                            topicId = ""
                                            topicOrderInput = "1"
                                            topicTitleValue = ""
                                            summaryValue = ""
                                            keyPointsValue = ""
                                            formulaeValue = ""
                                        },
                                        label = { Text("➕ নতুন তৈরি করুন") }
                                    )
                                    FilterChip(
                                        selected = editMode,
                                        onClick = {
                                            editMode = true
                                        },
                                        label = { Text("✏️ এডিট করুন") }
                                    )
                                }
                                
                                var classDropdownExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = classIdValue,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("শ্রেণি নির্বাচন করুন") },
                                        trailingIcon = {
                                            IconButton(onClick = { classDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Class")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { classDropdownExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = classDropdownExpanded,
                                        onDismissRequest = { classDropdownExpanded = false }
                                    ) {
                                        val classesToShow = if (liveClasses.isNotEmpty()) liveClasses else com.example.data.model.AcademicClassProvider.ALL_CLASSES
                                        classesToShow.forEach { cls: com.example.data.model.AcademicClass ->
                                            val displayName = cls.name
                                            DropdownMenuItem(
                                                text = { Text("$displayName (${cls.board})") },
                                                onClick = {
                                                    classIdValue = cls.id
                                                    classDropdownExpanded = false
                                                    subjectIdValue = "" // Reset subject on class change
                                                    chapterIdValue = "" // Reset chapter
                                                }
                                            )
                                        }
                                    }
                                }

                                var subjectDropdownExpanded by remember { mutableStateOf(false) }
                                val availableSubsForClass = remember(classIdValue, dbSubjects) {
                                    getAdminClassSubjectsHelper(classIdValue)
                                }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = subjectIdValue,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("বিষয় নির্বাচন করুন") },
                                        placeholder = { Text("প্রথমে শ্রেণি নির্বাচন করুন") },
                                        trailingIcon = {
                                            IconButton(onClick = { subjectDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Subject")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { if (classIdValue.isNotBlank()) subjectDropdownExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = subjectDropdownExpanded,
                                        onDismissRequest = { subjectDropdownExpanded = false }
                                    ) {
                                        if (availableSubsForClass.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("কোন বিষয় খুঁজে পাওয়া যায়নি") },
                                                onClick = { subjectDropdownExpanded = false }
                                            )
                                        } else {
                                            availableSubsForClass.forEach { sub ->
                                                DropdownMenuItem(
                                                    text = { Text("${sub.name} (${sub.code})") },
                                                    onClick = {
                                                        subjectIdValue = sub.id
                                                        subjectDropdownExpanded = false
                                                        chapterIdValue = "" // Reset chapter on subject change
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                var chapterDropdownExpanded by remember { mutableStateOf(false) }
                                val availableChaptersForSubject = remember(subjectIdValue, dbChapters) {
                                    dbChapters.filter { it.subjectId == subjectIdValue || it.subjectId == subjectIdValue.substringAfterLast("_", "") }
                                }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = chapterIdValue,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("অধ্যায় নির্বাচন করুন") },
                                        placeholder = { Text("প্রথমে বিষয় নির্বাচন করুন") },
                                        trailingIcon = {
                                            IconButton(onClick = { chapterDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Chapter")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { if (subjectIdValue.isNotBlank()) chapterDropdownExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = chapterDropdownExpanded,
                                        onDismissRequest = { chapterDropdownExpanded = false }
                                    ) {
                                        if (availableChaptersForSubject.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("কোন অধ্যায় খুঁজে পাওয়া যায়নি") },
                                                onClick = { chapterDropdownExpanded = false }
                                            )
                                        } else {
                                            availableChaptersForSubject.forEach { ch ->
                                                DropdownMenuItem(
                                                    text = { Text("অধ্যায় ${ch.chapterNumber}: ${ch.title}") },
                                                    onClick = {
                                                        chapterIdValue = ch.id
                                                        chapterDropdownExpanded = false
                                                        val tpIdx = topicOrderInput.trim().toIntOrNull() ?: 1
                                                        topicId = "${ch.id}_t$tpIdx"
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                val availableTopicsForChapter = remember(chapterIdValue, dbTopics) {
                                    dbTopics.filter { it.chapterId == chapterIdValue }
                                }

                                if (editMode) {
                                    var editTopicDropdownExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = selectedTopicToEdit?.title ?: "এডিটের জন্য টপিক সিলেক্ট করুন",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("টপিক নির্বাচন করুন") },
                                            trailingIcon = {
                                                IconButton(onClick = { editTopicDropdownExpanded = true }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Topic")
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().clickable { if (chapterIdValue.isNotBlank()) editTopicDropdownExpanded = true }
                                        )
                                        DropdownMenu(
                                            expanded = editTopicDropdownExpanded,
                                            onDismissRequest = { editTopicDropdownExpanded = false }
                                        ) {
                                            if (availableTopicsForChapter.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("কোন টপিক খুঁজে পাওয়া যায়নি") },
                                                    onClick = { editTopicDropdownExpanded = false }
                                                )
                                            } else {
                                                availableTopicsForChapter.forEach { tp ->
                                                    DropdownMenuItem(
                                                        text = { Text("টপিক ${tp.orderIndex}: ${tp.title}") },
                                                        onClick = {
                                                            selectedTopicToEdit = tp
                                                            editTopicDropdownExpanded = false
                                                            topicId = tp.id
                                                            topicOrderInput = tp.orderIndex.toString()
                                                            topicTitleValue = tp.title
                                                            summaryValue = tp.summary
                                                            keyPointsValue = tp.keyPointsJson.replace("|", " | ")
                                                            formulaeValue = tp.solutionData ?: ""
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = topicId,
                                    onValueChange = { if (!editMode) topicId = it.lowercase().replace(" ", "_") },
                                    readOnly = editMode,
                                    label = { Text("Topic ID (e.g. phy_ch1_t1)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = topicOrderInput,
                                    onValueChange = {
                                        if (!editMode) {
                                            topicOrderInput = it
                                            val idx = it.trim().toIntOrNull() ?: 1
                                            topicId = "${chapterIdValue}_t$idx"
                                        }
                                    },
                                    readOnly = editMode,
                                    label = { Text("টপিক ক্রমানুসার") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(value = topicTitleValue, onValueChange = { topicTitleValue = it }, label = { Text("টপিকের শিরোনাম (বাংলা)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = summaryValue, onValueChange = { summaryValue = it }, label = { Text("টপিকের সংক্ষিপ্ত সারসংক্ষেপ") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = keyPointsValue, onValueChange = { keyPointsValue = it }, label = { Text("গুরুত্বপূর্ণ পয়েন্টসমূহ (পাইপ '|' দিয়ে আলাদা করুন)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = formulaeValue, onValueChange = { formulaeValue = it }, label = { Text("সূত্রাবলী / বিশেষ পয়েন্ট (পাইপ '|' দিয়ে আলাদা করুন)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                
                                Button(
                                    onClick = {
                                        val kps = keyPointsValue.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                                        val fms = formulaeValue.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                                        val newTopic = com.example.data.firestore.FirestoreTopic(
                                            id = topicId.trim(),
                                            classId = classIdValue.trim(),
                                            subjectId = subjectIdValue.trim(),
                                            chapterId = chapterIdValue.trim(),
                                            orderIndex = topicOrderInput.trim().toIntOrNull() ?: 1,
                                            title = topicTitleValue.trim(),
                                            summary = summaryValue.trim(),
                                            keyPoints = kps,
                                            formulae = fms,
                                            questionCount = 0
                                        )
                                        viewModel.addFirestoreTopic(newTopic) { success, msg ->
                                            actionSuccess = success
                                            actionMessage = msg
                                            if (success && !editMode) {
                                                topicId = ""; topicTitleValue = ""; summaryValue = ""; keyPointsValue = ""; formulaeValue = ""
                                            }
                                        }
                                    },
                                    enabled = topicId.isNotBlank() && topicTitleValue.isNotBlank() && chapterIdValue.isNotBlank() && subjectIdValue.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = if (editMode) "💾 টপিক আপডেট করুন" else "💾 টপিক তৈরি করুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (subSectionTab == 3) {
                    item {
                        var chapterId by remember { mutableStateOf("") }
                        var classIdValue by remember { mutableStateOf("class_10_wbbse") }
                        var subjectIdValue by remember { mutableStateOf("") }
                        var chapterNumInput by remember { mutableStateOf("1") }
                        var chapterTitleValue by remember { mutableStateOf("") }
                        var descriptionValue by remember { mutableStateOf("") }
                        var totalTopicsInput by remember { mutableStateOf("5") }
                        var weightageInput by remember { mutableStateOf("15") }
                        
                        var editMode by remember { mutableStateOf(false) }
                        var selectedChapterToEdit by remember { mutableStateOf<com.example.data.local.ChapterEntity?>(null) }
                        
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = if (editMode) "📖 ফায়ারবেসে অধ্যায় এডিট করুন" else "📖 ফায়ারবেসে নতুন অধ্যায় (Chapter) যুক্ত করুন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = !editMode,
                                        onClick = {
                                            editMode = false
                                            selectedChapterToEdit = null
                                            chapterId = ""
                                            chapterNumInput = "1"
                                            chapterTitleValue = ""
                                            descriptionValue = ""
                                            totalTopicsInput = "5"
                                            weightageInput = "15"
                                        },
                                        label = { Text("➕ নতুন তৈরি করুন") }
                                    )
                                    FilterChip(
                                        selected = editMode,
                                        onClick = {
                                            editMode = true
                                        },
                                        label = { Text("✏️ এডিট করুন") }
                                    )
                                }
                                
                                var classDropdownExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = classIdValue,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("শ্রেণি নির্বাচন করুন") },
                                        trailingIcon = {
                                            IconButton(onClick = { classDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Class")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { classDropdownExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = classDropdownExpanded,
                                        onDismissRequest = { classDropdownExpanded = false }
                                    ) {
                                        val classesToShow = if (liveClasses.isNotEmpty()) liveClasses else com.example.data.model.AcademicClassProvider.ALL_CLASSES
                                        classesToShow.forEach { cls: com.example.data.model.AcademicClass ->
                                            val displayName = cls.name
                                            DropdownMenuItem(
                                                text = { Text("$displayName (${cls.board})") },
                                                onClick = {
                                                    classIdValue = cls.id
                                                    classDropdownExpanded = false
                                                    subjectIdValue = "" // Reset subject on class change
                                                }
                                            )
                                        }
                                    }
                                }

                                var subjectDropdownExpanded by remember { mutableStateOf(false) }
                                val availableSubsForClass = remember(classIdValue, dbSubjects) {
                                    getAdminClassSubjectsHelper(classIdValue)
                                }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = subjectIdValue,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("বিষয় নির্বাচন করুন") },
                                        placeholder = { Text("প্রথমে শ্রেণি নির্বাচন করুন") },
                                        trailingIcon = {
                                            IconButton(onClick = { subjectDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Subject")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { if (classIdValue.isNotBlank()) subjectDropdownExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = subjectDropdownExpanded,
                                        onDismissRequest = { subjectDropdownExpanded = false }
                                    ) {
                                        if (availableSubsForClass.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("কোন বিষয় খুঁজে পাওয়া যায়নি") },
                                                onClick = { subjectDropdownExpanded = false }
                                            )
                                        } else {
                                            availableSubsForClass.forEach { sub ->
                                                DropdownMenuItem(
                                                    text = { Text("${sub.name} (${sub.code})") },
                                                    onClick = {
                                                        subjectIdValue = sub.id
                                                        subjectDropdownExpanded = false
                                                        val chNum = chapterNumInput.trim().toIntOrNull() ?: 1
                                                        chapterId = "${sub.code}_ch$chNum"
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                val availableChaptersForSubject = remember(subjectIdValue, dbChapters) {
                                    dbChapters.filter { it.subjectId == subjectIdValue || it.subjectId == subjectIdValue.substringAfterLast("_", "") }
                                }

                                if (editMode) {
                                    var editChapterDropdownExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = selectedChapterToEdit?.title ?: "এডিটের জন্য অধ্যায় সিলেক্ট করুন",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("অধ্যায় নির্বাচন করুন") },
                                            trailingIcon = {
                                                IconButton(onClick = { editChapterDropdownExpanded = true }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Chapter")
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().clickable { if (subjectIdValue.isNotBlank()) editChapterDropdownExpanded = true }
                                        )
                                        DropdownMenu(
                                            expanded = editChapterDropdownExpanded,
                                            onDismissRequest = { editChapterDropdownExpanded = false }
                                        ) {
                                            if (availableChaptersForSubject.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("কোন অধ্যায় খুঁজে পাওয়া যায়নি") },
                                                    onClick = { editChapterDropdownExpanded = false }
                                                )
                                            } else {
                                                availableChaptersForSubject.forEach { ch ->
                                                    DropdownMenuItem(
                                                        text = { Text("অধ্যায় ${ch.chapterNumber}: ${ch.title}") },
                                                        onClick = {
                                                            selectedChapterToEdit = ch
                                                            editChapterDropdownExpanded = false
                                                            chapterId = ch.id
                                                            chapterNumInput = ch.chapterNumber.toString()
                                                            chapterTitleValue = ch.title
                                                            descriptionValue = ch.description
                                                            totalTopicsInput = ch.totalTopics.toString()
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = chapterId,
                                    onValueChange = { if (!editMode) chapterId = it.lowercase().replace(" ", "_") },
                                    readOnly = editMode,
                                    label = { Text("Chapter ID (e.g. phy_ch1)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = chapterNumInput,
                                    onValueChange = {
                                        if (!editMode) {
                                            chapterNumInput = it
                                            val subCode = if (subjectIdValue.contains("_")) subjectIdValue.substringAfterLast("_") else subjectIdValue
                                            val num = it.trim().toIntOrNull() ?: 1
                                            chapterId = "${subCode}_ch$num"
                                        }
                                    },
                                    readOnly = editMode,
                                    label = { Text("অধ্যায় নম্বর (e.g. 1, 2, 3)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(value = chapterTitleValue, onValueChange = { chapterTitleValue = it }, label = { Text("অধ্যায়ের শিরোনাম (বাংলা)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = descriptionValue, onValueChange = { descriptionValue = it }, label = { Text("অধ্যায়ের বিবরণ") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = totalTopicsInput, onValueChange = { totalTopicsInput = it }, label = { Text("টপিকের মোট সংখ্যা") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = weightageInput, onValueChange = { weightageInput = it }, label = { Text("নম্বর বরাদ্দ / গুরুত্ব (Weightage)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                
                                Button(
                                    onClick = {
                                        val newCh = com.example.data.firestore.FirestoreChapter(
                                            id = chapterId.trim(),
                                            classId = classIdValue.trim(),
                                            subjectId = subjectIdValue.trim(),
                                            chapterNumber = chapterNumInput.trim().toIntOrNull() ?: 1,
                                            title = chapterTitleValue.trim(),
                                            description = descriptionValue.trim(),
                                            totalTopics = totalTopicsInput.trim().toIntOrNull() ?: 5,
                                            weightageMarks = weightageInput.trim().toIntOrNull() ?: 15,
                                            orderIndex = chapterNumInput.trim().toIntOrNull() ?: 1
                                        )
                                        viewModel.addFirestoreChapter(newCh) { success, msg ->
                                            actionSuccess = success
                                            actionMessage = msg
                                            if (success && !editMode) {
                                                chapterId = ""; chapterTitleValue = ""; descriptionValue = ""
                                            }
                                        }
                                    },
                                    enabled = chapterId.isNotBlank() && chapterTitleValue.isNotBlank() && subjectIdValue.isNotBlank() && classIdValue.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = if (editMode) "💾 অধ্যায় আপডেট করুন" else "💾 অধ্যায় তৈরি করুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 8) {
            com.example.ui.screens.admin.AdminCurriculumCRUDTab(
                viewModel = viewModel,
                isBengali = isBengali,
                onShowMessage = { msg, success ->
                    actionSuccess = success
                    actionMessage = msg
                },
                modifier = Modifier.weight(1f)
            )
        } else if (selectedTab == 9) {
            AdminFirestoreIndexManager(
                isBengali = isBengali,
                modifier = Modifier.weight(1f)
            )
        } else if (selectedTab == 10) {
            val customPds by viewModel.customPodcasts.collectAsState()
            val clipboardManager = LocalClipboardManager.current

            LaunchedEffect(Unit) {
                viewModel.fetchCustomPodcasts()
            }

            var pTitle by remember { mutableStateOf("") }
            var pSubject by remember { mutableStateOf("") }
            var pDuration by remember { mutableStateOf("১০ মিনিট") }
            var pNarrator by remember { mutableStateOf("সহায়ক শিক্ষক") }
            var pBoardId by remember { mutableStateOf("WBBSE") }
            var pClassName by remember { mutableStateOf("Class 10") }
            var pAudioUrl by remember { mutableStateOf("") }
            var boardDropdownExp by remember { mutableStateOf(false) }
            var classDropdownExp by remember { mutableStateOf(false) }
            var subjectDropdownExp by remember { mutableStateOf(false) }
            var isSavingPodcast by remember { mutableStateOf(false) }

            val podcastBoardList = listOf(
                "WBBSE",
                "WBCHSE",
                "WBBPE",
                "CBSE",
                "ALL"
            )

            val getBoardDisplayName = { bId: String ->
                when (bId) {
                    "WBBSE" -> "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ (৫ম থেকে ১০ম)"
                    "WBCHSE" -> "পশ্চিমবঙ্গ উচ্চমাধ্যমিক শিক্ষা সংসদ (১১শ ও ১২শ)"
                    "WBBPE" -> "পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ (১ম থেকে ৪র্থ)"
                    "CBSE" -> "কেন্দ্রীয় মাধ্যমিক শিক্ষা পর্ষদ"
                    "ALL" -> "সকল শিক্ষা বোর্ড"
                    else -> bId
                }
            }

            val podcastClassList = listOf(
                "Class 10",
                "Class 12",
                "Class 11",
                "Class 9",
                "Class 8",
                "Class 7",
                "Class 6",
                "Class 5",
                "ALL"
            )

            val getClassDisplayName = { cls: String ->
                when (cls) {
                    "Class 5" -> "পঞ্চম শ্রেণি"
                    "Class 6" -> "ষষ্ঠ শ্রেণি"
                    "Class 7" -> "সপ্তম শ্রেণি"
                    "Class 8" -> "অষ্টম শ্রেণি"
                    "Class 9" -> "নবম শ্রেণি"
                    "Class 10" -> "দশম শ্রেণি (মাধ্যমিক)"
                    "Class 11" -> "একাদশ শ্রেণি (উচ্চমাধ্যমিক)"
                    "Class 12" -> "দ্বাদশ শ্রেণি (উচ্চমাধ্যমিক)"
                    "ALL" -> "সকল শ্রেণি"
                    else -> cls
                }
            }

            val getClassSubjects = { cls: String ->
                when (cls) {
                    "Class 11", "Class 12" -> listOf(
                        "পদার্থবিদ্যা",
                        "রসায়নবিদ্যা",
                        "জীববিজ্ঞান",
                        "গণিত",
                        "বাংলা",
                        "ইংরেজি",
                        "ইতিহাস",
                        "ভূগোল"
                    )
                    "Class 9", "Class 10" -> listOf(
                        "ইতিহাস",
                        "ভূগোল",
                        "জীবনবিজ্ঞান",
                        "ভৌতবিজ্ঞান",
                        "গণিত",
                        "বাংলা",
                        "ইংরেজি"
                    )
                    "Class 7", "Class 8" -> listOf(
                        "বিজ্ঞান",
                        "পরিবেশ ও বিজ্ঞান",
                        "গণিত",
                        "ইতিহাস",
                        "ভূগোল",
                        "বাংলা",
                        "ইংরেজি"
                    )
                    "Class 5", "Class 6" -> listOf(
                        "আমাদের পরিবেশ",
                        "বিজ্ঞান",
                        "গণিত",
                        "বাংলা",
                        "ইংরেজি"
                    )
                    else -> listOf(
                        "ইতিহাস",
                        "ভূগোল",
                        "জীবনবিজ্ঞান",
                        "ভৌতবিজ্ঞান",
                        "গণিত",
                        "বাংলা",
                        "ইংরেজি",
                        "সাধারণ লেকচার"
                    )
                }
            }

            var pSearchQuery by remember { mutableStateOf("") }
            var pFilterBoard by remember { mutableStateOf("ALL") }
            var pFilterClass by remember { mutableStateOf("ALL") }

            var editingPodcast by remember { mutableStateOf<FirestorePodcast?>(null) }
            var deletingPodcast by remember { mutableStateOf<FirestorePodcast?>(null) }

            var pBulkInput by remember { mutableStateOf("") }
            var bulkUploadStatusMessage by remember { mutableStateOf<String?>(null) }
            var bulkUploadSuccess by remember { mutableStateOf(true) }

            val pBulkSample = """শিরোনাম | বিষয় | সময়কাল | বক্তা | শ্রেণি | বোর্ড | অডিও ফাইল লিংক
বঙ্গভঙ্গ আন্দোলন ও স্বদেশী যুগ | ইতিহাস | ১২ মিনিট | ড. সুদীপ বোস | Class 10 | WBBSE | https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3
স্থিরতড়িৎ ও গাউসের সূত্র | পদার্থবিদ্যা | ১৬ মিনিট | ড. পার্থ মুখার্জী | Class 12 | WBCHSE | https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3
উদ্ভিদের সংবেদনশীলতা | জীবনবিজ্ঞান | ১৫ মিনিট | শ্রীমতী সেন | Class 9 | WBBSE | https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"""

            val testPlayAudio: (String, String, String) -> Unit = { url, title, subject ->
                if (url.isBlank()) {
                    actionSuccess = false
                    actionMessage = "⚠️ অডিও লিংক ফাঁকা! কোনো লিংক দিয়ে টেস্ট করুন।"
                } else if (AudioPodcastManager.isPlaying && AudioPodcastManager.currentlyPlaying?.audioUrl == url) {
                    AudioPodcastManager.onPlayPauseAction?.invoke()
                } else {
                    val intent = Intent(context, AudioPodcastService::class.java).apply {
                        action = AudioPodcastService.ACTION_PLAY
                        putExtra(AudioPodcastService.EXTRA_TITLE, title.ifBlank { "টেস্ট অডিও" })
                        putExtra(AudioPodcastService.EXTRA_SUBJECT, subject.ifBlank { "পডকাস্ট অডিও" })
                        putExtra(AudioPodcastService.EXTRA_NARRATOR, "অ্যাডমিন টেস্ট")
                        putExtra(AudioPodcastService.EXTRA_DURATION, "১০ মিনিট")
                        putExtra(AudioPodcastService.EXTRA_CLASS, "All")
                        putExtra(AudioPodcastService.EXTRA_URL, url)
                        putExtra(AudioPodcastService.EXTRA_BOARD_ID, "ALL")
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                }
            }

            val filteredCustomPds = remember(customPds, pFilterBoard, pFilterClass, pSearchQuery) {
                customPds.filter { p ->
                    val matchesBoard = pFilterBoard == "ALL" || p.boardId.equals(pFilterBoard, ignoreCase = true) || p.boardId.isBlank()
                    val matchesClass = pFilterClass == "ALL" || p.className.equals(pFilterClass, ignoreCase = true)
                    val query = pSearchQuery.trim().lowercase()
                    val matchesSearch = query.isBlank() ||
                            p.title.lowercase().contains(query) ||
                            p.subject.lowercase().contains(query) ||
                            p.narrator.lowercase().contains(query) ||
                            p.boardId.lowercase().contains(query)
                    matchesBoard && matchesClass && matchesSearch
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "এখানে যুক্ত করা অডিও পডকাস্ট সরাসরি শিক্ষার্থীদের পডকাস্ট স্ক্রিনে প্রদর্শিত হবে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 1. New Podcast Creation Form Card (Class-First, Structured & Accurate)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Icon(
                                            Icons.Default.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(6.dp).size(22.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "নতুন অডিও পডকাস্ট তৈরি করুন",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Firestore-এ সেভ ও লাইভ সিঙ্ক হবে",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = CorrectGreen.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(CorrectGreen)
                                        )
                                        Text(
                                            text = "ক্লাউড সিঙ্ক",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CorrectGreen,
                                            softWrap = false,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                            // ─── ধাপ ১: শিক্ষা বোর্ড নির্বাচন ───
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "১. শিক্ষা বোর্ড নির্বাচন *:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Quick Board Chips Row for 1-tap board selection
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(podcastBoardList) { bId ->
                                        val isSelected = pBoardId == bId
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                pBoardId = bId
                                                // Intelligent class adjustment when board is switched
                                                if (bId == "WBCHSE" && (pClassName !in listOf("Class 11", "Class 12", "ALL"))) {
                                                    pClassName = "Class 12"
                                                } else if (bId == "WBBSE" && (pClassName in listOf("Class 11", "Class 12"))) {
                                                    pClassName = "Class 10"
                                                } else if (bId == "WBBPE" && (pClassName !in listOf("Class 5", "ALL"))) {
                                                    pClassName = "Class 5"
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = when (bId) {
                                                        "WBBSE" -> "মধ্যশিক্ষা পর্ষদ"
                                                        "WBCHSE" -> "উচ্চমাধ্যমিক সংসদ"
                                                        "WBBPE" -> "প্রাথমিক শিক্ষা পর্ষদ"
                                                        "CBSE" -> "কেন্দ্রীয় পর্ষদ"
                                                        "ALL" -> "সকল বোর্ড"
                                                        else -> bId
                                                    },
                                                    fontSize = 11.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimaryIndigo.copy(alpha = 0.2f),
                                                selectedLabelColor = PrimaryIndigo
                                            )
                                        )
                                    }
                                }

                                // Full Board Dropdown Selector
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = getBoardDisplayName(pBoardId),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("নির্বাচিত বোর্ড") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boardDropdownExp) },
                                        modifier = Modifier.fillMaxWidth().clickable { boardDropdownExp = true },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { boardDropdownExp = true }
                                    )
                                    DropdownMenu(
                                        expanded = boardDropdownExp,
                                        onDismissRequest = { boardDropdownExp = false }
                                    ) {
                                        podcastBoardList.forEach { bId ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = getBoardDisplayName(bId),
                                                        fontWeight = if (pBoardId == bId) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    pBoardId = bId
                                                    if (bId == "WBCHSE" && (pClassName !in listOf("Class 11", "Class 12", "ALL"))) {
                                                        pClassName = "Class 12"
                                                    } else if (bId == "WBBSE" && (pClassName in listOf("Class 11", "Class 12"))) {
                                                        pClassName = "Class 10"
                                                    } else if (bId == "WBBPE" && (pClassName !in listOf("Class 5", "ALL"))) {
                                                        pClassName = "Class 5"
                                                    }
                                                    boardDropdownExp = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // ─── ধাপ ২: শ্রেণি নির্বাচন ───
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "২. লক্ষ্য শ্রেণি:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Quick Class Chips Row for 1-tap class selection
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(podcastClassList) { cls ->
                                        val isSelected = pClassName == cls
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { pClassName = cls },
                                            label = {
                                                Text(
                                                    text = when (cls) {
                                                        "Class 10" -> "দশম শ্রেণি"
                                                        "Class 12" -> "দ্বাদশ শ্রেণি"
                                                        "Class 11" -> "একাদশ শ্রেণি"
                                                        "Class 9" -> "নবম শ্রেণি"
                                                        "Class 8" -> "অষ্টম শ্রেণি"
                                                        "Class 7" -> "সপ্তম শ্রেণি"
                                                        "Class 6" -> "ষষ্ঠ শ্রেণি"
                                                        "Class 5" -> "পঞ্চম শ্রেণি"
                                                        "ALL" -> "সকল শ্রেণি"
                                                        else -> cls
                                                    },
                                                    fontSize = 11.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimaryIndigo.copy(alpha = 0.2f),
                                                selectedLabelColor = PrimaryIndigo
                                            )
                                        )
                                    }
                                }

                                // Full Class Dropdown Selector
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = getClassDisplayName(pClassName),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("নির্বাচিত শ্রেণি") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExp) },
                                        modifier = Modifier.fillMaxWidth().clickable { classDropdownExp = true },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { classDropdownExp = true }
                                    )
                                    DropdownMenu(
                                        expanded = classDropdownExp,
                                        onDismissRequest = { classDropdownExp = false }
                                    ) {
                                        podcastClassList.forEach { cls ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = getClassDisplayName(cls),
                                                        fontWeight = if (pClassName == cls) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    pClassName = cls
                                                    classDropdownExp = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // ─── ধাপ ৩: বিষয় নির্বাচন ───
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "৩. বিষয় নির্বাচন:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = pSubject,
                                        onValueChange = { pSubject = it },
                                        label = { Text("বিষয় (যেমন: ইতিহাস, ভূগোল, পদার্থবিদ্যা)") },
                                        placeholder = { Text("ক্লিক করে বা লিখে বিষয় নির্ধারণ করুন") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        trailingIcon = {
                                            IconButton(onClick = { subjectDropdownExp = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Subject")
                                            }
                                        }
                                    )
                                    DropdownMenu(
                                        expanded = subjectDropdownExp,
                                        onDismissRequest = { subjectDropdownExp = false }
                                    ) {
                                        getClassSubjects(pClassName).forEach { sub ->
                                            DropdownMenuItem(
                                                text = { Text(sub, fontWeight = if (pSubject == sub) FontWeight.Bold else FontWeight.Normal) },
                                                onClick = {
                                                    pSubject = sub
                                                    subjectDropdownExp = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "দ্রুত পছন্দের বিষয় বেছে নিন:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(getClassSubjects(pClassName)) { qs ->
                                        val isSubSelected = pSubject == qs
                                        FilterChip(
                                            selected = isSubSelected,
                                            onClick = { pSubject = qs },
                                            label = { Text(qs, fontSize = 11.5.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = ExamGold.copy(alpha = 0.2f),
                                                selectedLabelColor = ExamGold
                                            )
                                        )
                                    }
                                }
                            }

                            // ─── ধাপ ৪: পডকাস্টের শিরোনাম ───
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Title, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "৪. পডকাস্টের শিরোনাম *:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                OutlinedTextField(
                                    value = pTitle,
                                    onValueChange = { pTitle = it },
                                    label = { Text("শিরোনাম লিখুন *") },
                                    placeholder = { Text("যেমন: চলতড়িৎ ও ওহমের সূত্রের গাণিতিক শর্টকাট") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                                Text(
                                    text = "💡 পডকাস্টে আলোচিত অধ্যায় ও মূল বিষয়ের সুস্পষ্ট নাম দিন।",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // ─── ধাপ ৫: সময়কাল ও বক্তা/শিক্ষকের নাম ───
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "৫. সময়কাল:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    OutlinedTextField(
                                        value = pDuration,
                                        onValueChange = { pDuration = it },
                                        label = { Text("যেমন: ১০ মিনিট") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(listOf("৫ মিনিট", "১০ মিনিট", "১৫ মিনিট", "২০ মিনিট", "৩০ মিনিট")) { dur ->
                                            SuggestionChip(
                                                onClick = { pDuration = dur },
                                                label = { Text(dur, fontSize = 10.5.sp) }
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "বক্তা / শিক্ষক:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    OutlinedTextField(
                                        value = pNarrator,
                                        onValueChange = { pNarrator = it },
                                        label = { Text("যেমন: সহায়ক শিক্ষক") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(listOf("সহায়ক শিক্ষক", "বিষয় শিক্ষক", "ড. সমীর রায়", "অধ্যাপক এস. বোস")) { nar ->
                                            SuggestionChip(
                                                onClick = { pNarrator = nar },
                                                label = { Text(nar, fontSize = 10.5.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            // ─── ধাপ ৬: অডিও ফাইল লিংক ও ইনস্ট্যান্ট প্রিভিউ টেস্ট ───
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Audiotrack, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "৬. অডিও ফাইল লিংক *:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                OutlinedTextField(
                                    value = pAudioUrl,
                                    onValueChange = { pAudioUrl = it },
                                    label = { Text("সরাসরি প্লে-যোগ্য অডিও ফাইলের লিংক দিন *") },
                                    placeholder = { Text("https://example.com/audio.mp3") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = PrimaryIndigo) },
                                    trailingIcon = {
                                        if (pAudioUrl.isNotBlank()) {
                                            IconButton(onClick = { pAudioUrl = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                )

                                Text(
                                    text = "💡 MP3, M4A, Firebase Storage বা যেকোনো সরাসরি প্লে-যোগ্য অডিও লিংক দিন।",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clip = clipboardManager.getText()?.text
                                            if (!clip.isNullOrBlank()) {
                                                pAudioUrl = clip.trim()
                                                actionSuccess = true
                                                actionMessage = "✓ ক্লিপবোর্ড থেকে অডিও লিংক পেস্ট করা হয়েছে!"
                                            } else {
                                                actionSuccess = false
                                                actionMessage = "⚠️ ক্লিপবোর্ডে কোনো লিংক পাওয়া যায়নি!"
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("পেস্ট", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            pAudioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("নমুনা URL", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    val isFormPlaying = AudioPodcastManager.isPlaying && AudioPodcastManager.currentlyPlaying?.audioUrl == pAudioUrl.trim()
                                    Button(
                                        onClick = {
                                            if (pAudioUrl.isBlank()) {
                                                actionSuccess = false
                                                actionMessage = "⚠️ টেস্ট শোনার জন্য প্রথমে একটি অডিও লিংক দিন বা 'নমুনা URL' এ ট্যাপ করুন।"
                                            } else {
                                                testPlayAudio(pAudioUrl.trim(), pTitle.trim(), pSubject.trim())
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFormPlaying) WrongRed else if (pAudioUrl.isNotBlank()) CorrectGreen else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Icon(
                                            if (isFormPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isFormPlaying) "পজ" else "টেস্ট শুনুন", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // ─── লাইভ স্টুডেন্ট প্রিভিউ কার্ড (Live Student View Preview) ───
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "👁️ শিক্ষার্থীদের অ্যাপে লাইভ প্রিভিউ:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (pAudioUrl.isNotBlank()) CorrectGreen.copy(alpha = 0.15f) else ExamGold.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, if (pAudioUrl.isNotBlank()) CorrectGreen.copy(alpha = 0.4f) else ExamGold.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = if (pAudioUrl.isNotBlank()) "লিংক প্রস্তুত ✔️" else "লিংক ফাঁকা ⚠️",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (pAudioUrl.isNotBlank()) CorrectGreen else ExamGold,
                                                softWrap = false,
                                                maxLines = 1,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    // Mock Preview Card
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(PrimaryIndigo.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Headphones,
                                                    contentDescription = null,
                                                    tint = PrimaryIndigo,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = PrimaryIndigo.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = pBoardId,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = PrimaryIndigo,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = ExamGold.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = getClassDisplayName(pClassName),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = ExamGold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    ) {
                                                        Text(
                                                            text = pSubject.ifBlank { "বিষয়" },
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = pTitle.ifBlank { "পডকাস্টের শিরোনাম এখানে দেখাবে..." },
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (pTitle.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    maxLines = 1
                                                )

                                                Text(
                                                    text = "${pDuration.ifBlank { "১০ মিনিট" }} • ${pNarrator.ifBlank { "সহায়ক শিক্ষক" }}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // ─── সংরক্ষণ বোতাম (Save Podcast to Firebase) ───
                            Button(
                                onClick = {
                                    val finalDuration = pDuration.trim().ifBlank { "১০ মিনিট" }
                                    val finalNarrator = pNarrator.trim().ifBlank { "সহায়ক শিক্ষক" }
                                    val finalSubject = pSubject.trim().ifBlank { "সাধারণ লেকচার" }
                                    isSavingPodcast = true

                                    viewModel.addCustomPodcast(
                                        title = pTitle.trim(),
                                        subject = finalSubject,
                                        duration = finalDuration,
                                        narrator = finalNarrator,
                                        className = pClassName,
                                        boardId = pBoardId,
                                        audioUrl = pAudioUrl.trim()
                                    ) { success, msg ->
                                        isSavingPodcast = false
                                        actionSuccess = success
                                        actionMessage = msg
                                        if (success) {
                                            pTitle = ""
                                            pSubject = ""
                                            pDuration = "১০ মিনিট"
                                            pNarrator = "সহায়ক শিক্ষক"
                                            pAudioUrl = ""
                                        }
                                    }
                                },
                                enabled = pTitle.isNotBlank() && pAudioUrl.isNotBlank() && !isSavingPodcast,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSavingPodcast) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ফায়ারবেসে সংরক্ষণ হচ্ছে...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("🎙️ ফায়ারবেসে পডকাস্ট সংরক্ষণ করুন", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                                }
                            }

                            if (pTitle.isBlank() || pAudioUrl.isBlank()) {
                                Text(
                                    text = "⚠️ সংরক্ষণ করতে শিরোনাম ও অডিও ফাইল লিংক অবশ্যই পূরণ করুন।",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ExamGold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Batch Upload Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                ) {
                                    Icon(
                                        Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(6.dp).size(20.dp)
                                    )
                                }
                                Text(
                                    text = "📤 ব্যাচ পডকাস্ট আপলোড (CSV / JSON)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Text(
                                text = "নিচে পাইপ (|) বা কমা (,) দিয়ে আলাদা করে একাধিক পডকাস্ট একসাথে আপলোড করুন:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        pBulkInput = pBulkSample
                                        bulkUploadStatusMessage = "✓ নমুনা CSV লোড হয়েছে!"
                                        bulkUploadSuccess = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("📋 নমুনা CSV লোড", fontSize = 11.5.sp, maxLines = 1)
                                }

                                if (pBulkInput.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            pBulkInput = ""
                                            bulkUploadStatusMessage = null
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = WrongRed)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = pBulkInput,
                                onValueChange = { pBulkInput = it },
                                placeholder = {
                                    Text("শিরোনাম | বিষয় | সময়কাল | বক্তা | শ্রেণি | বোর্ড (যেমন: WBBSE / WBCHSE) | অডিও URL")
                                },
                                minLines = 4,
                                maxLines = 8,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            bulkUploadStatusMessage?.let { msg ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (bulkUploadSuccess) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, if (bulkUploadSuccess) CorrectGreen.copy(alpha = 0.4f) else WrongRed.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (bulkUploadSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (bulkUploadSuccess) CorrectGreen else WrongRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = msg,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (bulkUploadSuccess) CorrectGreen else WrongRed
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.importBulkCustomPodcasts(pBulkInput) { success, msg, count ->
                                        bulkUploadSuccess = success
                                        bulkUploadStatusMessage = msg
                                        actionSuccess = success
                                        actionMessage = msg
                                        if (success) {
                                            pBulkInput = ""
                                        }
                                    }
                                },
                                enabled = pBulkInput.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📤 কাস্টম পডকাস্ট ব্যাচ আপলোড করুন", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                        }
                    }
                }

                // 3. Existing Custom Podcasts List with Search & Filter
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 বর্তমান কাস্টম পডকাস্টসমূহ (${customPds.size} টি)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryIndigo.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "ফিল্টার্ড: ${filteredCustomPds.size} টি",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Search Field
                        OutlinedTextField(
                            value = pSearchQuery,
                            onValueChange = { pSearchQuery = it },
                            placeholder = { Text("🔎 পডকাস্ট খুঁজুন (শিরোনাম, বিষয় বা বক্তা)...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingIcon = {
                                if (pSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { pSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear Search", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Board & Class Filter Chips
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "শিক্ষা বোর্ড ফিল্টার:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val boards = listOf("ALL", "WBBSE", "WBCHSE", "WBBPE", "CBSE")
                                items(boards) { b ->
                                    val isSelected = pFilterBoard == b
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { pFilterBoard = b },
                                        label = { Text(if (b == "ALL") "সকল বোর্ড" else b, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryIndigo.copy(alpha = 0.2f),
                                            selectedLabelColor = PrimaryIndigo
                                        )
                                    )
                                }
                            }
                        }

                        // Class Filter Chips
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "শ্রেণি ফিল্টার:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val classes = listOf("ALL", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12")
                                items(classes) { cls ->
                                    val isSelected = pFilterClass == cls
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { pFilterClass = cls },
                                        label = { Text(getClassDisplayName(cls), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryIndigo.copy(alpha = 0.2f),
                                            selectedLabelColor = PrimaryIndigo
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredCustomPds.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = if (customPds.isEmpty()) "কোনো কাস্টম পডকাস্ট পাওয়া যায়নি।\nউপরের ফর্মটি পূরণ করে যুক্ত করুন।"
                                        else "ফিল্টারের সাথে মেলে এমন কোনো পডকাস্ট নেই।",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredCustomPds) { p ->
                        val isThisPlaying = AudioPodcastManager.isPlaying && AudioPodcastManager.currentlyPlaying?.audioUrl == p.audioUrl
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isThisPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isThisPlaying) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Audio Play / Pause Avatar Icon
                                    IconButton(
                                        onClick = {
                                            testPlayAudio(p.audioUrl, p.title, p.subject)
                                        },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isThisPlaying) WrongRed else PrimaryIndigo.copy(alpha = 0.15f)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isThisPlaying) "Pause" else "Play",
                                            tint = if (isThisPlaying) Color.White else PrimaryIndigo,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = p.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = PrimaryIndigo.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = p.boardId.ifBlank { "WBBSE" },
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = ExamGold.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = getClassDisplayName(p.className),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ExamGold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = p.subject.replace(Regex("\\s*\\([A-Za-z\\s&]+\\)\\s*"), "").trim(),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "• ${p.narrator}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = p.duration,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ExamGold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Audio URL display row with copy & actions
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.Link,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = p.audioUrl,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(p.audioUrl))
                                                actionSuccess = true
                                                actionMessage = "✓ অডিও লিংক কপি করা হয়েছে!"
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copy Link",
                                                tint = PrimaryIndigo,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Bottom Row Action Buttons: Edit & Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isThisPlaying) Icons.Default.VolumeUp else Icons.Default.Audiotrack,
                                            contentDescription = null,
                                            tint = if (isThisPlaying) CorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (isThisPlaying) "🔊 প্লে হচ্ছে..." else "আইডি: ${p.id.takeLast(8)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isThisPlaying) CorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isThisPlaying) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { editingPodcast = p },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("সম্পাদনা", fontSize = 11.5.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { deletingPodcast = p },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WrongRed),
                                            border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.5f)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WrongRed, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("মুছুন", fontSize = 11.5.sp, color = WrongRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Edit Podcast Dialog
            if (editingPodcast != null) {
                val ep = editingPodcast!!
                var editBoardId by remember(ep) { mutableStateOf(ep.boardId.ifBlank { "WBBSE" }) }
                var editTitle by remember(ep) { mutableStateOf(ep.title) }
                var editSubject by remember(ep) { mutableStateOf(ep.subject) }
                var editDuration by remember(ep) { mutableStateOf(ep.duration) }
                var editNarrator by remember(ep) { mutableStateOf(ep.narrator) }
                var editClassName by remember(ep) { mutableStateOf(ep.className) }
                var editAudioUrl by remember(ep) { mutableStateOf(ep.audioUrl) }
                var editBoardDropdownExp by remember { mutableStateOf(false) }
                var editClassDropdownExp by remember { mutableStateOf(false) }
                var editSubjectDropdownExp by remember { mutableStateOf(false) }

                Dialog(
                    onDismissRequest = { editingPodcast = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(18.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ExamGold.copy(alpha = 0.2f)
                                    ) {
                                        Text("✏️", fontSize = 16.sp, modifier = Modifier.padding(6.dp))
                                    }
                                    Column {
                                        Text(
                                            text = "পডকাস্ট সম্পাদনা ও আপডেট",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "আইডি: ${ep.id}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ExamGold
                                        )
                                    }
                                }

                                IconButton(onClick = { editingPodcast = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }

                            HorizontalDivider()

                            // ১. শিক্ষা বোর্ড নির্বাচন
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "১. শিক্ষা বোর্ড *:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(podcastBoardList) { bId ->
                                        val isSelected = editBoardId == bId
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                editBoardId = bId
                                                if (bId == "WBCHSE" && (editClassName !in listOf("Class 11", "Class 12", "ALL"))) {
                                                    editClassName = "Class 12"
                                                } else if (bId == "WBBSE" && (editClassName in listOf("Class 11", "Class 12"))) {
                                                    editClassName = "Class 10"
                                                } else if (bId == "WBBPE" && (editClassName !in listOf("Class 5", "ALL"))) {
                                                    editClassName = "Class 5"
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = when (bId) {
                                                        "WBBSE" -> "মধ্যশিক্ষা পর্ষদ"
                                                        "WBCHSE" -> "উচ্চমাধ্যমিক সংসদ"
                                                        "WBBPE" -> "প্রাথমিক শিক্ষা পর্ষদ"
                                                        "CBSE" -> "কেন্দ্রীয় পর্ষদ"
                                                        "ALL" -> "সকল বোর্ড"
                                                        else -> bId
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimaryIndigo.copy(alpha = 0.2f),
                                                selectedLabelColor = PrimaryIndigo
                                            )
                                        )
                                    }
                                }

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = getBoardDisplayName(editBoardId),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("নির্বাচিত বোর্ড") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editBoardDropdownExp) },
                                        modifier = Modifier.fillMaxWidth().clickable { editBoardDropdownExp = true },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { editBoardDropdownExp = true }
                                    )
                                    DropdownMenu(
                                        expanded = editBoardDropdownExp,
                                        onDismissRequest = { editBoardDropdownExp = false }
                                    ) {
                                        podcastBoardList.forEach { bId ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = getBoardDisplayName(bId),
                                                        fontWeight = if (editBoardId == bId) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    editBoardId = bId
                                                    if (bId == "WBCHSE" && (editClassName !in listOf("Class 11", "Class 12", "ALL"))) {
                                                        editClassName = "Class 12"
                                                    } else if (bId == "WBBSE" && (editClassName in listOf("Class 11", "Class 12"))) {
                                                        editClassName = "Class 10"
                                                    } else if (bId == "WBBPE" && (editClassName !in listOf("Class 5", "ALL"))) {
                                                        editClassName = "Class 5"
                                                    }
                                                    editBoardDropdownExp = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // ২. শ্রেণি নির্বাচন
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "২. লক্ষ্য শ্রেণি:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = getClassDisplayName(editClassName),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("শ্রেণি") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editClassDropdownExp) },
                                        modifier = Modifier.fillMaxWidth().clickable { editClassDropdownExp = true },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { editClassDropdownExp = true }
                                    )
                                    DropdownMenu(
                                        expanded = editClassDropdownExp,
                                        onDismissRequest = { editClassDropdownExp = false }
                                    ) {
                                        podcastClassList.forEach { cls ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = getClassDisplayName(cls),
                                                        fontWeight = if (editClassName == cls) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    editClassName = cls
                                                    editClassDropdownExp = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // ৩. বিষয় নির্বাচন
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "৩. বিষয়:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = editSubject,
                                        onValueChange = { editSubject = it },
                                        label = { Text("বিষয়") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        trailingIcon = {
                                            IconButton(onClick = { editSubjectDropdownExp = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        }
                                    )
                                    DropdownMenu(
                                        expanded = editSubjectDropdownExp,
                                        onDismissRequest = { editSubjectDropdownExp = false }
                                    ) {
                                        getClassSubjects(editClassName).forEach { sub ->
                                            DropdownMenuItem(
                                                text = { Text(sub) },
                                                onClick = {
                                                    editSubject = sub
                                                    editSubjectDropdownExp = false
                                                }
                                            )
                                        }
                                    }
                                }

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(getClassSubjects(editClassName)) { qs ->
                                        FilterChip(
                                            selected = editSubject == qs,
                                            onClick = { editSubject = qs },
                                            label = { Text(qs, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            // ৪. শিরোনাম
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "৪. পডকাস্টের শিরোনাম *:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                OutlinedTextField(
                                    value = editTitle,
                                    onValueChange = { editTitle = it },
                                    label = { Text("শিরোনাম লিখুন *") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // ৫. সময়কাল ও বক্তা
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = editDuration,
                                    onValueChange = { editDuration = it },
                                    label = { Text("৫. সময়কাল") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = editNarrator,
                                    onValueChange = { editNarrator = it },
                                    label = { Text("বক্তা / শিক্ষক") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // ৬. অডিও ফাইল লিংক
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "৬. অডিও ফাইল লিংক *:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                OutlinedTextField(
                                    value = editAudioUrl,
                                    onValueChange = { editAudioUrl = it },
                                    label = { Text("অডিও ফাইল লিংক *") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = PrimaryIndigo) },
                                    trailingIcon = {
                                        if (editAudioUrl.isNotBlank()) {
                                            IconButton(onClick = { editAudioUrl = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                )

                                val isEditPlaying = AudioPodcastManager.isPlaying && AudioPodcastManager.currentlyPlaying?.audioUrl == editAudioUrl.trim()
                                Button(
                                    onClick = {
                                        testPlayAudio(editAudioUrl.trim(), editTitle.trim(), editSubject.trim())
                                    },
                                    enabled = editAudioUrl.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEditPlaying) WrongRed else CorrectGreen
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        if (isEditPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isEditPlaying) "⏸️ অডিও পজ করুন" else "▶️ অডিও টেস্ট প্লে করুন", fontSize = 12.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { editingPodcast = null },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("বাতিল")
                                }

                                Button(
                                    onClick = {
                                        val updated = ep.copy(
                                            title = editTitle.trim(),
                                            subject = editSubject.trim().ifBlank { "সাধারণ লেকচার" },
                                            duration = editDuration.trim().ifBlank { "১০ মিনিট" },
                                            narrator = editNarrator.trim().ifBlank { "সহায়ক শিক্ষক" },
                                            className = editClassName,
                                            boardId = editBoardId,
                                            audioUrl = editAudioUrl.trim()
                                        )
                                        viewModel.updateCustomPodcast(updated) { success, msg ->
                                            actionSuccess = success
                                            actionMessage = msg
                                            if (success) {
                                                editingPodcast = null
                                            }
                                        }
                                    },
                                    enabled = editTitle.isNotBlank() && editAudioUrl.isNotBlank(),
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("আপডেট সংরক্ষণ", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Delete Confirmation Dialog (with Warning Icon at CENTER TOP of the Card)
            if (deletingPodcast != null) {
                val dp = deletingPodcast!!
                Dialog(
                    onDismissRequest = { deletingPodcast = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // ⚠️ Warning Icon placed at CENTER TOP of the card
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(WrongRed)
                                        .border(2.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = "Warning",
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }

                                Text(
                                    text = "পডকাস্ট মুছে ফেলতে চান?",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = dp.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Text(
                                            text = "${dp.subject} • ${dp.className} • ${dp.narrator}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "⚠️ এটি ক্লাউড ফায়ারস্টোর থেকে স্থায়ীভাবে মুছে ফেলা হবে এবং শিক্ষার্থীরা আর এই পডকাস্টটি শুনতে পাবে না।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { deletingPodcast = null },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Text("বাতিল")
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.deleteCustomPodcast(dp.id) { success, msg ->
                                                actionSuccess = success
                                                actionMessage = msg
                                                if (success) {
                                                    deletingPodcast = null
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("মুছে ফেলুন", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingQuestion != null) {
        val q = editingQuestion!!
        var editQText by remember(q) { mutableStateOf(q.question) }
        var editOpt0 by remember(q) { mutableStateOf(q.options.getOrNull(0) ?: "") }
        var editOpt1 by remember(q) { mutableStateOf(q.options.getOrNull(1) ?: "") }
        var editOpt2 by remember(q) { mutableStateOf(q.options.getOrNull(2) ?: "") }
        var editOpt3 by remember(q) { mutableStateOf(q.options.getOrNull(3) ?: "") }
        var editCorrectIdx by remember(q) { mutableIntStateOf(q.correctOptionIndex) }
        var editDifficulty by remember(q) { mutableStateOf(if (q.difficulty.isNotBlank()) q.difficulty.uppercase() else "MEDIUM") }

        val existingPyq = q.tags.firstOrNull { it.startsWith("PYQ_") }?.removePrefix("PYQ_")
            ?: q.tags.firstOrNull { it.matches(Regex("\\d{4}")) } ?: ""
        var editPyqYear by remember(q) { mutableStateOf(existingPyq) }
        var editMarks by remember(q) { mutableIntStateOf(if (q.marks > 0) q.marks else 1) }
        var editExplanation by remember(q) { mutableStateOf(q.explanation) }

        var editClassId by remember(q) { mutableStateOf(q.classId.ifBlank { "class_10_wbbse" }) }
        var editSubjectId by remember(q) { mutableStateOf(q.subjectId) }
        var editChapterId by remember(q) { mutableStateOf(q.chapterId) }
        var editTopicId by remember(q) { mutableStateOf(q.topicId) }

        var editClassDropdownExpanded by remember { mutableStateOf(false) }
        var editSubjectDropdownExpanded by remember { mutableStateOf(false) }
        var editChapterDropdownExpanded by remember { mutableStateOf(false) }
        var editTopicDropdownExpanded by remember { mutableStateOf(false) }

        val editSubjects = remember(editClassId, dbSubjects) { getAdminClassSubjectsHelper(editClassId) }
        val editFilteredChapters = remember(editSubjectId, effectiveChapters) {
            effectiveChapters.filter { it.subjectId == editSubjectId }
        }
        val editFilteredTopics = remember(editChapterId, effectiveTopics) {
            effectiveTopics.filter { it.chapterId == editChapterId }
        }

        Dialog(
            onDismissRequest = { editingQuestion = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f)
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ExamGold.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "✍️",
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "প্রশ্ন সম্পাদনা ও আপডেট",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "ID: ${q.id} • MCQ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExamGold
                                    )
                                }
                            }
                            IconButton(onClick = { editingQuestion = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "শ্রেণি, বিষয়, অধ্যায় ও টপিক (এক সারিতে নির্বাচন করুন):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ExamGold)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    val currClass = allClasses.firstOrNull { it.id == editClassId }
                                    val clsLabel = currClass?.name ?: editClassId
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable { editClassDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Class, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "🎓 $clsLabel",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = editClassDropdownExpanded,
                                        onDismissRequest = { editClassDropdownExpanded = false }
                                    ) {
                                        allClasses.forEach { cls ->
                                            DropdownMenuItem(
                                                text = { Text(cls.name, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    editClassId = cls.id
                                                    val subs = getAdminClassSubjects(cls.id)
                                                    editSubjectId = subs.firstOrNull()?.id ?: ""
                                                    val chs = allChapters.filter { it.subjectId == editSubjectId }
                                                    editChapterId = chs.firstOrNull()?.id ?: ""
                                                    val tps = allTopics.filter { it.chapterId == editChapterId }
                                                    editTopicId = tps.firstOrNull()?.id ?: ""
                                                    editClassDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Box {
                                    val currSub = editSubjects.firstOrNull { it.id == editSubjectId }
                                    val subLabel = currSub?.name ?: editSubjectId.replace("subject_", "").replace("sub_", "").replace("_", " ")
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable { editSubjectDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "📚 $subLabel",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = editSubjectDropdownExpanded,
                                        onDismissRequest = { editSubjectDropdownExpanded = false }
                                    ) {
                                        editSubjects.forEach { sub ->
                                            DropdownMenuItem(
                                                text = { Text(sub.name, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    editSubjectId = sub.id
                                                    val chs = allChapters.filter { it.subjectId == sub.id }
                                                    editChapterId = chs.firstOrNull()?.id ?: ""
                                                    val tps = allTopics.filter { it.chapterId == editChapterId }
                                                    editTopicId = tps.firstOrNull()?.id ?: ""
                                                    editSubjectDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Box {
                                    val currCh = editFilteredChapters.firstOrNull { it.id == editChapterId }
                                    val chLabel = currCh?.title ?: if (editChapterId.isNotBlank()) editChapterId else ("অধ্যায় নেই")
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.clickable { editChapterDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "📖 $chLabel",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = editChapterDropdownExpanded,
                                        onDismissRequest = { editChapterDropdownExpanded = false }
                                    ) {
                                        if (editFilteredChapters.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("কোনো অধ্যায় নেই") },
                                                onClick = { editChapterDropdownExpanded = false }
                                            )
                                        } else {
                                            editFilteredChapters.forEach { ch ->
                                                DropdownMenuItem(
                                                    text = { Text(ch.title) },
                                                    onClick = {
                                                        editChapterId = ch.id
                                                        val tps = allTopics.filter { it.chapterId == ch.id }
                                                        editTopicId = tps.firstOrNull()?.id ?: ""
                                                        editChapterDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Box {
                                    val currTp = editFilteredTopics.firstOrNull { it.id == editTopicId }
                                    val tpLabel = currTp?.title ?: if (editTopicId.isNotBlank()) editTopicId else ("টপিক নেই")
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.clickable { editTopicDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Topic, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "🏷️ $tpLabel",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = editTopicDropdownExpanded,
                                        onDismissRequest = { editTopicDropdownExpanded = false }
                                    ) {
                                        if (editFilteredTopics.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("কোনো টপিক নেই") },
                                                onClick = { editTopicDropdownExpanded = false }
                                            )
                                        } else {
                                            editFilteredTopics.forEach { tp ->
                                                DropdownMenuItem(
                                                    text = { Text(tp.title) },
                                                    onClick = {
                                                        editTopicId = tp.id
                                                        editTopicDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editQText,
                            onValueChange = { editQText = it },
                            label = { Text("প্রশ্নের মূল বিবরণ:") },
                            placeholder = { Text("এখানে প্রশ্নটি লিখুন...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "৪টি অপশন ও সঠিক উত্তর নির্বাচন:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CorrectGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "সঠিক উত্তরে ক্লিক করুন ✓",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CorrectGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            val optLabels = listOf("ক (A)", "খ (B)", "গ (C)", "ঘ (D)")
                            val optValues = listOf(editOpt0, editOpt1, editOpt2, editOpt3)
                            val setOptValues = listOf<(String) -> Unit>(
                                { editOpt0 = it },
                                { editOpt1 = it },
                                { editOpt2 = it },
                                { editOpt3 = it }
                            )

                            optLabels.forEachIndexed { idx, label ->
                                val isCorrect = editCorrectIdx == idx
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isCorrect) CorrectGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        if (isCorrect) 1.5.dp else 1.dp,
                                        if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clickable { editCorrectIdx = idx }
                                                .padding(2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                RadioButton(
                                                    selected = isCorrect,
                                                    onClick = { editCorrectIdx = idx },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = Color.White,
                                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        OutlinedTextField(
                                            value = optValues[idx],
                                            onValueChange = setOptValues[idx],
                                            placeholder = { Text("অপশন $label এর বিবরণ") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = if (isCorrect) CorrectGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = editPyqYear,
                                onValueChange = { editPyqYear = it },
                                label = { Text("বিগত সাল / PYQ (ঐচ্ছিক)", maxLines = 1) },
                                placeholder = { Text("2025") },
                                leadingIcon = { Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp)) },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = editMarks.toString(),
                                onValueChange = { editMarks = it.toIntOrNull() ?: 1 },
                                label = { Text("নম্বর (Marks)", maxLines = 1) },
                                leadingIcon = { Icon(Icons.Default.Score, contentDescription = null, tint = ExamGold, modifier = Modifier.size(16.dp)) },
                                singleLine = true,
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "কঠিনতার মাত্রা (Difficulty Level):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    "EASY" to ("সহজ"),
                                    "MEDIUM" to ("মাঝারি"),
                                    "HARD" to ("কঠিন")
                                ).forEach { (diffKey, diffLabel) ->
                                    val isSelected = editDifficulty.equals(diffKey, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { editDifficulty = diffKey },
                                        label = { Text(diffLabel, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = when(diffKey) {
                                                "EASY" -> CorrectGreen
                                                "HARD" -> WrongRed
                                                else -> ExamGold
                                            },
                                            selectedLabelColor = if (diffKey == "MEDIUM") Color(0xFF1E1B2E) else Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editExplanation,
                            onValueChange = { editExplanation = it },
                            label = { Text("প্রশ্নের ব্যাখ্যা ও সমাধান (ঐচ্ছিক)") },
                            placeholder = { Text("সঠিক উত্তরের কারণ বা সূত্র লিখুন...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { editingQuestion = null },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Text("বাতিল করুন", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val opts = listOf(editOpt0, editOpt1, editOpt2, editOpt3).filter { it.isNotBlank() }
                                    val safeOpts = if (opts.size >= 2) opts else listOf(editOpt0, editOpt1, editOpt2, editOpt3)
                                    val safeCorrectIdx = editCorrectIdx.coerceIn(0, (safeOpts.size - 1).coerceAtLeast(0))

                                    val updatedTags = mutableListOf<String>()
                                    if (editPyqYear.isNotBlank()) {
                                        updatedTags.add("PYQ_${editPyqYear.trim()}")
                                    }
                                    q.tags.filterNot { it.startsWith("PYQ_") }.forEach { updatedTags.add(it) }

                                    val updatedQ = q.copy(
                                        classId = editClassId,
                                        subjectId = editSubjectId,
                                        chapterId = editChapterId,
                                        topicId = editTopicId,
                                        question = editQText,
                                        options = safeOpts,
                                        correctOptionIndex = safeCorrectIdx,
                                        correctAnswerText = safeOpts.getOrNull(safeCorrectIdx) ?: "",
                                        difficulty = editDifficulty,
                                        marks = editMarks,
                                        tags = updatedTags,
                                        explanation = editExplanation
                                    )
                                    viewModel.updateFirestoreQuestion(updatedQ) { success, msg ->
                                        actionSuccess = success
                                        actionMessage = msg
                                        editingQuestion = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("💾 সংরক্ষণ করুন", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (deletingQuestion != null) {
        val q = deletingQuestion!!
        AlertDialog(
            onDismissRequest = { deletingQuestion = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WrongRed) },
            title = { Text("প্রশ্ন মুছে ফেলতে চান?") },
            text = {
                Text(
                    if (isBengali) "আপনি কি নিশ্চিত যে '${q.question}' প্রশ্নটি ফায়ারবেস ক্লাউড থেকে স্থায়ীভাবে মুছে ফেলতে চান?"
                    else "Are you sure you want to permanently delete question ID: ${q.id}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetId = q.id
                        deletingQuestion = null
                        viewModel.deleteFirestoreQuestion(targetId) { success, msg ->
                            actionSuccess = success
                            actionMessage = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed)
                ) {
                    Text("🗑️ মুছে ফেলুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingQuestion = null }) {
                    Text("বাতিল করুন")
                }
            }
        )
    }

    if (showJsonGuideDialog) {
        Dialog(
            onDismissRequest = { showJsonGuideDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.88f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "📖 JSON ফাইল স্ট্রাকচার গাইড",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "সঠিক নিয়মে JSON ফাইল তৈরি ও ডাউনলোডের নিয়মাবলী",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { showJsonGuideDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Question Bank Guide
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "১. প্রশ্ন ব্যাংক JSON ফরম্যাট:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Button(
                                        onClick = {
                                            downloadQuestionJsonLauncher.launch("desh_exam_sample_questions.json")
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ডাউনলোড", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(
                                    text = "প্রতিটি প্রশ্নের অবজেক্টে নিচের ফিল্ডগুলো থাকবে:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("• question : মূল প্রশ্ন (String - আবশ্যক)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("• options : ৪টি অপশনের লিস্ট (Array of Strings - আবশ্যক)", fontSize = 12.sp)
                                        Text("• correctOptionIndex : সঠিক উত্তরের ইনডেক্স (0=১ম, 1=২য়, 2=৩য়, 3=৪র্থ) (Number - আবশ্যক)", fontSize = 12.sp, color = CorrectGreen, fontWeight = FontWeight.SemiBold)
                                        Text("• explanation : সঠিক উত্তরের ব্যাখ্যা ও সমাধান (String - ঐচ্ছিক)", fontSize = 12.sp)
                                        Text("• difficulty : 'EASY', 'MEDIUM', অথবা 'HARD' (String - ঐচ্ছিক)", fontSize = 12.sp)
                                        Text("• marks : প্রশ্নের মান (যেমন: 1, 2, 5) (Number - ঐচ্ছিক)", fontSize = 12.sp)
                                        Text("• pyqYear : বিগত বছরের সাল যেমন '2024', '2025' (String - ঐচ্ছিক)", fontSize = 12.sp)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(sampleJsonTemplate))
                                            bulkFileStatusMessage = "✓ নমুনা প্রশ্ন JSON কপি করা হয়েছে!"
                                            bulkFileStatusSuccess = true
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("নমুনা কোড কপি করুন", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // 2. Syllabus / Curriculum Guide
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "২. সিলেবাস (অধ্যায় ও টপিক) JSON ফরম্যাট:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Button(
                                        onClick = {
                                            downloadSyllabusJsonLauncher.launch("desh_exam_sample_syllabus.json")
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ডাউনলোড", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(
                                    text = "অধ্যায় ও তার অন্তর্গত টপিকসমূহের অবজেক্ট স্ট্রাকচার:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("• chapterNumber : অধ্যায় নম্বর (Number - আবশ্যক)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("• title : অধ্যায়ের নাম (String - আবশ্যক)", fontSize = 12.sp)
                                        Text("• description : অধ্যায়ের সংক্ষিপ্ত বিবরণ (String - ঐচ্ছিক)", fontSize = 12.sp)
                                        Text("• topics : টপিক লিস্ট (Array of Topic Objects)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        Text("    - orderIndex : টপিকের ক্রম (Number)", fontSize = 11.sp)
                                        Text("    - title : টপিকের নাম (String)", fontSize = 11.sp)
                                        Text("    - summary : সারসংক্ষেপ (String)", fontSize = 11.sp)
                                        Text("    - keyPoints : মূল পয়েন্টসমূহ (Array of Strings)", fontSize = 11.sp)
                                        Text("    - formulae : সূত্রাবলী (Array of Strings)", fontSize = 11.sp)
                                        Text("    - contentData : বিস্তারিত পড়াশোনা কনটেন্ট (String)", fontSize = 11.sp)
                                        Text("    - videoUrl : ভিডিও লেকচার লিংক (String)", fontSize = 11.sp)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(sampleCurriculumJsonTemplate))
                                            bulkFileStatusMessage = "✓ নমুনা সিলেবাস JSON কপি করা হয়েছে!"
                                            bulkFileStatusSuccess = true
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("নমুনা কোড কপি করুন", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Button(
                        onClick = { showJsonGuideDialog = false },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ঠিক আছে (Close)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (deletingTestTemplate != null) {
        val test = deletingTestTemplate!!
        AlertDialog(
            onDismissRequest = { deletingTestTemplate = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WrongRed) },
            title = { Text("টেস্ট ডিলিট করতে চান?") },
            text = {
                Text(
                    if (isBengali) "আপনি কি নিশ্চিত যে '${test.title}' টেস্ট টেমপ্লেটটি ফায়ারবেস ক্লাউড থেকে স্থায়ীভাবে মুছে ফেলতে চান?"
                    else "Are you sure you want to delete test '${test.title}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetId = test.id
                        deletingTestTemplate = null
                        viewModel.deleteFirestoreTestTemplate(targetId) { success, msg ->
                            actionSuccess = success
                            actionMessage = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed)
                ) {
                    Text("🗑️ মুছে ফেলুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTestTemplate = null }) {
                    Text("বাতিল করুন")
                }
            }
        )
    }

    if (showEditAdminDialog) {
        val fbScope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { if (!isSavingAdminProfile) showEditAdminDialog = false },
            title = {
                Text(
                    text = if (isBengali) "🛡️ অ্যাডমিন প্রোফাইল এডিট করুন" else "🛡️ Edit Admin Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isBengali) "আপনার নতুন ডিসপ্লে নাম লিখুন:" else "Enter your new display name:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editAdminNameText,
                        onValueChange = { editAdminNameText = it },
                        placeholder = { Text("e.g. Super Admin") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (isSavingAdminProfile) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text(
                                text = if (isBengali) "সংরক্ষণ করা হচ্ছে..." else "Saving to Firebase...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editAdminNameText.isNotBlank()) {
                            isSavingAdminProfile = true
                            fbScope.launch {
                                val result = viewModel.authRepository.updateDisplayName(editAdminNameText)
                                isSavingAdminProfile = false
                                if (result is com.example.data.auth.AuthResult.Success) {
                                    showEditAdminDialog = false
                                    actionSuccess = true
                                    actionMessage = if (isBengali) "অ্যাডমিন ডিসপ্লে নাম সফলভাবে পরিবর্তিত হয়েছে!" else "Admin display name updated successfully!"
                                } else {
                                    actionSuccess = false
                                    actionMessage = if (isBengali) "পরিবর্তন করতে ব্যর্থ হয়েছে!" else "Failed to update profile name."
                                }
                            }
                        }
                    },
                    enabled = !isSavingAdminProfile && editAdminNameText.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isBengali) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditAdminDialog = false },
                    enabled = !isSavingAdminProfile
                ) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
        }
    }
}

@Composable
private fun AdminStatGradientBadge(
    count: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    borderColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.35f)),
        modifier = modifier
            .background(Brush.verticalGradient(gradientColors), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = count,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = accentColor,
                    maxLines = 1
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AdminFirestoreIndexManager(
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

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
            .fillMaxWidth()
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
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ ফায়ারস্টোর ১-ক্লিক ইনডেক্স ম্যানেজার",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "নিচের প্রতিটি ইনডেক্সে ক্লিক করলে ফায়ারবেস কনসোলে সরাসরি ১-ক্লিকে ইনডেক্স পেজ তৈরি হয়ে যাবে। পছন্দসই বাটন চাপুন!",
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
                            text = "🔥 1-Click Create Index in Firebase",
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
