package com.mambobryan.data.tables.poem

import com.mambobryan.data.tables.user.UsersTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object PoemLikesTable : UUIDTable() {

    val createdAt = datetime("created_at")
    val poemId = reference("poem_id", PoemsTable)
    val userId = reference("user_id", UsersTable)

}

data class PoemLike(
    val id: UUID,
    val createdAt: LocalDateTime,
    val poemId: Long,
    val userId: UUID
)