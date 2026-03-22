package com.cherrye.splitmoney.repository.impl

import com.cherrye.splitmoney.models.Group
import com.cherrye.splitmoney.models.User
import com.cherrye.splitmoney.repository.interfaces.GroupRepository
import com.cherrye.splitmoney.splitMoney

class SqlDelightGroupRepository(private val db: splitMoney) : GroupRepository {
    private val groupQueries = db.groupQueries
    private val groupMemberQueries = db.groupMembersQueries
    private val expenseQueries = db.expenseQueries
    private val expenseParticipantsQueries = db.expenseParticipantsQueries

    override suspend fun createGroup(name: String, creator: Long): Long {
        groupQueries.insertGroup(name = name, creator_id = creator)
        val groupId = groupQueries.lastInsertRowId().executeAsOne()
        groupMemberQueries.insertMember(groupId, creator)
        return groupId
    }

    override suspend fun getAllGroups(): List<Group> {
        return groupQueries.selectAllGroups().executeAsList().mapNotNull { row ->
            val group = groupQueries.selectGroupById(row.id).executeAsOneOrNull() ?: return@mapNotNull null
            val members = getUserInGroup(group.id)
            Group(
                id = group.id,
                name = group.name,
                creator = User(group.creator_id, group.creator_username),
                members = (listOf(User(group.creator_id, group.creator_username)) + members)
                    .distinctBy { it.id }
            )
        }
    }

    override suspend fun getAllGroupsForUser(userId: Long): List<Group> {
        val createdGroups = groupQueries.selectGroupsByCreator(userId).executeAsList().map {
            Group(id = it.id, name = it.name, creator = User(it.creator_id, it.creator_username), members = emptyList())
        }

        val memberGroups = groupQueries.selectGroupsByMember(userId).executeAsList().map {
            Group(id = it.id, name = it.name, creator = User(it.creator_id, it.creator_username), members = emptyList())
        }

        val allGroups = (createdGroups + memberGroups).distinctBy { it.id }

        return allGroups.map { row ->
            val members = getUserInGroup(row.id)
            Group(
                id = row.id,
                name = row.name,
                creator = User(row.creator.id, row.creator.username),
                members = (listOf(User(row.creator.id, row.creator.username)) + members).distinctBy { it.id }
            )
        }
    }

    override suspend fun updateGroupName(groupId: Long, newName: String) {
        groupQueries.updateGroupName(newName, groupId)
    }

    override suspend fun deleteGroup(groupId: Long) {
        val expenseIds = expenseQueries.selectExpenseIdsForGroup(groupId.toString()).executeAsList()
        expenseIds.forEach { expenseId ->
            expenseParticipantsQueries.deleteParticipantsForExpense(expenseId)
        }
        expenseQueries.deleteExpensesForGroup(groupId.toString())
        groupMemberQueries.deleteMembersByGroup(groupId)
        groupQueries.deleteGroup(groupId)
    }

    override suspend fun addUserToGroup(groupId: Long, user: User) {
        val existingMemberIds = groupMemberQueries.selectMembersForGroup(groupId).executeAsList().map { it.id }.toSet()
        if (!existingMemberIds.contains(user.id)) {
            groupMemberQueries.insertMember(groupId, user.id)
        }
    }

    override suspend fun getUserInGroup(groupId: Long): List<User> {
        return groupMemberQueries.selectMembersForGroup(groupId).executeAsList().map { User(it.id, it.username) }
    }

    override suspend fun getGroupById(groupId: Long): Group? {
        val groupRow = groupQueries.selectGroupById(groupId).executeAsOneOrNull() ?: return null
        val members = getUserInGroup(groupRow.id)
        return Group(
            id = groupRow.id,
            name = groupRow.name,
            creator = User(groupRow.creator_id, groupRow.creator_username),
            members = (listOf(User(groupRow.creator_id, groupRow.creator_username)) + members).distinctBy { it.id }
        )
    }
}
