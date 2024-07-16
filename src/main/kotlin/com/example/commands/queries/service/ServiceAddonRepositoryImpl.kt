package com.example.commands.queries.service

import com.example.dao.ServiceAddOnRepository
import com.example.db.service.Service
import com.example.db.service.ServiceAddOn
import com.example.db.service.ServiceAddOns
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.service.ServiceAddOnRequest
import com.example.models.service.ServiceAddOnResponse
import io.ktor.server.plugins.*
import java.util.*

class ServiceAddonRepositoryImpl : ServiceAddOnRepository {

    override suspend fun getServiceAddons(serviceId: UUID): List<ServiceAddOnResponse> = dbQuery {
        return@dbQuery try {
            val serviceAddOns = ServiceAddOn.find { ServiceAddOns.service eq serviceId }
            if (serviceAddOns.empty()) {
                throw NotFoundException("Service add on matching that id: $serviceId does not exist")
            }
            serviceAddOns
                .map { it.toServiceAddOnResponse() }
        } catch (e: Exception) {
            when (e) {
                is NotFoundException -> throw e
                else -> throw e
            }
        }
    }

    override suspend fun createServiceAddOn(serviceId: UUID, serviceAddOn: ServiceAddOnRequest): ServiceAddOnResponse =
        dbQuery {
            val addonExists = ServiceAddOn.find { ServiceAddOns.name eq serviceAddOn.name }.singleOrNull()
            if (addonExists != null) {
                throw IllegalArgumentException("A service add-on with the name ${serviceAddOn.name} already exists")
            }
            return@dbQuery ServiceAddOn.new {
                service = Service[serviceId]
                name = serviceAddOn.name
                description = serviceAddOn.description
                price = serviceAddOn.price
                imageUrl = serviceAddOn.imageUrl
            }.toServiceAddOnResponse()
        }

    override suspend fun updateServiceAddOn(
        serviceId: UUID,
        serviceAddOnId: UUID,
        serviceAddOn: ServiceAddOnRequest
    ): Boolean = dbQuery {
        ServiceAddOn.findByIdAndUpdate(serviceAddOnId) { update ->
            update.name = serviceAddOn.name
            update.description = serviceAddOn.description
            update.price = serviceAddOn.price
            update.imageUrl = serviceAddOn.imageUrl
        }
        return@dbQuery true
    }

    override suspend fun deleteServiceAddOn(serviceAddOnId: UUID): Boolean = dbQuery {
        ServiceAddOn.findById(serviceAddOnId)
            ?.delete()
        true
    }
}