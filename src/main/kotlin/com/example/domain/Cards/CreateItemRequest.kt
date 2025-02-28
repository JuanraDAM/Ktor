package com.example.domain.Cards

import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    val title: String,
    val description: String? = null,
    val weight: Int,
    val image: String,  // Cadena Base64
    val userId: Int,
    val latitude: Double? = null,
    val longitude: Double? = null
)
