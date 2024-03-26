package com.example.db.order

import com.example.db.employee.Employee
import com.example.db.employee.EmployeeTable
import com.example.db.product.Product
import com.example.db.product.ProductTable
import com.example.db.supplier.Supplier
import com.example.db.supplier.SupplierTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object OrderItemsTable: IntIdTable() {
    val orderId = reference("order_id", OrderTable, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", ProductTable, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity")
    val price = decimal("price", 10, 2)
}

class OrderItem(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<OrderItem>(OrderItemsTable)

    var orderId by Order referencedOn OrderItemsTable.orderId
    var productId by Product referencedOn OrderItemsTable.productId
    var quantity by OrderItemsTable.quantity
    var price by OrderItemsTable.price
}

object OrderTable: IntIdTable() {
    val employeeId = reference("employee_id", EmployeeTable, onDelete = ReferenceOption.CASCADE)
    val supplierId = reference("product_id", SupplierTable, onDelete = ReferenceOption.CASCADE)
    val orderDate = datetime("order_date").clientDefault { LocalDateTime.now() }
    val totalAmount = decimal("total_amount", 10, 2)
    val orderStatus = varchar("order_status", 50).clientDefault { "PENDING" }
}

class Order(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Order>(OrderTable)

    var employeeId by Employee referencedOn OrderTable.employeeId
    var supplierId by Supplier referencedOn OrderTable.supplierId
    var orderDate by OrderTable.orderDate
    var totalAmount by OrderTable.totalAmount
    val orderItems by OrderItem referrersOn OrderItemsTable.orderId
    var orderStatus by OrderTable.orderStatus
}