package com.example.dailyexpense.ui.presenter.expenseReport


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface // Keep this for Previews
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyexpense.ui.theme.DailyExpenseTheme
import com.example.dailyexpense.ui.uiComponents.AppPrimaryButton
import com.example.dailyexpense.ui.uiComponents.AppText
import com.example.dailyexpense.ui.uiComponents.AppTextButton
import com.example.dailyexpense.ui.uiComponents.AppYBarChart // <--- IMPORT AppYBarChart


@Composable
fun ExpenseReportScreenLayout(
    reportData: ExpenseReportData,
    isSimulatingExport: Boolean,
    exportSimulationMessage: String?,
    onExportCsvClicked: () -> Unit,
    onExportPdfClicked: () -> Unit,
    onExportTxtClicked: () -> Unit,
    onDismissSimulationMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AppText(
            text = reportData.reportTitle,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(16.dp))

        AppInfoRow(
            label = "Overall Total:",
            value = reportData.overallTotalAmountFormatted,
            labelStyle = MaterialTheme.typography.bodyLarge,
            valueStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        AppReportSectionTitle("Daily Totals")
        if (reportData.dailyTotals.isEmpty()) {
            AppText(
                text = "No daily expenses recorded for this period.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            reportData.dailyTotals.forEach { daily ->
                AppInfoRow(
                    label = daily.dateLabel,
                    value = daily.totalAmountFormatted
                )
            }
        }
        // Replace AppMockChartViewInternal with AppYBarChart
        AppYBarChart( // <--- REPLACEMENT
            title = "Daily Spending Overview",
            chartDataEntries = reportData.dailySpendingChartData,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth() // Optional: AppYBarChart might handle its own width
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        AppReportSectionTitle("Category Totals")
        if (reportData.categoryTotals.isEmpty()) {
            AppText(
                text = "No expenses by category for this period.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            reportData.categoryTotals.forEach { category ->
                AppInfoRow(
                    label = "${category.categoryDisplayName}:",
                    value = "${category.totalAmountFormatted} (${String.format("%.1f%%", category.percentageOfTotal * 100)})"
                )
            }
        }
        // Replace AppMockChartViewInternal with AppYBarChart
        AppYBarChart( // <--- REPLACEMENT
            title = "Category Spending Breakdown",
            chartDataEntries = reportData.categorySpendingChartData,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth() // Optional: AppYBarChart might handle its own width
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        AppReportSectionTitle("Export Options")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppPrimaryButton(
                text = "Share as CSV",
                onClick = onExportCsvClicked,
                enabled = !isSimulatingExport,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            AppPrimaryButton(
                text = "Share as PDF (Simulated)",
                onClick = onExportPdfClicked,
                enabled = !isSimulatingExport,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            AppPrimaryButton(
                text = "Share as TXT (Simulated)",
                onClick = onExportTxtClicked,
                enabled = !isSimulatingExport,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
        }

        if (isSimulatingExport || exportSimulationMessage != null) {
            Spacer(Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    if (isSimulatingExport) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            AppText(
                                text = exportSimulationMessage ?: "Processing your report...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else if (exportSimulationMessage != null) {
                        AppText(
                            text = exportSimulationMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                        AppTextButton(
                            text = "OK",
                            onClick = onDismissSimulationMessage,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}


@Composable
private fun AppReportSectionTitle(title: String, modifier: Modifier = Modifier) {
    AppText(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun AppInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    valueStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = label,
            style = labelStyle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        AppText(
            text = value,
            style = valueStyle,
            textAlign = TextAlign.End
        )
    }
}

// You can now remove AppMockChartViewInternal if it's no longer used anywhere else.
// @Composable
// private fun AppMockChartViewInternal(
//     title: String,
//     data: List<ChartDataEntry>,
//     modifier: Modifier = Modifier
// ) {
//     // ... old implementation
// }


// --- PREVIEW FUNCTIONS ---
// (Your existing preview functions remain largely the same, they will now use AppYBarChart via ExpenseReportScreenLayout)

@Preview(showBackground = true, name = "Expense Report - Full Data")
@Composable
fun ExpenseReportScreenLayoutPreview_Full() {
    val sampleReportDataFull = ExpenseReportData(
        reportTitle = "Monthly Expense Summary - October",
        overallTotalAmount = 15750.50,
        overallTotalAmountFormatted = "₹15,750.50",
        dailyTotals = listOf(
            DailyTotal("Oct 01, 2023", 1200.00, "₹1,200.00"),
            DailyTotal("Oct 02, 2023", 850.75, "₹850.75"),
            DailyTotal("Oct 03, 2023", 2100.00, "₹2,100.00")
        ),
        categoryTotals = listOf(
            CategoryTotal("Food & Dining", 4500.00, "₹4,500.00", 0.285f),
            CategoryTotal("Travel", 3200.00, "₹3,200.00", 0.203f),
            CategoryTotal("Utilities", 2800.00, "₹2,800.00", 0.178f),
            CategoryTotal("Entertainment", 1500.25, "₹1,500.25", 0.095f),
            CategoryTotal("Other", 3750.25, "₹3,750.25", 0.238f)
        ),
        dailySpendingChartData = listOf(
            ChartDataEntry("Oct 01", 1200.00f),
            ChartDataEntry("Oct 02", 850.75f),
            ChartDataEntry("Oct 03", 2100.00f),
            ChartDataEntry("Oct 04", 500.00f),
            ChartDataEntry("Oct 05", 1150.00f)
        ),
        categorySpendingChartData = listOf(
            ChartDataEntry("Food", 4500.00f),
            ChartDataEntry("Travel", 3200.00f),
            ChartDataEntry("Utilities", 2800.00f),
            ChartDataEntry("Entertain", 1500.25f),
            ChartDataEntry("Other", 3750.25f)
        )
    )

    DailyExpenseTheme {
        Surface { // Ensure Surface is wrapping for background color from theme
            ExpenseReportScreenLayout(
                reportData = sampleReportDataFull,
                isSimulatingExport = false,
                exportSimulationMessage = null,
                onExportCsvClicked = {},
                onExportPdfClicked = {},
                onExportTxtClicked = {},
                onDismissSimulationMessage = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Expense Report - Empty State")
@Composable
fun ExpenseReportScreenLayoutPreview_Empty() {
    val sampleReportDataEmpty = ExpenseReportData(
        reportTitle = "No Expenses Logged Yet",
        overallTotalAmount = 0.0,
        overallTotalAmountFormatted = "₹0.00",
        dailyTotals = emptyList(),
        categoryTotals = emptyList(),
        dailySpendingChartData = emptyList(),
        categorySpendingChartData = emptyList()
    )

    DailyExpenseTheme {
        Surface {
            ExpenseReportScreenLayout(
                reportData = sampleReportDataEmpty,
                isSimulatingExport = false,
                exportSimulationMessage = null,
                onExportCsvClicked = {},
                onExportPdfClicked = {},
                onExportTxtClicked = {},
                onDismissSimulationMessage = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Expense Report - Simulating Export")
@Composable
fun ExpenseReportScreenLayoutPreview_SimulatingExport() {
    val sampleReportData = ExpenseReportData(
        reportTitle = "Quick Report",
        overallTotalAmount = 500.00,
        overallTotalAmountFormatted = "₹500.00",
        dailyTotals = listOf(DailyTotal("Today", 500.00, "₹500.00")),
        categoryTotals = listOf(CategoryTotal("Misc", 500.00, "₹500.00", 1.0f)),
        dailySpendingChartData = listOf(ChartDataEntry("Today", 500f)),
        categorySpendingChartData = listOf(ChartDataEntry("Misc", 500f))
    )

    DailyExpenseTheme {
        Surface {
            ExpenseReportScreenLayout(
                reportData = sampleReportData,
                isSimulatingExport = true,
                exportSimulationMessage = "Generating your PDF report...",
                onExportCsvClicked = {},
                onExportPdfClicked = {},
                onExportTxtClicked = {},
                onDismissSimulationMessage = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Expense Report - Simulation Message")
@Composable
fun ExpenseReportScreenLayoutPreview_SimulationMessage() {
    val sampleReportData = ExpenseReportData(
        reportTitle = "Another Report",
        overallTotalAmount = 250.00,
        overallTotalAmountFormatted = "₹250.00",
        dailyTotals = listOf(DailyTotal("Yesterday", 250.00, "₹250.00")),
        categoryTotals = listOf(CategoryTotal("Snacks", 250.00, "₹250.00", 1.0f)),
        dailySpendingChartData = listOf(ChartDataEntry("Yesterday", 250f)),
        categorySpendingChartData = listOf(ChartDataEntry("Snacks", 250f))
    )

    DailyExpenseTheme {
        Surface {
            ExpenseReportScreenLayout(
                reportData = sampleReportData,
                isSimulatingExport = false,
                exportSimulationMessage = "Your TXT file has been shared via dummy intent!",
                onExportCsvClicked = {},
                onExportPdfClicked = {},
                onExportTxtClicked = {},
                onDismissSimulationMessage = {}
            )
        }
    }
}

// Ensure your Data classes (ExpenseReportData, DailyTotal, CategoryTotal, ChartDataEntry)
// are defined correctly and are accessible here.
// For example:
// data class ExpenseReportData(
//     val reportTitle: String,
//     val overallTotalAmount: Double,
//     val overallTotalAmountFormatted: String,
//     val dailyTotals: List<DailyTotal>,
//     val categoryTotals: List<CategoryTotal>,
//     val dailySpendingChartData: List<ChartDataEntry>,
//     val categorySpendingChartData: List<ChartDataEntry>
// )
// data class DailyTotal(val dateLabel: String, val totalAmount: Double, val totalAmountFormatted: String)
// data class CategoryTotal(
//     val categoryDisplayName: String,
//     val totalAmount: Double,
//     val totalAmountFormatted: String,
//     val percentageOfTotal: Float
// )
// data class ChartDataEntry(val label: String, val value: Float) // Already defined for AppYBarChart

