package com.example.models.supplier

import com.example.models.supplier.address.SupplierAddressResponse
import com.example.models.util.ApprovalStatus
import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class SupplierResponse(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val phone: Long,
    val address: SupplierAddressResponse,
    val email: String,
    val password: String,
    val status: ApprovalStatus,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date,
    @Serializable(with = DateSerializer::class)
    val updatedAt: Date
)

