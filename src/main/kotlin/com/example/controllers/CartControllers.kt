package com.example.controllers

import com.example.commons.DatabaseUtil.dbQuery
import com.example.dao.ServiceCartRepository
import com.example.db.*
import com.example.models.*
import org.jetbrains.exposed.sql.and
import java.util.*

suspend fun addProductToCart(
    employeeId: String,
    productCart: ProductCart
): Boolean = dbQuery {
    val productExist = ProductCartEntity
        .find { (ProductCarts.employee eq employeeId) and (ProductCarts.product eq productCart.productId) }
        .singleOrNull()
    if (productExist != null) {
        productExist.quantity += productCart.quantity
        return@dbQuery false
    }
    ProductCartEntity.new {
        this.employee = Employee[employeeId]
        this.product = Product[productCart.productId]
        this.quantity = productCart.quantity
    }
    return@dbQuery true
}

suspend fun getProductsInCart(
    employeeId: String
): List<ProductCartResponse> = dbQuery {
    ProductCartEntity.find {
        ProductCarts.employee eq employeeId
    }.map { it.toProductCartResponse() }
}

suspend fun removeProductFromCart(
    employeeId: String,
    productId: UUID
): Boolean = dbQuery {
    val product = ProductCartEntity.find {
        (ProductCarts.employee eq employeeId) and
                (ProductCarts.product eq productId)
    }.singleOrNull()
    product?.delete()
    return@dbQuery true
}

suspend fun clearCart(employeeId: String) = dbQuery {
    ProductCartEntity.find { ProductCarts.employee eq employeeId }
        .forEach { it.delete() }
}


class ServiceCartRepositoryImpl : ServiceCartRepository {

    override suspend fun addServiceToCart(
        customerId: String,
        service: AddServiceToCart
    ): String = dbQuery {
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


    override suspend fun addServiceAddOnToCart(
        customerId: String,
        serviceAddon: AddServiceAddonToCart
    ): String =
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

    override suspend fun getCart(customerId: String): List<ServiceCartResponse> = dbQuery {
        ServiceCartItem.find { ServiceCart.customerId eq customerId }
            .groupBy { it.service ?: it.serviceAddon }
            .map { (serviceOrAddon, cartItems) ->
                ServiceCartResponse(
                    customerId = id,
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

    override suspend fun removeAddonFromCart(
        customerId: String,
        serviceAddon: UUID
    ): Boolean = dbQuery {
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

    override suspend fun removeServiceFromCart(
        customerId: String,
        serviceId: UUID
    ): Boolean = dbQuery {
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

    override suspend fun clearCart(customerId: String): Boolean = dbQuery {
        val cartItems = ServiceCartItem.find { ServiceCart.customerId eq customerId }
        if (cartItems.empty()) {
            false
        } else {
            cartItems.forEach { it.delete() }
            true
        }
    }
}