package com.example.models.employee

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class EmployeeUpdate(
    val username: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val email: String,
    val password: String,
    val phone: Long,
    val role: Roles,
    val availability: Availability?
)
