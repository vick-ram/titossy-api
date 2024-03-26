package com.example.commands.queries.message

import com.example.db.message.Message
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.room.MessageRequest
import com.example.models.room.MessageResponse
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private fun Message.toMessageResponse() = MessageResponse(
    id = this.id.value,
    text = this.message,
    username = this.username,
    timeStamp = Date.from(this.timestamp.atZone(ZoneId.systemDefault()).toInstant()),
)

suspend fun insertMessage(messageRequest: MessageRequest): MessageResponse? = dbQuery {
    return@dbQuery try {
        val message = Message.new {
            this.message = messageRequest.text
            this.username = messageRequest.username
            this.timestamp = LocalDateTime.now()
        }.toMessageResponse()
        message
    } catch (e: Exception) {
        null
    }
}

suspend fun getAllMessages(): List<MessageResponse>? = dbQuery {
    return@dbQuery try {
        Message.all().map { it.toMessageResponse() }.sortedByDescending { it.timeStamp }
    } catch (e: Exception) {
        null
    }
}