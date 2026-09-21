package com.example.data.repository

import com.example.data.sample.SampleDataProvider
import com.example.domain.model.Product
import com.example.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryProductRepository : ProductRepository {

    private val _products = MutableStateFlow<List<Product>>(
        SampleDataProvider.getSampleProducts()
    )

    override fun getAllProducts(): Flow<List<Product>> = _products.asStateFlow()

    override suspend fun getProductById(id: String): Product? {
        return _products.value.firstOrNull { it.id == id }
    }

    override suspend fun addProduct(product: Product): Result<Unit> {
        _products.value = _products.value + product
        return Result.success(Unit)
    }

    override suspend fun updateProduct(product: Product): Result<Unit> {
        _products.value = _products.value.map { if (it.id == product.id) product else it }
        return Result.success(Unit)
    }

    override suspend fun deleteProduct(id: String): Result<Unit> {
        _products.value = _products.value.filterNot { it.id == id }
        return Result.success(Unit)
    }
}
