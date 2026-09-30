package com.example.data.repository

data class PyqPdfItem(
    val id: String,
    val year: Int,
    val subjectCode: String,
    val titleBengali: String,
    val titleEnglish: String,
    val subjectBengali: String,
    val subjectEnglish: String,
    val fileSize: String,
    val totalMarks: Int = 90,
    val timeLimit: String = "৩ ঘণ্টা ১৫ মিনিট",
    val isOfficial: Boolean = true,
    val downloadUrl: String? = null, // Can point to remote PDF or auto-generated
    val assetPath: String? = null,
    val className: String = "Class 10"
)

object PyqPdfRepository {

    val supportedYears = listOf(2026, 2025, 2024, 2023, 2022, 2020, 2019, 2018, 2017, 2016, 2015)

    val allSubjects = listOf(
        Triple("BENGALI", "বাংলা", "Bengali 1st Language"),
        Triple("ENGLISH", "ইংরেজি", "English 2nd Language"),
        Triple("MATHEMATICS", "গণিত", "Mathematics"),
        Triple("PHYSICAL_SCIENCE", "ভৌতবিজ্ঞান", "Physical Science"),
        Triple("LIFE_SCIENCE", "জীবনবিজ্ঞান", "Life Science"),
        Triple("HISTORY", "ইতিহাস", "History"),
        Triple("GEOGRAPHY", "ভূগোল", "Geography")
    )

