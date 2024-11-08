package com.example.routing

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
            val tokenPayload = call.request.headers["Authorization"]
            val token = tokenPayload?.split(" ")?.get(1)
            val decodedToken = decodeJwt(token!!, jwtVerifier)
            val userId = decodedToken?.subject ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No userId"))
            val partnerId = call.parameters["partnerId"] ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No partnerId"))
            handlePrivateChat(userId, partnerId)
        }
    }
}

fun fetchMessagesBetweenBetweenUsers(sender: String, receiver: String): List<MessageResponse> = transaction {
    MessageEntity.find {
        (Messages.sender eq sender and (Messages.receiver eq receiver)) or
                (Messages.sender eq receiver and (Messages.receiver eq sender))
    }.orderBy(Messages.timestamp to org.jetbrains.exposed.sql.SortOrder.ASC)
        .map{it.toMessageResponse()}
}

fun Route.messageRoute() {
    authenticate("auth-jwt") {
        get("/api/messages/{receiver}") {
            val principal = call.principal<JWTPrincipal>()
            val sender = principal?.subject ?: return@get call.respond(HttpStatusCode.BadRequest)
            val receiver = call.parameters["receiver"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(fetchMessagesBetweenBetweenUsers(sender, receiver))
        }
    }
}

