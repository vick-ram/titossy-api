package com.example.db

import com.example.commons.LocalDateTimeSerializer
import com.example.commons.UUIDSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.*

object Messages: UUIDTable("messages") {
    val message = text("message")
    val sender = varchar("sender", 255)
    val receiver = varchar("receiver", 255)
    val timestamp = datetime("timestamp").default(LocalDateTime.now())
}

class MessageEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<MessageEntity>(Messages)

    var message by Messages.message
    var sender by Messages.sender
    var receiver by Messages.receiver
    var timestamp by Messages.timestamp

    fun toMessageResponse() = MessageResponse(
        id = id.value,
        message = message,
        sender = sender,
        receiver = receiver,
        timestamp = timestamp
    )
}


@Serializable
data class MessageResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val message: String,
    val sender: String,
    val receiver: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: LocalDateTime
)