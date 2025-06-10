package com.example.commons

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.*
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

suspend fun uploadImageToHippo(
    file: File,
    client: HttpClient,
    apiKey: String,
    url: String
): String {
    return try {
        val completeUrl = "$url?api_key=$apiKey"
        val response = client.submitFormWithBinaryData(
            url = completeUrl,
            formData = formData {
                append("file", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                })
            }
        ).body<ImgHippoResponse>()

        response.data.viewUrl
    } catch (e: Exception) {
        println("Error uploading to imgHippo: ${e.localizedMessage}")
        throw Exception("Failed to upload image: HTTP ${e.localizedMessage}")
    }
}

@Serializable
data class ImgHippoResponse(
    val success: Boolean,
    val status: Int,
    val message: String,
    val data: ImgHippoData
)

@Serializable
data class ImgHippoData(
    val title: String,
    val url: String,
    @SerialName("view_url") val viewUrl: String,
    val extension: String,
    val size: Long,
    @SerialName("created_at") val createdAt: String,
)

class ImageService(
    private val client: HttpClient,
    private val file: File,
    private val imgHippoUrl: String,
    private val imgHippoApiKey: String,
    private val imgBBUrl: String,
    private val imgBbApiKey: String,
) {
    suspend fun uploadImage(): String {
        return try {
            uploadToImgHippo()
        } catch (e: Exception) {
            uploadToImgBB()
        }
    }

    private suspend fun uploadToImgHippo(): String {
        val response = client.submitFormWithBinaryData(
            url = "${imgHippoUrl}?api_key=$imgHippoApiKey",
            formData = formData {
                append("file", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                })
            }
        ).body<ImgHippoResponse>()

        return response.data.viewUrl
    }

    private suspend fun uploadToImgBB(): String {
        val response = client.submitFormWithBinaryData(
            url = "${imgBBUrl}?key=$imgBbApiKey",
            formData = formData {
                append("image", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                })
            }
        ).body<ImgBBResponse>()

        return response.data.displayUrl
    }
}


@Serializable
data class ImgBBResponse(
    val data: ImageBBData,
    val success: Boolean,
    val status: Int,
)

@Serializable
data class ImageBBData(
    val id: String,
    val title: String,
    @SerialName("url_viewer") val urlViewer: String,
    val url: String,
    @SerialName("display_url") val displayUrl: String,
    val width: String,
    val height: String,
    val size: String,
    val time: String,
    val expiration: String,
    val image: ImgBBImage,
    val thumb: ImgBBImage,
    val medium: ImgBBImage? = null,
    @SerialName("delete_url") val deleteUrl: String,
)

@Serializable
data class ImgBBImage(
    @SerialName("filename") val fileName: String,
    val name: String,
    val mime: String,
    val extension: String,
    val url: String,
)

