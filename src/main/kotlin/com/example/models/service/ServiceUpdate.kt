package com.example.models.service

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isPositive
import org.valiktor.validate
import java.util.Date

@Serializable
data class ServiceUpdate(
    val serviceId: Int,
    val serviceName: String,
    val category: Int,
    val description: String,
    val price: Double,
    val duration: Int,
    val imageUrl: String,
    val status: String,
    @Serializable(with = DateSerializer::class)
    val updatedAt: Date
) {
    init {
        validate(this){
            validate(ServiceUpdate::serviceName).isNotEmpty()
            validate(ServiceUpdate::category).isPositive()
            validate(ServiceUpdate::description).isNotEmpty()
            validate(ServiceUpdate::price).isPositive()
            validate(ServiceUpdate::duration).isPositive()
            validate(ServiceUpdate::status).isNotEmpty()
        }
    }
}
