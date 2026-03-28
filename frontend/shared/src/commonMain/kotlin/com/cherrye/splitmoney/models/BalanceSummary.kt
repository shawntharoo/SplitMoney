package com.cherrye.splitmoney.models

import kotlinx.serialization.Serializable

@Serializable
data class BalanceSummary(
    val user: User,
    val owes: Map<User, Double>,
    val owedBy: Map<User, Double>
)
