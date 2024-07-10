package com.example.dao

import com.example.db.cart.ServiceCartItem
import com.example.models.cart.AddServiceAddonToCart
import com.example.models.cart.AddServiceToCart
import com.example.models.cart.ServiceCartResponse
import java.math.BigDecimal
import java.util.*

interface ServiceCartRepository {
    suspend fun addServiceToCart(customerId: UUID, service: AddServiceToCart): String
    suspend fun addServiceAddOnToCart(customerId: UUID, serviceAddon: AddServiceAddonToCart): String
    suspend fun getCart(customerId: UUID): List<ServiceCartResponse>
    suspend fun removeAddonFromCart(customerId: UUID, serviceAddon: UUID): Boolean
    suspend fun removeServiceFromCart(customerId: UUID, serviceId: UUID): Boolean
    suspend fun clearCart(customerId: UUID): Boolean
}
