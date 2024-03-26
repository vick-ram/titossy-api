package com.example.commands.queries.inventory

import com.example.db.employee.Employee
import com.example.db.inventory.Inventory
import com.example.db.inventory.InventoryTable
import com.example.db.product.Product
import com.example.db.product.ProductTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.inventory.InventoryRequest
import com.example.models.inventory.InventoryResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private fun Inventory.toInventoryResponse() = InventoryResponse(
    inventoryId = this.id.value,
    productId = this.product.id.value,
    employeeId = this.employee.id.value,
    quantity = this.quantity,
    lastUpdate = Date.from(this.lastUpdate.atZone(ZoneId.systemDefault()).toInstant())

)
suspend fun createInventory(inventoryRequest: InventoryRequest) = dbQuery {
    val inventory = Inventory.new {
        product = Product[inventoryRequest.productId]
        employee = Employee[inventoryRequest.employeeId]
        quantity = inventoryRequest.quantity
        lastUpdate = LocalDateTime.now()
    }
    inventory.toInventoryResponse()
}

suspend fun getInventoryById(inventoryId: Int): InventoryResponse? = dbQuery {
    return@dbQuery try {
        val inventory = Inventory.findById(inventoryId)
        inventory?.toInventoryResponse()
    } catch (e: Exception) {
        null
    }
}


suspend fun getAllInventoryItems(): List<InventoryResponse>? = dbQuery {
    return@dbQuery try {
        Inventory.all().map { it.toInventoryResponse() }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun updateInventory(inventoryId: Int, inventoryRequest: InventoryRequest) = dbQuery {
    val inventory = Inventory.findById(inventoryId) ?: throw IllegalArgumentException("No inventory found for id $inventoryId")
    inventory.apply {
        product = Product[inventoryRequest.productId]
        employee = Employee[inventoryRequest.employeeId]
        quantity = inventoryRequest.quantity
        lastUpdate = LocalDateTime.now()
    }.toInventoryResponse()
}

suspend fun getInventoryByProductId(productId: Int): List<InventoryResponse>? = dbQuery {
    return@dbQuery try {
        Inventory.find { InventoryTable.productId eq productId }.map { it.toInventoryResponse() }
    } catch (e: Exception) {
        emptyList()
    }
}


suspend fun deleteInventory(inventoryId: Int): Boolean = dbQuery {
    return@dbQuery try {
        val inventory = Inventory.findById(inventoryId) ?: return@dbQuery false
        inventory.delete()
        true
    } catch (e: Exception) {
        false
    }
}
