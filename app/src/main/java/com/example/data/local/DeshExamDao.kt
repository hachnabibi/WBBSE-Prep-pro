package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class TopicMistakeCount(
    val topicId: String,
    val mistakeCount: Int
)

@Dao
interface DeshExamDao {

    @Query("SELECT * FROM subjects ORDER BY code ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE classId = :classId ORDER BY code ASC")
    fun getSubjectsForClass(classId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE classId = :classId AND (:stream = 'all' OR stream = 'common' OR stream = 'all' OR stream = :stream OR stream LIKE '%' || :stream || '%') ORDER BY code ASC")
    fun getSubjectsForClassAndStream(classId: String, stream: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE classId = :classId")
    suspend fun getSubjectsForClassOnce(classId: String): List<SubjectEntity>

    @Query("DELETE FROM subjects WHERE classId = :classId")
    suspend fun deleteSubjectsForClass(classId: String)

    @Query("SELECT * FROM questions WHERE classId = :classId")
    suspend fun getQuestionsForClass(classId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE classId = :classId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsForClass(classId: String, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE (classId = :classId OR :classId = '') AND subjectId = :subjectId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsForSubjectAndClass(classId: String, subjectId: String, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM subjects")
    suspend fun getAllSubjectsOnce(): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY chapterNumber ASC")
    fun getChaptersForSubject(subjectId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY chapterNumber ASC")
    suspend fun getChaptersForSubjectOnce(subjectId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: String): ChapterEntity?

    @Query("SELECT * FROM chapters")
    suspend fun getAllChaptersOnce(): List<ChapterEntity>

    @Query("SELECT * FROM topics WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getTopicsForChapter(chapterId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    suspend fun getTopicsForChapterOnce(chapterId: String): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getTopicsForSubject(subjectId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    suspend fun getTopicsForSubjectOnce(subjectId: String): List<TopicEntity>

    @Query("SELECT * FROM topics")
    suspend fun getAllTopicsOnce(): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: String): TopicEntity?

    @Query("SELECT * FROM questions WHERE topicId = :topicId")
    suspend fun getQuestionsForTopic(topicId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId")
    suspend fun getQuestionsForChapter(chapterId: String): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions WHERE chapterId = :chapterId")
    suspend fun getQuestionCountForChapter(chapterId: String): Int

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsForSubject(subjectId: String, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<String>): List<QuestionEntity>

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<QuestionEntity>

    @Query("SELECT * FROM questions")
    fun getAllQuestionsFlow(): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionsCount(): Int

    @Query("SELECT COUNT(*) FROM question_attempts")
    suspend fun getAttemptsCountOnce(): Int

    @Query("SELECT COUNT(*) FROM question_attempts WHERE isCorrect = 1")
    suspend fun getCorrectAttemptsCountOnce(): Int

    @Query("SELECT SUM(timeSpentSeconds) FROM question_attempts")
    suspend fun getTotalTimeSpentSecondsOnce(): Long

    // Attempts and Weak Areas
    @Insert
    suspend fun insertAttempt(attempt: QuestionAttemptEntity)

    @Query("SELECT * FROM question_attempts ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAttempts(): Flow<List<QuestionAttemptEntity>>

    @Query("SELECT DISTINCT questionId FROM question_attempts WHERE isCorrect = 0 ORDER BY timestamp DESC LIMIT 30")
    suspend fun getWrongAttemptedQuestionIds(): List<String>

    @Query("SELECT topicId, COUNT(*) as mistakeCount FROM question_attempts WHERE isCorrect = 0 GROUP BY topicId ORDER BY mistakeCount DESC")
    suspend fun getTopicMistakeCounts(): List<TopicMistakeCount>

    // User Progress
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressOnce(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserProgressEntity)

    @Query("UPDATE topics SET masteryPercentage = :mastery WHERE id = :topicId")
    suspend fun updateTopicMastery(topicId: String, mastery: Int)

    @Query("UPDATE topics SET isUnlocked = 1 WHERE id = :topicId")
    suspend fun unlockTopic(topicId: String)

    @Query("UPDATE topics SET isUnlocked = 1")
    suspend fun unlockAllTopics()

    @Query("UPDATE chapters SET bossUnlocked = 1 WHERE id = :chapterId")
    suspend fun unlockChapterBoss(chapterId: String)

    @Query("UPDATE chapters SET bossUnlocked = 1")
    suspend fun unlockAllChapterBosses()

    @Query("UPDATE chapters SET bossCompleted = 1, masteryPercentage = 100 WHERE id = :chapterId")
    suspend fun markChapterBossCompleted(chapterId: String)

    @Query("UPDATE chapters SET bossCompleted = :isCompleted, masteryPercentage = :mastery WHERE id = :chapterId")
    suspend fun setChapterCompletedStatus(chapterId: String, isCompleted: Boolean, mastery: Int)

    @Query("UPDATE subjects SET completedChapters = :completed, masteryPercentage = :mastery WHERE id = :subjectId")
    suspend fun updateSubjectProgress(subjectId: String, completed: Int, mastery: Int)

    @Query("UPDATE chapters SET completedTopics = :completedTopics, masteryPercentage = :mastery WHERE id = :chapterId")
    suspend fun updateChapterProgress(chapterId: String, completedTopics: Int, mastery: Int)

    // Quests
    @Query("SELECT * FROM daily_quests")
    fun getDailyQuests(): Flow<List<DailyQuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateQuest(quest: DailyQuestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<DailyQuestEntity>)

    // Bulk Seed & Sync
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: String)

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

    @Query("DELETE FROM chapters")
    suspend fun deleteAllChapters()

    @Query("DELETE FROM topics")
    suspend fun deleteAllTopics()

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()

    // Bookmarks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun removeBookmark(id: String)

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    suspend fun isBookmarked(id: String): Boolean

    // Recently Viewed
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecentlyViewed(recentlyViewed: RecentlyViewedEntity)

    @Query("SELECT * FROM recently_viewed ORDER BY timestamp DESC LIMIT 5")
    fun getRecentlyViewed(): Flow<List<RecentlyViewedEntity>>

    // Video Playback Progress
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVideoProgress(progress: VideoProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllVideoProgress(progressList: List<VideoProgressEntity>)

    @Query("SELECT * FROM video_progress WHERE id = :id LIMIT 1")
    suspend fun getVideoProgressById(id: String): VideoProgressEntity?

    @Query("SELECT * FROM video_progress WHERE topicId = :topicId ORDER BY lastWatchedTimestamp DESC LIMIT 1")
    suspend fun getLatestVideoProgressForTopic(topicId: String): VideoProgressEntity?

    @Query("SELECT * FROM video_progress WHERE topicId = :topicId")
    fun getVideoProgressForTopicFlow(topicId: String): Flow<List<VideoProgressEntity>>

    @Query("SELECT * FROM video_progress ORDER BY lastWatchedTimestamp DESC")
    fun getAllVideoProgressFlow(): Flow<List<VideoProgressEntity>>

    @Query("SELECT * FROM video_progress ORDER BY lastWatchedTimestamp DESC")
    suspend fun getAllVideoProgressOnce(): List<VideoProgressEntity>

    // Education Boards Local Persistence
    @Query("SELECT * FROM education_boards ORDER BY orderIndex ASC")
    fun getAllBoardsFlow(): Flow<List<BoardEntity>>

    @Query("SELECT * FROM education_boards ORDER BY orderIndex ASC")
    suspend fun getAllBoardsOnce(): List<BoardEntity>

    @Query("SELECT * FROM education_boards WHERE id = :id LIMIT 1")
    suspend fun getBoardById(id: String): BoardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: BoardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBoards(boards: List<BoardEntity>)

    @Query("DELETE FROM education_boards WHERE id = :id")
    suspend fun deleteBoardById(id: String)

    // Academic Classes Local Persistence
    @Query("SELECT * FROM academic_classes ORDER BY classNumber ASC")
    fun getAllClassesFlow(): Flow<List<AcademicClassEntity>>

    @Query("SELECT * FROM academic_classes ORDER BY classNumber ASC")
    suspend fun getAllClassesOnce(): List<AcademicClassEntity>

    @Query("SELECT * FROM academic_classes WHERE board = :board ORDER BY classNumber ASC")
    fun getClassesForBoardFlow(board: String): Flow<List<AcademicClassEntity>>

    @Query("SELECT * FROM academic_classes WHERE board = :board ORDER BY classNumber ASC")
    suspend fun getClassesForBoardOnce(board: String): List<AcademicClassEntity>

    @Query("SELECT * FROM academic_classes WHERE id = :id LIMIT 1")
    suspend fun getClassById(id: String): AcademicClassEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(academicClass: AcademicClassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllClasses(classes: List<AcademicClassEntity>)

    @Query("DELETE FROM academic_classes WHERE id = :id")
    suspend fun deleteClassById(id: String)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteChapterById(id: String)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteTopicById(id: String)
}
