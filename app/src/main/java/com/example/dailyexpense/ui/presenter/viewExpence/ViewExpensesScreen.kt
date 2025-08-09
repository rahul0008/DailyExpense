package com.example.dailyexpense.ui.presenter.viewExpence // Or your chosen navigation/screen package

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ViewExpensesScreen(
    modifier: Modifier = Modifier,
    viewModel: ViewExpensesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    ViewExpensesLayout(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}
