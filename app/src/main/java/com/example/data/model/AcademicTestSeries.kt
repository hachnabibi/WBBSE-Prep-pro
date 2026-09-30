package com.example.data.model

enum class TestType {
    UNIT_TEST,
    MOCK_TEST
}

data class AcademicTestItem(
    val id: String,
    val testNumber: Int,
    val type: TestType,
    val titleBengali: String = "",
    val titleEnglish: String = titleBengali,
    val descriptionBengali: String = "",
    val descriptionEnglish: String = descriptionBengali,
    val subjectCode: SubjectCode,
    val questionCount: Int = if (type == TestType.UNIT_TEST) 10 else 30,
    val timeLimitMinutes: Int = if (type == TestType.UNIT_TEST) 15 else 45,
    val xpReward: Int = if (type == TestType.UNIT_TEST) 150 else 300,
    val badgeLabel: String = if (type == TestType.UNIT_TEST) "ইউনিট টেস্ট $testNumber" else "মক টেস্ট $testNumber"
) {
    val title: String get() = if (titleBengali.isNotBlank()) titleBengali else titleEnglish
    val description: String get() = if (descriptionBengali.isNotBlank()) descriptionBengali else descriptionEnglish

    constructor(
        id: String,
        testNumber: Int,
        type: TestType,
        title: String,
        description: String,
        subjectCode: SubjectCode,
        questionCount: Int = if (type == TestType.UNIT_TEST) 10 else 30,
        timeLimitMinutes: Int = if (type == TestType.UNIT_TEST) 15 else 45,
        xpReward: Int = if (type == TestType.UNIT_TEST) 150 else 300,
        badgeLabel: String = if (type == TestType.UNIT_TEST) "ইউনিট টেস্ট $testNumber" else "মক টেস্ট $testNumber"
    ) : this(
        id = id,
        testNumber = testNumber,
        type = type,
        titleBengali = title,
        titleEnglish = title,
        descriptionBengali = description,
        descriptionEnglish = description,
        subjectCode = subjectCode,
        questionCount = questionCount,
        timeLimitMinutes = timeLimitMinutes,
        xpReward = xpReward,
        badgeLabel = badgeLabel
    )
}

// Backwards compatibility aliases
typealias Class10TestItem = AcademicTestItem
typealias Class10TestSeriesProvider = AcademicTestSeriesProvider

object AcademicTestSeriesProvider {

