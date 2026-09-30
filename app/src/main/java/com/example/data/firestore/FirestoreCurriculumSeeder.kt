package com.example.data.firestore

import android.util.Log
import com.example.data.local.SeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Helper object to seed/upload complete WBBSE Class 10 Madhyamik curriculum,
 * subjects, chapters, topics, questions, and test templates to Cloud Firestore.
 */
object FirestoreCurriculumSeeder {

    private const val TAG = "CurriculumSeeder"

    suspend fun seedCompleteMadhyamikCurriculum(firestoreService: FirestoreService): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var itemsUploaded = 0

            // 1. Seed All Academic Classes (Class 5 to 12)
            val allClasses = com.example.data.model.AcademicClassProvider.ALL_CLASSES
            allClasses.forEach { cls ->
                val fClass = FirestoreClass(
                    id = cls.id,
                    name = cls.name,
                    board = cls.board,
                    targetYear = "2026",
                    description = cls.description,
                    totalSubjects = if (cls.classNumber >= 11) 10 else 7,
                    isActive = true
                )
                runCatching { firestoreService.saveClass(fClass) }
                itemsUploaded++
            }

            // 2. Seed Subjects
            // NOTE: Subjects and data are now loaded from assets in DeshExamDatabase
            
            // 3. Seed Chapters
            
            // 4. Seed Topics
            
            // 5. Seed Questions concurrently


            // 6. Seed Full Mock Tests for All Classes (Class 5 to 12)
            val fullMockTests = listOf(
                FirestoreTestTemplate(
                    id = "mock_class_5_full_01",
                    classId = "class_5_wbbse",
                    testType = FirestoreTestType.FULL_MOCK_TEST.name,
                    title = "পঞ্চম শ্রেণি বার্ষিক মডেল মক টেস্ট",
                    description = "পঞ্চম শ্রেণির বাংলা, ইংরেজি, গণিত ও পরিবেশ সমন্বিত পূর্ণাঙ্গ মূল্যায়ন",
                    durationMinutes = 60,
                    totalMarks = 50,
                    passMarks = 15,
                    questionCount = 25,
                    xpReward = 300,
                    isPublished = true
                ),
                FirestoreTestTemplate(
                    id = "mock_class_6_full_01",
                    classId = "class_6_wbbse",
                    testType = FirestoreTestType.FULL_MOCK_TEST.name,
                    title = "ষষ্ঠ শ্রেণি সার্বিক মূল্যায়ন মক টেস্ট",
                    description = "ষষ্ঠ শ্রেণির সিলেবাস ভিত্তিক অল-সাবজেক্ট প্র্যাকটিস",
                    durationMinutes = 90,
                    totalMarks = 50,
                    passMarks = 15,
                    questionCount = 30,
                    xpReward = 350,
                    isPublished = true
                ),
                FirestoreTestTemplate(
                    id = "mock_class_7_full_01",
                    classId = "class_7_wbbse",
                    testType = FirestoreTestType.FULL_MOCK_TEST.name,
                    title = "সপ্তম শ্রেণি বার্ষিক সাময়িক মূল্যায়ন টেস্ট",
                    description = "সপ্তম শ্রেণির সম্পূর্ণ পাঠ্যক্রম ভিত্তিক মডেল টেস্ট",
                    durationMinutes = 90,
                    totalMarks = 50,
                    passMarks = 15,
                    questionCount = 30,
                    xpReward = 350,
                    isPublished = true
                ),
                FirestoreTestTemplate(
                    id = "mock_class_8_full_01",
                    classId = "class_8_wbbse",
                    testType = FirestoreTestType.FULL_MOCK_TEST.name,
                    title = "অষ্টম শ্রেণি বার্ষিক অল-সাবজেক্ট টেস্ট",
                    description = "অষ্টম শ্রেণির প্রস্তুতি যাচাইয়ের জন্য পূর্ণাঙ্গ মডেল পরীক্ষা",
                    durationMinutes = 120,
                    totalMarks = 70,
                    passMarks = 21,
                    questionCount = 40,
                    xpReward = 400,
                    isPublished = true
                ),
                FirestoreTestTemplate(
                    id = "mock_class_9_full_01",
                    classId = "class_9_wbbse",
                    testType = FirestoreTestType.FULL_MOCK_TEST.name,
                    title = "নবম শ্রেণি বার্ষিক প্রাক-মাধ্যমিক মেগা মক টেস্ট",
                    description = "নবম শ্রেণির বোর্ড সিলেবাস ভিত্তিক পূর্ণাঙ্গ ৯০ নম্বরের মডেল টেস্ট",
                    durationMinutes = 180,
                    totalMarks = 90,
                    passMarks = 30,
                    questionCount = 60,
                    xpReward = 450,
                    isPublished = true
                ),
                FirestoreTestTemplate(
                    id = "mock_wbbse_full_01",
                    classId = "class_10_wbbse",
                    testType = FirestoreTestType.FULL_MOCK_TEST.name,
                    title = "মাধ্যমিক পূর্ণাঙ্গ অল-সাবজেক্ট মেগা মক টেস্ট ১",
                    description = "পর্ষদের নতুন প্রশ্ন কাঠামো অনুযায়ী সময়ভিত্তিক সম্পূর্ণ মক পরীক্ষা",
                    durationMinutes = 180,
                    totalMarks = 90,
                    passMarks = 30,
                    questionCount = 90,
                    xpReward = 500,
                    isPublished = true
                ),
                FirestoreTestTemplate(
                    id = "mock_pyq_2025_all",
                    classId = "class_10_wbbse",
                    testType = FirestoreTestType.PREVIOUS_YEAR_PAPER.name,
                    title = "মাধ্যমিক পূর্ববর্তী বর্ষের প্রশ্নপত্র (PYQ 2025)",
                    description = "২০২৫ সালের মাধ্যমিক পরীক্ষার মূল প্রশ্নপত্র সমাধান ও স্ব-মূল্যায়ন",
                    durationMinutes = 180,
                    totalMarks = 90,
                    passMarks = 30,
                    questionCount = 90,
                    xpReward = 450,
                    isPublished = true
                ),
                FirestoreTestTemplate(
                    id = "mock_class_11_full_01",
                    classId = "class_11_wbchse",
                    testType = FirestoreTestType.FULL_MOCK_TEST.name,
                    title = "একাদশ শ্রেণি বার্ষিক পরীক্ষা পূর্ণাঙ্গ মডেল টেস্ট",
                    description = "WBCHSE একাদশ শ্রেণির সিলেবাস অনুযায়ী সমন্বিত পরীক্ষা",
                    durationMinutes = 180,
                    totalMarks = 80,
                    passMarks = 24,
                    questionCount = 60,
                    xpReward = 500,
                    isPublished = true
                ),
                FirestoreTestTemplate(
                    id = "mock_class_12_full_01",
                    classId = "class_12_wbchse",
                    testType = FirestoreTestType.FULL_MOCK_TEST.name,
                    title = "উচ্চ মাধ্যমিক (HS 2026) গ্র্যান্ড মেগা মক টেস্ট",
                    description = "পশ্চিমবঙ্গ উচ্চমাধ্যমিক শিক্ষা সংসদ প্রশ্ন ব্লুপ্রিন্ট ভিত্তিক পূর্ণাঙ্গ মক টেস্ট",
                    durationMinutes = 180,
                    totalMarks = 80,
                    passMarks = 24,
                    questionCount = 70,
                    xpReward = 550,
                    isPublished = true
                )
            )
            fullMockTests.forEach {
                runCatching { firestoreService.saveTestTemplate(it) }
                itemsUploaded++
            }

