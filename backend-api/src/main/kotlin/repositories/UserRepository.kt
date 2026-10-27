package com.example.repositories

import com.example.models.User
import com.example.database.UsersTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserRepository {

    fun findById(id: Int): User? = transaction {
        UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()?.let { row ->
            User(
                id = row[UsersTable.id],
                name = row[UsersTable.name],
                surname = row[UsersTable.surname],
                email = row[UsersTable.email],
                passwordHash = row[UsersTable.passwordHash],
                createdAt = row[UsersTable.createdAt]
            )
        }
    }

    fun findByEmail(email: String): User? {
        return transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.email eq email }
                .map { row ->
                    User(
                        id = row[UsersTable.id],
                        name = row[UsersTable.name],
                        surname = row[UsersTable.surname],
                        email = row[UsersTable.email],
                        passwordHash = row[UsersTable.passwordHash],
                        createdAt = row[UsersTable.createdAt]
                    )
                }
                .singleOrNull()
        }
    }
}
