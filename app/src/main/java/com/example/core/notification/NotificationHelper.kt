package com.example.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private const val CHANNEL_ID_BUSINESS = "stylesphere_business_reminders"
    private const val CHANNEL_NAME_BUSINESS = "Business Alerts & Reminders"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_BUSINESS,
                CHANNEL_NAME_BUSINESS,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily accounting, supplier reminders, and stock warnings"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun sendLocalAlert(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        initNotificationChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BUSINESS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            manager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Handled gracefully if permission denied
        }
    }
}
