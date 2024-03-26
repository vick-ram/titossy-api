package com.example.models.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val productId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val image: String
)
