package com.example.dailyexpense.ui.presenter.viewExpence // Or your chosen navigation/screen package

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ViewExpensesScreen(
    modifier: Modifier = Modifier,
    viewModel: ViewExpensesViewModel = hiltViewModel() // Injects the ViewModel
    // Add navController if you need to navigate from this screen
    // navController: NavController
) {
    // Collect the UI state from the ViewModel
    val state by viewModel.uiState.collectAsState()

    // Pass the current state and a lambda to handle events to the layout composable
    ViewExpensesLayout(
        state = state,
        onEvent = viewModel::onEvent, // Pass the onEvent function from the ViewModel
        modifier = modifier
    )
}
