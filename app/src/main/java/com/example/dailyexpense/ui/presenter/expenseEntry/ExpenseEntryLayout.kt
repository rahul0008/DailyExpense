package com.example.dailyexpense.ui.presenter.expenseEntry


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyexpense.R
import com.example.dailyexpense.ui.theme.DailyExpenseTheme
import com.example.dailyexpense.ui.uiComponents.AppPrimaryButton
import com.example.dailyexpense.ui.uiComponents.AppText
import com.example.dailyexpense.ui.uiComponents.AppTextField
import com.example.dailyexpense.util.essentialEnums.ExpenseCategory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryLayout(
    state: ExpenseScreenState, onEvent: (ExpenseScreenEvent) -> Unit, modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val successIndicatorVisible = state.submissionStatus == SubmissionStatus.Success

    LaunchedEffect(state.submissionStatus) {
        if (state.submissionStatus == SubmissionStatus.Success) {
            delay(2000)
            onEvent(ExpenseScreenEvent.ClearSubmissionStatus)
        }
    }

    Scaffold(
        modifier = modifier, topBar = {
            TopAppBar(
                title = {
                    AppText(
                        "Add New Expense", style = MaterialTheme.typography.headlineSmall
                    )
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TotalSpentTodayCard(totalSpent = state.totalSpentTodayFormatted)

                Spacer(modifier = Modifier.height(24.dp))

                AppText(
                    "Enter Expense Details",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AppTextField(
                            value = state.expenseTitle,
                            onValueChange = { onEvent(ExpenseScreenEvent.TitleChanged(it)) },
                            label = { AppText("Title*") },
                            placeholder = { AppText("e.g., Lunch with team") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            isError = state.titleError != null,
                            supportingText = {
                                state.titleError?.let {
                                    AppText(
                                        it, color = MaterialTheme.colorScheme.error
                                    )
                                }
                            })

                        Spacer(modifier = Modifier.height(16.dp))

                        AppTextField(
                            value = state.expenseAmount,
                            onValueChange = { onEvent(ExpenseScreenEvent.AmountChanged(it)) },
                            label = { AppText("Amount (₹)*") },
                            placeholder = { AppText("e.g., 500.00") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            isError = state.amountError != null,
                            supportingText = {
                                state.amountError?.let {
                                    AppText(
                                        it, color = MaterialTheme.colorScheme.error
                                    )
                                }
                            })

                        Spacer(modifier = Modifier.height(16.dp))

                        // Inside ExpenseEntryLayout's Card:
                        CategorySelector(
                            selectedCategory = state.selectedCategory,
                            onCategorySelected = { category ->
                                onEvent(ExpenseScreenEvent.CategorySelected(category))
                            },
                            isExpanded = state.isCategoryDropdownExpanded, // This state comes from ViewModel
                            onExpandedChange = { onEvent(ExpenseScreenEvent.ToggleCategoryDropdown) } // This event should toggle the state in ViewModel
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AppTextField(
                            value = state.expenseNotes,
                            onValueChange = { onEvent(ExpenseScreenEvent.NotesChanged(it)) },
                            label = { AppText("Optional Notes") },
                            placeholder = { AppText("Any details... (max 100 chars)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                            ),
                            singleLine = false,
                            maxLines = 3,
                            supportingText = {
                                AppText("${state.expenseNotes.length}/100")
                            })

                        Spacer(modifier = Modifier.height(16.dp))

                        ReceiptImageSection(
                            imageUri = state.receiptImageUri, onImageClick = {
                                onEvent(
                                    ExpenseScreenEvent.ReceiptImageSelected(
                                        if (state.receiptImageUri == null) "mock_uri_placeholder" else null
                                    )
                                )
                            })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppPrimaryButton(
                    text = if (state.isSubmitting) "Adding..." else "Add Expense",
                    onClick = { onEvent(ExpenseScreenEvent.SubmitExpense) }, // Context is still needed for Toast in ViewModel
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            AnimatedVisibility(
                visible = successIndicatorVisible,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(
                    animationSpec = tween(
                        300
                    )
                ),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(
                    animationSpec = tween(
                        300
                    )
                ),
                modifier = Modifier.align(Alignment.Center)
            ) {
                SuccessIndicator()
            }
        }
    }
}

@Composable
fun TotalSpentTodayCard(totalSpent: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                AppText(
                    "Total Spent Today",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                AppText(
                    totalSpent,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_wallet),
                contentDescription = "Wallet Icon",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: ExpenseCategory,
    onCategorySelected: (ExpenseCategory) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: () -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { onExpandedChange() },
        modifier = Modifier.fillMaxWidth()
    ) {
        AppTextField(
            value = selectedCategory.displayName,
            onValueChange = { /* Read Only */ },
            readOnly = true,
            label = { AppText("Category*") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange() },
            modifier = Modifier.fillMaxWidth()
        ) {
            ExpenseCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { AppText(category.displayName) }, onClick = {
                    onCategorySelected(category)
                }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}


@Composable
fun ReceiptImageSection(imageUri: String?, onImageClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.Start) {
        AppText("Receipt (Optional)", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onImageClick)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = painterResource(
                        id = R.drawable.ic_reciept
                    ),
                    contentDescription = "Receipt Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_reciept),
                        contentDescription = "Add Receipt",
                        modifier = Modifier.size(40.dp),
                        // tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    AppText("Tap to add image", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun SuccessIndicator() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppText(
                "Expense Added!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}


@Preview(showBackground = true, name = "Expense Entry Light Theme")
@Preview(
    showBackground = true,
    name = "Expense Entry Dark Theme",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ExpenseEntryLayoutPreview() {
    // Define a sample state for the preview
    val sampleState = ExpenseScreenState(
        expenseTitle = "Team Lunch",
        expenseAmount = "1250.75",
        selectedCategory = ExpenseCategory.FOOD,
        expenseNotes = "Lunch meeting with the development team at The Good Place.",
        totalSpentTodayFormatted = "₹2,300.50",
        submissionStatus = SubmissionStatus.Idle, // Or SubmissionStatus.Success to see the indicator
        titleError = null, // "Title cannot be empty" to see error state
        amountError = null, // "Invalid amount" to see error state
        receiptImageUri = null // or "mock_uri_placeholder" to see the image version
    )

    // A simple no-op onEvent for preview purposes
    val onEvent: (ExpenseScreenEvent) -> Unit = remember { {} }

    // Apply your app's theme for accurate preview
    // Replace DailyExpenseTheme with your actual theme composable
    DailyExpenseTheme {
        Surface(color = MaterialTheme.colorScheme.background) { // Optional: Use Surface for background
            ExpenseEntryLayout(
                state = sampleState, onEvent = onEvent
            )
        }
    }
}

@Preview(showBackground = true, name = "Expense Entry With Errors")
@Composable
fun ExpenseEntryLayoutErrorPreview() {
    val errorState = ExpenseScreenState(
        expenseTitle = "",
        expenseAmount = "abc",
        totalSpentTodayFormatted = "₹0.00",
        titleError = "Title cannot be empty",
        amountError = "Invalid amount format"
    )
    val onEvent: (ExpenseScreenEvent) -> Unit = remember { {} }

    DailyExpenseTheme {
        ExpenseEntryLayout(
            state = errorState, onEvent = onEvent
        )
    }
}

@Preview(showBackground = true, name = "Expense Entry Success Indicator")
@Composable
fun ExpenseEntrySuccessPreview() {
    val successState = ExpenseScreenState(
        totalSpentTodayFormatted = "₹500.00", submissionStatus = SubmissionStatus.Success
    )
    val onEvent: (ExpenseScreenEvent) -> Unit = remember { {} }

    DailyExpenseTheme {
        ExpenseEntryLayout(
            state = successState, onEvent = onEvent
        )
    }
}

