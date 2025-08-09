package com.example.dailyexpense.ui.presenter.viewExpence

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyexpense.db.entity.ExpenseEntity
import com.example.dailyexpense.repo.dbRepo.ExpenseRepository
import com.example.dailyexpense.util.essentialEnums.ExpenseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ViewExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewExpensesScreenState()) // Ensure ViewExpensesScreenState has rawExpensesList
    val uiState: StateFlow<ViewExpensesScreenState> = _uiState.asStateFlow()

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
        currency = Currency.getInstance("INR")
    }

    private val plainNumberParser: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        isGroupingUsed = false
    }

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())


    init {
        loadExpensesForCurrentFilter()
    }

    fun onEvent(event: ViewExpensesScreenEvent) {
        when (event) {
            is ViewExpensesScreenEvent.LoadExpenses -> loadExpensesForCurrentFilter(event.forceRefresh)
            is ViewExpensesScreenEvent.DateFilterChanged -> {
                _uiState.update {
                    it.copy(
                        selectedDateFilter = event.filterType,
                        customSelectedDateMillis = if (event.filterType != DateFilterType.CUSTOM_DATE) null else it.customSelectedDateMillis
                    )
                }
                loadExpensesForCurrentFilter()
            }
            is ViewExpensesScreenEvent.CustomDateSelected -> {
                _uiState.update { it.copy(selectedDateFilter = DateFilterType.CUSTOM_DATE, customSelectedDateMillis = event.dateMillis) }
                loadExpensesForCurrentFilter()
            }
            is ViewExpensesScreenEvent.DateRangeSelected -> {
                _uiState.update { it.copy(
                    selectedDateFilter = DateFilterType.DATE_RANGE,
                    customDateRangeStartMillis = event.startDateMillis,
                    customDateRangeEndMillis = event.endDateMillis
                )}
                loadExpensesForCurrentFilter()
            }
            ViewExpensesScreenEvent.OpenDatePickerDialog -> _uiState.update { it.copy(showDatePickerDialog = true) }
            ViewExpensesScreenEvent.DismissDatePickerDialog -> _uiState.update { it.copy(showDatePickerDialog = false) }

            is ViewExpensesScreenEvent.GroupingOptionChanged -> {
                _uiState.update { it.copy(currentGrouping = event.grouping) }
                // Re-process using the stored raw entities from the current state
                processExpensesForDisplay(_uiState.value.rawExpensesList) // MODIFIED HERE
            }
            is ViewExpensesScreenEvent.ExpenseClicked -> {
                println("Expense clicked: ${event.expenseId}")
            }
            is ViewExpensesScreenEvent.DeleteExpense -> {
                viewModelScope.launch {
                    // expenseRepository.deleteExpenseById(event.expenseId)
                    // loadExpensesForCurrentFilter() // Refresh after delete
                }
            }
        }
    }

    private fun loadExpensesForCurrentFilter(forceRefresh: Boolean = false) {
        _uiState.update { it.copy(isLoading = true) }

        val (startDate, endDate) = calculateDateRange(_uiState.value)

        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.getExpensesForDateRange(startDate, endDate)
                .catch { e ->
                    // Ensure rawExpensesList is also cleared or handled appropriately on error
                    _uiState.update { it.copy(isLoading = false, rawExpensesList = emptyList()) }
                    println("Error loading expenses: ${e.message}")
                }
                .collect { entities ->
                    // Store the fetched entities in rawExpensesList first
                    _uiState.update { it.copy(rawExpensesList = entities) }
                    // Then process these entities for display
                    processExpensesForDisplay(entities) // Pass the fresh entities
                }
        }
    }

    private fun processExpensesForDisplay(entities: List<ExpenseEntity>) {
        // This 'entities' parameter should now always be the full list for the current filter
        if (entities.isEmpty() && _uiState.value.currentGrouping != GroupingOption.NONE) {
            Log.w("ViewExpensesVM", "processExpensesForDisplay called with empty entities while grouping is active. This might lead to empty grouped list.")
        }


        val uiListItems = entities.map { mapEntityToUiListItem(it) }.sortedByDescending { it.originalTimestamp }
        val totalAmount = entities.sumOf { it.amount }

        val groupedItems: List<GroupedExpenses> = when (_uiState.value.currentGrouping) {
            GroupingOption.NONE -> emptyList()
            GroupingOption.BY_CATEGORY -> {
                // Ensure uiListItems is derived from the passed 'entities'
                entities.map { mapEntityToUiListItem(it) } // Re-map if necessary, or use a pre-mapped list if sure it's up-to-date
                    .groupBy { it.category }
                    .map { (category, items) ->
                        GroupedExpenses(
                            groupTitle = category.displayName.uppercase(),
                            expenses = items.sortedByDescending { it.originalTimestamp },
                            totalAmountFormattedInGroup = currencyFormatter.format(items.sumOf { item ->
                                parseFormattedAmountToDouble(item.amountFormatted, item.title)
                            })
                        )
                    }.sortedBy { it.groupTitle }
            }
            GroupingOption.BY_TIME -> {
                // Same principle: use 'entities' to derive uiListItems if needed for this block
                val itemsForTimeGrouping = entities.map { mapEntityToUiListItem(it) }

                // Helper function (ensure it's defined within or accessible to this scope)
                fun getThreeHourSlot(timestamp: Long): Pair<Int, String> {
                    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
                    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                    val slotIndex = hourOfDay / 3

                    val startHour = slotIndex * 3
                    // val endHour = startHour + 2 // Not directly used for nextSlotStartHour logic

                    val tempCal = Calendar.getInstance().apply { clear() }

                    tempCal.set(Calendar.HOUR_OF_DAY, startHour)
                    val startHourStr = timeFormat.format(tempCal.time)

                    val nextSlotStartHour = (slotIndex + 1) * 3
                    tempCal.set(Calendar.HOUR_OF_DAY, nextSlotStartHour)
                    var endHourStr = timeFormat.format(tempCal.time)

                    if (slotIndex == 7) { // 9 PM - 12 AM (midnight)
                        val midnightCal = Calendar.getInstance().apply {
                            timeInMillis = timestamp // Start with current item's day
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            add(Calendar.DAY_OF_YEAR, 1) // Midnight of the *next* day
                        }
                        endHourStr = timeFormat.format(midnightCal.time) // Should show 12:00 AM for the next day
                    }


                    val datePart = fullDateFormat.format(Date(timestamp))
                    val slotTitle = "$datePart ($startHourStr - $endHourStr)"
                    return Pair(slotIndex, slotTitle)
                }

                itemsForTimeGrouping.groupBy { listItem ->
                    try {
                        val calendar = Calendar.getInstance().apply { timeInMillis = listItem.originalTimestamp }
                        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                        val slotIndex = hourOfDay / 3
                        val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dateKey = dateKeyFormat.format(Date(listItem.originalTimestamp))
                        "$dateKey-slot$slotIndex"
                    } catch (_: Exception) {
                        "INVALID_TIMESTAMP_GROUP_${listItem.originalTimestamp}"
                    }
                }
                    .mapNotNull { (groupingKey, itemsInGroup) ->
                        itemsInGroup.firstOrNull()?.let { firstItemInGroup ->
                            val (_, groupTitleDisplay) = try {
                                getThreeHourSlot(firstItemInGroup.originalTimestamp)
                            } catch (_: Exception) {
                                val parts = groupingKey.split("-slot")
                                val datePart = parts.getOrNull(0) ?: "Unknown Date"
                                val slotPart = parts.getOrNull(1)?.let { "Slot $it" } ?: "Unknown Slot"
                                val formattedDate = try {
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(datePart)!!
                                    )
                                } catch (_: Exception) { datePart }
                                Pair(0, "$formattedDate ($slotPart)")
                            }

                            GroupedExpenses(
                                groupTitle = groupTitleDisplay,
                                expenses = itemsInGroup.sortedByDescending { it.originalTimestamp },
                                totalAmountFormattedInGroup = currencyFormatter.format(itemsInGroup.sumOf { item ->
                                    parseFormattedAmountToDouble(item.amountFormatted, item.title)
                                })
                            )
                        }
                    }
                    .sortedWith(compareByDescending<GroupedExpenses> {
                        it.expenses.firstOrNull()?.originalTimestamp
                    }.thenBy {
                        // This secondary sort by slot index might be tricky if title format changes
                        // Prefer deriving slot index directly if possible for robust sorting
                        val title = it.groupTitle
                        try {
                            // A more robust way would be to get the slot index from the groupingKey or first item
                            // For now, this parsing relies on the specific title format
                            if (title.contains("Slot ")) {
                                title.substringAfterLast("Slot ").substringBefore(")").toIntOrNull()
                            } else if (title.contains("-slot")) { // from fallback
                                title.substringAfterLast("-slot").toIntOrNull()
                            } else { // try to get from first part of time range e.g. (12:00 AM
                                val timePart = title.substringAfter("(").substringBefore(" -")
                                val cal = Calendar.getInstance()
                                cal.time = timeFormat.parse(timePart)!!
                                cal.get(Calendar.HOUR_OF_DAY) / 3
                            }
                        } catch (_: Exception) {
                            0 // Fallback
                        }
                    })
            }
        }

        Log.i("ViewExpensesVM", "processExpensesForDisplay: Grouping: ${_uiState.value.currentGrouping}, UIListItems (for NONE): ${uiListItems.size}, GroupedItems: ${groupedItems.size}, Total Entities: ${entities.size}")
        _uiState.update {
            it.copy(
                isLoading = false,
                // rawExpensesList is already updated, no need to change it here
                expenses = if (it.currentGrouping == GroupingOption.NONE) uiListItems else emptyList(),
                groupedExpenses = if (it.currentGrouping != GroupingOption.NONE) groupedItems else emptyList(),
                totalExpensesCount = entities.size, // This should be from the original entities count
                totalExpensesAmountFormatted = currencyFormatter.format(totalAmount) // totalAmount from original entities
            )
        }
    }
    // ... rest of your ViewModel (mapEntityToUiListItem, mapUiItemToEntity, parseFormattedAmountToDouble, calculateDateRange, etc.)
    // Ensure these helper methods are correct.

    private fun mapEntityToUiListItem(entity: ExpenseEntity): ExpenseListItem {
        val todayCal = Calendar.getInstance()
        val expenseCal = Calendar.getInstance().apply { timeInMillis = entity.timestamp }

        val dateLabel = when {
            isSameDay(expenseCal, todayCal) -> timeFormat.format(expenseCal.time) // Time for today
            isYesterday(expenseCal, todayCal) -> "Yesterday, ${timeFormat.format(expenseCal.time)}"
            else -> dateFormat.format(expenseCal.time) // Date for older
        }

        return ExpenseListItem(
            id = entity.id,
            title = entity.title,
            amountFormatted = currencyFormatter.format(entity.amount),
            category = ExpenseCategory.entries.firstOrNull { it.name.equals(entity.category, ignoreCase = true) } ?: ExpenseCategory.OTHER,
            dateLabel = dateLabel,
            originalTimestamp = entity.timestamp,
            imageUri = entity.imageUri
        )
    }

    private fun parseFormattedAmountToDouble(formattedAmount: String, itemTitleForErrorLog: String = "N/A"): Double {
        val currencySymbol = currencyFormatter.currency?.symbol ?: "₹"

        val cleanedAmountString = formattedAmount
            .removePrefix(currencySymbol)
            .replace(",", "")
            .trim()

        return try {
            if (cleanedAmountString.isEmpty()) {
                0.0
            } else {
                // Using plainNumberParser which should handle US locale (dots for decimals)
                plainNumberParser.parse(cleanedAmountString)?.toDouble() ?: 0.0
            }
        } catch (e: java.text.ParseException) { // Use java.text.ParseException
            System.err.println(
                "ParseException in parseFormattedAmountToDouble: Could not parse amount string: '$cleanedAmountString' " +
                        "(original: '$formattedAmount') for item title '$itemTitleForErrorLog'. Error: ${e.message}"
            )
            0.0
        } catch (e: Exception) { // Catch other potential exceptions like NumberFormatException
            System.err.println(
                "Exception in parseFormattedAmountToDouble: Could not parse amount string: '$cleanedAmountString' " +
                        "(original: '$formattedAmount') for item title '$itemTitleForErrorLog'. Error: ${e.message}"
            )
            0.0
        }
    }
    private fun calculateDateRange(currentState: ViewExpensesScreenState): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        when (currentState.selectedDateFilter) {
            DateFilterType.TODAY -> {
                setCalendarToStartOfDay(calendar)
                val start = calendar.timeInMillis
                setCalendarToEndOfDay(calendar)
                val end = calendar.timeInMillis
                return Pair(start, end)
            }
            DateFilterType.YESTERDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                setCalendarToStartOfDay(calendar)
                val start = calendar.timeInMillis
                setCalendarToEndOfDay(calendar)
                val end = calendar.timeInMillis
                return Pair(start, end)
            }
            DateFilterType.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                setCalendarToStartOfDay(calendar)
                val start = calendar.timeInMillis
                // To get end of week correctly
                calendar.add(Calendar.WEEK_OF_YEAR, 1) // Go to start of next week
                setCalendarToStartOfDay(calendar)      // Still start of that day
                calendar.add(Calendar.MILLISECOND, -1) // Go to the very end of the previous day (last day of current week)
                val end = calendar.timeInMillis
                return Pair(start,end)
            }
            DateFilterType.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                setCalendarToStartOfDay(calendar)
                val start = calendar.timeInMillis
                // To get end of month correctly
                calendar.add(Calendar.MONTH, 1)        // Go to start of next month
                setCalendarToStartOfDay(calendar)      // Still start of that day
                calendar.add(Calendar.MILLISECOND, -1) // Go to the very end of the previous day (last day of current month)
                val end = calendar.timeInMillis
                return Pair(start, end)
            }
            DateFilterType.CUSTOM_DATE -> {
                currentState.customSelectedDateMillis?.let {
                    calendar.timeInMillis = it
                    setCalendarToStartOfDay(calendar)
                    val start = calendar.timeInMillis
                    setCalendarToEndOfDay(calendar)
                    val end = calendar.timeInMillis
                    return Pair(start, end)
                } ?: return Pair(0L, System.currentTimeMillis())
            }
            DateFilterType.DATE_RANGE -> {
                val start = currentState.customDateRangeStartMillis?.let {
                    val tempCal = Calendar.getInstance().apply { timeInMillis = it }
                    setCalendarToStartOfDay(tempCal) // Ensure start of day for the start date
                    tempCal.timeInMillis
                } ?: 0L

                val end = currentState.customDateRangeEndMillis?.let {
                    val tempCal = Calendar.getInstance().apply { timeInMillis = it }
                    setCalendarToEndOfDay(tempCal)
                    tempCal.timeInMillis
                } ?: System.currentTimeMillis()
                return Pair(start, end)
            }
        }
    }

    private fun setCalendarToStartOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    private fun setCalendarToEndOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
        val yesterdayCal = (cal2.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        return isSameDay(cal1, yesterdayCal)
    }
}
