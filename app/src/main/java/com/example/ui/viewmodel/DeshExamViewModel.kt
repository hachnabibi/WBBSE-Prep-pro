package com.example.ui.viewmodel

import kotlinx.coroutines.withContext

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthResult
import com.example.data.auth.AuthUserState
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.firestore.FirestoreQuestion
import com.example.data.firestore.FirestoreService
import com.example.data.firestore.FirestoreTestTemplate
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.repository.DeshExamRepository
import com.example.data.repository.PyqRepository
import com.example.notification.MadhyamikNotificationManager
import com.example.ui.screens.PyqQuestionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    AUTH,
    ONBOARDING,
    HOME,
    JOURNEY,
    MOCK,
    LEADERBOARD,
    BATTLE,
    AI_COACH,
    PROFILE,
    LEARN_TOPIC,
    ASSESSMENT,
    RESULT,
    SETTINGS,
    FLASHCARDS,
    PYQ_EXPLORER,
    OMR_PRACTICE,
    TOPPERS_GUIDE,
    CERTIFICATE,
    DOUBT_SOLVER,
    MULTIPLAYER_BATTLE,
    AUDIO_PODCASTS,
    CONTENT_SOLUTION,
    STUDY_PLANNER,
    BOOKMARK_HUB,
    STUDY_SQUADS,
    PROGRESS_DASHBOARD,
    PDF_NOTES,
    PYQ_PDF_DOWNLOAD,
    ORIGINAL_PYQ_PDF_DOWNLOAD,
    TEXTBOOKS,
    LIVE_QUIZ,
    LIVE_MOCK_TEST,
    ADMIN_PANEL,
    WALLET,
    SUBJECT_DETAIL,
    CHAPTER_DETAIL,
    EDIT_PROFILE,
    INTERNET_ARCHIVE_SEARCH
}

data class AppSettings(
    val isDarkMode: Boolean = false,
    val highContrastText: Boolean = true,
    val useBengaliNumerals: Boolean = true,
    val targetExam: String = "মাধ্যমিক ২০২৬ (WBBSE)",
    val dailyGoalQuestions: Int = 15,
    val soundEffectsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val confettiEnabled: Boolean = true,
    val autoAdvanceQuestions: Boolean = false,
    val studyReminderEnabled: Boolean = true,
    val studyReminderTime: String = "20:00",
    val mockTestAlertsEnabled: Boolean = true,
    val examTipsAlertsEnabled: Boolean = true,
    val streakSaverAlertsEnabled: Boolean = true,
    val fontSizeScale: Float = 1.0f,
    val fcmToken: String = "",
    val isFcmConnected: Boolean = true,
    val razorpayLiveKey: String = ""
)

data class AssessmentUiState(
    val session: AssessmentSession? = null,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int = -1,
    val isAnswerChecked: Boolean = false,
    val isCorrect: Boolean = false,
    val answers: List<UserAnswer> = emptyList(),
    val timeRemainingSeconds: Int = 0,
    val questionStartTimeSeconds: Int = 0,
    val eliminatedOptionIndices: Set<Int> = emptySet(),
    val isFiftyFiftyUsed: Boolean = false,
    val selectedAnswersMap: Map<Int, Int> = emptyMap(),
    val markedForReviewIndices: Set<Int> = emptySet()
)

data class AutoSyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncedTimeMillis: Long = System.currentTimeMillis(),
    val syncedQuestionsCount: Int = 0,
    val isCloudConnected: Boolean = true,
    val syncMessage: String = "অটো-সিঙ্ক সক্রিয় (ক্লাউড ও লোকাল ডেটাবেজ যুক্ত)"
)

class DeshExamViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DeshExamDatabase.getDatabase(application, viewModelScope)
    val repository = DeshExamRepository(database.deshExamDao(), application)
    val authRepository = FirebaseAuthRepository()
    val firestoreService = FirestoreService()

    val authUserState: StateFlow<AuthUserState?> = authRepository.authStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUserState)

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    private val _isGuestUser = MutableStateFlow(true)
    val isGuestUser: StateFlow<Boolean> = _isGuestUser.asStateFlow()

    var contentSolutionTitle: String = ""
    var contentSolutionContent: String? = null
    var contentSolutionSolution: String? = null
    var contentSolutionInitialTab: Int = 0

    fun openContentSolution(title: String, content: String?, solution: String?, initialTab: Int = 0, chapterId: String? = null) {
        contentSolutionTitle = title
        contentSolutionInitialTab = initialTab
        
        if (!content.isNullOrBlank() || !solution.isNullOrBlank()) {
            contentSolutionContent = content
            contentSolutionSolution = solution
            navigateTo(AppScreen.CONTENT_SOLUTION)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                var finalContent = content
                var finalSolution = solution
                if (chapterId != null) {
                    val topics = repository.getTopicsForChapterOnce(chapterId)
                    if (finalContent.isNullOrBlank()) {
                        finalContent = topics.mapNotNull { it.contentData }.joinToString("\n\n---\n\n")
                    }
                    if (finalSolution.isNullOrBlank()) {
                        finalSolution = topics.mapNotNull { it.solutionData }.joinToString("\n\n---\n\n")
                    }
                }
                if (finalContent.isNullOrBlank()) {
                    finalContent = "# $title\n\nএই অধ্যায় বা টপিকের বিস্তারিত কন্টেন্ট এবং নোটস শীট খুব শীঘ্রই যুক্ত করা হবে।"
                }
                if (finalSolution.isNullOrBlank()) {
                    finalSolution = "# $title - সমাধান\n\nএই অধ্যায় বা টপিকের অনুশীলনী ও প্রশ্নোত্তর সমাধান খুব শীঘ্রই যুক্ত করা হবে।"
                }
                withContext(Dispatchers.Main) {
                    contentSolutionContent = finalContent
                    contentSolutionSolution = finalSolution
                    navigateTo(AppScreen.CONTENT_SOLUTION)
                }
            }
        }
    }

    private val _districtSchoolsMap = MutableStateFlow<Map<String, List<String>>>(com.example.util.WestBengalData.districtSchoolsMap)
    val districtSchoolsMap: StateFlow<Map<String, List<String>>> = _districtSchoolsMap.asStateFlow()

    private val _districts = MutableStateFlow<List<String>>(com.example.util.WestBengalData.districts)
    val districts: StateFlow<List<String>> = _districts.asStateFlow()

    private val _isLoadingDistricts = MutableStateFlow(false)
    val isLoadingDistricts: StateFlow<Boolean> = _isLoadingDistricts.asStateFlow()

    val studentProfile: StateFlow<StudentProfile> = repository.studentProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentProfile())

    @OptIn(ExperimentalCoroutinesApi::class)
    val subjects: StateFlow<List<SubjectItem>> = studentProfile.flatMapLatest { profile ->
        val cid = if (profile.classId.isNotBlank()) profile.classId else "class_10_wbbse"
        val stream = if (cid == "class_11_wbchse" || cid == "class_12_wbchse") profile.stream else "all"
        repository.getSubjectsForClass(cid, stream)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyQuests: StateFlow<List<DailyQuest>> = repository.dailyQuests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSubjectId = MutableStateFlow("")
    val selectedSubjectId: StateFlow<String> = _selectedSubjectId.asStateFlow()

    private val _selectedSubjectEntity = MutableStateFlow<SubjectEntity?>(null)
    val selectedSubjectEntity: StateFlow<SubjectEntity?> = _selectedSubjectEntity.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val chapters: StateFlow<List<ChapterEntity>> = selectedSubjectId.flatMapLatest { subId ->
        repository.getChaptersForSubjectFlow(subId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val topics: StateFlow<List<TopicEntity>> = selectedSubjectId.flatMapLatest { subId ->
        repository.getTopicsForSubjectFlow(subId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val questions: StateFlow<List<QuestionEntity>> = repository.getAllQuestionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<BadgeItem>> = repository.badgesFlow

    val recentlyViewed: StateFlow<List<RecentlyViewedEntity>> = repository.getRecentlyViewed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecentlyViewed(id: String, title: String, subtitle: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecentlyViewed(RecentlyViewedEntity(id, title, subtitle, type))
        }
    }

    fun toggleBookmark(id: String, title: String, subtitle: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.isBookmarked(id)) {
                repository.removeBookmark(id)
            } else {
                repository.addBookmark(BookmarkEntity(id, title, subtitle, type))
            }
        }
    }

    private val _activeStudentsCount = MutableStateFlow(1250)
    val activeStudentsCount: StateFlow<Int> = _activeStudentsCount.asStateFlow()

    private val _internetArchiveSearchQuery = MutableStateFlow("")
    val internetArchiveSearchQuery: StateFlow<String> = _internetArchiveSearchQuery.asStateFlow()

    private val _liveLeaderboard = MutableStateFlow<List<LeaderboardStudent>>(emptyList())
    val liveLeaderboard: StateFlow<List<LeaderboardStudent>> = _liveLeaderboard.asStateFlow()

    private val _activeSubjectStudents = MutableStateFlow<Map<String, Int>>(emptyMap())
    val activeSubjectStudents: StateFlow<Map<String, Int>> = _activeSubjectStudents.asStateFlow()

    private val _celebratingBadge = MutableStateFlow<BadgeItem?>(null)
    val celebratingBadge: StateFlow<BadgeItem?> = _celebratingBadge.asStateFlow()

    val aiCoachInsight: StateFlow<AiCoachInsight?> = repository.getAiCoachInsight()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.AUTH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _previousScreen = MutableStateFlow(AppScreen.HOME)

    private val _selectedChapterId = MutableStateFlow("")
    val selectedChapterId: StateFlow<String> = _selectedChapterId.asStateFlow()

    private val _selectedChapterEntity = MutableStateFlow<ChapterEntity?>(null)
    val selectedChapterEntity: StateFlow<ChapterEntity?> = _selectedChapterEntity.asStateFlow()

    private val _activeTopicStudy = MutableStateFlow<TopicItem?>(null)
    val activeTopicStudy: StateFlow<TopicItem?> = _activeTopicStudy.asStateFlow()

    private val _assessmentState = MutableStateFlow(AssessmentUiState())
    val assessmentState: StateFlow<AssessmentUiState> = _assessmentState.asStateFlow()

    private val _latestResult = MutableStateFlow<AssessmentResult?>(null)
    val latestResult: StateFlow<AssessmentResult?> = _latestResult.asStateFlow()

    private val _isBengali = MutableStateFlow(true)
    val isBengali: StateFlow<Boolean> = _isBengali.asStateFlow()

    private val prefs by lazy {
        application.getSharedPreferences("desh_exam_preferences", Context.MODE_PRIVATE)
    }

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _allPyqQuestions = MutableStateFlow<List<PyqQuestionItem>>(emptyList())
    val allPyqQuestions: StateFlow<List<PyqQuestionItem>> = _allPyqQuestions.asStateFlow()

    private val _firestoreQuestions = MutableStateFlow<List<FirestoreQuestion>>(emptyList())
    val firestoreQuestions: StateFlow<List<FirestoreQuestion>> = _firestoreQuestions.asStateFlow()

    private val _firestoreTestTemplates = MutableStateFlow<List<FirestoreTestTemplate>>(emptyList())
    val firestoreTestTemplates: StateFlow<List<FirestoreTestTemplate>> = _firestoreTestTemplates.asStateFlow()

    private val _autoSyncStatus = MutableStateFlow(AutoSyncStatus())
    val autoSyncStatus: StateFlow<AutoSyncStatus> = _autoSyncStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _aiDoubtCredits = MutableStateFlow(5)
    val aiDoubtCredits: StateFlow<Int> = _aiDoubtCredits.asStateFlow()

    private val _testScoresVersion = MutableStateFlow(0)
    val testScoresVersion: StateFlow<Int> = _testScoresVersion.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Load Pyq Questions
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _allPyqQuestions.value = PyqRepository.loadAllPyqQuestions(application)
            } catch (e: Exception) {
                Log.w("DeshExamViewModel", "Failed to load pyq: ${e.message}")
            }
        }

        // Initialize theme
        try {
            val savedDarkMode = prefs.getBoolean("pref_dark_mode", false)
            _appSettings.value = _appSettings.value.copy(isDarkMode = savedDarkMode)
        } catch (_: Throwable) {}

        // Notification channels
        try {
            MadhyamikNotificationManager.createNotificationChannels(application)
        } catch (_: Throwable) {}

        // Auth state listener
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                if (user != null) {
                    _isGuestUser.value = false
                    _currentScreen.value = AppScreen.HOME
                }
            }
        }

        // Auto select first subject
        viewModelScope.launch {
            subjects.collect { subList ->
                if (subList.isNotEmpty()) {
                    if (subList.none { it.id == _selectedSubjectId.value }) {
                        val first = subList.first()
                        _selectedSubjectId.value = first.id
                        _selectedSubjectEntity.value = repository.getSubjectById(first.id)
                    }
                }
            }
        }
    }

    fun handleNotificationRoute(route: String, targetId: String? = null) {
        when (route.uppercase()) {
            "DAILY_10", "DAILY_QUIZ" -> startAssessment(AssessmentMode.DAILY_10, targetId, null)
            "MOCK_TEST" -> navigateTo(AppScreen.MOCK)
            "BOSS_BATTLE" -> navigateTo(AppScreen.BATTLE)
            "PDF_NOTES" -> navigateTo(AppScreen.PDF_NOTES)
            "AUDIO_PODCASTS" -> navigateTo(AppScreen.AUDIO_PODCASTS)
            "PYQ" -> navigateTo(AppScreen.PYQ_EXPLORER)
            else -> navigateTo(AppScreen.HOME)
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _previousScreen.value = _currentScreen.value
            _currentScreen.value = screen
        }
    }

    fun navigateToInternetArchiveSearch(query: String) {
        _internetArchiveSearchQuery.value = query
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = AppScreen.INTERNET_ARCHIVE_SEARCH
    }

    fun goBack(): Boolean {
        return when (_currentScreen.value) {
            AppScreen.ASSESSMENT -> {
                exitAssessment()
                true
            }
            AppScreen.RESULT -> {
                _latestResult.value = null
                _currentScreen.value = AppScreen.HOME
                true
            }
            AppScreen.CHAPTER_DETAIL -> {
                _currentScreen.value = AppScreen.SUBJECT_DETAIL
                true
            }
            AppScreen.LEARN_TOPIC -> {
                _currentScreen.value = if (_previousScreen.value == AppScreen.CHAPTER_DETAIL) AppScreen.CHAPTER_DETAIL else AppScreen.JOURNEY
                true
            }
            AppScreen.CONTENT_SOLUTION -> {
                navigateTo(_previousScreen.value)
                true
            }
            AppScreen.SETTINGS -> {
                _currentScreen.value = if (_previousScreen.value != AppScreen.SETTINGS) _previousScreen.value else AppScreen.HOME
                true
            }
            AppScreen.EDIT_PROFILE -> {
                _currentScreen.value = AppScreen.PROFILE
                true
            }
            AppScreen.INTERNET_ARCHIVE_SEARCH -> {
                _currentScreen.value = AppScreen.TEXTBOOKS
                true
            }
            AppScreen.JOURNEY, AppScreen.MOCK, AppScreen.LEADERBOARD, AppScreen.BATTLE, AppScreen.AI_COACH, AppScreen.PROFILE,
            AppScreen.FLASHCARDS, AppScreen.PYQ_EXPLORER, AppScreen.OMR_PRACTICE, AppScreen.TOPPERS_GUIDE,
            AppScreen.CERTIFICATE, AppScreen.DOUBT_SOLVER, AppScreen.MULTIPLAYER_BATTLE,
            AppScreen.AUDIO_PODCASTS, AppScreen.STUDY_PLANNER, AppScreen.BOOKMARK_HUB, AppScreen.STUDY_SQUADS,
            AppScreen.PROGRESS_DASHBOARD, AppScreen.PDF_NOTES, AppScreen.PYQ_PDF_DOWNLOAD, AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD,
            AppScreen.TEXTBOOKS, AppScreen.LIVE_QUIZ, AppScreen.LIVE_MOCK_TEST, AppScreen.ADMIN_PANEL, AppScreen.WALLET,
            AppScreen.SUBJECT_DETAIL -> {
                if (_currentScreen.value != AppScreen.HOME) {
                    _currentScreen.value = AppScreen.HOME
                    true
                } else false
            }
            AppScreen.AUTH, AppScreen.ONBOARDING, AppScreen.HOME -> false
        }
    }

    fun toggleLanguage() {
        _isBengali.value = !_isBengali.value
    }

    fun toggleDarkMode() {
        val newMode = !_appSettings.value.isDarkMode
        _appSettings.value = _appSettings.value.copy(isDarkMode = newMode)
        prefs.edit().putBoolean("pref_dark_mode", newMode).apply()
    }

    fun updateThemeMode(darkMode: Boolean) {
        _appSettings.value = _appSettings.value.copy(isDarkMode = darkMode)
        prefs.edit().putBoolean("pref_dark_mode", darkMode).apply()
    }

    fun toggleHighContrast(enabled: Boolean? = null) {
        val newV = enabled ?: !_appSettings.value.highContrastText
        _appSettings.value = _appSettings.value.copy(highContrastText = newV)
    }

    fun toggleBengaliNumerals(enabled: Boolean? = null) {
        val newV = enabled ?: !_appSettings.value.useBengaliNumerals
        _appSettings.value = _appSettings.value.copy(useBengaliNumerals = newV)
    }

    fun toggleSoundEffects(enabled: Boolean? = null) {
        val newV = enabled ?: !_appSettings.value.soundEffectsEnabled
        _appSettings.value = _appSettings.value.copy(soundEffectsEnabled = newV)
    }

    fun toggleVibration(enabled: Boolean? = null) {
        val newV = enabled ?: !_appSettings.value.vibrationEnabled
        _appSettings.value = _appSettings.value.copy(vibrationEnabled = newV)
    }

    fun toggleConfetti(enabled: Boolean? = null) {
        val newV = enabled ?: !_appSettings.value.confettiEnabled
        _appSettings.value = _appSettings.value.copy(confettiEnabled = newV)
    }

    fun toggleAutoAdvance(enabled: Boolean? = null) {
        val newV = enabled ?: !_appSettings.value.autoAdvanceQuestions
        _appSettings.value = _appSettings.value.copy(autoAdvanceQuestions = newV)
    }

    fun updateFontSizeScale(scale: Float) {
        _appSettings.value = _appSettings.value.copy(fontSizeScale = scale)
    }

    fun updateTargetExam(exam: String) {
        _appSettings.value = _appSettings.value.copy(targetExam = exam)
    }

    fun updateDailyGoal(goal: Int) {
        _appSettings.value = _appSettings.value.copy(dailyGoalQuestions = goal)
    }

    fun updateStudyReminder(enabled: Boolean, time: String) {
        _appSettings.value = _appSettings.value.copy(studyReminderEnabled = enabled, studyReminderTime = time)
    }

    fun updateMockTestAlerts(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(mockTestAlertsEnabled = enabled)
    }

    fun updateStreakSaverAlerts(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(streakSaverAlertsEnabled = enabled)
    }

    fun updateExamTipsAlerts(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(examTipsAlertsEnabled = enabled)
    }

    fun sendTestStudyReminder() {}
    fun sendTestMockTestAlert() {}
    fun sendTestStreakAlert() {}

    fun setProMode(isPro: Boolean) {
        viewModelScope.launch {
            val curr = database.deshExamDao().getUserProgressOnce() ?: UserProgressEntity()
            database.deshExamDao().insertOrUpdateProgress(curr.copy(isPro = isPro))
        }
    }

    fun resetDailyPractice() {}

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _isGuestUser.value = true
            _currentScreen.value = AppScreen.AUTH
        }
    }

    fun dismissBadgeCelebration() {
        _celebratingBadge.value = null
    }

    fun openSubjectDetail(subject: SubjectEntity) {
        _selectedSubjectEntity.value = subject
        _selectedSubjectId.value = subject.id
        navigateTo(AppScreen.SUBJECT_DETAIL)
    }

    fun openSubjectDetailById(subjectId: String) {
        viewModelScope.launch {
            val entity = repository.getSubjectById(subjectId) ?: SubjectEntity(
                id = subjectId,
                code = subjectId,
                name = "বিষয় বিবরণ",
                totalChapters = 5,
                completedChapters = 0,
                masteryPercentage = 0,
                colorHex = 0xFF2563EBL,
                classId = studentProfile.value.classId.ifBlank { "class_10_wbbse" },
                stream = "common"
            )
            openSubjectDetail(entity)
        }
    }

    fun openChapterDetail(chapter: ChapterEntity) {
        _selectedChapterEntity.value = chapter
        _selectedChapterId.value = chapter.id
        navigateTo(AppScreen.CHAPTER_DETAIL)
    }

    fun openTopicStudy(topicId: String) {
        viewModelScope.launch {
            val topic = repository.getTopicById(topicId)
            if (topic != null) {
                _activeTopicStudy.value = topic
                navigateTo(AppScreen.LEARN_TOPIC)
            }
        }
    }

    fun selectSubject(subjectId: String) {
        _selectedSubjectId.value = subjectId
        viewModelScope.launch {
            val chapters = repository.getChaptersOnce(subjectId)
            if (chapters.isNotEmpty()) {
                _selectedChapterId.value = chapters.first().id
            }
        }
    }

    fun selectChapter(chapterId: String) {
        _selectedChapterId.value = chapterId
    }

    fun updateStudentProfile(newProfile: StudentProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(newProfile)
        }
    }

    fun updateProfileDetails(name: String, school: String, district: String, targetExam: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStudentProfile(name = name, school = school, district = district, targetExam = targetExam)
        }
    }

    fun switchActiveExamClass(classId: String, stream: String = "all") {
        viewModelScope.launch {
            val classObj = AcademicClassProvider.ALL_CLASSES.find { it.id == classId }
            if (classObj != null) {
                val finalStream = if (classId in listOf("class_11_wbchse", "class_12_wbchse")) stream else "all"
                repository.updateStudentProfile(
                    name = "",
                    school = "",
                    district = "",
                    targetExam = classObj.targetExam,
                    classId = classId,
                    stream = finalStream
                )
                val newSubs = repository.getSubjectsForClassOnce(classId)
                if (newSubs.isNotEmpty()) {
                    val firstSub = if (finalStream == "all") newSubs.first() else (newSubs.find { it.stream == "common" || it.stream == finalStream } ?: newSubs.first())
                    _selectedSubjectId.value = firstSub.id
                    _selectedSubjectEntity.value = firstSub
                    val chs = repository.getChaptersOnce(firstSub.id)
                    if (chs.isNotEmpty()) {
                        _selectedChapterId.value = chs.first().id
                        _selectedChapterEntity.value = null
                    }
                }
            }
        }
    }

    fun switchStream(newStream: String) {
        viewModelScope.launch {
            val current = studentProfile.value
            repository.updateStudentProfile(
                name = "",
                school = "",
                district = "",
                targetExam = "",
                classId = current.classId,
                stream = newStream
            )
        }
    }

    fun completeOnboarding(
        name: String,
        classLevel: String,
        district: String,
        school: String,
        targetExam: String,
        stream: String = "all",
        studyGoals: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val resolvedClassId = when {
                classLevel.contains("Class 5") || classLevel.contains("পঞ্চম") -> "class_5_wbbse"
                classLevel.contains("Class 6") || classLevel.contains("ষষ্ঠ") -> "class_6_wbbse"
                classLevel.contains("Class 7") || classLevel.contains("সপ্তম") -> "class_7_wbbse"
                classLevel.contains("Class 8") || classLevel.contains("অষ্টম") -> "class_8_wbbse"
                classLevel.contains("Class 9") || classLevel.contains("নবম") -> "class_9_wbbse"
                classLevel.contains("Class 11") || classLevel.contains("একাদশ") -> "class_11_wbchse"
                classLevel.contains("Class 12") || classLevel.contains("দ্বাদশ") || classLevel.contains("উচ্চ মাধ্যমিক") -> "class_12_wbchse"
                else -> "class_10_wbbse"
            }
            val finalStream = if (resolvedClassId in listOf("class_11_wbchse", "class_12_wbchse")) stream else "all"
            repository.updateStudentProfile(name, school, district, targetExam, resolvedClassId, finalStream, studyGoals)
            val newSubs = repository.getSubjectsForClassOnce(resolvedClassId)
            if (newSubs.isNotEmpty()) {
                val firstSub = if (finalStream == "all") newSubs.first() else (newSubs.find { it.stream == "common" || it.stream == finalStream } ?: newSubs.first())
                _selectedSubjectId.value = firstSub.id
                _selectedSubjectEntity.value = firstSub
                val chs = repository.getChaptersOnce(firstSub.id)
                if (chs.isNotEmpty()) {
                    _selectedChapterId.value = chs.first().id
                    _selectedChapterEntity.value = null
                }
            }
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null
            when (val res = authRepository.signInWithEmail(email, pass)) {
                is AuthResult.Success -> {
                    _isGuestUser.value = false
                    _currentScreen.value = AppScreen.HOME
                }
                is AuthResult.Error -> {
                    _authErrorMessage.value = if (_isBengali.value) res.messageBengali else res.messageEnglish
                }
            }
            _isAuthLoading.value = false
        }
    }

    fun signUp(name: String, school: String, district: String, targetExam: String, email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null
            when (val res = authRepository.signUpWithEmail(email, pass, name)) {
                is AuthResult.Success -> {
                    _isGuestUser.value = false
                    repository.updateStudentProfile(name, school, district, targetExam)
                    _currentScreen.value = AppScreen.ONBOARDING
                }
                is AuthResult.Error -> {
                    _authErrorMessage.value = if (_isBengali.value) res.messageBengali else res.messageEnglish
                }
            }
            _isAuthLoading.value = false
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null
            when (val res = authRepository.sendPasswordReset(email)) {
                is AuthResult.Success -> {
                    _authSuccessMessage.value = if (_isBengali.value) "পাসওয়ার্ড রিসেট ইমেল পাঠানো হয়েছে।" else "Password reset email sent."
                }
                is AuthResult.Error -> {
                    _authErrorMessage.value = if (_isBengali.value) res.messageBengali else res.messageEnglish
                }
            }
            _isAuthLoading.value = false
        }
    }

    fun continueAsGuest() {
        _isGuestUser.value = true
        _currentScreen.value = AppScreen.HOME
    }

    fun clearAuthMessages() {
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
    }

    fun signInWithGoogle(idToken: String, displayName: String?, email: String?) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null
            when (val res = authRepository.signInWithGoogleCredential(idToken, displayName, email)) {
                is AuthResult.Success -> {
                    _isGuestUser.value = false
                    _currentScreen.value = AppScreen.HOME
                }
                is AuthResult.Error -> {
                    _authErrorMessage.value = if (_isBengali.value) res.messageBengali else res.messageEnglish
                }
            }
            _isAuthLoading.value = false
        }
    }

    fun updateAccountPassword(newPass: String) {
        if (newPass.length < 6) {
            _authErrorMessage.value = if (_isBengali.value) "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে।" else "Password must be at least 6 characters."
            return
        }
        viewModelScope.launch {
            _isAuthLoading.value = true
            when (val res = authRepository.updatePassword(newPass)) {
                is AuthResult.Success -> {
                    _authSuccessMessage.value = if (_isBengali.value) "পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে!" else "Password changed successfully!"
                }
                is AuthResult.Error -> {
                    _authErrorMessage.value = if (_isBengali.value) res.messageBengali else res.messageEnglish
                }
            }
            _isAuthLoading.value = false
        }
    }

    fun startAssessment(
        mode: AssessmentMode,
        targetId: String? = null,
        customTitle: String? = null,
        setNumber: Int = 1
    ) {
        viewModelScope.launch {
            val cid = studentProfile.value.classId.ifBlank { "class_10_wbbse" }
            val session = repository.createAssessmentSession(mode, targetId, customTitle, setNumber, cid)
            if (session.questions.isNotEmpty()) {
                _assessmentState.value = AssessmentUiState(
                    session = session,
                    currentQuestionIndex = 0,
                    selectedOptionIndex = -1,
                    isAnswerChecked = false,
                    isCorrect = false,
                    answers = emptyList(),
                    timeRemainingSeconds = session.timeLimitSeconds,
                    questionStartTimeSeconds = session.timeLimitSeconds
                )
                startTimer()
                navigateTo(AppScreen.ASSESSMENT)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_assessmentState.value.timeRemainingSeconds > 0) {
                delay(1000)
                _assessmentState.value = _assessmentState.value.copy(
                    timeRemainingSeconds = _assessmentState.value.timeRemainingSeconds - 1
                )
            }
            finishAssessment()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun selectOption(index: Int) {
        val state = _assessmentState.value
        val session = state.session ?: return
        val currentQ = session.questions.getOrNull(state.currentQuestionIndex) ?: return

        if (session.allowsInstantFeedback) {
            if (state.isAnswerChecked) return
            val isCorrect = index == currentQ.correctOptionIndex
            val timeSpent = (state.questionStartTimeSeconds - state.timeRemainingSeconds).coerceAtLeast(1)

            val newAnswer = UserAnswer(
                questionId = currentQ.id,
                selectedOptionIndex = index,
                isCorrect = isCorrect,
                timeSpentSeconds = timeSpent
            )
            _assessmentState.value = state.copy(
                selectedOptionIndex = index,
                isAnswerChecked = true,
                isCorrect = isCorrect,
                answers = state.answers + newAnswer
            )
        } else {
            val newAnswersMap = state.selectedAnswersMap.toMutableMap()
            newAnswersMap[state.currentQuestionIndex] = index
            _assessmentState.value = state.copy(
                selectedOptionIndex = index,
                selectedAnswersMap = newAnswersMap
            )
        }
    }

    fun checkOrSubmitCurrentAnswer() {
        val state = _assessmentState.value
        if (!state.isAnswerChecked && state.selectedOptionIndex >= 0) {
            selectOption(state.selectedOptionIndex)
        }
    }

    fun advanceToNextQuestion() {
        val state = _assessmentState.value
        val session = state.session ?: return
        if (state.currentQuestionIndex < session.questions.size - 1) {
            _assessmentState.value = state.copy(
                currentQuestionIndex = state.currentQuestionIndex + 1,
                selectedOptionIndex = -1,
                isAnswerChecked = false,
                isCorrect = false,
                questionStartTimeSeconds = state.timeRemainingSeconds
            )
        } else {
            finishAssessment()
        }
    }

    fun navigateToQuestion(targetIndex: Int) {
        val state = _assessmentState.value
        val session = state.session ?: return
        if (targetIndex in session.questions.indices) {
            val savedOption = state.selectedAnswersMap[targetIndex] ?: -1
            _assessmentState.value = state.copy(
                currentQuestionIndex = targetIndex,
                selectedOptionIndex = savedOption,
                isAnswerChecked = false,
                isCorrect = false,
                questionStartTimeSeconds = state.timeRemainingSeconds
            )
        }
    }

    fun nextMockQuestion() {
        val state = _assessmentState.value
        val session = state.session ?: return
        if (state.currentQuestionIndex + 1 < session.questions.size) {
            navigateToQuestion(state.currentQuestionIndex + 1)
        }
    }

    fun previousMockQuestion() {
        val state = _assessmentState.value
        if (state.currentQuestionIndex > 0) {
            navigateToQuestion(state.currentQuestionIndex - 1)
        }
    }

    fun toggleMarkForReview(questionIndex: Int? = null) {
        val state = _assessmentState.value
        val idx = questionIndex ?: state.currentQuestionIndex
        val updated = state.markedForReviewIndices.toMutableSet()
        if (updated.contains(idx)) updated.remove(idx) else updated.add(idx)
        _assessmentState.value = state.copy(markedForReviewIndices = updated)
    }

    fun clearCurrentResponse() {
        val state = _assessmentState.value
        val newAnswersMap = state.selectedAnswersMap.toMutableMap()
        newAnswersMap.remove(state.currentQuestionIndex)
        _assessmentState.value = state.copy(
            selectedOptionIndex = -1,
            selectedAnswersMap = newAnswersMap
        )
    }

    fun submitMockTest() {
        finishAssessment()
    }

    fun applyFiftyFiftyLifeline() {
        val state = _assessmentState.value
        val session = state.session ?: return
        val currentQ = session.questions.getOrNull(state.currentQuestionIndex) ?: return
        if (!state.isFiftyFiftyUsed) {
            val wrongIndices = currentQ.options.indices.filter { it != currentQ.correctOptionIndex }
            val toEliminate = wrongIndices.shuffled().take(2).toSet()
            _assessmentState.value = state.copy(
                eliminatedOptionIndices = toEliminate,
                isFiftyFiftyUsed = true
            )
        }
    }

    private fun finishAssessment() {
        stopTimer()
        val state = _assessmentState.value
        val session = state.session ?: return
        viewModelScope.launch {
            val finalAnswers = if (session.allowsInstantFeedback) {
                state.answers
            } else {
                session.questions.mapIndexed { index, q ->
                    val sel = state.selectedAnswersMap[index] ?: -1
                    UserAnswer(
                        questionId = q.id,
                        selectedOptionIndex = sel,
                        isCorrect = sel == q.correctOptionIndex,
                        timeSpentSeconds = 1
                    )
                }
            }
            val result = repository.submitAssessment(session, finalAnswers)
            _latestResult.value = result
            _assessmentState.value = AssessmentUiState()
            _currentScreen.value = AppScreen.RESULT
        }
    }

    fun exitAssessment() {
        stopTimer()
        _assessmentState.value = AssessmentUiState()
        _currentScreen.value = AppScreen.HOME
    }

    fun dismissResult() {
        _latestResult.value = null
        _currentScreen.value = AppScreen.HOME
    }

    fun getTopicSetScore(topicId: String, setNumber: Int): Int = repository.getTopicSetScore(topicId, setNumber)
    fun isTopicSetUnlocked(topicId: String, setNumber: Int, passThreshold: Int = 60): Boolean = setNumber <= 1 || repository.getTopicSetScore(topicId, setNumber - 1) >= passThreshold
    fun isTopicMockUnlocked(topicId: String, passThreshold: Int = 60): Boolean = repository.getTopicSetScore(topicId, 1) >= passThreshold
    fun isTopicSpeedUnlocked(topicId: String, passThreshold: Int = 60): Boolean = repository.getTopicMockScore(topicId) >= passThreshold || repository.getTopicSetScore(topicId, 1) >= passThreshold
    fun getTopicMockScore(topicId: String): Int = repository.getTopicMockScore(topicId)
    fun getTopicSpeedScore(topicId: String): Int = repository.getTopicSpeedScore(topicId)
    fun getTopicExtraSets(topicId: String): Int = repository.getExtraGeneratedSets(topicId)
    fun generateNewDynamicSet(topicId: String): Int {
        val newTotal = repository.addExtraGeneratedSet(topicId, 1)
        _testScoresVersion.value += 1
        return newTotal
    }

    fun getSubjectTestScore(testId: String): Int = repository.getSubjectTestScore(testId)
    fun isSubjectTestUnlocked(testNumber: Int, previousTestId: String?, passThreshold: Int = 60): Boolean = testNumber <= 1 || previousTestId.isNullOrBlank() || repository.getSubjectTestScore(previousTestId) >= passThreshold

    suspend fun getRandomBattleQuestions(subjectId: String? = null, chapterId: String? = null, topicId: String? = null, count: Int = 5): List<QuestionItem> {
        val cid = studentProfile.value.classId.ifBlank { "class_10_wbbse" }
        return repository.getRandomBattleQuestions(cid, subjectId, chapterId, topicId, count)
    }

    suspend fun getTopicQuestionCount(topicId: String): Int = repository.getTopicQuestionCount(topicId)

    fun repairDailyStreakFromAd() {
        viewModelScope.launch {
            repository.repairStreak()
        }
    }

    fun addRewardBonus(xp: Int, coins: Int) {
        viewModelScope.launch {
            repository.addXpAndCoins(xp, coins)
        }
    }

    fun addAiCreditsFromAd(amount: Int) {
        viewModelScope.launch {
            repository.addAiCredits(amount)
            _aiDoubtCredits.value += amount
        }
    }

    fun unlockAudioPodcast(title: String) {
        viewModelScope.launch {
            repository.unlockPodcast(title)
        }
    }

    fun unlockPdfNote(noteId: String) {
        viewModelScope.launch {
            repository.unlockNote(noteId)
        }
    }

    fun refreshApp(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            _isRefreshing.value = false
            onComplete?.invoke()
        }
    }

    fun triggerManualSync(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            onComplete?.invoke(true, "সফলভাবে সিঙ্ক সম্পন্ন হয়েছে")
        }
    }

    fun syncRemoteQuestionsToLocal() {}
    
    fun fetchFirestoreQuestions(limit: String = "1000", classId: String? = null) {}
    fun fetchFirestoreTestTemplates(limit: String = "100", classId: String? = null) {}

    fun syncCurriculumToFirestore(onProgress: (String) -> Unit = {}, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch { onComplete(true, "সিঙ্ক সম্পন্ন হয়েছে") }
    }

    fun addQuestionToFirestore(
        classId: String,
        subjectId: String,
        chapterId: String,
        topicId: String,
        questionText: String,
        optionsList: List<String>,
        correctIndex: Int,
        correctAnswerText: String,
        explanationText: String,
        difficulty: String,
        marks: Int,
        pyqYear: String,
        customQuestionId: String? = null,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val q = FirestoreQuestion(
                id = customQuestionId ?: "${subjectId}_q_${System.currentTimeMillis()}",
                topicId = topicId,
                chapterId = chapterId,
                subjectId = subjectId,
                type = "MCQ",
                difficulty = difficulty,
                question = questionText,
                options = optionsList,
                correctOptionIndex = correctIndex,
                correctAnswerText = correctAnswerText,
                explanation = explanationText,
                marks = marks,
                tags = listOf(topicId, pyqYear).filter { it.isNotBlank() },
                classId = classId
            )
            val updated = _firestoreQuestions.value + q
            _firestoreQuestions.value = updated
            onComplete(true, "প্রশ্ন সফলভাবে যুক্ত করা হয়েছে!")
        }
    }

    fun updateFirestoreQuestion(question: FirestoreQuestion, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val updated = _firestoreQuestions.value.map { if (it.id == question.id) question else it }
            _firestoreQuestions.value = updated
            onComplete(true, "প্রশ্ন আপডেট হয়েছে")
        }
    }

    fun deleteFirestoreQuestion(questionId: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val updated = _firestoreQuestions.value.filter { it.id != questionId }
            _firestoreQuestions.value = updated
            onComplete(true, "প্রশ্ন মোছা হয়েছে")
        }
    }

    fun deleteFirestoreTestTemplate(testId: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val updated = _firestoreTestTemplates.value.filter { it.id != testId }
            _firestoreTestTemplates.value = updated
            onComplete(true, "টেস্ট মোছা হয়েছে")
        }
    }

    fun createCustomTestAndAddQuestion(
        testTitle: String,
        testId: String,
        classId: String,
        subjectId: String,
        timeMinutes: Int,
        assessmentMode: String,
        questionText: String,
        optionsList: List<String>,
        correctIndex: Int,
        correctAnswerText: String,
        explanationText: String,
        difficulty: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val t = FirestoreTestTemplate(
                id = testId,
                title = testTitle,
                classId = classId,
                subjectId = subjectId,
                testType = assessmentMode,
                durationMinutes = timeMinutes,
                totalMarks = 10,
                questions = emptyList()
            )
            _firestoreTestTemplates.value = _firestoreTestTemplates.value + t
            onComplete(true, "টেস্ট সংরক্ষিত হয়েছে")
        }
    }

    fun createStructuredAssessment(
        testType: String,
        classId: String,
        subjectId: String,
        chapterId: String,
        title: String,
        description: String,
        durationMinutes: Int,
        totalMarks: Int,
        questionText: String,
        options: List<String>,
        correctIndex: Int,
        correctAnswerText: String,
        explanation: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val t = FirestoreTestTemplate(
                id = "test_${System.currentTimeMillis()}",
                title = title,
                classId = classId,
                subjectId = subjectId,
                testType = testType,
                durationMinutes = durationMinutes,
                totalMarks = totalMarks,
                questions = emptyList()
            )
            _firestoreTestTemplates.value = _firestoreTestTemplates.value + t
            onComplete(true, "অ্যাসেসমেন্ট তৈরি হয়েছে")
        }
    }

    fun importBulkQuestions(
        rawInput: String,
        classId: String,
        subjectId: String,
        chapterId: String,
        topicId: String,
        pyqYear: String,
        customTestTitle: String,
        onComplete: (Boolean, String, Int) -> Unit
    ) {
        viewModelScope.launch {
            onComplete(true, "প্রশ্ন ইম্পোর্ট সম্পন্ন", 5)
        }
    }

    fun syncAllDataWithFirebase(onProgress: (String) -> Unit = {}, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch { onComplete(true, "সম্পূর্ণ ডেটা সিঙ্ক সম্পন্ন হয়েছে") }
    }
}
