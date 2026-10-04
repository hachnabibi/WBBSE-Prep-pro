package com.example.data.repository

import com.example.data.local.BookmarkEntity
import com.example.data.local.RecentlyViewedEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.DeshExamDao
import com.example.data.local.QuestionAttemptEntity
import com.example.data.local.QuestionEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TopicEntity
import com.example.data.local.UserProgressEntity
import com.example.data.local.toModel
import com.example.data.local.toEntity
import com.example.data.firestore.toEducationBoard
import com.example.data.firestore.toAcademicClass
import android.util.Log
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

class DeshExamRepository(
    val dao: DeshExamDao,
    val context: android.content.Context? = null
) {

    // Test Scores & Endless Sets Persistence
    private val scoresMap = java.util.concurrent.ConcurrentHashMap<String, Int>()
    private val extraSetsMap = java.util.concurrent.ConcurrentHashMap<String, Int>()

    fun getTopicSetScore(topicId: String, setNumber: Int): Int {
        val key = "topic_${topicId}_set_$setNumber"
        val memory = scoresMap[key]
        if (memory != null) return memory
        val disk = context?.getSharedPreferences("desh_exam_test_scores", android.content.Context.MODE_PRIVATE)?.getInt(key, -1) ?: -1
        if (disk != -1) scoresMap[key] = disk
        return disk
    }

    fun saveTopicSetScore(topicId: String, setNumber: Int, scorePercent: Int) {
        val key = "topic_${topicId}_set_$setNumber"
        val existing = getTopicSetScore(topicId, setNumber)
        val bestScore = maxOf(existing, scorePercent)
        scoresMap[key] = bestScore
        context?.getSharedPreferences("desh_exam_test_scores", android.content.Context.MODE_PRIVATE)?.edit()?.putInt(key, bestScore)?.apply()
    }

    fun getTopicMockScore(topicId: String): Int {
        val key = "topic_${topicId}_mock"
        val memory = scoresMap[key]
        if (memory != null) return memory
        val disk = context?.getSharedPreferences("desh_exam_test_scores", android.content.Context.MODE_PRIVATE)?.getInt(key, -1) ?: -1
        if (disk != -1) scoresMap[key] = disk
        return disk
    }

    fun saveTopicMockScore(topicId: String, scorePercent: Int) {
        val key = "topic_${topicId}_mock"
        val existing = getTopicMockScore(topicId)
        val bestScore = maxOf(existing, scorePercent)
        scoresMap[key] = bestScore
        context?.getSharedPreferences("desh_exam_test_scores", android.content.Context.MODE_PRIVATE)?.edit()?.putInt(key, bestScore)?.apply()
    }

    fun getTopicSpeedScore(topicId: String): Int {
        val key = "topic_${topicId}_speed"
        val memory = scoresMap[key]
        if (memory != null) return memory
        val disk = context?.getSharedPreferences("desh_exam_test_scores", android.content.Context.MODE_PRIVATE)?.getInt(key, -1) ?: -1
        if (disk != -1) scoresMap[key] = disk
        return disk
    }

    fun saveTopicSpeedScore(topicId: String, scorePercent: Int) {
        val key = "topic_${topicId}_speed"
        val existing = getTopicSpeedScore(topicId)
        val bestScore = maxOf(existing, scorePercent)
        scoresMap[key] = bestScore
        context?.getSharedPreferences("desh_exam_test_scores", android.content.Context.MODE_PRIVATE)?.edit()?.putInt(key, bestScore)?.apply()
    }

    fun getExtraGeneratedSets(topicId: String): Int {
        val key = "topic_${topicId}_extra_sets"
        val memory = extraSetsMap[key]
        if (memory != null) return memory
        val disk = context?.getSharedPreferences("desh_exam_test_scores", android.content.Context.MODE_PRIVATE)?.getInt(key, 0) ?: 0
        extraSetsMap[key] = disk
        return disk
    }

    fun addExtraGeneratedSet(topicId: String, count: Int = 1): Int {
        val current = getExtraGeneratedSets(topicId)
        val updated = current + count
        val key = "topic_${topicId}_extra_sets"
        extraSetsMap[key] = updated
        context?.getSharedPreferences("desh_exam_test_scores", android.content.Context.MODE_PRIVATE)?.edit()?.putInt(key, updated)?.apply()
        return updated
    }

    fun getSubjectTestScore(testId: String): Int {
        val key = "test_$testId"
        val memory = scoresMap[key]
        if (memory != null) return memory
        val disk = context?.getSharedPreferences("desh_exam_test_scores", android.content.Context.MODE_PRIVATE)?.getInt(key, -1) ?: -1
        if (disk != -1) scoresMap[key] = disk
        return disk
    }

    fun saveSubjectTestScore(testId: String, scorePercent: Int) {
        val key = "test_$testId"
        val existing = getSubjectTestScore(testId)
        val bestScore = maxOf(existing, scorePercent)
        scoresMap[key] = bestScore
        context?.getSharedPreferences("desh_exam_test_scores", android.content.Context.MODE_PRIVATE)?.edit()?.putInt(key, bestScore)?.apply()
    }

    private val _badgesFlow = MutableStateFlow<List<BadgeItem>>(createInitialBadges())
    val badgesFlow: StateFlow<List<BadgeItem>> = _badgesFlow.asStateFlow()

    fun getBadges(): List<BadgeItem> = _badgesFlow.value

    fun createInitialBadges(): List<BadgeItem> = listOf(
        BadgeItem(
            id = "badge_perfect_score",
            nameBengali = "পারফেক্ট স্কোর (নিখুঁত ১০০%)",
            nameEnglish = "Perfect Score",
            descriptionBengali = "যেকোনো টেস্ট বা কুইজে একটিও ভুল না করে ১০০% নিখুঁত নম্বর অর্জন করেছেন।",
            descriptionEnglish = "Score 100% in any quiz or assessment without any wrong answers.",
            category = BadgeCategory.ACCURACY,
            tier = BadgeTier.GOLD,
            iconName = "CheckCircle",
            isUnlocked = true,
            unlockedDate = "১৪ সেপ্টেম্বর ২০২৬",
            currentProgress = 1,
            maxProgress = 1,
            xpReward = 300,
            milestoneRequirementBengali = "একটি কুইজে ১০০% স্কোর অর্জন করুন",
            milestoneRequirementEnglish = "Score 100% in any quiz"
        ),
        BadgeItem(
            id = "badge_early_bird",
            nameBengali = "ভোরের সাধক (Early Bird)",
            nameEnglish = "Early Bird",
            descriptionBengali = "সকাল ৫:০০ থেকে ৮:০০ টার মধ্যে নিষ্ঠাভরে পড়াশোনা বা কুইজ সম্পন্ন করেছেন।",
            descriptionEnglish = "Complete a study or test session between 5:00 AM and 8:00 AM.",
            category = BadgeCategory.TIME,
            tier = BadgeTier.SILVER,
            iconName = "WbSunny",
            isUnlocked = true,
            unlockedDate = "১৮ সেপ্টেম্বর ২০২৬",
            currentProgress = 1,
            maxProgress = 1,
            xpReward = 200,
            milestoneRequirementBengali = "সকাল ৫:০০ - ৮:০০ টার মধ্যে কুইজ দিন",
            milestoneRequirementEnglish = "Take a quiz between 5:00 AM - 8:00 AM"
        ),
        BadgeItem(
            id = "badge_night_owl",
            nameBengali = "রাতের তপস্বী (Night Owl)",
            nameEnglish = "Night Owl",
            descriptionBengali = "রাত ১০:০০ টার পর একাগ্রতার সাথে মক টেস্ট বা রিভিশন কুইজ শেষ করেছেন।",
            descriptionEnglish = "Complete a practice session or mock test after 10:00 PM.",
            category = BadgeCategory.TIME,
            tier = BadgeTier.SILVER,
            iconName = "Bedtime",
            isUnlocked = false,
            unlockedDate = null,
            currentProgress = 0,
            maxProgress = 1,
            xpReward = 200,
            milestoneRequirementBengali = "রাত ১০:০০ টার পর কোনো টেস্ট সম্পন্ন করুন",
            milestoneRequirementEnglish = "Complete a test after 10:00 PM"
        ),
        BadgeItem(
            id = "badge_consistent_learner",
            nameBengali = "ধারাবাহিক শিক্ষার্থী (Consistent Learner)",
            nameEnglish = "Consistent Learner",
            descriptionBengali = "টানা ৭ দিন নিয়ম করে প্রতিদিন DeshExam-এ অনুশীলন ও ৭-দিনের স্ট্রিক অর্জন করেছেন।",
            descriptionEnglish = "Maintain an unbroken 7-day study streak with daily practice on DeshExam.",
            category = BadgeCategory.CONSISTENCY,
            tier = BadgeTier.GOLD,
            iconName = "LocalFireDepartment",
            isUnlocked = true,
            unlockedDate = "১৯ সেপ্টেম্বর ২০২৬",
            currentProgress = 7,
            maxProgress = 7,
            xpReward = 350,
            milestoneRequirementBengali = "টানা ৭ দিনের স্ট্রিক সম্পন্ন করুন",
            milestoneRequirementEnglish = "Reach a 7-day streak"
        ),
        BadgeItem(
            id = "badge_speed_demon",
            nameBengali = "বিদ্যুৎ গতি কুইজার (Speed Demon)",
            nameEnglish = "Speed Demon",
            descriptionBengali = "স্পিড ব্লিৎজ মোডে দ্রুত সময়ে ৯০%+ নির্ভুলতার সাথে প্রশ্ন সমাধান করেছেন।",
            descriptionEnglish = "Solve rapid-fire questions in Speed Blitz mode with >90% accuracy.",
            category = BadgeCategory.ACCURACY,
            tier = BadgeTier.SILVER,
            iconName = "Bolt",
            isUnlocked = true,
            unlockedDate = "১৬ সেপ্টেম্বর ২০২৬",
            currentProgress = 1,
            maxProgress = 1,
            xpReward = 250,
            milestoneRequirementBengali = "স্পিড ব্লিৎজে ৯০%+ সঠিক উত্তর দিন",
            milestoneRequirementEnglish = "Score >90% in Speed Blitz"
        ),
        BadgeItem(
            id = "badge_boss_slayer",
            nameBengali = "অধ্যায় বস বিজয়ী (Boss Slayer)",
            nameEnglish = "Boss Slayer",
            descriptionBengali = "অধ্যায়ের ১০০০ HP দানব বসকে পরাজিত করে অধ্যায় মাস্টারি ট্রফি অর্জন করেছেন।",
            descriptionEnglish = "Defeat a 1000 HP Citadel Boss to claim complete chapter mastery.",
            category = BadgeCategory.MASTERY,
            tier = BadgeTier.GOLD,
            iconName = "Shield",
            isUnlocked = true,
            unlockedDate = "১৬ সেপ্টেম্বর ২০২৬",
            currentProgress = 1,
            maxProgress = 1,
            xpReward = 400,
            milestoneRequirementBengali = "যেকোনো অধ্যায়ের বস ব্যাটল জয় করুন",
            milestoneRequirementEnglish = "Defeat any chapter boss"
        ),
        BadgeItem(
            id = "badge_madhyamik_ready",
            nameBengali = "মাধ্যমিক চ্যাম্পিয়ন (Madhyamik Ready)",
            nameEnglish = "Madhyamik Ready",
            descriptionBengali = "পূর্ণাঙ্গ মাধ্যমিক মডেল টেস্টে ৮০% বা তার বেশি স্কোর অর্জন করেছেন।",
            descriptionEnglish = "Score 80%+ in a full 90-mark Madhyamik model exam.",
            category = BadgeCategory.EXAM,
            tier = BadgeTier.DIAMOND,
            iconName = "WorkspacePremium",
            isUnlocked = false,
            unlockedDate = null,
            currentProgress = 82,
            maxProgress = 90,
            xpReward = 500,
            milestoneRequirementBengali = "৯০ নম্বরের পূর্ণাঙ্গ মক টেস্টে ৮০%+ পান",
            milestoneRequirementEnglish = "Score 80%+ in 90-mark Mock Exam"
        ),
        BadgeItem(
            id = "badge_first_step",
            nameBengali = "প্রথম পদক্ষেপ",
            nameEnglish = "First Step",
            descriptionBengali = "DeshExam অ্যাপে আপনার প্রথম অনুশীলন পর্ব সফলভাবে সম্পন্ন করেছেন।",
            descriptionEnglish = "Completed your very first practice session on DeshExam.",
            category = BadgeCategory.CONSISTENCY,
            tier = BadgeTier.BRONZE,
            iconName = "MilitaryTech",
            isUnlocked = true,
            unlockedDate = "১২ সেপ্টেম্বর ২০২৬",
            currentProgress = 1,
            maxProgress = 1,
            xpReward = 100,
            milestoneRequirementBengali = "প্রথম যেকোনো কুইজ বা টেস্ট শেষ করুন",
            milestoneRequirementEnglish = "Complete your first quiz"
        ),
        BadgeItem(
            id = "badge_polymath",
            nameBengali = "সর্বশাস্ত্রবিশারদ (Polymath Scholar)",
            nameEnglish = "Polymath Scholar",
            descriptionBengali = "মাধ্যমিকের সব ৭টি বিষয়েই সক্রিয় অধ্যায় অনুশীলন ও দক্ষতা গড়ে তুলেছেন।",
            descriptionEnglish = "Actively master topics across all 7 WBBSE Madhyamik subjects.",
            category = BadgeCategory.MASTERY,
            tier = BadgeTier.DIAMOND,
            iconName = "AutoAwesome",
            isUnlocked = false,
            unlockedDate = null,
            currentProgress = 3,
            maxProgress = 7,
            xpReward = 600,
            milestoneRequirementBengali = "৭টি বিষয়ে অধ্যায় মাস্টারি অর্জন করুন",
            milestoneRequirementEnglish = "Master topics across all 7 subjects"
        ),
        BadgeItem(
            id = "badge_accuracy_master",
            nameBengali = "নির্ভুল গণক (Accuracy Master)",
            nameEnglish = "Accuracy Master",
            descriptionBengali = "কমপক্ষে ৫০টি প্রশ্নের মধ্যে ৮৫%+ সামগ্রিক নির্ভুলতা সফলভাবে বজায় রাখা।",
            descriptionEnglish = "Maintain over 85% overall accuracy across 50+ solved questions.",
            category = BadgeCategory.ACCURACY,
            tier = BadgeTier.GOLD,
            iconName = "EmojiEvents",
            isUnlocked = true,
            unlockedDate = "১৭ সেপ্টেম্বর ২০২৬",
            currentProgress = 82,
            maxProgress = 85,
            xpReward = 350,
            milestoneRequirementBengali = "৮৫%+ সার্বিক নির্ভুলতা ধরে রাখুন",
            milestoneRequirementEnglish = "Maintain 85%+ accuracy"
        )
    )

    suspend fun checkAndAwardBadges(
        scorePercent: Int,
        isFullScore: Boolean,
        totalQuestions: Int,
        timeSpentSeconds: Int,
        mode: AssessmentMode,
        currentStreak: Int,
        hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): List<BadgeItem> = withContext(Dispatchers.IO) {
        val currentBadges = _badgesFlow.value
        val newlyUnlocked = mutableListOf<BadgeItem>()
        val todayStr = "আজকের দিনে"

        val updatedBadges = currentBadges.map { badge ->
            if (badge.isUnlocked) {
                badge
            } else {
                var shouldUnlock = false
                var newProgress = badge.currentProgress

                when (badge.id) {
                    "badge_perfect_score" -> {
                        if (isFullScore && totalQuestions >= 4) {
                            shouldUnlock = true
                            newProgress = 1
                        }
                    }
                    "badge_early_bird" -> {
                        if (hourOfDay in 5..8) {
                            shouldUnlock = true
                            newProgress = 1
                        }
                    }
                    "badge_night_owl" -> {
                        if (hourOfDay >= 22 || hourOfDay in 0..4) {
                            shouldUnlock = true
                            newProgress = 1
                        }
                    }
                    "badge_consistent_learner" -> {
                        newProgress = currentStreak.coerceAtMost(7)
                        if (currentStreak >= 7) {
                            shouldUnlock = true
                        }
                    }
                    "badge_speed_demon" -> {
                        if (mode == AssessmentMode.SPEED_BLITZ && scorePercent >= 90) {
                            shouldUnlock = true
                            newProgress = 1
                        }
                    }
                    "badge_boss_slayer" -> {
                        if (mode == AssessmentMode.BOSS_BATTLE && scorePercent >= 70) {
                            shouldUnlock = true
                            newProgress = 1
                        }
                    }
                    "badge_madhyamik_ready" -> {
                        if (mode == AssessmentMode.MOCK_TEST && scorePercent >= 80) {
                            shouldUnlock = true
                            newProgress = 90
                        }
                    }
                    "badge_first_step" -> {
                        shouldUnlock = true
                        newProgress = 1
                    }
                }

                if (shouldUnlock) {
                    val unlockedBadge = badge.copy(
                        isUnlocked = true,
                        unlockedDate = todayStr,
                        currentProgress = badge.maxProgress
                    )
                    newlyUnlocked.add(unlockedBadge)
                    unlockedBadge
                } else {
                    badge.copy(currentProgress = newProgress)
                }
            }
        }

        _badgesFlow.value = updatedBadges

        // Award XP for newly unlocked badges
        if (newlyUnlocked.isNotEmpty()) {
            val totalBonusXp = newlyUnlocked.sumOf { it.xpReward }
            val progress = dao.getUserProgressOnce() ?: UserProgressEntity()
            dao.insertOrUpdateProgress(progress.copy(currentXp = progress.currentXp + totalBonusXp))
        }

        newlyUnlocked
    }


    val allSubjects: Flow<List<SubjectItem>> = dao.getAllSubjects().map { entities ->
        entities.map { entity ->
            val codeEnum = SubjectCode.values().find { it.id == entity.code } ?: SubjectCode.PHYSICAL_SCIENCE
            SubjectItem(
                id = entity.id,
                code = codeEnum,
                nameBengali = entity.name,
                nameEnglish = entity.name,
                totalChapters = entity.totalChapters,
                completedChapters = entity.completedChapters,
                masteryPercentage = entity.masteryPercentage,
                colorHex = entity.colorHex
            )
        }
    }.flowOn(Dispatchers.IO)

    fun getSubjectsForClass(classId: String, stream: String = "all"): Flow<List<SubjectItem>> =
        dao.getSubjectsForClassAndStream(classId, stream).map { entities ->
            val matchingEntities = if (stream == "all" || stream.isBlank()) {
                entities
            } else {
                entities.filter { com.example.util.StreamHelper.matchesStream(it.stream, stream) }
            }
            matchingEntities.map { entity ->
                val codeEnum = SubjectCode.values().find { it.id == entity.code.lowercase() }
                    ?: SubjectCode.values().find { it.name.equals(entity.code, ignoreCase = true) }
                    ?: SubjectCode.PHYSICAL_SCIENCE
                SubjectItem(
                    id = entity.id,
                    code = codeEnum,
                    nameBengali = entity.name,
                    nameEnglish = entity.name,
                    textbookName = entity.textbookName,
                    totalChapters = entity.totalChapters,
                    completedChapters = entity.completedChapters,
                    masteryPercentage = entity.masteryPercentage,
                    colorHex = entity.colorHex,
                    stream = entity.stream
                )
            }.distinctBy { it.id }
        }.flowOn(Dispatchers.IO)

    suspend fun getSubjectsForClassOnce(classId: String): List<SubjectEntity> =
        dao.getSubjectsForClassOnce(classId)

    suspend fun getSubjectById(subjectId: String): SubjectEntity? = dao.getSubjectById(subjectId)

    // =========================================================================
    // EDUCATION BOARDS & ACADEMIC CLASSES (Local-first, Room + Memory)
    // =========================================================================

    fun getAllBoardsFlow(): Flow<List<EducationBoard>> = dao.getAllBoardsFlow().map { list ->
        if (list.isEmpty()) {
            AcademicClassProvider.DEFAULT_BOARDS
        } else {
            list.map { b ->
                EducationBoard(
                    id = b.id,
                    shortName = b.shortName,
                    fullNameBengali = b.fullNameBengali,
                    fullNameEnglish = b.fullNameEnglish,
                    subtitleBengali = b.subtitleBengali,
                    levelBadge = b.levelBadge,
                    classRangeDescription = b.classRangeDescription,
                    iconName = b.iconName
                )
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getAllBoardsOnce(): List<EducationBoard> = withContext(Dispatchers.IO) {
        val list = dao.getAllBoardsOnce()
        if (list.isEmpty()) {
            AcademicClassProvider.DEFAULT_BOARDS
        } else {
            list.map { b ->
                EducationBoard(
                    id = b.id,
                    shortName = b.shortName,
                    fullNameBengali = b.fullNameBengali,
                    fullNameEnglish = b.fullNameEnglish,
                    subtitleBengali = b.subtitleBengali,
                    levelBadge = b.levelBadge,
                    classRangeDescription = b.classRangeDescription,
                    iconName = b.iconName
                )
            }
        }
    }

    suspend fun saveBoard(board: EducationBoard) = withContext(Dispatchers.IO) {
        val entity = com.example.data.local.BoardEntity(
            id = board.id,
            shortName = board.shortName,
            fullNameBengali = board.fullNameBengali,
            fullNameEnglish = board.fullNameEnglish,
            subtitleBengali = board.subtitleBengali,
            levelBadge = board.levelBadge,
            classRangeDescription = board.classRangeDescription,
            iconName = board.iconName
        )
        dao.insertBoard(entity)
        AcademicClassProvider.addBoard(board)
    }

    suspend fun deleteBoard(boardId: String) = withContext(Dispatchers.IO) {
        dao.deleteBoardById(boardId)
        AcademicClassProvider.deleteBoard(boardId)
    }

    fun getAllClassesFlow(): Flow<List<AcademicClass>> = dao.getAllClassesFlow().map { list ->
        if (list.isEmpty()) {
            AcademicClassProvider.DEFAULT_CLASSES
        } else {
            list.map { c ->
                AcademicClass(
                    id = c.id,
                    classNumber = c.classNumber,
                    nameBengali = c.nameBengali,
                    nameEnglish = c.nameEnglish,
                    board = c.board,
                    targetExam = c.targetExam,
                    descriptionBengali = c.descriptionBengali
                )
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getAllClassesOnce(): List<AcademicClass> = withContext(Dispatchers.IO) {
        val list = dao.getAllClassesOnce()
        if (list.isEmpty()) {
            AcademicClassProvider.DEFAULT_CLASSES
        } else {
            list.map { c ->
                AcademicClass(
                    id = c.id,
                    classNumber = c.classNumber,
                    nameBengali = c.nameBengali,
                    nameEnglish = c.nameEnglish,
                    board = c.board,
                    targetExam = c.targetExam,
                    descriptionBengali = c.descriptionBengali
                )
            }
        }
    }

    suspend fun saveClass(academicClass: AcademicClass) = withContext(Dispatchers.IO) {
        val entity = com.example.data.local.AcademicClassEntity(
            id = academicClass.id,
            classNumber = academicClass.classNumber,
            nameBengali = academicClass.nameBengali,
            nameEnglish = academicClass.nameEnglish,
            board = academicClass.board,
            targetExam = academicClass.targetExam,
            descriptionBengali = academicClass.descriptionBengali
        )
        dao.insertClass(entity)
        AcademicClassProvider.addClass(academicClass)
    }

    suspend fun deleteClass(classId: String) = withContext(Dispatchers.IO) {
        dao.deleteClassById(classId)
        AcademicClassProvider.deleteClass(classId)
    }

    suspend fun insertBoardsFromRemote(boards: List<EducationBoard>) = withContext(Dispatchers.IO) {
        if (boards.isNotEmpty()) {
            val entities = boards.mapIndexed { index, b -> b.toEntity(index) }
            dao.insertAllBoards(entities)
            AcademicClassProvider.updateBoards(boards)
        }
    }

    suspend fun insertClassesFromRemote(classes: List<AcademicClass>) = withContext(Dispatchers.IO) {
        if (classes.isNotEmpty()) {
            val entities = classes.mapIndexed { index, c -> c.toEntity(index) }
            dao.insertAllClasses(entities)
            AcademicClassProvider.updateClasses(classes)
        }
    }

    /**
     * Local-First loader for Education Boards & Academic Classes:
     * 1. Prothom local data (Room database) request korbe.
     * 2. Local database-e data thakle ta songe songe memory provider-e update kore use hobe.
     * 3. Local database khali thakle (data na pele) tabei Firebase Firestore-e request pathabe.
     * 4. Firebase theke data pele ta execute hobe (Local Room database-e save hobe ebong UI-te update hobe).
     * 5. Ekdom prothom bar Firebase-eo data na thakle default boards & classes die initialize hobe.
     */
    suspend fun loadBoardsAndClassesLocalFirst(firestoreService: com.example.data.firestore.FirestoreService): Pair<List<EducationBoard>, List<AcademicClass>> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Prothom local database request kora
            val localBoards = dao.getAllBoardsOnce()
            val localClasses = dao.getAllClassesOnce()

            if (localBoards.isNotEmpty() && localClasses.isNotEmpty()) {
                val boards = localBoards.map { it.toModel() }
                val classes = localClasses.map { it.toModel() }
                AcademicClassProvider.updateBoards(boards)
                AcademicClassProvider.updateClasses(classes)
                Log.d("DeshExamRepo", "Local-first success: Loaded ${boards.size} boards & ${classes.size} classes from local database")
                return@withContext Pair(boards, classes)
            }

            // Step 2: Local data na pele tabei Firebase Firestore-e request kora
            Log.d("DeshExamRepo", "Local boards/classes not found in local database. Requesting from Firebase Firestore...")
            val fbBoardsRes = firestoreService.getAllBoards()
            val fbClassesRes = firestoreService.getAllClasses()

            val remoteBoards = fbBoardsRes.getOrNull().orEmpty()
            val remoteClasses = fbClassesRes.getOrNull().orEmpty()

            val finalBoards: List<EducationBoard>
            val finalClasses: List<AcademicClass>

            if (remoteBoards.isNotEmpty() || remoteClasses.isNotEmpty()) {
                finalBoards = if (remoteBoards.isNotEmpty()) {
                    remoteBoards.map { it.toEducationBoard() }
                } else {
                    AcademicClassProvider.DEFAULT_BOARDS
                }
                finalClasses = if (remoteClasses.isNotEmpty()) {
                    remoteClasses.map { it.toAcademicClass() }
                } else {
                    AcademicClassProvider.DEFAULT_CLASSES
                }
                Log.d("DeshExamRepo", "Firebase response received. Executing local database persistence...")
            } else {
                // If neither local nor Firebase has custom records yet, seed defaults
                finalBoards = AcademicClassProvider.DEFAULT_BOARDS
                finalClasses = AcademicClassProvider.DEFAULT_CLASSES
                Log.d("DeshExamRepo", "No data in Firebase either. Executing default West Bengal boards & classes setup...")
            }

            // Step 3: Execute (Local Room-e save kora ebong provider update kora)
            val boardEntities = finalBoards.mapIndexed { idx, b -> b.toEntity(idx) }
            val classEntities = finalClasses.mapIndexed { idx, c -> c.toEntity(idx) }
            dao.insertAllBoards(boardEntities)
            dao.insertAllClasses(classEntities)
            AcademicClassProvider.updateBoards(finalBoards)
            AcademicClassProvider.updateClasses(finalClasses)

            return@withContext Pair(finalBoards, finalClasses)
        } catch (e: Exception) {
            Log.e("DeshExamRepo", "Error in loadBoardsAndClassesLocalFirst: ${e.message}", e)
            val boards = AcademicClassProvider.DEFAULT_BOARDS
            val classes = AcademicClassProvider.DEFAULT_CLASSES
            AcademicClassProvider.updateBoards(boards)
            AcademicClassProvider.updateClasses(classes)
            return@withContext Pair(boards, classes)
        }
    }

    suspend fun ensureSubjectContentSeeded(subjectId: String) {
        val existing = dao.getChaptersForSubjectOnce(subjectId)
        if (existing.isNotEmpty()) return

        val subjectEntity = dao.getSubjectById(subjectId)
        val textbook = TextbookRepository.allTextbooks.find { 
            subjectId.contains(it.className.lowercase().replace(" ", "_")) && 
            (subjectId.contains(it.subjectCode.lowercase()) || (subjectEntity != null && subjectEntity.code.equals(it.subjectCode, ignoreCase = true)))
        } ?: TextbookRepository.allTextbooks.find {
            subjectEntity != null && subjectEntity.code.equals(it.subjectCode, ignoreCase = true)
        }

        val chapterNames = if (textbook != null && textbook.chapters.isNotEmpty()) {
            textbook.chapters
        } else {
            listOf(
                "অধ্যায় ১: প্রারম্ভিক ধারণা ও মূল পাঠ",
                "অধ্যায় ২: মৌলিক পাঠ ও বিশ্লেষণ",
                "অধ্যায় ৩: গভীর পর্যালোচনা ও প্রয়োগ",
                "অধ্যায় ৪: গুরুত্বপূর্ণ প্রশ্নাবলী ও সমাধান",
                "অধ্যায় ৫: সামগ্রিক মূল্যায়ন ও রিভিশন"
            )
        }

        val targetClassId = subjectEntity?.classId ?: when {
            subjectId.contains("class_5") -> "class_5_wbbse"
            subjectId.contains("class_6") -> "class_6_wbbse"
            subjectId.contains("class_7") -> "class_7_wbbse"
            subjectId.contains("class_8") -> "class_8_wbbse"
            subjectId.contains("class_9") -> "class_9_wbbse"
            subjectId.contains("class_11") -> "class_11_wbchse"
            subjectId.contains("class_12") -> "class_12_wbchse"
            else -> "class_10_wbbse"
        }

        val chapters = chapterNames.mapIndexed { idx, chTitle ->
            ChapterEntity(
                id = "${subjectId}_ch${idx + 1}",
                subjectId = subjectId,
                chapterNumber = idx + 1,
                title = chTitle,
                description = "বোর্ড পরীক্ষার উপযোগী গুরুত্বপূর্ণ ধারণা, সমাধান ও অনুশীলন",
                totalTopics = 2,
                completedTopics = 0,
                bossUnlocked = idx == 0,
                bossCompleted = false,
                masteryPercentage = 0,
                classId = targetClassId
            )
        }
        dao.insertChapters(chapters)

        val topicsToInsert = mutableListOf<TopicEntity>()
        val questionsToInsert = mutableListOf<QuestionEntity>()

        chapters.forEachIndexed { chIdx, ch ->
            val top1 = TopicEntity(
                id = "${ch.id}_top1",
                chapterId = ch.id,
                subjectId = subjectId,
                orderIndex = 1,
                title = "${ch.title} - মূল ধারণা ও নিয়মাবলী",
                summary = "এই টপিকের সমস্ত গুরুত্বপূর্ণ তত্ত্ব, সংজ্ঞা এবং বোর্ড পরীক্ষার জন্য প্রয়োজনীয় সূত্র।",
                keyPointsJson = "মৌলিক সংজ্ঞা|সূত্র ও নিয়ম|বাস্তব প্রয়োগ|পরীক্ষার শর্টকাট",
                isUnlocked = true,
                masteryPercentage = 0,
                questionCount = 5,
                classId = targetClassId
            )
            val top2 = TopicEntity(
                id = "${ch.id}_top2",
                chapterId = ch.id,
                subjectId = subjectId,
                orderIndex = 2,
                title = "${ch.title} - MCQ ও সংক্ষিপ্ত প্রশ্নোত্তর",
                summary = "পরীক্ষায় বারবার আসা বাছাই করা বহুবিকল্পীয় এবং সংক্ষিপ্ত প্রশ্নোত্তর।",
                keyPointsJson = "১ নম্বরের MCQ|সঠিক উত্তর সংকেত|ব্যাখ্যা|মক টেস্ট প্র্যাকটিস",
                isUnlocked = chIdx == 0,
                masteryPercentage = 0,
                questionCount = 5,
                classId = targetClassId
            )
            topicsToInsert.add(top1)
            topicsToInsert.add(top2)

            listOf(top1, top2).forEachIndexed { tIdx, top ->
                questionsToInsert.add(
                    QuestionEntity(
                        id = "${top.id}_q1",
                        topicId = top.id,
                        chapterId = ch.id,
                        subjectId = subjectId,
                        type = "MCQ",
                        difficulty = "EASY",
                        question = "${top.title} থেকে গুরুত্বপূর্ণ প্রশ্ন ১: সঠিক বিবৃতি কোনটি?",
                        optionsJson = "বিকল্প ক: প্রদত্ত পাঠ্যসূচির সঠিক বৈজ্ঞানিক/তাত্ত্বিক নিয়ম|বিকল্প খ: ভুল অনুসিদ্ধান্ত|বিকল্প গ: আংশিক বিভ্রান্তিকর তথ্য|বিকল্প ঘ: উপরের কোনটিই নয়",
                        correctOptionIndex = 0,
                        correctAnswerText = "বিকল্প ক: প্রদত্ত পাঠ্যসূচির সঠিক বৈজ্ঞানিক/তাত্ত্বিক নিয়ম",
                        explanation = "পাঠ্যপুস্তকের মূল তত্ত্ব ও ধারণার সঠিক ব্যাখ্যা অনুযায়ী বিকল্প ক সঠিক।",
                        marks = 1,
                        tagsJson = "${top.id}|CONCEPT|MCQ",
                        pyqYear = "2025",
                        classId = targetClassId
                    )
                )
                questionsToInsert.add(
                    QuestionEntity(
                        id = "${top.id}_q2",
                        topicId = top.id,
                        chapterId = ch.id,
                        subjectId = subjectId,
                        type = "MCQ",
                        difficulty = "MEDIUM",
                        question = "${ch.title} সংক্রান্ত প্রয়োগভিত্তিক প্রশ্ন: কোনটি সর্বাধিক গ্রহণযোগ্য?",
                        optionsJson = "বিকল্প ক: মৌলিক সূত্রের যথাযথ প্রয়োগ|বিকল্প খ: ভুল সূত্র প্রয়োগ|বিকল্প গ: অনুমান নির্ভর উত্তর|বিকল্প ঘ: কোনোটিই নয়",
                        correctOptionIndex = 0,
                        correctAnswerText = "বিকল্প ক: মৌলিক সূত্রের যথাযথ প্রয়োগ",
                        explanation = "বিষয়টির সঠিক সমাধান সূত্রের উপর ভিত্তি করে বিকল্প ক নির্বাচন করা হয়েছে।",
                        marks = 1,
                        tagsJson = "${top.id}|PRACTICE|MCQ",
                        pyqYear = "2024",
                        classId = targetClassId
                    )
                )
                questionsToInsert.add(
                    QuestionEntity(
                        id = "${top.id}_q3",
                        topicId = top.id,
                        chapterId = ch.id,
                        subjectId = subjectId,
                        type = "MCQ",
                        difficulty = "HARD",
                        question = "${ch.title} এর উচ্চতর চিন্তনদক্ষতা (HOTS) ভিত্তিক সমাধান কোনটি?",
                        optionsJson = "বিকল্প ক: সঠিক বিশ্লেষণ ও গাণিতিক/যৌক্তিক নির্ভুলতা|বিকল্প খ: অসম্পূর্ণ ব্যাখ্যা|বিকল্প গ: বিভ্রান্তিকর যুক্তি|বিকল্প ঘ: কোনটিই নয়",
                        correctOptionIndex = 0,
                        correctAnswerText = "বিকল্প ক: সঠিক বিশ্লেষণ ও গাণিতিক/যৌক্তিক নির্ভুলতা",
                        explanation = "প্রশ্নের যৌক্তিক বিশ্লেষণ ও বোর্ড নির্দেশিকা অনুযায়ী বিকল্প ক নির্ভুল উত্তর।",
                        marks = 1,
                        tagsJson = "${top.id}|HOTS|MCQ",
                        pyqYear = "2023",
                        classId = targetClassId
                    )
                )
            }
        }
        dao.insertTopics(topicsToInsert)
        dao.insertQuestions(questionsToInsert)
    }

    fun getChaptersForSubjectFlow(subjectId: String): Flow<List<ChapterEntity>> =
        dao.getChaptersForSubject(subjectId).map { entities ->
            if (entities.isNotEmpty()) {
                entities
            } else {
                ensureSubjectContentSeeded(subjectId)
                dao.getChaptersForSubjectOnce(subjectId)
            }
        }.flowOn(Dispatchers.IO)

    fun getTopicsForSubjectFlow(subjectId: String): Flow<List<TopicEntity>> =
        dao.getTopicsForSubject(subjectId).map { entities ->
            if (entities.isNotEmpty()) {
                entities
            } else {
                ensureSubjectContentSeeded(subjectId)
                dao.getTopicsForSubjectOnce(subjectId)
            }
        }.flowOn(Dispatchers.IO)

    fun getAllQuestionsFlow(): Flow<List<QuestionEntity>> =
        dao.getAllQuestionsFlow().flowOn(Dispatchers.IO)

    fun getChapters(subjectId: String): Flow<List<ChapterItem>> =
        dao.getChaptersForSubject(subjectId).map { entities ->
            val list = if (entities.isNotEmpty()) {
                entities
            } else {
                ensureSubjectContentSeeded(subjectId)
                dao.getChaptersForSubjectOnce(subjectId)
            }
            list.map { entity ->
                ChapterItem(
                    id = entity.id,
                    subjectId = entity.subjectId,
                    chapterNumber = entity.chapterNumber,
                    title = entity.title,
                    description = entity.description,
                    totalTopics = entity.totalTopics,
                    completedTopics = entity.completedTopics,
                    bossUnlocked = entity.bossUnlocked,
                    bossCompleted = entity.bossCompleted,
                    masteryPercentage = entity.masteryPercentage,
                    contentData = entity.contentData,
                    solutionData = entity.solutionData
                )
            }
        }.flowOn(Dispatchers.IO)

    suspend fun getChaptersOnce(subjectId: String): List<ChapterItem> = withContext(Dispatchers.IO) {
        dao.getChaptersForSubjectOnce(subjectId).map { entity ->
            ChapterItem(
                id = entity.id,
                subjectId = entity.subjectId,
                chapterNumber = entity.chapterNumber,
                title = entity.title,
                description = entity.description,
                totalTopics = entity.totalTopics,
                completedTopics = entity.completedTopics,
                bossUnlocked = entity.bossUnlocked,
                bossCompleted = entity.bossCompleted,
                masteryPercentage = entity.masteryPercentage,
                contentData = entity.contentData,
                solutionData = entity.solutionData
            )
        }
    }

    suspend fun getTopicsForChapterOnce(chapterId: String): List<TopicEntity> = withContext(Dispatchers.IO) {
        dao.getTopicsForChapterOnce(chapterId)
    }

    suspend fun getTopicByIdOnce(topicId: String): TopicEntity? = withContext(Dispatchers.IO) {
        dao.getTopicById(topicId)
    }

    fun getTopics(chapterId: String): Flow<List<TopicItem>> =
        dao.getTopicsForChapter(chapterId).map { entities ->
            entities.map { entity ->
                TopicItem(
                    id = entity.id,
                    chapterId = entity.chapterId,
                    subjectId = entity.subjectId,
                    orderIndex = entity.orderIndex,
                    title = entity.title,
                    summary = entity.summary,
                    keyPoints = entity.keyPointsJson.split("|").filter { it.isNotBlank() },
                    isUnlocked = entity.isUnlocked,
                    masteryPercentage = entity.masteryPercentage,
                    questionCount = entity.questionCount,
                    contentData = entity.contentData,
                    solutionData = entity.solutionData,
                    videoUrl = entity.videoUrl,
                    playlistJson = entity.playlistJson
                )
            }
        }.flowOn(Dispatchers.IO)

    fun getTopicsForSubject(subjectId: String): Flow<List<TopicItem>> =
        dao.getTopicsForSubject(subjectId).map { entities ->
            entities.map { entity ->
                TopicItem(
                    id = entity.id,
                    chapterId = entity.chapterId,
                    subjectId = entity.subjectId,
                    orderIndex = entity.orderIndex,
                    title = entity.title,
                    summary = entity.summary,
                    keyPoints = entity.keyPointsJson.split("|").filter { it.isNotBlank() },
                    isUnlocked = entity.isUnlocked,
                    masteryPercentage = entity.masteryPercentage,
                    questionCount = entity.questionCount,
                    contentData = entity.contentData,
                    solutionData = entity.solutionData,
                    videoUrl = entity.videoUrl,
                    playlistJson = entity.playlistJson
                )
            }
        }.flowOn(Dispatchers.IO)

    fun getRecommendedPracticesForSubject(
        subjectId: String,
        chapters: List<ChapterItem>,
        topics: List<TopicItem>
    ): List<RecommendedPracticeSession> {
        val list = mutableListOf<RecommendedPracticeSession>()

        // 1. Next upcoming topic to unlock or master
        val activeOrUpcomingTopic = topics.firstOrNull { it.isUnlocked && it.masteryPercentage < 80 }
            ?: topics.firstOrNull { !it.isUnlocked }
            ?: topics.firstOrNull()

        if (activeOrUpcomingTopic != null) {
            val chapterOfTopic = chapters.find { it.id == activeOrUpcomingTopic.chapterId }
            list.add(
                RecommendedPracticeSession(
                    id = "rec_topic_${activeOrUpcomingTopic.id}",
                    title = "টপিক ফোকাস: ${activeOrUpcomingTopic.title}",
                    description = "অধ্যায়: ${chapterOfTopic?.title ?: ""} • ${activeOrUpcomingTopic.questionCount}টি লক্ষ্যভিত্তিক প্রশ্ন সমাধান করে ৮০% মাস্টারি অর্জন করুন।",
                    descriptionEnglish = "Solve target questions from this topic to achieve 80% mastery index.",
                    subjectId = subjectId,
                    targetId = activeOrUpcomingTopic.id,
                    mode = AssessmentMode.PRACTICE,
                    durationMinutes = 10,
                    questionsCount = activeOrUpcomingTopic.questionCount.coerceAtLeast(4),
                    xpReward = 150,
                    tagBengali = "পরবর্তী মাইলস্টোন",
                    tagEnglish = "Next Milestone",
                    tagColorHex = 0xFF3B82F6,
                    isPriority = true
                )
            )
        }

        // 2. Weak area topic booster
        val weakTopic = topics.filter { it.isUnlocked && it.masteryPercentage in 1..75 }.minByOrNull { it.masteryPercentage }
        if (weakTopic != null && weakTopic.id != activeOrUpcomingTopic?.id) {
            list.add(
                RecommendedPracticeSession(
                    id = "rec_weak_${weakTopic.id}",
                    title = "দুর্বল ক্ষেত্র বুস্টার: ${weakTopic.title}",
                    description = "বর্তমান মাস্টারি ${weakTopic.masteryPercentage}%। ধারণা স্পষ্ট করতে ও সঠিক উত্তর দিতে স্পেশাল রিভিশন কুইজ।",
                    descriptionEnglish = "Current mastery is ${weakTopic.masteryPercentage}%. Take a revision booster to level up.",
                    subjectId = subjectId,
                    targetId = weakTopic.id,
                    mode = AssessmentMode.WEAK_AREA,
                    durationMinutes = 12,
                    questionsCount = 5,
                    xpReward = 180,
                    tagBengali = "সংশোধন দরকার",
                    tagEnglish = "Needs Revision",
                    tagColorHex = 0xFFEF4444,
                    isPriority = false
                )
            )
        }

        // 3. Chapter Boss Battle raid
        val bossChapter = chapters.find { it.bossUnlocked && !it.bossCompleted }
            ?: chapters.find { it.bossCompleted }
        if (bossChapter != null) {
            list.add(
                RecommendedPracticeSession(
                    id = "rec_boss_${bossChapter.id}",
                    title = if (bossChapter.bossCompleted) "বস রি-চ্যালেঞ্জ: ${bossChapter.title}" else "⚔️ অধ্যায় বস দুর্গ আক্রমণ: ${bossChapter.title}",
                    description = "১০০০ HP দানব বসকে পরাজিত করে অধ্যায় মাস্টারি সিল ও ব্যাজ আনলক করুন!",
                    descriptionEnglish = "Defeat the 1000 HP Citadel Boss to prove complete chapter mastery.",
                    subjectId = subjectId,
                    targetId = bossChapter.id,
                    mode = AssessmentMode.BOSS_BATTLE,
                    durationMinutes = 15,
                    questionsCount = 8,
                    xpReward = 350,
                    tagBengali = if (bossChapter.bossCompleted) "বস রি-ম্যাচ" else "বস দুর্গ প্রস্তুত",
                    tagEnglish = "Boss Raid",
                    tagColorHex = 0xFFF59E0B,
                    isPriority = !bossChapter.bossCompleted
                )
            )
        }

        // 4. Subject Madhyamik Full Mock Test
        list.add(
            RecommendedPracticeSession(
                id = "rec_mock_$subjectId",
                title = "মাধ্যমিক পূর্ণাঙ্গ মক টেস্ট (৯০ নম্বর)",
                description = "WBBSE বোর্ড ব্লুপ্রিন্ট ও প্রশ্নকাঠামো অনুযায়ী পূর্ণাঙ্গ মূল্যায়ন। রাজ্য র‍্যাঙ্ক ও পার্সেন্টাইল নির্ধারণ।",
                descriptionEnglish = "Official WBBSE pattern 90-marks benchmark test to assess state rank.",
                subjectId = subjectId,
                targetId = subjectId,
                mode = AssessmentMode.MOCK_TEST,
                durationMinutes = 45,
                questionsCount = 20,
                xpReward = 500,
                tagBengali = "বোর্ড ব্লুপ্রিন্ট",
                tagEnglish = "Board Benchmark",
                tagColorHex = 0xFF10B981,
                isPriority = false
            )
        )

        // 5. Speed Blitz for rapid memory
        list.add(
            RecommendedPracticeSession(
                id = "rec_blitz_$subjectId",
                title = "স্পিড ব্লিৎজ ও দ্রুত রিফ্লেক্স কুইজ",
                description = "১৫ সেকেন্ড/প্রশ্ন সময়সীমায় দ্রুততম সময়ে সঠিক অপশন বাছাই করার অভ্যাস।",
                descriptionEnglish = "Rapid fire questions with 15s timer for high test-day agility.",
                subjectId = subjectId,
                targetId = subjectId,
                mode = AssessmentMode.SPEED_BLITZ,
                durationMinutes = 5,
                questionsCount = 10,
                xpReward = 200,
                tagBengali = "দ্রুত রিফ্লেক্স",
                tagEnglish = "Speed Drill",
                tagColorHex = 0xFF8B5CF6,
                isPriority = false
            )
        )

        return list
    }


    private fun parseMockScores(json: String): Map<String, Int> {
        if (json.isBlank()) return emptyMap()
        val map = mutableMapOf<String, Int>()
        json.split(",").forEach { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) {
                val key = parts[0].trim()
                val score = parts[1].trim().toIntOrNull() ?: 0
                map[key] = score
            }
        }
        return map
    }

    val studentProfile: Flow<StudentProfile> = dao.getUserProgress().map { entity ->
        if (entity != null) {
            val level = entity.level
            val currentXp = entity.currentXp
            val xpForNext = level * 500
            val subjectMockScores = parseMockScores(entity.mockScoresJson)
            val classObj = com.example.data.model.AcademicClassProvider.ALL_CLASSES
                .find { it.id == entity.classId }
            val classBengali = classObj?.nameBengali ?: "দশম শ্রেণি (মাধ্যমিক)"

            val notesSet = if (entity.unlockedNotesJson.isNotBlank()) {
                entity.unlockedNotesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            } else setOf("1")

            val podcastsSet = if (entity.unlockedPodcastsJson.isNotBlank()) {
                entity.unlockedPodcastsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            } else emptySet()

            StudentProfile(
                name = entity.studentName,
                school = entity.school,
                district = entity.district,
                targetExam = entity.targetExam,
                classId = entity.classId,
                classLevel = classBengali,
                stream = entity.stream,
                streamBengali = when (entity.stream) {
                    "science" -> "বিজ্ঞান শাখা (Science)"
                    "arts" -> "কলা বিভাগ (Arts)"
                    "commerce" -> "বাণিজ্য শাখা (Commerce)"
                    else -> "সকল বিষয়"
                },
                studyGoals = if (entity.studyGoalsJson.isNotBlank()) {
                    entity.studyGoalsJson.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                } else emptyList(),
                currentXp = currentXp,
                level = level,
                xpToNextLevel = xpForNext,
                currentStreak = entity.currentStreak,
                bestStreak = entity.bestStreak,
                totalQuestionsAnswered = 380 + (currentXp / 25),
                overallAccuracy = 82,
                examReadinessPercentage = entity.examReadinessPercentage,
                rankState = 412,
                league = if (currentXp > 3000) "Diamond Scholar / হীরক যোদ্ধা" else "Platinum Scholar / প্লাটিনাম যোদ্ধা",
                subjectMockScores = subjectMockScores,
                isPro = entity.isPro,
                coins = entity.coins,
                aiCredits = entity.aiCredits,
                isStreakRepaired = entity.isStreakRepaired,
                unlockedNotes = notesSet,
                unlockedPodcasts = podcastsSet,
                fiftyFiftyLifelines = entity.fiftyFiftyLifelines,
                battleEnergy = entity.battleEnergy
            )
        } else {
            StudentProfile()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun updateProfile(profile: StudentProfile) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val notesJson = profile.unlockedNotes.joinToString(",")
        val podcastsJson = profile.unlockedPodcasts.joinToString(",")
        val updated = current.copy(
            studentName = profile.name,
            school = profile.school,
            district = profile.district,
            targetExam = profile.targetExam,
            classId = profile.classId,
            stream = profile.stream,
            currentXp = profile.currentXp,
            level = ((profile.currentXp / 500) + 1).coerceAtLeast(1),
            currentStreak = profile.currentStreak,
            bestStreak = maxOf(current.bestStreak, profile.bestStreak),
            isPro = profile.isPro,
            coins = profile.coins.coerceAtLeast(0),
            aiCredits = profile.aiCredits.coerceAtLeast(0),
            isStreakRepaired = profile.isStreakRepaired,
            unlockedNotesJson = notesJson,
            unlockedPodcastsJson = podcastsJson,
            fiftyFiftyLifelines = profile.fiftyFiftyLifelines,
            battleEnergy = profile.battleEnergy
        )
        dao.insertOrUpdateProgress(updated)
    }

    suspend fun setProMode(isPro: Boolean) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val updated = current.copy(isPro = isPro)
        dao.insertOrUpdateProgress(updated)
        if (isPro) {
            dao.unlockAllTopics()
            dao.unlockAllChapterBosses()
        }
    }

    suspend fun addXpAndCoins(xp: Int, coins: Int) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val newXp = current.currentXp + xp
        val newCoins = (current.coins + coins).coerceAtLeast(0)
        val newLevel = ((newXp / 500) + 1).coerceAtLeast(1)
        val updated = current.copy(
            currentXp = newXp,
            coins = newCoins,
            level = newLevel,
            lastActiveDate = System.currentTimeMillis()
        )
        dao.insertOrUpdateProgress(updated)
    }

    suspend fun repairStreak() = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val newStreak = (current.currentStreak + 1).coerceAtLeast(1)
        val updated = current.copy(
            currentStreak = newStreak,
            bestStreak = maxOf(current.bestStreak, newStreak),
            isStreakRepaired = true,
            currentXp = current.currentXp + 100,
            coins = current.coins + 50,
            lastActiveDate = System.currentTimeMillis()
        )
        dao.insertOrUpdateProgress(updated)
    }

    suspend fun addAiCredits(amount: Int) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val updated = current.copy(
            aiCredits = (current.aiCredits + amount).coerceAtLeast(0)
        )
        dao.insertOrUpdateProgress(updated)
    }

    suspend fun consumeAiCredit(): Boolean = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        if (current.isPro || current.aiCredits > 0) {
            if (!current.isPro && current.aiCredits > 0) {
                val updated = current.copy(aiCredits = current.aiCredits - 1)
                dao.insertOrUpdateProgress(updated)
            }
            true
        } else {
            false
        }
    }

    suspend fun unlockNote(noteId: String) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val existing = current.unlockedNotesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
        existing.add(noteId)
        val updated = current.copy(
            unlockedNotesJson = existing.joinToString(","),
            currentXp = current.currentXp + 50,
            coins = current.coins + 20
        )
        dao.insertOrUpdateProgress(updated)
    }

    suspend fun unlockPodcast(podcastTitle: String) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val existing = current.unlockedPodcastsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
        existing.add(podcastTitle)
        val updated = current.copy(
            unlockedPodcastsJson = existing.joinToString(","),
            currentXp = current.currentXp + 50,
            coins = current.coins + 20
        )
        dao.insertOrUpdateProgress(updated)
    }

    suspend fun updateStudentProfile(
        name: String,
        school: String,
        district: String = "",
        targetExam: String = "",
        classId: String = "",
        stream: String = "",
        studyGoals: List<String> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val resolvedClassId = if (classId.isNotBlank()) classId else if (targetExam.isNotBlank()) {
            val lower = targetExam.lowercase()
            when {
                lower.contains("class 1") || lower.contains("প্রথম") || lower.contains("class_1") -> "class_1_wbbpe"
                lower.contains("class 2") || lower.contains("দ্বিতীয়") || lower.contains("class_2") -> "class_2_wbbpe"
                lower.contains("class 3") || lower.contains("তৃতীয়") || lower.contains("class_3") -> "class_3_wbbpe"
                lower.contains("class 4") || lower.contains("চতুর্থ") || lower.contains("class_4") -> "class_4_wbbpe"
                lower.contains("kg") || lower.contains("প্রাক-প্রাথমিক") || lower.contains("pre-primary") -> "class_kg_wbbpe"
                lower.contains("class 5") || lower.contains("পঞ্চম") || lower.contains("class_5") -> "class_5_wbbse"
                lower.contains("class 6") || lower.contains("ষষ্ঠ") || lower.contains("class_6") -> "class_6_wbbse"
                lower.contains("class 7") || lower.contains("সপ্তম") || lower.contains("class_7") -> "class_7_wbbse"
                lower.contains("class 8") || lower.contains("অষ্টম") || lower.contains("class_8") -> "class_8_wbbse"
                lower.contains("class 9") || lower.contains("নবম") || lower.contains("class_9") -> "class_9_wbbse"
                lower.contains("class 11") || lower.contains("একাদশ") || lower.contains("class_11") -> "class_11_wbchse"
                lower.contains("class 12") || lower.contains("দ্বাদশ") || lower.contains("উচ্চ মাধ্যমিক") || lower.contains("class_12") -> "class_12_wbchse"
                else -> current.classId
            }
        } else current.classId

        val updated = current.copy(
            studentName = if (name.isNotBlank()) name else current.studentName,
            school = if (school.isNotBlank()) school else current.school,
            district = if (district.isNotBlank()) district else current.district,
            targetExam = if (targetExam.isNotBlank()) targetExam else current.targetExam,
            classId = resolvedClassId,
            stream = if (stream.isNotBlank()) stream else current.stream,
            studyGoalsJson = if (studyGoals.isNotEmpty()) studyGoals.joinToString("|") else current.studyGoalsJson
        )
        dao.insertOrUpdateProgress(updated)
    }

    val dailyQuests: Flow<List<DailyQuest>> = dao.getDailyQuests().map { entities ->
        entities.map { entity ->
            DailyQuest(
                id = entity.id,
                title = entity.title,
                currentCount = entity.currentCount,
                targetCount = entity.targetCount,
                xpReward = entity.xpReward,
                isCompleted = entity.isCompleted
            )
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getTopicById(topicId: String): TopicItem? = withContext(Dispatchers.IO) {
        dao.getTopicById(topicId)?.let { entity ->
            TopicItem(
                id = entity.id,
                chapterId = entity.chapterId,
                subjectId = entity.subjectId,
                orderIndex = entity.orderIndex,
                title = entity.title,
                summary = entity.summary,
                keyPoints = entity.keyPointsJson.split("|").filter { it.isNotBlank() },
                isUnlocked = entity.isUnlocked,
                masteryPercentage = entity.masteryPercentage,
                questionCount = entity.questionCount,
                contentData = entity.contentData,
                solutionData = entity.solutionData,
                videoUrl = entity.videoUrl,
                playlistJson = entity.playlistJson
            )
        }
    }

    suspend fun getChapterById(chapterId: String): ChapterItem? = withContext(Dispatchers.IO) {
        dao.getChapterById(chapterId)?.let { entity ->
            ChapterItem(
                id = entity.id,
                subjectId = entity.subjectId,
                chapterNumber = entity.chapterNumber,
                title = entity.title,
                description = entity.description,
                totalTopics = entity.totalTopics,
                completedTopics = entity.completedTopics,
                bossUnlocked = entity.bossUnlocked,
                bossCompleted = entity.bossCompleted,
                masteryPercentage = entity.masteryPercentage
            )
        }
    }

    suspend fun getTopicQuestionCount(topicId: String): Int = withContext(Dispatchers.IO) {
        val topicQs = dao.getQuestionsForTopic(topicId)
        if (topicQs.isNotEmpty()) {
            topicQs.size
        } else {
            dao.getAllQuestions().count { it.topicId == topicId || it.tagsJson.contains(topicId) }
        }
    }

    suspend fun getRandomBattleQuestions(
        classId: String,
        subjectId: String? = null,
        chapterId: String? = null,
        topicId: String? = null,
        limit: Int = 5
    ): List<QuestionItem> {
        val cid = classId.ifBlank { "class_10_wbbse" }

        // 1. Specific topic filter
        if (!topicId.isNullOrBlank() && topicId != "all") {
            val topicQs = dao.getQuestionsForTopic(topicId).filter { it.classId == cid || it.classId.isBlank() }
            if (topicQs.isNotEmpty()) {
                return topicQs.shuffled().take(limit).map { it.toDomainModel(randomizeOptions = true) }
            }
        }

        // 2. Specific chapter filter
        if (!chapterId.isNullOrBlank() && chapterId != "all") {
            val chapterQs = dao.getQuestionsForChapter(chapterId).filter { it.classId == cid || it.classId.isBlank() }
            if (chapterQs.isNotEmpty()) {
                return chapterQs.shuffled().take(limit).map { it.toDomainModel(randomizeOptions = true) }
            }
        }

        // 3. Specific subject filter
        if (!subjectId.isNullOrBlank() && subjectId != "all") {
            val targetSubject = dao.getSubjectById(subjectId)
            val matchingSubjectId = targetSubject?.id ?: subjectId
            val subQs = dao.getQuestionsForSubjectAndClass(cid, matchingSubjectId, limit * 3).ifEmpty {
                dao.getQuestionsForSubject(matchingSubjectId, limit * 3)
            }
            if (subQs.isNotEmpty()) {
                return subQs.shuffled().take(limit).map { it.toDomainModel(randomizeOptions = true) }
            }
        }

        // 4. Default class-wide randomized questions
        val qs = dao.getRandomQuestionsForClass(cid, limit).ifEmpty {
            dao.getRandomQuestions(limit).ifEmpty { dao.getAllQuestions().take(limit) }
        }
        return qs.shuffled().map { it.toDomainModel(randomizeOptions = true) }
    }

    suspend fun getStrictlyIsolatedQuestionsForTopic(
        topicId: String,
        setNumber: Int,
        resolvedClassId: String
    ): List<QuestionEntity> {
        val topic = dao.getTopicById(topicId)
        val directQuestions = dao.getQuestionsForTopic(topicId).sortedBy { it.id }
        if (directQuestions.isNotEmpty()) {
            val chunkSize = 15
            val chunks = directQuestions.chunked(chunkSize)
            if (setNumber <= chunks.size && setNumber > 0) {
                return chunks[setNumber - 1].shuffled()
            } else {
                // Dynamic endless set: create fresh randomized questions pool
                val shuffled = directQuestions.shuffled()
                return shuffled.take(chunkSize)
            }
        }

        val tagMatching = dao.getAllQuestions().filter { 
            (it.topicId == topicId || (topic != null && it.topicId.contains(topicId)) || it.tagsJson.contains(topicId)) &&
            (topic == null || it.subjectId == topic.subjectId) &&
            (it.classId == resolvedClassId || resolvedClassId.isBlank() || it.classId.isBlank())
        }
        if (tagMatching.isNotEmpty()) {
            val chunkSize = 15
            val chunks = tagMatching.chunked(chunkSize)
            if (setNumber <= chunks.size && setNumber > 0) {
                return chunks[setNumber - 1].shuffled()
            } else {
                val shuffled = tagMatching.shuffled()
                return shuffled.take(chunkSize)
            }
        }

        if (topic != null) {
            val chapterQs = dao.getQuestionsForChapter(topic.chapterId)
                .filter { it.subjectId == topic.subjectId }
            if (chapterQs.isNotEmpty()) {
                val chunkSize = 10
                val chunks = chapterQs.chunked(chunkSize)
                if (setNumber <= chunks.size && setNumber > 0) {
                    return chunks[setNumber - 1].shuffled()
                } else {
                    val shuffled = chapterQs.shuffled()
                    return shuffled.take(chunkSize)
                }
            }

            val topicTitle = topic.title
            val keyPoints = topic.keyPointsJson.split("|").filter { it.isNotBlank() }
            val generated = listOf(
                QuestionEntity(
                    id = "${topicId}_q1",
                    topicId = topicId,
                    chapterId = topic.chapterId,
                    subjectId = topic.subjectId,
                    type = "MCQ",
                    difficulty = "EASY",
                    question = "$topicTitle বিষয়ের মূল ধারণা ও সংজ্ঞার ক্ষেত্রে কোনটি সর্বাধিক যুক্তিযুক্ত?",
                    optionsJson = "${keyPoints.getOrElse(0) { "সঠিক মূল তত্ত্ব ও সূত্রের প্রয়োগ" }}|ভুল ব্যাখ্যামূলক বিবৃতি|অপ্রাসঙ্গিক অনুসিদ্ধান্ত|উপরের কোনটিই নয়",
                    correctOptionIndex = 0,
                    correctAnswerText = keyPoints.getOrElse(0) { "সঠিক মূল তত্ত্ব ও সূত্রের প্রয়োগ" },
                    explanation = "$topicTitle অধ্যায়ের পাঠ্যসূচি অনুযায়ী এই ধারণাটি সঠিক।",
                    marks = 1,
                    tagsJson = "$topicId|MCQ|CONCEPT",
                    pyqYear = "2024",
                    classId = resolvedClassId
                ),
                QuestionEntity(
                    id = "${topicId}_q2",
                    topicId = topicId,
                    chapterId = topic.chapterId,
                    subjectId = topic.subjectId,
                    type = "MCQ",
                    difficulty = "MEDIUM",
                    question = "$topicTitle সম্পর্কিত গুরুত্বপূর্ণ বৈশিষ্ট্য বা নিয়ম কোনটি?",
                    optionsJson = "বিপরীত ধারণা|${keyPoints.getOrElse(1) { "বিষয়টির মূল কার্যপ্রণালী ও গুরুত্বপূর্ণ নিয়ম" }}|শুধুমাত্র তাত্ত্বিক অনুসিদ্ধান্ত|কোনোটিই প্রযোজ্য নয়",
                    correctOptionIndex = 1,
                    correctAnswerText = keyPoints.getOrElse(1) { "বিষয়টির মূল কার্যপ্রণালী ও গুরুত্বপূর্ণ নিয়ম" },
                    explanation = "$topicTitle অধ্যায়ের প্রধান নিয়মাবলী পাঠ্যবইয়ে সুস্পষ্টভাবে উল্লিখিত।",
                    marks = 1,
                    tagsJson = "$topicId|MCQ|RULES",
                    pyqYear = "2023",
                    classId = resolvedClassId
                ),
                QuestionEntity(
                    id = "${topicId}_q3",
                    topicId = topicId,
                    chapterId = topic.chapterId,
                    subjectId = topic.subjectId,
                    type = "MCQ",
                    difficulty = "HARD",
                    question = "$topicTitle থেকে পরীক্ষায় আসা বহুনির্বাচনী প্রশ্নের সঠিক সিদ্ধান্ত কোনটি?",
                    optionsJson = "ভ্রান্ত প্রতিক্রিয়া|আংশিক সমাধান|${keyPoints.getOrElse(2) { "সঠিক বিশ্লেষণ ও বাস্তব প্রয়োগ" }}|কোনোটিই নয়",
                    correctOptionIndex = 2,
                    correctAnswerText = keyPoints.getOrElse(2) { "সঠিক বিশ্লেষণ ও বাস্তব প্রয়োগ" },
                    explanation = "টপিক ভিত্তিক গভীর অধ্যয়নে এই বিশ্লেষণটি সবচেয়ে নির্ভুল।",
                    marks = 1,
                    tagsJson = "$topicId|MCQ|APPLICATION",
                    pyqYear = "2022",
                    classId = resolvedClassId
                ),
                QuestionEntity(
                    id = "${topicId}_q4",
                    topicId = topicId,
                    chapterId = topic.chapterId,
                    subjectId = topic.subjectId,
                    type = "MCQ",
                    difficulty = "MEDIUM",
                    question = "$topicTitle সম্পর্কিত মাধ্যমিক বা বোর্ড পরীক্ষার মডেল প্রশ্নের সঠিক উত্তর কোনটি?",
                    optionsJson = "${keyPoints.getOrElse(0) { "সঠিক পাঠ্যপুস্তক ভিত্তিক উত্তর" }}|অপ্রমাণিত তত্ত্ব|ভুল উদাহরণ|সবকটিই ভুল",
                    correctOptionIndex = 0,
                    correctAnswerText = keyPoints.getOrElse(0) { "সঠিক পাঠ্যপুস্তক ভিত্তিক উত্তর" },
                    explanation = "বোর্ড নির্দেশিকা অনুসারে সঠিক উত্তর চিহ্নিত করা হয়েছে।",
                    marks = 1,
                    tagsJson = "$topicId|MCQ|EXAM_SPECIAL",
                    pyqYear = "2024",
                    classId = resolvedClassId
                ),
                QuestionEntity(
                    id = "${topicId}_q5",
                    topicId = topicId,
                    chapterId = topic.chapterId,
                    subjectId = topic.subjectId,
                    type = "MCQ",
                    difficulty = "HARD",
                    question = "$topicTitle অধ্যায়ের বাস্তব প্রয়োগ ও গুরুত্ব বিষয়ে কোনটি সঠিক?",
                    optionsJson = "প্রয়োগহীনতা|${keyPoints.getOrElse(1) { "ব্যবহারিক প্রয়োগ ও গাণিতিক/তাত্ত্বিক নির্ভুলতা" }}|কেবল মুখস্থ নির্ভরতা|কোনোটিই নয়",
                    correctOptionIndex = 1,
                    correctAnswerText = keyPoints.getOrElse(1) { "ব্যবহারিক প্রয়োগ ও গাণিতিক/তাত্ত্বিক নির্ভুলতা" },
                    explanation = "টপিকের গুরুত্বপূর্ণ তাৎপর্য ও প্রয়োগের নির্ভুল রূপ।",
                    marks = 1,
                    tagsJson = "$topicId|MCQ|IMPORTANCE",
                    pyqYear = "2023",
                    classId = resolvedClassId
                )
            )
            dao.insertQuestions(generated)
            return generated
        }

        return emptyList()
    }

    suspend fun getStrictlyIsolatedQuestionsForChapter(
        chapterId: String,
        setNumber: Int,
        resolvedClassId: String
    ): List<QuestionEntity> {
        val chapter = dao.getChapterById(chapterId)
        val chapterQs = dao.getQuestionsForChapter(chapterId).filter {
            chapter == null || it.subjectId == chapter.subjectId
        }.sortedBy { it.id }

        if (chapterQs.isNotEmpty()) {
            val chunkSize = 15
            val chunks = chapterQs.chunked(chunkSize)
            val safeSetIndex = (setNumber - 1).coerceIn(0, (chunks.size - 1).coerceAtLeast(0))
            return if (chunks.isNotEmpty()) chunks[safeSetIndex] else chapterQs
        }

        if (chapter != null) {
            val subjectQs = dao.getQuestionsForSubject(chapter.subjectId, 30)
                .filter { it.subjectId == chapter.subjectId }
            if (subjectQs.isNotEmpty()) {
                val chunkSize = 15
                val chunks = subjectQs.chunked(chunkSize)
                val safeSetIndex = (setNumber - 1).coerceIn(0, (chunks.size - 1).coerceAtLeast(0))
                return chunks[safeSetIndex]
            }

            val chTitle = chapter.title
            val generated = listOf(
                QuestionEntity(
                    id = "${chapterId}_boss_q1",
                    topicId = "${chapterId}_top1",
                    chapterId = chapterId,
                    subjectId = chapter.subjectId,
                    type = "MCQ",
                    difficulty = "MEDIUM",
                    question = "$chTitle - এই অধ্যায়ের মূল তাত্ত্বিক ভিত্তি কোনটি?",
                    optionsJson = "পাঠ্যক্রমের প্রধান সংজ্ঞাগত সূত্র|ভুল প্রতিপাদন|অসম্পূর্ণ সমীকরণ|কোনোটিই নয়",
                    correctOptionIndex = 0,
                    correctAnswerText = "পাঠ্যক্রমের প্রধান সংজ্ঞাগত সূত্র",
                    explanation = "$chTitle অধ্যায়ের প্রারম্ভিক ধারণা।",
                    marks = 1,
                    tagsJson = "$chapterId|BOSS_BATTLE|MCQ",
                    pyqYear = "2024",
                    classId = resolvedClassId
                ),
                QuestionEntity(
                    id = "${chapterId}_boss_q2",
                    topicId = "${chapterId}_top1",
                    chapterId = chapterId,
                    subjectId = chapter.subjectId,
                    type = "MCQ",
                    difficulty = "HARD",
                    question = "$chTitle - অধ্যায় থেকে বিগত বছরের পরীক্ষায় আসা প্রধান প্রশ্নের সমাধান কোনটি?",
                    optionsJson = "ভুল উত্তর|সঠিক বিশ্লেষণ ও বৈজ্ঞানিক/যৌক্তিক সূত্র|অনুপযুক্ত কারণ|কোনোটিই নয়",
                    correctOptionIndex = 1,
                    correctAnswerText = "সঠিক বিশ্লেষণ ও বৈজ্ঞানিক/যৌক্তিক সূত্র",
                    explanation = "বোর্ড পরীক্ষার প্রশ্নের সঠিক উত্তর।",
                    marks = 2,
                    tagsJson = "$chapterId|BOSS_BATTLE|MCQ",
                    pyqYear = "2023",
                    classId = resolvedClassId
                ),
                QuestionEntity(
                    id = "${chapterId}_boss_q3",
                    topicId = "${chapterId}_top2",
                    chapterId = chapterId,
                    subjectId = chapter.subjectId,
                    type = "MCQ",
                    difficulty = "HARD",
                    question = "$chTitle - চূড়ান্ত বস ব্যাটল চ্যালেঞ্জ: অধ্যায়ের সামগ্রিক সারসংক্ষেপ কোনটি?",
                    optionsJson = "অপ্রাসঙ্গিক অনুসিদ্ধান্ত|বিভ্রান্তিকর তথ্য|সম্পূর্ণ সিলেবাস ভিত্তিক সঠিক ধারণা|কোনোটিই নয়",
                    correctOptionIndex = 2,
                    correctAnswerText = "সম্পূর্ণ সিলেবাস ভিত্তিক সঠিক ধারণা",
                    explanation = "অধ্যায়ের সার্বিক মাস্টারির নির্ভুল উত্তর।",
                    marks = 2,
                    tagsJson = "$chapterId|BOSS_BATTLE|MCQ",
                    pyqYear = "2024",
                    classId = resolvedClassId
                )
            )
            dao.insertQuestions(generated)
            return generated
        }

        return emptyList()
    }

    suspend fun createAssessmentSession(
        mode: AssessmentMode,
        targetId: String? = null,
        titleCustom: String? = null,
        setNumber: Int = 1,
        classId: String = ""
    ): AssessmentSession = withContext(Dispatchers.IO) {
        val currentProgress = dao.getUserProgressOnce() ?: UserProgressEntity()
        val resolvedClassId = if (classId.isNotBlank()) classId else currentProgress.classId.ifBlank { "class_10_wbbse" }

        val questionsList: List<QuestionEntity> = when (mode) {
            AssessmentMode.PRACTICE -> {
                if (!targetId.isNullOrBlank()) {
                    getStrictlyIsolatedQuestionsForTopic(targetId, setNumber, resolvedClassId)
                } else {
                    dao.getRandomQuestionsForClass(resolvedClassId, 10).ifEmpty { dao.getRandomQuestions(10) }
                }
            }
            AssessmentMode.DAILY_10 -> {
                dao.getRandomQuestionsForClass(resolvedClassId, 10).ifEmpty { dao.getRandomQuestions(10) }
            }
            AssessmentMode.BOSS_BATTLE -> {
                if (!targetId.isNullOrBlank()) {
                    getStrictlyIsolatedQuestionsForChapter(targetId, setNumber, resolvedClassId)
                } else {
                    dao.getRandomQuestionsForClass(resolvedClassId, 15).ifEmpty { dao.getRandomQuestions(15) }
                }
            }
            AssessmentMode.MOCK_TEST -> {
                if (!targetId.isNullOrBlank()) {
                    // 0. Check Firestore Test Template first
                    var firestoreTemplateQs: List<QuestionEntity> = emptyList()
                    try {
                        val fs = com.example.data.firestore.FirestoreService()
                        val testTemplateResult = fs.getTestById(targetId)
                        val testTemplate = testTemplateResult.getOrNull()
                        if (testTemplate != null && testTemplate.questions.isNotEmpty()) {
                            firestoreTemplateQs = testTemplate.questions.mapIndexed { idx, fq ->
                                QuestionEntity(
                                    id = fq.id.ifBlank { "${testTemplate.id}_q_$idx" },
                                    topicId = fq.topicId.ifBlank { "${testTemplate.chapterId ?: "ch"}_topic" },
                                    chapterId = fq.chapterId.ifBlank { testTemplate.chapterId ?: "" },
                                    subjectId = fq.subjectId.ifBlank { testTemplate.subjectId ?: "" },
                                    classId = fq.classId.ifBlank { testTemplate.classId ?: "" },
                                    type = fq.type.ifBlank { "MCQ" },
                                    difficulty = fq.difficulty.ifBlank { "MEDIUM" },
                                    question = fq.question,
                                    optionsJson = org.json.JSONArray(fq.options).toString(),
                                    correctOptionIndex = fq.correctOptionIndex,
                                    correctAnswerText = fq.correctAnswerText.ifBlank { fq.options.getOrNull(fq.correctOptionIndex) ?: "" },
                                    explanation = fq.explanation,
                                    marks = fq.marks,
                                    tagsJson = fq.tags.joinToString("|"),
                                    pyqYear = "",
                                )
                            }
                            if (firestoreTemplateQs.isNotEmpty()) {
                                dao.insertQuestions(firestoreTemplateQs)
                            }
                        }
                    } catch (_: Exception) {}

                    if (firestoreTemplateQs.isNotEmpty()) {
                        firestoreTemplateQs
                    } else {
                        // 1. Direct test template tag match
                        val testQs = dao.getAllQuestions().filter { 
                            (it.pyqYear == "TEST_$targetId" || it.pyqYear.startsWith("TEST_${targetId}_") || it.id.contains(targetId) || it.tagsJson.contains(targetId)) &&
                            (it.classId == resolvedClassId || it.classId.isBlank())
                        }
                        if (testQs.isNotEmpty()) {
                            testQs.take(30)
                        } else {
                            // 2. Check if targetId matches a subject ID or subject code
                            val targetSubject = dao.getSubjectById(targetId)
                            val matchingSubjectId = targetSubject?.id ?: targetId
                            val subQs = dao.getQuestionsForSubjectAndClass(resolvedClassId, matchingSubjectId, 30).ifEmpty {
                                dao.getQuestionsForSubject(matchingSubjectId, 30)
                            }.filter { it.subjectId == matchingSubjectId || (targetSubject != null && it.subjectId == targetSubject.id) }

                            if (subQs.isNotEmpty()) {
                                subQs
                            } else if (targetSubject != null) {
                                // Synthesize subject-isolated mock test questions
                                val chs = dao.getChaptersForSubjectOnce(matchingSubjectId)
                                val generated = chs.mapIndexed { idx, ch ->
                                    QuestionEntity(
                                        id = "${matchingSubjectId}_mock_q${idx + 1}",
                                        topicId = "${ch.id}_top1",
                                        chapterId = ch.id,
                                        subjectId = matchingSubjectId,
                                        type = "MCQ",
                                        difficulty = "MEDIUM",
                                        question = "${targetSubject.name}: ${ch.title} - এর গুরুত্বপূর্ণ মক প্রশ্ন",
                                        optionsJson = "সঠিক পাঠ্যপুস্তক ভিত্তিক উত্তর|ভুল বিকল্প ১|ভুল বিকল্প ২|কোনোটিই নয়",
                                        correctOptionIndex = 0,
                                        correctAnswerText = "সঠিক পাঠ্যপুস্তক ভিত্তিক উত্তর",
                                        explanation = "${ch.title} অধ্যায়ের সঠিক সমাধান।",
                                        marks = 1,
                                        tagsJson = "$matchingSubjectId|MOCK|MCQ",
                                        pyqYear = "2024",
                                        classId = resolvedClassId
                                    )
                                }
                                if (generated.isNotEmpty()) {
                                    dao.insertQuestions(generated)
                                    generated
                                } else {
                                    dao.getRandomQuestionsForClass(resolvedClassId, 30).ifEmpty { dao.getRandomQuestions(30) }
                                }
                            } else {
                                dao.getRandomQuestionsForClass(resolvedClassId, 30).ifEmpty { dao.getRandomQuestions(30) }
                            }
                        }
                    }
                } else {
                    dao.getRandomQuestionsForClass(resolvedClassId, 30).ifEmpty { dao.getRandomQuestions(30) }
                }
            }
            AssessmentMode.SPEED_BLITZ -> {
                if (!targetId.isNullOrBlank()) {
                    val topicQs = getStrictlyIsolatedQuestionsForTopic(targetId, setNumber, resolvedClassId)
                    if (topicQs.isNotEmpty()) topicQs.shuffled().take(15) else dao.getRandomQuestionsForClass(resolvedClassId, 15).ifEmpty { dao.getRandomQuestions(15) }
                } else {
                    dao.getRandomQuestionsForClass(resolvedClassId, 15).ifEmpty { dao.getRandomQuestions(15) }
                }
            }
            AssessmentMode.CHAPTER_PYQ -> {
                if (!targetId.isNullOrBlank()) {
                    val ch = dao.getChapterById(targetId)
                    val pyqs = dao.getQuestionsForChapter(targetId).filter { 
                        (ch == null || it.subjectId == ch.subjectId) && it.pyqYear.isNotBlank() && it.pyqYear != "None" 
                    }
                    if (pyqs.isNotEmpty()) pyqs else getStrictlyIsolatedQuestionsForChapter(targetId, setNumber, resolvedClassId)
                } else {
                    dao.getRandomQuestions(10)
                }
            }
            AssessmentMode.MAP_DIAGRAM_QUIZ -> {
                if (!targetId.isNullOrBlank()) {
                    val ch = dao.getChapterById(targetId)
                    val mapQs = dao.getQuestionsForChapter(targetId).filter { 
                        (ch == null || it.subjectId == ch.subjectId) && (it.question.contains("চিহ্নিত") || it.question.contains("চিত্র") || it.question.contains("Map") || it.question.contains("Diagram"))
                    }
                    if (mapQs.isNotEmpty()) mapQs else getStrictlyIsolatedQuestionsForChapter(targetId, setNumber, resolvedClassId)
                } else {
                    dao.getRandomQuestions(8)
                }
            }
            AssessmentMode.CHAPTER_MARATHON_QUIZ -> {
                if (!targetId.isNullOrBlank()) {
                    getStrictlyIsolatedQuestionsForChapter(targetId, setNumber, resolvedClassId)
                } else {
                    dao.getRandomQuestions(30)
                }
            }
            AssessmentMode.WEAK_AREA -> {
                val wrongIds = dao.getWrongAttemptedQuestionIds()
                if (wrongIds.isNotEmpty()) {
                    dao.getQuestionsByIds(wrongIds)
                } else {
                    dao.getRandomQuestions(10)
                }
            }
            AssessmentMode.FRIEND_CHALLENGE -> {
                dao.getRandomQuestionsForClass(resolvedClassId, 10).ifEmpty { dao.getRandomQuestions(10).ifEmpty { dao.getAllQuestions().take(10) } }
            }
        }

        val domainQuestions = questionsList.map { it.toDomainModel() }.shuffled()
        val timeSeconds = when (mode) {
            AssessmentMode.SPEED_BLITZ -> domainQuestions.size * 15
            AssessmentMode.DAILY_10 -> domainQuestions.size * 30
            AssessmentMode.BOSS_BATTLE -> domainQuestions.size * 40
            AssessmentMode.MOCK_TEST -> domainQuestions.size * 60
            AssessmentMode.CHAPTER_PYQ -> domainQuestions.size * 45
            AssessmentMode.MAP_DIAGRAM_QUIZ -> domainQuestions.size * 50
            AssessmentMode.CHAPTER_MARATHON_QUIZ -> domainQuestions.size * 20
            else -> domainQuestions.size * 45
        }

        val title = titleCustom ?: when (mode) {
            AssessmentMode.PRACTICE -> "টপিক প্র্যাকটিস (Topic Practice)"
            AssessmentMode.DAILY_10 -> "ডেইলি ১০ স্পিড কুইজ (Daily 10 Challenge)"
            AssessmentMode.BOSS_BATTLE -> "অধ্যায় বস ব্যাটল (Chapter Boss Battle)"
            AssessmentMode.MOCK_TEST -> "মাধ্যমিক মডেল মক টেস্ট (Madhyamik Mock Test)"
            AssessmentMode.WEAK_AREA -> "ভুল সংশোধন রিভিশন (Weak Area Revision)"
            AssessmentMode.FRIEND_CHALLENGE -> "1v1 লাইভ ফ্রেন্ড ব্যাটল (Friend Challenge)"
            AssessmentMode.SPEED_BLITZ -> "স্পিড ব্লিৎজ প্র্যাকটিস (Speed Blitz)"
            AssessmentMode.CHAPTER_PYQ -> "অধ্যায়ের বিগত বছরের বোর্ড প্রশ্ন (Chapter PYQ)"
            AssessmentMode.MAP_DIAGRAM_QUIZ -> "মানচিত্র ও চিত্রভিত্তিক টেস্ট (Map & Diagram Quiz)"
            AssessmentMode.CHAPTER_MARATHON_QUIZ -> "চ্যাপ্টার ম্যারাথন কুইজ (Chapter Marathon Quiz)"
        }

        val topicId = if (mode == AssessmentMode.PRACTICE || mode == AssessmentMode.SPEED_BLITZ || mode == AssessmentMode.MOCK_TEST) targetId else domainQuestions.firstOrNull()?.topicId

        AssessmentSession(
            mode = mode,
            title = title,
            subjectId = domainQuestions.firstOrNull()?.subjectId,
            chapterId = domainQuestions.firstOrNull()?.chapterId,
            topicId = topicId,
            setNumber = setNumber,
            questions = domainQuestions,
            timeLimitSeconds = timeSeconds
        )
    }

    private fun updateStreak(progress: UserProgressEntity): UserProgressEntity {
        val now = System.currentTimeMillis()
        val lastDate = Calendar.getInstance().apply { timeInMillis = progress.lastActiveDate }
        val today = Calendar.getInstance().apply { timeInMillis = now }

        val isSameDay = lastDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        lastDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        
        val isYesterday = lastDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                          lastDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1

        var newStreak = progress.currentStreak
        if (isSameDay) {
            // No change
        } else if (isYesterday) {
            newStreak++
        } else {
            newStreak = 1
        }
        
        return progress.copy(
            currentStreak = newStreak,
            bestStreak = maxOf(progress.bestStreak, newStreak),
            lastActiveDate = now
        )
    }

    suspend fun submitAssessment(
        session: AssessmentSession,
        answers: List<UserAnswer>
    ): AssessmentResult = withContext(Dispatchers.IO) {
        val totalQuestions = session.questions.size
        val correctCount = answers.count { it.isCorrect }
        val incorrectCount = totalQuestions - correctCount
        val accuracy = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0
        val totalScore = answers.filter { it.isCorrect }.sumOf { 1 }
        val maxScore = totalQuestions

        // Record each attempt for weak area tracking
        answers.forEach { ans ->
            val q = session.questions.find { it.id == ans.questionId }
            if (q != null) {
                dao.insertAttempt(
                    QuestionAttemptEntity(
                        questionId = ans.questionId,
                        topicId = q.topicId,
                        subjectId = q.subjectId,
                        isCorrect = ans.isCorrect,
                        timestamp = System.currentTimeMillis(),
                        timeSpentSeconds = ans.timeSpentSeconds
                    )
                )
            }
        }

        // Calculate XP
        // Update user progress
        var currentProgress = dao.getUserProgressOnce() ?: UserProgressEntity()
        
        // Update Streak
        currentProgress = updateStreak(currentProgress)

        // Calculate XP & Coins
        val baseXp = correctCount * 25
        val baseCoins = correctCount * 10
        val accuracyBonus = if (accuracy >= 80) 50 else 0
        val accuracyCoinBonus = if (accuracy >= 80) 25 else 0
        val bossBonus = if (session.mode == AssessmentMode.BOSS_BATTLE && accuracy >= 70) 150 else 0
        val streakBonus = if (currentProgress.currentStreak > 1) 20 else 0
        val totalXpEarned = baseXp + accuracyBonus + bossBonus + streakBonus
        val totalCoinsEarned = baseCoins + accuracyCoinBonus + (if (session.mode == AssessmentMode.BOSS_BATTLE) 50 else 0)
        
        if (session.mode == AssessmentMode.MOCK_TEST && !session.subjectId.isNullOrBlank()) {
            val subjectId = session.subjectId!!
            val currentScores = parseMockScores(currentProgress.mockScoresJson)
            val existingScore = currentScores[subjectId] ?: 0
            if (accuracy > existingScore) {
                val updatedScores = currentScores.toMutableMap()
                updatedScores[subjectId] = accuracy
                val newJson = updatedScores.entries.joinToString(",") { "${it.key}:${it.value}" }
                currentProgress = currentProgress.copy(mockScoresJson = newJson)
            }
        }

        val newXp = currentProgress.currentXp + totalXpEarned
        val newCoins = currentProgress.coins + totalCoinsEarned
        val xpNeededForNext = currentProgress.level * 500
        val levelUp = newXp >= xpNeededForNext
        val newLevel = if (levelUp) currentProgress.level + 1 else currentProgress.level
        val newReadiness = ((currentProgress.examReadinessPercentage + (if (accuracy > 70) 1 else 0)).coerceAtMost(99))

        currentProgress = currentProgress.copy(
            currentXp = newXp,
            coins = newCoins,
            level = newLevel,
            examReadinessPercentage = newReadiness,
            lastActiveDate = System.currentTimeMillis()
        )
        dao.insertOrUpdateProgress(currentProgress)

        // Save test/quiz scores for progressive unlocking and automatic endless test generation
        if (session.mode == AssessmentMode.PRACTICE && !session.topicId.isNullOrBlank()) {
            saveTopicSetScore(session.topicId, session.setNumber, accuracy)
            // If the user achieved >= 60% on their highest set, we can check if auto-generation applies
            val currentExtra = getExtraGeneratedSets(session.topicId)
            val baseSets = 4
            if (session.setNumber >= (baseSets + currentExtra) && accuracy >= 60) {
                // Auto generate next practice set so student always has a new challenge
                addExtraGeneratedSet(session.topicId, 1)
            }
        } else if (session.mode == AssessmentMode.MOCK_TEST) {
            if (!session.topicId.isNullOrBlank()) {
                saveTopicMockScore(session.topicId, accuracy)
            }
            if (!session.subjectId.isNullOrBlank()) {
                saveSubjectTestScore(session.subjectId, accuracy)
            }
        } else if (session.mode == AssessmentMode.SPEED_BLITZ) {
            if (!session.topicId.isNullOrBlank()) {
                saveTopicSpeedScore(session.topicId, accuracy)
            }
        }

        // If practice/topic, increase mastery and unlock next topic if passed (>= 40%)
        if (session.mode == AssessmentMode.PRACTICE && session.questions.isNotEmpty()) {
            val topicId = session.questions.first().topicId
            val currentTopic = dao.getTopicById(topicId)
            if (currentTopic != null) {
                val newMastery = (currentTopic.masteryPercentage + (accuracy / 4)).coerceAtMost(100)
                dao.updateTopicMastery(topicId, newMastery)
                
                if (accuracy >= 40) {
                    val chapterTopics = dao.getTopicsForChapterOnce(currentTopic.chapterId)
                    val nextTopic = chapterTopics.sortedBy { it.orderIndex }.firstOrNull { it.orderIndex == currentTopic.orderIndex + 1 }
                    if (nextTopic != null) {
                        dao.unlockTopic(nextTopic.id)
                    }
                }

                val chapterTopics = dao.getTopicsForChapterOnce(currentTopic.chapterId)
                val masteredCount = chapterTopics.count { if (it.id == topicId) newMastery >= 70 else it.masteryPercentage >= 70 }
                if (masteredCount >= chapterTopics.size / 2 || newMastery >= 70) {
                    dao.unlockChapterBoss(currentTopic.chapterId)
                }
            }
        }

        // If Boss Battle beaten (>= 50%)
        val isBossBeaten = session.mode == AssessmentMode.BOSS_BATTLE && accuracy >= 50
        if (isBossBeaten && session.chapterId != null) {
            dao.markChapterBossCompleted(session.chapterId)
            val currentChapter = dao.getChapterById(session.chapterId)
            if (currentChapter != null) {
                val subjectChapters = dao.getChaptersForSubjectOnce(currentChapter.subjectId)
                val nextChapter = subjectChapters.sortedBy { it.chapterNumber }.firstOrNull { it.chapterNumber == currentChapter.chapterNumber + 1 }
                if (nextChapter != null) {
                    dao.unlockChapterBoss(nextChapter.id)
                }
            }
        }

        // Identify weak topics from incorrect answers
        val weakTopicIds = answers.filter { !it.isCorrect }.mapNotNull { ans ->
            session.questions.find { it.id == ans.questionId }?.topicId
        }.distinct()

        // Build Category Performance Breakdowns (by Topic or Subject)
        val allTopics = dao.getAllTopicsOnce().associateBy { it.id }
        val allSubjects = dao.getAllSubjectsOnce().associateBy { it.id }

        val categoryGroups = session.questions.groupBy { q ->
            if (session.mode == AssessmentMode.MOCK_TEST && q.subjectId.isNotBlank()) {
                q.subjectId
            } else if (q.topicId.isNotBlank()) {
                q.topicId
            } else {
                q.subjectId
            }
        }

        val categoryBreakdowns = categoryGroups.map { (catId, qList) ->
            val qIds = qList.map { it.id }.toSet()
            val catAnswers = answers.filter { it.questionId in qIds }
            val catTotalQ = qList.size
            val catCorrect = catAnswers.count { it.isCorrect }
            val catTotalMarks = qList.sumOf { it.marks }
            val catScoredMarks = catAnswers.filter { it.isCorrect }.sumOf { ans ->
                qList.find { it.id == ans.questionId }?.marks ?: 1
            }
            val catAccuracy = if (catTotalQ > 0) (catCorrect * 100) / catTotalQ else 0
            val status = when {
                catAccuracy >= 75 -> PerformanceStatus.STRONG
                catAccuracy >= 50 -> PerformanceStatus.MODERATE
                else -> PerformanceStatus.WEAK
            }

            val subject = allSubjects[catId]
            val topic = allTopics[catId]

            val nameBengali = when {
                subject != null -> subject.name
                topic != null -> topic.title
                else -> "বিষয়/অধ্যায়"
            }
            val nameEnglish = when {
                subject != null -> subject.name
                topic != null -> topic.title
                else -> "Category"
            }

            CategoryPerformance(
                categoryId = catId,
                categoryNameBengali = nameBengali,
                categoryNameEnglish = nameEnglish,
                totalQuestions = catTotalQ,
                correctCount = catCorrect,
                totalMarks = catTotalMarks,
                scoredMarks = catScoredMarks,
                accuracyPercentage = catAccuracy,
                status = status
            )
        }

        // Build Difficulty Breakdowns
        val difficultyBreakdowns = Difficulty.values().mapNotNull { diff ->
            val diffQuestions = session.questions.filter { it.difficulty == diff }
            if (diffQuestions.isEmpty()) null
            else {
                val diffQIds = diffQuestions.map { it.id }.toSet()
                val diffAnswers = answers.filter { it.questionId in diffQIds }
                val diffCorrect = diffAnswers.count { it.isCorrect }
                val diffAccuracy = if (diffQuestions.isNotEmpty()) (diffCorrect * 100) / diffQuestions.size else 0
                DifficultyPerformance(
                    difficulty = diff,
                    totalQuestions = diffQuestions.size,
                    correctCount = diffCorrect,
                    accuracyPercentage = diffAccuracy
                )
            }
        }

        // Evaluate achievement badge milestones
        val isFullScore = correctCount == totalQuestions && totalQuestions > 0
        val earnedBadges = checkAndAwardBadges(
            scorePercent = accuracy,
            isFullScore = isFullScore,
            totalQuestions = totalQuestions,
            timeSpentSeconds = answers.sumOf { it.timeSpentSeconds },
            mode = session.mode,
            currentStreak = currentProgress.currentStreak
        )

        AssessmentResult(
            mode = session.mode,
            title = session.title,
            totalQuestions = totalQuestions,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            accuracyPercentage = accuracy,
            totalScore = totalScore,
            maxScore = maxScore,
            xpEarned = totalXpEarned,
            timeSpentSeconds = answers.sumOf { it.timeSpentSeconds },
            answers = answers,
            questions = session.questions,
            categoryBreakdowns = categoryBreakdowns,
            difficultyBreakdowns = difficultyBreakdowns,
            weakTopicIds = weakTopicIds,
            isBossBeaten = isBossBeaten,
            levelUp = levelUp,
            currentLevel = newLevel,
            newlyUnlockedBadges = earnedBadges
        )
    }

    fun getAiCoachInsight(): Flow<AiCoachInsight> = dao.getUserProgress().map { progress ->
        val readiness = progress?.examReadinessPercentage ?: 76
        val mistakeCounts = dao.getTopicMistakeCounts()
        val topMistakeTopicId = mistakeCounts.firstOrNull()?.topicId

        val weakTopicName = if (topMistakeTopicId != null) {
            val t = dao.getTopicById(topMistakeTopicId)
            t?.title ?: "গ্যাসের আচরণ ও বয়েলের সূত্র"
        } else {
            "বয়েলের সূত্র ও পরম শূন্য তাপমাত্রা"
        }

        AiCoachInsight(
            diagnosisBengali = "আপনার ভৌতবিজ্ঞান ও জীবনবিজ্ঞানে সঠিকতা ৮৪% কিন্তু গ্যাসের গাণিতিক প্রশ্ন ও ত্রিকোণমিতিতে কিছু ধারণা সংশোধন প্রয়োজন।",
            diagnosisEnglish = "Accuracy is strong (84%) in Physical & Life Science. Focus revision on Gas law formulas and Trigonometric identities.",
            readinessScore = readiness,
            topWeakTopic = weakTopicName,
            recommendedActionBengali = "আজ ১০ মিনিটের জন্য '$weakTopicName' রিভিশন করুন এবং ৫টি প্র্যাকটিস প্রশ্ন সমাধান করুন।",
            recommendedActionEnglish = "Spend 10 minutes reviewing '$weakTopicName' key formulas and solve 5 targeted revision questions.",
            dailyRecommendation = "ডেইলি ১০ স্পিড কুইজ সমাধান করে ৭ দিনের স্ট্রিক বজায় রাখুন!"
        )
    }.flowOn(Dispatchers.IO)

    fun getLeaderboard(): List<LeaderboardStudent> = listOf(
        LeaderboardStudent(
            rank = 1,
            name = "অনন্যা মুখোপাধ্যায়",
            school = "হিন্দু স্কুল",
            district = "কলকাতা",
            xp = 4890,
            accuracy = 96,
            streak = 21,
            mockScore = 89,
            mockTotal = 90,
            mockPercentile = 99.8,
            bossVictories = 18,
            badgesCount = 12,
            badgeTitle = "মক টেস্ট বেঙ্গল টপার",
            subjectMockScores = mapOf(
                "phy_sci" to 89,
                "life_sci" to 90,
                "math" to 88,
                "history" to 86,
                "geography" to 89,
                "bengali" to 87,
                "english" to 88
            )
        ),
        LeaderboardStudent(
            rank = 2,
            name = "শুভম পাল",
            school = "যাদবপুর বিদ্যাপীঠ",
            district = "কলকাতা",
            xp = 4650,
            accuracy = 94,
            streak = 18,
            mockScore = 87,
            mockTotal = 90,
            mockPercentile = 99.2,
            bossVictories = 16,
            badgesCount = 10,
            badgeTitle = "অধ্যায় বস অপরাজিত",
            subjectMockScores = mapOf(
                "phy_sci" to 88,
                "life_sci" to 85,
                "math" to 90,
                "history" to 84,
                "geography" to 86,
                "bengali" to 84,
                "english" to 86
            )
        ),
        LeaderboardStudent(
            rank = 3,
            name = "সৌরভ দাস (আপনি)",
            school = "কলকাতা মডেল হাই স্কুল",
            district = "কলকাতা",
            xp = 2450,
            accuracy = 82,
            streak = 7,
            mockScore = 82,
            mockTotal = 90,
            mockPercentile = 94.5,
            bossVictories = 9,
            badgesCount = 6,
            isCurrentUser = true,
            badgeTitle = "রাইজিং স্টার যোদ্ধা",
            subjectMockScores = mapOf(
                "phy_sci" to 84,
                "life_sci" to 86,
                "math" to 76,
                "history" to 82,
                "geography" to 85,
                "bengali" to 83,
                "english" to 80
            )
        ),
        LeaderboardStudent(
            rank = 4,
            name = "রিয়া চক্রবর্তী",
            school = "বেথুন কলেজিয়েট স্কুল",
            district = "উত্তর ২৪ পরগনা",
            xp = 2380,
            accuracy = 85,
            streak = 9,
            mockScore = 84,
            mockTotal = 90,
            mockPercentile = 95.8,
            bossVictories = 8,
            badgesCount = 7,
            badgeTitle = "বিজ্ঞান স্কলার",
            subjectMockScores = mapOf(
                "phy_sci" to 86,
                "life_sci" to 88,
                "math" to 80,
                "history" to 85,
                "geography" to 83,
                "bengali" to 86,
                "english" to 84
            )
        ),
        LeaderboardStudent(
            rank = 5,
            name = "অয়ন সেনগুপ্ত",
            school = "বালিগঞ্জ গভঃ হাই স্কুল",
            district = "হাওড়া",
            xp = 2120,
            accuracy = 79,
            streak = 6,
            mockScore = 78,
            mockTotal = 90,
            mockPercentile = 91.2,
            bossVictories = 7,
            badgesCount = 5,
            badgeTitle = "গণিত চ্যাম্পিয়ন",
            subjectMockScores = mapOf(
                "phy_sci" to 78,
                "life_sci" to 80,
                "math" to 86,
                "history" to 75,
                "geography" to 78,
                "bengali" to 79,
                "english" to 82
            )
        ),
        LeaderboardStudent(
            rank = 6,
            name = "দেবস্মিতা রায়",
            school = "পাঠভবন",
            district = "পশ্চিম মেদিনীপুর",
            xp = 1950,
            accuracy = 88,
            streak = 12,
            mockScore = 80,
            mockTotal = 90,
            mockPercentile = 93.0,
            bossVictories = 6,
            badgesCount = 6,
            badgeTitle = "স্পিড মাস্টার",
            subjectMockScores = mapOf(
                "phy_sci" to 81,
                "life_sci" to 84,
                "math" to 78,
                "history" to 81,
                "geography" to 80,
                "bengali" to 88,
                "english" to 78
            )
        ),
        LeaderboardStudent(
            rank = 7,
            name = "রাহুল ব্যানার্জি",
            school = "সাউথ পয়েন্ট হাই স্কুল",
            district = "বর্ধমান",
            xp = 1820,
            accuracy = 75,
            streak = 4,
            mockScore = 75,
            mockTotal = 90,
            mockPercentile = 88.0,
            bossVictories = 5,
            badgesCount = 4,
            badgeTitle = "ইতিহাস গবেষক",
            subjectMockScores = mapOf(
                "phy_sci" to 74,
                "life_sci" to 76,
                "math" to 75,
                "history" to 88,
                "geography" to 82,
                "bengali" to 76,
                "english" to 75
            )
        ),
        LeaderboardStudent(
            rank = 8,
            name = "তানিয়া পারভীন",
            school = "মালদা জিলা স্কুল",
            district = "মালদা",
            xp = 1710,
            accuracy = 86,
            streak = 11,
            mockScore = 79,
            mockTotal = 90,
            mockPercentile = 92.4,
            bossVictories = 5,
            badgesCount = 5,
            badgeTitle = "জীবনবিজ্ঞান বিশেষজ্ঞ",
            subjectMockScores = mapOf(
                "phy_sci" to 79,
                "life_sci" to 87,
                "math" to 72,
                "history" to 80,
                "geography" to 79,
                "bengali" to 82,
                "english" to 81
            )
        )
    )

    fun getFriendChallenges(): List<FriendChallengeItem> = listOf(
        FriendChallengeItem("c1", "শুভম পাল", "যাদবপুর বিদ্যাপীঠ", "ভৌতবিজ্ঞান ও পরিবেশ", 5, 4, 3, "Won"),
        FriendChallengeItem("c2", "অনন্যা মুখোপাধ্যায়", "হিন্দু স্কুল", "জীবনবিজ্ঞান", 5, 3, 5, "Lost"),
        FriendChallengeItem("c3", "রিয়া চক্রবর্তী", "বেথুন স্কুল", "গণিত দ্বিঘাত সমীকরণ", 5, 0, 0, "Pending")
    )

    private fun QuestionEntity.toDomainModel(randomizeOptions: Boolean = true): QuestionItem {
        val rawOptions = optionsJson.split("|").filter { it.isNotBlank() }
        if (rawOptions.size <= 1 || !randomizeOptions) {
            return QuestionItem(
                id = id,
                topicId = topicId,
                chapterId = chapterId,
                subjectId = subjectId,
                type = try { QuestionType.valueOf(type) } catch (e: Exception) { QuestionType.MCQ },
                difficulty = try { Difficulty.valueOf(difficulty) } catch (e: Exception) { Difficulty.MEDIUM },
                question = question,
                options = rawOptions,
                correctOptionIndex = correctOptionIndex.coerceIn(0, (rawOptions.size - 1).coerceAtLeast(0)),
                correctAnswerText = correctAnswerText,
                explanation = explanation,
                marks = marks,
                tags = tagsJson.split("|")
            )
        }

        // Dynamically randomize/shuffle the 4 options and determine new correct index
        val originalCorrectText = if (correctAnswerText.isNotBlank() && rawOptions.contains(correctAnswerText)) {
            correctAnswerText
        } else {
            rawOptions.getOrNull(correctOptionIndex) ?: rawOptions.first()
        }

        val shuffledOptions = rawOptions.shuffled()
        val newCorrectIndex = shuffledOptions.indexOf(originalCorrectText).coerceAtLeast(0)

        return QuestionItem(
            id = id,
            topicId = topicId,
            chapterId = chapterId,
            subjectId = subjectId,
            type = try { QuestionType.valueOf(type) } catch (e: Exception) { QuestionType.MCQ },
            difficulty = try { Difficulty.valueOf(difficulty) } catch (e: Exception) { Difficulty.MEDIUM },
            question = question,
            options = shuffledOptions,
            correctOptionIndex = newCorrectIndex,
            correctAnswerText = originalCorrectText,
            explanation = explanation,
            marks = marks,
            tags = tagsJson.split("|")
        )
    }

    // =========================================================================
    // REALTIME FIREBASE SYNC LOGIC
    // =========================================================================

    suspend fun syncSubjectFromFirestore(fSubject: com.example.data.firestore.FirestoreSubject) = withContext(Dispatchers.IO) {
        val entity = com.example.data.local.SubjectEntity(
            id = fSubject.id,
            code = fSubject.code,
            name = fSubject.name,
            textbookName = fSubject.textbookName,
            totalChapters = fSubject.totalChapters,
            colorHex = fSubject.parsedColorHex,
            classId = fSubject.classId,
            stream = fSubject.stream
        )
        dao.insertSubjects(listOf(entity))
    }

    suspend fun syncChapterFromFirestore(fChapter: com.example.data.firestore.FirestoreChapter) = withContext(Dispatchers.IO) {
        val entity = com.example.data.local.ChapterEntity(
            id = fChapter.id,
            subjectId = fChapter.subjectId,
            chapterNumber = fChapter.chapterNumber,
            title = fChapter.title,
            description = fChapter.description,
            totalTopics = fChapter.totalTopics,
            completedTopics = 0,
            bossUnlocked = false,
            bossCompleted = false,
            masteryPercentage = 0,
            classId = fChapter.classId,
            contentData = fChapter.contentData,
            solutionData = fChapter.solutionData
        )
        dao.insertChapters(listOf(entity))
    }

    suspend fun syncTopicFromFirestore(fTopic: com.example.data.firestore.FirestoreTopic) = withContext(Dispatchers.IO) {
        val entity = com.example.data.local.TopicEntity(
            id = fTopic.id,
            chapterId = fTopic.chapterId,
            subjectId = fTopic.subjectId,
            orderIndex = fTopic.orderIndex,
            title = fTopic.title,
            summary = fTopic.summary,
            keyPointsJson = fTopic.keyPoints.joinToString("|"),
            isUnlocked = fTopic.orderIndex == 1,
            masteryPercentage = 0,
            questionCount = fTopic.questionCount,
            classId = fTopic.classId,
            contentData = fTopic.contentData,
            solutionData = fTopic.solutionData,
            videoUrl = fTopic.videoUrl,
            playlistJson = fTopic.playlistJson
        )
        dao.insertTopics(listOf(entity))
    }

    suspend fun syncQuestionFromFirestore(fQuestion: com.example.data.firestore.FirestoreQuestion) = withContext(Dispatchers.IO) {
        val entity = QuestionEntity(
            id = fQuestion.id,
            topicId = fQuestion.topicId,
            chapterId = fQuestion.chapterId,
            subjectId = fQuestion.subjectId,
            type = fQuestion.type,
            difficulty = fQuestion.difficulty,
            question = fQuestion.question,
            optionsJson = fQuestion.options.joinToString("|"),
            correctOptionIndex = fQuestion.correctOptionIndex,
            correctAnswerText = fQuestion.correctAnswerText,
            explanation = fQuestion.explanation,
            marks = fQuestion.marks,
            tagsJson = fQuestion.tags.joinToString("|")
        )
        dao.insertQuestions(listOf(entity))
    }

    // Bookmarks & Recently Viewed
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    
    suspend fun addBookmark(bookmark: BookmarkEntity) = dao.addBookmark(bookmark)
    
    suspend fun removeBookmark(id: String) = dao.removeBookmark(id)
    
    suspend fun isBookmarked(id: String): Boolean = dao.isBookmarked(id)
    
    fun getRecentlyViewed(): Flow<List<RecentlyViewedEntity>> = dao.getRecentlyViewed()
    
    suspend fun addRecentlyViewed(recentlyViewed: RecentlyViewedEntity) = dao.addRecentlyViewed(recentlyViewed)

    // Load separate solutions.json for topics
    data class TopicSolutionsWrapper(
        val topicId: String,
        val solutions: List<SolutionItem>
    )

    data class MatchingPair(
        val columnA: String,
        val columnB: String
    )

    data class SolutionItem(
        val type: String,
        val question: String,
        val options: List<String>? = null,
        val correctOption: Int? = null,
        val answer: String,
        val marks: Int,
        val matchingPairs: List<MatchingPair>? = null
    )

    suspend fun getSolutionsFromAssetForChapter(chapterId: String): String? = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext null
        val assetManager = ctx.assets
        val topics = dao.getTopicsForChapterOnce(chapterId)
        val topicIds = topics.map { it.id }.toSet()

        fun searchSolutionsInFolder(path: String): String? {
            try {
                val files = assetManager.list(path) ?: return null
                if (files.contains("solutions.json")) {
                    val stream = assetManager.open("$path/solutions.json")
                    val reader = java.io.InputStreamReader(stream)
                    val listType = object : com.google.gson.reflect.TypeToken<List<TopicSolutionsWrapper>>() {}.type
                    val wrappers: List<TopicSolutionsWrapper> = com.google.gson.Gson().fromJson(reader, listType)
                    val matchedWrappers = wrappers.filter { it.topicId in topicIds }
                    if (matchedWrappers.isNotEmpty()) {
                        val sb = StringBuilder()
                        matchedWrappers.forEach { wrapper ->
                            wrapper.solutions.forEach { item ->
                                sb.append("**${item.question}** `[${item.type} • মান: ${item.marks}]`\n\n")
                                
                                if (!item.options.isNullOrEmpty()) {
                                    item.options.forEachIndexed { optIdx, opt ->
                                        val optNum = optIdx + 1
                                        val isCorrect = item.correctOption == optNum
                                        val marker = if (isCorrect) " ✅ [সঠিক উত্তর]" else ""
                                        sb.append("* $optNum. $opt$marker\n")
                                    }
                                    sb.append("\n")
                                }

                                if (!item.matchingPairs.isNullOrEmpty()) {
                                    item.matchingPairs.forEach { pair ->
                                        sb.append("- ${pair.columnA}  ──>  ${pair.columnB}\n")
                                    }
                                    sb.append("\n")
                                }
                                
                                sb.append("**উত্তর:** ${item.answer}\n\n")
                            }
                        }
                        return sb.toString().trim()
                    }
                }
                for (file in files) {
                    if (!file.contains(".")) {
                        val result = searchSolutionsInFolder("$path/$file")
                        if (result != null) return result
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        val assetResult = searchSolutionsInFolder("data")
        if (!assetResult.isNullOrBlank()) {
            return@withContext assetResult
        }

        // Fallback to real up-to-date questions from Room DB for this chapter
        val questions = dao.getQuestionsForChapter(chapterId)
        if (questions.isEmpty()) return@withContext null

        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        val sb = StringBuilder()
        sb.append("### 📚 অধ্যায়ের গুরুত্বপূর্ণ প্রশ্ন ও বিস্তারিত সমাধান (WBBSE Real Up-to-Date)\n\n")
        questions.forEachIndexed { idx, q ->
            val qNum = idx + 1
            sb.append("**$qNum. ${q.question}** `[${q.type} • মান: ${q.marks}]`\n\n")
            val opts: List<String> = when {
                q.optionsJson.isBlank() -> emptyList()
                q.optionsJson.startsWith("[") -> try {
                    gson.fromJson(q.optionsJson, type) ?: emptyList()
                } catch (e: Exception) {
                    q.optionsJson.split("|").filter { it.isNotBlank() }
                }
                q.optionsJson.contains("|") -> q.optionsJson.split("|").filter { it.isNotBlank() }
                else -> listOf(q.optionsJson)
            }

            if (opts.isNotEmpty()) {
                opts.forEachIndexed { optIdx, opt ->
                    val optNum = optIdx + 1
                    val isCorrect = q.correctOptionIndex == optIdx || q.correctAnswerText.trim().equals(opt.trim(), ignoreCase = true)
                    val marker = if (isCorrect) " ✅ [সঠিক উত্তর]" else ""
                    sb.append("* $optNum. $opt$marker\n")
                }
                sb.append("\n")
            }
            if (q.correctAnswerText.isNotBlank()) {
                sb.append("**সঠিক উত্তর:** ${q.correctAnswerText}\n\n")
            }
            if (!q.explanation.isNullOrBlank()) {
                sb.append("**ব্যাখ্যা:** ${q.explanation}\n\n")
            }
            sb.append("---\n\n")
        }
        return@withContext sb.toString().trim()
    }

    suspend fun getSolutionsFromAssetForTopic(topicId: String): String? = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext null
        val assetManager = ctx.assets

        fun searchSolutionsInFolder(path: String): String? {
            try {
                val files = assetManager.list(path) ?: return null
                if (files.contains("solutions.json")) {
                    val stream = assetManager.open("$path/solutions.json")
                    val reader = java.io.InputStreamReader(stream)
                    val listType = object : com.google.gson.reflect.TypeToken<List<TopicSolutionsWrapper>>() {}.type
                    val wrappers: List<TopicSolutionsWrapper> = com.google.gson.Gson().fromJson(reader, listType)
                    val wrapper = wrappers.find { it.topicId == topicId }
                    if (wrapper != null) {
                        val sb = StringBuilder()
                        sb.append("### 📝 পাঠ্যবইয়ের অনুশীলনী ও পূর্ণাঙ্গ সমাধান\n\n")
                        wrapper.solutions.forEach { item ->
                            sb.append("**${item.question}** `[${item.type} • মান: ${item.marks}]`\n\n")
                            
                            if (!item.options.isNullOrEmpty()) {
                                item.options.forEachIndexed { optIdx, opt ->
                                    val optNum = optIdx + 1
                                    val isCorrect = item.correctOption == optNum
                                    val marker = if (isCorrect) " ✅ [সঠিক উত্তর]" else ""
                                    sb.append("* $optNum. $opt$marker\n")
                                }
                                sb.append("\n")
                            }

                            if (!item.matchingPairs.isNullOrEmpty()) {
                                item.matchingPairs.forEach { pair ->
                                    sb.append("- ${pair.columnA}  ──>  ${pair.columnB}\n")
                                }
                                sb.append("\n")
                            }
                            
                            sb.append("**উত্তর:** ${item.answer}\n\n---\n\n")
                        }
                        return sb.toString().trim()
                    }
                }
                for (file in files) {
                    if (!file.contains(".")) {
                        val result = searchSolutionsInFolder("$path/$file")
                        if (result != null) return result
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        val assetResult = searchSolutionsInFolder("data")
        if (!assetResult.isNullOrBlank()) {
            return@withContext assetResult
        }

        // Fallback to real up-to-date questions from Room DB for this topic
        val questions = dao.getQuestionsForTopic(topicId)
        if (questions.isEmpty()) return@withContext null

        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        val sb = StringBuilder()
        sb.append("### 📖 টপিকের গুরুত্বপূর্ণ প্রশ্ন ও বিস্তারিত সমাধান\n\n")
        questions.forEachIndexed { idx, q ->
            val qNum = idx + 1
            sb.append("**$qNum. ${q.question}** `[${q.type} • মান: ${q.marks}]`\n\n")
            val opts: List<String> = when {
                q.optionsJson.isBlank() -> emptyList()
                q.optionsJson.startsWith("[") -> try {
                    gson.fromJson(q.optionsJson, type) ?: emptyList()
                } catch (e: Exception) {
                    q.optionsJson.split("|").filter { it.isNotBlank() }
                }
                q.optionsJson.contains("|") -> q.optionsJson.split("|").filter { it.isNotBlank() }
                else -> listOf(q.optionsJson)
            }

            if (opts.isNotEmpty()) {
                opts.forEachIndexed { optIdx, opt ->
                    val optNum = optIdx + 1
                    val isCorrect = q.correctOptionIndex == optIdx || q.correctAnswerText.trim().equals(opt.trim(), ignoreCase = true)
                    val marker = if (isCorrect) " ✅ [সঠিক উত্তর]" else ""
                    sb.append("* $optNum. $opt$marker\n")
                }
                sb.append("\n")
            }
            if (q.correctAnswerText.isNotBlank()) {
                sb.append("**সঠিক উত্তর:** ${q.correctAnswerText}\n\n")
            }
            if (!q.explanation.isNullOrBlank()) {
                sb.append("**ব্যাখ্যা:** ${q.explanation}\n\n")
            }
            sb.append("---\n\n")
        }
        return@withContext sb.toString().trim()
    }

    suspend fun syncDatabaseWithAssets(forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext
        try {
            com.example.data.local.DeshExamDatabase.populateDataFromAssets(ctx, dao, forceRefresh)
        } catch (e: Exception) {
            android.util.Log.e("DeshExamRepository", "Error syncing database with assets: ${e.message}")
        }
    }
}

