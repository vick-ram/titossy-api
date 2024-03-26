package com.example.models.service

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ServiceResponse(
    val serviceId: Int,
    val serviceName: String,
    val category: Int,
    val description: String,
    val price: Double,
    val duration: Int,
    val imageUrl: String,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date,
    @Serializable(with = DateSerializer::class)
    val updatedAt: Date
)

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String
)

