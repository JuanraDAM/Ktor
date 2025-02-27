package com.example.data.repositories

import com.example.domain.models.Item
import com.example.domain.repositories.ItemRepository
import com.example.data.db.ItemsTable
import com.example.data.db.UsersTable
import com.example.presentation.routes.CreateItemRequest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ItemRepositoryImpl : ItemRepository {

    override suspend fun createItem(request: CreateItemRequest): Item = transaction {
        // Inserta el nuevo ítem en la base de datos y obtiene el ID generado
        val generatedId = ItemsTable.insertAndGetId { row ->
            row[title] = request.title
            row[description] = request.description
            row[weight] = request.weight
            row[image] = request.image
            // Usa la tabla de usuarios para construir el EntityID
            row[userId] = EntityID(request.userId, UsersTable)
        }.value

        // Devuelve el ítem creado con el ID persistido
        Item(
            id = generatedId,
            title = request.title,
            description = request.description,
            weight = request.weight,
            image = request.image,
            userId = request.userId
        )
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
            row[userId] = EntityID(item.userId, UsersTable)
        } > 0
    }

    override suspend fun deleteItem(id: Int): Boolean = transaction {
        ItemsTable.deleteWhere { ItemsTable.id eq id } > 0
    }
}
