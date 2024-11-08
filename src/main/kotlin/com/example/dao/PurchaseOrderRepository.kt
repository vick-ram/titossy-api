package com.example.dao

import com.example.db.PurchaseOrder
import com.example.models.PurchaseOrderRequest
import com.example.models.PurchaseOrderResponse
import com.example.models.UpdateOrderStatus

interface PurchaseOrderRepository {
    suspend fun createPurchaseOrder(
        employeeId: String,
        purchaseOrder: PurchaseOrderRequest
    ): PurchaseOrderResponse
    suspend fun getFilteredPurchaseOrders(
        filter: (PurchaseOrder) -> Boolean
    ): List<PurchaseOrderResponse>
    suspend fun searchPurchaseOrder(
        query: String?
    ): List<PurchaseOrderResponse>
    suspend fun updatePurchaseOrderStatus(
        purchaseOrderId: String,
        orderStatus: UpdateOrderStatus
    ): Boolean
    suspend fun updatePurchaseOrder(
        employeeId: String,
        id: String,
        purchaseOrder: PurchaseOrderRequest
    ): Boolean
    suspend fun deletePurchaseOrder(purchaseOrderId: String): Boolean
}