package com.example.data.repository

import com.example.data.local.dao.AccountDao
import com.example.data.local.entity.AccountEntity
import com.example.domain.model.Account
import com.example.domain.model.Transaction
import com.example.domain.repository.AccountRepository
import com.example.domain.usecase.FinancialCalculationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class RoomAccountRepository(
    private val accountDao: AccountDao,
    private val calculationUseCase: FinancialCalculationUseCase = FinancialCalculationUseCase()
) : AccountRepository {

    override fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAccountById(id: String): Account? {
        return accountDao.getAccountById(id)?.toDomain()
    }

    override suspend fun addAccount(account: Account): Result<Unit> {
        return try {
            accountDao.insertAccount(AccountEntity.fromDomain(account))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBalances(transactions: List<Transaction>): Result<Unit> {
        return try {
            val entities = accountDao.getAllAccounts().firstOrNull() ?: emptyList()
            val currentAccounts = entities.map { it.toDomain() }
            val updated = calculationUseCase.calculateAccountBalances(currentAccounts, transactions)
            accountDao.insertAccounts(updated.map { AccountEntity.fromDomain(it) })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAccount(account: Account): Result<Unit> {
        return try {
            accountDao.updateAccount(AccountEntity.fromDomain(account))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
