package com.example.models.product

import com.example.models.util.BigDecimalSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotZero
import org.valiktor.functions.isPositive
import org.valiktor.functions.validate
import org.valiktor.validate
import java.math.BigDecimal

@Serializable
data class ProductRequest(
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal,
    val image: String? = null,
    val stock: Int,
    val reorderLevel: Int,
) : java.io.Serializable {
    fun validate(): ProductRequest {
        validate(this) {
            validate(ProductRequest::name).isNotBlank()
            validate(ProductRequest::description).isNotBlank()
            validate(ProductRequest::unitPrice).isPositive()
            validate(ProductRequest::stock).isPositive().isNotZero()
            validate(ProductRequest::reorderLevel).isPositive().isNotZero().validate { it >= stock }
        }
        return this
    }
}