    // Add your direct PDF links here
    val originalPdfLinks = mapOf(
        "2026_GEOGRAPHY" to "https://wbshiksha.com/wp-content/uploads/2026/08/Madhyamik-Geography-Question-Paper-2026.pdf",
        "2026_BENGALI" to "https://wbshiksha.com/wp-content/uploads/2026/08/Madhyamik-Bengali-Question-Paper-2026.pdf",
        "2026_ENGLISH" to "https://wbshiksha.com/wp-content/uploads/2026/08/Madhyamik-English-Question-Paper-2026.pdf",
        "2026_HISTORY" to "https://wbshiksha.com/wp-content/uploads/2026/08/Madhyamik-History-Question-Paper-2026-PDF.pdf",
        "2026_MATHEMATICS" to "https://wbshiksha.com/wp-content/uploads/2026/08/Madhyamik-Mathematics-Question-Paper-2026.pdf",
        "2026_PHYSICAL_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/Madhyamik-Physical-Science-Question-Paper-2026.pdf",
        "2026_LIFE_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/Madhyamik-Life-Science-Question-Paper-2026-PDF-Download.pdf",

        // 2025 Subjects
        "2025_BENGALI" to "https://wbshiksha.com/wp-content/uploads/2026/08/2025-Bengali-Question-Madhyamik-.pdf",
        "2025_ENGLISH" to "https://wbshiksha.com/wp-content/uploads/2026/08/2025-English-Question-Madhyamik-.pdf",
        "2025_HISTORY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2025-History-Question-Madhyamik-.pdf",
        "2025_GEOGRAPHY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2025-Geography-Question-Madhyamik-.pdf",
        "2025_MATHEMATICS" to "https://wbshiksha.com/wp-content/uploads/2026/08/2025-Mathematics-Question-Madhyamik-.pdf",
        "2025_LIFE_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2025-Life-Science-Question-Madhyamik-.pdf",
        "2025_PHYSICAL_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2025-Physical-Science-Question-Madhyamik-.pdf",

        // 2024 Subjects
        "2024_BENGALI" to "https://wbshiksha.com/wp-content/uploads/2026/08/2024-Bengali-Question-Madhyamik-.pdf",
        "2024_ENGLISH" to "https://wbshiksha.com/wp-content/uploads/2026/08/2024-English-Question-Madhyamik-.pdf",
        "2024_HISTORY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2024-History-Question-Madhyamik-.pdf",
        "2024_GEOGRAPHY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2024-Geography-Question-Madhyamik-.pdf",
        "2024_MATHEMATICS" to "https://wbshiksha.com/wp-content/uploads/2026/08/2024-Mathematics-Question-Madhyamik-.pdf",
        "2024_PHYSICAL_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2024-Physical-Science-Question-Madhyamik-.pdf",
        "2024_LIFE_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2024-Life-Science-Question-Madhyamik-.pdf",
        
        // 2023 Subjects
        "2023_BENGALI" to "https://wbshiksha.com/wp-content/uploads/2026/08/2023-Bengali-Question-Madhyamik-.pdf",
        "2023_ENGLISH" to "https://wbshiksha.com/wp-content/uploads/2026/08/2023-English-Question-Madhyamik.pdf",
        "2023_HISTORY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2023-History-Question-Madhyamik.pdf",
        "2023_GEOGRAPHY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2023-Geography-Question-Madhyamik.pdf",
        "2023_MATHEMATICS" to "https://wbshiksha.com/wp-content/uploads/2026/08/2023-Math-Question-Madhyamik.pdf",
        "2023_LIFE_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2023-Life-Science-Question-Madhyamik.pdf",
        "2023_PHYSICAL_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2023-Physical-Science-Question-_Madhyamik_.pdf",

        // 2022 Subjects
        "2022_BENGALI" to "https://wbshiksha.com/wp-content/uploads/2026/08/2022-Bengali-Question-Paper-Madhyamik.pdf",
        "2022_ENGLISH" to "https://wbshiksha.com/wp-content/uploads/2026/08/2022-English-Question-Paper-Madhyamik.pdf",
        "2022_HISTORY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2022-History-Question-Paper-Madhyamik.pdf",
        "2022_GEOGRAPHY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2022-Geography-Question-Paper-Madhyamik.pdf",
        "2022_MATHEMATICS" to "https://wbshiksha.com/wp-content/uploads/2026/08/2022-Mathematics-Question-Paper-Madhyamik.pdf",
        "2022_LIFE_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2022-Life-Science-Question-Paper-Madhyamik.pdf",
        "2022_PHYSICAL_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2022-Physical-Science-Madhyamik.pdf",

        // 2020 Subjects
        "2020_BENGALI" to "https://wbshiksha.com/wp-content/uploads/2026/08/2020-Bengali-Question-Madhyamik.pdf",
        "2020_ENGLISH" to "https://wbshiksha.com/wp-content/uploads/2026/08/2020-English-Question-Madhyamik-.pdf",
        "2020_HISTORY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2020-History-Question-Madhyamik.pdf",
        "2020_GEOGRAPHY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2020-Geography-Question-Madhyamik-.pdf",
        "2020_MATHEMATICS" to "https://wbshiksha.com/wp-content/uploads/2026/08/2020-Mathematics-Question-Madhyamik.pdf",
        "2020_LIFE_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2020-Life-Science-Question-Madhyamik-.pdf",
        "2020_PHYSICAL_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2020-Physical-Science-Question-Madhyamik.pdf",

        // 2019 Subjects
        "2019_BENGALI" to "https://wbshiksha.com/wp-content/uploads/2026/08/2019-Bengali-Question-_-Madhyamik-__watermark-_1_.pdf",
        "2019_ENGLISH" to "https://wbshiksha.com/wp-content/uploads/2026/08/2019-English-Question-_-Madhyamik-__watermark.pdf",
        "2019_HISTORY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2019-History-Question-_-Madhyamik-__watermark.pdf",
        "2019_GEOGRAPHY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2019-Geography-Question-_-Madhyamik-__watermark.pdf",
        "2019_MATHEMATICS" to "https://wbshiksha.com/wp-content/uploads/2026/08/2019-Mathematics-Question-_-Madhyamik-__watermark.pdf",
        "2019_LIFE_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2019-Life-Science-Question-_-Madhyamik-__watermark.pdf",
        "2019_PHYSICAL_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2019-Physical-Science-_-Madhyamik-__watermark.pdf",

        // 2018 Subjects
        "2018_BENGALI" to "https://wbshiksha.com/wp-content/uploads/2026/08/2018-Bengali-Question-_-Madhyamik-__watermark-1.pdf",
        "2018_ENGLISH" to "https://wbshiksha.com/wp-content/uploads/2026/08/2018-English-Question-_-Madhyamik-__watermark.pdf",
        "2018_HISTORY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2018-History-Question-_-Madhyamik-__watermark.pdf",
        "2018_GEOGRAPHY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2018-Geography-Question-_-Madhyamik-__watermark.pdf",
        "2018_MATHEMATICS" to "https://wbshiksha.com/wp-content/uploads/2026/08/2018-Mathematics-Question-_-Madhyamik-__watermark.pdf",
        "2018_LIFE_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2018-Life-Science-Question-_-Madhyamik-__watermark.pdf",
        "2018_PHYSICAL_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2018-Physical-Science-Question-_-Madhyamik-__watermark.pdf",

        // 2017 Subjects
        "2017_BENGALI" to "https://wbshiksha.com/wp-content/uploads/2026/08/2017-Bengali-Question-Madhyamik.pdf",
        "2017_ENGLISH" to "https://wbshiksha.com/wp-content/uploads/2026/08/2017-English-Question-Madhyamik.pdf",
        "2017_HISTORY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2017-History-Question-Madhyamik-_watermark.pdf",
        "2017_GEOGRAPHY" to "https://wbshiksha.com/wp-content/uploads/2026/08/2017-Geography-Question-Madhyamik-_watermark-_1_.pdf",
        "2017_MATHEMATICS" to "https://wbshiksha.com/wp-content/uploads/2026/08/2017-Mathematics-Question-Madhyamik-_watermark.pdf",
        "2017_LIFE_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2017-Life-Science-Question-Madhyamik-_watermark.pdf",
        "2017_PHYSICAL_SCIENCE" to "https://wbshiksha.com/wp-content/uploads/2026/08/2017-Physical-Science-Question-Madhyamik-_watermark.pdf",

        // Example format for other classes:
        "Class9_2026_BENGALI" to "https://example.com/class9_bengali_2026.pdf"
    )

