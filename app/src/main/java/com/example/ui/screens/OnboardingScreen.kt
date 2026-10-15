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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
    onCompleteOnboarding: (name: String, classLevel: String, district: String, school: String, targetExam: String, stream: String, studyGoals: List<String>, phone: String, gender: String, dob: String) -> Unit,
    onNavigateToAuth: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    authUserState: com.example.data.auth.AuthUserState? = null
) {
    // Navigation State
    var currentStep by rememberSaveable { mutableIntStateOf(1) }

    // Form State
    val defaultOnboardingName = remember(profile.name, authUserState) {
        when {
            !profile.name.isBlank() -> profile.name
            authUserState?.displayName?.isNotBlank() == true -> authUserState.displayName
            authUserState?.email?.isNotBlank() == true -> authUserState.email.substringBefore("@")
            else -> ""
        }
    }
    var name by rememberSaveable(defaultOnboardingName) { mutableStateOf(defaultOnboardingName) }
    var phone by rememberSaveable { mutableStateOf(profile.phoneNumber) }
    var gender by rememberSaveable { mutableStateOf(profile.gender.ifBlank { "male" }) }
    var dob by rememberSaveable { mutableStateOf(profile.dateOfBirth) }

    val initialClassObj = remember(profile.classId) { AcademicClassProvider.getClassById(profile.classId) }
    var selectedBoardId by rememberSaveable { mutableStateOf(initialClassObj?.board ?: "WBBSE") }
    var selectedClassId by rememberSaveable { mutableStateOf(profile.classId.ifBlank { "class_10_wbbse" }) }
    var selectedClassNumber by rememberSaveable { mutableIntStateOf(initialClassObj?.classNumber ?: 10) }
    var classLevel by rememberSaveable { mutableStateOf(profile.classLevel.ifBlank { "মাধ্যমিক - WBBSE" }) }
    var selectedStream by rememberSaveable { mutableStateOf(profile.stream.ifBlank { "all" }) }
    var targetExam by rememberSaveable { mutableStateOf(profile.targetExam.ifBlank { "WBBSE Madhyamik 2027" }) }
    var district by rememberSaveable { mutableStateOf(profile.district.ifBlank { "কলকাতা" }) }
    var block by rememberSaveable { mutableStateOf("সদর ব্লক") }
    var school by rememberSaveable { mutableStateOf(profile.school) }

    // Storing multiple goals as comma separated string for easy saveable
    val defaultGoalsStr = remember(profile.studyGoals) {
        if (profile.studyGoals.isNotEmpty()) {
            profile.studyGoals.joinToString(",")
        } else {
            "study_daily,prepare_exams,mock_practice,syllabus_completion"
        }
    }
    var selectedGoalsStr by rememberSaveable { mutableStateOf(defaultGoalsStr) }
    val selectedGoals = remember(selectedGoalsStr) {
        if (selectedGoalsStr.isBlank()) emptyList<String>() else selectedGoalsStr.split(",")
    }

    BackHandler(enabled = currentStep in 2..4) {
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
            if (currentStep < 5) {
                OnboardingHeader(
                    currentStep = currentStep,
                    totalSteps = 4,
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (step) {
                            1 -> Step1Name(
                                name = name,
                                onNameChange = { name = it },
                                phone = phone,
                                onPhoneChange = { phone = it },
                                gender = gender,
                                onGenderChange = { gender = it },
                                dob = dob,
                                onDobChange = { dob = it },
                                isBengali = isBengali,
                                onNext = { currentStep++ }
                            )
                            2 -> Step2Class(
                                selectedBoardId = selectedBoardId,
                                onBoardChange = { newBoard ->
                                    if (selectedBoardId != newBoard) {
                                        selectedBoardId = newBoard
                                        selectedClassId = "" // clear selected class when board changes
                                    }
                                },
                                selectedClassId = selectedClassId,
                                selectedClassNumber = selectedClassNumber,
                                selectedStream = selectedStream,
                                onClassChange = { clsId, clsNum, clsLvl, exam, stream ->
                                    selectedClassId = clsId
                                    selectedClassNumber = clsNum
                                    classLevel = clsLvl
                                    targetExam = exam
                                    selectedStream = stream
                                },
                                isBengali = isBengali,
                                onNext = { currentStep++ }
                            )
                            3 -> Step3LocationAndSchool(
                                district = district,
                                onDistrictChange = { newDist ->
                                    district = newDist
                                    val defaultBlocks = com.example.util.WestBengalData.getBlocksForDistrict(newDist)
                                    if (defaultBlocks.isNotEmpty()) {
                                        block = defaultBlocks.first()
                                    }
                                    val suggestedSchools = districtSchoolsMap[newDist] ?: emptyList()
                                    if (suggestedSchools.isNotEmpty()) {
                                        school = suggestedSchools.first()
                                    } else {
                                        school = ""
                                    }
                                },
                                block = block,
                                onBlockChange = { newBlock ->
                                    block = newBlock
                                    val blockSchools = com.example.util.WestBengalData.getSchoolsForBlock(newBlock, district)
                                    if (blockSchools.isNotEmpty()) {
                                        school = blockSchools.first()
                                    }
                                },
                                school = school,
                                onSchoolChange = { school = it },
                                selectedClassNumber = selectedClassNumber,
                                districts = districts,
                                availableSchools = com.example.util.WestBengalData.getSchoolsForBlock(block, district),
                                isLoadingDistricts = isLoadingDistricts,
                                isBengali = isBengali,
                                onNext = { currentStep++ }
                            )
                            4 -> Step5Goals(
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
                            5 -> Step6Complete(
                                name = name,
                                classLevel = classLevel,
                                district = district,
                                school = school,
                                phone = phone,
                                gender = gender,
                                dob = dob,
                                targetExam = targetExam,
                                selectedGoals = selectedGoals,
                                isBengali = isBengali,
                                onFinish = {
                                    onCompleteOnboarding(
                                        name,
                                        classLevel,
                                        district,
                                        school,
                                        targetExam,
                                        selectedStream,
                                        selectedGoals,
                                        phone,
                                        gender,
                                        dob
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
// STEP 1: PERSONAL DETAILS (Name, Phone, Gender, Date of Birth)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1Name(
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    dob: String,
    onDobChange: (String) -> Unit,
    isBengali: Boolean,
    onNext: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logo
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AppPrimaryStart, AppPrimaryEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "স্বাগতম! 👋",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "আপনার নাম, মোবাইল, জেন্ডার ও জন্ম তারিখ দিন।",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. FULL NAME
                Column {
                    Text(
                        text = "১. শিক্ষার্থীর পুরো নাম",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        placeholder = { Text("আপনার পুরো নাম", maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AppPrimaryAccent) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedLabelColor = Color(0xFF818CF8),
                            unfocusedLabelColor = AppTextSecondary,
                            focusedTextColor = AppTextPrimary,
                            unfocusedTextColor = AppTextPrimary,
                            focusedContainerColor = AppInput.copy(alpha = 0.4f),
                            unfocusedContainerColor = AppInput.copy(alpha = 0.4f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 2. MOBILE NUMBER
                Column {
                    Text(
                        text = "২. মোবাইল নম্বর",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = onPhoneChange,
                        placeholder = { Text("১০ ডিজিটের মোবাইল নম্বর", maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AppPrimaryAccent) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedLabelColor = Color(0xFF818CF8),
                            unfocusedLabelColor = AppTextSecondary,
                            focusedTextColor = AppTextPrimary,
                            unfocusedTextColor = AppTextPrimary,
                            focusedContainerColor = AppInput.copy(alpha = 0.4f),
                            unfocusedContainerColor = AppInput.copy(alpha = 0.4f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. GENDER SELECTION
                Column {
                    Text(
                        text = "৩. লিঙ্গ",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    val genderOptions = listOf(
                        Triple("male", "👦 ছাত্র", "Male"),
                        Triple("female", "👧 ছাত্রী", "Female"),
                        Triple("other", "✨ অন্যান্য", "Other")
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        genderOptions.forEach { (id, label, _) ->
                            val isSelected = gender.equals(id, ignoreCase = true)
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF2E1065).copy(alpha = 0.6f) else AppInput.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF818CF8) else Color(0x33FFFFFF)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable { onGenderChange(id) }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        ),
                                        color = if (isSelected) AppTextPrimary else AppTextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. DATE OF BIRTH
                Column {
                    Text(
                        text = "৪. জন্ম তারিখ",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    fun openDatePicker() {
                        val cal = java.util.Calendar.getInstance()
                        val year = cal.get(java.util.Calendar.YEAR) - 15
                        val month = cal.get(java.util.Calendar.MONTH)
                        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)

                        android.app.DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                val formattedDay = selectedDay.toString().padStart(2, '0')
                                val formattedMonth = (selectedMonth + 1).toString().padStart(2, '0')
                                onDobChange("$formattedDay/$formattedMonth/$selectedYear")
                            },
                            year, month, day
                        ).show()
                    }

                    OutlinedTextField(
                        value = dob,
                        onValueChange = onDobChange,
                        placeholder = { Text("DD/MM/YYYY", maxLines = 1) },
                        leadingIcon = { 
                            IconButton(onClick = { openDatePicker() }) {
                                Icon(Icons.Default.Event, contentDescription = "Pick Date", tint = AppPrimaryAccent) 
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = { openDatePicker() }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = AppTextSecondary)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedLabelColor = Color(0xFF818CF8),
                            unfocusedLabelColor = AppTextSecondary,
                            focusedTextColor = AppTextPrimary,
                            unfocusedTextColor = AppTextPrimary,
                            focusedContainerColor = AppInput.copy(alpha = 0.4f),
                            unfocusedContainerColor = AppInput.copy(alpha = 0.4f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Fixed Bottom Button
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            OnboardingPrimaryButton(
                text = if (isBengali) "পরবর্তী →" else "Next →",
                enabled = name.trim().isNotBlank(),
                onClick = onNext
            )
        }
    }
}

@Composable
fun ClassSelectionItem(
    cls: com.example.data.model.AcademicClass,
    isSelected: Boolean,
    isBengali: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val displayTitle = when {
        !isBengali -> "Class ${cls.classNumber}"
        cls.nameBengali.isNotBlank() -> cls.nameBengali
        else -> cls.name
    }

    Box(
        modifier = modifier.padding(top = 8.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) Color.Transparent else AppInput.copy(alpha = 0.4f),
            border = if (isSelected) null else BorderStroke(1.dp, Color(0x33FFFFFF)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .then(
                    if (isSelected) Modifier.background(
                        Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF06B6D4))),
                        shape = RoundedCornerShape(20.dp)
                    ) else Modifier
                )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = if (isSelected) Color.White else AppTextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        if (isSelected) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF0D9488),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
            ) {
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STEP 2: BOARD & CLASS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
private fun getOnboardingDynamicTargetExam(classId: String, stream: String): String {
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val nextYear = currentYear + 1
    return when (classId) {
        "class_10_wbbse" -> "WBBSE Madhyamik $nextYear"
        "class_11_wbchse" -> {
            val sem = if (stream.contains("sem2")) 2 else 1
            val year = if (sem == 2) nextYear else currentYear
            "WBCHSE Class 11 Sem $sem Exam $year"
        }
        "class_12_wbchse" -> {
            val sem = if (stream.contains("sem4")) 4 else 3
            val year = if (sem == 4) nextYear else currentYear
            "WBCHSE Class 12 Sem $sem Exam $year"
        }
        else -> {
            val cls = com.example.data.model.AcademicClassProvider.ALL_CLASSES.find { it.id == classId }
            cls?.targetExam ?: "Exam $currentYear"
        }
    }
}

@Composable
fun Step2Class(
    selectedBoardId: String,
    onBoardChange: (String) -> Unit,
    selectedClassId: String,
    selectedClassNumber: Int,
    selectedStream: String,
    onClassChange: (classId: String, classNum: Int, classLevel: String, targetExam: String, stream: String) -> Unit,
    isBengali: Boolean,
    onNext: () -> Unit
) {
    val allBoards by AcademicClassProvider.ALL_BOARDS_FLOW.collectAsState()
    val allClasses by AcademicClassProvider.ALL_CLASSES_FLOW.collectAsState()

    val initialSemId = remember(selectedStream) {
        if (selectedStream.contains("_sem")) selectedStream.substringAfter("_") else null
    }
    var onboardingSemesterId by remember(selectedClassId) { mutableStateOf(initialSemId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Screen Title & Subtitle
            Text(
                text = if (isBengali) "শিক্ষা বোর্ড ও শ্রেণি নির্বাচন" else "Select Board & Class",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = AppTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isBengali) "আপনার সিলেবাস ও মক টেস্ট পার্সোনালাইজ করুন" else "Personalize your syllabus and mock tests",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // SECTION 1: EDUCATION BOARD
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = AppTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isBengali) "এডুকেশন বোর্ড" else "Education Board",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTextPrimary
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allBoards.forEach { board ->
                    val isBoardSelected = selectedBoardId.equals(board.id, ignoreCase = true)
                    val boardLabel = when {
                        isBengali && board.fullNameBengali.isNotBlank() -> board.fullNameBengali
                        board.id.equals("WBBPE", ignoreCase = true) -> "West Bengal Primary Board (WBBPE)"
                        board.id.equals("WBBSE", ignoreCase = true) -> "West Bengal Board (WBBSE - Madhyamik)"
                        board.id.equals("WBCHSE", ignoreCase = true) -> "WBCHSE (Higher Secondary 11th & 12th)"
                        else -> board.fullNameBengali.ifBlank { board.shortName }
                    }

                    Surface(
                        onClick = { onBoardChange(board.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isBoardSelected) Color(0xFF2E1065).copy(alpha = 0.6f) else AppInput.copy(alpha = 0.4f),
                        border = BorderStroke(
                            width = if (isBoardSelected) 2.dp else 1.dp,
                            color = if (isBoardSelected) Color(0xFF818CF8) else Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = boardLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = AppTextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Check / Radio indicator
                            if (isBoardSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF06B6D4)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color(0xFF475569), CircleShape)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // SECTION 2: CLASS SELECTION
            val selectedBoard = allBoards.find { it.id.equals(selectedBoardId, ignoreCase = true) }
            val classesForBoard = remember(selectedBoardId, allClasses) {
                if (selectedBoardId.isBlank()) emptyList()
                else {
                    val list = allClasses.filter { it.board.equals(selectedBoardId, ignoreCase = true) }
                    if (list.isNotEmpty()) list
                    else allClasses.filter { it.board.contains(selectedBoardId, ignoreCase = true) || selectedBoardId.contains(it.board, ignoreCase = true) }
                        .ifEmpty { allClasses }
                }
            }

            Text(
                text = if (isBengali) "শ্রেণি নির্বাচন" else "Class Selection",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (selectedBoardId.isBlank()) {
                Text(
                    text = if (isBengali) "প্রথমে বোর্ড নির্বাচন করুন" else "Please select an Education Board first",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTextSecondary
                )
            } else {
                if (classesForBoard.size <= 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        classesForBoard.forEach { cls ->
                            val isSelected = selectedClassId.equals(cls.id, ignoreCase = true)
                            ClassSelectionItem(
                                cls = cls,
                                isSelected = isSelected,
                                isBengali = isBengali,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (cls.classNumber in 11..12) {
                                        onClassChange(
                                            cls.id,
                                            cls.classNumber,
                                            if (isBengali) cls.nameBengali else cls.nameEnglish,
                                            cls.targetExam,
                                            "" // empty stream initially, forcing them to pick sem & stream
                                        )
                                        onboardingSemesterId = null
                                    } else {
                                        val stream = "all"
                                        val exam = getOnboardingDynamicTargetExam(cls.id, stream)
                                        onClassChange(
                                            cls.id,
                                            cls.classNumber,
                                            if (isBengali) cls.nameBengali else cls.nameEnglish,
                                            exam,
                                            stream
                                        )
                                    }
                                }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(classesForBoard) { cls ->
                            val isSelected = selectedClassId.equals(cls.id, ignoreCase = true)
                            ClassSelectionItem(
                                cls = cls,
                                isSelected = isSelected,
                                isBengali = isBengali,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    if (cls.classNumber in 11..12) {
                                        onClassChange(
                                            cls.id,
                                            cls.classNumber,
                                            if (isBengali) cls.nameBengali else cls.nameEnglish,
                                            cls.targetExam,
                                            "" // empty stream initially, forcing them to pick sem & stream
                                        )
                                        onboardingSemesterId = null
                                    } else {
                                        val stream = "all"
                                        val exam = getOnboardingDynamicTargetExam(cls.id, stream)
                                        onClassChange(
                                            cls.id,
                                            cls.classNumber,
                                            if (isBengali) cls.nameBengali else cls.nameEnglish,
                                            exam,
                                            stream
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // SECTION 3: SEMESTER SELECTION (When Class 11 or 12 selected)
            AnimatedVisibility(
                visible = selectedClassNumber in listOf(11, 12) && selectedClassId.isNotBlank(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Text(
                        text = if (isBengali) "প্রথমে আপনার সিলেবাসের সেমিস্টার নির্বাচন করুন:" else "First, select your syllabus semester:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val semesters = if (selectedClassId == "class_11_wbchse") {
                        listOf(
                            "sem1" to Pair("সেমিস্টার ১", "প্রথম সেমিস্টার - MCQ ভিত্তিক পরীক্ষা"),
                            "sem2" to Pair("সেমিস্টার ২", "দ্বিতীয় সেমিস্টার - সংক্ষিপ্ত প্রশ্ন ভিত্তিক পরীক্ষা")
                        )
                    } else {
                        listOf(
                            "sem3" to Pair("সেমিস্টার ৩", "তৃতীয় সেমিস্টার - MCQ ভিত্তিক পরীক্ষা"),
                            "sem4" to Pair("সেমিস্টার ৪", "চতুর্থ সেমিস্টার - সংক্ষিপ্ত প্রশ্ন ভিত্তিক পরীক্ষা")
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        semesters.forEach { (semId, semNames) ->
                            val isSemSelected = onboardingSemesterId == semId
                            Surface(
                                onClick = {
                                    onboardingSemesterId = semId
                                    // Reset selected stream when semester changes so they must pick stream again
                                    val cls = allClasses.find { it.id.equals(selectedClassId, ignoreCase = true) }
                                    if (cls != null) {
                                        onClassChange(
                                            cls.id,
                                            cls.classNumber,
                                            if (isBengali) cls.nameBengali else cls.nameEnglish,
                                            cls.targetExam,
                                            "" // clear stream selection on semester change
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSemSelected) Color(0xFF1E1B4B).copy(alpha = 0.6f) else AppInput.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    width = if (isSemSelected) 2.dp else 1.dp,
                                    color = if (isSemSelected) Color(0xFF818CF8) else Color(0x33FFFFFF)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSemSelected) Color(0xFF4338CA) else Color(0x1AFFFFFF),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = semNames.first,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = AppTextPrimary
                                        )
                                        Text(
                                            text = semNames.second,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppTextSecondary
                                        )
                                    }
                                    if (isSemSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF818CF8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: STREAM SELECTION (When semester is selected)
            AnimatedVisibility(
                visible = selectedClassNumber in listOf(11, 12) && selectedClassId.isNotBlank() && onboardingSemesterId != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Text(
                        text = if (isBengali) "আপনার পড়ার বিভাগ নির্বাচন করুন:" else "Select your academic stream:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val streams = listOf(
                        Triple("science", "Science 🧪", "বিজ্ঞান 🧪"),
                        Triple("arts", "Arts 📚", "কলা 📚"),
                        Triple("commerce", "Commerce 💼", "বাণিজ্য 💼")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        streams.forEach { (stId, labelEn, labelBn) ->
                            val currentCombinedStream = "${stId}_$onboardingSemesterId"
                            val isStreamSelected = selectedStream == currentCombinedStream
                            val streamText = if (isBengali) labelBn else labelEn

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 8.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        val cls = allClasses.find { it.id.equals(selectedClassId, ignoreCase = true) }
                                        if (cls != null) {
                                            val semNum = onboardingSemesterId?.replace("sem", "") ?: "1"
                                            val semBengali = when (semNum) {
                                                "1" -> "১"
                                                "2" -> "২"
                                                "3" -> "৩"
                                                "4" -> "৪"
                                                else -> semNum
                                            }
                                            val strLabelBn = when (stId) {
                                                "science" -> "বিজ্ঞান"
                                                "arts" -> "কলা"
                                                "commerce" -> "বাণিজ্য"
                                                else -> "সাধারণ"
                                            }
                                            val strLabelEn = when (stId) {
                                                "science" -> "Science"
                                                "arts" -> "Arts"
                                                "commerce" -> "Commerce"
                                                else -> "General"
                                            }
                                            val classLevelText = if (isBengali) {
                                                "${cls.nameBengali} • $strLabelBn • সেমিস্টার $semBengali"
                                            } else {
                                                "${cls.nameEnglish} • $strLabelEn • Semester $semNum"
                                            }
                                            val combinedStream = "${stId}_$onboardingSemesterId"
                                            val dynamicExam = getOnboardingDynamicTargetExam(cls.id, combinedStream)

                                            onClassChange(
                                                cls.id,
                                                cls.classNumber,
                                                classLevelText,
                                                dynamicExam,
                                                combinedStream
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isStreamSelected) Color(0xFF1E1B4B).copy(alpha = 0.6f) else AppInput.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        width = if (isStreamSelected) 2.dp else 1.dp,
                                        color = if (isStreamSelected) Color(0xFF818CF8) else Color(0x33FFFFFF)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = streamText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isStreamSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isStreamSelected) Color.White else AppTextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Floating "Selected" badge above selected stream
                                if (isStreamSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4338CA),
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .offset(y = (-8).dp)
                                    ) {
                                        Text(
                                            text = if (isBengali) "নির্বাচিত" else "Selected",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Fixed Bottom Button
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            OnboardingPrimaryButton(
                text = if (isBengali) "পরবর্তী ধাপ →" else "Next Step →",
                enabled = selectedBoardId.isNotBlank() && selectedClassId.isNotBlank() && (selectedClassNumber !in listOf(11, 12) || selectedStream.contains("_sem")),
                onClick = onNext
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STEP 3: DISTRICT, BLOCK & SCHOOL (All in one Screen)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3LocationAndSchool(
    district: String,
    onDistrictChange: (String) -> Unit,
    block: String,
    onBlockChange: (String) -> Unit,
    school: String,
    onSchoolChange: (String) -> Unit,
    selectedClassNumber: Int = 10,
    districts: List<String>,
    availableSchools: List<String>,
    isLoadingDistricts: Boolean,
    isBengali: Boolean,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "জেলা, ব্লক ও বিদ্যালয় নির্বাচন",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "স্থান ও বিদ্যালয়ের তথ্য সম্পূর্ণ করুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. DISTRICT SELECTOR
                var districtExpanded by remember { mutableStateOf(false) }
                
                Column {
                    Text(
                        text = "১. জেলা",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = districtExpanded,
                        onExpandedChange = { districtExpanded = !districtExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = district,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("জেলা নির্বাচন করুন", maxLines = 1) },
                            leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, tint = AppPrimaryAccent) },
                            trailingIcon = {
                                if (isLoadingDistricts) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AppPrimaryAccent)
                                } else {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedLabelColor = Color(0xFF818CF8),
                                unfocusedLabelColor = AppTextSecondary,
                                focusedTextColor = AppTextPrimary,
                                unfocusedTextColor = AppTextPrimary,
                                focusedContainerColor = AppInput.copy(alpha = 0.4f),
                                unfocusedContainerColor = AppInput.copy(alpha = 0.4f)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = { districtExpanded = false },
                            modifier = Modifier.background(AppCard.copy(alpha = 0.95f))
                        ) {
                            districts.forEach { dist ->
                                DropdownMenuItem(
                                    text = { Text(dist, color = AppTextPrimary) },
                                    onClick = {
                                        onDistrictChange(dist)
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 2. BLOCK SELECTOR / INPUT
                var blockExpanded by remember { mutableStateOf(false) }
                val defaultBlocks = remember(district) {
                    com.example.util.WestBengalData.getBlocksForDistrict(district)
                }
                
                Column {
                    Text(
                        text = "২. ব্লক / থানা",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = blockExpanded,
                        onExpandedChange = { blockExpanded = !blockExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = block,
                            onValueChange = { onBlockChange(it) },
                            placeholder = { Text("ব্লকের নাম লিখুন বা বাছুন", maxLines = 1) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = AppPrimaryAccent) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = blockExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedLabelColor = Color(0xFF818CF8),
                                unfocusedLabelColor = AppTextSecondary,
                                focusedTextColor = AppTextPrimary,
                                unfocusedTextColor = AppTextPrimary,
                                focusedContainerColor = AppInput.copy(alpha = 0.4f),
                                unfocusedContainerColor = AppInput.copy(alpha = 0.4f)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = blockExpanded,
                            onDismissRequest = { blockExpanded = false },
                            modifier = Modifier.background(AppCard.copy(alpha = 0.95f))
                        ) {
                            defaultBlocks.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text(b, color = AppTextPrimary) },
                                    onClick = {
                                        onBlockChange(b)
                                        blockExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 3. SCHOOL SEARCH & SELECTOR WITH TYPE AUTO-FILTERING
                var searchQuery by remember { mutableStateOf("") }
                var activeCategoryFilter by remember { mutableStateOf("AUTO") }

                val classCategoryBadge = remember(selectedClassNumber, activeCategoryFilter) {
                    when {
                        activeCategoryFilter == "PRIMARY" || (activeCategoryFilter == "AUTO" && selectedClassNumber in 1..4) -> "🟢 ১ম-৪র্থ"
                        activeCategoryFilter == "JUNIOR_HIGH" -> "🔵 ৫ম-৮ম"
                        else -> "🟣 ৫ম-১২ম"
                    }
                }

                val schoolsForCategory = remember(block, district, selectedClassNumber, activeCategoryFilter) {
                    com.example.util.WestBengalData.getSchoolsForClassAndBlock(block, district, selectedClassNumber, activeCategoryFilter)
                }

                val filteredSchools = remember(searchQuery, schoolsForCategory) {
                    if (searchQuery.isBlank()) schoolsForCategory
                    else schoolsForCategory.filter { it.contains(searchQuery, ignoreCase = true) }
                }
                
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "৩. বিদ্যালয়",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2E1065).copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(0xFF818CF8))
                        ) {
                            Text(
                                text = classCategoryBadge,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // School Type Category Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filters = listOf(
                            "AUTO" to "অটো",
                            "PRIMARY" to "১ম-৪র্থ",
                            "HIGH_SCHOOL" to "৫ম-১২ম",
                            "JUNIOR_HIGH" to "৫ম-৮ম"
                        )
                        filters.forEach { (key, label) ->
                            val isSelected = activeCategoryFilter == key
                            Surface(
                                onClick = { activeCategoryFilter = key },
                                shape = CircleShape,
                                color = if (isSelected) Color(0xFF4F46E5).copy(alpha = 0.8f) else AppInput.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF818CF8) else Color(0x22FFFFFF))
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.White else AppTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    var schoolExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = schoolExpanded,
                        onExpandedChange = { schoolExpanded = !schoolExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = school,
                            onValueChange = { 
                                onSchoolChange(it)
                                schoolExpanded = true
                            },
                            placeholder = { Text("স্কুলের নাম লিখুন বা বাছুন", maxLines = 1) },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = AppPrimaryAccent) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schoolExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedLabelColor = Color(0xFF818CF8),
                                unfocusedLabelColor = AppTextSecondary,
                                focusedTextColor = AppTextPrimary,
                                unfocusedTextColor = AppTextPrimary,
                                focusedContainerColor = AppInput.copy(alpha = 0.4f),
                                unfocusedContainerColor = AppInput.copy(alpha = 0.4f)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = schoolExpanded,
                            onDismissRequest = { schoolExpanded = false },
                            modifier = Modifier
                                .background(AppCard)
                                .heightIn(max = 280.dp)
                        ) {
                            if (filteredSchools.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("কোনো স্কুল পাওয়া যায়নি", color = AppTextSecondary) },
                                    onClick = {},
                                    enabled = false
                                )
                            } else {
                                filteredSchools.forEach { s ->
                                    val isSelected = school == s
                                    val schoolTags = com.example.util.getSchoolInfo(s).getTags()
                                    
                                    DropdownMenuItem(
                                        text = {
                                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                Text(
                                                    text = s,
                                                    color = if (isSelected) Color(0xFF818CF8) else AppTextPrimary,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                )
                                                if (schoolTags.isNotEmpty()) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    ) {
                                                        schoolTags.take(3).forEach { tag ->
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                                                                border = BorderStroke(0.5.dp, Color(0x22FFFFFF))
                                                            ) {
                                                                Text(
                                                                    text = tag,
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                    color = AppTextSecondary,
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        trailingIcon = {
                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppPrimaryAccent, modifier = Modifier.size(18.dp))
                                            }
                                        },
                                        onClick = {
                                            onSchoolChange(s)
                                            schoolExpanded = false
                                        }
                                    )
                                }
                            }

                            if (school.isNotBlank() && !availableSchools.contains(school)) {
                                HorizontalDivider(color = Color(0x22FFFFFF), thickness = 0.5.dp)
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp)) },
                                    text = {
                                        Text(
                                            text = "‘$school’ নতুন স্কুল হিসেবে যোগ করুন",
                                            color = Color(0xFF38BDF8),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        com.example.util.WestBengalData.addSchoolToBlock(block, district, school)
                                        onSchoolChange(school)
                                        schoolExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Fixed Bottom Button
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            OnboardingPrimaryButton(
                text = "পরবর্তী →",
                enabled = district.trim().isNotBlank() && school.trim().isNotBlank(),
                onClick = onNext
            )
        }
    }
}

private data class GoalOptionData(
    val id: String,
    val titleBn: String,
    val subtitleBn: String,
    val icon: ImageVector
)

@Composable
fun GoalCard(
    titleBn: String,
    subtitleBn: String,
    isSelected: Boolean,
    defaultIcon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) Color(0xFF1E1B4B).copy(alpha = 0.6f) else AppCard.copy(alpha = 0.35f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF818CF8) else Color(0x33FFFFFF)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF06B6D4)))
                        else androidx.compose.ui.graphics.SolidColor(AppInput.copy(alpha = 0.5f))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Check else defaultIcon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else AppTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleBn,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitleBn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTextSecondary
                )
            }
        }
    }
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
    val goalOptions = remember {
        listOf(
            GoalOptionData(
                id = "study_daily",
                titleBn = "প্রতিদিনের রুটিন মেনে চলা",
                subtitleBn = "প্রতিদিন নির্দিষ্ট সময় ধরে নিয়মিত চর্চা করা",
                icon = Icons.Default.EventAvailable
            ),
            GoalOptionData(
                id = "prepare_exams",
                titleBn = "পরীক্ষায় সেরা প্রস্তুতি নেওয়া",
                subtitleBn = "বোর্ড পরীক্ষার জন্য সম্পূর্ণ সিলেবাস শেষ করা",
                icon = Icons.Default.Assignment
            ),
            GoalOptionData(
                id = "improve_weak",
                titleBn = "কঠিন বিষয় সহজে শেখা",
                subtitleBn = "গণিত ও বিজ্ঞানের কঠিন অধ্যায় সহজে আয়ত্ত করা",
                icon = Icons.Default.Psychology
            ),
            GoalOptionData(
                id = "mock_practice",
                titleBn = "বিগত বছরের প্রশ্ন সমাধান",
                subtitleBn = "টেস্ট পেপার ও কুইজ নিয়মিত অনুশীলন করা",
                icon = Icons.Default.Quiz
            ),
            GoalOptionData(
                id = "top_marks",
                titleBn = "পরীক্ষায় সর্বোচ্চ নম্বর পাওয়া",
                subtitleBn = "সব বিষয়ে সেরা নম্বর ও ভালো ফলাফল অর্জন করা",
                icon = Icons.Default.EmojiEvents
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "আপনার পড়াশোনার লক্ষ্য কী?",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = AppTextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "আপনার জন্য সঠিক পড়াশোনার অভিজ্ঞতা তৈরি করতে সাহায্য করবে",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                goalOptions.forEach { goal ->
                    val isSelected = selectedGoals.contains(goal.id) || 
                            selectedGoals.contains(goal.titleBn) || 
                            selectedGoals.contains(goal.subtitleBn)
                    
                    GoalCard(
                        titleBn = goal.titleBn,
                        subtitleBn = goal.subtitleBn,
                        isSelected = isSelected,
                        defaultIcon = goal.icon,
                        onClick = { onGoalToggle(goal.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Fixed Bottom Button
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            OnboardingPrimaryButton(
                text = "আমার পড়াশোনা শুরু করি →",
                enabled = selectedGoals.isNotEmpty(),
                onClick = onNext
            )
        }
    }
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
    phone: String = "",
    gender: String = "",
    dob: String = "",
    targetExam: String = "",
    selectedGoals: List<String> = emptyList(),
    isBengali: Boolean,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AppPrimaryStart, AppPrimaryEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Celebration, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "সব প্রস্তুত! 🎉",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = AppTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "স্বাগতম, $name!",
                style = MaterialTheme.typography.titleMedium,
                color = AppTextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryItem(Icons.Default.Person, if (isBengali) "নাম" else "Name", name)
                if (phone.isNotBlank()) {
                    SummaryItem(Icons.Default.Phone, if (isBengali) "মোবাইল নম্বর" else "Mobile", phone)
                }
                if (dob.isNotBlank() || gender.isNotBlank()) {
                    val genderLabel = when (gender.lowercase()) {
                        "male" -> "পুরুষ"
                        "female" -> "মহিলা"
                        "other" -> "অন্যান্য"
                        else -> gender
                    }
                    val dobGenderStr = listOfNotNull(
                        dob.ifBlank { null },
                        genderLabel.ifBlank { null }
                    ).joinToString(" • ")
                    SummaryItem(Icons.Default.Badge, if (isBengali) "জন্মতারিখ ও লিঙ্গ" else "DOB & Gender", dobGenderStr)
                }
                SummaryItem(Icons.Default.School, if (isBengali) "শ্রেণি ও বোর্ড" else "Class & Board", classLevel)
                if (targetExam.isNotBlank()) {
                    SummaryItem(Icons.Default.Star, if (isBengali) "টার্গেট পরীক্ষা" else "Target Exam", targetExam)
                }
                SummaryItem(Icons.Default.LocationOn, if (isBengali) "জেলা" else "District", district)
                SummaryItem(Icons.Default.LocationCity, if (isBengali) "বিদ্যালয়" else "School", school)
                if (selectedGoals.isNotEmpty()) {
                    SummaryItem(
                        Icons.Default.TrackChanges,
                        if (isBengali) "পড়াশোনার লক্ষ্য" else "Study Goals",
                        if (isBengali) "${selectedGoals.size}টি লক্ষ্য নির্বাচন করা হয়েছে" else "${selectedGoals.size} Goals Selected"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isBengali) "আপনার পার্সোনালাইজড পড়াশোনা ও প্রস্তুতির অভিজ্ঞতা এখন শুরু হতে চলেছে।" else "Your personalized learning journey is about to begin.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Fixed Bottom Button
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            OnboardingPrimaryButton(
                text = "ড্যাশবোর্ডে যান →",
                enabled = true,
                onClick = onFinish
            )
        }
    }
}

@Composable
fun SummaryItem(icon: ImageVector, label: String, text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppInput.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E1065).copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = AppTextSecondary)
                Text(text = text.ifBlank { "N/A" }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = AppTextPrimary)
            }
        }
    }
}


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// REUSABLE COMPONENTS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun SegmentedProgressBar(currentStep: Int, totalSteps: Int = 5) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 1..totalSteps) {
            val isFilled = i <= currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFilled) Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF6366F1))
                        )
                        else androidx.compose.ui.graphics.SolidColor(AppActiveSurface.copy(alpha = 0.5f))
                    )
            )
        }
    }
}

@Composable
fun OnboardingHeader(
    currentStep: Int,
    totalSteps: Int = 5,
    onBack: (() -> Unit)?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onBack != null) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = AppCard.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(38.dp))
            }

            // Step Badge Pill
            Surface(
                shape = CircleShape,
                color = Color(0x1A38BDF8),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
            ) {
                Text(
                    text = "$currentStep / $totalSteps",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.size(38.dp))
        }

        SegmentedProgressBar(currentStep = currentStep, totalSteps = totalSteps)
    }
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
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = AppActiveSurface.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF06B6D4)))
                    else Brush.horizontalGradient(listOf(AppActiveSurface.copy(alpha = 0.4f), AppActiveSurface.copy(alpha = 0.4f))),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else AppTextSecondary
            )
        }
    }
}

@Composable
fun SelectableOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 60.dp,
    icon: ImageVector? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF818CF8) else Color(0x33FFFFFF),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E1B4B).copy(alpha = 0.6f) else AppCard.copy(alpha = 0.35f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) AppPrimaryAccent else AppTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = if (height <= 60.dp) 13.sp else 15.sp
                    ),
                    color = if (isSelected) AppTextPrimary else AppTextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
