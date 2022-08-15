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
