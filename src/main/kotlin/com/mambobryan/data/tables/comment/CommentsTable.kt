package com.mambobryan.data.tables.comment

import com.mambobryan.data.tables.poem.PoemEntity
import com.mambobryan.data.tables.poem.PoemsTable
import com.mambobryan.data.tables.user.*
import com.mambobryan.utils.toDate
import com.mambobryan.utils.toDateTimeString
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.*

object CommentsTable : UUIDTable() {

    val createdAt = datetime("comment_created_at")
    val updatedAt = datetime("comment_updated_at").nullable()
    val content = text("comment_content")

    val userId = reference("comment_user_id", UsersTable)
    val poemId = reference("comment_poem_id", PoemsTable)

}

class CommentEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    companion object : UUIDEntityClass<CommentEntity>(CommentsTable)

    var createdAt by CommentsTable.createdAt
    var updatedAt by CommentsTable.updatedAt
    var content by CommentsTable.content

    var user by UserEntity referencedOn CommentsTable.userId
    var poem by PoemEntity referencedOn CommentsTable.poemId


}

/**
 * DATA CLASSES
 */

data class Comment(
    val id: UUID,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val content: String,
    val userId: UUID,
    val poemId: UUID
)

data class CommentComplete(
    val id: UUID,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val content: String,
    val user: UserEntity,
    val poem: PoemEntity
)

data class CommentDTO(
    val id: String,
    val createdAt: String,
    val updatedAt: String?,
    val content: String,
    val user: UserMinimalDTO?,
)

data class CommentWithPoemDTO(
    val id: String,
    val createdAt: String,
    val updatedAt: String?,
    val content: String,
    val user: UserDto,
    val poem: PoemEntity,
)

/**
 * MAPPERS/CONVERTERS
 */

internal fun CommentEntity?.toCompleteComment(): CommentComplete? {
    if (this == null) return null
    return try {
        CommentComplete(
            id = this.id.value,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            content = this.content,
            user = this.user,
            poem = this.poem,
        )
    } catch (e: Exception) {
        val message = "CommentEntity to Complete Comment -> ${e.localizedMessage}"
        println(message)
        null
    }
}

internal fun CommentComplete?.toCommentDTO(): CommentDTO?{
    if (this == null) return null
    return try {
        CommentDTO(
            id = this.id.toString(),
            createdAt = this.createdAt.toDate().toDateTimeString()!!,
            updatedAt = this.updatedAt.toDate().toDateTimeString(),
            content = this.content,
            user = this.user.toUser().toMinimalUserDto()
        )
    } catch (e: Exception){
        val message = "CompleteComment to  CommentDTO -> ${e.localizedMessage}"
        println(message)
        null
    }
}

internal fun ResultRow?.toComment(): Comment? {
    if (this == null) return null
    return Comment(
        id = this[CommentsTable.id].value,
        createdAt = this[CommentsTable.createdAt],
        updatedAt = this[CommentsTable.updatedAt],
        content = this[CommentsTable.content],
        userId = this[CommentsTable.userId].value,
        poemId = this[CommentsTable.poemId].value

    )
}