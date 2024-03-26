package com.example.models.room

import com.example.models.util.DateSerializer
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class MessageRequest(
    val text: String,
    val username: String,
    @Serializable(with = DateSerializer::class)
    val timeStamp: Date
)

@Serializable
data class MessageResponse(
    val id: Int,
    val text: String,
    val username: String,
    @Serializable(with = DateSerializer::class)
    val timeStamp: Date
)