package com.example.util

import com.example.data.local.SubjectEntity
import com.example.data.model.SubjectItem

/**
 * Helper utility to parse and filter subjects based on comma-separated or single 'stream' definitions.
 *
 * Supported streams in WBCHSE / CBSE / Higher Secondary:
 * - "science" (বিজ্ঞান)
 * - "arts" (কলা / মানবিক)
 * - "commerce" (বাণিজ্য)
 * - "common" or "all" (সকলের জন্য প্রযোজ্য)
 *
 * Supports multi-stream declarations such as:
 * - "stream": "science, arts"
 * - "stream": "science, commerce"
 * - "stream": "arts, commerce"
 * - "stream": "science, arts, commerce"
 */
object StreamHelper {

    /**
     * Splits a comma-separated stream string into a normalized list of trimmed lowercase stream tokens.
     * E.g. "science, arts" -> ["science", "arts"]
     */
    fun parseStreams(streamString: String?): List<String> {
        if (streamString.isNullOrBlank()) {
            return listOf("common")
        }
        val tokens = streamString.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        return if (tokens.isEmpty()) listOf("common") else tokens
    }

    /**
     * Checks whether a subject's stream configuration satisfies the selected target stream.
     *
     * @param subjectStream Comma-separated or single stream definition from subject (e.g., "science, arts", "common", "science")
     * @param targetStream Active student or filter stream (e.g., "science", "arts", "commerce", "all", or blank)
     * @return true if the subject should be visible under the target stream
     */
    fun matchesStream(subjectStream: String?, targetStream: String?): Boolean {
        // If target stream is empty or "all", all subjects are displayed
        val rawTarget = targetStream?.trim()?.lowercase() ?: ""
        val normalizedTarget = if (rawTarget.contains("_")) rawTarget.split("_")[0] else rawTarget
        if (normalizedTarget.isBlank() || normalizedTarget == "all") {
            return true
        }

        val subjectStreams = parseStreams(subjectStream)

        // If subject is designated as "common" or "all", it belongs to all streams (e.g., Bengali, English)
        if (subjectStreams.contains("common") || subjectStreams.contains("all")) {
            return true
        }

        // If targetStream is comma-separated (e.g., searching for "science, arts"), match if any intersect
        if (normalizedTarget.contains(",")) {
            val targets = normalizedTarget.split(",").map { it.trim() }.filter { it.isNotBlank() }
            return subjectStreams.any { s -> targets.contains(s) }
        }

        // Standard check: Does this subject list contain the selected stream?
        return subjectStreams.contains(normalizedTarget)
    }

    /**
     * Filters a list of SubjectItem domain models based on target stream.
     */
    fun filterSubjects(subjects: List<SubjectItem>, targetStream: String?): List<SubjectItem> {
        if (targetStream.isNullOrBlank() || targetStream.trim().lowercase() == "all") {
            return subjects
        }
        return subjects.filter { matchesStream(it.stream, targetStream) }
    }

    /**
     * Filters a list of SubjectEntity database models based on target stream.
     */
    fun filterSubjectEntities(subjects: List<SubjectEntity>, targetStream: String?): List<SubjectEntity> {
        if (targetStream.isNullOrBlank() || targetStream.trim().lowercase() == "all") {
            return subjects
        }
        return subjects.filter { matchesStream(it.stream, targetStream) }
    }

    /**
     * Formats a user-friendly label for a stream or multi-stream string.
     * E.g. "science, arts" -> "বিজ্ঞান • কলা" (Bengali) or "Science • Arts" (English)
     */
    fun getFormattedStreamLabel(streamString: String?, isBengali: Boolean = true): String {
        val streams = parseStreams(streamString)
        if (streams.isEmpty() || streams.contains("common") || streams.contains("all")) {
            return if (isBengali) "সকল বিভাগ (Common)" else "Common / All"
        }

        return streams.joinToString(" • ") { s ->
            when (s) {
                "science" -> if (isBengali) "বিজ্ঞান" else "Science"
                "arts" -> if (isBengali) "কলা" else "Arts"
                "commerce" -> if (isBengali) "বাণিজ্য" else "Commerce"
                "vocational" -> if (isBengali) "বৃত্তিমূলক" else "Vocational"
                else -> s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }
}

/**
 * Extension functions for SubjectItem
 */
fun SubjectItem.belongsToStream(targetStream: String?): Boolean {
    return StreamHelper.matchesStream(this.stream, targetStream)
}

fun SubjectItem.getStreamList(): List<String> {
    return StreamHelper.parseStreams(this.stream)
}

fun List<SubjectItem>.filterByStream(targetStream: String?): List<SubjectItem> {
    return StreamHelper.filterSubjects(this, targetStream)
}

/**
 * Extension functions for SubjectEntity
 */
fun SubjectEntity.belongsToStream(targetStream: String?): Boolean {
    return StreamHelper.matchesStream(this.stream, targetStream)
}

fun SubjectEntity.getStreamList(): List<String> {
    return StreamHelper.parseStreams(this.stream)
}

fun List<SubjectEntity>.filterEntitiesByStream(targetStream: String?): List<SubjectEntity> {
    return StreamHelper.filterSubjectEntities(this, targetStream)
}
