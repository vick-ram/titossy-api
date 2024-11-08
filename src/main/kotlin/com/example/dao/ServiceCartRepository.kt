package com.example.dao

import com.example.models.AddServiceAddonToCart
import com.example.models.AddServiceToCart
import com.example.models.ServiceCartResponse
import java.util.*

interface ServiceCartRepository {
    suspend fun addServiceToCart(customerId: String, service: AddServiceToCart): String
    suspend fun addServiceAddOnToCart(customerId: String, serviceAddon: AddServiceAddonToCart): String
    suspend fun getCart(customerId: String): List<ServiceCartResponse>
    suspend fun removeAddonFromCart(customerId: String, serviceAddon: UUID): Boolean
    suspend fun removeServiceFromCart(customerId: String, serviceId: UUID): Boolean
    suspend fun clearCart(customerId: String): Boolean
}
