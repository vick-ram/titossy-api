package com.example.routing

import com.example.commons.ApiResponse
import com.example.commons.Product
import com.example.commons.uploadImageToHippo
import com.example.controllers.ProductRepositoryImpl
import com.example.dao.ProductRepository
import com.example.models.ProductRequest
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.math.BigDecimal

fun Route.productRoutes(
    client: HttpClient,
    apiKey: String,
    url: String
) {
    val productRepository: ProductRepository = ProductRepositoryImpl()

    post("/api/product") {
        val productMultipart = call.receiveMultipart()

        var name: String? = null
        var description: String? = null
        var price: BigDecimal? = null
        var image: String? = null
        var stock: Int? = null
        var reorderLevel: Int? = null

        productMultipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "name" -> name = part.value
                        "description" -> description = part.value
                        "price" -> price = part.value.toBigDecimalOrNull()
                        "stock" -> stock = part.value.toIntOrNull()
                        "reorderLevel" -> reorderLevel = part.value.toIntOrNull()
                    }
                }

                is PartData.FileItem -> {
                    if (part.name == "image") {
                        val fileBytes = part.streamProvider().readBytes()
                        val tempFile = File.createTempFile(
                            "upload-",
                            part.originalFileName
                        )
                        tempFile.writeBytes(fileBytes)
                        try {
                            image = uploadImageToHippo(tempFile, client, apiKey, url)
                        }catch (e: Exception) {
                            e.printStackTrace()
                            call.respond(
                                ApiResponse.error(
                                    HttpStatusCode.InternalServerError,
                                    "Failed to upload image"
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
            val productRequest = ProductRequest(
                name = name!!,
                description = description!!,
                unitPrice = price!!,
                image = image,
                stock = stock!!,
                reorderLevel = reorderLevel!!
            )
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Created,
                    productRepository.addProduct(productRequest),
                    "Product created successfully"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
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

    get<Product> { query ->
        try {
            when {

                query.search != null -> {
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.OK,
                            productRepository.searchProduct(query.search),
                            null
                        )
                    )
                }

                else -> {
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.OK,
                            productRepository.getAllProducts(),
                            null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            call.respond(
                ApiResponse.error(
                    HttpStatusCode.BadRequest,
                    e.message
                )
            )
        }
    }

    put<Product.Id, ProductRequest> { param, _ ->

        val productUpdateMultipart = call.receiveMultipart()

        var name: String? = null
        var description: String? = null
        var price: BigDecimal? = null
        var image: String? = null
        var stock: Int? = null
        var reorderLevel: Int? = null

        productUpdateMultipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "name" -> name = part.value
                        "description" -> description = part.value
                        "price" -> price = part.value.toBigDecimalOrNull()
                        "stock" -> stock = part.value.toIntOrNull()
                        "reorderLevel" -> reorderLevel = part.value.toIntOrNull()
                    }
                }

                is PartData.FileItem -> {
                    if (part.name == "image") {
                        val fileBytes = part.streamProvider().readBytes()
                        val tempFile = File.createTempFile(
                            "upload-",
                            part.originalFileName
                        )
                        tempFile.writeBytes(fileBytes)
                        image = uploadImageToHippo(tempFile, client, apiKey, url)
                        tempFile.delete()
                    }
                }

                else -> {}
            }
            part.dispose()
        }

        try {
            val productRequest = ProductRequest(
                name = name!!,
                description = description!!,
                unitPrice = price!!,
                image = image,
                stock = stock!!,
                reorderLevel = reorderLevel!!
            )
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    productRepository.updateProduct(
                        param.id,
                        productRequest
                    ),
                    "Product updated successfully"
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

    patch<Product.Id, Int> { param, quantity ->
        try {
            productRepository.updateProductQuantity(
                param.id,
                quantity
            )
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    null,
                    "Product successfully updated"
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

    delete<Product.Id> { param ->
        productRepository.deleteProduct(param.id)
        ApiResponse.success(
            HttpStatusCode.NoContent,
            null,
            "Product deleted successfully"
        )
    }
}