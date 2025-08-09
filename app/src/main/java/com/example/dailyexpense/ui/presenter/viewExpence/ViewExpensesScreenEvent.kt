package com.example.dailyexpense.ui.presenter.viewExpence

sealed interface ViewExpensesScreenEvent {
    data class LoadExpenses(val forceRefresh: Boolean = false) : ViewExpensesScreenEvent // Initial load or refresh
    data class DateFilterChanged(val filterType: DateFilterType) : ViewExpensesScreenEvent
    data class CustomDateSelected(val dateMillis: Long) : ViewExpensesScreenEvent
    data class DateRangeSelected(val startDateMillis: Long, val endDateMillis: Long) : ViewExpensesScreenEvent
    data object OpenDatePickerDialog : ViewExpensesScreenEvent
    data object DismissDatePickerDialog : ViewExpensesScreenEvent
    // Add events for DateRangePicker dialog if you implement it
    data class GroupingOptionChanged(val grouping: GroupingOption) : ViewExpensesScreenEvent
    data class ExpenseClicked(val expenseId: Long) : ViewExpensesScreenEvent // For navigation to details
    data class DeleteExpense(val expenseId: Long) : ViewExpensesScreenEvent // Optional: if you add swipe-to-delete
}
