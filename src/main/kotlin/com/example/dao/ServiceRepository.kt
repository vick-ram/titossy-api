package com.example.dao

import com.example.db.service.Service
import com.example.models.service.ServiceRequest
import com.example.models.service.ServiceResponse
import java.util.*

interface ServiceRepository {
    suspend fun getFilteredServices(filter: (Service) -> Boolean): List<ServiceResponse>
    suspend fun createService(service: ServiceRequest): ServiceResponse
    suspend fun updateService(serviceId: UUID, service: ServiceRequest): Boolean
    suspend fun deleteService(serviceId: UUID): Boolean

    suspend fun searchService(search: String): List<ServiceResponse>
}