package com.example.models.inventory

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class InventoryResponse(
    val inventoryId: Int,
    val productId: Int,
    val employeeId: Int,
    val quantity: Int,
    @Serializable(with = DateSerializer::class)
    val lastUpdate: Date,
)
