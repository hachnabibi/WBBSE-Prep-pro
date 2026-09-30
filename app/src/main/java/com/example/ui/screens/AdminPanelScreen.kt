package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import com.example.data.model.AssessmentMode
import com.example.ui.theme.ExamGold
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel

private fun getAdminClassSubjects(classId: String): List<SubjectEntity> {
    return listOf(
        SubjectEntity("${classId}_main", "main", "সকল বিষয় (Curriculum)", "All Subjects", 5, 0, 0, 0xFF3B82F6L, classId, "common")
    )
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
    val isAdmin = currentUid == "juVeT3Z6WDcKhgO4a6kaU7kkbm43"

    if (!isAdmin) {
        Column(
            modifier = modifier
                .fillMaxSize()
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

    val firestoreQuestionsList by viewModel.firestoreQuestions.collectAsState()
    val firestoreTestTemplatesList by viewModel.firestoreTestTemplates.collectAsState()

    var cmsQuestionSearchQuery by remember { mutableStateOf("") }
    var editingQuestion by remember { mutableStateOf<com.example.data.firestore.FirestoreQuestion?>(null) }
    var deletingQuestion by remember { mutableStateOf<com.example.data.firestore.FirestoreQuestion?>(null) }
    var deletingTestTemplate by remember { mutableStateOf<com.example.data.firestore.FirestoreTestTemplate?>(null) }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Sync Seeder, 1 = Add Question, 2 = Question CMS, 3 = Custom Test, 4 = Structured, 5 = Test CMS, 6 = Bulk Import

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

    // Bulk Import state
    var bulkImportText by remember { mutableStateOf("") }
    var bulkCustomTestTitle by remember { mutableStateOf("") }
    var bulkPyqYearInput by remember { mutableStateOf("") }

    val sampleJsonTemplate = """[
  {
    "question": "সালোকসংশ্লেষ প্রক্রিয়ায় নির্গত গ্যাসের নাম কী?",
    "options": ["অক্সিজেন", "কার্বন ডাই অক্সাইড", "নাইট্রোজেন", "হাইড্রোজেন"],
    "correctOptionIndex": 0,
    "explanation": "সালোকসংশ্লেষ প্রক্রিয়ায় উপজাত হিসেবে অক্সিজেন নির্গত হয়।"
  },
  {
    "question": "মানবদেহের দীর্ঘতম স্নায়ু কোনটি?",
    "options": ["ভেগাস", "অপটিক", "সায়াটিক", "অডিটরি"],
    "correctOptionIndex": 2,
    "explanation": "সায়াটিক নার্ভ হলো মানবদেহের সবচেয়ে দীর্ঘতম স্নায়ু।"
  }
]"""

    val sampleCsvTemplate = """উদ্ভিদের জল সংবহনকারী কলা কোনটি? | জাইলেম | ফ্লোয়েম | ক্যামবিয়াম | কর্টেক্স | 0 | জাইলেম কলার মাধ্যমে জল সংবহন হয়।
কোষের শক্তিঘর বলা হয় কাকে? | গলগি বডি | মাইটোকন্ড্রিয়া | নিউক্লিয়াস | রাইবোজোম | 1 | মাইটোকন্ড্রিয়ায় ATP উৎপন্ন হয়।"""

    // 1. Academic Classes (Class 5 to 12)
    val allClasses = remember { AcademicClassProvider.ALL_CLASSES }
    var selectedClass by remember {
        mutableStateOf(allClasses.firstOrNull { it.id == "class_10_wbbse" } ?: allClasses.first())
    }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    // Pre-existing curriculum data
    val allChapters by viewModel.chapters.collectAsState()
    val allTopics by viewModel.topics.collectAsState()

    val classSubjects = remember(selectedClass) {
        getAdminClassSubjects(selectedClass.id)
    }

    var selectedSubject by remember(selectedClass) {
        mutableStateOf(classSubjects.first())
    }
    
    LaunchedEffect(selectedSubject) {
        viewModel.selectSubject(selectedSubject.id)
    }

    val filteredChapters = remember(selectedSubject, selectedClass, allChapters) {
        val chs = allChapters.filter { it.subjectId == selectedSubject.id }
        if (chs.isNotEmpty()) chs else listOf(
            ChapterEntity(
                id = "${selectedClass.id}_${selectedSubject.id}_ch1",
                subjectId = selectedSubject.id,
                chapterNumber = 1,
                title = "সাধারণ অধ্যায় (Chapter 1)",
                description = "",
                totalTopics = 1,
                completedTopics = 0,
                bossUnlocked = true,
                bossCompleted = false,
                masteryPercentage = 0
            ),
            ChapterEntity(
                id = "${selectedClass.id}_${selectedSubject.id}_ch2",
                subjectId = selectedSubject.id,
                chapterNumber = 2,
                title = "গুরুত্বপূর্ণ অধ্যায় (Chapter 2)",
                description = "",
                totalTopics = 1,
                completedTopics = 0,
                bossUnlocked = true,
                bossCompleted = false,
                masteryPercentage = 0
            )
        )
    }

    var selectedChapter by remember(selectedSubject, selectedClass) {
        mutableStateOf(filteredChapters.first())
    }

    var selectedStructuredChapter by remember(filteredChapters) {
        mutableStateOf(filteredChapters.firstOrNull())
    }

    val filteredTopics = remember(selectedChapter, allTopics) {
        val tps = allTopics.filter { it.chapterId == selectedChapter.id }
        if (tps.isNotEmpty()) tps else listOf(
            TopicEntity(
                id = "${selectedChapter.id}_top1",
                chapterId = selectedChapter.id,
                subjectId = selectedSubject.id,
                orderIndex = 1,
                title = "সাধারণ টপিক ও প্রশ্নাবলী (টপিক ১)",
                summary = "",
                keyPointsJson = "",
                isUnlocked = true,
                masteryPercentage = 0,
                questionCount = 10
            ),
            TopicEntity(
                id = "${selectedChapter.id}_top2",
                chapterId = selectedChapter.id,
                subjectId = selectedSubject.id,
                orderIndex = 2,
                title = "পরীক্ষা উপযোগী নমুনা প্রশ্ন (টপিক ২)",
                summary = "",
                keyPointsJson = "",
                isUnlocked = true,
                masteryPercentage = 0,
                questionCount = 10
            )
        )
    }

    var selectedTopic by remember(selectedChapter) {
        mutableStateOf(filteredTopics.first())
    }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }
    var topicDropdownExpanded by remember { mutableStateOf(false) }
    var pyqDropdownExpanded by remember { mutableStateOf(false) }

    var cmsClassDropdownExpanded by remember { mutableStateOf(false) }
    var cmsSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var testCmsClassDropdownExpanded by remember { mutableStateOf(false) }
    var testCmsSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var bulkClassDropdownExpanded by remember { mutableStateOf(false) }
    var bulkSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var bulkChapterDropdownExpanded by remember { mutableStateOf(false) }
    var bulkTopicDropdownExpanded by remember { mutableStateOf(false) }
    var customClassDropdownExpanded by remember { mutableStateOf(false) }
    var customSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var structuredClassDropdownExpanded by remember { mutableStateOf(false) }
    var structuredSubjectDropdownExpanded by remember { mutableStateOf(false) }

    // Question Content State
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

    val defaultGeneratedId by remember(selectedSubject, selectedChapter, pyqYear) {
        derivedStateOf {
            val cleanSub = selectedSubject.id.replace("subject_", "").replace("-", "_").take(8)
            val cleanCh = selectedChapter.id.replace("chapter_", "").replace("-", "_").take(8)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Selector
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 12.dp
        ) {
            val tabs = listOf(
                "🚀 সিঙ্ক" to "🚀 Sync",
                "✍️ প্রশ্ন যোগ" to "✍️ Add Question",
                "📋 প্রশ্ন CMS" to "📋 Question CMS",
                "📝 কাস্টম টেস্ট" to "📝 Custom Test",
                "🎯 স্ট্রাকচার্ড" to "🎯 Structured",
                "📑 টেস্ট CMS" to "📑 Test CMS",
                "📦 বাল্ক ইমপোর্ট" to "📦 Bulk Import"
            )
            tabs.forEachIndexed { index, (bengaliTitle, englishTitle) ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        selectedTab = index
                        if (index == 2) viewModel.fetchFirestoreQuestions()
                        if (index == 5) viewModel.fetchFirestoreTestTemplates()
                    },
                    text = {
                        Text(
                            text = if (isBengali) bengaliTitle else englishTitle,
                            maxLines = 1,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

            // Status message banner
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
                // Tab 0: Realtime Auto-Sync & Seeder Card
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Live Auto-Sync Status Card
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, if (autoSyncStatus.isCloudConnected) CorrectGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (autoSyncStatus.isCloudConnected) CorrectGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Sync,
                                                    contentDescription = null,
                                                    tint = if (autoSyncStatus.isCloudConnected) CorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "রিয়েল-টাইম অটো-সিঙ্ক (Auto-Sync)",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "🟢 ব্যাকগ্রাউন্ডে স্বয়ংক্রিয়ভাবে সিঙ্ক হচ্ছে",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CorrectGreen,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = autoSyncStatus.syncMessage,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val timeStr = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault()).format(java.util.Date(autoSyncStatus.lastSyncedTimeMillis))
                                        Text(
                                            text = "সর্বশেষ সিঙ্ক: $timeStr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.triggerManualSync { success, msg ->
                                            actionSuccess = success
                                            actionMessage = msg
                                        }
                                    },
                                    enabled = !autoSyncStatus.isSyncing && !isLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    if (autoSyncStatus.isSyncing) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("সিঙ্ক হচ্ছে...", color = Color.White)
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "🔄 এখনই সম্পূর্ণ ক্লাউড ডেটা সিঙ্ক করুন",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Bulk Seeder Card
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ExamGold.copy(alpha = 0.2f),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ExamGold)
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "ফায়ারবেস সিলেবাস সিডার",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "৫ম থেকে ১২দশ শ্রেণি ও বিগত বছরের প্রশ্নসহ সিলেবাস ক্লাউডে পাঠান",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                                Text(
                                    text = "👉 এটি এক ক্লিকে ৫ম থেকে ১২দশ শ্রেণি পর্যন্ত সমস্ত সিলেবাস, বিষয় ও প্রশ্নাবলী ফায়ারবেস ক্লাউডে আপলোড ও সিঙ্ক করে দেয়।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = {
                                        viewModel.syncCurriculumToFirestore { success, msg ->
                                            actionSuccess = success
                                            actionMessage = msg
                                        }
                                    },
                                    enabled = !isLoading && !autoSyncStatus.isSyncing,
                                    colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("সিঙ্ক হচ্ছে...", color = Color.White)
                                    } else {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF1E1B2E))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "🚀 সম্পূর্ণ সিলেবাস ক্লাউডে সিঙ্ক করুন",
                                            color = Color(0xFF1E1B2E),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // Tab 1: Choice-based Question Entry (With Class Selector: Class 5 to 12)
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
                                    text = "প্রথমে শ্রেণি নির্বাচন করুন, তারপর বিষয়, অধ্যায় ও অপশন বেছে নিয়ে প্রশ্ন যোগ করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 1. Class Selector
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "১. শ্রেণি নির্বাচন করুন:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Quick horizontal Class Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allClasses) { cls ->
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
                                    label = { Text("শ্রেণি ও বোর্ড") },
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
                                                classDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Pre-existing Subject Selector (Filtered for selected Class)
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
                                label = { Text("২. বিষয় নির্বাচন করুন") },
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
                                            val chs = allChapters.filter { it.subjectId == subject.id }
                                            selectedChapter = chs.firstOrNull() ?: filteredChapters.first()
                                            val tps = allTopics.filter { it.chapterId == selectedChapter.id }
                                            selectedTopic = tps.firstOrNull() ?: filteredTopics.first()
                                            subjectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Pre-existing Chapter Selector
                    item {
                        ExposedDropdownMenuBox(
                            expanded = chapterDropdownExpanded,
                            onExpandedChange = { chapterDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = "অধ্যায় ${selectedChapter.chapterNumber}: ${selectedChapter.title}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("৩. অধ্যায় নির্বাচন করুন") },
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
                                filteredChapters.forEach { chapter ->
                                    DropdownMenuItem(
                                        text = { Text("অধ্যায় ${chapter.chapterNumber}: ${chapter.title}") },
                                        onClick = {
                                            selectedChapter = chapter
                                            val tps = allTopics.filter { it.chapterId == chapter.id }
                                            selectedTopic = tps.firstOrNull() ?: filteredTopics.first()
                                            chapterDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Pre-existing Topic Selector
                    item {
                        ExposedDropdownMenuBox(
                            expanded = topicDropdownExpanded,
                            onExpandedChange = { topicDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedTopic.title,
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

                    // 5. Pre-existing PYQ Year Selector (Dropdown + Quick Chips)
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

                            // Quick selection chips for PYQ
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

                    // 6. Custom / Auto Question ID
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
                                label = { Text("কাস্টম আইডি (ঐচ্ছিক - ফাঁকা রাখলে স্বয়ংক্রিয় তৈরি হবে)") },
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
                                    "💡 আপনি চাইলে নিজের পছন্দমতো আইডি লিখতে পারেন অথবা ঘরটি ফাঁকা রাখলে স্বয়ংক্রিয়ভাবে ক্লিন আইডি বসে যাবে।"
                                else
                                    "💡 You can enter your own ID (e.g. q_ben_ch1_01) or leave empty for clean auto-structured ID.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 7. Question Text
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

                    // 8. Options (A, B, C, D)
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

                    // 9. Pre-existing Correct Index Selector (Direct Buttons - NO Text Input!)
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
                                optionTags.forEachIndexed { index, letter ->
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

                            // Show preview of selected correct answer
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

                    // 10. Difficulty Level Selector
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

                    // 11. Explanation
                    item {
                        OutlinedTextField(
                            value = explanationText,
                            onValueChange = { explanationText = it },
                            label = { Text("১১. প্রশ্নের ব্যাখ্যা বা সমাধান (ঐচ্ছিক)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Submit Button
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                val opts = listOf(opt1, opt2, opt3, opt4).filter { it.isNotBlank() }
                                val resolvedCorrectAnswer = opts.getOrNull(selectedCorrectIndex) ?: opt1
                                viewModel.addQuestionToFirestore(
                                    classId = selectedClass.id,
                                    subjectId = selectedSubject.id,
                                    chapterId = selectedChapter.id,
                                    topicId = selectedTopic.id,
                                    questionText = questionText,
                                    optionsList = opts,
                                    correctIndex = selectedCorrectIndex,
                                    correctAnswerText = resolvedCorrectAnswer,
                                    explanationText = explanationText.ifBlank { "সঠিক উত্তর হলো $resolvedCorrectAnswer।" },
                                    difficulty = difficulty,
                                    marks = 1,
                                    pyqYear = pyqYear,
                                    customQuestionId = customQuestionId.ifBlank { null }
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
                                    }
                                }
                            },
                            enabled = !isLoading && questionText.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank(),
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
                // Tab 2: Question CMS Manager (Full Add, Edit, Delete, Filter)
                val allSubjectOption = remember(selectedClass.id) {
                    SubjectEntity(
                        id = "ALL",
                        code = "ALL",
                        name = "সকল বিষয়",
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
                                            label = { Text("বিষয়") },
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
                                    label = { Text("🔍 প্রশ্ন অথবা আইডি দিয়ে খুঁজুন...") },
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
                                        text = if (isBengali) "মোট ${firestoreQuestionsList.size} টি প্রশ্ন ক্লাউডে সংরক্ষিত আছে (ফিল্টার করা: ${filteredQuestions.size} টি)। যেকোনো প্রশ্ন সরাসরি সম্পাদনা বা মুছে ফেলতে পারেন।"
                                        else "Total ${firestoreQuestionsList.size} questions in Firebase (${filteredQuestions.size} matching filter). Edit or delete directly.",
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
                                        text = "নিচের বাটনে চাপ দিলে সমস্ত বিষয়ের প্রশ্ন ক্লাউডে আপলোড হয়ে যাবে।",
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
                                    // Row 1: ID Badge & Difficulty Chip
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

                                    // Row 2: Subject, Class & PYQ Badges
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

                                    // Action Buttons: Symmetrical & Balanced equal widths
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
                // Tab 3: Custom Test & Question Builder
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

                    // Test Configuration Card
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
                                        label = { Text("সময় (মিনিট)") },
                                        modifier = Modifier.width(110.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Class & Subject selector for Test
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            // Interactive Class Dropdown
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

                            // Interactive Subject Dropdown
                            ExposedDropdownMenuBox(
                                expanded = customSubjectDropdownExpanded,
                                onExpandedChange = { customSubjectDropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedSubject.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("বিষয়") },
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
                                                selectedChapter = chs.firstOrNull() ?: filteredChapters.first()
                                                val tps = allTopics.filter { it.chapterId == selectedChapter.id }
                                                selectedTopic = tps.firstOrNull() ?: filteredTopics.first()
                                                customSubjectDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Question fields for Custom Test
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

                    // Correct Option selector
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

                    // Explanation & Submit
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
                // Tab 4: Structured Test Creator (Chapter Assessment, Subject Mastery Quiz, Full Syllabus Mock)
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
                                    text = "নির্দিষ্ট অধ্যায় বা বিষয় নির্বাচন করে অধ্যায় পরীক্ষা, বিষয়ভিত্তিক বিশেষ কুইজ ও সম্পূর্ণ সিলেবাস মক টেস্ট তৈরি করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Test Type Selector
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "টেস্টের ধরন নির্বাচন:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val structuredTypes = listOf(
                                "CHAPTER_ASSESSMENT" to ("অধ্যায় পরীক্ষা"),
                                "SUBJECT_MASTERY_QUIZ" to ("বিষয়ভিত্তিক বিশেষ কুইজ"),
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

                    // Class & Subject Selector
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            // Interactive Class Dropdown
                            ExposedDropdownMenuBox(
                                expanded = structuredClassDropdownExpanded,
                                onExpandedChange = { structuredClassDropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedClass.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("শ্রেণি") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = structuredClassDropdownExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = structuredClassDropdownExpanded,
                                    onDismissRequest = { structuredClassDropdownExpanded = false }
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
                                                structuredClassDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Interactive Subject Dropdown
                            ExposedDropdownMenuBox(
                                expanded = structuredSubjectDropdownExpanded,
                                onExpandedChange = { structuredSubjectDropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedSubject.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("বিষয়") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = structuredSubjectDropdownExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
                                                selectedChapter = chs.firstOrNull() ?: filteredChapters.first()
                                                val tps = allTopics.filter { it.chapterId == selectedChapter.id }
                                                selectedTopic = tps.firstOrNull() ?: filteredTopics.first()
                                                structuredSubjectDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Chapter Selection (if CHAPTER_ASSESSMENT or SUBJECT_MASTERY_QUIZ)
                    if (structuredTestType != "FULL_SYLLABUS_MOCK") {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "নির্দিষ্ট অধ্যায় নির্বাচন করুন:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(filteredChapters) { ch ->
                                        val isSelected = selectedStructuredChapter?.id == ch.id
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) CorrectGreen else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { selectedStructuredChapter = ch }
                                        ) {
                                            Text(
                                                text = ch.title,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Test Meta Fields
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = structuredTestTitle,
                                onValueChange = { structuredTestTitle = it },
                                label = { Text("টেস্টের শিরোনাম") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = structuredTestDuration,
                                onValueChange = { structuredTestDuration = it },
                                label = { Text("সময় (মিনিট)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = structuredTestMarks,
                                onValueChange = { structuredTestMarks = it },
                                label = { Text("মোট নম্বর") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Question fields
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "প্রশ্ন যোগ করুন:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            OutlinedTextField(
                                value = structuredQuestionText,
                                onValueChange = { structuredQuestionText = it },
                                label = { Text("প্রশ্নের বিবরণ") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = structuredOpt1,
                                onValueChange = { structuredOpt1 = it },
                                label = { Text("অপশন ক") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = structuredOpt2,
                                onValueChange = { structuredOpt2 = it },
                                label = { Text("অপশন খ") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = structuredOpt3,
                                onValueChange = { structuredOpt3 = it },
                                label = { Text("অপশন গ") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = structuredOpt4,
                                onValueChange = { structuredOpt4 = it },
                                label = { Text("অপশন ঘ") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = structuredExplanation,
                                onValueChange = { structuredExplanation = it },
                                label = { Text("ব্যাখ্যা ও সমাধান") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Submit Button
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val opts = listOf(structuredOpt1, structuredOpt2, structuredOpt3, structuredOpt4).filter { it.isNotBlank() }
                                val correctAns = opts.getOrNull(structuredCorrectIndex) ?: structuredOpt1
                                viewModel.createStructuredAssessment(
                                    testType = structuredTestType,
                                    classId = selectedClass.id,
                                    subjectId = selectedSubject.id,
                                    chapterId = selectedStructuredChapter?.id ?: "${selectedSubject.id}_ch1",
                                    title = structuredTestTitle,
                                    description = "Structured test linked to ${selectedStructuredChapter?.title ?: selectedSubject.name}",
                                    durationMinutes = structuredTestDuration.toIntOrNull() ?: 25,
                                    totalMarks = structuredTestMarks.toIntOrNull() ?: 25,
                                    questionText = structuredQuestionText,
                                    options = opts,
                                    correctIndex = structuredCorrectIndex,
                                    correctAnswerText = correctAns,
                                    explanation = structuredExplanation.ifBlank { "সঠিক উত্তর।" }
                                ) { success, msg ->
                                    actionSuccess = success
                                    actionMessage = msg
                                    if (success) {
                                        structuredQuestionText = ""
                                        structuredOpt1 = ""
                                        structuredOpt2 = ""
                                        structuredOpt3 = ""
                                        structuredOpt4 = ""
                                        structuredExplanation = ""
                                    }
                                }
                            },
                            enabled = !isLoading && structuredTestTitle.isNotBlank() && structuredQuestionText.isNotBlank() && structuredOpt1.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = ExamGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF1E1B2E))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("সংরক্ষণ হচ্ছে...", color = Color(0xFF1E1B2E))
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF1E1B2E))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "🚀 টেস্ট তৈরি ও সংরক্ষণ করুন",
                                    color = Color(0xFF1E1B2E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            } else if (selectedTab == 5) {
                // Tab 5: Test Template CMS Manager (View & Delete Tests)
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
                                        Icon(Icons.Default.Quiz, contentDescription = null, tint = ExamGold)
                                        Text(
                                            text = "📑 ফায়ারবেস টেস্ট টেমপ্লেট ব্যবস্থাপনা",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.fetchFirestoreTestTemplates(selectedClass.id, selectedSubject.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ExamGold)
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
                                            label = { Text("বিষয়") },
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
                                    color = ExamGold.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isBengali) "মোট ${firestoreTestTemplatesList.size} টি টেস্ট টেমপ্লেট ফায়ারবেসে সংরক্ষিত আছে।"
                                        else "Total ${firestoreTestTemplatesList.size} test templates available in Firebase.",
                                        style = MaterialTheme.typography.bodySmall,
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
                                CircularProgressIndicator(color = ExamGold)
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
                                        text = "কোনো টেস্ট টেমপ্লেট পাওয়া যায়নি",
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
                                        Text(
                                            text = test.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ExamGold.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = test.testType,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = ExamGold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = "⏱️ ${test.durationMinutes} মিনিট",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "🎯 ${test.totalMarks} নম্বর",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "❓ ${test.questions.size} টি প্রশ্ন",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "ID: ${test.id}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )

                                    Button(
                                        onClick = { deletingTestTemplate = test },
                                        colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("🗑️ টেস্ট ডিলিট করুন", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 6) {
                // Tab 6: Bulk Test / Question Import
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
                                    text = "জেসন (JSON) অ্যারে অথবা পাইপ লাইন (|) ফরম্যাটে একসাথে একাধিক প্রশ্ন পেস্ট করে সরাসরি ফায়ারবেস ক্লাউড ও ডেটাবেজে সংরক্ষণ করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Target Class, Subject, Chapter & Topic Selector
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
                                    text = "টার্গেট শ্রেণি, বিষয়, অধ্যায় ও টপিক নির্বাচন:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    // Row 1: Class & Subject in single row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // 1. Class Dropdown
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
                                                            val chs = allChapters.filter { it.subjectId == selectedSubject.id }
                                                            selectedChapter = chs.firstOrNull() ?: filteredChapters.first()
                                                            val tps = allTopics.filter { it.chapterId == selectedChapter.id }
                                                            selectedTopic = tps.firstOrNull() ?: filteredTopics.first()
                                                            bulkClassDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // 2. Subject Dropdown
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
                                                            selectedChapter = chs.firstOrNull() ?: filteredChapters.first()
                                                            val tps = allTopics.filter { it.chapterId == selectedChapter.id }
                                                            selectedTopic = tps.firstOrNull() ?: filteredTopics.first()
                                                            bulkSubjectDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Row 2: Chapter & Topic in single row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // 3. Chapter Dropdown / Filter
                                        ExposedDropdownMenuBox(
                                            expanded = bulkChapterDropdownExpanded,
                                            onExpandedChange = { bulkChapterDropdownExpanded = it },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = selectedChapter.title,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("৩. অধ্যায়", maxLines = 1) },
                                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkChapterDropdownExpanded) },
                                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            ExposedDropdownMenu(
                                                expanded = bulkChapterDropdownExpanded,
                                                onDismissRequest = { bulkChapterDropdownExpanded = false }
                                            ) {
                                                filteredChapters.forEach { chapter ->
                                                    DropdownMenuItem(
                                                        text = { Text(chapter.title) },
                                                        onClick = {
                                                            selectedChapter = chapter
                                                            val tps = allTopics.filter { it.chapterId == chapter.id }
                                                            selectedTopic = tps.firstOrNull() ?: filteredTopics.first()
                                                            bulkChapterDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // 4. Topic Dropdown / Filter
                                        ExposedDropdownMenuBox(
                                            expanded = bulkTopicDropdownExpanded,
                                            onExpandedChange = { bulkTopicDropdownExpanded = it },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = selectedTopic.title,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("৪. টপিক", maxLines = 1) },
                                                leadingIcon = { Icon(Icons.Default.Topic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkTopicDropdownExpanded) },
                                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            ExposedDropdownMenu(
                                                expanded = bulkTopicDropdownExpanded,
                                                onDismissRequest = { bulkTopicDropdownExpanded = false }
                                            ) {
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
                    }

                    // Template Helper Buttons (High contrast for light & dark theme)
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { bulkImportText = sampleJsonTemplate },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "📄 নমুনা ফরম্যাট ১ (JSON)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            OutlinedButton(
                                onClick = { bulkImportText = sampleCsvTemplate },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                    contentColor = MaterialTheme.colorScheme.secondary
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "📋 নমুনা ফরম্যাট ২ (Pipe)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            if (bulkImportText.isNotBlank()) {
                                IconButton(
                                    onClick = { bulkImportText = "" },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = WrongRed)
                                }
                            }
                        }
                    }

                    // Optional Custom Test Title & PYQ Tag (Single clean Row)
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
                                modifier = Modifier.weight(1.15f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = bulkPyqYearInput,
                                onValueChange = { bulkPyqYearInput = it },
                                label = { Text("বিগত সাল (ঐচ্ছিক)", maxLines = 1) },
                                placeholder = { Text("2025") },
                                leadingIcon = { Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                                singleLine = true,
                                modifier = Modifier.weight(0.85f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Raw Content Input Textarea
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
                                            "এখানে নমুনা ফরম্যাট অনুযায়ী প্রশ্ন পেস্ট করুন (যেমন: প্রশ্ন | অপশন১ | অপশন২ | অপশন৩ | অপশন৪ | সঠিক_সূচক(০-৩) | ব্যাখ্যা)..."
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

                    // Realtime Parser Preview
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
                                             if (isBengali) "✓ সঠিকভাবে শনাক্ত করা হয়েছে: $lineCount টি প্রশ্ন"
                                             else "✓ Valid format detected: $lineCount questions (${if (isJson) "JSON" else "Pipe-Separated"})"
                                        } else {
                                            if (isBengali) "⚠️ কোনো সঠিক প্রশ্ন পাওয়া যায়নি। নমুনা দেখুন।"
                                            else "⚠️ No valid questions parsed. Check template."
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (lineCount > 0) CorrectGreen else WrongRed
                                    )
                                }
                            }
                        }
                    }

                    // Bulk Upload Submit Button
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.importBulkQuestions(
                                    rawInput = bulkImportText,
                                    classId = selectedClass.id,
                                    subjectId = selectedSubject.id,
                                    chapterId = selectedChapter.id,
                                    topicId = selectedTopic.id,
                                    pyqYear = bulkPyqYearInput,
                                    customTestTitle = bulkCustomTestTitle
                                ) { success, msg, count ->
                                    actionSuccess = success
                                    actionMessage = msg
                                    if (success) {
                                        bulkImportText = ""
                                    }
                                }
                            },
                            enabled = !isLoading && bulkImportText.isNotBlank(),
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
            }
        }

    // --- CMS Dialogs ---
    // 1. Full-Featured Edit Question Modal Dialog
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

        val editSubjects = remember(editClassId) { getAdminClassSubjects(editClassId) }
        val editFilteredChapters = remember(editSubjectId, allChapters) {
            val list = allChapters.filter { it.subjectId == editSubjectId }
            if (list.isEmpty()) allChapters else list
        }
        val editFilteredTopics = remember(editChapterId, allTopics) {
            val list = allTopics.filter { it.chapterId == editChapterId }
            if (list.isEmpty()) allTopics else list
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
                    // Header Bar
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

                    // Scrollable Form Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Single Horizontal Scrollable Row for Class, Subject, Chapter & Topic
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "শ্রেণি, বিষয়, অধ্যায় ও টপিক (এক সারিতে নির্বাচন করুন):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ExamGold)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Class Selector Dropdown Chip
                                Box {
                                    val currClass = allClasses.firstOrNull { it.id == editClassId }
                                    val clsLabel = if (currClass != null) (currClass.name) else editClassId
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

                                // Subject Selector Dropdown Chip
                                Box {
                                    val currSub = editSubjects.firstOrNull { it.id == editSubjectId }
                                    val subLabel = if (currSub != null) (currSub.name) else editSubjectId.replace("subject_", "").replace("sub_", "").replace("_", " ")
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

                                // Chapter Selector Dropdown Chip
                                Box {
                                    val currCh = editFilteredChapters.firstOrNull { it.id == editChapterId }
                                    val chLabel = currCh?.title ?: if (editChapterId.isNotBlank()) editChapterId else ("অধ্যায় নির্বাচন")
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

                                // Topic Selector Dropdown Chip
                                Box {
                                    val currTp = editFilteredTopics.firstOrNull { it.id == editTopicId }
                                    val tpLabel = currTp?.title ?: if (editTopicId.isNotBlank()) editTopicId else ("টপিক নির্বাচন")
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

                        // 2. Question Text Field
                        OutlinedTextField(
                            value = editQText,
                            onValueChange = { editQText = it },
                            label = { Text("প্রশ্নের মূল বিবরণ:") },
                            placeholder = { Text("এখানে প্রশ্নটি লিখুন...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // 3. Options with Integrated Correct Answer Radio/Click Selector
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
                                        // Radio / Check Selector Box
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

                                        // Text Input for this Option
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

                        // 4. PYQ Year & Marks in Single Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // PYQ Year Field
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

                            // Marks Field
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

                        // 5. Difficulty Level Selector
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "কঠিনতার মাত্রা (Difficulty Level):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    "EASY" to ("সহজ (Easy)"),
                                    "MEDIUM" to ("মাঝারি (Medium)"),
                                    "HARD" to ("কঠিন (Hard)")
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

                        // 6. Explanation Field
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

                    // Bottom Action Bar: Side-by-Side Symmetrical Buttons
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

    // 2. Delete Question Confirmation Dialog
    if (deletingQuestion != null) {
        val q = deletingQuestion!!
        AlertDialog(
            onDismissRequest = { deletingQuestion = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WrongRed) },
            title = { Text("প্রশ্ন মুছে ফেলতে চান?") },
            text = {
                Text(
                    if (isBengali) "আপনি কি নিশ্চিত যে '${q.question}' প্রশ্নটি ফায়ারবেস ক্লাউড থেকে স্থায়ীভাবে মুছে ফেলতে চান?"
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

    // 3. Delete Test Template Confirmation Dialog
    if (deletingTestTemplate != null) {
        val test = deletingTestTemplate!!
        AlertDialog(
            onDismissRequest = { deletingTestTemplate = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WrongRed) },
            title = { Text("টেস্ট ডিলিট করতে চান?") },
            text = {
                Text(
                    if (isBengali) "আপনি কি নিশ্চিত যে '${test.title}' টেস্ট টেমপ্লেটটি ফায়ারবেস ক্লাউড থেকে স্থায়ীভাবে মুছে ফেলতে চান?"
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
}
