package com.example.routing.service

import com.example.commands.queries.service.ServiceRepositoryImpl
import com.example.dao.ServiceRepository
import com.example.exceptions.ApiResponse
import com.example.models.service.ServiceRequest
import com.example.routing.util.Service
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.math.BigDecimal
import java.util.*

fun Route.serviceRoutes(client: HttpClient) {
    val dao: ServiceRepository = ServiceRepositoryImpl()

    post("/api/service") {

        val serviceMultipart = call.receiveMultipart()

        var name: String? = null
        var description: String? = null
        var price: BigDecimal? = null
        var imageUrl: String? = null

        serviceMultipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "name" -> name = part.value
                        "description" -> description = part.value
                        "price" -> price = part.value.toBigDecimal()
                    }
                }

                is PartData.FileItem -> {
                    val ext = part.originalFileName?.let { it1 -> File(it1).extension }
                    val file = File("uploads/service/${UUID.randomUUID()}.$ext")
                    part.streamProvider().use { its ->
                        file.outputStream().buffered().use {
                            its.copyTo(it)
                        }
                    }
                    imageUrl = uploadImageToHippo(file, client)
                    file.delete()
                }

                else -> {}
            }
            part.dispose()
        }
        try {
            val service = ServiceRequest(name!!, description!!, price!!, imageUrl!!)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Created,
                    dao.createService(service),
                    "Service created successfully"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> {
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.Conflict,
                            e.message
                        )
                    )
                }

                is BadRequestException -> {
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            "Something went wrong"
                        )
                    )
                }
            }
        }
    }

    get<Service> { param ->
        when {
            param.name != null -> {
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        dao.getFilteredServices { it.name.contains(param.name, ignoreCase = true) },
                        null
                    )
                )
            }

            param.search != null -> {
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        dao.searchService(param.search),
                        null
                    )
                )
            }

            else -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    dao.getFilteredServices { true },
                    null
                )
            )
        }
    }

    get<Service.Id> { param ->
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    dao.getFilteredServices { it.id.value == param.id }.firstOrNull(),
                    null
                )
            )
        } catch (e: Exception) {
            call.respond(
                ApiResponse.error(
                    HttpStatusCode.NotFound,
                    e.message
                )
            )
        }
    }

    put<Service.Id, ServiceRequest> { param, serviceRequest ->
        try {
            dao.updateService(param.id, serviceRequest)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Accepted,
                    null,
                    "Service successfully updated"
                )
            )
        } catch (e: Exception) {
            call.respond(
                ApiResponse.error(
                    HttpStatusCode.Conflict,
                    e.message
                )
            )
        }
    }

    delete<Service.Id> { param ->
        try {
            dao.deleteService(param.id)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.NoContent,
                    null,
                    "Service successfully deleted"
                )
            )
        } catch (e: Exception) {
            call.respond(
                ApiResponse.error(
                    HttpStatusCode.BadRequest,
                    e.message
                )
            )
        }
    }
}