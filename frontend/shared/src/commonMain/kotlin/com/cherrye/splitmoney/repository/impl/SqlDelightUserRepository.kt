package com.cherrye.splitmoney.repository.impl

import com.cherrye.splitmoney.models.User
import com.cherrye.splitmoney.repository.interfaces.UserRepository
import com.cherrye.splitmoney.splitMoney

class SqlDelightUserRepository(private val db: splitMoney) : UserRepository {
    private val userQueries = db.userQueries
    private val groupMembersQueries = db.groupMembersQueries

    override suspend fun getUserById(userId: String): User? {
        return userQueries.selectUserById(userId.toLong()).executeAsOneOrNull()?.let {
            User(it.id, it.username)
        }
    }

    override suspend fun getUserByUsername(username: String): User? {
        return userQueries.selectUserByUserName(username).executeAsOneOrNull()?.let {
            User(it.id, it.username)
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return userQueries.selectAllUsers().executeAsList().map { User(it.id, it.username) }
    }

    override suspend fun createUser(user: User) {
        userQueries.insertUser(user.username)
    }

    override suspend fun deleteUserIfAllowed(userId: Long): Boolean {
        val inAnyExpense = userQueries.isUserInAnyExpense(userId, userId).executeAsOne()
        val isGroupCreator = userQueries.isUserCreatorOfAnyGroup(userId).executeAsOne()
        if (inAnyExpense || isGroupCreator) return false

        groupMembersQueries.deleteUserFromAllGroups(userId)
        userQueries.deleteUserById(userId)
        return true
    }
}
