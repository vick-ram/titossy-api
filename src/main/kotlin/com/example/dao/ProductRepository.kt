package com.example.dao

import com.example.models.ProductRequest
import com.example.models.ProductResponse
import com.example.models.ProductUpdate
import java.util.*

interface ProductRepository {
    suspend fun getProductById(id: UUID): ProductResponse?
    suspend fun getAllProducts(): List<ProductResponse>
    suspend fun searchProduct(query: String): List<ProductResponse>
    suspend fun addProduct(product: ProductRequest): ProductResponse
    suspend fun updateProductQuantity(id: UUID, quantity: Int): Boolean
    suspend fun updateProduct(id: UUID, product: ProductUpdate): Boolean
    suspend fun deleteProduct(id: UUID): Boolean
    suspend fun getProductsBySupplier(supplierId: String): List<ProductResponse>
    suspend fun getLowStockProducts(): List<ProductResponse>
    suspend fun getProductsNeedingReorder(): List<ProductResponse>
}