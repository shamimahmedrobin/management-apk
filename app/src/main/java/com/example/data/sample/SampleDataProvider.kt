package com.example.data.sample

import com.example.domain.model.*
import java.util.Calendar

object SampleDataProvider {

    fun getDefaultAccounts(): List<Account> {
        return listOf(
            Account(
                id = "acc_cash",
                name = "Cash Drawer",
                type = AccountType.CASH,
                accountNumber = "Outlet Vault",
                openingBalance = 25000.0,
                currentBalance = 25000.0,
                isSystemDefault = true
            ),
            Account(
                id = "acc_bkash",
                name = "bKash Merchant",
                type = AccountType.BKASH,
                accountNumber = "01712-345678",
                openingBalance = 85000.0,
                currentBalance = 85000.0,
                isSystemDefault = true
            ),
            Account(
                id = "acc_nagad",
                name = "Nagad Merchant",
                type = AccountType.NAGAD,
                accountNumber = "01844-987654",
                openingBalance = 42000.0,
                currentBalance = 42000.0,
                isSystemDefault = true
            ),
            Account(
                id = "acc_rocket",
                name = "Rocket",
                type = AccountType.ROCKET,
                accountNumber = "01911-554433-2",
                openingBalance = 18000.0,
                currentBalance = 18000.0,
                isSystemDefault = true
            ),
            Account(
                id = "acc_bank",
                name = "BRAC Bank (Corp)",
                type = AccountType.BANK,
                accountNumber = "1501-2049-5832-001",
                openingBalance = 240000.0,
                currentBalance = 240000.0,
                isSystemDefault = true
            ),
            Account(
                id = "acc_courier",
                name = "Courier COD Reserve",
                type = AccountType.COURIER,
                accountNumber = "Steadfast & Pathao Hub",
                openingBalance = 65000.0,
                currentBalance = 65000.0,
                isSystemDefault = true
            )
        )
    }

