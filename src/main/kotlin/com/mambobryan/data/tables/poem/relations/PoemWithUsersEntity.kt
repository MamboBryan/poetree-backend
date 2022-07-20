package com.mambobryan.data.tables.poem.relations

import com.mambobryan.data.tables.comment.CommentEntity
import com.mambobryan.data.tables.comment.CommentsTable
import com.mambobryan.data.tables.poem.*
import com.mambobryan.data.tables.topic.*
import com.mambobryan.data.tables.user.UserDto
import com.mambobryan.data.tables.user.UserEntity
import com.mambobryan.data.tables.user.toUser
import com.mambobryan.data.tables.user.toUserDto
import com.mambobryan.utils.toDate
import com.mambobryan.utils.toDateTimeString
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class CompletePoemEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    companion object : UUIDEntityClass<CompletePoemEntity>(PoemsTable)

    var createdAt by PoemsTable.createdAt
    var updatedAt by PoemsTable.updatedAt
    var editedAt by PoemsTable.editedAt

    var title by PoemsTable.title
    var content by PoemsTable.content
    var contentAsHtml by PoemsTable.contentAsHtml

    var user by UserEntity referencedOn PoemsTable.userId
    var topic by TopicEntity referencedOn PoemsTable.topicId

    val likes by PoemLikeEntity referrersOn PoemLikesTable.poemId
    val bookmarks by BookmarkEntity referrersOn BookmarksTable.poemId
    val comments by CommentEntity referrersOn CommentsTable.poemId
    val reads by ReadEntity referrersOn ReadsTable.poemId

}

data class CompletePoemDto(
    val id: String,
    val createdAt: String?,
    val updatedAt: String?,
    val editedAt: String?,
    val title: String?,
    val content: String?,
    val html: String?,
    val user: UserDto?,
    val topic: TopicDto?,
    val likes: Long,
    val bookmarks: Long,
    val comments: Long,
    val reads: Long,
)

fun CompletePoemEntity?.toPoemDto(): CompletePoemDto? {
    if (this == null) return null
    return try {

        val user = this.user
        val topic = this.topic

        CompletePoemDto(
            id = this.id.toString(),
            createdAt = this.createdAt.toDate().toDateTimeString(),
            updatedAt = this.updatedAt.toDate().toDateTimeString(),
            editedAt = this.editedAt.toDate().toDateTimeString(),
            title = this.title,
            content = this.content,
            html = this.contentAsHtml,
            user = user.toUser().toUserDto(),
            topic = topic.toTopic().toTopicDto(),
            likes = this.likes.count(),
            bookmarks = this.bookmarks.count(),
            comments = this.comments.count(),
            reads = this.reads.count()
        )

    } catch (e: Exception) {
        val message = "CompletePoem to PoemDto Error -> ${e.localizedMessage}"
        println(message)
        null
    }
}