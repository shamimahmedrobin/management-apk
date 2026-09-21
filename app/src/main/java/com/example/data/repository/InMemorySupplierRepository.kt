package com.example.data.repository

import com.example.data.sample.SampleDataProvider
import com.example.domain.model.Supplier
import com.example.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemorySupplierRepository : SupplierRepository {

    private val _suppliers = MutableStateFlow<List<Supplier>>(
        SampleDataProvider.getSampleSuppliers()
    )

    override fun getAllSuppliers(): Flow<List<Supplier>> = _suppliers.asStateFlow()

    override suspend fun getSupplierById(id: String): Supplier? {
        return _suppliers.value.firstOrNull { it.id == id }
    }

    override suspend fun addSupplier(supplier: Supplier): Result<Unit> {
        _suppliers.value = _suppliers.value + supplier
        return Result.success(Unit)
    }

    override suspend fun updateSupplier(supplier: Supplier): Result<Unit> {
        _suppliers.value = _suppliers.value.map { if (it.id == supplier.id) supplier else it }
        return Result.success(Unit)
    }
}
