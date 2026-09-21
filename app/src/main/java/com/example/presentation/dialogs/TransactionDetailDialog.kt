package com.example.presentation.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.core.utils.CurrencyFormatter
import com.example.core.utils.DateUtils
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TransferBlue

@Composable
fun TransactionDetailDialog(
    transaction: Transaction,
    onDismiss: () -> Unit
) {
    val (typeColor, prefix) = when (transaction.type) {
        TransactionType.INCOME -> Pair(ProfitGreen, "+")
        TransactionType.EXPENSE -> Pair(ExpenseRed, "-")
        TransactionType.TRANSFER -> Pair(TransferBlue, "")
    }

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
                    Text(
                        text = "Transaction Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Header Box
                Surface(
                    color = typeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = transaction.type.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = typeColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$prefix${CurrencyFormatter.formatBDT(transaction.amount, includeDecimals = true)}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = typeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Rows
                DetailRow(label = "Transaction ID", value = transaction.id)
                DetailRow(label = "Category", value = transaction.categoryName)
                DetailRow(label = "Date & Time", value = "${DateUtils.formatDate(transaction.dateMillis)} • ${transaction.timeString}")
                DetailRow(label = "Primary Account", value = transaction.accountId.removePrefix("acc_").uppercase())
                if (transaction.destinationAccountId != null) {
                    DetailRow(label = "Destination Account", value = transaction.destinationAccountId.removePrefix("acc_").uppercase())
                }
                if (transaction.reference.isNotBlank()) {
                    DetailRow(label = "Reference / TrxID", value = transaction.reference)
                }
                if (!transaction.orderId.isNullOrBlank()) {
                    DetailRow(label = "Order ID", value = transaction.orderId)
                }
                if (!transaction.customerName.isNullOrBlank()) {
                    DetailRow(label = "Customer", value = transaction.customerName)
                }
                if (transaction.description.isNotBlank()) {
                    DetailRow(label = "Description", value = transaction.description)
                }
                if (transaction.notes.isNotBlank()) {
                    DetailRow(label = "Notes", value = transaction.notes)
                }
                if (transaction.receiptAttachmentUri != null) {
                    DetailRow(label = "Receipt Attached", value = transaction.receiptAttachmentUri)
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

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
}
