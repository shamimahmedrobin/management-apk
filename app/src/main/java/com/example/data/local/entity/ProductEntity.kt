package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Product
import com.example.domain.model.ProductStatus

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val purchaseCost: Double,
    val sellingPrice: Double,
    val currentStock: Int,
    val minimumStock: Int = 5,
    val totalSold: Int = 0,
    val status: String = "IN_STOCK"
) {
    fun toDomain(): Product {
        val parsedStatus = try {
            ProductStatus.valueOf(status)
        } catch (e: Exception) {
            ProductStatus.IN_STOCK
        }

        return Product(
            id = id,
            name = name,
            sku = sku,
            category = category,
            purchaseCost = purchaseCost,
            sellingPrice = sellingPrice,
            currentStock = currentStock,
            minimumStock = minimumStock,
            totalSold = totalSold,
            status = parsedStatus
        )
    }

    companion object {
        fun fromDomain(product: Product): ProductEntity {
            return ProductEntity(
                id = product.id,
                name = product.name,
                sku = product.sku,
                category = product.category,
                purchaseCost = product.purchaseCost,
                sellingPrice = product.sellingPrice,
                currentStock = product.currentStock,
                minimumStock = product.minimumStock,
                totalSold = product.totalSold,
                status = product.status.name
            )
        }
    }
}
