package com.example.routing.supplier

import com.example.commands.queries.supplier.*
import com.example.models.supplier.SupplierUpdate
import com.example.models.util.ApprovalStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.supplierRoutes(){
    route("/supplier") {
        get {
            val suppliers = getAllSuppliers()
            try {
                call.respond(HttpStatusCode.OK,suppliers)
            }catch (e: Exception){
                call.respond(HttpStatusCode.NotFound,"${e.message}")
            }
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: 0
            val supplier = getSupplierById(id)
            try {
                supplier?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: run {
                    call.respond(HttpStatusCode.NotFound, "Supplier not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "${e.message}")
            }
        }
        patch("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: 0
            val status = call.receive<ApprovalStatus>()
            try {
                val supplier = updateSupplierStatus(id, status)
                supplier?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: run {
                    call.respond(HttpStatusCode.NotFound, "Supplier not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "${e.message}")
            }
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: 0
            val supplierUpdateRequest = call.receive<SupplierUpdate>()

            val supplierToUpdate = updateSupplier(id, supplierUpdateRequest)
            try {
                supplierToUpdate?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: run {
                    call.respond(HttpStatusCode.NotFound, "Supplier not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "${e.message}")
            }
        }

        delete("/{id}"){
            val id = call.parameters["id"]?.toInt() ?: 0
            try {
                val supplier = deleteSupplier(id)
                if(supplier){
                    call.respond(HttpStatusCode.OK,"Supplier deleted successfully")
                }else{
                    call.respond(HttpStatusCode.NotFound,"Supplier not found")
                }
            }catch (e: Exception){
                call.respond(HttpStatusCode.InternalServerError,"${e.message}")
            }
        }
    }
}