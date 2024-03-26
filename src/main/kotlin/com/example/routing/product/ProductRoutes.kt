package com.example.routing.product

import com.example.commands.queries.product.createProduct
import com.example.commands.queries.product.deleteProduct
import com.example.commands.queries.product.searchProducts
import com.example.commands.queries.product.updateProduct
import com.example.models.product.ProductRequest
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes() {
    route("/product") {

        post {
            val product = call.receive<ProductRequest>()
            val createdProduct = createProduct(product)
            createdProduct?.let {
                call.respond(it)
            } ?: call.respond("Product creation failed")
        }

        get("/search") {
            val query = call.request.queryParameters["query"] ?: ""
            val products = searchProducts(query)
            products?.let {
                call.respond(it)
            } ?: call.respond("No products found")
        }
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val product = call.receive<ProductRequest>()
            try {
                val updatedProduct = id?.let { updateProduct(it, product) }
                updatedProduct?.let {
                    call.respond(it)
                } ?: call.respond("Product update failed")
            }catch (e: Exception){
                call.respond("${e.message}")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            try {
                val product = id?.let { deleteProduct(it) }
                product?.let {
                    call.respond(it)
                } ?: call.respond("Product deletion failed")
            }catch (e: Exception){
                call.respond("${e.message}")
            }
        }
    }
}