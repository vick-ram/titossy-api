package com.example.models.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationMessage(
    val en: String
)

@Serializable
data class Notification(
    @SerialName("include_external_user_ids")
    val externalUserIds: List<String>? = null,
    @SerialName("included_segments")
    val includedSegments: List<String>,
    val contents: NotificationMessage,
    val headings: NotificationMessage,
    @SerialName("app_id")
    val appId: String
)