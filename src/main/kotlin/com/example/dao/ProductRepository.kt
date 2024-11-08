package com.example.dao

import com.example.models.ProductRequest
import com.example.models.ProductResponse
import java.util.*

interface ProductRepository {
    suspend fun getProductById(id: UUID): ProductResponse?
    suspend fun getAllProducts(): List<ProductResponse>
    suspend fun searchProduct(query: String): List<ProductResponse>
    suspend fun addProduct(product: ProductRequest): ProductResponse
    suspend fun updateProductQuantity(id: UUID, quantity: Int): Boolean
    suspend fun updateProduct(id: UUID, product: ProductRequest): Boolean
    suspend fun deleteProduct(id: UUID): Boolean
}