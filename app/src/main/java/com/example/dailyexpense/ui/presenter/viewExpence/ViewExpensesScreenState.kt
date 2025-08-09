package com.example.dailyexpense.ui.presenter.viewExpence

import com.example.dailyexpense.db.entity.ExpenseEntity
import com.example.dailyexpense.util.essentialEnums.ExpenseCategory

data class ViewExpensesScreenState(
    val isLoading: Boolean = true,
    val rawExpensesList: List<ExpenseEntity> = emptyList(),
    val expenses: List<ExpenseListItem> = emptyList(),
    val groupedExpenses: List<GroupedExpenses> = emptyList(), // Used when grouping is active
    val selectedDateFilter: DateFilterType = DateFilterType.TODAY,
    val customSelectedDateMillis: Long? = null, // For DateFilterType.CUSTOM_DATE
    val customDateRangeStartMillis: Long? = null, // For DateFilterType.DATE_RANGE
    val customDateRangeEndMillis: Long? = null,   // For DateFilterType.DATE_RANGE
    val currentGrouping: GroupingOption = GroupingOption.NONE,
    val totalExpensesCount: Int = 0,
    val totalExpensesAmountFormatted: String = "₹0.00",
    val showDatePickerDialog: Boolean = false,
    val showDateRangePickerDialog: Boolean = false // For selecting a range
) {
    val showEmptyState: Boolean
        get() = !isLoading && expenses.isEmpty() && groupedExpenses.isEmpty()
}



data class ExpenseListItem(
    val id: Long,
    val title: String,
    val amountFormatted: String,
    val category: ExpenseCategory,
    val dateLabel: String,
    val originalTimestamp: Long,
    val imageUri: String? = null
)

// For grouped views
data class GroupedExpenses(
    val groupTitle: String,
    val expenses: List<ExpenseListItem>,
    val totalAmountFormattedInGroup: String
)

enum class DateFilterType {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH,
    CUSTOM_DATE,
    DATE_RANGE
}

enum class GroupingOption {
    NONE,
    BY_CATEGORY,
    BY_TIME // Could also be BY_MONTH etc.
}