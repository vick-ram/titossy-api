package com.example.models.util

import com.example.auth.Config
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun hashedPassword(password: String): String {
    val secret = Config.SECRET
    val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(secretKey)
    val hash = mac.doFinal(password.toByteArray())
    return Base64.getEncoder().encodeToString(hash)
}

fun comparePassword(password: String, hashedPassword: String): Boolean {
    return hashedPassword(password) == hashedPassword
}

fun fromHashedPassword(hashedPassword: String, password: String): String {
    return if (comparePassword(password, hashedPassword)) {
        hashedPassword
    } else {
        throw Exception("Password did not match")
    }
}