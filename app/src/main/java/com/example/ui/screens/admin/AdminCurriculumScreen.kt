package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.DeshExamViewModel

/**
 * ট্যাব ৭: কারিকুলাম ম্যানেজার (Curriculum Manager)
 * সাব-ট্যাব ০: বিষয়, অধ্যায় ও পাঠ (Subject, Chapter, Topic CRUD)
 * সাব-ট্যাব ১: বোর্ড ও ক্লাস ব্যবস্থাপনা (Education Boards & Academic Classes)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCurriculumScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean = true,
    onShowMessage: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sub-tabs navigation
        PrimaryTabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = {
                    Text(
                        text = if (isBengali) "বিষয়, অধ্যায় ও পাঠ" else "Subjects & Chapters",
                        fontWeight = if (subTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = {
                    Text(
                        text = if (isBengali) "বোর্ড ও শ্রেণি" else "Boards & Classes",
                        fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            )
        }

        // SubTab content
        when (subTab) {
            0 -> {
                AdminCurriculumCRUDTab(
                    viewModel = viewModel,
                    isBengali = isBengali,
                    onShowMessage = onShowMessage,
                    modifier = Modifier.weight(1f)
                )
            }
            1 -> {
                AdminBoardClassTab(
                    viewModel = viewModel,
                    isBengali = isBengali,
                    onShowMessage = onShowMessage,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
