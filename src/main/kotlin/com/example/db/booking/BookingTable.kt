package com.example.db.booking

import com.example.db.customer.Customer
import com.example.db.customer.CustomerTable
import com.example.db.employee.Employee
import com.example.db.employee.EmployeeTable
import com.example.db.service.Service
import com.example.db.service.ServiceAddOn
import com.example.db.service.ServiceAddOns
import com.example.db.service.Services
import com.example.db.util.*
import com.example.models.booking.BookingResponse
import com.example.models.booking.BookingServiceAddOnResponse
import com.example.models.booking.BookingStatus
import com.example.models.booking.Frequency
import com.example.models.util.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.*

object Bookings : CustomStringTable("bookings") {
    val customerId = reference("customer_id", CustomerTable, onDelete = ReferenceOption.CASCADE)
    val bookingDateTime = datetime("booking_datetime").clientDefault { LocalDateTime.now() }
    val frequency = customEnumeration(
        "frequency",
        "frequency",
        { value -> Frequency.valueOf(value as String) },
        { PGEnum("frequency", it) }
    ).default(Frequency.ONE_TIME)
    val additionalInstructions = text("additional_instructions").nullable()
    val address = text("address").nullable()
    val totalAmount = decimal("total_amount", 10, 2)
    val paid = bool("paid").default(false)
    val bookingStatus = customEnumeration(
        "booking_status",
        "bookingstatus",
        { value -> BookingStatus.valueOf(value as String) },
        { PGEnum("bookingstatus", it) }
    ).default(BookingStatus.PENDING)
}

object BookingAssignments : CustomUUIDTable("booking_assignments") {
    val bookingId = reference("booking_id", Bookings, onDelete = ReferenceOption.CASCADE)
    val cleanerId = reference("cleaner_id", EmployeeTable, onDelete = ReferenceOption.CASCADE)
    val assignedBy = reference("assigned_by", EmployeeTable, onDelete = ReferenceOption.SET_NULL).nullable()
}

class BookingAssignment(id: EntityID<UUID>) : CustomUUIDEntity(id, BookingAssignments) {
    companion object : CustomUUIDEntityClass<BookingAssignment>(BookingAssignments)

    var booking by Booking referencedOn BookingAssignments.bookingId
    var cleaner by Employee referencedOn BookingAssignments.cleanerId
    var assignedBy by Employee optionalReferencedOn BookingAssignments.assignedBy

    fun toBookingCleanerResponse() = BookingCleanerResponse(
        bookingAssignment = BookingAssignResponse(
            bookingId = this.booking.id.value,
            customer = this.booking.customer.fullName,
            service = this.booking.bookingServiceAddOns.first().service?.name ?: "",
            instructions = this.booking.additionalInstructions ?: "",
            address = this.booking.address,
            bookingStatus = this.booking.bookingStatus

        ),
        cleaner = this.cleaner.fullName,
        assignedBy = this.assignedBy?.fullName,
        assignedDate = this.booking.bookingDateTime
    )
}

object BookingServiceAddOns : CustomUUIDTable("booking_service_addons") {
    val bookingId = reference("booking_id", Bookings, onDelete = ReferenceOption.CASCADE)
    val serviceId = reference("service_id", Services, onDelete = ReferenceOption.CASCADE)
        .nullable()
    val serviceAddOnId = reference("service_add_on_id", ServiceAddOns, onDelete = ReferenceOption.CASCADE)
        .nullable()
    val quantity = integer("quantity").default(1)
    val subtotal = decimal("subtotal", 10, 2)
}

class Booking(id: EntityID<String>) : CustomStringEntity(id, Bookings) {
    companion object : CustomStringEntityClass<Booking>(Bookings)

    var bookingId by Bookings.id
    var customer by Customer referencedOn Bookings.customerId
    var bookingDateTime by Bookings.bookingDateTime
    var frequency by Bookings.frequency
    var additionalInstructions by Bookings.additionalInstructions
    var address by Bookings.address
    var totalAmount by Bookings.totalAmount
    var paid by Bookings.paid
    var bookingStatus by Bookings.bookingStatus

    val bookingServiceAddOns by BookingServiceAddOn referrersOn BookingServiceAddOns.bookingId

    fun toBookingResponse() = BookingResponse(
        bookingId = this.id.value,
        customer = this.customer.fullName,
        bookingDateTime = this.bookingDateTime,
        frequency = this.frequency,
        additionalInstructions = this.additionalInstructions,
        address = this.address,
        bookingServiceAddOns = this.bookingServiceAddOns.map { it.toBookingServiceAddOnResponse() }.toList(),
        totalAmount = this.totalAmount,
        paid = this.paid,
        bookingStatus = this.bookingStatus,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

class BookingServiceAddOn(id: EntityID<UUID>) : CustomUUIDEntity(id, BookingServiceAddOns) {
    companion object : CustomUUIDEntityClass<BookingServiceAddOn>(BookingServiceAddOns)

    var booking by Booking referencedOn BookingServiceAddOns.bookingId
    var service by Service optionalReferencedOn BookingServiceAddOns.serviceId
    var serviceAddOn by ServiceAddOn optionalReferencedOn BookingServiceAddOns.serviceAddOnId
    var quantity by BookingServiceAddOns.quantity
    var subtotal by BookingServiceAddOns.subtotal

    fun toBookingServiceAddOnResponse() = BookingServiceAddOnResponse(
        service?.name,
        serviceAddOn?.name,
        quantity,
        subtotal,
    )
}

@Serializable
data class BookingCleanerResponse(
    val bookingAssignment: BookingAssignResponse,
    val cleaner: String,
    val assignedBy: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val assignedDate: LocalDateTime
)

@Serializable
data class BookingAssignResponse(
    val bookingId: String,
    val customer: String,
    val service: String,
    val instructions: String,
    val address: String?,
    val bookingStatus: BookingStatus
)



