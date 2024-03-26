package com.example.models.order

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemRequest(
    val orderId: Int,
    val productId: Int,
    val quantity: Int
)

@Serializable
data class OrderItemResponse(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val price: Double
)