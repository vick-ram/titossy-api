package com.example.commands.queries.booking

import com.example.commands.cachePath
import com.example.dao.BookingRepository
import com.example.db.booking.Booking
import com.example.models.booking.BookingRequest
import com.example.models.booking.BookingResponse
import com.example.models.booking.UpdateBookingStatus
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.EntryUnit
import org.ehcache.config.units.MemoryUnit
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration
import java.io.File
import java.util.UUID

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

    override suspend fun createNewBooking(customerId: UUID, bookingRequest: BookingRequest): BookingResponse {
        return delegate.createNewBooking(customerId, bookingRequest)
            .also { reqCache.put(it.bookingId, bookingRequest) }
    }

    override suspend fun getFilteredBookings(filter: (Booking) -> Boolean): List<BookingResponse> {
        return delegate.getFilteredBookings(filter).also {
            if (it.size == 1) {
                resCache.put(it[0].bookingId, it[0])
            }
        }
    }

    override suspend fun updateBookingStatus(id: String, bookingStatusUpdate: UpdateBookingStatus): Boolean {
        return delegate.updateBookingStatus(id, bookingStatusUpdate)
    }

    override suspend fun updateBooking(customerId: UUID, id: String, bookingUpdateRequest: BookingRequest): Boolean {
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