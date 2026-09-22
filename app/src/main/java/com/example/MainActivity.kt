package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.security.BiometricAuthManager
import com.example.core.security.SecurityManager
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.dialogs.AddExpenseDialog
import com.example.presentation.dialogs.AddIncomeDialog
import com.example.presentation.dialogs.AddOrderDialog
import com.example.presentation.dialogs.AddTransferDialog
import com.example.presentation.more.MoreScreen
import com.example.presentation.orders.OrdersScreen
import com.example.presentation.reports.ReportsScreen
import com.example.presentation.security.PinLockScreen
import com.example.presentation.settings.SettingsScreen
import com.example.presentation.transactions.TransactionsScreen
import com.example.presentation.viewmodel.StyleSphereViewModel
import com.example.ui.theme.AppThemeSetting
import com.example.ui.theme.MyApplicationTheme

enum class MainDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, Icons.Outlined.Dashboard),
    TRANSACTIONS("Transactions", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
    ORDERS("Orders", Icons.Default.LocalShipping, Icons.Outlined.LocalShipping),
    REPORTS("Reports", Icons.Default.BarChart, Icons.Outlined.BarChart),
    MORE("More", Icons.Default.MoreHoriz, Icons.Outlined.MoreHoriz)
}

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SecurityManager.init(this)

        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: StyleSphereViewModel = viewModel(
                factory = StyleSphereViewModel.provideFactory(context.applicationContext as android.app.Application)
            )
            val themeSetting by viewModel.themeSetting.collectAsState()
            val accounts by viewModel.accounts.collectAsState()

            val isAppLocked by SecurityManager.isAppLocked.collectAsState()
            val isBiometricEnabled by SecurityManager.isBiometricEnabled.collectAsState()

            // Trigger biometric prompt immediately on launch if app is locked
            LaunchedEffect(isAppLocked) {
                if (isAppLocked && isBiometricEnabled) {
                    if (BiometricAuthManager.canAuthenticate(this@MainActivity)) {
                        BiometricAuthManager.promptBiometric(
                            activity = this@MainActivity,
                            title = "Business Management",
                            subtitle = "Biometric Verification",
                            description = "Scan your fingerprint or face to authenticate.",
                            onSuccess = {
                                SecurityManager.unlockWithBiometric()
                            }
                        )
                    }
                }
            }

            MyApplicationTheme(themeSetting = themeSetting) {
                if (isAppLocked) {
                    PinLockScreen(
                        onUnlocked = { /* unlocked state handled in SecurityManager */ },
                        onTriggerBiometric = {
                            if (BiometricAuthManager.canAuthenticate(this@MainActivity)) {
                                BiometricAuthManager.promptBiometric(
                                    activity = this@MainActivity,
                                    onSuccess = {
                                        SecurityManager.unlockWithBiometric()
                                    }
                                )
                            }
                        }
                    )
                } else {
                    StyleSphereMainApp(
                        viewModel = viewModel,
                        accounts = accounts,
                        onLockApp = { SecurityManager.lockApp() }
                    )
                }
            }
        }
    }
}

@Composable
fun StyleSphereMainApp(
    viewModel: StyleSphereViewModel,
    accounts: List<com.example.domain.model.Account>,
    onLockApp: () -> Unit
) {
    var currentDestination by remember { mutableStateOf(MainDestination.DASHBOARD) }
    var showSettingsScreen by remember { mutableStateOf(false) }

    // Dialog Visibility States
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddTransferDialog by remember { mutableStateOf(false) }
    var showAddOrderDialog by remember { mutableStateOf(false) }

    if (showSettingsScreen) {
        SettingsScreen(
            viewModel = viewModel,
            onBackClick = { showSettingsScreen = false },
            onLockApp = onLockApp
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    MainDestination.values().forEach { destination ->
                        val isSelected = currentDestination == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        ) { innerPadding ->
            Crossfade(
                targetState = currentDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                label = "ScreenTransition"
            ) { destination ->
                when (destination) {
                    MainDestination.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { currentDestination = MainDestination.TRANSACTIONS },
                        onNavigateToOrders = { currentDestination = MainDestination.ORDERS },
                        onNavigateToMore = { currentDestination = MainDestination.MORE },
                        onNavigateToSettings = { showSettingsScreen = true },
                        onAddIncomeClick = { showAddIncomeDialog = true },
                        onAddExpenseClick = { showAddExpenseDialog = true },
                        onAddTransferClick = { showAddTransferDialog = true },
                        onAddOrderClick = { showAddOrderDialog = true }
                    )

                    MainDestination.TRANSACTIONS -> TransactionsScreen(
                        viewModel = viewModel,
                        onAddIncomeClick = { showAddIncomeDialog = true },
                        onAddExpenseClick = { showAddExpenseDialog = true },
                        onAddTransferClick = { showAddTransferDialog = true }
                    )

                    MainDestination.ORDERS -> OrdersScreen(
                        viewModel = viewModel,
                        onAddNewOrderClick = { showAddOrderDialog = true }
                    )

                    MainDestination.REPORTS -> ReportsScreen(
                        viewModel = viewModel
                    )

                    MainDestination.MORE -> MoreScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { showSettingsScreen = true }
                    )
                }
            }
        }
    }

    // Global Dialogs
    if (showAddIncomeDialog) {
        AddIncomeDialog(
            accounts = accounts,
            onDismiss = { showAddIncomeDialog = false },
            onConfirm = { amt, cat, accId, ref, ordId, cust, notes, dateMillis ->
                viewModel.addIncome(
                    amount = amt,
                    category = cat,
                    accountId = accId,
                    reference = ref,
                    orderId = ordId,
                    customerName = cust,
                    notes = notes,
                    dateMillis = dateMillis
                )
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            accounts = accounts,
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { amt, cat, accId, desc, ref, notes, receiptUri, dateMillis ->
                viewModel.addExpense(
                    amount = amt,
                    category = cat,
                    accountId = accId,
                    description = desc,
                    reference = ref,
                    notes = notes,
                    receiptUri = receiptUri,
                    dateMillis = dateMillis
                )
            }
        )
    }

    if (showAddTransferDialog) {
        AddTransferDialog(
            accounts = accounts,
            onDismiss = { showAddTransferDialog = false },
            onConfirm = { srcId, destId, amt, ref, notes, dateMillis ->
                viewModel.addTransfer(
                    sourceAccountId = srcId,
                    destinationAccountId = destId,
                    amount = amt,
                    reference = ref,
                    notes = notes,
                    dateMillis = dateMillis
                )
            }
        )
    }

    if (showAddOrderDialog) {
        AddOrderDialog(
            onDismiss = { showAddOrderDialog = false },
            onConfirm = { cust, phone, addr, prod, qty, cost, price, deliv, cour, pay, notes, trackCode, orderDateMillis ->
                viewModel.addOrder(
                    customerName = cust,
                    phoneNumber = phone,
                    address = addr,
                    productName = prod,
                    quantity = qty,
                    productCost = cost,
                    sellingPrice = price,
                    deliveryCharge = deliv,
                    courier = cour,
                    paymentMethod = pay,
                    notes = notes,
                    trackingCode = trackCode,
                    orderDateMillis = orderDateMillis
                )
            }
        )
    }
}
