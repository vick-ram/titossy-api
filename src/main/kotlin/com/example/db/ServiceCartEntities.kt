package com.example.db

import com.example.commons.CustomUUIDEntity
import com.example.commons.CustomUUIDEntityClass
import com.example.commons.CustomUUIDTable
import com.example.models.ServiceAddOnCartItemResponse
import com.example.models.ServiceCartItemResponse
import com.example.models.ServiceCartResponse
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import java.math.BigDecimal
import java.util.*

object ServiceCart : CustomUUIDTable("service_carts") {
    val customerId = reference(
        "Customer_id",
        CustomerTable,
        onDelete = ReferenceOption.CASCADE
    )
    val serviceId = reference(
        "service_id",
        Services,
        onDelete = ReferenceOption.CASCADE
    ).nullable()
    val serviceAddonId = reference(
        "service_addon_id",
        ServiceAddOns,
        onDelete = ReferenceOption.CASCADE
    ).nullable()
    val quantity = integer("quantity").default(1)
}

class ServiceCartItem(id: EntityID<UUID>) : CustomUUIDEntity(id, ServiceCart) {

    companion object : CustomUUIDEntityClass<ServiceCartItem>(ServiceCart)

    var customer by Customer referencedOn ServiceCart.customerId
    var service by Service optionalReferencedOn ServiceCart.serviceId
    var serviceAddon by ServiceAddOn optionalReferencedOn ServiceCart.serviceAddonId
    var quantity by ServiceCart.quantity

    val totalAmount: BigDecimal
        get() {
            val servicePrice = service
                ?.price
                ?.times(quantity.toBigDecimal())
                ?: BigDecimal.ZERO
            val addonPrice = serviceAddon
                ?.price
                ?.times(quantity.toBigDecimal())
                ?: BigDecimal.ZERO
            return servicePrice.add(addonPrice)
        }

    fun toServiceCartResponse() = ServiceCartResponse(
        customerId = customer.id.value,
        service = service?.let {
            ServiceCartItemResponse(
                id = it.id.value,
                name = it.name,
                price = it.price,
                thumbnail = it.imageUrl,
                quantity = quantity
            )
        },
        addOns = serviceAddon?.let {
            ServiceAddOnCartItemResponse(
                id = it.id.value,
                name = it.name,
                price = it.price,
                thumbnail = it.imageUrl,
                quantity = quantity
            )
        },
        total = totalAmount
    )
}