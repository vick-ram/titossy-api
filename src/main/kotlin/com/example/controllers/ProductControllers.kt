package com.example.controllers

import com.example.commons.DatabaseUtil.dbQuery
import com.example.commons.customMatch
import com.example.commons.generateRandomString
import com.example.dao.ProductRepository
import com.example.db.Product
import com.example.db.ProductTable
import com.example.db.Supplier
import com.example.db.getFirstLettersOfWords
import com.example.models.ProductRequest
import com.example.models.ProductResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class ProductRepositoryImpl : ProductRepository {
    override suspend fun getProductById(id: UUID): ProductResponse? = dbQuery {
        return@dbQuery Product.findById(id)
            ?.toProductResponse()
    }

    override suspend fun getAllProducts(): List<ProductResponse> = dbQuery {
        return@dbQuery Product.all()
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .map { it.toProductResponse() }
    }

    override suspend fun addProduct(product: ProductRequest): ProductResponse = dbQuery {
        val productExists = Product
            .find { ProductTable.name eq product.name }
            .singleOrNull()
        val supplier = Supplier.findById(product.supplierId)
            ?: throw IllegalArgumentException("Supplier with id ${product.supplierId} not found")
        if (productExists == null) {
            return@dbQuery Product.new {
                this.name = product.name
                this.description = product.description
                this.price = product.unitPrice
                this.image = product.image
                this.stock = product.stock
                this.reorderLevel = product.reorderLevel
                this.sku = getFirstLettersOfWords(this.name) + "-" + generateRandomString(5)
                this.supplier = supplier
                this.tsv = "to_tsvector('english', '${this.name} ${this.description}')"
            }.toProductResponse()
        } else {
            throw IllegalArgumentException("Product already exists")
        }
    }

    override suspend fun updateProductQuantity(
        id: UUID,
        quantity: Int
    ): Boolean = dbQuery {
        Product.findByIdAndUpdate(id) {
            it.stock += quantity
        }
        return@dbQuery true
    }

    override suspend fun updateProduct(
        id: UUID,
        product: ProductRequest
    ): Boolean = dbQuery {
        val supplier = Supplier.findById(product.supplierId)
            ?: throw IllegalArgumentException("Supplier with id ${product.supplierId} not found")
        Product.findByIdAndUpdate(id) { update ->
            update.name = product.name
            update.description = product.description
            update.price = product.unitPrice
            update.image = product.image
            update.stock = product.stock
            update.reorderLevel = product.reorderLevel
            update.supplier = supplier
        }
        return@dbQuery true
    }

    override suspend fun deleteProduct(id: UUID): Boolean = dbQuery {
        val p = Product.find { ProductTable.id eq id }.singleOrNull()
        p?.delete()
            ?: false
        return@dbQuery true

    }

    override suspend fun searchProduct(
        query: String
    ): List<ProductResponse> = dbQuery {
        return@dbQuery ProductTable.selectAll()
            .where { ProductTable.tsv.customMatch(query) }
            .map { Product.wrapRow(it).toProductResponse() }
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
    }

    // NEW: Get products by supplier
    override suspend fun getProductsBySupplier(supplierId: String): List<ProductResponse> = dbQuery {
        return@dbQuery Product.find { ProductTable.supplier eq supplierId }
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .map { it.toProductResponse() }
    }

    // NEW: Get low stock products (below reorder level)
    override suspend fun getLowStockProducts(): List<ProductResponse> = dbQuery {
        return@dbQuery Product.find { ProductTable.stock less ProductTable.reorderLevel }
            .sortedBy { it.stock } // Sort by most critical first
            .map { it.toProductResponse() }
    }

    // NEW: Get products needing reorder (stock <= reorderLevel)
    override suspend fun getProductsNeedingReorder(): List<ProductResponse> = dbQuery {
        return@dbQuery Product.find { ProductTable.stock lessEq ProductTable.reorderLevel }
            .sortedBy { it.stock }
            .map { it.toProductResponse() }
    }
}