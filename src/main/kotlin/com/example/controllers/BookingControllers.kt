package com.example.controllers

import com.example.commons.*
import com.example.commons.DatabaseUtil.dbQuery
import com.example.dao.BookingCleanerRepository
import com.example.dao.BookingRepository
import com.example.db.*
import com.example.db.Booking
import com.example.db.Customer
import com.example.db.Employee
import com.example.models.BookingAssign
import com.example.models.BookingRequest
import com.example.models.BookingResponse
import com.example.models.UpdateBookingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.EntryUnit
import org.ehcache.config.units.MemoryUnit
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import java.math.BigDecimal
import java.time.LocalDateTime

/*Booking Cache*/
class BookingCache(
    private val delegate: BookingRepository,
    storageFile: File?
) : BookingRepository {
    private val uniquePath = cachePath(storageFile)
    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerPersistenceConfiguration(uniquePath))
        .withCache(
            "bookingRequestCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String::class.javaObjectType,
                BookingRequest::class.java,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(1000, EntryUnit.ENTRIES)
                    .offheap(10, MemoryUnit.MB)
                    .disk(100, MemoryUnit.MB, true)
            )
        )
        .withCache(
            "bookingResponseResponse",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String::class.javaObjectType,
                BookingResponse::class.java,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(1000, EntryUnit.ENTRIES)
                    .offheap(10, MemoryUnit.MB)
                    .disk(100, MemoryUnit.MB, true)
            )
        ).build(true)
    private val reqCache =
        cacheManager.getCache("bookingRequestCache", String::class.javaObjectType, BookingRequest::class.java)
    private val resCache =
        cacheManager.getCache("bookingResponseResponse", String::class.javaObjectType, BookingResponse::class.java)

    override suspend fun createNewBooking(
        customerId: String,
        bookingRequest: BookingRequest,
        scope: CoroutineScope
    ): BookingResponse {
        return delegate.createNewBooking(customerId, bookingRequest, scope)
            .also { reqCache.put(it.bookingId, bookingRequest) }
    }

    override suspend fun getFilteredBookings(
        filter: (Booking) -> Boolean
    ): List<BookingResponse> {
        return delegate.getFilteredBookings(filter).also {
            if (it.size == 1) {
                resCache.put(it[0].bookingId, it[0])
            }
        }
    }

    override suspend fun updateBookingStatus(
        id: String,
        bookingStatusUpdate: UpdateBookingStatus,
        scope: CoroutineScope
    ): Boolean {
        return delegate.updateBookingStatus(id, bookingStatusUpdate, scope)
    }

    override suspend fun updateBooking(
        customerId: String,
        id: String,
        bookingUpdateRequest: BookingRequest
    ): Boolean {
        reqCache.put(id, bookingUpdateRequest)
        return delegate.updateBooking(customerId, id, bookingUpdateRequest)
    }

    override suspend fun deleteBooking(id: String): Boolean {
        reqCache.remove(id)
        return delegate.deleteBooking(id)
    }

    override suspend fun clearBookings(): Boolean {
        return delegate.clearBookings()
    }
}

