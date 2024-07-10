package com.example.dao

import com.example.models.notification.Notification

interface OneSignalService {
    suspend fun sendNotification(notification: Notification): Boolean
}