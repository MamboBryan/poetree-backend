package com.mambobryan.data.tables.poem

import com.mambobryan.data.tables.user.UserEntity
import com.mambobryan.data.tables.user.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.*

object PoemLikesTable : UUIDTable() {

    val createdAt = datetime("created_at")
    val poemId = reference("poem_id", PoemsTable)
    val userId = reference("user_id", UsersTable)

}

class PoemLikeEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    companion object : UUIDEntityClass<PoemLikeEntity>(PoemLikesTable)

    var createdAt by PoemLikesTable.createdAt
    var poem by PoemEntity referencedOn PoemLikesTable.poemId
    var user by UserEntity referencedOn PoemLikesTable.userId

}

object UserPoemLikeEntity : Table() {

    val poem = reference("poem", PoemsTable)
    val user = reference("user", UsersTable)
    val like = reference("like", PoemLikesTable)

    override val primaryKey: PrimaryKey = PrimaryKey(poem, user, like)

}

data class PoemLike(
    val id: UUID, val createdAt: LocalDateTime, val poemId: UUID, val userId: UUID
)

internal fun ResultRow?.toPoemLike(): PoemLike? {
    if (this == null) return null
    return try {
        PoemLike(
            id = this[PoemLikesTable.id].value,
            createdAt = this[PoemLikesTable.createdAt],
            userId = this[PoemLikesTable.userId].value,
            poemId = this[PoemLikesTable.poemId].value,
        )
    } catch (e: Exception) {
        println("Row To Like -> ${e.localizedMessage}")
        null
    }
}