    val classes = listOf("Class 12", "Class 11", "Class 10", "Class 9", "Class 8", "Class 7", "Class 6", "Class 5")

    fun getPdfsForYear(year: Int): List<PyqPdfItem> {
        val sizeList = listOf("2.6 MB", "1.9 MB", "3.2 MB", "2.8 MB", "2.5 MB", "3.1 MB", "2.7 MB")
        val allPdfs = mutableListOf<PyqPdfItem>()

        for (className in classes) {
            allSubjects.forEachIndexed { index, (code, benName, engName) ->
                val titleBen = when (className) {
                    "Class 12" -> "উচ্চমাধ্যমিক $year $benName আসল প্রশ্নপত্র ও উত্তর"
                    "Class 11" -> "একাদশ শ্রেণি $year $benName বার্ষিক প্রশ্নপত্র ও উত্তর"
                    "Class 10" -> "মাধ্যমিক $year $benName আসল প্রশ্নপত্র ও উত্তর"
                    else -> "$className $year $benName বার্ষিক প্রশ্নপত্র"
                }
                val titleEng = when (className) {
                    "Class 12" -> "Higher Secondary (HS) $year $engName Original Question Paper"
                    "Class 11" -> "Class 11 $year $engName Annual Exam Paper"
                    "Class 10" -> "Madhyamik $year $engName Original Question Paper & Answer"
                    else -> "$className $year $engName Annual Exam Paper"
                }
                val marks = when (className) {
                    "Class 12", "Class 11" -> 80
                    "Class 10", "Class 9" -> 90
                    else -> 50
                }
                val time = if (marks >= 80) "৩ ঘণ্টা ১৫ মিনিট" else "২ ঘণ্টা"
                // For Class 10, support legacy key format "${year}_${code}", for others support "${className}_${year}_${code}"
                val url = originalPdfLinks["${className.replace(" ", "")}_${year}_${code}"] ?: if (className == "Class 10") originalPdfLinks["${year}_${code}"] else null

                allPdfs.add(
                    PyqPdfItem(
                        id = "pyq_pdf_${year}_${code.lowercase()}_${className.replace(" ", "").lowercase()}",
                        year = year,
                        subjectCode = code,
                        titleBengali = titleBen,
                        titleEnglish = titleEng,
                        subjectBengali = benName,
                        subjectEnglish = engName,
                        fileSize = sizeList.getOrElse(index) { "2.5 MB" },
                        totalMarks = marks,
                        timeLimit = time,
                        isOfficial = true,
                        downloadUrl = url,
                        assetPath = null,
                        className = className
                    )
                )
            }
        }
        return allPdfs
    }

    fun getAllPdfs(): List<PyqPdfItem> {
        return supportedYears.flatMap { year -> getPdfsForYear(year) }
    }
}
