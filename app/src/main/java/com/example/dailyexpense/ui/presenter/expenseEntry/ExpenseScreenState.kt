package com.example.dailyexpense.ui.presenter.expenseEntry

import com.example.dailyexpense.util.essentialEnums.ExpenseCategory

data class ExpenseScreenState(
    // Input Field Values
    val expenseTitle: String = "",
    val expenseAmount: String = "",
    val selectedCategory: ExpenseCategory = ExpenseCategory.FOOD,
    val expenseNotes: String = "",
    val receiptImageUri: String? = null,

    // UI Control State
    val isCategoryDropdownExpanded: Boolean = false,
    val isSubmitting: Boolean = false,
    val submissionStatus: SubmissionStatus = SubmissionStatus.Idle,

    // Data Display State
    val totalSpentTodayFormatted: String = "₹0.00",

    // Form Validation Errors
    val titleError: String? = null,
    val amountError: String? = null,
)

// Enum to manage the submission status more clearly
enum class SubmissionStatus {
    Idle,
    Success,
    Error,
    Loading
}

