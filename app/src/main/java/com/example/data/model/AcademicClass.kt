package com.example.data.model

import com.example.data.local.SubjectEntity

data class AcademicClass(
    val id: String,
    val classNumber: Int,
    val nameBengali: String = "",
    val nameEnglish: String = nameBengali,
    val board: String = "WBBSE",
    val targetExam: String = "",
    val descriptionBengali: String = "",
    val descriptionEnglish: String = descriptionBengali
) {
    val name: String get() = if (nameBengali.isNotBlank()) nameBengali else nameEnglish
    val description: String get() = if (descriptionBengali.isNotBlank()) descriptionBengali else descriptionEnglish

    constructor(
        id: String,
        classNumber: Int,
        name: String,
        board: String,
        targetExam: String,
        description: String = ""
    ) : this(
        id = id,
        classNumber = classNumber,
        nameBengali = name,
        nameEnglish = name,
        board = board,
        targetExam = targetExam,
        descriptionBengali = description,
        descriptionEnglish = description
    )
}

data class AcademicStream(
    val id: String,
    val nameBengali: String = "",
    val nameEnglish: String = nameBengali,
    val iconName: String = "School"
) {
    val name: String get() = if (nameBengali.isNotBlank()) nameBengali else nameEnglish

    constructor(
        id: String,
        name: String,
        iconName: String = "School"
    ) : this(
        id = id,
        nameBengali = name,
        nameEnglish = name,
        iconName = iconName
    )
}

object AcademicStreams {
    val ALL = AcademicStream("all", "সকল বিষয় (All Streams)", "All Streams", "Apps")
    val SCIENCE = AcademicStream("science", "বিজ্ঞান শাখা (Science)", "Science Stream", "Science")
    val ARTS = AcademicStream("arts", "কলা বিভাগ (Arts / Humanities)", "Arts Stream", "AutoStories")
    val COMMERCE = AcademicStream("commerce", "বাণিজ্য শাখা (Commerce)", "Commerce Stream", "AccountBalanceWallet")

    val LIST = listOf(ALL, SCIENCE, ARTS, COMMERCE)
    val HS_STREAMS = listOf(SCIENCE, ARTS, COMMERCE, ALL)
}

object AcademicClassProvider {
    val ALL_CLASSES: List<AcademicClass> = listOf(
        AcademicClass(
            id = "class_5_wbbse",
            classNumber = 5,
            nameBengali = "পঞ্চম শ্রেণি",
            nameEnglish = "Class 5 (Primary)",
            board = "WBBSE",
            targetExam = "WBBSE Class 5 Exam",
            descriptionBengali = "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ পঞ্চম শ্রেণির সিলেবাস ও পরীক্ষা"
        ),
        AcademicClass(
            id = "class_6_wbbse",
            classNumber = 6,
            nameBengali = "ষষ্ঠ শ্রেণি",
            nameEnglish = "Class 6",
            board = "WBBSE",
            targetExam = "WBBSE Class 6 Exam",
            descriptionBengali = "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ ষষ্ঠ শ্রেণির সিলেবাস ও পরীক্ষা"
        ),
        AcademicClass(
            id = "class_7_wbbse",
            classNumber = 7,
            nameBengali = "সপ্তম শ্রেণি",
            nameEnglish = "Class 7",
            board = "WBBSE",
            targetExam = "WBBSE Class 7 Exam",
            descriptionBengali = "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ সপ্তম শ্রেণির সিলেবাস ও পরীক্ষা"
        ),
        AcademicClass(
            id = "class_8_wbbse",
            classNumber = 8,
            nameBengali = "অষ্টম শ্রেণি",
            nameEnglish = "Class 8",
            board = "WBBSE",
            targetExam = "WBBSE Class 8 Exam",
            descriptionBengali = "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ অষ্টম শ্রেণির সিলেবাস ও পরীক্ষা"
        ),
        AcademicClass(
            id = "class_9_wbbse",
            classNumber = 9,
            nameBengali = "নবম শ্রেণি",
            nameEnglish = "Class 9",
            board = "WBBSE",
            targetExam = "WBBSE Class 9 Exam",
            descriptionBengali = "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ নবম শ্রেণির সিলেবাস ও পরীক্ষা"
        ),
        AcademicClass(
            id = "class_10_wbbse",
            classNumber = 10,
            nameBengali = "দশম শ্রেণি (মাধ্যমিক)",
            nameEnglish = "Class 10 (Madhyamik WBBSE)",
            board = "WBBSE",
            targetExam = "WBBSE Madhyamik 2026",
            descriptionBengali = "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ দশম শ্রেণির সম্পূর্ণ সিলেবাস ও মাধ্যমিক পরীক্ষার প্রস্তুতি"
        ),
        AcademicClass(
            id = "class_11_wbchse",
            classNumber = 11,
            nameBengali = "একাদশ শ্রেণি (উচ্চ মাধ্যমিক)",
            nameEnglish = "Class 11 (Higher Secondary WBCHSE)",
            board = "WBCHSE",
            targetExam = "WBCHSE Class 11 Exam 2026",
            descriptionBengali = "পশ্চিমবঙ্গ উচ্চমাধ্যমিক শিক্ষা সংসদ একাদশ শ্রেণির সিলেবাস ও প্রস্তুতি"
        ),
        AcademicClass(
            id = "class_12_wbchse",
            classNumber = 12,
            nameBengali = "দ্বাদশ শ্রেণি (উচ্চ মাধ্যমিক)",
            nameEnglish = "Class 12 (Higher Secondary WBCHSE)",
            board = "WBCHSE",
            targetExam = "WBCHSE Higher Secondary 2026",
            descriptionBengali = "পশ্চিমবঙ্গ উচ্চমাধ্যমিক শিক্ষা সংসদ দ্বাদশ শ্রেণির উচ্চ মাধ্যমিক পরীক্ষার প্রস্তুতি"
        )
    )

    fun getClassName(classId: String, isBengali: Boolean): String {
        val clean = classId.lowercase()
        val found = ALL_CLASSES.find { clean.contains(it.id) || clean.contains("class_${it.classNumber}") }
        return if (found != null) {
            if (isBengali) found.nameBengali else found.nameEnglish
        } else {
            if (isBengali) "দশম শ্রেণি" else "Class 10"
        }
    }
}

