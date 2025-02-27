package com.example.domain.cards.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    val title: String,
    val description: String? = null,
    val weight: Int,
    val image: String,
    val userId: Int
)
