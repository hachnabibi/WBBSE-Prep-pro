package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SubjectEntity

fun getAdminClassSubjects(classId: String): List<SubjectEntity> {
    return when {
        classId.contains("wbchse") || classId.contains("class_11") || classId.contains("class_12") -> listOf(
            SubjectEntity("${classId}_bengali", "bengali", "বাংলা (Bengali)", "Bengali", 10, 0, 0, 0xFFE11D48L, classId, "common"),
            SubjectEntity("${classId}_english", "english", "ইংরেজি (English)", "English", 10, 0, 0, 0xFF2563EBL, classId, "common"),
            SubjectEntity("${classId}_physics", "physics", "পদার্থবিদ্যা (Physics)", "Physics", 10, 0, 0, 0xFF7C3AEDL, classId, "science"),
            SubjectEntity("${classId}_chemistry", "chemistry", "রসায়ন (Chemistry)", "Chemistry", 10, 0, 0, 0xFF059669L, classId, "science"),
            SubjectEntity("${classId}_mathematics", "mathematics", "গণিত (Mathematics)", "Mathematics", 10, 0, 0, 0xFFD97706L, classId, "science"),
            SubjectEntity("${classId}_biology", "biology", "জীববিজ্ঞান (Biological Science)", "Biology", 10, 0, 0, 0xFF16A34AL, classId, "science"),
            SubjectEntity("${classId}_history", "history", "ইতিহাস (History)", "History", 10, 0, 0, 0xFFD97706L, classId, "arts"),
            SubjectEntity("${classId}_geography", "geography", "ভূগোল (Geography)", "Geography", 10, 0, 0, 0xFF0284C7L, classId, "arts"),
            SubjectEntity("${classId}_pol_science", "political_science", "রাষ্ট্রবিজ্ঞান (Political Science)", "Political Science", 10, 0, 0, 0xFF8B5CF6L, classId, "arts"),
            SubjectEntity("${classId}_accountancy", "accountancy", "হিসাবশাস্ত্র (Accountancy)", "Accountancy", 10, 0, 0, 0xFF0D9488L, classId, "commerce")
        )
        classId.contains("wbbpe") -> listOf(
            SubjectEntity("${classId}_bengali", "bengali", "আমার বই / পাতাবাহার (বাংলা)", "Bengali", 5, 0, 0, 0xFFE11D48L, classId, "common"),
            SubjectEntity("${classId}_english", "english", "Butterfly / English", "English", 5, 0, 0, 0xFF2563EBL, classId, "common"),
            SubjectEntity("${classId}_math", "mathematics", "আমার গণিত (Mathematics)", "Mathematics", 5, 0, 0, 0xFF059669L, classId, "common"),
            SubjectEntity("${classId}_environment", "environment", "আমাদের পরিবেশ (Environment)", "Environment", 5, 0, 0, 0xFF16A34AL, classId, "common")
        )
        else -> listOf(
            SubjectEntity("${classId}_bengali", "bengali", "প্রথম ভাষা বাংলা (Bengali)", "Bengali", 10, 0, 0, 0xFFE11D48L, classId, "common"),
            SubjectEntity("${classId}_english", "english", "দ্বিতীয় ভাষা ইংরেজি (English)", "English", 10, 0, 0, 0xFF2563EBL, classId, "common"),
            SubjectEntity("${classId}_mathematics", "mathematics", "গণিত (Mathematics)", "Mathematics", 10, 0, 0, 0xFF059669L, classId, "common"),
            SubjectEntity("${classId}_physical_science", "physical_science", "ভৌত বিজ্ঞান (Physical Science)", "Physical Science", 10, 0, 0, 0xFF7C3AEDL, classId, "common"),
            SubjectEntity("${classId}_life_science", "life_science", "জীবন বিজ্ঞান (Life Science)", "Life Science", 10, 0, 0, 0xFF16A34AL, classId, "common"),
            SubjectEntity("${classId}_history", "history", "ইতিহাস (History)", "History", 10, 0, 0, 0xFFD97706L, classId, "common"),
            SubjectEntity("${classId}_geography", "geography", "ভূগোল (Geography)", "Geography", 10, 0, 0, 0xFF0284C7L, classId, "common")
        )
    }
}

@Composable
fun AdminStatGradientBadge(
    count: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    borderColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.35f)),
        modifier = modifier
            .background(Brush.verticalGradient(gradientColors), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = count,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = accentColor,
                    maxLines = 1
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
