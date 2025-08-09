package com.example.dailyexpense.ui.presenter.expenseReport

sealed class ExpenseReportScreenEvent {
    data object LoadReport : ExpenseReportScreenEvent()

    // Events for initiating export/share actions
    data object RequestCsvExport : ExpenseReportScreenEvent()
    data object RequestPdfExport : ExpenseReportScreenEvent()
    data object RequestTxtExport : ExpenseReportScreenEvent() // Example for another format

    // Event after the share intent has been launched (or attempted) by the UI
    data object ShareIntentCompleted : ExpenseReportScreenEvent()

    // Event to dismiss any simulation messages
    data object DismissExportSimulationMessage : ExpenseReportScreenEvent()
}