package com.example.models

import com.example.commons.EventType
import com.example.commons.LocalDateTimeSerializer
import com.example.commons.shortUUID
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ActivityLog(
    val id: String = shortUUID(),
    val userId: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: LocalDateTime,
    val eventType: EventType?,
    val eventData: String?
)