/*Repository implementations*/
class BookingCleanerRepositoryImpl : BookingCleanerRepository {
    override suspend fun assignBookingToCleaner(
        supervisorId: String,
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

            BookingAssignment.new {
                this.booking = booking
                this.cleaner = cleaner
                this.assignedBy = supervisorId.let { Employee.findById(it) }
            }
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
        cleanerId: String
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

class BookingRepositoryImpl : BookingRepository {
    override suspend fun createNewBooking(
        customerId: String,
        bookingRequest: BookingRequest,
        scope: CoroutineScope
    ): BookingResponse = dbQuery {
        try {
            var totalAmount = BigDecimal.ZERO
            val bookingDateTime = LocalDateTime.of(
                bookingRequest.bookingDate,
                bookingRequest.bookingTime
            )
            val customer = Customer[customerId]
            val booking = Booking.new {
                this.customer = Customer[customerId]
                this.bookingDateTime = bookingDateTime
                this.frequency = bookingRequest.frequency
                this.additionalInstructions = bookingRequest.additionalInstructions
                this.address = bookingRequest.address
                this.totalAmount = totalAmount
                this.bookingStatus = BookingStatus.PENDING
            }
            scope.launch {
                logUserEvent(
                    userId = customerId,
                    event = EventType.BOOKING_MADE,
                    eventData = "Booking ${booking.id} placed by ${customer.fullName} at ${booking.createdAt}"
                )
            }
            val frequency = when (bookingRequest.frequency) {
                Frequency.ONE_TIME -> BigDecimal.ONE
                Frequency.WEEKLY -> BigDecimal("1.8")
                Frequency.BIWEEKLY -> BigDecimal("1.6")
                Frequency.MONTHLY -> BigDecimal("1.5")
            }
            val serviceCartItems = ServiceCartItem.find { ServiceCart.customerId.eq(customerId) }.toList()
            if (serviceCartItems.isEmpty()) {
                throw IllegalArgumentException("Cannot create booking. The cart is empty")
            }

            serviceCartItems.forEach { cartItem ->
                val serviceTotalAmount = cartItem.toServiceCartResponse().total * frequency
                totalAmount += serviceTotalAmount

                cartItem.service?.let {
                    BookingServiceAddOn.new {
                        this.booking = booking
                        this.service = it
                        this.quantity = cartItem.quantity
                        this.subtotal = it.price.times(cartItem.quantity.toBigDecimal())
                            .times(frequency)
                    }
                }
                cartItem.serviceAddon?.let {
                    BookingServiceAddOn.new {
                        this.booking = booking
                        this.serviceAddOn = it
                        this.quantity = cartItem.quantity
                        this.subtotal = it.price.times(cartItem.quantity.toBigDecimal())
                            .times(frequency)
                    }
                }
            }
            booking.totalAmount = totalAmount
            serviceCartItems.forEach { cartItem ->
                cartItem.delete()
            }
            return@dbQuery booking.toBookingResponse()
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> throw e
                else -> {
                    println("UnExpected error: ${e.message}")
                    e.printStackTrace()
                    throw UnexpectedError("An unexpected error occurred")
                }
            }
        }
    }

    override suspend fun clearBookings(): Boolean = dbQuery {
        Booking.all()
            .forEach {
                it.delete()
            }
        return@dbQuery true
    }

    override suspend fun getFilteredBookings(
        filter: (Booking) -> Boolean
    ): List<BookingResponse> = dbQuery {
        return@dbQuery Booking.all()
            .filter(filter)
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .map { it.toBookingResponse() }
    }

    override suspend fun updateBookingStatus(
        id: String,
        bookingStatusUpdate: UpdateBookingStatus,
        scope: CoroutineScope
    ): Boolean = dbQuery {
        val bookingExists = Bookings
            .selectAll()
            .where { Bookings.id eq id }
            .count() > 0
        val booking = Booking.find { Bookings.id eq id }.singleOrNull()

        if (!bookingExists) {
            throw IllegalArgumentException("Booking with id $id does not exist")
        }

        scope.launch {
            logUserEvent(
                userId = booking?.customer?.id?.value ?: "",
                event = EventType.BOOKING_STATUS_CHANGE,
                eventData = "Booking ${booking?.id} status changed from ${booking?.bookingStatus} to ${bookingStatusUpdate.status}"
            )
        }
        Booking.findByIdAndUpdate(id) { updateStatus ->
            updateStatus.bookingStatus = bookingStatusUpdate.status
        }
        return@dbQuery true
    }

    override suspend fun updateBooking(
        customerId: String,
        id: String,
        bookingUpdateRequest: BookingRequest
    ): Boolean = dbQuery {
        val bookingExists = Bookings.selectAll()
            .where { Bookings.id eq id }
            .count() > 0
        if (!bookingExists) {
            throw IllegalArgumentException("Booking with id $id does not exist")
        }
        val bookingDateTime = LocalDateTime.of(
            bookingUpdateRequest.bookingDate,
            bookingUpdateRequest.bookingTime
        )
        Booking.findByIdAndUpdate(id) { update ->
            update.customer = Customer[customerId]
            update.bookingDateTime = bookingDateTime
            update.frequency = bookingUpdateRequest.frequency
            update.additionalInstructions = bookingUpdateRequest.additionalInstructions
            update.address = bookingUpdateRequest.address
        }
        return@dbQuery true
    }

    override suspend fun deleteBooking(id: String): Boolean = dbQuery {
        val bookingExists = Bookings.selectAll()
            .where { Bookings.id eq id }
            .count() > 0
        if (!bookingExists) {
            throw IllegalArgumentException("Booking with id $id does not exist")
        }
        Booking.findById(id)?.delete()
        return@dbQuery true
    }
}