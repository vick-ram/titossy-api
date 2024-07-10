package com.example.models.purchase_order

import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrderStatus(
    val status: OrderStatus
)
