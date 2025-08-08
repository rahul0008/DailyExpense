package com.example.dailyexpense.ui.presenter.expense

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier // Modifier might be passed down to ExpenseEntryLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailyexpense.ui.theme.DailyExpenseTheme

@Composable
fun ExpenseEntryScreen(
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by expenseViewModel.uiState.collectAsState()

    DailyExpenseTheme {
        ExpenseEntryLayout(
            state = uiState,
            onEvent = expenseViewModel::onEvent,
        )
    }
}