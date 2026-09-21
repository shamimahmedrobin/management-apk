package com.example.domain.model

enum class AccountType(val defaultName: String) {
    CASH("Cash"),
    BKASH("bKash"),
    NAGAD("Nagad"),
    ROCKET("Rocket"),
    BANK("Bank"),
    COURIER("Courier"),
    CUSTOM("Custom")
}

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val accountNumber: String = "",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val totalMoneyIn: Double = 0.0,
    val totalMoneyOut: Double = 0.0,
    val transactionCount: Int = 0,
    val isSystemDefault: Boolean = false
)
