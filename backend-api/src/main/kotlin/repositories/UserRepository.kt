package com.example.repositories

import com.example.models.User
import com.example.models.UserRole
import com.example.security.PasswordHasher
import com.example.database.UsersTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserRepository {

    fun createUser(
        name: String,
        surname: String,
        email: String,
        password: String,
        role: UserRole
    ): Int {
        return transaction {

            val passwordHash = PasswordHasher.hash(password)

            UsersTable.insert {
                it[UsersTable.name] = name
                it[UsersTable.surname] = surname
                it[UsersTable.email] = email
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.role] = role.name
            } get UsersTable.id
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
                        role = UserRole.valueOf(row[UsersTable.role]),
                        isActive = row[UsersTable.isActive],
                        createdAt = row[UsersTable.createdAt]
                    )
                }
                .singleOrNull()
        }
    }
    fun getAllUsers(): List<User> {
        return transaction {
            UsersTable
                .selectAll()
                .map { row ->
                    User(
                        id = row[UsersTable.id],
                        name = row[UsersTable.name],
                        surname = row[UsersTable.surname],
                        email = row[UsersTable.email],
                        passwordHash = row[UsersTable.passwordHash],
                        role = UserRole.valueOf(row[UsersTable.role]),
                        isActive = row[UsersTable.isActive],
                        createdAt = row[UsersTable.createdAt]
                    )
                }
        }
    }
    fun updateUser(id: Int, name: String, surname: String, email: String, role: UserRole,isActive: Boolean): Boolean {
        return transaction {
            UsersTable.update({ UsersTable.id eq id }) {
                it[UsersTable.name] = name
                it[UsersTable.surname] = surname
                it[UsersTable.email] = email
                it[UsersTable.role] = role.name
                it[UsersTable.isActive] = isActive
            } > 0
        }
    }
    fun setUserActiveStatus(id: Int,isActive: Boolean): Boolean {
        return transaction {
            UsersTable.update({ UsersTable.id eq id }) {
                it[UsersTable.isActive] = isActive
            } > 0
        }
    }
    fun deleteUser(id: Int): Boolean {
        return transaction {
            UsersTable.deleteWhere {
                UsersTable.id eq id
            } > 0
        }
    }
}
