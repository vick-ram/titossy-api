package com.example.models

import com.example.auth.Config
import kotlinx.serialization.Serializable
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Serializable
data class Customer(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val phone: Long,
    val gender: String,
    val email: String,
    val password: String,
    val status: String
)

//fun hashedPassword(password: String): String {
//    return Mac.getInstance("HmacSHA256").doFinal(password.toByteArray()).toString()
//}

fun hashedPassword(password: String): String {
    val secret = Config.SECRET
    val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(secretKey)
    val hash = mac.doFinal(password.toByteArray())
    return Base64.getEncoder().encodeToString(hash)
}