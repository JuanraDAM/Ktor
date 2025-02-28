package com.example.domain.Cards

import kotlinx.serialization.Serializable

@Serializable
data class UpdateItemRequest(
    val title: String? = null,
    val description: String? = null,
    val weight: Int? = null,
    val image: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
