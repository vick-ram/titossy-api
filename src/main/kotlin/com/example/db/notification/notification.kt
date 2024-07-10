package com.example.db.notification

import com.example.models.notification.Notification
import com.example.models.notification.NotificationMessage
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object NotificationTable : IntIdTable("notifications") {
    val externalUserIds = text("external_user_ids", eagerLoading = true).nullable()
    val includedSegments = text("included_segments", eagerLoading = true)
    val contents = text("contents", eagerLoading = true)
    val headings = text("headings", eagerLoading = true)
    val appId = text("app_id", eagerLoading = true)
}

class NotificationEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<NotificationEntity>(NotificationTable)

    var externalUserIds by NotificationTable.externalUserIds
    var includedSegments by NotificationTable.includedSegments
    var contents by NotificationTable.contents
    var headings by NotificationTable.headings
    var appId by NotificationTable.appId

    fun toNotification() = Notification(
        externalUserIds = externalUserIds?.split(",")?.map { it.trim() },
        includedSegments = includedSegments.split(",").map { it.trim() },
        contents = NotificationMessage(en = contents),
        headings = NotificationMessage(en = headings),
        appId = appId
    )
}