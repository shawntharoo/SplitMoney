//
//  HomeViewController.swift
//  iosApp
//
//  Created by Sandy Adikaram on 7/12/2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import shared
import UIKit

class MainTabBarController: UITabBarController {
    override func viewDidLoad() {
        super.viewDidLoad()

        let groups = UINavigationController(rootViewController: GroupsViewController())
        groups.tabBarItem = UITabBarItem(title: "Groups", image: UIImage(systemName: "folder"), selectedImage: UIImage(systemName: "folder.fill"))

        let users = UINavigationController(rootViewController: UsersViewController())
        users.tabBarItem = UITabBarItem(title: "Users", image: UIImage(systemName: "person.2"), selectedImage: UIImage(systemName: "person.2.fill"))

        viewControllers = [groups, users]
    }
}

class GroupsViewController: BaseViewController<HomeScreenViewModel> {
    private var localViewModel: HomeScreenViewModel?
    private var groups: [Group] = []

    private let scrollView = UIScrollView()
    private let stackView = UIStackView()

    override func setupInterfaceBinding(viewModel: HomeScreenViewModel) {
        localViewModel = viewModel
        title = "Groups"
        view.backgroundColor = .systemBackground
        setupUI()
        loadGroups()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        loadGroups()
    }

    private func setupUI() {
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            barButtonSystemItem: .add,
            target: self,
            action: #selector(addGroupTapped)
        )

        scrollView.translatesAutoresizingMaskIntoConstraints = false
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.axis = .vertical
        stackView.spacing = 10

