package com.example.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

enum class IncomeCategory(val displayName: String) {
    CAPITAL("Capital (মূলধন)"),
    PRODUCT_SALE("Product Sale"),
    COURIER_COLLECTION("Courier Collection"),
    BKASH("bKash"),
    NAGAD("Nagad"),
    ROCKET("Rocket"),
    BANK("Bank"),
    CASH("Cash"),
    OTHER("Other Income")
}

enum class ExpenseCategory(val displayName: String) {
    PRODUCT_PURCHASE("Product Purchase"),
    PACKAGING("Packaging"),
    COURIER("Courier"),
    FACEBOOK_ADS("Facebook Ads"),
    INSTAGRAM_ADS("Instagram Ads"),
    GOOGLE_ADS("Google Ads"),
    WEBSITE("Website"),
    HOSTING("Hosting"),
    DOMAIN("Domain"),
    SALARY("Salary"),
    TRANSPORT("Transport"),
    OFFICE("Office"),
    ELECTRICITY("Electricity"),
    INTERNET("Internet"),
    PHONE("Phone"),
    REFUND("Refund"),
    OTHER("Other")
}

data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val dateMillis: Long,
    val timeString: String,
    val categoryName: String,
    val accountId: String,
    val destinationAccountId: String? = null, // for transfers
    val description: String = "",
    val reference: String = "",
    val orderId: String? = null,
    val customerName: String? = null,
    val notes: String = "",
    val receiptAttachmentUri: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)
