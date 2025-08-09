package com.example.dailyexpense.ui.presenter.expenseReport

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dailyexpense.ui.uiComponents.AppText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseReportScreen(
    viewModel: ExpenseReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Effect to launch the Share Intent when triggered
    LaunchedEffect(uiState.triggerShareIntent, uiState.shareableContentUriString) {
        if (uiState.triggerShareIntent && uiState.shareableContentUriString != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = uiState.shareableContentMimeType ?: "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(uiState.shareableContentUriString))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Report Via"))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No app found to share this type of file.", Toast.LENGTH_LONG).show()
            }
            // Notify ViewModel that share intent has been processed
            viewModel.onEvent(ExpenseReportScreenEvent.ShareIntentCompleted)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Report") },
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.reportData == null -> { // Show full screen loading only on initial load
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.reportData != null -> {
                    ExpenseReportScreenLayout(
                        reportData = uiState.reportData!!,
                        isSimulatingExport = uiState.isSimulatingExport,
                        exportSimulationMessage = uiState.exportSimulationMessage,
                        onExportCsvClicked = { viewModel.onEvent(ExpenseReportScreenEvent.RequestCsvExport) },
                        onExportPdfClicked = { viewModel.onEvent(ExpenseReportScreenEvent.RequestPdfExport) },
                        onExportTxtClicked = { viewModel.onEvent(ExpenseReportScreenEvent.RequestTxtExport) },
                        onDismissSimulationMessage = { viewModel.onEvent(ExpenseReportScreenEvent.DismissExportSimulationMessage) }
                    )
                }
                else -> { // Handles case where not loading, no error, but no data (e.g., after a failed load cleared data)
                    AppText(
                        text = "No report data available.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
            }
        }
    }
}
