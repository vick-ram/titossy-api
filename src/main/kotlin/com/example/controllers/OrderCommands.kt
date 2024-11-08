package com.example.controllers

import com.example.commons.DatabaseUtil.dbQuery
import com.example.commons.OrderStatus
import com.example.dao.PurchaseOrderRepository
import com.example.db.*
import com.example.models.PurchaseOrderRequest
import com.example.models.PurchaseOrderResponse
import com.example.models.UpdateOrderStatus
import org.jetbrains.exposed.sql.selectAll
import java.math.BigDecimal

class PurchaseOrderRepositoryImpl : PurchaseOrderRepository {
    override suspend fun createPurchaseOrder(
        employeeId: String,
        purchaseOrder: PurchaseOrderRequest
    ): PurchaseOrderResponse = dbQuery {
        var totalAmount = BigDecimal.ZERO
        val newOrder = PurchaseOrder.new {
            this.employee = Employee[employeeId]
            this.supplier = Supplier[purchaseOrder.supplierId]
            this.expectedDate = purchaseOrder.expectedDate
            this.totalAmount = totalAmount
            this.orderStatus = OrderStatus.PENDING
        }
        val cartItems = ProductCartEntity.find { ProductCarts.employee eq employeeId }.toList()

        cartItems.forEach { cartItem ->
            val subtotal = cartItem.quantity.toBigDecimal().times(cartItem.product.price)
            PurchaseOrderItem.new {
                this.order = newOrder
                this.product = cartItem.product
                this.quantity = cartItem.quantity
                this.unitPrice = cartItem.product.price
                this.subtotal = subtotal
            }
            totalAmount += subtotal
        }
        newOrder.totalAmount = totalAmount
        cartItems.forEach { it.delete() }
        return@dbQuery newOrder.toOrderResponse()
    }

    override suspend fun getFilteredPurchaseOrders(
        filter: (PurchaseOrder) -> Boolean
    ): List<PurchaseOrderResponse> =
        dbQuery {
            PurchaseOrder.all()
                .filter(filter)
                .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
                .map { it.toOrderResponse() }
        }

    override suspend fun searchPurchaseOrder(query: String?): List<PurchaseOrderResponse> = dbQuery {
        PurchaseOrder.all()
            .filter { order ->
                if (query != null) {
                    order.employee.fullName.contains(query, ignoreCase = true)
                            || order.supplier.email.contains(query, ignoreCase = true)
                            || order.orderStatus.name.contains(query, ignoreCase = true)
                } else {
                    true
                }
            }
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .map { it.toOrderResponse() }
    }

    override suspend fun updatePurchaseOrderStatus(
        purchaseOrderId: String,
        orderStatus: UpdateOrderStatus
    ): Boolean =
        dbQuery {
            val orderToUpdate = PurchaseOrder.find { PurchaseOrders.id eq purchaseOrderId }.singleOrNull()
            if (orderToUpdate != null && orderStatus.status == OrderStatus.RECEIVED) {
                orderToUpdate.purchaseOrderItems.forEach { item ->
                    val product = item.product
                    product.stock += item.quantity
                    product.flush()
                }
            }
            orderToUpdate?.orderStatus = orderStatus.status
            orderToUpdate?.flush()
            return@dbQuery true
        }

    override suspend fun updatePurchaseOrder(
        employeeId: String,
        id: String,
        purchaseOrder: PurchaseOrderRequest
    ): Boolean = dbQuery {
        val purchaseOrderFromDb = PurchaseOrders.selectAll().where { PurchaseOrders.id eq id }
        val purchaseOrderExists = purchaseOrderFromDb.count() > 0
        val totalAmount = BigDecimal.ZERO
        if (!purchaseOrderExists) {
            throw IllegalArgumentException("product with id $id does not exist")
        }
        val newOrderUpdate = PurchaseOrder.findByIdAndUpdate(id) { update ->
            update.employee = Employee[employeeId]
            update.supplier = Supplier[purchaseOrder.supplierId]
            update.expectedDate = purchaseOrder.expectedDate
            update.totalAmount = totalAmount
        }
        newOrderUpdate?.totalAmount = totalAmount
        return@dbQuery true
    }

    override suspend fun deletePurchaseOrder(purchaseOrderId: String): Boolean = dbQuery {
        val purchaseOrderExists = PurchaseOrders.selectAll().where { PurchaseOrders.id eq purchaseOrderId }.count() > 0
        if (!purchaseOrderExists) {
            throw IllegalArgumentException("product with id $purchaseOrderId does not exist")
        }
        PurchaseOrder.findById(purchaseOrderId)?.delete()
        return@dbQuery true
    }
}