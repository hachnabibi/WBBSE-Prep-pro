package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.SubjectCode

/**
 * Provides unique SVG / Vector Icons for every WBBSE & WBCHSE subject in the curriculum list.
 */
object SubjectSvgIconProvider {

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    fun getIconForSubjectCode(code: SubjectCode): ImageVector {
        return when (code) {
            SubjectCode.PHYSICAL_SCIENCE -> Icons.Default.Science
            SubjectCode.LIFE_SCIENCE -> Icons.Default.Biotech
            SubjectCode.MATHEMATICS -> Icons.Default.Calculate
            SubjectCode.HISTORY -> Icons.Default.HistoryEdu
            SubjectCode.GEOGRAPHY -> Icons.Default.Public
            SubjectCode.BENGALI -> Icons.AutoMirrored.Filled.MenuBook
            SubjectCode.BENGALI_RAPID -> Icons.Default.Book
            SubjectCode.ENGLISH -> Icons.Default.Translate
            SubjectCode.ENGLISH_RAPID -> Icons.Default.Book
            SubjectCode.BENGALI_GRAMMAR -> Icons.Default.Spellcheck
            SubjectCode.ENGLISH_GRAMMAR -> Icons.Default.FontDownload
            SubjectCode.PHYSICS -> Icons.Default.ElectricBolt
            SubjectCode.CHEMISTRY -> Icons.Default.Science
            SubjectCode.BIOLOGY -> Icons.Default.Biotech
            SubjectCode.POL_SCI -> Icons.Default.AccountBalance
            SubjectCode.ECONOMICS -> Icons.AutoMirrored.Filled.TrendingUp
            SubjectCode.PHILOSOPHY -> Icons.Default.Psychology
            SubjectCode.ACCOUNTANCY -> Icons.AutoMirrored.Filled.ReceiptLong
            SubjectCode.BUSINESS_STUDIES -> Icons.Default.BusinessCenter
            SubjectCode.COMPUTER_SCIENCE -> Icons.Default.LaptopMac
            SubjectCode.EDUCATION -> Icons.AutoMirrored.Filled.MenuBook
            SubjectCode.NUTRITION -> Icons.Default.LocalDining
            SubjectCode.JOURNALISM -> Icons.Default.Campaign
            SubjectCode.SANSKRIT -> Icons.Default.Language
            SubjectCode.PERSIAN -> Icons.Default.Language
            SubjectCode.ARABIC -> Icons.Default.Language
            SubjectCode.AGRICULTURE -> Icons.Default.Agriculture
            SubjectCode.BASIC_MATH -> Icons.Default.Calculate
            SubjectCode.ANTHROPOLOGY -> Icons.Default.NaturePeople
            SubjectCode.SCIENCE_OF_WELL_BEING -> Icons.Default.SelfImprovement
            SubjectCode.APPLIED_AI -> Icons.Default.SmartToy
            SubjectCode.SOCIOLOGY -> Icons.Default.Groups
            SubjectCode.FISHERIES -> Icons.Default.SetMeal
            SubjectCode.PSYCHOLOGY -> Icons.Default.Psychology
            SubjectCode.STATISTICS -> Icons.Default.BarChart
            SubjectCode.HUMAN_DEVELOPMENT -> Icons.Default.EmojiPeople
            SubjectCode.MUSIC -> Icons.Default.MusicNote
            SubjectCode.VISUAL_ARTS -> Icons.Default.Palette
            SubjectCode.HEALTH_PHY_ED -> Icons.Default.SportsHandball
            SubjectCode.MODERN_COMP_APP -> Icons.Default.Computer
            SubjectCode.ENVIRONMENT_STUDIES -> Icons.Default.Eco
            SubjectCode.ENVIRONMENTAL_SCIENCE -> Icons.Default.Eco
            SubjectCode.CYBER_SECURITY -> Icons.Default.Security
            SubjectCode.AI_DATA_SCIENCE -> Icons.Default.Insights
            SubjectCode.COMMERCIAL_LAW_AUDITING -> Icons.Default.Gavel
            SubjectCode.COSTING_TAXATION -> Icons.Default.RequestQuote
            SubjectCode.BUSINESS_MATH_STATISTICS -> Icons.Default.Calculate
            SubjectCode.VOCATIONAL -> Icons.Default.Build
            else -> getIconForSubjectId(code.id)
        }
    }

    fun getIconForSubjectId(subjectId: String): ImageVector {
        val lower = subjectId.lowercase()
        return when {
            lower.contains("phy_sci") || lower.contains("physci") || lower.contains("physical") -> Icons.Default.Science
            lower.contains("life_sci") || lower.contains("lifesci") || lower.contains("life") || lower.contains("bio") -> Icons.Default.Biotech
            lower.contains("math") || lower.contains("গণিত") -> Icons.Default.Calculate
            lower.contains("hist") || lower.contains(" history") || lower.contains("ইতিহাস") -> Icons.Default.HistoryEdu
            lower.contains("geog") || lower.contains("geography") || lower.contains("ভূগোল") -> Icons.Default.Public
            lower.contains("ben_gram") || lower.contains("bengaligrammar") -> Icons.Default.Spellcheck
            lower.contains("eng_gram") || lower.contains("englishgrammar") -> Icons.Default.FontDownload
            lower.contains("bengali") || lower.contains("ben") || lower.contains("বাংলা") -> Icons.AutoMirrored.Filled.MenuBook
            lower.contains("english") || lower.contains("eng") || lower.contains("ইংরেজি") -> Icons.Default.Translate
            lower.contains("physics") -> Icons.Default.ElectricBolt
            lower.contains("chemistry") -> Icons.Default.Science
            lower.contains("pol_sci") || lower.contains("pol") -> Icons.Default.AccountBalance
            lower.contains("econ") -> Icons.AutoMirrored.Filled.TrendingUp
            lower.contains("phil") -> Icons.Default.Psychology
            lower.contains("acct") || lower.contains("account") -> Icons.AutoMirrored.Filled.ReceiptLong
            lower.contains("bst") || lower.contains("business") -> Icons.Default.BusinessCenter
            lower.contains("comp") || lower.contains("computer") -> Icons.Default.LaptopMac
            else -> Icons.AutoMirrored.Filled.MenuBook
        }
    }
}

/**
 * Reusable Subject Icon Badge component with custom background tinting and unique vector icon.
 */
@Composable
fun SubjectIconBadge(
    subjectCode: SubjectCode,
    colorHex: Long = subjectCode.colorHex,
    size: Dp = 46.dp,
    iconSize: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val iconVector = SubjectSvgIconProvider.getIconForSubjectCode(subjectCode)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(colorHex).copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = subjectCode.name,
            tint = Color(colorHex),
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun SubjectIconBadgeById(
    subjectId: String,
    colorHex: Long,
    size: Dp = 46.dp,
    iconSize: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val iconVector = SubjectSvgIconProvider.getIconForSubjectId(subjectId)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(colorHex).copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = subjectId,
            tint = Color(colorHex),
            modifier = Modifier.size(iconSize)
        )
    }
}
