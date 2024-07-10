package com.example.models.employee

import com.example.models.util.ApprovalStatus
import com.example.models.util.Gender
import com.example.models.util.LocalDateTimeSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class EmployeeResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val username: String,
    val fullName: String,
    val gender: Gender,
    val email: String,
    val password: String,
    val phone: String,
    val role: Roles,
    val availability: Availability?,
    val approvalStatus: ApprovalStatus?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)
