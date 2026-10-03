package com.example.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicClass
import com.example.data.model.AcademicClassProvider
import com.example.data.model.EducationBoard
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBoardClassTab(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onShowMessage: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val allBoards by viewModel.allBoards.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()

    var isAddingBoardExpanded by remember { mutableStateOf(false) }
    var isAddingClassExpanded by remember { mutableStateOf(false) }
    var isSyncingDefaults by remember { mutableStateOf(false) }

    // Board Form State
    var boardIdInput by remember { mutableStateOf("") }
    var boardShortNameInput by remember { mutableStateOf("") }
    var boardFullNameBengaliInput by remember { mutableStateOf("") }
    var boardFullNameEnglishInput by remember { mutableStateOf("") }
    var boardSubtitleBengaliInput by remember { mutableStateOf("") }
    var boardLevelBadgeInput by remember { mutableStateOf("") }
    var boardClassRangeInput by remember { mutableStateOf("") }
    var boardSelectedIcon by remember { mutableStateOf("MenuBook") }
    var isBoardSubmitting by remember { mutableStateOf(false) }

    // Class Form State
    var selectedTargetBoardId by remember { mutableStateOf("WBBPE") }
    var classNumberInput by remember { mutableStateOf("0") }
    var classIdInput by remember { mutableStateOf("class_kg_wbbpe") }
    var classNameBengaliInput by remember { mutableStateOf("প্রাক-প্রাথমিক (KG)") }
    var classNameEnglishInput by remember { mutableStateOf("Pre-Primary (KG)") }
    var classTargetExamInput by remember { mutableStateOf("WBBPE Pre-Primary / KG") }
    var classDescriptionInput by remember { mutableStateOf("পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ প্রাক-প্রাথমিক পাঠ্যক্রম") }
    var isClassSubmitting by remember { mutableStateOf(false) }

    // Delete Confirmation dialog state
    var itemToDelete by remember { mutableStateOf<Pair<String, Boolean>?>(null) } // Pair(id, isBoard)

    val availableIcons = listOf(
        "MenuBook" to Icons.Default.MenuBook,
        "ChildCare" to Icons.Default.ChildCare,
        "School" to Icons.Default.School,
        "AutoStories" to Icons.Default.AutoStories,
        "AccountBalance" to Icons.Default.AccountBalance,
        "Apps" to Icons.Default.Apps
    )

    fun getBoardIcon(iconName: String): ImageVector {
        return when (iconName) {
            "ChildCare" -> Icons.Default.ChildCare
            "School" -> Icons.Default.School
            "AutoStories" -> Icons.Default.AutoStories
            "AccountBalance" -> Icons.Default.AccountBalance
            "Apps" -> Icons.Default.Apps
            else -> Icons.Default.MenuBook
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. HEADER INFO & LOCAL-FIRST / REALTIME CLOUD STATUS
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CorrectGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = "Cloud & Local Sync",
                                        tint = CorrectGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isBengali) "বোর্ড ও ক্লাস ম্যানেজমেন্ট" else "Boards & Classes Manager",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isBengali) "রিয়েলটাইম ফায়ারবেস + লোকাল-ফার্স্ট ক্যাশ" else "Realtime Firebase + Local-First Cache",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Counts pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${allBoards.size} বোর্ড • ${allClasses.size} ক্লাস",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Local-first mechanism explanation
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Local First",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isBengali)
                                    "⚡ লোকাল-ফার্স্ট মেকানিজম: অ্যাপ চালু হলে প্রথমে অফলাইন লোকাল ডেটাবেজ চেক হয়। লোকাল ডেটা না পেলেই কেবল ফায়ারবেস থেকে ফেচ ও এক্সিকিউট (লোকাল সেভ) হয়। নতুন ডেটা ফায়ারবেসে যোগ করলে তা সাথে সাথে রিয়েলটাইমে সব ডিভাইসে আপডেট হয়ে যাবে।"
                                else
                                    "⚡ Local-First Engine: The app requests local Room DB first. Only if not found does it query Firebase Firestore & executes local persistence. Any changes in Firebase reflect across all users in realtime.",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // One-click Sync defaults to Firebase
                    Button(
                        onClick = {
                            isSyncingDefaults = true
                            viewModel.syncDefaultBoardsAndClassesToFirebase { success, msg ->
                                isSyncingDefaults = false
                                onShowMessage(msg, success)
                            }
                        },
                        enabled = !isSyncingDefaults,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSyncingDefaults) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("ফায়ারবেসে সিঙ্ক হচ্ছে...", fontSize = 13.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "🔥 ডিফল্ট বোর্ড ও ক্লাস ফায়ারবেসে সিঙ্ক করুন" else "🔥 Sync Default Boards & Classes to Firebase",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. ADD NEW EDUCATION BOARD FORM (EXPANDABLE)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAddingBoardExpanded = !isAddingBoardExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AddBusiness,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isBengali) "➕ নতুন শিক্ষা বোর্ড যোগ করুন" else "➕ Add New Education Board",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { isAddingBoardExpanded = !isAddingBoardExpanded }) {
                            Icon(
                                imageVector = if (isAddingBoardExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }

                    AnimatedVisibility(visible = isAddingBoardExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = boardIdInput,
                                onValueChange = { 
                                    boardIdInput = it.uppercase().trim()
                                    if (boardShortNameInput.isBlank()) boardShortNameInput = boardIdInput
                                },
                                label = { Text("বোর্ড কোড / ID (যেমন: WBBPE, CBSE)") },
                                placeholder = { Text("WBBPE") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = boardShortNameInput,
                                onValueChange = { boardShortNameInput = it },
                                label = { Text("সংক্ষিপ্ত নাম (Short Name)") },
                                placeholder = { Text("WBBPE") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = boardFullNameBengaliInput,
                                onValueChange = { boardFullNameBengaliInput = it },
                                label = { Text("সম্পূর্ণ বাংলা নাম") },
                                placeholder = { Text("পশ্চিমবঙ্গ প্রাথমিক শিক্ষা পর্ষদ") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = boardFullNameEnglishInput,
                                onValueChange = { boardFullNameEnglishInput = it },
                                label = { Text("সম্পূর্ণ ইংরেজি নাম (Full English Name)") },
                                placeholder = { Text("West Bengal Board of Primary Education") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = boardSubtitleBengaliInput,
                                    onValueChange = { boardSubtitleBengaliInput = it },
                                    label = { Text("সাবটাইটেল") },
                                    placeholder = { Text("প্রাথমিক শিক্ষা পর্ষদ") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = boardLevelBadgeInput,
                                    onValueChange = { boardLevelBadgeInput = it },
                                    label = { Text("লেভেল ব্যাজ") },
                                    placeholder = { Text("প্রাথমিক স্তর") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            OutlinedTextField(
                                value = boardClassRangeInput,
                                onValueChange = { boardClassRangeInput = it },
                                label = { Text("ক্লাস রেঞ্জ বিবরণ") },
                                placeholder = { Text("প্রাক-প্রাথমিক (KG) ও ১ম থেকে ৪র্থ শ্রেণি") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Icon selector
                            Text(
                                text = "বোর্ড আইকন সিলেক্ট করুন:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(availableIcons) { (name, icon) ->
                                    val isSelected = boardSelectedIcon == name
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { boardSelectedIcon = name },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = name,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        label = { Text(name, fontSize = 12.sp) }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val id = boardIdInput.trim()
                                    val short = boardShortNameInput.ifBlank { id }
                                    val fullBengali = boardFullNameBengaliInput.ifBlank { short }
                                    if (id.isBlank()) {
                                        onShowMessage("অনুগ্রহ করে বোর্ড ID লিখুন", false)
                                        return@Button
                                    }
                                    isBoardSubmitting = true
                                    val board = EducationBoard(
                                        id = id,
                                        shortName = short,
                                        fullNameBengali = fullBengali,
                                        fullNameEnglish = boardFullNameEnglishInput.ifBlank { short },
                                        subtitleBengali = boardSubtitleBengaliInput.ifBlank { short },
                                        levelBadge = boardLevelBadgeInput.ifBlank { "বোর্ড স্তর" },
                                        classRangeDescription = boardClassRangeInput,
                                        iconName = boardSelectedIcon
                                    )
                                    viewModel.addOrUpdateBoard(board) { success, msg ->
                                        isBoardSubmitting = false
                                        onShowMessage(msg, success)
                                        if (success) {
                                            boardIdInput = ""
                                            boardShortNameInput = ""
                                            boardFullNameBengaliInput = ""
                                            boardFullNameEnglishInput = ""
                                            boardSubtitleBengaliInput = ""
                                            boardLevelBadgeInput = ""
                                            boardClassRangeInput = ""
                                            isAddingBoardExpanded = false
                                        }
                                    }
                                },
                                enabled = !isBoardSubmitting,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isBoardSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("💾 বোর্ড ফায়ারবেস ও অ্যাপে সেভ করুন")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. ADD NEW CLASS TO BOARD FORM (EXPANDABLE)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAddingClassExpanded = !isAddingClassExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CorrectGreen.copy(alpha = 0.12f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AddCard,
                                        contentDescription = null,
                                        tint = CorrectGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isBengali) "➕ বোর্ডে নতুন ক্লাস যোগ করুন" else "➕ Add New Class to Board",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { isAddingClassExpanded = !isAddingClassExpanded }) {
                            Icon(
                                imageVector = if (isAddingClassExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }

                    AnimatedVisibility(visible = isAddingClassExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Target Board Selector
                            Text(
                                text = "বোর্ড নির্বাচন করুন:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allBoards) { board ->
                                    val isSelected = selectedTargetBoardId.equals(board.id, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedTargetBoardId = board.id
                                            val num = classNumberInput.toIntOrNull() ?: 0
                                            classIdInput = if (num == 0) "class_kg_${board.id.lowercase()}" else "class_${num}_${board.id.lowercase()}"
                                        },
                                        label = { Text(board.shortName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                                    )
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = classNumberInput,
                                    onValueChange = {
                                        classNumberInput = it
                                        val num = it.toIntOrNull() ?: 0
                                        classIdInput = if (num == 0) "class_kg_${selectedTargetBoardId.lowercase()}" else "class_${num}_${selectedTargetBoardId.lowercase()}"
                                        if (num == 0) {
                                            classNameBengaliInput = "প্রাক-প্রাথমিক (KG)"
                                            classNameEnglishInput = "Pre-Primary (KG)"
                                        } else {
                                            classNameBengaliInput = AcademicClassProvider.getBengaliClassShortBadge(num)
                                            classNameEnglishInput = "Class $num"
                                        }
                                        classTargetExamInput = "$selectedTargetBoardId $classNameEnglishInput"
                                    },
                                    label = { Text("ক্লাস নম্বর (0 = KG)") },
                                    placeholder = { Text("1") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = classIdInput,
                                    onValueChange = { classIdInput = it.lowercase().trim() },
                                    label = { Text("ক্লাস ID") },
                                    placeholder = { Text("class_1_wbbpe") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            OutlinedTextField(
                                value = classNameBengaliInput,
                                onValueChange = { classNameBengaliInput = it },
                                label = { Text("বাংলা নাম") },
                                placeholder = { Text("প্রথম শ্রেণি") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = classNameEnglishInput,
                                onValueChange = { classNameEnglishInput = it },
                                label = { Text("ইংরেজি নাম") },
                                placeholder = { Text("Class 1") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = classTargetExamInput,
                                onValueChange = { classTargetExamInput = it },
                                label = { Text("টার্গেট পরীক্ষা (Target Exam)") },
                                placeholder = { Text("WBBPE Class 1 Exam") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = classDescriptionInput,
                                onValueChange = { classDescriptionInput = it },
                                label = { Text("বিবরণ (Description)") },
                                placeholder = { Text("সিলেবাস ও পরীক্ষা প্রস্তুতি") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    val id = classIdInput.trim()
                                    val num = classNumberInput.toIntOrNull() ?: 0
                                    val nameBengali = classNameBengaliInput.trim()
                                    if (id.isBlank() || nameBengali.isBlank()) {
                                        onShowMessage("অনুগ্রহ করে ক্লাস ID ও নাম লিখুন", false)
                                        return@Button
                                    }
                                    isClassSubmitting = true
                                    val academicClass = AcademicClass(
                                        id = id,
                                        classNumber = num,
                                        nameBengali = nameBengali,
                                        nameEnglish = classNameEnglishInput.ifBlank { nameBengali },
                                        board = selectedTargetBoardId,
                                        targetExam = classTargetExamInput.ifBlank { "$selectedTargetBoardId $nameBengali" },
                                        descriptionBengali = classDescriptionInput
                                    )
                                    viewModel.addOrUpdateClass(academicClass) { success, msg ->
                                        isClassSubmitting = false
                                        onShowMessage(msg, success)
                                        if (success) {
                                            isAddingClassExpanded = false
                                        }
                                    }
                                },
                                enabled = !isClassSubmitting,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen)
                            ) {
                                if (isClassSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("💾 ক্লাস ফায়ারবেস ও অ্যাপে সেভ করুন")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. REALTIME LIST OF BOARDS AND THEIR CLASSES
        item {
            Text(
                text = if (isBengali) "📋 নিবন্ধিত বোর্ড ও ক্লাসের রিয়েলটাইম তালিকা" else "📋 Registered Boards & Classes (Live)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        items(allBoards, key = { it.id }) { board ->
            val boardClasses = allClasses.filter { it.board.equals(board.id, ignoreCase = true) }
                .sortedBy { it.classNumber }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Board Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getBoardIcon(board.iconName),
                                        contentDescription = board.shortName,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = board.shortName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = board.levelBadge.ifBlank { "বোর্ড" },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = board.fullNameBengali,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (board.classRangeDescription.isNotBlank()) {
                                    Text(
                                        text = "শ্রেণি: ${board.classRangeDescription}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF059669),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Action Buttons for Board
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    selectedTargetBoardId = board.id
                                    isAddingClassExpanded = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "Add Class",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { itemToDelete = Pair(board.id, true) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Board",
                                    tint = WrongRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Classes List for this Board
                    if (boardClasses.isEmpty()) {
                        Text(
                            text = if (isBengali) "এই বোর্ডে এখনও কোনো ক্লাস যোগ করা হয়নি।" else "No classes added to this board yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            boardClasses.forEach { cls ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (cls.classNumber == 0) Color(0xFF059669).copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = if (cls.classNumber == 0) "KG" else "${cls.classNumber}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (cls.classNumber == 0) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = cls.nameBengali,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${cls.id} • ${cls.targetExam}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { itemToDelete = Pair(cls.id, false) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete Class",
                                                tint = WrongRed.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    val deleteTarget = itemToDelete
    if (deleteTarget != null) {
        val (targetId, isBoard) = deleteTarget
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(if (isBoard) "বোর্ড মুছে ফেলতে চান?" else "ক্লাস মুছে ফেলতে চান?")
            },
            text = {
                Text(
                    if (isBoard) "আপনি কি নিশ্চিত যে '$targetId' বোর্ডটি ফায়ারবেস ও অ্যাপের ডেটাবেজ থেকে মুছে ফেলতে চান? এতে এই বোর্ডের অধীনস্থ সব ক্লাসও প্রভাবিত হতে পারে।"
                    else "আপনি কি নিশ্চিত যে '$targetId' ক্লাসটি ফায়ারবেস ও অ্যাপের ডেটাবেজ থেকে মুছে ফেলতে চান?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete = null
                        if (isBoard) {
                            viewModel.deleteBoard(targetId) { success, msg ->
                                onShowMessage(msg, success)
                            }
                        } else {
                            viewModel.deleteClass(targetId) { success, msg ->
                                onShowMessage(msg, success)
                            }
                        }
                    }
                ) {
                    Text("হ্যাঁ, ডিলিট করুন", color = WrongRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
