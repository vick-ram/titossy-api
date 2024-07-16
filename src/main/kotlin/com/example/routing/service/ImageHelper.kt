package com.example.routing.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
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
    @SerialName("view_url") val viewUrl: String,
    @SerialName("created_at")val createdAt: String,
)
