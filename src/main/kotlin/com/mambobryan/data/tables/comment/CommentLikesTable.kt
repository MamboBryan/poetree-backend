package com.mambobryan.data.tables.comment

import com.mambobryan.data.tables.user.UsersTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object CommentLikesTable : UUIDTable() {

    val createdAt = datetime("comment_like_created")
    val commentId = reference("comment_id", CommentsTable)
    val userId = reference("user_id", UsersTable)

}

data class CommentLike(
    val id: UUID, val createdAt: LocalDateTime, val commentId: UUID, val userId: UUID
)

internal fun ResultRow?.toCommentLike(): CommentLike? {
    if (this == null) return null
    return CommentLike(
        id = this[CommentLikesTable.id].value,
        createdAt = this[CommentLikesTable.createdAt],
        userId = this[CommentLikesTable.userId].value,
        commentId = this[CommentLikesTable.commentId].value,
    )
}

