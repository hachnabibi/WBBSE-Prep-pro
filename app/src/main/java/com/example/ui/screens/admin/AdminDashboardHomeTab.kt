package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CloudBackupState
import com.example.ui.viewmodel.CloudDatabaseStats
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.ui.viewmodel.LocalDatabaseStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class DashboardConfirmAction {
    FULL_BACKUP,
    QUESTION_SYNC
}

@Composable
fun AdminDashboardHomeTab(
    viewModel: DeshExamViewModel,
    backupState: CloudBackupState,
    isLoading: Boolean,
    isBengali: Boolean,
    onAction: (Boolean, String) -> Unit,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cloudStats by viewModel.cloudStats.collectAsState()
    var pendingAction by rememberSaveable { mutableStateOf<DashboardConfirmAction?>(null) }
    val localStats = backupState.stats
    val totalLocalItems = localStats?.totalItems() ?: 0
    val isBusy = isLoading || backupState.isBackingUp
    val scheme = MaterialTheme.colorScheme

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            DashboardIntro(isBengali = isBengali)
        }

        item {
            CloudStatusCard(
                cloudStats = cloudStats,
                localStats = localStats,
                isBengali = isBengali,
                onRefresh = {
                    viewModel.loadLocalDatabaseStats()
                    viewModel.checkCloudDatabaseIntegrity()
                }
            )
        }

        item {
            LocalStatsCard(
                stats = localStats,
                isBengali = isBengali,
                isRefreshing = backupState.isLoadingStats,
                error = backupState.statsError,
                onRefresh = { viewModel.loadLocalDatabaseStats() }
            )
        }

        item {
            BackupActionsCard(
                backupState = backupState,
                isBengali = isBengali,
                enabled = !isBusy,
                onFullBackup = { pendingAction = DashboardConfirmAction.FULL_BACKUP },
                onQuestionSync = { pendingAction = DashboardConfirmAction.QUESTION_SYNC }
            )
        }

        item {
            QuickActionsCard(
                isBengali = isBengali,
                onNavigate = onNavigate
            )
        }
    }

    pendingAction?.let { action ->
        val isFullBackup = action == DashboardConfirmAction.FULL_BACKUP
        val actionTitle = if (isFullBackup) {
            if (isBengali) "সম্পূর্ণ ব্যাকআপ নিশ্চিত করুন" else "Confirm full backup"
        } else {
            if (isBengali) "প্রশ্ন সিঙ্ক নিশ্চিত করুন" else "Confirm question sync"
        }
        val actionDescription = if (isFullBackup) {
            if (isBengali) {
                "লোকাল ডেটার $totalLocalItems টি আইটেম Firebase-এ পাঠানো হবে। আপনার সংযোগ স্থিতিশীল আছে কি না নিশ্চিত করে এগিয়ে যান।"
            } else {
                "$totalLocalItems local items will be sent to Firebase. Make sure your connection is stable before continuing."
            }
        } else {
            if (isBengali) {
                "লোকাল প্রশ্ন ব্যাংকের ${localStats?.questionCount ?: 0} টি প্রশ্ন Firebase-এ সিঙ্ক করা হবে।"
            } else {
                "${localStats?.questionCount ?: 0} local questions will be synced to Firebase."
            }
        }
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            icon = {
                Icon(
                    if (isFullBackup) Icons.Default.CloudUpload else Icons.Default.Quiz,
                    contentDescription = null,
                    tint = scheme.primary
                )
            },
            title = { Text(actionTitle, fontWeight = FontWeight.Bold) },
            text = { Text(actionDescription) },
            confirmButton = {
                Button(
                    onClick = {
                        pendingAction = null
                        if (isFullBackup) {
                            viewModel.pushLocalDatabaseToFirebase { success, message ->
                                onAction(success, message)
                            }
                        } else {
                            viewModel.forceSyncQuestionsToFirebase { success, message ->
                                onAction(success, message)
                            }
                        }
                    },
                    enabled = !isBusy,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBengali) "এগিয়ে যান" else "Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun DashboardIntro(isBengali: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = if (isBengali) "স্বাগতম, অ্যাডমিন" else "Welcome, Admin",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (isBengali) {
                "ডেটা, ক্লাউড সিঙ্ক এবং দ্রুত কাজগুলো এক জায়গায় পরিচালনা করুন।"
            } else {
                "Manage data, cloud sync, and common tasks from one place."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CloudStatusCard(
    cloudStats: CloudDatabaseStats,
    localStats: LocalDatabaseStats?,
    isBengali: Boolean,
    onRefresh: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val hasChecked = cloudStats.lastCheckedTimestamp > 0
    val statusColor = when {
        cloudStats.isChecking -> scheme.primary
        !hasChecked -> scheme.tertiary
        cloudStats.isOnline -> Color(0xFF16875B)
        else -> scheme.error
    }
    val statusLabel = when {
        cloudStats.isChecking -> if (isBengali) "যাচাই হচ্ছে" else "Checking"
        !hasChecked -> if (isBengali) "এখনও যাচাই হয়নি" else "Not checked yet"
        cloudStats.isOnline -> if (isBengali) "ক্লাউড সংযুক্ত" else "Cloud connected"
        else -> if (isBengali) "ক্লাউডে সংযোগ নেই" else "Cloud unavailable"
    }
    val cloudTotal = cloudStats.totalItems()
    val localTotal = localStats?.totalItems()
    val lastChecked = cloudStats.lastCheckedTimestamp.takeIf { it > 0 }?.let {
        formatTimestamp(it)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = when {
                            cloudStats.isChecking -> Icons.Default.CloudSync
                            cloudStats.isOnline && hasChecked -> Icons.Default.CloudDone
                            hasChecked -> Icons.Default.CloudOff
                            else -> Icons.Default.CloudSync
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.padding(11.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBengali) "ক্লাউড সংযোগ" else "Cloud connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    enabled = !cloudStats.isChecking,
                    modifier = Modifier
                        .clip(CircleShape)
                ) {
                    if (cloudStats.isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = if (isBengali) "রিফ্রেশ" else "Refresh",
                            tint = scheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.45f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CloudMetric(
                    label = if (isBengali) "ক্লাউড আইটেম" else "Cloud items",
                    value = cloudTotal.toString(),
                    modifier = Modifier.weight(1f)
                )
                CloudMetric(
                    label = if (isBengali) "লোকাল আইটেম" else "Local items",
                    value = localTotal?.toString() ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }
            if (lastChecked != null) {
                Text(
                    text = if (isBengali) "সর্বশেষ যাচাই: $lastChecked" else "Last checked: $lastChecked",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CloudMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LocalStatsCard(
    stats: LocalDatabaseStats?,
    isBengali: Boolean,
    isRefreshing: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val statsItems = remember(stats, isBengali) {
        listOf(
            StatItem(stats?.boardCount?.toString() ?: "—", if (isBengali) "বোর্ড" else "Boards", Icons.Default.AccountBalance, Color(0xFF4F46E5)),
            StatItem(stats?.classCount?.toString() ?: "—", if (isBengali) "শ্রেণি" else "Classes", Icons.Default.School, Color(0xFF059669)),
            StatItem(stats?.subjectCount?.toString() ?: "—", if (isBengali) "বিষয়" else "Subjects", Icons.Default.MenuBook, Color(0xFFD97706)),
            StatItem(stats?.chapterCount?.toString() ?: "—", if (isBengali) "অধ্যায়" else "Chapters", Icons.Default.BookmarkBorder, Color(0xFF0284C7)),
            StatItem(stats?.topicCount?.toString() ?: "—", if (isBengali) "টপিক" else "Topics", Icons.Default.Layers, Color(0xFF7C3AED)),
            StatItem(stats?.questionCount?.toString() ?: "—", if (isBengali) "প্রশ্ন" else "Questions", Icons.Default.Quiz, Color(0xFFE11D48))
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isBengali) "লোকাল ডেটাবেজ" else "Local database",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (stats == null) {
                            if (isBengali) "পরিসংখ্যান লোড হচ্ছে" else "Loading statistics"
                        } else {
                            val count = stats.totalItems()
                            if (isBengali) "মোট $count টি রেকর্ড" else "$count total records"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                if (isRefreshing && stats == null) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = if (isBengali) "পরিসংখ্যান রিফ্রেশ" else "Refresh statistics",
                                tint = scheme.primary
                            )
                        }
                    }
                }
            }

            if (error != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.errorContainer.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRefresh, enabled = !isRefreshing) {
                            Text(if (isBengali) "আবার" else "Retry")
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                statsItems.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            StatBadge(
                                count = item.count,
                                label = item.label,
                                icon = item.icon,
                                accentColor = item.color,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupActionsCard(
    backupState: CloudBackupState,
    isBengali: Boolean,
    enabled: Boolean,
    onFullBackup: () -> Unit,
    onQuestionSync: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = if (isBengali) "ব্যাকআপ ও সিঙ্ক" else "Backup & sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isBengali) {
                        "ক্লাউডে পাঠানোর আগে প্রতিটি কাজ নিশ্চিত করতে হবে।"
                    } else {
                        "Confirm each action before sending data to the cloud."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }

            val backupTimeText = if (backupState.lastBackupTimestamp > 0) {
                val formatted = formatTimestamp(backupState.lastBackupTimestamp)
                if (isBengali) "সর্বশেষ সফল ব্যাকআপ: $formatted" else "Last successful backup: $formatted"
            } else {
                if (isBengali) "এই সেশনে সফল ব্যাকআপ নেই" else "No successful backup this session"
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = scheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = scheme.primary)
                    Text(
                        backupTimeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (backupState.isBackingUp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            backupState.step,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${(backupState.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { backupState.progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                }
            } else if (backupState.error != null) {
                Text(
                    text = backupState.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.error
                )
            } else if (backupState.summary != null && backupState.lastBackupTimestamp > 0) {
                Text(
                    text = if (isBengali) "সর্বশেষ ব্যাকআপ সফল হয়েছে" else "The last backup completed successfully",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF16875B),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onFullBackup,
                enabled = enabled,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    if (backupState.isBackingUp) {
                        if (isBengali) "ব্যাকআপ চলছে..." else "Backup in progress..."
                    } else {
                        if (isBengali) "সম্পূর্ণ ডেটা ব্যাকআপ" else "Back up all data"
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onQuestionSync,
                enabled = enabled,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(if (isBengali) "শুধু প্রশ্ন ব্যাংক সিঙ্ক" else "Sync question bank only")
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    isBengali: Boolean,
    onNavigate: (Int) -> Unit
) {
    val actions = listOf(
        QuickAction(
            title = if (isBengali) "প্রশ্ন যোগ" else "Add question",
            subtitle = if (isBengali) "নতুন প্রশ্ন তৈরি করুন" else "Create a new question",
            icon = Icons.Default.Quiz,
            destination = 1
        ),
        QuickAction(
            title = if (isBengali) "কারিকুলাম" else "Curriculum",
            subtitle = if (isBengali) "বিষয় ও অধ্যায় সাজান" else "Manage subjects and chapters",
            icon = Icons.Default.MenuBook,
            destination = 7
        ),
        QuickAction(
            title = if (isBengali) "বাল্ক ইমপোর্ট" else "Bulk import",
            subtitle = if (isBengali) "একসাথে ডেটা ইমপোর্ট করুন" else "Import multiple records",
            icon = Icons.Default.FileUpload,
            destination = 6
        ),
        QuickAction(
            title = if (isBengali) "রিপোর্ট ও কনটেন্ট অডিট" else "Reports & content audit",
            subtitle = if (isBengali) "রিপোর্ট ও কনটেন্টের মান দেখুন" else "Review reports and content quality",
            icon = Icons.Default.FactCheck,
            destination = 10
        )
    )
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isBengali) "দ্রুত কাজ" else "Quick actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            actions.forEachIndexed { index, action ->
                QuickActionRow(action = action, onClick = { onNavigate(action.destination) })
                if (index < actions.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionRow(action: QuickAction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                action.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(action.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(action.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatBadge(
    count: String,
    label: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            Text(
                text = count,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = accentColor,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class StatItem(
    val count: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private data class QuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val destination: Int
)

private fun LocalDatabaseStats.totalItems(): Int =
    boardCount + classCount + subjectCount + chapterCount + topicCount + questionCount

private fun CloudDatabaseStats.totalItems(): Int =
    boardCount + classCount + subjectCount + chapterCount + topicCount + questionCount + testCount

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
