package com.example.dailyexpense.repo.dbRepo // Or your preferred package

import com.example.dailyexpense.db.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    suspend fun insertExpense(expense: ExpenseEntity)

    suspend fun updateExpense(expense: ExpenseEntity)

    suspend fun deleteExpense(expense: ExpenseEntity)

    fun getExpenseById(id: Long): Flow<ExpenseEntity?>

    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    fun getExpensesForDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> // For "Total Spent Today"

    // You could add more complex business logic methods here if needed,
    // for example, calculating total expenses for a month, etc.
    // fun getTotalExpensesForMonth(year: Int, month: Int): Flow<Double>
}
