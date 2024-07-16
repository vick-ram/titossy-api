package com.example.models.util

import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun hashedPassword(
    password: String,
    secret: String
): String {
    val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(secretKey)
    val hash = mac.doFinal(password.toByteArray())
    return Base64.getEncoder().encodeToString(hash)
}

fun comparePassword(password: String, hashedPassword: String, secret: String): Boolean {
    return hashedPassword(password, secret) == hashedPassword
}