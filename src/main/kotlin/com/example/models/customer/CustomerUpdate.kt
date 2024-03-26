package com.example.models.customer

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class CustomerUpdate(
    val username: String,
    val firstName: String,
    val lastName: String,
    val phone: Long,
    val address: Int,
    val gender: Gender,
    val email: String,
    val password: String,
    @Serializable(with = DateSerializer::class)
    val updatedAt: Date
)
