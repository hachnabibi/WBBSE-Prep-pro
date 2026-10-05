package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.SubjectEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.QuestionEntity
import com.example.notification.MadhyamikNotificationManager
import com.example.ui.components.AppNavigationDrawerSheet
import com.example.ui.components.BottomNavBar
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.*
import com.example.ui.theme.DeshExamTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DeshExamViewModel
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import android.app.Activity
import android.widget.Toast
import com.example.util.AdMobManager
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    private var pendingNavigateTo: String? = null
    private var pendingTargetTestId: String? = null

    companion object {
        var onRazorpaySuccess: ((String) -> Unit)? = null
        var lastAppOpenAdTime: Long = 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            Checkout.preload(applicationContext)
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "Razorpay preload failed safely: ${e.message}", e)
        }

        // Handle notification deep link on cold start
        extractNotificationExtras(intent)

        setContent {
            DeshExamApp(
                initialNavigateTo = pendingNavigateTo,
                initialTargetTestId = pendingTargetTestId,
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onRazorpaySuccess = null
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        Toast.makeText(this, "🎉 Razorpay Payment Successful! ID: $razorpayPaymentId", Toast.LENGTH_LONG).show()
        onRazorpaySuccess?.invoke(razorpayPaymentId ?: "SUCCESS")
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Toast.makeText(this, "❌ Razorpay Payment Failed: $response", Toast.LENGTH_LONG).show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractNotificationExtras(intent)
    }

    private fun extractNotificationExtras(intent: Intent?) {
        intent?.let {
            pendingNavigateTo = it.getStringExtra(MadhyamikNotificationManager.EXTRA_NAVIGATE_TO)
            pendingTargetTestId = it.getStringExtra(MadhyamikNotificationManager.EXTRA_TARGET_TEST_ID)
        }
    }
}

