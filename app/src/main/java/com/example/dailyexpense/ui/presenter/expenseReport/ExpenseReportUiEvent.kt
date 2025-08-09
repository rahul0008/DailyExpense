package com.example.dailyexpense.ui.presenter.expenseReport

import android.net.Uri
import android.widget.Toast

sealed interface ExpenseReportUiEvent {
    data class ShowToast(val message: String, val duration: Int = Toast.LENGTH_SHORT) : ExpenseReportUiEvent
    data class ShareFile(val fileUri: Uri, val mimeType: String) : ExpenseReportUiEvent
}
