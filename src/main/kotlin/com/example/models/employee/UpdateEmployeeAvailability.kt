package com.example.models.employee

import kotlinx.serialization.Serializable

@Serializable
data class UpdateEmployeeAvailability(
    val availability: Availability
)
