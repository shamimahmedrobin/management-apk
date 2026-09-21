package com.example.core.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val formatter = DecimalFormat("#,##,##0.00").apply {
        decimalFormatSymbols = decimalFormatSymbols.apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
    }

    private val compactFormatter = DecimalFormat("#,##,##0").apply {
        decimalFormatSymbols = decimalFormatSymbols.apply {
            groupingSeparator = ','
        }
    }

    fun formatBDT(amount: Double, includeDecimals: Boolean = false): String {
        val formatted = if (includeDecimals) {
            formatter.format(amount)
        } else {
            compactFormatter.format(amount)
        }
        return "৳ $formatted"
    }

    fun formatAmountOnly(amount: Double, includeDecimals: Boolean = false): String {
        return if (includeDecimals) formatter.format(amount) else compactFormatter.format(amount)
    }
}
