package com.cherrye.splitmoney.models

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: Long,
    val name: String,
    val creator: User,
    val members: List<User>
)