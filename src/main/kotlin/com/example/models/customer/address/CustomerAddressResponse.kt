package com.example.models.customer.address

import kotlinx.serialization.Serializable

@Serializable
data class CustomerAddressResponse(
    val id: Int,
    val street: String,
    val city: String,
    val state: String,
    val zip: String,
)