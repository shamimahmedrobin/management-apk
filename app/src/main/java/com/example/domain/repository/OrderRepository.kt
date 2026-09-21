package com.example.domain.repository

import com.example.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getAllOrders(): Flow<List<Order>>
    suspend fun getOrderById(id: String): Order?
    suspend fun addOrder(order: Order): Result<Unit>
    suspend fun updateOrder(order: Order): Result<Unit>
    suspend fun deleteOrder(id: String): Result<Unit>
}
