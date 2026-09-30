package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.StudentProfile
import com.example.data.model.SubjectItem
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    fun generateProgressPdf(
        context: Context,
        profile: StudentProfile,
        subjects: List<SubjectItem>,
        isBengali: Boolean
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#4F46E5") // PrimaryIndigo
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#4B5563")
            textSize = 12f
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E1B4B")
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val boxPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#F3F4F6")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#D1D5DB")
            strokeWidth = 1f
            isAntiAlias = true
        }

        var yPos = 50f
        val leftMargin = 40f
        val pageWidth = 515f

        // Title Header
        canvas.drawText("WBBSE Exam Prep - Student Performance Report", leftMargin, yPos, titlePaint)
        yPos += 22f
        canvas.drawText("Generated on: ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}", leftMargin, yPos, subtitlePaint)
        yPos += 20f

        canvas.drawLine(leftMargin, yPos, leftMargin + pageWidth, yPos, linePaint)
        yPos += 25f

        // Student Info Box
        canvas.drawRoundRect(leftMargin, yPos, leftMargin + pageWidth, yPos + 80f, 8f, 8f, boxPaint)
        val infoX = leftMargin + 15f
        var infoY = yPos + 22f
        
        canvas.drawText("Student Name: ${profile.name}", infoX, infoY, headerPaint)
        infoY += 20f
        canvas.drawText("School: ${profile.school}", infoX, infoY, paint)
        infoY += 18f
        canvas.drawText("Target Exam: ${profile.targetExam}  |  League: ${profile.league}  |  Level: ${profile.level}", infoX, infoY, subtitlePaint)

        yPos += 105f

        // KPI Summary Header
        canvas.drawText("Performance Summary", leftMargin, yPos, headerPaint)
        yPos += 20f

        // 4 KPI Stats in a row
        val colWidth = pageWidth / 4f
        val kpis = listOf(
            "Solved" to "${profile.totalQuestionsAnswered}",
            "Accuracy" to "${profile.overallAccuracy}%",
            "Readiness" to "${profile.examReadinessPercentage}%",
            "Study Days" to "${profile.totalStudyDays} Days"
        )

        kpis.forEachIndexed { index, (label, valStr) ->
            val x = leftMargin + (index * colWidth) + 5f
            canvas.drawRect(x, yPos, x + colWidth - 10f, yPos + 50f, boxPaint)
            
            val valPaint = Paint(headerPaint).apply { textSize = 14f; color = android.graphics.Color.parseColor("#4F46E5") }
            val lblPaint = Paint(subtitlePaint).apply { textSize = 10f }
            
            canvas.drawText(valStr, x + 10f, yPos + 22f, valPaint)
            canvas.drawText(label, x + 10f, yPos + 38f, lblPaint)
        }

        yPos += 70f

        // Subject Mastery Matrix Header
        canvas.drawText("Subject-wise Mastery Breakdown", leftMargin, yPos, headerPaint)
        yPos += 25f

        subjects.forEach { subject ->
            canvas.drawText(subject.name, leftMargin, yPos, paint)
            canvas.drawText("${subject.masteryPercentage}%", leftMargin + 380f, yPos, headerPaint)
            
            // Progress Bar background
            canvas.drawRect(leftMargin, yPos + 6f, leftMargin + 360f, yPos + 16f, boxPaint)
            // Progress bar fill
            val fillWidth = 360f * (subject.masteryPercentage.toFloat() / 100f)
            val fillPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#10B981")
                style = Paint.Style.FILL
            }
            canvas.drawRect(leftMargin, yPos + 6f, leftMargin + fillWidth, yPos + 16f, fillPaint)

            yPos += 32f
        }

        yPos += 20f
        canvas.drawLine(leftMargin, yPos, leftMargin + pageWidth, yPos, linePaint)
        yPos += 30f

        // Footer note
        val footerPaint = Paint(subtitlePaint).apply { textSize = 10f; color = android.graphics.Color.parseColor("#9CA3AF") }
        canvas.drawText("This report is generated automatically by WBBSE Exam Prep App for parents & teachers.", leftMargin, yPos, footerPaint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "WBBSE_Student_Report_${System.currentTimeMillis()}.pdf")
        return try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun sharePdf(context: Context, pdfFile: File, isBengali: Boolean) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "ছাত্র-ছাত্রীর প্রোগ্রেস রিপোর্ট (WBBSE)")
            putExtra(Intent.EXTRA_TEXT, "এখানে আমার WBBSE প্রস্তুতি এবং টেস্ট স্কোর রিপোর্ট দেওয়া হলো।")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "রিপোর্ট শেয়ার করুন")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
