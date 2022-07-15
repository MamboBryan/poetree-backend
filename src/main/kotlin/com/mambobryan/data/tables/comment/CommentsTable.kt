package com.mambobryan.data.tables.comment

import com.mambobryan.data.tables.user.UsersTable
import com.mambobryan.data.tables.poem.PoemsTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.*

object CommentsTable : UUIDTable() {

    val createdAt = datetime("comment_created_at")
    val updatedAt = datetime("comment_updated_at")
    val editedAt = datetime("comment_edited_at").nullable()
    val content = text("comment_content")

    val userId = reference("comment_user_id", UsersTable)
    val poemId = reference("comment_poem_id", PoemsTable)

}

data class Comment(
    val id: UUID,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val editedAt: LocalDateTime?,
    val content: String,
    val userId: UUID,
    val poemId: UUID
)

internal fun ResultRow?.toComment(): Comment? {
    if (this == null) return null
    return Comment(
        id = this[CommentsTable.id].value,
        createdAt = this[CommentsTable.createdAt],
        updatedAt = this[CommentsTable.updatedAt],
        editedAt = this[CommentsTable.editedAt],
        content = this[CommentsTable.content],
        userId = this[CommentsTable.userId].value,
        poemId = this[CommentsTable.poemId].value

    )
}