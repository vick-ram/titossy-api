package com.example.models

import com.example.commons.LocalDateTimeSerializer
import com.example.commons.NotificationState
import com.example.commons.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Notification(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val message: String,
    val bookingId: String,
    val state: NotificationState = NotificationState.UNREAD,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
