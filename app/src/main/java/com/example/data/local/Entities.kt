package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val totalChapters: Int,
    val completedChapters: Int = 0,
    val masteryPercentage: Int = 0,
    val colorHex: Long,
    val classId: String = "class_10_wbbse",
    val stream: String = "common",
    val textbookName: String = ""
) {
    val nameBengali: String get() = com.example.data.model.ContentLocalization.getSubjectTitle(id, com.example.data.model.SubjectCode.values().find { it.id == code.lowercase() }, name, true)
    val nameEnglish: String get() = com.example.data.model.ContentLocalization.getSubjectTitle(id, com.example.data.model.SubjectCode.values().find { it.id == code.lowercase() }, name, false)
    val parsedStreams: List<String> get() = com.example.util.StreamHelper.parseStreams(stream)
    fun belongsToStream(targetStream: String?): Boolean = com.example.util.StreamHelper.matchesStream(stream, targetStream)

    constructor(
        id: String,
        code: String,
        nameBengali: String,
        nameEnglish: String = nameBengali,
        totalChapters: Int,
        completedChapters: Int = 0,
        masteryPercentage: Int = 0,
        colorHex: Long,
        classId: String = "class_10_wbbse",
        stream: String = "common",
        textbookName: String = ""
    ) : this(
        id = id,
        code = code,
        name = if (nameBengali.isNotBlank()) nameBengali else nameEnglish,
        totalChapters = totalChapters,
        completedChapters = completedChapters,
        masteryPercentage = masteryPercentage,
        colorHex = colorHex,
        classId = classId,
        stream = stream,
        textbookName = textbookName
    )
}

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val chapterNumber: Int,
    val title: String,
    val description: String,
    val totalTopics: Int,
    val completedTopics: Int,
    val bossUnlocked: Boolean,
    val bossCompleted: Boolean,
    val masteryPercentage: Int,
    val classId: String = "class_10_wbbse",
    val contentData: String? = null,
    val solutionData: String? = null
) {
    val titleBengali: String get() = com.example.data.model.ContentLocalization.getChapterTitle(id, title, true)
    val titleEnglish: String get() = com.example.data.model.ContentLocalization.getChapterTitle(id, title, false)
    val descriptionBengali: String get() = com.example.data.model.ContentLocalization.getChapterDescription(id, description, true)
    val descriptionEnglish: String get() = com.example.data.model.ContentLocalization.getChapterDescription(id, description, false)
}

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val chapterId: String,
    val subjectId: String,
    val orderIndex: Int,
    val title: String,
    val summary: String,
    val keyPointsJson: String,
    val isUnlocked: Boolean,
    val masteryPercentage: Int,
    val questionCount: Int = 0,
    val classId: String = "class_10_wbbse",
    val contentData: String? = null,
    val solutionData: String? = null,
    val videoUrl: String? = null,
    val playlistJson: String? = null,
    val summative: Int = 3,
    val author: String = ""
) {
    val titleBengali: String get() = com.example.data.model.ContentLocalization.getTopicTitle(id, title, true)
    val titleEnglish: String get() = com.example.data.model.ContentLocalization.getTopicTitle(id, title, false)
    val summaryBengali: String get() = com.example.data.model.ContentLocalization.getTopicSummary(id, summary, true)
    val summaryEnglish: String get() = com.example.data.model.ContentLocalization.getTopicSummary(id, summary, false)
}

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val chapterId: String,
    val subjectId: String,
    val type: String,
    val difficulty: String,
    val question: String,
    val optionsJson: String,
    val correctOptionIndex: Int,
    val correctAnswerText: String,
    val explanation: String,
    val marks: Int,
    val tagsJson: String,
    val pyqYear: String = "",
    val classId: String = "class_10_wbbse",
    val podcastUrl: String = "",
    val podcastTitle: String = ""
)

