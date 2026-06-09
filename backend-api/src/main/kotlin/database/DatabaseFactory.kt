package com.example.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseFactory {

    fun init(config: ApplicationConfig) {

        val hikariConfig = HikariConfig().apply {

            driverClassName = config
                .property("database.driver")
                .getString()

            jdbcUrl = config
                .property("database.url")
                .getString()

            username = config
                .property("database.user")
                .getString()

            password = config
                .property("database.password")
                .getString()

            maximumPoolSize = 10

            isAutoCommit = false

            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)

        Database.connect(dataSource)
    }
}
