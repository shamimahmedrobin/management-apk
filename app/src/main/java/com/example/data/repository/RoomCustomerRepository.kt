package com.example.data.repository

import com.example.data.local.dao.CustomerDao
import com.example.data.local.entity.CustomerEntity
import com.example.domain.model.Customer
import com.example.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCustomerRepository(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCustomerById(id: String): Customer? {
        return customerDao.getCustomerById(id)?.toDomain()
    }

    override suspend fun addCustomer(customer: Customer): Result<Unit> {
        return try {
            customerDao.insertCustomer(CustomerEntity.fromDomain(customer))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCustomer(customer: Customer): Result<Unit> {
        return try {
            customerDao.updateCustomer(CustomerEntity.fromDomain(customer))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
