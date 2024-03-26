package com.example.commands.queries.service

import com.example.commands.customMatch
import com.example.db.service.Category
import com.example.db.service.Service
import com.example.db.service.ServiceTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.service.ServiceRequest
import com.example.models.service.ServiceResponse
import com.example.models.service.ServiceUpdate
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private fun Service.toServiceResponse() = ServiceResponse(
    serviceId = this.id.value,
    serviceName = this.name,
    category = this.category.id.value,
    description = this.description,
    price = this.price.toDouble(),
    duration = this.duration,
    imageUrl = this.imageUrl ?: "",
    createdAt = Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()),
    updatedAt = Date.from(updatedAt.atZone(ZoneId.systemDefault()).toInstant())
)

suspend fun createService(serviceRequest: ServiceRequest): ServiceResponse? = dbQuery {
    val serviceExists = ServiceTable.selectAll().where { ServiceTable.name eq serviceRequest.serviceName }.count() > 0
    if (serviceExists) {
        return@dbQuery null
    } else {
        return@dbQuery try {
            Service.new {
                this.name = serviceRequest.serviceName
                this.category = Category.findById(serviceRequest.category) ?: throw Exception("Category not found")
                this.description = serviceRequest.description
                this.price = serviceRequest.price.toBigDecimal()
                this.duration = serviceRequest.duration
                this.imageUrl = serviceRequest.imageUrl
            }.toServiceResponse()
        } catch (e: ExposedSQLException) {
            null
        }
    }
}

suspend fun getAllServices(): List<ServiceResponse> = dbQuery {
    return@dbQuery try {
        Service.all()
            .limit(50)
            .map { it.toServiceResponse() }
    } catch (e: Exception) {
        emptyList<ServiceResponse>()
    }
}

suspend fun getServiceById(serviceId: Int) = dbQuery {
    return@dbQuery try {
        Service.findById(serviceId)?.toServiceResponse()
    } catch (e: Exception) {
        null
    }
}

suspend fun getServiceByCategory(categoryId: Int): List<ServiceResponse> = dbQuery {
    return@dbQuery try {
        Service.find { ServiceTable.category eq categoryId }
            .limit(50)
            .map { it.toServiceResponse() }
    } catch (e: Exception) {
        emptyList<ServiceResponse>()
    }
}

suspend fun updateService(serviceId: Int, serviceUpdateRequest: ServiceUpdate): ServiceResponse? = dbQuery {
    return@dbQuery try {
        Service.findById(serviceId)?.apply {
            this.name = serviceUpdateRequest.serviceName
            this.category = Category.findById(serviceUpdateRequest.category)!!
            this.description = serviceUpdateRequest.description
            this.price = serviceUpdateRequest.price.toBigDecimal()
            this.duration = serviceUpdateRequest.duration
            this.imageUrl = serviceUpdateRequest.imageUrl
            this.updatedAt = LocalDateTime.now()
        }?.toServiceResponse()
    } catch (e: Exception) {
        Service.findById(serviceId)?.toServiceResponse()
    }
}
suspend fun searchService(query: String): List<ServiceResponse> = dbQuery {
    return@dbQuery try {
        ServiceTable.selectAll().where { ServiceTable.tsv.customMatch(query) }
            .map { Service.wrapRow(it).toServiceResponse()}
    } catch (e: Exception) {
        emptyList<ServiceResponse>()
    }
}

suspend fun deleteService(serviceId: Int): Boolean = dbQuery {
    return@dbQuery try {
        Service.findById(serviceId)?.delete()
        true
    } catch (e: Exception) {
        false
    }
}
