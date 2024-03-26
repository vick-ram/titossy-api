package com.example.models.supplier

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class SupplierUpdate(
    val username: String,
    val firstName: String,
    val lastName: String,
    val phone: Long,
    val address: Int,
    val email: String,
    val password: String
)
