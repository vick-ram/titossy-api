package com.example.exceptions

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

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
