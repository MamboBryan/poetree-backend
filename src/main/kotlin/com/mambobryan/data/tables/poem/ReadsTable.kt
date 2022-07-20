package com.mambobryan.data.tables.poem

import com.mambobryan.data.tables.comment.CommentsTable
import com.mambobryan.data.tables.user.UserEntity
import com.mambobryan.data.tables.user.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.*

object ReadsTable : UUIDTable() {

    val createdAt = datetime("created_at")
    val poemId = reference("poem_id", PoemsTable)
    val userId = reference("user_id", UsersTable)

}

class ReadEntity(id: EntityID<UUID>) : UUIDEntity(id){

    companion object: UUIDEntityClass<ReadEntity>(ReadsTable)

    var createdAt by ReadsTable.createdAt

    var user by UserEntity referencedOn ReadsTable.userId
    var poem by PoemEntity referencedOn ReadsTable.poemId

}

data class Read(
    val id: UUID, val createdAt: LocalDateTime, val poemId: Int, val userId: UUID
)