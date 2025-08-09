package com.example.dailyexpense.ui.presenter.viewExpence

import android.util.Log
import androidx.core.net.ParseException
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class ViewExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewExpensesScreenState())
    val uiState: StateFlow<ViewExpensesScreenState> = _uiState.asStateFlow()

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
    }

    private val plainNumberParser: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
      isGroupingUsed = false // Or ensure commas are removed from string before parsing
    }

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())


    init {
        // Load initial expenses (e.g., for today)
        loadExpensesForCurrentFilter()
    }

    fun onEvent(event: ViewExpensesScreenEvent) {
        when (event) {
            is ViewExpensesScreenEvent.LoadExpenses -> loadExpensesForCurrentFilter(event.forceRefresh)
            is ViewExpensesScreenEvent.DateFilterChanged -> {
                _uiState.update {
                    it.copy(
                        selectedDateFilter = event.filterType,
                        customSelectedDateMillis = if (event.filterType != DateFilterType.CUSTOM_DATE) null else it.customSelectedDateMillis // Clear custom date if not custom
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
                // Re-process existing expenses for grouping or reload if grouping affects query
                processExpensesForDisplay(_uiState.value.expenses.map { mapUiItemToEntity(it) } ) // Pass original entities if available
            }
            is ViewExpensesScreenEvent.ExpenseClicked -> {
                // Handle navigation or detail view logic
                println("Expense clicked: ${event.expenseId}")
            }
            is ViewExpensesScreenEvent.DeleteExpense -> {
                viewModelScope.launch {
                    // expenseRepository.deleteExpenseById(event.expenseId)
                    // loadExpensesForCurrentFilter() // Refresh
                }
            }
        }
    }

    private fun loadExpensesForCurrentFilter(forceRefresh: Boolean = false) {
        _uiState.update { it.copy(isLoading = true) }

        val (startDate, endDate) = calculateDateRange(_uiState.value)

        viewModelScope.launch {
            expenseRepository.getExpensesForDateRange(startDate, endDate)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false) } // Handle error
                    println("Error loading expenses: ${e.message}")
                }
                .collect { entities ->
                    processExpensesForDisplay(entities)
                }
        }
    }

    private fun processExpensesForDisplay(entities: List<ExpenseEntity>) {
        val uiListItems = entities.map { mapEntityToUiListItem(it) }.sortedByDescending { it.originalTimestamp }
        val totalAmount = entities.sumOf { it.amount }

        val groupedItems: List<GroupedExpenses> = when (_uiState.value.currentGrouping) {
            GroupingOption.NONE -> emptyList()
            GroupingOption.BY_CATEGORY -> {
                uiListItems.groupBy { it.category }
                    .map { (category, items) ->
                        GroupedExpenses(
                            groupTitle = category.displayName.uppercase(),
                            expenses = items.sortedByDescending { it.originalTimestamp },
                            totalAmountFormattedInGroup = currencyFormatter.format(items.sumOf { item ->
                                // Need to parse amount back from formatted string or use original amount
                                parseFormattedAmountToDouble(item.amountFormatted, item.title)

                            })
                        )
                    }.sortedBy { it.groupTitle }
            }
                GroupingOption.BY_Time -> { // GROUPING BY 3-HOUR INTERVALS
                    // Helper function to determine the 3-hour slot and its display name
                    fun getThreeHourSlot(timestamp: Long): Pair<Int, String> {
                        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
                        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY) // 0-23

                        val slotIndex = hourOfDay / 3 // Integer division gives 0 for 0-2, 1 for 3-5, etc.
                        // slotIndex will range from 0 (12 AM - 3 AM) to 7 (9 PM - 12 AM)

                        val startHour = slotIndex * 3
                        val endHour = startHour + 2 // Inclusive end for display, e.g., 0-2, 3-5

                        // Format start and end hours for display (e.g., "12 AM", "03 PM")
                        // Using a temporary calendar to format hours correctly with AM/PM
                        val tempCal = Calendar.getInstance()
                        tempCal.clear() // Clear fields before setting hour

                        tempCal.set(Calendar.HOUR_OF_DAY, startHour)
                        val startHourStr = timeFormat.format(tempCal.time) // Use your existing timeFormat

                        tempCal.set(Calendar.HOUR_OF_DAY, endHour) // For the end of the display range (e.g. 2 for 0-2)
                        // To make it look like "X to Y", we actually want the start of the next slot for the upper bound of the range.
                        // For example, 12 AM - 3 AM (exclusive of 3 AM)
                        val nextSlotStartHour = (slotIndex + 1) * 3
                        tempCal.set(Calendar.HOUR_OF_DAY, nextSlotStartHour)
                        val endHourStr = timeFormat.format(tempCal.time)

                        // Special case for the last slot (9 PM - 12 AM)
                        val displayEndHourStr = if (slotIndex == 7) { // 9 PM - 12 AM (midnight)
                            tempCal.set(Calendar.HOUR_OF_DAY, 0) // Midnight of next day
                            timeFormat.format(tempCal.time)
                        } else {
                            endHourStr
                        }

                        // Group title like "Oct 26, 2023 (12:00 AM - 03:00 AM)"
                        val datePart = fullDateFormat.format(Date(timestamp)) // "MMM dd, yyyy"
                        val slotTitle = "$datePart ($startHourStr - $displayEndHourStr)"

                        // The key for grouping needs to be unique per day AND per slot.
                        // Combine date with slot index for a stable key.
                        val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dateKey = dateKeyFormat.format(Date(timestamp))
                        val groupingKey = "$dateKey-slot$slotIndex"


                        return Pair(slotIndex, slotTitle) // Return slot index for sorting, and title for display
                    }

                    uiListItems.groupBy { listItem ->
                        // Group by a key that combines the date and the 3-hour slot index
                        try {
                            val calendar = Calendar.getInstance().apply { timeInMillis = listItem.originalTimestamp }
                            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                            val slotIndex = hourOfDay / 3
                            val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val dateKey = dateKeyFormat.format(Date(listItem.originalTimestamp))
                            "$dateKey-slot$slotIndex" // e.g., "2023-10-26-slot0", "2023-10-26-slot1"
                        } catch (_: Exception) {
                            "INVALID_TIMESTAMP_GROUP_${listItem.originalTimestamp}"
                        }
                    }
                        .mapNotNull { (groupingKey, itemsInGroup) ->
                            itemsInGroup.firstOrNull()?.let { firstItemInGroup ->
                                // Generate the group title using the helper (or derive from key if necessary)
                                val (_, groupTitleDisplay) = try {
                                    getThreeHourSlot(firstItemInGroup.originalTimestamp)
                                } catch (_: Exception) {
                                    // Fallback title generation if getThreeHourSlot fails (e.g. from invalid timestamp key)
                                    val parts = groupingKey.split("-slot")
                                    val datePart = parts.getOrNull(0) ?: "Unknown Date"
                                    val slotPart = parts.getOrNull(1)?.let { "Slot $it" } ?: "Unknown Slot"
                                    val formattedDate = try {
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
                                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(datePart)!!
                                        )
                                    } catch (_: Exception) { datePart }
                                    Pair(0, "$formattedDate ($slotPart)") // Default slot index for Pair
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
                        // Sort the groups:
                        // 1. By date (ascending/descending as you prefer)
                        // 2. Then by the slot index within that date (ascending)
                        .sortedWith(compareByDescending<GroupedExpenses> {
                            // Primary sort: by the timestamp of the first item in the group (effectively by date and then start of slot time)
                            it.expenses.firstOrNull()?.originalTimestamp
                        }.thenBy {
                            // Secondary sort: if you need to refine sorting for groups that might start at the exact same millisecond
                            // (unlikely with 3-hour slots, but good for robustness if needed)
                            // For 3-hour slots based on `originalTimestamp`, the primary sort should be sufficient.
                            // If the title was just the slot index, you'd sort by that.
                            // Here, we can parse the slot index from the title if needed for explicit sorting,
                            // but sorting by `originalTimestamp` of the first item should naturally order the slots.
                            val title = it.groupTitle
                            // Attempt to extract slot index if needed for a more granular sort, though originalTimestamp sort is usually enough
                            val slotIndexFromTitle = try { title.substringAfterLast("Slot ").substringBefore(")").toIntOrNull() ?:
                            title.substringAfterLast("slot").toIntOrNull() // From fallback key
                            } catch (_: Exception) { null }
                            slotIndexFromTitle ?: 0 // Fallback if slot index can't be parsed from title
                        })
                }
            }


        Log.i("gggggg", "processExpensesForDisplay: $uiListItems")
            _uiState.update {
            it.copy(
                isLoading = false,
                expenses = if (it.currentGrouping == GroupingOption.NONE) uiListItems else emptyList(),
                groupedExpenses = if (it.currentGrouping != GroupingOption.NONE) groupedItems else emptyList(),
                totalExpensesCount = entities.size,
                totalExpensesAmountFormatted = currencyFormatter.format(totalAmount)
            )
        }
    }


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

    // Helper to map UI item back to entity if needed for re-processing (simplistic)
    private fun mapUiItemToEntity(item: ExpenseListItem): ExpenseEntity {
        return ExpenseEntity(
            id = item.id,
            title = item.title,
            amount = parseFormattedAmountToDouble(item.amountFormatted, item.title),
            category = item.category.name,
            timestamp = item.originalTimestamp,
            imageUri = item.imageUri,
            notes = "" // Notes not present in ExpenseListItem, add if needed
        )
    }


    private fun parseFormattedAmountToDouble(formattedAmount: String, itemTitleForErrorLog: String = "N/A"): Double {
        val currencySymbol = currencyFormatter.currency?.symbol ?: "₹" // Fallback to "₹"

        // Remove currency symbol AND any grouping separators (commas for INR)
        val cleanedAmountString = formattedAmount
            .removePrefix(currencySymbol)
            .replace(",", "")             // Remove commas used as grouping separators in INR
            .trim()

        return try {
            if (cleanedAmountString.isEmpty()) {
                0.0
            } else {
                plainNumberParser.parse(cleanedAmountString)?.toDouble() ?: 0.0
            }
        } catch (e: ParseException) {
            System.err.println(
                "ParseException in parseFormattedAmountToDouble: Could not parse amount string: '$cleanedAmountString' " +
                        "(original: '$formattedAmount') for item title '$itemTitleForErrorLog'. Error: ${e.message}"
            )
            0.0 // Default to 0.0 if parsing fails
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
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.add(Calendar.MILLISECOND, -1) // End of last day of current week
                val end = calendar.timeInMillis
                return Pair(start,end)
            }
            DateFilterType.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                setCalendarToStartOfDay(calendar)
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1) // End of last day of current month
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
                } ?: return Pair(0L, 0L) // Should not happen if date is selected
            }
             DateFilterType.DATE_RANGE -> {
                val start = currentState.customDateRangeStartMillis ?: 0L
                val end = currentState.customDateRangeEndMillis?.let {
                    // Ensure end of day for the end date
                    val tempCal = Calendar.getInstance().apply { timeInMillis = it }
                    setCalendarToEndOfDay(tempCal)
                    tempCal.timeInMillis
                } ?: System.currentTimeMillis() // Default to now if not set
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