@Composable
fun DeshExamApp(
    viewModel: DeshExamViewModel = viewModel(),
    initialNavigateTo: String? = null,
    initialTargetTestId: String? = null
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isBengali by viewModel.isBengali.collectAsStateWithLifecycle()
    val profile by viewModel.studentProfile.collectAsStateWithLifecycle()
    val authUserState by viewModel.authUserState.collectAsStateWithLifecycle()
    val isAdmin = true // Temporary public access, user-based restrictions will be added later
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val recentlyViewed by viewModel.recentlyViewed.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val dailyQuests by viewModel.dailyQuests.collectAsStateWithLifecycle()
    val aiCoachInsight by viewModel.aiCoachInsight.collectAsStateWithLifecycle()
    val activeStudentsCount by viewModel.activeStudentsCount.collectAsStateWithLifecycle()
    val activeSubjectStudents by viewModel.activeSubjectStudents.collectAsStateWithLifecycle()
    val liveLeaderboard by viewModel.liveLeaderboard.collectAsStateWithLifecycle()
    val assessmentState by viewModel.assessmentState.collectAsStateWithLifecycle()
    val latestResult by viewModel.latestResult.collectAsStateWithLifecycle()
    val activeTopicStudy by viewModel.activeTopicStudy.collectAsStateWithLifecycle()
    val selectedSubjectEntity by viewModel.selectedSubjectEntity.collectAsStateWithLifecycle()
    val selectedChapterEntity by viewModel.selectedChapterEntity.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearToastMessage()
        }
    }

    // Request notification permission for Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    LaunchedEffect(Unit) {
        val activity = context as? Activity
        val now = System.currentTimeMillis()
        if (activity != null && (now - MainActivity.lastAppOpenAdTime > 45_000L)) {
            var attempts = 0
            while (attempts < 6 && !AdMobManager.hasAppOpenAd()) {
                delay(300L)
                attempts++
            }
            if (AdMobManager.hasAppOpenAd()) {
                AdMobManager.showAppOpenAd(activity) {
                    MainActivity.lastAppOpenAdTime = System.currentTimeMillis()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Handle initial notification routing if launched from notification
            initialNavigateTo?.let {
                viewModel.handleNotificationRoute(it, initialTargetTestId)
            }
        } catch (_: Throwable) {
            // Gracefully ignore permission launch issues
        }
    }

    DeshExamTheme(
        darkTheme = settings.isDarkMode,
        fontSizeScale = settings.fontSizeScale
    ) {
        // Handle system back button smoothly
        BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.AUTH && currentScreen != AppScreen.SPLASH) {
            viewModel.goBack()
        }

        val activeLessonForFullScreen by viewModel.activeVideoLesson.collectAsState()

        val isFullScreenMode = (currentScreen == AppScreen.SPLASH) ||
                (currentScreen == AppScreen.ASSESSMENT) ||
                (currentScreen == AppScreen.RESULT) ||
                (currentScreen == AppScreen.LEARN_TOPIC) ||
                (currentScreen == AppScreen.VIDEO_LESSON && activeLessonForFullScreen != null) ||
                (currentScreen == AppScreen.AUTH) ||
                (currentScreen == AppScreen.ONBOARDING) ||
                (currentScreen == AppScreen.MULTIPLAYER_BATTLE) ||
                (currentScreen == AppScreen.LIVE_QUIZ) ||
                (currentScreen == AppScreen.LIVE_MOCK_TEST) ||
                (currentScreen == AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD) ||
                (currentScreen == AppScreen.PYQ_PDF_DOWNLOAD) ||
                (currentScreen == AppScreen.TEXTBOOKS) ||
                (currentScreen == AppScreen.INTERNET_ARCHIVE_SEARCH) ||
                (currentScreen == AppScreen.ADMIN_PANEL)

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        val showTopHeader = !isFullScreenMode && currentScreen == AppScreen.HOME
        val showBottomBar = !isFullScreenMode && currentScreen != AppScreen.EDIT_PROFILE && currentScreen != AppScreen.ADMIN_PANEL && currentScreen != AppScreen.MEMBERSHIP

        var isHeaderVisible by remember { mutableStateOf(true) }

        // Smoothly hide top header when scrolling down on home page, reveal when scrolling up
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (currentScreen != AppScreen.HOME) return Offset.Zero
                    val delta = available.y
                    if (delta < -8f) {
                        isHeaderVisible = false
                    } else if (delta > 8f) {
                        isHeaderVisible = true
                    }
                    return Offset.Zero
                }
            }
        }

        LaunchedEffect(currentScreen) {
            isHeaderVisible = true
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = !isFullScreenMode,
            drawerContent = {
                AppNavigationDrawerSheet(
                    currentScreen = currentScreen,
                    profile = profile,
                    isBengali = isBengali,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                    onSignOut = { viewModel.signOut() },
                    isAdmin = isAdmin,
                    authUserState = authUserState
                )
            }
        ) {
            Scaffold(
                topBar = {
                    if (showTopHeader) {
                        AnimatedVisibility(
                            visible = isHeaderVisible,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) + fadeIn(animationSpec = tween(220)),
                            exit = slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = tween(220, easing = FastOutLinearInEasing)
                            ) + fadeOut(animationSpec = tween(180))
                        ) {
                            TopHeaderBar(
                                title = null,
                                isBengali = isBengali,
                                isDarkMode = settings.isDarkMode,
                                coinCount = profile.coins,
                                showBackButton = false,
                                onBack = { },
                                onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onToggleTheme = { viewModel.toggleDarkMode() },
                                onOpenWallet = { viewModel.navigateTo(AppScreen.WALLET) },
                                onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) }
                            )
                        }
                    }
                },
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(
                        currentScreen = currentScreen,
                        isBengali = isBengali,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }
            },
            contentWindowInsets = WindowInsets(0.dp),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(
                        top = if (showTopHeader && isHeaderVisible) innerPadding.calculateTopPadding() else 0.dp,
                        bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp
                    )
                    .fillMaxSize()
            ) {
                val screenModifier = Modifier.fillMaxSize()

                when (currentScreen) {
                AppScreen.SPLASH -> {
                    SplashScreen(
                        modifier = Modifier.fillMaxSize(),
                        onTimeout = { viewModel.forceExitSplash() }
                    )
                }
                AppScreen.AUTH -> {
                    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
                    val authError by viewModel.authErrorMessage.collectAsState()
                    val authSuccess by viewModel.authSuccessMessage.collectAsState()
                    val districts by viewModel.districts.collectAsState()
                    val districtSchoolsMap by viewModel.districtSchoolsMap.collectAsState()
                    val isLoadingDistricts by viewModel.isLoadingDistricts.collectAsState()

                    AuthScreen(
                        isBengali = isBengali,
                        isLoading = isAuthLoading,
                        errorMessage = authError,
                        successMessage = authSuccess,
                        districts = districts,
                        districtSchoolsMap = districtSchoolsMap,
                        isLoadingDistricts = isLoadingDistricts,
                        onSignIn = { email, pass -> viewModel.signIn(email, pass) },
                        onSignUp = { name, school, district, targetExam, email, pass ->
                            viewModel.signUp(name, school, district, targetExam, email, pass)
                        },
                        onForgotPassword = { email -> viewModel.sendPasswordReset(email) },
                        onContinueAsGuest = { viewModel.continueAsGuest() },
                        onClearError = { viewModel.clearAuthMessages() },
                        onGoogleSignIn = { idToken, name, email ->
                            viewModel.signInWithGoogle(idToken, name, email)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.ONBOARDING -> {
                    val districts by viewModel.districts.collectAsState()
                    val districtSchoolsMap by viewModel.districtSchoolsMap.collectAsState()
                    val isLoadingDistricts by viewModel.isLoadingDistricts.collectAsState()

                    OnboardingScreen(
                        profile = profile,
                        isBengali = isBengali,
                        districts = districts,
                        districtSchoolsMap = districtSchoolsMap,
                        isLoadingDistricts = isLoadingDistricts,
                        onCompleteOnboarding = { name, classLevel, district, school, targetExam, stream, studyGoals, phone, gender, dob ->
                            viewModel.completeOnboarding(name, classLevel, district, school, targetExam, stream, studyGoals, phone, gender, dob)
                        },
                        onNavigateToAuth = { viewModel.navigateTo(AppScreen.AUTH) },
                        modifier = Modifier.fillMaxSize(),
                        authUserState = authUserState
                    )
                }
                AppScreen.HOME -> {
                    HomeScreen(
                        profile = profile,
                        subjects = subjects,
                        dailyQuests = dailyQuests,
                        recentlyViewed = recentlyViewed,
                        bookmarks = bookmarks,
                        activeStudentsCount = activeStudentsCount,
                        isBengali = isBengali,
                        onStartAssessment = { mode, targetId, title ->
                            viewModel.startAssessment(mode, targetId, title)
                        },
                        onSelectSubject = { subjectId ->
                            viewModel.selectSubject(subjectId)
                        },
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onSwitchClass = { classId ->
                            viewModel.switchActiveExamClass(classId)
                        },
                        onSwitchStream = { stream ->
                            viewModel.switchStream(stream)
                        },
                        onRepairStreak = {
                            viewModel.repairDailyStreakFromAd()
                        },
                        onOpenSubjectDetail = { subject ->
                            viewModel.openSubjectDetail(subject)
                        },
                        modifier = screenModifier
                    )
                }
                AppScreen.JOURNEY -> {
                    JourneyMapScreen(
                        viewModel = viewModel,
                        activeSubjectStudents = activeSubjectStudents,
                        isBengali = isBengali,
                        onOpenTopicStudy = { topicId -> viewModel.openTopicStudy(topicId) },
                        onStartAssessment = { mode, targetId, title ->
                            viewModel.startAssessment(mode, targetId, title)
                        },
                        modifier = screenModifier
                    )
                }
                AppScreen.MOCK -> {
                    MockTestHubScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onStartMockTest = { mode, targetId, title ->
                            viewModel.startAssessment(mode, targetId, title)
                        },
                        onOpenSubjectDetail = { subjectId ->
                            viewModel.openSubjectDetailById(subjectId)
                        },
                        modifier = screenModifier
                    )
                }
                AppScreen.LEADERBOARD -> {
                    LeaderboardScreen(
                        viewModel = viewModel,
                        liveLeaderboard = liveLeaderboard,
                        isBengali = isBengali,
                        onStartAssessment = { mode, targetId, title ->
                            viewModel.startAssessment(mode, targetId, title)
                        },
                        modifier = screenModifier
                    )
                }
                AppScreen.BATTLE -> {
                    ChallengeArenaScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onStartChallenge = { mode, targetId, title ->
                            viewModel.startAssessment(mode, targetId, title)
                        },
                        modifier = screenModifier
                    )
                }
                AppScreen.AI_COACH -> {
                    AiCoachScreen(
                        insight = aiCoachInsight,
                        isBengali = isBengali,
                        onStartTargetedPractice = { mode, targetId, title ->
                            viewModel.startAssessment(mode, targetId, title)
                        },
                        onBack = { viewModel.goBack() },
                        modifier = screenModifier
                    )
                }
                AppScreen.PROFILE -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        profile = profile,
                        isBengali = isBengali,
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        modifier = screenModifier
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        profile = profile,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = screenModifier
                    )
                }
                AppScreen.FLASHCARDS -> {
                    FlashcardsScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.PYQ_EXPLORER -> {
                    PyqExplorerScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        onStartTest = { targetId ->
                            viewModel.startAssessment(com.example.data.model.AssessmentMode.MOCK_TEST, targetId, if (isBengali) "PYQ মক পরীক্ষা" else "PYQ Mock Exam")
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.OMR_PRACTICE -> {
                    OmrPracticeScreen(
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.TOPPERS_GUIDE -> {
                    ToppersGuideScreen(
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.CERTIFICATE -> {
                    CertificateScreen(
                        profile = profile,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.DOUBT_SOLVER -> {
                    val aiCredits by viewModel.aiDoubtCredits.collectAsState()
                    DoubtSolverScreen(
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        credits = aiCredits,
                        onWatchAdForCredits = { viewModel.addAiCreditsFromAd(3) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.MULTIPLAYER_BATTLE -> {
                    MultiplayerBattleScreen(
                        isBengali = isBengali,
                        viewModel = viewModel,
                        onBack = { viewModel.goBack() },
                        onRewardEarned = { xp, coins -> viewModel.addRewardBonus(xp, coins) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.AUDIO_PODCASTS -> {
                    AudioPodcastsScreen(
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        classId = profile.classId,
                        unlockedPodcasts = profile.unlockedPodcasts,
                        onUnlockPodcast = { title -> viewModel.unlockAudioPodcast(title) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.STUDY_PLANNER -> {
                    StudyPlannerScreen(
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.BOOKMARK_HUB -> {
                    BookmarkHubScreen(
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.STUDY_SQUADS -> {
                    StudySquadsScreen(
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.PROGRESS_DASHBOARD -> {
                    ProgressDashboardScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.BADGES -> {
                    com.example.ui.screens.BadgesScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.PDF_NOTES -> {
                    PdfNotesScreen(
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        classId = profile.classId,
                        unlockedNotes = profile.unlockedNotes,
                        onUnlockNote = { noteId -> viewModel.unlockPdfNote(noteId) },
                        onNavigateToPyqPdf = { viewModel.navigateTo(AppScreen.PYQ_PDF_DOWNLOAD) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.PYQ_PDF_DOWNLOAD -> {
                    SolvedPyqPdfDownloadScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        onNavigateToOriginal = { viewModel.navigateTo(AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.ORIGINAL_PYQ_PDF_DOWNLOAD -> {
                    OriginalPyqPdfDownloadScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        onNavigateToSolved = { viewModel.navigateTo(AppScreen.PYQ_PDF_DOWNLOAD) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.TEXTBOOKS -> {
                    TextbooksScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onNavigateToInternetArchiveSearch = { query ->
                            viewModel.navigateToInternetArchiveSearch(query)
                        },
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.LIVE_QUIZ -> {
                    LiveQuizScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.LIVE_MOCK_TEST -> {
                    LiveMockTestScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.INTERNET_ARCHIVE_SEARCH -> {
                    val searchQuery by viewModel.internetArchiveSearchQuery.collectAsState()
                    InternetArchiveSearchScreen(
                        initialQuery = searchQuery,
                        isBengali = isBengali,
                        onNavigate = { route ->
                            try {
                                val screen = AppScreen.valueOf(route)
                                viewModel.navigateTo(screen)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.ADMIN_PANEL -> {
                    AdminPanelScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = screenModifier
                    )
                }
                AppScreen.MEMBERSHIP -> {
                    MembershipScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.WEAKNESS_INSIGHT -> {
                    WeaknessInsightScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        onStartRevision = { viewModel.navigateTo(AppScreen.MOCK) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.WALLET -> {
                    WalletScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.SUBJECT_DETAIL -> {
                    val selectedSubjectEntity by viewModel.selectedSubjectEntity.collectAsState()
                    val chaptersList by viewModel.chapters.collectAsState()
                    val topicsList by viewModel.topics.collectAsState()
                    val questionsList by viewModel.questions.collectAsState()

                    selectedSubjectEntity?.let { sub ->
                        SubjectDetailScreen(
                            subject = sub,
                            chapters = chaptersList.filter { it.subjectId == sub.id },
                            topics = topicsList,
                            questions = questionsList,
                            isBengali = isBengali,
                            onBack = { viewModel.goBack() },
                            onOpenChapterDetail = { chapter ->
                                viewModel.openChapterDetail(chapter)
                            },
                            onStartAssessment = { mode, targetId, title ->
                                viewModel.startAssessment(mode, targetId, title)
                            },
                            onLearnTopic = { chapterId, topicId ->
                                viewModel.openTopicStudy(topicId)
                            },
                            getTestScore = { testId -> viewModel.getSubjectTestScore(testId) },
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                AppScreen.CHAPTER_DETAIL -> {
                    val selectedChapterEntity by viewModel.selectedChapterEntity.collectAsState()
                    val selectedSubjectEntity by viewModel.selectedSubjectEntity.collectAsState()
                    val topicsList by viewModel.topics.collectAsState()

                    selectedChapterEntity?.let { ch ->
                        val chTopics = topicsList.filter { it.chapterId == ch.id }
                        ChapterDetailScreen(
                            chapter = ch,
                            topics = chTopics,
                            subjectColorLong = selectedSubjectEntity?.colorHex ?: 0xFF2563EBL,
                            isBengali = isBengali,
                            onBack = { viewModel.goBack() },
                            onSelectTopic = { topicId ->
                                viewModel.openTopicStudy(topicId)
                            },
                            onStartAssessment = { mode, targetId, title ->
                                viewModel.startAssessment(mode, targetId, title)
                            },
                            onViewContent = { title, content, solution ->
                                viewModel.openContentSolution(title, content, solution, initialTab = 0, chapterId = ch.id)
                            },
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                AppScreen.LEARN_TOPIC -> {
                    activeTopicStudy?.let { topic ->
                        LearnTopicScreen(
                            topic = topic,
                            isBengali = isBengali,
                            viewModel = viewModel,
                            onBack = { viewModel.goBack() },
                            onStartPractice = { mode, targetId, title, setNumber ->
                                viewModel.startAssessment(mode, targetId, title, setNumber)
                            },
                            getQuestionCount = { topicId -> viewModel.getTopicQuestionCount(topicId) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                AppScreen.VIDEO_LESSON -> {
                    val activeLesson by viewModel.activeVideoLesson.collectAsState()
                    val activeTopic by viewModel.activeVideoTopic.collectAsState()
                    if (activeLesson != null) {
                        VideoLessonScreen(
                            lesson = activeLesson!!,
                            topic = activeTopic,
                            isBengali = isBengali,
                            viewModel = viewModel,
                            onBack = { viewModel.closeVideoLesson() },
                            onStartQuiz = { mode, targetId, title ->
                                viewModel.startAssessment(mode, targetId, title)
                            },
                            onOpenDoubtSolver = { viewModel.navigateTo(AppScreen.DOUBT_SOLVER) },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        com.example.ui.screens.VideoHubScreen(
                            viewModel = viewModel,
                            isBengali = isBengali,
                            onBack = { viewModel.goBack() },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                AppScreen.CONTENT_SOLUTION -> {
                    com.example.ui.screens.ContentSolutionScreen(
                        title = viewModel.contentSolutionTitle,
                        contentData = viewModel.contentSolutionContent,
                        solutionData = viewModel.contentSolutionSolution,
                        initialTab = viewModel.contentSolutionInitialTab,
                        isBengali = isBengali,
                        onBack = { viewModel.goBack() }
                    )
                }
                AppScreen.ASSESSMENT -> {
                    AssessmentScreen(
                        state = assessmentState,
                        isBengali = isBengali,
                        onSelectOption = { idx -> viewModel.selectOption(idx) },
                        onCheckOrSubmit = { viewModel.checkOrSubmitCurrentAnswer() },
                        onAdvanceNext = { viewModel.advanceToNextQuestion() },
                        onExit = { viewModel.exitAssessment() },
                        onNavigateToQuestion = { targetIdx -> viewModel.navigateToQuestion(targetIdx) },
                        onNextMockQuestion = { viewModel.nextMockQuestion() },
                        onPreviousMockQuestion = { viewModel.previousMockQuestion() },
                        onToggleMarkForReview = { qIdx -> viewModel.toggleMarkForReview(qIdx) },
                        onClearResponse = { viewModel.clearCurrentResponse() },
                        onSubmitMockTest = { viewModel.submitMockTest() },
                        onUseFiftyFifty = { viewModel.applyFiftyFiftyLifeline() },
                        onUseSkip = { viewModel.advanceToNextQuestion() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppScreen.RESULT -> {
                    latestResult?.let { result ->
                        ResultAnalysisScreen(
                            result = result,
                            isBengali = isBengali,
                            onRetryWeakArea = {
                                viewModel.startAssessment(
                                    com.example.data.model.AssessmentMode.WEAK_AREA,
                                    null,
                                    if (isBengali) "ভুল প্রশ্ন সংশোধন" else "Retry Mistakes"
                                )
                            },
                            onReturnHome = { viewModel.dismissResult() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                AppScreen.EDIT_PROFILE -> {
                    val districts by viewModel.districts.collectAsState()
                    val districtSchoolsMap by viewModel.districtSchoolsMap.collectAsState()
                    val isLoadingDistricts by viewModel.isLoadingDistricts.collectAsState()
                    EditProfileScreen(
                        viewModel = viewModel,
                        profile = profile,
                        isBengali = isBengali,
                        districts = districts,
                        districtSchoolsMap = districtSchoolsMap,
                        isLoadingDistricts = isLoadingDistricts,
                        onBack = { viewModel.goBack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
}
}
