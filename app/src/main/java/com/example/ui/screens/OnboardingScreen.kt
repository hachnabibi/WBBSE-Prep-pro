package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicClassProvider
import com.example.data.model.StudentProfile

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// THEME COLORS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
private val AppBackground = Color(0xFF080D1D)
private val AppCard = Color(0xFF1A2639)
private val AppInput = Color(0xFF152137)
private val AppSecondarySurface = Color(0xFF0E172A)
private val AppActiveSurface = Color(0xFF34445A)
private val AppPrimaryAccent = Color(0xFF4F46E5)
private val AppPrimaryStart = Color(0xFF4935D5)
private val AppPrimaryEnd = Color(0xFF6366F1)
private val AppTextPrimary = Color(0xFFF8FAFC)
private val AppTextSecondary = Color(0xFF94A3B8)
private val AppBorder = Color(0xFF334155)

private data class OnboardingStreamOption(
    val id: String,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    profile: StudentProfile,
    isBengali: Boolean,
    districts: List<String>,
    districtSchoolsMap: Map<String, List<String>>,
    isLoadingDistricts: Boolean,
    onCompleteOnboarding: (name: String, classLevel: String, district: String, school: String, targetExam: String, stream: String, studyGoals: List<String>) -> Unit,
    onNavigateToAuth: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Navigation State
    var currentStep by rememberSaveable { mutableIntStateOf(1) }

    // Form State
    var name by rememberSaveable { mutableStateOf(if (profile.name == "Sourav Das" || profile.name.isBlank()) "" else profile.name) }
    var selectedClassNumber by rememberSaveable { mutableIntStateOf(10) }
    var classLevel by rememberSaveable { mutableStateOf(profile.classLevel.ifBlank { "দশম শ্রেণি (মাধ্যমিক) - WBBSE" }) }
    var selectedStream by rememberSaveable { mutableStateOf(profile.stream.ifBlank { "all" }) }
    var targetExam by rememberSaveable { mutableStateOf(profile.targetExam.ifBlank { "WBBSE Madhyamik 2026" }) }
    var district by rememberSaveable { mutableStateOf(profile.district) }
    var school by rememberSaveable { mutableStateOf(profile.school) }

    // Storing multiple goals as comma separated string for easy saveable
    var selectedGoalsStr by rememberSaveable { mutableStateOf(profile.studyGoals.joinToString(",")) }
    val selectedGoals = remember(selectedGoalsStr) {
        if (selectedGoalsStr.isBlank()) emptyList<String>() else selectedGoalsStr.split(",")
    }

    BackHandler(enabled = currentStep in 2..5) {
        currentStep--
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header / Progress
            if (currentStep < 6) {
                OnboardingHeader(
                    currentStep = currentStep,
                    totalSteps = 5,
                    onBack = if (currentStep > 1) { { currentStep-- } } else null
                )
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))).togetherWith(
                                slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300))
                            )
                        } else {
                            (slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300))).togetherWith(
                                slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
                            )
                        }
                    },
                    label = "onboarding_steps"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (step) {
                            1 -> Step1Name(
                                name = name,
                                onNameChange = { name = it },
                                isBengali = isBengali,
                                onNext = { currentStep++ }
                            )
                            2 -> Step2Class(
                                selectedClassNumber = selectedClassNumber,
                                selectedStream = selectedStream,
                                onClassChange = { clsNum, clsLevel, exam, stream ->
                                    selectedClassNumber = clsNum
                                    classLevel = clsLevel
                                    targetExam = exam
                                    selectedStream = stream
                                },
                                isBengali = isBengali,
                                onNext = { currentStep++ }
                            )
                            3 -> Step3District(
                                district = district,
                                onDistrictChange = { 
                                    district = it
                                    // Auto-select first school if available
                                    val suggestedSchools = districtSchoolsMap[it] ?: emptyList()
                                    if (suggestedSchools.isNotEmpty()) {
                                        school = suggestedSchools.first()
                                    } else {
                                        school = "" // Clear if no schools
                                    }
                                },
                                districts = districts,
                                isLoading = isLoadingDistricts,
                                isBengali = isBengali,
                                onNext = { currentStep++ }
                            )
                            4 -> Step4School(
                                school = school,
                                onSchoolChange = { school = it },
                                availableSchools = districtSchoolsMap[district] ?: emptyList(),
                                isBengali = isBengali,
                                onNext = { currentStep++ }
                            )
                            5 -> Step5Goals(
                                selectedGoals = selectedGoals,
                                onGoalToggle = { goal ->
                                    val current = selectedGoals.toMutableList()
                                    if (current.contains(goal)) current.remove(goal)
                                    else current.add(goal)
                                    selectedGoalsStr = current.joinToString(",")
                                },
                                isBengali = isBengali,
                                onNext = {
                                    currentStep++
                                }
                            )
                            6 -> Step6Complete(
                                name = name,
                                classLevel = classLevel,
                                district = district,
                                school = school,
                                isBengali = isBengali,
                                onFinish = {
                                    onCompleteOnboarding(
                                        name,
                                        classLevel,
                                        district,
                                        school,
                                        targetExam,
                                        selectedStream,
                                        selectedGoals
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STEP 1: NAME
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1Name(
    name: String,
    onNameChange: (String) -> Unit,
    isBengali: Boolean,
    onNext: () -> Unit
) {
    Spacer(modifier = Modifier.height(32.dp))
    
    // Logo
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(AppPrimaryStart, AppPrimaryEnd))),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "স্বাগতম! \uD83D\uDC4B",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = AppTextPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "চলুন আপনার শেখার অভিজ্ঞতাটি ব্যক্তিগত করে তুলি",
        style = MaterialTheme.typography.bodyMedium,
        color = AppTextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "প্রথমে আপনার নামটি বলুন",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = AppTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "আপনাকে কোন নামে সম্বোধন করব?",
                style = MaterialTheme.typography.bodySmall,
                color = AppTextSecondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("শিক্ষার্থীর পুরো নাম (Full Name)") },
                placeholder = { Text("আপনার নাম লিখুন") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AppTextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppPrimaryAccent,
                    unfocusedBorderColor = AppBorder,
                    focusedLabelColor = AppPrimaryAccent,
                    unfocusedLabelColor = AppTextSecondary,
                    focusedTextColor = AppTextPrimary,
                    unfocusedTextColor = AppTextPrimary,
                    focusedContainerColor = AppInput,
                    unfocusedContainerColor = AppInput
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    OnboardingPrimaryButton(
        text = "পরবর্তী →",
        enabled = name.trim().isNotBlank(),
        onClick = onNext
    )
    Spacer(modifier = Modifier.height(32.dp))
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STEP 2: CLASS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun Step2Class(
    selectedClassNumber: Int,
    selectedStream: String,
    onClassChange: (Int, String, String, String) -> Unit,
    isBengali: Boolean,
    onNext: () -> Unit
) {
    Spacer(modifier = Modifier.height(32.dp))
    
    Text(
        text = "আপনি কোন শ্রেণিতে পড়েন?",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = AppTextPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "আপনার শ্রেণি নির্বাচন করুন",
        style = MaterialTheme.typography.bodyMedium,
        color = AppTextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    val allClasses by AcademicClassProvider.ALL_CLASSES_FLOW.collectAsState()
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
    ) {
        items(allClasses) { cls ->
            val isSelected = selectedClassNumber == cls.classNumber
            
            SelectableOptionCard(
                text = cls.nameBengali.ifBlank { AcademicClassProvider.getBengaliClassShortBadge(cls.classNumber) },
                isSelected = isSelected,
                onClick = {
                    val stream = if (cls.classNumber in 11..12) "science" else "all"
                    onClassChange(
                        cls.classNumber,
                        if (cls.classNumber in 11..12) "${cls.name} (Science) - ${cls.board}" else "${cls.name} - ${cls.board}",
                        cls.targetExam,
                        stream
                    )
                }
            )
        }
    }
    
    // Sub-class selector for Class 11 & 12
    AnimatedVisibility(
        visible = selectedClassNumber in listOf(11, 12),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column(modifier = Modifier.padding(top = 24.dp)) {
            Text(
                text = "বিভাগ বেছে নিন:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = AppTextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            val subOptions = listOf(
                OnboardingStreamOption("science", "বিজ্ঞান (Science)", Icons.Default.Science),
                OnboardingStreamOption("arts", "কলা (Arts)", Icons.Default.AutoStories),
                OnboardingStreamOption("commerce", "বাণিজ্য (Commerce)", Icons.Default.AccountBalanceWallet)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subOptions.forEach { opt ->
                    val isSubSelected = selectedStream == opt.id
                    SelectableOptionCard(
                        text = opt.label,
                        isSelected = isSubSelected,
                        onClick = {
                            val cls = allClasses.find { it.classNumber == selectedClassNumber }
                            if (cls != null) {
                                onClassChange(
                                    cls.classNumber,
                                    "${cls.name} (${opt.label}) - ${cls.board}",
                                    cls.targetExam,
                                    opt.id
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        height = 60.dp
                    )
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    OnboardingPrimaryButton(
        text = "পরবর্তী →",
        enabled = true,
        onClick = onNext
    )
    Spacer(modifier = Modifier.height(32.dp))
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STEP 3: DISTRICT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3District(
    district: String,
    onDistrictChange: (String) -> Unit,
    districts: List<String>,
    isLoading: Boolean,
    isBengali: Boolean,
    onNext: () -> Unit
) {
    Spacer(modifier = Modifier.height(32.dp))
    
    Text(
        text = "আপনি কোন জেলায় থাকেন?",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = AppTextPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "আপনার শেখার অভিজ্ঞতা আরও ব্যক্তিগত করতে আপনার জেলা নির্বাচন করুন।",
        style = MaterialTheme.typography.bodyMedium,
        color = AppTextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = district,
            onValueChange = {},
            readOnly = true,
            label = { Text("জেলা (District)") },
            placeholder = { Text("আপনার জেলা নির্বাচন করুন") },
            leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, tint = AppTextSecondary) },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AppPrimaryAccent)
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppPrimaryAccent,
                unfocusedBorderColor = AppBorder,
                focusedLabelColor = AppPrimaryAccent,
                unfocusedLabelColor = AppTextSecondary,
                focusedTextColor = AppTextPrimary,
                unfocusedTextColor = AppTextPrimary,
                focusedContainerColor = AppInput,
                unfocusedContainerColor = AppInput
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppCard)
        ) {
            if (districts.isEmpty() && !isLoading) {
                DropdownMenuItem(
                    text = { Text("No districts available", color = AppTextSecondary) },
                    onClick = { expanded = false }
                )
            } else {
                districts.forEach { dist ->
                    DropdownMenuItem(
                        text = { Text(dist, color = AppTextPrimary) },
                        onClick = {
                            onDistrictChange(dist)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    OnboardingPrimaryButton(
        text = "পরবর্তী →",
        enabled = district.trim().isNotBlank(),
        onClick = onNext
    )
    Spacer(modifier = Modifier.height(32.dp))
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STEP 4: SCHOOL
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step4School(
    school: String,
    onSchoolChange: (String) -> Unit,
    availableSchools: List<String>,
    isBengali: Boolean,
    onNext: () -> Unit
) {
    Spacer(modifier = Modifier.height(32.dp))
    
    Text(
        text = "আপনার স্কুল কোনটি?",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = AppTextPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "আপনার স্কুল নির্বাচন করুন",
        style = MaterialTheme.typography.bodyMedium,
        color = AppTextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredSchools = remember(searchQuery, availableSchools) {
        if (searchQuery.isBlank()) availableSchools
        else availableSchools.filter { it.contains(searchQuery, ignoreCase = true) }
    }
    
    // We'll use a combination of TextField for search + List below it
    OutlinedTextField(
        value = searchQuery.ifEmpty { school },
        onValueChange = { 
            searchQuery = it 
            if (it != school) onSchoolChange("") // clear selection if typing
        },
        label = { Text("আপনার স্কুলের নাম লিখুন") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppTextSecondary) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppPrimaryAccent,
            unfocusedBorderColor = AppBorder,
            focusedLabelColor = AppPrimaryAccent,
            unfocusedLabelColor = AppTextSecondary,
            focusedTextColor = AppTextPrimary,
            unfocusedTextColor = AppTextPrimary,
            focusedContainerColor = AppInput,
            unfocusedContainerColor = AppInput
        ),
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 250.dp)
    ) {
        if (filteredSchools.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "কোনো স্কুল পাওয়া যায়নি",
                    color = AppTextSecondary
                )
            }
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                filteredSchools.forEach { s ->
                    val isSelected = school == s
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSchoolChange(s)
                                searchQuery = "" // clear search to show selected name
                            }
                            .background(if (isSelected) AppPrimaryAccent.copy(alpha = 0.1f) else Color.Transparent)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = if (isSelected) AppPrimaryAccent else AppTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = s,
                            color = if (isSelected) AppPrimaryAccent else AppTextPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppPrimaryAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                    HorizontalDivider(color = AppBorder, thickness = 0.5.dp)
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    OnboardingPrimaryButton(
        text = "পরবর্তী →",
        enabled = school.trim().isNotBlank(),
        onClick = onNext
    )
    Spacer(modifier = Modifier.height(32.dp))
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STEP 5: GOALS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun Step5Goals(
    selectedGoals: List<String>,
    onGoalToggle: (String) -> Unit,
    isBengali: Boolean,
    onNext: () -> Unit
) {
    Spacer(modifier = Modifier.height(32.dp))
    
    Text(
        text = "আপনার পড়াশোনার লক্ষ্য কী?",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = AppTextPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "আপনার জন্য সঠিক পড়াশোনার অভিজ্ঞতা তৈরি করতে সাহায্য করবে।",
        style = MaterialTheme.typography.bodyMedium,
        color = AppTextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    val goals = listOf(
        "প্রতিদিন পড়াশোনা করতে চাই",
        "পরীক্ষার প্রস্তুতি নিতে চাই",
        "ভালো ফলাফল করতে চাই",
        "দুর্বল বিষয়গুলোতে উন্নতি করতে চাই"
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        goals.forEach { goal ->
            val isSelected = selectedGoals.contains(goal)
            SelectableOptionCard(
                text = goal,
                isSelected = isSelected,
                onClick = { onGoalToggle(goal) },
                height = 70.dp,
                icon = if (isSelected) Icons.Default.CheckCircle else null
            )
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    OnboardingPrimaryButton(
        text = "আমার পড়াশোনা শুরু করি →",
        enabled = selectedGoals.isNotEmpty(),
        onClick = onNext
    )
    Spacer(modifier = Modifier.height(32.dp))
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STEP 6: COMPLETE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun Step6Complete(
    name: String,
    classLevel: String,
    district: String,
    school: String,
    isBengali: Boolean,
    onFinish: () -> Unit
) {
    Spacer(modifier = Modifier.height(48.dp))
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(AppPrimaryStart, AppPrimaryEnd))),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Celebration, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "সব প্রস্তুত! \uD83C\uDF89",
        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        color = AppTextPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "স্বাগতম, $name!",
        style = MaterialTheme.typography.titleMedium,
        color = AppTextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryItem(Icons.Default.Person, name)
            SummaryItem(Icons.Default.School, classLevel)
            SummaryItem(Icons.Default.LocationOn, district)
            SummaryItem(Icons.Default.LocationCity, school)
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "আপনার personalized learning journey এখন শুরু হতে চলেছে।",
        style = MaterialTheme.typography.bodyMedium,
        color = AppTextSecondary,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    OnboardingPrimaryButton(
        text = "ড্যাশবোর্ডে যান →",
        enabled = true,
        onClick = onFinish
    )
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
fun SummaryItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppPrimaryAccent, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = AppTextPrimary, style = MaterialTheme.typography.bodyLarge)
    }
}


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// REUSABLE COMPONENTS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun OnboardingHeader(
    currentStep: Int,
    totalSteps: Int,
    onBack: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppTextPrimary)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
        
        Text(
            text = "$currentStep / $totalSteps",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = AppTextSecondary
        )
        
        // Placeholder for symmetry
        Spacer(modifier = Modifier.size(48.dp))
    }
    
    // Progress Bar
    LinearProgressIndicator(
        progress = { currentStep.toFloat() / totalSteps.toFloat() },
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp),
        color = AppPrimaryAccent,
        trackColor = AppSecondarySurface
    )
}

@Composable
fun OnboardingPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppPrimaryAccent,
            contentColor = Color.White,
            disabledContainerColor = AppActiveSurface,
            disabledContentColor = AppTextSecondary
        )
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SelectableOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 80.dp,
    icon: ImageVector? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AppPrimaryAccent else AppBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppPrimaryAccent.copy(alpha = 0.15f) else AppCard
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) AppPrimaryAccent else AppTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) AppTextPrimary else AppTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