        view.addSubview(scrollView)
        scrollView.addSubview(stackView)

        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            stackView.topAnchor.constraint(equalTo: scrollView.topAnchor, constant: 16),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            stackView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor, constant: -16)
        ])
    }

    private func loadGroups() {
        guard let vm = localViewModel else { return }
        vm.loadAllGroups { [weak self] groups, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let error = error {
                    self.showError(error.localizedDescription)
                    return
                }
                self.groups = groups ?? []
                self.renderGroups()
            }
        }
    }

    private func renderGroups() {
        stackView.arrangedSubviews.forEach {
            stackView.removeArrangedSubview($0)
            $0.removeFromSuperview()
        }

        if groups.isEmpty {
            let label = UILabel()
            label.text = "No groups yet"
            label.textColor = .secondaryLabel
            stackView.addArrangedSubview(label)
            return
        }

        groups.forEach { group in
            let row = UIStackView()
            row.axis = .horizontal
            row.spacing = 8

            let openButton = UIButton(type: .system)
            openButton.setTitle(group.name, for: .normal)
            openButton.contentHorizontalAlignment = .left
            openButton.addAction(UIAction { [weak self] _ in
                let vc = GroupExpensesViewController(group: group)
                self?.navigationController?.pushViewController(vc, animated: true)
            }, for: .touchUpInside)

            let deleteButton = UIButton(type: .system)
            deleteButton.setTitle("Delete", for: .normal)
            deleteButton.tintColor = .systemRed
            deleteButton.addAction(UIAction { [weak self] _ in
                self?.confirmDeleteGroup(group)
            }, for: .touchUpInside)

            row.addArrangedSubview(openButton)
            row.addArrangedSubview(deleteButton)
            stackView.addArrangedSubview(row)
        }
    }

    @objc private func addGroupTapped() {
        let alert = UIAlertController(title: "New Group", message: nil, preferredStyle: .alert)
        alert.addTextField { $0.placeholder = "Group name" }
        alert.addTextField { $0.placeholder = "Creator username" }
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        alert.addAction(UIAlertAction(title: "Create", style: .default, handler: { [weak self] _ in
            guard let self = self, let vm = self.localViewModel else { return }
            let groupName = alert.textFields?[0].text ?? ""
            let creator = alert.textFields?[1].text ?? ""
            vm.createGroup(name: groupName, creatorUsername: creator) { _, error in
                DispatchQueue.main.async {
                    if let error = error {
                        self.showError(error.localizedDescription)
                    } else {
                        self.loadGroups()
                    }
                }
            }
        }))
        present(alert, animated: true)
    }

    private func confirmDeleteGroup(_ group: Group) {
        let alert = UIAlertController(
            title: "Delete Group",
            message: "This will delete all expenses in this group.",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        alert.addAction(UIAlertAction(title: "Delete", style: .destructive, handler: { [weak self] _ in
            guard let self = self, let vm = self.localViewModel else { return }
            vm.deleteGroup(groupId: group.id) { error in
                DispatchQueue.main.async {
                    if let error = error {
                        self.showError(error.localizedDescription)
                    } else {
                        self.loadGroups()
                    }
                }
            }
        }))
        present(alert, animated: true)
    }

    private func showError(_ message: String) {
        let alert = UIAlertController(title: "Error", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }
}

class GroupExpensesViewController: BaseViewController<HomeScreenViewModel> {
    private var localViewModel: HomeScreenViewModel?
    private var expenses: [Expense] = []
    private let group: Group
    private let stackView = UIStackView()
    private let scrollView = UIScrollView()

    init(group: Group) {
        self.group = group
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        return nil
    }

    override func setupInterfaceBinding(viewModel: HomeScreenViewModel) {
        localViewModel = viewModel
        title = group.name
        view.backgroundColor = .systemBackground
        setupUI()
        loadExpenses()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        loadExpenses()
    }

    private func setupUI() {
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            barButtonSystemItem: .add,
            target: self,
            action: #selector(addExpenseTapped)
        )

        scrollView.translatesAutoresizingMaskIntoConstraints = false
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.axis = .vertical
        stackView.spacing = 10

        view.addSubview(scrollView)
        scrollView.addSubview(stackView)

        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            stackView.topAnchor.constraint(equalTo: scrollView.topAnchor, constant: 16),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            stackView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor, constant: -16)
        ])
    }

    private func loadExpenses() {
        guard let vm = localViewModel else { return }
        vm.loadExpensesForGroup(groupId: group.id) { [weak self] expenses, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let error = error {
                    self.showError(error.localizedDescription)
                    return
                }
                self.expenses = expenses ?? []
                self.renderExpenses()
            }
        }
    }

    private func renderExpenses() {
        stackView.arrangedSubviews.forEach {
            stackView.removeArrangedSubview($0)
            $0.removeFromSuperview()
        }

        if expenses.isEmpty {
            let label = UILabel()
            label.text = "No expenses in this group"
            label.textColor = .secondaryLabel
            stackView.addArrangedSubview(label)
            return
        }

        expenses.forEach { expense in
            let row = UIStackView()
            row.axis = .horizontal
            row.spacing = 8

            let openButton = UIButton(type: .system)
            openButton.setTitle("\(expense.title) • $\(String(format: "%.2f", expense.amount))", for: .normal)
            openButton.contentHorizontalAlignment = .left
            openButton.addAction(UIAction { [weak self] _ in
                let vc = ExpenseDetailsViewController(expenseId: expense.id)
                self?.navigationController?.pushViewController(vc, animated: true)
            }, for: .touchUpInside)

            let deleteButton = UIButton(type: .system)
            deleteButton.setTitle("Delete", for: .normal)
            deleteButton.tintColor = .systemRed
            deleteButton.addAction(UIAction { [weak self] _ in
                self?.deleteExpense(expense.id)
            }, for: .touchUpInside)

            row.addArrangedSubview(openButton)
            row.addArrangedSubview(deleteButton)
            stackView.addArrangedSubview(row)
        }
    }

    @objc private func addExpenseTapped() {
        let alert = UIAlertController(title: "New Expense", message: nil, preferredStyle: .alert)
        alert.addTextField { $0.placeholder = "Title" }
        alert.addTextField { $0.placeholder = "Amount"; $0.keyboardType = .decimalPad }
        alert.addTextField { $0.placeholder = "Payer username" }
        alert.addTextField { $0.placeholder = "Participants comma separated (optional)" }
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        alert.addAction(UIAlertAction(title: "Add", style: .default, handler: { [weak self] _ in
            guard let self = self, let vm = self.localViewModel else { return }
            let title = alert.textFields?[0].text ?? ""
            let amount = Double(alert.textFields?[1].text ?? "") ?? 0.0
            let payer = alert.textFields?[2].text ?? ""
            let participantsRaw = alert.textFields?[3].text ?? ""
            let participants = participantsRaw
                .split(separator: ",")
                .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
                .filter { !$0.isEmpty }

            vm.addExpense(
                groupId: self.group.id,
                title: title,
                amount: amount,
                payerUsername: payer,
                participantUsernames: participants
            ) { _, error in
                DispatchQueue.main.async {
                    if let error = error {
                        self.showError(error.localizedDescription)
                    } else {
                        self.loadExpenses()
                    }
                }
            }
        }))
        present(alert, animated: true)
    }

    private func deleteExpense(_ expenseId: String) {
        guard let vm = localViewModel else { return }
        vm.deleteExpense(expenseId: expenseId) { [weak self] error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let error = error {
                    self.showError(error.localizedDescription)
                } else {
                    self.loadExpenses()
                }
            }
        }
    }

    private func showError(_ message: String) {
        let alert = UIAlertController(title: "Error", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }
}

