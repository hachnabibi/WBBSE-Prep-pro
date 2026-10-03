package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicClass
import com.example.data.model.AcademicClassProvider
import com.example.data.model.StudentProfile
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.ExamGold
import com.example.ui.theme.ExamFlame
import com.example.ui.viewmodel.DeshExamViewModel

/**
 * Data representation for Education Boards
 */
private data class EducationBoardItem(
    val id: String,
    val shortName: String,
    val fullNameBengali: String,
    val subtitleBengali: String,
    val levelBadge: String,
    val classRangeDescription: String,
    val icon: ImageVector,
    val accentColor: Color
)

/**
 * Data representation for Senior Secondary Streams / Sub-classes
 */
private data class SubClassStreamItem(
    val id: String,
    val titleBengali: String,
    val titleEnglish: String,
    val keySubjectsBengali: String,
    val icon: ImageVector,
    val accentColor: Color,
    val tagLabel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: DeshExamViewModel,
    profile: StudentProfile,
    isBengali: Boolean,
    districts: List<String>,
    districtSchoolsMap: Map<String, List<String>>,
    isLoadingDistricts: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    // Mutable states for basic info
    var editName by remember(profile.name) { mutableStateOf(profile.name) }
    var editSchool by remember(profile.school) { mutableStateOf(profile.school) }
    var editDistrict by remember(profile.district) { mutableStateOf(profile.district) }

    // All available classes & boards from provider (Realtime flow)
    val allClassesList by AcademicClassProvider.ALL_CLASSES_FLOW.collectAsState()
    val allBoardsList by AcademicClassProvider.ALL_BOARDS_FLOW.collectAsState()

    // 1. Manage currently selected academic class object
    var selectedClass by remember(profile.classId) {
        mutableStateOf(allClassesList.find { it.id == profile.classId } ?: allClassesList.first())
    }

    // 2. Manage Board selection (Step 1: Board first)
    val initialBoard = remember(profile.classId) {
        when {
            profile.classId.contains("wbchse", ignoreCase = true) || profile.classId in listOf("class_11_wbchse", "class_12_wbchse") -> "WBCHSE"
            profile.classId.contains("wbbpe", ignoreCase = true) || profile.classId.startsWith("class_kg") || profile.classId in listOf("class_1_wbbpe", "class_2_wbbpe", "class_3_wbbpe", "class_4_wbbpe") -> "WBBPE"
            else -> "WBBSE"
        }
    }
    var selectedBoard by remember { mutableStateOf(initialBoard) }

    // 3. Classes filtered dynamically by selected board (Step 2: Classes for that board)
    val filteredClasses = remember(selectedBoard, allClassesList) {
        allClassesList.filter { it.board.equals(selectedBoard, ignoreCase = true) }
    }

    // 4. Sub-class / Stream condition (Step 3: Sub-class if available for Class 11 & 12)
    val hasSubClasses = selectedClass.classNumber in listOf(11, 12) || selectedClass.board.equals("WBCHSE", ignoreCase = true)
    var selectedStream by remember(profile.stream) {
        val initialStream = if (profile.stream.isNotBlank() && profile.stream != "all") profile.stream else "science"
        mutableStateOf(initialStream)
    }

    // Available Education Boards (dynamically sourced from AcademicClassProvider)
    val educationBoards = remember(allBoardsList) {
        allBoardsList.map { board ->
            val icon = when {
                board.id == "WBBPE" || board.iconName == "ChildCare" -> Icons.Default.ChildCare
                board.iconName == "School" || board.id.contains("HSE", ignoreCase = true) -> Icons.Default.School
                else -> Icons.Default.MenuBook
            }
            val accentColor = when (board.id) {
                "WBBPE" -> Color(0xFF059669)
                "WBCHSE" -> Color(0xFF7C3AED)
                "CBSE" -> Color(0xFFEA580C)
                "ICSE" -> Color(0xFF0D9488)
                else -> Color(0xFF2563EB)
            }
            EducationBoardItem(
                id = board.id,
                shortName = board.shortName,
                fullNameBengali = board.fullNameBengali,
                subtitleBengali = board.subtitleBengali,
                levelBadge = board.levelBadge,
                classRangeDescription = board.classRangeDescription,
                icon = icon,
                accentColor = accentColor
            )
        }
    }

    // Available Streams / Sub-classes for Class 11 and 12
    val streamOptions = remember {
        listOf(
            SubClassStreamItem(
                id = "science",
                titleBengali = "বিজ্ঞান শাখা (Science)",
                titleEnglish = "Science Stream",
                keySubjectsBengali = "পদার্থবিদ্যা, রসায়ন, গণিত, জীববিজ্ঞান ও কম্পিউটার সায়েন্স",
                icon = Icons.Default.Science,
                accentColor = Color(0xFF2563EB),
                tagLabel = "STEM"
            ),
            SubClassStreamItem(
                id = "commerce",
                titleBengali = "বাণিজ্য শাখা (Commerce)",
                titleEnglish = "Commerce Stream",
                keySubjectsBengali = "হিসাবশাস্ত্র, কারবারি শিক্ষা, অর্থনীতি ও বাণিজ্যিক গণিত",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = Color(0xFF059669),
                tagLabel = "Commerce"
            ),
            SubClassStreamItem(
                id = "arts",
                titleBengali = "কলা বিভাগ (Arts / Humanities)",
                titleEnglish = "Arts / Humanities Stream",
                keySubjectsBengali = "ইতিহাস, ভূগোল, রাষ্ট্রবিজ্ঞান, দর্শন ও শিক্ষাবিজ্ঞান",
                icon = Icons.Default.AutoStories,
                accentColor = Color(0xFFD97706),
                tagLabel = "Humanities"
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("edit_profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Header with Back Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .testTag("btn_back_edit_profile")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column {
                        Text(
                            text = if (isBengali) "প্রোফাইল সম্পাদনা" else "Edit Profile",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBengali) "বোর্ড, শ্রেণি ও শাখা নির্বাচন" else "Board, Class & Stream Selection",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        focusManager.clearFocus()
                        val finalStream = if (hasSubClasses) selectedStream else "all"
                        viewModel.updateProfileDetails(editName, editSchool, editDistrict, selectedClass.targetExam)
                        viewModel.switchActiveExamClass(selectedClass.id, finalStream)
                        onBack()
                    },
                    enabled = !isAuthLoading,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "সংরক্ষণ", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Hero Avatar & Live Configuration Preview
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.5.dp, ExamGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = editName.take(1).ifBlank { "S" }.uppercase(),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = PrimaryIndigo
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = editName.ifBlank { "শিক্ষার্থীর প্রোফাইল" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // 1. Board Name • Class • District Chip Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    val classShortName = AcademicClassProvider.getBengaliClassShortBadge(selectedClass.classNumber)
                                    val distText = editDistrict.ifBlank { "জেলা" }
                                    Text(
                                        text = "$selectedBoard • $classShortName • $distText",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryIndigo,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 2. Sub-Class / Stream Chip Badge
                            val streamColor = if (hasSubClasses) {
                                when (selectedStream) {
                                    "science" -> Color(0xFF2563EB)
                                    "commerce" -> Color(0xFF059669)
                                    "arts" -> Color(0xFFD97706)
                                    else -> PrimaryIndigo
                                }
                            } else Color(0xFF059669)

                            val streamText = if (hasSubClasses) {
                                when (selectedStream) {
                                    "science" -> "🔬 বিজ্ঞান শাখা (Science)"
                                    "commerce" -> "💼 বাণিজ্য শাখা (Commerce)"
                                    "arts" -> "📚 কলা বিভাগ (Arts)"
                                    else -> "সকল শাখা"
                                }
                            } else "📘 সাধারণ পাঠ্যক্রম (সব বিষয়)"

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = streamColor.copy(alpha = 0.12f),
                                border = BorderStroke(0.8.dp, streamColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = streamText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                    color = streamColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // 3. School Name right below
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = editSchool.ifBlank { "বিদ্যালয় নির্বাচন করুন" },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Basic Information (Name, District, School)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "ব্যক্তিগত তথ্য (Basic Info)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 1. Name Input
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("শিক্ষার্থীর পুরো নাম") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_name_new")
                    )

                    // 2. District Selector Dropdown
                    var districtExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = districtExpanded,
                        onExpandedChange = { districtExpanded = !districtExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editDistrict,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("জেলা (District)") },
                            leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
                            trailingIcon = {
                                if (isLoadingDistricts) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("input_edit_district_dropdown_new")
                        )
                        ExposedDropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = { districtExpanded = false }
                        ) {
                            districts.forEach { dist ->
                                DropdownMenuItem(
                                    text = { Text(dist) },
                                    onClick = {
                                        editDistrict = dist
                                        districtExpanded = false
                                        val suggestedSchools = districtSchoolsMap[dist] ?: emptyList()
                                        if (suggestedSchools.isNotEmpty()) {
                                            editSchool = suggestedSchools.first()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 3. School Selector Dropdown
                    var schoolExpanded by remember { mutableStateOf(false) }
                    val districtSchools = remember(editDistrict) { districtSchoolsMap[editDistrict] ?: emptyList() }
                    ExposedDropdownMenuBox(
                        expanded = schoolExpanded,
                        onExpandedChange = { schoolExpanded = !schoolExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editSchool,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("বিদ্যালয়ের নাম (School)") },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schoolExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("input_edit_school_dropdown_new")
                        )
                        ExposedDropdownMenu(
                            expanded = schoolExpanded,
                            onDismissRequest = { schoolExpanded = false }
                        ) {
                            if (districtSchools.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("কোনো স্কুল পাওয়া যায়নি") },
                                    onClick = { schoolExpanded = false }
                                )
                            } else {
                                districtSchools.forEach { sch ->
                                    DropdownMenuItem(
                                        text = { Text(sch) },
                                        onClick = {
                                            editSchool = sch
                                            schoolExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // STEP 1: EDUCATION BOARD SELECTION (বোর্ড নির্বাচন - সবার আগে বোর্ড সিলেক্ট হবে)
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_step_1_board")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Step 1 Header Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryIndigo,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "১",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "ধাপ ১: শিক্ষা বোর্ড নির্বাচন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "প্রথমে আপনার শিক্ষার পর্ষদ বা সংসদ বেছে নিন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Selected indicator pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryIndigo.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = selectedBoard,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = PrimaryIndigo
                            )
                        }
                    }

                    // Board Option Cards
                    educationBoards.forEach { board ->
                        val isBoardSelected = selectedBoard.equals(board.id, ignoreCase = true)
                        val borderColor = if (isBoardSelected) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        val bgColor = if (isBoardSelected) PrimaryIndigo.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = bgColor,
                            border = BorderStroke(if (isBoardSelected) 2.dp else 1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedBoard != board.id) {
                                        selectedBoard = board.id
                                        // Auto-adjust class to match new board
                                        val newBoardClasses = allClassesList.filter { it.board.equals(board.id, ignoreCase = true) }
                                        if (newBoardClasses.none { it.id == selectedClass.id }) {
                                            selectedClass = when (board.id) {
                                                "WBCHSE" -> newBoardClasses.firstOrNull { it.classNumber == 11 } ?: newBoardClasses.first()
                                                "WBBPE" -> newBoardClasses.firstOrNull { it.classNumber == 1 } ?: newBoardClasses.first()
                                                else -> newBoardClasses.firstOrNull { it.classNumber == 10 } ?: newBoardClasses.first()
                                            }
                                        }
                                    }
                                }
                                .testTag("board_option_${board.id.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isBoardSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = board.icon,
                                            contentDescription = null,
                                            tint = if (isBoardSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = board.shortName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                                color = if (isBoardSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isBoardSelected) PrimaryIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = board.levelBadge,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                    color = if (isBoardSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Text(
                                            text = board.fullNameBengali,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Text(
                                            text = board.classRangeDescription,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                RadioButton(
                                    selected = isBoardSelected,
                                    onClick = {
                                        if (selectedBoard != board.id) {
                                            selectedBoard = board.id
                                            val newBoardClasses = allClassesList.filter { it.board.equals(board.id, ignoreCase = true) }
                                            if (newBoardClasses.none { it.id == selectedClass.id }) {
                                                selectedClass = when (board.id) {
                                                    "WBCHSE" -> newBoardClasses.firstOrNull { it.classNumber == 11 } ?: newBoardClasses.first()
                                                    "WBBPE" -> newBoardClasses.firstOrNull { it.classNumber == 1 } ?: newBoardClasses.first()
                                                    else -> newBoardClasses.firstOrNull { it.classNumber == 10 } ?: newBoardClasses.first()
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // STEP 2: CLASS SELECTION FOR THE SELECTED BOARD (বোর্ড অনুযায়ী শ্রেণি প্রদর্শিত হবে)
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_step_2_classes")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Step 2 Header Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ExamFlame,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "২",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "ধাপ ২: শ্রেণি নির্বাচন ($selectedBoard)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "নির্বাচিত ${selectedBoard} বোর্ডের অন্তর্ভুক্ত শ্রেণি নির্বাচন করুন:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Selected Class Chip Badge
                        val selectedClassBadge = AcademicClassProvider.getBengaliClassShortBadge(selectedClass.classNumber)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExamFlame.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, ExamFlame.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedClass.classNumber == 0) Icons.Default.ChildCare else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ExamFlame,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = selectedClassBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = ExamFlame
                                )
                            }
                        }
                    }

                    // Quick Class Selection Chips Row
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "শ্রেণি বাছুন (Class Selection Chips):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredClasses) { cls ->
                                val isClassSelected = selectedClass.id == cls.id
                                val chipBadgeText = AcademicClassProvider.getBengaliClassShortBadge(cls.classNumber)
                                FilterChip(
                                    selected = isClassSelected,
                                    onClick = { selectedClass = cls },
                                    label = {
                                        Text(
                                            text = chipBadgeText,
                                            fontWeight = if (isClassSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    },
                                    leadingIcon = if (isClassSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else if (cls.classNumber == 0) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.ChildCare,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryIndigo,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("chip_class_${cls.id}")
                                )
                            }
                        }
                    }

                    // Render only classes belonging to the selected board
                    filteredClasses.forEach { cls ->
                        val isClassSelected = selectedClass.id == cls.id
                        val borderColor = if (isClassSelected) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        val containerColor = if (isClassSelected) PrimaryIndigo.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(containerColor)
                                .border(
                                    width = if (isClassSelected) 2.dp else 1.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedClass = cls
                                }
                                .padding(12.dp)
                                .testTag("class_option_${cls.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isClassSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (cls.classNumber == 0) "KG" else "${cls.classNumber}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = if (isClassSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = cls.nameBengali,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isClassSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val classBadgeInfo: Pair<String, Color> = when {
                                            cls.classNumber == 0 -> Pair("প্রাক-প্রাথমিক (KG)", Color(0xFF059669))
                                            cls.board == "WBBPE" -> Pair("প্রাথমিক স্তর", Color(0xFF059669))
                                            cls.classNumber == 10 -> Pair("বোর্ড পরীক্ষা (Madhyamik)", ExamFlame)
                                            cls.classNumber in 11..12 -> Pair(if (cls.classNumber == 11) "একাদশ শ্রেণি" else "ফাইনাল (HS)", Color(0xFF7C3AED))
                                            else -> Pair("মাধ্যমিক স্তর", PrimaryIndigo)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = classBadgeInfo.second.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = classBadgeInfo.first,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = classBadgeInfo.second
                                            )
                                        }
                                    }

                                    Text(
                                        text = cls.targetExam,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            RadioButton(
                                selected = isClassSelected,
                                onClick = { selectedClass = cls }
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // STEP 3: SUB-CLASS / STREAM SELECTION (সাব-ক্লাস বা শাখা যদি থাকে)
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_step_3_subclass")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Step 3 Header Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (hasSubClasses) Color(0xFF059669) else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "৩",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "ধাপ ৩: সাব-ক্লাস / শাখা নির্বাচন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val classShort = AcademicClassProvider.getBengaliClassShortBadge(selectedClass.classNumber)
                                Text(
                                    text = if (hasSubClasses) "$classShort-এর জন্য আপনার শাখা নির্বাচন করুন:" 
                                    else "এই শ্রেণির জন্য কোনো সাব-ক্লাস প্রযোজ্য নয়",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Sub-class Badge (both when active and general)
                        if (hasSubClasses) {
                            val streamBadgeBg = when (selectedStream) {
                                "science" -> Color(0xFF2563EB)
                                "commerce" -> Color(0xFF059669)
                                "arts" -> Color(0xFFD97706)
                                else -> PrimaryIndigo
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = streamBadgeBg.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, streamBadgeBg.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val streamIcon = when (selectedStream) {
                                        "science" -> Icons.Default.Science
                                        "commerce" -> Icons.Default.AccountBalanceWallet
                                        "arts" -> Icons.Default.AutoStories
                                        else -> Icons.Default.Apps
                                    }
                                    Icon(
                                        imageVector = streamIcon,
                                        contentDescription = null,
                                        tint = streamBadgeBg,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = when (selectedStream) {
                                            "science" -> "বিজ্ঞান"
                                            "commerce" -> "বাণিজ্য"
                                            "arts" -> "কলা"
                                            else -> "সকল"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = streamBadgeBg
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF059669).copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "সাধারণ পাঠ্যক্রম",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF059669)
                                    )
                                }
                            }
                        }
                    }

                    // If the selected class has sub-classes (streams for Class 11 and 12)
                    if (hasSubClasses) {
                        // Quick Stream Selection Chips Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "শাখা বাছুন",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                streamOptions.forEach { streamItem ->
                                    val isStreamSelected = selectedStream == streamItem.id
                                    FilterChip(
                                        selected = isStreamSelected,
                                        onClick = { selectedStream = streamItem.id },
                                        label = {
                                            Text(
                                                text = streamItem.tagLabel,
                                                fontWeight = if (isStreamSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = streamItem.icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = streamItem.accentColor,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("chip_stream_${streamItem.id}")
                                    )
                                }
                            }
                        }

                        Text(
                            text = "আপনার নির্বাচিত শাখা অনুযায়ী বিষয়ের তালিকা, সিলেবাস ও মক টেস্ট ফিল্টার হবে:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        streamOptions.forEach { streamItem ->
                            val isStreamSelected = selectedStream == streamItem.id
                            val borderColor = if (isStreamSelected) streamItem.accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            val containerColor = if (isStreamSelected) streamItem.accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = containerColor,
                                border = BorderStroke(if (isStreamSelected) 2.dp else 1.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedStream = streamItem.id }
                                    .testTag("stream_option_${streamItem.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isStreamSelected) streamItem.accentColor else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = streamItem.icon,
                                                contentDescription = null,
                                                tint = if (isStreamSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = streamItem.titleBengali,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isStreamSelected) streamItem.accentColor else MaterialTheme.colorScheme.onSurface
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = streamItem.accentColor.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = streamItem.tagLabel,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                        color = streamItem.accentColor
                                                    )
                                                }
                                            }

                                            Text(
                                                text = streamItem.keySubjectsBengali,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2
                                            )
                                        }
                                    }

                                    RadioButton(
                                        selected = isStreamSelected,
                                        onClick = { selectedStream = streamItem.id },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = streamItem.accentColor
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // For classes 5-10 where there are no separate streams/sub-classes
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "সাব-ক্লাস বা শাখা প্রযোজ্য নয়",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${selectedClass.nameBengali}-এর জন্য কোনো আলাদা শাখা বিভাজন নেই। সমস্ত আবশ্যিক সাধারণ বিষয় (বাংলা, ইংরেজি, গণিত, বিজ্ঞান ইত্যাদি) আপনার ড্যাশবোর্ডে থাকবে।",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SUMMARY CARD: LIVE RECAP OF USER CONFIGURATION
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "সারসংক্ষেপ (Configuration Summary)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryIndigo
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "বোর্ড:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = selectedBoard, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column {
                            Text(text = "শ্রেণি:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = selectedClass.nameBengali, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column {
                            Text(text = "শাখা / সাব-ক্লাস:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val streamLabel = if (hasSubClasses) {
                                when (selectedStream) {
                                    "science" -> "বিজ্ঞান"
                                    "commerce" -> "বাণিজ্য"
                                    "arts" -> "কলা"
                                    else -> "সকল"
                                }
                            } else "সাধারণ"
                            Text(text = streamLabel, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // ACTION BUTTONS: SAVE & CANCEL
        // =========================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val finalStream = if (hasSubClasses) selectedStream else "all"
                        // 1. Update basic student details (Name, School, District, TargetExam)
                        viewModel.updateProfileDetails(editName, editSchool, editDistrict, selectedClass.targetExam)
                        // 2. Switch active studying class & curriculum stream
                        viewModel.switchActiveExamClass(selectedClass.id, finalStream)
                        // 3. Return to previous screen
                        onBack()
                    },
                    enabled = !isAuthLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_save_profile_new_screen")
                ) {
                    if (isAuthLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "প্রোফাইল পরিবর্তন সংরক্ষণ করুন" else "Save Profile Changes",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_cancel_edit_profile")
                ) {
                    Text(
                        text = if (isBengali) "বাতিল করুন" else "Cancel",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
