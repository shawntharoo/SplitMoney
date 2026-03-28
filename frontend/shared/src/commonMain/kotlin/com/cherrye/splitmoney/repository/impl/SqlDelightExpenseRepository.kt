package com.cherrye.splitmoney.repository.impl

import com.benasher44.uuid.uuid4
import com.cherrye.splitmoney.models.Expense
import com.cherrye.splitmoney.models.User
import com.cherrye.splitmoney.repository.interfaces.ExpenseRepository
import com.cherrye.splitmoney.splitMoney


class SqlDelightExpenseRepository(private val db: splitMoney): ExpenseRepository {
    private val expenseQueries = db.expenseQueries
    private val participantQueries = db.expenseParticipantsQueries
    private val userQueries = db.userQueries

    override suspend fun insertExpense(
        groupId: String,
        title: String,
        amount: Double,
        payerId: Int,
        date: String
    ): Expense {
        val id = uuid4().toString()
        expenseQueries.insertExpense(
            id = id,
            group_id = groupId,
            title = title,
            amount = amount,
            payer_id =  payerId.toLong()
        )

        val payer = userQueries.selectUserById(payerId.toLong())
            .executeAsOne()
            .let { User(it.id, it.username) }

        return Expense(
            id = id,
            groupId = groupId,
            title = title,
            amount = amount,
            payer = payer,
            participants = listOf(payer)
        )
    }

    override suspend fun addExpense(
        groupId: String,
        title: String,
        amount: Double,
        payer: User,
        participants: List<User>
    ): Expense {
        val id = uuid4().toString()

        expenseQueries.insertExpense(
            id = id,
            group_id = groupId,
            title = title,
            amount = amount,
            payer_id = payer.id
        )

        participants.forEach { participants ->
            participantQueries.insertParticipants(
                expense_id = id,
                user_id = participants.id
            )
        }

        return Expense(
            id = id,
            groupId = groupId,
            title = title,
            amount = amount,
            payer = payer,
            participants = participants
        )
    }

    override suspend fun getExpensesForGroup(groupId: String): List<Expense> {
        val expenseRows = expenseQueries.selecteExpensesForGroup(groupId).executeAsList()

        return expenseRows.map { row ->
            val payer = userQueries.selectUserById(row.payer_id)
                .executeAsOne()
                .let { User(it.id, it.username) }

            val participants = participantQueries.selectParticipantsForExpense(row.id)
                .executeAsList()
                .map { User(it.id, it.username) }

            Expense(
                id = row.id,
                groupId = row.group_id,
                title = row.title,
                amount = row.amount,
                payer = payer,
                participants
            )
        }
    }

    override suspend fun updateExpenseTitle(expenseId: String, newTitle: String) {
        expenseQueries.updateExpenseTitle(newTitle, expenseId)
    }

    override suspend fun deleteExpense(expenseId: String) {
        participantQueries.deleteParticipantsForExpense(expenseId)
        expenseQueries.deleteExpense(expenseId)
    }

    override suspend fun deleteExpensesForGroup(groupId: String) {
        val expenseIds = expenseQueries.selectExpenseIdsForGroup(groupId).executeAsList()
        expenseIds.forEach { expenseId ->
            participantQueries.deleteParticipantsForExpense(expenseId)
        }
        expenseQueries.deleteExpensesForGroup(groupId)
    }

    override suspend fun getExpenseById(expenseId: String): Expense? {
        val expenseRow = expenseQueries.selectExpenseById(expenseId).executeAsOneOrNull()

        if(expenseRow != null) {
            val payer = userQueries.selectUserById(expenseRow.payer_id).executeAsOne().let {
                User(it.id, it.username)
            }
            val participants = participantQueries.selectParticipantsForExpense(expenseId).executeAsList()
                .map { User(it.id, it.username) }

            return Expense(
                expenseRow.id,
                expenseRow.group_id,
                expenseRow.title,
                expenseRow.amount,
                payer,
                participants
            )
        }
        return null
    }
}
