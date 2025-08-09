package com.example.dailyexpense.ui.presenter.expenseReport

import com.example.dailyexpense.db.entity.ExpenseEntity // Make sure this import is correct

data class ExpenseReportScreenState(
    val isLoading: Boolean = true, // For initial data load
    val reportData: ExpenseReportData? = null,
    val error: String? = null,

    // State for managing the share action (primarily for PDF/TXT simulation via state)
    val triggerShareIntent: Boolean = false,
    val shareableContentMimeType: String? = null,
    val shareableContentUriString: String? = null,

    // State to indicate a mock export action is in progress (for PDF/TXT simulation)
    val isSimulatingExport: Boolean = false,
    val exportSimulationMessage: String? = null,

    // Specific state for CSV generation loading indicator
    val isGeneratingCsv: Boolean = false
)

data class ExpenseReportData(
    val reportTitle: String = "Last 7 Days Report",
    val dailyTotals: List<DailyTotal> = emptyList(),
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val overallTotalAmount: Double = 0.0,
    val overallTotalAmountFormatted: String = "",
    val dailySpendingChartData: List<ChartDataEntry> = emptyList(),
    val categorySpendingChartData: List<ChartDataEntry> = emptyList(),
    val rawExpensesData: List<ExpenseEntity> = emptyList() // For CSV generation
)

data class DailyTotal(
    val dateLabel: String,
    val totalAmount: Double,
    val totalAmountFormatted: String
)

data class CategoryTotal(
    val categoryDisplayName: String,
    val totalAmount: Double,
    val totalAmountFormatted: String,
    val percentageOfTotal: Float // e.g., 0.25f for 25%
)

data class ChartDataEntry(
    val label: String,        // For X-axis (e.g., date or category name)
    val value: Float          // For Y-axis (e.g., amount)
)
