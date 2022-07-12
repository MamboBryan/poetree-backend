package com.mambobryan.repositories

import com.mambobryan.data.models.User
import com.mambobryan.data.models.Users
import com.mambobryan.data.models.toUser
import com.mambobryan.data.query
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement

class UsersRepository {

    suspend fun getUserByEmail(email: String): User? = query {
        Users.select { Users.userEmail eq email }.map { it.toUser() }.singleOrNull()
    }

    suspend fun create(email: String, hash: String): User? {

        val now = System.currentTimeMillis()

        var statement: InsertStatement<Number>? = null

        query {
            statement = Users.insert {
                it[Users.userCreatedAt] = now
                it[Users.userUpdatedAt] = now
                it[Users.userEmail] = email
                it[Users.userHash] = hash
            }
        }

        return statement?.resultedValues?.get(0).toUser()

    }

}