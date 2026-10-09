package com.example.data.firestore

import com.example.data.model.Difficulty
import com.example.data.model.QuestionType

/**
 * Types of tests supported in the Madhyamik preparation curriculum.
 */
enum class FirestoreTestType {
    TOPIC_PRACTICE,          // ৫-১০টি প্রশ্নের ছোট টপিক ভিত্তিক প্র্যাকটিস কুইজ
    CHAPTER_ASSESSMENT,      // অধ্যায় মূল্যায়ন (Chapter Assessment) - linked to specific chapters & grades
    SUBJECT_MASTERY_QUIZ,    // বিষয় দখল কুইজ (Subject Mastery Quiz) - linked to specific subjects & grades
    FULL_SYLLABUS_MOCK,      // সম্পূর্ণ সিলেবাস মক টেস্ট (Full Syllabus Mock) - linked to specific grades
    CHAPTER_BOSS_BATTLE,     // অধ্যায়ের সমাপনী চ্যালেঞ্জ (Boss Battle)
    SUBJECT_UNIT_TEST,       // সম্পূর্ণ বিষয়ের ইউনিট টেস্ট
    FULL_MOCK_TEST,          // সম্পূর্ণ ৯০ নম্বরের মাধ্যমিক টেস্ট পেপার মক টেস্ট
    PREVIOUS_YEAR_PAPER,     // বিগত বছরের মাধ্যমিক প্রশ্নপত্র (PYQ)
    SPEED_BLITZ,             // সময়ভিত্তিক দ্রুত উত্তর দেওয়ার কুইজ
    DAILY_10                 // প্রতিদিনের র্যান্ডম ১০টি প্রশ্ন
}

/**
 * Structured Document Model for Chapter Assessment linked to specific chapters and grades.
 */
data class FirestoreChapterAssessment(
    val id: String = "",
    val classId: String = "class_10_wbbse",
    val subjectId: String = "",
    val chapterId: String = "",
    val title: String = "",
    val description: String = "",
    val durationMinutes: Int = 20,
    val totalMarks: Int = 25,
    val questions: List<FirestoreQuestion> = emptyList(),
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
}

/**
 * Structured Document Model for Subject Mastery Quiz linked to specific subjects and grades.
 */
data class FirestoreSubjectMasteryQuiz(
    val id: String = "",
    val classId: String = "class_10_wbbse",
    val subjectId: String = "",
    val title: String = "",
    val description: String = "",
    val durationMinutes: Int = 30,
    val totalMarks: Int = 50,
    val questions: List<FirestoreQuestion> = emptyList(),
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
}

/**
 * Structured Document Model for Full Syllabus Mock linked to specific grades and board curricula.
 */
data class FirestoreFullSyllabusMock(
    val id: String = "",
    val classId: String = "class_10_wbbse",
    val title: String = "",
    val description: String = "",
    val durationMinutes: Int = 180, // 3 hours Madhyamik exam
    val totalMarks: Int = 90,
    val questions: List<FirestoreQuestion> = emptyList(),
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
}

/**
 * Board representation in Firestore (e.g. WBBPE, WBBSE, WBCHSE, CBSE).
 */
