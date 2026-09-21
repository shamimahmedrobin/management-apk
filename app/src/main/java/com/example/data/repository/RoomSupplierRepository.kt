package com.example.data.repository

import com.example.data.local.dao.SupplierDao
import com.example.data.local.entity.SupplierEntity
import com.example.domain.model.Supplier
import com.example.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSupplierRepository(
    private val supplierDao: SupplierDao
) : SupplierRepository {

    override fun getAllSuppliers(): Flow<List<Supplier>> {
        return supplierDao.getAllSuppliers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSupplierById(id: String): Supplier? {
        return supplierDao.getSupplierById(id)?.toDomain()
    }

    override suspend fun addSupplier(supplier: Supplier): Result<Unit> {
        return try {
            supplierDao.insertSupplier(SupplierEntity.fromDomain(supplier))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSupplier(supplier: Supplier): Result<Unit> {
        return try {
            supplierDao.updateSupplier(SupplierEntity.fromDomain(supplier))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
