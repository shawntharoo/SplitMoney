package com.cherrye.splitmoney.android.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cherrye.splitmoney.android.base.BaseFragmentWithBindings
import com.cherrye.splitmoney.android.databinding.FragmentHomeScreenBinding
import com.cherrye.splitmoney.models.Expense
import com.cherrye.splitmoney.models.Group
import com.cherrye.splitmoney.models.User
import com.cherrye.splitmoney.viewmodels.HomeScreenViewModel
import dev.icerock.moko.mvvm.createViewModelFactory
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class HomeScreenFragment : BaseFragmentWithBindings<FragmentHomeScreenBinding, HomeScreenViewModel>() {
    override val viewModelClass: Class<HomeScreenViewModel>
        get() = HomeScreenViewModel::class.java

    private enum class Tab {
        GROUPS,
        USERS
    }

    private enum class GroupsPage {
        LIST,
        EXPENSES,
        EXPENSE_DETAILS
    }

    private var currentTab = Tab.GROUPS
    private var groupsPage = GroupsPage.LIST

    private var groups: List<Group> = emptyList()
    private var users: List<User> = emptyList()
    private var expenses: List<Expense> = emptyList()

    private var selectedGroup: Group? = null
    private var selectedExpense: Expense? = null
    private var expenseDetails: HomeScreenViewModel.ExpenseDetails? = null

    override fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeScreenBinding {
        return FragmentHomeScreenBinding.inflate(inflater, container, false)
    }

    override fun viewModelFactory(): ViewModelProvider.Factory {
        return createViewModelFactory { inject<HomeScreenViewModel>().value }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.groupsTabButton.setOnClickListener {
            switchToGroupsTab()
        }

        binding.usersTabButton.setOnClickListener {
            switchToUsersTab()
        }

        binding.createGroupButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch { createGroup() }
        }

        binding.backToGroupsButton.setOnClickListener {
            groupsPage = GroupsPage.LIST
            selectedGroup = null
            selectedExpense = null
            expenseDetails = null
            renderCurrentPage()
        }

        binding.backToExpensesButton.setOnClickListener {
            groupsPage = GroupsPage.EXPENSES
            selectedExpense = null
            expenseDetails = null
            renderCurrentPage()
        }

        binding.addExpenseButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch { addExpense() }
        }

        binding.addParticipantButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch { addParticipant() }
        }

        binding.addUserButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch { addUser() }
        }

        renderCurrentPage()
        viewLifecycleOwner.lifecycleScope.launch { loadGroups() }
    }

    private fun switchToGroupsTab() {
        currentTab = Tab.GROUPS
        renderCurrentPage()
    }

    private fun switchToUsersTab() {
        currentTab = Tab.USERS
        renderCurrentPage()
        viewLifecycleOwner.lifecycleScope.launch { loadUsers() }
    }

    private fun renderCurrentPage() {
        val showGroups = currentTab == Tab.GROUPS
        binding.groupsRoot.visibility = if (showGroups) View.VISIBLE else View.GONE
        binding.usersRoot.visibility = if (showGroups) View.GONE else View.VISIBLE

        binding.groupsListPage.visibility = if (showGroups && groupsPage == GroupsPage.LIST) View.VISIBLE else View.GONE
        binding.groupExpensesPage.visibility = if (showGroups && groupsPage == GroupsPage.EXPENSES) View.VISIBLE else View.GONE
        binding.expenseDetailsPage.visibility = if (showGroups && groupsPage == GroupsPage.EXPENSE_DETAILS) View.VISIBLE else View.GONE

        binding.groupsTabButton.isEnabled = !showGroups
        binding.usersTabButton.isEnabled = showGroups

        if (showGroups && groupsPage == GroupsPage.EXPENSES) {
            binding.selectedGroupTitle.text = selectedGroup?.let { "Group: ${it.name}" } ?: "Group"
        }
    }

    private suspend fun loadGroups() {
        runSafely {
            groups = viewModel.loadAllGroups()
            renderGroups()
        }
    }

    private suspend fun loadUsers() {
        runSafely {
            users = viewModel.loadAllUsers()
            renderUsers()
        }
    }

    private suspend fun loadExpensesForSelectedGroup() {
        val group = selectedGroup ?: return
        runSafely {
            expenses = viewModel.loadExpensesForGroup(group.id)
            renderExpenses()
        }
    }

    private suspend fun loadExpenseDetails() {
        val expense = selectedExpense ?: return
        runSafely {
            expenseDetails = viewModel.loadExpenseDetails(expense.id)
            renderExpenseDetails()
        }
    }

    private suspend fun createGroup() {
        val groupName = binding.groupNameInput.text?.toString()?.trim().orEmpty()
        val creator = binding.groupCreatorInput.text?.toString()?.trim().orEmpty()
        if (groupName.isBlank()) {
            showToast("Enter a group name")
            return
        }
        if (creator.isBlank()) {
            showToast("Enter creator username")
            return
        }

        runSafely {
            viewModel.createGroup(groupName, creator)
            binding.groupNameInput.setText("")
            loadGroups()
            showToast("Group created")
        }
    }

    private suspend fun deleteGroup(group: Group) {
        runSafely {
            viewModel.deleteGroup(group.id)
            if (selectedGroup?.id == group.id) {
                selectedGroup = null
                selectedExpense = null
                expenseDetails = null
                groupsPage = GroupsPage.LIST
                renderCurrentPage()
            }
            loadGroups()
            showToast("Group deleted")
        }
    }

    private suspend fun addExpense() {
        val group = selectedGroup ?: return

        val title = binding.expenseTitleInput.text?.toString()?.trim().orEmpty()
        val amountRaw = binding.expenseAmountInput.text?.toString()?.trim().orEmpty()
        val payer = binding.expensePayerInput.text?.toString()?.trim().orEmpty()
        val participants = binding.expenseParticipantsInput.text?.toString().orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (title.isBlank()) {
            showToast("Enter expense title")
            return
        }
        if (payer.isBlank()) {
            showToast("Enter payer username")
            return
        }

        val amount = amountRaw.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            showToast("Enter a valid amount")
            return
        }

        runSafely {
            viewModel.addExpense(
                groupId = group.id,
                title = title,
                amount = amount,
                payerUsername = payer,
                participantUsernames = participants
            )
            binding.expenseTitleInput.setText("")
            binding.expenseAmountInput.setText("")
            binding.expensePayerInput.setText("")
            binding.expenseParticipantsInput.setText("")
            loadExpensesForSelectedGroup()
            showToast("Expense added")
        }
    }

    private suspend fun deleteExpense(expense: Expense) {
        runSafely {
            viewModel.deleteExpense(expense.id)
            if (selectedExpense?.id == expense.id) {
                selectedExpense = null
                expenseDetails = null
                groupsPage = GroupsPage.EXPENSES
                renderCurrentPage()
            }
            loadExpensesForSelectedGroup()
            showToast("Expense deleted")
        }
    }

    private suspend fun addParticipant() {
        val expense = selectedExpense ?: return
        val username = binding.addParticipantInput.text?.toString()?.trim().orEmpty()
        if (username.isBlank()) {
            showToast("Enter participant username")
            return
        }

        runSafely {
            viewModel.addParticipantToExpense(expense.id, username)
            binding.addParticipantInput.setText("")
            loadExpenseDetails()
            showToast("Participant added")
        }
    }

    private suspend fun removeParticipant(username: String) {
        val expense = selectedExpense ?: return
        runSafely {
            viewModel.removeParticipantFromExpense(expense.id, username)
            loadExpenseDetails()
            showToast("Participant removed")
        }
    }

    private suspend fun addUser() {
        val username = binding.userNameInput.text?.toString()?.trim().orEmpty()
        if (username.isBlank()) {
            showToast("Enter username")
            return
        }

        runSafely {
            viewModel.createUser(username)
            binding.userNameInput.setText("")
            loadUsers()
            showToast("User added")
        }
    }

    private suspend fun deleteUser(user: User) {
        runSafely {
            val deleted = viewModel.deleteUserByUsername(user.username)
            if (deleted) {
                loadUsers()
                showToast("User deleted")
            } else {
                showToast("User cannot be deleted. They are involved in expenses or own a group.")
            }
        }
    }

    private fun renderGroups() {
        binding.groupsContainer.removeAllViews()
        if (groups.isEmpty()) {
            val text = TextView(requireContext()).apply {
                this.text = "No groups yet"
            }
            binding.groupsContainer.addView(text)
            return
        }

        groups.forEach { group ->
            val row = createRow()

            val openButton = Button(requireContext()).apply {
                text = group.name
                setOnClickListener {
                    selectedGroup = group
                    selectedExpense = null
                    expenseDetails = null
                    groupsPage = GroupsPage.EXPENSES
                    renderCurrentPage()
                    viewLifecycleOwner.lifecycleScope.launch { loadExpensesForSelectedGroup() }
                }
            }

            val deleteButton = Button(requireContext()).apply {
                text = "Delete"
                setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch { deleteGroup(group) }
                }
            }

            row.addView(openButton)
            row.addView(deleteButton)
            binding.groupsContainer.addView(row)
        }
    }

    private fun renderExpenses() {
        binding.expensesContainer.removeAllViews()
        if (expenses.isEmpty()) {
            val text = TextView(requireContext()).apply {
                this.text = "No expenses in this group"
            }
            binding.expensesContainer.addView(text)
            return
        }

        expenses.forEach { expense ->
            val row = createRow()

            val openButton = Button(requireContext()).apply {
                text = "${expense.title} - ${formatMoney(expense.amount)}"
                setOnClickListener {
                    selectedExpense = expense
                    groupsPage = GroupsPage.EXPENSE_DETAILS
                    renderCurrentPage()
                    viewLifecycleOwner.lifecycleScope.launch { loadExpenseDetails() }
                }
            }

            val deleteButton = Button(requireContext()).apply {
                text = "Delete"
                setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch { deleteExpense(expense) }
                }
            }

            row.addView(openButton)
            row.addView(deleteButton)
            binding.expensesContainer.addView(row)
        }
    }

    private fun renderExpenseDetails() {
        val details = expenseDetails ?: return
        binding.expenseInfoText.text = buildString {
            append(details.expense.title)
            append("\nAmount: ")
            append(formatMoney(details.expense.amount))
            append("\nPayer: ")
            append(details.expense.payer.username)
        }

        binding.participantsContainer.removeAllViews()
        details.balances.forEach { balance ->
            val row = createRow()
            val info = TextView(requireContext()).apply {
                text = "${balance.user.username} - owes ${formatMoney(balance.owes)} - gets ${formatMoney(balance.gets)}"
            }
            val removeButton = Button(requireContext()).apply {
                text = "Remove"
                setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch { removeParticipant(balance.user.username) }
                }
            }
            row.addView(info)
            row.addView(removeButton)
            binding.participantsContainer.addView(row)
        }

        val settlementsText = if (details.settlements.isEmpty()) {
            "Settlement:\nEveryone is settled."
        } else {
            "Settlement:\n" + details.settlements.joinToString("\n") {
                "${it.from.username} pays ${it.to.username} ${formatMoney(it.amount)}"
            }
        }
        binding.expenseSettlementsText.text = settlementsText
    }

    private fun renderUsers() {
        binding.usersContainer.removeAllViews()
        if (users.isEmpty()) {
            val text = TextView(requireContext()).apply {
                this.text = "No users yet"
            }
            binding.usersContainer.addView(text)
            return
        }

        users.forEach { user ->
            val row = createRow()

            val label = TextView(requireContext()).apply {
                text = user.username
            }

            val deleteButton = Button(requireContext()).apply {
                text = "Delete"
                setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch { deleteUser(user) }
                }
            }

            row.addView(label)
            row.addView(deleteButton)
            binding.usersContainer.addView(row)
        }
    }

    private fun createRow(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
            }
        }
    }

    private suspend fun runSafely(block: suspend () -> Unit) {
        try {
            block()
        } catch (error: Throwable) {
            showToast(error.message ?: "Something went wrong")
        }
    }

    private fun formatMoney(amount: Double): String {
        return "$" + String.format("%.2f", amount)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
