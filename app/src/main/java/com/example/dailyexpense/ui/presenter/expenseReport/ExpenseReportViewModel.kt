package com.example.dailyexpense.ui.presenter.expenseReport

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyexpense.db.entity.ExpenseEntity // Assuming this is your DB entity
import com.example.dailyexpense.repo.dbRepo.ExpenseRepository
import com.example.dailyexpense.util.essentialEnums.ExpenseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay // For simulation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExpenseReportViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    @ApplicationContext private val applicationContext: Context // Still useful for package name
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseReportScreenState())
    val uiState: StateFlow<ExpenseReportScreenState> = _uiState.asStateFlow()

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
    }
    private val reportDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())

    init {
        onEvent(ExpenseReportScreenEvent.LoadReport)
    }

    fun onEvent(event: ExpenseReportScreenEvent) {
        when (event) {
            ExpenseReportScreenEvent.LoadReport -> loadExpenseReportData()
            ExpenseReportScreenEvent.RequestCsvExport -> simulateExport("CSV")
            ExpenseReportScreenEvent.RequestPdfExport -> simulateExport("PDF")
            ExpenseReportScreenEvent.RequestTxtExport -> simulateExport("TXT")
            ExpenseReportScreenEvent.ShareIntentCompleted -> {
                _uiState.update {
                    it.copy(
                        triggerShareIntent = false,
                        shareableContentUriString = null,
                        shareableContentMimeType = null
                    )
                }
            }
            ExpenseReportScreenEvent.DismissExportSimulationMessage -> {
                _uiState.update { it.copy(exportSimulationMessage = null, isSimulatingExport = false) }
            }
        }
    }

    private fun loadExpenseReportData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            setCalendarToStartOfDay(calendar)
            val startDate = calendar.timeInMillis

            expenseRepository.getExpensesForDateRange(startDate, endDate)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load report: ${e.message}") }
                }
                .collect { expenses ->
                    processExpensesToReportData(expenses, startDate, endDate)
                }
        }
    }

    private suspend fun processExpensesToReportData(expenses: List<ExpenseEntity>, reportStartDate: Long, reportEndDate: Long) {
        withContext(Dispatchers.Default) {
            val dailyTotalsMap = mutableMapOf<String, Double>()
            val dailyLabels = mutableListOf<String>()
            val tempCal = Calendar.getInstance().apply { timeInMillis = reportStartDate }
            val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (i in 0 until 7) {
                val dateKey = dayKeyFormat.format(tempCal.time)
                dailyTotalsMap[dateKey] = 0.0
                dailyLabels.add(reportDateFormat.format(tempCal.time))
                tempCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            expenses.forEach { expense ->
                val dateKey = dayKeyFormat.format(Date(expense.timestamp))
                dailyTotalsMap[dateKey] = (dailyTotalsMap[dateKey] ?: 0.0) + expense.amount
            }

            val dailyTotalsList = dailyLabels.mapNotNull { dateLabel ->
                val keyToFind = dailyTotalsMap.keys.firstOrNull { key ->
                    try { reportDateFormat.format(dayKeyFormat.parse(key)!!) == dateLabel } catch (e: Exception) { false }
                }
                keyToFind?.let { key ->
                    val total = dailyTotalsMap[key]!!
                    DailyTotal(dateLabel, total, currencyFormatter.format(total))
                }
            }.sortedBy { it.dateLabel }

            val dailySpendingChartData = dailyTotalsList.mapNotNull {
                try {
                    ChartDataEntry(label = dayOfWeekFormat.format(reportDateFormat.parse(it.dateLabel)!!), value = it.totalAmount.toFloat())
                } catch (e: Exception) { null } // Handle parse exception
            }

            val overallTotalAmount = expenses.sumOf { it.amount }
            val categoryTotalsList = expenses
                .groupBy { ExpenseCategory.entries.firstOrNull { cat -> cat.name.equals(it.category, ignoreCase = true) } ?: ExpenseCategory.OTHER }
                .map { (category, items) ->
                    val categorySum = items.sumOf { it.amount }
                    CategoryTotal(
                        category.displayName.uppercase(),
                        categorySum,
                        currencyFormatter.format(categorySum),
                        if (overallTotalAmount > 0) (categorySum / overallTotalAmount).toFloat() else 0f
                    )
                }.sortedByDescending { it.totalAmount }

            val categorySpendingChartData = categoryTotalsList.map {
                ChartDataEntry(label = it.categoryDisplayName, value = it.totalAmount.toFloat())
            }

            val reportData = ExpenseReportData(
                reportTitle = "Report: ${reportDateFormat.format(Date(reportStartDate))} - ${reportDateFormat.format(Date(reportEndDate))}",
                dailyTotals = dailyTotalsList,
                categoryTotals = categoryTotalsList,
                overallTotalAmount = overallTotalAmount,
                overallTotalAmountFormatted = currencyFormatter.format(overallTotalAmount),
                dailySpendingChartData = dailySpendingChartData,
                categorySpendingChartData = categorySpendingChartData
            )
            _uiState.update { it.copy(isLoading = false, reportData = reportData, error = null) }
        }
    }

    private fun simulateExport(format: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSimulatingExport = true, exportSimulationMessage = "Simulating $format export...") }
            delay(1500) // Simulate work

            // In a real scenario, you'd generate a file and get its URI.
            // For simulation, we can create a dummy URI or just indicate completion.
            // To trigger the share intent for UI testing, we can create a dummy file.
            val dummyFileName = "simulated_report_${System.currentTimeMillis()}.${format.lowercase()}"
            val dummyFile = File(applicationContext.cacheDir, dummyFileName)
            try {
                dummyFile.createNewFile() // Create an empty file
                dummyFile.writeText("This is a simulated $format report.\nReport Title: ${_uiState.value.reportData?.reportTitle ?: "N/A"}")

                val authority = "${applicationContext.packageName}.fileprovider"
                val fileUri = FileProvider.getUriForFile(applicationContext, authority, dummyFile)

                _uiState.update {
                    it.copy(
                        isSimulatingExport = false,
                        exportSimulationMessage = "$format export simulation complete. Ready to share.",
                        triggerShareIntent = true, // Set this to true
                        shareableContentUriString = fileUri.toString(),
                        shareableContentMimeType = when (format.uppercase()) {
                            "CSV" -> "text/csv"
                            "PDF" -> "application/pdf"
                            "TXT" -> "text/plain"
                            else -> "application/octet-stream"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSimulatingExport = false,
                        exportSimulationMessage = "Error during $format simulation: ${e.message}",
                        triggerShareIntent = false
                    )
                }
            }
        }
    }

    private fun setCalendarToStartOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }
}
