package com.cherrye.splitmoney.repository.interfaces

import com.cherrye.splitmoney.models.User

interface ExpenseParticipantsRepository {
    suspend fun getParticipantsForExpense(expenseId: String): List<User>
    suspend fun addParticipant(expenseId: String, user: User)
    suspend fun deleteParticipant(expenseId: String, userId: Long)
    suspend fun deleteParticipantsForExpense(expenseId: String)
}
