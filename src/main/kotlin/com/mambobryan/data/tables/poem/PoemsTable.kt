package com.mambobryan.data.tables.poem

import com.mambobryan.data.tables.topic.TopicsTable
import com.mambobryan.data.tables.user.UsersTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.*

object PoemsTable : UUIDTable() {

    val createdAt = datetime("poem_created_at")
    val updatedAt = datetime("poem_updated_at")
    val editedAt = datetime("poem_edited_at").nullable()

    val title = text("poem_title")
    val content = text("poem_content")
    val contentAsHtml = text("poem_content_html")

    val userId = reference("poem_user_id", UsersTable)
    val topicId = reference("poem_topic_id", TopicsTable)

}

data class Poem(
    val id: UUID,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val editedAt: LocalDateTime?,
    val title: String,
    val content: String,
    val contentAsHtml: String,
    val userId: UUID,
    val topicId: Int
)

internal fun ResultRow?.toPoem(): Poem?{
    if(this == null) return null
    return Poem(
        id = this[PoemsTable.id].value,
        createdAt = this[PoemsTable.createdAt],
        updatedAt = this[PoemsTable.updatedAt],
        editedAt = this[PoemsTable.editedAt],
        title = this[PoemsTable.title],
        content = this[PoemsTable.content],
        contentAsHtml = this[PoemsTable.contentAsHtml],
        userId = this[PoemsTable.userId].value,
        topicId = this[PoemsTable.topicId].value,
    )
}