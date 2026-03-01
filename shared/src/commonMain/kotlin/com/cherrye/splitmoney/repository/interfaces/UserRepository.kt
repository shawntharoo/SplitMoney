package com.cherrye.splitmoney.repository.interfaces

import com.cherrye.splitmoney.models.User

interface UserRepository {
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun createUser(user: User)
}