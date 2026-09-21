package com.example.data.repository

import com.example.data.local.dao.OrderDao
import com.example.data.local.entity.OrderEntity
import com.example.domain.model.Order
import com.example.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomOrderRepository(
    private val orderDao: OrderDao
) : OrderRepository {

    override fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getOrderById(id: String): Order? {
        return orderDao.getOrderById(id)?.toDomain()
    }

    override suspend fun addOrder(order: Order): Result<Unit> {
        return try {
            orderDao.insertOrder(OrderEntity.fromDomain(order))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrder(order: Order): Result<Unit> {
        return try {
            orderDao.updateOrder(OrderEntity.fromDomain(order))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteOrder(id: String): Result<Unit> {
        return try {
            orderDao.deleteOrderById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
