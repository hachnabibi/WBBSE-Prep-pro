package com.example.ui.screens.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.viewmodel.CloudBackupState
import com.example.ui.viewmodel.DeshExamViewModel

/**
 * ট্যাব ০: ক্লাউড ব্যাকআপ ও ড্যাশবোর্ড
 */
@Composable
fun AdminDashboardScreen(
    viewModel: DeshExamViewModel,
    backupState: CloudBackupState,
    isLoading: Boolean,
    isBengali: Boolean = true,
    onAction: (Boolean, String) -> Unit,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AdminDashboardHomeTab(
        viewModel = viewModel,
        backupState = backupState,
        isLoading = isLoading,
        isBengali = isBengali,
        onAction = onAction,
        onNavigate = onNavigate,
        modifier = modifier
    )
}