@Entity(tableName = "question_attempts")
data class QuestionAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: String,
    val topicId: String,
    val subjectId: String,
    val isCorrect: Boolean,
    val timestamp: Long,
    val timeSpentSeconds: Int
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val studentName: String = "",
    val school: String = "",
    val district: String = "",
    val targetExam: String = "WBBSE Madhyamik 2026",
    val profileImageUri: String = "",
    val currentXp: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDate: Long = System.currentTimeMillis(),
    val examReadinessPercentage: Int = 0,
    val mockScoresJson: String = "",
    val isPro: Boolean = false,
    val classId: String = "class_10_wbbse",
    val stream: String = "all",
    val coins: Int = 0,
    val aiCredits: Int = 0,
    val unlockedNotesJson: String = "1",
    val unlockedPodcastsJson: String = "",
    val fiftyFiftyLifelines: Int = 0,
    val battleEnergy: Int = 0,
    val isStreakRepaired: Boolean = false,
    val studyGoalsJson: String = "",
    val claimedCoinTaskIdsJson: String = ""
)

@Entity(tableName = "daily_quests")
data class DailyQuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val currentCount: Int,
    val targetCount: Int,
    val xpReward: Int,
    val isCompleted: Boolean
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String, // format: "pyq_{id}" or "textbook_{id}"
    val title: String,
    val subtitle: String,
    val type: String, // "PYQ" or "TEXTBOOK"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recently_viewed")
data class RecentlyViewedEntity(
    @PrimaryKey val id: String, // format: "pyq_{id}" or "textbook_{id}"
    val title: String,
    val subtitle: String,
    val type: String, // "PYQ" or "TEXTBOOK"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_progress")
data class VideoProgressEntity(
    @PrimaryKey val id: String, // format: "${topicId}_${videoId}"
    val topicId: String,
    val videoId: String,
    val title: String,
    val positionMs: Long,
    val durationMs: Long,
    val playbackSpeed: Float = 1.0f,
    val isCompleted: Boolean = false,
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "education_boards")
data class BoardEntity(
    @PrimaryKey val id: String,
    val shortName: String,
    val fullNameBengali: String,
    val fullNameEnglish: String = fullNameBengali,
    val subtitleBengali: String = "",
    val levelBadge: String = "",
    val classRangeDescription: String = "",
    val iconName: String = "MenuBook",
    val orderIndex: Int = 0
)

@Entity(tableName = "academic_classes")
data class AcademicClassEntity(
    @PrimaryKey val id: String,
    val classNumber: Int,
    val nameBengali: String,
    val nameEnglish: String = nameBengali,
    val board: String = "WBBSE",
    val targetExam: String = "",
    val descriptionBengali: String = "",
    val orderIndex: Int = 0
)

fun BoardEntity.toModel(): com.example.data.model.EducationBoard = com.example.data.model.EducationBoard(
    id = id,
    shortName = shortName,
    fullNameBengali = fullNameBengali,
    fullNameEnglish = fullNameEnglish,
    subtitleBengali = subtitleBengali,
    levelBadge = levelBadge,
    classRangeDescription = classRangeDescription,
    iconName = iconName
)

fun com.example.data.model.EducationBoard.toEntity(orderIndex: Int = 0): BoardEntity = BoardEntity(
    id = id,
    shortName = shortName,
    fullNameBengali = fullNameBengali,
    fullNameEnglish = fullNameEnglish,
    subtitleBengali = subtitleBengali,
    levelBadge = levelBadge,
    classRangeDescription = classRangeDescription,
    iconName = iconName,
    orderIndex = orderIndex
)

fun AcademicClassEntity.toModel(): com.example.data.model.AcademicClass = com.example.data.model.AcademicClass(
    id = id,
    classNumber = classNumber,
    nameBengali = nameBengali,
    nameEnglish = nameEnglish,
    board = board,
    targetExam = targetExam,
    descriptionBengali = descriptionBengali
)

fun com.example.data.model.AcademicClass.toEntity(orderIndex: Int = 0): AcademicClassEntity = AcademicClassEntity(
    id = id,
    classNumber = classNumber,
    nameBengali = nameBengali,
    nameEnglish = nameEnglish,
    board = board,
    targetExam = targetExam,
    descriptionBengali = descriptionBengali,
    orderIndex = orderIndex
)


