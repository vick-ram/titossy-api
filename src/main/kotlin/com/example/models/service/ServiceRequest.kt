package com.example.models.service

import com.example.models.util.BigDecimalSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate
import java.math.BigDecimal

@Serializable
data class ServiceRequest(
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val imageUrl: String?
) : java.io.Serializable {
    fun validate(): ServiceRequest {
        validate(this){
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
        validate(this) {
            validate(ServiceAddOnRequest::name).hasSize(min = 3, max = 50)
            validate(ServiceAddOnRequest::description).hasSize(min = 3, max = 200)
            validate(ServiceAddOnRequest::price).isPositive()
        }
        return this
    }
}
