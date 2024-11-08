package com.example.db

import com.example.commons.*
import com.example.models.PurchaseOrderItemResponse
import com.example.models.PurchaseOrderResponse
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.date
import java.time.LocalDate

object PurchaseOrders : CustomStringTable("purchase_orders") {
    val employee = reference(
        "employee_id",
        EmployeeTable,
        onDelete = ReferenceOption.CASCADE
    )
    val supplier = reference(
        "supplier_id",
        SupplierTable,
        onDelete = ReferenceOption.CASCADE
    )
    val expectedDate = date("expected_date").clientDefault { LocalDate.now() }
    val totalAmount = decimal("total_amount", 10, 2)
    val paid = bool("paid").default(false)
    val orderStatus = customEnumeration(
        "order_status",
        "order_status",
        { value -> OrderStatus.valueOf(value as String) },
        { PGEnum("order_status", it) }
    ).default(OrderStatus.PENDING)
}

object PurchaseOrderItems : CustomStringTable("purchase_order_items") {
    val order = reference(
        "order",
        PurchaseOrders,
        onDelete = ReferenceOption.CASCADE
    )
    val product = reference(
        "product_id",
        ProductTable,
        onDelete = ReferenceOption.CASCADE
    )
    val quantity = integer("quantity")
    val unitPrice = decimal("price", 10, 2)
    val subtotal = decimal("sub_total", 10, 2)
}

class PurchaseOrder(id: EntityID<String>) : CustomStringEntity(id, PurchaseOrders) {
    companion object : CustomStringEntityClass<PurchaseOrder>(PurchaseOrders)

    var employee by Employee referencedOn PurchaseOrders.employee
    var supplier by Supplier referencedOn PurchaseOrders.supplier
    var expectedDate by PurchaseOrders.expectedDate
    var totalAmount by PurchaseOrders.totalAmount
    var paid by PurchaseOrders.paid
    var orderStatus by PurchaseOrders.orderStatus

    val purchaseOrderItems by PurchaseOrderItem referrersOn PurchaseOrderItems.order

    fun toOrderResponse() = PurchaseOrderResponse(
        purchaseOrderId = id.value,
        employee = employee.fullName,
        supplier = supplier.fullName,
        expectedDate = expectedDate,
        purchaseOrderItems = purchaseOrderItems.map {
            it.toPurchaseOrderItemResponse()
        }.toMutableList(),
        totalAmount = this.totalAmount,
        paid = paid,
        orderStatus = orderStatus,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

class PurchaseOrderItem(id: EntityID<String>) : CustomStringEntity(id, PurchaseOrderItems) {

    companion object : CustomStringEntityClass<PurchaseOrderItem>(PurchaseOrderItems)

    var order by PurchaseOrder referencedOn PurchaseOrderItems.order
    var product by Product referencedOn PurchaseOrderItems.product
    var quantity by PurchaseOrderItems.quantity
    var unitPrice by PurchaseOrderItems.unitPrice
    var subtotal by PurchaseOrderItems.subtotal

    fun toPurchaseOrderItemResponse() = PurchaseOrderItemResponse(
        purchaseOrderItemId = id.value,
        product = product.toProductResponse().name,
        quantity = quantity,
        unitPrice = unitPrice,
        subtotal = subtotal,
    )
}