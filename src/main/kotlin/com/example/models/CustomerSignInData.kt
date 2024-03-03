package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class CustomerSignInData(
    val username: String,
    val password: String
)
