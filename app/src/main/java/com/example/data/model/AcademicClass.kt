package com.example.data.model

import com.example.data.local.SubjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

data class EducationBoard(
    val id: String,
    val shortName: String,
    val fullNameBengali: String,
    val fullNameEnglish: String = fullNameBengali,
    val subtitleBengali: String = "",
    val levelBadge: String = "",
    val classRangeDescription: String = "",
    val iconName: String = "MenuBook"
)

object AcademicClassProvider {
    val DEFAULT_BOARDS: List<EducationBoard> = listOf(
        EducationBoard(
            id = "WBBPE",
            shortName = "WBBPE",
            fullNameBengali = "পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ",
            fullNameEnglish = "West Bengal Board of Primary Education",
            subtitleBengali = "প্রাথমিক শিক্ষা পর্ষদ",
            levelBadge = "প্রাথমিক স্তর",
            classRangeDescription = "প্রাক-প্রাথমিক থেকে ৪র্থ শ্রেণি",
            iconName = "ChildCare"
        ),
        EducationBoard(
            id = "WBBSE",
            shortName = "WBBSE",
            fullNameBengali = "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ",
            fullNameEnglish = "West Bengal Board of Secondary Education",
            subtitleBengali = "মাধ্যমিক শিক্ষা পর্ষদ",
            levelBadge = "মাধ্যমিক স্তর",
            classRangeDescription = "৫ম থেকে ১০ম শ্রেণি",
            iconName = "MenuBook"
        ),
        EducationBoard(
            id = "WBCHSE",
            shortName = "WBCHSE",
            fullNameBengali = "পশ্চিমবঙ্গ উচ্চমাধ্যমিক শিক্ষা সংসদ",
            fullNameEnglish = "West Bengal Council of Higher Secondary Education",
            subtitleBengali = "উচ্চ মাধ্যমিক শিক্ষা সংসদ",
            levelBadge = "উচ্চ মাধ্যমিক স্তর",
            classRangeDescription = "একাদশ ও দ্বাদশ শ্রেণি",
            iconName = "School"
        )
    )

    private val _dynamicBoards = MutableStateFlow<List<EducationBoard>>(DEFAULT_BOARDS)
    val ALL_BOARDS_FLOW: StateFlow<List<EducationBoard>> = _dynamicBoards.asStateFlow()
    val ALL_BOARDS: List<EducationBoard> get() = _dynamicBoards.value

