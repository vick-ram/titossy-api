package com.example.db.util

import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

object Notifications : CustomUUIDTable("notifications") {
    val title = text("title")
    val message = text("message")
    val read = bool("read").default(false)
}

class Notification(id: EntityID<UUID>) : CustomUUIDEntity(id, Notifications) {
    companion object : CustomUUIDEntityClass<Notification>(Notifications)

    var title by Notifications.title
    var message by Notifications.message
    var read by Notifications.read

    fun toNotificationResponse() = NotificationResponse(
        notificationId = id.value.toString(),
        title = title,
        message = message,
        read = read,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}


data class NotificationResponse(
    val notificationId: String,
    val title: String,
    val message: String,
    val read: Boolean,
    val createdAt: String,
    val updatedAt: String
)