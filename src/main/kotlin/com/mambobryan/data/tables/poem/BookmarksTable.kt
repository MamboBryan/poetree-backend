package com.mambobryan.data.tables.poem

import com.mambobryan.data.tables.topic.Topic
import com.mambobryan.data.tables.topic.TopicsTable
import com.mambobryan.data.tables.user.UsersTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object BookmarksTable : UUIDTable() {

    val createdAt = datetime("created_at")
    val poemId = reference("poem_id", PoemsTable)
    val userId = reference("user_id", UsersTable)

}

data class Bookmark(
    val id: UUID, val createdAt: LocalDateTime, val poemId: UUID, val userId: UUID
)

internal fun ResultRow?.toBookmark(): Bookmark? {
    if (this == null) return null
    return Bookmark(
        id = this[BookmarksTable.id].value,
        createdAt = this[BookmarksTable.createdAt],
        userId = this[BookmarksTable.userId].value,
        poemId = this[BookmarksTable.poemId].value,
    )
}
