package com.cherrye.splitmoney.models

import kotlinx.serialization.Serializable

@Serializable
data class Balance(
    val user: User,
    val amount: Double
)
