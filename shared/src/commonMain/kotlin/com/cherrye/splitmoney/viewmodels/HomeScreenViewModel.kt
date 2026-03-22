package com.cherrye.splitmoney.viewmodels

import com.cherrye.splitmoney.models.Expense
import com.cherrye.splitmoney.models.Group
import com.cherrye.splitmoney.models.User
import com.cherrye.splitmoney.repository.interfaces.ExpenseParticipantsRepository
import com.cherrye.splitmoney.repository.interfaces.ExpenseRepository
import com.cherrye.splitmoney.repository.interfaces.GroupMembersRepository
import com.cherrye.splitmoney.repository.interfaces.GroupRepository
import com.cherrye.splitmoney.repository.interfaces.UserRepository
import kotlin.Exception
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.round
import org.koin.core.annotation.Factory

@Factory
class HomeScreenViewModel(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val expenseRepository: ExpenseRepository,
    private val groupMembersRepository: GroupMembersRepository,
    private val expenseParticipantsRepository: ExpenseParticipantsRepository
) : BaseViewModel() {

    data class GroupDetails(
        val group: Group,
        val expenses: List<Expense>,
        val members: List<User>,
        val balances: List<UserNetBalance>,
        val settlements: List<Settlement>
    )

    data class UserNetBalance(
        val user: User,
        val netAmount: Double
    )

    data class Settlement(
        val from: User,
        val to: User,
        val amount: Double
    )

    data class ExpenseParticipantBalance(
        val user: User,
        val owes: Double,
        val gets: Double
    )

    data class ExpenseDetails(
        val expense: Expense,
        val balances: List<ExpenseParticipantBalance>,
        val settlements: List<Settlement>
    )

    @Throws(Exception::class)
    suspend fun loadAllGroups(): List<Group> {
        return groupRepository.getAllGroups().sortedBy { it.name.lowercase() }
    }

    @Throws(Exception::class)
    suspend fun loadGroupsForUsername(username: String): List<Group> {
        val currentUser = findOrCreateUser(username)
        return groupRepository.getAllGroupsForUser(currentUser.id).sortedBy { it.name.lowercase() }
    }

    @Throws(Exception::class)
    suspend fun createGroup(name: String, creatorUsername: String): Group {
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) { "Group name cannot be empty" }

        val creator = findOrCreateUser(creatorUsername)
        val groupId = groupRepository.createGroup(normalizedName, creator.id)
        return groupRepository.getGroupById(groupId)
            ?: Group(id = groupId, name = normalizedName, creator = creator, members = listOf(creator))
    }

    @Throws(Exception::class)
    suspend fun deleteGroup(groupId: Long) {
        groupRepository.deleteGroup(groupId)
    }

    @Throws(Exception::class)
    suspend fun addMemberByUsername(groupId: Long, username: String): Group {
        val user = findOrCreateUser(username)
        val existingMemberIds = groupMembersRepository.getMembers(groupId).map { it.id }.toSet()
        if (!existingMemberIds.contains(user.id)) {
            groupMembersRepository.addMember(groupId, user.id)
        }

        return groupRepository.getGroupById(groupId)
            ?: error("Group not found")
    }

    @Throws(Exception::class)
    suspend fun addExpense(
        groupId: Long,
        title: String,
        amount: Double,
        payerUsername: String,
        participantUsernames: List<String>
    ): Expense {
        val normalizedTitle = title.trim()
        require(normalizedTitle.isNotBlank()) { "Expense title cannot be empty" }
        require(amount > 0.0) { "Expense amount must be greater than 0" }

        val payer = findOrCreateUser(payerUsername)
        ensureMember(groupId, payer.id)

        val participants = if (participantUsernames.isEmpty()) {
            getAllMembersWithCreator(groupId)
        } else {
            participantUsernames
                .map { findOrCreateUser(it) }
                .onEach { ensureMember(groupId, it.id) }
                .distinctBy { it.id }
        }

        val finalParticipants = if (participants.isEmpty()) listOf(payer) else participants

        return expenseRepository.addExpense(
            groupId = groupId.toString(),
            title = normalizedTitle,
            amount = amount,
            payer = payer,
            participants = finalParticipants
        )
    }

    @Throws(Exception::class)
    suspend fun loadExpensesForGroup(groupId: Long): List<Expense> {
        return expenseRepository.getExpensesForGroup(groupId.toString())
    }

    @Throws(Exception::class)
    suspend fun deleteExpense(expenseId: String) {
        expenseRepository.deleteExpense(expenseId)
    }

    @Throws(Exception::class)
    suspend fun loadExpenseDetails(expenseId: String): ExpenseDetails {
        val expense = expenseRepository.getExpenseById(expenseId) ?: error("Expense not found")
        val balances = calculateExpenseBalances(expense)
        val settlements = balances.filter { it.owes > 0.009 }.map {
            Settlement(from = it.user, to = expense.payer, amount = roundMoney(it.owes))
        }
        return ExpenseDetails(expense = expense, balances = balances, settlements = settlements)
    }

    @Throws(Exception::class)
    suspend fun addParticipantToExpense(expenseId: String, username: String): Expense {
        val expense = expenseRepository.getExpenseById(expenseId) ?: error("Expense not found")
        val user = findOrCreateUser(username)
        ensureGroupMember(expense.groupId, user.id)
        expenseParticipantsRepository.addParticipant(expenseId, user)
        return expenseRepository.getExpenseById(expenseId) ?: expense
    }

    @Throws(Exception::class)
    suspend fun removeParticipantFromExpense(expenseId: String, username: String): Expense {
        val expense = expenseRepository.getExpenseById(expenseId) ?: error("Expense not found")
        val user = userRepository.getUserByUsername(username.trim()) ?: error("User not found")
        require(user.id != expense.payer.id) { "Payer cannot be removed from expense participants" }
        require(expense.participants.size > 1) { "Expense needs at least one participant" }
        expenseParticipantsRepository.deleteParticipant(expenseId, user.id)
        return expenseRepository.getExpenseById(expenseId) ?: expense
    }

    @Throws(Exception::class)
    suspend fun loadAllUsers(): List<User> {
        return userRepository.getAllUsers().sortedBy { it.username.lowercase() }
    }

    @Throws(Exception::class)
    suspend fun createUser(username: String): User {
        return findOrCreateUser(username)
    }

    @Throws(Exception::class)
    suspend fun deleteUserByUsername(username: String): Boolean {
        val user = userRepository.getUserByUsername(username.trim()) ?: return false
        return userRepository.deleteUserIfAllowed(user.id)
    }

    @Throws(Exception::class)
    suspend fun loadGroupDetails(groupId: Long): GroupDetails {
        val group = groupRepository.getGroupById(groupId) ?: error("Group not found")
        val expenses = expenseRepository.getExpensesForGroup(groupId.toString())
        val members = getAllMembersWithCreator(groupId)
        val balances = calculateBalances(members, expenses)
        val settlements = calculateSettlements(balances)

        return GroupDetails(
            group = group,
            expenses = expenses,
            members = members,
            balances = balances,
            settlements = settlements
        )
    }

    private suspend fun findOrCreateUser(username: String): User {
        val normalizedUsername = username.trim()
        require(normalizedUsername.isNotBlank()) { "Username cannot be empty" }

        val existing = userRepository.getUserByUsername(normalizedUsername)
        if (existing != null) return existing

        userRepository.createUser(User(id = 0L, username = normalizedUsername))
        return userRepository.getUserByUsername(normalizedUsername)
            ?: error("User was created but could not be loaded")
    }

    private suspend fun ensureMember(groupId: Long, userId: Long) {
        val memberIds = groupMembersRepository.getMembers(groupId).map { it.id }.toSet()
        if (!memberIds.contains(userId)) {
            groupMembersRepository.addMember(groupId, userId)
        }
    }

    private suspend fun ensureGroupMember(groupId: String, userId: Long) {
        val groupIdLong = groupId.toLongOrNull() ?: return
        ensureMember(groupIdLong, userId)
    }

    private suspend fun getAllMembersWithCreator(groupId: Long): List<User> {
        val group = groupRepository.getGroupById(groupId) ?: error("Group not found")
        return (listOf(group.creator) + groupMembersRepository.getMembers(groupId)).distinctBy { it.id }
    }

    private fun calculateExpenseBalances(expense: Expense): List<ExpenseParticipantBalance> {
        val participants = if (expense.participants.isEmpty()) {
            listOf(expense.payer)
        } else {
            expense.participants.distinctBy { it.id }
        }

        val count = participants.size.coerceAtLeast(1)
        val share = expense.amount / count
        val payerInParticipants = participants.any { it.id == expense.payer.id }

        val list = participants.map { user ->
            if (user.id == expense.payer.id) {
                val receivable = if (payerInParticipants) expense.amount - share else expense.amount
                ExpenseParticipantBalance(user = user, owes = 0.0, gets = roundMoney(receivable))
            } else {
                ExpenseParticipantBalance(user = user, owes = roundMoney(share), gets = 0.0)
            }
        }.toMutableList()

        if (!payerInParticipants) {
            list += ExpenseParticipantBalance(user = expense.payer, owes = 0.0, gets = roundMoney(expense.amount))
        }

        return list.sortedBy { it.user.username.lowercase() }
    }

    private fun calculateBalances(members: List<User>, expenses: List<Expense>): List<UserNetBalance> {
        val userById = members.associateBy { it.id }
        val netAmounts = members.associate { it.id to 0.0 }.toMutableMap()

        expenses.forEach { expense ->
            val participants = if (expense.participants.isEmpty()) {
                listOf(expense.payer)
            } else {
                expense.participants.distinctBy { it.id }
            }

            if (participants.isEmpty()) return@forEach

            val share = expense.amount / participants.size
            participants.forEach { participant ->
                val current = netAmounts[participant.id] ?: 0.0
                netAmounts[participant.id] = current - share
            }

            val payerCurrent = netAmounts[expense.payer.id] ?: 0.0
            netAmounts[expense.payer.id] = payerCurrent + expense.amount
        }

        return netAmounts.mapNotNull { (userId, amount) ->
            userById[userId]?.let {
                UserNetBalance(
                    user = it,
                    netAmount = roundMoney(amount)
                )
            }
        }.sortedBy { it.user.username.lowercase() }
    }

    private fun calculateSettlements(balances: List<UserNetBalance>): List<Settlement> {
        val debtors = balances
            .filter { it.netAmount < -0.009 }
            .map { MutableBalance(it.user, abs(it.netAmount)) }
            .toMutableList()

        val creditors = balances
            .filter { it.netAmount > 0.009 }
            .map { MutableBalance(it.user, it.netAmount) }
            .toMutableList()

        val settlements = mutableListOf<Settlement>()
        var debtorIndex = 0
        var creditorIndex = 0

        while (debtorIndex < debtors.size && creditorIndex < creditors.size) {
            val debtor = debtors[debtorIndex]
            val creditor = creditors[creditorIndex]
            val payment = roundMoney(min(debtor.amount, creditor.amount))

            if (payment > 0.0) {
                settlements += Settlement(
                    from = debtor.user,
                    to = creditor.user,
                    amount = payment
                )
            }

            debtor.amount = roundMoney(debtor.amount - payment)
            creditor.amount = roundMoney(creditor.amount - payment)

            if (debtor.amount <= 0.009) debtorIndex++
            if (creditor.amount <= 0.009) creditorIndex++
        }

        return settlements
    }

    private fun roundMoney(value: Double): Double {
        return round(value * 100.0) / 100.0
    }

    private data class MutableBalance(
        val user: User,
        var amount: Double
    )
}