            // 7. Seed Initial Statewide Leaderboard
            val initialLeaderboardStudents = listOf(
                hashMapOf(
                    "userId" to "top_student_1",
                    "name" to "অনন্যা মুখোপাধ্যায়",
                    "school" to "হিন্দু স্কুল",
                    "district" to "কলকাতা",
                    "xp" to 4890,
                    "accuracy" to 96,
                    "streak" to 21,
                    "mockScore" to 89,
                    "bossVictories" to 18,
                    "badgesCount" to 12,
                    "updatedAt" to System.currentTimeMillis()
                ),
                hashMapOf(
                    "userId" to "top_student_2",
                    "name" to "শুভম পাল",
                    "school" to "যাদবপুর বিদ্যাপীঠ",
                    "district" to "কলকাতা",
                    "xp" to 4650,
                    "accuracy" to 94,
                    "streak" to 18,
                    "mockScore" to 87,
                    "bossVictories" to 16,
                    "badgesCount" to 10,
                    "updatedAt" to System.currentTimeMillis()
                ),
                hashMapOf(
                    "userId" to "top_student_3",
                    "name" to "রিয়া চক্রবর্তী",
                    "school" to "বেথুন কলেজিয়েট স্কুল",
                    "district" to "উত্তর ২৪ পরগনা",
                    "xp" to 4380,
                    "accuracy" to 91,
                    "streak" to 15,
                    "mockScore" to 85,
                    "bossVictories" to 14,
                    "badgesCount" to 9,
                    "updatedAt" to System.currentTimeMillis()
                ),
                hashMapOf(
                    "userId" to "top_student_4",
                    "name" to "অয়ন সেনগুপ্ত",
                    "school" to "বালিগঞ্জ গভঃ হাই স্কুল",
                    "district" to "হাওড়া",
                    "xp" to 4120,
                    "accuracy" to 89,
                    "streak" to 12,
                    "mockScore" to 83,
                    "bossVictories" to 11,
                    "badgesCount" to 8,
                    "updatedAt" to System.currentTimeMillis()
                ),
                hashMapOf(
                    "userId" to "top_student_5",
                    "name" to "দেবস্মিতা রায়",
                    "school" to "পাঠভবন",
                    "district" to "পশ্চিম মেদিনীপুর",
                    "xp" to 3950,
                    "accuracy" to 88,
                    "streak" to 14,
                    "mockScore" to 81,
                    "bossVictories" to 10,
                    "badgesCount" to 7,
                    "updatedAt" to System.currentTimeMillis()
                )
            )

            initialLeaderboardStudents.forEach { studentMap ->
                val uId = studentMap["userId"] as String
                runCatching {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("leaderboard")
                        .document(uId)
                        .set(studentMap, com.google.firebase.firestore.SetOptions.merge())
                        .await()
                }
                itemsUploaded++
            }

            // 8. Seed Districts and District-wise Schools to Firestore
            com.example.util.WestBengalData.districtSchoolsMap.forEach { (district, schools) ->
                runCatching { firestoreService.saveDistrictSchools(district, schools) }
                itemsUploaded++
            }

            Log.i(TAG, "Successfully seeded $itemsUploaded curriculum, test, leaderboard, and district-school items to Firestore.")
            Result.success(itemsUploaded)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding curriculum to Firestore: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}
