package com.mambobryan.data.tables.comment

import com.mambobryan.data.tables.user.UsersTable
import com.mambobryan.data.tables.poem.PoemsTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.UUID

object CommentLikesTable : UUIDTable() {

    val createdAt = datetime("comment_like_created")
    val poemId = reference("poem_id", PoemsTable)
    val userId = reference("user_id", UsersTable)

}

data class CommentLike(
    val id: Long, val poemId: Int, val userId: UUID
)

