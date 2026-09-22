package com.example.presentation.more

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.SecurityManager
import com.example.core.utils.CurrencyFormatter
import java.util.Locale
import com.example.domain.model.Account
import com.example.domain.model.Customer
import com.example.domain.model.Product
import com.example.domain.model.Supplier
import com.example.presentation.common.SectionHeader
import com.example.presentation.viewmodel.StyleSphereViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: StyleSphereViewModel,
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val couriers by viewModel.courierSummaries.collectAsState()

    var activeSubSection by remember { mutableStateOf(0) } // 0: Accounts, 1: Inventory, 2: Contacts, 3: Couriers
    val subSectionTitles = listOf("Accounts", "Inventory", "Contacts", "Couriers")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Business Hub",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Navigation Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subSectionTitles.forEachIndexed { index, title ->
                    FilterChip(
                        selected = activeSubSection == index,
                        onClick = { activeSubSection = index },
                        label = { Text(title, fontWeight = if (activeSubSection == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            when (activeSubSection) {
                0 -> AccountsView(accounts = accounts)
                1 -> InventoryView(products = products)
                2 -> ContactsView(customers = customers, suppliers = suppliers)
                3 -> CouriersView(couriers = couriers)
            }
        }
    }
}

@Composable
private fun AccountsView(accounts: List<Account>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Accounts & Payment Wallets",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Manage multi-channel liquidity across mobile financial services, bank deposits, and cash in hand.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(accounts, key = { it.id }) { acc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(acc.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(acc.type.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = CurrencyFormatter.formatBDT(acc.currentBalance),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Money In", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.formatBDT(acc.totalMoneyIn), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ProfitGreen)
                        }
                        Column {
                            Text("Total Money Out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.formatBDT(acc.totalMoneyOut), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ExpenseRed)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Transactions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${acc.transactionCount} records", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryView(products: List<Product>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Product Catalog & Stock Management", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Tracks current stock, reorder levels, purchase cost vs retail selling price, and profit margins.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(products, key = { it.id }) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("SKU: ${product.sku} • Category: ${product.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (product.currentStock <= product.minimumStock) ExpenseRed.copy(alpha = 0.12f) else ProfitGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${product.currentStock} in stock",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (product.currentStock <= product.minimumStock) ExpenseRed else ProfitGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cost: ${CurrencyFormatter.formatBDT(product.purchaseCost)}", style = MaterialTheme.typography.bodySmall)
                        Text("Price: ${CurrencyFormatter.formatBDT(product.sellingPrice)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Margin: ${String.format(Locale.US, "%.1f", product.profitMarginPercent)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactsView(customers: List<Customer>, suppliers: List<Supplier>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Customers (Repeat Buyers & Due Amounts)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(customers, key = { it.id }) { customer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(customer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("${customer.phone} • ${customer.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${customer.totalOrders} total orders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(CurrencyFormatter.formatBDT(customer.totalPurchase), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        if (customer.dueAmount > 0) {
                            Text("Due: ${CurrencyFormatter.formatBDT(customer.dueAmount)}", style = MaterialTheme.typography.labelSmall, color = ExpenseRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Wholesale Suppliers", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(suppliers, key = { it.id }) { sup ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(sup.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("${sup.phone} • ${sup.contactPerson}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Paid: ${CurrencyFormatter.formatBDT(sup.paidAmount)}", style = MaterialTheme.typography.bodySmall, color = ProfitGreen)
                        if (sup.dueAmount > 0) {
                            Text("Due: ${CurrencyFormatter.formatBDT(sup.dueAmount)}", style = MaterialTheme.typography.labelSmall, color = ExpenseRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CouriersView(couriers: List<com.example.domain.model.CourierSummary>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Courier Operations & COD Logistics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Track parcel deliveries, returns, collected Cash-on-Delivery, and pending courier settlements.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(couriers, key = { it.courierOption.name }) { courier ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(courier.courierOption.displayName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("${courier.totalParcels} Parcels Shipped", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivered: ${courier.delivered}", style = MaterialTheme.typography.bodySmall, color = ProfitGreen, fontWeight = FontWeight.Bold)
                        Text("Cancelled/Ret: ${courier.cancelled + courier.returned}", style = MaterialTheme.typography.bodySmall, color = ExpenseRed)
                        Text("Pending: ${courier.pending}", style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total COD: ${CurrencyFormatter.formatBDT(courier.totalCodAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        Text("Pending: ${CurrencyFormatter.formatBDT(courier.pendingCollection)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarningAmber)
                    }
                }
            }
        }
    }
}
