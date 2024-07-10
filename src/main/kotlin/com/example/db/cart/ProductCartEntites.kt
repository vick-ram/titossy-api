package com.example.db.cart

import com.example.db.employee.Employee
import com.example.db.employee.EmployeeTable
import com.example.db.product.Product
import com.example.db.product.ProductTable
import com.example.models.util.BigDecimalSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.math.BigDecimal

object ProductCarts : IntIdTable("product_carts") {
    val employee = reference("inventory", EmployeeTable, onDelete = ReferenceOption.CASCADE)
    val product = reference("product_id", ProductTable, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("qty")
}

class ProductCartEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProductCartEntity>(ProductCarts)

    var employee by Employee referencedOn ProductCarts.employee
    var product by Product referencedOn ProductCarts.product
    var quantity by ProductCarts.quantity

    fun toProductCartResponse() = ProductCartResponse(
        product = ProductInCart(
            name = product.name,
            price = product.price,
            stock = product.stock
        ),
        quantity = quantity
    )

}

@Serializable
data class ProductCartResponse(
    val product: ProductInCart,
    val quantity: Int
)

@Serializable
data class ProductInCart(
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val stock: Int
)