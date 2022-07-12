package com.mambobryan.repositories

import com.mambobryan.data.models.User
import com.mambobryan.data.models.Users
import com.mambobryan.data.models.toUser
import com.mambobryan.data.query
import com.mambobryan.utils.toDateLong
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update

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
        id: Int, email: String?, username: String?, bio: String?, dateOfBirth: String?, gender: Int?
    ): User? = query {

        val now = System.currentTimeMillis()

        val dobAsLong = dateOfBirth.toDateLong()

        Users.update {
            it[Users.userUpdatedAt] = now
            it[Users.userSetupAt] = now
            if (email != null) it[Users.userEmail] = email
            if (username != null) it[Users.userName] = username
            if (bio != null) it[Users.userBio] = bio
            if (dobAsLong != null) it[Users.userDateOfBirth] = dobAsLong
            if (gender != null) it[Users.userGender] = gender
        }

        Users.select(Users.userId eq id).map { it.toUser() }.singleOrNull()

    }

    suspend fun getUser(userId: Int): User? = query {
        Users.select(Users.userId eq userId).map { it.toUser() }.singleOrNull()
    }

}