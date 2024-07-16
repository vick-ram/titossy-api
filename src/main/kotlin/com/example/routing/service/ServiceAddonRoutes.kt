package com.example.routing.service

import com.example.commands.queries.service.ServiceAddonRepositoryImpl
import com.example.dao.ServiceAddOnRepository
import com.example.exceptions.ApiResponse
import com.example.models.service.ServiceAddOnRequest
import com.example.routing.util.SERVICE_ADDON
import com.example.routing.util.SERVICE_ADDON_ID
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.math.BigDecimal
import java.util.*

fun Route.serviceAddonRoutes(
    client: HttpClient,
    apiKey: String,
    url: String
) {
    val dao: ServiceAddOnRepository = ServiceAddonRepositoryImpl()

    post(SERVICE_ADDON) {
        val serviceAddonMultipart = call.receiveMultipart()
        val serviceId = UUID.fromString(call.parameters["id"])

        var name: String? = null
        var description: String? = null
        var price: BigDecimal? = null
        var imageUrl: String? = null

        serviceAddonMultipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "name" -> name = part.value
                        "description" -> description = part.value
                        "price" -> price = part.value.toBigDecimal()
                    }
                }

                is PartData.FileItem -> {
                    if (part.name == "imageUrl") {
                        val fileBytes = part.streamProvider().readBytes()
                        val tempFile = File.createTempFile("upload-", part.originalFileName)
                        tempFile.writeBytes(fileBytes)
                        imageUrl = uploadImageToHippo(tempFile, client, apiKey, url)
                        tempFile.delete()
                    }
                }

                else -> {}
            }
            part.dispose()
        }

        try {
            val serviceAddonRequest = ServiceAddOnRequest(name!!, description!!, price!!, imageUrl!!)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Created,
                    dao.createServiceAddOn(serviceId, serviceAddonRequest),
                    null
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

    get(SERVICE_ADDON) {
        try {
            val serviceId = UUID.fromString(call.parameters["id"])
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    dao.getServiceAddons(serviceId),
                    null
                )
            )
        } catch (e: Exception) {
            when (e) {
                is NotFoundException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.NotFound,
                        e.message
                    )
                )

                else -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.InternalServerError,
                        e.message
                    )
                )
            }
        }
    }

    put(SERVICE_ADDON_ID) {

        val serviceAddOnMultipartUpdate = call.receiveMultipart()
        val serviceId = UUID.fromString(call.parameters["id"])
        val serviceAddOnId = UUID.fromString(call.parameters["addonId"])

        var name: String? = null
        var description: String? = null
        var price: BigDecimal? = null
        var imageUrl: String? = null

        serviceAddOnMultipartUpdate.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "name" -> name = part.value
                        "description" -> description = part.value
                        "price" -> price = part.value.toBigDecimal()
                    }
                }

                is PartData.FileItem -> {
                    val fileBytes = part.streamProvider().readBytes()
                    val tempFile = File.createTempFile("upload-", part.originalFileName)
                    tempFile.writeBytes(fileBytes)
                    imageUrl = uploadImageToHippo(tempFile, client, apiKey, url)
                    tempFile.delete()
                }

                else -> {}
            }
            part.dispose()
        }

        try {
            val serviceAddonUpdate = ServiceAddOnRequest(name!!, description!!, price!!, imageUrl!!)
            dao.updateServiceAddOn(serviceId, serviceAddOnId, serviceAddonUpdate)
        } catch (e: Exception) {
            call.respond(
                ApiResponse.error(
                    HttpStatusCode.Conflict,
                    e.message
                )
            )
        }
    }

    delete(SERVICE_ADDON_ID) {
        try {
            dao.deleteServiceAddOn(UUID.fromString(call.parameters["addonId"]))
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.NoContent,
                    null,
                    "Service add on deleted successfully"
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