package com.example.models

import com.example.commons.BigDecimalSerializer
import com.example.commons.LocalDateTimeSerializer
import com.example.commons.UUIDSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotZero
import org.valiktor.functions.isPositive
import org.valiktor.functions.validate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Serializable
data class ProductRequest(
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal,
    val image: String? = null,
    val stock: Int,
    val reorderLevel: Int,
    val supplierId: String,
) : java.io.Serializable {
    fun validate(): ProductRequest {
        org.valiktor.validate(this) {
            validate(ProductRequest::name).isNotBlank()
            validate(ProductRequest::description).isNotBlank()
            validate(ProductRequest::unitPrice).isPositive()
            validate(ProductRequest::stock).isPositive().isNotZero()
            validate(ProductRequest::reorderLevel).isPositive().isNotZero().validate { it >= stock }
            validate(ProductRequest::supplierId).isNotBlank()
        }
        return this
    }
}

@Serializable
data class ProductUpdate(
    val name: String? = null,
    val description: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal? = null,
    val image: String? = null,
    val stock: Int? = null,
    val reorderLevel: Int? = null,
    val supplierId: String? = null,
)

@Serializable
data class ProductResponse(
    @Serializable(with = UUIDSerializer::class)
    val productId: UUID,
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal,
    val image: String?,
    val stock: Int,
    val reorderLevel: Int,
    val sku: String,
    val supplierId: String,
    val supplierName: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
) : java.io.Serializable