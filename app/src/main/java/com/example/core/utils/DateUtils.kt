package com.example.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatDateTime(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))
    }

    fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    }

    fun formatTime(millis: Long): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        return SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))
    }

    fun getRelativeDate(millis: Long): String {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }

        val isToday = today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        if (isToday) return "Today, ${formatTime(millis)}"

        today.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) return "Yesterday, ${formatTime(millis)}"

        return formatDateTime(millis)
    }

    fun getCurrentDateFormatted(): String {
        return SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }
}
