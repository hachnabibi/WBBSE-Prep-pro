package com.example.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class PlaylistItem(
    val id: String? = "",
    val partNumber: Int = 1,
    val title: String? = "",
    val titleBengali: String? = null,
    val duration: String? = "১২:৩০ মিনিট",
    val youtubeVideoId: String? = "",
    val videoUrl: String? = "", // Direct MP4 or media stream URL
    val instructorName: String? = "বিষয়ভিত্তিক শিক্ষক",
    val description: String? = ""
) {
    fun getEffectiveVideoSource(): String {
        val direct = videoUrl?.trim()
        if (!direct.isNullOrBlank()) return direct
        val yt = youtubeVideoId?.trim()
        if (!yt.isNullOrBlank()) return yt
        return "R-LMIuytqmg"
    }

    fun isDirectMp4(): Boolean {
        return VideoLesson.isDirectMediaUrl(getEffectiveVideoSource())
    }

    fun getEffectiveVideoId(): String = VideoLesson.extractYoutubeId(getEffectiveVideoSource())

    val safeId: String get() = id ?: ""
    val safeTitle: String get() = titleBengali?.takeIf { it.isNotBlank() } ?: title?.takeIf { it.isNotBlank() } ?: "পর্ব $partNumber"
    val safeDuration: String get() = duration?.takeIf { it.isNotBlank() } ?: "১২:০০ মিনিট"
    val safeInstructorName: String get() = instructorName?.takeIf { it.isNotBlank() } ?: "বিষয় বিশেষজ্ঞ শিক্ষক"
    val safeDescription: String get() = description ?: ""
}

data class VideoLesson(
    val id: String? = "",
    val topicId: String? = "",
    val chapterId: String? = "",
    val subjectId: String? = "",
    val title: String? = "",
    val titleBengali: String? = null,
    val instructorName: String? = "পশ্চিমবঙ্গ বোর্ড শিক্ষক মণ্ডলী",
    val channelOrSource: String? = "মাধ্যমিক ডিজিটাল ক্লাসরুম",
    val duration: String? = "১৫:৩০ মিনিট",
    val youtubeVideoId: String? = "R-LMIuytqmg", // YouTube Video ID or direct MP4 URL
    val videoUrl: String? = "", // Direct MP4 / Hosted Video URL
    val summary: String? = "এই ভিডিও ক্লাসে প্রতিটি মূল কনসেপ্ট, সূত্র ও গুরুত্বপূর্ণ প্রশ্নের সহজ ব্যাখ্যা দেওয়া হয়েছে।",
    val playlist: List<PlaylistItem>? = emptyList(),
    val keyTakeaways: List<String>? = emptyList()
) {
    fun getEffectiveVideoSource(): String {
        val direct = videoUrl?.trim()
        if (!direct.isNullOrBlank()) return direct
        val yt = youtubeVideoId?.trim()
        if (!yt.isNullOrBlank()) return yt
        return "R-LMIuytqmg"
    }

    fun isDirectMp4(): Boolean {
        return isDirectMediaUrl(getEffectiveVideoSource())
    }

    fun getEffectiveVideoId(): String {
        return extractYoutubeId(getEffectiveVideoSource())
    }

    val safeId: String get() = id ?: "video_${safeTopicId}"
    val safeTopicId: String get() = topicId ?: ""
    val safeChapterId: String get() = chapterId ?: ""
    val safeSubjectId: String get() = subjectId ?: ""
    val safeTitle: String get() = titleBengali?.takeIf { it.isNotBlank() } ?: title?.takeIf { it.isNotBlank() } ?: "টপিক ভিডিও ক্লাস ও বিস্তারিত আলোচনা"
    val safeInstructorName: String get() = instructorName?.takeIf { it.isNotBlank() } ?: "পশ্চিমবঙ্গ বোর্ড শিক্ষক মণ্ডলী"
    val safeChannelOrSource: String get() = channelOrSource?.takeIf { it.isNotBlank() } ?: "মাধ্যমিক ডিজিটাল ক্লাসরুম"
    val safeDuration: String get() = duration?.takeIf { it.isNotBlank() } ?: "১৫:৩০ মিনিট"
    val safeSummary: String get() = summary?.takeIf { it.isNotBlank() } ?: "এই ভিডিও ক্লাসে প্রতিটি মূল কনসেপ্ট, সূত্র ও গুরুত্বপূর্ণ প্রশ্নের সহজ ব্যাখ্যা দেওয়া হয়েছে।"
    val safePlaylist: List<PlaylistItem> get() = playlist?.filterNotNull() ?: emptyList()
    val safeKeyTakeaways: List<String> get() = keyTakeaways?.filterNotNull() ?: emptyList()

    companion object {
        fun isDirectMediaUrl(url: String?): Boolean {
            if (url == null) return false
            val lower = url.lowercase().trim()
            if (lower.isBlank()) return false
            return lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mkv") ||
                   lower.endsWith(".m4v") || lower.endsWith(".mov") || lower.endsWith(".mp3") ||
                   lower.endsWith(".m3u8") || lower.contains("/video/") || lower.contains(".mp4?") ||
                   lower.contains("storage.googleapis.com") || lower.contains("cloudinary.com") ||
                   lower.contains("supabase.co/storage") || lower.contains("firebasestorage.googleapis.com") ||
                   (lower.startsWith("http") && !lower.contains("youtube.com") && !lower.contains("youtu.be"))
        }

        fun extractYoutubeId(urlOrId: String?): String {
            if (urlOrId == null) return "R-LMIuytqmg"
            val trimmed = urlOrId.trim()
            if (trimmed.isBlank()) return "R-LMIuytqmg"
            if (!trimmed.contains("/") && !trimmed.contains("?") && !trimmed.contains("=")) {
                return trimmed
            }
            // Match various YouTube URL formats: watch?v=, youtu.be/, embed/, shorts/, v/
            val patterns = listOf(
                "(?:v=|/v/|youtu\\.be/|/embed/|/shorts/|/e/|watch\\?v=)([^#&?\\s]+)".toRegex(),
                "(?<=watch\\?v=)[^#&?\\s]+".toRegex(),
                "(?<=youtu\\.be/)[^#&?\\s]+".toRegex(),
                "(?<=embed/)[^#&?\\s]+".toRegex(),
                "(?<=shorts/)[^#&?\\s]+".toRegex()
            )
            for (pattern in patterns) {
                val match = pattern.find(trimmed)
                if (match != null) {
                    val groupVal = if (match.groupValues.size > 1) match.groupValues[1] else match.value
                    if (groupVal.isNotBlank()) return groupVal.trim()
                }
            }
            return trimmed
        }
    }
}

