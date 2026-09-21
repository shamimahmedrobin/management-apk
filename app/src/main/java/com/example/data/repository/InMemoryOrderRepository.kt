package com.example.data.repository

import com.example.data.sample.SampleDataProvider
import com.example.domain.model.Order
import com.example.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class InMemoryOrderRepository : OrderRepository {

    private val _orders = MutableStateFlow<List<Order>>(
        SampleDataProvider.getSampleOrders()
    )

    override fun getAllOrders(): Flow<List<Order>> {
        return _orders.asStateFlow().map { list ->
            list.sortedByDescending { it.orderDateMillis }
        }
    }

    override suspend fun getOrderById(id: String): Order? {
        return _orders.value.firstOrNull { it.id == id }
    }

    override suspend fun addOrder(order: Order): Result<Unit> {
        _orders.value = listOf(order) + _orders.value
        return Result.success(Unit)
    }

    override suspend fun updateOrder(order: Order): Result<Unit> {
        _orders.value = _orders.value.map { if (it.id == order.id) order else it }
        return Result.success(Unit)
    }

    override suspend fun deleteOrder(id: String): Result<Unit> {
        _orders.value = _orders.value.filterNot { it.id == id }
        return Result.success(Unit)
    }
}
