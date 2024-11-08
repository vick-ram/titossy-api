package com.example.controllers

import com.example.commons.DatabaseUtil.dbQuery
import com.example.commons.EventType
import com.example.commons.NotificationState
import com.example.db.ActivityLogEntity
import com.example.db.NotificationEntity
import com.example.models.ActivityLog
import com.example.models.Notification
import io.ktor.server.plugins.*

suspend fun logUserEvent(
    userId: String,
    event: EventType,
    eventData: String
): ActivityLog = dbQuery {
    val activityLog = ActivityLogEntity.new {
        this.userId = userId
        this.eventType = event
        this.eventData = eventData
    }
    return@dbQuery activityLog.toActivityLogs()
}

suspend fun getActivityLogs(): List<ActivityLog> = dbQuery {
    return@dbQuery ActivityLogEntity
        .all()
        .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
        .map { it.toActivityLogs() }
}

/*Notification controller*/
suspend fun getUnreadNotifications(): List<Notification> = dbQuery {
    return@dbQuery NotificationEntity
        .all()
        .filter { it.state == NotificationState.UNREAD }
        .sortedByDescending { it.createdAt }
        .map { it.toNotification() }
}

suspend fun markNotificationAsRead(notificationId: String): Boolean = dbQuery {
    val notification = NotificationEntity.findById(notificationId)
        ?: throw NotFoundException("Notification with id $notificationId could not be found")
    notification.state = NotificationState.READ
    return@dbQuery true
}