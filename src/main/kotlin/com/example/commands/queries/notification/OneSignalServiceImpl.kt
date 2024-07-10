package com.example.commands.queries.notification

import com.example.dao.OneSignalService
import com.example.models.notification.Notification
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class OneSignalServiceImpl(
    private val client: HttpClient
) : OneSignalService {
    override suspend fun sendNotification(notification: Notification): Boolean {
        return try {
            client.post(System.getenv("ONE_SIGNAL_URL")) {
                headers {
                    headers.append("Authorization", "Basic ${System.getenv("ONE_SIGNAL_API_KEY")}")
                }
                contentType(ContentType.Application.Json)
                setBody(notification)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}