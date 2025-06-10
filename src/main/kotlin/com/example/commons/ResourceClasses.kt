package com.example.commons

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Resource("/api/customer")
class Customer(
    val email: String? = null,
    val status: String? = null,
    val search: String? = null
) {
    @Resource("auth")
    class Auth(val parent: Customer) {
        @Resource("sign_up")
        class SignUp(val parent: Auth)

        @Resource("sign_in")
        class SignIn(val parent: Auth)
    }

    @Resource("{id}")
    class Id(
        val parent: Customer,
        val id: String
    )

    @Resource("forgot_password")
    class ChangePassword(val parent: Customer)

    @Resource("profile")
    class Profile(val parent: Id)

}

@Resource("/api/booking")
class Booking(
    val status: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime? = null
) {
    @Resource("{id}")
    class Id(val parent: Booking, val id: String)
}

@Resource("api/feedback")
class Feedback(
    val message: String? = null,
    val customerId: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime? = null
) {
    @Resource("{id}")
    class Id(
        val parent: Feedback,
        val id: String
    )
}

@Resource("/api/employee")
class Employee(
    val email: String? = null,
    val username: String? = null,
    val status: String? = null,
    val role: String? = null,
    val search: String? = null
) {
    @Resource("auth")
    class Auth(val parent: Employee = Employee()) {
        @Resource("sign_up")
        class SignUp(val parent: Auth)

        @Resource("sign_in")
        class SignIn(val parent: Auth)

    }

    @Resource("{id}")
    class Id(
        val parent: Employee,
        val id: String
    )

    @Resource("forgot_password")
    class ChangePassword(val parent: Employee)

}

@Resource("/api/order")
class Order(
    val search: String? = null,
    val status: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime? = null
) {
    @Resource("{id}")
    class Id(
        val parent: Order,
        val id: String
    )
}

@Resource("/api/payment")
class Payment {
    @Resource("customer")
    class Customer(
        val parent: Payment,
        val phone: String? = null,
        val refNumber: String? = null,
        val paymentStatus: String? = null,
        @Serializable(with = LocalDateTimeSerializer::class)
        val date: LocalDateTime? = null
    ) {
        @Resource("{id}")
        class Id(
            val parent: Customer,
            val id: String
        )

        @Resource("receipt")
        class Receipt(
            val parent: Id
        )
    }

    @Resource("supplier")
    class Supplier(
        val parent: Payment,
        val phone: String? = null,
        val refNumber: String? = null,
        val paymentStatus: String? = null,
        @Serializable(with = LocalDateTimeSerializer::class)
        val date: LocalDateTime? = null
    ) {
        @Resource("{id}")
        class Id(
            val parent: Supplier,
            val id: String
        )
    }
}

@Resource("/api/supplier")
class Supplier(
    val email: String? = null,
    val status: String? = null,
    val search: String? = null
) {
    @Resource("auth")
    class Auth(val parent: Supplier) {
        @Resource("sign_up")
        class SignUp(val parent: Auth)

        @Resource("sign_in")
        class SignIn(val parent: Auth)
    }

    @Resource("{id}")
    class Id(
        val parent: Supplier,
        val id: String
    )

    @Resource("change_password")
    class ChangePassword(val parent: Supplier)

    @Resource("approve_all")
    class ApproveAll(val parent: Supplier)

    @Resource("profile")
    class Profile(val parent: Id)
}

@Resource("/api/product")
class Product(
    val search: String? = null,
    val category: String? = null,
    val name: String? = null,
    val supplierId: String? = null
) {
    @Resource("{id}")
    class Id(
        val parent: Product,
        @Serializable(with = UUIDSerializer::class)
        val id: UUID
    )
}

@Resource("/api/service")
class Service(
    val search: String? = null,
    val name: String? = null
) {
    @Resource("{id}")
    class Id(
        val parent: Service,
        @Serializable(with = UUIDSerializer::class)
        val id: UUID
    ) {
        @Resource("addon")
        class ServiceAddon(
            val parent: Id,
            val search: String? = null
        ) {
            @Resource("{id}")
            class Id(
                val parent: ServiceAddon,
                @Serializable(with = UUIDSerializer::class)
                val id: UUID
            )
        }
    }
}

@Resource("/api/inventory")
class Inventory() {
    @Resource("{id}")
    class Id(
        val parent: Inventory,
        @Serializable(with = UUIDSerializer::class)
        val id: UUID
    )
}

const val SERVICE_ADDON = "/api/service/{id}/addon"
const val SERVICE_ADDON_ID = "$SERVICE_ADDON/{addonId}"
const val CART = "/api/carts"