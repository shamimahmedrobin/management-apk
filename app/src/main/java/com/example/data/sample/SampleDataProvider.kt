package com.example.data.sample

import com.example.domain.model.*

object SampleDataProvider {

    fun getDefaultAccounts(): List<Account> {
        return listOf(
            Account(
                id = "acc_cash",
                name = "Cash / ক্যাশ",
                type = AccountType.CASH,
                accountNumber = "Cash Drawer",
                openingBalance = 0.0,
                currentBalance = 0.0,
                isSystemDefault = true
            ),
            Account(
                id = "acc_bkash",
                name = "bKash / বিকাশ",
                type = AccountType.BKASH,
                accountNumber = "bKash Account",
                openingBalance = 0.0,
                currentBalance = 0.0,
                isSystemDefault = true
            ),
            Account(
                id = "acc_nagad",
                name = "Nagad / নগদ",
                type = AccountType.NAGAD,
                accountNumber = "Nagad Account",
                openingBalance = 0.0,
                currentBalance = 0.0,
                isSystemDefault = true
            ),
            Account(
                id = "acc_rocket",
                name = "Rocket / রকেট",
                type = AccountType.ROCKET,
                accountNumber = "Rocket Account",
                openingBalance = 0.0,
                currentBalance = 0.0,
                isSystemDefault = true
            ),
            Account(
                id = "acc_bank",
                name = "Bank Account / ব্যাংক",
                type = AccountType.BANK,
                accountNumber = "Bank Account",
                openingBalance = 0.0,
                currentBalance = 0.0,
                isSystemDefault = true
            ),
            Account(
                id = "acc_courier",
                name = "Courier COD Reserve",
                type = AccountType.COURIER,
                accountNumber = "Steadfast, Pathao & CarryBee",
                openingBalance = 0.0,
                currentBalance = 0.0,
                isSystemDefault = true
            )
        )
    }

    fun getSampleTransactions(): List<Transaction> = emptyList()

    fun getSampleOrders(): List<Order> = emptyList()

    fun getSampleProducts(): List<Product> = emptyList()

    fun getSampleCustomers(): List<Customer> = emptyList()

    fun getSampleSuppliers(): List<Supplier> = emptyList()
}
