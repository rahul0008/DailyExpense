package com.example.dailyexpense.ui.presenter.expense

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyexpense.db.entity.ExpenseEntity
import com.example.dailyexpense.repo.dbRepo.ExpenseRepository
import com.example.dailyexpense.util.essentialEnums.ExpenseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseScreenState())
    val uiState: StateFlow<ExpenseScreenState> = _uiState.asStateFlow()

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
    }

    init {
        loadTotalSpentToday()
    }

    private fun loadTotalSpentToday() {
        viewModelScope.launch {
            val (startOfDay, endOfDay) = getTodayDateRangeTimestamps()

            // Uses expenseRepository which calls DAO's getExpensesForDateRange (using timestamp)
            expenseRepository.getExpensesForDateRange(startOfDay, endOfDay)
                .map { expenses -> expenses.sumOf { it.amount } }
                .catch { e ->
                    println("Error fetching total spent today: ${e.message}")
                    _uiState.update { it.copy(totalSpentTodayFormatted = currencyFormatter.format(0.0)) } // Show 0 on error
                }
                .collect { total ->
                    _uiState.update {
                        it.copy(totalSpentTodayFormatted = currencyFormatter.format(total))
                    }
                }
        }
    }

    fun onEvent(event: ExpenseScreenEvent) {
        when (event) {
            is ExpenseScreenEvent.TitleChanged -> {
                val newTitle = event.title
                _uiState.update {
                    it.copy(
                        expenseTitle = newTitle,
                        titleError = if (newTitle.isBlank()) "Title cannot be empty" else null
                    )
                }
            }
            is ExpenseScreenEvent.AmountChanged -> {
                val newAmount = event.amount
                var error: String? = null
                if (newAmount.isBlank()) {
                    error = "Amount cannot be empty"
                } else if (!newAmount.matches(Regex("^\\d*\\.?\\d*\\s*\$"))) { // Allow trailing space then trim
                    error = "Invalid amount format"
                }
                _uiState.update {
                    it.copy(
                        expenseAmount = newAmount,
                        amountError = error
                    )
                }
            }
            is ExpenseScreenEvent.CategorySelected -> {
                _uiState.update {
                    it.copy(
                        selectedCategory = event.category,
                        isCategoryDropdownExpanded = false
                    )
                }
            }
            is ExpenseScreenEvent.NotesChanged -> {
                if (event.notes.length <= 100) {
                    _uiState.update { it.copy(expenseNotes = event.notes) }
                }
            }
            is ExpenseScreenEvent.ReceiptImageSelected -> { // Updated to match state if needed
                _uiState.update { it.copy(receiptImageUri = event.uri) } // State uses receiptImageUri
            }
            is ExpenseScreenEvent.ToggleCategoryDropdown -> {
                _uiState.update { it.copy(isCategoryDropdownExpanded = !it.isCategoryDropdownExpanded) }
            }
            is ExpenseScreenEvent.SubmitExpense -> {
                handleSubmitExpense(event.context)
            }
            is ExpenseScreenEvent.ClearSubmissionStatus -> {
                _uiState.update { it.copy(submissionStatus = SubmissionStatus.Idle) }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val currentState = _uiState.value
        val title = currentState.expenseTitle
        // Trim the amount string before validation
        val amountStr = currentState.expenseAmount.trim()


        val titleErrorUpdate = if (title.isBlank()) "Title cannot be empty" else null
        if (titleErrorUpdate != null) isValid = false

        var amountErrorUpdate: String? = null
        if (amountStr.isBlank()) {
            amountErrorUpdate = "Amount cannot be empty"
            isValid = false
        } else {
            try {
                val amountValue = amountStr.toDouble() // toDouble handles trimmed string
                if (amountValue <= 0) {
                    amountErrorUpdate = "Amount must be positive"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                amountErrorUpdate = "Invalid amount format"
                isValid = false
            }
        }
        if (amountErrorUpdate != null) isValid = false

        _uiState.update {
            it.copy(
                expenseAmount = amountStr, // Store trimmed version
                titleError = titleErrorUpdate,
                amountError = amountErrorUpdate
            )
        }
        return isValid
    }

    private fun handleSubmitExpense(context: Context) {
        if (!validateInputs()) {
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submissionStatus = SubmissionStatus.Loading) }

        viewModelScope.launch {
            val currentState = _uiState.value
            val amountDouble = currentState.expenseAmount.toDoubleOrNull() ?: 0.0

            val newExpenseEntity = ExpenseEntity(
                title = currentState.expenseTitle.trim(),
                amount = amountDouble,
                category = currentState.selectedCategory.name, // Storing enum's .name for robustness
                notes = currentState.expenseNotes.trim().ifBlank { null },
                imageUri = currentState.receiptImageUri, // Matching your ExpenseEntity field
                timestamp = System.currentTimeMillis() // Explicitly set, though entity has default
            )

            try {
                expenseRepository.insertExpense(newExpenseEntity)
                Toast.makeText(context, "Expense Added: ${newExpenseEntity.title}", Toast.LENGTH_SHORT).show()

                _uiState.update {
                    it.copy(
                        expenseTitle = "",
                        expenseAmount = "",
                        selectedCategory = ExpenseCategory.FOOD,
                        expenseNotes = "",
                        receiptImageUri = null, // Clear the URI from UI state
                        isSubmitting = false,
                        submissionStatus = SubmissionStatus.Success,
                        titleError = null,
                        amountError = null
                    )
                }
            } catch (e: Exception) {
                println("Error inserting expense: ${e.message}")
                Toast.makeText(context, "Failed to add expense. Please try again.", Toast.LENGTH_LONG).show()
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionStatus = SubmissionStatus.Error
                    )
                }
            }
        }
    }

    private fun getTodayDateRangeTimestamps(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        return Pair(startOfDay, endOfDay)
    }
}
