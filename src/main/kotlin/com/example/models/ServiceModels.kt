package com.example.models

import com.example.commons.BigDecimalSerializer
import com.example.commons.LocalDateTimeSerializer
import com.example.commons.UUIDSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Serializable
data class ServiceRequest(
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val imageUrl: String?
) : java.io.Serializable {
    fun validate(): ServiceRequest {
        org.valiktor.validate(this) {
            validate(ServiceRequest::name).isNull()
            validate(ServiceRequest::description).isNotNull().hasSize(min = 3, max = 200)
            validate(ServiceRequest::price).isPositive().isNotZero()
        }
        return this
    }
}

@Serializable
data class ServiceAddOnRequest(
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val imageUrl: String?
) : java.io.Serializable {
    fun validate(): ServiceAddOnRequest {
        org.valiktor.validate(this) {
            validate(ServiceAddOnRequest::name).hasSize(min = 3, max = 50)
            validate(ServiceAddOnRequest::description).hasSize(min = 3, max = 200)
            validate(ServiceAddOnRequest::price).isPositive()
        }
        return this
    }
}

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

@Serializable
data class ServiceResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val imageUrl: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
) : java.io.Serializable

@Serializable
data class ServiceAddOnResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val serviceId: UUID,
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val imageUrl: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
) : java.io.Serializable