package com.example.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.domain.model.CourierOption

object CourierTracker {

    /**
     * Returns tracking URL for the courier and tracking/consignment ID.
     */
    fun getTrackingUrl(courier: CourierOption, trackingCode: String): String? {
        val code = trackingCode.trim()
        if (code.isBlank()) return null
        return when (courier) {
            CourierOption.STEADFAST -> "https://steadfast.com.bd/t/$code"
            CourierOption.PATHAO -> "https://merchant.pathao.com/tracking?consignment_id=$code"
            CourierOption.CARRYBEE -> "https://carrybee.com/tracking?tracking_id=$code"
            CourierOption.REDX -> "https://redx.com.bd/track/$code"
            CourierOption.PAPERFLY -> "https://paperfly.com.bd/tracking.php?tracking_id=$code"
            CourierOption.OTHER -> null
        }
    }

    /**
     * Opens tracking URL in system browser
     */
    fun openTrackingWebsite(context: Context, courier: CourierOption, trackingCode: String) {
        val url = getTrackingUrl(courier, trackingCode)
        if (url != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No tracking URL available for ${courier.displayName}", Toast.LENGTH_SHORT).show()
        }
    }
}
