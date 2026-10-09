package com.example.data.model

enum class QuestionType {
    MCQ,
    TRUE_FALSE,
    FILL_BLANK,
    SHORT_ANSWER
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class AssessmentMode {
    PRACTICE,
    DAILY_10,
    BOSS_BATTLE,
    MOCK_TEST,
    WEAK_AREA,
    FRIEND_CHALLENGE,
    SPEED_BLITZ,
    CHAPTER_PYQ,
    MAP_DIAGRAM_QUIZ,
    CHAPTER_MARATHON_QUIZ
}

enum class SubjectCode(
    val id: String,
    val subjectNameBengali: String,
    val subjectNameEnglish: String,
    val icon: String,
    val colorHex: Long
) {
    PHYSICAL_SCIENCE("phy_sci", "ভৌতবিজ্ঞান ও পরিবেশ", "Physical Science", "Atom", 0xFF3B82F6),
    LIFE_SCIENCE("life_sci", "জীবনবিজ্ঞান ও পরিবেশ", "Life Science", "Dna", 0xFF10B981),
    MATHEMATICS("math", "গণিত", "Mathematics", "Calculate", 0xFFF59E0B),
    HISTORY("history", "ইতিহাস ও পরিবেশ", "History", "HistoryEdu", 0xFFEC4899),
    GEOGRAPHY("geography", "ভূগোল ও পরিবেশ", "Geography", "Public", 0xFF06B6D4),
    BENGALI("bengali", "প্রথম ভাষা বাংলা", "Bengali", "MenuBook", 0xFF8B5CF6),
    BENGALI_RAPID("bengali_rapid", "সহায়ক পাঠ", "Rapid Reader", "Book", 0xFF8B5CF6),
    ENGLISH_RAPID("english_rapid", "ইংরেজি সহায়ক পাঠ", "English Rapid Reader", "MenuBook", 0xFF14B8A6),
    ENGLISH("english", "ইংরেজি", "English", "Translate", 0xFF14B8A6),
    BENGALI_GRAMMAR("ben_gram", "বাংলা ব্যাকরণ", "Bengali Grammar", "Spellcheck", 0xFF6D28D9),
    ENGLISH_GRAMMAR("eng_gram", "ইংরেজি ব্যাকরণ", "English Grammar", "FontDownload", 0xFF0D9488),
    PHYSICS("physics", "পদার্থবিদ্যা", "Physics", "Science", 0xFF3B82F6),
    CHEMISTRY("chemistry", "রসায়নবিদ্যা", "Chemistry", "Biotech", 0xFFEC4899),
    BIOLOGY("biology", "জীববিজ্ঞান", "Biological Sciences", "LocalFlorist", 0xFF10B981),
    POL_SCI("pol_sci", "রাষ্ট্রবিজ্ঞান", "Political Science", "AccountBalance", 0xFFF97316),
    ECONOMICS("economics", "অর্থনীতি", "Economics", "TrendingUp", 0xFF6366F1),
    PHILOSOPHY("philosophy", "দর্শন ও শিক্ষাবিজ্ঞান", "Philosophy & Education", "Psychology", 0xFF7C3AED),
    ACCOUNTANCY("accountancy", "হিসাবশাস্ত্র", "Accountancy", "ReceiptLong", 0xFF059669),
    BUSINESS_STUDIES("business_studies", "কারবারি শিক্ষা", "Business Studies", "BusinessCenter", 0xFFD97706),
    COMPUTER_SCIENCE("comp_sci", "কম্পিউটার সায়েন্স", "Computer Science", "LaptopMac", 0xFF2563EB),
    EDUCATION("education", "শিক্ষাবিজ্ঞান", "Education", "MenuBook", 0xFF14B8A6),
    NUTRITION("nutrition", "পুষ্টিবিজ্ঞান", "Nutrition", "LocalDining", 0xFFF59E0B),
    JOURNALISM("journalism", "সাংবাদিকতা", "Journalism & Mass Comm", "Campaign", 0xFF8B5CF6),
    SANSKRIT("sanskrit", "সংস্কৃত", "Sanskrit", "Language", 0xFF6366F1),
    PERSIAN("persian", "ফারসি", "Persian", "Language", 0xFFD97706),
    ARABIC("arabic", "আরবি", "Arabic", "Language", 0xFF059669),
    AGRICULTURE("agriculture", "কৃষিবিজ্ঞান", "Agriculture", "Agriculture", 0xFF10B981),
    BASIC_MATH("basic_math", "প্রাথমিক গণিত", "Basic Mathematics", "Calculate", 0xFFF59E0B),
    ANTHROPOLOGY("anthropology", "নৃতত্ত্ববিদ্যা", "Anthropology", "NaturePeople", 0xFFEC4899),
    SCIENCE_OF_WELL_BEING("sowb", "সায়েন্স অফ ওয়েল বিং", "Science of Well Being", "SelfImprovement", 0xFF3B82F6),
    APPLIED_AI("applied_ai", "অ্যাপ্লায়েড এ.আই.", "Applied Artificial Intelligence", "SmartToy", 0xFF7C3AED),
    SOCIOLOGY("sociology", "সমাজবিদ্যা", "Sociology", "Groups", 0xFF3B82F6),
    FISHERIES("fisheries", "মৎস্যবিজ্ঞান", "Fisheries & Aquaculture", "SetMeal", 0xFF06B6D4),
    PSYCHOLOGY("psychology", "মনোবিদ্যা", "Psychology", "Psychology", 0xFF8B5CF6),
    STATISTICS("statistics", "পরিসংখ্যান", "Statistics", "BarChart", 0xFF06B6D4),
    HUMAN_DEVELOPMENT("human_dev", "হিউম্যান ডেভেলপমেন্ট", "Human Development", "EmojiPeople", 0xFFEC4899),
    MUSIC("music", "সংগীত", "Music", "MusicNote", 0xFFF97316),
    VISUAL_ARTS("visual_arts", "দৃশ্যমান শিল্প", "Visual Arts", "Palette", 0xFFEC4899),
    HEALTH_PHY_ED("health_phy_ed", "স্বাস্থ্য ও শারীরশিক্ষা", "Health & Physical Education", "SportsHandball", 0xFF10B981),
    MODERN_COMP_APP("coma", "মডার্ন কম্পিউটার অ্যাপ্লিকেশন", "Modern Computer Application", "Computer", 0xFF2563EB),
    ENVIRONMENT_STUDIES("envs", "পরিবেশ বিদ্যা", "Environment Studies", "Eco", 0xFF10B981),
    ENVIRONMENTAL_SCIENCE("evsc", "পরিবেশ বিজ্ঞান", "Environmental Science", "Eco", 0xFF10B981),
    CYBER_SECURITY("cbst", "সাইবার সিকিউরিটি", "Cyber Security", "Security", 0xFFEF4444),
    AI_DATA_SCIENCE("aids", "এ.আই. এবং ডেটা সায়েন্স", "Artificial Intelligence & Data Science", "Insights", 0xFF3B82F6),
    COMMERCIAL_LAW_AUDITING("clpa", "কমার্শিয়াল ল অ্যান্ড প্রিলিমিনারিজ অফ অডিটিং", "Commercial Law and Preliminaries of Auditing", "Gavel", 0xFFD97706),
    COSTING_TAXATION("cstx", "কস্টিং অ্যান্ড ট্যাক্সেশন", "Costing and Taxation", "RequestQuote", 0xFF10B981),
    BUSINESS_MATH_STATISTICS("bmbs", "বিজনেস ম্যাথমেটিকস অ্যান্ড বেসিক স্ট্যাটিস্টিকস", "Business Mathematics and Basic Statistics", "Calculate", 0xFFF59E0B),
    VOCATIONAL("vocational", "বৃত্তিমূলক বিষয়", "Vocational Subject", "Build", 0xFF6B7280),
    AMAR_BOI("amar_boi", "আমার বই", "Amar Boi", "AutoStories", 0xFFF59E0B),
    SOHOJ_PATH("sohoj_path", "সহজ পাঠ", "Sohoj Path", "Book", 0xFF8B5CF6),
    HEALTH("health", "স্বাস্থ্য ও শারীরশিক্ষা", "Health & Physical Education", "SportsHandball", 0xFF10B981),
    KUTUM_KATAM("kutum_katam", "কুটুম-কাটাম", "Kutum-Katam", "Palette", 0xFFEC4899),
    MJARU("mjaru", "মজারু", "Mjaru", "Star", 0xFF06B6D4),
    BHASHAPATH("bhashapath", "ভাষা পাঠ", "Bhasha Path", "Translate", 0xFFEC4899),
    ENVIRONMENT("environment", "আমাদের পরিবেশ", "Our Environment", "Eco", 0xFF10B981);

    val nameBengali: String get() = subjectNameBengali
    val nameEnglish: String get() = subjectNameEnglish
    val displayName: String get() = subjectNameBengali
}

data class SubjectItem(
    val id: String,
    val code: SubjectCode,
    val name: String = "",
    val textbookName: String = "",
    val totalChapters: Int,
    val completedChapters: Int = 0,
    val masteryPercentage: Int = 0,
    val colorHex: Long = code.colorHex,
    val stream: String = "common"
) {
    val displayName: String get() = name.ifBlank { nameBengali.ifBlank { code.subjectNameBengali } }
    val nameBengali: String get() = ContentLocalization.getSubjectTitle(id, code, name.ifBlank { code.subjectNameBengali }, true)
    val nameEnglish: String get() = ContentLocalization.getSubjectTitle(id, code, name.ifBlank { code.subjectNameEnglish }, false)

    constructor(
        id: String,
        code: SubjectCode,
        nameBengali: String,
        nameEnglish: String = nameBengali,
        textbookName: String = "",
        totalChapters: Int,
        completedChapters: Int = 0,
        masteryPercentage: Int = 0,
        colorHex: Long = code.colorHex,
        stream: String = "common"
    ) : this(
        id = id,
        code = code,
        name = if (nameBengali.isNotBlank()) nameBengali else if (nameEnglish.isNotBlank()) nameEnglish else code.subjectNameBengali,
        textbookName = textbookName,
        totalChapters = totalChapters,
        completedChapters = completedChapters,
        masteryPercentage = masteryPercentage,
        colorHex = if (colorHex != 0L) colorHex else code.colorHex,
        stream = stream
    )

    fun getTextbook(isBengali: Boolean = true): String {
        return textbookName
    }

    val parsedStreams: List<String> get() = com.example.util.StreamHelper.parseStreams(stream)
    fun belongsToStream(targetStream: String?): Boolean = com.example.util.StreamHelper.matchesStream(stream, targetStream)
}

data class ChapterItem(
    val id: String,
    val subjectId: String,
    val chapterNumber: Int,
    val title: String,
    val description: String = "",
    val totalTopics: Int,
    val completedTopics: Int,
    val bossUnlocked: Boolean,
    val bossCompleted: Boolean,
    val masteryPercentage: Int,
    val contentData: String? = null,
    val solutionData: String? = null
) {
    val titleBengali: String get() = ContentLocalization.getChapterTitle(id, title, true)
    val titleEnglish: String get() = ContentLocalization.getChapterTitle(id, title, false)
    val descriptionBengali: String get() = ContentLocalization.getChapterDescription(id, description, true)
    val descriptionEnglish: String get() = ContentLocalization.getChapterDescription(id, description, false)
}

data class TopicItem(
    val id: String,
    val chapterId: String,
    val subjectId: String,
    val orderIndex: Int,
    val title: String,
    val summary: String = "",
    val keyPoints: List<String>,
    val isUnlocked: Boolean,
    val masteryPercentage: Int,
    val questionCount: Int,
    val contentData: String? = null,
    val solutionData: String? = null,
    val videoUrl: String? = null,
    val playlistJson: String? = null
) {
    val titleBengali: String get() = ContentLocalization.getTopicTitle(id, title, true)
    val titleEnglish: String get() = ContentLocalization.getTopicTitle(id, title, false)
    val summaryBengali: String get() = ContentLocalization.getTopicSummary(id, summary, true)
    val summaryEnglish: String get() = ContentLocalization.getTopicSummary(id, summary, false)
}

data class QuestionItem(
    val id: String,
    val topicId: String,
    val chapterId: String,
    val subjectId: String,
    val type: QuestionType,
    val difficulty: Difficulty,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val correctAnswerText: String,
    val explanation: String,
    val marks: Int = 1,
    val tags: List<String> = emptyList()
)

data class UserAnswer(
    val questionId: String,
    val selectedOptionIndex: Int = -1,
    val textAnswer: String = "",
    val isCorrect: Boolean,
    val timeSpentSeconds: Int
)

data class AssessmentSession(
    val mode: AssessmentMode,
    val title: String,
    val subjectId: String?,
    val chapterId: String?,
    val topicId: String? = null,
    val setNumber: Int = 1,
    val questions: List<QuestionItem>,
    val timeLimitSeconds: Int,
    val allowsInstantFeedback: Boolean = mode != AssessmentMode.MOCK_TEST
)

data class CategoryPerformance(
    val categoryId: String,
    val categoryName: String = "",
    val totalQuestions: Int,
    val correctCount: Int,
    val totalMarks: Int,
    val scoredMarks: Int,
    val accuracyPercentage: Int,
    val status: PerformanceStatus
) {
    val categoryNameBengali: String get() = categoryName
    val categoryNameEnglish: String get() = categoryName

    constructor(
        categoryId: String,
        categoryNameBengali: String,
        categoryNameEnglish: String = categoryNameBengali,
        totalQuestions: Int,
        correctCount: Int,
        totalMarks: Int,
        scoredMarks: Int,
        accuracyPercentage: Int,
        status: PerformanceStatus
    ) : this(
        categoryId = categoryId,
        categoryName = if (categoryNameBengali.isNotBlank()) categoryNameBengali else categoryNameEnglish,
        totalQuestions = totalQuestions,
        correctCount = correctCount,
        totalMarks = totalMarks,
        scoredMarks = scoredMarks,
        accuracyPercentage = accuracyPercentage,
        status = status
    )
}

enum class PerformanceStatus {
    STRONG,
    MODERATE,
    WEAK
}

data class DifficultyPerformance(
    val difficulty: Difficulty,
    val totalQuestions: Int,
    val correctCount: Int,
    val accuracyPercentage: Int
)

data class AssessmentResult(
    val mode: AssessmentMode,
    val title: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val accuracyPercentage: Int,
    val totalScore: Int,
    val maxScore: Int,
    val xpEarned: Int,
    val timeSpentSeconds: Int,
    val answers: List<UserAnswer>,
    val questions: List<QuestionItem> = emptyList(),
    val categoryBreakdowns: List<CategoryPerformance> = emptyList(),
    val difficultyBreakdowns: List<DifficultyPerformance> = emptyList(),
    val weakTopicIds: List<String> = emptyList(),
    val isBossBeaten: Boolean = false,
    val levelUp: Boolean = false,
    val currentLevel: Int = 1,
    val newlyUnlockedBadges: List<BadgeItem> = emptyList()
)

data class StudentProfile(
    val name: String = "Sourav Das",
    val school: String = "Kolkata Model High School",
    val district: String = "কলকাতা",
    val targetExam: String = "WBBSE Madhyamik 2026",
    val classId: String = "class_10_wbbse",
    val classLevel: String = "দশম শ্রেণি (মাধ্যমিক)",
    val stream: String = "all",
    val streamBengali: String = "সকল বিষয়",
    val profileImageUri: String = "",
    val studyGoals: List<String> = emptyList(),
    val phoneNumber: String = "",
    val gender: String = "male",
    val dateOfBirth: String = "",
    val currentXp: Int = 2450,
    val level: Int = 6,
    val xpToNextLevel: Int = 3000,
    val currentStreak: Int = 7,
    val bestStreak: Int = 14,
    val totalQuestionsAnswered: Int = 380,
    val overallAccuracy: Int = 82,
    val examReadinessPercentage: Int = 76,
    val rankState: Int = 412,
    val league: String = "Platinum Scholar / প্লাটিনাম যোদ্ধা",
    val streakShields: Int = 2,
    val totalStudyDays: Int = 42,
    val todayStreakGoalMet: Boolean = true,
    val streakMultiplier: Double = 1.5,
    val subjectMockScores: Map<String, Int> = emptyMap(),
    val isPro: Boolean = false,
    val coins: Int = 150,
    val aiCredits: Int = 5,
    val isStreakRepaired: Boolean = false,
    val unlockedNotes: Set<String> = setOf("1"),
    val unlockedPodcasts: Set<String> = emptySet(),
    val fiftyFiftyLifelines: Int = 3,
    val battleEnergy: Int = 5,
    val claimedCoinTaskIds: Set<String> = emptySet()
)

data class StreakDay(
    val dayOfWeekBengali: String,
    val dayOfWeekEnglish: String,
    val dayNumber: Int,
    val isCompleted: Boolean,
    val isToday: Boolean,
    val questionsAnswered: Int = 0,
    val minutesSpent: Int = 0
)

data class StreakMilestone(
    val dayRequirement: Int,
    val title: String,
    val xpBonus: Int,
    val badgeTitle: String,
    val isClaimed: Boolean
)

data class DailyQuest(
    val id: String,
    val title: String,
    val currentCount: Int,
    val targetCount: Int,
    val xpReward: Int,
    val isCompleted: Boolean
)

enum class BadgeCategory {
    ALL,
    ACCURACY,
    CONSISTENCY,
    TIME,
    MASTERY,
    EXAM
}

enum class BadgeTier {
    BRONZE,
    SILVER,
    GOLD,
    DIAMOND
}

data class BadgeItem(
    val id: String,
    val name: String = "",
    val description: String = "",
    val descriptionEnglish: String = "",
    val category: BadgeCategory = BadgeCategory.ALL,
    val tier: BadgeTier = BadgeTier.BRONZE,
    val iconName: String,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null,
    val currentProgress: Int = 0,
    val maxProgress: Int = 1,
    val xpReward: Int = 150,
    val milestoneRequirementBengali: String = "",
    val milestoneRequirementEnglish: String = ""
) {
    val nameBengali: String get() = name
    val nameEnglish: String get() = name
    val descriptionBengali: String get() = description

    constructor(
        id: String,
        nameBengali: String,
        nameEnglish: String = nameBengali,
        descriptionBengali: String,
        descriptionEnglish: String = "",
        category: BadgeCategory = BadgeCategory.ALL,
        tier: BadgeTier = BadgeTier.BRONZE,
        iconName: String,
        isUnlocked: Boolean,
        unlockedDate: String? = null,
        currentProgress: Int = 0,
        maxProgress: Int = 1,
        xpReward: Int = 150,
        milestoneRequirementBengali: String = "",
        milestoneRequirementEnglish: String = ""
    ) : this(
        id = id,
        name = if (nameBengali.isNotBlank()) nameBengali else nameEnglish,
        description = if (descriptionBengali.isNotBlank()) descriptionBengali else descriptionEnglish,
        descriptionEnglish = descriptionEnglish,
        category = category,
        tier = tier,
        iconName = iconName,
        isUnlocked = isUnlocked,
        unlockedDate = unlockedDate,
        currentProgress = currentProgress,
        maxProgress = maxProgress,
        xpReward = xpReward,
        milestoneRequirementBengali = milestoneRequirementBengali,
        milestoneRequirementEnglish = milestoneRequirementEnglish
    )
}

data class LeaderboardStudent(
    val rank: Int,
    val name: String,
    val school: String,
    val district: String = "কলকাতা",
    val xp: Int,
    val accuracy: Int,
    val streak: Int,
    val mockScore: Int = 85,
    val mockTotal: Int = 90,
    val mockPercentile: Double = 98.5,
    val bossVictories: Int = 10,
    val badgesCount: Int = 5,
    val isCurrentUser: Boolean = false,
    val badgeTitle: String = "Scholar",
    val subjectMockScores: Map<String, Int> = emptyMap()
)

data class FriendChallengeItem(
    val id: String,
    val opponentName: String,
    val opponentSchool: String,
    val subjectName: String,
    val questionCount: Int,
    val myScore: Int,
    val opponentScore: Int,
    val status: String // "Pending", "Completed", "Won", "Lost"
)

data class AiCoachInsight(
    val diagnosisBengali: String = "আপনার সার্বিক প্রস্তুতি সন্তোষজনক। তবে বয়েলের সূত্র ও ত্রিকোণমিতিতে কিছু ধারণা পরিমার্জন করা প্রয়োজন।",
    val diagnosisEnglish: String = "Your overall preparation is strong. Focus on Boyle's law numericals and Trigonometry identities.",
    val readinessScore: Int = 76,
    val topWeakTopic: String = "বয়েলের সূত্র ও পরম শূন্য তাপমাত্রা",
    val recommendedActionBengali: String = "আজ ১০ মিনিটের জন্য 'বয়েলের সূত্র ও পরম শূন্য তাপমাত্রা' রিভিশন করুন এবং ৫টি প্র্যাকটিস প্রশ্ন সমাধান করুন।",
    val recommendedActionEnglish: String = "Spend 10 minutes reviewing Boyle's law and solve 5 practice questions.",
    val dailyRecommendation: String = "ডেইলি ১০ স্পিড কুইজ সমাধান করে ৭ দিনের স্ট্রিক বজায় রাখুন!"
)

data class RecommendedPracticeSession(
    val id: String,
    val title: String,
    val description: String = "",
    val descriptionEnglish: String = "",
    val subjectId: String,
    val targetId: String?,
    val mode: AssessmentMode,
    val durationMinutes: Int,
    val questionsCount: Int,
    val xpReward: Int,
    val tagBengali: String,
    val tagEnglish: String,
    val tagColorHex: Long = 0xFF4F46E5,
    val isPriority: Boolean = false
) {
    val titleBengali: String get() = title
    val titleEnglish: String get() = title
    val descriptionBengali: String get() = description
}

data class SubjectRoadmapMilestone(
    val chapter: ChapterItem,
    val topics: List<TopicItem>,
    val status: ChapterRoadmapStatus,
    val madhyamikMarksWeight: Int = 18,
    val recommendedPracticeTitle: String? = null
)

enum class ChapterRoadmapStatus {
    COMPLETED,
    IN_PROGRESS,
    UPCOMING,
    LOCKED
}