object VideoLessonProvider {
    private val gson = Gson()
    // In-memory or custom overridden URLs map: topicId -> customVideoId
    private val customTopicVideos = mutableMapOf<String, String>()
    // Dedicated lessons loaded from videos.json in subject folders
    private val registeredJsonLessons = mutableMapOf<String, VideoLesson>()

    fun registerLessons(lessons: List<VideoLesson>) {
        lessons.forEach { lesson ->
            val tId = lesson.safeTopicId
            if (tId.isNotBlank()) {
                registeredJsonLessons[tId] = lesson
            }
        }
    }

    fun getLessonsForSubject(subjectId: String): List<VideoLesson> {
        return registeredJsonLessons.values.filter { it.subjectId == subjectId }
    }

    fun setCustomVideoUrl(topicId: String, urlOrId: String) {
        customTopicVideos[topicId] = urlOrId
    }

    fun getVideoForTopic(topic: TopicItem): VideoLesson {
        return getVideoForTopic(
            topicId = topic.id,
            topicTitle = topic.title,
            subjectId = topic.subjectId,
            topicVideoUrl = topic.videoUrl,
            topicPlaylistJson = topic.playlistJson
        )
    }

    fun getVideoForTopic(
        topicId: String,
        topicTitle: String = "",
        subjectId: String = "",
        topicVideoUrl: String? = null,
        topicPlaylistJson: String? = null
    ): VideoLesson {
        val customId = customTopicVideos[topicId]
        
        // 1. If custom video was set during runtime in app
        if (!customId.isNullOrBlank()) {
            val baseLesson = registeredJsonLessons[topicId] ?: defaultTopicLessons[topicId] ?: generateDefaultLessonForTopic(topicId, topicTitle, subjectId, customId, topicPlaylistJson)
            return baseLesson.copy(youtubeVideoId = customId)
        }

        // 2. If dedicated video lesson was loaded from videos.json
        val jsonLesson = registeredJsonLessons[topicId]
        if (jsonLesson != null) {
            return jsonLesson
        }

        // 3. If topic videoUrl or playlistJson is present from topics.json
        if (!topicVideoUrl.isNullOrBlank() || !topicPlaylistJson.isNullOrBlank()) {
            return generateDefaultLessonForTopic(topicId, topicTitle, subjectId, topicVideoUrl, topicPlaylistJson)
        }

        // 4. Fallback to default hardcoded lessons or generated lesson
        return defaultTopicLessons[topicId] ?: generateDefaultLessonForTopic(topicId, topicTitle, subjectId, null, null)
    }

