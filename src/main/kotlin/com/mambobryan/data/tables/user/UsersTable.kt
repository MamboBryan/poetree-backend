package com.mambobryan.data.tables.user

import com.mambobryan.data.tables.topic.Topic
import com.mambobryan.utils.*
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

object UsersTable : UUIDTable() {

    val userCreatedAt = datetime("user_created_at")
    val userUpdatedAt = datetime("user_updated_at").nullable()
    val userSetupAt = datetime("user_setup_at").nullable()
    val userEmail = varchar("user_email", 50).uniqueIndex()
    val userHash = varchar("user_hash", 128)
    val userName = varchar("user_name", 50).nullable()
    val userImage = varchar("user_image", 50).nullable()
    val userBio = text("user_bio").nullable()
    val userDateOfBirth = date("user_dob").nullable()
    val userGender = integer("user_gender").nullable()
    val userToken = text("user_token").nullable()

}

data class User(
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val setupAt: LocalDateTime?,
    val id: UUID?,
    val email: String,
    @Exclude val hash: String,
    val username: String?,
    val imageUrl: String?,
    val bio: String?,
    val dateOfBirth: LocalDate?,
    val gender: Int?
)

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    companion object : UUIDEntityClass<UserEntity>(UsersTable)

    var createdAt by UsersTable.userCreatedAt
    var updatedAt by UsersTable.userUpdatedAt
    var setupAt by UsersTable.userSetupAt
    var name by UsersTable.userName
    var image by UsersTable.userImage
    var bio by UsersTable.userBio
    var dateOfBirth by UsersTable.userDateOfBirth
    var gender by UsersTable.userGender
    var email by UsersTable.userEmail
    var hash by UsersTable.userHash


}

data class UserDto(
    val id: String,
    val createdAt: String?,
    val updatedAt: String?,
    val name: String?,
    val image: String?,
    val bio: String?,
    val dateOfBirth: String?,
    val gender: Int?,
)

data class CompleteUserDto(
    val id: String,
    val createdAt: String?,
    val updatedAt: String?,
    val name: String?,
    val image: String?,
    val bio: String?,
    val dateOfBirth: String?,
    val gender: Int?,
    val reads: Long?,
    val likes: Long?,
    val bookmarks: Long?
)

data class UserMinimalDTO(
    val id: String,
    val createdAt: String?,
    val name: String?,
    val image: String?,
)

fun UserEntity?.toUser(): User? {
    if (this == null) return null
    return try {
        User(
            id = this.id.value,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            username = this.name,
            imageUrl = this.image,
            bio = this.bio,
            dateOfBirth = this.dateOfBirth,
            gender = this.gender,
            setupAt = this.setupAt,
            email = this.email,
            hash = this.hash
        )

    } catch (e: Exception) {
        val message = "UserEntity to User Error -> ${e.localizedMessage}"
        println(message)
        null
    }
}

fun User?.toMinimalUserDto(): UserMinimalDTO? {
    if (this == null) return null
    return try {
        UserMinimalDTO(
            id = this.id.toString(),
            createdAt = this.createdAt.toDate().toDateTimeString(),
            name = this.username,
            image = this.imageUrl
        )
    } catch (e: Exception) {
        println(e.localizedMessage)
        null
    }
}

fun User?.toUserDto(): UserDto? {
    if (this == null) return null
    return try {
        UserDto(
            id = this.id.toString(),
            createdAt = this.createdAt.toDate().toDateTimeString(),
            updatedAt = this.updatedAt.toDate().toDateTimeString(),
            name = this.username,
            image = this.imageUrl,
            bio = this.bio,
            dateOfBirth = this.dateOfBirth.toDate().toDateString(),
            gender = this.gender
        )
    } catch (e: Exception) {
        val message = "UserEntity to UserDTO Error -> ${e.localizedMessage}"
        println(message)
        null
    }
}

fun User?.toCompleteUserDto(reads: Long?, bookmarks: Long?, likes: Long?): CompleteUserDto? {
    if (this == null) return null
    return try {
        CompleteUserDto(
            id = this.id.toString(),
            createdAt = this.createdAt.toDate().toDateTimeString(),
            updatedAt = this.updatedAt.toDate().toDateTimeString(),
            name = this.username,
            image = this.imageUrl,
            bio = this.bio,
            dateOfBirth = this.dateOfBirth.toDate().toDateString(),
            gender = this.gender,
            likes = likes,
            reads = reads,
            bookmarks = bookmarks
        )
    } catch (e: Exception) {
        val message = "UserEntity to CompleteUserDto Error -> ${e.localizedMessage}"
        println(message)
        null
    }
}


internal fun ResultRow?.toUser(): User? {
    if (this == null) return null
    return User(
        createdAt = this[UsersTable.userCreatedAt],
        updatedAt = this[UsersTable.userUpdatedAt],
        setupAt = this[UsersTable.userSetupAt],
        id = this[UsersTable.id].value,
        email = this[UsersTable.userEmail],
        hash = this[UsersTable.userHash],
        username = this[UsersTable.userName],
        imageUrl = this[UsersTable.userImage],
        bio = this[UsersTable.userBio],
        dateOfBirth = this[UsersTable.userDateOfBirth],
        gender = this[UsersTable.userGender]
    )
}

internal fun Query?.toUserList(): List<User?> {
    if (this == null) return listOf()
    return this.map { it.toUser() }
}