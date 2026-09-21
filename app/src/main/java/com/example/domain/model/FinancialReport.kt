package com.example.domain.model

data class CourierSummary(
    val courierOption: CourierOption,
    val totalParcels: Int = 0,
    val delivered: Int = 0,
    val cancelled: Int = 0,
    val returned: Int = 0,
    val pending: Int = 0,
    val totalCodAmount: Double = 0.0,
    val receivedAmount: Double = 0.0,
    val pendingCollection: Double = 0.0,
    val courierCharges: Double = 0.0
)

enum class ReportPeriod(val displayName: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    LIFETIME("Lifetime"),
    CUSTOM("Custom")
}

data class CategoryExpenseItem(
    val categoryName: String,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int
)

data class FinancialSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMarginPercent: Double = 0.0,
    val totalOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val returnedOrders: Int = 0,
    val pendingOrders: Int = 0,
    val grossSales: Double = 0.0,
    val productCost: Double = 0.0,
    val deliveryCost: Double = 0.0,
    val advertisingCost: Double = 0.0,
    val otherExpenses: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val averageProfitPerOrder: Double = 0.0
)
