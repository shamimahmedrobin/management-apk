package com.example.presentation.orders

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.core.utils.CurrencyFormatter
import com.example.core.utils.DateUtils
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.presentation.common.EmptyState
import com.example.presentation.common.StatCard
import com.example.presentation.viewmodel.StyleSphereViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: StyleSphereViewModel,
    onAddNewOrderClick: () -> Unit
) {
    val orders by viewModel.filteredOrders.collectAsState()
    val allOrders by viewModel.orders.collectAsState()
    val statusFilter by viewModel.orderStatusFilter.collectAsState()
    val searchQuery by viewModel.orderSearchQuery.collectAsState()

    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

    // Quick metrics
    val totalCount = allOrders.size
    val deliveredCount = allOrders.count { it.status == OrderStatus.DELIVERED }
    val pendingCount = allOrders.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PROCESSING || it.status == OrderStatus.SHIPPED }
    val returnedCount = allOrders.count { it.status == OrderStatus.RETURNED || it.status == OrderStatus.CANCELLED }

    if (selectedOrderForDetail != null) {
        OrderDetailDialog(
            order = selectedOrderForDetail!!,
            onDismiss = { selectedOrderForDetail = null },
            onStatusChange = { newStatus ->
                viewModel.updateOrderStatus(selectedOrderForDetail!!.id, newStatus)
                selectedOrderForDetail = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Orders & Deliveries",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewOrderClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("New Order") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Total",
                    value = "$totalCount",
                    icon = Icons.Default.ShoppingBag,
                    accentColor = PrimaryTeal,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Delivered",
                    value = "$deliveredCount",
                    icon = Icons.Default.CheckCircle,
                    accentColor = ProfitGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active",
                    value = "$pendingCount",
                    icon = Icons.Default.Schedule,
                    accentColor = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Returned",
                    value = "$returnedCount",
                    icon = Icons.Default.Cancel,
                    accentColor = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setOrderSearchQuery(it) },
                placeholder = { Text("Search by customer, phone, tracking code...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setOrderSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { viewModel.setOrderStatusFilter(null) },
                    label = { Text("All (${allOrders.size})") }
                )
                OrderStatus.values().forEach { status ->
                    val count = allOrders.count { it.status == status }
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { viewModel.setOrderStatusFilter(status) },
                        label = { Text("${status.displayName} ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (orders.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.LocalShipping,
                    title = "No orders found",
                    message = "Tap 'New Order' to record customer parcels"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderItemCard(
                            order = order,
                            onClick = { selectedOrderForDetail = order }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemCard(
    order: Order,
    onClick: () -> Unit
) {
    val (statusBg, statusFg) = when (order.status) {
        OrderStatus.DELIVERED -> Pair(ProfitGreen.copy(alpha = 0.12f), ProfitGreen)
        OrderStatus.PENDING -> Pair(WarningAmber.copy(alpha = 0.15f), WarningAmber)
        OrderStatus.CONFIRMED, OrderStatus.PROCESSING -> Pair(TransferBlue.copy(alpha = 0.12f), TransferBlue)
        OrderStatus.SHIPPED -> Pair(CourierIndigo.copy(alpha = 0.12f), CourierIndigo)
        OrderStatus.CANCELLED, OrderStatus.RETURNED -> Pair(ExpenseRed.copy(alpha = 0.12f), ExpenseRed)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top: Order ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.id} • ${DateUtils.formatShortDate(order.orderDateMillis)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = order.status.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer & Phone
            Text(
                text = order.customerName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${order.phoneNumber} • ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            // Product & Financial Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.productName} (x${order.quantity})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Courier: ${order.courier.displayName} (${order.trackingCode})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatBDT(order.sellingPrice),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Profit: ${CurrencyFormatter.formatBDT(order.estimatedProfit)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (order.estimatedProfit >= 0) ProfitGreen else ExpenseRed
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailDialog(
    order: Order,
    onDismiss: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit
) {
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Order Details",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Selling Price:", style = MaterialTheme.typography.bodySmall)
                            Text(CurrencyFormatter.formatBDT(order.sellingPrice), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Product Cost:", style = MaterialTheme.typography.bodySmall)
                            Text("-${CurrencyFormatter.formatBDT(order.productCost)}", style = MaterialTheme.typography.bodySmall, color = ExpenseRed)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Cost:", style = MaterialTheme.typography.bodySmall)
                            Text("-${CurrencyFormatter.formatBDT(order.deliveryCharge)}", style = MaterialTheme.typography.bodySmall, color = ExpenseRed)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Order Profit:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyFormatter.formatBDT(order.estimatedProfit),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (order.estimatedProfit >= 0) ProfitGreen else ExpenseRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Update Order Status:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = order.status.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        OrderStatus.values().forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.displayName) },
                                onClick = {
                                    onStatusChange(st)
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}
