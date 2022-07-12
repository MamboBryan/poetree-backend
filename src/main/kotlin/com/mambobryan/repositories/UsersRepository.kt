package com.mambobryan.repositories

import com.mambobryan.data.models.User
import com.mambobryan.data.models.Users
import com.mambobryan.data.models.toUser
import com.mambobryan.data.query
import com.mambobryan.utils.toDateLong
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

    suspend fun update(
        id: Int,
        email: String?,
        username: String?,
        bio: String?,
        dateOfBirth: String?,
        gender: Int?,
        imageUrl: String? = null,
        token: String? = null
    ): User? = query {

        val now = System.currentTimeMillis()
        val dobAsLong = dateOfBirth.toDateLong()

        if (email.isNullOrBlank().not()) {
            val exists = Users.select { Users.userEmail eq email!! }.firstOrNull() != null
            if (exists) return@query null
        }

        Users.update {
            it[Users.userUpdatedAt] = now
            it[Users.userSetupAt] = now
            if (email != null) it[Users.userEmail] = email
            if (username != null) it[Users.userName] = username
            if (bio != null) it[Users.userBio] = bio
            if (dobAsLong != null) it[Users.userDateOfBirth] = dobAsLong
            if (gender != null) it[Users.userGender] = gender
            if (imageUrl != null) it[Users.userImage] = imageUrl
            if (token != null) it[Users.userToken] = token
        }

        Users.select(Users.userId eq id).map { it.toUser() }.singleOrNull()

    }

    suspend fun getUser(userId: Int): User? = query {
        Users.select(Users.userId eq userId).map { it.toUser() }.singleOrNull()
    }

    suspend fun getUsers(id: Int): List<User?> = query {
        Users.select { Users.userId neq id }.map { it.toUser() }
    }

    suspend fun getUsers(userId: Int, query: String): List<User?> = query {

        val newQuery = "%$query%"

        val condition = Op.build { Users.userEmail like newQuery or (Users.userName like newQuery) }

        Users.select { Users.userId neq userId and condition }.map { it.toUser() }

    }

    suspend fun delete(userId: Int) = query {
        val result = Users.deleteWhere { Users.userId eq userId }
        result == 0
    }

}