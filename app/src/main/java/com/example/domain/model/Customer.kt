package com.example.domain.model

data class Customer(
    val id: String,
    val name: String,
    val phone: String,
    val address: String = "",
    val totalOrders: Int = 0,
    val totalPurchase: Double = 0.0,
    val dueAmount: Double = 0.0,
    val notes: String = ""
)

data class Supplier(
    val id: String,
    val name: String,
    val phone: String,
    val address: String = "",
    val totalPurchase: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val contactPerson: String = ""
)
