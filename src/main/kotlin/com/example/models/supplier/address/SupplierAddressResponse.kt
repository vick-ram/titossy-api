package com.example.models.supplier.address

import kotlinx.serialization.Serializable

@Serializable
data class SupplierAddressResponse(
    val id: Int,
    val street: String,
    val city: String,
    val state: String,
    val zip: String,
)