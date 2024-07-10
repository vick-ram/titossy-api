package com.example.commands.queries.cart

import com.example.dao.ServiceCartRepository
import com.example.db.cart.ServiceCart
import com.example.db.cart.ServiceCartItem
import com.example.db.customer.Customer
import com.example.db.service.Service
import com.example.db.service.ServiceAddOn
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.cart.*
import org.jetbrains.exposed.sql.and
import java.util.*

class ServiceCartRepositoryImpl : ServiceCartRepository {

    override suspend fun addServiceToCart(customerId: UUID, service: AddServiceToCart): String = dbQuery {
        val existingCartItem = ServiceCartItem.find {
            (ServiceCart.customerId eq customerId) and (ServiceCart.serviceId eq service.serviceId)
        }.singleOrNull()

        if (existingCartItem == null) {
            ServiceCartItem.new {
                this.customer = Customer[customerId]
                this.service = service.serviceId?.let { Service[it] }
                this.quantity = service.quantity
            }
            return@dbQuery "Service added to cart successfully"
        } else {
            return@dbQuery "Service is already in cart"
        }
    }


    override suspend fun addServiceAddOnToCart(customerId: UUID, serviceAddon: AddServiceAddonToCart): String =
        dbQuery {
            val existingCartItem = ServiceCartItem.find {
                (ServiceCart.customerId eq customerId) and (ServiceCart.serviceAddonId eq serviceAddon.serviceAddon)
            }.singleOrNull()
            if (existingCartItem == null) {
                ServiceCartItem.new {
                    this.customer = Customer[customerId]
                    this.serviceAddon = serviceAddon.serviceAddon.let { ServiceAddOn[it] }
                    this.quantity = serviceAddon.quantity
                }
                return@dbQuery "Service add-on added to cart successfully"
            } else {
                existingCartItem.quantity += serviceAddon.quantity
                return@dbQuery "Service addon quantity updated"
            }
        }

    override suspend fun getCart(customerId: UUID): List<ServiceCartResponse> = dbQuery {
        ServiceCartItem.find { ServiceCart.customerId eq customerId }
            .groupBy { it.service ?: it.serviceAddon }
            .map { (serviceOrAddon, cartItems) ->
                ServiceCartResponse(
                    customerId = UUID.fromString(id),
                    service = if (serviceOrAddon is Service) {
                        ServiceCartItemResponse(
                            id = serviceOrAddon.id.value,
                            name = serviceOrAddon.name,
                            price = serviceOrAddon.price,
                            thumbnail = serviceOrAddon.imageUrl,
                            quantity = cartItems.sumOf { it.quantity }
                        )
                    } else null,
                    addOns = if (serviceOrAddon is ServiceAddOn) {
                        ServiceAddOnCartItemResponse(
                            id = serviceOrAddon.id.value,
                            name = serviceOrAddon.name,
                            price = serviceOrAddon.price,
                            thumbnail = serviceOrAddon.imageUrl,
                            quantity = cartItems.sumOf { it.quantity }
                        )
                    } else null,
                    total = cartItems.sumOf { it.totalAmount }
                )
            }
    }

    override suspend fun removeAddonFromCart(customerId: UUID, serviceAddon: UUID): Boolean = dbQuery {
        val cartItem =
            ServiceCartItem.find {
                (ServiceCart.customerId eq customerId) and (ServiceCart.serviceAddonId eq serviceAddon)
            }.singleOrNull()
        if (cartItem != null) {
            cartItem.delete()
            return@dbQuery true
        } else {
            return@dbQuery false
        }
    }

    override suspend fun removeServiceFromCart(customerId: UUID, serviceId: UUID): Boolean = dbQuery {
        val serviceExistInCart =
            ServiceCartItem.find {
                (ServiceCart.customerId eq customerId) and (ServiceCart.serviceId eq serviceId)
            }.singleOrNull()
        if (serviceExistInCart != null) {
            serviceExistInCart.delete()
            return@dbQuery true
        } else {
            return@dbQuery false
        }
    }

    override suspend fun clearCart(customerId: UUID): Boolean = dbQuery {
        val cartItems = ServiceCartItem.find { ServiceCart.customerId eq customerId }
        if (cartItems.empty()) {
            false
        } else {
            cartItems.forEach { it.delete() }
            true
        }
    }
}