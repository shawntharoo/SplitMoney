package com.cherrye.splitmoney.services.dbCon

import app.cash.sqldelight.db.SqlDriver
import com.cherrye.splitmoney.repository.impl.SqlDelightExpenseParticipantsRepository
import com.cherrye.splitmoney.repository.impl.SqlDelightExpenseRepository
import com.cherrye.splitmoney.repository.impl.SqlDelightGroupMembersRepository
import com.cherrye.splitmoney.repository.impl.SqlDelightGroupRepository
import com.cherrye.splitmoney.repository.impl.SqlDelightUserRepository
import com.cherrye.splitmoney.repository.interfaces.ExpenseParticipantsRepository
import com.cherrye.splitmoney.repository.interfaces.ExpenseRepository
import com.cherrye.splitmoney.repository.interfaces.GroupMembersRepository
import com.cherrye.splitmoney.repository.interfaces.GroupRepository
import com.cherrye.splitmoney.repository.interfaces.UserRepository
import com.cherrye.splitmoney.splitMoney

class AppModule(driver: SqlDriver) {
    private val database = splitMoney(driver)

    val groupRepository: GroupRepository = SqlDelightGroupRepository(database)
    val expenseRepository: ExpenseRepository = SqlDelightExpenseRepository(database)
    val userRepository: UserRepository = SqlDelightUserRepository(database)
    val expenseParticipantsRepository: ExpenseParticipantsRepository = SqlDelightExpenseParticipantsRepository(database)
    val groupMembersRepository: GroupMembersRepository = SqlDelightGroupMembersRepository(database)
}