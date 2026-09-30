package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ArchiveSearchResult(
    val identifier: String,
    val title: String,
    val creator: String,
    val year: String,
    val downloads: Int,
    val coverImageUrl: String = "https://archive.org/services/img/$identifier",
    val formattedDownloads: String = formatDownloads(downloads)
) {
    companion object {
        fun formatDownloads(count: Int): String {
            return when {
                count >= 1_000_000 -> {
                    val m = count / 1_000_000.0
                    String.format(Locale.US, "%.1fM", m).replace(".0M", "M")
                }
                count >= 1_000 -> {
                    val k = count / 1_000.0
                    String.format(Locale.US, "%.1fK", k).replace(".0K", "K")
                }
                else -> count.toString()
            }
        }
    }
}

object InternetArchiveApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // Helper functions to clean up titles and creators
    private fun sanitizeTitle(title: String): String {
        return title
            .replace(Regex("\\[.*?\\]|\\(.*?\\)"), "") // Remove content inside [] or ()
            .replace(Regex("\\s+"), " ")                // Normalize spaces
            .trim()
    }

    private fun sanitizeCreator(creator: String): String {
        var cleaned = creator
            // Remove content inside [] or ()
            .replace(Regex("\\[.*?\\]|\\(.*?\\)"), "")
            // Remove years like 1800-1890, 1800, etc.
            .replace(Regex("\\d{4}-\\d{4}|\\d{4}"), "")
            // Remove noise keywords
            .replace(Regex("(?i)unknown author|n/a"), "")

        // Fix Last, First -> First Last if it's a simple comma format
        if (cleaned.contains(",") && cleaned.split(",").size == 2) {
            val parts = cleaned.split(",")
            cleaned = "${parts[1].trim()} ${parts[0].trim()}"
        }

        return cleaned
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { "Unknown Author" }
    }

    suspend fun searchTexts(query: String, page: Int = 1): List<ArchiveSearchResult> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return@withContext emptyList()

        val encodedQuery = URLEncoder.encode(trimmedQuery, "UTF-8")
        val url = "https://archive.org/advancedsearch.php?q=$encodedQuery+AND+mediatype:texts&fl[]=identifier,title,creator,year,downloads&sort[]=downloads+desc&rows=30&page=$page&output=json"

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) DeshExam/1.0")
            .build()

        val results = mutableListOf<ArchiveSearchResult>()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonBody = response.body?.string()
                if (jsonBody != null) {
                    val json = JSONObject(jsonBody)
                    val docs = json.optJSONObject("response")?.optJSONArray("docs")
                    if (docs != null) {
                        for (i in 0 until docs.length()) {
                            val doc = docs.getJSONObject(i)
                            val identifier = doc.optString("identifier", "")
                            if (identifier.isBlank()) continue

                            val rawTitle = doc.optString("title", "Unknown Title")
                            val cleanTitle = sanitizeTitle(rawTitle)

                            val creatorRaw = doc.opt("creator")
                            val cleanCreator = when (creatorRaw) {
                                is JSONArray -> {
                                    val list = mutableListOf<String>()
                                    for (j in 0 until creatorRaw.length()) {
                                        val authorItem = sanitizeCreator(creatorRaw.optString(j, ""))
                                        if (authorItem.isNotBlank()) list.add(authorItem)
                                    }
                                    if (list.isEmpty()) "Unknown Author" else list.joinToString(", ")
                                }
                                is String -> sanitizeCreator(creatorRaw)
                                else -> "Unknown Author"
                            }

                            val year = doc.optString("year", "").ifBlank { "N/A" }
                            val downloads = doc.optInt("downloads", 0)

                            results.add(
                                ArchiveSearchResult(
                                    identifier = identifier,
                                    title = cleanTitle,
                                    creator = cleanCreator,
                                    year = year,
                                    downloads = downloads
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        results
    }

    /**
     * Resolves the best direct PDF download URL for an Internet Archive text item.
     */
    suspend fun getPdfDownloadUrl(identifier: String): String = withContext(Dispatchers.IO) {
        try {
            val url = "https://archive.org/metadata/$identifier/files"
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) DeshExam/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonBody = response.body?.string()
                if (jsonBody != null) {
                    val json = JSONObject(jsonBody)
                    val filesArray = json.optJSONArray("result")
                    if (filesArray != null) {
                        var candidatePdfName: String? = null
                        for (i in 0 until filesArray.length()) {
                            val fileObj = filesArray.getJSONObject(i)
                            val name = fileObj.optString("name", "")
                            val format = fileObj.optString("format", "")
                            if (name.endsWith(".pdf", ignoreCase = true)) {
                                if (format.contains("Text PDF", ignoreCase = true) || candidatePdfName == null) {
                                    candidatePdfName = name
                                }
                            }
                        }
                        if (candidatePdfName != null) {
                            val encodedName = candidatePdfName.replace(" ", "%20")
                            return@withContext "https://archive.org/download/$identifier/$encodedName"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Standard direct download convention for Internet Archive items
        "https://archive.org/download/$identifier/$identifier.pdf"
    }
}
