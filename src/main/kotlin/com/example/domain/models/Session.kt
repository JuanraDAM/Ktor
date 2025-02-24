package com.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: Int,
    val userId: Int,
    val token: String
)
