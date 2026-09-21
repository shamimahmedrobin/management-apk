package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Customer

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val address: String = "",
    val totalOrders: Int = 0,
    val totalPurchase: Double = 0.0,
    val dueAmount: Double = 0.0,
    val notes: String = ""
) {
    fun toDomain(): Customer {
        return Customer(
            id = id,
            name = name,
            phone = phone,
            address = address,
            totalOrders = totalOrders,
            totalPurchase = totalPurchase,
            dueAmount = dueAmount,
            notes = notes
        )
    }

    companion object {
        fun fromDomain(customer: Customer): CustomerEntity {
            return CustomerEntity(
                id = customer.id,
                name = customer.name,
                phone = customer.phone,
                address = customer.address,
                totalOrders = customer.totalOrders,
                totalPurchase = customer.totalPurchase,
                dueAmount = customer.dueAmount,
                notes = customer.notes
            )
        }
    }
}
