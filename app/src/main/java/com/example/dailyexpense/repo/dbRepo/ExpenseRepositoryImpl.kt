package com.example.dailyexpense.repo.dbRepo // Or your preferred package

import com.example.dailyexpense.db.dao.ExpenseDao
import com.example.dailyexpense.db.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao // Depends on the DAO interface
) : ExpenseRepository {

    override suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    override suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    override suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    override fun getExpenseById(id: Long): Flow<ExpenseEntity?> {
        return expenseDao.getExpenseById(id)
    }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }

    // Example of implementing a more complex method if you had one in the interface:
    // override fun getTotalExpensesForMonth(year: Int, month: Int): Flow<Double> {
    //     // You might have a specific DAO method for this or calculate it here
    //     // from the results of getAllExpenses() or a more targeted query.
    //     return expenseDao.getExpensesForMonth(year, month).map { expenses ->
    //         expenses.sumOf { it.amount }
    //     }
    // }
}
