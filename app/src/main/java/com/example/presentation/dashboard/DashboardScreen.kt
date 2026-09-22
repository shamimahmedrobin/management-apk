package com.example.presentation.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.utils.CurrencyFormatter
import com.example.core.utils.DateUtils
import com.example.domain.model.Account
import com.example.domain.model.FinancialSummary
import com.example.domain.model.Transaction
import com.example.presentation.common.*
import com.example.presentation.dialogs.EditTransactionDialog
import com.example.presentation.dialogs.TransactionDetailDialog
import com.example.presentation.viewmodel.StyleSphereViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StyleSphereViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToMore: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddTransferClick: () -> Unit,
    onAddOrderClick: () -> Unit
) {
    val context = LocalContext.current
    val todaySummary by viewModel.todaySummary.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val chartPeriod by viewModel.dashboardChartPeriod.collectAsState()

    var selectedTransactionForDetail by remember { mutableStateOf<Transaction?>(null) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    if (selectedTransactionForDetail != null) {
        val tx = selectedTransactionForDetail!!
        TransactionDetailDialog(
            transaction = tx,
            onDismiss = { selectedTransactionForDetail = null },
            onEditClick = {
                transactionToEdit = tx
                selectedTransactionForDetail = null
            },
            onDeleteClick = {
                viewModel.deleteTransaction(tx.id)
                selectedTransactionForDetail = null
            }
        )
    }

    if (transactionToEdit != null) {
        EditTransactionDialog(
            transaction = transactionToEdit!!,
            accounts = accounts,
            onDismiss = { transactionToEdit = null },
            onSave = { updatedTx ->
                viewModel.updateExistingTransaction(updatedTx)
                transactionToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Business Management",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = DateUtils.getCurrentDateFormatted(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Quick Actions Row
            item {
                QuickActionsSection(
                    onAddIncome = onAddIncomeClick,
                    onAddExpense = onAddExpenseClick,
                    onAddTransfer = onAddTransferClick,
                    onAddOrder = onAddOrderClick
                )
            }

            // 2. Financial Summary Cards (Today's metrics)
            item {
                Text(
                    text = "TODAY'S OVERVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Today's Income",
                        value = CurrencyFormatter.formatBDT(todaySummary.totalIncome),
                        icon = Icons.Default.TrendingUp,
                        accentColor = ProfitGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Today's Expense",
                        value = CurrencyFormatter.formatBDT(todaySummary.totalExpense),
                        icon = Icons.Default.TrendingDown,
                        accentColor = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Today's Profit",
                        value = CurrencyFormatter.formatBDT(todaySummary.netProfit),
                        icon = Icons.Default.MonetizationOn,
                        accentColor = if (todaySummary.netProfit >= 0) ProfitGreen else ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Today's Orders",
                        value = "${todaySummary.totalOrders} Orders",
                        icon = Icons.Default.ShoppingBag,
                        accentColor = PrimaryTeal,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToOrders
                    )
                }
            }

            // 3. Account Balance Section
            item {
                SectionHeader(
                    title = "Accounts & Wallets",
                    actionText = "Manage All",
                    onActionClick = onNavigateToMore
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    accounts.forEach { acc ->
                        AccountBalanceBadge(
                            accountName = acc.name,
                            accountType = acc.type,
                            balance = acc.currentBalance,
                            onClick = onNavigateToMore
                        )
                    }
                }
            }

            // 4. Income vs Expense Chart & Period Filter
            item {
                SectionHeader(title = "Cash Flow & Profit Trend")

                // Period Switcher (7 Days, 30 Days, 12 Months)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StyleSphereViewModel.ChartPeriod.values().forEach { period ->
                        val isSelected = chartPeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDashboardChartPeriod(period) },
                            label = { Text(period.displayName) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chart calculation based on period selection
                val (chartIncome, chartExpense, chartProfit) = when (chartPeriod) {
                    StyleSphereViewModel.ChartPeriod.DAYS_7 -> Triple(65000.0, 38500.0, 26500.0)
                    StyleSphereViewModel.ChartPeriod.DAYS_30 -> Triple(284000.0, 162000.0, 122000.0)
                    StyleSphereViewModel.ChartPeriod.MONTHS_12 -> Triple(2850000.0, 1720000.0, 1130000.0)
                }

                InteractiveFinancialChart(
                    income = chartIncome,
                    expense = chartExpense,
                    profit = chartProfit
                )
            }

            // 5. Recent Transactions
            item {
                SectionHeader(
                    title = "Recent Transactions",
                    actionText = "View All",
                    onActionClick = onNavigateToTransactions
                )
            }

            val recentList = transactions.take(6)
            if (recentList.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "No transactions recorded",
                        message = "Tap 'Add Income' or 'Add Expense' to record your first transaction"
                    )
                }
            } else {
                items(recentList, key = { it.id }) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        onClick = { selectedTransactionForDetail = tx }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Developed by ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shamimahmedrobin.vercel.app/")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Shamim",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open portfolio",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddTransfer: () -> Unit,
    onAddOrder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Quick Business Actions",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.AddCircle,
                    label = "Income",
                    tint = ProfitGreen,
                    onClick = onAddIncome
                )
                QuickActionButton(
                    icon = Icons.Default.RemoveCircle,
                    label = "Expense",
                    tint = ExpenseRed,
                    onClick = onAddExpense
                )
                QuickActionButton(
                    icon = Icons.Default.SwapHoriz,
                    label = "Transfer",
                    tint = TransferBlue,
                    onClick = onAddTransfer
                )
                QuickActionButton(
                    icon = Icons.Default.ShoppingBag,
                    label = "New Order",
                    tint = PrimaryTeal,
                    onClick = onAddOrder
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
