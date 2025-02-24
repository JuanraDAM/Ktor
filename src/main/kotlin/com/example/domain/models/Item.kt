package com.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: Int,
    val title: String,
    val description: String?,
    val weight: Int,       // Por ejemplo, en gramos
    val image: String,     // Cadena Base64
    val userId: Int
)
