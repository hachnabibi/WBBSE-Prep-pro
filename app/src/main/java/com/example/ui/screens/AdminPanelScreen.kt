package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.admin.*
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.DeshExamViewModel
import kotlinx.coroutines.launch

/**
 * অ্যাডমিন প্যানেল মূল কোঅর্ডিনেটর স্ক্রিন (Admin Panel Coordinator Shell)
 * নেভিগেশন ড্রয়ার এবং হরিজোন্টাল ট্যাববারের মাধ্যমে প্রতিটি আলাদা অ্যাডমিন স্ক্রিন লোড করে।
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: DeshExamViewModel,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUid = viewModel.authRepository.currentUserState?.uid
    val isAdmin = true // Temporary public access

    if (!isAdmin) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Access Denied",
                tint = WrongRed,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "অননুমোদিত প্রবেশাধিকার (Access Denied)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "শুধুমাত্র অনুমোদিত অ্যাডমিন এই প্যানেলটি ব্যবহার করতে পারবেন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ফিরে যান")
            }
        }
        return
    }

    val isLoading by viewModel.isAuthLoading.collectAsState()
    val authSuccess by viewModel.authSuccessMessage.collectAsState()
    val authError by viewModel.authErrorMessage.collectAsState()
    val backupState by viewModel.cloudBackupState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var actionSuccess by remember { mutableStateOf(false) }

    val authUser by viewModel.authUserState.collectAsState()
    var showEditAdminDialog by remember { mutableStateOf(false) }
    var editAdminNameText by remember { mutableStateOf("") }
    var isSavingAdminProfile by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val adminTabs = remember {
        listOf(
            Pair("📊 ক্লাউড ব্যাকআপ ও ড্যাশবোর্ড", Icons.Default.CloudSync),
            Pair("✍️ নতুন প্রশ্ন সংযোজন", Icons.Default.PlaylistAdd),
            Pair("📋 প্রশ্ন ব্যাংক ম্যানেজমেন্ট (CMS)", Icons.Default.MenuBook),
            Pair("📝 কাস্টম মক টেস্ট ক্রিয়েটর", Icons.Default.Quiz),
            Pair("🎯 লাইভ এক্সাম ও স্ট্রাকচার্ড টেস্ট", Icons.Default.Stars),
            Pair("📑 টেস্ট টেমপ্লেট ম্যানেজমেন্ট", Icons.Default.Assignment),
            Pair("📦 বাল্ক ইমপোর্ট (প্রশ্ন ও সিলেবাস)", Icons.Default.FileUpload),
            Pair("🏫 কারিকুলাম ম্যানেজার", Icons.Default.AddBusiness),
            Pair("⚡ ফায়ারস্টোর ইনডেক্স ম্যানেজার", Icons.Default.FlashOn),
            Pair("🎙️ অডিও পডকাস্ট ম্যানেজমেন্ট", Icons.Default.Audiotrack)
        )
    }



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                drawerTonalElevation = 4.dp,
                modifier = Modifier.width(320.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Column {
                        Text(
                            text = "🛠️ অ্যাডমিন প্যানেল",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ফায়ারবেস ক্লাউড ম্যানেজার",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 12.dp)
                ) {
                    adminTabs.forEachIndexed { index, (title, icon) ->
                        val isSelected = selectedTab == index
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.5.sp
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                selectedTab = index
                                scope.launch { drawerState.close() }
                                if (index == 2) viewModel.fetchFirestoreQuestions()
                                if (index == 5) viewModel.fetchFirestoreTestTemplates()
                                if (index == 9) viewModel.fetchCustomPodcasts()
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Admin Avatar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = authUser?.displayName ?: "অ্যাডমিন ইউজার",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = authUser?.email ?: "admin@deshexam.com",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    editAdminNameText = authUser?.displayName ?: ""
                                    showEditAdminDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Admin",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ফিরে যান",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                val (screenTitle, screenSubtitle) = when (selectedTab) {
                    0 -> "ক্লাউড ব্যাকআপ ও ড্যাশবোর্ড" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    1 -> "নতুন প্রশ্ন সংযোজন" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    2 -> "প্রশ্ন ব্যাংক ম্যানেজমেন্ট" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    3 -> "কাস্টম মক টেস্ট ক্রিয়েটর" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    4 -> "লাইভ এক্সাম ও স্ট্রাকচার্ড টেস্ট" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    5 -> "টেস্ট টেমপ্লেট ব্যবস্থাপনা" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    6 -> "বাল্ক ইমপোর্ট (প্রশ্ন ও সিলেবাস)" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    7 -> "কারিকুলাম ম্যানেজার" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    8 -> "ফায়ারস্টোর ইনডেক্স ম্যানেজার" to "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল"
                    9 -> "🎙️ পডকাস্ট ব্যবস্থাপনা" to "অডিও লেকচার ও রিভিশন হাব"
                    else -> "🛠️ অ্যাডমিন কন্ট্রোল প্যানেল" to "ম্যানেজমেন্ট মডিউল"
                }

                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = screenTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (selectedTab == 9) 15.sp else 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = screenSubtitle,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (selectedTab == 9) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        if (selectedTab != 0) {
                            IconButton(onClick = { selectedTab = 0 }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "ড্যাশবোর্ডে ফিরুন",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open drawer menu")
                            }
                        }
                    },
                    actions = {
                        if (selectedTab == 9) {
                            IconButton(
                                onClick = {
                                    viewModel.fetchCustomPodcasts()
                                    actionSuccess = true
                                    actionMessage = "🔄 ক্লাউড থেকে পডকাস্ট তালিকা রিফ্রেশ করা হয়েছে।"
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "রিফ্রেশ",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (selectedTab != 0) {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "মেনু ড্রয়ার",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Exit Admin",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { innerPadding ->
            BackHandler(enabled = selectedTab != 0) {
                selectedTab = 0
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val displayMessage = actionMessage ?: authSuccess ?: authError
                if (displayMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (actionSuccess) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (actionSuccess) CorrectGreen else WrongRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (actionSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (actionSuccess) CorrectGreen else WrongRed
                            )
                            Text(
                                text = displayMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { actionMessage = null; viewModel.clearAuthMessages() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }



                // Screen Switcher
                when (selectedTab) {
                    0 -> AdminDashboardScreen(
                        viewModel = viewModel,
                        backupState = backupState,
                        isLoading = isLoading,
                        isBengali = isBengali,
                        onAction = { success, msg ->
                            actionSuccess = success
                            actionMessage = msg
                        },
                        modifier = Modifier.weight(1f)
                    )
                    1 -> AdminAddQuestionScreen(
                        viewModel = viewModel,
                        isLoading = isLoading,
                        isBengali = isBengali,
                        onAction = { success, msg ->
                            actionSuccess = success
                            actionMessage = msg
                        },
                        modifier = Modifier.weight(1f)
                    )
                    2 -> AdminQuestionBankScreen(
                        viewModel = viewModel,
                        isLoading = isLoading,
                        isBengali = isBengali,
                        onAction = { success, msg ->
                            actionSuccess = success
                            actionMessage = msg
                        },
                        modifier = Modifier.weight(1f)
                    )
                    3 -> AdminCustomTestScreen(
                        viewModel = viewModel,
                        isLoading = isLoading,
                        isBengali = isBengali,
                        onAction = { success, msg ->
                            actionSuccess = success
                            actionMessage = msg
                        },
                        modifier = Modifier.weight(1f)
                    )
                    4 -> AdminLiveExamScreen(
                        viewModel = viewModel,
                        isLoading = isLoading,
                        isBengali = isBengali,
                        onAction = { success, msg ->
                            actionSuccess = success
                            actionMessage = msg
                        },
                        modifier = Modifier.weight(1f)
                    )
                    5 -> AdminTestTemplatesScreen(
                        viewModel = viewModel,
                        isLoading = isLoading,
                        isBengali = isBengali,
                        onAction = { success, msg ->
                            actionSuccess = success
                            actionMessage = msg
                        },
                        modifier = Modifier.weight(1f)
                    )
                    6 -> AdminBulkImportScreen(
                        viewModel = viewModel,
                        isLoading = isLoading,
                        isBengali = isBengali,
                        onAction = { success, msg ->
                            actionSuccess = success
                            actionMessage = msg
                        },
                        modifier = Modifier.weight(1f)
                    )
                    7 -> AdminCurriculumScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onShowMessage = { msg, success ->
                            actionSuccess = success
                            actionMessage = msg
                        },
                        modifier = Modifier.weight(1f)
                    )
                    8 -> AdminIndexManagerScreen(
                        isBengali = isBengali,
                        modifier = Modifier.weight(1f)
                    )
                    9 -> AdminPodcastScreen(
                        viewModel = viewModel,
                        isBengali = isBengali,
                        onAction = { success, msg ->
                            actionSuccess = success
                            actionMessage = msg
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Profile Edit Dialog
    if (showEditAdminDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSavingAdminProfile) showEditAdminDialog = false },
            title = {
                Text(
                    text = if (isBengali) "অ্যাডমিন নাম পরিবর্তন" else "Change Admin Display Name",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBengali) "নতুন নাম লিখুন:" else "Enter new display name:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = editAdminNameText,
                        onValueChange = { editAdminNameText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isSavingAdminProfile) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text(
                                text = if (isBengali) "সংরক্ষণ হচ্ছে..." else "Saving...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editAdminNameText.isNotBlank()) {
                            isSavingAdminProfile = true
                            scope.launch {
                                val result = viewModel.authRepository.updateDisplayName(editAdminNameText)
                                isSavingAdminProfile = false
                                if (result is com.example.data.auth.AuthResult.Success) {
                                    showEditAdminDialog = false
                                    actionSuccess = true
                                    actionMessage = if (isBengali) "অ্যাডমিন ডিসপ্লে নাম সফলভাবে পরিবর্তিত হয়েছে!" else "Admin display name updated successfully!"
                                } else {
                                    actionSuccess = false
                                    actionMessage = if (isBengali) "পরিবর্তন করতে ব্যর্থ হয়েছে!" else "Failed to update profile name."
                                }
                            }
                        }
                    },
                    enabled = !isSavingAdminProfile && editAdminNameText.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isBengali) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditAdminDialog = false },
                    enabled = !isSavingAdminProfile
                ) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
