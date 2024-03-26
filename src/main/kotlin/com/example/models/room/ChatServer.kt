package com.example.models.room

import com.example.commands.queries.message.insertMessage
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChatServer {
    private val members = ConcurrentHashMap<String, WebSocketSession>()

    fun memberJoin(memberId: String, socket: WebSocketSession) {
        members[memberId] = socket
    }
    suspend fun sendMessages(username: String, message: String) {
        val newMessage = insertMessage(
            MessageRequest(
                username = username,
                text = message,
                timeStamp = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
            )
        )
        newMessage?.let { broadcastMessage(it) }
    }

    private suspend fun broadcastMessage(messageResponse: MessageResponse) {
        members.values.forEach { socket ->
            val parsedMessage = Json.encodeToString(messageResponse)
            socket.send(Frame.Text(parsedMessage))
        }
    }

    suspend fun tryDisconnect(username: String) {
        members[username]?.close()
        if (members.containsKey(username)) {
            members.remove(username)
        }
    }
}