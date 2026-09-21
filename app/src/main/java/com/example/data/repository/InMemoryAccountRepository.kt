package com.example.data.repository

import com.example.data.sample.SampleDataProvider
import com.example.domain.model.Account
import com.example.domain.model.Transaction
import com.example.domain.repository.AccountRepository
import com.example.domain.usecase.FinancialCalculationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAccountRepository(
    private val calculationUseCase: FinancialCalculationUseCase = FinancialCalculationUseCase()
) : AccountRepository {

    private val _accounts = MutableStateFlow<List<Account>>(
        calculationUseCase.calculateAccountBalances(
            SampleDataProvider.getDefaultAccounts(),
            SampleDataProvider.getSampleTransactions()
        )
    )

    override fun getAllAccounts(): Flow<List<Account>> = _accounts.asStateFlow()

    override suspend fun getAccountById(id: String): Account? {
        return _accounts.value.firstOrNull { it.id == id }
    }

    override suspend fun addAccount(account: Account): Result<Unit> {
        _accounts.value = _accounts.value + account
        return Result.success(Unit)
    }

    override suspend fun updateBalances(transactions: List<Transaction>): Result<Unit> {
        val updated = calculationUseCase.calculateAccountBalances(_accounts.value, transactions)
        _accounts.value = updated
        return Result.success(Unit)
    }

    override suspend fun updateAccount(account: Account): Result<Unit> {
        _accounts.value = _accounts.value.map { if (it.id == account.id) account else it }
        return Result.success(Unit)
    }
}
