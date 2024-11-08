package com.example.commons

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    val status: String,
    val statusCode: Int? = null,
    val data: T? = null,
    val message: String? = null,
    val error: T? = null
)

object ApiResponse {
    fun <T> success(statusCode: HttpStatusCode?, data: T, message: String?) =
        Response(status = "success", statusCode = statusCode?.value, data = data, message = message)

    fun <T> error(statusCode: HttpStatusCode?, error: T? = null) =
        Response(status = "error", statusCode = statusCode?.value, error = error)
}
