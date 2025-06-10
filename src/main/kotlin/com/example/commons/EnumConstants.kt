package com.example.commons

enum class BookingStatus {
    PENDING, APPROVED, IN_PROGRESS, CANCELLED, COMPLETED
}

enum class Frequency {
    ONE_TIME,
    WEEKLY,
    BIWEEKLY,
    MONTHLY
}

enum class ApprovalStatus {
    PENDING, APPROVED, REJECTED
}

enum class Gender {
    MALE, FEMALE, OTHER, NOT_SPECIFIED
}

enum class Roles {
    ADMIN, MANAGER, INVENTORY, FINANCE, SUPERVISOR, CLEANER
}

enum class Availability {
    AVAILABLE, UNAVAILABLE, ON_LEAVE
}

enum class NotificationState{ READ, UNREAD }

enum class PaymentMethod {
    CASH, MOBILE, CARD
}

enum class PaymentStatus {
    PENDING, CONFIRMED, REFUNDED, CANCELLED
}

enum class OrderStatus {
    PENDING,
    REVIEWED,
    APPROVED,
    ACKNOWLEDGED,
    RECEIVED,
    COMPLETED,
    CANCELLED
}

enum class EventType {
    REGISTRATION,
    LOGIN,
    LOGOUT,
    DELETE,
    PASSWORD_RESET,
    UPDATE,
    SERVICE_ADDED_TO_CART,
    BOOKING_MADE,
    PURCHASE_ORDER_PLACED,
    BOOKING_CANCELLED,
    PAYMENT_SUCCESS,
    BOOKING_STATUS_CHANGE,
    BOOKING_ASSIGNMENT
}
