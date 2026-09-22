package com.example.presentation.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.core.utils.DateUtils
import com.example.domain.model.*
import com.example.presentation.common.TransactionDatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var amountText by remember { mutableStateOf(transaction.amount.toString().removeSuffix(".0")) }
    var selectedCategory by remember { mutableStateOf(transaction.categoryName) }
    var selectedAccountId by remember { mutableStateOf(transaction.accountId) }
    var selectedDestAccountId by remember { mutableStateOf(transaction.destinationAccountId ?: accounts.getOrNull(1)?.id ?: "") }
    var referenceText by remember { mutableStateOf(transaction.reference) }
    var descriptionText by remember { mutableStateOf(transaction.description) }
    var notesText by remember { mutableStateOf(transaction.notes) }
    var customerNameText by remember { mutableStateOf(transaction.customerName ?: "") }
    var orderIdText by remember { mutableStateOf(transaction.orderId ?: "") }
    var selectedDateMillis by remember { mutableStateOf(transaction.dateMillis) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var destAccountDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit Transaction (লেনদেন এডিট)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ID: ${transaction.id} • ${transaction.type.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Date Picker (তারিখ নির্বাচন)
                TransactionDatePickerField(
                    selectedDateMillis = selectedDateMillis,
                    onDateSelected = { selectedDateMillis = it },
                    label = "Transaction Date (তারিখ)"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount (৳ BDT) *") },
                    leadingIcon = { Text("৳", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selection (for Income and Expense)
                if (transaction.type == TransactionType.INCOME) {
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Income Category *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            IncomeCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        selectedCategory = cat.displayName
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (transaction.type == TransactionType.EXPENSE) {
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Expense Category *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            ExpenseCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        selectedCategory = cat.displayName
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Primary Account
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = it }
                ) {
                    val currentAccName = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: selectedAccountId
                    OutlinedTextField(
                        value = currentAccName,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                if (transaction.type == TransactionType.TRANSFER) "From Account (Source) *"
                                else "Account *"
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${acc.type.name})") },
                                onClick = {
                                    selectedAccountId = acc.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Destination Account (for Transfer)
                if (transaction.type == TransactionType.TRANSFER) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = destAccountDropdownExpanded,
                        onExpandedChange = { destAccountDropdownExpanded = it }
                    ) {
                        val destAccName = accounts.firstOrNull { it.id == selectedDestAccountId }?.name ?: selectedDestAccountId
                        OutlinedTextField(
                            value = destAccName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To Account (Destination) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destAccountDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = destAccountDropdownExpanded,
                            onDismissRequest = { destAccountDropdownExpanded = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${acc.type.name})") },
                                    onClick = {
                                        selectedDestAccountId = acc.id
                                        destAccountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reference / TrxID
                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it },
                    label = { Text("Reference / Trx ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (transaction.type == TransactionType.INCOME) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customerNameText,
                        onValueChange = { customerNameText = it },
                        label = { Text("Customer Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = orderIdText,
                        onValueChange = { orderIdText = it },
                        label = { Text("Order ID (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Memo") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0.0) {
                                errorMessage = "Please enter a valid amount greater than 0"
                                return@Button
                            }
                            if (transaction.type == TransactionType.TRANSFER && selectedAccountId == selectedDestAccountId) {
                                errorMessage = "Source and Destination accounts cannot be the same"
                                return@Button
                            }

                            val updated = transaction.copy(
                                amount = amt,
                                dateMillis = selectedDateMillis,
                                timeString = DateUtils.formatTime(selectedDateMillis),
                                categoryName = selectedCategory,
                                accountId = selectedAccountId,
                                destinationAccountId = if (transaction.type == TransactionType.TRANSFER) selectedDestAccountId else transaction.destinationAccountId,
                                reference = referenceText.trim(),
                                description = descriptionText.trim().ifBlank {
                                    when (transaction.type) {
                                        TransactionType.INCOME -> "Income: $selectedCategory"
                                        TransactionType.EXPENSE -> "Expense: $selectedCategory"
                                        TransactionType.TRANSFER -> "Transfer"
                                    }
                                },
                                notes = notesText.trim(),
                                customerName = customerNameText.trim().ifBlank { null },
                                orderId = orderIdText.trim().ifBlank { null }
                            )

                            onSave(updated)
                            onDismiss()
                        }
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
