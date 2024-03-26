package com.example.models.customer

import com.example.models.customer.address.CustomerAddressResponse
import com.example.models.util.ApprovalStatus
import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class CustomerResponse(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val phone: Long,
    val address: CustomerAddressResponse,
    val gender: Gender,
    val email: String,
    val password: String,
    val status: ApprovalStatus,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date,
    @Serializable(with = DateSerializer::class)
    val updatedAt: Date
)
