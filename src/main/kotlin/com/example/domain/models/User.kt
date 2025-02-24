package com.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val email: String,
    val password: String // Almacenaremos el hash (usando BCrypt)
)
