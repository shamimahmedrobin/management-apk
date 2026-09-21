package com.example.domain.model

enum class ProductStatus(val displayName: String) {
    IN_STOCK("In Stock"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock"),
    DISCONTINUED("Discontinued")
}

data class Product(
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val purchaseCost: Double,
    val sellingPrice: Double,
    val currentStock: Int,
    val minimumStock: Int = 5,
    val totalSold: Int = 0,
    val status: ProductStatus = ProductStatus.IN_STOCK
) {
    val profitPerItem: Double
        get() = sellingPrice - purchaseCost

    val profitMarginPercent: Double
        get() = if (sellingPrice > 0) ((sellingPrice - purchaseCost) / sellingPrice) * 100 else 0.0

    val isLowStock: Boolean
        get() = currentStock > 0 && currentStock <= minimumStock

    val isOutOfStock: Boolean
        get() = currentStock <= 0
}
