package com.cherrye.splitmoney.repository.interfaces

import com.cherrye.splitmoney.models.Expense
import com.cherrye.splitmoney.models.User

interface ExpenseRepository {
    suspend fun insertExpense(groupId: String, title: String, amount: Double,
                           payerId: Int, date: String): Expense
    suspend fun addExpense(groupId: String, title: String, amount: Double, payer: User, participants: List<User>): Expense
    suspend fun getExpensesForGroup(groupId: String): List<Expense>
    suspend fun updateExpenseTitle(expenseId: String, newTitle: String)
    suspend fun deleteExpense(expenseId: String)
    suspend fun getExpenseById(expenseId: String): Expense?
}