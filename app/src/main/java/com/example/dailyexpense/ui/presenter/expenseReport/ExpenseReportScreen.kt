package com.example.dailyexpense.ui.presenter.expenseReport

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dailyexpense.ui.uiComponents.AppText // Assuming this exists and is styled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseReportScreen(
    viewModel: ExpenseReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // This callback is after the share activity closes.
        // For the state-driven share (PDF/TXT), ShareIntentCompleted is sent from its LaunchedEffect.
        // For event-driven share (CSV), the ViewModel handles its state post-event emission.
        // No explicit action might be needed here unless you want common cleanup.
    }

    // Collect one-time UI events from ViewModel (for Toasts, CSV Share)
    LaunchedEffect(key1 = Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ExpenseReportUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, event.duration).show()
                }
                is ExpenseReportUiEvent.ShareFile -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = event.mimeType
                        putExtra(Intent.EXTRA_STREAM, event.fileUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        shareLauncher.launch(Intent.createChooser(shareIntent, "Share Report Via"))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No app found to share this type of file.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Effect to launch Share Intent for PDF/TXT simulation (driven by uiState.triggerShareIntent)
    LaunchedEffect(uiState.triggerShareIntent, uiState.shareableContentUriString) {
        if (uiState.triggerShareIntent && uiState.shareableContentUriString != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = uiState.shareableContentMimeType ?: "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uiState.shareableContentUriString!!.toUri())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                shareLauncher.launch(Intent.createChooser(shareIntent, "Share Report Via"))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No app found to share this type of file.", Toast.LENGTH_LONG).show()
            }
            // Notify ViewModel that share intent has been processed so it can reset triggerShareIntent
            viewModel.onEvent(ExpenseReportScreenEvent.ShareIntentCompleted)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Expense Report", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.reportData == null -> {
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
                        isGeneratingCsv = uiState.isGeneratingCsv, // Pass CSV loading state
                        isSimulatingExport = uiState.isSimulatingExport, // For PDF/TXT simulation card
                        exportSimulationMessage = uiState.exportSimulationMessage, // For PDF/TXT simulation card
                        onExportCsvClicked = { viewModel.onEvent(ExpenseReportScreenEvent.RequestCsvExport) },
                        onExportPdfClicked = { viewModel.onEvent(ExpenseReportScreenEvent.RequestPdfExport) },
                        onExportTxtClicked = { viewModel.onEvent(ExpenseReportScreenEvent.RequestTxtExport) },
                        onDismissSimulationMessage = { viewModel.onEvent(ExpenseReportScreenEvent.DismissExportSimulationMessage) }
                    )
                }
                else -> {
                    AppText(
                        text = "No report data available.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
            }
        }
    }
}
