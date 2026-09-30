package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        RecentlyViewedEntity::class
    ],
    version = 39,
    exportSchema = false
)
abstract class DeshExamDatabase : RoomDatabase() {

    abstract fun deshExamDao(): DeshExamDao

    companion object {
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
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope,
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.deshExamDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.deshExamDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: DeshExamDao) {
                val gson = com.google.gson.Gson()
                val assetManager = context.assets

                suspend fun loadFromFolder(path: String) {
                    val files = try {
                        assetManager.list(path)
                    } catch (e: Exception) { null } ?: return

                    if (files.contains("subject.json")) {
                        // Load Subject
                        try {
                            val subjectStream = assetManager.open("$path/subject.json")
                            val subject = gson.fromJson(java.io.InputStreamReader(subjectStream), SubjectEntity::class.java)
                            dao.insertSubjects(listOf(subject))
                        } catch (e: Exception) { e.printStackTrace() }

                        // Load Chapters
                        try {
                            val chaptersStream = assetManager.open("$path/chapters.json")
                            val listType = object : com.google.gson.reflect.TypeToken<List<ChapterEntity>>() {}.type
                            val chapters: List<ChapterEntity> = gson.fromJson(java.io.InputStreamReader(chaptersStream), listType)
                            dao.insertChapters(chapters)
                        } catch (e: Exception) { e.printStackTrace() }

                        // Load Topics
                        try {
                            val topicsStream = assetManager.open("$path/topics.json")
                            val listType = object : com.google.gson.reflect.TypeToken<List<TopicEntity>>() {}.type
                            val topics: List<TopicEntity> = gson.fromJson(java.io.InputStreamReader(topicsStream), listType)
                            dao.insertTopics(topics)
                        } catch (e: Exception) { e.printStackTrace() }

                        // Load Questions
                        try {
                            val listType = object : com.google.gson.reflect.TypeToken<List<QuestionEntity>>() {}.type
                            val questionsFiles = assetManager.list("$path/questions")
                            if (!questionsFiles.isNullOrEmpty()) {
                                // Load all json files from the questions folder
                                for (qFile in questionsFiles) {
                                    if (qFile.endsWith(".json")) {
                                        val questionsStream = assetManager.open("$path/questions/$qFile")
                                        val questions: List<QuestionEntity> = gson.fromJson(java.io.InputStreamReader(questionsStream), listType)
                                        dao.insertQuestions(questions)
                                    }
                                }
                            } else {
                                // Fallback to single questions.json
                                val questionsStream = assetManager.open("$path/questions.json")
                                val questions: List<QuestionEntity> = gson.fromJson(java.io.InputStreamReader(questionsStream), listType)
                                dao.insertQuestions(questions)
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    } else {
                        // Recursively search subdirectories
                        for (file in files) {
                            if (!file.contains(".")) {
                                loadFromFolder("$path/$file")
                            }
                        }
                    }
                }

                loadFromFolder("data")

                // Initial quests and progress
                dao.insertQuests(SeedData.getInitialQuests())
                dao.insertOrUpdateProgress(UserProgressEntity())
            }
        }
    }
}
