package com.example.bottomNavigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.dailyexpense.ui.presenter.expenseEntry.ExpenseEntryScreen
import com.example.dailyexpense.ui.presenter.expenseReport.ExpenseReportScreen
import com.example.dailyexpense.ui.presenter.viewExpence.ViewExpensesScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Add this if Scaffold padding isn't used directly
@Composable
fun MainAppScreen(modifier: Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier =modifier,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // re-selecting the same item
                                launchSingleTop = true
                                // Restore state when re-selecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.ExpenseEntry.route, // Your desired start screen
            modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
        ) {
            composable(Screen.ExpenseEntry.route) { ExpenseEntryScreen() }
            composable(Screen.ViewExpenses.route) { ViewExpensesScreen() }
            composable(Screen.ExpenseReport.route) { ExpenseReportScreen() }
        }
    }
}
