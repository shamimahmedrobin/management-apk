package com.example.domain.repository

import com.example.domain.model.Supplier
import kotlinx.coroutines.flow.Flow

interface SupplierRepository {
    fun getAllSuppliers(): Flow<List<Supplier>>
    suspend fun getSupplierById(id: String): Supplier?
    suspend fun addSupplier(supplier: Supplier): Result<Unit>
    suspend fun updateSupplier(supplier: Supplier): Result<Unit>
}
