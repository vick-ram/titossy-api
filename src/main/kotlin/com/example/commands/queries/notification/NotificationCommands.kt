package com.example.commands.queries.notification

import com.example.db.notification.NotificationEntity
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.notification.Notification

suspend fun saveNotification(notification: Notification) = dbQuery {
    return@dbQuery try {
        NotificationEntity.new {
            externalUserIds = notification.externalUserIds?.joinToString(",")
            includedSegments = notification.includedSegments.joinToString(",")
            contents = notification.contents.en
            headings = notification.headings.en
            appId = notification.appId
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}