    /**
     * Generates structured academic test series with Unit Tests and Mock Tests.
     */
    val ALL_ACADEMIC_TESTS: List<AcademicTestItem> = buildList {
        val subjectsList = listOf(
            SubjectCode.PHYSICAL_SCIENCE,
            SubjectCode.LIFE_SCIENCE,
            SubjectCode.MATHEMATICS,
            SubjectCode.HISTORY,
            SubjectCode.GEOGRAPHY,
            SubjectCode.BENGALI,
            SubjectCode.ENGLISH
        )

        var testCounter = 1
        var unitCount = 0
        var mockCount = 0

        val unitTopicsMap = mapOf(
            SubjectCode.PHYSICAL_SCIENCE to listOf(
                "পরিবেশের জন্য ভাবনা ও গ্যাসের আচরণ",
                "রাসায়নিক গণনা ও তাপের ঘটনাগ্রুপ",
                "আলো ও গোলীয় দর্পণ",
                "চলতড়িৎ ও ওহমের সূত্র",
                "পর্যায় সারণি ও রাসায়নিক বন্ধন",
                "তড়িৎ বিশ্লেষণ ও অজৈব রসায়ন",
                "ধাতুবিদ্যা ও জৈব রসায়ন",
                "পরমাণুর নিউক্লিয়াস ও তেজস্ক্রিয়তা",
                "সম্পূর্ণ ভৌতবিজ্ঞান রিভিশন ১",
                "সম্পূর্ণ ভৌতবিজ্ঞান রিভিশন ২"
            ),
            SubjectCode.LIFE_SCIENCE to listOf(
                "জীবজগতে নিয়ন্ত্রণ ও সমন্বয় (হরমোন)",
                "স্নায়ুতন্ত্র ও উপযোজন",
                "জীবনের ধারাবাহিকতা ও কোষ বিভাজন",
                "জনন ও বংশগতি",
                "মেন্ডেলের সূত্র ও জিনগত রোগ",
                "অভিব্যক্তি ও অভিযোজন",
                "পরিবেশ, তার সম্পদ ও সংরক্ষণ",
                "জীববৈচিত্র্য ও পরিবেশ দূষণ",
                "সম্পূর্ণ জীবনবিজ্ঞান রিভিশন ১",
                "সম্পূর্ণ জীবনবিজ্ঞান রিভিশন ২"
            ),
            SubjectCode.MATHEMATICS to listOf(
                "একচল বিশিষ্ট দ্বিঘাত সমীকরণ",
                "সরল সুদকষা ও চক্রবৃদ্ধি সুদ",
                "বৃত্ত সম্পর্কিত উপপাদ্য ও কোণ",
                "অনুপাত ও সমানুপাত",
                "দ্বিঘাত করণী ও ভেদের অঙ্ক",
                "গোলক, চোঙ ও শঙ্কু গঠন",
                "ত্রিকোণমিতিক অনুপাত ও অভেদাবলী",
                "উচ্চতা ও দূরত্ব (Height & Distance)",
                "রাশিবিজ্ঞান (Mean, Median, Mode)",
                "সম্পূর্ণ গণিত রিভিশন"
            ),
            SubjectCode.HISTORY to listOf(
                "ইতিহাসের ধারণা ও সংস্কার আন্দোলন",
                "প্রতিরোধ ও বিদ্রোহ (সাঁওতাল ও নীল)",
                "সংঘবদ্ধতার গোড়ার কথা (১৮৫৭ মহাবিদ্রোহ)",
                "অভিব্যক্তি ও ছাপাখানার বিকাশ",
                "বিকল্প চিন্তা ও উদ্যোগ (বসু বিজ্ঞান মন্দির)",
                "বিশ শতকের ভারতে কৃষক ও শ্রমিক আন্দোলন",
                "ভারতে নারী, ছাত্র ও প্রান্তিক জনগোষ্ঠীর ভূমিকা",
                "উপনিবেশিক ভারত পুনর্গঠন ও দেশভাগ",
                "সম্পূর্ণ ইতিহাস রিভিশন ১",
                "সম্পূর্ণ ইতিহাস রিভিশন ২"
            ),
            SubjectCode.GEOGRAPHY to listOf(
                "বহির্জাত প্রক্রিয়া ও ভূমিরূপ গঠন (নদী, হিমবাহ, বায়ু)",
                "বায়ুমণ্ডল ও বায়ুর চাপবলয়",
                "বারিFrame / বারিমণ্ডল ও সমুদ্রস্রোত",
                "বর্জ্য ব্যবস্থাপনা ও পরিবেশ",
                "ভারতের প্রাকৃতিক পরিবেশ (ভূপ্রকৃতি ও নদনদী)",
                "ভারতের জলবায়ু, মৃত্তিকা ও স্বাভাবিক উদ্ভিদ",
                "ভারতের কৃষি ও শিল্প ব্যবস্থা",
                "ভারতের জনসংখ্যা ও পরিবহন ব্যবস্থা",
                "উপগ্রহ চিত্র ও ভূবৈচিত্র্যসূচক মানচিত্র",
                "সম্পূর্ণ ভূগোল রিভিশন"
            ),
            SubjectCode.BENGALI to listOf(
                "জ্ঞানচক্ষু ও অসুখী একজন",
                "বহুরূপী ও আয় আরো বেঁধে বেঁধে থাকি",
                "পথের দাবী ও প্রলয়োল্লাস",
                "অস্ত্রের বিরুদ্ধে গান ও হারিয়ে যাওয়া কালি কলম",
                "সিন্ধুতীরে ও অদল বদল",
                "অস্ত্রের বিরুদ্ধে গান ও কণি (১-৫ অধ্যায়)",
                "কণি (৬-১৪ অধ্যায়)",
                "বাংলা ব্যাকরণ: কারক ও সমাস",
                "বাংলা ব্যাকরণ: বাক্য ও বাচ্য",
                "সম্পূর্ণ বাংলা ভাষা ও সাহিত্য রিভিশন"
            ),
            SubjectCode.ENGLISH to listOf(
                "Father's Help & Fable",
                "The Passing Away of Bapu & Sea Fever",
                "Our Runaway Kite & Cat",
                "The Snail & Grammar unseen",
                "English Grammar: Tense & Voice Change",
                "English Grammar: Narration & Articles",
                "Unseen Reading Comprehension Passages",
                "Writing Skills: Notice & Letter",
                "Writing Skills: Report & Processing",
                "Full English Paper Revision"
            )
        )

        for (i in 1..30) {
            val isMock = (i % 6 == 0) // Every 6th test is a Grand Mock Test after 5 Unit Tests
            val subject = subjectsList[(i - 1) % subjectsList.size]

            if (!isMock) {
                unitCount++
                val topicsList = unitTopicsMap[subject] ?: emptyList()
                val topicName = topicsList.getOrElse((unitCount - 1) % topicsList.size) { "গুরুত্বপূর্ণ অধ্যায় অনুশীলনী" }

                add(
                    AcademicTestItem(
                        id = "test_series_unit_$i",
                        testNumber = i,
                        type = TestType.UNIT_TEST,
                        titleBengali = "ইউনিট টেস্ট $unitCount: ${subject.displayName} ($topicName)",
                        titleEnglish = "Unit Test $unitCount: ${subject.nameEnglish} ($topicName)",
                        descriptionBengali = "অধ্যায়ভিত্তিক ১০টি গুরুত্বপূর্ণ MCQ ও সংক্ষিপ্ত প্রশ্নাবলী। সময়: ১৫ মিনিট।",
                        subjectCode = subject,
                        questionCount = 10,
                        timeLimitMinutes = 15,
                        xpReward = 150,
                        badgeLabel = "Unit Test #$unitCount"
                    )
                )
            } else {
                mockCount++
                add(
                    AcademicTestItem(
                        id = "test_series_mock_$i",
                        testNumber = i,
                        type = TestType.MOCK_TEST,
                        titleBengali = "🏆 মক টেস্ট $mockCount: ${subject.displayName} (পূর্ববর্তী ৫টি ইউনিট সমন্বিত)",
                        titleEnglish = "🏆 Mock Test $mockCount: ${subject.nameEnglish} (5-Units Cumulative Mock)",
                        descriptionBengali = "বোর্ড প্যাটার্নে ২৫টি বাছাই করা প্রশ্নপত্র। সময়: ৪৫ মিনিট। র্যাঙ্ক ও মূল্যায়ন।",
                        subjectCode = subject,
                        questionCount = 25,
                        timeLimitMinutes = 45,
                        xpReward = 300,
                        badgeLabel = "Mock Exam #$mockCount"
                    )
                )
            }
        }
    }

