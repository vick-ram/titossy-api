package com.example.commands.queries.product

import com.example.commands.customMatch
import com.example.db.product.Product
import com.example.db.product.ProductTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.product.ProductRequest
import com.example.models.product.ProductResponse
import org.jetbrains.exposed.sql.*

private fun Product.toProductResponse() = ProductResponse(
    productId = this.id.value,
    name = this.name,
    description = this.description,
    price = this.price.toDouble(),
    image = this.image
)

suspend fun createProduct(productRequest: ProductRequest): ProductResponse? = dbQuery {
    return@dbQuery try {
        Product.new {
            this.name = productRequest.name
            this.description = productRequest.description
            this.price = productRequest.price.toBigDecimal()
            this.image = productRequest.image!!
        }.toProductResponse()
    } catch (e: Exception) {
        null
    }
}

suspend fun getAllProducts(): List<ProductResponse> = dbQuery {
    return@dbQuery try {
        Product.all().limit(100)
            .map { it.toProductResponse() }
    } catch (e: Exception){
        emptyList()
    }
}

suspend fun getProductById(id: Int): ProductResponse? = dbQuery {
    return@dbQuery try {
        val product = Product.findById(id)
        product?.toProductResponse()
    }catch (e: Exception){
        null
    }
}

suspend fun updateProduct(id: Int, productRequest: ProductRequest): ProductResponse? = dbQuery {
    return@dbQuery try {
        val product = Product.findById(id)
        product?.apply {
            this.name = productRequest.name
            this.description = productRequest.description
            this.price = productRequest.price.toBigDecimal()
            this.image = productRequest.image!!
        }?.toProductResponse()
    }catch (e: Exception){
        null
    }
}

suspend fun searchProducts(query: String): List<ProductResponse>? = dbQuery {
    return@dbQuery try {
        ProductTable.selectAll().where { ProductTable.tsv.customMatch(query) }.map {
                Product.wrapRow(it).toProductResponse()
            }
    } catch (e: Exception) {
        null
    }
}

suspend fun deleteProduct(id: Int): Boolean = dbQuery {
    return@dbQuery try {
        Product.findById(id)?.delete()
        true
    }catch (e: Exception){
        false
    }
}
