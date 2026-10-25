package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firestore.ContentQualityIssue
import com.example.data.firestore.ContentReport
import com.example.data.firestore.AdminActivityEvent
import com.example.ui.viewmodel.DeshExamViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val reportStatuses = listOf("open", "in_review", "resolved", "dismissed", "all")

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminGovernanceScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onAction: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.adminGovernanceState.collectAsState()
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }
    var selectedReportFilter by rememberSaveable { mutableStateOf("open") }
    var editingReport by remember { mutableStateOf<ContentReport?>(null) }
    var adminNote by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(selectedSection) {
        when (selectedSection) {
            0 -> viewModel.refreshAdminContentReports()
            1 -> viewModel.scanLocalContentQuality()
            2 -> viewModel.refreshAdminActivityLog()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBengali) "কনটেন্ট ও প্রশাসন" else "Content & governance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isBengali) {
                        "রিপোর্ট পর্যালোচনা করুন, কনটেন্টের সমস্যা খুঁজুন এবং অ্যাডমিন পরিবর্তন অনুসরণ করুন।"
                    } else {
                        "Review reports, find content issues, and track admin changes."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PrimaryTabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    GovernanceTab(
                        selected = selectedSection == 0,
                        label = if (isBengali) "রিপোর্ট" else "Reports",
                        icon = Icons.Default.Flag,
                        onClick = { selectedSection = 0 }
                    )
                    GovernanceTab(
                        selected = selectedSection == 1,
                        label = if (isBengali) "কনটেন্ট যাচাই" else "Quality",
                        icon = Icons.Default.FactCheck,
                        onClick = { selectedSection = 1 }
                    )
                    GovernanceTab(
                        selected = selectedSection == 2,
                        label = if (isBengali) "অ্যাক্টিভিটি" else "Activity",
                        icon = Icons.Default.History,
                        onClick = { selectedSection = 2 }
                    )
                }
            }
        }

        when (selectedSection) {
            0 -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(reportStatuses) { status ->
                        FilterChip(
                            selected = selectedReportFilter == status,
                            onClick = { selectedReportFilter = status },
                            label = {
                                Text(
                                    text = statusLabel(status, isBengali),
                                    maxLines = 1,
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }
                val reports = remember(state.reports, selectedReportFilter) {
                    if (selectedReportFilter == "all") state.reports
                    else state.reports.filter { it.status == selectedReportFilter }
                }
                GovernanceList(
                    isLoading = state.isLoadingReports,
                    error = state.reportsError,
                    emptyText = if (isBengali) "এই অবস্থার কোনো রিপোর্ট নেই" else "No reports in this state",
                    isEmpty = reports.isEmpty(),
                    onRetry = { viewModel.refreshAdminContentReports() }
                ) {
                    items(reports, key = ContentReport::id) { report ->
                        ContentReportCard(
                            report = report,
                            isBengali = isBengali,
                            onSetStatus = { status ->
                                viewModel.updateAdminContentReport(report.id, status, report.adminNote) { success, message ->
                                    onAction(success, message)
                                }
                            },
                            onEditNote = {
                                adminNote = report.adminNote
                                editingReport = report
                            }
                        )
                    }
                }
            }

            1 -> {
                val issues = state.qualityIssues
                val highCount = remember(issues) { issues.count { it.severity == "high" } }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeveritySummary(
                        count = highCount,
                        label = if (isBengali) "জরুরি" else "High",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    SeveritySummary(
                        count = issues.count { it.severity == "medium" },
                        label = if (isBengali) "পর্যালোচনা" else "Review",
                        color = Color(0xFFB36A00),
                        modifier = Modifier.weight(1f)
                    )
                    SeveritySummary(
                        count = issues.count { it.severity == "low" },
                        label = if (isBengali) "উন্নত করুন" else "Improve",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.scanLocalContentQuality() },
                        enabled = !state.isScanningQuality
                    ) {
                        if (state.isScanningQuality) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = if (isBengali) "আবার স্ক্যান" else "Run scan")
                        }
                    }
                }
                GovernanceList(
                    isLoading = state.isScanningQuality,
                    error = state.qualityError,
                    emptyText = if (isBengali) "কোনো সমস্যা পাওয়া যায়নি" else "No content issues found",
                    isEmpty = issues.isEmpty(),
                    onRetry = { viewModel.scanLocalContentQuality() }
                ) {
                    items(issues, key = { "${it.entityType}:${it.entityId}:${it.category}" }) { issue ->
                        QualityIssueCard(issue = issue, isBengali = isBengali)
                    }
                }
            }

            2 -> GovernanceList(
                isLoading = state.isLoadingActivities,
                error = state.activitiesError,
                emptyText = if (isBengali) {
                    "এখনও কোনো অ্যাডমিন পরিবর্তন রেকর্ড হয়নি"
                } else {
                    "No admin changes have been recorded yet"
                },
                isEmpty = state.activities.isEmpty(),
                onRetry = { viewModel.refreshAdminActivityLog() }
            ) {
                items(state.activities, key = AdminActivityEvent::id) { activity ->
                    ActivityCard(activity = activity, isBengali = isBengali)
                }
            }
        }
    }

    editingReport?.let { report ->
        AlertDialog(
            onDismissRequest = { editingReport = null },
            title = {
                Text(
                    if (isBengali) "অ্যাডমিন নোট" else "Admin note",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = adminNote,
                    onValueChange = { if (it.length <= 1000) adminNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    label = { Text(if (isBengali) "অভ্যন্তরীণ নোট" else "Internal note") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAdminContentReport(report.id, report.status, adminNote) { success, message ->
                            onAction(success, message)
                            if (success) editingReport = null
                        }
                    }
                ) {
                    Text(if (isBengali) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingReport = null }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun GovernanceTab(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp)) },
        text = { Text(label, fontSize = 11.sp, maxLines = 1) }
    )
}

@Composable
private fun GovernanceList(
    isLoading: Boolean,
    error: String?,
    emptyText: String,
    isEmpty: Boolean,
    onRetry: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    if (isLoading && error == null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator()
            Text(emptyText, style = MaterialTheme.typography.bodySmall)
        }
    } else if (error != null) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                OutlinedButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            content = {
                if (isEmpty) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = emptyText,
                                modifier = Modifier.padding(20.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    content()
                }
            }
        )
    }
}

