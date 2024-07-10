package com.example.routing.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.Serializable
import java.io.File
import java.util.*

fun storeServiceImage(fileBytes: ByteArray, part: PartData.FileItem): String {
    val fileName = part.originalFileName ?: "file.jpg"
    val directory = File("uploads/service")
    directory.mkdirs()

    val file = File(directory, fileName)
    file.writeBytes(fileBytes)

    return "/uploads/service/$fileName"
}

fun storeAddonsImage(fileBytes: ByteArray, part: PartData.FileItem): String {
    val fName = part.originalFileName ?: "addon.jpg"
    val directory = File("uploads/addon")
    directory.mkdirs()

    val file = File(directory, fName)
    file.writeBytes(fileBytes)

    return "/uploads/addon/$fName"
}

fun storeProductImage(fileBytes: ByteArray, part: PartData.FileItem): String {
    val fName = part.originalFileName ?: "product.jpg"
    val directory = File("uploads/product")
    directory.mkdirs()

    val file = File(directory, fName)
    file.writeBytes(fileBytes)

    return "/uploads/product/$fName"
}

fun storeProfilePic(file: File): String {
    val directory = File("uploads/profile")
    val fileName = UUID.nameUUIDFromBytes(file.readBytes()).toString()
    directory.mkdirs()

    File(directory, fileName).writeBytes(file.readBytes())
    return "/uploads/profile/$fileName"
}

fun deleteServiceImage(fileBytes: ByteArray, part: PartData.FileItem) {
    val imageUrl = storeServiceImage(fileBytes, part)
    val fileName = imageUrl.substringAfterLast("/")
    val directory = File("uploads/service")
    val file = File(directory, fileName)
    file.delete()
}

fun deleteAddOnImage(fileBytes: ByteArray, part: PartData.FileItem) {
    val imageUrl = storeAddonsImage(fileBytes, part)
    val fileName = imageUrl.substringAfterLast("/")
    val directory = File("uploads/addon")
    val file = File(directory, fileName)
    file.delete()
}

fun deleteProductImage(fileBytes: ByteArray, part: PartData.FileItem) {
    val imageUrl = storeProductImage(fileBytes, part)
    val fileName = imageUrl.substringAfterLast("/")
    val directory = File("uploads/product")
    val file = File(directory, fileName)
    file.delete()
}

fun deleteProfilePic(fileBytes: ByteArray) {
    val directory = File("uploads/profile")
    val fileName = UUID.nameUUIDFromBytes(fileBytes).toString()
    val file = File(directory, fileName)
    file.delete()
}

suspend fun uploadImageToHippo(file: File, client: HttpClient): String {
    return try {
        val completeUrl = "${System.getenv("IMGHIPPO_URL")}?api_key=${System.getenv("IMGHIPPO_API_KEY")}"
        val response = client.submitFormWithBinaryData(
            url = completeUrl,
            formData = formData {
                append("file", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                })
            }
        ).body<ImgHippoResponse>()
        response.data.view_url
    } catch (e: Exception) {
        println("Error uploading to imgHippo: ${e.localizedMessage}")
        throw e
    }
}

@Serializable
data class ImgHippoResponse(
    val success: Boolean,
    val status: Int,
    val data: ImgHippoData
)

@Serializable
data class ImgHippoData(
    val name: String,
    val url: String,
    val view_url: String,
    val created_at: String,
)
