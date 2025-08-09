package com.example.dailyexpense.ui.presenter.expenseEntry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyexpense.db.entity.ExpenseEntity
import com.example.dailyexpense.repo.dbRepo.ExpenseRepository
import com.example.dailyexpense.util.essentialEnums.ExpenseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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


    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
        currency = Currency.getInstance("INR")
    }

    init {
        loadTotalSpentToday()
    }

    private fun loadTotalSpentToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val (startOfDay, endOfDay) = getTodayDateRangeTimestamps()

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
                } else if (!newAmount.matches(Regex("^\\d*\\.?\\d*\\s*$"))) { // Allow trailing space then trim
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
                handleSubmitExpense()
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
                val amountValue = amountStr.toDouble()
                if (amountValue <= 0) {
                    amountErrorUpdate = "Amount must be positive"
                    isValid = false
                }
            } catch (_: NumberFormatException) {
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

    private fun handleSubmitExpense() {
        if (!validateInputs()) {
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submissionStatus = SubmissionStatus.Loading) }

        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            val amountDouble = currentState.expenseAmount.toDoubleOrNull() ?: 0.0

            val newExpenseEntity = ExpenseEntity(
                title = currentState.expenseTitle.trim(),
                amount = amountDouble,
                category = currentState.selectedCategory.name,
                notes = currentState.expenseNotes.trim().ifBlank { null },
                imageUri = currentState.receiptImageUri,
                timestamp = System.currentTimeMillis()
            )

            try {
                expenseRepository.insertExpense(newExpenseEntity)

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
                _eventFlow.emit(UiEvent.ShowToast("Failed to add expense. Please try again."))
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