class ExpenseDetailsViewController: BaseViewController<HomeScreenViewModel> {
    private var localViewModel: HomeScreenViewModel?
    private var details: HomeScreenViewModel.ExpenseDetails?
    private let expenseId: String

    private let infoLabel = UILabel()
    private let settlementLabel = UILabel()
    private let participantsStack = UIStackView()
    private let scrollView = UIScrollView()
    private let contentStack = UIStackView()

    init(expenseId: String) {
        self.expenseId = expenseId
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        return nil
    }

    override func setupInterfaceBinding(viewModel: HomeScreenViewModel) {
        localViewModel = viewModel
        title = "Expense"
        view.backgroundColor = .systemBackground
        setupUI()
        loadDetails()
    }

    private func setupUI() {
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            barButtonSystemItem: .add,
            target: self,
            action: #selector(addParticipantTapped)
        )

        scrollView.translatesAutoresizingMaskIntoConstraints = false
        contentStack.translatesAutoresizingMaskIntoConstraints = false
        contentStack.axis = .vertical
        contentStack.spacing = 12

        infoLabel.numberOfLines = 0
        settlementLabel.numberOfLines = 0
        participantsStack.axis = .vertical
        participantsStack.spacing = 8

        view.addSubview(scrollView)
        scrollView.addSubview(contentStack)
        contentStack.addArrangedSubview(infoLabel)
        contentStack.addArrangedSubview(participantsStack)
        contentStack.addArrangedSubview(settlementLabel)

        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            contentStack.topAnchor.constraint(equalTo: scrollView.topAnchor, constant: 16),
            contentStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            contentStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            contentStack.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor, constant: -16)
        ])
    }

    private func loadDetails() {
        guard let vm = localViewModel else { return }
        vm.loadExpenseDetails(expenseId: expenseId) { [weak self] details, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let error = error {
                    self.showError(error.localizedDescription)
                    return
                }
                self.details = details
                self.renderDetails()
            }
        }
    }

    private func renderDetails() {
        guard let details = details else { return }
        infoLabel.text = "\(details.expense.title)\nAmount: $\(String(format: "%.2f", details.expense.amount))\nPayer: \(details.expense.payer.username)"

        participantsStack.arrangedSubviews.forEach {
            participantsStack.removeArrangedSubview($0)
            $0.removeFromSuperview()
        }

        details.balances.forEach { balance in
            let row = UIStackView()
            row.axis = .horizontal
            row.spacing = 8

            let label = UILabel()
            label.text = "\(balance.user.username) • owes $\(String(format: "%.2f", balance.owes)) • gets $\(String(format: "%.2f", balance.gets))"
            label.numberOfLines = 0

            let removeButton = UIButton(type: .system)
            removeButton.setTitle("Remove", for: .normal)
            removeButton.tintColor = .systemRed
            removeButton.addAction(UIAction { [weak self] _ in
                self?.removeParticipant(balance.user.username)
            }, for: .touchUpInside)

            row.addArrangedSubview(label)
            row.addArrangedSubview(removeButton)
            participantsStack.addArrangedSubview(row)
        }

        if details.settlements.isEmpty {
            settlementLabel.text = "Settlement: Everyone is settled."
        } else {
            settlementLabel.text = "Settlement:\n" + details.settlements.map {
                "- \($0.from.username) pays \($0.to.username) $\(String(format: "%.2f", $0.amount))"
            }.joined(separator: "\n")
        }
    }

    @objc private func addParticipantTapped() {
        let alert = UIAlertController(title: "Add Participant", message: nil, preferredStyle: .alert)
        alert.addTextField { $0.placeholder = "Username" }
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        alert.addAction(UIAlertAction(title: "Add", style: .default, handler: { [weak self] _ in
            guard let self = self, let vm = self.localViewModel else { return }
            let username = alert.textFields?.first?.text ?? ""
            vm.addParticipantToExpense(expenseId: self.expenseId, username: username) { _, error in
                DispatchQueue.main.async {
                    if let error = error {
                        self.showError(error.localizedDescription)
                    } else {
                        self.loadDetails()
                    }
                }
            }
        }))
        present(alert, animated: true)
    }

    private func removeParticipant(_ username: String) {
        guard let vm = localViewModel else { return }
        vm.removeParticipantFromExpense(expenseId: expenseId, username: username) { [weak self] _, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let error = error {
                    self.showError(error.localizedDescription)
                } else {
                    self.loadDetails()
                }
            }
        }
    }

    private func showError(_ message: String) {
        let alert = UIAlertController(title: "Error", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }
}

