package com.example.repositories

import User
import UserRole
import PasswordHasher
import com.example.database.UsersTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserRepository {

    fun createUser(
        username: String,
        password: String,
        role: UserRole
    ): Int {
        return transaction {

            val passwordHash = PasswordHasher.hash(password)

            UsersTable.insert {
                it[UsersTable.username] = username
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.role] = role.name
            } get UsersTable.id
        }
    }

    fun findByUsername(username: String): User? {
        return transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.username eq username }
                .map { row ->
                    User(
                        id = row[UsersTable.id],
                        username = row[UsersTable.username],
                        passwordHash = row[UsersTable.passwordHash],
                        role = UserRole.valueOf(row[UsersTable.role])
                    )
                }
                .singleOrNull()
        }
    }
}