package com.example.models

import com.example.commons.BookingStatus
import com.example.commons.Frequency
import com.example.commons.BigDecimalSerializer
import com.example.commons.LocalDateSerializer
import com.example.commons.LocalDateTimeSerializer
import com.example.commons.LocalTimeSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.functions.isIn
import org.valiktor.functions.isLessThanOrEqualTo
import org.valiktor.functions.isNotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Serializable
data class BookingAssign(
    val bookingId: String,
    val cleanerId: String
)

@Serializable
data class BookingServiceAddOnResponse(
    val service: String?,
    val serviceAddOn: String?,
    val quantity: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val subtotal: BigDecimal,
)

@Serializable
data class BookingRequest(
    @Serializable(with = LocalDateSerializer::class)
    val bookingDate: LocalDate,
    @Serializable(with = LocalTimeSerializer::class)
    val bookingTime: LocalTime,
    val frequency: Frequency,
    val additionalInstructions: String? = null,
    val address: String?
) : java.io.Serializable {
    fun validate(): BookingRequest {
        org.valiktor.validate(this) {
            validate(BookingRequest::bookingDate).isNotNull()
                .isGreaterThanOrEqualTo(LocalDate.now())
            validate(BookingRequest::bookingTime).isNotNull()
                .isGreaterThanOrEqualTo(LocalTime.of(8, 0))
                .isLessThanOrEqualTo(LocalTime.of(17, 0))
            validate(BookingRequest::frequency).isNotNull().isIn(Frequency.entries)
        }
        return this
    }
}

@Serializable
data class BookingResponse(
    val bookingId: String,
    val customer: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val bookingDateTime: LocalDateTime,
    val frequency: Frequency,
    val additionalInstructions: String? = null,
    val address: String?,
    val bookingServiceAddOns: List<BookingServiceAddOnResponse>,
    @Serializable(with = BigDecimalSerializer::class)
    val totalAmount: BigDecimal,
    val paid: Boolean,
    val bookingStatus: BookingStatus,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
) : java.io.Serializable

@Serializable
data class UpdateBookingStatus(
    val status: BookingStatus
)