    fun getSampleTransactions(): List<Transaction> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        // Days offsets
        fun daysAgo(days: Int, hour: Int = 11, minute: Int = 30): Long {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -days)
            c.set(Calendar.HOUR_OF_DAY, hour)
            c.set(Calendar.MINUTE, minute)
            return c.timeInMillis
        }

        return listOf(
            // Today's Transactions
            Transaction(
                id = "TX-1001",
                type = TransactionType.INCOME,
                amount = 4850.0,
                dateMillis = daysAgo(0, 10, 15),
                timeString = "10:15 AM",
                categoryName = IncomeCategory.PRODUCT_SALE.displayName,
                accountId = "acc_bkash",
                reference = "TRX-BK-92813",
                orderId = "ORD-8821",
                customerName = "Tanvir Ahmed",
                description = "Royal Silk Panjabi prepaid order via bKash",
                notes = "Customer verified delivery address in Dhanmondi"
            ),
            Transaction(
                id = "TX-1002",
                type = TransactionType.INCOME,
                amount = 12400.0,
                dateMillis = daysAgo(0, 11, 45),
                timeString = "11:45 AM",
                categoryName = IncomeCategory.COURIER_COLLECTION.displayName,
                accountId = "acc_courier",
                reference = "ST-COD-4092",
                description = "Steadfast Courier COD Daily Disbursement",
                notes = "Batch payout for 5 delivered parcels"
            ),
            Transaction(
                id = "TX-1003",
                type = TransactionType.EXPENSE,
                amount = 3500.0,
                dateMillis = daysAgo(0, 13, 20),
                timeString = "01:20 PM",
                categoryName = ExpenseCategory.FACEBOOK_ADS.displayName,
                accountId = "acc_bank",
                reference = "FB-INV-99320",
                description = "Meta Ads Campaign - Festive Autumn Collection",
                notes = "Daily budget top-up on credit card"
            ),
            Transaction(
                id = "TX-1004",
                type = TransactionType.EXPENSE,
                amount = 1200.0,
                dateMillis = daysAgo(0, 14, 10),
                timeString = "02:10 PM",
                categoryName = ExpenseCategory.PACKAGING.displayName,
                accountId = "acc_cash",
                reference = "VOUCHER-501",
                description = "Premium Matte Packaging Boxes (100 pcs)",
                notes = "Bought from Islampur packaging market"
            ),
            Transaction(
                id = "TX-1005",
                type = TransactionType.TRANSFER,
                amount = 25000.0,
                dateMillis = daysAgo(0, 15, 0),
                timeString = "03:00 PM",
                categoryName = "Transfer",
                accountId = "acc_bkash",
                destinationAccountId = "acc_bank",
                reference = "BK2BK-7721",
                description = "Settlement: bKash Merchant to BRAC Bank Corp",
                notes = "Daily sweep transfer"
            ),

            // Yesterday
            Transaction(
                id = "TX-1006",
                type = TransactionType.INCOME,
                amount = 7600.0,
                dateMillis = daysAgo(1, 12, 30),
                timeString = "12:30 PM",
                categoryName = IncomeCategory.PRODUCT_SALE.displayName,
                accountId = "acc_nagad",
                reference = "NG-88319",
                orderId = "ORD-8818",
                customerName = "Nusrat Jahan",
                description = "Organza Saree & Stole purchase via Nagad"
            ),
            Transaction(
                id = "TX-1007",
                type = TransactionType.EXPENSE,
                amount = 4500.0,
                dateMillis = daysAgo(1, 16, 45),
                timeString = "04:45 PM",
                categoryName = ExpenseCategory.COURIER.displayName,
                accountId = "acc_bkash",
                reference = "PATHAO-INV-22",
                description = "Pathao Parcel Delivery Prepaid Charges"
            ),

            // 2 Days ago
            Transaction(
                id = "TX-1008",
                type = TransactionType.INCOME,
                amount = 18500.0,
                dateMillis = daysAgo(2, 14, 0),
                timeString = "02:00 PM",
                categoryName = IncomeCategory.COURIER_COLLECTION.displayName,
                accountId = "acc_courier",
                reference = "REDX-DISB-10",
                description = "RedX COD remittance settlement"
            ),
            Transaction(
                id = "TX-1009",
                type = TransactionType.EXPENSE,
                amount = 32000.0,
                dateMillis = daysAgo(3, 11, 0),
                timeString = "11:00 AM",
                categoryName = ExpenseCategory.PRODUCT_PURCHASE.displayName,
                accountId = "acc_bank",
                reference = "SUPP-INV-301",
                description = "Bengal Fabrics: Premium Egyptian Cotton Fabric Roll",
                notes = "Raw materials for winter panjabi lot"
            ),

            // 4-5 Days ago
            Transaction(
                id = "TX-1010",
                type = TransactionType.INCOME,
                amount = 9200.0,
                dateMillis = daysAgo(4, 17, 15),
                timeString = "05:15 PM",
                categoryName = IncomeCategory.CASH.displayName,
                accountId = "acc_cash",
                reference = "POS-0089",
                customerName = "Mahir Faysal",
                description = "Showroom Walk-in Cash Sale: 2 Linen Shirts"
            ),
            Transaction(
                id = "TX-1011",
                type = TransactionType.EXPENSE,
                amount = 15000.0,
                dateMillis = daysAgo(5, 10, 0),
                timeString = "10:00 AM",
                categoryName = ExpenseCategory.SALARY.displayName,
                accountId = "acc_bank",
                reference = "SAL-SEP-01",
                description = "Staff Advance & Operation Manager Allowance"
            ),
            Transaction(
                id = "TX-1012",
                type = TransactionType.TRANSFER,
                amount = 20000.0,
                dateMillis = daysAgo(6, 16, 20),
                timeString = "04:20 PM",
                categoryName = "Transfer",
                accountId = "acc_courier",
                destinationAccountId = "acc_bank",
                reference = "COD-SWEEP-09",
                description = "Courier COD Collection transferred to Main Bank"
            )
        )
    }

    fun getSampleOrders(): List<Order> {
        val cal = Calendar.getInstance()

        fun daysAgo(days: Int): Long {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -days)
            return c.timeInMillis
        }

        return listOf(
            Order(
                id = "ORD-8821",
                customerName = "Tanvir Ahmed",
                phoneNumber = "01711-223344",
                deliveryAddress = "House 14, Road 7, Dhanmondi, Dhaka",
                productName = "Royal Silk Embroidered Panjabi",
                quantity = 1,
                productCost = 2100.0,
                sellingPrice = 4850.0,
                deliveryCharge = 80.0,
                courier = CourierOption.STEADFAST,
                paymentMethod = PaymentMethod.BKASH,
                orderDateMillis = daysAgo(0),
                status = OrderStatus.PROCESSING,
                deliveryStatus = "Packaging ready for pickup",
                trackingCode = "ST-8821-DH",
                notes = "Gift packing requested"
            ),
            Order(
                id = "ORD-8820",
                customerName = "Nusrat Jahan",
                phoneNumber = "01822-334455",
                deliveryAddress = "Apt 4B, Concord Tower, Gulshan 2, Dhaka",
                productName = "Organza Floral Festive Saree",
                quantity = 1,
                productCost = 3500.0,
                sellingPrice = 7600.0,
                deliveryCharge = 120.0,
                courier = CourierOption.PATHAO,
                paymentMethod = PaymentMethod.COD,
                orderDateMillis = daysAgo(1),
                status = OrderStatus.SHIPPED,
                deliveryStatus = "Out for delivery with rider",
                trackingCode = "PTH-90214",
                notes = "Call before delivery between 2pm-5pm"
            ),
            Order(
                id = "ORD-8819",
                customerName = "Shakib Al Hasan",
                phoneNumber = "01933-445566",
                deliveryAddress = "Sector 11, Uttara, Dhaka",
                productName = "Luxury Pima Cotton Polo (Navy)",
                quantity = 2,
                productCost = 750.0,
                sellingPrice = 1850.0,
                deliveryCharge = 80.0,
                courier = CourierOption.STEADFAST,
                paymentMethod = PaymentMethod.COD,
                orderDateMillis = daysAgo(1),
                status = OrderStatus.DELIVERED,
                deliveryStatus = "Delivered & COD collected",
                trackingCode = "ST-8819-UT",
                notes = "Cash received by Steadfast"
            ),
            Order(
                id = "ORD-8818",
                customerName = "Farzana Kabir",
                phoneNumber = "01644-556677",
                deliveryAddress = "Nasirabad Housing, Chattogram",
                productName = "Premium Linen Slim-Fit Shirt",
                quantity = 1,
                productCost = 850.0,
                sellingPrice = 2200.0,
                deliveryCharge = 150.0,
                courier = CourierOption.REDX,
                paymentMethod = PaymentMethod.NAGAD,
                orderDateMillis = daysAgo(2),
                status = OrderStatus.DELIVERED,
                deliveryStatus = "Delivered successfully",
                trackingCode = "RDX-77189",
                notes = "Prepaid in full"
            ),
            Order(
                id = "ORD-8817",
                customerName = "Mahir Faysal",
                phoneNumber = "01555-667788",
                deliveryAddress = "Kumarpara, Sylhet Sadar, Sylhet",
                productName = "Classic Mandarin Collar Kurta",
                quantity = 1,
                productCost = 1100.0,
                sellingPrice = 2650.0,
                deliveryCharge = 150.0,
                courier = CourierOption.PAPERFLY,
                paymentMethod = PaymentMethod.COD,
                orderDateMillis = daysAgo(3),
                status = OrderStatus.DELIVERED,
                deliveryStatus = "Delivered",
                trackingCode = "PFLY-44021"
            ),
            Order(
                id = "ORD-8816",
                customerName = "Nafisa Rahman",
                phoneNumber = "01788-990011",
                deliveryAddress = "Boalia, Rajshahi",
                productName = "Monogram Genuine Leather Wallet",
                quantity = 1,
                productCost = 600.0,
                sellingPrice = 1600.0,
                deliveryCharge = 150.0,
                courier = CourierOption.STEADFAST,
                paymentMethod = PaymentMethod.COD,
                orderDateMillis = daysAgo(4),
                status = OrderStatus.RETURNED,
                deliveryStatus = "Returned: Customer unavailable",
                trackingCode = "ST-8816-RJ",
                notes = "Customer phone switched off after 3 attempts"
            ),
            Order(
                id = "ORD-8815",
                customerName = "Arif Chowdhury",
                phoneNumber = "01811-229988",
                deliveryAddress = "Mirpur DOHS, Dhaka",
                productName = "Royal Silk Embroidered Panjabi",
                quantity = 1,
                productCost = 2100.0,
                sellingPrice = 4850.0,
                deliveryCharge = 80.0,
                courier = CourierOption.PATHAO,
                paymentMethod = PaymentMethod.COD,
                orderDateMillis = daysAgo(5),
                status = OrderStatus.DELIVERED,
                deliveryStatus = "Delivered & COD Remitted",
                trackingCode = "PTH-8815-MP"
            )
        )
    }

    fun getSampleProducts(): List<Product> {
        return listOf(
            Product(
                id = "PRD-01",
                name = "Royal Silk Embroidered Panjabi",
                sku = "SSP-PNJ-01",
                category = "Ethnic Wear",
                purchaseCost = 2100.0,
                sellingPrice = 4850.0,
                currentStock = 2, // Low stock alert!
                minimumStock = 5,
                totalSold = 48,
                status = ProductStatus.LOW_STOCK
            ),
            Product(
                id = "PRD-02",
                name = "Organza Floral Festive Saree",
                sku = "SSP-SAR-08",
                category = "Women's Ethnic",
                purchaseCost = 3500.0,
                sellingPrice = 7600.0,
                currentStock = 12,
                minimumStock = 4,
                totalSold = 34,
                status = ProductStatus.IN_STOCK
            ),
            Product(
                id = "PRD-03",
                name = "Luxury Pima Cotton Polo",
                sku = "SSP-PLO-14",
                category = "Casual Wear",
                purchaseCost = 750.0,
                sellingPrice = 1850.0,
                currentStock = 28,
                minimumStock = 8,
                totalSold = 92,
                status = ProductStatus.IN_STOCK
            ),
            Product(
                id = "PRD-04",
                name = "Premium Linen Slim-Fit Shirt",
                sku = "SSP-SHT-22",
                category = "Formal & Casual",
                purchaseCost = 850.0,
                sellingPrice = 2200.0,
                currentStock = 19,
                minimumStock = 6,
                totalSold = 65,
                status = ProductStatus.IN_STOCK
            ),
            Product(
                id = "PRD-05",
                name = "Monogram Genuine Leather Wallet",
                sku = "SSP-ACC-03",
                category = "Accessories",
                purchaseCost = 600.0,
                sellingPrice = 1600.0,
                currentStock = 0, // Out of stock!
                minimumStock = 5,
                totalSold = 39,
                status = ProductStatus.OUT_OF_STOCK
            ),
            Product(
                id = "PRD-06",
                name = "Classic Mandarin Collar Kurta",
                sku = "SSP-KRT-05",
                category = "Ethnic Wear",
                purchaseCost = 1100.0,
                sellingPrice = 2650.0,
                currentStock = 14,
                minimumStock = 5,
                totalSold = 28,
                status = ProductStatus.IN_STOCK
            )
        )
    }

    fun getSampleCustomers(): List<Customer> {
        return listOf(
            Customer(
                id = "CUST-01",
                name = "Tanvir Ahmed",
                phone = "01711-223344",
                address = "Dhanmondi, Dhaka",
                totalOrders = 4,
                totalPurchase = 18400.0,
                dueAmount = 0.0,
                notes = "VIP customer, prefers bKash payment"
            ),
            Customer(
                id = "CUST-02",
                name = "Nusrat Jahan",
                phone = "01822-334455",
                address = "Gulshan 2, Dhaka",
                totalOrders = 3,
                totalPurchase = 22800.0,
                dueAmount = 0.0,
                notes = "Festive seasonal buyer"
            ),
            Customer(
                id = "CUST-03",
                name = "Farzana Kabir",
                phone = "01644-556677",
                address = "Nasirabad, Chattogram",
                totalOrders = 2,
                totalPurchase = 5800.0,
                dueAmount = 450.0,
                notes = "Pending partial delivery courier balance"
            ),
            Customer(
                id = "CUST-04",
                name = "Shakib Al Hasan",
                phone = "01933-445566",
                address = "Uttara, Dhaka",
                totalOrders = 5,
                totalPurchase = 14500.0,
                dueAmount = 0.0
            )
        )
    }

    fun getSampleSuppliers(): List<Supplier> {
        return listOf(
            Supplier(
                id = "SUPP-01",
                name = "Bengal Premium Textiles Ltd",
                phone = "01711-998877",
                address = "Narayanganj BSCIC Industrial Area",
                totalPurchase = 145000.0,
                paidAmount = 120000.0,
                dueAmount = 25000.0,
                contactPerson = "Engr. Rafiqul Islam"
            ),
            Supplier(
                id = "SUPP-02",
                name = "Dhaka Heritage Embroidery",
                phone = "01822-112233",
                address = "Islampur, Old Dhaka",
                totalPurchase = 68000.0,
                paidAmount = 68000.0,
                dueAmount = 0.0,
                contactPerson = "Mohammad Younus"
            ),
            Supplier(
                id = "SUPP-03",
                name = "Apex Packaging & Box Craft",
                phone = "01944-778899",
                address = "Chawkbazar, Dhaka",
                totalPurchase = 32000.0,
                paidAmount = 28000.0,
                dueAmount = 4000.0,
                contactPerson = "Abdur Rahim"
            )
        )
    }
}
