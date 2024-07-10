package com.example.commands.queries.order

import com.example.dao.PurchaseOrderRepository
import com.example.db.cart.ProductCartEntity
import com.example.db.cart.ProductCarts
import com.example.db.employee.Employee
import com.example.db.order.PurchaseOrder
import com.example.db.order.PurchaseOrderItem
import com.example.db.order.PurchaseOrders
import com.example.db.product.Product
import com.example.db.supplier.Supplier
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.purchase_order.OrderStatus
import com.example.models.purchase_order.PurchaseOrderRequest
import com.example.models.purchase_order.PurchaseOrderResponse
import com.example.models.purchase_order.UpdateOrderStatus
import org.jetbrains.exposed.sql.selectAll
import java.math.BigDecimal
import java.util.*

class PurchaseOrderRepositoryImpl : PurchaseOrderRepository {
    override suspend fun createPurchaseOrder(
        employeeId: UUID,
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

    override suspend fun getFilteredPurchaseOrders(filter: (PurchaseOrder) -> Boolean): List<PurchaseOrderResponse> =
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
            if (orderToUpdate != null && orderStatus.status == OrderStatus.DELIVERED) {
                orderToUpdate.purchaseOrderItems.forEach { item ->
                    val product = item.product
                    product.stock += item.quantity
                    product.flush()
                }
            }
            orderToUpdate?.orderStatus = orderStatus.status
            orderToUpdate?.flush()
            /*PurchaseOrder.findByIdAndUpdate(purchaseOrderId) { update ->
                update.orderStatus = orderStatus.status
            }*/
            return@dbQuery true
        }

    override suspend fun updatePurchaseOrder(
        employeeId: UUID,
        id: String,
        purchaseOrder: PurchaseOrderRequest
    ): Boolean = dbQuery {
        val purchaseOrderFromDb = PurchaseOrders.selectAll().where { PurchaseOrders.id eq id }
        val purchaseOrderExists = purchaseOrderFromDb.count() > 0
        var totalAmount = BigDecimal.ZERO
        if (!purchaseOrderExists) {
            throw IllegalArgumentException("product with id $id does not exist")
        }
        val newOrderUpdate = PurchaseOrder.findByIdAndUpdate(id) { update ->
            update.employee = Employee[employeeId]
            update.supplier = Supplier[purchaseOrder.supplierId]
            update.expectedDate = purchaseOrder.expectedDate
            update.totalAmount = totalAmount
        }

        purchaseOrder.products?.forEach { purchaseOrderItem ->
            val subtotal = purchaseOrderItem.unitPrice.times(purchaseOrderItem.quantity.toBigDecimal())
            PurchaseOrderItem.new {
                this.order = newOrderUpdate!!
                this.product = Product[purchaseOrderItem.productId]
                this.quantity = purchaseOrderItem.quantity
                this.unitPrice = purchaseOrderItem.unitPrice
                this.subtotal = subtotal
            }
            totalAmount += subtotal
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