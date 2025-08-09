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
import androidx.compose.material3.HorizontalDivider // Corrected import
import androidx.compose.material3.DividerDefaults // For thickness/color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyexpense.ui.theme.DailyExpenseTheme // Assuming your theme
import com.example.dailyexpense.ui.uiComponents.AppPrimaryButton // Assuming this exists
import com.example.dailyexpense.ui.uiComponents.AppText // Assuming this exists
import com.example.dailyexpense.ui.uiComponents.AppTextButton // Assuming this exists
import com.example.dailyexpense.ui.uiComponents.AppYBarChart // Assuming this exists
import java.util.Locale // For string formatting

@Composable
fun ExpenseReportScreenLayout(
    reportData: ExpenseReportData,
    isGeneratingCsv: Boolean, // New state for CSV export button
    isSimulatingExport: Boolean, // For PDF/TXT simulation card and button state
    exportSimulationMessage: String?, // For PDF/TXT simulation card
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
            thickness = DividerDefaults.Thickness, // Use DividerDefaults
            color = DividerDefaults.color         // Use DividerDefaults
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
        if (reportData.dailySpendingChartData.isNotEmpty()) {
            AppYBarChart(
                title = "Daily Spending Overview",
                chartDataEntries = reportData.dailySpendingChartData,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            )
        }
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
                    value = buildString {
                        append(category.totalAmountFormatted)
                        append(" (")
                        append(String.format(Locale.getDefault(), "%.1f%%", category.percentageOfTotal * 100))
                        append(")")
                    }
                )
            }
        }
        if (reportData.categorySpendingChartData.isNotEmpty()) {
            AppYBarChart(
                title = "Category Spending Breakdown",
                chartDataEntries = reportData.categorySpendingChartData,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            )
        }
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
                text = if (isGeneratingCsv) "Generating CSV..." else "Share as CSV",
                onClick = onExportCsvClicked,
                enabled = !isGeneratingCsv && !isSimulatingExport, // Disable if CSV generating or other simulation running
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            AppPrimaryButton(
                text = "Share as PDF (Simulated)",
                onClick = onExportPdfClicked,
                enabled = !isSimulatingExport && !isGeneratingCsv, // Disable if simulation running or CSV generating
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            AppPrimaryButton(
                text = "Share as TXT (Simulated)",
                onClick = onExportTxtClicked,
                enabled = !isSimulatingExport && !isGeneratingCsv, // Disable if simulation running or CSV generating
                modifier = Modifier.fillMaxWidth(0.85f)
            )
        }

        // Card for PDF/TXT simulation messages
        if (isSimulatingExport || (exportSimulationMessage != null)) {
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
                    if (isSimulatingExport && exportSimulationMessage != null) { // Show progress if actively simulating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            AppText(
                                text = exportSimulationMessage, //  "Processing your report..."
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else if (exportSimulationMessage != null) { // Show message if simulation done or error
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
        Spacer(Modifier.height(16.dp)) // Final spacer for scroll padding
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
            maxLines = 2, // Allow label to wrap slightly
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f) // Give label space
        )
        Spacer(Modifier.width(8.dp)) // Space between label and value
        AppText(
            text = value,
            style = valueStyle,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f) // Give value space
        )
    }
}

// --- PREVIEW FUNCTIONS ---
@Preview(showBackground = true, name = "Expense Report - Full Data")
@Composable
fun ExpenseReportScreenLayoutPreview_Full() {
    val sampleDailyTotals = listOf(
        DailyTotal("Jul 01, 2024", 150.0, "₹150.00"),
        DailyTotal("Jul 02, 2024", 200.0, "₹200.00")
    )
    val sampleCategoryTotals = listOf(
        CategoryTotal("FOOD", 250.0, "₹250.00", 0.714f),
        CategoryTotal("TRANSPORT", 100.0, "₹100.00", 0.286f)
    )
    val sampleChartData = listOf(
        ChartDataEntry("Mon", 150f),
        ChartDataEntry("Tue", 200f)
    )
    val sampleReportDataFull = ExpenseReportData(
        reportTitle = "Sample Report: Jul 01 - Jul 02, 2024",
        overallTotalAmount = 350.0,
        overallTotalAmountFormatted = "₹350.00",
        dailyTotals = sampleDailyTotals,
        categoryTotals = sampleCategoryTotals,
        dailySpendingChartData = sampleChartData,
        categorySpendingChartData = sampleCategoryTotals.map { ChartDataEntry(it.categoryDisplayName, it.totalAmount.toFloat()) },
        rawExpensesData = emptyList() // Not visually represented in layout preview directly
    )

    DailyExpenseTheme {
        Surface {
            ExpenseReportScreenLayout(
                reportData = sampleReportDataFull,
                isGeneratingCsv = false, // Default state
                isSimulatingExport = false, // Default state
                exportSimulationMessage = null, // Default state
                onExportCsvClicked = {},
                onExportPdfClicked = {},
                onExportTxtClicked = {},
                onDismissSimulationMessage = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Expense Report - Empty Data")
@Composable
fun ExpenseReportScreenLayoutPreview_Empty() {
    val sampleReportDataEmpty = ExpenseReportData(
        reportTitle = "Sample Report: No Data",
        overallTotalAmount = 0.0,
        overallTotalAmountFormatted = "₹0.00",
        dailyTotals = emptyList(),
        categoryTotals = emptyList(),
        dailySpendingChartData = emptyList(),
        categorySpendingChartData = emptyList(),
        rawExpensesData = emptyList()
    )
    DailyExpenseTheme {
        Surface {
            ExpenseReportScreenLayout(
                reportData = sampleReportDataEmpty,
                isGeneratingCsv = false,
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

@Preview(showBackground = true, name = "Expense Report - Generating CSV")
@Composable
fun ExpenseReportScreenLayoutPreview_GeneratingCsv() {
    val sampleReportDataFull = ExpenseReportData( /* ... same as sampleReportDataFull above ... */ )
    DailyExpenseTheme {
        Surface {
            ExpenseReportScreenLayout(
                reportData = sampleReportDataFull, // Use some data to see the layout
                isGeneratingCsv = true, // Key state for this preview
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

@Preview(showBackground = true, name = "Expense Report - Simulating PDF Export")
@Composable
fun ExpenseReportScreenLayoutPreview_SimulatingPdf() {
    val sampleReportDataFull = ExpenseReportData( /* ... same as sampleReportDataFull above ... */ )
    DailyExpenseTheme {
        Surface {
            ExpenseReportScreenLayout(
                reportData = sampleReportDataFull,
                isGeneratingCsv = false,
                isSimulatingExport = true, // Key state for this preview
                exportSimulationMessage = "Processing your PDF report...", // Message during simulation
                onExportCsvClicked = {},
                onExportPdfClicked = {},
                onExportTxtClicked = {},
                onDismissSimulationMessage = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Expense Report - Simulation Message Ready")
@Composable
fun ExpenseReportScreenLayoutPreview_SimulationMessage() {
    val sampleReportDataFull = ExpenseReportData( /* ... same as sampleReportDataFull above ... */ )
    DailyExpenseTheme {
        Surface {
            ExpenseReportScreenLayout(
                reportData = sampleReportDataFull,
                isGeneratingCsv = false,
                isSimulatingExport = false, // Simulation is done
                exportSimulationMessage = "Your TXT report is ready to be shared!", // Message after simulation
                onExportCsvClicked = {},
                onExportPdfClicked = {},
                onExportTxtClicked = {},
                onDismissSimulationMessage = {}
            )
        }
    }
}

