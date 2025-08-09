package com.example.bottomNavigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle // Example Icon
import androidx.compose.material.icons.filled.Analytics // Example Icon
import androidx.compose.material.icons.filled.List // Example Icon
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object ExpenseEntry : Screen("expense_entry", "Entry", Icons.Filled.AddCircle)
    object ViewExpenses : Screen("view_expenses", "Expenses", Icons.AutoMirrored.Filled.List)
    object ExpenseReport : Screen("expense_report", "Report", Icons.Filled.Analytics)
}

val bottomNavItems = listOf(
    Screen.ExpenseEntry,
    Screen.ViewExpenses,
    Screen.ExpenseReport
)
