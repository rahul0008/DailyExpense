package com.example.dailyexpense.ui.presenter.expense

import android.content.Context
import com.example.dailyexpense.util.essentialEnums.ExpenseCategory

sealed class ExpenseScreenEvent {
    // Input Changes
    data class TitleChanged(val title: String) : ExpenseScreenEvent()
    data class AmountChanged(val amount: String) : ExpenseScreenEvent()
    data class CategorySelected(val category: ExpenseCategory) : ExpenseScreenEvent()
    data class NotesChanged(val notes: String) : ExpenseScreenEvent()
    data class ReceiptImageSelected(val uri: String?) : ExpenseScreenEvent() // Mock URI

    // UI Interactions
    data object ToggleCategoryDropdown : ExpenseScreenEvent()
    data class SubmitExpense(val context: Context) : ExpenseScreenEvent() // Context for Toast
    data object ClearSubmissionStatus : ExpenseScreenEvent() // To reset animation/toast

    // Lifecycle or Data Loading events (if applicable, e.g., for fetching totalSpentToday)
    // data object LoadInitialData : ExpenseScreenEvent()
}

