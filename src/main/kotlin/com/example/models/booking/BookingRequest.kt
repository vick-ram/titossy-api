package com.example.models.booking

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate
import java.util.Date

@Serializable
data class BookingRequest(
    val customerId: Int,
    val employeeId: Int,
    @Serializable(with = DateSerializer::class)
    val scheduleDate: Date,
    val bookingItems: List<BookingItemRequest>
) {
    init {
        validate(this){
            validate(BookingRequest::bookingItems).isNotEmpty()
        }
    }
}
