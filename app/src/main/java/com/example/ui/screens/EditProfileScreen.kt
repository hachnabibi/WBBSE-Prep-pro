package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicClass
import com.example.data.model.AcademicClassProvider
import com.example.data.model.StudentProfile
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.ExamGold
import com.example.ui.theme.ExamFlame
import com.example.ui.viewmodel.DeshExamViewModel

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

    // Mutable states for input values
    var editName by remember(profile.name) { mutableStateOf(profile.name) }
    var editSchool by remember(profile.school) { mutableStateOf(profile.school) }
    var editDistrict by remember(profile.district) { mutableStateOf(profile.district) }
    
    // Manage currently selected academic class object
    val allClassesList = remember { AcademicClassProvider.ALL_CLASSES }
    var selectedClass by remember(profile.classId) { 
        mutableStateOf(allClassesList.find { it.id == profile.classId } ?: allClassesList.first()) 
    }
    
    // Manage stream selection for class 11 and 12
    var selectedStream by remember(profile.stream) { mutableStateOf(profile.stream) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("edit_profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Avatar & Header banner
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
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                        .padding(20.dp)
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
                                .border(2.dp, ExamGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = editName.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = PrimaryIndigo
                            )
                        }
                        Column {
                            Text(
                                text = "প্রোফাইল সংশোধন",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "আপনার সঠিক ক্লাস ও বিদ্যালয়ের তথ্য আপডেট করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Form Fields Container
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
                    Text(
                        text = "ব্যক্তিগত তথ্য (Basic Info)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 1. Name Input
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("শিক্ষার্থীর নাম") },
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
                            label = { Text("বিদ্যালয়ের নাম") },
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
                                    text = { Text("কোনো স্কুল পাওয়া যায়নি") },
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

        // Beautiful Interactive Class Selection Card Section
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "আমার শ্রেণি নির্বাচন (Select Academic Class)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBengali) "অ্যাপের সম্পূর্ণ সিলেবাস এবং সমস্ত মক টেস্ট আপনি যে ক্লাস নির্বাচন করবেন তার উপর ভিত্তি করে অটো-ফাইল্টার হবে।" 
                        else "App curriculum and mocks dynamically adjust based on the selected class below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Render class selection cards with beautiful indicators
                    allClassesList.forEach { cls ->
                        val isSelected = selectedClass.id == cls.id
                        val borderColor = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        val containerColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(containerColor)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedClass = cls
                                }
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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${cls.classNumber}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column {
                                    Text(
                                        text = "${cls.classNumber}ম শ্রেণী (${cls.board})",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = cls.targetExam,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedClass = cls }
                            )
                        }
                    }
                }
            }
        }

        // Show Stream Selection only if Class 11 or 12 is chosen
        val isSeniorSecondary = selectedClass.id in listOf("class_11_wbchse", "class_12_wbchse")
        if (isSeniorSecondary) {
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "স্ট্রিম নির্বাচন (Choose Stream)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        listOf("science" to "Science / বিজ্ঞান", "commerce" to "Commerce / বাণিজ্য", "arts" to "Arts / কলা").forEach { (streamKey, streamLabel) ->
                            val isStreamSelected = selectedStream == streamKey
                            val streamBg = if (isStreamSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = streamBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedStream = streamKey }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = streamLabel, fontWeight = FontWeight.Bold)
                                    RadioButton(
                                        selected = isStreamSelected,
                                        onClick = { selectedStream = streamKey }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons Row (Save / Cancel)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        // Update basic info in local Profile DB and Firestore
                        viewModel.updateProfileDetails(editName, editSchool, editDistrict, selectedClass.targetExam)
                        // Update active studying class
                        viewModel.switchActiveExamClass(selectedClass.id, if (isSeniorSecondary) selectedStream else "all")
                        
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
                            text = "প্রোফাইল পরিবর্তন সংরক্ষণ করুন",
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
                ) {
                    Text(text = "বাতিল করুন")
                }
            }
        }
    }
}
