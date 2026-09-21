package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.CourierOption
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.PaymentMethod

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val customerName: String,
    val phoneNumber: String,
    val deliveryAddress: String = "",
    val productName: String,
    val quantity: Int = 1,
    val productCost: Double,
    val sellingPrice: Double,
    val deliveryCharge: Double = 0.0,
    val courier: String,
    val paymentMethod: String,
    val orderDateMillis: Long = System.currentTimeMillis(),
    val status: String,
    val deliveryStatus: String = "In Transit",
    val trackingCode: String = "",
    val notes: String = ""
) {
    fun toDomain(): Order {
        val parsedCourier = try {
            CourierOption.valueOf(courier)
        } catch (e: Exception) {
            CourierOption.STEADFAST
        }
        val parsedPaymentMethod = try {
            PaymentMethod.valueOf(paymentMethod)
        } catch (e: Exception) {
            PaymentMethod.COD
        }
        val parsedStatus = try {
            OrderStatus.valueOf(status)
        } catch (e: Exception) {
            OrderStatus.PENDING
        }

        return Order(
            id = id,
            customerName = customerName,
            phoneNumber = phoneNumber,
            deliveryAddress = deliveryAddress,
            productName = productName,
            quantity = quantity,
            productCost = productCost,
            sellingPrice = sellingPrice,
            deliveryCharge = deliveryCharge,
            courier = parsedCourier,
            paymentMethod = parsedPaymentMethod,
            orderDateMillis = orderDateMillis,
            status = parsedStatus,
            deliveryStatus = deliveryStatus,
            trackingCode = trackingCode,
            notes = notes
        )
    }

    companion object {
        fun fromDomain(order: Order): OrderEntity {
            return OrderEntity(
                id = order.id,
                customerName = order.customerName,
                phoneNumber = order.phoneNumber,
                deliveryAddress = order.deliveryAddress,
                productName = order.productName,
                quantity = order.quantity,
                productCost = order.productCost,
                sellingPrice = order.sellingPrice,
                deliveryCharge = order.deliveryCharge,
                courier = order.courier.name,
                paymentMethod = order.paymentMethod.name,
                orderDateMillis = order.orderDateMillis,
                status = order.status.name,
                deliveryStatus = order.deliveryStatus,
                trackingCode = order.trackingCode,
                notes = order.notes
            )
        }
    }
}
