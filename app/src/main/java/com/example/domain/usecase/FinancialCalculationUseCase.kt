package com.example.domain.usecase

import com.example.domain.model.*
import java.util.Calendar

class FinancialCalculationUseCase {

    fun filterTransactionsByPeriod(
        transactions: List<Transaction>,
        period: ReportPeriod,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null
    ): List<Transaction> {
        if (period == ReportPeriod.LIFETIME) return transactions

        val (startMillis, endMillis) = getDateRangeForPeriod(period, customStartMillis, customEndMillis)
        return transactions.filter { it.dateMillis in startMillis..endMillis }
    }

    fun filterOrdersByPeriod(
        orders: List<Order>,
        period: ReportPeriod,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null
    ): List<Order> {
        if (period == ReportPeriod.LIFETIME) return orders

        val (startMillis, endMillis) = getDateRangeForPeriod(period, customStartMillis, customEndMillis)
        return orders.filter { it.orderDateMillis in startMillis..endMillis }
    }

    fun getDateRangeForPeriod(
        period: ReportPeriod,
        customStart: Long? = null,
        customEnd: Long? = null
    ): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        return when (period) {
            ReportPeriod.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
            ReportPeriod.YESTERDAY -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
            ReportPeriod.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
            ReportPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
            ReportPeriod.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
            ReportPeriod.CUSTOM -> {
                val start = customStart ?: 0L
                val end = customEnd ?: now
                Pair(start, end)
            }
            ReportPeriod.LIFETIME -> Pair(0L, Long.MAX_VALUE)
        }
    }

    fun calculateFinancialSummary(
        transactions: List<Transaction>,
        orders: List<Order>
    ): FinancialSummary {
        var totalIncome = 0.0
        var totalExpense = 0.0
        var adCost = 0.0
        var deliveryCostExpense = 0.0
        var productCostExpense = 0.0
        var otherExpense = 0.0

        for (tx in transactions) {
            when (tx.type) {
                TransactionType.INCOME -> totalIncome += tx.amount
                TransactionType.EXPENSE -> {
                    totalExpense += tx.amount
                    when {
                        tx.categoryName.contains("Ads", ignoreCase = true) -> adCost += tx.amount
                        tx.categoryName.contains("Courier", ignoreCase = true) -> deliveryCostExpense += tx.amount
                        tx.categoryName.contains("Purchase", ignoreCase = true) -> productCostExpense += tx.amount
                        else -> otherExpense += tx.amount
                    }
                }
                TransactionType.TRANSFER -> {
                    // Critical requirement: Transfers MUST NOT affect income, expense, or profit
                }
            }
        }

        val netProfit = totalIncome - totalExpense
        val profitMargin = if (totalIncome > 0) (netProfit / totalIncome) * 100 else 0.0

        val totalOrders = orders.size
        val deliveredOrders = orders.count { it.status == OrderStatus.DELIVERED }
        val cancelledOrders = orders.count { it.status == OrderStatus.CANCELLED }
        val returnedOrders = orders.count { it.status == OrderStatus.RETURNED }
        val pendingOrders = orders.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PROCESSING || it.status == OrderStatus.SHIPPED || it.status == OrderStatus.CONFIRMED }

        val grossSales = orders.sumOf { it.sellingPrice * it.quantity }
        val orderProductCosts = orders.sumOf { it.productCost * it.quantity }
        val orderDeliveryCosts = orders.sumOf { it.deliveryCharge }

        val aov = if (totalOrders > 0) grossSales / totalOrders else 0.0
        val avgProfitPerOrder = if (totalOrders > 0) {
            val totalOrderProfit = orders.sumOf { it.estimatedProfit }
            totalOrderProfit / totalOrders
        } else 0.0

        return FinancialSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netProfit = netProfit,
            profitMarginPercent = profitMargin,
            totalOrders = totalOrders,
            deliveredOrders = deliveredOrders,
            cancelledOrders = cancelledOrders,
            returnedOrders = returnedOrders,
            pendingOrders = pendingOrders,
            grossSales = grossSales,
            productCost = if (productCostExpense > 0) productCostExpense else orderProductCosts,
            deliveryCost = if (deliveryCostExpense > 0) deliveryCostExpense else orderDeliveryCosts,
            advertisingCost = adCost,
            otherExpenses = otherExpense,
            averageOrderValue = aov,
            averageProfitPerOrder = avgProfitPerOrder
        )
    }

    fun calculateCategoryExpenses(transactions: List<Transaction>): List<CategoryExpenseItem> {
        val expenseTx = transactions.filter { it.type == TransactionType.EXPENSE }
        val totalExpense = expenseTx.sumOf { it.amount }
        if (totalExpense <= 0) return emptyList()

        return expenseTx.groupBy { it.categoryName }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                CategoryExpenseItem(
                    categoryName = cat,
                    amount = sum,
                    percentage = (sum / totalExpense) * 100,
                    transactionCount = list.size
                )
            }
            .sortedByDescending { it.amount }
    }

    fun calculateCourierSummaries(orders: List<Order>): List<CourierSummary> {
        return CourierOption.values().map { courier ->
            val courierOrders = orders.filter { it.courier == courier }
            val total = courierOrders.size
            val delivered = courierOrders.count { it.status == OrderStatus.DELIVERED }
            val cancelled = courierOrders.count { it.status == OrderStatus.CANCELLED }
            val returned = courierOrders.count { it.status == OrderStatus.RETURNED }
            val pending = total - delivered - cancelled - returned

            val totalCod = courierOrders.sumOf { (it.sellingPrice * it.quantity) + it.deliveryCharge }
            val deliveredOrders = courierOrders.filter { it.status == OrderStatus.DELIVERED }
            val receivedAmount = deliveredOrders.sumOf { (it.sellingPrice * it.quantity) + it.deliveryCharge }
            val pendingOrders = courierOrders.filter { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED && it.status != OrderStatus.RETURNED }
            val pendingCollection = pendingOrders.sumOf { (it.sellingPrice * it.quantity) + it.deliveryCharge }
            val courierCharges = courierOrders.sumOf { it.deliveryCharge }

            CourierSummary(
                courierOption = courier,
                totalParcels = total,
                delivered = delivered,
                cancelled = cancelled,
                returned = returned,
                pending = pending,
                totalCodAmount = totalCod,
                receivedAmount = receivedAmount,
                pendingCollection = pendingCollection,
                courierCharges = courierCharges
            )
        }
    }

    fun calculateAccountBalances(
        accounts: List<Account>,
        transactions: List<Transaction>
    ): List<Account> {
        return accounts.map { account ->
            var moneyIn = 0.0
            var moneyOut = 0.0
            var txCount = 0

            for (tx in transactions) {
                when (tx.type) {
                    TransactionType.INCOME -> {
                        if (tx.accountId == account.id) {
                            moneyIn += tx.amount
                            txCount++
                        }
                    }
                    TransactionType.EXPENSE -> {
                        if (tx.accountId == account.id) {
                            moneyOut += tx.amount
                            txCount++
                        }
                    }
                    TransactionType.TRANSFER -> {
                        // Source account: decrease balance (money out)
                        if (tx.accountId == account.id) {
                            moneyOut += tx.amount
                            txCount++
                        }
                        // Destination account: increase balance (money in)
                        if (tx.destinationAccountId == account.id) {
                            moneyIn += tx.amount
                            txCount++
                        }
                    }
                }
            }

            account.copy(
                currentBalance = account.openingBalance + moneyIn - moneyOut,
                totalMoneyIn = moneyIn,
                totalMoneyOut = moneyOut,
                transactionCount = txCount
            )
        }
    }
}
