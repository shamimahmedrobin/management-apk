package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val amount: Double,
    val dateMillis: Long,
    val timeString: String,
    val categoryName: String,
    val accountId: String,
    val destinationAccountId: String? = null,
    val description: String = "",
    val reference: String = "",
    val orderId: String? = null,
    val customerName: String? = null,
    val notes: String = "",
    val receiptAttachmentUri: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(): Transaction {
        val parsedType = try {
            TransactionType.valueOf(type)
        } catch (e: Exception) {
            TransactionType.EXPENSE
        }

        return Transaction(
            id = id,
            type = parsedType,
            amount = amount,
            dateMillis = dateMillis,
            timeString = timeString,
            categoryName = categoryName,
            accountId = accountId,
            destinationAccountId = destinationAccountId,
            description = description,
            reference = reference,
            orderId = orderId,
            customerName = customerName,
            notes = notes,
            receiptAttachmentUri = receiptAttachmentUri,
            createdAtMillis = createdAtMillis
        )
    }

    companion object {
        fun fromDomain(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                type = transaction.type.name,
                amount = transaction.amount,
                dateMillis = transaction.dateMillis,
                timeString = transaction.timeString,
                categoryName = transaction.categoryName,
                accountId = transaction.accountId,
                destinationAccountId = transaction.destinationAccountId,
                description = transaction.description,
                reference = transaction.reference,
                orderId = transaction.orderId,
                customerName = transaction.customerName,
                notes = transaction.notes,
                receiptAttachmentUri = transaction.receiptAttachmentUri,
                createdAtMillis = transaction.createdAtMillis
            )
        }
    }
}
