package com.example.models.inventory

import kotlinx.serialization.Serializable
import org.valiktor.functions.isPositive
import org.valiktor.validate

@Serializable
data class InventoryRequest(
    val productId: Int,
    val employeeId: Int,
    val quantity: Int,
    val stockLevel: Int
){
    init {
        validate(this){
            validate(InventoryRequest::quantity).isPositive()
            validate(InventoryRequest::stockLevel).isPositive()
        }
    }
}