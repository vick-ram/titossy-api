package com.example.commands.queries.cart

import com.example.db.cart.ProductCartEntity
import com.example.db.cart.ProductCartResponse
import com.example.db.cart.ProductCarts
import com.example.db.employee.Employee
import com.example.db.product.Product
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.cart.ProductCart
import org.jetbrains.exposed.sql.and
import java.util.*

suspend fun addProductToCart(employeeId: UUID, productCart: ProductCart): Boolean = dbQuery {
    val productExist = ProductCartEntity
        .find { (ProductCarts.employee eq employeeId) and (ProductCarts.product eq productCart.productId) }
        .singleOrNull()
    if (productExist != null) {
        productExist.quantity += productCart.quantity
        return@dbQuery false
    }
    ProductCartEntity.new {
        this.employee = Employee[employeeId]
        this.product = Product[productCart.productId]
        this.quantity = productCart.quantity
    }
    return@dbQuery true
}

suspend fun getProductsInCart(
    employeeId: UUID
): List<ProductCartResponse> = dbQuery {
    ProductCartEntity.find {
        ProductCarts.employee eq employeeId
    }.map { it.toProductCartResponse() }
}

suspend fun removeProductFromCart(
    employeeId: UUID,
    productId: UUID
): Boolean = dbQuery {
    val product = ProductCartEntity.find {
        (ProductCarts.employee eq employeeId) and
                (ProductCarts.product eq productId)
    }.singleOrNull()
    product?.delete()
    return@dbQuery true
}

suspend fun clearCart(employeeId: UUID) = dbQuery {
    ProductCartEntity.find { ProductCarts.employee eq employeeId }
        .forEach { it.delete() }
}