package com.example.data.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:mariadb://localhost:3306/ktor_db"
            driverClassName = "org.mariadb.jdbc.Driver"
            username = System.getenv("DB_USER") ?: "root"
            password = System.getenv("DB_PASSWORD") ?: "password"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        transaction {
            // Crea las tablas si no existen, sin eliminar las que ya están
            SchemaUtils.createMissingTablesAndColumns(UsersTable, ItemsTable)
        }
    }
}
