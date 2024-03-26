package com.example.routing.service

import com.example.commands.queries.service.*
import com.example.models.service.ServiceRequest
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.serviceRoutes() {
    route("/services") {
        post {
            val multipartData = call.receiveMultipart()
            var serviceRequest = ServiceRequest("", 0, "", 0.0, 0, null)
            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "name" -> serviceRequest = serviceRequest.copy(serviceName = part.value)
                            "category" -> serviceRequest = serviceRequest.copy(category = part.value.toInt())
                            "description" -> serviceRequest = serviceRequest.copy(description = part.value)
                            "price" -> serviceRequest = serviceRequest.copy(price = part.value.toDouble())
                            "duration" -> serviceRequest = serviceRequest.copy(duration = part.value.toInt())
                        }
                    }

                    is PartData.FileItem -> {
                        val fileBytes = part.streamProvider().readBytes()
                        val file = File("uploads/${part.originalFileName}")
                        file.writeBytes(fileBytes)
                        serviceRequest = serviceRequest.copy(imageUrl = file.absolutePath)
                    }

                    else -> {}
                }
                part.dispose()
            }
            createService(serviceRequest)
        }

        get("/search") {
            val query = call.request.queryParameters["query"]
            val searchProduct = query?.let { searchService(it) }
            searchProduct?.let {
                call.respond(it)
            } ?: call.respond("No services found")
        }

        get {
            val services = getAllServices()
            try {
                call.respond(services)
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            try {
                val service = id?.let { getServiceById(it) }
                service?.let {
                    call.respond(it)
                } ?: call.respond("Service not found")
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }

        get("/category") {
            val categoryId = call.request.queryParameters["category"]?.toIntOrNull()
            val services = categoryId?.let { getServiceByCategory(it) }
            services?.let {
                call.respond(it)
            } ?: call.respond("No services found")
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            try {
                val service = id?.let { deleteService(it) }
                service?.let {
                    call.respond(it)
                } ?: call.respond("Service deletion failed")
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }
    }
}