package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Account
import com.example.domain.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String,
    val accountNumber: String = "",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val totalMoneyIn: Double = 0.0,
    val totalMoneyOut: Double = 0.0,
    val transactionCount: Int = 0,
    val isSystemDefault: Boolean = false
) {
    fun toDomain(): Account {
        val parsedType = try {
            AccountType.valueOf(type)
        } catch (e: Exception) {
            AccountType.CUSTOM
        }

        return Account(
            id = id,
            name = name,
            type = parsedType,
            accountNumber = accountNumber,
            openingBalance = openingBalance,
            currentBalance = currentBalance,
            totalMoneyIn = totalMoneyIn,
            totalMoneyOut = totalMoneyOut,
            transactionCount = transactionCount,
            isSystemDefault = isSystemDefault
        )
    }

    companion object {
        fun fromDomain(account: Account): AccountEntity {
            return AccountEntity(
                id = account.id,
                name = account.name,
                type = account.type.name,
                accountNumber = account.accountNumber,
                openingBalance = account.openingBalance,
                currentBalance = account.currentBalance,
                totalMoneyIn = account.totalMoneyIn,
                totalMoneyOut = account.totalMoneyOut,
                transactionCount = account.transactionCount,
                isSystemDefault = account.isSystemDefault
            )
        }
    }
}
