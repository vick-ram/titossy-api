package com.example.routing

import com.example.commons.*
import com.example.controllers.ServiceAddonRepositoryImpl
import com.example.controllers.ServiceRepositoryImpl
import com.example.dao.ServiceAddOnRepository
import com.example.dao.ServiceRepository
import com.example.models.ServiceAddOnRequest
import com.example.models.ServiceAddonUpdate
import com.example.models.ServiceRequest
import com.example.models.ServiceUpdate
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

fun Route.serviceAddonRoutes(
    client: HttpClient,
    imgHippoUrl: String,
    imgHippoApiKey: String,
    imgBBUrl: String,
    imgBBApiKey: String,
//    apiKey: String,
//    url: String
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
                        try {
                            val createImageService = ImageService(client,tempFile, imgHippoUrl, imgHippoApiKey, imgBBUrl, imgBBApiKey)
                            imageUrl = createImageService.uploadImage()
//                            imageUrl = uploadImageToHippo(tempFile, client, apiKey, url)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            call.respond(
                                ApiResponse.error(
                                    HttpStatusCode.InternalServerError,
                                    e.message
                                )
                            )
                            return@forEachPart
                        } finally {
                            if (tempFile.exists()) {
                                tempFile.delete()
                            }
                        }
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
                    if (part.name == "imageUrl") {
                        val fileBytes = part.streamProvider().readBytes()
                        val tempFile = File.createTempFile("upload-", part.originalFileName)
                        tempFile.writeBytes(fileBytes)
                        try {
                            val updateImageService = ImageService(client,tempFile, imgHippoUrl, imgHippoApiKey, imgBBUrl, imgBBApiKey)
                            imageUrl = updateImageService.uploadImage()
//                            imageUrl = uploadImageToHippo(tempFile, client, apiKey, url)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            call.respond(
                                ApiResponse.error(
                                    HttpStatusCode.InternalServerError,
                                    e.message
                                )
                            )
                        } finally {
                            if (tempFile.exists()) {
                                tempFile.delete()
                            }
                        }
                    }
                }

                else -> {}
            }
            part.dispose()
        }

        try {
            val serviceAddonUpdate = ServiceAddonUpdate(
                name = name!!,
                description = description!!,
                price = price!!,
                imageUrl = imageUrl!!
            )
            dao.updateServiceAddOn(
                serviceId,
                serviceAddOnId,
                serviceAddonUpdate
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

fun Route.serviceRoutes(
    client: HttpClient,
    imgHippoUrl: String,
    imgHippoApiKey: String,
    imgBBUrl: String,
    imgBBApiKey: String,
//    apiKey: String,
//    url: String
) {
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
                    if (part.name == "imageUrl") {
                        val fileBytes = part.streamProvider().readBytes()
                        val tempFile = File.createTempFile("upload-", part.originalFileName)
                        tempFile.writeBytes(fileBytes)
                        try {
                            val createImageService = ImageService(client, tempFile, imgHippoUrl, imgHippoApiKey, imgBBUrl, imgBBApiKey)
                            imageUrl = createImageService.uploadImage()
//                            imageUrl = uploadImageToHippo(tempFile, client, apiKey, url)
                            println("Image url from imghippo: $imageUrl")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            call.respond(
                                ApiResponse.error(
                                    HttpStatusCode.InternalServerError,
                                    "Error uploading image ${e.localizedMessage}"
                                )
                            )
                            return@forEachPart
                        } finally {
                            if (tempFile.exists()) {
                                tempFile.delete()
                            }
                        }
                    }
                }

                else -> {}
            }
            part.dispose()
        }
        try {
            val service = ServiceRequest(
                name = name!!,
                description = description!!,
                price = price!!,
                imageUrl = imageUrl!!
            )
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

    put<Service.Id, ServiceUpdate> { param, _ ->
        val updateServiceMultipart = call.receiveMultipart()

        var name: String? = null
        var description: String? = null
        var price: BigDecimal? = null
        var imageUrl: String? = null

        updateServiceMultipart.forEachPart { part ->
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
                        val updateImageService = ImageService(client, tempFile, imgHippoUrl, imgHippoApiKey, imgBBUrl, imgBBApiKey)
                        imageUrl = updateImageService.uploadImage()
//                        imageUrl = uploadImageToHippo(tempFile, client, apiKey, url)
                        tempFile.delete()
                    }
                }

                else -> {}
            }
            part.dispose()
        }

        try {
            val service = ServiceUpdate(name!!, description!!, price!!, imageUrl!!)
            dao.updateService(param.id, service)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    null,
                    "Service updated successfully"
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