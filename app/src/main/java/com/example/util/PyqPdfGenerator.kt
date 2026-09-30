package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.ui.screens.PyqQuestionItem
import java.io.File
import java.io.FileOutputStream

object PyqPdfGenerator {

    /**
     * Generates a stylized, official-format Madhyamik Question Paper PDF and returns the file.
     */
    fun generatePyqPdf(
        context: Context,
        year: Int,
        subjectCode: String,
        subjectNameBengali: String,
        subjectNameEnglish: String,
        questions: List<PyqQuestionItem>,
        includeAnswers: Boolean = true
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E1B4B") // Dark Indigo
            textSize = 15f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val boardHeaderPaint = Paint().apply {
            color = Color.parseColor("#4338CA")
            textSize = 12f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.parseColor("#374151")
            textSize = 9.5f
            isAntiAlias = true
        }

        val metaBoldPaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val groupHeaderPaint = Paint().apply {
            color = Color.parseColor("#1D4ED8")
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val questionPaint = Paint().apply {
            color = Color.parseColor("#1F2937")
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#374151")
            textSize = 9f
            isAntiAlias = true
        }

        val answerPaint = Paint().apply {
            color = Color.parseColor("#047857") // Emerald
            textSize = 8.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#D1D5DB")
            strokeWidth = 1f
        }

        val groupBgPaint = Paint().apply {
            color = Color.parseColor("#EEF2FF")
            style = Paint.Style.FILL
        }

        var currentPageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = 36f

        fun drawHeader() {
            canvas.drawText("WEST BENGAL BOARD OF SECONDARY EDUCATION", pageWidth / 2f, yPos, boardHeaderPaint)
            yPos += 16f
            canvas.drawText("পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ — মাধ্যমিক পরীক্ষা $year", pageWidth / 2f, yPos, titlePaint)
            yPos += 15f
            val subHeaderTitle = if (includeAnswers) {
                "$subjectNameBengali ($subjectNameEnglish) — প্রশ্নপত্র ও মডেল উত্তর"
            } else {
                "$subjectNameBengali ($subjectNameEnglish) — মূল পশ্চিমবঙ্গ পর্ষদ প্রশ্নপত্র"
            }
            canvas.drawText(subHeaderTitle, pageWidth / 2f, yPos, boardHeaderPaint)
            yPos += 14f

            // Metadata row
            canvas.drawLine(30f, yPos, (pageWidth - 30).toFloat(), yPos, dividerPaint)
            yPos += 14f

            canvas.drawText("সময় : ৩ ঘণ্টা ১৫ মিনিট", 36f, yPos, metaPaint)
            val marksText = "পূর্ণমান : ৯০ নম্বর"
            canvas.drawText(marksText, (pageWidth - 36 - metaBoldPaint.measureText(marksText)), yPos, metaBoldPaint)
            yPos += 8f
            canvas.drawLine(30f, yPos, (pageWidth - 30).toFloat(), yPos, dividerPaint)
            yPos += 18f
        }

        drawHeader()

        fun checkPageBreak(requiredSpace: Float) {
            if (yPos + requiredSpace > pageHeight - 40f) {
                // Draw footer on current page
                canvas.drawText("পৃষ্ঠা $currentPageNumber • Desh Exam Madhyamik PYQ Archive", pageWidth / 2f, pageHeight - 20f, metaPaint.apply { textAlign = Paint.Align.CENTER })
                pdfDocument.finishPage(page)

                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 36f

                // Mini header for continuation pages
                canvas.drawText("মাধ্যমিক পরীক্ষা $year — $subjectNameBengali (পৃষ্ঠা $currentPageNumber)", pageWidth / 2f, yPos, titlePaint.apply { textSize = 11f })
                yPos += 10f
                canvas.drawLine(30f, yPos, (pageWidth - 30).toFloat(), yPos, dividerPaint)
                yPos += 16f
            }
        }

        // Group questions
        val grouped = questions.groupBy { it.group.ifBlank { "বিভাগ 'ক' (প্রশ্নাবলি)" } }

        if (grouped.isEmpty()) {
            checkPageBreak(60f)
            canvas.drawText("এই বিষয়ের $year সালের প্রশ্নপত্র সংকলন ও প্রস্তুত হচ্ছে...", 40f, yPos, questionPaint)
            yPos += 20f
        }

        // Helper function to draw multi-line wrapped text and automatically check page break
        fun drawWrappedBlock(
            text: String,
            startX: Float,
            maxWidth: Float,
            paint: Paint,
            lineHeight: Float
        ) {
            val words = text.replace("\r", "").split(" ")
            var currentLine = ""
            for (word in words) {
                if (word.contains("\n")) {
                    val subWords = word.split("\n")
                    for (i in subWords.indices) {
                        val part = subWords[i]
                        val testLine = if (currentLine.isEmpty()) part else "$currentLine $part"
                        if (paint.measureText(testLine) < maxWidth) {
                            currentLine = testLine
                        } else {
                            if (currentLine.isNotEmpty()) {
                                checkPageBreak(lineHeight + 4f)
                                canvas.drawText(currentLine, startX, yPos, paint)
                                yPos += lineHeight
                            }
                            currentLine = part
                        }
                        if (i < subWords.size - 1) {
                            if (currentLine.isNotEmpty()) {
                                checkPageBreak(lineHeight + 4f)
                                canvas.drawText(currentLine, startX, yPos, paint)
                                yPos += lineHeight
                                currentLine = ""
                            }
                        }
                    }
                } else {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (paint.measureText(testLine) < maxWidth) {
                        currentLine = testLine
                    } else {
                        if (currentLine.isNotEmpty()) {
                            checkPageBreak(lineHeight + 4f)
                            canvas.drawText(currentLine, startX, yPos, paint)
                            yPos += lineHeight
                        }
                        currentLine = word
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                checkPageBreak(lineHeight + 4f)
                canvas.drawText(currentLine, startX, yPos, paint)
                yPos += lineHeight
            }
        }

        for ((groupName, qList) in grouped) {
            checkPageBreak(40f)
            // Group Header Banner
            canvas.drawRect(30f, yPos - 12f, (pageWidth - 30).toFloat(), yPos + 10f, groupBgPaint)
            canvas.drawText("■ $groupName", 38f, yPos + 4f, groupHeaderPaint)
            yPos += 22f

            for ((index, q) in qList.withIndex()) {
                checkPageBreak(40f)

                // Marks indicator on the right
                val marksLabel = "[${q.marks}]"
                canvas.drawText(marksLabel, pageWidth - 60f, yPos, metaBoldPaint)

                // Question text with number
                val qNumberText = "${index + 1}. "
                val fullQuestionText = "$qNumberText${q.question.trim()}"
                drawWrappedBlock(
                    text = fullQuestionText,
                    startX = 36f,
                    maxWidth = pageWidth - 105f,
                    paint = questionPaint,
                    lineHeight = 13f
                )

                // Options if present (MCQ)
                if (!q.options.isNullOrEmpty()) {
                    val optPrefixes = listOf("(ক) ", "(খ) ", "(গ) ", "(ঘ) ")
                    var optLine = ""
                    for (i in q.options.indices) {
                        val prefix = optPrefixes.getOrElse(i) { "(${i + 1}) " }
                        optLine += prefix + q.options[i] + "    "
                    }
                    drawWrappedBlock(
                        text = optLine,
                        startX = 46f,
                        maxWidth = pageWidth - 80f,
                        paint = bodyPaint,
                        lineHeight = 12f
                    )
                }

                // Complete Official Answer & Solution (No truncation!) - Only when includeAnswers is true
                if (includeAnswers && q.officialAnswerBengali.isNotBlank()) {
                    val ansText = "✓ উত্তর: ${q.officialAnswerBengali.trim()}"
                    drawWrappedBlock(
                        text = ansText,
                        startX = 46f,
                        maxWidth = pageWidth - 80f,
                        paint = answerPaint,
                        lineHeight = 12f
                    )
                }

                // Complete Key Tips (No truncation!) - Only when includeAnswers is true
                if (includeAnswers && q.keyTips.isNotBlank()) {
                    val tipText = "• টিপস: ${q.keyTips.trim()}"
                    drawWrappedBlock(
                        text = tipText,
                        startX = 46f,
                        maxWidth = pageWidth - 80f,
                        paint = bodyPaint,
                        lineHeight = 11.5f
                    )
                }

                yPos += 5f
                canvas.drawLine(36f, yPos, (pageWidth - 36).toFloat(), yPos, dividerPaint.apply { strokeWidth = 0.5f })
                yPos += 8f
            }
        }

        // Draw footer on last page
        canvas.drawText("পৃষ্ঠা $currentPageNumber • Desh Exam Madhyamik PYQ Archive", pageWidth / 2f, pageHeight - 20f, metaPaint.apply { textAlign = Paint.Align.CENTER })
        pdfDocument.finishPage(page)

        // Write to application cache
        val outputDir = File(context.cacheDir, "pyq_pdfs")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val safeSubject = subjectCode.lowercase().replace(" ", "_")
        val outputFile = File(outputDir, "Madhyamik_${year}_${safeSubject}.pdf")

        return try {
            val fos = FileOutputStream(outputFile)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Opens the generated PDF file using standard Android Intent.
     */
    fun openPdfFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooser = Intent.createChooser(intent, "পিডিএফ ওপেন করুন")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "পিডিএফ ফাইলটি সেভ হয়েছে: ${file.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
