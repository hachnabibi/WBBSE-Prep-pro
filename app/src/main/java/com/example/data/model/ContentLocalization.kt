package com.example.data.model

import com.example.data.local.ChapterEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TopicEntity

/**
 * ContentLocalization provides complete bilingual support (Bengali & English)
 * for subjects, chapters, topics, and digits across the DeshExam curriculum.
 */
object ContentLocalization {

    fun formatNumber(num: Int, isBengali: Boolean): String {
        return if (isBengali) num.toString().toBengaliDigits() else num.toString()
    }

    fun cleanEnglishFallback(text: String): String {
        val openParen = text.lastIndexOf('(')
        val closeParen = text.lastIndexOf(')')
        if (openParen != -1 && closeParen > openParen) {
            val candidate = text.substring(openParen + 1, closeParen).trim()
            if (candidate.isNotEmpty() && candidate.any { it in 'A'..'Z' || it in 'a'..'z' }) {
                return candidate
            }
        }
        return text
    }

    fun getSubjectTitle(subjectId: String, code: SubjectCode?, fallback: String, isBengali: Boolean): String {
        val targetCode = code ?: run {
            val cleanId = subjectId.lowercase()
            SubjectCode.values().find { cleanId.contains(it.id) }
        }

        if (targetCode != null) {
            val cleanId = subjectId.lowercase()
            if (targetCode != SubjectCode.PHYSICAL_SCIENCE || cleanId.contains("phy") || cleanId.contains("science")) {
                return if (isBengali) targetCode.subjectNameBengali else targetCode.subjectNameEnglish
            }
        }

        return if (isBengali) {
            fallback.ifBlank { "বিষয়" }
        } else {
            cleanEnglishFallback(fallback).ifBlank { "Subject" }
        }
    }

    fun getChapterTitle(chapterId: String, fallback: String, isBengali: Boolean): String {
        return if (isBengali) {
            fallback.ifBlank { "অধ্যায়" }
        } else {
            cleanEnglishFallback(fallback)
        }
    }

    fun getChapterDescription(chapterId: String, fallback: String, isBengali: Boolean): String {
        return fallback
    }

    fun getTopicTitle(topicId: String, fallback: String, isBengali: Boolean): String {
        return if (isBengali) {
            fallback.ifBlank { "টপিক" }
        } else {
            cleanEnglishFallback(fallback)
        }
    }

    fun getTopicSummary(topicId: String, fallback: String, isBengali: Boolean): String {
        return fallback
    }
}

/**
 * Extension Functions for fluent UI consumption
 */
fun String.toBengaliDigits(): String {
    val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    var result = this
    for (i in 0..9) {
        result = result.replace(englishDigits[i], bengaliDigits[i])
    }
    return result
}

fun Int.toBengaliDigits(): String = this.toString().toBengaliDigits()

fun SubjectItem.getTitle(isBengali: Boolean): String {
    return ContentLocalization.getSubjectTitle(id, code, name, isBengali)
}

fun SubjectItem.getClassLevelName(isBengali: Boolean): String {
    return AcademicClassProvider.getClassName(this.id, isBengali)
}

fun SubjectEntity.getTitle(isBengali: Boolean): String {
    val codeEnum = SubjectCode.values().find { it.id == code.lowercase() }
    return ContentLocalization.getSubjectTitle(id, codeEnum, name, isBengali)
}

fun SubjectEntity.getTextbook(isBengali: Boolean = true): String {
    return this.textbookName
}

fun SubjectEntity.getClassLevelName(isBengali: Boolean): String {
    return AcademicClassProvider.getClassName(if (this.classId.isNotBlank()) this.classId else this.id, isBengali)
}

fun ChapterItem.getTitle(isBengali: Boolean): String {
    return ContentLocalization.getChapterTitle(id, title, isBengali)
}

fun ChapterItem.getDescription(isBengali: Boolean): String {
    return ContentLocalization.getChapterDescription(id, description, isBengali)
}

fun ChapterEntity.getTitle(isBengali: Boolean): String {
    return ContentLocalization.getChapterTitle(id, title, isBengali)
}

fun ChapterEntity.getDescription(isBengali: Boolean): String {
    return ContentLocalization.getChapterDescription(id, description, isBengali)
}

fun TopicItem.getTitle(isBengali: Boolean): String {
    return ContentLocalization.getTopicTitle(id, title, isBengali)
}

fun TopicItem.getSummary(isBengali: Boolean): String {
    return ContentLocalization.getTopicSummary(id, summary, isBengali)
}

fun TopicEntity.getTitle(isBengali: Boolean): String {
    return ContentLocalization.getTopicTitle(id, title, isBengali)
}

fun TopicEntity.getSummary(isBengali: Boolean): String {
    return ContentLocalization.getTopicSummary(id, summary, isBengali)
}
