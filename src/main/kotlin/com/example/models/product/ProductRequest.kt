package com.example.models.product

import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isPositive
import org.valiktor.validate

@Serializable
data class ProductRequest(
    val name: String,
    val description: String,
    val price: Double,
    val image: String?
) {
    init {
        validate(this){
            validate(ProductRequest::name).isNotBlank()
            validate(ProductRequest::description).isNotBlank()
            validate(ProductRequest::price).isPositive()
        }
    }
}