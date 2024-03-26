package com.example.models.order

import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

@Serializable
data class OrderRequest(
    val employeeId: Int,
    val supplierId: Int,
    val orderItems: List<OrderItemRequest>
){
    init {
        validate(this){
            validate(OrderRequest::orderItems).isNotEmpty()
        }
    }
}
