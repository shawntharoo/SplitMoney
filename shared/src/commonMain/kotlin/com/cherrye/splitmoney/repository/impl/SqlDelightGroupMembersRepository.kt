package com.cherrye.splitmoney.repository.impl

import com.cherrye.splitmoney.models.User
import com.cherrye.splitmoney.repository.interfaces.GroupMembersRepository
import com.cherrye.splitmoney.splitMoney

class SqlDelightGroupMembersRepository(private val db: splitMoney): GroupMembersRepository {
    private val groupMemberQueries = db.groupMembersQueries
    override suspend fun addMember(groupId: Long, userId: Long) {
        groupMemberQueries.insertMember(groupId, userId)
    }

    override suspend fun getMembers(groupId: Long): List<User> {
        return groupMemberQueries.selectMembersForGroup(groupId).executeAsList()
            .map {
                User(
                    it.id,
                    it.username
                )
            }
    }

    override suspend fun deleteMember(groupId: Long, userId: Long) {
        groupMemberQueries.deleteMember(groupId, userId)
    }

    override suspend fun deleteMembersForGroup(groupId: Long) {
        groupMemberQueries.deleteMembersByGroup(groupId)
    }
}