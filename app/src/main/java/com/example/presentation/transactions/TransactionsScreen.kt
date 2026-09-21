package com.example.presentation.transactions

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ReportPeriod
import com.example.domain.model.Transaction
import com.example.presentation.common.EmptyState
import com.example.presentation.common.TransactionItemRow
import com.example.presentation.dialogs.TransactionDetailDialog
import com.example.presentation.viewmodel.StyleSphereViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: StyleSphereViewModel,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddTransferClick: () -> Unit
) {
    val txList by viewModel.filteredTransactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val currentTab by viewModel.txTab.collectAsState()
    val currentPeriod by viewModel.txPeriod.collectAsState()
    val searchQuery by viewModel.txSearchQuery.collectAsState()
    val accountFilter by viewModel.txAccountFilter.collectAsState()

    var selectedTransactionForDetail by remember { mutableStateOf<Transaction?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    if (selectedTransactionForDetail != null) {
        TransactionDetailDialog(
            transaction = selectedTransactionForDetail!!,
            onDismiss = { selectedTransactionForDetail = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transactions Ledger",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (accountFilter != null || currentPeriod != ReportPeriod.THIS_MONTH) {
                                    Badge()
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.FilterList, contentDescription = "Filters")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddIncomeClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add Entry") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setTxSearchQuery(it) },
                placeholder = { Text("Search by ID, order, category, desc...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setTxSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Tabs: All, Income, Expense, Transfer
            val tabTitles = listOf("All", "Income", "Expense", "Transfer")
            TabRow(
                selectedTabIndex = currentTab,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = currentTab == index,
                        onClick = { viewModel.setTxTab(index) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (currentTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Period Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportPeriod.values().forEach { period ->
                    FilterChip(
                        selected = currentPeriod == period,
                        onClick = { viewModel.setTxPeriod(period) },
                        label = { Text(period.displayName) }
                    )
                }
            }

            // Active Account Filter Badge if any
            if (accountFilter != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val accName = accounts.firstOrNull { it.id == accountFilter }?.name ?: accountFilter
                    SuggestionChip(
                        onClick = { viewModel.setTxAccountFilter(null) },
                        label = { Text("Account: $accName") },
                        icon = { Icon(Icons.Default.Close, contentDescription = "Remove filter", modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transactions List
            if (txList.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No transactions found",
                    message = "Try changing search keywords or adjusting your period/category filters"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(txList, key = { it.id }) { tx ->
                        TransactionItemRow(
                            transaction = tx,
                            onClick = { selectedTransactionForDetail = tx }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Account / Category Filter Modal Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Filter Transactions by Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    FilterChip(
                        selected = accountFilter == null,
                        onClick = {
                            viewModel.setTxAccountFilter(null)
                            showFilterSheet = false
                        },
                        label = { Text("All Accounts") },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    accounts.forEach { acc ->
                        FilterChip(
                            selected = accountFilter == acc.id,
                            onClick = {
                                viewModel.setTxAccountFilter(acc.id)
                                showFilterSheet = false
                            },
                            label = { Text("${acc.name} (${acc.type.name})") },
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }
}
