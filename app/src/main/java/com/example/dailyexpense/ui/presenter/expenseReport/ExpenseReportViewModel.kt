package com.example.dailyexpense.ui.presenter.expenseReport

import android.content.Context
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyexpense.db.entity.ExpenseEntity
import com.example.dailyexpense.repo.dbRepo.ExpenseRepository // Ensure this import is correct
import com.example.dailyexpense.util.essentialEnums.ExpenseCategory // Ensure this import is correct
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
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
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseReportScreenState())
    val uiState: StateFlow<ExpenseReportScreenState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ExpenseReportUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
        currency = Currency.getInstance("INR")
    }
    private val reportDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val entityDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // For CSV content
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault()) // For chart labels

    init {
        onEvent(ExpenseReportScreenEvent.LoadReport)
    }

    fun onEvent(event: ExpenseReportScreenEvent) {
        when (event) {
            ExpenseReportScreenEvent.LoadReport -> loadExpenseReportData()
            ExpenseReportScreenEvent.RequestCsvExport -> exportReportAsCsv()
            ExpenseReportScreenEvent.RequestPdfExport -> simulateExport("PDF")
            ExpenseReportScreenEvent.RequestTxtExport -> simulateExport("TXT")
            ExpenseReportScreenEvent.ShareIntentCompleted -> {
                // Clear state related to the direct share trigger (used by PDF/TXT simulation)
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
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -6) // Last 7 days including today
            setCalendarToStartOfDay(calendar)
            val startDate = calendar.timeInMillis

            expenseRepository.getExpensesForDateRange(startDate, endDate)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load report: ${e.message}") }
                    viewModelScope.launch { _eventFlow.emit(ExpenseReportUiEvent.ShowToast("Error loading report data")) }
                }
                .collect { expenses ->
                    processExpensesToReportData(expenses, startDate, endDate)
                }
        }
    }

    private suspend fun processExpensesToReportData(expenses: List<ExpenseEntity>, reportStartDate: Long, reportEndDate: Long) {
        withContext(Dispatchers.Default) { // Use Default dispatcher for CPU-intensive processing
            val dailyTotalsMap = mutableMapOf<String, Double>()
            val dailyLabels = mutableListOf<String>()
            val tempCal = Calendar.getInstance().apply { timeInMillis = reportStartDate }
            val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (i in 0 until 7) {
                val dateKey = dayKeyFormat.format(tempCal.time)
                dailyTotalsMap[dateKey] = 0.0
                dailyLabels.add(reportDateFormat.format(tempCal.time)) // For display and mapping
                tempCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            expenses.forEach { expense ->
                val dateKey = dayKeyFormat.format(Date(expense.timestamp))
                dailyTotalsMap[dateKey] = (dailyTotalsMap[dateKey] ?: 0.0) + expense.amount
            }

            val dailyTotalsList = dailyLabels.mapNotNull { dateLabel ->
                // Find the key in dailyTotalsMap that corresponds to this display dateLabel
                val keyForDateLabel = dailyTotalsMap.keys.firstOrNull { key ->
                    try {
                        reportDateFormat.format(dayKeyFormat.parse(key)!!) == dateLabel
                    } catch (_: Exception) { false }
                }
                keyForDateLabel?.let { key ->
                    val total = dailyTotalsMap[key]!!
                    DailyTotal(dateLabel, total, currencyFormatter.format(total))
                }
            }.sortedBy { it.dateLabel } // Ensure consistent order based on display label


            val dailySpendingChartData = dailyTotalsList.mapNotNull {
                try {
                    ChartDataEntry(
                        label = dayOfWeekFormat.format(reportDateFormat.parse(it.dateLabel)!!),
                        value = it.totalAmount.toFloat()
                    )
                } catch (_: Exception) {
                    // Log or handle parsing error for chart data
                    null
                }
            }

            val overallTotalAmount = expenses.sumOf { it.amount }
            val categoryTotalsList = expenses
                .groupBy { ExpenseCategory.entries.firstOrNull { cat -> cat.name.equals(it.category, ignoreCase = true) } ?: ExpenseCategory.OTHER }
                .map { (category, items) ->
                    val categorySum = items.sumOf { it.amount }
                    CategoryTotal(
                        categoryDisplayName = category.displayName.uppercase(),
                        totalAmount = categorySum,
                        totalAmountFormatted = currencyFormatter.format(categorySum),
                        percentageOfTotal = if (overallTotalAmount > 0) (categorySum / overallTotalAmount).toFloat() else 0f
                    )
                }.sortedByDescending { it.totalAmount }

            val categorySpendingChartData = categoryTotalsList.map {
                ChartDataEntry(label = it.categoryDisplayName.take(3), value = it.totalAmount.toFloat())
            }

            val reportData = ExpenseReportData(
                reportTitle = "Report: ${reportDateFormat.format(Date(reportStartDate))} - ${reportDateFormat.format(Date(reportEndDate))}",
                rawExpensesData = expenses, // Store raw expenses for CSV
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

    private fun exportReportAsCsv() {
        viewModelScope.launch {
            _eventFlow.emit(ExpenseReportUiEvent.ShowToast("Generating CSV report..."))
            _uiState.update { it.copy(isGeneratingCsv = true) }

            val currentReportData = _uiState.value.reportData
            if (currentReportData == null || currentReportData.rawExpensesData.isEmpty()) {
                _eventFlow.emit(ExpenseReportUiEvent.ShowToast("No data available to export."))
                _uiState.update { it.copy(isGeneratingCsv = false) }
                return@launch
            }

            val csvContent = withContext(Dispatchers.IO) { // IO for file operations and string building
                generateCsvContent(currentReportData.rawExpensesData, currentReportData.reportTitle)
            }

            if (csvContent.isBlank()) {
                _eventFlow.emit(ExpenseReportUiEvent.ShowToast("Failed to generate CSV content."))
                _uiState.update { it.copy(isGeneratingCsv = false) }
                return@launch
            }

            try {
                val fileName = "ExpenseReport_${System.currentTimeMillis()}.csv"
                // Ensure "reports" subdir exists or use cacheDir directly
                val reportsDir = File(applicationContext.cacheDir, "reports")
                if (!reportsDir.exists()) {
                    reportsDir.mkdirs()
                }
                val csvFile = File(reportsDir, fileName)

                FileWriter(csvFile).use { writer ->
                    writer.write(csvContent)
                }

                val authority = "${applicationContext.packageName}.fileprovider"
                val fileUri = FileProvider.getUriForFile(applicationContext, authority, csvFile)

                _eventFlow.emit(ExpenseReportUiEvent.ShareFile(fileUri, "text/csv"))
                _eventFlow.emit(ExpenseReportUiEvent.ShowToast("CSV report ready to share."))
            } catch (e: Exception) {
                // Log.e("ExpenseReportVM", "Error saving/sharing CSV", e)
                _eventFlow.emit(ExpenseReportUiEvent.ShowToast("Error creating CSV: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isGeneratingCsv = false) }
            }
        }
    }

    private fun generateCsvContent(expenses: List<ExpenseEntity>, reportTitle: String): String {
        val stringBuilder = StringBuilder()
        // CSV Header
        stringBuilder.append("Report Title:,$reportTitle\n")
        stringBuilder.append("Exported Date:,${entityDateFormat.format(Date())}\n\n")
        stringBuilder.append("Date,Title,Amount,Category,Notes\n")

        // CSV Rows
        expenses.sortedBy { it.timestamp }.forEach { expense -> // Sort by timestamp for consistency
            stringBuilder.append("\"${entityDateFormat.format(Date(expense.timestamp))}\",")
            stringBuilder.append("\"${expense.title.replace("\"", "\"\"")}\",") // Escape quotes
            stringBuilder.append("${expense.amount},") // Assuming amount is plain number
            stringBuilder.append("\"${expense.category.replace("\"", "\"\"")}\",")
            stringBuilder.append("\"${(expense.notes ?: "").replace("\"", "\"\"")}\"\n")
        }
        return stringBuilder.toString()
    }

    private fun simulateExport(format: String) { // For PDF/TXT
        viewModelScope.launch {
            _uiState.update { it.copy(isSimulatingExport = true, exportSimulationMessage = "Simulating $format export...") }
            delay(1500) // Simulate work

            val dummyFileName = "simulated_report_${System.currentTimeMillis()}.${format.lowercase()}"
            val reportsDir = File(applicationContext.cacheDir, "reports") // Use same subdir
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }
            val dummyFile = File(reportsDir, dummyFileName)

            try {
                dummyFile.createNewFile()
                dummyFile.writeText("This is a simulated $format report.\nReport Title: ${_uiState.value.reportData?.reportTitle ?: "N/A"}")

                val authority = "${applicationContext.packageName}.fileprovider"
                val fileUri = FileProvider.getUriForFile(applicationContext, authority, dummyFile)

                _uiState.update {
                    it.copy(
                        isSimulatingExport = false,
                        exportSimulationMessage = "$format export simulation complete. Ready to share.",
                        triggerShareIntent = true, // This tells the UI to use its state-driven share
                        shareableContentUriString = fileUri.toString(),
                        shareableContentMimeType = when (format.uppercase()) {
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
                viewModelScope.launch { _eventFlow.emit(ExpenseReportUiEvent.ShowToast("Error in $format simulation"))}
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
