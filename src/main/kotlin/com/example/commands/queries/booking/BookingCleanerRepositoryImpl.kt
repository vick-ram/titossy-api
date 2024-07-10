package com.example.commands.queries.booking

import com.example.dao.BookingCleanerRepository
import com.example.db.booking.Booking
import com.example.db.booking.BookingAssignment
import com.example.db.booking.BookingAssignments
import com.example.db.booking.BookingCleanerResponse
import com.example.db.employee.Employee
import com.example.db.employee.EmployeeTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.booking.BookingAssign
import com.example.models.booking.Frequency
import com.example.models.employee.Roles
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class BookingCleanerRepositoryImpl : BookingCleanerRepository {
    override suspend fun assignBookingToCleaner(
        supervisorId: UUID,
        bookingAssign: BookingAssign
    ): Boolean =
        dbQuery {
            val booking = Booking.findById(bookingAssign.bookingId)
                ?: throw IllegalArgumentException("Invalid booking ID")
            val cleaner = Employee.findById(bookingAssign.cleanerId)
                ?: throw IllegalArgumentException("Invalid cleaner ID")

            if (cleaner.role != Roles.CLEANER) {
                throw IllegalArgumentException("The employee is not a cleaner")
            }

            /*val existingAssignment = BookingAssignment.find {
                (BookingAssignments.cleanerId eq bookingAssign.cleanerId)
            }.any { assignment ->
                hasDateTimeConflict(
                    booking.bookingDateTime,
                    assignment.booking.bookingDateTime,
                    booking.frequency
                )
            }

            if (existingAssignment) {
                throw IllegalArgumentException("The cleaner already has a conflicting booking")
            }*/

            BookingAssignment.new {
                this.booking = booking
                this.cleaner = cleaner
                this.assignedBy = supervisorId.let { Employee.findById(it) }
            }
            /*EmployeeTable.update({ EmployeeTable.id eq cleanerId }) {
                it[EmployeeTable.availability] = Availability.UNAVAILABLE
            }*/
            return@dbQuery true
        }

    override suspend fun getFilteredAssignBookings(
        filter: (BookingAssignment) -> Boolean
    ): List<BookingCleanerResponse> =
        dbQuery {
            return@dbQuery try {
                BookingAssignment
                    .all()
                    .filter(filter)
                    .map { it.toBookingCleanerResponse() }
                    .toList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

    override suspend fun updateBookingCleaner(
        bookingId: String,
        cleanerId: UUID
    ): Boolean = dbQuery {
        val cleanerExists = EmployeeTable
            .selectAll()
            .where { (EmployeeTable.id eq cleanerId) }
            .singleOrNull()
        if (cleanerExists == null || cleanerExists[EmployeeTable.role] != Roles.CLEANER) {
            throw IllegalArgumentException("Cleaner with id $cleanerId does not exist or does not match Role specified")
        }
        val bookingCleaner =
            BookingAssignment.find {
                (BookingAssignments.bookingId eq bookingId) and (BookingAssignments.cleanerId eq cleanerId)
            }.singleOrNull()
        if (bookingCleaner == null) {
            throw IllegalArgumentException("Booking with id $bookingId and cleaner with id $cleanerId does not exist")
        }
        return@dbQuery true
    }

    override suspend fun deleteBookingCleaner(bookingId: String): Boolean = dbQuery {
        val bookingCleaner = BookingAssignment.find {
            BookingAssignments.bookingId eq bookingId
        }.singleOrNull()
        if (bookingCleaner == null) {
            throw IllegalArgumentException("Booking with id $bookingId does not exist")
        }
        bookingCleaner.delete()
        return@dbQuery true
    }
}


fun hasDateTimeConflict(
    bookingDateTime: LocalDateTime,
    otherDateTime: LocalDateTime,
    frequency: Frequency
): Boolean {
    return when (frequency) {
        Frequency.ONE_TIME -> bookingDateTime.isEqual(otherDateTime)
        Frequency.WEEKLY -> bookingDateTime.truncatedTo(ChronoUnit.DAYS) == otherDateTime.truncatedTo(ChronoUnit.DAYS)
                && bookingDateTime.dayOfWeek == otherDateTime.dayOfWeek

        Frequency.BIWEEKLY -> {
            val weeksBetween = ChronoUnit.WEEKS.between(otherDateTime, bookingDateTime).toInt()
            weeksBetween % 2 == 0 && bookingDateTime.truncatedTo(ChronoUnit.DAYS) == otherDateTime.truncatedTo(
                ChronoUnit.DAYS
            )
        }

        Frequency.MONTHLY -> bookingDateTime.dayOfMonth == otherDateTime.dayOfMonth
    }
}