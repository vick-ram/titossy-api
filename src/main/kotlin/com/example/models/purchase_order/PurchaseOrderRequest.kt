package com.example.models.purchase_order

import com.example.models.util.LocalDateSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotNull
import org.valiktor.validate
import java.time.LocalDate
import java.util.*

@Serializable
data class PurchaseOrderRequest(
    @Serializable(with = UUIDSerializer::class)
    val supplierId: UUID,
    @Serializable(with = LocalDateSerializer::class)
    val expectedDate: LocalDate
) {
    fun validate(): PurchaseOrderRequest {
        validate(this) {
            validate(PurchaseOrderRequest::supplierId).isNotNull()
        }
        return this
    }
}
