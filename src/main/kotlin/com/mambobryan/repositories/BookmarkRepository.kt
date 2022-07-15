package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.tables.poem.BookmarksTable
import com.mambobryan.data.tables.poem.toBookmark
import com.mambobryan.utils.defaultOkResponse
import com.mambobryan.utils.serverErrorResponse
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime
import java.util.*

class BookmarkRepository {

    suspend fun create(userId: UUID, poemId: UUID): ServerResponse<out Any?> {

        var statement: InsertStatement<Number>? = null

        return try {

            val exists = BookmarksTable.select { BookmarksTable.userId eq userId and (BookmarksTable.poemId eq poemId) }
                .firstOrNull() != null

            if (exists) defaultOkResponse(message = "Poem already bookmarked", data = null)

            query {
                statement = BookmarksTable.insert {
                    it[BookmarksTable.createdAt] = LocalDateTime.now()
                    it[BookmarksTable.userId] = userId
                    it[BookmarksTable.poemId] = poemId
                }
            }

            defaultOkResponse(message = "bookmarked", data = statement?.resultedValues?.get(0).toBookmark())

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

    suspend fun delete(userId: UUID, poemId: UUID) = query {
        try {

            val result =
                BookmarksTable.deleteWhere { BookmarksTable.userId eq userId and (BookmarksTable.poemId eq poemId) }
            defaultOkResponse(message = "bookmark deleted", data = result != 0)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }
    }


}