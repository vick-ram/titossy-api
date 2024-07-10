package com.example.models.service

import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ServiceUpdate(
    @Serializable(with = UUIDSerializer::class)
    val serviceId: UUID,
    val serviceName: String,
    val category: String,
    val description: String,
    val price: Double,
    val imageUrl: String?
)
