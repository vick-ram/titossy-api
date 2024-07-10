package com.example.commands.queries.service

import com.example.commands.customMatch
import com.example.dao.ServiceRepository
import com.example.db.service.Service
import com.example.db.service.Services
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.service.ServiceRequest
import com.example.models.service.ServiceResponse
import org.jetbrains.exposed.sql.selectAll
import java.util.*

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

    override suspend fun updateService(serviceId: UUID, service: ServiceRequest): Boolean = dbQuery {
        Service.findByIdAndUpdate(serviceId) { update ->
            update.name = service.name
            update.description = service.description
            update.price = service.price
            update.imageUrl = service.imageUrl
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
