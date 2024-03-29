package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.tables.poem.BookmarksTable
import com.mambobryan.data.tables.poem.PoemLikesTable
import com.mambobryan.data.tables.poem.PoemsTable
import com.mambobryan.data.tables.poem.ReadsTable
import com.mambobryan.data.tables.user.*
import com.mambobryan.data.tables.user.toUser
import com.mambobryan.data.tables.user.toUserList
import com.mambobryan.utils.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
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

            val user = statement?.resultedValues?.get(0).toUser()

            defaultCreatedResponse(message = "signed up successfully", data = user)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun updatePassword(id: UUID, hash: String): ServerResponse<out Any?> = query {
        return@query try {

            UsersTable.update({ UsersTable.id eq id }) {
                it[UsersTable.userUpdatedAt] = LocalDateTime.now()
                it[UsersTable.userHash] = hash
            }

            defaultCreatedResponse(message = "password updated", data = null)

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
    ): ServerResponse<out Any?> = query {

        try {

            val now = LocalDateTime.now()
            val dobAsDate = dateOfBirth.toDate()

            if (email.isNullOrBlank().not()) {
                val exists =
                    UsersTable.select { UsersTable.userEmail eq email!! and (UsersTable.id neq id) }.empty().not()

                if (exists) return@query defaultBadRequestResponse(message = "Email already registered to another account.")
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

            val user = UsersTable.select(UsersTable.id eq id).map { it.toUser().toUserDto() }.singleOrNull()

            defaultOkResponse(message = "success", data = user)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = "Couldn't update user details")
        }

    }

    suspend fun getUser(userId: UUID, formatToDto: Boolean) = query {
        try {
            val user = UsersTable.select(UsersTable.id eq userId).map {
                if (formatToDto) it.toUser().toUserDto()
                else it.toUser()
            }.singleOrNull()
            defaultOkResponse(message = "success", data = user)
        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = "Couldn't get user")
        }
    }

    suspend fun getUserDetails(userId: UUID) = query {
        try {

            val read = Op.build { ReadsTable.userId eq UsersTable.id }
            val reads = ReadsTable.id.count()

            val like = Op.build { PoemLikesTable.userId eq UsersTable.id }
            val likes = PoemLikesTable.id.count()

            val bookmark = Op.build { BookmarksTable.userId eq UsersTable.id }
            val bookmarks = BookmarksTable.id.count()

            val columns = listOf(reads, bookmarks, likes, *UsersTable.columns.toTypedArray())

            val data =
                UsersTable.join(otherTable = ReadsTable, joinType = JoinType.LEFT, additionalConstraint = { read })
                    .join(otherTable = BookmarksTable, joinType = JoinType.LEFT, additionalConstraint = { bookmark })
                    .join(otherTable = PoemLikesTable, joinType = JoinType.LEFT, additionalConstraint = { like })
                    .slice(columns).select(UsersTable.id eq userId).groupBy(UsersTable.id).map {
                        it.toUser().toCompleteUserDto(reads = it[reads], bookmarks = it[bookmarks], likes = it[likes])
                    }.singleOrNull()

            defaultOkResponse(message = "success", data = data)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = "Couldn't get user")
        }
    }

    suspend fun getUsers(userId: UUID, query: String, page: Int = 1) = query {

        val (limit, offset) = getLimitAndOffset(page)

        val condition = Op.build {
            when (query.isBlank()) {
                true -> {
                    UsersTable.id neq userId
                }

                false -> {
                    UsersTable.id neq userId and (UsersTable.userName.lowerCase() like "%$query%".lowercase() or (UsersTable.userEmail.lowerCase() like "%$query%".lowercase()))
                }
            }
        }

        try {

            val users = UsersTable.select { condition }
                .limit(n = limit, offset = offset)
                .toUserList()
                .map { it.toMinimalUserDto() }

            val data = getPagedData(page= page, result = users)

            defaultOkResponse(message = "users got successfully", data = data)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

    suspend fun getUserByEmail(email: String) = query {

        try {

            val result = UsersTable.select { UsersTable.userEmail eq email }.map { it.toUser() }.singleOrNull()
            defaultOkResponse(message = "user got successfully", data = result)

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