package com.example.data.repositories

import com.example.domain.models.Item
import com.example.domain.repositories.ItemRepository
import com.example.data.db.ItemsTable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ItemRepositoryImpl : ItemRepository {
    override suspend fun createItem(item: Item): Int = transaction {
        ItemsTable.insertAndGetId { row ->
            row[title] = item.title
            row[description] = item.description
            row[weight] = item.weight
            row[image] = item.image
            row[userId] = item.userId
        }.value
    }

    override suspend fun getAllItems(): List<Item> = transaction {
        ItemsTable.selectAll().map {
            Item(
                id = it[ItemsTable.id].value,
                title = it[ItemsTable.title],
                description = it[ItemsTable.description],
                weight = it[ItemsTable.weight],
                image = it[ItemsTable.image],
                userId = it[ItemsTable.userId].value
            )
        }
    }

    override suspend fun getItemById(id: Int): Item? = transaction {
        ItemsTable.select { ItemsTable.id eq id }
            .map {
                Item(
                    id = it[ItemsTable.id].value,
                    title = it[ItemsTable.title],
                    description = it[ItemsTable.description],
                    weight = it[ItemsTable.weight],
                    image = it[ItemsTable.image],
                    userId = it[ItemsTable.userId].value
                )
            }
            .singleOrNull()
    }

    override suspend fun updateItem(id: Int, item: Item): Boolean = transaction {
        ItemsTable.update({ ItemsTable.id eq id }) { row ->
            row[title] = item.title
            row[description] = item.description
            row[weight] = item.weight
            row[image] = item.image
            row[userId] = item.userId
        } > 0
    }

    override suspend fun deleteItem(id: Int): Boolean = transaction {
        ItemsTable.deleteWhere { ItemsTable.id eq id } > 0
    }
}
