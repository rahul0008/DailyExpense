package com.example.dailyexpense.ui.presenter.expenseReport

sealed class ExpenseReportScreenEvent {
    data object LoadReport : ExpenseReportScreenEvent()

    // Events for initiating export/share actions
    data object RequestCsvExport : ExpenseReportScreenEvent()
    data object RequestPdfExport : ExpenseReportScreenEvent() // For PDF simulation
    data object RequestTxtExport : ExpenseReportScreenEvent() // For TXT simulation

    // Event after the share intent has been launched (or attempted) by the UI,
    // primarily used by the PDF/TXT simulation flow that relies on uiState.triggerShareIntent.
    data object ShareIntentCompleted : ExpenseReportScreenEvent()

    // Event to dismiss any simulation messages (for PDF/TXT simulation card)
    data object DismissExportSimulationMessage : ExpenseReportScreenEvent()
}