    private fun generateDefaultLessonForTopic(
        topicId: String,
        topicTitle: String,
        subjectId: String,
        explicitVideoUrl: String?,
        explicitPlaylistJson: String?
    ): VideoLesson {
        val title = topicTitle.ifBlank { "টপিক ভিডিও ক্লাস ও বিস্তারিত আলোচনা" }
        val effectiveVideoId = explicitVideoUrl?.takeIf { it.isNotBlank() } ?: "R-LMIuytqmg"

        // Try parsing playlistJson if provided from topics.json
        var customPlaylist: List<PlaylistItem>? = null
        if (!explicitPlaylistJson.isNullOrBlank()) {
            try {
                val listType = object : TypeToken<List<PlaylistItem>>() {}.type
                customPlaylist = gson.fromJson<List<PlaylistItem>>(explicitPlaylistJson, listType)
            } catch (_: Exception) {}
        }

        val playlistItems = customPlaylist?.filterNotNull()?.takeIf { it.isNotEmpty() } ?: listOf(
            PlaylistItem(
                id = "${topicId}_part_1",
                partNumber = 1,
                title = "$title - পর্ব ১ (মূল কনসেপ্ট ও সূত্র)",
                titleBengali = "$title - পর্ব ১ (মূল কনসেপ্ট ও সূত্র)",
                duration = "১৪:২০ মিনিট",
                youtubeVideoId = effectiveVideoId,
                instructorName = "বিষয় বিশেষজ্ঞ শিক্ষক",
                description = "মৌলিক ধারণা, সংজ্ঞা ও সূত্রসমূহের বিশদ ব্যাখ্যা।"
            ),
            PlaylistItem(
                id = "${topicId}_part_2",
                partNumber = 2,
                title = "$title - পর্ব ২ (গুরুত্বপূর্ণ প্রশ্নোত্তর)",
                titleBengali = "$title - পর্ব ২ (গুরুত্বপূর্ণ প্রশ্নোত্তর)",
                duration = "১২:১৫ মিনিট",
                youtubeVideoId = effectiveVideoId,
                instructorName = "সহকারী শিক্ষক মণ্ডলী",
                description = "পরীক্ষার জন্য অতি গুরুত্বপূর্ণ সংক্ষিপ্ত ও বর্ণনামূলক প্রশ্ন।"
            ),
            PlaylistItem(
                id = "${topicId}_part_3",
                partNumber = 3,
                title = "$title - পর্ব ৩ (বিগত বছরের প্রশ্ন সমাধান)",
                titleBengali = "$title - পর্ব ৩ (বিগত বছরের প্রশ্ন সমাধান)",
                duration = "১৬:৪০ মিনিট",
                youtubeVideoId = effectiveVideoId,
                instructorName = "পরীক্ষা পরামর্শক শিক্ষক",
                description = "মাধ্যমিক ও টেস্ট পরীক্ষার বিগত ৫ বছরের সাজেস্টিভ প্রশ্ন সমাধান।"
            ),
            PlaylistItem(
                id = "${topicId}_part_4",
                partNumber = 4,
                title = "$title - পর্ব ৪ (শর্ট ট্রিকস ও দ্রুত রিভিশন)",
                titleBengali = "$title - পর্ব ৪ (শর্ট ট্রিকস ও দ্রুত রিভিশন)",
                duration = "০৯:৫০ মিনিট",
                youtubeVideoId = effectiveVideoId,
                instructorName = "সিনিয়র মেন্টর",
                description = "মনে রাখার সহজ টেকনিক ও শেষ মুহূর্তের কুইক রিভিশন।"
            )
        )

        return VideoLesson(
            id = "video_$topicId",
            topicId = topicId,
            subjectId = subjectId,
            title = title,
            titleBengali = "$title - সম্পূর্ণ প্লেলিস্ট ও ক্লাস",
            instructorName = "পশ্চিমবঙ্গ বোর্ড শিক্ষক মণ্ডলী",
            channelOrSource = "মাধ্যমিক ডিজিটাল ক্লাসরুম",
            duration = "${playlistItems.size}টি পর্বের সম্পূর্ণ প্লেলিস্ট",
            youtubeVideoId = effectiveVideoId,
            summary = "এই ক্লাসে $title সম্পর্কে বিস্তারিত ধারণা, গুরুত্বপূর্ণ গাণিতিক সমস্যা এবং পরীক্ষার সম্ভাব্য প্রশ্নগুলো সহজ ভাষায় আলোচনা করা হয়েছে।",
            playlist = playlistItems,
            keyTakeaways = listOf(
                "টপিকের সমস্ত মৌলিক সূত্র ও সংজ্ঞা স্পষ্ট করে বোঝা।",
                "চিত্র ও গাণিতিক উদাহরণের নির্ভুল প্রয়োগ কৌশল।",
                "পরীক্ষার জন্য সেরা উত্তর লেখার নিয়ম।"
            )
        )
    }

