package com.cherrye.splitmoney.android.screens

import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
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

        binding.openCreateGroupDialogButton.setOnClickListener {
            showCreateGroupDialog()
        }

        binding.openCreateExpenseDialogButton.setOnClickListener {
            showCreateExpenseDialog()
        }

        binding.openCreateUserDialogButton.setOnClickListener {
            showCreateUserDialog()
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

        binding.addParticipantButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch { addParticipant() }
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

        updateBottomTabStyles(showGroups)

        if (showGroups && groupsPage == GroupsPage.EXPENSES) {
            binding.selectedGroupTitle.text = selectedGroup?.let { "Group: ${it.name}" } ?: "Group"
        }
    }

    private fun updateBottomTabStyles(showGroups: Boolean) {
        val selectedBackground = ColorStateList.valueOf(0xFFD8C5A5.toInt())
        val selectedText = ContextCompat.getColor(requireContext(), android.R.color.black)
        val unselectedBackground = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        val unselectedText = ContextCompat.getColor(requireContext(), android.R.color.white)

        if (showGroups) {
            binding.groupsTabButton.backgroundTintList = selectedBackground
            binding.groupsTabButton.setTextColor(selectedText)
            binding.usersTabButton.backgroundTintList = unselectedBackground
            binding.usersTabButton.setTextColor(unselectedText)
        } else {
            binding.usersTabButton.backgroundTintList = selectedBackground
            binding.usersTabButton.setTextColor(selectedText)
            binding.groupsTabButton.backgroundTintList = unselectedBackground
            binding.groupsTabButton.setTextColor(unselectedText)
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

    private fun showCreateGroupDialog() {
        val context = requireContext()
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 0)
        }

        val groupNameInput = EditText(context).apply {
            hint = "Group name"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val creatorInput = EditText(context).apply {
            hint = "Creator username"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        container.addView(groupNameInput)
        container.addView(creatorInput)

        AlertDialog.Builder(context)
            .setTitle("New Group")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Create") { _, _ ->
                val groupName = groupNameInput.text?.toString()?.trim().orEmpty()
                val creator = creatorInput.text?.toString()?.trim().orEmpty()
                viewLifecycleOwner.lifecycleScope.launch {
                    createGroup(groupName, creator)
                }
            }
            .show()
    }

    private suspend fun createGroup(groupName: String, creator: String) {
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

    private fun showCreateExpenseDialog() {
        if (selectedGroup == null) {
            showToast("Select a group first")
            return
        }

        val context = requireContext()
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 0)
        }

        val titleInput = EditText(context).apply {
            hint = "Expense title"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val amountInput = EditText(context).apply {
            hint = "Amount"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val payerInput = EditText(context).apply {
            hint = "Payer username"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val participantsInput = EditText(context).apply {
            hint = "Participants comma separated (optional)"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        container.addView(titleInput)
        container.addView(amountInput)
        container.addView(payerInput)
        container.addView(participantsInput)

        AlertDialog.Builder(context)
            .setTitle("New Expense")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Create") { _, _ ->
                val title = titleInput.text?.toString()?.trim().orEmpty()
                val amountRaw = amountInput.text?.toString()?.trim().orEmpty()
                val payer = payerInput.text?.toString()?.trim().orEmpty()
                val participants = participantsInput.text?.toString().orEmpty()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                viewLifecycleOwner.lifecycleScope.launch {
                    addExpense(title, amountRaw, payer, participants)
                }
            }
            .show()
    }

    private suspend fun addExpense(
        title: String,
        amountRaw: String,
        payer: String,
        participants: List<String>
    ) {
        val group = selectedGroup ?: return

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

    private fun showCreateUserDialog() {
        val context = requireContext()
        val input = EditText(context).apply {
            hint = "Username"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        AlertDialog.Builder(context)
            .setTitle("New User")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Create") { _, _ ->
                val username = input.text?.toString()?.trim().orEmpty()
                viewLifecycleOwner.lifecycleScope.launch {
                    addUser(username)
                }
            }
            .show()
    }

    private suspend fun addUser(username: String) {
        if (username.isBlank()) {
            showToast("Enter username")
            return
        }

        runSafely {
            viewModel.createUser(username)
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
            val text = createEmptyStateText("No groups yet")
            binding.groupsContainer.addView(text)
            return
        }

        groups.forEach { group ->
            val row = createRowContainer()
            val openText = createPrimaryRowText(group.name).apply {
                setOnClickListener {
                    selectedGroup = group
                    selectedExpense = null
                    expenseDetails = null
                    groupsPage = GroupsPage.EXPENSES
                    renderCurrentPage()
                    viewLifecycleOwner.lifecycleScope.launch { loadExpensesForSelectedGroup() }
                }
            }

            val deleteAction = createActionLink("Delete").apply {
                setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch { deleteGroup(group) }
                }
            }

            row.addView(openText)
            row.addView(deleteAction)
            binding.groupsContainer.addView(row)
        }
    }

    private fun renderExpenses() {
        binding.expensesContainer.removeAllViews()
        if (expenses.isEmpty()) {
            val text = createEmptyStateText("No expenses in this group")
            binding.expensesContainer.addView(text)
            return
        }

        expenses.forEach { expense ->
            val row = createRowContainer()
            val openText = createPrimaryRowText("${expense.title} - ${formatMoney(expense.amount)}").apply {
                setOnClickListener {
                    selectedExpense = expense
                    groupsPage = GroupsPage.EXPENSE_DETAILS
                    renderCurrentPage()
                    viewLifecycleOwner.lifecycleScope.launch { loadExpenseDetails() }
                }
            }

            val deleteAction = createActionLink("Delete").apply {
                setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch { deleteExpense(expense) }
                }
            }

            row.addView(openText)
            row.addView(deleteAction)
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
            val row = createRowContainer()
            val info = createPrimaryRowText(
                "${balance.user.username} - owes ${formatMoney(balance.owes)} - gets ${formatMoney(balance.gets)}"
            ).apply {
                text = "${balance.user.username} - owes ${formatMoney(balance.owes)} - gets ${formatMoney(balance.gets)}"
            }
            val removeAction = createActionLink("Remove").apply {
                setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch { removeParticipant(balance.user.username) }
                }
            }
            row.addView(info)
            row.addView(removeAction)
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
            val text = createEmptyStateText("No users yet")
            binding.usersContainer.addView(text)
            return
        }

        users.forEach { user ->
            val row = createRowContainer()
            val label = createPrimaryRowText(user.username)
            val deleteAction = createActionLink("Delete").apply {
                setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch { deleteUser(user) }
                }
            }

            row.addView(label)
            row.addView(deleteAction)
            binding.usersContainer.addView(row)
        }
    }

    private fun createRowContainer(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(18).toFloat()
                setColor(0xFFFFFBF5.toInt())
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        }
    }

    private fun createPrimaryRowText(textValue: String): TextView {
        return TextView(requireContext()).apply {
            text = textValue
            setTextColor(0xFF1C1A17.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            isClickable = true
            isFocusable = true
        }
    }

    private fun createActionLink(textValue: String): TextView {
        return TextView(requireContext()).apply {
            text = textValue
            setTextColor(0xFF8D3B33.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            setPadding(dp(12), dp(6), 0, dp(6))
            gravity = Gravity.END
            isClickable = true
            isFocusable = true
        }
    }

    private fun createEmptyStateText(textValue: String): TextView {
        return TextView(requireContext()).apply {
            text = textValue
            setTextColor(0xFF6C6258.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setPadding(0, dp(14), 0, dp(8))
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
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
