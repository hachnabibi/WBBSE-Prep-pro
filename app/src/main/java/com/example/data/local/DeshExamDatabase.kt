package com.example.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.data.repository.DeshExamRepository

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        TopicEntity::class,
        QuestionEntity::class,
        QuestionAttemptEntity::class,
        UserProgressEntity::class,
        DailyQuestEntity::class,
        BookmarkEntity::class,
        RecentlyViewedEntity::class,
        VideoProgressEntity::class
    ],
    version = 43,
    exportSchema = false
)
abstract class DeshExamDatabase : RoomDatabase() {

    abstract fun deshExamDao(): DeshExamDao

    companion object {
        private const val TAG = "DeshExamDatabase"

        @Volatile
        private var INSTANCE: DeshExamDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DeshExamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeshExamDatabase::class.java,
                    "deshexam_database"
                )
                    .addCallback(DatabaseCallback(scope, context.applicationContext))
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                
                // Immediately trigger asset sync in background
                scope.launch(Dispatchers.IO) {
                    try {
                        populateDataFromAssets(context.applicationContext, instance.deshExamDao())
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during initial database populate: ${e.message}", e)
                    }
                }
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope,
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch(Dispatchers.IO) {
                    INSTANCE?.let { database ->
                        populateDataFromAssets(context, database.deshExamDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                scope.launch(Dispatchers.IO) {
                    INSTANCE?.let { database ->
                        populateDataFromAssets(context, database.deshExamDao())
                    }
                }
            }
        }

        suspend fun populateDataFromAssets(context: Context, dao: DeshExamDao, forceRefresh: Boolean = false) {
            val gson = com.google.gson.Gson()
            val assetManager = context.assets

            suspend fun loadFromFolder(path: String) {
                val files = try {
                    assetManager.list(path)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to list files at $path: ${e.message}")
                    null
                } ?: return

                if (files.contains("subject.json")) {
                    val inferredClassId = if (path.contains("class_5")) "class_5_wbbse" else "class_10_wbbse"

                    // 1. Load Subject
                    try {
                        val subjectStream = assetManager.open("$path/subject.json")
                        val subject = gson.fromJson(java.io.InputStreamReader(subjectStream), SubjectEntity::class.java)
                        if (subject != null) {
                            val safeSubject = subject.copy(
                                id = subject.id ?: "",
                                code = subject.code ?: subject.id ?: "",
                                name = subject.name ?: "",
                                classId = subject.classId ?: inferredClassId,
                                stream = subject.stream ?: "common",
                                textbookName = subject.textbookName ?: ""
                            )
                            dao.insertSubjects(listOf(safeSubject))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed loading subject from $path/subject.json: ${e.message}")
                    }

                    // 2. Load Chapters
                    try {
                        if (files.contains("chapters.json")) {
                            val chaptersStream = assetManager.open("$path/chapters.json")
                            val listType = object : com.google.gson.reflect.TypeToken<List<ChapterEntity>>() {}.type
                            val chapters: List<ChapterEntity>? = gson.fromJson(java.io.InputStreamReader(chaptersStream), listType)
                            if (!chapters.isNullOrEmpty()) {
                                val safeChapters = chapters.map { ch ->
                                    ch.copy(
                                        id = ch.id ?: "",
                                        subjectId = ch.subjectId ?: "",
                                        title = ch.title ?: "",
                                        description = ch.description ?: "",
                                        classId = ch.classId ?: inferredClassId,
                                        contentData = ch.contentData,
                                        solutionData = ch.solutionData
                                    )
                                }
                                dao.insertChapters(safeChapters)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed loading chapters from $path/chapters.json: ${e.message}")
                    }

                    // 3. Load Topics & Solutions
                    try {
                        if (files.contains("topics.json")) {
                            val topicsStream = assetManager.open("$path/topics.json")
                            val listType = object : com.google.gson.reflect.TypeToken<List<TopicEntity>>() {}.type
                            val rawTopics: List<TopicEntity>? = gson.fromJson(java.io.InputStreamReader(topicsStream), listType)
                            if (!rawTopics.isNullOrEmpty()) {
                                var topics = rawTopics.map { top ->
                                    top.copy(
                                        id = top.id ?: "",
                                        chapterId = top.chapterId ?: "",
                                        subjectId = top.subjectId ?: "",
                                        title = top.title ?: "",
                                        summary = top.summary ?: "",
                                        keyPointsJson = top.keyPointsJson ?: "",
                                        classId = top.classId ?: inferredClassId,
                                        contentData = top.contentData,
                                        solutionData = top.solutionData,
                                        videoUrl = top.videoUrl,
                                        playlistJson = top.playlistJson
                                    )
                                }
                                
                                // Check if solutions.json is available in this folder to enrich topic solutionData
                                if (files.contains("solutions.json")) {
                                    try {
                                        val solStream = assetManager.open("$path/solutions.json")
                                        val solType = object : com.google.gson.reflect.TypeToken<List<DeshExamRepository.TopicSolutionsWrapper>>() {}.type
                                        val wrappers: List<DeshExamRepository.TopicSolutionsWrapper>? = gson.fromJson(java.io.InputStreamReader(solStream), solType)
                                        if (!wrappers.isNullOrEmpty()) {
                                            val solMap = wrappers.associateBy { it.topicId }
                                            topics = topics.map { topic ->
                                                val wrapper = solMap[topic.id]
                                                if (wrapper != null && (topic.solutionData.isNullOrBlank() || topic.solutionData.contains("এর প্রশ্নোত্তরগুলো এখানে থাকবে"))) {
                                                    val sb = java.lang.StringBuilder()
                                                    sb.append("## 📝 পাঠ্যবইয়ের হাতে-কলমে সমাধান ও প্রশ্নোত্তর\n\n")
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
                                                    topic.copy(solutionData = sb.toString().trim())
                                                } else {
                                                    topic
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Failed merging solutions.json for $path: ${e.message}")
                                    }
                                }

                                // Check if videos.json is available in this folder to register dedicated video lessons
                                if (files.contains("videos.json")) {
                                    try {
                                        val vidStream = assetManager.open("$path/videos.json")
                                        val vidType = object : com.google.gson.reflect.TypeToken<List<com.example.data.model.VideoLesson>>() {}.type
                                        val rawVideoLessons: List<com.example.data.model.VideoLesson>? = gson.fromJson(java.io.InputStreamReader(vidStream), vidType)
                                        if (!rawVideoLessons.isNullOrEmpty()) {
                                            val videoLessons = rawVideoLessons.filterNotNull()
                                            com.example.data.model.VideoLessonProvider.registerLessons(videoLessons)
                                            val videoMap = videoLessons.associateBy { it.safeTopicId }
                                            topics = topics.map { topic ->
                                                val vid = videoMap[topic.id]
                                                if (vid != null) {
                                                    topic.copy(
                                                        videoUrl = vid.getEffectiveVideoSource(),
                                                        playlistJson = if (vid.safePlaylist.isNotEmpty()) gson.toJson(vid.safePlaylist) else topic.playlistJson
                                                    )
                                                } else {
                                                    topic
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Failed merging videos.json for $path: ${e.message}")
                                    }
                                }

                                dao.insertTopics(topics)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed loading topics from $path/topics.json: ${e.message}", e)
                    }

                    // 4. Load Questions
                    try {
                        val listType = object : com.google.gson.reflect.TypeToken<List<QuestionEntity>>() {}.type
                        val questionsFiles = try { assetManager.list("$path/questions") } catch (e: Exception) { null }
                        if (!questionsFiles.isNullOrEmpty()) {
                            for (qFile in questionsFiles) {
                                if (qFile.endsWith(".json")) {
                                    val questionsStream = assetManager.open("$path/questions/$qFile")
                                    val rawQuestions: List<QuestionEntity>? = gson.fromJson(java.io.InputStreamReader(questionsStream), listType)
                                    if (!rawQuestions.isNullOrEmpty()) {
                                        val safeQuestions = rawQuestions.map { q ->
                                            q.copy(
                                                id = q.id ?: "",
                                                topicId = q.topicId ?: "",
                                                chapterId = q.chapterId ?: "",
                                                subjectId = q.subjectId ?: "",
                                                type = q.type ?: "MCQ",
                                                difficulty = q.difficulty ?: "MEDIUM",
                                                question = q.question ?: "",
                                                optionsJson = q.optionsJson ?: "",
                                                correctAnswerText = q.correctAnswerText ?: "",
                                                explanation = q.explanation ?: "",
                                                tagsJson = q.tagsJson ?: "",
                                                pyqYear = q.pyqYear ?: "",
                                                classId = q.classId ?: inferredClassId
                                            )
                                        }
                                        dao.insertQuestions(safeQuestions)
                                    }
                                }
                            }
                        } else if (files.contains("questions.json")) {
                            val questionsStream = assetManager.open("$path/questions.json")
                            val rawQuestions: List<QuestionEntity>? = gson.fromJson(java.io.InputStreamReader(questionsStream), listType)
                            if (!rawQuestions.isNullOrEmpty()) {
                                val safeQuestions = rawQuestions.map { q ->
                                    q.copy(
                                        id = q.id ?: "",
                                        topicId = q.topicId ?: "",
                                        chapterId = q.chapterId ?: "",
                                        subjectId = q.subjectId ?: "",
                                        type = q.type ?: "MCQ",
                                        difficulty = q.difficulty ?: "MEDIUM",
                                        question = q.question ?: "",
                                        optionsJson = q.optionsJson ?: "",
                                        correctAnswerText = q.correctAnswerText ?: "",
                                        explanation = q.explanation ?: "",
                                        tagsJson = q.tagsJson ?: "",
                                        pyqYear = q.pyqYear ?: "",
                                        classId = q.classId ?: inferredClassId
                                    )
                                }
                                dao.insertQuestions(safeQuestions)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed loading questions from $path: ${e.message}", e)
                    }
                } else {
                    // Recursively explore subdirectories
                    for (file in files) {
                        if (!file.contains(".")) {
                            loadFromFolder("$path/$file")
                        }
                    }
                }
            }

            try {
                loadFromFolder("data")
            } catch (e: Exception) {
                Log.e(TAG, "Error traversing assets/data: ${e.message}", e)
            }

            // Initial quests and user progress if missing
            try {
                dao.insertQuests(SeedData.getInitialQuests())
                val currentProgress = dao.getUserProgressOnce()
                if (currentProgress == null) {
                    dao.insertOrUpdateProgress(UserProgressEntity())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding quests or progress: ${e.message}")
            }
        }
    }
}
