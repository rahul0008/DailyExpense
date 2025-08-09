package com.example.dailyexpense.ui.presenter.viewExpence

sealed interface ViewExpensesScreenEvent {
    data class LoadExpenses(val forceRefresh: Boolean = false) : ViewExpensesScreenEvent
    data class DateFilterChanged(val filterType: DateFilterType) : ViewExpensesScreenEvent
    data class CustomDateSelected(val dateMillis: Long) : ViewExpensesScreenEvent
    data class DateRangeSelected(val startDateMillis: Long, val endDateMillis: Long) : ViewExpensesScreenEvent
    data object OpenDatePickerDialog : ViewExpensesScreenEvent
    data object DismissDatePickerDialog : ViewExpensesScreenEvent
    data class GroupingOptionChanged(val grouping: GroupingOption) : ViewExpensesScreenEvent
    data class ExpenseClicked(val expenseId: Long) : ViewExpensesScreenEvent
    data class DeleteExpense(val expenseId: Long) : ViewExpensesScreenEvent
}
