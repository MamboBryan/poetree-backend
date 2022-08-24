package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.tables.poem.BookmarksTable
import com.mambobryan.data.tables.poem.PoemsTable
import com.mambobryan.data.tables.poem.ReadsTable
import com.mambobryan.data.tables.poem.toBookmark
import com.mambobryan.utils.defaultCreatedResponse
import com.mambobryan.utils.defaultNotFoundResponse
import com.mambobryan.utils.defaultOkResponse
import com.mambobryan.utils.serverErrorResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime
import java.util.*

class BookmarkRepository {

    suspend fun create(userId: UUID, poemId: UUID): ServerResponse<out Any?> {

        return try {

            query { PoemsTable.select(PoemsTable.id eq poemId).firstOrNull() }
                ?: return defaultNotFoundResponse(message = "poem not found")

            val bookmarked = query {
                BookmarksTable.select { BookmarksTable.userId eq userId and (BookmarksTable.poemId eq poemId) }
                    .firstOrNull()
            }
            if (bookmarked != null) return defaultOkResponse(message = "poem already bookmarked", data = null)

            query {
                BookmarksTable.insert {
                    it[BookmarksTable.createdAt] = LocalDateTime.now()
                    it[BookmarksTable.userId] = userId
                    it[BookmarksTable.poemId] = poemId
                }
            }

            defaultCreatedResponse(message = "poem bookmarked", data = null)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

    suspend fun delete(userId: UUID, poemId: UUID) = query {
        try {

            PoemsTable.select(PoemsTable.id eq poemId).firstOrNull()
                ?: return@query defaultNotFoundResponse(message = "poem not found")

            val result =
                BookmarksTable.deleteWhere { BookmarksTable.userId eq userId and (BookmarksTable.poemId eq poemId) }
            defaultOkResponse(message = "poem bookmark deleted", data = result != 0)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }
    }


}