package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class StudyTask(
    val id: Int,
    val title: String,
    val subject: String,
    var isDone: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlannerScreen(
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tasks by remember { mutableStateOf(emptyList<StudyTask>()) }

    var newTaskText by remember { mutableStateOf("") }

    val completedCount = tasks.count { it.isDone }
    val progress = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 0f

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Progress Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryEmerald.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, SecondaryEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "আজকের স্টাডি প্রোগ্রেস",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(shape = RoundedCornerShape(8.dp), color = SecondaryEmerald) {
                            Text(
                                text = "$completedCount / ${tasks.size} সম্পন্ন",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = SecondaryEmerald,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // Input new task
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    placeholder = { Text("নতুন টার্গেট বা টাস্ক যোগ করুন...") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("new_task_input"),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (newTaskText.isNotBlank()) {
                            tasks = tasks + StudyTask(tasks.size + 1, newTaskText, "সাধারণ", false)
                            newTaskText = ""
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.height(56.dp).testTag("add_task_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }

            Text(
                text = "আজকের রুটিন ও টাস্ক লিস্ট:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (tasks.isEmpty()) {
                    item {
                        Text(
                            text = if (isBengali) "এখনও কোনো কাজ যোগ করা হয়নি। উপরের ঘর থেকে আজকের লক্ষ্য যোগ করুন।" else "No tasks yet. Add a goal above to plan your study day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    }
                }
                items(tasks) { task ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (task.isDone) SecondaryEmerald.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tasks = tasks.map { if (it.id == task.id) it.copy(isDone = !it.isDone) else it }
                            }
                            .testTag("task_item_${task.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { checked ->
                                    tasks = tasks.map { if (it.id == task.id) it.copy(isDone = checked) else it }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SecondaryEmerald)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (task.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    ),
                                    color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = task.subject,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                tasks = tasks.filter { it.id != task.id }
                            }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = WrongRed.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}