class UsersViewController: BaseViewController<HomeScreenViewModel> {
    private var localViewModel: HomeScreenViewModel?
    private var users: [User] = []
    private let stackView = UIStackView()
    private let scrollView = UIScrollView()

    override func setupInterfaceBinding(viewModel: HomeScreenViewModel) {
        localViewModel = viewModel
        title = "Users"
        view.backgroundColor = .systemBackground
        setupUI()
        loadUsers()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        loadUsers()
    }

    private func setupUI() {
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            barButtonSystemItem: .add,
            target: self,
            action: #selector(addUserTapped)
        )

        scrollView.translatesAutoresizingMaskIntoConstraints = false
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.axis = .vertical
        stackView.spacing = 10

        view.addSubview(scrollView)
        scrollView.addSubview(stackView)

        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            stackView.topAnchor.constraint(equalTo: scrollView.topAnchor, constant: 16),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            stackView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor, constant: -16)
        ])
    }

    private func loadUsers() {
        guard let vm = localViewModel else { return }
        vm.loadAllUsers { [weak self] users, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let error = error {
                    self.showError(error.localizedDescription)
                    return
                }
                self.users = users ?? []
                self.renderUsers()
            }
        }
    }

    private func renderUsers() {
        stackView.arrangedSubviews.forEach {
            stackView.removeArrangedSubview($0)
            $0.removeFromSuperview()
        }

        if users.isEmpty {
            let label = UILabel()
            label.text = "No users yet"
            label.textColor = .secondaryLabel
            stackView.addArrangedSubview(label)
            return
        }

        users.forEach { user in
            let row = UIStackView()
            row.axis = .horizontal
            row.spacing = 8

            let label = UILabel()
            label.text = user.username

            let deleteButton = UIButton(type: .system)
            deleteButton.setTitle("Delete", for: .normal)
            deleteButton.tintColor = .systemRed
            deleteButton.addAction(UIAction { [weak self] _ in
                self?.deleteUser(user.username)
            }, for: .touchUpInside)

            row.addArrangedSubview(label)
            row.addArrangedSubview(deleteButton)
            stackView.addArrangedSubview(row)
        }
    }

    @objc private func addUserTapped() {
        let alert = UIAlertController(title: "Add User", message: nil, preferredStyle: .alert)
        alert.addTextField { $0.placeholder = "Username" }
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        alert.addAction(UIAlertAction(title: "Add", style: .default, handler: { [weak self] _ in
            guard let self = self, let vm = self.localViewModel else { return }
            let username = alert.textFields?.first?.text ?? ""
            vm.createUser(username: username) { _, error in
                DispatchQueue.main.async {
                    if let error = error {
                        self.showError(error.localizedDescription)
                    } else {
                        self.loadUsers()
                    }
                }
            }
        }))
        present(alert, animated: true)
    }

    private func deleteUser(_ username: String) {
        guard let vm = localViewModel else { return }
        vm.deleteUserByUsername(username: username) { [weak self] success, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                if let error = error {
                    self.showError(error.localizedDescription)
                    return
                }
                if success?.boolValue == true {
                    self.loadUsers()
                } else {
                    self.showError("User cannot be deleted. They are involved in expenses or own a group.")
                }
            }
        }
    }

    private func showError(_ message: String) {
        let alert = UIAlertController(title: "Error", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }
}

class HomeViewController: GroupsViewController {}
