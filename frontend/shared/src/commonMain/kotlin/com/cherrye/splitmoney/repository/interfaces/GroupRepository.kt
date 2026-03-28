package com.cherrye.splitmoney.repository.interfaces

import com.cherrye.splitmoney.models.Group
import com.cherrye.splitmoney.models.User

interface GroupRepository {
    suspend fun createGroup(name: String, creator: Long) : Long
    suspend fun getAllGroups(): List<Group>
    suspend fun getAllGroupsForUser(userId: Long) : List<Group>
    suspend fun updateGroupName(groupId: Long, newName: String)
    suspend fun deleteGroup(groupId: Long)
    suspend fun addUserToGroup(groupId: Long, user: User)
    suspend fun getUserInGroup(groupId: Long): List<User>
    suspend fun getGroupById(groupId: Long): Group?
}
