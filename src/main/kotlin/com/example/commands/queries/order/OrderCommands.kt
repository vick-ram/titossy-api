package com.example.commands.queries.order

import com.example.db.employee.Employee
import com.example.db.order.Order
import com.example.db.order.OrderItem
import com.example.db.order.OrderTable
import com.example.db.product.Product
import com.example.db.supplier.Supplier
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.order.OrderItemResponse
import com.example.models.order.OrderRequest
import com.example.models.order.OrderResponse
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private fun OrderItem.toOrderItemResponse() = OrderItemResponse(
    id = this.id.value,
    orderId = this.id.value,
    productId = this.productId.id.value,
    quantity = this.quantity,
    price = this.price.toDouble()
)

private fun Order.toOrderResponse() = OrderResponse(
    oderId = this.id.value,
    employeeId = this.employeeId.id.value,
    supplierId = this.supplierId.id.value,
    orderDate = Date.from(this.orderDate.atZone(ZoneId.systemDefault()).toInstant()),
    orderItems = this.orderItems.map { it.toOrderItemResponse() },
    total = this.totalAmount.toDouble(),
    orderStatus = this.orderStatus
)

suspend fun createOrder(orderRequest: OrderRequest): OrderResponse {
    return dbQuery {
        val newOrder = Order.new {
            employeeId = Employee[orderRequest.employeeId]
            supplierId = Supplier[orderRequest.supplierId]
            orderDate = LocalDateTime.now()
            totalAmount = orderItems.sumOf { it.price * it.quantity.toBigDecimal() }
            orderStatus = "PENDING"
        }
        orderRequest.orderItems.forEach {
            OrderItem.new {
                orderId = newOrder
                productId = Product[it.productId]
                quantity = it.quantity
                price = Product[it.productId].price
            }
        }
        newOrder.toOrderResponse()
    }
}

suspend fun getOrders(): List<OrderResponse> = dbQuery {
    return@dbQuery try {
        Order.all().map(Order::toOrderResponse)
    }catch (e: Exception){
        emptyList()
    }
}

suspend fun getOrderById(orderId: Int): OrderResponse? = dbQuery {
    return@dbQuery try {
        Order.findById(orderId)?.toOrderResponse()
    }catch (e: Exception) {
        null
    }
}

suspend fun getOrdersByEmployeeId(employeeId: Int): List<OrderResponse> = dbQuery {
    return@dbQuery try {
        Order.find { OrderTable.employeeId eq employeeId }.map(Order::toOrderResponse)
    }catch (e: Exception){
        emptyList()
    }
}

suspend fun getOrdersBySupplierId(supplierId: Int): List<OrderResponse> = dbQuery {
    return@dbQuery try {
        Order.find { OrderTable.supplierId eq supplierId }.map(Order::toOrderResponse)
    }catch (e: Exception){
        emptyList()
    }
}

suspend fun getOrdersByDate(date: Date): List<OrderResponse> = dbQuery {
    return@dbQuery try {
        val localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        val startOfDay = localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0)
        Order.find {
            OrderTable.orderDate greaterEq startOfDay and
                    (OrderTable.orderDate less localDateTime.plusDays(1).withHour(0).withMinute(0).withSecond(0)
                        .withNano(0))
        }.map { it.toOrderResponse() }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun getOrdersByStatus(orderStatus: String): List<OrderResponse> = dbQuery {
    return@dbQuery try {
        Order.find { OrderTable.orderStatus eq orderStatus }.map(Order::toOrderResponse)
    }catch (e: Exception){
        emptyList()
    }
}

suspend fun updateOrderStatus(orderId: Int, orderStatus: String): OrderResponse? = dbQuery {
    return@dbQuery try {
        val order = Order.findById(orderId) ?: return@dbQuery null
        order.orderStatus = orderStatus
        order.toOrderResponse()
    } catch (e: Exception){
        null
    }
}

suspend fun deleteOrder(orderId: Int): Boolean = dbQuery {
    return@dbQuery try {
        val order = Order.findById(orderId) ?: return@dbQuery false
        order.delete()
        true
    }catch (e: Exception){
        false
    }
}