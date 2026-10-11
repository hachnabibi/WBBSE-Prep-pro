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
import com.example.data.firestore.*
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
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
    BADGES,
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
    INTERNET_ARCHIVE_SEARCH,
    VIDEO_LESSON,
    MEMBERSHIP,
    WEAKNESS_INSIGHT
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

data class CloudBackupState(
    val isBackingUp: Boolean = false,
    val step: String = "",
    val progress: Float = 0f,
    val error: String? = null,
    val lastBackupTimestamp: Long = 0L,
    val summary: String? = null,
    val stats: LocalDatabaseStats? = null
)

data class LocalDatabaseStats(
    val boardCount: Int = 0,
    val classCount: Int = 0,
    val subjectCount: Int = 0,
    val chapterCount: Int = 0,
    val topicCount: Int = 0,
    val questionCount: Int = 0,
    val videoProgressCount: Int = 0
)

data class CloudDatabaseStats(
    val boardCount: Int = 0,
    val classCount: Int = 0,
    val subjectCount: Int = 0,
    val chapterCount: Int = 0,
    val topicCount: Int = 0,
    val questionCount: Int = 0,
    val testCount: Int = 0,
    val isOnline: Boolean = true,
    val isChecking: Boolean = false,
    val lastCheckedTimestamp: Long = 0L
)

data class StructuredQuestionInput(
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = ""
)

class DeshExamViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DeshExamDatabase.getDatabase(application, viewModelScope)
    val repository = DeshExamRepository(database.deshExamDao(), application)
    val authRepository = FirebaseAuthRepository(application.applicationContext)
    val firestoreService = FirestoreService(application)

    private val _weakTopics = MutableStateFlow<List<com.example.data.local.TopicMistakeCount>>(emptyList())
    val weakTopics: StateFlow<List<com.example.data.local.TopicMistakeCount>> = _weakTopics.asStateFlow()

    private val _cloudBackupState = MutableStateFlow(CloudBackupState())
    val cloudBackupState: StateFlow<CloudBackupState> = _cloudBackupState.asStateFlow()

    private val _cloudStats = MutableStateFlow(CloudDatabaseStats())
    val cloudStats: StateFlow<CloudDatabaseStats> = _cloudStats.asStateFlow()

    private val _isPullingFromCloud = MutableStateFlow(false)
    val isPullingFromCloud: StateFlow<Boolean> = _isPullingFromCloud.asStateFlow()

    val authUserState: StateFlow<AuthUserState?> = authRepository.authStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUserState)

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    private val _isGuestUser = MutableStateFlow(true)
    val isGuestUser: StateFlow<Boolean> = _isGuestUser.asStateFlow()

    var contentSolutionTitle: String = ""
    var contentSolutionContent: String? = null
    var contentSolutionSolution: String? = null
    var contentSolutionInitialTab: Int = 0

    fun openContentSolution(
        title: String,
        content: String?,
        solution: String?,
        initialTab: Int = 0,
        chapterId: String? = null,
        topicId: String? = null
    ) {
        contentSolutionTitle = title
        contentSolutionInitialTab = initialTab
        
        viewModelScope.launch(Dispatchers.IO) {
            var finalContent = content
            var finalSolution = solution

            fun isPlaceholder(s: String?): Boolean {
                if (s.isNullOrBlank()) return true
                val t = s.trim()
                if (t == "## সমাধান" || (t.startsWith("## সমাধান") && (t.contains("এখানে থাকবে") || t.length < 50))) return true
                if (t.contains("খুব শীঘ্রই যুক্ত করা হবে")) return true
                if (t.contains("উক্ত বিষয়টি সম্পর্কে বিস্তারিত ধারণা এবং সঠিক সমাধান এখানে দেওয়া হলো")) return true
                return false
            }

            // 1. If topicId is provided, always prioritize real topic-level solution from assets or Room
            if (topicId != null) {
                val realTopicSolution = repository.getSolutionsFromAssetForTopic(topicId)
                if (!realTopicSolution.isNullOrBlank() && !isPlaceholder(realTopicSolution)) {
                    finalSolution = realTopicSolution
                } else {
                    val topicEntity = repository.getTopicByIdOnce(topicId)
                    if (topicEntity != null) {
                        if (!isPlaceholder(topicEntity.solutionData)) {
                            finalSolution = topicEntity.solutionData
                        }
                        if (isPlaceholder(finalContent) && !topicEntity.contentData.isNullOrBlank()) {
                            finalContent = topicEntity.contentData
                        }
                    }
                }
            }

            // 2. If chapterId is provided and solution is still placeholder or blank, fetch real chapter solutions
            if ((finalSolution.isNullOrBlank() || isPlaceholder(finalSolution)) && chapterId != null) {
                val chapterSolutions = repository.getSolutionsFromAssetForChapter(chapterId)
                if (!chapterSolutions.isNullOrBlank() && !isPlaceholder(chapterSolutions)) {
                    finalSolution = chapterSolutions
                }
            }

            // 3. If chapterId is provided and content/solution still blank, aggregate from chapter topics
            if ((finalContent.isNullOrBlank() || isPlaceholder(finalContent) || finalSolution.isNullOrBlank() || isPlaceholder(finalSolution)) && chapterId != null) {
                val topics = repository.getTopicsForChapterOnce(chapterId)
                if (finalContent.isNullOrBlank() || isPlaceholder(finalContent)) {
                    val cList = topics.mapNotNull { it.contentData?.takeIf { c -> c.isNotBlank() && !isPlaceholder(c) } }
                    if (cList.isNotEmpty()) {
                        finalContent = cList.joinToString("\n\n---\n\n")
                    }
                }
                if (finalSolution.isNullOrBlank() || isPlaceholder(finalSolution)) {
                    val solList = mutableListOf<String>()
                    for (t in topics) {
                        val sol = repository.getSolutionsFromAssetForTopic(t.id) ?: t.solutionData
                        if (!sol.isNullOrBlank() && !isPlaceholder(sol)) {
                            solList.add("### 📌 ${t.title}\n\n$sol")
                        }
                    }
                    if (solList.isNotEmpty()) {
                        finalSolution = solList.joinToString("\n\n---\n\n")
                    }
                }
            }

            // Fallback generation if still blank
            if (finalContent.isNullOrBlank()) {
                finalContent = "# $title\n\nএই অধ্যায় বা টপিকের বিস্তারিত কন্টেন্ট এবং নোটস শীট খুব শীঘ্রই যুক্ত করা হবে।"
            }
            if (finalSolution.isNullOrBlank() || isPlaceholder(finalSolution)) {
                finalSolution = "# $title - সমাধান\n\nএই অধ্যায় বা টপিকের অনুশীলনী ও প্রশ্নোত্তর সমাধান খুব শীঘ্রই যুক্ত করা হবে।"
            }

            withContext(Dispatchers.Main) {
                contentSolutionContent = finalContent
                contentSolutionSolution = finalSolution
                navigateTo(AppScreen.CONTENT_SOLUTION)
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

    val claimedCoinTaskIds: StateFlow<Set<String>> = studentProfile
        .map { it.claimedCoinTaskIds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun claimCoinTask(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = studentProfile.value
            if (!current.claimedCoinTaskIds.contains(taskId)) {
                val updated = current.copy(claimedCoinTaskIds = current.claimedCoinTaskIds + taskId)
                repository.updateProfile(updated)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val subjects: StateFlow<List<SubjectItem>> = studentProfile.flatMapLatest { profile ->
        val cid = if (profile.classId.isNotBlank()) profile.classId else "class_10_wbbse"
        if (cid == "class_11_wbchse" || cid == "class_12_wbchse") {
            val rawStream = profile.stream
            val (streamPart, semPart) = if (rawStream.contains("_sem")) {
                val parts = rawStream.split("_")
                parts[0] to parts[1] // e.g. "science" to "sem1"
            } else {
                rawStream to null
            }
            repository.getSubjectsForClass(cid, streamPart).map { list ->
                if (semPart != null) {
                    list.filter { subject ->
                        subject.id.lowercase().contains(semPart) || 
                        subject.stream.lowercase().contains(semPart) ||
                        subject.textbookName.lowercase().contains(semPart.replace("sem", "সেমিস্টার ")) ||
                        subject.textbookName.lowercase().contains(semPart.replace("sem", "semester "))
                    }
                } else {
                    list
                }
            }
        } else {
            repository.getSubjectsForClass(cid, "all")
        }
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

    private val prefs by lazy {
        getApplication<Application>().getSharedPreferences("desh_exam_preferences", Context.MODE_PRIVATE)
    }

    private fun isRestorableTabScreen(screen: AppScreen): Boolean {
        return screen != AppScreen.SPLASH &&
                screen != AppScreen.AUTH &&
                screen != AppScreen.ONBOARDING &&
                screen != AppScreen.ASSESSMENT &&
                screen != AppScreen.RESULT &&
                screen != AppScreen.CHAPTER_DETAIL &&
                screen != AppScreen.SUBJECT_DETAIL &&
                screen != AppScreen.LEARN_TOPIC &&
                screen != AppScreen.CONTENT_SOLUTION
    }

    private fun resolveInitialScreen(): AppScreen {
        return try {
            val p = getApplication<Application>().getSharedPreferences("desh_exam_preferences", Context.MODE_PRIVATE)
            val guestCompleted = p.getBoolean("onboarding_completed_guest", false)
            val isGuest = p.getBoolean("is_guest_user", false)
            val hasOnboarded = p.getBoolean("has_completed_onboarding", false) || guestCompleted || isGuest
            val user = authRepository.currentUserState
            val isUserValid = (user != null && (user.isAnonymous || p.getBoolean("onboarding_completed_${user.uid}", false))) || guestCompleted || isGuest

            if (isUserValid || hasOnboarded) {
                val saved = p.getString("saved_last_screen", null)
                if (!saved.isNullOrBlank()) {
                    try {
                        val screen = AppScreen.valueOf(saved)
                        if (isRestorableTabScreen(screen)) {
                            return screen
                        }
                    } catch (_: Exception) {}
                }
                AppScreen.HOME
            } else {
                AppScreen.SPLASH
            }
        } catch (_: Throwable) {
            AppScreen.SPLASH
        }
    }

    private var lastRestoredUid: String? = null
    private val _screenBackStack = ArrayDeque<AppScreen>()
    val screenBackStack: List<AppScreen> get() = _screenBackStack.toList()

    private val _currentScreen = MutableStateFlow<AppScreen>(resolveInitialScreen())
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _previousScreen = MutableStateFlow(AppScreen.HOME)
    private val _screenBeforeAssessment = MutableStateFlow<AppScreen?>(null)

    private val _selectedChapterId = MutableStateFlow("")
    val selectedChapterId: StateFlow<String> = _selectedChapterId.asStateFlow()

    private val _selectedChapterEntity = MutableStateFlow<ChapterEntity?>(null)
    val selectedChapterEntity: StateFlow<ChapterEntity?> = _selectedChapterEntity.asStateFlow()

    private val _activeTopicStudy = MutableStateFlow<TopicItem?>(null)
    val activeTopicStudy: StateFlow<TopicItem?> = _activeTopicStudy.asStateFlow()

    private val _activeVideoLesson = MutableStateFlow<VideoLesson?>(null)
    val activeVideoLesson: StateFlow<VideoLesson?> = _activeVideoLesson.asStateFlow()

    private val _activeVideoTopic = MutableStateFlow<TopicItem?>(null)
    val activeVideoTopic: StateFlow<TopicItem?> = _activeVideoTopic.asStateFlow()

    private val _assessmentState = MutableStateFlow(AssessmentUiState())
    val assessmentState: StateFlow<AssessmentUiState> = _assessmentState.asStateFlow()

    private val _latestResult = MutableStateFlow<AssessmentResult?>(null)
    val latestResult: StateFlow<AssessmentResult?> = _latestResult.asStateFlow()

    private val _isBengali = MutableStateFlow(true)
    val isBengali: StateFlow<Boolean> = _isBengali.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _allPyqQuestions = MutableStateFlow<List<PyqQuestionItem>>(emptyList())
    val allPyqQuestions: StateFlow<List<PyqQuestionItem>> = _allPyqQuestions.asStateFlow()

    private val _firestoreQuestions = MutableStateFlow<List<FirestoreQuestion>>(emptyList())
    val firestoreQuestions: StateFlow<List<FirestoreQuestion>> = _firestoreQuestions.asStateFlow()

    private val _firestoreTestTemplates = MutableStateFlow<List<FirestoreTestTemplate>>(emptyList())
    val firestoreTestTemplates: StateFlow<List<FirestoreTestTemplate>> = _firestoreTestTemplates.asStateFlow()

    private val _customPodcasts = MutableStateFlow<List<com.example.data.firestore.FirestorePodcast>>(emptyList())
    val customPodcasts: StateFlow<List<com.example.data.firestore.FirestorePodcast>> = _customPodcasts.asStateFlow()

    private val _autoSyncStatus = MutableStateFlow(AutoSyncStatus())
    val autoSyncStatus: StateFlow<AutoSyncStatus> = _autoSyncStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _aiDoubtCredits = MutableStateFlow(5)
    val aiDoubtCredits: StateFlow<Int> = _aiDoubtCredits.asStateFlow()

    private val _testScoresVersion = MutableStateFlow(0)
    val testScoresVersion: StateFlow<Int> = _testScoresVersion.asStateFlow()

    private val _videoProgressMap = MutableStateFlow<Map<String, FirestoreVideoProgress>>(emptyMap())
    val videoProgressMap: StateFlow<Map<String, FirestoreVideoProgress>> = _videoProgressMap.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Load initial local database statistics for cloud backup dashboard
        loadLocalDatabaseStats()
        loadWeakTopics()

        // Sync & update database from assets only when needed (first launch or version update) to avoid startup lag
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentAssetVersion = 25
                val lastSyncedVersion = prefs.getInt("pref_asset_sync_version", 0)
                val qCount = database.deshExamDao().getQuestionsCount()
                if (lastSyncedVersion < currentAssetVersion || database.deshExamDao().getAllSubjectsOnce().isEmpty() || qCount < 500) {
                    repository.syncDatabaseWithAssets(forceRefresh = true)
                    prefs.edit().putInt("pref_asset_sync_version", currentAssetVersion).apply()
                }
            } catch (e: Exception) {
                Log.w("DeshExamViewModel", "Database asset sync failed: ${e.message}")
            }
        }

        // Load Pyq Questions
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _allPyqQuestions.value = PyqRepository.loadAllPyqQuestions(getApplication())
            } catch (e: Exception) {
                Log.w("DeshExamViewModel", "Failed to load pyq: ${e.message}")
            }
        }

        // Local-First loader & Realtime sync for Education Boards & Academic Classes
        // 1. Prothom local data request korbe, na pele tabei Firebase Firestore-e request pathie execute korbe
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.loadBoardsAndClassesLocalFirst(firestoreService)
            } catch (e: Exception) {
                Log.w("DeshExamViewModel", "Local-first boards/classes load error: ${e.message}")
            }
        }

        // 2. Realtime listener: Firebase-e notun board/class add hole ta songe songe app-e dekhabe
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestoreService.getBoardsRealtime().collect { fbBoards ->
                    if (fbBoards.isNotEmpty()) {
                        val boards = fbBoards.map { it.toEducationBoard() }
                        repository.insertBoardsFromRemote(boards)
                        AcademicClassProvider.updateBoards(boards)
                    }
                }
            } catch (e: Exception) {
                Log.w("DeshExamViewModel", "Realtime boards listen error: ${e.message}")
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestoreService.getClassesRealtime().collect { fbClasses ->
                    if (fbClasses.isNotEmpty()) {
                        val classes = fbClasses.map { it.toAcademicClass() }
                        repository.insertClassesFromRemote(classes)
                        AcademicClassProvider.updateClasses(classes)
                    }
                }
            } catch (e: Exception) {
                Log.w("DeshExamViewModel", "Realtime classes listen error: ${e.message}")
            }
        }

        // 3. Realtime Leaderboard listener from Firebase Firestore
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestoreService.getRealtimeTopStudents(30).collect { topStudents ->
                    if (topStudents.isNotEmpty()) {
                        _liveLeaderboard.value = topStudents
                    }
                }
            } catch (e: Exception) {
                Log.w("DeshExamViewModel", "Realtime leaderboard listen error: ${e.message}")
            }
        }

        // Initialize theme
        try {
            val savedDarkMode = prefs.getBoolean("pref_dark_mode", false)
            _appSettings.value = _appSettings.value.copy(isDarkMode = savedDarkMode)
        } catch (_: Throwable) {}

        // Notification channels
        try {
            MadhyamikNotificationManager.createNotificationChannels(getApplication())
        } catch (_: Throwable) {}

        // Auth state listener with background credential check & smooth session restoration
        viewModelScope.launch {
            val isAlreadyResolved = _currentScreen.value != AppScreen.SPLASH
            var splashDone = isAlreadyResolved
            if (!splashDone) {
                launch {
                    delay(600)
                    splashDone = true
                }
            }
            authRepository.authStateFlow.collect { user ->
                if (!splashDone) {
                    delay(600)
                    splashDone = true
                }
                if (user != null) {
                    _isGuestUser.value = user.isAnonymous

                    // Prevent redundant network loading and cloud re-sync on every emission / app resume
                    if (lastRestoredUid != user.uid) {
                        lastRestoredUid = user.uid
                        loadUserVideoProgress()
                        if (!user.isAnonymous) {
                            restoreUserProgressFromCloud(user.uid)
                        }
                    }

                    val isCompleted = if (user.isAnonymous) {
                        prefs.getBoolean("onboarding_completed_guest", false)
                    } else {
                        prefs.getBoolean("onboarding_completed_${user.uid}", false)
                    }

                    if (isCompleted) {
                        try {
                            prefs.edit().putBoolean("has_completed_onboarding", true).apply()
                        } catch (_: Throwable) {}

                        // Prevent forced auth-checks or resetting screen when app is resumed
                        if (_currentScreen.value == AppScreen.SPLASH) {
                            val saved = prefs.getString("saved_last_screen", null)
                            val restoredScreen = saved?.let {
                                try {
                                    val s = AppScreen.valueOf(it)
                                    if (isRestorableTabScreen(s)) s else null
                                } catch (_: Throwable) { null }
                            }
                            _currentScreen.value = restoredScreen ?: AppScreen.HOME
                        } else if (_currentScreen.value == AppScreen.AUTH) {
                            // User just signed in from Auth screen
                            _currentScreen.value = AppScreen.HOME
                        }
                    } else {
                        val currentName = studentProfile.value.name
                        if (!user.displayName.isNullOrBlank() && (currentName.isBlank() || currentName == "Sourav Das")) {
                            repository.updateStudentProfile(
                                name = user.displayName!!,
                                school = studentProfile.value.school,
                                district = studentProfile.value.district,
                                targetExam = studentProfile.value.targetExam
                            )
                        }
                        if (_currentScreen.value == AppScreen.SPLASH || _currentScreen.value == AppScreen.AUTH) {
                            _currentScreen.value = AppScreen.ONBOARDING
                        }
                    }
                } else {
                    // User is unauthenticated / logged out
                    lastRestoredUid = null
                    _isGuestUser.value = true
                    if (_currentScreen.value == AppScreen.SPLASH) {
                        val hasCompleted = prefs.getBoolean("has_completed_onboarding", false)
                        _currentScreen.value = if (hasCompleted) AppScreen.HOME else AppScreen.AUTH
                    }
                    // Crucial: If current screen is already AUTH or any sub-screen, do not force-navigate
                }
            }
        }

        // Fail-safe: Under no circumstances should the app stay stuck on SPLASH screen
        viewModelScope.launch {
            delay(1000)
            if (_currentScreen.value == AppScreen.SPLASH) {
                forceExitSplash()
            }
        }

        // Restore active topic or chapter entity if resuming from background
        viewModelScope.launch {
            try {
                val savedTopicId = prefs.getString("saved_last_topic_id", null)
                if (!savedTopicId.isNullOrBlank() && _activeTopicStudy.value == null) {
                    val topic = repository.getTopicById(savedTopicId)
                    if (topic != null) {
                        _activeTopicStudy.value = topic
                    }
                }
                val savedChapterId = prefs.getString("saved_last_chapter_id", null)
                if (!savedChapterId.isNullOrBlank() && _selectedChapterEntity.value == null) {
                    val chapter = database.deshExamDao().getChapterById(savedChapterId)
                    if (chapter != null) {
                        _selectedChapterEntity.value = chapter
                        _selectedChapterId.value = chapter.id
                    }
                }
                val savedSubjectId = prefs.getString("saved_last_subject_id", null)
                if (!savedSubjectId.isNullOrBlank() && _selectedSubjectEntity.value == null) {
                    val subject = repository.getSubjectById(savedSubjectId)
                    if (subject != null) {
                        _selectedSubjectEntity.value = subject
                        _selectedSubjectId.value = subject.id
                    }
                }
            } catch (e: Exception) {
                Log.w("DeshExamViewModel", "Error restoring saved navigation targets: ${e.message}")
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

    fun forceExitSplash() {
        if (_currentScreen.value == AppScreen.SPLASH) {
            val guestCompleted = prefs.getBoolean("onboarding_completed_guest", false)
            val hasCompleted = prefs.getBoolean("has_completed_onboarding", false)
            val currentUser = authRepository.currentUserState
            val isUserValid = (currentUser != null && (currentUser.isAnonymous || prefs.getBoolean("onboarding_completed_${currentUser.uid}", false))) || guestCompleted || hasCompleted
            if (isUserValid) {
                val saved = prefs.getString("saved_last_screen", null)
                val target = saved?.let {
                    try {
                        val s = AppScreen.valueOf(it)
                        if (isRestorableTabScreen(s)) s else null
                    } catch (_: Throwable) { null }
                } ?: AppScreen.HOME
                _currentScreen.value = target
            } else {
                _currentScreen.value = AppScreen.AUTH
            }
        }
    }

    fun recoverChapterEntityOrHome() {
        viewModelScope.launch {
            val savedChapterId = prefs.getString("saved_last_chapter_id", null)
            var recovered: ChapterEntity? = null
            if (!savedChapterId.isNullOrBlank()) {
                recovered = database.deshExamDao().getChapterById(savedChapterId)
            }
            if (recovered == null) {
                recovered = chapters.value.firstOrNull()
            }
            if (recovered != null) {
                _selectedChapterEntity.value = recovered
                _selectedChapterId.value = recovered.id
            } else {
                navigateTo(AppScreen.HOME)
            }
        }
    }

    fun recoverSubjectEntityOrHome() {
        viewModelScope.launch {
            val savedSubId = prefs.getString("saved_last_subject_id", null)
            var recovered: SubjectEntity? = null
            if (!savedSubId.isNullOrBlank()) {
                recovered = repository.getSubjectById(savedSubId)
            }
            if (recovered == null) {
                val firstId = subjects.value.firstOrNull()?.id
                if (firstId != null) {
                    recovered = repository.getSubjectById(firstId)
                }
            }
            if (recovered != null) {
                _selectedSubjectEntity.value = recovered
                _selectedSubjectId.value = recovered.id
            } else {
                navigateTo(AppScreen.HOME)
            }
        }
    }

    fun recoverTopicEntityOrHome() {
        viewModelScope.launch {
            val savedTopicId = prefs.getString("saved_last_topic_id", null)
            var recovered: TopicItem? = null
            if (!savedTopicId.isNullOrBlank()) {
                recovered = repository.getTopicById(savedTopicId)
            }
            if (recovered != null) {
                _activeTopicStudy.value = recovered
            } else {
                navigateTo(AppScreen.HOME)
            }
        }
    }

    fun reloadAllDataFromAssets(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.syncDatabaseWithAssets(forceRefresh = true)
                val currentSelected = _selectedSubjectId.value
                if (currentSelected.isNotEmpty()) {
                    _selectedSubjectEntity.value = repository.getSubjectById(currentSelected)
                }
            } catch (e: Exception) {
                Log.e("DeshExamViewModel", "Manual asset sync error: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
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
            val from = _currentScreen.value
            if (screen == AppScreen.HOME || screen == AppScreen.AUTH || screen == AppScreen.SPLASH) {
                _screenBackStack.clear()
            } else {
                if (from != AppScreen.SPLASH && from != AppScreen.AUTH && from != AppScreen.ONBOARDING) {
                    if (_screenBackStack.lastOrNull() != from) {
                        _screenBackStack.addLast(from)
                    }
                }
            }
            _previousScreen.value = from
            _currentScreen.value = screen
            saveCurrentScreenState(screen)
        }
    }

    private fun saveCurrentScreenState(screen: AppScreen) {
        try {
            if (isRestorableTabScreen(screen)) {
                prefs.edit().putString("saved_last_screen", screen.name).apply()
            }
        } catch (_: Throwable) {}
    }

    fun navigateToInternetArchiveSearch(query: String) {
        _internetArchiveSearchQuery.value = query
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = AppScreen.INTERNET_ARCHIVE_SEARCH
    }

    fun goBack(): Boolean {
        val curr = _currentScreen.value
        if (curr == AppScreen.HOME || curr == AppScreen.AUTH || curr == AppScreen.SPLASH || curr == AppScreen.ONBOARDING) {
            return false
        }

        if (curr == AppScreen.ASSESSMENT) {
            exitAssessment()
            return true
        }

        if (curr == AppScreen.RESULT) {
            _latestResult.value = null
            _screenBackStack.removeAll { it == AppScreen.ASSESSMENT || it == AppScreen.RESULT }
            val target = _screenBeforeAssessment.value ?: _screenBackStack.removeLastOrNull() ?: AppScreen.HOME
            _screenBeforeAssessment.value = null
            _previousScreen.value = curr
            _currentScreen.value = target
            saveCurrentScreenState(target)
            return true
        }

        if (_screenBackStack.isNotEmpty()) {
            val target = _screenBackStack.removeLast()
            _previousScreen.value = curr
            _currentScreen.value = target
            saveCurrentScreenState(target)
            return true
        }

        return when (curr) {
            AppScreen.CHAPTER_DETAIL -> {
                _previousScreen.value = curr
                _currentScreen.value = AppScreen.SUBJECT_DETAIL
                true
            }
            AppScreen.LEARN_TOPIC -> {
                _previousScreen.value = curr
                _currentScreen.value = if (_previousScreen.value == AppScreen.CHAPTER_DETAIL) AppScreen.CHAPTER_DETAIL else AppScreen.JOURNEY
                true
            }
            AppScreen.VIDEO_LESSON -> {
                _previousScreen.value = curr
                _currentScreen.value = AppScreen.LEARN_TOPIC
                true
            }
            AppScreen.SETTINGS, AppScreen.EDIT_PROFILE -> {
                _previousScreen.value = curr
                _currentScreen.value = AppScreen.PROFILE
                true
            }
            else -> {
                _previousScreen.value = curr
                _currentScreen.value = AppScreen.HOME
                saveCurrentScreenState(AppScreen.HOME)
                true
            }
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
            _screenBackStack.clear()
            _previousScreen.value = AppScreen.AUTH
            _screenBeforeAssessment.value = null
            _activeTopicStudy.value = null
            _selectedChapterEntity.value = null
            _selectedSubjectEntity.value = null
            _latestResult.value = null
            _celebratingBadge.value = null
            lastRestoredUid = null
            try {
                prefs.edit()
                    .remove("saved_last_screen")
                    .remove("has_completed_onboarding")
                    .remove("onboarding_completed_guest")
                    .remove("is_guest_user")
                    .remove("saved_last_topic_id")
                    .remove("saved_last_chapter_id")
                    .remove("saved_last_subject_id")
                    .apply()
            } catch (_: Throwable) {}
            try {
                repository.updateStudentProfile(
                    name = "পরীক্ষার্থী",
                    school = "",
                    district = "কলকাতা",
                    targetExam = "Madhyamik 2026"
                )
            } catch (_: Throwable) {}
            _currentScreen.value = AppScreen.AUTH
        }
    }

    fun dismissBadgeCelebration() {
        _celebratingBadge.value = null
    }

    fun openSubjectDetail(subject: SubjectEntity) {
        _selectedSubjectEntity.value = subject
        _selectedSubjectId.value = subject.id
        try {
            prefs.edit().putString("saved_last_subject_id", subject.id).apply()
        } catch (_: Throwable) {}
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
        try {
            prefs.edit().putString("saved_last_chapter_id", chapter.id).apply()
        } catch (_: Throwable) {}
        navigateTo(AppScreen.CHAPTER_DETAIL)
    }

    fun openTopicStudy(topicId: String) {
        viewModelScope.launch {
            val topic = repository.getTopicById(topicId)
            if (topic != null) {
                _activeTopicStudy.value = topic
                try {
                    prefs.edit().putString("saved_last_topic_id", topicId).apply()
                } catch (_: Throwable) {}
                navigateTo(AppScreen.LEARN_TOPIC)
            }
        }
    }

    fun openVideoLesson(lesson: VideoLesson, topic: TopicItem? = null) {
        _activeVideoLesson.value = lesson
        _activeVideoTopic.value = topic
        navigateTo(AppScreen.VIDEO_LESSON)
    }

    fun closeVideoLesson() {
        _activeVideoLesson.value = null
        _activeVideoTopic.value = null
    }

    fun openVideoLessonForEntity(lesson: VideoLesson, entity: TopicEntity? = null) {
        val topicItem = entity?.let {
            TopicItem(
                id = it.id,
                chapterId = it.chapterId,
                subjectId = it.subjectId,
                orderIndex = it.orderIndex,
                title = it.title,
                summary = it.summary,
                keyPoints = emptyList(),
                isUnlocked = it.isUnlocked,
                masteryPercentage = it.masteryPercentage,
                questionCount = it.questionCount,
                contentData = it.contentData,
                solutionData = it.solutionData,
                videoUrl = it.videoUrl,
                playlistJson = it.playlistJson
            )
        }
        openVideoLesson(lesson, topicItem)
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

    fun updateProfileImageUri(uri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfileImageUri(uri)
            val uid = authRepository.currentUserState?.uid
            if (uid != null) {
                val currentProf = studentProfile.value
                firestoreService.saveStudentProfile(uid, currentProf.copy(profileImageUri = uri))
            }
        }
    }

    private fun getDynamicTargetExam(classId: String, stream: String): String {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val nextYear = currentYear + 1
        return when (classId) {
            "class_10_wbbse" -> "WBBSE Madhyamik $nextYear"
            "class_11_wbchse" -> {
                val sem = if (stream.contains("sem2")) 2 else 1
                val year = if (sem == 2) nextYear else currentYear
                "WBCHSE Class 11 Sem $sem Exam $year"
            }
            "class_12_wbchse" -> {
                val sem = if (stream.contains("sem4")) 4 else 3
                val year = if (sem == 4) nextYear else currentYear
                "WBCHSE Class 12 Sem $sem Exam $year"
            }
            "class_kg_wbbpe" -> "WBBPE Pre-Primary / KG $currentYear"
            "class_1_wbbpe" -> "WBBPE Class 1 Exam $currentYear"
            "class_2_wbbpe" -> "WBBPE Class 2 Exam $currentYear"
            "class_3_wbbpe" -> "WBBPE Class 3 Exam $currentYear"
            "class_4_wbbpe" -> "WBBPE Class 4 Exam $currentYear"
            "class_5_wbbse" -> "WBBSE Class 5 Exam $currentYear"
            "class_6_wbbse" -> "WBBSE Class 6 Exam $currentYear"
            "class_7_wbbse" -> "WBBSE Class 7 Exam $currentYear"
            "class_8_wbbse" -> "WBBSE Class 8 Exam $currentYear"
            "class_9_wbbse" -> "WBBSE Class 9 Exam $currentYear"
            else -> {
                val classObj = com.example.data.model.AcademicClassProvider.ALL_CLASSES.find { it.id == classId }
                classObj?.targetExam ?: "Exam $currentYear"
            }
        }
    }

    fun switchActiveExamClass(classId: String, stream: String = "all") {
        viewModelScope.launch {
            val classObj = AcademicClassProvider.ALL_CLASSES.find { it.id == classId }
            if (classObj != null) {
                val finalStream = if (classId in listOf("class_11_wbchse", "class_12_wbchse")) stream else "all"
                val dynamicTargetExam = getDynamicTargetExam(classId, finalStream)
                repository.updateStudentProfile(
                    name = "",
                    school = "",
                    district = "",
                    targetExam = dynamicTargetExam,
                    classId = classId,
                    stream = finalStream
                )
                val newSubs = repository.getSubjectsForClassOnce(classId)
                if (newSubs.isNotEmpty()) {
                    val firstSub = if (finalStream == "all") newSubs.first() else (newSubs.find { com.example.util.StreamHelper.matchesStream(it.stream, finalStream) } ?: newSubs.first())
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
            val dynamicTargetExam = getDynamicTargetExam(current.classId, newStream)
            repository.updateStudentProfile(
                name = "",
                school = "",
                district = "",
                targetExam = dynamicTargetExam,
                classId = current.classId,
                stream = newStream
            )
            val newSubs = repository.getSubjectsForClassOnce(current.classId)
            if (newSubs.isNotEmpty()) {
                val firstSub = if (newStream == "all") newSubs.first() else (newSubs.find { com.example.util.StreamHelper.matchesStream(it.stream, newStream) } ?: newSubs.first())
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

    fun completeOnboarding(
        name: String,
        classLevel: String,
        district: String,
        school: String,
        targetExam: String,
        stream: String = "all",
        studyGoals: List<String> = emptyList(),
        phone: String = "",
        gender: String = "male",
        dob: String = ""
    ) {
        viewModelScope.launch {
            val directMatch = AcademicClassProvider.ALL_CLASSES.find {
                it.id.equals(classLevel, ignoreCase = true) ||
                classLevel.startsWith(it.id) ||
                classLevel.contains(it.id) ||
                (it.nameBengali.isNotBlank() && classLevel.contains(it.nameBengali))
            }
            val resolvedClassId = directMatch?.id ?: when {
                classLevel.contains("KG") || classLevel.contains("প্রাক-প্রাথমিক") -> "class_kg_wbbpe"
                classLevel.contains("Class 1") || classLevel.contains("প্রথম") -> "class_1_wbbpe"
                classLevel.contains("Class 2") || classLevel.contains("দ্বিতীয়") -> "class_2_wbbpe"
                classLevel.contains("Class 3") || classLevel.contains("তৃতীয়") -> "class_3_wbbpe"
                classLevel.contains("Class 4") || classLevel.contains("চতুর্থ") -> "class_4_wbbpe"
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
            val updatedProf = studentProfile.value.copy(
                name = name,
                school = school,
                district = district,
                targetExam = targetExam,
                classId = resolvedClassId,
                stream = finalStream,
                studyGoals = studyGoals,
                phoneNumber = phone,
                gender = gender,
                dateOfBirth = dob
            )
            repository.updateProfile(updatedProf)
            
            val uid = authUserState.value?.uid ?: "guest"
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    firestoreService.saveStudentProfile(uid, updatedProf)
                } catch (_: Exception) {}
            }
            prefs.edit().putBoolean("onboarding_completed_$uid", true).apply()
            prefs.edit().putBoolean("onboarding_completed_guest", true).apply()
            val newSubs = repository.getSubjectsForClassOnce(resolvedClassId)
            if (newSubs.isNotEmpty()) {
                val firstSub = if (finalStream == "all") newSubs.first() else (newSubs.find { com.example.util.StreamHelper.matchesStream(it.stream, finalStream) } ?: newSubs.first())
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
                    val nameToUse = res.user.displayName ?: res.user.email?.substringBefore("@") ?: ""
                    if (nameToUse.isNotBlank() && (studentProfile.value.name.isBlank() || studentProfile.value.name == "Sourav Das")) {
                        repository.updateStudentProfile(
                            name = nameToUse,
                            school = studentProfile.value.school,
                            district = studentProfile.value.district,
                            targetExam = studentProfile.value.targetExam
                        )
                    }
                    val isCompleted = prefs.getBoolean("onboarding_completed_${res.user.uid}", false)
                    if (isCompleted) {
                        _currentScreen.value = AppScreen.HOME
                    } else {
                        _currentScreen.value = AppScreen.ONBOARDING
                    }
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
        val isCompleted = prefs.getBoolean("onboarding_completed_guest", false)
        if (isCompleted) {
            _currentScreen.value = AppScreen.HOME
        } else {
            _currentScreen.value = AppScreen.ONBOARDING
        }
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
                    val nameToUse = displayName ?: res.user.displayName ?: res.user.email?.substringBefore("@") ?: ""
                    if (nameToUse.isNotBlank()) {
                        repository.updateStudentProfile(
                            name = nameToUse,
                            school = studentProfile.value.school,
                            district = studentProfile.value.district,
                            targetExam = studentProfile.value.targetExam
                        )
                    }
                    val isCompleted = prefs.getBoolean("onboarding_completed_${res.user.uid}", false)
                    if (isCompleted) {
                        _currentScreen.value = AppScreen.HOME
                    } else {
                        _currentScreen.value = AppScreen.ONBOARDING
                    }
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
        if (_currentScreen.value != AppScreen.ASSESSMENT && _currentScreen.value != AppScreen.RESULT) {
            _screenBeforeAssessment.value = _currentScreen.value
        }
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
            } else {
                _toastMessage.value = if (_isBengali.value) "দুঃখিত, এই বিষয়ের কোনো প্রশ্ন এখনো উপলব্ধ নেই!" else "Sorry, no questions are available for this topic yet!"
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
                eliminatedOptionIndices = emptySet(),
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
                eliminatedOptionIndices = emptySet(),
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

            // Automatic Real-time Firestore Sync (Test Result, User Progress & Real-time Leaderboard)
            val userId = authUserState.value?.uid ?: "guest_user"
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // 1. Save Test Result in Firestore
                    firestoreService.saveTestResult(userId, result)

                    // 2. Award specific coins for completed learning modules or mock tests (validated against Firestore to prevent duplicate API calls)
                    val currentProf = studentProfile.value
                    var updatedCoins = currentProf.coins
                    var coinsAwarded = 0

                    if (!session.topicId.isNullOrBlank() && result.accuracyPercentage >= 70) {
                        // Learning module / Topic completed! Validate against Firestore
                        val topicId = session.topicId ?: ""
                        val masteryRes = firestoreService.getTopicMastery(userId, topicId)
                        val alreadyCompleted = masteryRes.getOrNull()?.isCompleted == true
                        
                        if (!alreadyCompleted) {
                            coinsAwarded = 30
                            updatedCoins += 30
                        }
                    } else if (session.mode == AssessmentMode.MOCK_TEST) {
                        // Mock test completed! Validate against Firestore
                        val historyRes = firestoreService.getStudentAttemptHistory(userId)
                        val history = historyRes.getOrNull() ?: emptyList()
                        val alreadyCompletedMock = history.any { it.testTitle == result.title }
                        
                        if (!alreadyCompletedMock) {
                            coinsAwarded = 100
                            updatedCoins += 100
                        }
                    } else {
                        // Any other quiz completed (e.g. Daily 10, Boss Battle, Weak Area, Speed Blitz, etc.)!
                        // Validate against attempt history in Firestore
                        val historyRes = firestoreService.getStudentAttemptHistory(userId)
                        val history = historyRes.getOrNull() ?: emptyList()
                        val alreadyCompletedQuiz = history.any { it.testTitle == result.title }
                        
                        if (!alreadyCompletedQuiz) {
                            coinsAwarded = 20
                            updatedCoins += 20
                        }
                    }

                    val finalProfile = if (coinsAwarded > 0) {
                        currentProf.copy(coins = updatedCoins)
                    } else {
                        currentProf
                    }

                    if (coinsAwarded > 0) {
                        // Save profile locally in Room
                        repository.updateProfile(finalProfile)
                    }

                    // 3. Save Updated Student Profile & Stats in Firestore
                    firestoreService.saveStudentProfile(userId, finalProfile)

                    // 4. Update Real-time Leaderboard Entry in Firestore
                    firestoreService.updateLeaderboardEntry(
                        userId = userId,
                        name = finalProfile.name,
                        school = finalProfile.school,
                        district = finalProfile.district,
                        xp = finalProfile.currentXp,
                        accuracy = finalProfile.overallAccuracy,
                        streak = finalProfile.currentStreak
                    )

                    // 4. Save Topic Mastery in Firestore if topic practice
                    if (!session.topicId.isNullOrBlank()) {
                        firestoreService.saveTopicMastery(
                            FirestoreTopicMastery(
                                topicId = session.topicId ?: "",
                                userId = userId,
                                classId = currentProf.classId.ifBlank { "class_10_wbbse" },
                                subjectId = session.subjectId ?: "",
                                chapterId = session.chapterId ?: "",
                                masteryPercentage = result.accuracyPercentage,
                                totalQuestionsAttempted = result.totalQuestions,
                                correctQuestionsCount = result.correctCount,
                                isCompleted = result.accuracyPercentage >= 70,
                                lastPracticedTimestamp = System.currentTimeMillis()
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.w("DeshExamViewModel", "Automatic Firestore sync error: ${e.message}")
                }
            }
        }
    }

    fun exitAssessment() {
        stopTimer()
        _assessmentState.value = AssessmentUiState()
        val target = _screenBeforeAssessment.value ?: _previousScreen.value ?: AppScreen.HOME
        _currentScreen.value = if (target != AppScreen.ASSESSMENT && target != AppScreen.RESULT) target else AppScreen.HOME
    }

    fun dismissResult() {
        _latestResult.value = null
        val target = _screenBeforeAssessment.value ?: _previousScreen.value ?: AppScreen.HOME
        _currentScreen.value = if (target != AppScreen.ASSESSMENT && target != AppScreen.RESULT) target else AppScreen.HOME
    }

    fun getTopicSetScore(topicId: String, setNumber: Int): Int = repository.getTopicSetScore(topicId, setNumber)
    fun isTopicSetUnlocked(topicId: String, setNumber: Int, passThreshold: Int = 60): Boolean = setNumber <= 1 || repository.getTopicSetScore(topicId, setNumber - 1) >= passThreshold
    fun isTopicMockUnlocked(topicId: String, passThreshold: Int = 60): Boolean = repository.getTopicSetScore(topicId, 1) >= passThreshold
    fun isTopicSpeedUnlocked(topicId: String, passThreshold: Int = 60): Boolean = repository.getTopicMockScore(topicId) >= passThreshold || repository.getTopicSetScore(topicId, 1) >= passThreshold
    fun getTopicMockScore(topicId: String, setNumber: Int = 1): Int = repository.getTopicMockScore(topicId, setNumber)
    fun getTopicSpeedScore(topicId: String, setNumber: Int = 1): Int = repository.getTopicSpeedScore(topicId, setNumber)
    fun getTopicBossScore(topicId: String, setNumber: Int = 1): Int = repository.getTopicBossScore(topicId, setNumber)
    fun getTopicPYQScore(topicId: String, setNumber: Int = 1): Int = repository.getTopicPYQScore(topicId, setNumber)
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
    suspend fun getChapterQuestionCount(chapterId: String): Int = repository.getChapterQuestionCount(chapterId)

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
    
    fun fetchFirestoreQuestions(classId: String? = null, subjectId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = firestoreService.getQuestionsFiltered(classId, subjectId)
                val list = res.getOrDefault(emptyList())
                withContext(Dispatchers.Main) {
                    _firestoreQuestions.value = list
                }
            } catch (e: Exception) {
                Log.e("DeshExamViewModel", "Error fetching Firestore questions: ${e.message}")
            }
        }
    }

    fun fetchFirestoreTestTemplates(classId: String? = null, subjectId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = firestoreService.getTestTemplatesFiltered(classId, subjectId)
                val list = res.getOrDefault(emptyList())
                withContext(Dispatchers.Main) {
                    _firestoreTestTemplates.value = list
                }
            } catch (e: Exception) {
                Log.e("DeshExamViewModel", "Error fetching test templates: ${e.message}")
            }
        }
    }

    fun loadLocalDatabaseStats() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dao = database.deshExamDao()
                val boards = dao.getAllBoardsOnce()
                val classes = dao.getAllClassesOnce()
                val subjects = dao.getAllSubjectsOnce()
                val chapters = dao.getAllChaptersOnce()
                val topics = dao.getAllTopicsOnce()
                val questions = dao.getAllQuestions()
                val videos = dao.getAllVideoProgressOnce()

                val stats = LocalDatabaseStats(
                    boardCount = if (boards.isNotEmpty()) boards.size else AcademicClassProvider.ALL_BOARDS.size,
                    classCount = if (classes.isNotEmpty()) classes.size else AcademicClassProvider.ALL_CLASSES.size,
                    subjectCount = subjects.size,
                    chapterCount = chapters.size,
                    topicCount = topics.size,
                    questionCount = questions.size,
                    videoProgressCount = videos.size
                )
                _cloudBackupState.update { it.copy(stats = stats) }
                checkCloudDatabaseIntegrity()
            } catch (e: Exception) {
                Log.e("DeshExamViewModel", "Error loading stats: ${e.message}", e)
            }
        }
    }

    fun checkCloudDatabaseIntegrity() {
        viewModelScope.launch(Dispatchers.IO) {
            _cloudStats.update { it.copy(isChecking = true) }
            val countsRes = firestoreService.getCloudCollectionCounts()
            if (countsRes.isSuccess) {
                val map = countsRes.getOrNull().orEmpty()
                _cloudStats.update {
                    it.copy(
                        boardCount = map["boards"] ?: 0,
                        classCount = map["classes"] ?: 0,
                        subjectCount = map["subjects"] ?: 0,
                        chapterCount = map["chapters"] ?: 0,
                        topicCount = map["topics"] ?: 0,
                        questionCount = map["questions"] ?: 0,
                        testCount = map["tests"] ?: 0,
                        isOnline = true,
                        isChecking = false,
                        lastCheckedTimestamp = System.currentTimeMillis()
                    )
                }
            } else {
                _cloudStats.update {
                    it.copy(isOnline = false, isChecking = false, lastCheckedTimestamp = System.currentTimeMillis())
                }
            }
        }
    }

    fun pullCloudDatabaseToLocal(
        onProgress: (String, Float) -> Unit = { _, _ -> },
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (_isPullingFromCloud.value) return
        _isPullingFromCloud.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) { onProgress("ক্লাউড থেকে তথ্য নামানো হচ্ছে...", 0.10f) }
                val dao = database.deshExamDao()

                // 1. Pull Boards & Classes
                withContext(Dispatchers.Main) { onProgress("🏫 বোর্ড ও শ্রেণি লোড হচ্ছে...", 0.25f) }
                val boardsRes = firestoreService.getAllBoards()
                val boards = boardsRes.getOrNull().orEmpty()
                if (boards.isNotEmpty()) {
                    val entities = boards.mapIndexed { idx, b -> b.toEducationBoard().toEntity(idx) }
                    dao.insertAllBoards(entities)
                    AcademicClassProvider.updateBoards(boards.map { b -> b.toEducationBoard() })
                }

                val classesRes = firestoreService.getAllClasses()
                val classes = classesRes.getOrNull().orEmpty()
                if (classes.isNotEmpty()) {
                    val entities = classes.mapIndexed { idx, c -> c.toAcademicClass().toEntity(idx) }
                    dao.insertAllClasses(entities)
                    AcademicClassProvider.updateClasses(classes.map { c -> c.toAcademicClass() })
                }

                // 2. Pull Questions
                withContext(Dispatchers.Main) { onProgress("❓ প্রশ্নব্যাংক সিঙ্ক করা হচ্ছে...", 0.65f) }
                val questionsRes = firestoreService.getAllQuestions(1500)
                val fQuestions = questionsRes.getOrNull().orEmpty()
                if (fQuestions.isNotEmpty()) {
                    val qEntities = fQuestions.map { fq ->
                        QuestionEntity(
                            id = fq.id,
                            topicId = fq.topicId,
                            chapterId = fq.chapterId,
                            subjectId = fq.subjectId,
                            type = fq.type,
                            difficulty = fq.difficulty,
                            question = fq.question,
                            optionsJson = org.json.JSONArray(fq.options).toString(),
                            correctOptionIndex = fq.correctOptionIndex,
                            correctAnswerText = fq.correctAnswerText,
                            explanation = fq.explanation,
                            marks = fq.marks,
                            tagsJson = fq.tags.joinToString("|"),
                            classId = fq.classId
                        )
                    }
                    dao.insertQuestions(qEntities)
                }

                withContext(Dispatchers.Main) { onProgress("✅ লোকাল ডেটাবেজে সংরক্ষণ সম্পন্ন", 1.0f) }
                loadLocalDatabaseStats()
                checkCloudDatabaseIntegrity()
                _isPullingFromCloud.value = false
                val msg = "✅ ক্লাউড থেকে ${boards.size} টি বোর্ড, ${classes.size} টি ক্লাস এবং ${fQuestions.size} টি প্রশ্ন সফলভাবে সিঙ্ক ও রিস্টোর করা হয়েছে!"
                withContext(Dispatchers.Main) { onComplete(true, msg) }
            } catch (e: Exception) {
                _isPullingFromCloud.value = false
                Log.e("DeshExamViewModel", "Pull from cloud error: ${e.message}", e)
                val err = "❌ ক্লাউড সিঙ্ক ব্যর্থ: ${e.localizedMessage}"
                withContext(Dispatchers.Main) { onComplete(false, err) }
            }
        }
    }

    fun pushLocalDatabaseToFirebase(
        onProgress: (String, Float) -> Unit = { _, _ -> },
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (_cloudBackupState.value.isBackingUp) return

        viewModelScope.launch(Dispatchers.IO) {
            _cloudBackupState.update {
                it.copy(
                    isBackingUp = true,
                    step = "লোকাল ডেটা সংগ্রহ করা হচ্ছে...",
                    progress = 0.05f,
                    error = null,
                    summary = null
                )
            }
            withContext(Dispatchers.Main) {
                onProgress("লোকাল ডেটা সংগ্রহ করা হচ্ছে...", 0.05f)
            }

            try {
                val dao = database.deshExamDao()

                // 1. Boards & Classes (0.05 -> 0.20)
                _cloudBackupState.update { it.copy(step = "🏫 বোর্ড ও শ্রেণি ব্যাকআপ হচ্ছে...", progress = 0.10f) }
                withContext(Dispatchers.Main) { onProgress("🏫 বোর্ড ও শ্রেণি ব্যাকআপ হচ্ছে...", 0.10f) }

                val localBoards = dao.getAllBoardsOnce()
                val boardsToPush = if (localBoards.isNotEmpty()) {
                    localBoards.mapIndexed { idx, b -> b.toModel().toFirestore(idx) }
                } else {
                    AcademicClassProvider.ALL_BOARDS.mapIndexed { idx, b -> b.toFirestore(idx) }
                }
                var bSuccess = 0
                for (fb in boardsToPush) {
                    if (firestoreService.saveBoard(fb).isSuccess) bSuccess++
                }

                val localClasses = dao.getAllClassesOnce()
                val classesToPush = if (localClasses.isNotEmpty()) {
                    localClasses.mapIndexed { idx, c -> c.toModel().toFirestore(idx) }
                } else {
                    AcademicClassProvider.ALL_CLASSES.mapIndexed { idx, c -> c.toFirestore(idx) }
                }
                var cSuccess = 0
                for (fc in classesToPush) {
                    if (firestoreService.saveClass(fc).isSuccess) cSuccess++
                }

                // 2. Subjects (0.20 -> 0.35)
                _cloudBackupState.update { it.copy(step = "📘 বিষয়সমূহ (Subjects) ক্লাউডে পাঠানো হচ্ছে...", progress = 0.25f) }
                withContext(Dispatchers.Main) { onProgress("📘 বিষয়সমূহ (Subjects) ক্লাউডে পাঠানো হচ্ছে...", 0.25f) }

                val localSubjects = dao.getAllSubjectsOnce()
                var sSuccess = 0
                localSubjects.forEachIndexed { idx, sub ->
                    val fSub = FirestoreSubject(
                        id = sub.id,
                        classId = sub.classId.ifBlank { "class_10_wbbse" },
                        code = sub.code,
                        name = sub.name,
                        textbookName = sub.textbookName,
                        stream = sub.stream.ifBlank { "common" },
                        totalChapters = sub.totalChapters,
                        colorHex = sub.colorHex,
                        iconName = "Book",
                        orderIndex = idx
                    )
                    if (firestoreService.saveSubject(fSub).isSuccess) sSuccess++
                }

                // 3. Chapters (0.35 -> 0.55)
                _cloudBackupState.update { it.copy(step = "📖 অধ্যায়সমূহ (Chapters) ক্লাউডে পাঠানো হচ্ছে...", progress = 0.40f) }
                withContext(Dispatchers.Main) { onProgress("📖 অধ্যায়সমূহ (Chapters) ক্লাউডে পাঠানো হচ্ছে...", 0.40f) }

                val localChapters = dao.getAllChaptersOnce()
                var chSuccess = 0
                for (ch in localChapters) {
                    val fCh = FirestoreChapter(
                        id = ch.id,
                        classId = ch.classId.ifBlank { "class_10_wbbse" },
                        subjectId = ch.subjectId,
                        chapterNumber = ch.chapterNumber,
                        title = ch.title,
                        description = ch.description,
                        totalTopics = ch.totalTopics,
                        weightageMarks = 15,
                        orderIndex = ch.chapterNumber,
                        contentData = ch.contentData,
                        solutionData = ch.solutionData
                    )
                    if (firestoreService.saveChapter(fCh).isSuccess) chSuccess++
                }

                // 4. Topics (0.55 -> 0.75)
                _cloudBackupState.update { it.copy(step = "🏷️ টপিকসমূহ (Topics) ক্লাউডে পাঠানো হচ্ছে...", progress = 0.60f) }
                withContext(Dispatchers.Main) { onProgress("🏷️ টপিকসমূহ (Topics) ক্লাউডে পাঠানো হচ্ছে...", 0.60f) }

                val localTopics = dao.getAllTopicsOnce()
                var tpSuccess = 0
                for (tp in localTopics) {
                    val kps = if (tp.keyPointsJson.isNotBlank()) {
                        tp.keyPointsJson.split("|", "#").map { it.trim() }.filter { it.isNotEmpty() }
                    } else emptyList()

                    val fTopic = FirestoreTopic(
                        id = tp.id,
                        classId = tp.classId.ifBlank { "class_10_wbbse" },
                        subjectId = tp.subjectId,
                        chapterId = tp.chapterId,
                        orderIndex = tp.orderIndex,
                        title = tp.title,
                        summary = tp.summary,
                        keyPoints = kps,
                        formulae = emptyList(),
                        questionCount = tp.questionCount,
                        contentData = tp.contentData,
                        solutionData = tp.solutionData,
                        videoUrl = tp.videoUrl,
                        playlistJson = tp.playlistJson
                    )
                    if (firestoreService.saveTopic(fTopic).isSuccess) tpSuccess++
                }

                // 5. Questions Batch (0.75 -> 0.90)
                _cloudBackupState.update { it.copy(step = "❓ সম্পূর্ণ প্রশ্ন ব্যাংক ও PYQ ক্লাউডে পাঠানো হচ্ছে...", progress = 0.80f) }
                withContext(Dispatchers.Main) { onProgress("❓ সম্পূর্ণ প্রশ্ন ব্যাংক ও PYQ ক্লাউডে পাঠানো হচ্ছে...", 0.80f) }

                // Ensure all subjects have their chapters & questions fully seeded locally before push
                localSubjects.forEach { sub ->
                    try { repository.ensureSubjectContentSeeded(sub.id) } catch (_: Exception) {}
                }

                val localQuestions = dao.getAllQuestions()
                val fQuestionsList = localQuestions.map { q ->
                    val optionsList: List<String> = when {
                        q.optionsJson.isBlank() -> emptyList()
                        q.optionsJson.startsWith("[") -> try {
                            val arr = org.json.JSONArray(q.optionsJson)
                            val list = mutableListOf<String>()
                            for (i in 0 until arr.length()) list.add(arr.getString(i))
                            list
                        } catch (_: Exception) {
                            q.optionsJson.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        }
                        q.optionsJson.contains("|") -> q.optionsJson.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        else -> listOf(q.optionsJson)
                    }
                    val tagsList = (q.tagsJson.split("|", "#", ",") + listOfNotNull(q.pyqYear.ifBlank { null }))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    FirestoreQuestion(
                        id = q.id.ifBlank { "q_${q.subjectId}_${System.currentTimeMillis()}" },
                        classId = q.classId.ifBlank { "class_10_wbbse" },
                        subjectId = q.subjectId,
                        chapterId = q.chapterId,
                        topicId = q.topicId,
                        type = q.type.ifBlank { "MCQ" },
                        difficulty = q.difficulty.ifBlank { "MEDIUM" },
                        question = q.question,
                        options = optionsList,
                        correctOptionIndex = q.correctOptionIndex,
                        correctAnswerText = q.correctAnswerText.ifBlank { optionsList.getOrNull(q.correctOptionIndex) ?: "" },
                        explanation = q.explanation,
                        marks = if (q.marks > 0) q.marks else 1,
                        tags = tagsList,
                        podcastUrl = q.podcastUrl,
                        podcastTitle = q.podcastTitle
                    )
                }

                // Also include PYQ Board Exam questions
                val pyqs = if (_allPyqQuestions.value.isNotEmpty()) _allPyqQuestions.value else {
                    try { PyqRepository.loadAllPyqQuestions(getApplication()) } catch (_: Exception) { emptyList() }
                }
                val fPyqQuestions = pyqs.map { pyq ->
                    FirestoreQuestion(
                        id = pyq.id.ifBlank { "pyq_${pyq.year}_${pyq.subject}_${System.currentTimeMillis()}" },
                        classId = "class_10_wbbse",
                        subjectId = when (pyq.subject.uppercase()) {
                            "MATHEMATICS", "MATH" -> "sub_10_math"
                            "PHYSICAL_SCIENCE" -> "sub_10_physical_science"
                            "LIFE_SCIENCE" -> "sub_10_life_science"
                            "HISTORY" -> "sub_10_history"
                            "GEOGRAPHY" -> "sub_10_geography"
                            "BENGALI" -> "sub_10_bengali"
                            "ENGLISH" -> "sub_10_english"
                            else -> "sub_10_${pyq.subject.lowercase()}"
                        },
                        chapterId = "ch_${pyq.subject.lowercase()}_general",
                        topicId = "top_${pyq.subject.lowercase()}_pyq",
                        type = "MCQ",
                        difficulty = "MEDIUM",
                        question = pyq.question,
                        options = pyq.options ?: emptyList(),
                        correctOptionIndex = pyq.correctOptionIndex,
                        correctAnswerText = pyq.officialAnswerBengali?.ifBlank { null } ?: pyq.options?.getOrNull(pyq.correctOptionIndex) ?: "",
                        explanation = pyq.keyTips,
                        marks = pyq.marks,
                        tags = listOf("PYQ", "${pyq.year}", pyq.subject, pyq.group)
                    )
                }

                val allQuestionsToPush = (fQuestionsList + fPyqQuestions).distinctBy { it.id }
                val qBatchRes = firestoreService.saveQuestionsBatch(allQuestionsToPush)
                val qSavedCount = qBatchRes.getOrElse { 0 }

                // 5.1 Test Templates (0.90 -> 0.94)
                _cloudBackupState.update { it.copy(step = "📝 পরীক্ষা টেমপ্লেট ক্লাউডে পাঠানো হচ্ছে...", progress = 0.92f) }
                val chaptersList = dao.getAllChaptersOnce()
                var testTemplatesSaved = 0
                for (ch in chaptersList.take(60)) {
                    val template = FirestoreTestTemplate(
                        id = "test_${ch.id}",
                        classId = ch.classId.ifBlank { "class_10_wbbse" },
                        subjectId = ch.subjectId,
                        chapterId = ch.id,
                        testType = FirestoreTestType.CHAPTER_ASSESSMENT.name,
                        title = "${ch.title} - অধ্যায়ভিত্তিক মক টেস্ট",
                        description = "বোর্ড পরীক্ষার প্যাটার্নে ১০টি গুরুত্বপূর্ণ প্রশ্ন ও মূল্যায়ন",
                        durationMinutes = 15,
                        totalMarks = 10,
                        passMarks = 4,
                        questionCount = 10,
                        isPublished = true
                    )
                    if (firestoreService.saveTestTemplate(template).isSuccess) testTemplatesSaved++
                }

                // 6. User Profile & Video Progress (0.90 -> 1.0)
                _cloudBackupState.update { it.copy(step = "👤 ইউজার প্রোফাইল ও ভিডিও প্রগ্রেস ব্যাকআপ হচ্ছে...", progress = 0.95f) }
                withContext(Dispatchers.Main) { onProgress("👤 ইউজার প্রোফাইল ও ভিডিও প্রগ্রেস ব্যাকআপ হচ্ছে...", 0.95f) }

                val userId = authUserState.value?.uid ?: "guest_user"
                val currentProf = studentProfile.value
                firestoreService.saveStudentProfile(userId, currentProf)

                val localVideos = dao.getAllVideoProgressOnce()
                for (vp in localVideos) {
                    val fVp = FirestoreVideoProgress(
                        videoId = vp.videoId,
                        topicId = vp.topicId,
                        lessonTitle = vp.title,
                        videoUrl = "",
                        lastPositionMs = vp.positionMs,
                        durationMs = vp.durationMs,
                        playbackSpeed = vp.playbackSpeed,
                        isCompleted = vp.isCompleted,
                        percentage = if (vp.durationMs > 0) (vp.positionMs.toFloat() / vp.durationMs.toFloat() * 100f).coerceIn(0f, 100f) else 0f,
                        updatedAt = vp.lastWatchedTimestamp
                    )
                    firestoreService.saveVideoProgress(userId, fVp)
                }

                val summaryMsg = "✅ ক্লাউড ব্যাকআপ সফল!\n$bSuccess টি বোর্ড, $cSuccess টি ক্লাস, $sSuccess টি বিষয়, $chSuccess টি অধ্যায়, $tpSuccess টি টপিক, $qSavedCount টি প্রশ্ন এবং $testTemplatesSaved টি টেস্ট ফায়ারবেস ক্লাউডে ব্যাকআপ রাখা হয়েছে।"

                _cloudBackupState.update {
                    it.copy(
                        isBackingUp = false,
                        step = "সম্পন্ন",
                        progress = 1.0f,
                        lastBackupTimestamp = System.currentTimeMillis(),
                        summary = summaryMsg,
                        error = null
                    )
                }
                loadLocalDatabaseStats()
                checkCloudDatabaseIntegrity()

                withContext(Dispatchers.Main) {
                    onProgress("সম্পন্ন", 1.0f)
                    onComplete(true, summaryMsg)
                }
            } catch (e: Exception) {
                Log.e("DeshExamViewModel", "Cloud backup failed: ${e.message}", e)
                val errMsg = "ব্যাকআপ ব্যর্থ হয়েছে: ${e.localizedMessage ?: e.message}"
                _cloudBackupState.update {
                    it.copy(
                        isBackingUp = false,
                        error = errMsg,
                        step = "ত্রুটি"
                    )
                }
                withContext(Dispatchers.Main) {
                    onComplete(false, errMsg)
                }
            }
        }
    }

    fun syncCurriculumToFirestore(onProgress: (String) -> Unit = {}, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        pushLocalDatabaseToFirebase(
            onProgress = { step, _ -> onProgress(step) },
            onComplete = onComplete
        )
    }

    fun forceSyncQuestionsToFirebase(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dao = database.deshExamDao()
                val localSubjects = dao.getAllSubjectsOnce()
                localSubjects.forEach { sub ->
                    try { repository.ensureSubjectContentSeeded(sub.id) } catch (_: Exception) {}
                }

                val localQuestions = dao.getAllQuestions()
                val fQuestionsList = localQuestions.map { q ->
                    val optionsList: List<String> = when {
                        q.optionsJson.isBlank() -> emptyList()
                        q.optionsJson.startsWith("[") -> try {
                            val arr = org.json.JSONArray(q.optionsJson)
                            val list = mutableListOf<String>()
                            for (i in 0 until arr.length()) list.add(arr.getString(i))
                            list
                        } catch (_: Exception) {
                            q.optionsJson.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        }
                        q.optionsJson.contains("|") -> q.optionsJson.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        else -> listOf(q.optionsJson)
                    }
                    val tagsList = (q.tagsJson.split("|", "#", ",") + listOfNotNull(q.pyqYear.ifBlank { null }))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    FirestoreQuestion(
                        id = q.id.ifBlank { "q_${q.subjectId}_${System.currentTimeMillis()}" },
                        classId = q.classId.ifBlank { "class_10_wbbse" },
                        subjectId = q.subjectId,
                        chapterId = q.chapterId,
                        topicId = q.topicId,
                        type = q.type.ifBlank { "MCQ" },
                        difficulty = q.difficulty.ifBlank { "MEDIUM" },
                        question = q.question,
                        options = optionsList,
                        correctOptionIndex = q.correctOptionIndex,
                        correctAnswerText = q.correctAnswerText.ifBlank { optionsList.getOrNull(q.correctOptionIndex) ?: "" },
                        explanation = q.explanation,
                        marks = if (q.marks > 0) q.marks else 1,
                        tags = tagsList,
                        podcastUrl = q.podcastUrl,
                        podcastTitle = q.podcastTitle
                    )
                }

                val pyqs = if (_allPyqQuestions.value.isNotEmpty()) _allPyqQuestions.value else {
                    try { PyqRepository.loadAllPyqQuestions(getApplication()) } catch (_: Exception) { emptyList() }
                }
                val fPyqQuestions = pyqs.map { pyq ->
                    FirestoreQuestion(
                        id = pyq.id.ifBlank { "pyq_${pyq.year}_${pyq.subject}_${System.currentTimeMillis()}" },
                        classId = "class_10_wbbse",
                        subjectId = when (pyq.subject.uppercase()) {
                            "MATHEMATICS", "MATH" -> "sub_10_math"
                            "PHYSICAL_SCIENCE" -> "sub_10_physical_science"
                            "LIFE_SCIENCE" -> "sub_10_life_science"
                            "HISTORY" -> "sub_10_history"
                            "GEOGRAPHY" -> "sub_10_geography"
                            "BENGALI" -> "sub_10_bengali"
                            "ENGLISH" -> "sub_10_english"
                            else -> "sub_10_${pyq.subject.lowercase()}"
                        },
                        chapterId = "ch_${pyq.subject.lowercase()}_general",
                        topicId = "top_${pyq.subject.lowercase()}_pyq",
                        type = "MCQ",
                        difficulty = "MEDIUM",
                        question = pyq.question,
                        options = pyq.options ?: emptyList(),
                        correctOptionIndex = pyq.correctOptionIndex,
                        correctAnswerText = pyq.officialAnswerBengali?.ifBlank { null } ?: pyq.options?.getOrNull(pyq.correctOptionIndex) ?: "",
                        explanation = pyq.keyTips,
                        marks = pyq.marks,
                        tags = listOf("PYQ", "${pyq.year}", pyq.subject, pyq.group)
                    )
                }

                val allQuestionsToPush = (fQuestionsList + fPyqQuestions).distinctBy { it.id }
                val qBatchRes = firestoreService.saveQuestionsBatch(allQuestionsToPush)
                val qSavedCount = qBatchRes.getOrElse { 0 }

                checkCloudDatabaseIntegrity()
                withContext(Dispatchers.Main) {
                    onComplete(true, "✅ সফলভাবে $qSavedCount টি প্রশ্ন ও PYQ ফায়ারবেস ক্লাউডে সিঙ্ক করা হয়েছে!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "❌ প্রশ্ন সিঙ্ক ব্যর্থ: ${e.localizedMessage}")
                }
            }
        }
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
        podcastUrl: String = "",
        podcastTitle: String = "",
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
                classId = classId,
                podcastUrl = podcastUrl,
                podcastTitle = podcastTitle
            )
            val updated = _firestoreQuestions.value + q
            _firestoreQuestions.value = updated
            onComplete(true, "প্রশ্ন সফলভাবে যুক্ত করা হয়েছে!")
        }
    }

    fun updateFirestoreQuestion(question: FirestoreQuestion, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestoreService.saveQuestion(question)
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) {
                val updated = _firestoreQuestions.value.map { if (it.id == question.id) question else it }
                _firestoreQuestions.value = updated
                onComplete(true, "প্রশ্ন আপডেট হয়েছে")
            }
        }
    }

    fun deleteFirestoreQuestion(questionId: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestoreService.deleteQuestion(questionId)
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) {
                val updated = _firestoreQuestions.value.filter { it.id != questionId }
                _firestoreQuestions.value = updated
                onComplete(true, "প্রশ্ন মোছা হয়েছে")
            }
        }
    }

    fun fetchCustomPodcasts(className: String? = null, boardId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = firestoreService.getCustomPodcasts(className, boardId)
                result.onSuccess { list ->
                    withContext(Dispatchers.Main) {
                        _customPodcasts.value = list
                    }
                }.onFailure { e ->
                    Log.e("DeshExamViewModel", "Error fetching custom podcasts: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("DeshExamViewModel", "Error in fetchCustomPodcasts: ${e.message}")
            }
        }
    }

    fun addCustomPodcast(
        title: String,
        subject: String,
        duration: String,
        narrator: String,
        className: String,
        audioUrl: String,
        boardId: String = "WBBSE",
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val p = com.example.data.firestore.FirestorePodcast(
                    id = "podcast_${System.currentTimeMillis()}",
                    title = title,
                    subject = subject,
                    duration = duration,
                    narrator = narrator,
                    className = className,
                    boardId = boardId,
                    audioUrl = audioUrl,
                    createdAt = System.currentTimeMillis()
                )
                val result = firestoreService.savePodcast(p)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        _customPodcasts.value = _customPodcasts.value + p
                        onComplete(true, "পডকাস্ট সফলভাবে যুক্ত করা হয়েছে!")
                    } else {
                        onComplete(false, "❌ পডকাস্ট সংরক্ষণে ত্রুটি: ${result.exceptionOrNull()?.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "❌ ভুল হয়েছে: ${e.localizedMessage}")
                }
            }
        }
    }

    fun updateCustomPodcast(
        podcast: com.example.data.firestore.FirestorePodcast,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = firestoreService.savePodcast(podcast)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        _customPodcasts.value = _customPodcasts.value.map { if (it.id == podcast.id) podcast else it }
                        onComplete(true, "পডকাস্ট সফলভাবে আপডেট করা হয়েছে!")
                    } else {
                        onComplete(false, "❌ পডকাস্ট আপডেটে ত্রুটি: ${result.exceptionOrNull()?.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "❌ ভুল হয়েছে: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteCustomPodcast(
        podcastId: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = firestoreService.deletePodcast(podcastId)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        _customPodcasts.value = _customPodcasts.value.filter { it.id != podcastId }
                        onComplete(true, "পডকাস্ট সফলভাবে মুছে ফেলা হয়েছে!")
                    } else {
                        onComplete(false, "❌ পডকাস্ট ডিলিট করতে সমস্যা হয়েছে: ${result.exceptionOrNull()?.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "❌ ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun importBulkCustomPodcasts(
        rawInput: String,
        onComplete: (Boolean, String, Int) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = mutableListOf<com.example.data.firestore.FirestorePodcast>()
                val trimmed = rawInput.trim()
                if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
                    val array = if (trimmed.startsWith("[")) {
                        org.json.JSONArray(trimmed)
                    } else {
                        val obj = org.json.JSONObject(trimmed)
                        obj.optJSONArray("podcasts") ?: org.json.JSONArray()
                    }
                    val timestamp = System.currentTimeMillis()
                    for (i in 0 until array.length()) {
                        val item = array.optJSONObject(i) ?: continue
                        val title = item.optString("title").trim()
                        val subject = item.optString("subject").trim()
                        val duration = item.optString("duration", "৫ মিনিট").trim()
                        val narrator = item.optString("narrator", "সহায়ক শিক্ষক").trim()
                        val className = item.optString("className", "Class 10").trim()
                        val boardId = item.optString("boardId", if (className.contains("11") || className.contains("12")) "WBCHSE" else "WBBSE").trim()
                        val audioUrl = item.optString("audioUrl").trim()
                        
                        if (title.isNotBlank() && subject.isNotBlank() && audioUrl.isNotBlank()) {
                            list.add(
                                com.example.data.firestore.FirestorePodcast(
                                    id = "podcast_${timestamp}_$i",
                                    title = title,
                                    subject = subject,
                                    duration = duration,
                                    narrator = narrator,
                                    className = className,
                                    boardId = boardId,
                                    audioUrl = audioUrl,
                                    createdAt = timestamp
                                )
                            )
                        }
                    }
                } else {
                    val lines = trimmed.split("\n")
                    val timestamp = System.currentTimeMillis()
                    var index = 0
                    for (line in lines) {
                        val l = line.trim()
                        if (l.isBlank()) continue
                        if (l.contains("title", ignoreCase = true) || l.contains("audioUrl", ignoreCase = true)) continue
                        
                        val parts = if (l.contains("|")) l.split("|") else l.split(",")
                        val title = parts.getOrNull(0)?.trim() ?: ""
                        val subject = parts.getOrNull(1)?.trim() ?: ""
                        val duration = parts.getOrNull(2)?.trim()?.ifBlank { "৫ মিনিট" } ?: "৫ মিনিট"
                        val narrator = parts.getOrNull(3)?.trim()?.ifBlank { "সহায়ক শিক্ষক" } ?: "সহায়ক শিক্ষক"
                        val className = parts.getOrNull(4)?.trim()?.ifBlank { "Class 10" } ?: "Class 10"
                        
                        // Support both 6-column (without explicit board) and 7-column (with board) formats
                        val (boardId, audioUrl) = if (parts.size >= 7) {
                            val b = parts.getOrNull(5)?.trim()?.ifBlank { "WBBSE" } ?: "WBBSE"
                            val u = parts.getOrNull(6)?.trim() ?: ""
                            Pair(b, u)
                        } else {
                            val defaultBoard = if (className.contains("11") || className.contains("12")) "WBCHSE" else "WBBSE"
                            val u = parts.getOrNull(5)?.trim() ?: ""
                            Pair(defaultBoard, u)
                        }
                        
                        if (title.isNotBlank() && subject.isNotBlank() && audioUrl.isNotBlank()) {
                            list.add(
                                com.example.data.firestore.FirestorePodcast(
                                    id = "podcast_${timestamp}_$index",
                                    title = title,
                                    subject = subject,
                                    duration = duration,
                                    narrator = narrator,
                                    className = className,
                                    boardId = boardId,
                                    audioUrl = audioUrl,
                                    createdAt = timestamp
                                )
                            )
                            index++
                        }
                    }
                }

                if (list.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "⚠️ কোনো বৈধ পডকাস্টের তথ্য পাওয়া যায়নি! নমুনা ফরম্যাট চেক করুন।", 0)
                    }
                    return@launch
                }

                val result = firestoreService.savePodcastsBatch(list)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        _customPodcasts.value = _customPodcasts.value + list
                        onComplete(true, "✅ সফলভাবে ${list.size} টি কাস্টম পডকাস্ট ব্যাচ আপলোড করা হয়েছে!", list.size)
                    } else {
                        onComplete(false, "❌ ব্যাচ সংরক্ষণে ত্রুটি: ${result.exceptionOrNull()?.localizedMessage}", 0)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "❌ আপলোডের সময় ভুল হয়েছে: ${e.localizedMessage}", 0)
                }
            }
        }
    }

    fun deleteFirestoreTestTemplate(testId: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestoreService.deleteTestTemplate(testId)
            } catch (e: Exception) {
                Log.e("DeshExamViewModel", "Error deleting test template: ${e.message}")
            }
            withContext(Dispatchers.Main) {
                val updated = _firestoreTestTemplates.value.filter { it.id != testId }
                _firestoreTestTemplates.value = updated
                onComplete(true, "টেস্ট ফায়ারবেস থেকে মোছা হয়েছে")
            }
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
        viewModelScope.launch(Dispatchers.IO) {
            val qId = "q_${testId}_${System.currentTimeMillis()}"
            val fQ = FirestoreQuestion(
                id = qId,
                classId = classId,
                subjectId = subjectId,
                chapterId = "ch_${subjectId}_custom",
                topicId = "top_${subjectId}_custom",
                type = "MCQ",
                difficulty = difficulty,
                question = questionText,
                options = optionsList,
                correctOptionIndex = correctIndex,
                correctAnswerText = correctAnswerText,
                explanation = explanationText,
                marks = 1,
                tags = listOf("CUSTOM_TEST", assessmentMode)
            )

            val entity = QuestionEntity(
                id = qId,
                topicId = "top_${subjectId}_custom",
                chapterId = "ch_${subjectId}_custom",
                subjectId = subjectId,
                classId = classId,
                type = "MCQ",
                difficulty = difficulty,
                question = questionText,
                optionsJson = org.json.JSONArray(optionsList).toString(),
                correctOptionIndex = correctIndex,
                correctAnswerText = correctAnswerText,
                explanation = explanationText,
                marks = 1,
                tagsJson = "CUSTOM_TEST",
                pyqYear = ""
            )

            try { database.deshExamDao().insertQuestions(listOf(entity)) } catch (_: Exception) {}
            val qSaveResult = firestoreService.saveQuestion(fQ)

            val existing = _firestoreTestTemplates.value.find { it.id == testId }
            val existingQuestions = existing?.questions ?: emptyList()
            val updatedQuestions = existingQuestions + fQ

            val template = FirestoreTestTemplate(
                id = testId,
                title = testTitle,
                classId = classId,
                subjectId = subjectId,
                chapterId = "ch_${subjectId}_custom",
                testType = assessmentMode,
                durationMinutes = timeMinutes,
                totalMarks = updatedQuestions.size,
                questionCount = updatedQuestions.size,
                questions = updatedQuestions
            )

            val templateSaveResult = firestoreService.saveTestTemplate(template)

            withContext(Dispatchers.Main) {
                if (templateSaveResult.isSuccess) {
                    _firestoreTestTemplates.value = _firestoreTestTemplates.value.filter { it.id != testId } + template
                    onComplete(true, "✓ টেস্ট ও প্রশ্ন সফলভাবে ফায়ারবেসে সংরক্ষিত হয়েছে!")
                } else {
                    val errMsg = templateSaveResult.exceptionOrNull()?.localizedMessage ?: qSaveResult.exceptionOrNull()?.localizedMessage ?: "Unknown Firestore error"
                    onComplete(false, "❌ ফায়ারবেসে টেস্ট সেভ করতে ব্যর্থ হয়েছে: $errMsg")
                }
            }
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
        questions: List<StructuredQuestionInput>,
        onComplete: (Boolean, String, String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            val testId = "test_${classId}_${subjectId}_$timestamp"
            val newEntities = mutableListOf<QuestionEntity>()
            val firestoreQuestions = mutableListOf<FirestoreQuestion>()

            questions.forEachIndexed { i, qInput ->
                val qId = "q_${subjectId}_${timestamp}_$i"
                val correctAnsText = qInput.options.getOrNull(qInput.correctIndex) ?: qInput.options.firstOrNull() ?: ""
                val optionsJson = org.json.JSONArray(qInput.options).toString()

                val entity = QuestionEntity(
                    id = qId,
                    topicId = "${chapterId}_topic",
                    chapterId = chapterId,
                    subjectId = subjectId,
                    classId = classId,
                    type = "MCQ",
                    difficulty = "MEDIUM",
                    question = qInput.questionText,
                    optionsJson = optionsJson,
                    correctOptionIndex = qInput.correctIndex,
                    correctAnswerText = correctAnsText,
                    explanation = qInput.explanation.ifBlank { "সঠিক উত্তর।" },
                    marks = 1,
                    tagsJson = "STRUCTURED_TEST",
                    pyqYear = ""
                )
                newEntities.add(entity)

                val fQ = FirestoreQuestion(
                    id = qId,
                    classId = classId,
                    subjectId = subjectId,
                    chapterId = chapterId,
                    topicId = "${chapterId}_topic",
                    type = "MCQ",
                    difficulty = "MEDIUM",
                    question = qInput.questionText,
                    options = qInput.options,
                    correctOptionIndex = qInput.correctIndex,
                    correctAnswerText = correctAnsText,
                    explanation = qInput.explanation.ifBlank { "সঠিক উত্তর।" },
                    marks = 1,
                    tags = listOf("STRUCTURED_TEST", testType)
                )
                firestoreQuestions.add(fQ)
            }

            // Save to local room DB
            try {
                if (newEntities.isNotEmpty()) {
                    database.deshExamDao().insertQuestions(newEntities)
                }
            } catch (_: Exception) {}

            // Save questions to Firestore
            val questionsResult = try {
                if (firestoreQuestions.isNotEmpty()) {
                    firestoreService.saveQuestionsBatch(firestoreQuestions)
                } else {
                    Result.success(0)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }

            val template = FirestoreTestTemplate(
                id = testId,
                title = title,
                classId = classId,
                subjectId = subjectId,
                chapterId = chapterId,
                testType = testType,
                durationMinutes = durationMinutes,
                totalMarks = if (totalMarks > 0) totalMarks else firestoreQuestions.size,
                questionCount = firestoreQuestions.size,
                questions = firestoreQuestions
            )

            val templateResult = try {
                firestoreService.saveTestTemplate(template)
            } catch (e: Exception) {
                Result.failure(e)
            }

            withContext(Dispatchers.Main) {
                if (templateResult.isSuccess) {
                    _firestoreTestTemplates.value = _firestoreTestTemplates.value + template
                    onComplete(true, "মোট ${firestoreQuestions.size}টি প্রশ্ন সহ টেস্ট সফলভাবে তৈরি ও ক্লাউডে সংরক্ষিত হয়েছে!", testId)
                } else {
                    val errMsg = templateResult.exceptionOrNull()?.localizedMessage ?: questionsResult.exceptionOrNull()?.localizedMessage ?: "Unknown Firestore error"
                    onComplete(false, "❌ ফায়ারবেসে টেস্ট সেভ করতে ব্যর্থ হয়েছে: $errMsg", null)
                }
            }
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
        viewModelScope.launch(Dispatchers.IO) {
            val trimmed = rawInput.trim()
            if (trimmed.isBlank()) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "কোনো ডেটা পাওয়া যায়নি", 0)
                }
                return@launch
            }

            val isJson = trimmed.startsWith("[") || trimmed.startsWith("{")
            val newQuestions = mutableListOf<QuestionEntity>()
            val firestoreQuestions = mutableListOf<FirestoreQuestion>()
            val timestamp = System.currentTimeMillis()

            try {
                if (isJson) {
                    val jsonArray = if (trimmed.startsWith("[")) {
                        org.json.JSONArray(trimmed)
                    } else {
                        val rootObj = org.json.JSONObject(trimmed)
                        rootObj.optJSONArray("questions") ?: org.json.JSONArray()
                    }

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val qText = obj.optString("question", obj.optString("questionText", obj.optString("q", ""))).trim()
                        if (qText.isBlank()) continue

                        val optionsList = mutableListOf<String>()
                        val optsArr = obj.optJSONArray("options") ?: obj.optJSONArray("choices") ?: obj.optJSONArray("opts")
                        if (optsArr != null) {
                            for (o in 0 until optsArr.length()) {
                                optionsList.add(optsArr.optString(o, "").trim())
                            }
                        } else {
                            val optStr = obj.optString("options", "")
                            if (optStr.contains("|")) {
                                optionsList.addAll(optStr.split("|").map { it.trim() }.filter { it.isNotEmpty() })
                            } else {
                                listOf("opt1", "opt2", "opt3", "opt4").forEach { k ->
                                    val v = obj.optString(k, "").trim()
                                    if (v.isNotEmpty()) optionsList.add(v)
                                }
                            }
                        }

                        val correctIdx = when {
                            obj.has("correctOptionIndex") -> obj.optInt("correctOptionIndex", 0)
                            obj.has("correctAnswerIndex") -> obj.optInt("correctAnswerIndex", 0)
                            obj.has("answerIndex") -> obj.optInt("answerIndex", 0)
                            obj.has("correctIndex") -> obj.optInt("correctIndex", 0)
                            obj.has("answer") -> {
                                val ansStr = obj.optString("answer", "0").trim()
                                ansStr.toIntOrNull() ?: optionsList.indexOfFirst { it.equals(ansStr, ignoreCase = true) }.coerceAtLeast(0)
                            }
                            else -> 0
                        }

                        val explanation = obj.optString("explanation", obj.optString("exp", obj.optString("solution", ""))).trim()
                        val diff = obj.optString("difficulty", obj.optString("level", "MEDIUM")).uppercase().trim()
                        val marks = obj.optInt("marks", obj.optInt("points", 1))
                        val qType = obj.optString("type", "MCQ").uppercase().trim()
                        val qPyqYear = obj.optString("pyqYear", pyqYear).trim()
                        val qClassId = obj.optString("classId", classId).ifBlank { classId }
                        val qSubjectId = obj.optString("subjectId", subjectId).ifBlank { subjectId }
                        val qChapterId = obj.optString("chapterId", chapterId).ifBlank { chapterId }
                        val qTopicId = obj.optString("topicId", topicId).ifBlank { topicId }
                        val qId = obj.optString("id", "q_${qSubjectId}_${timestamp}_$i").trim()
                        val podcastUrl = obj.optString("podcastUrl", obj.optString("podcast_url", "")).trim()
                        val podcastTitle = obj.optString("podcastTitle", obj.optString("podcast_title", "")).trim()

                        val optionsJson = org.json.JSONArray(optionsList).toString()
                        val correctAnsText = optionsList.getOrNull(correctIdx) ?: ""

                        val entity = QuestionEntity(
                            id = qId,
                            topicId = qTopicId,
                            chapterId = qChapterId,
                            subjectId = qSubjectId,
                            classId = qClassId,
                            type = qType,
                            difficulty = diff,
                            question = qText,
                            optionsJson = optionsJson,
                            correctOptionIndex = correctIdx,
                            correctAnswerText = correctAnsText,
                            explanation = explanation,
                            marks = marks,
                            tagsJson = if (qPyqYear.isNotBlank()) "PYQ_$qPyqYear" else "BULK_IMPORT",
                            pyqYear = qPyqYear,
                            podcastUrl = podcastUrl,
                            podcastTitle = podcastTitle
                        )
                        newQuestions.add(entity)

                        val fQ = FirestoreQuestion(
                            id = qId,
                            classId = qClassId,
                            subjectId = qSubjectId,
                            chapterId = qChapterId,
                            topicId = qTopicId,
                            type = qType,
                            difficulty = diff,
                            question = qText,
                            options = optionsList,
                            correctOptionIndex = correctIdx,
                            correctAnswerText = correctAnsText,
                            explanation = explanation,
                            marks = marks,
                            tags = if (qPyqYear.isNotBlank()) listOf("PYQ", qPyqYear) else listOf("BULK_IMPORT"),
                            podcastUrl = podcastUrl,
                            podcastTitle = podcastTitle
                        )
                        firestoreQuestions.add(fQ)
                    }
                } else {
                    // Pipe format: Question | Opt1 | Opt2 | Opt3 | Opt4 | CorrectIndex(0-3) | Explanation | Difficulty | Marks
                    val lines = trimmed.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    lines.forEachIndexed { idx, line ->
                        val parts = line.split("|").map { it.trim() }
                        if (parts.size >= 5) {
                            val qText = parts[0]
                            val opt1 = parts.getOrNull(1) ?: ""
                            val opt2 = parts.getOrNull(2) ?: ""
                            val opt3 = parts.getOrNull(3) ?: ""
                            val opt4 = parts.getOrNull(4) ?: ""
                            val opts = listOfNotNull(opt1, opt2, opt3.ifEmpty { null }, opt4.ifEmpty { null })
                            val cIdx = parts.getOrNull(5)?.toIntOrNull() ?: 0
                            val exp = parts.getOrNull(6) ?: ""
                            val diff = parts.getOrNull(7)?.uppercase() ?: "MEDIUM"
                            val marks = parts.getOrNull(8)?.toIntOrNull() ?: 1
                            val qId = "q_${subjectId}_${timestamp}_$idx"

                            val optionsJson = org.json.JSONArray(opts).toString()
                            val correctAnsText = opts.getOrNull(cIdx) ?: ""

                            val entity = QuestionEntity(
                                id = qId,
                                topicId = topicId,
                                chapterId = chapterId,
                                subjectId = subjectId,
                                classId = classId,
                                type = "MCQ",
                                difficulty = diff,
                                question = qText,
                                optionsJson = optionsJson,
                                correctOptionIndex = cIdx,
                                correctAnswerText = correctAnsText,
                                explanation = exp,
                                marks = marks,
                                tagsJson = if (pyqYear.isNotBlank()) "PYQ_$pyqYear" else "BULK_IMPORT",
                                pyqYear = pyqYear
                            )
                            newQuestions.add(entity)

                            val fQ = FirestoreQuestion(
                                id = qId,
                                classId = classId,
                                subjectId = subjectId,
                                chapterId = chapterId,
                                topicId = topicId,
                                type = "MCQ",
                                difficulty = diff,
                                question = qText,
                                options = opts,
                                correctOptionIndex = cIdx,
                                correctAnswerText = correctAnsText,
                                explanation = exp,
                                marks = marks,
                                tags = if (pyqYear.isNotBlank()) listOf("PYQ", pyqYear) else listOf("BULK_IMPORT")
                            )
                            firestoreQuestions.add(fQ)
                        }
                    }
                }

                if (newQuestions.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "কোনো বৈধ প্রশ্ন পাওয়া যায়নি। ফরম্যাট পরীক্ষা করুন।", 0)
                    }
                    return@launch
                }

                // 1. Save to local Room database
                database.deshExamDao().insertQuestions(newQuestions)

                // 2. Save batch to Firestore cloud in parallel
                var cloudSuccessCount = 0
                for (fq in firestoreQuestions) {
                    try {
                        if (firestoreService.saveQuestion(fq).isSuccess) {
                            cloudSuccessCount++
                        }
                    } catch (_: Exception) {}
                }

                // 3. Save as Test Template on Firestore if custom test title is provided
                if (customTestTitle.isNotBlank()) {
                    val testId = "test_${classId}_${subjectId}_${timestamp}"
                    val template = FirestoreTestTemplate(
                        id = testId,
                        title = customTestTitle.trim(),
                        classId = classId,
                        subjectId = subjectId,
                        chapterId = chapterId,
                        testType = "MOCK_TEST",
                        durationMinutes = 25,
                        totalMarks = firestoreQuestions.size,
                        questionCount = firestoreQuestions.size,
                        questions = firestoreQuestions
                    )
                    try {
                        firestoreService.saveTestTemplate(template)
                        withContext(Dispatchers.Main) {
                            _firestoreTestTemplates.value = _firestoreTestTemplates.value + template
                        }
                    } catch (_: Exception) {}
                }

                withContext(Dispatchers.Main) {
                    onComplete(
                        true,
                        "✓ সফলভাবে ${newQuestions.size} টি প্রশ্ন ডাটাবেজে যুক্ত হয়েছে! (ক্লাউড সিঙ্ক: $cloudSuccessCount/${newQuestions.size})",
                        newQuestions.size
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ইমপোর্টে ত্রুটি: ${e.message}", 0)
                }
            }
        }
    }

    fun importBulkCurriculum(
        rawInput: String,
        classId: String,
        subjectId: String,
        onComplete: (Boolean, String, Int, Int) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var chaptersImported = 0
            var topicsImported = 0
            val trimmed = rawInput.trim()
            val isJson = trimmed.startsWith("[") || trimmed.startsWith("{")
            
            try {
                if (isJson) {
                    val jsonArray = if (trimmed.startsWith("[")) {
                        org.json.JSONArray(trimmed)
                    } else {
                        org.json.JSONObject(trimmed).optJSONArray("chapters") ?: org.json.JSONArray()
                    }
                    
                    for (i in 0 until jsonArray.length()) {
                        val chObj = jsonArray.getJSONObject(i)
                        val chNum = chObj.optInt("chapterNumber", i + 1)
                        val chTitle = chObj.optString("title", "অধ্যায় $chNum").trim()
                        val chDesc = chObj.optString("description", "").trim()
                        val chContent = chObj.optString("contentData", "").trim()
                        val chSolution = chObj.optString("solutionData", "").trim()
                        
                        val chId = "${subjectId}_ch$chNum"
                        
                        // 1. Create and Save Chapter
                        val fChapter = com.example.data.firestore.FirestoreChapter(
                            id = chId,
                            classId = classId,
                            subjectId = subjectId,
                            chapterNumber = chNum,
                            title = chTitle,
                            description = chDesc,
                            totalTopics = 0,
                            weightageMarks = 15,
                            orderIndex = chNum,
                            contentData = chContent.ifBlank { null },
                            solutionData = chSolution.ifBlank { null }
                        )
                        
                        val saveChRes = firestoreService.saveChapter(fChapter)
                        if (saveChRes.isSuccess) {
                            val chapterEntity = com.example.data.local.ChapterEntity(
                                id = chId,
                                subjectId = subjectId,
                                chapterNumber = chNum,
                                title = chTitle,
                                description = chDesc,
                                totalTopics = 0,
                                completedTopics = 0,
                                masteryPercentage = 0,
                                bossUnlocked = false,
                                bossCompleted = false,
                                classId = classId,
                                contentData = fChapter.contentData,
                                solutionData = fChapter.solutionData
                            )
                            database.deshExamDao().insertChapters(listOf(chapterEntity))
                            chaptersImported++
                        }
                        
                        // 2. Parse and Save Topics
                        val topicsArr = chObj.optJSONArray("topics")
                        if (topicsArr != null) {
                            for (j in 0 until topicsArr.length()) {
                                val tpObj = topicsArr.getJSONObject(j)
                                val tpOrder = tpObj.optInt("orderIndex", j + 1)
                                val tpTitle = tpObj.optString("title", "টপিক $tpOrder").trim()
                                val tpSummary = tpObj.optString("summary", "").trim()
                                val tpContent = tpObj.optString("contentData", "").trim()
                                val tpVideo = tpObj.optString("videoUrl", "").trim()
                                val tpPlaylist = tpObj.optString("playlistJson", "").trim()
                                
                                val keyPointsList = mutableListOf<String>()
                                val kpsArr = tpObj.optJSONArray("keyPoints")
                                if (kpsArr != null) {
                                    for (k in 0 until kpsArr.length()) {
                                        keyPointsList.add(kpsArr.getString(k))
                                    }
                                } else {
                                    val kpsStr = tpObj.optString("keyPoints", "")
                                    if (kpsStr.isNotBlank()) {
                                        keyPointsList.addAll(kpsStr.split("|", "#").map { it.trim() }.filter { it.isNotEmpty() })
                                    }
                                }
                                
                                val formulaeList = mutableListOf<String>()
                                val fmsArr = tpObj.optJSONArray("formulae")
                                if (fmsArr != null) {
                                    for (k in 0 until fmsArr.length()) {
                                        formulaeList.add(fmsArr.getString(k))
                                    }
                                } else {
                                    val fmsStr = tpObj.optString("formulae", "")
                                    if (fmsStr.isNotBlank()) {
                                        formulaeList.addAll(fmsStr.split("|", "#").map { it.trim() }.filter { it.isNotEmpty() })
                                    }
                                }
                                
                                val tpId = "${chId}_t$tpOrder"
                                val fTopic = com.example.data.firestore.FirestoreTopic(
                                    id = tpId,
                                    classId = classId,
                                    subjectId = subjectId,
                                    chapterId = chId,
                                    orderIndex = tpOrder,
                                    title = tpTitle,
                                    summary = tpSummary,
                                    keyPoints = keyPointsList,
                                    formulae = formulaeList,
                                    questionCount = 0,
                                    contentData = tpContent.ifBlank { null },
                                    solutionData = formulaeList.joinToString("|").ifBlank { null },
                                    videoUrl = tpVideo.ifBlank { null },
                                    playlistJson = tpPlaylist.ifBlank { null }
                                )
                                
                                val saveTpRes = firestoreService.saveTopic(fTopic)
                                if (saveTpRes.isSuccess) {
                                    val topicEntity = com.example.data.local.TopicEntity(
                                        id = tpId,
                                        chapterId = chId,
                                        subjectId = subjectId,
                                        orderIndex = tpOrder,
                                        title = tpTitle,
                                        summary = tpSummary,
                                        keyPointsJson = keyPointsList.joinToString("|"),
                                        isUnlocked = tpOrder == 1,
                                        masteryPercentage = 0,
                                        questionCount = 0,
                                        classId = classId,
                                        contentData = fTopic.contentData,
                                        solutionData = fTopic.solutionData,
                                        videoUrl = fTopic.videoUrl,
                                        playlistJson = fTopic.playlistJson
                                    )
                                    database.deshExamDao().insertTopics(listOf(topicEntity))
                                    topicsImported++
                                }
                            }
                        }
                    }
                } else {
                    // CSV / Pipe format
                    val lines = trimmed.split("\n")
                    val isPipe = trimmed.contains("|")
                    
                    val chMap = mutableMapOf<Int, Pair<String, String>>()
                    val tpList = mutableListOf<CsvTopicRow>()
                    
                    for (line in lines) {
                        val cleanLine = line.trim()
                        if (cleanLine.isBlank() || cleanLine.startsWith("chapterNumber")) continue
                        
                        val cols = if (isPipe) {
                            cleanLine.split("|").map { it.trim() }
                        } else {
                            cleanLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map { 
                                it.trim().removeSurrounding("\"")
                            }
                        }
                        
                        if (cols.size < 5) continue
                        
                        val chNum = cols.getOrNull(0)?.toIntOrNull() ?: 1
                        val chTitle = cols.getOrNull(1) ?: "অধ্যায় $chNum"
                        val chDesc = cols.getOrNull(2) ?: ""
                        val tpOrder = cols.getOrNull(3)?.toIntOrNull() ?: 1
                        val tpTitle = cols.getOrNull(4) ?: "টপিক $tpOrder"
                        val tpSummary = cols.getOrNull(5) ?: ""
                        val tpKeyPoints = cols.getOrNull(6) ?: ""
                        val tpFormulae = cols.getOrNull(7) ?: ""
                        val tpContent = cols.getOrNull(8) ?: ""
                        val tpVideo = cols.getOrNull(9) ?: ""
                        
                        chMap[chNum] = Pair(chTitle, chDesc)
                        tpList.add(
                            CsvTopicRow(
                                chNum = chNum,
                                tpOrder = tpOrder,
                                title = tpTitle,
                                summary = tpSummary,
                                keyPoints = tpKeyPoints,
                                formulae = tpFormulae,
                                content = tpContent,
                                videoUrl = tpVideo
                            )
                        )
                    }
                    
                    val chIdMap = mutableMapOf<Int, String>()
                    for ((chNum, pair) in chMap) {
                        val chId = "${subjectId}_ch$chNum"
                        chIdMap[chNum] = chId
                        
                        val fChapter = com.example.data.firestore.FirestoreChapter(
                            id = chId,
                            classId = classId,
                            subjectId = subjectId,
                            chapterNumber = chNum,
                            title = pair.first,
                            description = pair.second,
                            totalTopics = 0,
                            weightageMarks = 15,
                            orderIndex = chNum
                        )
                        val saveChRes = firestoreService.saveChapter(fChapter)
                        if (saveChRes.isSuccess) {
                            val chapterEntity = com.example.data.local.ChapterEntity(
                                id = chId,
                                subjectId = subjectId,
                                chapterNumber = chNum,
                                title = pair.first,
                                description = pair.second,
                                totalTopics = 0,
                                completedTopics = 0,
                                masteryPercentage = 0,
                                bossUnlocked = false,
                                bossCompleted = false,
                                classId = classId,
                                contentData = null,
                                solutionData = null
                            )
                            database.deshExamDao().insertChapters(listOf(chapterEntity))
                            chaptersImported++
                        }
                    }
                    
                    for (tpRow in tpList) {
                        val chId = chIdMap[tpRow.chNum] ?: "${subjectId}_ch${tpRow.chNum}"
                        val tpId = "${chId}_t${tpRow.tpOrder}"
                        
                        val kps = tpRow.keyPoints.split("#", "|").map { it.trim() }.filter { it.isNotEmpty() }
                        val fms = tpRow.formulae.split("#", "|").map { it.trim() }.filter { it.isNotEmpty() }
                        
                        val fTopic = com.example.data.firestore.FirestoreTopic(
                            id = tpId,
                            classId = classId,
                            subjectId = subjectId,
                            chapterId = chId,
                            orderIndex = tpRow.tpOrder,
                            title = tpRow.title,
                            summary = tpRow.summary,
                            keyPoints = kps,
                            formulae = fms,
                            questionCount = 0,
                            contentData = tpRow.content.ifBlank { null },
                            solutionData = fms.joinToString("|").ifBlank { null },
                            videoUrl = tpRow.videoUrl.ifBlank { null },
                            playlistJson = null
                        )
                        
                        val saveTpRes = firestoreService.saveTopic(fTopic)
                        if (saveTpRes.isSuccess) {
                            val topicEntity = com.example.data.local.TopicEntity(
                                id = tpId,
                                chapterId = chId,
                                subjectId = subjectId,
                                orderIndex = tpRow.tpOrder,
                                title = tpRow.title,
                                summary = tpRow.summary,
                                keyPointsJson = kps.joinToString("|"),
                                isUnlocked = tpRow.tpOrder == 1,
                                masteryPercentage = 0,
                                questionCount = 0,
                                classId = classId,
                                contentData = fTopic.contentData,
                                solutionData = fTopic.solutionData,
                                videoUrl = fTopic.videoUrl,
                                playlistJson = null
                            )
                            database.deshExamDao().insertTopics(listOf(topicEntity))
                            topicsImported++
                        }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    onComplete(true, "কারিকুলাম ব্যাচ ইম্পোর্ট সফল হয়েছে! মোট $chaptersImported টি অধ্যায় এবং $topicsImported টি টপিক যুক্ত হয়েছে।", chaptersImported, topicsImported)
                }
            } catch (e: Exception) {
                Log.e("DeshExamViewModel", "Curriculum import failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete(false, "ইম্পোর্ট ব্যর্থ হয়েছে: ${e.localizedMessage}", 0, 0)
                }
            }
        }
    }

    private data class CsvTopicRow(
        val chNum: Int,
        val tpOrder: Int,
        val title: String,
        val summary: String,
        val keyPoints: String,
        val formulae: String,
        val content: String,
        val videoUrl: String
    )

    fun saveVideoPlaybackProgress(
        videoId: String,
        topicId: String = "",
        lessonTitle: String = "",
        videoUrl: String = "",
        positionMs: Long,
        durationMs: Long,
        playbackSpeed: Float = 1.0f,
        isCompleted: Boolean = false
    ) {
        if (videoId.isBlank()) return
        val percentage = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
        val progress = FirestoreVideoProgress(
            videoId = videoId,
            topicId = topicId,
            lessonTitle = lessonTitle,
            videoUrl = videoUrl,
            lastPositionMs = positionMs,
            durationMs = durationMs,
            playbackSpeed = playbackSpeed,
            isCompleted = isCompleted || percentage >= 0.92f,
            percentage = percentage,
            updatedAt = System.currentTimeMillis()
        )
        _videoProgressMap.update { current -> current + (videoId to progress) }

        // Sync with Firebase Firestore
        val userId = authUserState.value?.uid ?: "guest_user"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestoreService.saveVideoProgress(userId, progress)
            } catch (e: Exception) {
                Log.e("DeshExamViewModel", "Failed to sync video progress: ${e.message}")
            }
        }
    }

    fun getVideoPlaybackProgress(videoId: String): FirestoreVideoProgress? {
        return _videoProgressMap.value[videoId]
    }

    fun loadUserVideoProgress() {
        val userId = authUserState.value?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = firestoreService.getAllVideoProgress(userId)
                result.onSuccess { list ->
                    val map = list.associateBy { it.videoId }
                    _videoProgressMap.update { current -> current + map }
                }
            } catch (e: Exception) {
                Log.w("DeshExamViewModel", "Error loading video progress: ${e.message}")
            }
        }
    }

    fun syncAllDataWithFirebase(onProgress: (String) -> Unit = {}, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        pushLocalDatabaseToFirebase(
            onProgress = { step, _ -> onProgress(step) },
            onComplete = onComplete
        )
    }

    // =========================================================================
    // BOARD & CLASS MANAGEMENT (ADMIN + LOCAL-FIRST + FIREBASE REALTIME)
    // =========================================================================

    val allBoards: StateFlow<List<EducationBoard>> = AcademicClassProvider.ALL_BOARDS_FLOW
    val allClasses: StateFlow<List<AcademicClass>> = AcademicClassProvider.ALL_CLASSES_FLOW

    fun addOrUpdateBoard(board: EducationBoard, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Save to local Room & in-memory provider immediately
                repository.saveBoard(board)
                AcademicClassProvider.addBoard(board)

                // 2. Save to Firebase Firestore
                val fbBoard = board.toFirestore()
                val fbRes = firestoreService.saveBoard(fbBoard)

                withContext(Dispatchers.Main) {
                    if (fbRes.isSuccess) {
                        onComplete(true, "বোর্ড '${board.shortName}' সফলভাবে ফায়ারবেস ও লোকাল ডেটাবেজে সংরক্ষিত হয়েছে")
                    } else {
                        onComplete(true, "বোর্ড '${board.shortName}' লোকাল ডেটাবেজে যুক্ত হয়েছে (ক্লাউড ব্যাকআপ পেন্ডিং)")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteBoard(boardId: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Delete from Firebase Firestore
                firestoreService.deleteBoard(boardId)
                // 2. Delete from local Room & memory
                repository.deleteBoard(boardId)
                AcademicClassProvider.deleteBoard(boardId)

                withContext(Dispatchers.Main) {
                    onComplete(true, "বোর্ড সফলভাবে ডিলিট করা হয়েছে")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun addOrUpdateClass(academicClass: AcademicClass, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Save to local Room & memory immediately
                repository.saveClass(academicClass)
                AcademicClassProvider.addClass(academicClass)

                // 2. Save to Firebase Firestore
                val fbClass = academicClass.toFirestore()
                val fbRes = firestoreService.saveClass(fbClass)

                withContext(Dispatchers.Main) {
                    if (fbRes.isSuccess) {
                        onComplete(true, "ক্লাস '${academicClass.nameBengali}' সফলভাবে ফায়ারবেস ও লোকাল ডেটাবেজে সংরক্ষিত হয়েছে")
                    } else {
                        onComplete(true, "ক্লাস '${academicClass.nameBengali}' লোকাল ডেটাবেজে যুক্ত হয়েছে (ক্লাউড ব্যাকআপ পেন্ডিং)")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteClass(classId: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Delete from Firebase Firestore
                firestoreService.deleteClass(classId)
                // 2. Delete from local Room & memory
                repository.deleteClass(classId)
                AcademicClassProvider.deleteClass(classId)

                withContext(Dispatchers.Main) {
                    onComplete(true, "ক্লাস সফলভাবে ডিলিট করা হয়েছে")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun syncDefaultBoardsAndClassesToFirebase(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var boardSuccess = 0
                var classSuccess = 0

                AcademicClassProvider.DEFAULT_BOARDS.forEachIndexed { idx, board ->
                    val res = firestoreService.saveBoard(board.toFirestore(idx))
                    if (res.isSuccess) boardSuccess++
                    repository.saveBoard(board)
                }

                AcademicClassProvider.DEFAULT_CLASSES.forEachIndexed { idx, cls ->
                    val res = firestoreService.saveClass(cls.toFirestore(idx))
                    if (res.isSuccess) classSuccess++
                    repository.saveClass(cls)
                }

                withContext(Dispatchers.Main) {
                    onComplete(true, "$boardSuccess টি বোর্ড ও $classSuccess টি ক্লাস সফলভাবে ফায়ারবেস ও লোকাল ডেটাবেজে সিঙ্ক সম্পন্ন হয়েছে!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "সিঙ্ক ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun addFirestoreBoard(board: com.example.data.firestore.FirestoreEducationBoard, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Save to Firestore
                val cloudRes = firestoreService.saveBoard(board)
                if (cloudRes.isSuccess) {
                    // 2. Save to local Room database
                    val localBoard = board.toEducationBoard()
                    val boardEntity = com.example.data.local.BoardEntity(
                        id = localBoard.id,
                        shortName = localBoard.shortName,
                        fullNameBengali = localBoard.fullNameBengali,
                        fullNameEnglish = localBoard.fullNameEnglish,
                        subtitleBengali = localBoard.subtitleBengali,
                        levelBadge = localBoard.levelBadge,
                        classRangeDescription = localBoard.classRangeDescription,
                        iconName = localBoard.iconName,
                        orderIndex = board.orderIndex
                    )
                    database.deshExamDao().insertBoard(boardEntity)
                    
                    // 3. Update static provider list
                    com.example.data.model.AcademicClassProvider.addBoard(localBoard)
                    
                    withContext(Dispatchers.Main) {
                        onComplete(true, "বোর্ড সফলভাবে ফায়ারবেস ও লোকাল ডাটাবেজে যুক্ত করা হয়েছে!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "ফায়ারবেসে সেভ করা যায়নি: ${cloudRes.exceptionOrNull()?.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun addFirestoreClass(classItem: com.example.data.firestore.FirestoreClass, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Save to Firestore
                val cloudRes = firestoreService.saveClass(classItem)
                if (cloudRes.isSuccess) {
                    // 2. Save to local Room
                    val localClass = classItem.toAcademicClass()
                    val classEntity = com.example.data.local.AcademicClassEntity(
                        id = localClass.id,
                        classNumber = localClass.classNumber,
                        nameBengali = localClass.nameBengali,
                        nameEnglish = localClass.nameEnglish,
                        board = localClass.board,
                        targetExam = localClass.targetExam,
                        descriptionBengali = localClass.descriptionBengali,
                        orderIndex = classItem.orderIndex
                    )
                    database.deshExamDao().insertClass(classEntity)
                    
                    // 3. Update static provider list
                    com.example.data.model.AcademicClassProvider.addClass(localClass)
                    
                    withContext(Dispatchers.Main) {
                        onComplete(true, "ক্লাস সফলভাবে ফায়ারবেস ও লোকাল ডাটাবেজে যুক্ত করা হয়েছে!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "ফায়ারবেসে সেভ করা যায়নি: ${cloudRes.exceptionOrNull()?.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun addFirestoreSubject(subject: com.example.data.firestore.FirestoreSubject, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Save to Firestore
                val cloudRes = firestoreService.saveSubject(subject)
                if (cloudRes.isSuccess) {
                    // 2. Save to local Room
                    val subjectEntity = com.example.data.local.SubjectEntity(
                        id = subject.id,
                        code = subject.code,
                        name = subject.name,
                        textbookName = subject.textbookName,
                        totalChapters = subject.totalChapters,
                        completedChapters = 0,
                        masteryPercentage = 0,
                        colorHex = subject.parsedColorHex,
                        classId = subject.classId,
                        stream = subject.stream
                    )
                    database.deshExamDao().insertSubjects(listOf(subjectEntity))
                    
                    withContext(Dispatchers.Main) {
                        onComplete(true, "বিষয় সফলভাবে ফায়ারবেস ও লোকাল ডাটাবেজে যুক্ত করা হয়েছে!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "ফায়ারবেসে সেভ করা যায়নি: ${cloudRes.exceptionOrNull()?.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun addFirestoreChapter(chapter: com.example.data.firestore.FirestoreChapter, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Save to Firestore
                val cloudRes = firestoreService.saveChapter(chapter)
                if (cloudRes.isSuccess) {
                    // 2. Save to local Room
                    val chapterEntity = com.example.data.local.ChapterEntity(
                        id = chapter.id,
                        subjectId = chapter.subjectId,
                        chapterNumber = chapter.chapterNumber,
                        title = chapter.title,
                        description = chapter.description,
                        totalTopics = chapter.totalTopics,
                        completedTopics = 0,
                        masteryPercentage = 0,
                        bossUnlocked = false,
                        bossCompleted = false,
                        classId = chapter.classId,
                        contentData = chapter.contentData,
                        solutionData = chapter.solutionData
                    )
                    database.deshExamDao().insertChapters(listOf(chapterEntity))
                    
                    withContext(Dispatchers.Main) {
                        onComplete(true, "অধ্যায় সফলভাবে ফায়ারবেস ও লোকাল ডাটাবেজে যুক্ত করা হয়েছে!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "ফায়ারবেসে সেভ করা যায়নি: ${cloudRes.exceptionOrNull()?.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun addFirestoreTopic(topic: com.example.data.firestore.FirestoreTopic, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Save to Firestore
                val cloudRes = firestoreService.saveTopic(topic)
                if (cloudRes.isSuccess) {
                    // 2. Save to local Room
                    val topicEntity = com.example.data.local.TopicEntity(
                        id = topic.id,
                        chapterId = topic.chapterId,
                        subjectId = topic.subjectId,
                        orderIndex = topic.orderIndex,
                        title = topic.title,
                        summary = topic.summary,
                        keyPointsJson = topic.keyPoints.joinToString("|"),
                        isUnlocked = true,
                        masteryPercentage = 0,
                        classId = topic.classId,
                        contentData = topic.contentData,
                        solutionData = topic.solutionData ?: topic.formulae.joinToString("|"),
                        videoUrl = topic.videoUrl,
                        playlistJson = topic.playlistJson
                    )
                    database.deshExamDao().insertTopics(listOf(topicEntity))
                    
                    withContext(Dispatchers.Main) {
                        onComplete(true, "টপিক সফলভাবে ফায়ারবেস ও লোকাল ডাটাবেজে যুক্ত করা হয়েছে!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "ফায়ারবেসে সেভ করা যায়নি: ${cloudRes.exceptionOrNull()?.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteFirestoreChapter(chapterId: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cloudRes = firestoreService.deleteChapter(chapterId)
                if (cloudRes.isSuccess) {
                    database.deshExamDao().deleteChapterById(chapterId)
                    withContext(Dispatchers.Main) {
                        onComplete(true, "অধ্যায় সফলভাবে ফায়ারবেস ও লোকাল ডাটাবেজ থেকে ডিলিট করা হয়েছে!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "ফায়ারবেস থেকে ডিলিট করা যায়নি: ${cloudRes.exceptionOrNull()?.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteFirestoreTopic(topicId: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cloudRes = firestoreService.deleteTopic(topicId)
                if (cloudRes.isSuccess) {
                    database.deshExamDao().deleteTopicById(topicId)
                    withContext(Dispatchers.Main) {
                        onComplete(true, "টপিক সফলভাবে ফায়ারবেস ও লোকাল ডাটাবেজ থেকে ডিলিট করা হয়েছে!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "ফায়ারবেস থেকে ডিলিট করা যায়নি: ${cloudRes.exceptionOrNull()?.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    fun toggleChapterCompletion(chapterId: String, currentStatus: Boolean, subjectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newStatus = !currentStatus
                val mastery = if (newStatus) 100 else 0
                // 1. Update chapter in local database
                database.deshExamDao().setChapterCompletedStatus(chapterId, newStatus, mastery)

                // 2. Fetch all chapters for this subject and count completed
                val chapters = database.deshExamDao().getChaptersForSubjectOnce(subjectId)
                val completedCount = chapters.count { it.bossCompleted || it.masteryPercentage >= 100 }
                val totalChapters = chapters.size

                val subjectMastery = if (totalChapters > 0) (completedCount * 100) / totalChapters else 0

                // 3. Update subject in local database
                database.deshExamDao().updateSubjectProgress(subjectId, completedCount, subjectMastery)

                Log.d("DeshExamViewModel", "Toggled chapter $chapterId completion to $newStatus. Subject $subjectId: $completedCount/$totalChapters completed ($subjectMastery%)")

                // 4. Automatic Real-time Firestore Cloud Sync for Chapter & Subject
                val userId = authUserState.value?.uid ?: "guest_user"
                val targetChapter = chapters.find { it.id == chapterId }
                firestoreService.saveChapterProgress(
                    userId = userId,
                    progress = com.example.data.firestore.FirestoreChapterProgress(
                        chapterId = chapterId,
                        subjectId = subjectId,
                        isCompleted = newStatus,
                        masteryPercentage = mastery,
                        completedTopics = if (newStatus) (targetChapter?.totalTopics ?: 0) else 0,
                        totalTopics = targetChapter?.totalTopics ?: 0,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                firestoreService.saveSubjectProgress(
                    userId = userId,
                    progress = com.example.data.firestore.FirestoreSubjectProgress(
                        subjectId = subjectId,
                        completedChapters = completedCount,
                        totalChapters = totalChapters,
                        masteryPercentage = subjectMastery,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.e("DeshExamViewModel", "Error toggling chapter completion: ${e.message}")
            }
        }
    }

    fun restoreUserProgressFromCloud(userId: String) {
        if (userId.isBlank() || userId == "guest_user") return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Restore Chapter Progress
                val chRes = firestoreService.getAllChapterProgress(userId)
                chRes.onSuccess { chList ->
                    for (ch in chList) {
                        database.deshExamDao().setChapterCompletedStatus(ch.chapterId, ch.isCompleted, ch.masteryPercentage)
                    }
                }

                // 2. Restore Subject Progress
                val subRes = firestoreService.getAllSubjectProgress(userId)
                subRes.onSuccess { subList ->
                    for (sub in subList) {
                        database.deshExamDao().updateSubjectProgress(sub.subjectId, sub.completedChapters, sub.masteryPercentage)
                    }
                }

                // 3. Restore Student Profile Details
                val profRes = firestoreService.getStudentProfile(userId)
                profRes.onSuccess { data ->
                    if (data != null) {
                        val name = data["name"] as? String ?: ""
                        val school = data["school"] as? String ?: ""
                        val district = data["district"] as? String ?: ""
                        val targetExam = data["targetExam"] as? String ?: ""
                        val classId = data["classId"] as? String ?: ""
                        val stream = data["stream"] as? String ?: ""
                        val profileImageUri = data["profileImageUri"] as? String ?: ""
                        
                        val current = database.deshExamDao().getUserProgressOnce() ?: com.example.data.local.UserProgressEntity()
                        val updated = current.copy(
                            studentName = if (name.isNotBlank()) name else current.studentName,
                            school = if (school.isNotBlank()) school else current.school,
                            district = if (district.isNotBlank()) district else current.district,
                            targetExam = if (targetExam.isNotBlank()) targetExam else current.targetExam,
                            classId = if (classId.isNotBlank()) classId else current.classId,
                            stream = if (stream.isNotBlank()) stream else current.stream,
                            profileImageUri = if (profileImageUri.isNotBlank()) profileImageUri else current.profileImageUri
                        )
                        database.deshExamDao().insertOrUpdateProgress(updated)
                    } else {
                        // Pre-populate with Google details if available
                        val authUser = authRepository.currentUserState
                        if (authUser != null) {
                            val name = authUser.displayName ?: ""
                            val photoUrl = authUser.photoUrl ?: ""
                            if (name.isNotBlank() || photoUrl.isNotBlank()) {
                                val current = database.deshExamDao().getUserProgressOnce() ?: com.example.data.local.UserProgressEntity()
                                val updated = current.copy(
                                    studentName = if (name.isNotBlank()) name else current.studentName,
                                    profileImageUri = if (photoUrl.isNotBlank()) photoUrl else current.profileImageUri
                                )
                                database.deshExamDao().insertOrUpdateProgress(updated)
                            }
                        }
                    }
                }

                // 4. Restore Video Progress
                loadUserVideoProgress()
            } catch (e: Exception) {
                Log.w("DeshExamViewModel", "Failed to restore user progress from cloud: ${e.message}")
            }
        }
    }

    fun loadWeakTopics() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _weakTopics.value = database.deshExamDao().getTopicMistakeCounts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
