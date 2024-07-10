package com.example.dao

import com.example.db.order.PurchaseOrder
import com.example.models.purchase_order.PurchaseOrderRequest
import com.example.models.purchase_order.PurchaseOrderResponse
import com.example.models.purchase_order.UpdateOrderStatus
import java.util.UUID

interface PurchaseOrderRepository {
    suspend fun createPurchaseOrder(employeeId: UUID, purchaseOrder: PurchaseOrderRequest): PurchaseOrderResponse
    suspend fun getFilteredPurchaseOrders(filter: (PurchaseOrder) -> Boolean): List<PurchaseOrderResponse>
    suspend fun searchPurchaseOrder(query: String?): List<PurchaseOrderResponse>
    suspend fun updatePurchaseOrderStatus(purchaseOrderId: String, orderStatus: UpdateOrderStatus): Boolean
    suspend fun updatePurchaseOrder(employeeId: UUID, id: String, purchaseOrder: PurchaseOrderRequest): Boolean
    suspend fun deletePurchaseOrder(purchaseOrderId: String): Boolean
}