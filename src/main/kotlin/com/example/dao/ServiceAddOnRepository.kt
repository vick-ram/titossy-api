package com.example.dao

import com.example.models.ServiceAddOnRequest
import com.example.models.ServiceAddOnResponse
import com.example.models.ServiceAddonUpdate
import java.util.*

interface ServiceAddOnRepository {
    suspend fun getServiceAddons(serviceId: UUID): List<ServiceAddOnResponse>
    suspend fun createServiceAddOn(serviceId: UUID, serviceAddOn: ServiceAddOnRequest): ServiceAddOnResponse
    suspend fun updateServiceAddOn(serviceId: UUID, serviceAddOnId: UUID, serviceAddOn: ServiceAddonUpdate): Boolean
    suspend fun deleteServiceAddOn(serviceAddOnId: UUID): Boolean
}