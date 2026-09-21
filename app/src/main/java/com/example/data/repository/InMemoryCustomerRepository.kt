package com.example.data.repository

import com.example.data.sample.SampleDataProvider
import com.example.domain.model.Customer
import com.example.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryCustomerRepository : CustomerRepository {

    private val _customers = MutableStateFlow<List<Customer>>(
        SampleDataProvider.getSampleCustomers()
    )

    override fun getAllCustomers(): Flow<List<Customer>> = _customers.asStateFlow()

    override suspend fun getCustomerById(id: String): Customer? {
        return _customers.value.firstOrNull { it.id == id }
    }

    override suspend fun addCustomer(customer: Customer): Result<Unit> {
        _customers.value = _customers.value + customer
        return Result.success(Unit)
    }

    override suspend fun updateCustomer(customer: Customer): Result<Unit> {
        _customers.value = _customers.value.map { if (it.id == customer.id) customer else it }
        return Result.success(Unit)
    }
}
