package com.example.models.order

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class OrderResponse(
    val oderId: Int,
    val employeeId: Int,
    val supplierId: Int,
    @Serializable(with = DateSerializer::class)
    val orderDate: Date,
    val orderItems: List<OrderItemResponse>,
    val total: Double,
    val orderStatus: String
)
