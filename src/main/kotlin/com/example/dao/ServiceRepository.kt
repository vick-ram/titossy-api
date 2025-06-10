package com.example.dao

import com.example.db.Service
import com.example.models.ServiceRequest
import com.example.models.ServiceResponse
import com.example.models.ServiceUpdate
import java.util.*

interface ServiceRepository {
    suspend fun getFilteredServices(filter: (Service) -> Boolean): List<ServiceResponse>
    suspend fun createService(service: ServiceRequest): ServiceResponse
    suspend fun updateService(serviceId: UUID, service: ServiceUpdate): Boolean
    suspend fun deleteService(serviceId: UUID): Boolean

    suspend fun searchService(search: String): List<ServiceResponse>
}