    val ALL_CLASS10_TESTS: List<AcademicTestItem> get() = ALL_ACADEMIC_TESTS

    fun getTestsForSubject(subjectCode: SubjectCode): List<AcademicTestItem> {
        return ALL_ACADEMIC_TESTS.filter { it.subjectCode == subjectCode }
    }

    fun getTestsForClass(classId: String): List<AcademicTestItem> {
        return ALL_ACADEMIC_TESTS
    }

    fun getTestsForClassAndSubject(classId: String, subjectCode: SubjectCode): List<AcademicTestItem> {
        val filtered = ALL_ACADEMIC_TESTS.filter { it.subjectCode == subjectCode }
        if (filtered.isNotEmpty()) return filtered
        return listOf(
            AcademicTestItem(
                id = "${classId}_${subjectCode.id}_unit_1",
                testNumber = 1,
                type = TestType.UNIT_TEST,
                titleBengali = "ইউনিট টেস্ট ১: ${subjectCode.subjectNameBengali}",
                titleEnglish = "Unit Test 1: ${subjectCode.subjectNameEnglish}",
                descriptionBengali = "অধ্যায়ভিত্তিক প্রথম সাময়িক মূল্যায়ন ও বাছাই করা প্রশ্নপত্র।",
                subjectCode = subjectCode,
                questionCount = 10,
                timeLimitMinutes = 15,
                xpReward = 100,
                badgeLabel = "ইউনিট ১"
            ),
            AcademicTestItem(
                id = "${classId}_${subjectCode.id}_mock_1",
                testNumber = 2,
                type = TestType.MOCK_TEST,
                titleBengali = "🏆 পূর্ণাঙ্গ মডেল মক টেস্ট: ${subjectCode.subjectNameBengali}",
                titleEnglish = "🏆 Full Model Mock Test: ${subjectCode.subjectNameEnglish}",
                descriptionBengali = "সম্পূর্ণ সিলেবাস ভিত্তিক পূর্ণাঙ্গ পরীক্ষা ও মেধা যাচাই।",
                subjectCode = subjectCode,
                questionCount = 20,
                timeLimitMinutes = 30,
                xpReward = 250,
                badgeLabel = "মডেল মক"
            )
        )
    }
}
