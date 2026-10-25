package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.DeshExamViewModel

/**
 * Curriculum manager for subjects, chapters, topics, education boards, and academic classes.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminCurriculumScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean = true,
    onShowMessage: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by rememberSaveable { mutableIntStateOf(0) }
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.55f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.padding(11.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBengali) "কারিকুলাম ম্যানেজার" else "Curriculum Manager",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Spacer(Modifier.size(2.dp))
                    Text(
                        text = if (isBengali) {
                            "বিষয়, অধ্যায়, পাঠ ও শিক্ষা পর্ষদ"
                        } else {
                            "Manage subjects, lessons, and education boards"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = scheme.primary.copy(alpha = 0.09f)
                ) {
                    Text(
                        text = if (isBengali) "অ্যাডমিন" else "ADMIN",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = scheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = scheme.surfaceVariant.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryTabRow(
                selectedTabIndex = subTab,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = scheme.primary,
                divider = {},
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = subTab == 0,
                    onClick = { subTab = 0 },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = {
                        Text(
                            text = if (isBengali) "বিষয় ও পাঠ" else "Subjects & Lessons",
                            fontWeight = if (subTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
                Tab(
                    selected = subTab == 1,
                    onClick = { subTab = 1 },
                    icon = {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = {
                        Text(
                            text = if (isBengali) "বোর্ড ও শ্রেণি" else "Boards & Classes",
                            fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        when (subTab) {
            0 -> AdminCurriculumCRUDTab(
                viewModel = viewModel,
                isBengali = isBengali,
                onShowMessage = onShowMessage,
                modifier = Modifier.weight(1f)
            )

            1 -> AdminBoardClassTab(
                viewModel = viewModel,
                isBengali = isBengali,
                onShowMessage = onShowMessage,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
