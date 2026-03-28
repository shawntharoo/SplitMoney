package com.cherrye.splitmoney.repository.interfaces

import com.cherrye.splitmoney.models.User

interface GroupMembersRepository {
    suspend fun addMember(groupId: Long, userId: Long)
    suspend fun getMembers(groupId: Long): List<User>
    suspend fun deleteMember(groupId: Long, userId: Long)
    suspend fun deleteMembersForGroup(groupId: Long)
}