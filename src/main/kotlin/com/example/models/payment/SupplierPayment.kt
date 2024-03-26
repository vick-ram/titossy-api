package com.example.models.payment

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isPositive
import org.valiktor.validate
import java.util.*

@Serializable
data class SupplierPaymentRequest(
    val orderId: Int,
    val amount: Double,
    val method: String
){
    init {
        validate(this){
            validate(SupplierPaymentRequest::amount).isPositive()
            validate(SupplierPaymentRequest::method).isNotBlank()
        }
    }
}

@Serializable
data class SupplierPaymentResponse(
    val id: Int,
    val orderId: Int,
    @Serializable(with = DateSerializer::class)
    val date: Date,
    val amount: Double,
    val method: String,
    val status: PaymentStatus
)