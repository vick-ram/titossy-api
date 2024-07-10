package com.example.models.booking

import com.example.models.util.LocalDateSerializer
import com.example.models.util.LocalTimeSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

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
        validate(this) {
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
