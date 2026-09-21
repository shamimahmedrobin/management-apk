package com.example.domain.repository

import com.example.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<Customer>>
    suspend fun getCustomerById(id: String): Customer?
    suspend fun addCustomer(customer: Customer): Result<Unit>
    suspend fun updateCustomer(customer: Customer): Result<Unit>
}
