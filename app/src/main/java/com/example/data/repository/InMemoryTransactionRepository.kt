package com.example.data.repository

import com.example.data.sample.SampleDataProvider
import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class InMemoryTransactionRepository : TransactionRepository {

    private val _transactions = MutableStateFlow<List<Transaction>>(
        SampleDataProvider.getSampleTransactions()
    )

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return _transactions.asStateFlow().map { list ->
            list.sortedByDescending { it.dateMillis }
        }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        _transactions.value = listOf(transaction) + _transactions.value
        return Result.success(Unit)
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        _transactions.value = _transactions.value.filterNot { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return _transactions.value.firstOrNull { it.id == id }
    }
}
