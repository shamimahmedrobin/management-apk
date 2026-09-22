package com.example.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.utils.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDatePickerField(
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Transaction Date (তারিখ)"
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePickerDialog = true },
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                val calToday = Calendar.getInstance()
                val calSelected = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                val isToday = calToday.get(Calendar.YEAR) == calSelected.get(Calendar.YEAR) &&
                        calToday.get(Calendar.DAY_OF_YEAR) == calSelected.get(Calendar.DAY_OF_YEAR)

                val dateDisplayText = if (isToday) {
                    "Today (${DateUtils.formatDate(selectedDateMillis)})"
                } else {
                    DateUtils.formatDate(selectedDateMillis)
                }

                Text(
                    text = dateDisplayText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { showDatePickerDialog = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            // Preserve current time-of-day so ordering is sensible
                            val calCurrent = Calendar.getInstance()
                            val calSelected = Calendar.getInstance().apply {
                                timeInMillis = selectedMillis
                                set(Calendar.HOUR_OF_DAY, calCurrent.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, calCurrent.get(Calendar.MINUTE))
                                set(Calendar.SECOND, calCurrent.get(Calendar.SECOND))
                            }
                            onDateSelected(calSelected.timeInMillis)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
