package com.mambobryan.data.tables.topic

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object TopicsTable : IntIdTable() {

    val createdAt = datetime("topic_created_at")
    val updatedAt = datetime("topic_updated_at").nullable()
    val name = varchar("topic_name", 255).uniqueIndex()
    val color = varchar("topic_color", 255).uniqueIndex()

}

data class Topic(
    val id: Int,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val name: String,
    val color: String,
)

internal fun ResultRow?.toTopic(): Topic? {
    if (this == null) return null
    return Topic(
        id = this[TopicsTable.id].value,
        createdAt = this[TopicsTable.createdAt],
        updatedAt = this[TopicsTable.updatedAt],
        name = this[TopicsTable.name],
        color = this[TopicsTable.color],
    )
}

internal fun Query?.toTopicList(): List<Topic?> {
    if (this == null || this.empty()) return emptyList()
    return this.map { it.toTopic() }
}