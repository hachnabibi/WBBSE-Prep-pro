package com.example.data.repository

import android.content.Context
import com.example.ui.screens.PyqQuestionItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File

object PyqRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun loadAllPyqQuestions(context: Context): List<PyqQuestionItem> {
        val allQuestions = mutableListOf<PyqQuestionItem>()
        val assets = context.assets

        android.util.Log.d("PyqRepository", "Starting to scan pyq assets")

        // Scan /assets/pyq folder recursively
        fun scanFolder(path: String) {
            val files = assets.list(path) ?: return
            android.util.Log.d("PyqRepository", "Scanning folder: $path, files: ${files.joinToString()}")
            for (file in files) {
                val fullPath = if (path.isEmpty()) file else "$path/$file"
                if (file.endsWith(".json")) {
                    android.util.Log.d("PyqRepository", "Found json: $fullPath")
                    try {
                        val rawContent = assets.open(fullPath).bufferedReader().use { it.readText() }.trim()
                        val startIdx = rawContent.indexOf('[')
                        val endIdx = rawContent.lastIndexOf(']')
                        val cleanContent = if (startIdx != -1 && endIdx != -1 && endIdx >= startIdx) {
                            rawContent.substring(startIdx, endIdx + 1)
                        } else {
                            rawContent
                        }
                        val questions = json.decodeFromString<List<PyqQuestionItem>>(cleanContent)
                        // Normalize subject name to uppercase
                        val normalizedQuestions = questions.map { q ->
                            val normSub = when (q.subject.trim().uppercase().replace(" ", "_")) {
                                "MATH", "MATHEMATICS" -> "MATHEMATICS"
                                "HISTORY", "HIST" -> "HISTORY"
                                "LIFE_SCIENCE", "LIFESCIENCE", "LIFE_SCI" -> "LIFE_SCIENCE"
                                "PHYSICAL_SCIENCE", "PHYSICALSCIENCE", "PHY_SCI" -> "PHYSICAL_SCIENCE"
                                "GEOGRAPHY", "GEOG" -> "GEOGRAPHY"
                                "BENGALI", "BEN" -> "BENGALI"
                                "ENGLISH", "ENG" -> "ENGLISH"
                                else -> q.subject.trim().uppercase()
                            }
                            q.copy(subject = normSub)
                        }
                        android.util.Log.d("PyqRepository", "Parsed ${normalizedQuestions.size} questions from $fullPath")
                        allQuestions.addAll(normalizedQuestions)
                    } catch (e: Exception) {
                        android.util.Log.e("PyqRepository", "Error parsing $fullPath: ${e.message}")
                        e.printStackTrace()
                    }
                } else {
                    scanFolder(fullPath)
                }
            }
        }

        scanFolder("pyq")
        android.util.Log.d("PyqRepository", "Total questions loaded: ${allQuestions.size}")
        return allQuestions
    }
}
