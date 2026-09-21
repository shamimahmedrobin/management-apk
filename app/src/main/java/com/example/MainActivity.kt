package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.lifecycle.viewmodel.compose.viewModel
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: StyleSphereViewModel = viewModel(
                factory = StyleSphereViewModel.provideFactory(context.applicationContext as android.app.Application)
            )
            val themeSetting by viewModel.themeSetting.collectAsState()
            val accounts by viewModel.accounts.collectAsState()

            val isAppLocked by SecurityManager.isAppLocked.collectAsState()

            MyApplicationTheme(themeSetting = themeSetting) {
                if (isAppLocked) {
                    PinLockScreen(
                        onUnlocked = { /* unlocked state handled in SecurityManager */ }
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

    // Dialog Visibility States
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddTransferDialog by remember { mutableStateOf(false) }
    var showAddOrderDialog by remember { mutableStateOf(false) }

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
                        label = { Text(destination.title) }
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
                    onLockApp = onLockApp
                )
            }
        }

        // Global Dialogs
        if (showAddIncomeDialog) {
            AddIncomeDialog(
                accounts = accounts,
                onDismiss = { showAddIncomeDialog = false },
                onConfirm = { amt, cat, accId, ref, ordId, cust, notes ->
                    viewModel.addIncome(
                        amount = amt,
                        category = cat,
                        accountId = accId,
                        reference = ref,
                        orderId = ordId,
                        customerName = cust,
                        notes = notes
                    )
                }
            )
        }

        if (showAddExpenseDialog) {
            AddExpenseDialog(
                accounts = accounts,
                onDismiss = { showAddExpenseDialog = false },
                onConfirm = { amt, cat, accId, desc, ref, notes, receiptUri ->
                    viewModel.addExpense(
                        amount = amt,
                        category = cat,
                        accountId = accId,
                        description = desc,
                        reference = ref,
                        notes = notes,
                        receiptUri = receiptUri
                    )
                }
            )
        }

        if (showAddTransferDialog) {
            AddTransferDialog(
                accounts = accounts,
                onDismiss = { showAddTransferDialog = false },
                onConfirm = { srcId, destId, amt, ref, notes ->
                    viewModel.addTransfer(
                        sourceAccountId = srcId,
                        destinationAccountId = destId,
                        amount = amt,
                        reference = ref,
                        notes = notes
                    )
                }
            )
        }

        if (showAddOrderDialog) {
            AddOrderDialog(
                onDismiss = { showAddOrderDialog = false },
                onConfirm = { cust, phone, addr, prod, qty, cost, price, deliv, cour, pay, notes ->
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
                        notes = notes
                    )
                }
            )
        }
    }
}
