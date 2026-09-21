package com.example.domain.repository

import com.example.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    suspend fun getAccountById(id: String): Account?
    suspend fun addAccount(account: Account): Result<Unit>
    suspend fun updateBalances(
        transactions: List<com.example.domain.model.Transaction>
    ): Result<Unit>
    suspend fun updateAccount(account: Account): Result<Unit>
}
