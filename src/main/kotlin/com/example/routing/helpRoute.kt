package com.example.routing

import com.example.commons.ApiResponse
import com.example.commons.decodeJwt
import com.example.commons.makeJWTVerifier
import com.example.db.MessageEntity
import com.example.db.MessageResponse
import com.example.db.Messages
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.ehcache.impl.internal.concurrent.ConcurrentHashMap
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction

val chatRoom = ConcurrentHashMap<String, MutableList<DefaultWebSocketSession>>()

suspend fun DefaultWebSocketSession.handlePrivateChat(userId: String, partnerId: String) {
    val roomId = if (userId < partnerId) "$userId-$partnerId" else "$partnerId-$userId"
    chatRoom.getOrPut(roomId) { mutableListOf() }.add(this)

    try {
        for (frame in incoming) {
            if (frame is Frame.Text) {
                val receivedText = frame.readText()
                saveMessageToDatabase(userId, partnerId, receivedText)
                chatRoom[roomId]?.forEach { session ->
                    if (session != this) {
                        session.send(Frame.Text(receivedText))
                    }
                }
            }
        }
    } finally {
        chatRoom[roomId]?.remove(this)
        if (chatRoom[roomId]?.isEmpty() == true) {
            chatRoom.remove(roomId)
        }
    }
}

fun saveMessageToDatabase(sender: String, receiver: String, message: String) {
    transaction {
        MessageEntity.new {
            this.sender = sender
            this.receiver = receiver
            this.message = message
        }
    }
}

fun Application.configureWebsocket(secret: String, audience: String, issuer: String) {
    install(WebSockets) {
        pingPeriod = java.time.Duration.ofMinutes(1)
        timeout = java.time.Duration.ofMinutes(2)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/api/chat/{partnerId}") {
            val jwtVerifier = makeJWTVerifier(secret, audience, issuer)
            val token = call.request.queryParameters["token"]
            if (token == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No token specified"))
                return@webSocket
            }
            val decodedToken = decodeJwt(token, jwtVerifier)
            val userId = decodedToken?.subject ?: return@webSocket close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    "No userId"
                )
            )

            println("User id from token: $userId")
            val partnerId = call.parameters["partnerId"] ?: return@webSocket close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    "No partnerId"
                )
            )
            handlePrivateChat(userId, partnerId)
        }
    }
}

fun fetchMessagesBetweenBetweenUsers(sender: String, receiver: String): List<MessageResponse> = transaction {
    MessageEntity.find {
        (Messages.sender eq sender and (Messages.receiver eq receiver)) or
                (Messages.sender eq receiver and (Messages.receiver eq sender))
    }.orderBy(Messages.timestamp to org.jetbrains.exposed.sql.SortOrder.ASC)
        .map { it.toMessageResponse() }
}

fun Route.messageRoute() {
        get("/api/messages/{sender}/{receiver}") {
            val sender = call.parameters["sender"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val receiver = call.parameters["receiver"] ?: return@get call.respond(HttpStatusCode.BadRequest)

            call.respond(
                ApiResponse.success(
                    statusCode = HttpStatusCode.OK,
                    data = fetchMessagesBetweenBetweenUsers(sender, receiver),
                    message = "Messages for $receiver fetched successfully"
                )
            )
    }
}

