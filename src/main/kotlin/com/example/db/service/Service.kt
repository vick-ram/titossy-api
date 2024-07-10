package com.example.db.service

import com.example.db.util.CustomUUIDEntity
import com.example.db.util.CustomUUIDEntityClass
import com.example.db.util.CustomUUIDTable
import com.example.db.util.tsVector
import com.example.models.service.ServiceAddOnResponse
import com.example.models.service.ServiceResponse
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object Services : CustomUUIDTable("services") {
    val name = varchar("name", 255)
    val description = text("description")
    val price = decimal("price", 10, 2)
    val imageUrl = varchar("image_url", 255).nullable()
    val tsv = tsVector("tsv")
}

object ServiceAddOns : CustomUUIDTable("service_add_ons") {
    val service = reference("service_id", Services, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val description = text("description")
    val price = decimal("price", 10, 2)
    val imageUrl = varchar("image_url", 255).nullable()
}

class Service(id: EntityID<UUID>) : CustomUUIDEntity(id, Services) {
    companion object : CustomUUIDEntityClass<Service>(Services)

    var name by Services.name
    var description by Services.description
    var price by Services.price
    var imageUrl by Services.imageUrl
    var tsv by Services.tsv

    fun toServiceResponse() = ServiceResponse(
        id.value, name, description, price, imageUrl, createdAt, updatedAt
    )
}

class ServiceAddOn(id: EntityID<UUID>) : CustomUUIDEntity(id, ServiceAddOns) {
    companion object : CustomUUIDEntityClass<ServiceAddOn>(ServiceAddOns)

    var service by Service referencedOn ServiceAddOns.service
    var name by ServiceAddOns.name
    var description by ServiceAddOns.description
    var price by ServiceAddOns.price
    var imageUrl by ServiceAddOns.imageUrl

    fun toServiceAddOnResponse() = ServiceAddOnResponse(
        id.value, service.id.value, name, description, price, imageUrl, createdAt, updatedAt
    )
}
