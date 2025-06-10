package com.example.controllers

import com.example.commons.DatabaseUtil.dbQuery
import com.example.commons.customMatch
import com.example.dao.ServiceAddOnRepository
import com.example.dao.ServiceRepository
import com.example.db.Service
import com.example.db.ServiceAddOn
import com.example.db.ServiceAddOns
import com.example.db.Services
import com.example.models.ServiceAddOnRequest
import com.example.models.ServiceAddOnResponse
import com.example.models.ServiceAddonUpdate
import com.example.models.ServiceRequest
import com.example.models.ServiceResponse
import com.example.models.ServiceUpdate
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.selectAll
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
        serviceAddOn: ServiceAddonUpdate
    ): Boolean = dbQuery {
        ServiceAddOn.findByIdAndUpdate(serviceAddOnId) { update ->
            serviceAddOn.name?.let { update.name }
            serviceAddOn.description?.let { update.description }
            serviceAddOn.price?.let { update.price }
            serviceAddOn.imageUrl?.let { update.imageUrl }
        }
        return@dbQuery true
    }

    override suspend fun deleteServiceAddOn(serviceAddOnId: UUID): Boolean = dbQuery {
        ServiceAddOn.findById(serviceAddOnId)
            ?.delete()
        true
    }
}

class ServiceRepositoryImpl : ServiceRepository {

    override suspend fun getFilteredServices(filter: (Service) -> Boolean): List<ServiceResponse> {
        return dbQuery {
            return@dbQuery Service.all()
                .limit(20)
                .filter { filter(it) }
                .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
                .map { it.toServiceResponse() }
        }
    }

    override suspend fun createService(service: ServiceRequest): ServiceResponse = dbQuery {
        val serviceExists = Service.find { Services.name eq service.name }.singleOrNull()
        if (serviceExists != null) {
            throw IllegalArgumentException("A service with the name ${service.name} already exists")
        }
        return@dbQuery Service.new {
            this.name = service.name
            this.description = service.description
            this.price = service.price
            this.imageUrl = service.imageUrl
            this.tsv = "to_tsvector('english', '${this.name} ${this.description}')"
        }.toServiceResponse()
    }

    override suspend fun updateService(serviceId: UUID, service: ServiceUpdate): Boolean = dbQuery {
        Service.findByIdAndUpdate(serviceId) { update ->
            service.name?.let { update.name }
            service.description?.let { update.description }
            service.price?.let { update.price }
            service.imageUrl?.let { update.imageUrl }
        }
        return@dbQuery true
    }

    override suspend fun deleteService(serviceId: UUID): Boolean = dbQuery {
        Service.findById(serviceId)?.delete()
        return@dbQuery true
    }

    override suspend fun searchService(search: String): List<ServiceResponse> = dbQuery {
        val services = Services
            .selectAll()
            .where { Services.tsv.customMatch(search) }
            .map { Service.wrapRow(it).toServiceResponse() }
            .toList()
            .sortedByDescending { it.name.coerceAtLeast(it.description) }

        if (services.isEmpty()) {
            throw IllegalArgumentException("No services found matching the search query: $search")
        }
        return@dbQuery services
    }
}