package com.example.models.purchase_order

import com.example.models.util.LocalDateSerializer
import com.example.models.util.LocalDateTimeSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotNull
import org.valiktor.validate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Serializable
data class PurchaseOrderRequest(
    @Serializable(with = UUIDSerializer::class)
    val supplierId: UUID,
    @Serializable(with = LocalDateSerializer::class)
    val expectedDate: LocalDate,
    val products: MutableList<PurchaseOrderItemRequest>? = null
) {
    fun validate(): PurchaseOrderRequest {
        validate(this) {
            validate(PurchaseOrderRequest::supplierId).isNotNull()
        }
        return this
    }
}
