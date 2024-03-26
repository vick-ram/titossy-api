package com.example.routing.inventory

import com.example.commands.queries.inventory.createInventory
import com.example.commands.queries.inventory.deleteInventory
import com.example.commands.queries.inventory.getAllInventoryItems
import com.example.commands.queries.inventory.getInventoryById
import com.example.models.inventory.InventoryRequest
import com.example.routing.withRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.inventoryRoutes() {
    /*
    * Only employees with role INVENTORY
    */
    authenticate("auth-jwt") {
        withRole("INVENTORY") {
            post {
                val inventory = call.receive<InventoryRequest>()
                try {
                    createInventory(inventory)
                    call.respond(HttpStatusCode.Created, "Inventory created successfully")
                } catch (e: Exception) {
                    call.respond("${e.message}")
                }
            }

            get {
                val allInventory = getAllInventoryItems()

                try {
                    allInventory?.let {
                        call.respond(HttpStatusCode.OK, it)
                    }
                } catch (e: Exception) {
                    call.respond("${e.message}")
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toInt() ?: 0
                val inventory = getInventoryById(id)
                try {
                    inventory?.let {
                        call.respond(HttpStatusCode.OK, it)
                    }
                } catch (e: Exception) {
                    call.respond("${e.message}")
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toInt() ?: 0
                try {
                    deleteInventory(id)
                    call.respond(HttpStatusCode.Conflict, "Inventory deleted successfully")
                } catch (e: Exception) {
                    call.respond("${e.message}")
                }
            }
        }
    }
}