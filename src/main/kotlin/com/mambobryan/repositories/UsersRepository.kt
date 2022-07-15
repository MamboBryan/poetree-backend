package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.tables.user.User
import com.mambobryan.data.tables.user.UsersTable
import com.mambobryan.data.tables.user.toUser
import com.mambobryan.data.tables.user.toUserList
import com.mambobryan.utils.asLocalDate
import com.mambobryan.utils.defaultOkResponse
import com.mambobryan.utils.serverErrorResponse
import com.mambobryan.utils.toDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime
import java.util.UUID

class UsersRepository {

    suspend fun create(email: String, hash: String): ServerResponse<out Any?> {
        return try {

            var statement: InsertStatement<Number>? = null

            query {
                statement = UsersTable.insert {
                    it[UsersTable.userCreatedAt] = LocalDateTime.now()
                    it[UsersTable.userUpdatedAt] = LocalDateTime.now()
                    it[UsersTable.userEmail] = email
                    it[UsersTable.userHash] = hash
                }
            }

            defaultOkResponse(message = "signed up successfully", data = statement?.resultedValues?.get(0).toUser())

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun update(
        id: UUID,
        email: String?,
        username: String?,
        bio: String?,
        dateOfBirth: String?,
        gender: Int?,
        imageUrl: String? = null,
        token: String? = null
    ): User? = query {

        val now = LocalDateTime.now()
        val dobAsDate = dateOfBirth.toDate()

        if (email.isNullOrBlank().not()) {
            val exists = UsersTable.select { UsersTable.userEmail eq email!! }.firstOrNull() != null
            if (exists) return@query null
        }

        UsersTable.update({ UsersTable.id eq id }) {
            it[UsersTable.userUpdatedAt] = now
            it[UsersTable.userSetupAt] = now
            if (!email.isNullOrBlank()) it[UsersTable.userEmail] = email
            if (!username.isNullOrBlank()) it[UsersTable.userName] = username
            if (!bio.isNullOrBlank()) it[UsersTable.userBio] = bio
            if (dobAsDate != null) it[UsersTable.userDateOfBirth] = dobAsDate.asLocalDate()
            if (gender != null) it[UsersTable.userGender] = gender
            if (!imageUrl.isNullOrBlank()) it[UsersTable.userImage] = imageUrl
            if (!token.isNullOrBlank()) it[UsersTable.userToken] = token
        }

        UsersTable.select(UsersTable.id eq id).map { it.toUser() }.singleOrNull()

    }

    suspend fun getUser(userId: UUID) = query {
        try {
            val user = UsersTable.select(UsersTable.id eq userId).map { it.toUser() }.singleOrNull()
            defaultOkResponse(message = "success", data = user)
        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }
    }

    suspend fun getUsers(userId: UUID) = query {

        try {

            val condition = Op.build { UsersTable.id neq userId }

            val users = UsersTable.select { condition }.toUserList()
            defaultOkResponse(message = "users got successfully", data = users)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

    suspend fun getUserByEmail(email: String) = query {

        try {

            val result = UsersTable.select { UsersTable.userEmail eq email }.map { it.toUser() }.singleOrNull()
            defaultOkResponse(message = "user got successfuly", data = result)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

    suspend fun searchUsers(userId: UUID, query: String) = query {

        try {

            val condition = Op.build {
                UsersTable.id neq userId and (UsersTable.userName like "%$query%" or (UsersTable.userEmail like "%$query%"))
            }

            val users = UsersTable.select { condition }.map { it.toUser() }
            defaultOkResponse(message = "users got successfully", data = users)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

    suspend fun delete(userId: UUID) = query {

        try {

            val result = UsersTable.deleteWhere { UsersTable.id eq userId }
            defaultOkResponse(message = "user deleted", data = result != 0)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

}