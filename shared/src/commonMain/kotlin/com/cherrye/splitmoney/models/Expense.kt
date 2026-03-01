package com.cherrye.splitmoney.models

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val groupId: String,
    val title: String,
    val amount: Double,
    val payer: User,
    val participants: List<User>
)
