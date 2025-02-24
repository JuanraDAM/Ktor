package com.example.domain.repositories

import com.example.domain.models.Item

interface ItemRepository {
    suspend fun createItem(item: Item): Int
    suspend fun getAllItems(): List<Item>
    suspend fun getItemById(id: Int): Item?
    suspend fun updateItem(id: Int, item: Item): Boolean
    suspend fun deleteItem(id: Int): Boolean
}