data class FirestoreEducationBoard(
    val id: String = "",
    val shortName: String = "",
    val fullNameBengali: String = "",
    val fullNameEnglish: String = "",
    val subtitleBengali: String = "",
    val levelBadge: String = "",
    val classRangeDescription: String = "",
    val iconName: String = "MenuBook",
    val orderIndex: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Class representation in Firestore (e.g. WBBSE Class 10 / Madhyamik).
 */
data class FirestoreClass(
    val id: String = "class_10_wbbse",
    val classNumber: Int = 10,
    val name: String = "দশম শ্রেণি",
    val nameBengaliValue: String = "",
    val nameEnglishValue: String = "",
    val board: String = "WBBSE",
    val targetExam: String = "WBBSE Madhyamik 2026",
    val targetYear: String = "2026",
    val description: String = "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ দশম শ্রেণীর সম্পূর্ণ সিলেবাস ও প্রশ্নব্যাংক",
    val totalSubjects: Int = 7,
    val isActive: Boolean = true,
    val orderIndex: Int = 0
) {
    @get:com.google.firebase.firestore.Exclude
    val nameBengali: String get() = nameBengaliValue.ifBlank { name }
    @get:com.google.firebase.firestore.Exclude
    val nameEnglish: String get() = nameEnglishValue.ifBlank { name }
    @get:com.google.firebase.firestore.Exclude
    val descriptionBengali: String get() = description
    @get:com.google.firebase.firestore.Exclude
    val descriptionEnglish: String get() = description
}

fun FirestoreEducationBoard.toEducationBoard(): com.example.data.model.EducationBoard = com.example.data.model.EducationBoard(
    id = id,
    shortName = shortName.ifBlank { id },
    fullNameBengali = fullNameBengali.ifBlank { shortName },
    fullNameEnglish = fullNameEnglish.ifBlank { shortName },
    subtitleBengali = subtitleBengali,
    levelBadge = levelBadge,
    classRangeDescription = classRangeDescription,
    iconName = iconName.ifBlank { "MenuBook" }
)

fun com.example.data.model.EducationBoard.toFirestore(orderIndex: Int = 0): FirestoreEducationBoard = FirestoreEducationBoard(
    id = id,
    shortName = shortName,
    fullNameBengali = fullNameBengali,
    fullNameEnglish = fullNameEnglish,
    subtitleBengali = subtitleBengali,
    levelBadge = levelBadge,
    classRangeDescription = classRangeDescription,
    iconName = iconName,
    orderIndex = orderIndex,
    isActive = true
)

fun FirestoreClass.toAcademicClass(): com.example.data.model.AcademicClass = com.example.data.model.AcademicClass(
    id = id,
    classNumber = classNumber,
    nameBengali = nameBengali.ifBlank { name },
    nameEnglish = nameEnglish.ifBlank { name },
    board = board,
    targetExam = targetExam.ifBlank { "$board $name" },
    descriptionBengali = descriptionBengali.ifBlank { description }
)

fun com.example.data.model.AcademicClass.toFirestore(orderIndex: Int = 0): FirestoreClass = FirestoreClass(
    id = id,
    classNumber = classNumber,
    name = nameBengali,
    nameBengaliValue = nameBengali,
    nameEnglishValue = nameEnglish,
    board = board,
    targetExam = targetExam,
    targetYear = "2026",
    description = descriptionBengali,
    totalSubjects = 7,
    isActive = true,
    orderIndex = orderIndex
)

/**
 * Subject representation in Firestore.
 */
data class FirestoreSubject(
    val id: String = "",
    val classId: String = "class_10_wbbse",
    val code: String = "",
    val name: String = "",
    val textbookName: String = "",
    val stream: String = "common",
    val totalChapters: Int = 0,
    val colorHex: Any = 0xFF3B82F6L,
    val iconName: String = "Book",
    val orderIndex: Int = 0
) {
    @get:com.google.firebase.firestore.Exclude
    val nameBengali: String get() = name
    @get:com.google.firebase.firestore.Exclude
    val nameEnglish: String get() = name

    val parsedColorHex: Long
        @com.google.firebase.firestore.Exclude
        get() {
            return when (val raw = colorHex) {
                is Number -> raw.toLong()
                is String -> {
                    try {
                        val cleaned = raw.trim()
                        when {
                            cleaned.startsWith("0x") || cleaned.startsWith("0X") -> {
                                cleaned.removePrefix("0x").removePrefix("0X").toLong(16)
                            }
                            cleaned.startsWith("#") -> {
                                cleaned.removePrefix("#").toLong(16) or 0xFF000000L
                            }
                            else -> cleaned.toLong()
                        }
                    } catch (e: Exception) {
                        0xFF3B82F6L
                    }
                }
                else -> 0xFF3B82F6L
            }
        }
}

/**
 * Chapter representation in Firestore.
 */
data class FirestoreChapter(
    val id: String = "",
    val classId: String = "class_10_wbbse",
    val subjectId: String = "",
    val chapterNumber: Int = 1,
    val title: String = "",
    val description: String = "",
    val totalTopics: Int = 0,
    val weightageMarks: Int = 15,
    val orderIndex: Int = 0,
    val contentData: String? = null,
    val solutionData: String? = null
) {
    @get:com.google.firebase.firestore.Exclude
    val titleBengali: String get() = title
    @get:com.google.firebase.firestore.Exclude
    val titleEnglish: String get() = title
    @get:com.google.firebase.firestore.Exclude
    val descriptionBengali: String get() = description
    @get:com.google.firebase.firestore.Exclude
    val descriptionEnglish: String get() = description
}

/**
 * Topic representation in Firestore.
 */
data class FirestoreTopic(
    val id: String = "",
    val classId: String = "class_10_wbbse",
    val subjectId: String = "",
    val chapterId: String = "",
    val orderIndex: Int = 1,
    val title: String = "",
    val summary: String = "",
    val keyPoints: List<String> = emptyList(),
    val formulae: List<String> = emptyList(),
    val questionCount: Int = 0,
    val contentData: String? = null,
    val solutionData: String? = null,
    val videoUrl: String? = null,
    val playlistJson: String? = null
) {
    @get:com.google.firebase.firestore.Exclude
    val titleBengali: String get() = title
    @get:com.google.firebase.firestore.Exclude
    val titleEnglish: String get() = title
    @get:com.google.firebase.firestore.Exclude
    val summaryBengali: String get() = summary
    @get:com.google.firebase.firestore.Exclude
    val summaryEnglish: String get() = summary
}

/**
 * Firestore Question Model.
 */
data class FirestoreQuestion(
    val id: String = "",
    val classId: String = "class_10_wbbse",
    val subjectId: String = "",
    val chapterId: String = "",
    val topicId: String = "",
    val type: String = QuestionType.MCQ.name,
    val difficulty: String = Difficulty.MEDIUM.name,
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0,
    val correctAnswerText: String = "",
    val explanation: String = "",
    val marks: Int = 1,
    val tags: List<String> = emptyList(),
    val podcastUrl: String = "",
    val podcastTitle: String = ""
)

/**
 * Test Template / Definition in Firestore.
 * Supports Topic-wise, Chapter-wise, Subject-wise and Full-syllabus Mock Tests.
 */
data class FirestoreTestTemplate(
    val id: String = "",
    val classId: String = "class_10_wbbse",
    val subjectId: String? = null,
    val chapterId: String? = null,
    val topicId: String? = null,
    val testType: String = FirestoreTestType.TOPIC_PRACTICE.name,
    val title: String = "",
    val description: String = "",
    val durationMinutes: Int = 15,
    val totalMarks: Int = 10,
    val passMarks: Int = 4,
    val questionCount: Int = 10,
    val negativeMarkingPerQuestion: Double = 0.0,
    val xpReward: Int = 100,
    val questions: List<FirestoreQuestion> = emptyList(),
    @get:com.google.firebase.firestore.PropertyName("published")
    @set:com.google.firebase.firestore.PropertyName("published")
    @field:com.google.firebase.firestore.PropertyName("published")
    var isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    @get:com.google.firebase.firestore.Exclude
    val published: Boolean get() = isPublished
    @get:com.google.firebase.firestore.Exclude
    val titleBengali: String get() = title
    @get:com.google.firebase.firestore.Exclude
    val titleEnglish: String get() = title
    @get:com.google.firebase.firestore.Exclude
    val descriptionBengali: String get() = description
    @get:com.google.firebase.firestore.Exclude
    val descriptionEnglish: String get() = description
}

/**
 * Student's Submitted Answer for a specific question in a test.
 */
data class FirestoreUserAnswer(
    val questionId: String = "",
    val selectedOptionIndex: Int = -1,
    val textAnswer: String = "",
    val isCorrect: Boolean = false,
    val marksAwarded: Int = 0,
    val timeSpentSeconds: Int = 0
)

/**
 * Student's Test Attempt Record in Firestore.
 */
data class FirestoreTestAttempt(
    val id: String = "",
    val userId: String = "",
    val testId: String = "",
    val classId: String = "class_10_wbbse",
    val subjectId: String? = null,
    val chapterId: String? = null,
    val topicId: String? = null,
    val testType: String = FirestoreTestType.TOPIC_PRACTICE.name,
    val testTitle: String = "",
    val totalQuestions: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val unattemptedCount: Int = 0,
    val scoreScored: Int = 0,
    val maxScore: Int = 0,
    val accuracyPercentage: Int = 0,
    val xpEarned: Int = 0,
    val timeTakenSeconds: Int = 0,
    val isPassed: Boolean = true,
    val isBossBeaten: Boolean = false,
    val submittedAt: Long = System.currentTimeMillis(),
    val answers: List<FirestoreUserAnswer> = emptyList()
) {
    @get:com.google.firebase.firestore.Exclude
    val testTitleBengali: String get() = testTitle
    @get:com.google.firebase.firestore.Exclude
    val testTitleEnglish: String get() = testTitle
}

/**
 * Student's Topic Mastery Record in Firestore.
 */
data class FirestoreTopicMastery(
    val topicId: String = "",
    val userId: String = "",
    val classId: String = "class_10_wbbse",
    val subjectId: String = "",
    val chapterId: String = "",
    val masteryPercentage: Int = 0,
    val totalQuestionsAttempted: Int = 0,
    val correctQuestionsCount: Int = 0,
    val isCompleted: Boolean = false,
    val lastPracticedTimestamp: Long = System.currentTimeMillis()
)

/**
 * District and its schools representation in Firestore.
 */
data class FirestoreDistrict(
    val id: String = "",
    val name: String = "",
    val schools: List<String> = emptyList()
)

/**
 * Student's Video Playback Progress in Firestore for multi-device sync & resume.
 */
data class FirestoreVideoProgress(
    val videoId: String = "",
    val topicId: String = "",
    val lessonTitle: String = "",
    val videoUrl: String = "",
    val lastPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isCompleted: Boolean = false,
    val percentage: Float = 0f,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Student's Chapter Progress in Firestore for multi-device sync.
 */
data class FirestoreChapterProgress(
    val chapterId: String = "",
    val subjectId: String = "",
    val isCompleted: Boolean = false,
    val masteryPercentage: Int = 0,
    val completedTopics: Int = 0,
    val totalTopics: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Student's Subject Progress in Firestore for multi-device sync.
 */
data class FirestoreSubjectProgress(
    val subjectId: String = "",
    val completedChapters: Int = 0,
    val totalChapters: Int = 0,
    val masteryPercentage: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Independent Podcast added via Admin Panel in Firestore.
 */
data class FirestorePodcast(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val duration: String = "",
    val narrator: String = "",
    val className: String = "Class 10",
    val audioUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

