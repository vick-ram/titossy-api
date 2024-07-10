package com.example.dao

import com.example.db.service.ServiceAddOn
import com.example.models.service.ServiceAddOnRequest
import com.example.models.service.ServiceAddOnResponse
import java.util.*

interface ServiceAddOnRepository {
    suspend fun getServiceAddons(serviceId: UUID): List<ServiceAddOnResponse>
    suspend fun createServiceAddOn(serviceId: UUID, serviceAddOn: ServiceAddOnRequest): ServiceAddOnResponse
    suspend fun updateServiceAddOn(serviceId: UUID, serviceAddOnId: UUID, serviceAddOn: ServiceAddOnRequest): Boolean
    suspend fun deleteServiceAddOn(serviceAddOnId: UUID): Boolean
}