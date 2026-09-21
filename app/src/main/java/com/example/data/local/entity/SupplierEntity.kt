package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Supplier

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val address: String = "",
    val totalPurchase: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val contactPerson: String = ""
) {
    fun toDomain(): Supplier {
        return Supplier(
            id = id,
            name = name,
            phone = phone,
            address = address,
            totalPurchase = totalPurchase,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            contactPerson = contactPerson
        )
    }

    companion object {
        fun fromDomain(supplier: Supplier): SupplierEntity {
            return SupplierEntity(
                id = supplier.id,
                name = supplier.name,
                phone = supplier.phone,
                address = supplier.address,
                totalPurchase = supplier.totalPurchase,
                paidAmount = supplier.paidAmount,
                dueAmount = supplier.dueAmount,
                contactPerson = supplier.contactPerson
            )
        }
    }
}