@Composable
private fun ContentReportCard(
    report: ContentReport,
    isBengali: Boolean,
    onSetStatus: (String) -> Unit,
    onEditNote: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    statusLabel(report.status, isBengali),
                    color = statusColor(report.status),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    formatGovernanceTimestamp(report.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                report.reason,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                report.questionText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${report.reporterName.ifBlank { report.reporterUid }} • ${report.questionId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (report.details.isNotBlank()) {
                Text(report.details, style = MaterialTheme.typography.bodySmall)
            }
            if (report.adminNote.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = "${if (isBengali) "নোট" else "Note"}: ${report.adminNote}",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEditNote,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(if (isBengali) "নোট" else "Note", fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                if (report.status != "in_review" && report.status != "resolved") {
                    TextButton(onClick = { onSetStatus("in_review") }) {
                        Text(if (isBengali) "পর্যালোচনা" else "Review", fontSize = 11.sp)
                    }
                }
                if (report.status != "resolved") {
                    Button(
                        onClick = { onSetStatus("resolved") },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (isBengali) "সমাধান" else "Resolve", fontSize = 11.sp)
                    }
                }
                if (report.status != "dismissed") {
                    TextButton(onClick = { onSetStatus("dismissed") }) {
                        Text(if (isBengali) "বাদ" else "Dismiss", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun QualityIssueCard(issue: ContentQualityIssue, isBengali: Boolean) {
    val issueColor = when (issue.severity) {
        "high" -> MaterialTheme.colorScheme.error
        "medium" -> Color(0xFFB36A00)
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, issueColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                if (issue.severity == "high") Icons.Default.Warning else Icons.Default.FactCheck,
                contentDescription = null,
                tint = issueColor,
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isBengali) localizedIssueCategory(issue.category) else issue.category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(issue.description, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${issue.entityType}: ${issue.entityId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: AdminActivityEvent, isBengali: Boolean) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(activity.summary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${activity.actorName.ifBlank { activity.actorUid }} • ${activity.entityType}: ${activity.entityId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatGovernanceTimestamp(activity.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SeveritySummary(count: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count.toString(), fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, maxLines = 1)
        }
    }
}

private fun statusLabel(status: String, isBengali: Boolean): String = when (status) {
    "open" -> if (isBengali) "নতুন" else "Open"
    "in_review" -> if (isBengali) "পর্যালোচনাধীন" else "In review"
    "resolved" -> if (isBengali) "সমাধান" else "Resolved"
    "dismissed" -> if (isBengali) "বাতিল" else "Dismissed"
    "all" -> if (isBengali) "সব" else "All"
    else -> status
}

private fun statusColor(status: String): Color = when (status) {
    "resolved" -> Color(0xFF16875B)
    "dismissed" -> Color(0xFF697386)
    "in_review" -> Color(0xFFB36A00)
    else -> Color(0xFF2563EB)
}

private fun formatGovernanceTimestamp(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))

private fun localizedIssueCategory(category: String): String = when (category) {
    "Missing titles" -> "শিরোনাম নেই"
    "Empty chapters" -> "অধ্যায়ে টপিক নেই"
    "Broken curriculum links" -> "কারিকুলাম লিংক ভাঙা"
    "Thin topic content" -> "টপিকের তথ্য অসম্পূর্ণ"
    "Invalid questions" -> "প্রশ্নের তথ্য ভুল"
    "Invalid answers" -> "উত্তরের অপশন ভুল"
    "Missing explanations" -> "ব্যাখ্যা নেই"
    "Broken question links" -> "প্রশ্নের টপিক লিংক নেই"
    "Possible duplicate questions" -> "সম্ভাব্য ডুপ্লিকেট প্রশ্ন"
    else -> category
}
