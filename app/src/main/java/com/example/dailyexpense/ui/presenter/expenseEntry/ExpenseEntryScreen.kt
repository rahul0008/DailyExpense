package com.example.dailyexpense.ui.presenter.expenseEntry

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dailyexpense.ui.theme.DailyExpenseTheme

@Composable
fun ExpenseEntryScreen(
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by expenseViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        expenseViewModel.eventFlow.collect { event ->
            when(event) {
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT)
            }
        }
    }

    DailyExpenseTheme {
        ExpenseEntryLayout(
            state = uiState,
            onEvent = expenseViewModel::onEvent,
        )
    }
}