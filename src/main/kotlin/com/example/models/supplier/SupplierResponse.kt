package com.example.models.supplier

import com.example.models.util.ApprovalStatus
import com.example.models.util.LocalDateTimeSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class SupplierResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val fullName: String,
    val phone: String,
    val address: String,
    val email: String,
    val password: String,
    val status: ApprovalStatus,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)

