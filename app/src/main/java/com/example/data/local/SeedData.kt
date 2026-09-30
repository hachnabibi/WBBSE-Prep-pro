package com.example.data.local

object SeedData {



    fun getInitialQuests(): List<DailyQuestEntity> = listOf(
        DailyQuestEntity(
            id = "c10_quest_daily_10",
            title = "ডেইলি ১০ স্পিড চ্যালেঞ্জ সম্পন্ন করো",
            currentCount = 0,
            targetCount = 1,
            xpReward = 150,
            isCompleted = false
        ),
        DailyQuestEntity(
            id = "c10_quest_solve_15",
            title = "যে কোনো অধ্যায় থেকে ১৫টি প্রশ্ন সঠিক সমাধান করো",
            currentCount = 0,
            targetCount = 15,
            xpReward = 200,
            isCompleted = false
        ),
        DailyQuestEntity(
            id = "c10_quest_weak_area",
            title = "দুর্বল অধ্যায় রিভিশন প্র্যাকটিস করো",
            currentCount = 0,
            targetCount = 1,
            xpReward = 120,
            isCompleted = false
        ),
        DailyQuestEntity(
            id = "c10_quest_boss_attempt",
            title = "অধ্যায়ের বস ব্যাটল আনলক বা আক্রমণ করো",
            currentCount = 0,
            targetCount = 1,
            xpReward = 250,
            isCompleted = false
        )
    )
}
