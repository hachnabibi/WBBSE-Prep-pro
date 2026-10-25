package com.example.data.firestore

import android.util.Log
import com.example.data.model.AssessmentResult
import com.example.data.model.LeaderboardStudent
import com.example.data.model.StudentProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Service class for handling full Firestore operations for:
 * 1. Class, Subject, Chapter, Topic hierarchy.
 * 2. Topic-wise, Chapter-wise, Subject-wise and Mock Tests.
 * 3. Student Test Attempts, Answers and Analytics.
 * 4. User Progress, Streaks and Statewide Leaderboard.
 */
class FirestoreService(
    private val context: android.content.Context? = null,
    firestoreOverride: FirebaseFirestore? = null
) {
    private val firestoreInstance: FirebaseFirestore? by lazy {
        firestoreOverride ?: try {
            val isUnitTest = try {
                Class.forName("org.robolectric.Robolectric") != null
            } catch (_: Throwable) {
                false
            }
            if (isUnitTest) {
                Log.i(TAG, "Unit test environment (Robolectric) detected. Skipping live Firestore background initialization.")
                return@lazy null
            }
            val dbContext = context ?: try {
                com.google.firebase.FirebaseApp.getInstance().applicationContext
            } catch (_: Throwable) {
                null
            }
            if (dbContext != null) {
                try {
                    val dbDir = dbContext.getDatabasePath("firestore_init").parentFile
                    if (dbDir != null && !dbDir.exists()) {
                        dbDir.mkdirs()
                    }
                } catch (_: Throwable) {}
            }
            val db = if (dbContext != null) {
                val dbIdRes = dbContext.resources.getIdentifier("firestore_database_id", "string", dbContext.packageName)
                val dbId = if (dbIdRes != 0) {
                    val value = dbContext.getString(dbIdRes).trim()
                    value.ifBlank { "" }
                } else {
                    ""
                }
                if (dbId.isNotBlank()) {
                    Log.i(TAG, "Using Firestore custom database ID: $dbId")
                    FirebaseFirestore.getInstance(dbId)
                } else {
                    Log.w(TAG, "Firestore database id missing or blank, using default instance.")
                    FirebaseFirestore.getInstance()
                }
            } else {
                Log.w(TAG, "No context or FirebaseApp found, using default instance.")
                FirebaseFirestore.getInstance()
            }
            try {
                val isTest = try {
                    Class.forName("org.robolectric.Robolectric") != null
                } catch (_: Throwable) {
                    false
                }
                val cacheSettings = if (isTest) {
                    com.google.firebase.firestore.MemoryCacheSettings.newBuilder().build()
                } else {
                    com.google.firebase.firestore.PersistentCacheSettings.newBuilder().build()
                }
                val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(cacheSettings)
                    .build()
                db.firestoreSettings = settings
            } catch (_: Throwable) {}
            db
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseFirestore instance not available: ${e.message}")
            null
        }
    }

    private val firestore: FirebaseFirestore
        get() = firestoreInstance ?: throw IllegalStateException("FirebaseFirestore is not initialized")

    val isAvailable: Boolean
        get() = firestoreInstance != null
    companion object {
        private const val TAG = "FirestoreService"

        // Hierarchy Collections
        private const val COLLECTION_BOARDS = "education_boards"
        private const val COLLECTION_CLASSES = "classes"
        private const val COLLECTION_SUBJECTS = "subjects"
        private const val COLLECTION_CHAPTERS = "chapters"
        private const val COLLECTION_TOPICS = "topics"
        private const val COLLECTION_QUESTIONS = "questions"

        // Test Collections
        private const val COLLECTION_TESTS = "test_templates"
        private const val COLLECTION_TEST_ATTEMPTS = "test_attempts"
        private const val COLLECTION_CHAPTER_ASSESSMENTS = "chapter_assessments"
        private const val COLLECTION_SUBJECT_MASTERY_QUIZZES = "subject_mastery_quizzes"
        private const val COLLECTION_FULL_SYLLABUS_MOCKS = "full_syllabus_mocks"

        // Student Data Collections
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_TOPIC_PROGRESS = "topic_progress"
        private const val COLLECTION_CHAPTER_PROGRESS = "chapter_progress"
        private const val COLLECTION_SUBJECT_PROGRESS = "subject_progress"
        private const val COLLECTION_VIDEO_PROGRESS = "video_progress"
        private const val COLLECTION_LEADERBOARD = "leaderboard"
        private const val COLLECTION_DISTRICTS = "districts"
        private const val COLLECTION_PODCASTS = "podcasts"
        private const val COLLECTION_CONTENT_REPORTS = "content_reports"
        private const val COLLECTION_ADMIN_ACTIVITY = "admin_activity"
    }

    suspend fun submitContentReport(report: ContentReport): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val safeReport = report.copy(
                id = report.id.ifBlank { java.util.UUID.randomUUID().toString() },
                questionText = report.questionText.take(1000),
                reason = report.reason.take(80),
                details = report.details.take(1000),
                status = "open"
            )
            firestore.collection(COLLECTION_CONTENT_REPORTS)
                .document(safeReport.id)
                .set(safeReport)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting content report: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getContentReports(limit: Int = 100): Result<List<ContentReport>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_CONTENT_REPORTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.coerceIn(1, 200).toLong())
                .get()
                .await()
            Result.success(snapshot.documents.mapNotNull { document ->
                document.toObject(ContentReport::class.java)?.copy(id = document.id)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error loading content reports: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun updateContentReport(
        reportId: String,
        status: String,
        reviewerUid: String,
        adminNote: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(status in setOf("open", "in_review", "resolved", "dismissed")) {
                "Unsupported report status"
            }
            firestore.collection(COLLECTION_CONTENT_REPORTS)
                .document(reportId)
                .update(
                    mapOf(
                        "status" to status,
                        "reviewedAt" to System.currentTimeMillis(),
                        "reviewedBy" to reviewerUid,
                        "adminNote" to adminNote.take(1000)
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating content report: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun recordAdminActivity(event: AdminActivityEvent): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val safeEvent = event.copy(
                id = event.id.ifBlank { java.util.UUID.randomUUID().toString() },
                summary = event.summary.take(500)
            )
            firestore.collection(COLLECTION_ADMIN_ACTIVITY)
                .document(safeEvent.id)
                .set(safeEvent)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error recording admin activity: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getAdminActivity(limit: Int = 100): Result<List<AdminActivityEvent>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_ADMIN_ACTIVITY)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.coerceIn(1, 200).toLong())
                .get()
                .await()
            Result.success(snapshot.documents.mapNotNull { document ->
                document.toObject(AdminActivityEvent::class.java)?.copy(id = document.id)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error loading admin activity: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun saveDistrictSchools(district: String, schools: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = hashMapOf(
                "name" to district,
                "schools" to schools,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_DISTRICTS)
                .document(district)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving district schools: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllDistrictSchools(): Result<Map<String, List<String>>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_DISTRICTS)
                .get()
                .await()
            val map = mutableMapOf<String, List<String>>()
            for (doc in snapshot.documents) {
                val name = doc.getString("name") ?: doc.id
                @Suppress("UNCHECKED_CAST")
                val schools = doc.get("schools") as? List<String> ?: emptyList()
                if (schools.isNotEmpty()) {
                    map[name] = schools
                }
            }
            Result.success(map)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting district schools: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 0. EDUCATION BOARD (শিক্ষা বোর্ড) MANAGEMENT & REALTIME
    // =========================================================================

    suspend fun saveBoard(board: FirestoreEducationBoard): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_BOARDS)
                .document(board.id)
                .set(board, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved board: ${board.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving board: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteBoard(boardId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_BOARDS)
                .document(boardId)
                .delete()
                .await()
            Log.d(TAG, "Deleted board: $boardId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting board: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllBoards(): Result<List<FirestoreEducationBoard>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_BOARDS)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreEducationBoard::class.java) }
                .filter { it.isActive }
                .sortedBy { it.orderIndex }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting boards: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getBoardById(boardId: String): Result<FirestoreEducationBoard?> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection(COLLECTION_BOARDS)
                .document(boardId)
                .get()
                .await()
            Result.success(doc.toObject(FirestoreEducationBoard::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting board by id: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    fun getBoardsRealtime(): Flow<List<FirestoreEducationBoard>> = callbackFlow {
        if (!isAvailable) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection(COLLECTION_BOARDS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen to boards failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(FirestoreEducationBoard::class.java) }
                        .filter { it.isActive }
                        .sortedBy { it.orderIndex }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    // =========================================================================
    // 1. CLASS (শ্রেণী) MANAGEMENT & REALTIME
    // =========================================================================

    suspend fun saveClass(classItem: FirestoreClass): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_CLASSES)
                .document(classItem.id)
                .set(classItem, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved class: ${classItem.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving class: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteClass(classId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_CLASSES)
                .document(classId)
                .delete()
                .await()
            Log.d(TAG, "Deleted class: $classId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting class: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllClasses(): Result<List<FirestoreClass>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_CLASSES)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreClass::class.java) }
                .filter { it.isActive }
                .sortedBy { it.classNumber }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting classes: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getClassById(classId: String): Result<FirestoreClass?> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection(COLLECTION_CLASSES)
                .document(classId)
                .get()
                .await()
            Result.success(doc.toObject(FirestoreClass::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting class by id: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    fun getClassesRealtime(): Flow<List<FirestoreClass>> = callbackFlow {
        if (!isAvailable) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection(COLLECTION_CLASSES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen to classes failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(FirestoreClass::class.java) }
                        .filter { it.isActive }
                        .sortedBy { it.classNumber }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    // =========================================================================
    // 2. SUBJECT (বিষয়) MANAGEMENT
    // =========================================================================

    suspend fun saveSubject(subject: FirestoreSubject): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_SUBJECTS)
                .document(subject.id)
                .set(subject, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved subject: ${subject.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving subject: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getSubjectsByClass(classId: String = "class_10_wbbse"): Result<List<FirestoreSubject>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_SUBJECTS)
                .whereEqualTo("classId", classId)
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreSubject::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subjects: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteSubject(subjectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_SUBJECTS)
                .document(subjectId)
                .delete()
                .await()
            Log.d(TAG, "Deleted subject: $subjectId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subject: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 3. CHAPTER (অধ্যায়) MANAGEMENT
    // =========================================================================

    suspend fun saveChapter(
        chapter: FirestoreChapter,
        preserveWeightage: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val chapterRef = firestore.collection(COLLECTION_CHAPTERS).document(chapter.id)
            if (preserveWeightage) {
                val updates = mapOf(
                    "id" to chapter.id,
                    "classId" to chapter.classId,
                    "subjectId" to chapter.subjectId,
                    "chapterNumber" to chapter.chapterNumber,
                    "title" to chapter.title,
                    "description" to chapter.description,
                    "totalTopics" to chapter.totalTopics,
                    "orderIndex" to chapter.orderIndex,
                    "contentData" to chapter.contentData,
                    "solutionData" to chapter.solutionData
                )
                chapterRef.set(updates, SetOptions.merge()).await()
            } else {
                chapterRef.set(chapter, SetOptions.merge()).await()
            }
            Log.d(TAG, "Saved chapter: ${chapter.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving chapter: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getChaptersBySubject(subjectId: String): Result<List<FirestoreChapter>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_CHAPTERS)
                .whereEqualTo("subjectId", subjectId)
                .orderBy("chapterNumber", Query.Direction.ASCENDING)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreChapter::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chapters: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 4. TOPIC (টপিক / বিষয়বস্তু) MANAGEMENT
    // =========================================================================

    suspend fun saveTopic(
        topic: FirestoreTopic,
        preserveQuestionCount: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val topicRef = firestore.collection(COLLECTION_TOPICS).document(topic.id)
            if (preserveQuestionCount) {
                val updates = mapOf(
                    "id" to topic.id,
                    "classId" to topic.classId,
                    "subjectId" to topic.subjectId,
                    "chapterId" to topic.chapterId,
                    "orderIndex" to topic.orderIndex,
                    "title" to topic.title,
                    "summary" to topic.summary,
                    "keyPoints" to topic.keyPoints,
                    "formulae" to topic.formulae,
                    "contentData" to topic.contentData,
                    "solutionData" to topic.solutionData,
                    "videoUrl" to topic.videoUrl,
                    "playlistJson" to topic.playlistJson
                )
                topicRef.set(updates, SetOptions.merge()).await()
            } else {
                topicRef.set(topic, SetOptions.merge()).await()
            }
            Log.d(TAG, "Saved topic: ${topic.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving topic: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getTopicsByChapter(chapterId: String): Result<List<FirestoreTopic>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_TOPICS)
                .whereEqualTo("chapterId", chapterId)
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreTopic::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting topics: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 5. QUESTION (প্রশ্নব্যাংক) MANAGEMENT
    // =========================================================================

    suspend fun saveQuestion(question: FirestoreQuestion): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_QUESTIONS)
                .document(question.id)
                .set(question, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving question: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun saveQuestionsBatch(questions: List<FirestoreQuestion>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (questions.isEmpty()) return@withContext Result.success(0)
            var totalSaved = 0
            // Firestore writeBatch max limit is 500 ops per batch. Use safe 150 chunks to prevent payload size timeouts
            val chunks = questions.chunked(150)
            for (chunk in chunks) {
                try {
                    val batch = firestore.batch()
                    for (q in chunk) {
                        val safeId = if (q.id.isNotBlank()) {
                            q.id.replace("/", "_").replace(" ", "_").trim()
                        } else {
                            "q_${q.subjectId.ifBlank { "gen" }}_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}"
                        }
                        val cleanQuestion = q.copy(id = safeId)
                        val docRef = firestore.collection(COLLECTION_QUESTIONS).document(safeId)
                        batch.set(docRef, cleanQuestion, SetOptions.merge())
                    }
                    batch.commit().await()
                    totalSaved += chunk.size
                } catch (batchErr: Exception) {
                    Log.w(TAG, "Batch commit failed (${batchErr.message}), falling back to individual document writes for chunk...")
                    for (q in chunk) {
                        try {
                            val safeId = if (q.id.isNotBlank()) {
                                q.id.replace("/", "_").replace(" ", "_").trim()
                            } else {
                                "q_${q.subjectId.ifBlank { "gen" }}_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}"
                            }
                            val cleanQuestion = q.copy(id = safeId)
                            firestore.collection(COLLECTION_QUESTIONS)
                                .document(safeId)
                                .set(cleanQuestion, SetOptions.merge())
                                .await()
                            totalSaved++
                        } catch (singleErr: Exception) {
                            Log.e(TAG, "Failed saving single question ${q.id}: ${singleErr.message}")
                        }
                    }
                }
            }
            Log.d(TAG, "Successfully saved $totalSaved of ${questions.size} questions to Firestore")
            Result.success(totalSaved)
        } catch (e: Exception) {
            Log.e(TAG, "Error in batch saving questions: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getCloudCollectionCounts(): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val counts = mutableMapOf<String, Int>()
            val boardsSnap = firestore.collection(COLLECTION_BOARDS).count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            counts["boards"] = boardsSnap.count.toInt()
            val classesSnap = firestore.collection(COLLECTION_CLASSES).count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            counts["classes"] = classesSnap.count.toInt()
            val subjectsSnap = firestore.collection(COLLECTION_SUBJECTS).count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            counts["subjects"] = subjectsSnap.count.toInt()
            val chaptersSnap = firestore.collection(COLLECTION_CHAPTERS).count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            counts["chapters"] = chaptersSnap.count.toInt()
            val topicsSnap = firestore.collection(COLLECTION_TOPICS).count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            counts["topics"] = topicsSnap.count.toInt()
            val questionsSnap = firestore.collection(COLLECTION_QUESTIONS).count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            counts["questions"] = questionsSnap.count.toInt()
            val testsSnap = firestore.collection(COLLECTION_TESTS).count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            counts["tests"] = testsSnap.count.toInt()
            Result.success(counts)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cloud collection counts: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getQuestionsByTopic(topicId: String): Result<List<FirestoreQuestion>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_QUESTIONS)
                .whereEqualTo("topicId", topicId)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreQuestion::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting questions for topic: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getQuestionsByChapter(chapterId: String): Result<List<FirestoreQuestion>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_QUESTIONS)
                .whereEqualTo("chapterId", chapterId)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreQuestion::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting questions for chapter: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllQuestions(limit: Long = 1000): Result<List<FirestoreQuestion>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_QUESTIONS)
                .limit(limit)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreQuestion::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all questions: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getQuestionsFiltered(
        classId: String? = null,
        subjectId: String? = null,
        limit: Long = 1000
    ): Result<List<FirestoreQuestion>> = withContext(Dispatchers.IO) {
        try {
            var query: Query = firestore.collection(COLLECTION_QUESTIONS)
            if (!classId.isNullOrBlank()) {
                query = query.whereEqualTo("classId", classId)
            }
            if (!subjectId.isNullOrBlank()) {
                query = query.whereEqualTo("subjectId", subjectId)
            }
            val snapshot = query.limit(limit).get().await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreQuestion::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting filtered questions: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time live synchronization stream for questions.
     * Emits whenever a question is added, updated, or deleted on Firebase Firestore.
     */
    fun observeQuestionsRealtime(classId: String? = null, subjectId: String? = null): Flow<List<FirestoreQuestion>> = callbackFlow {
        try {
            val db = firestoreInstance
            if (db == null) {
                close(IllegalStateException("Firebase Firestore is not initialized"))
                return@callbackFlow
            }
            var query: Query = db.collection(COLLECTION_QUESTIONS)
            if (!classId.isNullOrBlank()) {
                query = query.whereEqualTo("classId", classId)
            }
            if (!subjectId.isNullOrBlank()) {
                query = query.whereEqualTo("subjectId", subjectId)
            }
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Realtime questions listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val questions = snapshot.documents.mapNotNull { it.toObject(FirestoreQuestion::class.java) }
                    trySend(questions)
                }
            }
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            close(e)
        }
    }.flowOn(Dispatchers.IO)

    // =========================================================================
    // 6. TEST TEMPLATES (TOPIC-WISE, CHAPTER-WISE, MOCK TESTS)
    // =========================================================================

    /**
     * Real-time live synchronization stream for test templates.
     */
    fun observeTestTemplatesRealtime(classId: String? = null): Flow<List<FirestoreTestTemplate>> = callbackFlow {
        try {
            val db = firestoreInstance
            if (db == null) {
                close(IllegalStateException("Firebase Firestore is not initialized"))
                return@callbackFlow
            }
            var query: Query = db.collection(COLLECTION_TESTS)
            if (!classId.isNullOrBlank()) {
                query = query.whereEqualTo("classId", classId)
            }
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Realtime tests listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tests = snapshot.documents.mapNotNull { it.toObject(FirestoreTestTemplate::class.java) }
                    trySend(tests)
                }
            }
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            close(e)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Creates or updates a test definition (Topic Quiz, Boss Battle, Full Mock).
     */
    suspend fun saveTestTemplate(testTemplate: FirestoreTestTemplate): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_TESTS)
                .document(testTemplate.id)
                .set(testTemplate, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved test template: ${testTemplate.id} (${testTemplate.testType})")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving test template: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Gets all tests for a specific topic (Topic Practice Quizzes).
     */
    suspend fun getTestsByTopic(topicId: String): Result<List<FirestoreTestTemplate>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_TESTS)
                .whereEqualTo("topicId", topicId)
                .whereEqualTo("published", true)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreTestTemplate::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting topic tests: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Gets all tests for a chapter (e.g. Chapter Boss Battle, Chapter Unit Quizzes).
     */
    suspend fun getTestsByChapter(chapterId: String): Result<List<FirestoreTestTemplate>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_TESTS)
                .whereEqualTo("chapterId", chapterId)
                .whereEqualTo("published", true)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreTestTemplate::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chapter tests: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Optimized composite query for tests by hierarchical nodes (subjectId, chapterId, topicId).
     */
    suspend fun getTestsByHierarchy(
        subjectId: String? = null,
        chapterId: String? = null,
        topicId: String? = null,
        classId: String? = null
    ): Result<List<FirestoreTestTemplate>> = withContext(Dispatchers.IO) {
        try {
            var query: Query = firestore.collection(COLLECTION_TESTS).whereEqualTo("published", true)
            if (!classId.isNullOrBlank()) query = query.whereEqualTo("classId", classId)
            if (!subjectId.isNullOrBlank()) query = query.whereEqualTo("subjectId", subjectId)
            if (!chapterId.isNullOrBlank()) query = query.whereEqualTo("chapterId", chapterId)
            if (!topicId.isNullOrBlank()) query = query.whereEqualTo("topicId", topicId)

            val snapshot = query.get().await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreTestTemplate::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying tests by hierarchy composite index: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Gets full-syllabus mock tests or previous year papers.
     */
    suspend fun getTestsByType(
        classId: String = "class_10_wbbse",
        testType: FirestoreTestType
    ): Result<List<FirestoreTestTemplate>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_TESTS)
                .whereEqualTo("classId", classId)
                .whereEqualTo("testType", testType.name)
                .whereEqualTo("published", true)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreTestTemplate::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tests by type: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getTestById(testId: String): Result<FirestoreTestTemplate?> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection(COLLECTION_TESTS)
                .document(testId)
                .get()
                .await()
            val item = if (doc.exists()) doc.toObject(FirestoreTestTemplate::class.java) else null
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting test by id: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 7. STUDENT TEST ATTEMPTS & RESULTS (পরীক্ষার ফলাফল ও হিস্ট্রি)
    // =========================================================================

    /**
     * Records a student's comprehensive test attempt in Firestore.
     */
    suspend fun recordTestAttempt(attempt: FirestoreTestAttempt): Result<String> = withContext(Dispatchers.IO) {
        try {
            val docRef = if (attempt.id.isNotBlank()) {
                firestore.collection(COLLECTION_USERS)
                    .document(attempt.userId)
                    .collection(COLLECTION_TEST_ATTEMPTS)
                    .document(attempt.id)
            } else {
                firestore.collection(COLLECTION_USERS)
                    .document(attempt.userId)
                    .collection(COLLECTION_TEST_ATTEMPTS)
                    .document()
            }

            val toSave = attempt.copy(id = docRef.id)
            docRef.set(toSave, SetOptions.merge()).await()

            // Increment student progress stats
            updateStudentProgressStats(
                userId = attempt.userId,
                xpEarned = attempt.xpEarned,
                questionsAnsweredCount = attempt.totalQuestions
            )

            // If it's a topic test, update topic mastery
            if (!attempt.topicId.isNullOrBlank() && !attempt.subjectId.isNullOrBlank() && !attempt.chapterId.isNullOrBlank()) {
                saveTopicMastery(
                    FirestoreTopicMastery(
                        topicId = attempt.topicId,
                        userId = attempt.userId,
                        classId = attempt.classId,
                        subjectId = attempt.subjectId,
                        chapterId = attempt.chapterId,
                        masteryPercentage = attempt.accuracyPercentage,
                        totalQuestionsAttempted = attempt.totalQuestions,
                        correctQuestionsCount = attempt.correctCount,
                        isCompleted = attempt.isPassed,
                        lastPracticedTimestamp = System.currentTimeMillis()
                    )
                )
            }

            Log.d(TAG, "Successfully recorded test attempt: ${docRef.id} for user: ${attempt.userId}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error recording test attempt: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Gets student attempt history across all tests.
     */
    suspend fun getStudentAttemptHistory(userId: String, limit: Long = 20): Result<List<FirestoreTestAttempt>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TEST_ATTEMPTS)
                .orderBy("submittedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreTestAttempt::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting attempt history: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 8. TOPIC MASTERY & PROGRESS
    // =========================================================================

    suspend fun saveTopicMastery(mastery: FirestoreTopicMastery): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_USERS)
                .document(mastery.userId)
                .collection(COLLECTION_TOPIC_PROGRESS)
                .document(mastery.topicId)
                .set(mastery, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving topic mastery: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getTopicMastery(userId: String, topicId: String): Result<FirestoreTopicMastery?> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TOPIC_PROGRESS)
                .document(topicId)
                .get()
                .await()
            val item = if (doc.exists()) doc.toObject(FirestoreTopicMastery::class.java) else null
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting topic mastery: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 8.1 VIDEO PLAYBACK PROGRESS & MULTI-DEVICE SYNC
    // =========================================================================

    suspend fun saveVideoProgress(userId: String, progress: FirestoreVideoProgress): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank() || progress.videoId.isBlank()) {
                return@withContext Result.success(Unit)
            }
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_VIDEO_PROGRESS)
                .document(progress.videoId)
                .set(progress, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved video progress for ${progress.videoId} at ${progress.lastPositionMs}ms")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving video progress: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getVideoProgress(userId: String, videoId: String): Result<FirestoreVideoProgress?> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank() || videoId.isBlank()) {
                return@withContext Result.success(null)
            }
            val doc = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_VIDEO_PROGRESS)
                .document(videoId)
                .get()
                .await()
            val item = if (doc.exists()) doc.toObject(FirestoreVideoProgress::class.java) else null
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting video progress for $videoId: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllVideoProgress(userId: String): Result<List<FirestoreVideoProgress>> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank()) {
                return@withContext Result.success(emptyList())
            }
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_VIDEO_PROGRESS)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreVideoProgress::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all video progress: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 8.2 CHAPTER & SUBJECT REALTIME PROGRESS (মাল্টি-ডিভাইস সিঙ্ক ও ব্যাকআপ)
    // =========================================================================

    suspend fun saveChapterProgress(userId: String, progress: FirestoreChapterProgress): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank() || progress.chapterId.isBlank()) return@withContext Result.success(Unit)
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_CHAPTER_PROGRESS)
                .document(progress.chapterId)
                .set(progress, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved chapter progress for ${progress.chapterId}: completed=${progress.isCompleted}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving chapter progress: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllChapterProgress(userId: String): Result<List<FirestoreChapterProgress>> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank()) return@withContext Result.success(emptyList())
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_CHAPTER_PROGRESS)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreChapterProgress::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all chapter progress: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun saveSubjectProgress(userId: String, progress: FirestoreSubjectProgress): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank() || progress.subjectId.isBlank()) return@withContext Result.success(Unit)
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_SUBJECT_PROGRESS)
                .document(progress.subjectId)
                .set(progress, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved subject progress for ${progress.subjectId}: ${progress.completedChapters}/${progress.totalChapters}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving subject progress: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllSubjectProgress(userId: String): Result<List<FirestoreSubjectProgress>> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank()) return@withContext Result.success(emptyList())
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_SUBJECT_PROGRESS)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreSubjectProgress::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all subject progress: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 9. STUDENT PROFILE & STATS
    // =========================================================================

    suspend fun saveStudentProfile(userId: String, profile: StudentProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userMap = hashMapOf(
                "userId" to userId,
                "name" to profile.name,
                "school" to profile.school,
                "district" to profile.district,
                "targetExam" to profile.targetExam,
                "classId" to profile.classId,
                "classLevel" to profile.classLevel,
                "stream" to profile.stream,
                "profileImageUri" to profile.profileImageUri,
                "currentXp" to profile.currentXp,
                "level" to profile.level,
                "currentStreak" to profile.currentStreak,
                "bestStreak" to profile.bestStreak,
                "totalQuestionsAnswered" to profile.totalQuestionsAnswered,
                "overallAccuracy" to profile.overallAccuracy,
                "examReadinessPercentage" to profile.examReadinessPercentage,
                "rankState" to profile.rankState,
                "league" to profile.league,
                "coins" to profile.coins,
                "aiCredits" to profile.aiCredits,
                "isStreakRepaired" to profile.isStreakRepaired,
                "isPro" to profile.isPro,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(userMap, SetOptions.merge())
                .await()

            updateLeaderboardEntry(
                userId = userId,
                name = profile.name,
                school = profile.school,
                district = profile.district,
                xp = profile.currentXp,
                accuracy = profile.overallAccuracy,
                streak = profile.currentStreak
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving student profile: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getStudentProfile(userId: String): Result<Map<String, Any>?> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection(COLLECTION_USERS).document(userId).get().await()
            if (doc.exists()) {
                Result.success(doc.data)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching student profile: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun updateStudentProgressStats(
        userId: String,
        xpEarned: Int,
        questionsAnsweredCount: Int,
        newStreak: Int? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val updates = mutableMapOf<String, Any>(
                "currentXp" to FieldValue.increment(xpEarned.toLong()),
                "totalQuestionsAnswered" to FieldValue.increment(questionsAnsweredCount.toLong()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (newStreak != null) {
                updates["currentStreak"] = newStreak
            }

            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating progress stats: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun updateLeaderboardEntry(
        userId: String,
        name: String,
        school: String,
        district: String = "কলকাতা",
        xp: Int,
        accuracy: Int,
        streak: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entry = hashMapOf(
                "userId" to userId,
                "name" to name,
                "school" to school,
                "district" to district,
                "xp" to xp,
                "accuracy" to accuracy,
                "streak" to streak,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_LEADERBOARD)
                .document(userId)
                .set(entry, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating leaderboard entry: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    fun getRealtimeTopStudents(limit: Long = 20): Flow<List<LeaderboardStudent>> = callbackFlow {
        val db = firestoreInstance
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listenerRegistration = db.collection(COLLECTION_LEADERBOARD)
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Leaderboard snapshot error: ${error.localizedMessage}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val students = snapshot.documents.mapIndexed { index, doc ->
                        LeaderboardStudent(
                            rank = index + 1,
                            name = doc.getString("name") ?: "Student",
                            school = doc.getString("school") ?: "School",
                            district = doc.getString("district") ?: "পশ্চিমবঙ্গ",
                            xp = doc.getLong("xp")?.toInt() ?: 0,
                            accuracy = doc.getLong("accuracy")?.toInt() ?: 80,
                            streak = doc.getLong("streak")?.toInt() ?: 1,
                            mockScore = doc.getLong("mockScore")?.toInt() ?: 85,
                            mockTotal = 90,
                            mockPercentile = 98.0 - (index * 0.5),
                            bossVictories = doc.getLong("bossVictories")?.toInt() ?: 5,
                            badgesCount = doc.getLong("badgesCount")?.toInt() ?: 4,
                            badgeTitle = when {
                                index == 0 -> "Grand Master / রাজ্য চ্যাম্পিয়ন"
                                index < 3 -> "Topper / সেরা শিক্ষার্থী"
                                index < 10 -> "Gold Warrior / সোনার যোদ্ধা"
                                else -> "Scholar / মেধাবী ছাত্র"
                            }
                        )
                    }
                    trySend(students)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Backward-compatible bridge for saving AssessmentResult directly to Firestore.
     */
    suspend fun saveTestResult(userId: String, result: AssessmentResult): Result<String> = withContext(Dispatchers.IO) {
        val attempt = FirestoreTestAttempt(
            userId = userId,
            testId = "assessment_${System.currentTimeMillis()}",
            testTitle = result.title,
            totalQuestions = result.totalQuestions,
            correctCount = result.correctCount,
            incorrectCount = result.incorrectCount,
            scoreScored = result.totalScore,
            maxScore = result.maxScore,
            accuracyPercentage = result.accuracyPercentage,
            xpEarned = result.xpEarned,
            timeTakenSeconds = result.timeSpentSeconds,
            isBossBeaten = result.isBossBeaten,
            submittedAt = System.currentTimeMillis()
        )
        recordTestAttempt(attempt)
    }

    // =========================================================================
    // CHAPTER ASSESSMENTS, SUBJECT MASTERY QUIZZES & FULL SYLLABUS MOCKS
    // =========================================================================

    suspend fun saveChapterAssessment(assessment: FirestoreChapterAssessment): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docId = assessment.id.ifBlank { "assessment_${assessment.chapterId}_${System.currentTimeMillis()}" }
            val finalAssessment = assessment.copy(id = docId)
            firestore.collection(COLLECTION_CHAPTER_ASSESSMENTS)
                .document(docId)
                .set(finalAssessment, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving chapter assessment: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getChapterAssessments(classId: String, chapterId: String): Result<List<FirestoreChapterAssessment>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_CHAPTER_ASSESSMENTS)
                .whereEqualTo("classId", classId)
                .whereEqualTo("chapterId", chapterId)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreChapterAssessment::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chapter assessments: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun saveSubjectMasteryQuiz(quiz: FirestoreSubjectMasteryQuiz): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docId = quiz.id.ifBlank { "mastery_${quiz.subjectId}_${System.currentTimeMillis()}" }
            val finalQuiz = quiz.copy(id = docId)
            firestore.collection(COLLECTION_SUBJECT_MASTERY_QUIZZES)
                .document(docId)
                .set(finalQuiz, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving subject mastery quiz: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getSubjectMasteryQuizzes(classId: String, subjectId: String): Result<List<FirestoreSubjectMasteryQuiz>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_SUBJECT_MASTERY_QUIZZES)
                .whereEqualTo("classId", classId)
                .whereEqualTo("subjectId", subjectId)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreSubjectMasteryQuiz::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subject mastery quizzes: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun saveFullSyllabusMock(mock: FirestoreFullSyllabusMock): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docId = mock.id.ifBlank { "mock_${mock.classId}_${System.currentTimeMillis()}" }
            val finalMock = mock.copy(id = docId)
            firestore.collection(COLLECTION_FULL_SYLLABUS_MOCKS)
                .document(docId)
                .set(finalMock, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving full syllabus mock: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getFullSyllabusMocks(classId: String): Result<List<FirestoreFullSyllabusMock>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_FULL_SYLLABUS_MOCKS)
                .whereEqualTo("classId", classId)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreFullSyllabusMock::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting full syllabus mocks: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteQuestion(questionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_QUESTIONS).document(questionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting question $questionId: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTestTemplate(testId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_TESTS).document(testId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting test template $testId: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteChapter(chapterId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_CHAPTERS).document(chapterId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting chapter $chapterId: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTopicsForChapter(chapterId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val topics = firestore.collection(COLLECTION_TOPICS)
                .whereEqualTo("chapterId", chapterId)
                .get()
                .await()

            topics.documents.chunked(500).forEach { documents ->
                val batch = firestore.batch()
                documents.forEach { document ->
                    batch.delete(document.reference)
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting topics for chapter $chapterId: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTopic(topicId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_TOPICS).document(topicId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting topic $topicId: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllTestTemplates(limit: Long = 200): Result<List<FirestoreTestTemplate>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_TESTS)
                .limit(limit)
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreTestTemplate::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all test templates: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getTestTemplatesFiltered(
        classId: String? = null,
        subjectId: String? = null,
        limit: Long = 200
    ): Result<List<FirestoreTestTemplate>> = withContext(Dispatchers.IO) {
        try {
            var query: Query = firestore.collection(COLLECTION_TESTS)
            if (!classId.isNullOrBlank()) {
                query = query.whereEqualTo("classId", classId)
            }
            if (!subjectId.isNullOrBlank()) {
                query = query.whereEqualTo("subjectId", subjectId)
            }
            val snapshot = query.limit(limit).get().await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreTestTemplate::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting filtered test templates: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun savePodcast(podcast: FirestorePodcast): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val id = podcast.id.ifBlank { "podcast_${System.currentTimeMillis()}" }
            val finalPodcast = podcast.copy(id = id)
            firestore.collection(COLLECTION_PODCASTS)
                .document(id)
                .set(finalPodcast, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving custom podcast: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun savePodcastsBatch(podcasts: List<FirestorePodcast>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (podcasts.isEmpty()) return@withContext Result.success(Unit)
            val chunks = podcasts.chunked(100)
            for (chunk in chunks) {
                val batch = firestore.batch()
                for (p in chunk) {
                    val id = p.id.ifBlank { "podcast_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}" }
                    val finalPodcast = p.copy(id = id)
                    val docRef = firestore.collection(COLLECTION_PODCASTS).document(id)
                    batch.set(docRef, finalPodcast, SetOptions.merge())
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error in batch saving custom podcasts: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getCustomPodcasts(className: String? = null, boardId: String? = null): Result<List<FirestorePodcast>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_PODCASTS).get().await()
            val list = snapshot.documents.mapNotNull { it.toObject(FirestorePodcast::class.java) }
            val filtered = list.filter { item ->
                val matchesClass = if (!className.isNullOrBlank() && className != "ALL") {
                    item.className.equals(className, ignoreCase = true) ||
                    item.className.replace(" ", "").equals(className.replace(" ", ""), ignoreCase = true) ||
                    item.className == "ALL" ||
                    item.className.isBlank()
                } else true

                val matchesBoard = if (!boardId.isNullOrBlank() && boardId != "ALL") {
                    item.boardId.equals(boardId, ignoreCase = true) ||
                    item.boardId == "ALL" ||
                    item.boardId.isBlank()
                } else true

                matchesClass && matchesBoard
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting custom podcasts: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun deletePodcast(podcastId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_PODCASTS)
                .document(podcastId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting podcast: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}