    private val defaultTopicLessons = mapOf(
        "phy_light_01" to VideoLesson(
            id = "video_phy_light_01",
            topicId = "phy_light_01",
            subjectId = "phy_sci",
            title = "আলোর প্রতিসরণ ও লেন্স",
            titleBengali = "আলোর প্রতিসরণ ও লেন্স - সম্পূর্ণ প্লেলিস্ট",
            instructorName = "সৌমেন বন্দ্যোপাধ্যায় (ভৌতবিজ্ঞান বিশেষজ্ঞ)",
            channelOrSource = "মাধ্যমিক ভৌতবিজ্ঞান সহায়িকা",
            duration = "৩টি পর্বের প্লেলিস্ট",
            youtubeVideoId = "vfMxTtyxMUE",
            summary = "স্নেলের সূত্র, সংকট কোণ, অভ্যন্তরীণ পূর্ণ প্রতিফলন এবং উত্তল ও অবতল লেন্সের প্রতিবিম্ব গঠনের সহজ বিশ্লেষণ।",
            playlist = listOf(
                PlaylistItem(
                    id = "phy_light_p1",
                    partNumber = 1,
                    title = "আলোর প্রতিসরণ ও স্নেলের সূত্র",
                    titleBengali = "পর্ব ১: আলোর প্রতিসরণ ও স্নেলের সূত্র",
                    duration = "১৫:২০ মিনিট",
                    youtubeVideoId = "vfMxTtyxMUE",
                    instructorName = "সৌমেন বন্দ্যোপাধ্যায়"
                ),
                PlaylistItem(
                    id = "phy_light_p2",
                    partNumber = 2,
                    title = "সংকট কোণ ও পূর্ণ অভ্যন্তরীণ প্রতিফলন",
                    titleBengali = "পর্ব ২: সংকট কোণ ও পূর্ণ অভ্যন্তরীণ প্রতিফলন",
                    duration = "১২:৪৫ মিনিট",
                    youtubeVideoId = "vfMxTtyxMUE",
                    instructorName = "সৌমেন বন্দ্যোপাধ্যায়"
                ),
                PlaylistItem(
                    id = "phy_light_p3",
                    partNumber = 3,
                    title = "উত্তল ও অবতল লেন্সের প্রতিবিম্ব গঠন",
                    titleBengali = "পর্ব ৩: উত্তল ও অবতল লেন্সের প্রতিবিম্ব গঠন",
                    duration = "১৮:১০ মিনিট",
                    youtubeVideoId = "vfMxTtyxMUE",
                    instructorName = "সৌমেন বন্দ্যোপাধ্যায়"
                )
            ),
            keyTakeaways = listOf(
                "প্রতিসরণের সূত্রাবলী ও স্নেলের সূত্রের গাণিতিক প্রয়োগ।",
                "বিভিন্ন অবস্থানে বস্তুর প্রতিবিম্বের অবস্থান ও প্রকৃতি।",
                "লেন্স সংক্রান্ত গুরুত্বপূর্ণ একক ও মাত্রার সঠিক ব্যবহার।"
            )
        )
    )
}
