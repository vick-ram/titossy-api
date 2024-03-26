package com.example.models.service

import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isPositive
import org.valiktor.validate

@Serializable
data class ServiceRequest(
    val serviceName: String,
    val category: Int,
    val description: String,
    val price: Double,
    val duration: Int,
    val imageUrl: String?
) {
    init {
        validate(this){
            validate(ServiceRequest::serviceName).isNotEmpty()
            validate(ServiceRequest::description).isNotEmpty()
            validate(ServiceRequest::price).isPositive()
            validate(ServiceRequest::duration).isPositive()
        }
    }
}

@Serializable
data class CategoryRequest(
    val name: String
)
