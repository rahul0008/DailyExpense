package com.example.dailyexpense.ui.presenter.expenseEntry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
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