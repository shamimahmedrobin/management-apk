package com.example.presentation.dialogs

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
import com.example.core.utils.CurrencyFormatter
import com.example.domain.model.CourierOption
import com.example.domain.model.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        customerName: String,
        phone: String,
        address: String,
        productName: String,
        quantity: Int,
        productCost: Double,
        sellingPrice: Double,
        deliveryCharge: Double,
        courier: CourierOption,
        paymentMethod: PaymentMethod,
        notes: String
    ) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var productCostText by remember { mutableStateOf("") }
    var sellingPriceText by remember { mutableStateOf("") }
    var deliveryChargeText by remember { mutableStateOf("80") }
    var selectedCourier by remember { mutableStateOf(CourierOption.STEADFAST) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.COD) }
    var notesText by remember { mutableStateOf("") }

    var courierExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }
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
                    Text(
                        text = "New Customer Order",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
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

                // Customer Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name *") },
                    placeholder = { Text("e.g. Shakib Al Hasan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number *") },
                    placeholder = { Text("01711-xxxxxx") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Address
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Delivery Address *") },
                    placeholder = { Text("House, Road, Area, City") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Product Name
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g. Royal Silk Panjabi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quantity & Cost
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = productCostText,
                        onValueChange = { productCostText = it },
                        label = { Text("Cost (৳)") },
                        placeholder = { Text("1500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selling Price & Delivery Charge
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("Selling Price *") },
                        placeholder = { Text("3500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deliveryChargeText,
                        onValueChange = { deliveryChargeText = it },
                        label = { Text("Delivery Charge") },
                        placeholder = { Text("80") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Courier Dropdown
                ExposedDropdownMenuBox(
                    expanded = courierExpanded,
                    onExpandedChange = { courierExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCourier.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Courier *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courierExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = courierExpanded,
                        onDismissRequest = { courierExpanded = false }
                    ) {
                        CourierOption.values().forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.displayName) },
                                onClick = {
                                    selectedCourier = c
                                    courierExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Method Dropdown
                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPaymentMethod.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentExpanded,
                        onDismissRequest = { paymentExpanded = false }
                    ) {
                        PaymentMethod.values().forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.displayName) },
                                onClick = {
                                    selectedPaymentMethod = p
                                    paymentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Order Notes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
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
                            val qty = quantityText.toIntOrNull() ?: 1
                            val cost = productCostText.toDoubleOrNull() ?: 0.0
                            val price = sellingPriceText.toDoubleOrNull() ?: 0.0
                            val delivery = deliveryChargeText.toDoubleOrNull() ?: 0.0

                            if (customerName.isBlank()) {
                                errorMessage = "Customer name is required"
                                return@Button
                            }
                            if (phoneNumber.isBlank()) {
                                errorMessage = "Phone number is required"
                                return@Button
                            }
                            if (productName.isBlank()) {
                                errorMessage = "Product name is required"
                                return@Button
                            }
                            if (price <= 0.0) {
                                errorMessage = "Selling price must be greater than 0"
                                return@Button
                            }

                            onConfirm(
                                customerName.trim(),
                                phoneNumber.trim(),
                                deliveryAddress.trim(),
                                productName.trim(),
                                qty,
                                cost,
                                price,
                                delivery,
                                selectedCourier,
                                selectedPaymentMethod,
                                notesText.trim()
                            )
                            onDismiss()
                        }
                    ) {
                        Text("Create Order")
                    }
                }
            }
        }
    }
}
