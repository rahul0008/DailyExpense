package com.example.dailyexpense.ui.presenter.expense

import com.example.dailyexpense.util.essentialEnums.ExpenseCategory

data class ExpenseScreenState(
    // Input Field Values
    val expenseTitle: String = "",
    val expenseAmount: String = "", // Keep as String for TextField, validate to Double
    val selectedCategory: ExpenseCategory = ExpenseCategory.FOOD, // Default category
    val expenseNotes: String = "",
    val receiptImageUri: String? = null, // Placeholder for image URI

    // UI Control State
    val isCategoryDropdownExpanded: Boolean = false,
    val isSubmitting: Boolean = false,
    val submissionStatus: SubmissionStatus = SubmissionStatus.Idle,

    // Data Display State
    val totalSpentTodayFormatted: String = "₹0.00", // Pre-formatted for display

    // Form Validation Errors
    val titleError: String? = null,
    val amountError: String? = null,
    // You could add categoryError: String? = null if needed
)

// Enum to manage the submission status more clearly
enum class SubmissionStatus {
    Idle,
    Success,
    Error, // Could add a message: Error(val message: String)
    Loading // If 'isSubmitting' is not enough, though often it is.
}

