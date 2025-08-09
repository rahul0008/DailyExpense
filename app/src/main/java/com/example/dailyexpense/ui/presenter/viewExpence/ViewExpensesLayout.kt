package com.example.dailyexpense.ui.presenter.viewExpence

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.* // For various icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyexpense.R // Your R file
import com.example.dailyexpense.ui.theme.DailyExpenseTheme
import com.example.dailyexpense.ui.uiComponents.AppText
import com.example.dailyexpense.util.essentialEnums.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewExpensesLayout(
    state: ViewExpensesScreenState,
    onEvent: (ViewExpensesScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current // For date pickers, etc.

    // State for DatePickerDialog
    val datePickerState = rememberDatePickerState()
    if (state.showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { onEvent(ViewExpensesScreenEvent.DismissDatePickerDialog) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onEvent(ViewExpensesScreenEvent.CustomDateSelected(it))
                    }
                    onEvent(ViewExpensesScreenEvent.DismissDatePickerDialog)
                }) { AppText("OK") }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ViewExpensesScreenEvent.DismissDatePickerDialog) }) { AppText("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    // You would add a similar DateRangePickerDialog if using DateFilterType.DATE_RANGE

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { AppText("View Expenses", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Grouping Toggle
                    IconButton(onClick = {
                        val nextGrouping = when (state.currentGrouping) {
                            GroupingOption.NONE -> GroupingOption.BY_CATEGORY
                            GroupingOption.BY_CATEGORY -> GroupingOption.BY_Time
                            GroupingOption.BY_Time -> GroupingOption.NONE
                        }
                        onEvent(ViewExpensesScreenEvent.GroupingOptionChanged(nextGrouping))
                    }) {
                        Icon(
                            imageVector = when (state.currentGrouping) {
                                GroupingOption.NONE -> Icons.AutoMirrored.Filled.ViewList
                                GroupingOption.BY_CATEGORY -> Icons.Filled.Category
                                GroupingOption.BY_Time -> Icons.Filled.CalendarViewDay
                            },
                            contentDescription = "Toggle Grouping"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            DateFilterChips(
                selectedFilter = state.selectedDateFilter,
                onFilterSelected = { filterType ->
                    if (filterType == DateFilterType.CUSTOM_DATE) {
                        onEvent(ViewExpensesScreenEvent.OpenDatePickerDialog)
                    } else {
                        onEvent(ViewExpensesScreenEvent.DateFilterChanged(filterType))
                    }
                },
                customDateMillis = state.customSelectedDateMillis
            )

            SummaryHeader(
                count = state.totalExpensesCount,
                totalAmount = state.totalExpensesAmountFormatted
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.showEmptyState) {
                EmptyStateView()
            } else {
                if (state.currentGrouping == GroupingOption.NONE) {
                    ExpensesList(expenses = state.expenses, onEvent = onEvent)
                } else {
                    GroupedExpensesList(groupedExpenses = state.groupedExpenses, onEvent = onEvent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterChips(
    selectedFilter: DateFilterType,
    onFilterSelected: (DateFilterType) -> Unit,
    customDateMillis: Long?
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DateFilterType.values().filterNot { it == DateFilterType.DATE_RANGE }.forEach { filter -> // Exclude DATE_RANGE for now
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    val labelText = when (filter) {
                        DateFilterType.TODAY -> "Today"
                        DateFilterType.YESTERDAY -> "Yesterday"
                        DateFilterType.THIS_WEEK -> "This Week"
                        DateFilterType.THIS_MONTH -> "This Month"
                        DateFilterType.CUSTOM_DATE -> {
                            if (selectedFilter == DateFilterType.CUSTOM_DATE && customDateMillis != null) {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(customDateMillis))
                            } else {
                                "Custom Date"
                            }
                        }
                        DateFilterType.DATE_RANGE -> "Date Range" // Not implemented in this UI example
                    }
                    AppText(labelText)
                },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Filled.Done, contentDescription = "Selected") }
                } else null
            )
        }
    }
}

@Composable
fun SummaryHeader(count: Int, totalAmount: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppText("Total Expenses", style = MaterialTheme.typography.labelMedium)
                AppText("$count", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            VerticalDivider(modifier = Modifier.height(40.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppText("Total Amount", style = MaterialTheme.typography.labelMedium)
                AppText(totalAmount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun ExpensesList(
    expenses: List<ExpenseListItem>,
    onEvent: (ViewExpensesScreenEvent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(expenses, key = { it.id }) { expense ->
            ExpenseRow(expenseItem = expense, onClick = {
                onEvent(ViewExpensesScreenEvent.ExpenseClicked(expense.id))
            })
        }
    }
}

@Composable
fun GroupedExpensesList(
    groupedExpenses: List<GroupedExpenses>,
    onEvent: (ViewExpensesScreenEvent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedExpenses.forEach { group ->
            stickyHeader { // Requires an OptIn if not already present
                GroupHeader(title = group.groupTitle, totalAmount = group.totalAmountFormattedInGroup)
            }
            items(group.expenses, key = { it.id }) { expense ->
                ExpenseRow(expenseItem = expense, onClick = {
                    onEvent(ViewExpensesScreenEvent.ExpenseClicked(expense.id))
                })
                // Add a small divider within group if desired, except for last item
                if (group.expenses.last() != expense) {
                    // Divider(modifier = Modifier.padding(start = 16.dp, end = 16.dp))
                }
            }
        }
    }
}

@Composable
fun GroupHeader(title: String, totalAmount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppText(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AppText(totalAmount, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseRow(expenseItem: ExpenseListItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Optional: Icon for category or image
            CategoryIcon(category = expenseItem.category, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                AppText(expenseItem.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                AppText(expenseItem.dateLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AppText(expenseItem.amountFormatted, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoryIcon(category: ExpenseCategory, modifier: Modifier = Modifier) {
    // Replace with actual icons based on your drawable resources
    val iconRes = when (category) {
        ExpenseCategory.STAFF -> R.drawable.ic_wallet
        ExpenseCategory.TRAVEL -> R.drawable.ic_wallet
        ExpenseCategory.FOOD -> R.drawable.ic_wallet
        ExpenseCategory.UTILITY -> R.drawable.ic_wallet
        ExpenseCategory.HOUSING -> R.drawable.ic_wallet
        ExpenseCategory.TRANSPORTATION -> R.drawable.ic_wallet
        ExpenseCategory.HEALTHCARE -> R.drawable.ic_wallet
        ExpenseCategory.PERSONAL_CARE -> R.drawable.ic_wallet
        ExpenseCategory.EDUCATION -> R.drawable.ic_wallet
        ExpenseCategory.ENTERTAINMENT -> R.drawable.ic_wallet
        ExpenseCategory.SHOPPING -> R.drawable.ic_wallet
        ExpenseCategory.INSURANCE -> R.drawable.ic_wallet
        ExpenseCategory.DEBT_PAYMENT -> R.drawable.ic_wallet
        ExpenseCategory.SAVINGS_INVESTMENTS -> R.drawable.ic_wallet
        ExpenseCategory.CHILDCARE -> R.drawable.ic_wallet
        ExpenseCategory.PETS -> R.drawable.ic_wallet
        ExpenseCategory.TAXES -> R.drawable.ic_wallet
        ExpenseCategory.GIFTS_DONATIONS -> R.drawable.ic_wallet
        ExpenseCategory.SUBSCRIPTIONS -> R.drawable.ic_wallet
        ExpenseCategory.MISCELLANEOUS -> R.drawable.ic_wallet
        ExpenseCategory.OTHER -> R.drawable.ic_reciept
    }
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = category.displayName,
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape).padding(8.dp), // Example styling
        tint = MaterialTheme.colorScheme.onSecondaryContainer
    )
}


@Composable
fun EmptyStateView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_empty_box), // Create this drawable
                contentDescription = "No Expenses Found",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppText(
                "No Expenses Found",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            AppText(
                "Try adjusting the filters or add some expenses.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- Preview ---
@Preview(showBackground = true, name = "View Expenses Light")
@Preview(showBackground = true, name = "View Expenses Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ViewExpensesLayoutPreview() {
    val sampleExpenses = listOf(
        ExpenseListItem(1, "Lunch", "₹150.00", ExpenseCategory.FOOD, "12:30 PM", System.currentTimeMillis()),
        ExpenseListItem(2, "Coffee", "₹80.00", ExpenseCategory.FOOD, "09:00 AM", System.currentTimeMillis()),
        ExpenseListItem(3, "Movie Tickets", "₹500.00", ExpenseCategory.ENTERTAINMENT, "Yesterday", System.currentTimeMillis() - 86400000)
    )
    val sampleGrouped = listOf(
        GroupedExpenses("FOOD", sampleExpenses.filter { it.category == ExpenseCategory.FOOD }, "₹230.00"),
        GroupedExpenses("ENTERTAINMENT", sampleExpenses.filter { it.category == ExpenseCategory.ENTERTAINMENT }, "₹500.00")
    )

    val state = ViewExpensesScreenState(
        isLoading = false,
        expenses = sampleExpenses,
        groupedExpenses = sampleGrouped,
        currentGrouping = GroupingOption.NONE, // Change to BY_CATEGORY to test
        selectedDateFilter = DateFilterType.TODAY,
        totalExpensesCount = 3,
        totalExpensesAmountFormatted = "₹730.00"
    )
    DailyExpenseTheme {
        Surface {
            ViewExpensesLayout(state = state, onEvent = {})
        }
    }
}

@Preview(showBackground = true, name = "View Expenses Empty State")
@Composable
fun ViewExpensesLayoutEmptyPreview() {
    val state = ViewExpensesScreenState(
        isLoading = false,
        expenses = emptyList(),
        groupedExpenses = emptyList(),
        totalExpensesCount = 0,
        totalExpensesAmountFormatted = "₹0.00"
    )
    DailyExpenseTheme {
        Surface {
            ViewExpensesLayout(state = state, onEvent = {})
        }
    }
}

@Preview(showBackground = true, name = "View Expenses Loading State")
@Composable
fun ViewExpensesLayoutLoadingPreview() {
    val state = ViewExpensesScreenState(isLoading = true)
    DailyExpenseTheme {
        Surface {
            ViewExpensesLayout(state = state, onEvent = {})
        }
    }
}

// Make sure you have placeholder drawables like:
// R.drawable.ic_food, ic_bills, etc.
// R.drawable.ic_empty_box
