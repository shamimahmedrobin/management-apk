package com.example.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.export.ExcelExporter
import com.example.core.export.FileShareHelper
import com.example.core.export.PdfExporter
import com.example.core.notification.NotificationHelper
import com.example.core.security.SecurityManager
import com.example.core.utils.DateUtils
import com.example.data.local.StyleSphereDatabase
import com.example.data.repository.*
import com.example.domain.model.*
import com.example.domain.repository.*
import com.example.domain.usecase.FinancialCalculationUseCase
import com.example.ui.theme.AppThemeSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class StyleSphereViewModel @JvmOverloads constructor(
    application: Application,
    private val transactionRepository: TransactionRepository = RoomTransactionRepository(
        StyleSphereDatabase.getInstance(application).transactionDao()
    ),
    private val accountRepository: AccountRepository = RoomAccountRepository(
        StyleSphereDatabase.getInstance(application).accountDao()
    ),
    private val orderRepository: OrderRepository = RoomOrderRepository(
        StyleSphereDatabase.getInstance(application).orderDao()
    ),
    private val productRepository: ProductRepository = RoomProductRepository(
        StyleSphereDatabase.getInstance(application).productDao()
    ),
    private val customerRepository: CustomerRepository = RoomCustomerRepository(
        StyleSphereDatabase.getInstance(application).customerDao()
    ),
    private val supplierRepository: SupplierRepository = RoomSupplierRepository(
        StyleSphereDatabase.getInstance(application).supplierDao()
    ),
    private val calculationUseCase: FinancialCalculationUseCase = FinancialCalculationUseCase()
) : AndroidViewModel(application) {

    private val themePrefs = application.getSharedPreferences("stylesphere_ui_prefs", Context.MODE_PRIVATE)

    // Theme & Settings State (Persisted)
    private val _themeSetting = MutableStateFlow(AppThemeSetting.SYSTEM)
    val themeSetting: StateFlow<AppThemeSetting> = _themeSetting.asStateFlow()

    init {
        // Initialize SecurityManager with Application context for PIN persistence
        SecurityManager.init(application)

        // Load saved theme preference
        val savedTheme = themePrefs.getString("app_theme_setting", AppThemeSetting.SYSTEM.name)
        _themeSetting.value = try {
            AppThemeSetting.valueOf(savedTheme ?: AppThemeSetting.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeSetting.SYSTEM
        }

        // Seed initial sample data into Room database if database is empty
        viewModelScope.launch(Dispatchers.IO) {
            val db = StyleSphereDatabase.getInstance(application)
            StyleSphereDatabase.seedInitialData(db)
        }
    }

    val transactions: StateFlow<List<Transaction>> = transactionRepository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val accounts: StateFlow<List<Account>> = accountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val orders: StateFlow<List<Order>> = orderRepository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val products: StateFlow<List<Product>> = productRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val customers: StateFlow<List<Customer>> = customerRepository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val suppliers: StateFlow<List<Supplier>> = supplierRepository.getAllSuppliers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setThemeSetting(setting: AppThemeSetting) {
        _themeSetting.value = setting
        themePrefs.edit { putString("app_theme_setting", setting.name) }
    }

    // Dashboard & Chart Period Filter (7 Days, 30 Days, 12 Months)
    enum class ChartPeriod(val displayName: String) {
        DAYS_7("7 Days"),
        DAYS_30("30 Days"),
        MONTHS_12("12 Months")
    }

    private val _dashboardChartPeriod = MutableStateFlow(ChartPeriod.DAYS_7)
    val dashboardChartPeriod: StateFlow<ChartPeriod> = _dashboardChartPeriod.asStateFlow()

    fun setDashboardChartPeriod(period: ChartPeriod) {
        _dashboardChartPeriod.value = period
    }

    // Reports Period
    private val _reportPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
    }

    // Today's Summary for Dashboard Top Cards
    val todaySummary: StateFlow<FinancialSummary> = combine(transactions, orders) { txs, ords ->
        val todayTxs = calculationUseCase.filterTransactionsByPeriod(txs, ReportPeriod.TODAY)
        val todayOrds = calculationUseCase.filterOrdersByPeriod(ords, ReportPeriod.TODAY)
        calculationUseCase.calculateFinancialSummary(todayTxs, todayOrds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // Filtered Report Summary
    val reportFinancialSummary: StateFlow<FinancialSummary> = combine(
        transactions,
        orders,
        reportPeriod
    ) { txs, ords, period ->
        val filteredTxs = calculationUseCase.filterTransactionsByPeriod(txs, period)
        val filteredOrds = calculationUseCase.filterOrdersByPeriod(ords, period)
        calculationUseCase.calculateFinancialSummary(filteredTxs, filteredOrds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // Category Expense Breakdown
    val categoryExpenses: StateFlow<List<CategoryExpenseItem>> = combine(
        transactions,
        reportPeriod
    ) { txs, period ->
        val filteredTxs = calculationUseCase.filterTransactionsByPeriod(txs, period)
        calculationUseCase.calculateCategoryExpenses(filteredTxs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Courier Summary
    val courierSummaries: StateFlow<List<CourierSummary>> = orders.map { ords ->
        calculationUseCase.calculateCourierSummaries(ords)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions Screen Filters
    data class TxFilters(
        val tab: Int = 0,
        val period: ReportPeriod = ReportPeriod.THIS_MONTH,
        val searchQuery: String = "",
        val accountFilter: String? = null,
        val categoryFilter: String? = null
    )

    private val _txFilters = MutableStateFlow(TxFilters())

    val txTab: StateFlow<Int> = _txFilters.map { it.tab }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val txPeriod: StateFlow<ReportPeriod> = _txFilters.map { it.period }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReportPeriod.THIS_MONTH)
    val txSearchQuery: StateFlow<String> = _txFilters.map { it.searchQuery }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val txAccountFilter: StateFlow<String?> = _txFilters.map { it.accountFilter }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val txCategoryFilter: StateFlow<String?> = _txFilters.map { it.categoryFilter }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setTxTab(tab: Int) { _txFilters.value = _txFilters.value.copy(tab = tab) }
    fun setTxPeriod(period: ReportPeriod) { _txFilters.value = _txFilters.value.copy(period = period) }
    fun setTxSearchQuery(query: String) { _txFilters.value = _txFilters.value.copy(searchQuery = query) }
    fun setTxAccountFilter(acc: String?) { _txFilters.value = _txFilters.value.copy(accountFilter = acc) }
    fun setTxCategoryFilter(cat: String?) { _txFilters.value = _txFilters.value.copy(categoryFilter = cat) }

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactions,
        _txFilters
    ) { list, filters ->
        var result = calculationUseCase.filterTransactionsByPeriod(list, filters.period)

        result = when (filters.tab) {
            1 -> result.filter { it.type == TransactionType.INCOME }
            2 -> result.filter { it.type == TransactionType.EXPENSE }
            3 -> result.filter { it.type == TransactionType.TRANSFER }
            else -> result
        }

        if (filters.accountFilter != null) {
            result = result.filter { it.accountId == filters.accountFilter || it.destinationAccountId == filters.accountFilter }
        }

        if (filters.categoryFilter != null) {
            result = result.filter { it.categoryName.equals(filters.categoryFilter, ignoreCase = true) }
        }

        if (filters.searchQuery.isNotBlank()) {
            val q = filters.searchQuery.trim().lowercase()
            result = result.filter {
                it.description.lowercase().contains(q) ||
                        it.id.lowercase().contains(q) ||
                        (it.orderId?.lowercase()?.contains(q) == true) ||
                        it.categoryName.lowercase().contains(q) ||
                        (it.customerName?.lowercase()?.contains(q) == true) ||
                        it.reference.lowercase().contains(q)
            }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Orders Screen Filters
    private val _orderStatusFilter = MutableStateFlow<OrderStatus?>(null)
    val orderStatusFilter: StateFlow<OrderStatus?> = _orderStatusFilter.asStateFlow()
    fun setOrderStatusFilter(status: OrderStatus?) { _orderStatusFilter.value = status }

    private val _orderSearchQuery = MutableStateFlow("")
    val orderSearchQuery: StateFlow<String> = _orderSearchQuery.asStateFlow()
    fun setOrderSearchQuery(query: String) { _orderSearchQuery.value = query }

    val filteredOrders: StateFlow<List<Order>> = combine(
        orders,
        _orderStatusFilter,
        _orderSearchQuery
    ) { list, status, query ->
        var result = list
        if (status != null) {
            result = result.filter { it.status == status }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.id.lowercase().contains(q) ||
                        it.customerName.lowercase().contains(q) ||
                        it.phoneNumber.contains(q) ||
                        it.productName.lowercase().contains(q) ||
                        it.trackingCode.lowercase().contains(q)
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Action: Add Income
    fun addIncome(
        amount: Double,
        category: String,
        accountId: String,
        reference: String,
        orderId: String?,
        customerName: String?,
        notes: String,
        dateMillis: Long = System.currentTimeMillis(),
        timeString: String = DateUtils.formatTime(System.currentTimeMillis())
    ): Result<Unit> {
        if (amount <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than 0"))
        if (category.isBlank()) return Result.failure(IllegalArgumentException("Category is required"))
        if (accountId.isBlank()) return Result.failure(IllegalArgumentException("Account is required"))

        val newTx = Transaction(
            id = "TX-${System.currentTimeMillis()}",
            type = TransactionType.INCOME,
            amount = amount,
            dateMillis = dateMillis,
            timeString = timeString,
            categoryName = category,
            accountId = accountId,
            reference = reference,
            orderId = orderId?.ifBlank { null },
            customerName = customerName?.ifBlank { null },
            notes = notes,
            description = "Income: $category ${if (!customerName.isNullOrBlank()) "($customerName)" else ""}"
        )

        viewModelScope.launch {
            transactionRepository.addTransaction(newTx)
            val updatedTxs = listOf(newTx) + transactions.value
            accountRepository.updateBalances(updatedTxs)
        }
        return Result.success(Unit)
    }

    // User Action: Add Expense
    fun addExpense(
        amount: Double,
        category: String,
        accountId: String,
        description: String,
        reference: String,
        notes: String,
        receiptUri: String? = null,
        dateMillis: Long = System.currentTimeMillis(),
        timeString: String = DateUtils.formatTime(System.currentTimeMillis())
    ): Result<Unit> {
        if (amount <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than 0"))
        if (category.isBlank()) return Result.failure(IllegalArgumentException("Category is required"))
        if (accountId.isBlank()) return Result.failure(IllegalArgumentException("Account is required"))

        val newTx = Transaction(
            id = "TX-${System.currentTimeMillis()}",
            type = TransactionType.EXPENSE,
            amount = amount,
            dateMillis = dateMillis,
            timeString = timeString,
            categoryName = category,
            accountId = accountId,
            description = if (description.isNotBlank()) description else "Expense: $category",
            reference = reference,
            notes = notes,
            receiptAttachmentUri = receiptUri
        )

        viewModelScope.launch {
            transactionRepository.addTransaction(newTx)
            val updatedTxs = listOf(newTx) + transactions.value
            accountRepository.updateBalances(updatedTxs)
        }
        return Result.success(Unit)
    }

    // User Action: Add Transfer
    fun addTransfer(
        sourceAccountId: String,
        destinationAccountId: String,
        amount: Double,
        reference: String,
        notes: String,
        dateMillis: Long = System.currentTimeMillis(),
        timeString: String = DateUtils.formatTime(System.currentTimeMillis())
    ): Result<Unit> {
        if (amount <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than 0"))
        if (sourceAccountId.isBlank() || destinationAccountId.isBlank()) {
            return Result.failure(IllegalArgumentException("Source and Destination accounts are required"))
        }
        if (sourceAccountId == destinationAccountId) {
            return Result.failure(IllegalArgumentException("Source and Destination accounts cannot be the same"))
        }

        val sourceAcc = accounts.value.firstOrNull { it.id == sourceAccountId }
        val destAcc = accounts.value.firstOrNull { it.id == destinationAccountId }

        val newTx = Transaction(
            id = "TX-${System.currentTimeMillis()}",
            type = TransactionType.TRANSFER,
            amount = amount,
            dateMillis = dateMillis,
            timeString = timeString,
            categoryName = "Transfer",
            accountId = sourceAccountId,
            destinationAccountId = destinationAccountId,
            reference = reference,
            description = "Transfer: ${sourceAcc?.name ?: sourceAccountId} → ${destAcc?.name ?: destinationAccountId}",
            notes = notes
        )

        viewModelScope.launch {
            transactionRepository.addTransaction(newTx)
            val updatedTxs = listOf(newTx) + transactions.value
            accountRepository.updateBalances(updatedTxs)
        }
        return Result.success(Unit)
    }

    // User Action: Delete Transaction
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transactionId)
            val remainingTxs = transactions.value.filterNot { it.id == transactionId }
            accountRepository.updateBalances(remainingTxs)
        }
    }

    // User Action: Add Order
    fun addOrder(
        customerName: String,
        phoneNumber: String,
        address: String,
        productName: String,
        quantity: Int,
        productCost: Double,
        sellingPrice: Double,
        deliveryCharge: Double,
        courier: CourierOption,
        paymentMethod: PaymentMethod,
        notes: String
    ): Result<Unit> {
        if (customerName.isBlank()) return Result.failure(IllegalArgumentException("Customer name is required"))
        if (phoneNumber.isBlank()) return Result.failure(IllegalArgumentException("Phone number is required"))
        if (productName.isBlank()) return Result.failure(IllegalArgumentException("Product name is required"))
        if (quantity <= 0) return Result.failure(IllegalArgumentException("Quantity must be at least 1"))
        if (sellingPrice <= 0) return Result.failure(IllegalArgumentException("Selling price must be greater than 0"))

        val newOrder = Order(
            id = "ORD-${8800 + (orders.value.size + 1)}",
            customerName = customerName,
            phoneNumber = phoneNumber,
            deliveryAddress = address,
            productName = productName,
            quantity = quantity,
            productCost = productCost,
            sellingPrice = sellingPrice,
            deliveryCharge = deliveryCharge,
            courier = courier,
            paymentMethod = paymentMethod,
            orderDateMillis = System.currentTimeMillis(),
            status = OrderStatus.PENDING,
            deliveryStatus = "Pending Verification",
            trackingCode = "${courier.name.take(3)}-${(10000..99999).random()}",
            notes = notes
        )

        viewModelScope.launch {
            orderRepository.addOrder(newOrder)
        }
        return Result.success(Unit)
    }

    // User Action: Update Order Status
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            val order = orderRepository.getOrderById(orderId)
            if (order != null) {
                val updated = order.copy(
                    status = newStatus,
                    deliveryStatus = when (newStatus) {
                        OrderStatus.PENDING -> "Pending Verification"
                        OrderStatus.CONFIRMED -> "Confirmed & In Queue"
                        OrderStatus.PROCESSING -> "Packaging in Progress"
                        OrderStatus.SHIPPED -> "Shipped with Courier"
                        OrderStatus.DELIVERED -> "Delivered & COD Received"
                        OrderStatus.CANCELLED -> "Cancelled by Customer"
                        OrderStatus.RETURNED -> "Returned to Hub"
                    }
                )
                orderRepository.updateOrder(updated)
            }
        }
    }

    // User Action: Delete Order
    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.deleteOrder(orderId)
        }
    }

    // User Action: Add Account
    fun addAccount(
        name: String,
        type: AccountType,
        accountNumber: String,
        openingBalance: Double
    ): Result<Unit> {
        val newAccount = Account(
            id = "acc_${System.currentTimeMillis()}",
            name = name,
            type = type,
            accountNumber = accountNumber,
            openingBalance = openingBalance,
            currentBalance = openingBalance
        )
        viewModelScope.launch {
            accountRepository.addAccount(newAccount)
        }
        return Result.success(Unit)
    }

    // Export PDF
    fun exportPdfReport(context: Context, period: ReportPeriod) {
        viewModelScope.launch {
            val file = PdfExporter.generateFinancialReportPdf(
                context = context,
                period = period,
                summary = reportFinancialSummary.value,
                accounts = accounts.value,
                transactions = transactions.value,
                orders = orders.value,
                couriers = courierSummaries.value,
                categoryExpenses = categoryExpenses.value
            )
            FileShareHelper.shareFile(
                context = context,
                file = file,
                mimeType = "application/pdf",
                title = "Share StyleSphere Financial Report (PDF)"
            )
        }
    }

    // Export Excel (.xlsx)
    fun exportExcelReport(context: Context) {
        viewModelScope.launch {
            val file = ExcelExporter.generateExcelReport(
                context = context,
                summary = reportFinancialSummary.value,
                accounts = accounts.value,
                transactions = transactions.value,
                orders = orders.value,
                couriers = courierSummaries.value,
                products = products.value,
                customers = customers.value
            )
            FileShareHelper.shareFile(
                context = context,
                file = file,
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                title = "Share StyleSphere Multi-Sheet Report (Excel)"
            )
        }
    }

    // Local Notification Trigger
    fun sendReminderAlert(context: Context, title: String, message: String) {
        NotificationHelper.sendLocalAlert(
            context = context,
            notificationId = (100..999).random(),
            title = title,
            message = message
        )
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = StyleSphereDatabase.getInstance(application)
                return StyleSphereViewModel(
                    application = application,
                    transactionRepository = RoomTransactionRepository(db.transactionDao()),
                    accountRepository = RoomAccountRepository(db.accountDao()),
                    orderRepository = RoomOrderRepository(db.orderDao()),
                    productRepository = RoomProductRepository(db.productDao()),
                    customerRepository = RoomCustomerRepository(db.customerDao()),
                    supplierRepository = RoomSupplierRepository(db.supplierDao()),
                    calculationUseCase = FinancialCalculationUseCase()
                ) as T
            }
        }
    }
}
