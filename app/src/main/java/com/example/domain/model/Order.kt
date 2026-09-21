package com.example.domain.model

enum class OrderStatus(val displayName: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    RETURNED("Returned")
}

enum class CourierOption(val displayName: String) {
    PATHAO("Pathao"),
    STEADFAST("Steadfast"),
    REDX("RedX"),
    PAPERFLY("Paperfly"),
    OTHER("Other")
}

enum class PaymentMethod(val displayName: String) {
    COD("Cash on Delivery (COD)"),
    BKASH("bKash"),
    NAGAD("Nagad"),
    ROCKET("Rocket"),
    BANK("Bank"),
    CASH("Cash")
}

data class Order(
    val id: String,
    val customerName: String,
    val phoneNumber: String,
    val deliveryAddress: String = "",
    val productName: String,
    val quantity: Int = 1,
    val productCost: Double,
    val sellingPrice: Double,
    val deliveryCharge: Double = 0.0,
    val courier: CourierOption = CourierOption.STEADFAST,
    val paymentMethod: PaymentMethod = PaymentMethod.COD,
    val orderDateMillis: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.PENDING,
    val deliveryStatus: String = "In Transit",
    val trackingCode: String = "",
    val notes: String = ""
) {
    val estimatedProfit: Double
        get() = (sellingPrice * quantity) - (productCost * quantity)
}
