package com.cherrye.splitmoney.repository.impl

import com.cherrye.splitmoney.models.User
import com.cherrye.splitmoney.repository.interfaces.ExpenseParticipantsRepository
import com.cherrye.splitmoney.splitMoney

class SqlDelightExpenseParticipantsRepository(private val db: splitMoney) :
    ExpenseParticipantsRepository {
    private val expenseParticipantsQueries = db.expenseParticipantsQueries
    override suspend fun getParticipantsForExpense(expenseId: String): List<User> {
        return expenseParticipantsQueries.selectParticipantsForExpense(expenseId).executeAsList().map {
            User(it.id, it.username)
        }
    }

    override suspend fun addParticipant(expenseId: String, user: User) {
        expenseParticipantsQueries.insertParticipants(expenseId, user.id)
    }

    override suspend fun deleteParticipant(expenseId: String, userId: Long) {
        expenseParticipantsQueries.deleteParticipant(expenseId, userId)
    }

    override suspend fun deleteParticipantsForExpense(expenseId: String) {
        expenseParticipantsQueries.deleteParticipantsForExpense(expenseId)
    }
}