    val DEFAULT_CLASSES: List<AcademicClass> = listOf(
        // === WBBPE (পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ: KG ও ১ম - ৪র্থ শ্রেণি) ===
        AcademicClass(
            id = "class_kg_wbbpe",
            classNumber = 0,
            nameBengali = "প্রাক-প্রাথমিক (KG)",
            nameEnglish = "Pre-Primary (KG)",
            board = "WBBPE",
            targetExam = "WBBPE Pre-Primary / KG",
            descriptionBengali = "পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ প্রাক-প্রাথমিক পাঠ্যক্রম (কুটুম কাটাম, মজারু)"
        ),
        AcademicClass(
            id = "class_1_wbbpe",
            classNumber = 1,
            nameBengali = "প্রথম শ্রেণি",
            nameEnglish = "Class 1",
            board = "WBBPE",
            targetExam = "WBBPE Class 1 Exam",
            descriptionBengali = "পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ প্রথম শ্রেণির সিলেবাস (আমার বই, সহজ পাঠ)"
        ),
        AcademicClass(
            id = "class_2_wbbpe",
            classNumber = 2,
            nameBengali = "দ্বিতীয় শ্রেণি",
            nameEnglish = "Class 2",
            board = "WBBPE",
            targetExam = "WBBPE Class 2 Exam ",
            descriptionBengali = "পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ দ্বিতীয় শ্রেণির সিলেবাস (আমার বই, সহজ পাঠ)"
        ),
        AcademicClass(
            id = "class_3_wbbpe",
            classNumber = 3,
            nameBengali = "তৃতীয় শ্রেণি",
            nameEnglish = "Class 3",
            board = "WBBPE",
            targetExam = "WBBPE Class 3 Exam",
            descriptionBengali = "পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ তৃতীয় শ্রেণির সিলেবাস (পাতাবাহার, বাটারফ্লাই, গণিত, আমাদের পরিবেশ)"
        ),
        AcademicClass(
            id = "class_4_wbbpe",
            classNumber = 4,
            nameBengali = "চতুর্থ শ্রেণি",
            nameEnglish = "Class 4",
            board = "WBBPE",
            targetExam = "WBBPE Class 4 Exam",
            descriptionBengali = "পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ চতুর্থ শ্রেণির সিলেবাস (পাতাবাহার, বাটারফ্লাই, গণিত, আমাদের পরিবেশ)"
        ),

        // === WBBSE (পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ: ৫ম - ১০ম শ্রেণি) ===
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
            nameBengali = "দশম শ্রেণি",
            nameEnglish = "Class 10",
            board = "WBBSE",
            targetExam = "WBBSE Madhyamik 2026",
            descriptionBengali = "পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ দশম শ্রেণির সম্পূর্ণ সিলেবাস ও মাধ্যমিক পরীক্ষার প্রস্তুতি"
        ),
        AcademicClass(
            id = "class_11_wbchse",
            classNumber = 11,
            nameBengali = "একাদশ শ্রেণি",
            nameEnglish = "Class 11",
            board = "WBCHSE",
            targetExam = "WBCHSE Class 11 Exam 2026",
            descriptionBengali = "পশ্চিমবঙ্গ উচ্চমাধ্যমিক শিক্ষা সংসদ একাদশ শ্রেণির সিলেবাস ও প্রস্তুতি"
        ),
        AcademicClass(
            id = "class_12_wbchse",
            classNumber = 12,
            nameBengali = "দ্বাদশ শ্রেণি",
            nameEnglish = "Class 12",
            board = "WBCHSE",
            targetExam = "WBCHSE Higher Secondary 2026",
            descriptionBengali = "পশ্চিমবঙ্গ উচ্চমাধ্যমিক শিক্ষা সংসদ দ্বাদশ শ্রেণির উচ্চ মাধ্যমিক পরীক্ষার প্রস্তুতি"
        )
    )

    private val _dynamicClasses = MutableStateFlow<List<AcademicClass>>(DEFAULT_CLASSES)
    val ALL_CLASSES_FLOW: StateFlow<List<AcademicClass>> = _dynamicClasses.asStateFlow()
    val ALL_CLASSES: List<AcademicClass> get() = _dynamicClasses.value

    fun updateBoards(boards: List<EducationBoard>) {
        if (boards.isNotEmpty()) {
            val merged = (boards + DEFAULT_BOARDS).distinctBy { it.id.uppercase() }
            _dynamicBoards.value = merged
        }
    }

    fun updateClasses(classes: List<AcademicClass>) {
        if (classes.isNotEmpty()) {
            val merged = (classes + DEFAULT_CLASSES).distinctBy { it.id.lowercase() }
            _dynamicClasses.value = merged
        }
    }

    fun addBoard(board: EducationBoard) {
        val current = _dynamicBoards.value.toMutableList()
        val index = current.indexOfFirst { it.id.equals(board.id, ignoreCase = true) }
        if (index >= 0) {
            current[index] = board
        } else {
            current.add(board)
        }
        _dynamicBoards.value = current
    }

    fun addClass(academicClass: AcademicClass) {
        val current = _dynamicClasses.value.toMutableList()
        val index = current.indexOfFirst { it.id.equals(academicClass.id, ignoreCase = true) }
        if (index >= 0) {
            current[index] = academicClass
        } else {
            current.add(academicClass)
        }
        _dynamicClasses.value = current
    }

    fun deleteBoard(boardId: String) {
        val current = _dynamicBoards.value.filterNot { it.id.equals(boardId, ignoreCase = true) }
        _dynamicBoards.value = current
        val remainingClasses = _dynamicClasses.value.filterNot { it.board.equals(boardId, ignoreCase = true) }
        _dynamicClasses.value = remainingClasses
    }

    fun deleteClass(classId: String) {
        val current = _dynamicClasses.value.filterNot { it.id.equals(classId, ignoreCase = true) }
        _dynamicClasses.value = current
    }

    fun getBoardById(boardId: String): EducationBoard? {
        return ALL_BOARDS.find { it.id.equals(boardId, ignoreCase = true) }
    }

    fun getClassById(classId: String): AcademicClass? {
        val clean = classId.lowercase()
        return ALL_CLASSES.find { it.id == clean || clean.contains(it.id) }
            ?: ALL_CLASSES.find { clean == "class_${it.classNumber}" }
    }

    fun getClassName(classId: String, isBengali: Boolean): String {
        val clean = classId.lowercase()
        val found = ALL_CLASSES.find { it.id == clean || clean.contains(it.id) }
            ?: ALL_CLASSES.find { clean == "class_${it.classNumber}" }
            ?: ALL_CLASSES.find { it.classNumber > 0 && clean.contains("class_${it.classNumber}_") }
        return if (found != null) {
            if (isBengali) found.nameBengali else found.nameEnglish
        } else {
            if (isBengali) "দশম শ্রেণি" else "Class 10"
        }
    }

    fun getBengaliClassShortBadge(classNumber: Int): String {
        return when (classNumber) {
            0 -> "প্রাক-প্রাথমিক (KG)"
            1 -> "১ম শ্রেণি"
            2 -> "২য় শ্রেণি"
            3 -> "৩য় শ্রেণি"
            4 -> "৪র্থ শ্রেণি"
            5 -> "৫ম শ্রেণি"
            6 -> "৬ষ্ঠ শ্রেণি"
            7 -> "৭ম শ্রেণি"
            8 -> "৮ম শ্রেণি"
            9 -> "৯ম শ্রেণি"
            10 -> "১০ম শ্রেণি"
            11 -> "একাদশ শ্রেণি"
            12 -> "দ্বাদশ শ্রেণি"
            else -> "${classNumber}ম শ্রেণি"
        }
    }
}

