package com.example.presentation.reports

import androidx.compose.foundation.background
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
import com.example.domain.model.CategoryExpenseItem
import java.util.Locale
import com.example.domain.model.CourierSummary
import com.example.domain.model.ReportPeriod
import com.example.presentation.common.InteractiveFinancialChart
import com.example.presentation.common.SectionHeader
import com.example.presentation.common.StatCard
import com.example.presentation.viewmodel.StyleSphereViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: StyleSphereViewModel
) {
    val context = LocalContext.current
    val summary by viewModel.reportFinancialSummary.collectAsState()
    val currentPeriod by viewModel.reportPeriod.collectAsState()
    val categoryExpenses by viewModel.categoryExpenses.collectAsState()
    val couriers by viewModel.courierSummaries.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Financial Analytics & Reports",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showExportDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export", fontWeight = FontWeight.Bold)
                    }
                }
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
            // Period Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportPeriod.values().forEach { period ->
                        FilterChip(
                            selected = currentPeriod == period,
                            onClick = { viewModel.setReportPeriod(period) },
                            label = { Text(period.displayName) },
                            leadingIcon = if (currentPeriod == period) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Key Financial Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Income",
                        value = CurrencyFormatter.formatBDT(summary.totalIncome),
                        icon = Icons.Default.ArrowDownward,
                        accentColor = ProfitGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Expense",
                        value = CurrencyFormatter.formatBDT(summary.totalExpense),
                        icon = Icons.Default.ArrowUpward,
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
                        title = "Net Business Profit",
                        value = CurrencyFormatter.formatBDT(summary.netProfit),
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = if (summary.netProfit >= 0) ProfitGreen else ExpenseRed,
                        subValue = "Margin: ${String.format(Locale.US, "%.1f", summary.profitMarginPercent)}%",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Avg Order Value",
                        value = CurrencyFormatter.formatBDT(summary.averageOrderValue),
                        icon = Icons.Default.Analytics,
                        accentColor = PrimaryTeal,
                        subValue = "Profit/Ord: ${CurrencyFormatter.formatBDT(summary.averageProfitPerOrder)}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interactive Flow Chart
            item {
                SectionHeader(title = "Flow & Cash Spread")
                InteractiveFinancialChart(
                    income = summary.totalIncome,
                    expense = summary.totalExpense,
                    profit = summary.netProfit
                )
            }

            // Profit & Cost Analytics Breakdown
            item {
                SectionHeader(title = "Profit & Cost Structure")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CostBreakdownRow(label = "Gross Sales", amount = summary.grossSales, color = ProfitGreen)
                        CostBreakdownRow(label = "Product Purchasing Cost", amount = summary.productCost, color = ExpenseRed)
                        CostBreakdownRow(label = "Delivery & Courier Costs", amount = summary.deliveryCost, color = ExpenseRed)
                        CostBreakdownRow(label = "Digital Advertising (Meta/Google)", amount = summary.advertisingCost, color = ExpenseRed)
                        CostBreakdownRow(label = "Operational & Other Expenses", amount = summary.otherExpenses, color = ExpenseRed)
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Net Retained Profit",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = CurrencyFormatter.formatBDT(summary.netProfit),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (summary.netProfit >= 0) ProfitGreen else ExpenseRed
                            )
                        }
                    }
                }
            }

            // Order Deliveries & Conversion Status
            item {
                SectionHeader(title = "Order Fulfillment & Status Ratio")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            OrderStatusMetric(label = "Total Orders", count = summary.totalOrders, color = PrimaryTeal)
                            OrderStatusMetric(label = "Delivered", count = summary.deliveredOrders, color = ProfitGreen)
                            OrderStatusMetric(label = "Cancelled", count = summary.cancelledOrders, color = ExpenseRed)
                            OrderStatusMetric(label = "Returned", count = summary.returnedOrders, color = WarningAmber)
                        }
                    }
                }
            }

            // Expense Breakdown by Category
            item {
                SectionHeader(title = "Expense by Category Breakdown")
            }

            if (categoryExpenses.isEmpty()) {
                item {
                    Text(
                        text = "No expenses recorded in this period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(categoryExpenses, key = { it.categoryName }) { cat ->
                    CategoryExpenseCard(item = cat)
                }
            }

            // Courier Performance Summary
            item {
                SectionHeader(title = "Courier & COD Performance")
            }

            items(couriers, key = { it.courierOption.name }) { courier ->
                CourierSummaryCard(courier = courier)
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Export Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Financial Reports") },
                text = {
                    Column {
                        Text(
                            text = "Choose your desired export format for StyleSphere business records:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                showExportDialog = false
                                viewModel.exportPdfReport(context, currentPeriod)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ExpenseRed)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Export PDF Report (Printable)")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                showExportDialog = false
                                viewModel.exportExcelReport(context)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = ProfitGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Export Excel (.xlsx) 8-Sheet Workbook")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
private fun CostBreakdownRow(label: String, amount: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = CurrencyFormatter.formatBDT(amount),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun OrderStatusMetric(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryExpenseCard(item: CategoryExpenseItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.categoryName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = CurrencyFormatter.formatBDT(item.amount),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ExpenseRed
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.transactionCount} transactions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", item.percentage)}% of total",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (item.percentage / 100.0).toFloat().coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(ExpenseRed)
                )
            }
        }
    }
}

@Composable
private fun CourierSummaryCard(courier: CourierSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = courier.courierOption.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CourierIndigo.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${courier.totalParcels} Parcels",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CourierIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total COD:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBDT(courier.totalCodAmount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Column {
                    Text("Received:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBDT(courier.receivedAmount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ProfitGreen)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pending Collection:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBDT(courier.pendingCollection), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = WarningAmber)
                }
            }
        }
    }
}
