package com.example.db

import com.example.commons.*
import com.example.models.ActivityLog
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object ActivityLogs: StringUUIDTable("activity_logs") {
    val userId = varchar("user_id", 100).nullable()
    val timestamp = datetime("timestamp").default(LocalDateTime.now())
    val eventType = enumerationByName<EventType>("event_type", 100)
    val eventData = text("event_data").nullable()
}

class ActivityLogEntity(id: EntityID<String>) : StringUUIDEntity(id, ActivityLogs) {
    companion object : StringUUIDEntityClass<ActivityLogEntity>(ActivityLogs)

    var userId by ActivityLogs.userId
    var timestamp by ActivityLogs.timestamp
    var eventType by ActivityLogs.eventType
    var eventData by ActivityLogs.eventData

    fun toActivityLogs() = ActivityLog(
        id = id.value,
        userId = userId,
        timestamp = timestamp,
        eventType = eventType,
        eventData = eventData
    )
}