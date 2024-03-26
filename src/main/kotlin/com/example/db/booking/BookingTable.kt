package com.example.db.booking

import com.example.db.customer.Customer
import com.example.db.customer.CustomerTable
import com.example.db.employee.Employee
import com.example.db.employee.EmployeeTable
import com.example.db.service.Service
import com.example.db.service.ServiceTable
import com.example.db.util.PGEnum
import com.example.db.util.tsVector
import com.example.models.booking.BookingStatus
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object BookingItemsTable : IntIdTable() {
    val service = reference("service", ServiceTable, onDelete = ReferenceOption.CASCADE)
    val booking = reference("booking", BookingTable, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity")
    val price = decimal("price", 10, 2)
    val duration = integer("duration")
}

object BookingTable : IntIdTable() {
    val customer = reference("customer", CustomerTable, onDelete = ReferenceOption.CASCADE)
    val employee = reference("employee", EmployeeTable, onDelete = ReferenceOption.CASCADE)
    val bookingDate = datetime("date").clientDefault { LocalDateTime.now() }
    val scheduledDate = datetime("time").clientDefault { LocalDateTime.now()}
    val totalAmount = decimal("total_amount", 10, 2)
    val bookingStatus = customEnumeration(
        "status",
        "bookingstatus",
        { value -> BookingStatus.valueOf(value as String) },
        { PGEnum("bookingstatus", it) }
    )
}

class BookingItem(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BookingItem>(BookingItemsTable)

    var service by Service referencedOn BookingItemsTable.service
    var booking by Booking referencedOn BookingItemsTable.booking
    var quantity by BookingItemsTable.quantity
    var price by BookingItemsTable.price
    var duration by BookingItemsTable.duration
}

class Booking(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Booking>(BookingTable)

    var customer by Customer referencedOn BookingTable.customer
    var employee by Employee referencedOn BookingTable.employee
    var bookingDate by BookingTable.bookingDate
    var scheduledDate by BookingTable.scheduledDate
    val bookingItems by BookingItem referrersOn BookingItemsTable.booking
    var totalAmount by BookingTable.totalAmount
    var bookingStatus by BookingTable.bookingStatus
}