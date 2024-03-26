package com.example.models.employee

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class EmployeeResponse(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val email: String,
    val password: String,
    val phone: Long,
    val role: Roles,
    val availability: Availability?,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date,
    @Serializable(with = DateSerializer::class)
    val updatedAt: Date
)
